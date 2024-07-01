package com.fiap.techchallenge4.infrasctructure.consumer.response;

public record PreparaEntregaDTO(
		Long idDoPedido,
		String cpfCliente,
		Long ean,
		Long quantidade
) {}
