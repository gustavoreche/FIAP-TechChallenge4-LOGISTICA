package com.fiap.techchallenge4.performance;

import io.gatling.javaapi.core.ActionBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;


public class PerformanceTestSimulation extends Simulation {

    private final JdbcTemplate jdbcTemplate = this.criaConexaoComBaseDeDados();
    private final ClientAndServer mockServerCliente = this.criaMockServerCliente();
    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl("http://localhost:8082");

    ActionBuilder atualizaPedidoEMTRANSPORTERequest = http("Atualiza para entrega EM TRANSPORTE")
            .put("/entrega/${idPedido}/EM_TRANSPORTE")
            .header("Content-Type", "application/json")
            .check(status().is(200));

    ActionBuilder atualizaPedidoENTREGUERequest = http("Atualiza para entrega ENTREGUE")
            .put("/entrega/${idPedido}/ENTREGUE")
            .header("Content-Type", "application/json")
            .check(status().is(200));

    ScenarioBuilder cenarioAtualizaEntregaEMTRANSPORTE = scenario("Atualiza para entrega EM TRANSPORTE")
            .exec(session -> {
                long idPedido = System.currentTimeMillis();

                jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'04087281086',null,null,'CRIADO');
                """
                        .formatted(idPedido));

                final var timeString = Long.toString(idPedido);
                final var cpf = timeString.substring(1, 12);

                jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','false');
                """.formatted(cpf));

                return session.set("idPedido", idPedido);
            })
            .exec(atualizaPedidoEMTRANSPORTERequest);

    ScenarioBuilder cenarioAtualizaEntregaENTREGUE = scenario("Atualiza para entrega ENTREGUE")
            .exec(session -> {
                long idPedido = System.currentTimeMillis() + 222222222L;

                jdbcTemplate.execute("""
                INSERT INTO tb_entrega (id_do_pedido, cpf_cliente,data_de_criacao,ean,quantidade_do_produto,
                cpf_entregador,tempo_estimado_de_entrega_em_horas,endereco_de_entrega,status_entrega) VALUES
                	 (%s,'71622958004','2024-06-26 22:57:46.037',7894900011517,30,'04087281086','01:00','Rua teste - N°100','EM_TRANSPORTE');
                """
                        .formatted(idPedido));

                final var timeString = Long.toString(idPedido);
                final var cpf = timeString.substring(1, 12);
                jdbcTemplate.execute("""
                INSERT INTO tb_entregador (cpf, quantidade_de_entrega_realizada,data_de_criacao,esta_em_entrega) VALUES
                	 (%s,0,'2024-06-26 22:57:46.037','true');
                """.formatted(cpf));

                return session.set("idPedido", idPedido);
            })
            .exec(atualizaPedidoENTREGUERequest);


    {

        setUp(
                cenarioAtualizaEntregaEMTRANSPORTE.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10))),
                cenarioAtualizaEntregaENTREGUE.injectOpen(
                        rampUsersPerSec(1)
                                .to(10)
                                .during(Duration.ofSeconds(10)),
                        constantUsersPerSec(10)
                                .during(Duration.ofSeconds(20)),
                        rampUsersPerSec(10)
                                .to(1)
                                .during(Duration.ofSeconds(10)))
        )
                .protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(600),
                        global().failedRequests().count().is(0L));

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
                                .withContentType(MediaType.APPLICATION_JSON)
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

        return clientAndServer;
    }

}