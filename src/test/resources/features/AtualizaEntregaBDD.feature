# language: pt

Funcionalidade: Teste de atualizar o status da entrega

  Cenário: Atualiza status para EM TRANSPORTE com sucesso
    Dado que informo uma entrega que ja foi cadastrada
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega foi atualizada para EM TRANSPORTE com sucesso

  Cenário: Atualiza status para EM TRANSPORTE de uma entrega que não foi criada
    Dado que informo uma entrega que não foi criada
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE

  Cenário: Atualiza status para EM TRANSPORTE de uma entrega que esta com status diferente de CRIADO
    Dado que informo uma entrega que esta com status diferente de CRIADO
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE

  Cenário: Atualiza status para EM TRANSPORTE com um cliente que não existe
    Dado que informo uma entrega com um cliente que não existe
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE

  Cenário: Atualiza status para EM TRANSPORTE com erro na api de cliente
    Dado que informo uma entrega e a api de cliente esta com erro
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE

  Cenário: Atualiza status para EM TRANSPORTE com um entregador que não existe
    Dado que informo uma entrega com um entregador que não existe
    Quando atualizo essa entrega para EM TRANSPORTE
    Entao recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE

  Cenário: Atualiza status para ENTREGUE com sucesso
    Dado que informo uma entrega que ja esta EM TRANSPORTE
    Quando atualizo essa entrega para ENTREGUE
    Entao recebo uma resposta que a entrega foi atualizada para ENTREGUE com sucesso

  Cenário: Atualiza status para ENTREGUE de uma entrega que não foi criada
    Dado que informo uma entrega que não foi criada
    Quando atualizo essa entrega para ENTREGUE
    Entao recebo uma resposta que a entrega não foi atualizada para ENTREGUE

  Cenário: Atualiza status para ENTREGUE com um entregador que não existe
    Dado que informo uma entrega com um entregador que não existe
    Quando atualizo essa entrega para ENTREGUE
    Entao recebo uma resposta que a entrega não foi atualizada para ENTREGUE
