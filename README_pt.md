# Axeron Manager (Prova de Conceito)

> **Axeron Manager (AxManager)** é uma Prova de Conceito (POC) de um ambiente próprio no Android que utiliza permissões de ADB para fornecer controle em nível de sistema. Este projeto explora a ideia de criar uma camada de execução independente e persistente baseada em ADB dentro do sistema.

[Ver em Inglês / View in English](README.md) | [切换到中文翻译](README_cn.md)

## 💡 O Conceito
Este projeto é uma exploração pessoal na criação de um **Ambiente ADB** dedicado no Android. Em vez de ser apenas um simples executor de comandos, o AxManager tem como objetivo estabelecer uma infraestrutura em segundo plano capaz de hospedar plugins, gerenciar otimizações do sistema e fornecer uma interface unificada para operações privilegiadas — tudo isso sem exigir acesso root completo (embora possa utilizar root caso esteja disponível).

## ✨ Recursos
- 🏗️ **Ambiente ADB Interno**  
  Um ambiente independente projetado para manter e utilizar privilégios em nível de ADB.
- 🖥️ **Executor de Shell**  
  Execute comandos de shell com sessões persistentes.  
  - Suporta **execução via ADB / Sem Root**.  
  - **Execução Root** opcional para recursos avançados.  

- ⚡ **Plugin (Módulo Sem Root)**  
  Um sistema para gerenciar módulos de terceiros no ambiente sem root. [Saiba mais](https://fahrez182.github.io/AxManager/plugin/what-is-plugin.html)  

- 🌐 **Interface WebUI**  
  Gerencie e interaja com o ambiente do sistema por meio de uma interface baseada na web.

## 📱 Por que esta Prova de Conceito?
- **Independência**: Visa minimizar a dependência de PCs externos para tarefas ADB após a configuração inicial.
- **Centrado no Ambiente**: Foco em criar uma camada privilegiada residente, em vez de apenas uma execução isolada de comandos.
- **Acessibilidade**: Traz capacidades similares às de "Root" para dispositivos sem root por meio de mecanismos nativos do sistema.

## 📖 Roteiro de Desenvolvimento
- [x] Ativador por Depuração sem fio.
- [x] Ativador por Linha de Comando / Root.
- [x] Suporte básico ao Executor de Shell (ADB/Sem Root).
- [x] Ativação automática ao usar Depuração sem fio (Em testes).
- [x] Sistema de [Plugins](https://fahrez182.github.io/AxManager/plugin/what-is-plugin.html) para extensões de terceiros.  
- [x] Modo do Desenvolvedor & Ferramentas avançadas de depuração.  
- [ ] Otimização de aplicativos baseada em perfis.

## 🔧 Compilação e Instalação
Clone o repositório e compile usando o Android Studio ou Gradle:

```bash
git clone https://github.com/fahrez182/AxManager.git
cd AxManager
./gradlew :manager:assembleDebug
```

Instale o aplicativo do gerenciador no seu dispositivo via ADB:

```bash
adb install manager/build/outputs/apk/debug/manager-debug.apk
```

## 🤝 Contribuições
Contribuições são muito bem-vindas!  
Sinta-se à vontade para abrir **issues**, enviar **pull requests** ou iniciar discussões para novas ideias e melhorias.

## 🙏 Créditos
- **[Magisk]()** "Ideias de **BusyBox** e Plugins (módulos sem root)"
- **[Shizuku](https://github.com/RikkaApps/Shizuku) / [API](https://github.com/RikkaApps/Shizuku-API)** "Ponto de partida e referência para aprendizado de IPC do Android e tratamento de permissões baseado em ADB"
- **[KernelSU](https://github.com/tiann/KernelSU) / [Next](https://github.com/KernelSU-Next/KernelSU-Next)** "Inspiração para a interface do usuário e recursos de WebUI."

## ⚠️ Avisos e Isenção de Responsabilidade Legal
Este projeto inclui partes adaptadas de código de:
- Shizuku Manager (© Rikka Apps)  
  Licenciado sob a Apache License, Versão 2.0  
  Repositório: https://github.com/RikkaApps/Shizuku
- Outros projetos de código aberto conforme creditados acima.

O AxManager não inclui nem distribui nenhum elemento visual original do Shizuku Manager, nem afirma ser um substituto oficial.  
Todo o código adaptado é utilizado estritamente para fins educacionais e experimentais, com devida atribuição e em total conformidade com a Apache License 2.0.

## 📜 Licença
Licenciado sob a [Apache License 2.0](LICENSE).
