import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class CpfCnpjUtils {

    private static final List<String> CPFS_INVALIDOS = Arrays.asList(
            "00000000000", "11111111111", "22222222222", "33333333333", "44444444444",
            "55555555555", "66666666666", "77777777777", "88888888888", "99999999999");

    private static final List<String> CNPJS_INVALIDOS = Arrays.asList(
            "00000000000000", "11111111111111", "22222222222222", "33333333333333", "44444444444444",
            "55555555555555", "66666666666666", "77777777777777", "88888888888888", "99999999999999");

    private CpfCnpjUtils() {
    }

    public static boolean isCpf(String cpf) {
        if (StringUtils.isEmpty(cpf) || !cpf.matches("^\\d{11}$") || CPFS_INVALIDOS.contains(cpf))
            return false;

        int soma = 0;
        int peso = 10;
        for (int i = 0; i < 9; i++) {
            int num = (cpf.charAt(i) - 48);
            soma = soma + (num * peso--);
        }

        int r = 11 - (soma % 11);
        char dig10 = ((r == 10) || (r == 11)) ? '0' : (char) (r + 48);

        if (dig10 != cpf.charAt(9))
            return false;

        soma = 0;
        peso = 11;
        for (int i = 0; i < 10; i++) {
            int num = (cpf.charAt(i) - 48);
            soma = soma + (num * peso--);
        }

        r = 11 - (soma % 11);
        char dig11 = ((r == 10) || (r == 11)) ? '0' : (char) (r + 48);

        return (dig11 == cpf.charAt(10));
    }

    public static boolean isCnpj(String cnpj) {
        if (StringUtils.isEmpty(cnpj) || !cnpj.matches("^\\d{14}$") || CNPJS_INVALIDOS.contains(cnpj))
            return false;

        int soma = 0;
        int peso = 2;
        for (int i = 11; i >= 0; i--) {
            int num = (cnpj.charAt(i) - 48);
            soma = soma + (num * peso++);
            if (peso == 10)
                peso = 2;
        }

        int r = soma % 11;
        char dig13 = ((r == 0) || (r == 1)) ? '0' : (char) ((11 - r) + 48);

        if (dig13 != cnpj.charAt(12))
            return false;

        soma = 0;
        peso = 2;
        for (int i = 12; i >= 0; i--) {
            int num = (cnpj.charAt(i) - 48);
            soma = soma + (num * peso++);
            if (peso == 10)
                peso = 2;
        }

        r = soma % 11;
        char dig14 = ((r == 0) || (r == 1)) ? '0' : (char) ((11 - r) + 48);

        return (dig14 == cnpj.charAt(13));
    }

    public static boolean isCpfCnpj(String cpfCnpj) {
        return isCpf(cpfCnpj) || isCnpj(cpfCnpj);
    }

    public static boolean isNotCpfCnpj(String cpfCnpj) {
        return !isCpfCnpj(cpfCnpj);
    }

    public static String mascararCpfCnpj(String cpfCnpj) {
        // Verifica se a string é nula ou se o tamanho não é 11 (CPF) nem 14 (CNPJ)
        if (cpfCnpj == null || !(cpfCnpj.length() == 11 || cpfCnpj.length() == 14)) {
            return null;
        }

        // Verifica se todos os caracteres são numéricos
        if (!cpfCnpj.matches("\\d+")) {
            return null;
        }

        // Máscara para CPF
        if (cpfCnpj.length() == 11) {
            return cpfCnpj.substring(0, 3) + ".***.***-" + cpfCnpj.substring(9);
        }

        // Máscara para CNPJ
        return cpfCnpj.substring(0, 2) + ".***.***/****-" + cpfCnpj.substring(12);

    }

    public static void main(String[] args) {
        String cpfValido = "07266658004"; // Válido
        String cnpjValido = "24023967000157"; // Válido

        String cpfInvalido = "99999999991"; // inválida
        String cnpjInvalido = "12345678000100"; // inválida


        System.out.println("CPF é válido? " + isCpfCnpj(cpfValido));
        System.out.println("CNPJ é válido? " + isCpfCnpj(cnpjValido));

        System.out.println("CPF é válido? " + isCpfCnpj(cpfInvalido));
        System.out.println("CNPJ é válido? " + isCpfCnpj(cnpjInvalido));

        System.out.println("CPF é inválido? " + isNotCpfCnpj(cpfValido));
        System.out.println("CNPJ é inválido? " + isNotCpfCnpj(cnpjValido));

        System.out.println("CPF é inválido? " + isNotCpfCnpj(cpfInvalido));
        System.out.println("CNPJ é inválido? " + isNotCpfCnpj(cnpjInvalido));

        System.out.println("CPF mascarado: " + mascararCpfCnpj(cpfValido));
        System.out.println("CNPJ mascarado: " + mascararCpfCnpj(cnpjValido));

        System.out.println("CPF mascarado: " + mascararCpfCnpj(cpfInvalido));
        System.out.println("CNPJ mascarado: " + mascararCpfCnpj(cnpjInvalido));
    }
}