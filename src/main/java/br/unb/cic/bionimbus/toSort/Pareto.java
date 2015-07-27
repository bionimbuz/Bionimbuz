/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package br.unb.cic.bionimbus.toSort;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.atan;
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
    
    // get the pareto curve of a list of points
    public static List<ResourceList> getParetoCurve(List<ResourceList> resources) {
        
        List<ResourceList> paretoCurve = new ArrayList();
        
        // set null element as the current limit
//        Float limitCost = Float.MAX_VALUE;
        Float limitTime = Float.MAX_VALUE;
//        Float newAngle;
//        Float oldAngle = (float) 0;
        
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
            Float cost = resource.getFullCost();
            Float execTime = resource.getMaxTime();
//            newAngle = (float) atan((cost)/(execTime));
//            System.out.println("(c=" + cost + ", t=" + execTime + ")");
//            System.out.println("angle = " + newAngle);
//            System.out.println("");
            
            // if second argument is less than or equal to the limit
            if (execTime <= limitTime) {
                // remove previous points that aren't pareto optimal by adding this new one
                boolean eliminated;
                if (paretoCurve.size() >= 2) {
                    do {
                        ResourceList p1 = paretoCurve.get(paretoCurve.size()-1);
                        ResourceList p2 = paretoCurve.get(paretoCurve.size()-2);

                        // calculate the angle (new.time,p1.cost)-p1-new
                        Float firstAngle = (float) atan((cost-p1.getFullCost())/(p1.getMaxTime()-execTime));

                        // calculate the angle (p1.time,p2.cost)-p2-p1
                        Float secondAngle = (float) atan((p1.getFullCost()-p2.getFullCost())/(p2.getMaxTime()-p1.getMaxTime()));

                        // remove p1 if it doesn't belong anymore to the pareto curve
                        eliminated = false;
                        if (firstAngle < secondAngle) {
                            paretoCurve.remove(paretoCurve.size());
                            eliminated = true;
                        }
                    } while(eliminated);
                }
                
                // add to the curve and set it as the new limit
                paretoCurve.add(resource);
//                limitCost = cost;
                limitTime = execTime;
            }
        }
        
        return paretoCurve;
    }
    
    // get the point from the ordered list closest to the intersection between
    // the pareto curve and the vector given by the angle alpha
//    public static Entry<Float, Float> getParetoOptimal(TreeMap<Float, Float> paretoCurve, Float alpha) {
    public static ResourceList getParetoOptimal(List<ResourceList> paretoCurve, Float alpha) {        
        
        // set closest point as inf
        ResourceList closestPoint = null;
        Float minDistance = Float.MAX_VALUE;
        
        // for each pareto curve point
        for (ResourceList resource : paretoCurve) {
            // update closest point
            Float cost = resource.getFullCost();
            Float execTime = resource.getMaxTime();
            Float distance = getDistanceFromCurve(alpha, cost, execTime);
            if (distance < minDistance) {
                minDistance = distance;
                closestPoint = resource;
            }
        }
        
        return closestPoint;
    }
    
    public static Float getDistanceFromCurve(Float alpha, Float x0, Float y0) {
        
        // create the vector of alpha
        Float x1, y1, x2, y2;
        x1 = new Float(0);
        y1 = new Float(0);
        x2 = new Float(cos(alpha));
        y2 = new Float(sin(alpha));
        
        // calculate distance
        Float distance = new Float(abs((y2-y1)*x0-(x2-x1)*y0 + x2*y1 - y2*x1)/
                sqrt(pow(y2-y1, 2) + pow(x2-x1, 2)));
        
        return distance;
    }
    
}
