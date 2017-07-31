/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched.model;

import br.unb.cic.bionimbuz.services.RepositoryService;

/**
 *
 * @author willian
 */
public class ParetoInfResourceList extends ResourceList{

    private final ElemType type;
    
    public enum ElemType {
        TIME,
        COST
    }
    
    public ParetoInfResourceList(ElemType type) {
        this.type = type;
    }

    @Override
    public Double getFullCost(RepositoryService rs) {
        if (type == ElemType.COST)
            return Double.POSITIVE_INFINITY;
        else
            return 0d;
    }

    @Override
    public Double getMaxTime(RepositoryService rs) {
        if (type == ElemType.TIME)
            return Double.POSITIVE_INFINITY;
        else
            return 0d;
    }
    
    
    
}
