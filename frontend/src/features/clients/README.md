# Conversa do cliente — integração futura

- `model/conversationGateway.js` é um adaptador mockado, sem navegação ou chamadas de rede.
- `viewmodel/useConversationViewModel.js` controla a ação, carregamento e feedback.
- `view/ConversationButton.jsx` renderiza um botão React local na ficha do cliente.

O contrato `openConversation({ clientId, phone, conversationUrl })` retorna `{ connected, message }`.
O mock retorna `connected: false` e avisa que a integração ainda não está conectada. Não envia dados do cliente.
Para integrar uma API externa, substitua o adaptador; o botão e seu ViewModel continuam usando o mesmo contrato.

A composição dos formulários fica em `features/studio/model/formConfiguration.js`, incluindo as divisões de Dados pessoais e Preferências.
