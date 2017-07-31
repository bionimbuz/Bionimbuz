/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.unb.cic.bionimbuz.services.sched.policy.impl;

import br.unb.cic.bionimbuz.model.Instance;
import br.unb.cic.bionimbuz.model.Job;
import br.unb.cic.bionimbuz.model.User;
import br.unb.cic.bionimbuz.model.Workflow;
import br.unb.cic.bionimbuz.plugin.PluginInfo;
import br.unb.cic.bionimbuz.plugin.PluginTask;
import br.unb.cic.bionimbuz.services.monitor.MonitoringService;
import br.unb.cic.bionimbuz.services.sched.policy.SchedPolicy;
import br.unb.cic.bionimbuz.tests.FromLogFileTestGenerator;
import br.unb.cic.bionimbuz.services.sched.model.Pareto;
import br.unb.cic.bionimbuz.tests.PipelineTestGenerator;
import br.unb.cic.bionimbuz.services.sched.model.Resource;
import br.unb.cic.bionimbuz.services.sched.model.ResourceList;
import br.unb.cic.bionimbuz.services.sched.model.SearchNode;
import br.unb.cic.bionimbuz.utils.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.aspectj.apache.bcel.verifier.exc.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author willian
 */
public class C99Supercolider extends SchedPolicy {

    private static final Logger LOGGER = LoggerFactory.getLogger(C99Supercolider.class);

    private ResourceList best;
    private List<Job> jobs;
    private List<ResourceList> bestList = new ArrayList<>();
    private int s2best = 0;
    private int s3best = 0;
    private int beam = 0;

    // alpha between 0 and 1
    //              time  cost
    private final double alpha = 0.5d;

    public Long id = 0l;
    private List<ResourceList> solutionsList = new ArrayList<>();
    private long prunableNodes = 0;
    private long pruned = 0;
    private long removedFromSearch = 0;
    private long finalSolutionBeam = 0;
    private int recoveryMemory[];
    private boolean outOfMemory = false;
    private final int numMaxResources;
    private long searchedNodes = 0;

    List<Job> jobsThis = new ArrayList<>();

    public Lock execLock;

    public C99Supercolider() {
        numMaxResources = 0;
    }

    public C99Supercolider(int numMaxResources) {
        this.numMaxResources = numMaxResources;
    }

    /**
     * Run the C99Supercolider Three stages Scheduling Algorithm. The stages
     * are: Stage One: Optimistic Greedy Search - O(rl.size * jobs.size). Stage
     * Two: Limited Discrepancy Search - O((jobs.size * (jobs.size+1))/2) ~
     * O(jobs.size^2) Stage Three: Iterative Widening Beam Search - O(rl.size ^
     * jobs.size)
     *
     * @param rl List of resources available for scheduling.
     * @param jobs List of jobs that need scheduling.
     * @return An updated ResourceList with all jobs scheduled.
     *
     * @see C99Supercolider.stageOne
     * @see C99Supercolider.stageTwo
     * @see C99Supercolider.stageThree
     */
    public ResourceList schedule(ResourceList rl, List<Job> jobs) {
        SearchNode root;

        jobsThis = jobs;

        // lock to ensure that the execution will be finished
        execLock = new ReentrantLock();

        try {
            execLock.lock();
            root = stageOne(rl, new LinkedList<>(jobs));
            System.out.println("best - " + bestList.size());
            for (ResourceList rll : bestList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }

            System.out.println("");
            System.out.println("full - " + solutionsList.size());
            for (ResourceList rll : solutionsList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }
//                recursiveSeachNodePrint(root, 0);
            stageTwo(root, new LinkedList<>(jobs));
            System.out.println("");
            System.out.println("___________________________________________________________________________________________");
            System.out.println("");
            System.out.println("best - " + bestList.size());
            for (ResourceList rll : bestList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }

            System.out.println("");
            System.out.println("full - " + solutionsList.size());
            for (ResourceList rll : solutionsList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }
            System.out.println("");
            System.out.println("___________________________________________________________________________________________");
            System.out.println("");
//                recursiveSeachNodePrint(root, 0);
            this.jobs = jobs;
            stageThree(root, rl.resources.size());

//                recursiveSeachNodePrint(root, 0);
        } catch (Exception e) {
            System.out.println("[schedule] exception: " + e.getMessage());
            System.out.println(Utility.getStackTrace(e));
        } finally {
            execLock.unlock();
        }

        return best;
    }

