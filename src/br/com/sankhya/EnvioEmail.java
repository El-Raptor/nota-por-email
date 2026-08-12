package br.com.sankhya;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.model.NotaFiscalDados;
import br.com.sankhya.service.NotaFiscalQueryService;
import br.com.sankhya.service.NotificacaoEmailService;
import br.com.sankhya.service.RelatorioService;

import java.math.BigDecimal;

/**
 * Compatível a partir de Sankhya OM 4.35b128
 */
public class EnvioEmail implements EventoProgramavelJava {

    private final NotaFiscalQueryService queryService;
    private final RelatorioService relatorioService;
    private final NotificacaoEmailService emailService;

    // Construtor inicializa as dependências dos serviços
    public EnvioEmail() {
        this.queryService = new NotaFiscalQueryService();
        this.relatorioService = new RelatorioService();
        this.emailService = new NotificacaoEmailService();
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO newCabVO = (DynamicVO) event.getVo();

        // 1. Validação da Regra de Negócio principal
        /*if (JapeSession.getProperty(AtributosRegras.APROVANDO) == null)
            return;*/
        if (!(newCabVO.asBigDecimal("CODPARC").equals(new BigDecimal(41011)))) {
            return;
        }

        // 2. Extração de dados (Model)
        NotaFiscalDados nota = new NotaFiscalDados(
                newCabVO.asBigDecimal("NUNOTA"),
                newCabVO.asBigDecimal("CODUSU"),
                newCabVO.asBigDecimal("CODPARC")
        );

        // 3. Consultas adicionais no banco
        nota.setEmailParceiro(queryService.buscarEmailParceiro(nota.getCodParc()));
        nota.setNuRfe(queryService.buscarNumeroRelatorio(nota.getNuNota()));
        String xml = queryService.buscarXml(nota.getNuNota());

        // 4. Processamento de Relatório
        byte[] pdf = relatorioService.gerarPdfSankhya(nota.getNuRfe(), nota.getNuNota(), nota.getCodUsu());

        // 5. Envio de E-mail
        emailService.enviarComAnexos(nota.getEmailParceiro(), nota.getNuNota(), pdf, xml);
    }

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {}

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {}

    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {}

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {}
}