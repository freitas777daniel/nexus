import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PegarMetricasBalcao {

    public static void main(String[] args) {
        Metrica metrica = pegarMetrica();
        System.out.println(metrica);
    }

    private static final Logger log = Logger.getLogger(PegarMetricasBalcao.class.getName());
    private static final String URL_BALCAO = "jdbc:postgresql://172.25.136.79:5432/balcao";
    private static final String USER_BALCAO = "sysbalcao";
    private static final String PASSWORD_BALCAO = "4bb93d3432cb7b31069a5360c1ba44dd6bf9252a";

    private static final String URL_VEICULO = "jdbc:postgresql://172.25.136.30:5432/dbveiculos_dev";
    private static final String USER_VEICULO = "danielsouza";
    private static final String PASSWORD_VEICULO = "4633897dcf65664e2077226ac996ec32b2778cac";

    private static Metrica pegarMetrica() {
        int total = 0;
        int aptas = 0;
        int naoAptas = 0;

        List<Solicitacao> solicitacoes = pegarSolicitacoesBalcao();
        total = solicitacoes.size();
        for (Solicitacao solicitacao : solicitacoes) {
            if (solicitacao.getStatusDigitalizacao() == 2 && solicitacaoValida(solicitacao)) {
                aptas++;
            } else {
                naoAptas++;
            }
        }

        Metrica metrica = new Metrica();
        metrica.setTotal(total);
        metrica.setAptas(aptas);
        metrica.setInaptas(naoAptas);
        return metrica;

    }

    private static List<Solicitacao> pegarSolicitacoesBalcao() {
        List<Solicitacao> retorno = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String query = "select s.codigo_identificador::int4, s.solicitacao_status_id " +
                "from balcao.balcao.papeis_pessoas_solicitacoes pps " +
                "inner join balcao.balcao.solicitacoes s on s.id = pps.solicitacao_id " +
                "where s.solicitacao_status_id = 2 " +
                "and pps.papel_id = 7 " +
                "order by s.created_at;";

        try {
            conn = DriverManager.getConnection(URL_BALCAO, USER_BALCAO, PASSWORD_BALCAO);
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Solicitacao solicitacao = new Solicitacao();
                solicitacao.setCodigoAtendimento(rs.getInt(1));
                solicitacao.setStatusDigitalizacao(rs.getInt(2));
                retorno.add(solicitacao);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            closeConnections(conn, pstmt, rs);
        }
        return retorno;
    }

    private static boolean solicitacaoValida(Solicitacao solicitacao) {
        boolean retorno = false;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String query = "select * from revam.is_atendimento_distribuivel_sem_restricao(?);";

        try {
            conn = DriverManager.getConnection(URL_VEICULO, USER_VEICULO, PASSWORD_VEICULO);
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, solicitacao.getCodigoAtendimento());
            rs = pstmt.executeQuery();
            if (rs.next()) {
                retorno = rs.getBoolean(1);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } finally {
            closeConnections(conn, pstmt, rs);
        }
        return retorno;
    }

    private static class Solicitacao {
        Solicitacao() {
        }

        private Integer codigoAtendimento;
        private Integer statusDigitalizacao;

        public Integer getCodigoAtendimento() {
            return codigoAtendimento;
        }

        public void setCodigoAtendimento(Integer codigoAtendimento) {
            this.codigoAtendimento = codigoAtendimento;
        }

        public Integer getStatusDigitalizacao() {
            return statusDigitalizacao;
        }

        public void setStatusDigitalizacao(Integer statusDigitalizacao) {
            this.statusDigitalizacao = statusDigitalizacao;
        }

        @Override
        public String toString() {
            return "Solicitacao{" +
                    "codigoAtendimento=" + codigoAtendimento +
                    ", statusDigitalizacao=" + statusDigitalizacao +
                    '}';
        }
    }

    private static class Metrica {
        Metrica() {}
        private Integer total;
        private Integer aptas;
        private Integer inaptas;

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public Integer getAptas() {
            return aptas;
        }

        public void setAptas(Integer aptas) {
            this.aptas = aptas;
        }

        public Integer getInaptas() {
            return inaptas;
        }

        public void setInaptas(Integer inaptas) {
            this.inaptas = inaptas;
        }

        @Override
        public String toString() {
            return "Metrica{" +
                    "total=" + total +
                    ", aptas=" + aptas +
                    ", inaptas=" + inaptas +
                    '}';
        }
    }

    private static void closeConnections(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("Não foi possível fechar o ResultSet: " + e.getMessage());
                throw new RuntimeException("Não foi possível fechar o ResultSet: " + e.getMessage(), e);
            }
        }
        closeConnections(conn, pstmt);
    }

    private static void closeConnections(Connection conn, PreparedStatement pstmt) {
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                log.info("Não foi possível fechar o PreparedStatement: " + e.getMessage());
                throw new RuntimeException("Não foi possível fechar o PreparedStatement: " + e.getMessage());
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.info("Não foi possível fechar a Connection: " + e.getMessage());
                throw new RuntimeException("Não foi possível fechar a Connection: " + e.getMessage());
            }
        }
    }
}