    /**
     * Stage One: Optimistic Greedy Search - O(rl.size * jobs.size). Schedule
     * all jobs based on local best decision given by the solution closest to
     * the optimization vector given by alpha in the pareto-optimal set. Only
     * one node by task is visited.
     *
     * @param rl List of resources available for scheduling.
     * @param jobs List of jobs that need scheduling.
     * @return The root node of the search tree.
     */
    private SearchNode stageOne(ResourceList rl, Queue<Job> jobs) {
        // create the root node
        SearchNode node = new SearchNode(rl, id, 0);
        id++;
        searchedNodes++;
        SearchNode nextNode;
        SearchNode root = node;
        Job job;
        long depth = 1;

        LOGGER.info("Stage One");

        // allocate all jobs
        while (!jobs.isEmpty()) {
            job = jobs.poll();
            node.toVisit = generatePriorityQueue(job, node.rl, depth);
            nextNode = node.toVisit.poll();
            node.visiting.add(nextNode);
//            printSearchNode(node, 0);
//            System.out.println("priorityQueue:");
//            for (Resource r : nextNode.rl.resources) {
//                System.out.println(r.toString());
//            }
//            System.out.println();

            // set last node as visited to the second-last node
            if (jobs.isEmpty()) {
                node.visiting.poll();
                node.visitedCount++;
            }

            // set the next node (depth. next task) by the priority queue 
            // toVisit of the current node
            node = nextNode;
            depth++;
            searchedNodes++;
        }
//        printSearchNode(node, 0);

        // set the current best as the solution previously found
        best = node.rl;
        bestList.add(new ResourceList(best));
        solutionsList.add(new ResourceList(best));

        // set the pareto lists for the pruning
        node = root;
        node.addToPareto(best.getAvgTime(rs), best.getFullCost(rs));
        while (!node.visiting.isEmpty()) {
            node = node.visiting.peek();
            node.addToPareto(best.getAvgTime(rs), best.getFullCost(rs));
        }

        return root;
    }

    /**
     * Stage Two: Limited Discrepancy Search - O((jobs.size * (jobs.size+1))/2)
     * ~ O(jobs.size^2). Given the Stage one search, it is assumed that one
     * choice is wrong and thus the scheduling after such choice is performed
     * one more with the second best local choice. This algorithm runs for all
     * choices (job allocations), thereby running jobs.size times with
     * decrescent depth of jobs.size - currentWrongNode.depth (considering that
     * the algorithm starts reworking the first node).
     *
     * @param root
     * @param jobs
     */
    private void stageTwo(SearchNode root, Queue<Job> jobs) {
        SearchNode node;
        SearchNode nextNode;
        long depth;

        LOGGER.info("Stage Two");

        // rework every i allocation
        for (int i = 0; i < jobs.size(); i++) {
            Queue<Job> jobsCopy = new LinkedList<>(jobs);
            int k = i;
            node = root;
            depth = 1;

            Stack<SearchNode> backtrackStack = new Stack<>();

            // run scheduler for jobsCopy list
            while (!jobsCopy.isEmpty()) {
                Job job = jobsCopy.poll();

                // if the allocation is said to be good, go to next node
                if (k > 0 && !jobsCopy.isEmpty()) {
                    backtrackStack.push(node);
                    node = node.visiting.peek();
                } // if not, perform allocation to next best option
                else {
                    // generate toVisit queue if this is the first time 
                    // the node is visited
                    if (node.toVisit.isEmpty()) {
                        node.toVisit = generatePriorityQueue(job, node.rl, depth);
                    }

                    // move nextNode to node.visiting and go to nextNode
                    nextNode = node.toVisit.poll();
                    node.visiting.add(nextNode);
                    backtrackStack.push(node);
                    node = nextNode;
                    searchedNodes++;
                }
                k--;
                depth++;
            }

            // update best if this is the case
            if (updateBest(node)) {
                s2best++;
            }

            SearchNode beforeLast = backtrackStack.pop();
//            System.out.println("beforeLast: " + beforeLast.id);
            beforeLast.visiting.poll();
            beforeLast.visitedCount++;
            beforeLast.addToPareto(node.rl.getMaxTime(rs), node.rl.getFullCost(rs));

            // backtrack the pareto results needed for pruning
            while (!backtrackStack.isEmpty()) {
                backtrackStack.pop().addToPareto(node.rl.getMaxTime(rs), node.rl.getFullCost(rs));
            }
        }
    }

