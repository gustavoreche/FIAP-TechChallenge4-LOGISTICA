package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.domain.InformacoesDoEndereco;
import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.domain.StatusEntregaEnum;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class EntregaUseCaseTest {

    @Test
    public void prepara_salvaNaBaseDeDados() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

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
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void prepara_naoSalvaNaBaseDeDados_entregaJaEstaSendoPreparada() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

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
                                        StatusEntregaEnum.CRIADO,
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

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
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void prepara_naoSalvaNaBaseDeDados_erroAoEscolherEntregador() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenThrow(RuntimeException.class);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

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
        verifyNoInteractions(streamBridge);
    }

    @Test
    public void atualiza_EMTRANSPORTE_salvaNaBaseDeDados() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
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
                                        StatusEntregaEnum.EM_TRANSPORTE,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(serviceEntregador.pegaInformacoesDoEndereco(Mockito.any(), Mockito.any()))
                .thenReturn(
                        new InformacoesDoEndereco(
                                "SP",
                                "Rua Teste - N°100"
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução
        final var atualiza = service.atualiza(
                1L,
                StatusEntregaControllerEnum.EM_TRANSPORTE
        );

        // avaliação
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verify(serviceEntregador, times(1)).pegaInformacoesDoEndereco(Mockito.any(), Mockito.any());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
        Assertions.assertTrue(atualiza);
    }

    @Test
    public void atualiza_EMTRANSPORTE_naoSalvaNaBaseDeDados_entregaNaoCadastrada() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );
        Mockito.when(serviceEntregador.pegaInformacoesDoEndereco(Mockito.any(), Mockito.any()))
                .thenReturn(
                        new InformacoesDoEndereco(
                                "SP",
                                "Rua Teste - N°100"
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução
        final var atualiza = service.atualiza(
                1L,
                StatusEntregaControllerEnum.EM_TRANSPORTE
        );

        // avaliação
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(0)).pegaInformacoesDoEndereco(Mockito.any(), Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        Assertions.assertFalse(atualiza);
    }

    @Test
    public void atualiza_EMTRANSPORTE_naoSalvaNaBaseDeDados_algumErroParaPegarEnderecoDoCliente() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
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
                                        StatusEntregaEnum.EM_TRANSPORTE,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(serviceEntregador.pegaInformacoesDoEndereco(Mockito.any(), Mockito.any()))
                .thenThrow(RuntimeException.class);
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução e avaliação
        final var atualizou = service.atualiza(
                1L,
                StatusEntregaControllerEnum.EM_TRANSPORTE
        );

        Assertions.assertFalse(atualizou);
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(1)).pegaInformacoesDoEndereco(Mockito.any(), Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualiza_ENTREGUE_salvaNaBaseDeDados() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
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
                                        StatusEntregaEnum.ENTREGUE,
                                        LocalDateTime.now()
                                )
                        )
                );

        Mockito.doNothing().when(serviceEntregador).defineEntregadorComoDisponivel(Mockito.any());

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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução
        final var atualiza = service.atualiza(
                1L,
                StatusEntregaControllerEnum.ENTREGUE
        );

        // avaliação
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        verify(serviceEntregador, times(1)).defineEntregadorComoDisponivel(Mockito.any());
        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
        Assertions.assertTrue(atualiza);
    }

    @Test
    public void atualiza_ENTREGUE_naoSalvaNaBaseDeDados_entregaNaoCadastrada() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
                .thenReturn(
                        Optional.empty()
                );

        Mockito.doNothing().when(serviceEntregador).defineEntregadorComoDisponivel(Mockito.any());

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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução
        final var atualiza = service.atualiza(
                1L,
                StatusEntregaControllerEnum.ENTREGUE
        );

        // avaliação
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(0)).defineEntregadorComoDisponivel(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        Assertions.assertFalse(atualiza);
    }

    @Test
    public void atualiza_ENTREGUE_naoSalvaNaBaseDeDados_naoEncontraEntregador() {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
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
                                        StatusEntregaEnum.EM_TRANSPORTE,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.doThrow(RuntimeException.class)
                .when(serviceEntregador).defineEntregadorComoDisponivel(Mockito.any());
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(streamBridge.send(Mockito.any(), Mockito.any()))
                .thenReturn(true);

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução e avaliação
        final var atualizou = service.atualiza(
                1L,
                StatusEntregaControllerEnum.ENTREGUE
        );

        Assertions.assertFalse(atualizou);
        verify(repository, times(1)).findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(1)).defineEntregadorComoDisponivel(Mockito.any());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @ParameterizedTest
    @MethodSource("requestValidandoCampos")
    public void prepara_camposInvalidos_naoSalvaNaBaseDeDados(Long idDoPedido,
                                                              String cpfCliente,
                                                              Long ean,
                                                              Long quantidade) {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

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
                                        StatusEntregaEnum.CRIADO,
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
                                StatusEntregaEnum.CRIADO,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(serviceEntregador.escolhe())
                .thenReturn("67539918080");

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

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
        verifyNoInteractions(streamBridge);
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_camposInvalidos_naoSalvaNaBaseDeDados(Long idDoPedido) {
        // preparação
        var repository = Mockito.mock(EntregaRepository.class);
        var serviceEntregador = Mockito.mock(EntregadorUseCase.class);
        var streamBridge = Mockito.mock(StreamBridge.class);

        Mockito.when(repository.findByIdDoPedidoAndStatusEntrega(Mockito.any(), Mockito.any()))
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
                                        StatusEntregaEnum.CRIADO,
                                        LocalDateTime.now()
                                )
                        )
                );

        var service = new EntregaUseCaseImpl(repository, serviceEntregador, streamBridge);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.atualiza(
                    idDoPedido == -1000 ? null : idDoPedido,
                    StatusEntregaControllerEnum.EM_TRANSPORTE
            );
        });

        verify(repository, times(0)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
        verify(serviceEntregador, times(0)).escolhe();
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
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
