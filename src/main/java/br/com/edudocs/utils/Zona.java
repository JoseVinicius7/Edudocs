package br.com.edudocs.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public enum Zona {
    URBANA("URBANA"),
    RURAL("RURAL");

    private final String value;

    Zona(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Normaliza a string de zona:
     * - null -> null
     * - trim, colapsa múltiplos espaços em 1
     * - remove acentuação
     * - toUpperCase(Locale.ROOT)
     */
    public static String normalize(String input) {
        if (input == null) return null;
        String trimmed = input.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) return null;

        // Remove acentos/diacríticos
        String withoutDiacritics = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        return withoutDiacritics.toUpperCase(Locale.ROOT);
    }

    /**
     * Verifica se a string (após normalização) corresponde a uma zona válida.
     */
    public static boolean isValid(String input) {
        String n = normalize(input);
        if (n == null) return false;
        return Arrays.stream(values()).anyMatch(z -> Objects.equals(z.value, n));
    }

    /**
     * Converte a string (qualquer formato) para a enum Zona, após normalização.
     * Lança IllegalArgumentException se o valor não for reconhecido.
     */
    public static Zona from(String input) {
        String n = normalize(input);
        if (n == null) {
            throw new IllegalArgumentException("Zona não pode ser nula ou vazia");
        }
        return Arrays.stream(values())
                .filter(z -> z.value.equals(n))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Zona inválida: " + input + ". Valores aceitos: RURAL, URBANA."
                ));
    }
}
