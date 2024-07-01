package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import com.fiap.techchallenge4.useCase.entrega.impl.EntregaUseCaseImpl;
import com.fiap.techchallenge4.useCase.entregador.EntregadorUseCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EntregaUseCaseTest {

    @Test
    public void prepara_salvaNaBaseDeDados() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);

        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );
        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new EntregaEntity(
                                1L,
                                "92084815061",
                                7894900011517L,
                                100L,
                                "67539918080",
                                null,
                                null,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador);

        // execução
        service.prepara(
                new PreparaEntregaDTO(
                        1L,
                        "92084815061",
                        7894900011517L,
                        100L
                )
        );

        // avaliação
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verify(serviceEntregador, times(1)).escolhe();
    }

    @Test
    public void prepara_naoSalvaNaBaseDeDados_entregaJaEstaSendoPreparada() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);

        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(
                        Optional.of(
                                new EntregaEntity(
                                        1L,
                                        "92084815061",
                                        7894900011517L,
                                        100L,
                                        "67539918080",
                                        null,
                                        null,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new EntregaEntity(
                                1L,
                                "92084815061",
                                7894900011517L,
                                100L,
                                "67539918080",
                                null,
                                null,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador);

        // execução
        service.prepara(
                new PreparaEntregaDTO(
                        1L,
                        "92084815061",
                        7894900011517L,
                        100L
                )
        );

        // avaliação
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(0)).escolhe();
    }

    @Test
    public void prepara_naoSalvaNaBaseDeDados_erroAoEscolherEntregador() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);

        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );
        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new EntregaEntity(
                                1L,
                                "92084815061",
                                7894900011517L,
                                100L,
                                "67539918080",
                                null,
                                null,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenThrow(RuntimeException.class);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.prepara(
                    new PreparaEntregaDTO(
                            1L,
                            "92084815061",
                            7894900011517L,
                            100L
                    )
            );
        });

        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(1)).escolhe();
    }

    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void cadastra_camposInvalidos_naoSalvaNaBaseDeDados(Long idDoPedido,
                                                               String cpfCliente,
                                                               Long ean,
                                                               Long quantidade) {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);

        Mockito.when(repository.findById(Mockito.any()))
                .thenReturn(
                        Optional.of(
                                new EntregaEntity(
                                        1L,
                                        "92084815061",
                                        7894900011517L,
                                        100L,
                                        "67539918080",
                                        null,
                                        null,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any()))
                .thenReturn(
                        new EntregaEntity(
                                1L,
                                "92084815061",
                                7894900011517L,
                                100L,
                                "67539918080",
                                null,
                                null,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.prepara(
                    new PreparaEntregaDTO(
                            idDoPedido,
                            cpfCliente,
                            ean,
                            quantidade
                    )
            );
        });

        verify(repository, times(0)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(0)).escolhe();
    }

    private static Stream<Arguments> requestValidandoCampos() {
        return Stream.of(
                Arguments.of(null, "92084815061", 123456789L, 100L),
                Arguments.of(-1L, "92084815061", 123456789L, 100L),
                Arguments.of(0L, "92084815061", 123456789L, 100L),
                Arguments.of(1L, null, 123456789L, 100L),
                Arguments.of(1L, "", 123456789L, 100L),
                Arguments.of(1L, " ", 123456789L, 100L),
                Arguments.of(1L, "teste", 123456789L, 100L),
                Arguments.of(1L, "1234567891", 123456789L, 100L),
                Arguments.of(1L, "123456789123", 123456789L, 100L),
                Arguments.of(1L, "92084815061", null, 100L),
                Arguments.of(1L, "92084815061", -1L, 100L),
                Arguments.of(1L, "92084815061", 0L, 100L),
                Arguments.of(1L, "92084815061", 123456789L, null),
                Arguments.of(1L, "92084815061", 123456789L, -1L),
                Arguments.of(1L, "92084815061", 123456789L, 0L),
                Arguments.of(1L, "92084815061", 123456789L, 1001L)
        );
    }

}
