package com.fiap.techchallenge4.integrados;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.domain.StatusEntregaEnum;
import com.fiap.techchallenge4.infrasctructure.cliente.client.ClienteClient;
import com.fiap.techchallenge4.infrasctructure.cliente.client.response.ClienteDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.controller.dto.AtualizaClienteDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.controller.dto.AtualizaPedidoDTO;
import com.fiap.techchallenge4.infrasctructure.entrega.model.EntregaEntity;
import com.fiap.techchallenge4.infrasctructure.entrega.repository.EntregaRepository;
import com.fiap.techchallenge4.infrasctructure.entregador.model.EntregadorEntity;
import com.fiap.techchallenge4.infrasctructure.entregador.repository.EntregadorRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;

import static com.fiap.techchallenge4.infrasctructure.entrega.controller.EntregaController.URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS;
import static org.mockito.Mockito.*;


@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EntregaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    EntregaRepository entregaRepository;

    @Autowired
    EntregadorRepository entregadorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @MockBean
    StreamBridge streamBridge;

    @Autowired
    @MockBean
    ClienteClient clientCliente;

    @BeforeEach
    void inicializaLimpezaDoDatabase() {
        this.entregaRepository.deleteAll();
        this.entregadorRepository.deleteAll();
    }

    @AfterAll
    void finalizaLimpezaDoDatabase() {
        this.entregaRepository.deleteAll();
        this.entregadorRepository.deleteAll();
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar200_salvaNaBaseDeDados() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras(null)
                .enderecoDeEntrega(null)
                .statusEntrega(StatusEntregaEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        final var entregador = EntregadorEntity.builder()
                .cpf(entregaSalva.getCpfEntregador())
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.clientCliente.pegaCliente(entregaSalva.getCpfCliente()))
                .thenReturn(
                        new ClienteDTO(
                                entregaSalva.getCpfCliente(),
                                "Nome de teste",
                                "Rua teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isOk()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertTrue(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());


        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertEquals("01:00", entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°100", entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.EM_TRANSPORTE, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados_entregaNaoCadastrada() throws Exception {
        final var entregador = EntregadorEntity.builder()
                .cpf("04087281086")
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.clientCliente.pegaCliente("92084815061"))
                .thenReturn(
                        new ClienteDTO(
                                "92084815061",
                                "Nome de teste",
                                "Rua teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        "92084815061",
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertFalse(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());

        Assertions.assertEquals(0, this.entregaRepository.findAll().size());
        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        verify(clientCliente, times(0)).pegaCliente(Mockito.any());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados_entregaComStatusDiferenteDeCRIADO() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras("01:00")
                .enderecoDeEntrega("Rua teste - N°100")
                .statusEntrega(StatusEntregaEnum.EM_TRANSPORTE)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        final var entregador = EntregadorEntity.builder()
                .cpf("04087281086")
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.clientCliente.pegaCliente("92084815061"))
                .thenReturn(
                        new ClienteDTO(
                                "92084815061",
                                "Nome de teste",
                                "Rua teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        "92084815061",
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertFalse(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());

        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertEquals("01:00", entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°100", entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.EM_TRANSPORTE, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        verify(clientCliente, times(0)).pegaCliente(Mockito.any());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados_clienteNaoEncontrado() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras(null)
                .enderecoDeEntrega(null)
                .statusEntrega(StatusEntregaEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        final var entregador = EntregadorEntity.builder()
                .cpf(entregaSalva.getCpfEntregador())
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.clientCliente.pegaCliente(entregaSalva.getCpfCliente()))
                .thenReturn(null);
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertFalse(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());


        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertNull(entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertNull(entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.CRIADO, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados_erroNaApiDeCliente() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras(null)
                .enderecoDeEntrega(null)
                .statusEntrega(StatusEntregaEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        final var entregador = EntregadorEntity.builder()
                .cpf(entregaSalva.getCpfEntregador())
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(false)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.doThrow(
                        new RuntimeException("API INDISPONIVEL!!")
                )
                .when(this.clientCliente)
                .pegaCliente("92084815061");
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertFalse(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());


        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertNull(entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertNull(entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.CRIADO, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
    }

    @Test
    public void atualiza_EMTRANSPORTE_deveRetornar204_naoSalvaNaBaseDeDados_entregadorNaoEncontrado() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras(null)
                .enderecoDeEntrega(null)
                .statusEntrega(StatusEntregaEnum.CRIADO)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        Mockito.when(this.clientCliente.pegaCliente(entregaSalva.getCpfCliente()))
                .thenReturn(
                        new ClienteDTO(
                                entregaSalva.getCpfCliente(),
                                "Nome de teste",
                                "Rua teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.EM_TRANSPORTE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        Assertions.assertEquals(0, this.entregadorRepository.findAll().size());

        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertNull(entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertNull(entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.CRIADO, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        verify(clientCliente, times(1)).pegaCliente(Mockito.any());
    }

    @Test
    public void atualiza_ENTREGUE_deveRetornar200_salvaNaBaseDeDados() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras("01:00")
                .enderecoDeEntrega("Rua teste - N°100")
                .statusEntrega(StatusEntregaEnum.EM_TRANSPORTE)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        final var entregador = EntregadorEntity.builder()
                .cpf(entregaSalva.getCpfEntregador())
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(true)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.ENTREGUE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isOk()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada() + 1, entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertFalse(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());


        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertEquals("01:00", entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°100", entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.ENTREGUE, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(2)).send(Mockito.any(), Mockito.any());
        verifyNoInteractions(clientCliente);
    }

    @Test
    public void atualiza_ENTREGUE_deveRetornar204_naoSalvaNaBaseDeDados_entregaNaoCadastrada() throws Exception {
        final var entregador = EntregadorEntity.builder()
                .cpf("04087281086")
                .quantidadeDeEntregaRealizada(0L)
                .estaEmEntrega(true)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregadorSalvo = this.entregadorRepository.save(entregador);

        Mockito.when(this.clientCliente.pegaCliente("92084815061"))
                .thenReturn(
                        new ClienteDTO(
                                "92084815061",
                                "Nome de teste",
                                "Rua teste",
                                100,
                                "SP",
                                LocalDateTime.now()
                        )
                );
        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        "92084815061",
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.ENTREGUE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        final var entregadorEntity = this.entregadorRepository.findById("04087281086").get();
        Assertions.assertEquals(entregadorSalvo.getCpf(), entregadorEntity.getCpf());
        Assertions.assertEquals(entregadorSalvo.getQuantidadeDeEntregaRealizada(), entregadorEntity.getQuantidadeDeEntregaRealizada());
        Assertions.assertTrue(entregadorEntity.getEstaEmEntrega());
        Assertions.assertNotNull(entregadorEntity.getDataDeCriacao());

        Assertions.assertEquals(0, this.entregaRepository.findAll().size());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        verifyNoInteractions(clientCliente);
    }

    @Test
    public void atualiza_ENTREGUE_deveRetornar204_naoSalvaNaBaseDeDados_naoEncontraEntregador() throws Exception {
        final var entrega = EntregaEntity.builder()
                .idDoPedido(1L)
                .cpfCliente("92084815061")
                .ean(7894900011517L)
                .quantidadeDoProduto(30L)
                .cpfEntregador("04087281086")
                .tempoEstimadoDeEntregaEmHoras("01:00")
                .enderecoDeEntrega("Rua teste - N°100")
                .statusEntrega(StatusEntregaEnum.EM_TRANSPORTE)
                .dataDeCriacao(LocalDateTime.now())
                .build();
        final var entregaSalva = this.entregaRepository.save(entrega);

        Mockito.when(this.streamBridge.send("cliente-atualiza-status", new AtualizaClienteDTO(
                        entregaSalva.getCpfCliente(),
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );
        Mockito.when(this.streamBridge.send("pedido-atualiza-status", new AtualizaPedidoDTO(
                        1L,
                        StatusEntregaControllerEnum.ENTREGUE)))
                .thenReturn(
                        true
                );

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", "1")
                                        .replace("{statusEntrega}", StatusEntregaEnum.ENTREGUE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isNoContent()
                )
                .andReturn();

        Assertions.assertEquals(0, this.entregadorRepository.findAll().size());


        final var entregaEntity = this.entregaRepository.findById(1L).get();

        Assertions.assertEquals(1, this.entregaRepository.findAll().size());
        Assertions.assertEquals(1L, entregaEntity.getIdDoPedido());
        Assertions.assertEquals("92084815061", entregaEntity.getCpfCliente());
        Assertions.assertEquals(7894900011517L, entregaEntity.getEan());
        Assertions.assertEquals(30L, entregaEntity.getQuantidadeDoProduto());
        Assertions.assertEquals("01:00", entregaEntity.getTempoEstimadoDeEntregaEmHoras());
        Assertions.assertEquals("Rua teste - N°100", entregaEntity.getEnderecoDeEntrega());
        Assertions.assertEquals(StatusEntregaEnum.EM_TRANSPORTE, entregaEntity.getStatusEntrega());
        Assertions.assertNotNull(entregaEntity.getDataDeCriacao());

        verify(streamBridge, times(0)).send(Mockito.any(), Mockito.any());
        verifyNoInteractions(clientCliente);
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_EMTRANSPORTE_camposInvalidos_naoSalvaNaBaseDeDados(Long idDoPedido) throws Exception {

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                .replace("{idDoPedido}", idDoPedido.toString())
                                .replace("{statusEntrega}", StatusEntregaEnum.EM_TRANSPORTE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest()
                );
    }

    @ParameterizedTest
    @ValueSource(longs = {
            -1000,
            -1L,
            0
    })
    public void atualiza_ENTREGUE_camposInvalidos_naoSalvaNaBaseDeDados(Long idDoPedido) throws Exception {

        this.mockMvc
                .perform(MockMvcRequestBuilders.put(
                                URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                                        .replace("{idDoPedido}", idDoPedido.toString())
                                        .replace("{statusEntrega}", StatusEntregaEnum.ENTREGUE.name())
                        )
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers
                        .status()
                        .isBadRequest()
                );
    }

}
