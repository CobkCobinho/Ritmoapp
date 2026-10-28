# Validação do pacote

Verificado em 28/10/2026.

- APK Android de demonstração compilado com sucesso.
- 12 testes da lógica executados: 12 aprovados, sem falhas.
- Testes de interface compilados com sucesso.
- Android Lint concluído: 0 erros e 7 avisos. Seis avisos sugerem versões mais recentes das ferramentas e bibliotecas; o projeto mantém versões fixas que foram compiladas juntas. Um aviso sugere complementar a configuração de backup para Android anterior ao 12; o manifesto já desativa o backup com allowBackup=false.
- Compatibilidade mínima configurada: Android 8.0 (API 26).
- Não houve execução em aparelho físico ou emulador. Portanto, a interação e o visual ainda precisam dessa verificação.
- O aplicativo não emite alarme quando está fechado. O cronômetro é reconciliado ao reabrir.

Para repetir a verificação no Android Studio, rode `testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug`. Para executar os testes de interface, conecte um aparelho ou emulador e rode `connectedDebugAndroidTest`.
