package com.fiap.techchallenge4.unitario;

import com.fiap.techchallenge4.infrasctructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrasctructure.cliente.client.response.ClienteDTO;
import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import com.fiap.techchallenge4.useCase.entregador.impl.EntregadorUseCaseImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

public class EntregadorUseCaseTest {

    @Test
    public void escolhe_sucesso() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

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

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução
        final var cpfEntregador = service.escolhe();

        // avaliação
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        Assertions.assertEquals("67539918080", cpfEntregador);
        verifyNoInteractions(client);
    }

    @Test
    public void escolhe_sucesso_maisDeUmEntregadorLivre_escolhePeloEntregadorQueTemMenosQuantidades() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

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

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução
        final var cpfEntregador = service.escolhe();

        // avaliação
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        Assertions.assertEquals("51102108022", cpfEntregador);
        verifyNoInteractions(client);
    }

    @Test
    public void escolhe_naoEncontraEntrega() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(repository.findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false))
                .thenReturn(
                        List.of()
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, service::escolhe);
        verify(repository, times(1)).findByEstaEmEntregaOrderByQuantidadeDeEntregaRealizadaAsc(false);
        verifyNoInteractions(client);
    }

    @Test
    public void pegaInformacoesDoEndereco_sucesso() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(client.pegaCliente("67539918080"))
                .thenReturn(
                        new ClienteDTO(
                                "67539918080",
                                "Cliente teste",
                                "Rua teste",
                                1234,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.of(
                                new EntregadorEntity(
                                        "12345678901",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução
        final var informacoesDoEndereco = service
                .pegaInformacoesDoEndereco("67539918080", "12345678901");

        // avaliação
        verify(client, times(1)).pegaCliente(Mockito.any());
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        Assertions.assertEquals("01:00", informacoesDoEndereco.tempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°1234", informacoesDoEndereco.enderecoDeEntrega());
    }

    @ParameterizedTest
    @MethodSource("siglasDosEstadosEHorarios")
    public void pegaInformacoesDoEndereco_sucesso(String siglaEstado,
                                                  String horarioEstimativa) {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(client.pegaCliente("67539918080"))
                .thenReturn(
                        new ClienteDTO(
                                "67539918080",
                                "Cliente teste",
                                "Rua teste",
                                1234,
                                siglaEstado,
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.of(
                                new EntregadorEntity(
                                        "12345678901",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução
        final var informacoesDoEndereco = service
                .pegaInformacoesDoEndereco("67539918080", "12345678901");

        // avaliação
        verify(client, times(1)).pegaCliente(Mockito.any());
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
        Assertions.assertEquals(horarioEstimativa, informacoesDoEndereco.tempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°1234", informacoesDoEndereco.enderecoDeEntrega());
    }

    @Test
    public void pegaInformacoesDoEndereco_clienteNaoEncontrado() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(client.pegaCliente("67539918080"))
                .thenReturn(null);
        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.of(
                                new EntregadorEntity(
                                        "12345678901",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.pegaInformacoesDoEndereco("67539918080", "12345678901");
        });

        verify(client, times(1)).pegaCliente(Mockito.any());
        verify(repository, times(0)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
    }

    @Test
    public void pegaInformacoesDoEndereco_erroNaApiDeCliente() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.doThrow(
                        new RuntimeException("API INDISPONIVEL!!")
                )
                .when(client)
                .pegaCliente("67539918080");
        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.of(
                                new EntregadorEntity(
                                        "12345678901",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.pegaInformacoesDoEndereco("67539918080", "12345678901");
        });

        verify(client, times(1)).pegaCliente(Mockito.any());
        verify(repository, times(0)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
    }

    @Test
    public void pegaInformacoesDoEndereco_entregadorNaoEncontrado() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(client.pegaCliente("67539918080"))
                .thenReturn(
                        new ClienteDTO(
                                "67539918080",
                                "Cliente teste",
                                "Rua teste",
                                1234,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.empty()
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.pegaInformacoesDoEndereco("67539918080", "12345678901");
        });

        verify(client, times(1)).pegaCliente(Mockito.any());
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
    }

    @Test
    public void defineEntregadorComoDisponivel_sucesso() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.of(
                                new EntregadorEntity(
                                        "12345678901",
                                        30L,
                                        false,
                                        LocalDateTime.now()
                                )
                        )
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução
        service.defineEntregadorComoDisponivel("12345678901");

        // avaliação
        verifyNoInteractions(client);
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(1)).save(Mockito.any());
    }

    @Test
    public void defineEntregadorComoDisponivel_naoEncontraEntregador() {
        // preparação
        var repository = Mockito.mock(EntregadorRepository.class);
        var client = Mockito.mock(ClienteClient.class);

        Mockito.when(repository.findById("12345678901"))
                .thenReturn(
                        Optional.empty()
                );
        Mockito.when(repository.save(Mockito.any(EntregadorEntity.class)))
                .thenReturn(
                        new EntregadorEntity(
                                "12345678901",
                                30L,
                                true,
                                LocalDateTime.now()
                        )
                );

        var service = new EntregadorUseCaseImpl(repository, client);

        // execução e avaliação
        var excecao = Assertions.assertThrows(RuntimeException.class, () -> {
            service.defineEntregadorComoDisponivel("12345678901");
        });

        // avaliação
        verifyNoInteractions(client);
        verify(repository, times(1)).findById(Mockito.any());
        verify(repository, times(0)).save(Mockito.any());
    }

    private static Stream<Arguments> siglasDosEstadosEHorarios() {
        return Stream.of(
                Arguments.of("SP", "01:00"),
                Arguments.of("RJ", "01:00"),
                Arguments.of("MG", "01:00"),
                Arguments.of("ES", "01:00"),
                Arguments.of("PR", "02:00"),
                Arguments.of("RS", "02:00"),
                Arguments.of("SC", "02:00"),
                Arguments.of("DF", "02:30"),
                Arguments.of("GO", "02:30"),
                Arguments.of("MT", "02:30"),
                Arguments.of("MS", "02:30"),
                Arguments.of("AL", "03:00"),
                Arguments.of("BA", "03:00"),
                Arguments.of("CE", "03:00"),
                Arguments.of("MA", "03:00"),
                Arguments.of("PB", "03:00"),
                Arguments.of("PE", "03:00"),
                Arguments.of("PI", "03:00"),
                Arguments.of("RN", "03:00"),
                Arguments.of("SE", "03:00"),
                Arguments.of("AC", "04:00"),
                Arguments.of("AP", "04:00"),
                Arguments.of("AM", "04:00"),
                Arguments.of("PA", "04:00"),
                Arguments.of("RO", "04:00"),
                Arguments.of("RR", "04:00"),
                Arguments.of("TO", "04:00"),
                Arguments.of("XX", "Sem estimativa de entrega")
        );
    }

}
