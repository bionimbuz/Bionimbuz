/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.utils.Pair;
import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.pow;
import static java.lang.StrictMath.sin;
import static java.lang.StrictMath.sqrt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
     * @return A pair containing the pareto curve and the remaining ResourceLists
     */
    public static Pair<List<ResourceList>, List<ResourceList>> getParetoCurve(List<ResourceList> resources) {
        
        List<ResourceList> paretoCurve = new ArrayList();
        List<ResourceList> remaining = new ArrayList();
        
        double limitTime = Double.MAX_VALUE;
        
        // sort the list of ResourceList by cost 
        Collections.sort(resources, new Comparator<ResourceList>() {
            // returns 1 if r2 before r1 and -1 otherwise
            @Override
            public int compare(ResourceList r1, ResourceList r2) {
                return r2.getFullCost() < r1.getFullCost() ? 1 : -1;
            }
        });
        
        // for all remaining pairs
        for (ResourceList resource : resources) {
            double execTime = resource.getMaxTime();
            // if second argument is less than or equal to the limit
            if (execTime <= limitTime) {
                // add to the curve and set it as the new limit
                paretoCurve.add(resource);
                limitTime = execTime;
            } else
                remaining.add(resource);
        }
        
        return new Pair(paretoCurve, remaining);
    }
    /**
     * get the point from the ordered list closest to the intersection 
     * between the pareto curve and the vector given by the angle alpha
     * @param cParetoCurve An ordered List of ResourceList representing an optimal
     * pareto set
     * @param alpha the angle of the vector in radians (must be between 0 and 1.57)
     * @return A ResourceList with cost/time closest to vector given by alpha
     */
    public static ResourceList getParetoOptimal(List<ResourceList> cParetoCurve, double alpha) {        
        
        // set closest point as inf
        ResourceList closestPoint = null;
        double minDistance = Double.MAX_VALUE;
        
        List<ResourceList> paretoCurve;
        

        if (cParetoCurve.size() < 2) {
            paretoCurve = new ArrayList<ResourceList>(cParetoCurve);

            // add (0,inf) and (inf,0) if the number of points is less than 2
            ResourceList infTime = new ParetoInfResourceList(ParetoInfResourceList.ElemType.TIME);
            ResourceList infCost = new ParetoInfResourceList(ParetoInfResourceList.ElemType.COST);
            paretoCurve.add(infTime);
            paretoCurve.add(infCost);
        } else
            paretoCurve = cParetoCurve;
        
        // for each pareto curve point
        for (ResourceList resource : paretoCurve) {
            // update closest point
            double cost = resource.getFullCost();
            double execTime = resource.getMaxTime();
            double distance = getDistanceFromCurve(alpha, cost, execTime);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = resource;
            }
        }
        
        return closestPoint;
    }
    
    private static double getDistanceFromCurve(double alpha, double x0, double y0) {
        
        // create the vector of alpha
        double x1, y1, x2, y2;
        x1 = 0d;
        y1 = 0d;
        x2 = cos(alpha);
        y2 = sin(alpha);
        
        // calculate distance
        return abs((y2-y1)*x0-(x2-x1)*y0 + x2*y1 - y2*x1)/
                sqrt(pow(y2-y1, 2) + pow(x2-x1, 2));
    }
    
}
