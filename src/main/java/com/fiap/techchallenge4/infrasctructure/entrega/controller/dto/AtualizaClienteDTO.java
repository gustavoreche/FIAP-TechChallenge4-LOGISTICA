package com.fiap.techchallenge4.infrasctructure.entrega.controller.dto;

import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;

public record AtualizaClienteDTO(
		String cpf,
		StatusEntregaControllerEnum statusEntrega
) {}
