package com.fiap.techchallenge4.bdd;

import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import static com.fiap.techchallenge4.infrasctructure.entrega.controller.EntregaController.URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS;
import static io.restassured.RestAssured.given;


public class AtualizaEntregaSteps {

    private Response response;
    private Long idPedido;
    private ClientAndServer mockServerCliente;

    @Dado("que informo uma entrega que ja foi cadastrada")
    public void queInformoUmaEntregaQueJaFoiCadastrada() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,%s,null,null,'CRIADO');
                """
                .formatted(idPedido, cpf));


        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega que não foi criada")
    public void queInformoUmaEntregaQueNaoFoiCriada() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);

        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega que esta com status diferente de CRIADO")
    public void queInformoUmaEntregaQueEstaComStatusDiferenteDeCriado() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,%s,null,null,'EM_TRANSPORTE');
                """
                .formatted(idPedido, cpf));


        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega com um cliente que não existe")
    public void queInformoUmaEntregaComUmClienteQueNaoExiste() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'12345678901','2024-06-26 22:57:46.037',7894900011517,30,%s,null,null,'CRIADO');
                """
                .formatted(idPedido, cpf));


        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega e a api de cliente esta com erro")
    public void queInformoUmaEntregaEAApiDeClienteEstaComErro() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'12345678902','2024-06-26 22:57:46.037',7894900011517,30,%s,null,null,'CRIADO');
                """
                .formatted(idPedido, cpf));


        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega com um entregador que não existe")
    public void queInformoUmaEntregaComUmEntregadorQueNaoExiste() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'11111111111',null,null,'CRIADO');
                """
                .formatted(idPedido));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Dado("que informo uma entrega que ja esta EM TRANSPORTE")
    public void queInformoUmaEntregaQueJaEstaEmTransporte() {
        final var jdbcTemplate = this.criaConexaoComBaseDeDados();

        this.idPedido = System.currentTimeMillis();
        final var timeString = Long.toString(idPedido);
        final var cpf = timeString.substring(1, 12);
        jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,%s,null,null,'EM_TRANSPORTE');
                """
                .formatted(idPedido, cpf));


        jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

        this.mockServerCliente = this.criaMockServerCliente();
    }

    @Quando("atualizo essa entrega para EM TRANSPORTE")
    public void atualizoEssaEntregaParaEmTransporte() {
        RestAssured.baseURI = "http://localhost:8082";
        this.response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                        .replace("{idDoPedido}", this.idPedido.toString())
                        .replace("{statusEntrega}", StatusEntregaControllerEnum.EM_TRANSPORTE.name())
                );
    }

    @Quando("atualizo essa entrega para ENTREGUE")
    public void atualizoEssaEntregaParaEntregue() {
        RestAssured.baseURI = "http://localhost:8082";
        this.response = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS
                        .replace("{idDoPedido}", this.idPedido.toString())
                        .replace("{statusEntrega}", StatusEntregaControllerEnum.ENTREGUE.name())
                );
    }

    @Entao("recebo uma resposta que a entrega foi atualizada para EM TRANSPORTE com sucesso")
    public void receboUmaRespostaQueAEntregaFoiAtualizadoParaEmTransporteComSucesso() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
        ;

        this.mockServerCliente.stop();
    }

    @Entao("recebo uma resposta que a entrega não foi atualizada para EM TRANSPORTE")
    public void receboUmaRespostaQueAEntregaNaoFoiAtualizadoParaEmTransporte() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
        ;

        this.mockServerCliente.stop();
    }

    @Entao("recebo uma resposta que a entrega foi atualizada para ENTREGUE com sucesso")
    public void receboUmaRespostaQueAEntregaFoiAtualizadoParaEntregueComSucesso() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.OK.value())
        ;

        this.mockServerCliente.stop();
    }

    @Entao("recebo uma resposta que a entrega não foi atualizada para ENTREGUE")
    public void receboUmaRespostaQueAEntregaNaoFoiAtualizadoParaEntregue() {
        this.response
                .prettyPeek()
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value())
        ;

        this.mockServerCliente.stop();
    }

    private JdbcTemplate criaConexaoComBaseDeDados() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5435/tech_challenge_4_logistica");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return new JdbcTemplate(dataSource);
    }

    private ClientAndServer criaMockServerCliente() {
        final var clientAndServer = ClientAndServer.startClientAndServer(8083);

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/cliente/71622958004")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(200)
                                .withBody("""
                                            {
                                                "cpf": "71622958004",
                                                "nome": "Cliente Teste",
                                                "enderecoLogradouro": "Rua Teste",
                                                "enderecoNumero": 123,
                                                "enderecoSiglaEstado": "SP",
                                                "dataDeCriacao": "2021-10-10T10:00:00"
                                            }
                                        """)
                );

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/cliente/12345678901")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(204)
                );

        clientAndServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/cliente/12345678902")
                )
                .respond(
                        HttpResponse.response()
                                .withContentType(org.mockserver.model.MediaType.APPLICATION_JSON)
                                .withStatusCode(500)
                );

        return clientAndServer;
    }

}
