public class CpfCnpjUtils {

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
        String cpf = "99999999991";
        String cnpj = "12345678000100";

        System.out.println("CPF mascarado: " + mascararCpfCnpj(cpf));  // Saída: 999.***.***-91
        System.out.println("CNPJ mascarado: " + mascararCpfCnpj(cnpj));  // Saída: 12.***.***/****-00
    }
}