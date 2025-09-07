package br.com.edudocs.utils.BaseLogger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class BaseServiceLogger {

    /**
     * Executa uma operação com logs padronizados.
     *
     * @param method Nome do método no formato Classe.metodo
     * @param action Código a ser executado
     * @param <T> Tipo de retorno da operação
     * @return Resultado da operação
     */
    public <T> T logExecution(String method, Supplier<T> action) {
        long start = System.currentTimeMillis();
        log.info("{} - start", method);

        try {
            T result = action.get();
            log.info("{} - success", method);
            return result;
        } catch (Exception ex) {
            log.error("{} - error - message={}", method, ex.getMessage(), ex);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} - end - durationMs={}", method, duration);
        }
    }

    /**
     * Variante para métodos que não retornam nada (void).
     *
     * @param method Nome do método no formato Classe.metodo
     * @param action Código a ser executado
     */
    public void logExecutionVoid(String method, Runnable action) {
        long start = System.currentTimeMillis();
        log.info("{} - start", method);

        try {
            action.run();
            log.info("{} - success", method);
        } catch (Exception ex) {
            log.error("{} - error - message={}", method, ex.getMessage(), ex);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} - end - durationMs={}", method, duration);
        }
    }
}
