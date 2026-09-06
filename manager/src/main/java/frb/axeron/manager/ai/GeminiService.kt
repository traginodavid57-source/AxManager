package frb.axeron.manager.ai

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class GeneratedCommand(
    val command: String,
    val explanation: String
)

data class PluginFile(
    val filename: String,
    val content: String
)

data class GeneratedPlugin(
    val pluginId: String,
    val pluginName: String,
    val files: List<PluginFile>,
    val explanation: String
)

class GeminiService(private val context: Context) {

    private val prefs = context.getSharedPreferences("gemini_ai_prefs", Context.MODE_PRIVATE)

    companion object {
        const val PREF_API_KEY = "gemini_api_key"
        const val PREF_MODEL = "gemini_model"
        const val DEFAULT_MODEL = "gemini-2.0-flash"

        val AVAILABLE_MODELS = listOf(
            "gemini-2.0-flash",
            "gemini-1.5-flash",
            "gemini-1.5-pro"
        )
    }

    var apiKey: String
        get() = prefs.getString(PREF_API_KEY, "") ?: ""
        set(value) = prefs.edit { putString(PREF_API_KEY, value.trim()) }

    var model: String
        get() = prefs.getString(PREF_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
        set(value) = prefs.edit { putString(PREF_MODEL, value.trim()) }

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    /**
     * Send a raw prompt to Gemini API
     */
    suspend fun callGemini(
        prompt: String,
        temperature: Float = 0.2f
    ): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("Chave de API do Gemini não configurada."))
        }

        try {
            val selectedModel = model
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$selectedModel:generateContent?key=$key"
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connectTimeout = 20000
                readTimeout = 40000
                doOutput = true
                doInput = true
            }

            // Build request JSON
            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            val partObj = JSONObject().apply {
                                put("text", prompt)
                            }
                            put(partObj)
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val genConfig = JSONObject().apply {
                    put("temperature", temperature)
                }
                put("generationConfig", genConfig)
            }

            // Write body
            OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                writer.write(requestJson.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use {
                    it.readText()
                }
                val respJson = JSONObject(responseText)
                val candidates = respJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    val text = parts.getJSONObject(0).getString("text")
                    Result.success(text)
                } else {
                    Result.failure(Exception("Nenhuma resposta retornada pelo Gemini."))
                }
            } else {
                val errorText = conn.errorStream?.let {
                    BufferedReader(InputStreamReader(it, "UTF-8")).use { reader -> reader.readText() }
                } ?: "HTTP $responseCode"

                val msg = try {
                    val errJson = JSONObject(errorText)
                    errJson.optJSONObject("error")?.optString("message") ?: errorText
                } catch (_: Exception) {
                    errorText
                }
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Test the provided API key
     */
    suspend fun testApiKey(testKey: String): Result<String> = withContext(Dispatchers.IO) {
        if (testKey.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Chave não pode ser vazia."))
        }
        val originalKey = apiKey
        try {
            apiKey = testKey
            val result = callGemini("Responda apenas: OK")
            if (result.isSuccess) {
                Result.success("Conexão bem-sucedida com o Gemini!")
            } else {
                result
            }
        } finally {
            if (originalKey.isNotBlank() && originalKey != testKey) {
                apiKey = originalKey
            }
        }
    }

    /**
     * Generate custom Android shell command based on user description
     */
    suspend fun generateCommand(userPrompt: String): Result<GeneratedCommand> {
        val systemPrompt = """
            Você é um assistente especialista no ecossistema Android, ADB, Axeron Manager e Shell Linux.
            O usuário deseja um comando para executar no terminal (QuickShell) do AxManager.
            
            Gere o comando mais seguro, eficiente e direto para a solicitação.
            Estruture sua resposta EXATAMENTE no seguinte formato:
            
            ### COMMAND
            [insira aqui somente a linha de comando pronta para ser executada no terminal]
            
            ### EXPLANATION
            [insira aqui uma explicação concisa em português do que o comando faz, suas flags e eventuais cuidados]
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nSolicitação do usuário: $userPrompt"
        val result = callGemini(fullPrompt, temperature = 0.2f)

        return result.map { text ->
            parseGeneratedCommand(text)
        }
    }

    private fun parseGeneratedCommand(raw: String): GeneratedCommand {
        val cmdMarker = "### COMMAND"
        val expMarker = "### EXPLANATION"

        if (raw.contains(cmdMarker) && raw.contains(expMarker)) {
            val afterCmd = raw.substringAfter(cmdMarker)
            val cmd = afterCmd.substringBefore(expMarker).trim()
                .removeSurrounding("```bash", "```")
                .removeSurrounding("```sh", "```")
                .removeSurrounding("```", "```")
                .trim()
            val exp = afterCmd.substringAfter(expMarker).trim()
            return GeneratedCommand(command = cmd, explanation = exp)
        }

        // Fallback: check for markdown code blocks
        val codeBlockRegex = Regex("```(?:bash|sh)?\\s*([\\s\\S]*?)```")
        val match = codeBlockRegex.find(raw)
        return if (match != null) {
            val cmd = match.groupValues[1].trim()
            val exp = raw.replace(match.value, "").trim()
            GeneratedCommand(command = cmd, explanation = exp)
        } else {
            GeneratedCommand(command = raw.lines().firstOrNull()?.trim() ?: raw.trim(), explanation = raw)
        }
    }

    /**
     * Generate an Axeron Plugin package files based on user description
     */
    suspend fun generatePlugin(userPrompt: String): Result<GeneratedPlugin> {
        val systemPrompt = """
            Você é um especialista no desenvolvimento de Plugins para o Axeron Manager (AxManager).
            Um Plugin do Axeron é um pacote com permissões ADB/Root contendo:
            1. module.prop (formato propriedades: id, name, version, versionCode, author, description, axeronPlugin=1000)
            2. service.sh (script opcional em shell executado em segundo plano na inicialização/ignite)
            3. action.sh (script opcional executado quando o usuário clica em Ação na interface do AxManager)
            4. customize.sh (script opcional executado durante a instalação do plugin)
            
            Gere todos os arquivos necessários para a solicitação do usuário.
            Estruture sua resposta EXATAMENTE no seguinte formato para cada arquivo:
            
            FILE: module.prop
            [conteúdo do module.prop]
            
            FILE: service.sh
            [conteúdo do service.sh]
            
            FILE: action.sh
            [conteúdo do action.sh]
            
            FILE: customize.sh
            [conteúdo do customize.sh]
            
            EXPLANATION:
            [explicação detalhada em português de como o plugin funciona e o que faz]
        """.trimIndent()

        val fullPrompt = "$systemPrompt\n\nSolicitação do usuário para o plugin: $userPrompt"
        val result = callGemini(fullPrompt, temperature = 0.3f)

        return result.map { text ->
            parseGeneratedPlugin(text)
        }
    }

    private fun parseGeneratedPlugin(raw: String): GeneratedPlugin {
        val files = mutableListOf<PluginFile>()
        val fileMarker = "FILE:"
        val expMarker = "EXPLANATION:"

        var explanation = ""
        if (raw.contains(expMarker)) {
            explanation = raw.substringAfter(expMarker).trim()
        }

        val parts = raw.substringBefore(expMarker).split(fileMarker)
        var pluginId = "my_custom_plugin"
        var pluginName = "Custom Plugin"

        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.isEmpty()) continue

            val lines = trimmed.lines()
            val filename = lines.first().trim()
            val content = lines.drop(1).joinToString("\n").trim()
                .removeSurrounding("```properties", "```")
                .removeSurrounding("```bash", "```")
                .removeSurrounding("```sh", "```")
                .removeSurrounding("```", "```")
                .trim()

            if (filename.isNotEmpty() && content.isNotEmpty()) {
                files.add(PluginFile(filename, content))
                if (filename == "module.prop") {
                    for (line in content.lines()) {
                        if (line.startsWith("id=")) pluginId = line.substringAfter("id=").trim()
                        if (line.startsWith("name=")) pluginName = line.substringAfter("name=").trim()
                    }
                }
            }
        }

        return GeneratedPlugin(
            pluginId = pluginId,
            pluginName = pluginName,
            files = files,
            explanation = explanation.ifEmpty { "Plugin gerado com sucesso pelo Gemini." }
        )
    }

    /**
     * Build and save a flashable ZIP file in Downloads/AxManager or app cache
     */
    fun savePluginToZip(plugin: GeneratedPlugin, preferCache: Boolean = false): Result<File> {
        val cleanId = plugin.pluginId.ifEmpty { "ax_plugin" }.replace(Regex("[^a-zA-Z0-9_]"), "_")
        return try {
            val baseDir = if (preferCache) {
                File(context.cacheDir, "plugins").apply { mkdirs() }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = File(downloadsDir, "AxManager/plugins")
                if (!dir.exists()) dir.mkdirs()
                if (dir.canWrite()) dir else File(context.cacheDir, "plugins").apply { mkdirs() }
            }
            val zipFile = File(baseDir, "${cleanId}.zip")

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in plugin.files) {
                    zos.putNextEntry(ZipEntry(file.filename))
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            Result.success(zipFile)
        } catch (e: Exception) {
            try {
                val fallbackDir = File(context.cacheDir, "plugins").apply { mkdirs() }
                val zipFile = File(fallbackDir, "${cleanId}.zip")
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    for (file in plugin.files) {
                        zos.putNextEntry(ZipEntry(file.filename))
                        zos.write(file.content.toByteArray(Charsets.UTF_8))
                        zos.closeEntry()
                    }
                }
                Result.success(zipFile)
            } catch (e2: Exception) {
                Result.failure(e2)
            }
        }
    }

    /**
     * Get a content Uri for the ZIP file using FileProvider
     */
    fun getPluginZipUri(file: File): Uri {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }
    }
}
