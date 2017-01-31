/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.model;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Id;
import javax.persistence.Transient;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Breno Rodrigues
 */
public class SLA {

    @Id
    private String id;
    @Transient
    private String idWorkflow;
    private String provider;
    private Integer objective;
    private Long period;
    private Double value;
    private Integer limitationType;
    private Long limitationValueExecutionTime;
    private Double limitationValueExecutionCost;
    private Boolean prediction;
    private List<Prediction> solutions;
    private Boolean limitationExecution;
    private Double execeedValueExecutionCost;

    public SLA() {

    }
    
    public SLA(String provider) {
        this.provider=provider;
    }

    public SLA(String provider, String idWorkflow, Integer objective, Long period, 
            Double value, Integer limitationType, Long limitationValueExecutionTime, 
            Double limitationValueExecutionCost, Boolean prediction, 
            List<Prediction> solutions, Boolean limitationExecution, 
            Double execeedValueExecutionCost) {
        this.provider = provider;
        this.idWorkflow = idWorkflow;
        this.objective = objective;
        this.period = period;
        this.value = value;
        this.limitationType = limitationType;
        this.limitationValueExecutionTime = limitationValueExecutionTime;
        this.limitationValueExecutionCost = limitationValueExecutionCost;
        this.execeedValueExecutionCost=execeedValueExecutionCost;
        this.prediction = prediction;
        this.solutions = solutions;
        this.limitationExecution = limitationExecution;
    }

    /**
     * @return the provider
     */
    public String getProvider() {
        return provider;
    }

    /**
     * @param provider the provider to set
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * @return the objective
     */
    public Integer getObjective() {
        return objective;
    }

    /**
     * @param objective the objective to set
     */
    public void setObjective(Integer objective) {
        this.objective = objective;
    }

    /**
     * @return the period
     */
    public Long getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(Long period) {
        this.period = period;
    }

    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Double value) {
        this.value = value;
    }

    public Double getExeceedValueExecutionCost() {
        return execeedValueExecutionCost;
    }

    public void setExeceedValueExecutionCost(Double execeedValueExecutionCost) {
        this.execeedValueExecutionCost = execeedValueExecutionCost;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the limitationType
     */
    public Integer getLimitationType() {
        return limitationType;
    }

    /**
     * @param limitationType the limitationType to set
     */
    public void setLimitationType(Integer limitationType) {
        this.limitationType = limitationType;
    }

    /**
     * @return the limitationValueExecutionTime
     */
    public Long getLimitationValueExecutionTime() {
        return limitationValueExecutionTime;
    }

    /**
     * @param limitationValueExecutionTime the limitationValueExecutionTime to
     * set
     */
    public void setLimitationValueExecutionTime(Long limitationValueExecutionTime) {
        this.limitationValueExecutionTime = limitationValueExecutionTime;
    }

    /**
     * @return the limitationValueExecutionCost
     */
    public Double getLimitationValueExecutionCost() {
        return limitationValueExecutionCost;
    }

    /**
     * @param limitationValueExecutionCost the limitationValueExecutionCost to
     * set
     */
    public void setLimitationValueExecutionCost(Double limitationValueExecutionCost) {
        this.limitationValueExecutionCost = limitationValueExecutionCost;
    }

    public Boolean getPrediction() {
        return prediction;
    }

    public void setPrediction(Boolean prediction) {
        this.prediction = prediction;
    }

    public List<Prediction> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Prediction> solutions) {
        this.solutions = solutions;
    }

    public Boolean getLimitationExecution() {
        return limitationExecution;
    }

    public void setLimitationExecution(Boolean limitationExecution) {
        this.limitationExecution = limitationExecution;
    }

    public String getIdWorkflow() {
        return idWorkflow;
    }

    public void setIdWorkflow(String idWorkflow) {
        this.idWorkflow = idWorkflow;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (IOException ex) {
            Logger.getLogger(SLA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
