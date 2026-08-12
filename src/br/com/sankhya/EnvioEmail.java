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
import java.nio.charset.StandardCharsets;
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

        /*if (JapeSession.getProperty(AtributosRegras.APROVANDO) == null)
            return;*/

        // Verifica se a nota foi aprovada.

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
        String xml = getXml(nuNota);

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
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),
                "nfe_" + nuNota + ".xml",
                "text/xml"
        );

        FilaMsgUtil.enviaEmail(dwfFacade, email);

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

    private String getXml(BigDecimal nuNota) throws Exception {
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

            nativeSql.appendSql("SELECT NFE.XML ");
            nativeSql.appendSql("  FROM TGFNFE NFE");
            nativeSql.appendSql(" WHERE NFE.NUNOTA = :NUNOTA");

            nativeSql.setNamedParameter("NUNOTA", new BigDecimal(54520));

            rset = nativeSql.executeQuery();

            if (rset.next()) {
                return rset.getString("XML");
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
