/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched.model;

import br.unb.cic.bionimbuz.utils.Pair;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author willian
 */
public class SearchNode {
    public Long id;
    
    public Queue<SearchNode> toVisit;
    public Queue<SearchNode> visiting;
    public long visitedCount;
    public final ResourceList rl;
    
    // pair = <time, cost>
    private ArrayList<Pair<Double, Double>> pareto;
    
    private double maxc;
    private double maxt;
    public boolean prunable;
    public long prunedChildren;
    public final long depth;

    public SearchNode(ResourceList rl, Long id, long depth) {
        this.rl = rl;
        this.id = id;
        toVisit = new LinkedList<SearchNode>();
        visiting = new LinkedList<SearchNode>();
        visitedCount = 0;
        pareto  = new ArrayList<Pair<Double, Double>>();
        prunable = false;
        prunedChildren = 0;
        this.depth = depth;
    }
    
    public void addToPareto (Double time, Double cost) {
        pareto.add(new Pair<Double, Double>(time, cost));
        pareto = Pareto.getParetoCurve(pareto);
        maxt = pareto.get(0).first;
        maxc = pareto.get(pareto.size()-1).second;
    }
    
    public void addToPareto (List<Pair<Double, Double>> list) {
        pareto.addAll(list);
        pareto = Pareto.getParetoCurve(pareto);
        maxt = pareto.get(0).first;
        maxc = pareto.get(pareto.size()-1).second;
    }
    
    public List<Pair<Double, Double>> getParetoList() {
        return pareto;
    }
    
    public double getMaxc() {
        return maxc;
    }
    
    public double getMaxt() {
        return maxt;
    }

    @Override
    public String toString() {
        return "id: " + id;
    }
    
}
