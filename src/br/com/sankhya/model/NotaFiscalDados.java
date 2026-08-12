package br.com.sankhya.model;

import java.math.BigDecimal;

public class NotaFiscalDados {
    private BigDecimal nuNota;
    private BigDecimal codUsu;
    private BigDecimal codParc;
    private BigDecimal nuRfe;
    private String emailParceiro;

    public NotaFiscalDados(BigDecimal nuNota, BigDecimal codUsu, BigDecimal codParc) {
        this.nuNota = nuNota;
        this.codUsu = codUsu;
        this.codParc = codParc;
    }

    public BigDecimal getNuNota() { return nuNota; }
    public BigDecimal getCodUsu() { return codUsu; }
    public BigDecimal getCodParc() { return codParc; }

    public BigDecimal getNuRfe() { return nuRfe; }
    public void setNuRfe(BigDecimal nuRfe) { this.nuRfe = nuRfe; }

    public String getEmailParceiro() { return emailParceiro; }
    public void setEmailParceiro(String emailParceiro) { this.emailParceiro = emailParceiro; }
}