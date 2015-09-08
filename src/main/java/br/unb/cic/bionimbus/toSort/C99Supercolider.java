/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbus.toSort;

import br.unb.cic.bionimbus.client.JobInfo;
import br.unb.cic.bionimbus.client.PipelineInfo;
import br.unb.cic.bionimbus.plugin.PluginInfo;
import br.unb.cic.bionimbus.plugin.PluginTask;
import br.unb.cic.bionimbus.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbus.utils.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author willian
 */
public class C99Supercolider extends SchedPolicy {

    private ResourceList best;
    private List<JobInfo> jobs;
    private final List<ResourceList> bestList = new ArrayList<ResourceList>();
    private int s2best = 0;
    private int s3best = 0;
    private int beam;
    // between 0 and 1.57
    //        time   cost
    private final double alpha = 0.75d;
    public Integer id = 0;
    
    /**
     * Run the C99Supercolider Three stages Scheduling Algorithm. The stages are:
     * Stage One: Optimistic Greedy Search - O(rl.size * jobs.size).
     * Stage Two: Limited Discrepancy Search - O((jobs.size * (jobs.size+1))/2) ~ O(jobs.size^2)
     * Stage Three: Iterative Widening Beam Search - O(rl.size ^ jobs.size)
     * @param rl List of resources available for scheduling.
     * @param jobs List of jobs that need scheduling.
     * @return An updated ResourceList with all jobs scheduled.
     * 
     * @see C99Supercolider.stageOne
     * @see C99Supercolider.stageTwo
     * @see C99Supercolider.stageThree
     */
    public ResourceList schedule(ResourceList rl, List<JobInfo> jobs) {
        SearchNode root = stageOne(rl, new LinkedList<JobInfo>(jobs));
//        recursiveSeachNodePrint(root, 0);
        stageTwo(root, new LinkedList<JobInfo>(jobs));
//        recursiveSeachNodePrint(root, 0);
        this.jobs = jobs;
        stageThree(root, rl.resources.size());
//        recursiveSeachNodePrint(root, 0);
        
        return best;
    }
    
    /**
     * Stage One: Optimistic Greedy Search - O(rl.size * jobs.size).
     * Schedule all jobs based on local best decision given by the solution 
     * closest to the optimization vector given by alpha in the pareto-optimal
     * set. Only one node by task is visited.
     * @param rl List of resources available for scheduling.
     * @param jobs List of jobs that need scheduling.
     * @return The root node of the search tree.
     */
    private SearchNode stageOne(ResourceList rl, Queue<JobInfo> jobs) {
        // create the root node
        SearchNode node = new SearchNode(rl, id);
        id++;
        SearchNode nextNode;
        SearchNode root = node;
        JobInfo job;
        
        System.out.println("Stage One");
        System.out.println("");
        
        // allocate all jobs
        while (!jobs.isEmpty()) {
            job = jobs.poll();
            node.toVisit = generatePriorityQueue(job, node.rl);
            nextNode = node.toVisit.poll();
            node.visiting.add(nextNode);
//            printSearchNode(node, 0);
            System.out.println("priorityQueue:");
            for (Resource r : nextNode.rl.resources) {
                System.out.println(r.toString());
            }
            System.out.println();
            // set the next node (depth. next task) by the priority queue 
            // toVisit of the current node
            node = nextNode;
        }
//        printSearchNode(node, 0);
        
        // set the current best as the solution previously found
        best = node.rl;
        bestList.add(new ResourceList(best));
        return root;
    }

