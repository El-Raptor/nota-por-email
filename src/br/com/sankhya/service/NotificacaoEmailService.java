package br.com.sankhya.service;

import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.email.FilaMsgUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

public class NotificacaoEmailService {

    public void enviarComAnexos(String destinatario, BigDecimal nuNota, byte[] pdf, String xml) throws Exception {
        if (destinatario == null || destinatario.trim().isEmpty()) {
            throw new Exception("E-mail do parceiro não configurado.");
        }

        FilaMsgUtil.Email email = new FilaMsgUtil.Email();
        email.setAssunto("Teste inicial");
        email.setMensagem("Segue a nota fiscal em anexo");
        email.setDestinatarios(destinatario);

        if (pdf != null) {
            email.addAnexo(
                    new ByteArrayInputStream(pdf),
                    "NF_" + nuNota + ".pdf",
                    "application/pdf"
            );
        }

        if (xml != null && !xml.trim().isEmpty()) {
            email.addAnexo(
                    new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                    "nfe_" + nuNota + ".xml",
                    "text/xml"
            );
        }

        FilaMsgUtil.enviaEmail(EntityFacadeFactory.getDWFFacade(), email);
    }
}