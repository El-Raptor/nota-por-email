package br.com.sankhya;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.ModifingFields;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.util.AgendamentoRelatorioHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.email.FilaMsgUtil;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Compatível a partir de Sankhya OM 4.35b128
 */
public class EnvioEmail implements EventoProgramavelJava {
    // TODO: Verifica se nota mudou de pendente para autorizada (OLD.STATUSNFE <> A AND NEW.STATUSNFE = A)
    // TODO: Obter Nro. Único do Relatório (NURFE)
    // TODO: Obter Nro. Único da Nota (NUNOTA)
    // TODO: Obter Código do Usuário Logado (CODUSU)

    // TODO: Gerar Relatório
    // TODO: Criar E-mail
    // TODO: Enviar E-mail

    @Override
    public void afterUpdate(PersistenceEvent event) throws Exception {
    }

    @Override
    public void beforeUpdate(PersistenceEvent event) throws Exception {
        DynamicVO newCabVO = (DynamicVO) event.getVo();
        DynamicVO oldCabVO = (DynamicVO) event.getOldVO();

        ModifingFields modFields = event.getModifingFields();

        if (JapeSession.getProperty(AtributosRegras.APROVANDO) == null)
            return;

        if (modFields.isModifing("AD_OBSPARCCOMERCIAL"))
            throw new Exception("Obs da nota se modificou. Antigo: " + oldCabVO.asString("AD_OBSPARCCOMERCIAL") + " Novo: " + newCabVO.asString("AD_OBSPARCCOMERCIAL"));

        /*if (newCabVO != null)
            throw new Exception("Status novo " + (String) modFields.getNewValue("PENDENTE") + " Antigo: " +  (String) modFields.getOldValue("PENDENTE"));*/
        // Verifica se a nota foi aprovada.
        //if (!(oldCabVO.asString("STATUSNFE").equals(newCabVO.asString("STATUSNFE"))) && newCabVO.asString("STATUSNFE").equals("A")) {
        //if (!(oldCabVO.asString("STATUSNOTA").equals(newCabVO.asString("STATUSNOTA"))) && newCabVO.asString("STATUSNOTA").equals("L")) { //TESTE
        if (!(newCabVO.asBigDecimal("CODPARC").equals(new BigDecimal(41011)))) return;

            BigDecimal nuNota = newCabVO.asBigDecimal("NUNOTA");
            BigDecimal codUsu = newCabVO.asBigDecimal("CODUSU");
            BigDecimal codParc = newCabVO.asBigDecimal("CODPARC");
            BigDecimal nuRfe = getNumeroRelatorio(nuNota);

            JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
            DynamicVO parceiroVO = parceiroDAO.findByPK(codParc);

            // Gera o relatório
            AgendamentoRelatorioHelper.ParametroRelatorio param = new AgendamentoRelatorioHelper.ParametroRelatorio(
                    "NUNOTA", BigDecimal.class.getName(), nuNota
            );

            List<AgendamentoRelatorioHelper.ParametroRelatorio> params = new ArrayList<>();
            params.add(param);

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            byte[] pdf = AgendamentoRelatorioHelper.getPrintableReport(nuRfe, params, codUsu, dwfFacade);

            // Obtém XML


            // Envia e-mail
            FilaMsgUtil.Email email = new FilaMsgUtil.Email();

            email.setAssunto("Teste inicial");
            email.setMensagem("Segue a nota fiscal em anexo");
            email.setDestinatarios(parceiroVO.asString("EMAIL"));
            email.addAnexo(
                    new ByteArrayInputStream(pdf),
                    "NF_" + nuNota + ".pdf",
                    "application/pdf"
            );
            email.addAnexo(
                    new ByteArrayInputStream(xml),
                    "nfe_" + nuNota + ".xml",
                    "text/xml"
            );

            FilaMsgUtil.enviaEmail(dwfFacade, email);

        //}
    }

    private BigDecimal getNumeroRelatorio(BigDecimal nuNota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        ResultSet rset = null;
        JapeSession.SessionHandle sessionHandle = null;

        try {
            sessionHandle = JapeSession.open();
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            nativeSql = new NativeSql(jdbc);

            nativeSql.appendSql("SELECT MON.NURFE ");
            nativeSql.appendSql("  FROM TGFCAB CAB ");
            nativeSql.appendSql("  JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER ");
            nativeSql.appendSql("                 AND CAB.DHTIPOPER = TOP.DHALTER ");
            nativeSql.appendSql("  JOIN TGFMON MON ON TOP.CODMODNF = MON.CODMODNF ");
            nativeSql.appendSql(" WHERE CAB.NUNOTA = :NUNOTA");

            nativeSql.setNamedParameter("NUNOTA", nuNota);

            rset = nativeSql.executeQuery();

            if (rset.next()) {
                return rset.getBigDecimal("NURFE");
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(sessionHandle);
        }
        return null;
    }

    @Override
    public void beforeInsert(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent event) throws Exception {

    }


    @Override
    public void afterDelete(PersistenceEvent event) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext tranCtx) throws Exception {

    }
}
