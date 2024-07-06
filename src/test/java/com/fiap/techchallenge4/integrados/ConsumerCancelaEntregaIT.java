package com.fiap.techchallenge4.integrados;

import com.fiap.techchallenge4.domain.StatusEntregaEnum;
import com.fiap.techchallenge4.infrasctructure.consumer.response.CancelaEntregaDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConsumerCancelaEntregaIT {

    @Autowired
    private StreamBridge streamBridge;

    @Autowired
    EntregaRepository repository;

    @BeforeEach
    void inicializaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @AfterAll
    void finalizaLimpezaDoDatabase() {
        this.repository.deleteAll();
    }

    @Test
    public void cancela_salvaNaBaseDeDados() throws ExecutionException, InterruptedException {

        this.repository.save(
                new EntregaEntity(
                        1L,
                        "92084815061",
                        1234567890L,
                        100L,
                        "67539918080",
                        null,
                        null,
                        StatusEntregaEnum.CRIADO,
                        LocalDateTime.now()
                )
        );
        this.streamBridge
                .send("logistica-cancela-entrega", new CancelaEntregaDTO(
                                1L
                        )
                );

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-cancela-entrega", new CancelaEntregaDTO(
                            1L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(1L, entrega.getIdDoPedido());
        Assertions.assertEquals("92084815061", entrega.getCpfCliente());
        Assertions.assertEquals(1234567890L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidadeDoProduto());
        Assertions.assertEquals("67539918080", entrega.getCpfEntregador());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
        Assertions.assertNull(entrega.getEnderecoDeEntrega());
        Assertions.assertNull(entrega.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals(StatusEntregaEnum.CANCELADO, entrega.getStatusEntrega());
    }

    @Test
    public void cancela_naoSalvaNaBaseDeDados_entregaJaEstaSendoPreparada() throws ExecutionException, InterruptedException {

        this.repository.save(
                new EntregaEntity(
                        1L,
                        "92084815061",
                        1234567890L,
                        100L,
                        "67539918080",
                        null,
                        null,
                        StatusEntregaEnum.EM_TRANSPORTE,
                        LocalDateTime.now()
                )
        );

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-cancela-entrega", new CancelaEntregaDTO(
                                    1L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        var entrega = this.repository.findAll().get(0);

        Assertions.assertEquals(1, this.repository.findAll().size());
        Assertions.assertEquals(1L, entrega.getIdDoPedido());
        Assertions.assertEquals("92084815061", entrega.getCpfCliente());
        Assertions.assertEquals(1234567890L, entrega.getEan());
        Assertions.assertEquals(100L, entrega.getQuantidadeDoProduto());
        Assertions.assertEquals("67539918080", entrega.getCpfEntregador());
        Assertions.assertNotNull(entrega.getDataDeCriacao());
        Assertions.assertNull(entrega.getEnderecoDeEntrega());
        Assertions.assertNull(entrega.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals(StatusEntregaEnum.EM_TRANSPORTE, entrega.getStatusEntrega());
    }

    @Test
    public void cancela_naoSalvaNaBaseDeDados_naoEncontraEntrega() throws ExecutionException, InterruptedException {

        var producer = CompletableFuture.runAsync(() -> {
            this.streamBridge
                    .send("logistica-cancela-entrega", new CancelaEntregaDTO(
                                    1L
                            )
                    );
        });

        producer.get();
        Thread.sleep(2000);

        Assertions.assertEquals(0, this.repository.findAll().size());
    }

}
