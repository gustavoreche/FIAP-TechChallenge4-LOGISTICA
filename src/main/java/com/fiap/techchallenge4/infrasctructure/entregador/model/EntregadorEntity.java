package com.fiap.techchallenge4.infrasctructure.entregador.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_entregador")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntregadorEntity {

    @Id
    private String cpf;
    private Long quantidadeDeEntregaRealizada;
    private Boolean estaEmEntrega;
    private LocalDateTime dataDeCriacao;

}
