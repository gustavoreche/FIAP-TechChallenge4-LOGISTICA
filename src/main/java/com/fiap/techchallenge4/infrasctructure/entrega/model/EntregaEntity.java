package com.fiap.techchallenge4.infrasctructure.entrega.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_entrega")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cpfCliente;
    private Long ean;
    private Long quantidadeDoProduto;
    private String cpfEntregador;
    private String tempoEstimadoDeEntregaEmHoras;
    private String enderecoDeEntrega;
    private LocalDateTime dataDeCriacao;

}
