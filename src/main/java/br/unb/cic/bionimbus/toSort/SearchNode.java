/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author willian
 */
public class SearchNode {
    public Queue<SearchNode> toVisit;
    public Queue<SearchNode> visiting;
    public Queue<SearchNode> visited;
    public ResourceList rl;
    public int beamWidth;
    public int id;
    public boolean leaf;

    public SearchNode(ResourceList rl, Integer id) {
        this.rl = rl;
        this.id = id;
        toVisit = new LinkedList<SearchNode>();
        visiting = new LinkedList<SearchNode>();
        visited = new LinkedList<SearchNode>();
    }

    @Override
    public String toString() {
        return "id: " + id;
    }
    
}
