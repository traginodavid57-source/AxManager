package frb.axeron.manager.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import frb.axeron.manager.ui.util.LocaleHelper
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

data class CommandSuggestionItem(
    val templatePt: String,
    val templateEn: String = templatePt,
    val descriptionPt: String,
    val descriptionEn: String = descriptionPt,
    val complementOptions: List<String> = emptyList(),
    val category: String = "",
    val keywords: List<String> = emptyList()
) {
    fun template(isPt: Boolean): String = if (isPt) templatePt else templateEn
    fun description(isPt: Boolean): String = if (isPt) descriptionPt else descriptionEn
}

object CommandSuggestionProvider {

    private val suggestions = listOf(
        // === PM (Package Manager) ===
        CommandSuggestionItem(
            templatePt = "pm trim-caches <tamanho>",
            templateEn = "pm trim-caches <size>",
            descriptionPt = "Liberar cache de aplicativos (ex: 1000M)",
            descriptionEn = "Free up app cache storage (e.g. 1000M)",
            complementOptions = listOf("1000M", "500M", "2000M", "100M", "4000M"),
            category = "pm",
            keywords = listOf("trim", "cache", "limpar", "armazenamento", "memoria", "clean")
        ),
        CommandSuggestionItem(
            templatePt = "pm list packages",
            templateEn = "pm list packages",
            descriptionPt = "Listar todos os pacotes instalados",
            descriptionEn = "List all installed packages",
            category = "pm",
            keywords = listOf("list", "packages", "listar", "pacotes", "apps")
        ),
        CommandSuggestionItem(
            templatePt = "pm list packages -3",
            templateEn = "pm list packages -3",
            descriptionPt = "Listar aplicativos de terceiros (instalados pelo usuário)",
            descriptionEn = "List third-party installed packages",
            category = "pm",
            keywords = listOf("list", "packages", "terceiros", "user")
        ),
        CommandSuggestionItem(
            templatePt = "pm list packages -s",
            templateEn = "pm list packages -s",
            descriptionPt = "Listar aplicativos do sistema",
            descriptionEn = "List system packages",
            category = "pm",
            keywords = listOf("list", "packages", "sistema", "system")
        ),
        CommandSuggestionItem(
            templatePt = "pm list packages -d",
            templateEn = "pm list packages -d",
            descriptionPt = "Listar aplicativos desativados",
            descriptionEn = "List disabled packages",
            category = "pm",
            keywords = listOf("list", "disabled", "desativados")
        ),
        CommandSuggestionItem(
            templatePt = "pm clear <pacote>",
            templateEn = "pm clear <package>",
            descriptionPt = "Limpar todos os dados e cache de um app",
            descriptionEn = "Clear all app data and cache",
            category = "pm",
            keywords = listOf("clear", "limpar", "dados", "reset")
        ),
        CommandSuggestionItem(
            templatePt = "pm uninstall <pacote>",
            templateEn = "pm uninstall <package>",
            descriptionPt = "Desinstalar um aplicativo",
            descriptionEn = "Uninstall an app package",
            category = "pm",
            keywords = listOf("uninstall", "desinstalar", "remover")
        ),
        CommandSuggestionItem(
            templatePt = "pm uninstall -k --user 0 <pacote>",
            templateEn = "pm uninstall -k --user 0 <package>",
            descriptionPt = "Desinstalar do usuário atual mantendo dados (debloat)",
            descriptionEn = "Uninstall for current user keeping data",
            category = "pm",
            keywords = listOf("uninstall", "debloat", "user")
        ),
        CommandSuggestionItem(
            templatePt = "pm disable-user --user 0 <pacote>",
            templateEn = "pm disable-user --user 0 <package>",
            descriptionPt = "Desativar aplicativo para o usuário atual",
            descriptionEn = "Disable package for current user",
            category = "pm",
            keywords = listOf("disable", "desativar", "bloquear")
        ),
        CommandSuggestionItem(
            templatePt = "pm enable <pacote>",
            templateEn = "pm enable <package>",
            descriptionPt = "Ativar um aplicativo desativado",
            descriptionEn = "Enable a disabled package",
            category = "pm",
            keywords = listOf("enable", "ativar")
        ),
        CommandSuggestionItem(
            templatePt = "pm path <pacote>",
            templateEn = "pm path <package>",
            descriptionPt = "Exibir caminho do APK no sistema",
            descriptionEn = "Show APK file path of package",
            category = "pm",
            keywords = listOf("path", "caminho", "apk")
        ),
        CommandSuggestionItem(
            templatePt = "pm install -r <caminho_apk>",
            templateEn = "pm install -r <apk_path>",
            descriptionPt = "Instalar APK mantendo dados existentes",
            descriptionEn = "Install or reinstall APK keeping app data",
            category = "pm",
            keywords = listOf("install", "instalar")
        ),
        CommandSuggestionItem(
            templatePt = "pm dump <pacote>",
            templateEn = "pm dump <package>",
            descriptionPt = "Exibir dados e permissões detalhadas do pacote",
            descriptionEn = "Dump package state and details",
            category = "pm",
            keywords = listOf("dump", "info")
        ),
        CommandSuggestionItem(
            templatePt = "pm grant <pacote> <permissao>",
            templateEn = "pm grant <package> <permission>",
            descriptionPt = "Conceder uma permissão ao aplicativo",
            descriptionEn = "Grant a permission to package",
            category = "pm",
            keywords = listOf("grant", "permissao", "permission")
        ),
        CommandSuggestionItem(
            templatePt = "pm revoke <pacote> <permissao>",
            templateEn = "pm revoke <package> <permission>",
            descriptionPt = "Revogar permissão do aplicativo",
            descriptionEn = "Revoke permission from package",
            category = "pm",
            keywords = listOf("revoke", "revogar")
        ),
        CommandSuggestionItem(
            templatePt = "pm reset-permissions",
            templateEn = "pm reset-permissions",
            descriptionPt = "Redefinir permissões de todos os apps para padrão",
            descriptionEn = "Reset all runtime permissions to default",
            category = "pm",
            keywords = listOf("reset", "permissions")
        ),

        // === CMD (Command Services) ===
        CommandSuggestionItem(
            templatePt = "cmd package compile -m speed -f <pacote>",
            templateEn = "cmd package compile -m speed -f <package>",
            descriptionPt = "Otimizar app com compilação AOT (Speed dexopt)",
            descriptionEn = "Compile package with speed filter (AOT)",
            complementOptions = listOf("-a", "<pacote>"),
            category = "cmd",
            keywords = listOf("compile", "speed", "otimizar", "dexopt", "performance")
        ),
        CommandSuggestionItem(
            templatePt = "cmd package compile -m speed -a",
            templateEn = "cmd package compile -m speed -a",
            descriptionPt = "Otimizar todos os aplicativos do sistema para velocidade",
            descriptionEn = "Compile all packages with speed filter",
            category = "cmd",
            keywords = listOf("compile", "all", "todos", "speed")
        ),
        CommandSuggestionItem(
            templatePt = "cmd package compile -m everything -f <pacote>",
            templateEn = "cmd package compile -m everything -f <package>",
            descriptionPt = "Compilar código completo do app (Everything dexopt)",
            descriptionEn = "Compile everything for package",
            category = "cmd",
            keywords = listOf("compile", "everything")
        ),
        CommandSuggestionItem(
            templatePt = "cmd package compile --reset <pacote>",
            templateEn = "cmd package compile --reset <package>",
            descriptionPt = "Resetar otimização/compilação do aplicativo",
            descriptionEn = "Reset package compilation state",
            category = "cmd",
            keywords = listOf("compile", "reset")
        ),
        CommandSuggestionItem(
            templatePt = "cmd package bg-dexopt-job",
            templateEn = "cmd package bg-dexopt-job",
            descriptionPt = "Executar job de otimização dex em segundo plano",
            descriptionEn = "Trigger background dex optimization job",
            category = "cmd",
            keywords = listOf("bg-dexopt", "dexopt", "job", "otimizacao")
        ),
        CommandSuggestionItem(
            templatePt = "cmd package list packages",
            templateEn = "cmd package list packages",
            descriptionPt = "Listar pacotes instalados via cmd",
            descriptionEn = "List packages via cmd service",
            category = "cmd",
            keywords = listOf("package", "list")
        ),
        CommandSuggestionItem(
            templatePt = "cmd deviceidle force-idle",
            templateEn = "cmd deviceidle force-idle",
            descriptionPt = "Forçar modo Doze (economia profunda de bateria)",
            descriptionEn = "Force device into Doze idle mode",
            category = "cmd",
            keywords = listOf("doze", "idle", "bateria", "battery")
        ),
        CommandSuggestionItem(
            templatePt = "cmd deviceidle unforce",
            templateEn = "cmd deviceidle unforce",
            descriptionPt = "Desativar modo Doze forçado",
            descriptionEn = "Unforce Doze idle mode",
            category = "cmd",
            keywords = listOf("doze", "unforce")
        ),
        CommandSuggestionItem(
            templatePt = "cmd deviceidle whitelist +<pacote>",
            templateEn = "cmd deviceidle whitelist +<package>",
            descriptionPt = "Adicionar app à lista de permissões do Doze",
            descriptionEn = "Add package to Doze battery whitelist",
            category = "cmd",
            keywords = listOf("doze", "whitelist")
        ),
        CommandSuggestionItem(
            templatePt = "cmd deviceidle whitelist -<pacote>",
            templateEn = "cmd deviceidle whitelist -<package>",
            descriptionPt = "Remover app da lista de permissões do Doze",
            descriptionEn = "Remove package from Doze whitelist",
            category = "cmd",
            keywords = listOf("doze", "whitelist")
        ),
        CommandSuggestionItem(
            templatePt = "cmd battery set level <nível>",
            templateEn = "cmd battery set level <level>",
            descriptionPt = "Simular nível de bateria (0 a 100)",
            descriptionEn = "Set simulated battery level (0-100)",
            complementOptions = listOf("100", "80", "50", "20", "5", "1"),
            category = "cmd",
            keywords = listOf("battery", "bateria", "level", "nivel")
        ),
        CommandSuggestionItem(
            templatePt = "cmd battery reset",
            templateEn = "cmd battery reset",
            descriptionPt = "Restaurar leitura real da bateria",
            descriptionEn = "Reset battery simulation to real values",
            category = "cmd",
            keywords = listOf("battery", "reset", "bateria")
        ),
        CommandSuggestionItem(
            templatePt = "cmd battery unplug",
            templateEn = "cmd battery unplug",
            descriptionPt = "Simular desconexão do carregador",
            descriptionEn = "Simulate battery unplugged from charger",
            category = "cmd",
            keywords = listOf("battery", "unplug")
        ),
        CommandSuggestionItem(
            templatePt = "cmd statusbar expand-notifications",
            templateEn = "cmd statusbar expand-notifications",
            descriptionPt = "Abrir painel de notificações",
            descriptionEn = "Open notification shade",
            category = "cmd",
            keywords = listOf("statusbar", "notificacao", "notifications")
        ),
        CommandSuggestionItem(
            templatePt = "cmd statusbar expand-settings",
            templateEn = "cmd statusbar expand-settings",
            descriptionPt = "Abrir painel de configurações rápidas",
            descriptionEn = "Open quick settings shade",
            category = "cmd",
            keywords = listOf("statusbar", "settings", "quick")
        ),
        CommandSuggestionItem(
            templatePt = "cmd statusbar collapse",
            templateEn = "cmd statusbar collapse",
            descriptionPt = "Fechar painéis da barra de status",
            descriptionEn = "Collapse status bar shades",
            category = "cmd",
            keywords = listOf("statusbar", "collapse", "fechar")
        ),
        CommandSuggestionItem(
            templatePt = "cmd power set-fixed-performance-mode-enabled true",
            templateEn = "cmd power set-fixed-performance-mode-enabled true",
            descriptionPt = "Ativar modo de performance fixa",
            descriptionEn = "Enable fixed performance mode",
            complementOptions = listOf("true", "false"),
            category = "cmd",
            keywords = listOf("power", "performance", "desempenho")
        ),
        CommandSuggestionItem(
            templatePt = "cmd appops set <pacote> <operacao> <modo>",
            templateEn = "cmd appops set <package> <op> <mode>",
            descriptionPt = "Configurar permissão AppOps de um app",
            descriptionEn = "Set AppOps permission for package",
            complementOptions = listOf("allow", "ignore", "deny", "default"),
            category = "cmd",
            keywords = listOf("appops", "permissoes")
        ),
        CommandSuggestionItem(
            templatePt = "cmd overlay list",
            templateEn = "cmd overlay list",
            descriptionPt = "Listar temas e sobreposições (overlays) instalados",
            descriptionEn = "List installed overlays/themes",
            category = "cmd",
            keywords = listOf("overlay", "temas")
        ),

        // === AM (Activity Manager) ===
        CommandSuggestionItem(
            templatePt = "am force-stop <pacote>",
            templateEn = "am force-stop <package>",
            descriptionPt = "Forçar fechamento imediato de um app",
            descriptionEn = "Force stop package immediately",
            category = "am",
            keywords = listOf("force-stop", "fechar", "parar")
        ),
        CommandSuggestionItem(
            templatePt = "am start -n <pacote>/<activity>",
            templateEn = "am start -n <package>/<activity>",
            descriptionPt = "Iniciar uma Activity de um aplicativo",
            descriptionEn = "Start an activity component",
            category = "am",
            keywords = listOf("start", "iniciar", "activity")
        ),
        CommandSuggestionItem(
            templatePt = "am start -a android.intent.action.VIEW -d <url>",
            templateEn = "am start -a android.intent.action.VIEW -d <url>",
            descriptionPt = "Abrir link ou URL no navegador padrão",
            descriptionEn = "Open link or URL in default browser",
            category = "am",
            keywords = listOf("browser", "url", "link")
        ),
        CommandSuggestionItem(
            templatePt = "am kill <pacote>",
            templateEn = "am kill <package>",
            descriptionPt = "Encerrar processos em segundo plano do app",
            descriptionEn = "Kill background processes of package",
            category = "am",
            keywords = listOf("kill", "encerrar")
        ),
        CommandSuggestionItem(
            templatePt = "am kill-all",
            templateEn = "am kill-all",
            descriptionPt = "Encerrar todos os processos seguros em segundo plano",
            descriptionEn = "Kill all safe background processes",
            category = "am",
            keywords = listOf("kill-all", "encerrar")
        ),

        // === SETTINGS ===
        CommandSuggestionItem(
            templatePt = "settings get system <chave>",
            templateEn = "settings get system <key>",
            descriptionPt = "Consultar valor de configuração do sistema",
            descriptionEn = "Get system setting value",
            category = "settings",
            keywords = listOf("settings", "get", "system")
        ),
        CommandSuggestionItem(
            templatePt = "settings get global <chave>",
            templateEn = "settings get global <key>",
            descriptionPt = "Consultar valor de configuração global",
            descriptionEn = "Get global setting value",
            category = "settings",
            keywords = listOf("settings", "get", "global")
        ),
        CommandSuggestionItem(
            templatePt = "settings get secure <chave>",
            templateEn = "settings get secure <key>",
            descriptionPt = "Consultar valor de configuração segura",
            descriptionEn = "Get secure setting value",
            category = "settings",
            keywords = listOf("settings", "get", "secure")
        ),
        CommandSuggestionItem(
            templatePt = "settings put system <chave> <valor>",
            templateEn = "settings put system <key> <value>",
            descriptionPt = "Gravar valor em configuração do sistema",
            descriptionEn = "Put value in system settings",
            category = "settings",
            keywords = listOf("settings", "put", "system")
        ),
        CommandSuggestionItem(
            templatePt = "settings put global <chave> <valor>",
            templateEn = "settings put global <key> <value>",
            descriptionPt = "Gravar valor em configuração global",
            descriptionEn = "Put value in global settings",
            category = "settings",
            keywords = listOf("settings", "put", "global")
        ),
        CommandSuggestionItem(
            templatePt = "settings put secure <chave> <valor>",
            templateEn = "settings put secure <key> <value>",
            descriptionPt = "Gravar valor em configuração segura",
            descriptionEn = "Put value in secure settings",
            category = "settings",
            keywords = listOf("settings", "put", "secure")
        ),
        CommandSuggestionItem(
            templatePt = "settings list global",
            templateEn = "settings list global",
            descriptionPt = "Listar todas as configurações globais",
            descriptionEn = "List all global settings",
            category = "settings",
            keywords = listOf("settings", "list", "global")
        ),
        CommandSuggestionItem(
            templatePt = "settings list system",
            templateEn = "settings list system",
            descriptionPt = "Listar todas as configurações do sistema",
            descriptionEn = "List all system settings",
            category = "settings",
            keywords = listOf("settings", "list", "system")
        ),
        CommandSuggestionItem(
            templatePt = "settings list secure",
            templateEn = "settings list secure",
            descriptionPt = "Listar todas as configurações seguras",
            descriptionEn = "List all secure settings",
            category = "settings",
            keywords = listOf("settings", "list", "secure")
        ),

        // === DUMPSYS ===
        CommandSuggestionItem(
            templatePt = "dumpsys battery",
            templateEn = "dumpsys battery",
            descriptionPt = "Exibir detalhes, nível e temperatura da bateria",
            descriptionEn = "Dump battery status, level and temperature",
            category = "dumpsys",
            keywords = listOf("dumpsys", "battery", "bateria")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys meminfo",
            templateEn = "dumpsys meminfo",
            descriptionPt = "Exibir consumo total de memória RAM",
            descriptionEn = "Dump total RAM memory usage",
            category = "dumpsys",
            keywords = listOf("dumpsys", "meminfo", "ram", "memoria")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys meminfo <pacote>",
            templateEn = "dumpsys meminfo <package>",
            descriptionPt = "Exibir consumo de RAM de um aplicativo específico",
            descriptionEn = "Dump package RAM memory usage",
            category = "dumpsys",
            keywords = listOf("dumpsys", "meminfo", "ram")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys cpuinfo",
            templateEn = "dumpsys cpuinfo",
            descriptionPt = "Exibir porcentagem de uso de CPU por processo",
            descriptionEn = "Dump CPU percentage by process",
            category = "dumpsys",
            keywords = listOf("dumpsys", "cpuinfo", "cpu")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys display",
            templateEn = "dumpsys display",
            descriptionPt = "Exibir dados da tela, resoluções e taxa em Hz",
            descriptionEn = "Dump display details, refresh rates and modes",
            category = "dumpsys",
            keywords = listOf("dumpsys", "display", "tela", "hz")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys power",
            templateEn = "dumpsys power",
            descriptionPt = "Exibir wakelocks e gerenciamento de energia",
            descriptionEn = "Dump power management and wakelocks",
            category = "dumpsys",
            keywords = listOf("dumpsys", "power", "energia")
        ),
        CommandSuggestionItem(
            templatePt = "dumpsys wifi",
            templateEn = "dumpsys wifi",
            descriptionPt = "Exibir status e conectividade do Wi-Fi",
            descriptionEn = "Dump WiFi connectivity and state",
            category = "dumpsys",
            keywords = listOf("dumpsys", "wifi")
        ),

        // === WM (Window Manager) ===
        CommandSuggestionItem(
            templatePt = "wm size",
            templateEn = "wm size",
            descriptionPt = "Exibir resolução de tela atual e física",
            descriptionEn = "Display current and physical screen size",
            category = "wm",
            keywords = listOf("wm", "size", "resolucao")
        ),
        CommandSuggestionItem(
            templatePt = "wm size <largura>x<altura>",
            templateEn = "wm size <width>x<height>",
            descriptionPt = "Alterar resolução de tela (ex: 1080x2400)",
            descriptionEn = "Override screen resolution (e.g. 1080x2400)",
            complementOptions = listOf("1080x2400", "1080x1920", "720x1600", "reset"),
            category = "wm",
            keywords = listOf("wm", "size", "resolucao")
        ),
        CommandSuggestionItem(
            templatePt = "wm size reset",
            templateEn = "wm size reset",
            descriptionPt = "Restaurar resolução padrão de fábrica",
            descriptionEn = "Reset screen resolution to default",
            category = "wm",
            keywords = listOf("wm", "size", "reset")
        ),
        CommandSuggestionItem(
            templatePt = "wm density",
            templateEn = "wm density",
            descriptionPt = "Exibir densidade de pixels (DPI) atual",
            descriptionEn = "Display current screen density (DPI)",
            category = "wm",
            keywords = listOf("wm", "density", "dpi")
        ),
        CommandSuggestionItem(
            templatePt = "wm density <dpi>",
            templateEn = "wm density <dpi>",
            descriptionPt = "Alterar densidade de DPI da tela (ex: 400)",
            descriptionEn = "Override screen density DPI (e.g. 400)",
            complementOptions = listOf("360", "380", "400", "420", "440", "480", "reset"),
            category = "wm",
            keywords = listOf("wm", "density", "dpi")
        ),
        CommandSuggestionItem(
            templatePt = "wm density reset",
            templateEn = "wm density reset",
            descriptionPt = "Restaurar DPI padrão da tela",
            descriptionEn = "Reset screen density to default",
            category = "wm",
            keywords = listOf("wm", "density", "reset")
        ),

        // === SVC ===
        CommandSuggestionItem(
            templatePt = "svc wifi <enable|disable>",
            templateEn = "svc wifi <enable|disable>",
            descriptionPt = "Ativar ou desativar Wi-Fi",
            descriptionEn = "Turn WiFi on or off",
            complementOptions = listOf("enable", "disable"),
            category = "svc",
            keywords = listOf("svc", "wifi")
        ),
        CommandSuggestionItem(
            templatePt = "svc data <enable|disable>",
            templateEn = "svc data <enable|disable>",
            descriptionPt = "Ativar ou desativar dados móveis",
            descriptionEn = "Turn mobile data on or off",
            complementOptions = listOf("enable", "disable"),
            category = "svc",
            keywords = listOf("svc", "data", "dados")
        ),
        CommandSuggestionItem(
            templatePt = "svc bluetooth <enable|disable>",
            templateEn = "svc bluetooth <enable|disable>",
            descriptionPt = "Ativar ou desativar Bluetooth",
            descriptionEn = "Turn Bluetooth on or off",
            complementOptions = listOf("enable", "disable"),
            category = "svc",
            keywords = listOf("svc", "bluetooth")
        ),

        // === INPUT ===
        CommandSuggestionItem(
            templatePt = "input tap <x> <y>",
            templateEn = "input tap <x> <y>",
            descriptionPt = "Simular toque nas coordenadas X e Y",
            descriptionEn = "Send tap input at X and Y coordinates",
            category = "input",
            keywords = listOf("input", "tap", "toque")
        ),
        CommandSuggestionItem(
            templatePt = "input swipe <x1> <y1> <x2> <y2> <duracao_ms>",
            templateEn = "input swipe <x1> <y1> <x2> <y2> <duration_ms>",
            descriptionPt = "Simular gesto de deslizar na tela",
            descriptionEn = "Send swipe gesture across screen",
            category = "input",
            keywords = listOf("input", "swipe", "arrastar")
        ),
        CommandSuggestionItem(
            templatePt = "input text <texto>",
            templateEn = "input text <string>",
            descriptionPt = "Digitar texto simulando teclado",
            descriptionEn = "Send keystroke text to active field",
            category = "input",
            keywords = listOf("input", "text", "digitar")
        ),
        CommandSuggestionItem(
            templatePt = "input keyevent <keycode>",
            templateEn = "input keyevent <keycode>",
            descriptionPt = "Enviar evento de tecla de hardware",
            descriptionEn = "Send hardware keyevent code",
            complementOptions = listOf("26 (Power)", "3 (Home)", "4 (Back)", "24 (Vol+)", "25 (Vol-)", "82 (Menu)"),
            category = "input",
            keywords = listOf("input", "keyevent", "tecla")
        ),

        // === GETPROP / SETPROP ===
        CommandSuggestionItem(
            templatePt = "getprop",
            templateEn = "getprop",
            descriptionPt = "Listar todas as propriedades do sistema Android",
            descriptionEn = "List all Android system properties",
            category = "prop",
            keywords = listOf("getprop", "properties")
        ),
        CommandSuggestionItem(
            templatePt = "getprop ro.build.version.release",
            templateEn = "getprop ro.build.version.release",
            descriptionPt = "Exibir versão do Android instalada",
            descriptionEn = "Get Android release version",
            category = "prop",
            keywords = listOf("getprop", "version")
        ),
        CommandSuggestionItem(
            templatePt = "getprop ro.product.model",
            templateEn = "getprop ro.product.model",
            descriptionPt = "Exibir modelo do aparelho",
            descriptionEn = "Get device model name",
            category = "prop",
            keywords = listOf("getprop", "model")
        ),
        CommandSuggestionItem(
            templatePt = "setprop <propriedade> <valor>",
            templateEn = "setprop <property> <value>",
            descriptionPt = "Alterar propriedade do sistema",
            descriptionEn = "Set system property value",
            category = "prop",
            keywords = listOf("setprop", "property")
        ),
        CommandSuggestionItem(
            templatePt = "resetprop <propriedade> <valor>",
            templateEn = "resetprop <property> <value>",
            descriptionPt = "Alterar propriedade protegida (resetprop)",
            descriptionEn = "Set protected system property",
            category = "prop",
            keywords = listOf("resetprop", "property")
        ),

        // === LINUX / SYSTEM ===
        CommandSuggestionItem(
            templatePt = "top -m 10",
            templateEn = "top -m 10",
            descriptionPt = "Monitorar os 10 processos com maior uso de CPU",
            descriptionEn = "Monitor top 10 CPU-consuming processes",
            complementOptions = listOf("-m 5", "-m 10", "-m 20", "-d 1"),
            category = "system",
            keywords = listOf("top", "cpu", "processos")
        ),
        CommandSuggestionItem(
            templatePt = "ps -ef",
            templateEn = "ps -ef",
            descriptionPt = "Listar todos os processos em execução",
            descriptionEn = "List all running processes",
            category = "system",
            keywords = listOf("ps", "processos")
        ),
        CommandSuggestionItem(
            templatePt = "logcat",
            templateEn = "logcat",
            descriptionPt = "Visualizar logs do sistema Android em tempo real",
            descriptionEn = "View real-time Android system logs",
            complementOptions = listOf("-c", "-d", "*:E", "-s"),
            category = "system",
            keywords = listOf("logcat", "logs")
        ),
        CommandSuggestionItem(
            templatePt = "logcat -c",
            templateEn = "logcat -c",
            descriptionPt = "Limpar histórico de logs do sistema",
            descriptionEn = "Clear system log buffers",
            category = "system",
            keywords = listOf("logcat", "clear")
        ),
        CommandSuggestionItem(
            templatePt = "df -h",
            templateEn = "df -h",
            descriptionPt = "Exibir espaço livre e usado nas partições",
            descriptionEn = "Display free and used disk space",
            category = "system",
            keywords = listOf("df", "disco", "armazenamento")
        ),
        CommandSuggestionItem(
            templatePt = "free -m",
            templateEn = "free -m",
            descriptionPt = "Exibir memória RAM livre e usada em MB",
            descriptionEn = "Show free and used RAM in MB",
            category = "system",
            keywords = listOf("free", "ram", "memoria")
        ),
        CommandSuggestionItem(
            templatePt = "whoami",
            templateEn = "whoami",
            descriptionPt = "Exibir usuário atual da sessão",
            descriptionEn = "Show current shell username",
            category = "system",
            keywords = listOf("whoami", "user", "usuario")
        ),
        CommandSuggestionItem(
            templatePt = "id",
            templateEn = "id",
            descriptionPt = "Exibir UID, GID e grupos de privilégio",
            descriptionEn = "Print UID, GID and groups",
            category = "system",
            keywords = listOf("id", "uid", "gid")
        ),
        CommandSuggestionItem(
            templatePt = "uname -a",
            templateEn = "uname -a",
            descriptionPt = "Exibir versão completa do Kernel Linux",
            descriptionEn = "Print full Linux kernel info",
            category = "system",
            keywords = listOf("uname", "kernel")
        ),
        CommandSuggestionItem(
            templatePt = "kill -9 <pid>",
            templateEn = "kill -9 <pid>",
            descriptionPt = "Encerrar processo imediatamente pelo PID",
            descriptionEn = "Force kill process by PID",
            category = "system",
            keywords = listOf("kill", "pid")
        ),

        // === AXERON ===
        CommandSuggestionItem(
            templatePt = "reignite",
            templateEn = "reignite",
            descriptionPt = "Reiniciar ambiente e serviços do Axeron",
            descriptionEn = "Re-ignite Axeron environment and plugins",
            category = "axeron",
            keywords = listOf("reignite", "axeron", "ignite")
        ),
        CommandSuggestionItem(
            templatePt = "axeron --help",
            templateEn = "axeron --help",
            descriptionPt = "Exibir ajuda e comandos do Axeron",
            descriptionEn = "Show Axeron CLI help and commands",
            category = "axeron",
            keywords = listOf("axeron", "help", "ajuda")
        )
    )

