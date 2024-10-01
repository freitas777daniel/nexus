import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class CheckoutGetran {

    private static final Path LOCAL_PROJETO = Paths.get(System.getProperty("user.home"), "apps", "workspace-intellij-idea", "1-projetos");
    private static final Path CONFIG_DIR = Paths.get("/home/daniel/apps/workspace-intellij-idea/2-dependencias/getran-configs-intellij-with-modules");
    private static final String SVN_PASSWORD = "detran123";
    private static final Logger LOGGER = Logger.getLogger(CheckoutGetran.class.getName());

    public static void main(String[] args) {
        configureLogging();
        String nomeProjeto = args.length > 0 ? args[0] : "getran-dev-balcao";
        String tipoProjeto = args.length > 1 ? args[1] : "160";
        String ambiente = args.length > 2 ? args[2] : "dev";

        checkoutGetran(nomeProjeto, tipoProjeto, ambiente);
    }

    private static void configureLogging() {
        try {
            Logger rootLogger = Logger.getLogger("");
            FileHandler fileHandler = new FileHandler("checkout_getran.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            rootLogger.addHandler(fileHandler);
            rootLogger.setLevel(Level.INFO);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao configurar o log", e);
        }
    }

    private static void checkoutGetran(String nomeProjeto, String tipoProjeto, String ambiente) {
        Path caminho = LOCAL_PROJETO.resolve(nomeProjeto);

        if (!Files.exists(LOCAL_PROJETO)) {
            LOGGER.log(Level.SEVERE, "Local do projeto não existe. Operação cancelada.");
            return;
        }

        if (Files.exists(caminho)) {
            LOGGER.log(Level.SEVERE, "O diretório de destino já existe. Operação cancelada.");
            return;
        }

        if ("160".equals(tipoProjeto)) {
            LOGGER.log(Level.INFO, "Iniciando checkout SVN para tipo de projeto 160");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/branches/ISSUES 2023/balcaodigital_dev/getran", caminho);
            checkoutSutilBalcao();
        } else if ("21".equals(tipoProjeto)) {
            LOGGER.log(Level.INFO, "Iniciando checkout SVN para tipo de projeto 21");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/trunk/PROJETOS/getran", caminho);
            checkoutSutil();
        } else {
            LOGGER.log(Level.SEVERE, "Tipo de projeto não suportado. Operação cancelada.");
            return;
        }

        LOGGER.log(Level.INFO, "Modificando arquivos do projeto");
        modificarContextXml(caminho.resolve("WebContent/META-INF/context.xml"));
        modificarLog4jXml(caminho.resolve("src/log4j.xml"));
        modificarBuildXml(caminho.resolve("build.xml"));
        modificarNotaFiscalFacade(caminho.resolve("src/renavam/nfe/facadeImpl/NotaFiscalEletronicaFacadeImpl.java"));
        modificarBuildProperties(caminho.resolve("build.properties"), nomeProjeto);

        LOGGER.log(Level.INFO, "Copiando diretório de configuração");
        copiarDiretorio(CONFIG_DIR.resolve(".idea"), caminho.resolve(".idea"));
        copiarArquivo(CONFIG_DIR.resolve("getran.iml"), caminho.resolve("getran.iml"));

        if ("21".equals(tipoProjeto)) {
            LOGGER.log(Level.INFO, "Modificando arquivo modules.xml para tipo de projeto 21");
            modificarModulesXml(caminho.resolve(".idea/modules.xml"));
        }

        if ("dev".equals(ambiente)) {
            LOGGER.log(Level.INFO, "Configurando ambiente de desenvolvimento");
            modificarContextXml(caminho.resolve("WebContent/META-INF/context.xml"), "dev");
            modificarSngmanagerProperties(caminho.resolve("src/renavam/resources/transacoes/sngmanager.properties"));
            checkoutSnaDev();
            checkoutTrrenavamDev();
            modificarBalcaodigitalProperties(caminho.resolve("src/renavam/resources/api/balcaodigital.properties"), "dev");
        } else if ("prod".equals(ambiente)) {
            LOGGER.log(Level.INFO, "Configurando ambiente de produção");
            checkoutSnaProd();
            checkoutTrrenavamProd();
            modificarBalcaodigitalProperties(caminho.resolve("src/renavam/resources/api/balcaodigital.properties"), "prod");
        } else {
            LOGGER.log(Level.SEVERE, "Ambiente de projeto não suportado. Operação cancelada.");
        }
    }

    private static void checkoutSvn(String url, Path destino) {
        LOGGER.log(Level.INFO, "Iniciando checkout SVN para URL: " + url);
        executarComando("sshpass", "-p", SVN_PASSWORD, "svn", "checkout", url, destino.toString());
    }

    private static void checkoutSutilBalcao() {
        Path destino = LOCAL_PROJETO.resolve("sutil-balcao");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto sutil-balcao");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/getranlibs/branches/ISSUES 2023/Sprint 01/balcao/sutil", destino);
            copiarArquivo(CONFIG_DIR.resolve("sutil.iml"), destino.resolve("sutil.iml"));
        }
    }

    private static void checkoutSutil() {
        Path destino = LOCAL_PROJETO.resolve("sutil");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto sutil");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/getranlibs/trunk/sutil", destino);
            copiarArquivo(CONFIG_DIR.resolve("sutil.iml"), destino.resolve("sutil.iml"));
        }
    }

    private static void modificarContextXml(Path arquivo, String... env) {
        LOGGER.log(Level.INFO, "Modificando arquivo context.xml");
        substituirTexto(arquivo, "username=\"[^\"]*\"", "username=\"danielsouza\"");
        substituirTexto(arquivo, "password=\"[^\"]*\"", "password=\"4633897dcf65664e2077226ac996ec32b2778cac\"");
        substituirTexto(arquivo, "initialSize=\"[^\"]*\"", "initialSize=\"2\"");
        substituirTexto(arquivo, "maxActive=\"[^\"]*\"", "maxActive=\"2\"");
        substituirTexto(arquivo, "maxIdle=\"[^\"]*\"", "maxIdle=\"2\"");
        if (env.length > 0 && "dev".equals(env[0])) {
            substituirTexto(arquivo, "url=\"jdbc:postgresql://172.25.136.30:5432/dbveiculos_dev\"", "url=\"jdbc:postgresql://172.25.136.81:5432/dbveiculos_dev\"");
        }
    }

    private static void modificarLog4jXml(Path arquivo) {
        LOGGER.log(Level.INFO, "Modificando arquivo log4j.xml");
        substituirTexto(arquivo, "<priority value=\"error\" \\/>", "<priority value=\"debug\" \\/>");
    }

    private static void modificarBuildXml(Path arquivo) {
        LOGGER.log(Level.INFO, "Modificando arquivo build.xml");
        substituirTexto(arquivo, "<eclipse.refreshLocal resource=\"@{projeto}\" depth=\"infinite\" \\/>", "<!--<eclipse.refreshLocal resource=\"@{projeto}\" depth=\"infinite\" \\/>-->");
    }

    private static void modificarNotaFiscalFacade(Path arquivo) {
        LOGGER.log(Level.INFO, "Modificando arquivo NotaFiscalEletronicaFacadeImpl.java");
        substituirTexto(arquivo, "loadService\\(\\);", "\\/\\/loadService\\(\\);");
    }

    private static void modificarBuildProperties(Path arquivo, String nomeProjeto) {
        LOGGER.log(Level.INFO, "Modificando arquivo build.properties");
        substituirTexto(arquivo, "getran.project.name=[^\\s]*", "getran.project.name=" + nomeProjeto);
    }

    private static void modificarModulesXml(Path arquivo) {
        LOGGER.log(Level.INFO, "Modificando arquivo modules.xml");
        substituirTexto(arquivo, "<module fileurl=\"file:\\/\\/$PROJECT_DIR$\\/..\\/sutil-balcao\\/sutil.iml\" filepath=\"$PROJECT_DIR$\\/..\\/sutil-balcao\\/sutil.iml\" \\/>", "<module fileurl=\"file:\\/\\/$PROJECT_DIR$\\/..\\/sutil\\/sutil.iml\" filepath=\"$PROJECT_DIR$\\/..\\/sutil\\/sutil.iml\" \\/>");
    }

    private static void modificarSngmanagerProperties(Path arquivo) {
        LOGGER.log(Level.INFO, "Modificando arquivo sngmanager.properties");
        substituirTexto(arquivo, "#SNG.Gateway.port=16504", "SNG.Gateway.port=16504");
        substituirTexto(arquivo, "SNG.Gateway.port=16505", "#SNG.Gateway.port=16505");
    }

    private static void checkoutSnaDev() {
        Path destino = LOCAL_PROJETO.resolve("sna-dev");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto sna-dev");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/trunk/PROJETOS/sna", destino);
            modificarSnaConfig(destino.resolve("web/WEB-INF/config.properties"), "dev");
            modificarContextXml(destino.resolve("web/META-INF/context.xml"), "dev");
            copiarArquivo(CONFIG_DIR.resolve("sna.iml"), destino.resolve("sna.iml"));
            modificarSnaLoginScripts(destino);
        }
    }

    private static void checkoutSnaProd() {
        Path destino = LOCAL_PROJETO.resolve("sna-prod");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto sna-prod");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/trunk/PROJETOS/sna", destino);
            modificarSnaConfig(destino.resolve("web/WEB-INF/config.properties"), "prod");
            modificarContextXml(destino.resolve("web/META-INF/context.xml"), "prod");
            copiarArquivo(CONFIG_DIR.resolve("sna.iml"), destino.resolve("sna.iml"));
            modificarSnaModuleConfigs(LOCAL_PROJETO.resolve(".idea"));
        }
    }

    private static void modificarSnaConfig(Path arquivo, String env) {
        LOGGER.log(Level.INFO, "Modificando arquivo config.properties para ambiente " + env);
        substituirTexto(arquivo, "^jdbc\\/default\\/username=.*", "jdbc\\/default\\/username=danielsouza");
        substituirTexto(arquivo, "^jdbc\\/default\\/password=.*", "jdbc\\/default\\/password=4633897dcf65664e2077226ac996ec32b2778cac");
        substituirTexto(arquivo, "^jdbc\\/default\\/maxConnections=.*", "jdbc\\/default\\/maxConnections=2");
        if ("dev".equals(env)) {
            substituirTexto(arquivo, "^jdbc\\/default\\/connectionURL=.*", "jdbc\\/default\\/connectionURL=jdbc:postgresql://172.25.136.81:5432/dbveiculos_dev");
        }
    }

    private static void modificarSnaLoginScripts(Path diretorio) {
        LOGGER.log(Level.INFO, "Modificando scripts de login para sna-dev");
        List<String> scripts = Arrays.asList("abrir-sessao-exec-htm.jsp", "abrir-sessao-htm.jsp", "barra-topo-explorer-htm.jsp", "fechar-ultima-sessao-aberta-htm.jsp", "verificar-login-htm.jsp");
        scripts.forEach(script -> {
            Path arquivo = diretorio.resolve("web/loginout/login/" + script);
            if (Files.exists(arquivo)) {
                substituirTexto(arquivo, "alert\\('Seu ultimo login foi: <%=ultLogin%>\\');", "<%--alert\\('Seu ultimo login foi: <%=ultLogin%>\\');--%>");
                adicionarTextoAoFinalDoArquivo(arquivo, "\n<script>\n\twindow.onload = function() {\n\t\tsetTimeout(function() {\n\t\t\tvar elemento = document.querySelector('input[name=\"submeter\"]');\n\t\t\tif (elemento)\n\t\t\t\telemento.click();\n\t\t}, 100);\n\t};\n</script>");
            }
        });
    }

    private static void modificarSnaModuleConfigs(Path diretorio) {
        LOGGER.log(Level.INFO, "Modificando configurações de módulo para sna-prod");
        try (Stream<Path> stream = Files.walk(diretorio)) {
            stream.filter(Files::isRegularFile)
                  .forEach(arquivo -> {
                      substituirTexto(arquivo, "sna-dev", "sna-prod");
                      substituirTexto(arquivo, "trrenavam-dev", "trrenavam-prod");
                  });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao modificar configurações de módulo", e);
        }
    }

    private static void checkoutTrrenavamDev() {
        Path destino = LOCAL_PROJETO.resolve("trrenavam-dev");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto trrenavam-dev");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/trunk/MODULOS/trrenavam", destino);
            modificarTrrenavamConfig(destino.resolve("src/configTransacoes/config.properties"), "dev");
            modificarTrrenavamSqlMapConfig(destino.resolve("src/montreal/resources/maps/SqlMapConfig.xml"), "dev");
            modificarTrrenavamConnectionPool(destino.resolve("src/montreal/str/banco/ConnectionPool.java"), "dev");
            modificarTrrenavamTransacoesSocket(destino.resolve("src/montreal/str/trenvio/comunicacao/TransacoesSocket.java"), "dev");
            modificarTrrenavamConfigTransacoes(destino.resolve("src/renavam/resources/transacoes/config.properties"), "dev");
            modificarSngmanagerProperties(destino.resolve("src/renavam/resources/transacoes/sngmanager.properties"));
            copiarArquivo(CONFIG_DIR.resolve("trrenavam.iml"), destino.resolve("trrenavam.iml"));
        }
    }

    private static void checkoutTrrenavamProd() {
        Path destino = LOCAL_PROJETO.resolve("trrenavam-prod");
        if (!Files.exists(destino)) {
            LOGGER.log(Level.INFO, "Checkout do projeto trrenavam-prod");
            checkoutSvn("svn+ssh://daniel.souza@172.25.136.61/usr/svn/veiculo/trunk/MODULOS/trrenavam", destino);
            modificarTrrenavamConfig(destino.resolve("src/configTransacoes/config.properties"), "prod");
            modificarTrrenavamSqlMapConfig(destino.resolve("src/montreal/resources/maps/SqlMapConfig.xml"), "prod");
            modificarTrrenavamConnectionPool(destino.resolve("src/montreal/str/banco/ConnectionPool.java"), "prod");
            modificarTrrenavamConfigTransacoes(destino.resolve("src/renavam/resources/transacoes/config.properties"), "prod");
            copiarArquivo(CONFIG_DIR.resolve("trrenavam.iml"), destino.resolve("trrenavam.iml"));
        }
    }

    private static void modificarTrrenavamConfig(Path arquivo, String env) {
        LOGGER.log(Level.INFO, "Modificando arquivo config.properties para trrenavam-" + env);
        substituirTexto(arquivo, "^maxActive=.*", "maxActive=2");
        substituirTexto(arquivo, "^maxIdle=.*", "maxIdle=2");
        substituirTexto(arquivo, "^minIdle=.*", "minIdle=2");
        if ("dev".equals(env)) {
            substituirTexto(arquivo, "^url-conexao = jdbc:postgresql://172.25.136.30:5432/db_veiculos_dev", "url-conexao = jdbc:postgresql://172.25.136.81:5432/db_veiculos_dev");
        }
    }

    private static void modificarTrrenavamSqlMapConfig(Path arquivo, String env) {
        LOGGER.log(Level.INFO, "Modificando arquivo SqlMapConfig.xml para trrenavam-" + env);
        substituirTexto(arquivo, "<property name=\"username\" value=\"[^\"]*\" \\/>", "<property name=\"username\" value=\"danielsouza\" \\/>");
        substituirTexto(arquivo, "<property name=\"password\" value=\"[^\"]*\" \\/>", "<property name=\"password\" value=\"4633897dcf65664e2077226ac996ec32b2778cac\" \\/>");
        substituirTexto(arquivo, "<property name=\"maxActive\" value=\"[^\"]*\" \\/>", "<property name=\"maxActive\" value=\"2\" \\/>");
        if ("dev".equals(env)) {
            substituirTexto(arquivo, "<property name=\"url\" value=\"jdbc:postgresql://172.25.136.30:5432/dbveiculos_dev\" \\/>", "<property name=\"url\" value=\"jdbc:postgresql://172.25.136.81:5432/dbveiculos_dev\" \\/>");
        }
    }

    private static void modificarTrrenavamConnectionPool(Path arquivo, String env) {
        LOGGER.log(Level.INFO, "Modificando arquivo ConnectionPool.java para trrenavam-" + env);
        substituirTexto(arquivo, "String url = \"jdbc:postgresql://172.25.136.30:5432/dbveiculos_dev\";", "String url = \"jdbc:postgresql://172.25.136.81:5432/dbveiculos_dev\";");
        substituirTexto(arquivo, "this.ds.setUsername\\( \"[^\"]*\");", "this.ds.setUsername\\( \"danielsouza\" \\);");
        substituirTexto(arquivo, "this.ds.setPassword\\( String.valueOf\\(this.[^\\)]*\\) \\);", "this.ds.setPassword\\( \"4633897dcf65664e2077226ac996ec32b2778cac\" \\);");
        substituirTexto(arquivo, "this.ds.setMaxActive\\( [^\\)]* \\);", "this.ds.setMaxActive\\( 1 \\);");
        substituirTexto(arquivo, "this.ds.setMaxIdle\\( [^\\)]* \\);", "this.ds.setMaxIdle\\( 1 \\);");
        substituirTexto(arquivo, "this.ds.setMinIdle\\( [^\\)]* \\);", "this.ds.setMinIdle\\( 1 \\);");
    }

    private static void modificarTrrenavamTransacoesSocket(Path arquivo, String env) {
        if ("dev".equals(env)) {
            LOGGER.log(Level.INFO, "Modificando arquivo TransacoesSocket.java para trrenavam-dev");
            substituirTexto(arquivo, "this.host = HostIP.PRODUCAO;", "this.host = HostIP.HOMOLOGACAO;");
        }
    }

    private static void modificarTrrenavamConfigTransacoes(Path arquivo, String env) {
        LOGGER.log(Level.INFO, "Modificando arquivo config.properties para trrenavam-" + env);
        substituirTexto(arquivo, "^maxActive=.*", "maxActive=2");
        substituirTexto(arquivo, "^maxIdle=.*", "maxIdle=2");
        substituirTexto(arquivo, "^minIdle=.*", "minIdle=2");
        if ("dev".equals(env)) {
            substituirTexto(arquivo, "url-conexao = jdbc:postgresql://172.25.136.30:5432/dbveiculos_dev", "url-conexao = jdbc:postgresql://172.25.136.81:5432/dbveiculos_dev");
        }
    }

    private static void modificarBalcaodigitalProperties(Path arquivo, String env) {
        if (Files.exists(arquivo)) {
            LOGGER.log(Level.INFO, "Modificando arquivo balcaodigital.properties para ambiente " + env);
            substituirTexto(arquivo, "env=prod", "env=" + env);
        }
    }

    private static void executarComando(String... comando) {
        try {
            LOGGER.log(Level.INFO, "Executando comando: " + String.join(" ", comando));
            ProcessBuilder processBuilder = new ProcessBuilder(comando);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                LOGGER.log(Level.SEVERE, "Erro ao executar comando: " + String.join(" ", comando));
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Erro ao executar comando", e);
        }
    }

    private static void substituirTexto(Path arquivo, String regex, String substituto) {
        try {
            List<String> linhas = Files.readAllLines(arquivo);
            List<String> novasLinhas = linhas.stream()
                                             .map(linha -> linha.replaceAll(regex, substituto))
                                             .collect(Collectors.toList());
            Files.write(arquivo, novasLinhas);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao substituir texto no arquivo " + arquivo, e);
        }
    }

    private static void adicionarTextoAoFinalDoArquivo(Path arquivo, String texto) {
        try {
            Files.write(arquivo, texto.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao adicionar texto ao final do arquivo " + arquivo, e);
        }
    }

    private static void copiarArquivo(Path origem, Path destino) {
        try {
            LOGGER.log(Level.INFO, "Copiando arquivo de " + origem + " para " + destino);
            Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao copiar arquivo", e);
        }
    }

    private static void copiarDiretorio(Path origem, Path destino) {
        try {
            LOGGER.log(Level.INFO, "Copiando diretório de " + origem + " para " + destino);
            Files.walk(origem)
                 .forEach(origemArquivo -> {
                     Path destinoArquivo = destino.resolve(origem.relativize(origemArquivo));
                     try {
                         Files.copy(origemArquivo, destinoArquivo, StandardCopyOption.REPLACE_EXISTING);
                     } catch (IOException e) {
                         LOGGER.log(Level.SEVERE, "Erro ao copiar arquivo", e);
                     }
                 });
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Erro ao copiar diretório", e);
        }
    }
}