    /**
     * Stage Three: Iterative Widening Beam Search - O(rl.size ^ jobs.size).
     * Perform the beam search algorithm several times, increasing the beam
     * width. The maximum beam width is the number of resources avaliable for
     * scheduling. Obs: This algorithm will hardly complete its execution.
     * However, if the algorithm is stopped the partial solution can still be
     * accessed.
     *
     * @param root
     * @param maxBeam
     * @see C99Supercolider.beamSearch
     */
    private void stageThree(SearchNode root, int maxBeam) {
        LOGGER.info("Stage Three");

        for (beam = 2; beam <= maxBeam; beam++) {
            LOGGER.info("Beam width: " + beam);
            beamSearch(root, jobs.size(), 0);
            // exit if this thread was interrupted
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Stage Three interrupted");
                return;
            }
            System.out.println("best - " + bestList.size());
            for (ResourceList rll : bestList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }

            System.out.println("");
            System.out.println("full - " + solutionsList.size());
            for (ResourceList rll : solutionsList) {
                System.out.println("[" + rll.getFullCost(rs) + "," + rll.getMaxTime(rs) + "];");
            }
        }
    }

    /**
     * Run a recursive beam search algorithm with the beam width given by the
     * internal variable beam. The algorithm updates the best internal variable
     * if a new best solution is found.
     *
     * @param node The root node on which the search should start.
     * @param tasksRemaining The number of tasks remaining to be scheduled. This
     * value should be the size of the task list. The value should be greater or
     * equal to one and no check is performed to ensure that.
     */
    private List<Pair<Double, Double>> beamSearch(SearchNode node, int tasksRemaining, long depth) {
        // exit if this thread was interrupted
        if (Thread.currentThread().isInterrupted()) {
            LOGGER.info("BeamSearch interrupted on node: " + node.id);
            return null;
        }

//        printSearchNode(node, 0);
//        System.out.println("");
        // calculate how many more nodes should be visited according to the 
        // current beam width
        long needed = beam - (node.visiting.size() + node.visitedCount + node.prunedChildren);

        // widen the visiting list/search beam if necessary
        if (needed > 0) {
            // wide the search bean by 'needed' elements
            for (int i = 0; i < needed; i++) {
                // transfer a node from to visit to visiting
                SearchNode nextNode = node.toVisit.poll();
                node.visiting.add(nextNode);
                searchedNodes++;

                // if the node isn't a leaf, thereby needing to generate for the
                // first time its toVisit queue
                if (tasksRemaining > 1) {
                    nextNode.toVisit = generatePriorityQueue(jobs.get(jobs.size() - tasksRemaining), nextNode.rl, depth);
                }
            }
        }

        // iterate through every visiting node n
        for (Iterator<SearchNode> iterator = node.visiting.iterator(); iterator.hasNext();) {
            SearchNode n = iterator.next();

            // if there still are nodes to visit, run them recursively
            if ((n.visiting.size() + n.toVisit.size()) > 0) {
                // prune child node if possible
                if (n.prunable && (n.rl.getMaxTime(rs) > node.getMaxt() || n.rl.getFullCost(rs) > node.getMaxc())) {
//                    System.out.println("Node " + n.id + " depth " + depth + " pruned");
                    iterator.remove();
                    node.prunedChildren++;
                    pruned++;
                    removedFromSearch += childNodesCount(tasksRemaining - 1);
                } else {
                    // add all newly generated pareto points from child node
                    node.addToPareto(beamSearch(n, tasksRemaining - 1, depth + 1));

                    if (Thread.currentThread().isInterrupted()) {
                        LOGGER.info("Interrupted on node: " + node.id);
                        return null;
                    }

                    // remove child if there is no more seaching to be done on it
                    if (n.toVisit.size() + n.visiting.size() == 0) {
                        node.visitedCount++;
                        iterator.remove();
                    }
                }
            } else // if the reason there are no more nodes to visit is that this 
            // is a leaf node
            if (tasksRemaining == 1) {
                // calculate the leaf cost and update best if it's a new best
                if (updateBest(n)) {
                    s3best++;
                }
                node.addToPareto(n.rl.getMaxTime(rs), n.rl.getFullCost(rs));
                node.visitedCount++;

                // remove node from visiting list
                iterator.remove();
            }
        }

//        printSearchNode(node, 0);
//        System.out.println("");
//        System.out.println("____________________________________________________________");
//        System.out.println("");
        return node.getParetoList();
    }

    /**
     * ******************************************************
     */
    /**
     * *************** Helper Functions *********************
     */
    /**
     * ******************************************************
     */
    /**
     * Generate a queue containing all possible combinations of allocations for
     * a job, given a ResourceList. The queue has two parts, a pareto-optimal
     * ordered part and a remaining solution part. The first, as it says, it's
     * the pareto-optimal curve of all possible solution ordered by its
     * proximity to the optimization vector given by the internal variable
     * alpha. The latest one is the remaining solutions that were dominated by
     * the pareto curve. This set of solutions is added to the final queue to
     * ensure completeness.
     *
     * TODO: maybe order the remaining solution set by it's nodes proximity to
     * the optimization vector.
     *
     * @param job The job to be scheduled.
     * @param rl The ResourceList on which the job should be scheduled.
     * @return A queue of SeachNode containing all possibilities of scheduling
     * of the input job on the given ResourceList, ordered according to the
     * above description.
     */
    private Queue<SearchNode> generatePriorityQueue(Job job, ResourceList rl, long depth) {
        List<ResourceList> rls = new ArrayList<>();

        // Generate a list of ResourceList on which a task is allocated to 
        // every single resource. A single ResourceList can't have more than
        // one job allocated to the set of all its resources
        for (Resource r : rl.resources) {
            ResourceList rlTemp = new ResourceList(rl);
            rlTemp.resources.get(rl.resources.indexOf(r)).allocateTask(job);
            rls.add(rlTemp);
        }

        // generate a pareto curve and also get the remaining ResourceLists
        Pair<List<ResourceList>, List<ResourceList>> rlPair = Pareto.getParetoCurve(rls, rs);
        List<ResourceList> pareto = rlPair.first;
        List<ResourceList> remaining = rlPair.second;

        // create an ordered queue by how pareto-optimal a solution is
        Queue<SearchNode> priorityQueue = new LinkedList<>();
        while (!pareto.isEmpty()) {
            ResourceList currentBest = Pareto.getParetoOptimal(pareto, alpha);
            pareto.remove(currentBest);
            priorityQueue.add(new SearchNode(currentBest, id, depth));
            id++;
        }

        // create another queue with the remaining solutions, thus, 
        // guaranteeing completeness 
        while (!remaining.isEmpty()) {
            Pair<List<ResourceList>, List<ResourceList>> rlPairRem = Pareto.getParetoCurve(remaining, rs);
            List<ResourceList> paretoRem = rlPairRem.first;
            remaining = rlPairRem.second;
            while (!paretoRem.isEmpty()) {
                ResourceList currentBest = Pareto.getParetoOptimal(paretoRem, alpha);
                paretoRem.remove(currentBest);
                SearchNode n = new SearchNode(currentBest, id, depth);
                n.prunable = true;
                prunableNodes++;
                priorityQueue.add(n);
                id++;
            }
        }

        return priorityQueue;
    }

    /**
     * Updates the current internal best solution, also adding it to the
     * solution list. It does not updates the solution counter (e.g. s2best).
     * Obs: The ResourceList variables from best and node are copied to ensure
     * that neither of them will be updated after being set the new best.
     *
     * @param node The node to be compared to the current best.
     * @return True if it's a new best, false otherwise.
     */
    boolean updateBest(SearchNode node) {
        List<ResourceList> lrl = new ArrayList<>(solutionsList);
        ResourceList newRl = new ResourceList(node.rl);
        lrl.add(newRl);

        Pair<List<ResourceList>, List<ResourceList>> ret = Pareto.getParetoCurve(lrl, rs);

        // it there is no remaining elements it means that this node has a new good solution
        if (!ret.second.contains(newRl)) {
            // check if the new solution isn't already there
            if (!solutionsList.contains(newRl)) {
                solutionsList.add(newRl);
                ret = Pareto.getParetoCurve(solutionsList, rs);
                bestList = ret.first;
//                ResourceList newBest = Pareto.getParetoOptimal(ret.first, alpha);

                System.out.println("New best - node: " + node.id);
                finalSolutionBeam = beam;
                return true;
            }
        }

        return false;
    }

    public long childNodesCount(long depth) {
        long base = numMaxResources;
        return (long) (pow(base, depth) + floor(pow(base, depth) / (base - 1)));
    }

    /**
     * Allocate memory space equivalent to 1000 integers if needed for OOME.
     * Using Hedging strategy. The allocation is ensured to happen by the end of
     * this method.
     */
    private void allocateRecoveryMemory() {
        recoveryMemory = new int[10000000];
        recoveryMemory[999] = 1;
    }

    /**
     * Release the recovery memory space if needed for treating an OOME. Using
     * Hedging strategy. The desallocation is ensured to happen only after this
     * method.
     */
    private void freeRecoveryMemory() {
        if (recoveryMemory[999] == 1) {
            recoveryMemory[999] = 1;
        }
        recoveryMemory = null;
    }

    /**
     * ******************************************************
     */
    /**
     * *************** Printing Methods *********************
     */
    /**
     * ******************************************************
     */
    private void printSearchNode(SearchNode node, int n) {
        printTab(n);
        System.out.println("Node" + node.id + " - d" + node.depth);

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
        System.out.println("Visited: " + node.visitedCount);

        printTab(n);
        System.out.println("paretoCount: " + node.getParetoList().size());

        printTab(n);
        System.out.println("prunable: " + node.prunable);

        printTab(n);
        System.out.println("pruned childs: " + node.prunedChildren);

        System.out.println("");
    }

    private void recursiveSeachNodePrint(SearchNode node, int n) {
        printSearchNode(node, n);
        for (SearchNode s : node.visiting) {
            recursiveSeachNodePrint(s, n + 1);
        }
    }

    static public String format(long l) {
        return NumberFormat.getNumberInstance(Locale.UK).format(l);
    }

    private void printTab(int n) {
        for (int i = 0; i < n; i++) {
            System.out.print("    ");
        }
    }

    /**
     * ******************************************************
     */
    /**
     * ********************* Main ***************************
     */
    /**
     * ******************************************************
     */
    /**
     * Test function
     *
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
//        RandomTestGenerator gen = new RandomTestGenerator();
        PipelineTestGenerator gen = new FromLogFileTestGenerator(Double.parseDouble(args[0]), args[1], args[2]);
//        PipelineTestGenerator gen = new FromLogFileTestGenerator(50, "LLNL-Thunder-2007-1.1-cln.swf", "res-reduced.txt");
        List<Workflow> pipelines = gen.getPipelinesTemplates();
        List<PluginInfo> resources = gen.getResourceTemplates();

//        testAllTimeoutPipelines(pipelines, resources);
        testFailurePronePipelines(pipelines, resources, Integer.parseInt(args[0]), args[3]);

    }

    private static void testAllTimeoutPipelines(List<Workflow> pipelines, List<PluginInfo> resources) {
        int execTime; // seconds
        int maxExecTime = 120; // seconds
        int timeoutCounts = 5;

        // run all pipelines
        for (Workflow p : pipelines) {
            // run each pipeline with different max execution times
            execTime = maxExecTime / timeoutCounts;
            boolean finished = false;
            for (int i = 1; i < timeoutCounts && !finished; i++) {
                C99Supercolider scheduler = new C99Supercolider(resources.size());
                finished = runPipeline(resources, execTime, scheduler, p);

                // Print test input
                System.out.println("MaxExecTime: " + execTime);
                if (finished) {
                    System.out.println("Complete sched");
                } else {
                    System.out.println("Incomplete sched");
                }

                System.out.println("Resources:");
                for (PluginInfo pi : resources) {
                    System.out.println("Id: " + pi.getId() + ", cost: " + pi.getCostPerHour() + ", freq: " + pi.getFactoryFrequencyCore() / 1000000000);
                }
                System.out.println("");
                System.out.println("Tasks:");
                for (Job j : pipelines.get(0).getJobs()) {
                    System.out.println("Id: " + j.getId() + ", cost: " + j.getWorstExecution());
                }

                // Print test output
//                System.out.println("");
//                System.out.println("Best List:");
//                for (ResourceList rll : scheduler.bestList) {
//                    System.out.println(rll);
//                }
                System.out.println("");
                System.out.println("Best List:");
                // sort the list of ResourceList by max exec time
                Collections.sort(scheduler.bestList, new Comparator<ResourceList>() {
                    // returns 1 if r2 before r1 and -1 otherwise
                    @Override
                    public int compare(ResourceList r1, ResourceList r2) {
                        if (r2.getMaxTime(rs) < r1.getMaxTime(rs)) {
                            return 1;
                        }
                        if (r2.getMaxTime(rs) > r1.getMaxTime(rs)) {
                            return -1;
                        } else {
                            if (r2.getFullCost(rs) < r1.getFullCost(rs)) {
                                return 1;
                            }
                            if (r2.getFullCost(rs) > r1.getFullCost(rs)) {
                                return -1;
                            }
                        }
                        return 0;
                    }
                });
                for (ResourceList rll : scheduler.bestList) {
                    System.out.println(rll);
                }

                System.out.println("Stage One: 1 - Stage Two: " + scheduler.s2best + " - Stage Three: " + scheduler.s3best);
                System.out.println("Final beam: " + (scheduler.beam - 1));
                System.out.println("Max number of nodes to search: " + format(scheduler.childNodesCount(p.getJobs().size())));
                System.out.format("Searched Nodes created: " + format(scheduler.id) + " - %5.5f%% of all possible nodes %n", 100 * (float) (scheduler.id) / scheduler.childNodesCount(p.getJobs().size()));
                System.out.format("Prunable nodes: " + format(scheduler.prunableNodes) + " - %5.5f%% of searched nodes %n",
                        100 * (float) scheduler.prunableNodes / (float) scheduler.id.doubleValue());
                System.out.format("Pruned nodes: " + format(scheduler.pruned) + " - %5.5f%% of searched nodes %n", 100 * ((float) scheduler.pruned / (float) scheduler.id));
                System.out.format("Nodes removed from search: " + format(scheduler.removedFromSearch) + " - %5.5f%% of all possible nodes %n", 100 * (float) scheduler.removedFromSearch / scheduler.childNodesCount(p.getJobs().size()));
                System.out.println("___________________________________________________________________________________________________");
                System.out.println("");
                System.out.println("");

                // update execTime
                execTime += maxExecTime / timeoutCounts;
            }
        }
    }

    private static void testFailurePronePipelines(List<Workflow> pipelines, List<PluginInfo> resources, int maxExecTime, String output) {
        long resNum = resources.size();
        int i = 0;
        PrintWriter writer = null;

        // open output file
        try {
            System.out.println("oppening output file");
            writer = new PrintWriter(new FileOutputStream(new File(output), true));
        } catch (FileNotFoundException ex) {
            LOGGER.error("[FileNotFoundException] - " + ex.getMessage());

            ex.printStackTrace();
        }

        for (Workflow pipeline : pipelines) {
            // this if is used to simulate a resumed test
            // negative i means do it from begining
            if (i > -1) {
                C99Supercolider scheduler = new C99Supercolider(resources.size());
                System.out.println("running pipeline " + i + " - " + Calendar.getInstance().getTime().toString());
                boolean finished = false;
                try {
                    finished = runPipeline(resources, maxExecTime, scheduler, pipeline);
                } catch (Error e) {
                    System.out.println(Utility.getStackTrace(e));
                    System.out.println(e.toString());
                } catch (Exception e) {
                    System.out.println(Utility.getStackTrace(e));
                    System.out.println(e.toString());
                }

                // assemble result data
                String result = "" + i + "\t"
                        + resNum + "\t"
                        + pipeline.getJobs().size() + "\t"
                        + "1\t"
                        + scheduler.s2best + "\t"
                        + scheduler.s3best + "\t"
                        + scheduler.beam + "\t"
                        + scheduler.childNodesCount(pipeline.getJobs().size()) + "\t"
                        + scheduler.searchedNodes + "\t"
                        + scheduler.prunableNodes + "\t"
                        + scheduler.pruned + "\t"
                        + scheduler.removedFromSearch + "\t"
                        + scheduler.bestList.size() + "\t"
                        + scheduler.finalSolutionBeam + "\t"
                        + scheduler.outOfMemory + "\t"
                        + finished + "\t[";
                for (ResourceList rll : scheduler.bestList) {
                    result += rll.result(rs) + "; ";
                }
                result += "]\t";
                for (ResourceList rll : scheduler.bestList) {
                    result += rll.toString() + "; ";
                }
                result += "]";

                System.out.println("");
                System.out.println("tasks - " + scheduler.jobsThis.size());
                for (Job rll : scheduler.jobsThis) {
                    System.out.println(rll.toString());
                }

                // flush test data
                writer.println(result);
                writer.flush();
                System.out.println("finished pipeline " + i + " - " + Calendar.getInstance().getTime().toString());
            }
            i++;
        }
        writer.close();
    }

    static boolean runPipeline(List<PluginInfo> resources, int maxExecTime, C99Supercolider s, Workflow p) {

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
        final Workflow pipeline = p;
        final C99Supercolider scheduler = s;
        Future<?> future = null;
        boolean finished = true;

        try {
            // execute pipeline scheduling
            future = executor.submit(new Runnable() {
                @Override
                public void run() {
                    scheduler.schedule(rl, pipeline.getJobs());
                    scheduler.outOfMemory = false;
                }
            });
            // reject all further submissions
            executor.shutdown();

            // wait for scheduling to finish or timeout
            future.get(maxExecTime, TimeUnit.SECONDS);
        } catch (OutOfMemoryError e) {
            // OOME: cancel task and wait for it to finish
            System.out.println("OOME");
            future.cancel(true);
            try {
                future.get();
            } catch (InterruptedException ex) {
                LOGGER.error("[InterruptedException] - " + ex.getMessage());

                ex.printStackTrace();
            } catch (ExecutionException ex) {
                LOGGER.error("[ExecutionException] - " + ex.getMessage());

                ex.printStackTrace();
            }
            finished = false;
            scheduler.outOfMemory = true;
            System.out.println("OOME - finished task");
        } catch (InterruptedException e) {
            System.out.println("job was interrupted");
            scheduler.outOfMemory = true;
        } catch (ExecutionException e) {
            System.out.println("caught exception: " + e.toString());
            System.out.println(Utility.getStackTrace(e));
            finished = false;
            scheduler.outOfMemory = true;
        } catch (TimeoutException e) {
            System.out.println("timeout");
            // force scheduler to stop
            executor.shutdownNow();

            scheduler.execLock.lock();
            finished = false;
            scheduler.outOfMemory = false;
            System.out.println("timeout - task finished");
        } finally {
            System.gc();
        }

        return finished;
    }

    /**
     * *******************************************************
     */
    /**
     * *********************** TODO **************************
     */
    /**
     * *******************************************************
     */
    /**
     * 
     * @param jobs
     * @return
     */
    @Override
    public HashMap<Job, PluginInfo> schedule(List<Job> jobs) {
        final ResourceList resources = new ResourceList();
        final Map<String, PluginInfo> peers = new HashMap<>();
        //Alterei para poder pegar somente a lista de ips fornecidos do usuario nos jobs
        for(PluginInfo plugin: getCloudMap().values()){
            for(Job job : jobs){
                if(job.getIpjob().contains(plugin.getHost().getAddress())){
                    String ip =job.getIpjob().get(job.getIpjob().indexOf(plugin.getHost().getAddress()));
                    plugin.setCostPerHour(custoInstancia(ip));
                    final Resource r = new Resource(plugin.getId(), plugin.getFactoryFrequencyCore(), plugin.getCostPerHour());
                    r.addTask(job);
                    peers.put(plugin.getId(), plugin);
                    resources.resources.add(r);
                }
            }
        }
//        schedule(rs.getCurrentResourceList(), jobs);
        schedule(resources, jobs);

        HashMap<Job, PluginInfo> sched = new HashMap<>();
//        Map<String, PluginInfo> peers = rs.getPeers();

        for (Resource r : best.resources) {
            for (Job j : r.getAllocatedTasks()) {
                sched.put(j, peers.get(r.id));
            }
        }

        return sched;
    }
    /**
     * procura o custo da instancia no ip passado no node do usuario
     * @param ip
     * @return 
     */
    private double custoInstancia(String ip){
        for (final User u : MonitoringService.getZkUsers()) {
            for (final Workflow work : u.getWorkflows()) {
               for (final Instance i : work.getIntancesWorkflow()) {
                   if(i.getIp().equals(ip)){
                       return i.getCostPerHour();
                   }
               }
            }
        }
                        
        return 0d;
    }

    @Override
    public List<PluginTask> relocate(Collection<Pair<Job, PluginTask>> taskPairs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cancelJobEvent(PluginTask task) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void jobDone(PluginTask task) {
        // nothing to do so far
    }

    @Override
    public String getPolicyName() {
        return "C99Supercolider";
    }

}