    fun extractCurrentCommandSegment(fullText: String, cursorIndex: Int): Pair<Int, String> {
        val safeCursor = cursorIndex.coerceIn(0, fullText.length)
        val beforeCursor = fullText.substring(0, safeCursor)
        val delimiters = charArrayOf(';', '&', '|', '\n')
        val lastDelim = beforeCursor.lastIndexOfAny(delimiters)
        val startIndex = if (lastDelim >= 0) lastDelim + 1 else 0
        val segment = beforeCursor.substring(startIndex)
        val leadingSpaces = segment.takeWhile { it.isWhitespace() }.length
        return Pair(startIndex + leadingSpaces, segment.trimStart())
    }

    fun getSuggestions(
        fullText: String,
        cursorIndex: Int,
        isPt: Boolean
    ): List<CommandSuggestionItem> {
        val (_, segment) = extractCurrentCommandSegment(fullText, cursorIndex)
        val cleanQuery = segment.trim().lowercase()

        if (cleanQuery.isEmpty()) {
            // Show starter / popular commands
            return suggestions.filter {
                it.templatePt in listOf(
                    "pm list packages",
                    "pm trim-caches <tamanho>",
                    "cmd package compile -m speed -f <pacote>",
                    "cmd package bg-dexopt-job",
                    "cmd deviceidle force-idle",
                    "dumpsys battery",
                    "top -m 10",
                    "logcat",
                    "ps -ef",
                    "reignite"
                )
            }
        }

        val startsWithMatches = mutableListOf<CommandSuggestionItem>()
        val keywordMatches = mutableListOf<CommandSuggestionItem>()

        for (item in suggestions) {
            val template = item.template(isPt).lowercase()
            val commandName = template.substringBefore(" <").substringBefore(" ")

            if (template.startsWith(cleanQuery)) {
                startsWithMatches.add(item)
            } else if (cleanQuery.startsWith("pm") && item.category == "pm") {
                if (template.contains(cleanQuery) || item.keywords.any { it.startsWith(cleanQuery.removePrefix("pm").trim()) }) {
                    startsWithMatches.add(item)
                }
            } else if (cleanQuery.startsWith("cmd") && item.category == "cmd") {
                if (template.contains(cleanQuery) || item.keywords.any { it.startsWith(cleanQuery.removePrefix("cmd").trim()) }) {
                    startsWithMatches.add(item)
                }
            } else if (template.contains(cleanQuery) ||
                item.description(isPt).lowercase().contains(cleanQuery) ||
                item.keywords.any { it.contains(cleanQuery) }
            ) {
                keywordMatches.add(item)
            }
        }

        return (startsWithMatches + keywordMatches).distinctBy { it.template(isPt) }
    }