    /**
     * Stage Two: Limited Discrepancy Search - O((jobs.size * (jobs.size+1))/2) ~ O(jobs.size^2).
     * Given the Stage one search, it is assumed that one choice is wrong and 
     * thus the scheduling after such choice is performed one more with the 
     * second best local choice. This algorithm runs for all choices (job 
     * allocations), thereby running jobs.size times with decrescent depth of
     * jobs.size - currentWrongNode.depth (considering that the algorithm starts 
     * reworking the first node).
     * @param root
     * @param jobs 
     */
    private void stageTwo(SearchNode root, Queue<JobInfo> jobs) {
        SearchNode node;
        SearchNode nextNode;
        
        System.out.println("Stage Two");
        System.out.println("");
        
        // rework every i allocation
        for (int i=0; i<jobs.size(); i++) {
            Queue<JobInfo> jobsCopy = new LinkedList<JobInfo>(jobs);
            int k = i;
            node = root;
            
            // run scheduler for jobsCopy list
            while (!jobsCopy.isEmpty()) {
                JobInfo job = jobsCopy.poll();
                
                // if the allocation is said to be good, go to next node
                if (k > 0) {
                    if (!jobsCopy.isEmpty())
                        node = node.visiting.peek();
                } 
                
                // if not, perform allocation to next best option
                else {
                    // generate toVisit queue if this is the first time 
                    // the node is visited
                    if (node.toVisit.isEmpty())
                        node.toVisit = generatePriorityQueue(job, node.rl);
                    
                    // move nextNode to node.visiting and go to nextNode
                    nextNode = node.toVisit.poll();
                    node.visiting.add(nextNode);
                    node = nextNode;
                }
                k--;
            }
            
            // update best if this is the case
            if (updateBest(node))
                s2best++;
        }
    }
    
    /**
     * Stage Three: Iterative Widening Beam Search - O(rl.size ^ jobs.size).
     * Perform the beam search algorithm several times, increasing the beam 
     * width. The maximum beam width is the number of resources avaliable for 
     * scheduling. 
     * Obs: This algorithm will hardly complete its execution. However, if the 
     * algorithm is stopped the partial solution can still be accessed.
     * @param root
     * @param maxBeam 
     * @see C99Supercolider.beamSearch
     */
    private void stageThree(SearchNode root, int maxBeam) {
        System.out.println("Stage Three");
        System.out.println("");
        
        for (beam = 2; beam <= maxBeam; beam++) {
            beamSearch(root, jobs.size());
        }
    }
    
    /**
     * Run a recursive beam search algorithm with the beam width given by the
     * internal variable beam. The algorithm updates the best internal variable
     * if a new best solution is found.
     * @param node The root node on which the search should start.
     * @param tasksRemaining The number of tasks remaining to be scheduled. 
     * This value should be the size of the task list. The value should be 
     * greater or equal to one and no check is performed to ensure that.
     */
    private void beamSearch(SearchNode node, int tasksRemaining) {
        // calculate how many more nodes should be visited according to the 
        // current beam width
        int needed = beam - (node.visiting.size() + node.visited.size());
        
        // widen the visiting list/search beam if necessary
        if (needed > 0) {
            // wide the search bean by 'needed' elements
            for (int i=0; i<needed; i++) {
                // transfer a node from to visit to visiting
                SearchNode nextNode = node.toVisit.poll();
                node.visiting.add(nextNode);
                
                // if the node isn't a leaf, thereby needing to generate for the
                // first time its toVisit queue
                if ((tasksRemaining-1) > 0)
                    nextNode.toVisit = generatePriorityQueue(jobs.get(jobs.size() - tasksRemaining), nextNode.rl);
            }
        }
        
        // iterate through every visiting node n
        for (Iterator<SearchNode> iterator = node.visiting.iterator(); iterator.hasNext();) {
            SearchNode n = iterator.next();
            
            // if there still are nodes to visit, run them recursively
            if ((n.visiting.size() + n.toVisit.size()) > 0)
                beamSearch(n, tasksRemaining-1);
            else {
                // if the reason there are no more nodes to visit is that this 
                // is a leaf node
                if (tasksRemaining == 1) {
                    // calculate the leaf cost and update best if it's a new best
                    if (updateBest(n))
                        s3best++;
                }
                
                // move node to visited list
                iterator.remove();
                node.visited.add(n);
            }
        }
    }
    
    /***********************************************************/
    /******************* Helper Functions **********************/
    /***********************************************************/
    
