package br.unb.cic.bionimbuz.model;

/**
 *
 * @author Vinicius
 */
public enum WorkflowStatus {
    PENDING("Workflow Pendente", "#2980b9"),
    EXECUTING("Workflow em Execução", "#2980b9"),
    FINALIZED_WITH_SUCCESS("Workflow Finalizado com Sucesso", "#27ae60"),
    FINALIZED_WITH_WARNINGS("Workflow Finalizado com Alertas", "#f39c12"),
    FINALIZED_WITH_ERRORS("Workflow Finalizado com Erros", "#c0392b"),
    PAUSED("Workflow Pausado", "#7f8c8d"),
    STOPPED_WITH_ERROR("Workflow Parado com Erros", "#c0392b");

    private final String status;

    // Color is a web interface parameter (CSS color)
    private final String color;

    private WorkflowStatus(final String status, final String color) {
        this.status = status;
        this.color = color;
    }

    @Override
    public String toString() {
        return status;
    }

    public String getColor() {
        return this.color;
    }
}
