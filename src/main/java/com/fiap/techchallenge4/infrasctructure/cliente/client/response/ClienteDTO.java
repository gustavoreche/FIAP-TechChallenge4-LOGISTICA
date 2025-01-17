package com.fiap.techchallenge4.infrasctructure.cliente.client.response;

import java.time.LocalDateTime;

public record ClienteDTO(

		String cpf,
		String nome,
		String enderecoLogradouro,
		Integer enderecoNumero,
		String enderecoSiglaEstado,
		LocalDateTime dataDeCriacao
) {}