    /**
     * Generate a queue containing all possible combinations of allocations for
     * a job, given a ResourceList. The queue has two parts, a pareto-optimal 
     * ordered part and a remaining solution part. The first, as it says, it's 
     * the pareto-optimal curve of all possible solution ordered by its 
     * proximity to the optimization vector given by the internal variable alpha.
     * The latest one is the remaining solutions that were dominated by the
     * pareto curve. This set of solutions is added to the final queue to ensure
     * completeness.
     * 
     * TODO: maybe order the remaining solution set by it's nodes proximity to 
     * the optimization vector.
     * @param job The job to be scheduled.
     * @param rl The ResourceList on which the job should be scheduled.
     * @return A queue of SeachNode containing all possibilities of scheduling
     * of the input job on the given ResourceList, ordered according to the 
     * above description.
     */
    private Queue<SearchNode> generatePriorityQueue(JobInfo job, ResourceList rl) {
        List<ResourceList> rls = new ArrayList<ResourceList>();
        
        // Generate a list of ResourceList on which a task is allocated to 
        // every single resource. A single ResourceList can't have more than
        // one job allocated to the set of all its resources
        for (Resource r : rl.resources) {
            ResourceList rlTemp = new ResourceList(rl);
            rlTemp.resources.get(rl.resources.indexOf(r)).allocateTask(job);
            rls.add(rlTemp);
        }
        
        // generate a pareto curve and also get the remaining ResourceLists
        Pair<List<ResourceList>, List<ResourceList>> rlPair = Pareto.getParetoCurve(rls);
        List<ResourceList> pareto = rlPair.first;
//        System.out.println("Resources:");
//        System.out.println(Arrays.toString(rls.toArray()));
//        System.out.println("generatePriorityQueue pareto: ");
//        System.out.println(Arrays.toString(pareto.toArray()));
        List<ResourceList> remaining = rlPair.second;
//        System.out.println("generatePriorityQueue remaining size: " + remaining.size());
        
        // create an ordered queue by how pareto-optimal a solution is
        Queue<SearchNode> priorityQueue = new LinkedList<SearchNode>();
        while (!pareto.isEmpty()) {
            ResourceList currentBest = Pareto.getParetoOptimal(pareto, alpha);
            pareto.remove(currentBest);
            priorityQueue.add(new SearchNode(currentBest, id));
            id++;
        }
        
//        System.out.println("out");
        
        // create another queue with the remaining solutions, thus, 
        // guaranteeing completeness 
        Queue<SearchNode> remainingQueue = new LinkedList<SearchNode>();
        for (ResourceList r : remaining) {
            remainingQueue.add(new SearchNode(r, id));
            id++;
        }
        
        // return both queues, with the pareto-optimal solutions first
        priorityQueue.addAll(remainingQueue);
        return priorityQueue;
    }
    
    /**
     * Updates the current internal best solution, also adding it to the 
     * solution list. It does not updates the solution counter (e.g. s2best).
     * Obs: The ResourceList variables from best and node are copied to ensure 
     * that neither of them will be updated after being set the new best.
     * @param node The node to be compared to the current best.
     * @return True if it's a new best, false otherwise.
     */
    boolean updateBest(SearchNode node) {
        List<ResourceList> lrl = new ArrayList<ResourceList>();
        lrl.add(new ResourceList(best));
        ResourceList newRl = new ResourceList(node.rl);
        lrl.add(newRl);

        ResourceList newBest = Pareto.getParetoOptimal(lrl, alpha);
        if (newBest == newRl) {
            System.out.println("New best");
            bestList.add(newBest);
            best = newBest;
            return true;
        }
        return false;
    }
    
    /***********************************************************/
    /******************* Printing Methods **********************/
    /***********************************************************/
    
    
    private void printSearchNode(SearchNode node, int n) {
        printTab(n);
        System.out.println("Node" + node.id + ": width" + node.beamWidth);
        
        printTab(n);
        System.out.println("Resources state:");
        printTab(n);
        System.out.println(Arrays.toString(node.rl.resources.toArray()));
        
        printTab(n);
        System.out.println("ToVisit state:");
        printTab(n);
        System.out.println(Arrays.toString(node.toVisit.toArray()));
        
        printTab(n);
        System.out.println("Visiting state:");
        printTab(n);
        System.out.println(Arrays.toString(node.visiting.toArray()));
        
        printTab(n);
        System.out.println("Visited state:");
        printTab(n);
        System.out.println(Arrays.toString(node.visited.toArray()));
        
        System.out.println("");
    }
    
