package br.com.sankhya.service;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;

public class NotaFiscalQueryService {

    public String buscarEmailParceiro(BigDecimal codParc) throws Exception {
        JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
        DynamicVO parceiroVO = parceiroDAO.findByPK(codParc);
        return parceiroVO != null ? parceiroVO.asString("EMAIL") : null;
    }

    public BigDecimal buscarNumeroRelatorio(BigDecimal nuNota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        JapeSession.SessionHandle sessionHandle = null;

        try {
            sessionHandle = JapeSession.open();
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql("SELECT MON.NURFE ");
            nativeSql.appendSql("  FROM TGFCAB CAB ");
            nativeSql.appendSql("  JOIN TGFTOP TOP ON CAB.CODTIPOPER = TOP.CODTIPOPER ");
            nativeSql.appendSql("                 AND CAB.DHTIPOPER = TOP.DHALTER ");
            nativeSql.appendSql("  JOIN TGFMON MON ON TOP.CODMODNF = MON.CODMODNF ");
            nativeSql.appendSql(" WHERE CAB.NUNOTA = :NUNOTA");
            nativeSql.setNamedParameter("NUNOTA", nuNota);

            ResultSet rset = nativeSql.executeQuery();
            if (rset.next()) {
                return rset.getBigDecimal("NURFE");
            }
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(sessionHandle);
        }
        return null;
    }

    public String buscarXml(BigDecimal nuNota) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql nativeSql = null;
        JapeSession.SessionHandle sessionHandle = null;

        try {
            sessionHandle = JapeSession.open();
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            nativeSql = new NativeSql(jdbc);
            nativeSql.appendSql("SELECT NFE.XML ");
            nativeSql.appendSql("  FROM TGFNFE NFE");
            nativeSql.appendSql(" WHERE NFE.NUNOTA = :NUNOTA");

            //'new BigDecimal(54520)'
            nativeSql.setNamedParameter("NUNOTA", nuNota);

            ResultSet rset = nativeSql.executeQuery();
            if (rset.next()) {
                return rset.getString("XML");
            }
        } finally {
            NativeSql.releaseResources(nativeSql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(sessionHandle);
        }
        return null;
    }
}