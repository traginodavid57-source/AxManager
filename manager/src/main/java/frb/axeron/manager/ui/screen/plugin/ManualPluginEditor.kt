package frb.axeron.manager.ui.screen.plugin

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.content.edit
import frb.axeron.manager.R
import frb.axeron.manager.ai.PluginFile
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Manages storage, persistence and template data for manual plugin editing.
 */
class ManualPluginManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("manual_plugin_editor_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_DRAFT_FILES = "draft_files_json"

        val DEFAULT_MODULE_PROP = """
            id=meu_plugin_exemplo
            name=Meu Plugin de Exemplo
            version=v1.0.0
            versionCode=1
            author=Seu Nome
            description=Modelo base de plugin para AxManager com scripts de inicialização, ações manuais e instalação personalizada.
        """.trimIndent()

        val DEFAULT_CUSTOMIZE_SH = """
            #!/system/bin/sh
            # ==============================================================================
            # customize.sh - Script executado durante a instalação do plugin
            # ==============================================================================
            # Use este script para preparar o ambiente, validar arquivos e configurar permissões.
            # ${'$'}MODPATH aponta para o diretório de instalação do plugin.

            ui_print "****************************************"
            ui_print "*   Instalando Meu Plugin de Exemplo   *"
            ui_print "****************************************"

            ui_print "- Configurando permissões dos scripts..."
            # Garante que os scripts tenham permissão de execução (chmod 0755)
            set_perm "${'$'}MODPATH/service.sh" 0 0 0755
            set_perm "${'$'}MODPATH/action.sh" 0 0 0755

            # Se houver outros scripts ou binários, configure-os aqui:
            # set_perm "${'$'}MODPATH/meu_binario" 0 0 0755

            ui_print "- Verificando suporte do dispositivo..."
            API_LEVEL=${'$'}(getprop ro.build.version.sdk)
            ui_print "- Android SDK Detectado: ${'$'}API_LEVEL"

            ui_print "- Instalação concluída com sucesso!"
        """.trimIndent()

        val DEFAULT_SERVICE_SH = """
            #!/system/bin/sh
            # ==============================================================================
            # service.sh - Executado em segundo plano durante a inicialização (Boot)
            # ==============================================================================
            # Este script é iniciado automaticamente quando o serviço Axeron inicia.
            # Ideal para aplicar tweaks de kernel, governors ou variáveis de sistema.

            # Aguarda até que o sistema Android conclua a inicialização
            until [ "${'$'}(getprop sys.boot_completed)" = "1" ]; do
                sleep 2
            done

            # Aguarda mais alguns segundos para estabilização dos serviços
            sleep 5

            echo "[MeuPlugin] Aplicando configurações de inicialização..."

            # Exemplo de otimização de memória virtual (swappiness / cache)
            if [ -f /proc/sys/vm/swappiness ]; then
                echo 60 > /proc/sys/vm/swappiness 2>/dev/null
            fi

            if [ -f /proc/sys/vm/vfs_cache_pressure ]; then
                echo 100 > /proc/sys/vm/vfs_cache_pressure 2>/dev/null
            fi

            echo "[MeuPlugin] Otimizações de boot aplicadas com sucesso!"
        """.trimIndent()

        val DEFAULT_ACTION_SH = """
            #!/system/bin/sh
            # ==============================================================================
            # action.sh - Executado quando o usuário aciona o botão de 'Ação' no AxManager
            # ==============================================================================
            # Qualquer saída aqui (echo) será exibida diretamente para o usuário.

            echo "=========================================="
            echo "   Executando Ação do Plugin de Exemplo   "
            echo "=========================================="

            echo "[1/3] Sincronizando dados no disco..."
            sync

            echo "[2/3] Liberando caches de página e inodes..."
            if [ -w /proc/sys/vm/drop_caches ]; then
                echo 3 > /proc/sys/vm/drop_caches 2>/dev/null
                echo "  -> Cache de RAM liberado com sucesso!"
            else
                echo "  -> Sem permissão direta para drop_caches, tentando via trim..."
                pm trim-caches 1000M 2>/dev/null
            fi

            echo "[3/3] Informações do Dispositivo:"
            echo "  -> Dispositivo: ${'$'}(getprop ro.product.model)"
            echo "  -> Versão Android: ${'$'}(getprop ro.build.version.release)"

            echo "=========================================="
            echo "   Ação finalizada com sucesso!          "
            echo "=========================================="
        """.trimIndent()
    }

    /**
     * Returns the built-in default template files.
     */
    fun getDefaultTemplate(): List<PluginFile> {
        return listOf(
            PluginFile("module.prop", DEFAULT_MODULE_PROP),
            PluginFile("customize.sh", DEFAULT_CUSTOMIZE_SH),
            PluginFile("service.sh", DEFAULT_SERVICE_SH),
            PluginFile("action.sh", DEFAULT_ACTION_SH)
        )
    }

    /**
     * Loads the saved draft files, or initializes with the default template if empty.
     */
    fun loadDraftFiles(): List<PluginFile> {
        val rawJson = prefs.getString(PREF_DRAFT_FILES, null) ?: return getDefaultTemplate()
        return try {
            val array = JSONArray(rawJson)
            val files = mutableListOf<PluginFile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val filename = obj.optString("filename", "")
                val content = obj.optString("content", "")
                if (filename.isNotEmpty()) {
                    files.add(PluginFile(filename, content))
                }
            }
            if (files.isEmpty()) getDefaultTemplate() else files
        } catch (e: Exception) {
            getDefaultTemplate()
        }
    }

    /**
     * Saves the draft files to SharedPreferences.
     */
    fun saveDraftFiles(files: List<PluginFile>) {
        try {
            val array = JSONArray()
            for (file in files) {
                val obj = JSONObject().apply {
                    put("filename", file.filename)
                    put("content", file.content)
                }
                array.put(obj)
            }
            prefs.edit { putString(PREF_DRAFT_FILES, array.toString()) }
        } catch (_: Exception) { }
    }

    /**
     * Extracts plugin ID and Name from the module.prop file.
     */
    fun extractPluginInfo(files: List<PluginFile>): Pair<String, String> {
        var id = "meu_plugin_exemplo"
        var name = "Meu Plugin de Exemplo"
        val propFile = files.find { it.filename == "module.prop" }
        if (propFile != null) {
            for (line in propFile.content.lines()) {
                val trimmed = line.trim()
                if (trimmed.startsWith("id=")) id = trimmed.substringAfter("id=").trim()
                if (trimmed.startsWith("name=")) name = trimmed.substringAfter("name=").trim()
            }
        }
        return Pair(id.ifEmpty { "meu_plugin_exemplo" }, name.ifEmpty { "Meu Plugin de Exemplo" })
    }

    /**
     * Packages the files into a flashable ZIP.
     */
    fun exportToZip(files: List<PluginFile>, preferCache: Boolean = false): Result<File> {
        return try {
            val (id, _) = extractPluginInfo(files)
            val cleanId = id.replace(Regex("[^a-zA-Z0-9_]"), "_").ifEmpty { "ax_plugin" }

            val targetDir = if (preferCache) {
                File(context.cacheDir, "plugins").apply { mkdirs() }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = File(downloadsDir, "AxManager/plugins")
                if (!dir.exists()) dir.mkdirs()
                if (dir.canWrite()) dir else File(context.cacheDir, "plugins").apply { mkdirs() }
            }

            val zipFile = File(targetDir, "${cleanId}.zip")
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                for (file in files) {
                    zos.putNextEntry(ZipEntry(file.filename))
                    zos.write(file.content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }
            }
            Result.success(zipFile)
        } catch (e: Exception) {
            try {
                val (id, _) = extractPluginInfo(files)
                val cleanId = id.replace(Regex("[^a-zA-Z0-9_]"), "_").ifEmpty { "ax_plugin" }
                val fallbackDir = File(context.cacheDir, "plugins").apply { mkdirs() }
                val zipFile = File(fallbackDir, "${cleanId}.zip")
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    for (file in files) {
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
     * Obtains the content Uri for the ZIP file via FileProvider.
     */
    fun getZipUri(file: File): Uri {
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

/**
 * Full manual editor for creating and editing Axeron plugins.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPluginEditorBottomSheet(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    onInstallPlugin: ((Uri) -> Unit)? = null,
    initialFiles: List<PluginFile>? = null
) {
    if (!showDialog) return

    val context = LocalContext.current
    val manager = remember { ManualPluginManager(context) }
    val scope = rememberCoroutineScope()

    // File list state
    val files = remember {
        mutableStateListOf<PluginFile>().apply {
            if (initialFiles != null && initialFiles.isNotEmpty()) {
                addAll(initialFiles)
            } else {
                addAll(manager.loadDraftFiles())
            }
        }
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showAddFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var fileToDeleteIndex by remember { mutableIntStateOf(-1) }
    var showAuthInstallDialog by remember { mutableStateOf(false) }

    // Quick shell snippets/symbols for comfortable typing on mobile
    val quickSymbols = listOf("#", "$", "\"", "'", "=", "{", "}", "[", "]", "|", ">", ">>", "&&", ";", "\\", "(", ")")

    // Helper to update current file's content
    fun updateContent(newContent: String) {
        val idx = selectedIndex.coerceIn(0, files.lastIndex)
        if (idx in 0 until files.size) {
            files[idx] = files[idx].copy(content = newContent)
            // Auto-save draft on modification
            manager.saveDraftFiles(files)
        }
    }

    // Confirmation dialog: Reset to default template
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.plugin_editor_reset_template)) },
            text = { Text(stringResource(R.string.plugin_editor_reset_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        files.clear()
                        files.addAll(manager.getDefaultTemplate())
                        selectedIndex = 0
                        manager.saveDraftFiles(files)
                        Toast.makeText(context, context.getString(R.string.plugin_editor_reset_template), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.plugin_editor_reset_template))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Dialog: Add new file
    if (showAddFileDialog) {
        AlertDialog(
            onDismissRequest = { showAddFileDialog = false },
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            title = { Text(stringResource(R.string.plugin_editor_add_file)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.plugin_editor_file_name), style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = newFileNameInput,
                        onValueChange = { newFileNameInput = it.trim().replace(" ", "_") },
                        placeholder = { Text("extra.sh") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = newFileNameInput.trim()
                        if (cleanName.isNotEmpty() && files.none { it.filename == cleanName }) {
                            files.add(PluginFile(cleanName, "#!/system/bin/sh\n# $cleanName\n\n"))
                            selectedIndex = files.lastIndex
                            manager.saveDraftFiles(files)
                            newFileNameInput = ""
                            showAddFileDialog = false
                        }
                    },
                    enabled = newFileNameInput.isNotBlank() && files.none { it.filename == newFileNameInput.trim() }
                ) {
                    Text(stringResource(R.string.plugin_editor_add_file))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFileDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Dialog: Delete file confirmation
    if (showDeleteConfirmDialog && fileToDeleteIndex in files.indices) {
        val target = files[fileToDeleteIndex]
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.plugin_editor_delete_file_confirm, target.filename)) },
            confirmButton = {
                Button(
                    onClick = {
                        files.removeAt(fileToDeleteIndex)
                        if (selectedIndex >= files.size) selectedIndex = files.lastIndex.coerceAtLeast(0)
                        manager.saveDraftFiles(files)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Dialog: Authorize and Install
    if (showAuthInstallDialog) {
        val (pluginId, pluginName) = manager.extractPluginInfo(files)
        AlertDialog(
            onDismissRequest = { showAuthInstallDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text(stringResource(R.string.plugin_editor_auth_install_title), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.plugin_editor_auth_install_msg, pluginName, pluginId),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        stringResource(R.string.gemini_auth_install_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAuthInstallDialog = false
                        manager.saveDraftFiles(files)
                        val saveRes = manager.exportToZip(files)
                        if (saveRes.isSuccess) {
                            val file = saveRes.getOrNull()!!
                            val uri = manager.getZipUri(file)
                            onDismissRequest()
                            onInstallPlugin?.invoke(uri)
                        } else {
                            Toast.makeText(
                                context,
                                "Erro ao empacotar plugin: ${saveRes.exceptionOrNull()?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.gemini_auth_install_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAuthInstallDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        onDismissRequest = onDismissRequest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val (currentPluginId, currentPluginName) = manager.extractPluginInfo(files)

            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.plugin_editor_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currentPluginName ($currentPluginId)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Close button
                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            // Action Buttons Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Authorize and Install
                if (onInstallPlugin != null) {
                    Button(
                        onClick = { showAuthInstallDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Outlined.Bolt, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.gemini_auth_install_confirm), fontSize = 12.sp)
                    }
                }

                // Save Draft Button
                OutlinedButton(
                    onClick = {
                        manager.saveDraftFiles(files)
                        Toast.makeText(context, context.getString(R.string.plugin_editor_draft_saved), Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.plugin_editor_save_draft), fontSize = 12.sp)
                }

                // Export ZIP Button
                OutlinedButton(
                    onClick = {
                        val res = manager.exportToZip(files)
                        if (res.isSuccess) {
                            val f = res.getOrNull()
                            Toast.makeText(context, "ZIP salvo em Downloads/AxManager/plugins/${f?.name}!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Erro ao salvar ZIP: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.gemini_save_zip), fontSize = 12.sp)
                }

                // Reset Template Button
                OutlinedButton(
                    onClick = { showResetDialog = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.plugin_editor_reset_template), fontSize = 12.sp)
                }
            }

            // File Tabs
            if (files.isNotEmpty()) {
                val currentIdx = selectedIndex.coerceIn(0, files.lastIndex)
                val activeFile = files[currentIdx]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = currentIdx,
                        modifier = Modifier.weight(1f),
                        edgePadding = 0.dp
                    ) {
                        files.forEachIndexed { index, file ->
                            Tab(
                                selected = currentIdx == index,
                                onClick = { selectedIndex = index },
                                text = { Text(file.filename, fontSize = 12.sp) }
                            )
                        }
                    }

                    // Add new file button
                    IconButton(
                        onClick = {
                            newFileNameInput = ""
                            showAddFileDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.plugin_editor_add_file))
                    }

                    // Delete current file button (if not module.prop)
                    if (activeFile.filename != "module.prop") {
                        IconButton(
                            onClick = {
                                fileToDeleteIndex = currentIdx
                                showDeleteConfirmDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Active file stats indicator
                val lineCount = activeFile.content.lines().size
                val charCount = activeFile.content.length
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${activeFile.filename} (${lineCount} linhas, ${charCount} caracteres)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick Shell Symbols Bar for touch typing
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickSymbols) { sym ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            onClick = {
                                updateContent(activeFile.content + sym)
                            }
                        ) {
                            Text(
                                text = sym,
                                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Code Editor Field
                OutlinedTextField(
                    value = activeFile.content,
                    onValueChange = { updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