    private void recursiveSeachNodePrint(SearchNode node, int n) {
        printSearchNode(node, n);
        for (SearchNode s : node.visiting)
            recursiveSeachNodePrint(s, n+1);
    }

    private void printTab(int n) {
        for (int i=0; i<n; i++)
            System.out.print("    ");
    }
    
    /***********************************************************/
    /************************* Main ****************************/
    /***********************************************************/
    
    /**
     * Test function
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        List<PipelineInfo> pipelines = PipelineTestGenerator.getPipelinesTemplates();
        List<PluginInfo> resources = PipelineTestGenerator.getResourceTemplates();
        final C99Supercolider scheduler = new C99Supercolider();
        int maxExecTime = 10; // seconds
        
        // run all pipelines
        for (PipelineInfo p : pipelines) {
            
            // convert List<PluginInfo> resources into a ResourceList
            final ResourceList rl = new ResourceList();
            for (PluginInfo info : resources) {
                Resource r = new Resource(info.getId(),
                            info.getFactoryFrequencyCore(),
                            info.getCostPerHour());
                rl.resources.add(r);
            }
            
            // create a thread to run the pipeline
            ExecutorService executor = Executors.newFixedThreadPool(1);
            final PipelineInfo pipeline = p;

            // execute pipeline scheduling
            Future<?> future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    scheduler.schedule(rl, pipeline.getJobs());
                }
            });
            executor.shutdown();            //        <-- reject all further submissions

            // wait for scheduling to finish or timeout
            try {
                future.get(maxExecTime, TimeUnit.SECONDS);  //     <-- wait 8 seconds to finish
            } catch (InterruptedException e) {    //     <-- possible error cases
                System.out.println("job was interrupted");
            } catch (ExecutionException e) {
                System.out.println("caught exception: " + e.getCause());
            } catch (TimeoutException e) {
                future.cancel(true);              //     <-- interrupt the job
                System.out.println("timeout");
            }

            // force scheduler to stop
            // TODO: scheduler don't actually stops
            executor.shutdownNow();
            
            // Print test input
            System.out.println("Resources:");
            for (PluginInfo pi : resources) {
                System.out.println("Id: " + pi.getId() + ", cost: " + pi.getCostPerHour() + ", freq: " + pi.getFactoryFrequencyCore()/1000000000);
            }
            System.out.println("");
            System.out.println("Tasks:");
            for (JobInfo j : pipelines.get(0).getJobs()) {
                System.out.println("Id: " + j.getId() + ", cost: " + j.getWorstExecution());
            }
            
            // Print test output
            System.out.println("");
            System.out.println("Best List:");
            for (ResourceList rll : scheduler.bestList) {
                System.out.println(rll);
            }
            System.out.println("Stage One: 1 - Stage Two: " + scheduler.s2best + " - Stage Three: " + scheduler.s3best);
            System.out.println("Final beam: " + scheduler.beam);
        }
    }
    
    /***********************************************************/
    /************************* TODO ****************************/
    /***********************************************************/
    
    @Override
    public HashMap<JobInfo, PluginInfo> schedule(List<JobInfo> jobs) {
        schedule(rs.getCurrentResourceList(), jobs);
        
        HashMap<JobInfo, PluginInfo> sched = new HashMap<JobInfo, PluginInfo>();
        Map<String, PluginInfo> peers = rs.getPeers();
        
        for (Resource r : best.resources) {
            for (JobInfo j : r.getAllocatedTasks()) {
                System.out.println("T" + j.getId() + " -> R" + r.id);
                sched.put(j, peers.get(r.id));
            }
        }

        return sched;
    }

    @Override
    public List<PluginTask> relocate(Collection<Pair<JobInfo, PluginTask>> taskPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelJobEvent(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void jobDone(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getPolicyName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}
