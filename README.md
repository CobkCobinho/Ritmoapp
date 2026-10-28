# Ritmo · Pomodoro para estudos

Um pequeno app Android em **Kotlin + Jetpack Compose**, com interface em português, para organizar matérias e estudar em ciclos de foco.

## O que ele faz

- Cronômetro com iniciar, pausar, retomar e recomeçar.
- Foco de **15, 25 ou 50 minutos**.
- Pausa curta de **5 minutos** e longa de **15 minutos**, a cada quatro focos concluídos.
- Cadastro e seleção de até 24 matérias.
- Meta diária ajustável de 1 a 12 sessões.
- Histórico de sessões concluídas e gráfico dos últimos sete dias.
- Dados locais, sem conta, anúncios, servidor ou conexão durante o uso.
- Recuperação da etapa em andamento ao sair e reabrir o app.

A próxima etapa fica pronta, mas só começa quando você toca no botão. Pausas, sessões canceladas e tempo parcial não contam como estudo concluído.

## Abrir e executar

1. Instale o [Android Studio](https://developer.android.com/studio).
2. Extraia o ZIP e abra a pasta **ritmo**, que contém `settings.gradle.kts`.
3. Aguarde a sincronização do Gradle. A primeira sincronização precisa de internet.
4. Se solicitado, instale **Android SDK Platform 36** e **Build Tools 35.0.0** pelo SDK Manager.
5. Use o JDK integrado do Android Studio ou JDK 17 para o Gradle.
6. Selecione um emulador ou um celular com **Android 8.0 ou superior** e clique em **Run ▶**.

No celular físico, ative as opções do desenvolvedor e a depuração USB para executar pelo Android Studio. Para experimentar sem abrir o código, transfira o arquivo **Ritmo-demo.apk**, fornecido separadamente, para o celular e abra-o para instalar. Ele é uma versão de demonstração; o app não foi publicado na Play Store.

## Como usar

1. Adicione uma matéria no botão **+**.
2. Escolha a matéria e a duração do foco.
3. Toque em **Começar foco**.
4. Ao terminar, inicie a pausa.
5. Acompanhe suas sessões na aba **Progresso**.

A matéria e a duração ficam bloqueadas quando uma etapa começa, inclusive quando está pausada. Conclua ou recomece a etapa para trocar. Recomeçar pede confirmação e descarta somente o progresso da etapa atual.

## Entenda o código

| Arquivo | Responsabilidade |
| --- | --- |
| `MainActivity.kt` | Abre o app e conecta a interface ao estado. |
| `StudyViewModel.kt` | Recebe as ações da tela e atualiza o relógio. |
| `domain/StudyEngine.kt` | Regras do Pomodoro, matérias e contagem das sessões. |
| `data/StudyRepository.kt` | Salva e recupera os dados locais. |
| `ui/RitmoApp.kt` | Telas, componentes e diálogos em Compose. |
| `ui/Theme.kt` | Cores e tipografia. |

O fluxo é: **toque na tela → ViewModel → regras → novo estado → tela atualizada**.

A lógica do cronômetro é independente do Android. Isso facilita a leitura e permite testar o avanço do tempo sem esperar os minutos de cada sessão.

### Como o tempo é preservado

Ao iniciar, o app salva o horário previsto para terminar. A tela calcula a diferença entre esse horário e o horário atual, em vez de simplesmente diminuir um contador a cada segundo.

Se você sair e voltar depois do prazo, o foco será registrado no horário em que deveria ter terminado, e a pausa ficará aguardando seu toque. O app não inventa novas sessões durante a sua ausência.

## Testes e compilação

No terminal do Android Studio, dentro da pasta do projeto:

**Windows:**
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

**macOS / Linux:**
```sh
chmod +x gradlew
./gradlew testDebugUnitTest assembleDebug
```

Os testes da lógica cobrem pausa/retomada, reabertura, conclusão única, pausa longa, troca de matéria, duração, cancelamento e contagem por data local.

Há também testes de interface em `app/src/androidTest`. Para executá-los, conecte um aparelho ou inicie um emulador e rode `connectedDebugAndroidTest`.

O APK gerado fica em `app/build/outputs/apk/debug/app-debug.apk`.

## Limites desta versão

- Não emite som nem notificação com o app fechado. O resultado é atualizado ao reabrir.
- Usa o relógio do aparelho: mudar a hora manualmente durante uma sessão afeta a contagem.
- Guarda as últimas **2.000 sessões**; totais e gráficos usam esse histórico.
- Mostra as últimas 30 sessões na tela de progresso.
- As matérias podem ser adicionadas; edição e exclusão ficam para uma próxima versão.
- Os dados ficam somente no aparelho. Desinstalar ou limpar os dados do app apaga o histórico.
- Não inclui sincronização entre aparelhos nem publicação na Play Store.

## Para estudar e evoluir

Se você vem de C# ou Python, comece por `StudyEngine.kt`: as classes de dados e funções têm menos detalhes específicos do Android.

Depois tente:
- Adicionar edição e exclusão de matérias.
- Permitir uma duração de foco personalizada.
- Exportar o histórico para CSV.
- Criar notificações ao concluir uma etapa.
- Trocar a persistência simples por Room quando o projeto crescer.

## Referências

- [Kotlin](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/develop/ui/compose)
- [Configuração e compatibilidade do Android Gradle Plugin](https://developer.android.com/build/releases/agp-8-13-0-release-notes)
- [Versões compatíveis de bibliotecas Compose](https://developer.android.com/develop/ui/compose/bom)