    fun getComplements(
        fullText: String,
        cursorIndex: Int,
        installedPackages: List<String>,
        isPt: Boolean
    ): List<String> {
        val (_, segment) = extractCurrentCommandSegment(fullText, cursorIndex)
        val lowerSegment = segment.trim().lowercase()

        // Check if there is an active placeholder like <...>
        val placeholder = Regex("<[^>]+>").find(fullText)

        // 1. Check for pm trim-caches
        if (lowerSegment.startsWith("pm trim-caches") || placeholder?.value in listOf("<tamanho>", "<size>")) {
            return listOf("1000M (1 GB)", "500M (500 MB)", "2000M (2 GB)", "100M (100 MB)", "4000M (4 GB)")
        }

        // 2. Check for battery set level
        if (lowerSegment.startsWith("cmd battery set level") || placeholder?.value in listOf("<nível>", "<level>")) {
            return listOf("100", "80", "50", "20", "5", "1")
        }

        // 3. Check for fixed performance mode
        if (lowerSegment.startsWith("cmd power set-fixed-performance-mode-enabled") || placeholder?.value in listOf("<ativar>", "<enabled>")) {
            return listOf("true", "false")
        }

        // 4. Check for svc wifi / data / bluetooth
        if (lowerSegment.startsWith("svc wifi") || lowerSegment.startsWith("svc data") || lowerSegment.startsWith("svc bluetooth")) {
            return listOf("enable", "disable")
        }

        // 5. Check for wm size
        if (lowerSegment.startsWith("wm size") && !lowerSegment.contains("reset") || placeholder?.value in listOf("<largura>x<altura>", "<width>x<height>")) {
            return listOf("1080x2400", "1080x1920", "720x1600", "reset")
        }

        // 6. Check for wm density
        if (lowerSegment.startsWith("wm density") && !lowerSegment.contains("reset") || placeholder?.value in listOf("<dpi>")) {
            return listOf("360", "380", "400", "420", "440", "480", "reset")
        }

        // 7. Check for input keyevent
        if (lowerSegment.startsWith("input keyevent") || placeholder?.value in listOf("<keycode>")) {
            return listOf("26 (Power)", "3 (Home)", "4 (Back)", "24 (Vol+)", "25 (Vol-)", "82 (Menu)")
        }

        // 8. Check for top
        if (lowerSegment.startsWith("top")) {
            return listOf("-m 5", "-m 10", "-m 20", "-d 1")
        }

        // 9. Check for logcat
        if (lowerSegment == "logcat" || lowerSegment.startsWith("logcat ")) {
            return listOf("-c", "-d", "*:E", "-s")
        }

        // 10. Check for appops
        if (lowerSegment.startsWith("cmd appops set")) {
            return listOf("allow", "ignore", "deny", "default")
        }

        // 11. Check for commands requiring package names
        val needsPackage = placeholder?.value in listOf("<pacote>", "<package>") ||
                lowerSegment.startsWith("pm clear") ||
                lowerSegment.startsWith("pm uninstall") ||
                lowerSegment.startsWith("pm enable") ||
                lowerSegment.startsWith("pm disable-user") ||
                lowerSegment.startsWith("pm dump") ||
                lowerSegment.startsWith("pm path") ||
                lowerSegment.startsWith("dumpsys meminfo ") ||
                lowerSegment.startsWith("cmd package compile -m speed -f")

        if (needsPackage) {
            val queryParam = lowerSegment.substringAfterLast(" ", "").removePrefix("+").removePrefix("-")
            val filtered = if (queryParam.isNotEmpty() && !queryParam.startsWith("<")) {
                installedPackages.filter { it.contains(queryParam, ignoreCase = true) }.take(8)
            } else {
                installedPackages.take(8)
            }
            if (lowerSegment.startsWith("cmd package compile")) {
                return listOf("-a (Todos os apps)") + filtered
            }
            return filtered
        }

        return emptyList()
    }

