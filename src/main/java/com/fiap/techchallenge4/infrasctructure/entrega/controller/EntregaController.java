package com.fiap.techchallenge4.infrasctructure.entrega.controller;

import com.fiap.techchallenge4.domain.StatusEntregaControllerEnum;
import com.fiap.techchallenge4.useCase.entrega.EntregaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.fiap.techchallenge4.infrasctructure.entrega.controller.EntregaController.URL_ENTREGA;

@Tag(
		name = "Logistica",
		description = "Serviço para realizar o gerenciamento de entregas dos pedidos no sistema"
)
@RestController
@RequestMapping(URL_ENTREGA)
public class EntregaController {

	public static final String URL_ENTREGA = "/entrega";
	public static final String URL_ENTREGA_COM_ID_DO_PEDIDO_E_STATUS = URL_ENTREGA + "/{idDoPedido}/{statusEntrega}";

	private final EntregaUseCase service;

	public EntregaController(final EntregaUseCase service) {
		this.service = service;
	}

	@Operation(
			summary = "Serviço para atualizar um pedido"
	)
	@PutMapping("/{idDoPedido}/{statusEntrega}")
	public ResponseEntity<Void> atualizaStatus(@PathVariable("idDoPedido") final Long idDoPedido,
											   @PathVariable("statusEntrega") final StatusEntregaControllerEnum statusEntrega) {
		final var atualizou = this.service.atualiza(idDoPedido, statusEntrega);
		if(atualizou) {
			return ResponseEntity
					.status(HttpStatus.OK)
					.build();
		}
		return ResponseEntity
				.status(HttpStatus.NO_CONTENT)
				.build();
	}

}
