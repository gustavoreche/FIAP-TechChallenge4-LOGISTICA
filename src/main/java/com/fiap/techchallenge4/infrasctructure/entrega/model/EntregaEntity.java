package com.fiap.techchallenge4.infrasctructure.entrega.model;

import com.fiap.techchallenge4.domain.StatusEntregaEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_entrega")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregaEntity {

    @Id
    private Long idDoPedido;
    private String cpfCliente;
    private Long ean;
    private Long quantidadeDoProduto;
    private String cpfEntregador;
    private String tempoEstimadoDeEntregaEmHoras;
    private String enderecoDeEntrega;
    @Enumerated(EnumType.STRING)
    private StatusEntregaEnum statusEntrega;
    private LocalDateTime dataDeCriacao;

}
