package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import com.fiap.techchallenge4.useCase.entregador.impl.EntregadorUseCaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EntregadorUseCaseTest {

    @Test
    public void escolhe_sucesso() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);

        Mockito.when(repository.findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false))
                .thenReturn(
                        List.of(
                                new EntregadorEntity(
                                        "67539918080",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );

        var service = new EntregadorUseCaseImpl(repository);

        // execução
        final var cpfEntregador = service.escolhe();

        // avaliação
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        Assertions.assertEquals("67539918080", cpfEntregador);
    }

    @Test
    public void escolhe_sucesso_maisDeUmEntregadorLivre_escolhePeloEntregadorQueTemMenosQuantidades() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);

        Mockito.when(repository.findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false))
                .thenReturn(
                        List.of(
                                new EntregadorEntity(
                                        "51102108022",
                                        20L,
                                        false,
                                        LocalDateTime.now()
                                ),
                                new EntregadorEntity(
                                        "67539918080",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );

        var service = new EntregadorUseCaseImpl(repository);

        // execução
        final var cpfEntregador = service.escolhe();

        // avaliação
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        Assertions.assertEquals("51102108022", cpfEntregador);
    }

    @Test
    public void escolhe_naoEncontraEntrega() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);

        Mockito.when(repository.findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false))
                .thenReturn(
                        List.of()
                );

        var service = new EntregadorUseCaseImpl(repository);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, service::escolhe);
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
    }

}
