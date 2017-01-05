/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbuz.services.sched.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import br.unb.cic.bionimbuz.services.RepositoryService;
import br.unb.cic.bionimbuz.utils.Pair;

/**
 *
 * @author willian
 */
public class Pareto {
    
    /**
     * Generate a pareto optimal set given a list of ResourceList.
     * The pareto optimal set is built by ordering by cost and adding each
     * element by time if it's not dominated by the previous point..
     * @param resources The elements to generate the pareto curve
     * @param rs The RepositoryService ref
     * @return A pair containing the pareto curve and the remaining ResourceLists
     */
    public static Pair<List<ResourceList>, List<ResourceList>> getParetoCurve(List<ResourceList> resources, final RepositoryService rs) {
        
        List<ResourceList> paretoCurve = new ArrayList<>();
        List<ResourceList> remaining = new ArrayList<>();
        
        double limitCost = Double.POSITIVE_INFINITY;
        
        // sort the list of ResourceList by max exec time
        Collections.sort(resources, new Comparator<ResourceList>() {
            // returns 1 if r2 before r1 and -1 otherwise
            @Override
            public int compare(ResourceList r1, ResourceList r2) {
                if (r2.getMaxTime(rs) < r1.getMaxTime(rs))
                    return 1;
                if (r2.getMaxTime(rs) > r1.getMaxTime(rs))
                    return -1;
                return 0;
            }
        });
        
        // for all remaining pairs
        for (ResourceList resource : resources) {
            double execCost = resource.getFullCost(rs);
            // if second argument is less than or equal to the limit
            if (execCost <= limitCost) {
                // add to the curve and set it as the new limit
                paretoCurve.add(resource);
                limitCost = execCost;
            } else
                remaining.add(resource);
        }
        
        return new Pair<>(paretoCurve, remaining);
    }
    /**
     * Get the point from the ordered list closest to the intersection 
     * between the pareto curve and the vector given by the angle alpha.
     * @param paretoCurve An ordered List of ResourceList representing an optimal
     * pareto set.
     * @param alpha the angle of the vector in radians (must be between 0 and 1.57)
     * @return A ResourceList with cost/time closest to vector given by alpha
     */
    public static ResourceList getParetoOptimal(List<ResourceList> paretoCurve, double alpha) {        
        
        if (paretoCurve.size() < 2) {
            // returns the only possible value if number of points is less than 2
            return paretoCurve.get(0);
        }
        
        return paretoCurve.get((int) Math.floor(paretoCurve.size() * alpha));
    }
    
    /**
     *
     * @param old
     * @return
     */
    public static ArrayList<Pair<Double, Double>> getParetoCurve(ArrayList<Pair<Double, Double>> old) {
        // pair = <time, cost>
        ArrayList<Pair<Double, Double>> newPareto = new ArrayList<Pair<Double, Double>>();
        
        // sort the old list by time
        Collections.sort(old, new Comparator<Pair<Double, Double>>() {
            // returns 1 if r2 before r1 and -1 otherwise
            @Override
            public int compare(Pair<Double, Double> p1, Pair<Double, Double> p2) {
                if (p2.first < p1.first)
                    return 1;
                if (p1.first < p2.first)
                    return -1;
                return 0;
            }
        });
        
        double max = Double.POSITIVE_INFINITY;
        for (Pair<Double, Double> pair : old) {
            if (pair.second < max) {
                max = pair.second;
                newPareto.add(pair);
            }
        }
        
        return newPareto;
    }
    
}
