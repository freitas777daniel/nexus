import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * A classe EnvironmentSwitcher é responsável por configurar os ambientes de desenvolvimento (DEV) e produção (PROD) para projetos.
 * Ela permite ao usuário escolher o ambiente desejado e copia os arquivos de configuração necessários para o diretório de projetos.
 */
public class EnvironmentSwitcher {
    public static void main(String[] args) {
        mudarAmbienteGetran();
    }

    /**
     * Método para alterar o ambiente de desenvolvimento para produção ou vice-versa.
     * Este método solicita ao usuário para escolher entre o ambiente de desenvolvimento (DEV)
     * ou produção (PROD), copia uma série de arquivos de configuração de um diretório de origem
     * para um diretório de destino, substituindo os existentes, com base na escolha do usuário.
     *
     * Funcionamento:
     * 1. Solicita ao usuário para escolher entre DEV (1) e PROD (2).
     * 2. Define os caminhos base para a origem e destino dos arquivos de configuração.
     * 3. Lista os projetos e arquivos específicos a serem manipulados.
     * 4. Determina o sufixo do ambiente com base na escolha do usuário.
     * 5. Realiza a cópia dos arquivos especificados, substituindo os existentes no destino.
     * 6. Trata possíveis exceções de entrada/saída durante a cópia dos arquivos.
     *
     * Exceções:
     * IOException - Capturada durante a operação de cópia se ocorrerem problemas de IO,
     * como permissões insuficientes ou falhas de disco.
     *
     * Considerações:
     * - É crucial que os caminhos dos arquivos e as permissões sejam adequados para evitar erros.
     * - O método fecha o recurso Scanner após o uso, o que pode afetar outras partes do programa
     * que utilizem o mesmo objeto Scanner.
     */
    private static void mudarAmbienteGetran() {
        Scanner scanner = new Scanner(System.in);

        // Solicita ao usuário para escolher entre ambiente de desenvolvimento ou produção
        System.out.println("Escolha o ambiente: 1 - DEV, 2 - PROD");
        int choice = scanner.nextInt();
        scanner.close();

        // Caminhos base para fonte e destino dos arquivos de configuração
        String sourceBasePath = "/home/daniel/apps/workspace-intellij-idea/2-dependencias/mudar_ambiente_getran/";
        String destinationBasePath = "/home/daniel/apps/workspace-intellij-idea/1-projetos/";

        // Nomes de projetos e arquivos a serem copiados
        String[] projects = {"getran-dev-21", "sna-dev", "trrenavam-dev"};
        String[] files = {
                "WebContent/META-INF/context.xml",
                "src/renavam/resources/transacoes/sngmanager.properties",
                "src/renavam/resources/api/balcaodigital.properties",
                "web/WEB-INF/config.properties",
                "web/META-INF/context.xml",
                "src/montreal/str/banco/ConnectionPool.java",
                "src/montreal/str/trenvio/comunicacao/TransacoesSocket.java",
                "bin/renavam/resources/transacoes/sngmanager.properties",
                "src/renavam/resources/transacoes/sngmanager.properties",
                "src/log4j.xml"
        };

        // Determina o sufixo do ambiente com base na escolha do usuário
        String env = (choice == 1) ? "dev" : "prod";

        // Processo de cópia dos arquivos para o diretório apropriado
        for (String project : projects) {
            for (String file : files) {
                Path sourcePath = Paths.get(sourceBasePath + project.split("-")[0] + "/" + env + "/" + file.substring(file.lastIndexOf('/') + 1));
                Path destinationPath = Paths.get(destinationBasePath + project + "/" + file);

                try {
                    // Executa a cópia do arquivo, substituindo-o se já existir
                    if (Files.exists(sourcePath)) {
                        Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Arquivo " + destinationPath + " substituído por " + sourcePath);
                    } else {
                        System.out.println("Arquivo " + sourcePath + " não existe.");
                    }
                } catch (IOException e) {
                    // Captura e reporta erros durante a cópia
                    System.out.println("Erro ao copiar o arquivo: " + e.getMessage());
                }
            }
        }
    }
}
