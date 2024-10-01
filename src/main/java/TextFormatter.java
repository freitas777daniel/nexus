import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A classe TextFormatter é responsável por formatar o conteúdo de um arquivo de texto.
 */
public class TextFormatter {
    public static void main(String[] args) {
        removerQuebraLinhasEspacosExtras();
    }

    /**
     * Este método executa a formatação do arquivo especificado no caminho {@code filePath}.
     * Ele lê todas as linhas do arquivo, junta-as com um espaço para remover quebras de linha e, em seguida, remove espaços extras.
     * O arquivo original é sobrescrito com o conteúdo formatado.
     */
    private static void removerQuebraLinhasEspacosExtras() {
        String filePath = "/home/daniel/Downloads/teste.txt";

        try {
            // Define o caminho do arquivo a partir da string de caminho
            Path path = Paths.get(filePath);

            // Lê todas as linhas do arquivo e junta-as, inserindo um espaço entre elas
            String content = String.join(" ", Files.readAllLines(path));

            // Remove espaços extras substituindo sequências de espaços por um único espaço
            String formattedContent = content.replaceAll("\\s+", " ");

            // Sobrescreve o arquivo original com o texto já formatado
            Files.write(path, formattedContent.getBytes());

            System.out.println("Arquivo formatado com sucesso!");
        } catch (IOException e) {
            // Exibe uma mensagem de erro se ocorrer algum problema durante a leitura ou escrita do arquivo
            System.err.println("Ocorreu um erro ao processar o arquivo: " + e.getMessage());
        }
    }
}
