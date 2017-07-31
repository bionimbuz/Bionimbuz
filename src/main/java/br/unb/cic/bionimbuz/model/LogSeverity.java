package br.unb.cic.bionimbuz.model;

/**
 * Defines the Severity of a Log. Don't change enum names (INFO, WARN, ERROR)
 * because it will crash workflow history page.
 *
 * @author Vinicius
 */
public enum LogSeverity {

    INFO("Informativo"),
    WARN("Alerta"),
    ERROR("Erro");

    private final String severity;

    private LogSeverity(final String severity) {
        this.severity = severity;
    }

    public String getText() {
        return severity;
    }

}