    fun applySuggestion(
        currentValue: TextFieldValue,
        item: CommandSuggestionItem,
        isPt: Boolean
    ): TextFieldValue {
        val fullText = currentValue.text
        val cursor = currentValue.selection.end.coerceIn(0, fullText.length)
        val (segStart, _) = extractCurrentCommandSegment(fullText, cursor)

        val prefix = fullText.substring(0, segStart)
        val suffix = fullText.substring(cursor)

        val template = item.template(isPt)
        val newCommand = prefix + template + suffix

        val placeholderMatch = Regex("<[^>]+>").find(template)
        return if (placeholderMatch != null) {
            val selStart = prefix.length + placeholderMatch.range.first
            val selEnd = prefix.length + placeholderMatch.range.last + 1
            TextFieldValue(text = newCommand, selection = TextRange(selStart, selEnd))
        } else {
            val withSpace = if (!newCommand.endsWith(" ")) "$newCommand " else newCommand
            TextFieldValue(text = withSpace, selection = TextRange(withSpace.length))
        }
    }

    fun applyComplement(
        currentValue: TextFieldValue,
        complementText: String
    ): TextFieldValue {
        val text = currentValue.text
        val selection = currentValue.selection

        // Strip explanatory label e.g. "1000M (1 GB)" -> "1000M"
        val actualValue = complementText.substringBefore(" (").trim()

        // If a placeholder or range is selected, replace that selection
        if (selection.start != selection.end) {
            val start = minOf(selection.start, selection.end).coerceIn(0, text.length)
            val end = maxOf(selection.start, selection.end).coerceIn(0, text.length)
            val newText = text.substring(0, start) + actualValue + text.substring(end)
            val newCursor = start + actualValue.length

            val nextMatch = Regex("<[^>]+>").find(newText, newCursor)
            return if (nextMatch != null) {
                TextFieldValue(
                    text = newText,
                    selection = TextRange(nextMatch.range.first, nextMatch.range.last + 1)
                )
            } else {
                val withSpace = if (!newText.endsWith(" ")) "$newText " else newText
                TextFieldValue(text = withSpace, selection = TextRange(withSpace.length))
            }
        }

        // If text contains a placeholder <...> anywhere, replace it
        val placeholderMatch = Regex("<[^>]+>").find(text)
        if (placeholderMatch != null) {
            val newText = text.replaceRange(placeholderMatch.range, actualValue)
            val newCursor = placeholderMatch.range.first + actualValue.length
            val nextMatch = Regex("<[^>]+>").find(newText, newCursor)
            return if (nextMatch != null) {
                TextFieldValue(
                    text = newText,
                    selection = TextRange(nextMatch.range.first, nextMatch.range.last + 1)
                )
            } else {
                val withSpace = if (!newText.endsWith(" ")) "$newText " else newText
                TextFieldValue(text = withSpace, selection = TextRange(withSpace.length))
            }
        }

        // Otherwise append value
        val trimmed = text.trimEnd()
        val newText = if (trimmed.isEmpty()) "$actualValue " else "$trimmed $actualValue "
        return TextFieldValue(text = newText, selection = TextRange(newText.length))
    }
}

