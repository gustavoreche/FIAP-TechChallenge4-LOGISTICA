package com.fiap.techchallenge4.integrados;

import com.fiap.techchallenge4.infrasctructure.consumer.response.PreparaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConsumerPreparaEntregaIT {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    EntregadorRepository repositoryEntregador;

    @Autowired
    EntregaRepository repositoryEntrega;

    @BeforeEach
    void inicializaLimpezaDoDatabase() {
        this.repositoryEntregador.deleteAll();
        this.repositoryEntrega.deleteAll();
    }

    @AfterAll
    void finalizaLimpezaDoDatabase() {
        this.repositoryEntregador.deleteAll();
        this.repositoryEntrega.deleteAll();
    }

    @Test
    public void prepara_salvaNaBaseDeDados() throws ExecutionException, InterruptedException {

        this.repositoryEntregador.save(EntregadorEntity.builder()
                .cpf("67539918080")
                .quantidadeDeEntregaRealizada(30L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build());

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-prepara-entrega", new PreparaEntregaDTO(
                            1L,
                            "92084815061",
                            1234567890L,
                            10L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repositoryEntrega.findAll().get(0);

        Assertions.assertEquals("92084815061", entrega.getCpfCliente());
        Assertions.assertEquals(1234567890L, entrega.getEan());
        Assertions.assertEquals(10L, entrega.getQuantidadeDoProduto());
        Assertions.assertEquals("67539918080", entrega.getCpfEntregador());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
        Assertions.assertNull(entrega.getEnderecoDeEntrega());
        Assertions.assertNull(entrega.getTempoEstimadoDeEntregaEmHoras());
    }

    @Test
    public void prepara_salvaNaBaseDeDados_comMaisDeUmEntregadorDisponivel() throws ExecutionException, InterruptedException {
        final var entregador1 = EntregadorEntity.builder()
                .cpf("67539918080")
                .quantidadeDeEntregaRealizada(30L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregador2 = EntregadorEntity.builder()
                .cpf("51102108022")
                .quantidadeDeEntregaRealizada(20L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregador3 = EntregadorEntity.builder()
                .cpf("04087281086")
                .quantidadeDeEntregaRealizada(10L)
                .estaEmEntrega(true)
                .dataDeCriacao(LocalDateTime.now())
                .build();

        final var entregadores = List.of(entregador1, entregador2, entregador3);
        this.repositoryEntregador.saveAll(entregadores);

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-prepara-entrega", new PreparaEntregaDTO(
                                    1L,
                                    "92084815061",
                                    1234567890L,
                                    10L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repositoryEntrega.findAll().get(0);

        Assertions.assertEquals("92084815061", entrega.getCpfCliente());
        Assertions.assertEquals(1234567890L, entrega.getEan());
        Assertions.assertEquals(10L, entrega.getQuantidadeDoProduto());
        Assertions.assertEquals("51102108022", entrega.getCpfEntregador());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
        Assertions.assertNull(entrega.getEnderecoDeEntrega());
        Assertions.assertNull(entrega.getTempoEstimadoDeEntregaEmHoras());
    }

    @Test
    public void prepara_naoSalvaNaBaseDeDados_naoEncontraEntregador() throws ExecutionException, InterruptedException {
        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-prepara-entrega", new PreparaEntregaDTO(
                                    1L,
                                    "92084815061",
                                    1234567890L,
                                    10L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        Assertions.assertEquals(0, this.repositoryEntrega.findAll().size());
    }

    @Test
    public void prepara_salvaNaBaseDeDados_entregaJaEstaSendoPreparada() throws ExecutionException, InterruptedException {

        this.repositoryEntrega.save(EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(1234567890L)
                .quantidadeDoProduto(10L)
                .cpfEntregador("51102108022")
                .tempoEstimadoDeEntregaEmHoras(null)
                .enderecoDeEntrega(null)
                .dataDeCriacao(LocalDateTime.now())
                .build());

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-prepara-entrega", new PreparaEntregaDTO(
                                    1L,
                                    "92084815061",
                                    1234567890L,
                                    10L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repositoryEntrega.findAll().get(0);

        Assertions.assertEquals("92084815061", entrega.getCpfCliente());
        Assertions.assertEquals(1234567890L, entrega.getEan());
        Assertions.assertEquals(10L, entrega.getQuantidadeDoProduto());
        Assertions.assertEquals("51102108022", entrega.getCpfEntregador());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
        Assertions.assertNull(entrega.getEnderecoDeEntrega());
        Assertions.assertNull(entrega.getTempoEstimadoDeEntregaEmHoras());
    }

}
