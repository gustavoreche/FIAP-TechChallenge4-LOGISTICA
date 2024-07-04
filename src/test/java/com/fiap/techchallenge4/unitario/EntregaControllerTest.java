package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.infrasctructure.entrega.controller.EntregaController;
import com.fiap.techchallenge4.useCase.entrega.impl.EntregaUseCaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class EntregaControllerTest {

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar200_salvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.when(service.atualiza(
                                anyLong(),
                                any(StatusEntregaControllerEnum.class)
                        )
                )
                .thenReturn(
                        true
                );

        var controller = new EntregaController(service);

        // execução
        var produto = controller.atualizaStatus(1L, StatusEntregaControllerEnum.EM_TRANSPORTE);

        // avaliação
        Assertions.assertEquals(HttpStatus.OK, produto.getStatusCode());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.when(service.atualiza(
                                anyLong(),
                                any(StatusEntregaControllerEnum.class)
                        )
                )
                .thenReturn(
                        false
                );

        var controller = new EntregaController(service);

        // execução
        var produto = controller.atualizaStatus(1L, StatusEntregaControllerEnum.EM_TRANSPORTE);

        // avaliação
        Assertions.assertEquals(HttpStatus.NO_CONTENT, produto.getStatusCode());
    }

    @Test
    public void atualiza_ENTREGUE_deveRetornar200_salvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.when(service.atualiza(
                                anyLong(),
                                any(StatusEntregaControllerEnum.class)
                        )
                )
                .thenReturn(
                        true
                );

        var controller = new EntregaController(service);

        // execução
        var produto = controller.atualizaStatus(1L, StatusEntregaControllerEnum.ENTREGUE);

        // avaliação
        Assertions.assertEquals(HttpStatus.OK, produto.getStatusCode());
    }

    @Test
    public void atualiza_ENTREGUE_deveRetornar204_naoSalvaNaBaseDeDados() {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.when(service.atualiza(
                                anyLong(),
                                any(StatusEntregaControllerEnum.class)
                        )
                )
                .thenReturn(
                        false
                );

        var controller = new EntregaController(service);

        // execução
        var produto = controller.atualizaStatus(1L, StatusEntregaControllerEnum.ENTREGUE);

        // avaliação
        Assertions.assertEquals(HttpStatus.NO_CONTENT, produto.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_EMTRANSPORTE_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.doThrow(
                        new IllegalArgumentException("Campos inválidos!")
                )
                .when(service)
                .atualiza(
                        anyLong(),
                        any(StatusEntregaControllerEnum.class)
                );

        var controller = new EntregaController(service);

        // execução e avaliação
        var excecao = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            controller.atualizaStatus(idPedido, StatusEntregaControllerEnum.EM_TRANSPORTE);
        });
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_ENTREGUE_camposInvalidos_naoSalvaNaBaseDeDados(Long idPedido) {
        // preparação
        var service = Mockito.mock(EntregaUseCaseImpl.class);
        Mockito.doThrow(
                        new IllegalArgumentException("Campos inválidos!")
                )
                .when(service)
                .atualiza(
                        anyLong(),
                        any(StatusEntregaControllerEnum.class)
                );

        var controller = new EntregaController(service);

        // execução e avaliação
        var excecao = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            controller.atualizaStatus(idPedido, StatusEntregaControllerEnum.ENTREGUE);
        });
    }

}