@Composable
fun CommandSuggestionsBar(
    commandText: TextFieldValue,
    installedPackages: List<String>,
    onApply: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    onOpenAi: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isPt = remember(context) {
        val locale = LocaleHelper.getCurrentAppLocale(context) ?: Locale.getDefault()
        locale.language.startsWith("pt")
    }

    val suggestions = remember(commandText.text, commandText.selection, isPt) {
        CommandSuggestionProvider.getSuggestions(commandText.text, commandText.selection.end, isPt)
    }

    val complements = remember(commandText.text, commandText.selection, installedPackages, isPt) {
        CommandSuggestionProvider.getComplements(commandText.text, commandText.selection.end, installedPackages, isPt)
    }

    if (suggestions.isEmpty() && complements.isEmpty() && onOpenAi == null) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 0. AI Assistant Button Chip
            if (onOpenAi != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        tonalElevation = 3.dp,
                        modifier = Modifier.clickable { onOpenAi() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isPt) "IA Gerar" else "AI Generate",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }
            }

            // 1. Complement chips (values that can directly complete or replace placeholder)
            items(complements) { complement ->
                ComplementChip(
                    label = complement,
                    onClick = {
                        val updated = CommandSuggestionProvider.applyComplement(commandText, complement)
                        onApply(updated)
                    }
                )
            }

            // 2. Command suggestions
            items(suggestions) { item ->
                CommandSuggestionChip(
                    item = item,
                    isPt = isPt,
                    onClick = {
                        val updated = CommandSuggestionProvider.applySuggestion(commandText, item, isPt)
                        onApply(updated)
                    }
                )
            }
        }
    }
}

@Composable
fun ComplementChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        tonalElevation = 3.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Bolt,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun CommandSuggestionChip(
    item: CommandSuggestionItem,
    isPt: Boolean,
    onClick: () -> Unit
) {
    val template = item.template(isPt)
    val description = item.description(isPt)

    val annotatedTemplate = remember(template) {
        buildAnnotatedString {
            val placeholderRegex = Regex("<[^>]+>")
            var lastIndex = 0
            for (match in placeholderRegex.findAll(template)) {
                if (match.range.first > lastIndex) {
                    append(template.substring(lastIndex, match.range.first))
                }
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(match.value)
                }
                lastIndex = match.range.last + 1
            }
            if (lastIndex < template.length) {
                append(template.substring(lastIndex))
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 1.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Terminal,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = annotatedTemplate,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
