import org.apache.commons.text.StringEscapeUtils;

/**
 * Classe que oferece funcionalidades para codificar e decodificar strings para o formato HTML.
 */
public class HtmlEncoderDecoder {

    /**
     * Codifica uma string para o formato HTML, convertendo caracteres especiais em entidades HTML.
     * Isso é útil para evitar que caracteres como '<' e '&' sejam processados como HTML.
     *
     * @param text A string original que pode conter caracteres especiais.
     * @return Uma string codificada em HTML onde caracteres especiais são substituídos por entidades HTML.
     */
    public static String encodeHtml(String text) {
        return StringEscapeUtils.escapeHtml4(text);
    }

    /**
     * Decodifica uma string de HTML para texto normal, convertendo entidades HTML de volta para seus caracteres originais.
     * Isso permite que textos que foram salvos ou transmitidos em formato HTML sejam exibidos como texto comum.
     *
     * @param html A string codificada em HTML que pode conter entidades HTML.
     * @return Uma string decodificada onde as entidades HTML são convertidas de volta para caracteres normais.
     */
    public static String decodeHtml(String html) {
        return StringEscapeUtils.unescapeHtml4(html);
    }

    /**
     * Método principal para demonstrar a codificação e decodificação de texto para HTML.
     *
     * @param args Argumentos da linha de comando não são utilizados neste exemplo.
     */
    public static void main(String[] args) {
        String originalText = "Texto com caracteres especiais: à, é, ñ, ç, <, >, &, \"!";
        String encodedHtml = encodeHtml(originalText);
        String decodedText = decodeHtml(encodedHtml);

        System.out.println("Texto Original: " + originalText);
        System.out.println("HTML Codificado: " + encodedHtml);
        System.out.println("Texto Decodificado: " + decodedText);
    }
}
