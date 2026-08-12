package br.com.sankhya.service;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.modelcore.util.AgendamentoRelatorioHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RelatorioService {

    public byte[] gerarPdfSankhya(BigDecimal nuRfe, BigDecimal nuNota, BigDecimal codUsu) throws Exception {
        if (nuRfe == null) {
            throw new Exception("Número do relatório (NURFE) não encontrado para a nota " + nuNota);
        }

        List<AgendamentoRelatorioHelper.ParametroRelatorio> params = new ArrayList<>();
        params.add(new AgendamentoRelatorioHelper.ParametroRelatorio(
                "NUNOTA", BigDecimal.class.getName(), nuNota
        ));

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        return AgendamentoRelatorioHelper.getPrintableReport(nuRfe, params, codUsu, dwfFacade);
    }
}