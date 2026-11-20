# Conversa do cliente — integração futura

- `model/conversationGateway.js` resolve o destino da conversa (link direto ou `wa.me`) e abre em nova aba.
- `viewmodel/useConversationViewModel.js` controla a ação, carregamento e feedback.
- `view/ConversationButton.jsx` renderiza um botão React local na ficha do cliente.

O contrato `openConversation({ clientId, phone, conversationUrl })` retorna `{ connected, message }`.
Quando não há `conversationUrl`, o gateway tenta abrir `https://wa.me/<telefone>` usando o telefone informado.
Se cliente não tiver link e telefone, o retorno é `connected: false` com mensagem orientando completar o cadastro.

A composição dos formulários fica em `features/studio/model/formConfiguration.js`, incluindo as divisões de Dados pessoais e Preferências.
