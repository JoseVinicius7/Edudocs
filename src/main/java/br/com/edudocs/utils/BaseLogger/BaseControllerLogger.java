package br.com.edudocs.utils.BaseLogger;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public abstract class BaseControllerLogger {

    /**
     * Executa uma ação com logs padronizados de start, success, error e end.
     * Compatível com qualquer ResponseEntity<T>
     *
     * @param method Nome do método no formato Classe.metodo
     * @param action Ação que retorna um ResponseEntity<T>
     * @param <T> Tipo do corpo do ResponseEntity
     * @return ResponseEntity<T>
     */
    protected <T> ResponseEntity<T> logExecution(String method, Supplier<ResponseEntity<T>> action) {
        long start = System.currentTimeMillis();
        log.info("{} - start", method);

        try {
            ResponseEntity<T> response = action.get();
            log.info("{} - success", method);
            return response;
        } catch (Exception ex) {
            log.error("{} - error - message={}", method, ex.getMessage(), ex);
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            log.info("{} - end - durationMs={}", method, duration);
        }
    }

    /**
     * Método auxiliar para logs de request padronizados.
     */
    protected void logRequest(String method, String message) {
        log.info("{} - request - {}", method, message);
    }

    /**
     * Método auxiliar para logs de response padronizados.
     */
    protected void logResponse(String method, HttpStatus status, String message) {
        log.info("{} - response - status={} - {}", method, status.value(), message);
    }
}
