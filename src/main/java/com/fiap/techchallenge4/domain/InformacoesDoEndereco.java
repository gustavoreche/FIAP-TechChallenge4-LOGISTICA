package com.fiap.techchallenge4.domain;

public record InformacoesDoEndereco(
        String tempoEstimadoDeEntregaEmHoras,
        String enderecoDeEntrega
) {

    public InformacoesDoEndereco(String tempoEstimadoDeEntregaEmHoras,
                                 String enderecoDeEntrega) {
        switch (tempoEstimadoDeEntregaEmHoras) {
            case "SP":
            case "RJ":
            case "MG":
            case "ES":
                this.tempoEstimadoDeEntregaEmHoras = "01:00";
                break;
            case "PR":
            case "RS":
            case "SC":
                this.tempoEstimadoDeEntregaEmHoras = "02:00";
                break;
            case "DF":
            case "GO":
            case "MT":
            case "MS":
                this.tempoEstimadoDeEntregaEmHoras = "02:30";
                break;
            case "AL":
            case "BA":
            case "CE":
            case "MA":
            case "PB":
            case "PE":
            case "PI":
            case "RN":
            case "SE":
                this.tempoEstimadoDeEntregaEmHoras = "03:00";
                break;
            case "AC":
            case "AP":
            case "AM":
            case "PA":
            case "RO":
            case "RR":
            case "TO":
                this.tempoEstimadoDeEntregaEmHoras = "04:00";
                break;
            default:
                this.tempoEstimadoDeEntregaEmHoras = "Sem estimativa de entrega";
                break;
        }

        this.enderecoDeEntrega = enderecoDeEntrega;
    }

}
