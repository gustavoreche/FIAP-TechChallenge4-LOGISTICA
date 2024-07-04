package com.fiap.techchallenge4.domain;

import java.util.Objects;

public record IdPedido(
        Long numero
) {

    public IdPedido {
        if (Objects.isNull(numero) || numero <= 0) {
            throw new IllegalArgumentException("ID DO PEDIDO NAO PODE SER NULO OU MENOR E IGUAL A ZERO!");
        }

    }

}
