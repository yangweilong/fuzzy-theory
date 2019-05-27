/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.planner;

import ai.RandomBiasedAI;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.ahtn.domain.Binding;
import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.HTNMethod;
import ai.ahtn.domain.HTNPhase;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.PredefinedOperators;
import ai.ahtn.domain.Term;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author santi
 */
public class AdversarialBoundedDepthPlannerAlphaBeta {
    public static int DEBUG = -1;

    public static boolean ALPHA_BETA_CUT = true;

    // - if the following variable is set to "false", the search will stop
    // as soon as the is a point where we have the number of desired actions
    // - if the following variable is set to "true", even after the number of
    // desired actions is reached, simulation will continue until the next choice
    // point, in order to get as much information as possible about the effects of the
    // selected actions.
    public static boolean SIMULATE_UNTIL_NEXT_CHOICEPOINT = true;

    MethodDecomposition maxPlanRoot;
    MethodDecomposition minPlanRoot;
    int maxPlayer;
    GameState gs;
    DomainDefinition dd;
    EvaluationFunction f;
    AI playoutAI = null;
    int PLAYOUT_LOOKAHEAD = 100;
    int maxDepth = 3;
    int operatorExecutionTimeout = 1000;
    static int MAX_TREE_DEPTH = 25;
    static int nPlayouts = 0;    // number of leaves in the current search (includes all the trees in the current ID process)
    
    List<AdversarialChoicePoint> stack;
    List<Integer> trail;    // how many bindings to remove, when popping this element of the stack

    List<Binding> bindings;

    int renamingIndex = 1;  // for renaming variables

    boolean lastRunSolvedTheProblem = false;    // if this is true, no need to continue iterative deepening

    // statistics
    public static int n_iterative_deepening_runs = 0;
    public static double max_iterative_deepening_depth = 0;
    public static double average_iterative_deepening_depth = 0;

    public static int n_trees = 0;
    public static double max_tree_leaves = 0;
    public static double last_tree_leaves = 0;
    public static double average_tree_leaves = 0;
    
    public static double max_tree_nodes = 0;
    public static double last_tree_nodes = 0;
    public static double average_tree_nodes = 0;

    public static double max_tree_depth = 0;
    public static double last_tree_depth = 0;
    public static double average_tree_depth = 0;
    
    public static double max_time_depth = 0;
    public static double last_time_depth = 0;
    public static double average_time_depth = 0;
    
    
    public static double reInex=0;
    
    public static void clearStatistics() {
        n_iterative_deepening_runs = 0;
        max_iterative_deepening_depth = 0;
        average_iterative_deepening_depth = 0;
        n_trees = 0;
        max_tree_leaves = 0;
        last_tree_leaves = 0;
        average_tree_leaves = 0;
        max_tree_nodes = 0;
        last_tree_nodes = 0;
        average_tree_nodes = 0;
        max_tree_depth = 0;
        last_tree_depth = 0;
        average_tree_depth = 0;
        max_time_depth = 0;
        last_time_depth = 0;
        average_time_depth = 0;
    }


    // depth is in actions:
    public AdversarialBoundedDepthPlannerAlphaBeta(Term goalPlayerMax, Term goalPlayerMin, int a_maxPlayer, int depth, int playoutLookahead, GameState a_gs, DomainDefinition a_dd, EvaluationFunction a_f, AI a_playoutAI) {
        HTNMethod max = null;
        HTNMethod min = null;
    	maxPlanRoot = new MethodDecomposition(goalPlayerMax,max);
        minPlanRoot = new MethodDecomposition(goalPlayerMin,min);
        minPlanRoot.renameVariables(1);
        renamingIndex = 2;
        maxPlayer = a_maxPlayer;
        gs = a_gs;
        dd = a_dd;
        stack = null;
        maxDepth = depth;
        PLAYOUT_LOOKAHEAD = playoutLookahead;
        f = a_f;
        playoutAI = a_playoutAI;
        
//        System.out.println(a_dd);
    }

    public Pair<MethodDecomposition,MethodDecomposition> getBestPlan() throws Exception {
        return getBestPlan(-1, -1, false);
    }
 
    // if "forceAnswer == true", then even if the search does not finish due to the time limit,
    // the best action found so far is returned
    public Pair<MethodDecomposition,MethodDecomposition> getBestPlan(long timeLimit, int maxPlayouts, boolean forceAnswer) throws Exception 
    {
        if (DEBUG>=1) 
        	System.out.println("AdversarialBoundedDepthPlanner.getBestPlan");

        if (stack==null) //初始化决策点
        {
            if (DEBUG>=1) 
            	System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: first time, initializing stack");
            stack = new ArrayList<>();
            stack.add(0,new AdversarialChoicePoint(maxPlanRoot, minPlanRoot, maxPlanRoot, minPlanRoot, gs,0,-1,-EvaluationFunction.VICTORY,EvaluationFunction.VICTORY,false));
            trail = new ArrayList<>();
            trail.add(0,0);
            bindings = new ArrayList<>();
        }

        last_tree_leaves = 0;
        last_tree_nodes = 0;
        last_tree_depth = 0;
        last_time_depth = 0;
        AdversarialChoicePoint root = stack.get(stack.size()-1);
        //MK
        root.dd = dd;
        //
        boolean timeout = false;

        do
        {
            if ((timeLimit>0&& System.currentTimeMillis()>=timeLimit) || (maxPlayouts>0 && nPlayouts>=maxPlayouts)) // 
            {
                if (forceAnswer) 
                {
                    timeout = true;
                }
                else 
                {
                    return null;
                }
            }

            // statistics:
            int treedepth = stack.size();
            if (treedepth>=last_tree_depth) last_tree_depth = treedepth;

            AdversarialChoicePoint choicePoint = stack.get(0);
            //MK
            choicePoint.dd = dd;
            choicePoint.restoreExecutionState();//恢复原来的状态

            if (DEBUG>=2) {
                System.out.println("\nAdversarialBoundedDepthPlanner.getBestPlan: stack size: " + stack.size() + ", bindings: " + bindings.size() + ", gs.time: " + choicePoint.gs.getTime() + ", operators: " + root.choicePointPlayerMin.getOperatorsBeingExecuted() + ", " + root.choicePointPlayerMax.getOperatorsBeingExecuted());

            }
            if (DEBUG>=3)
            {
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: bindings: " + bindings);
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: trail: " + trail);
                System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: stack:");
                for(int i = 0;i<stack.size();i++) {
                    System.out.println((stack.size()-i) + ": " + stack.get(i));
                }
                maxPlanRoot.printDetailed();
                minPlanRoot.printDetailed();
            }
            
            int bl = bindings.size();
            boolean pop = true;
            while(!timeout && choicePoint.nextExpansion(dd, bindings, renamingIndex, choicePoint)) {
                renamingIndex++;
                AdversarialChoicePoint tmp = simulateUntilNextChoicePoint(bindings, choicePoint); 
                if(tmp!=null)
                	{
                	   tmp.parantPoint = choicePoint;
                	}
                if (tmp==null) 
                {
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: plan execution failed");
                    choicePoint.restoreExecutionState();
//                    n_times_deadend_is_reached++;
                } else if ((tmp.choicePointPlayerMax==null && tmp.choicePointPlayerMin==null)) {
                    last_tree_nodes++;
                    // execution success:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: plan execution success or depth limit reached");
                    if (DEBUG>=3) System.out.println(tmp.gs);
                    if (DEBUG>=3) {
                        maxPlanRoot.printDetailed();
                        minPlanRoot.printDetailed();
                    }
                    // use evaluation function
                    float eval = playout(maxPlayer, tmp.gs);
//                    float eval = f.evaluate(maxPlayer, 1-maxPlayer, tmp.gs);
                    last_tree_leaves++;
                    
                    boolean alphaBetaTest = choicePoint.processEvaluation(eval, choicePoint.maxPlanRoot, choicePoint.minPlanRoot, true);
                    if (!ALPHA_BETA_CUT) alphaBetaTest = false;

                    //MK  记录规划中生成的可行方案
                    AdversarialChoicePoint tmpPoint = new AdversarialChoicePoint(choicePoint.choicePointPlayerMax,choicePoint.choicePointPlayerMin,choicePoint.maxPlanRoot,choicePoint.minPlanRoot,choicePoint.gs,choicePoint.operatorDepth,choicePoint.lastTimeOperatorsIssued);
                    tmpPoint.bestEvaluation =choicePoint.bestEvaluation;
                    tmpPoint.minimaxType =choicePoint.minimaxType;
                    tmpPoint.lastBindings = choicePoint.lastBindings;
                    tmpPoint.parantPoint = choicePoint.parantPoint;
                    root.PlanList.add(tmpPoint);
                    //
                    double time = tmp.gs.getTime() - root.gs.getTime();
                    if (time>last_time_depth) last_time_depth = time;

                    if (DEBUG>=2) 
                    {  
                        System.out.println("---- ---- ---- ----");
                        System.out.println(tmp.gs);
                        System.out.println("Evaluation: " + eval);
                        System.out.println("Bindings: " + bindings.size());
                        System.out.println("Bindings: " + bindings);
                      //  System.out.println("Max plan:");
//                        List<Pair<Integer,List<Term>>> l = maxPlanRoot.convertToOperatorList();
//                        for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
//                        System.out.println("Min plan:");
//                        List<Pair<Integer,List<Term>>> l2 = minPlanRoot.convertToOperatorList();
//                        for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                    choicePoint.restoreExecutionState();
                    // undo the bindings:
                    int n = bindings.size() - bl;
                    for(int i = 0;i<n;i++) bindings.remove(0);
                    if (alphaBetaTest) break;
                } 
                else 
                {
                    last_tree_nodes++;
//                    System.out.println("  next choice point");
                    // next choice point:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.getBestPlan: stack push");
                    stack.add(0,tmp);
                    trail.add(0,bindings.size() - bl); 
//                    System.out.println("trail.add(" + (bindings.size() - bl) + ")");
                    pop = false;
                    break;
                }
            }
            if (pop) 
            {
                if (!timeout && nPlayouts==0) 
                {
                	AdversarialChoicePoint cp = stack.get(0);
                    if ((cp.choicePointPlayerMax!=null && cp.choicePointPlayerMax.getType() == MethodDecomposition.METHOD_METHOD) ||
                        (cp.choicePointPlayerMax==null && cp.choicePointPlayerMin!=null &&cp.choicePointPlayerMin.getType() == MethodDecomposition.METHOD_METHOD)) 
                    {
                    	//进入当前处理环节的条件是
                        //1、 上次获得的choicePoint存在条件中列出的异常
                        //2、 上次得到的choicePoint不能继续分解
                        //3、 上次得到的choicePoint的所有的分解方案都不能成功执行
                    	if(DEBUG==1)
                    	{
                    	  System.out.println("Popping without finding any decomposition:");
                          System.out.println(cp);
                          System.out.println(bindings);
                          System.out.println(cp.gs);
                           //MK 如果出现异常，游戏结束
                           // System.exit(2);
                    	}
                    }
                }
                
                do {
                    pop = false;
                    stackPop();
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.nextPlan: stack pop");

                    // propagate the value up:
                    if (!stack.isEmpty()) 
                    {
                        if (choicePoint.minimaxType==-1) 
                        {
                        	if (DEBUG>=2) System.out.printf("run error\n");
                        } 
                        else 
                        {
                            pop = stack.get(0).processEvaluation(choicePoint.bestEvaluation, choicePoint.bestMaxPlan, choicePoint.bestMinPlan, false);
                            if (!ALPHA_BETA_CUT) pop = false;
                        }
                        if (pop)
                        {
                            choicePoint = stack.get(0);
                        }
                    }
                }while(pop && !stack.isEmpty());
            }
        }while(!stack.isEmpty());
        
        reInex = renamingIndex;
        
        if (DEBUG>=0) System.out.println(last_tree_leaves);
        stack = null;
        if (DEBUG>=1) System.out.println("AdversarialBoundedDepthPlanner.nextPlan: options exhausted for rootPlan");
        if (DEBUG>=1) 
        	System.out.println("best evaluation: " + root.bestEvaluation);
        if (root.bestEvaluation==EvaluationFunction.VICTORY ||
            root.bestEvaluation==-EvaluationFunction.VICTORY) 
        	lastRunSolvedTheProblem = true;
       
        if (root.bestMaxPlan==null &&root.bestMinPlan==null) 
        {
        	//当前的运行条件已经超出了HTN所包含的领域知识
            lastRunSolvedTheProblem = true;
            if(DEBUG==1)
              {
            	System.out.println("No AHTN can be found for situation:");
            	System.out.println(gs);
              }
        }
        if(root.bestMaxPlan!=null)
        {
        	//MK
            root.bestMaxPlan.bestEvaluation = root.bestEvaluation;           
            root.bestMaxPlan.plansaved = root; 
        }
        return new Pair<>(root.bestMaxPlan, root.bestMinPlan);
    }

    public static void GetcaculatePlanList(MethodDecomposition bestMaxPlan)
    {
    	 AdversarialChoicePoint root = bestMaxPlan.plansaved;
    	 if(root.PlanList.size()>1)
         {
         	SortPlan(root.PlanList);
         	List<AdversarialChoicePoint> caculateplan = new LinkedList<>();
             AdversarialChoicePoint tmp = root.PlanList.get(0).parantPoint;
             while(tmp.parantPoint!=null)
             {
             	tmp=tmp.parantPoint;
             }
             Orderplan(tmp,caculateplan);
             for(int i=0;i<caculateplan.size();i++)
             {
            	 AdversarialChoicePoint tmp1 = caculateplan.get(i);
            	 tmp1.maxPlanRootRem.getMethod().IDname = tmp1.maxPlanRootRem.getTerm().getFunctor().toString()+" "+tmp1.maxPlanRootRem.getMethod().getName();
            	 GivenIDname(tmp1.maxPlanRootRem.getMethod().getDecomposition(), tmp1.maxPlanRootRem.getMethod().IDname);
             }
             root.PlanList =caculateplan;
             root.bestMaxPlan.getMethod().IDname = root.bestMaxPlan.getTerm().getFunctor().toString()+" "+root.bestMaxPlan.getMethod().getName();
        	 GivenIDname(root.bestMaxPlan.getMethod().getDecomposition(), root.bestMaxPlan.getMethod().IDname);
         }
    }
    public static void GivenIDname(MethodDecomposition MD,String IDname)
    {
    	if(MD==null)
			return;
		switch(MD.getType())
		{
		     case MethodDecomposition.METHOD_METHOD:
		     {
		    	 MD.getMethod().IDname = IDname+" "+MD.getTerm().getFunctor().toString()+" "+MD.getMethod().getName();
		    	 GivenIDname(MD.getMethod().getDecomposition(),MD.getMethod().IDname);
		    	 break;
		     }
		     case MethodDecomposition.METHOD_PHASE:
		     {
		    	 if(MD.getPhase()!=null)
		    	 {
		    		 String name = IDname+" "+MD.getPhase().getName();
		    		 GivenIDname(MD.getPhase().getDecomposition(),name);
		    	 }
		    	 break;
		     }
		     case MethodDecomposition.METHOD_SEQUENCE:
		     case MethodDecomposition.METHOD_PARALLEL:
		     {
		    	 MethodDecomposition[] sub = MD.getSubparts();
	             for(int i = 0;i < sub.length;i++) 
	             {
	            	 GivenIDname(sub[i],IDname);
	             }
		    	 break;
		     }
		}
    }
    
    public static void Orderplan(AdversarialChoicePoint tmp,List<AdversarialChoicePoint> caculateplan)
    {
    	for(int i=0;i< tmp.subList.size();i++)
    	{
    		AdversarialChoicePoint tmp1=tmp.subList.get(i);
    		if(tmp1==tmp)
    		{
    			System.out.println("error");
    		}
    		if(tmp1.subList.size()==0)
    			caculateplan.add(tmp1);
    		else
    			Orderplan(tmp1,caculateplan);
    	}
    	
    }
    
    public static void SortPlan(List<AdversarialChoicePoint> planlist)
    {
    	List<Pair<AdversarialChoicePoint,AdversarialChoicePoint>> tmplist = new LinkedList<>();
    	for(int i=0;i<planlist.size();i++)
    	{
    		AdversarialChoicePoint tmp = planlist.get(i);
    		AdversarialChoicePoint tmp2 = tmp.parantPoint;
    		if(tmp2==null)
    			continue;
    		Pair<AdversarialChoicePoint,AdversarialChoicePoint> tmpplan = progatebestvalue(tmp,tmp2,tmp);
    		if(tmpplan.m_a!=null)
    		tmplist.add(tmpplan);
    	}
    	List<Pair<AdversarialChoicePoint,AdversarialChoicePoint>> tmplist4 = new LinkedList<>();
    	int num=0;
    	int oldnum=0;
    	for(int i=0;i<tmplist.size();i++)
    	{
    		if(tmplist4.contains(tmplist.get(i)))
    			continue;
    		tmplist4.add(tmplist.get(i));
    		num++;
    		for(int j=i+1;j<tmplist.size();j++)
    		{
    			if(tmplist4.contains(tmplist.get(j)))
        			continue;
    			if(tmplist.get(i).m_a.equals(tmplist.get(j).m_a))
    			{
    				tmplist4.add(tmplist.get(j));
    				num++;
    			}
    		}
    		for(int h=oldnum;h<num;h++)
    		{
    			if(tmplist.get(i).m_a==tmplist4.get(h).m_b)
        		{
        			System.out.println("error");
        		}
    			tmplist.get(i).m_a.subList.add(tmplist4.get(h).m_b);
    		}
    		oldnum = num;
    	}
    	
    	for(int i=0;i<tmplist.size();i++)
    	{
    		if(tmplist.get(i).m_a.minimaxType==0)
    		{
    			Collections.sort(tmplist.get(i).m_a.subList,new AdversarialChoicePointComparator());
    			tmplist.get(i).m_a.bestEvaluation = tmplist.get(i).m_a.subList.get(0).bestEvaluation;
    		}
    		else if(tmplist.get(i).m_a.minimaxType==1)
    		{
    			Collections.sort(tmplist.get(i).m_a.subList,new AdversarialChoicePointComparatorL());
    			tmplist.get(i).m_a.bestEvaluation = tmplist.get(i).m_a.subList.get(0).bestEvaluation;
    		}
    	}
    	
    	List<AdversarialChoicePoint> tmp5 = new LinkedList<>();
    	for(int j=0;j<tmplist.size();j++)
		{
    		if(!tmp5.contains(tmplist.get(j).m_a))
			tmp5.add(tmplist.get(j).m_a);
		}
    	if(tmp5.size()==1&&tmp5.get(0).parantPoint==null)
    		return;
    	SortPlan(tmp5);
  	}

    public static Pair<AdversarialChoicePoint,AdversarialChoicePoint> progatebestvalue(AdversarialChoicePoint tmp,AdversarialChoicePoint tmp2,AdversarialChoicePoint tmp3)
    {
    	if(tmp2==null)
    	{
    		if(tmp==tmp3)
    		{
    			System.out.println("error");
    		}
    		return new Pair<>(tmp,tmp3);
    	}
    		
    	if(tmp.minimaxType==tmp2.minimaxType)
    	{			
    		return progatebestvalue(tmp2,tmp2.parantPoint,tmp3);
    	}
    	else
    	{
    		return new Pair<>(tmp2,tmp3);
    	}
    }
    /*
    The search will end when:
        - the tree is searched to the maximum depth
        - or when System.currentTimeMillis() is larger or equal than timeLimit
        - or when int is larger or equal to maxPlayouts
    */
    public static Pair<MethodDecomposition,MethodDecomposition> getBestPlanIterativeDeepening(Term goalPlayerMax, Term goalPlayerMin, int a_maxPlayer, int timeout, int maxPlayouts, int a_playoutLookahead, GameState a_gs, DomainDefinition a_dd, EvaluationFunction a_f, AI a_playoutAI,boolean flag) throws Exception 
    {
        long start = System.currentTimeMillis();
        long timeLimit = start + timeout;
        if (timeout<=0) timeLimit = 0;
        Pair<MethodDecomposition,MethodDecomposition> bestLastDepth = null;
        double tmp_leaves = 0, tmp_nodes = 0, tmp_depth = 0, tmp_time = 0;
        int nPlayoutsBeforeStartingLastTime = 0, nPlayoutsUSedLastTime = 0;
        nPlayouts = 0;
        for(int depth = 1;;depth++)
        {
            Pair<MethodDecomposition,MethodDecomposition> best = null;
            long currentTime = System.currentTimeMillis();
            if (DEBUG>=1) 
            	System.out.println("Iterative Deepening depth: " + depth + " (total time so far: " + (currentTime - start) + "/" + timeout + ")" + " (total playouts so far: " + nPlayouts + "/" + maxPlayouts + ")");
            AdversarialBoundedDepthPlannerAlphaBeta planner = new AdversarialBoundedDepthPlannerAlphaBeta(goalPlayerMax, goalPlayerMin, a_maxPlayer, depth, a_playoutLookahead, a_gs, a_dd, a_f, a_playoutAI);
            //planner是一个规划器
            nPlayoutsBeforeStartingLastTime = nPlayouts;//迭代次数
            if (depth<=MAX_TREE_DEPTH)//树的深度，25层
            {
                int nPlayoutsleft = maxPlayouts - nPlayouts;
                if (maxPlayouts>0 && nPlayoutsleft>nPlayoutsUSedLastTime) //允许继续迭代
                {
                    if (DEBUG>=1) 
                    	System.out.println("last time we used " + nPlayoutsUSedLastTime + ", and there are " + nPlayoutsleft + " left, trying one more depth!");
                    best = planner.getBestPlan(timeLimit, maxPlayouts, (bestLastDepth==null ? true:false)); //true               
                } 
                else 
                {
                    if (DEBUG>=1) 
                    	System.out.println("last time we used " + nPlayoutsUSedLastTime + ", and there are only " + nPlayoutsleft + " left..., canceling search");
                }
                
            }
            nPlayoutsUSedLastTime = nPlayouts - nPlayoutsBeforeStartingLastTime;
            if (DEBUG>=1) System.out.println("    time taken: " + (System.currentTimeMillis() - currentTime));

            // print best plan:
            if (DEBUG>=1) 
            {
                if (best!=null) {
//                    if (best.m_a!=null) best.m_a.printDetailed();
                    System.out.println("Max plan:");
                    if (best.m_a!=null) {
                        List<Pair<Integer,List<Term>>> l = best.m_a.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                    if (best.m_b!=null) {
                        System.out.println("Min plan:");
                        List<Pair<Integer,List<Term>>> l2 = best.m_b.convertToOperatorList();
                        for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                    }
                }
            }

            if (best!=null) 
            {
                bestLastDepth = best;
                tmp_leaves = last_tree_leaves;
                tmp_nodes = last_tree_nodes;
//                System.out.println("nodes: " + tmp_nodes + ", leaves: " + tmp_leaves);
                tmp_depth = last_tree_depth;
                tmp_time = last_time_depth;
                if (planner.lastRunSolvedTheProblem)
                {
                    // statistics:
                    n_trees++;
                    if (tmp_leaves>max_tree_leaves) max_tree_leaves=tmp_leaves;
                    average_tree_leaves += tmp_leaves;
                    if (tmp_nodes>max_tree_nodes) max_tree_nodes=tmp_nodes;
                    average_tree_nodes += tmp_nodes;
                    average_tree_depth += tmp_depth;
                    if (tmp_depth>=max_tree_depth) max_tree_depth = tmp_depth;
                    average_time_depth += tmp_time;
                    if (tmp_time>=max_time_depth) max_time_depth = tmp_time;

                    n_iterative_deepening_runs++;
                    average_iterative_deepening_depth+=depth;
                    if (depth>max_iterative_deepening_depth) max_iterative_deepening_depth = depth;
                    if(bestLastDepth.m_a!=null&&flag)
                    {
                    	long currenttime = System.currentTimeMillis();
                        GetcaculatePlanList(bestLastDepth.m_a);
                        if(a_maxPlayer==0)
                        {
                        	a_gs.saveplantimeplayer0 = a_gs.saveplantimeplayer0+System.currentTimeMillis()- currenttime;
                        }
                        else
                        {
                     	   a_gs.saveplantimeplayer1 = a_gs.saveplantimeplayer1+System.currentTimeMillis()- currenttime; 
                        }
                    }
                    return bestLastDepth;
                }
            } 
            else
            {
                // statistics:
                n_trees++;
                if (tmp_leaves>max_tree_leaves) max_tree_leaves=tmp_leaves;
                average_tree_leaves += tmp_leaves;
                if (tmp_nodes>max_tree_nodes) max_tree_nodes=tmp_nodes;
                average_tree_nodes += tmp_nodes;
                average_tree_depth += tmp_depth;
                if (tmp_depth>=max_tree_depth) max_tree_depth = tmp_depth;
                average_time_depth += tmp_time;
                if (tmp_time>=max_time_depth) max_time_depth = tmp_time;

                n_iterative_deepening_runs++;
                average_iterative_deepening_depth+=depth-1; // the last one couldn't finish, so we have to add "depth-1"
                if ((depth-1)>max_iterative_deepening_depth) max_iterative_deepening_depth = depth-1;
                if(bestLastDepth.m_a!=null&&flag)
                {
                   long currenttime = System.currentTimeMillis();
                   GetcaculatePlanList(bestLastDepth.m_a);
                   if(a_maxPlayer==0)
                   {
                	   a_gs.saveplantimeplayer0 = a_gs.saveplantimeplayer0+System.currentTimeMillis()- currenttime;
                   }
                   else
                   {
                	   a_gs.saveplantimeplayer1 = a_gs.saveplantimeplayer1+System.currentTimeMillis()- currenttime; 
                   }
                }
                return bestLastDepth;
            }
       }
      //  return bestLastDepth;
    }


    /*
        - Return value "null" means execution failure
        - Return value <null,GameState> means execution success
        - <md,GameState> represents a choice point
    */
    
    int n = 0;
    public AdversarialChoicePoint simulateUntilNextChoicePoint(List<Binding> bindings, AdversarialChoicePoint previous_cp) throws Exception {
        GameState gs = previous_cp.gs;
        GameState gs2 = gs.clone();
        int lastTimeOperatorsIssued = previous_cp.getLastTimeOperatorsIssued();
        int operatorDepth = previous_cp.getOperatorDepth();
//        System.out.println(gs2.getTime() + " - " + lastTimeOperatorsIssued + " - " + operatorDepth);
        while(true) {
//            System.out.println(bindings);
            if (!SIMULATE_UNTIL_NEXT_CHOICEPOINT) {
                if (operatorDepth>=maxDepth && gs2.getTime()>lastTimeOperatorsIssued) {
                    // max depth reached:
                    return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            }
            List<MethodDecomposition> actions1 = new ArrayList<>();
            List<MethodDecomposition> actions2 = new ArrayList<>();
            List<MethodDecomposition> choicePoints1 = new ArrayList<>();
            List<MethodDecomposition> choicePoints2 = new ArrayList<>();
            //MK
            int er1 = previous_cp.maxPlanRoot.executionCycle(gs2, actions1, choicePoints1, previous_cp,bindings);  
            int er2 = previous_cp.minPlanRoot.executionCycle(gs2, actions2, choicePoints2, previous_cp,bindings);
            //
            if (SIMULATE_UNTIL_NEXT_CHOICEPOINT) {
                if (operatorDepth>=maxDepth && gs2.getTime()>lastTimeOperatorsIssued &&
                    (er1==MethodDecomposition.EXECUTION_CHOICE_POINT ||
                     er2==MethodDecomposition.EXECUTION_CHOICE_POINT)) {
                    // max depth reached:
//                    System.out.println(operatorDepth + " >= " + maxDepth);
                    return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            }
            if (er1==MethodDecomposition.EXECUTION_SUCCESS &&
                er2==MethodDecomposition.EXECUTION_SUCCESS) {
                return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
            } else if (er1==MethodDecomposition.EXECUTION_FAILURE ||
                       er2==MethodDecomposition.EXECUTION_FAILURE) {
                if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: execution failure " + er1 + ", " + er2);
                {
                    return null;
                }
            } else if (er1==MethodDecomposition.EXECUTION_CHOICE_POINT ||
                       er2==MethodDecomposition.EXECUTION_CHOICE_POINT) {
                MethodDecomposition cp_md = (choicePoints1.isEmpty() ? null:choicePoints1.get(0));
                if (cp_md==null) cp_md = (choicePoints2.isEmpty() ? null:choicePoints2.get(0));
                if (cp_md.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION||
                    cp_md.getType() == MethodDecomposition.METHOD_sufficient||
                	cp_md.getType() == MethodDecomposition.METHOD_necessary) 
                {
                     AdversarialChoicePoint acp = new AdversarialChoicePoint((choicePoints1.isEmpty() ? null:choicePoints1.get(0)),
                                                                             (choicePoints2.isEmpty() ? null:choicePoints2.get(0)),
                                                                             previous_cp.maxPlanRoot,previous_cp.minPlanRoot,
                                                                             gs2,operatorDepth,lastTimeOperatorsIssued,
                                                                             previous_cp.getAlpha(),previous_cp.getBeta(),false);
//                     System.out.println("testing non-branching condition: " + cp_md);
                     if (!acp.nextExpansion(dd, bindings, renamingIndex, acp)) 
                     {
                         return null;
                     }
                     renamingIndex++;
                } 
                else {
                    return new AdversarialChoicePoint((choicePoints1.isEmpty() ? null:choicePoints1.get(0)),
                                                      (choicePoints2.isEmpty() ? null:choicePoints2.get(0)),
                                                      previous_cp.maxPlanRoot,previous_cp.minPlanRoot,
                                                      gs2,operatorDepth,lastTimeOperatorsIssued,
                                                      previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
            } else if ((er1==MethodDecomposition.EXECUTION_WAITING_FOR_ACTION ||
                        er2==MethodDecomposition.EXECUTION_WAITING_FOR_ACTION) &&
                       er1!=MethodDecomposition.EXECUTION_ACTION_ISSUE &&
                       er2!=MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                boolean gameover = gs2.cycle();
                if (gameover) 
                {
                	return new AdversarialChoicePoint(null,null,previous_cp.maxPlanRoot,previous_cp.minPlanRoot, gs2,operatorDepth,lastTimeOperatorsIssued,previous_cp.getAlpha(),previous_cp.getBeta(),false);
                }
                List<MethodDecomposition> toDelete = null;
                if (previous_cp.maxPlanRoot.getOperatorsBeingExecuted()!=null) 
                {
                    for(MethodDecomposition md:previous_cp.maxPlanRoot.getOperatorsBeingExecuted()) {
                        previous_cp.captureExecutionStateNonRecursive(md);
                        // issue action:
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: continuing executing operator " + md.getUpdatedTerm());
                        if (PredefinedOperators.execute(md, gs2) ||
                            gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) {
//                            if (gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) System.out.println("operator timed out: " + md.getUpdatedTerm());
                            md.setExecutionState(2);
                            if (toDelete==null) toDelete = new ArrayList<>();
                            toDelete.add(md);
                            if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (1).");
                        } else {
                            md.setExecutionState(1);
                        }
                    }
                    if (toDelete!=null) previous_cp.maxPlanRoot.getOperatorsBeingExecuted().removeAll(toDelete);
                }
                if (previous_cp.minPlanRoot.getOperatorsBeingExecuted()!=null) {
                    toDelete = null;
                    for(MethodDecomposition md:previous_cp.minPlanRoot.getOperatorsBeingExecuted()) {
                        previous_cp.captureExecutionStateNonRecursive(md);
                        // issue action:
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: continuing executing operator " + md.getUpdatedTerm());
                        if (PredefinedOperators.execute(md, gs2) ||
                            gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) {
//                            if (gs2.getTime()>md.getUpdatedTermCycle()+operatorExecutionTimeout) System.out.println("operator timed out: " + md.getUpdatedTerm());
                            md.setExecutionState(2);
                            if (toDelete==null) toDelete = new ArrayList<>();
                            toDelete.add(md);
                            if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (2).");
                        } else {
                            md.setExecutionState(1);
                        }
                    }
                    if (toDelete!=null) previous_cp.minPlanRoot.getOperatorsBeingExecuted().removeAll(toDelete);
                }
            }

            if (er1==MethodDecomposition.EXECUTION_ACTION_ISSUE ||
                er2==MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                if (gs2.getTime()>lastTimeOperatorsIssued) {
                    lastTimeOperatorsIssued = gs2.getTime();
                    operatorDepth++;
                }
            }
            
            if (er1==MethodDecomposition.EXECUTION_ACTION_ISSUE) {
                for(MethodDecomposition md:actions1) {
                    previous_cp.captureExecutionStateNonRecursive(md);
                    md.setUpdatedTerm(md.getTerm().clone());
                    md.getUpdatedTerm().applyBindings(bindings);
                    md.setUpdatedTermCycle(gs2.getTime());
                    // issue action:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: executing operator " + md.getUpdatedTerm());
                    md.setOperatorExecutingState(0);
//                    System.out.println(md.getUpdatedTerm() + " <- " + bindings);
                    if (PredefinedOperators.execute(md, gs2)) {
                        md.setExecutionState(2);
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (3).");
                    } else {
                        md.setExecutionState(1);
                        if (previous_cp.maxPlanRoot.getOperatorsBeingExecuted()==null) {
                            previous_cp.maxPlanRoot.setOperatorsBeingExecuted(new ArrayList<>());
                        }
                        previous_cp.maxPlanRoot.getOperatorsBeingExecuted().add(md);
                    }
                }
            }
            if (er2==MethodDecomposition.EXECUTION_ACTION_ISSUE) 
            {
                for(MethodDecomposition md:actions2) 
                {
                    previous_cp.captureExecutionStateNonRecursive(md);
                    md.setUpdatedTerm(md.getTerm().clone());
                    md.getUpdatedTerm().applyBindings(bindings);
                    md.setUpdatedTermCycle(gs2.getTime());
                    // issue action:
                    if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: executing operator " + md.getUpdatedTerm());
                    md.setOperatorExecutingState(0);
                    if (PredefinedOperators.execute(md, gs2)) 
                    {
                        md.setExecutionState(2);
                        if (DEBUG>=2) System.out.println("AdversarialBoundedDepthPlanner.simulateUntilNextChoicePoint: operator complete (4).");
                    } else {
                        md.setExecutionState(1);
                        if (previous_cp.minPlanRoot.getOperatorsBeingExecuted()==null) {
                            previous_cp.minPlanRoot.setOperatorsBeingExecuted(new ArrayList<>());
                        }
                        previous_cp.minPlanRoot.getOperatorsBeingExecuted().add(md);
                    }
                }
            }
        }
    }


    public void stackPop() {
        AdversarialChoicePoint cp = stack.remove(0);
        cp.restoreAfterPop();
        int tmp = trail.remove(0);
        if (DEBUG>=2) System.out.println("StackPop! removing " + tmp + " bindings.");
        for(int i = 0;i<tmp;i++) bindings.remove(0);
        if (!stack.isEmpty()) stack.get(0).restoreExecutionState();
    }
    
   
    public float playout(int player, GameState gs) throws Exception {
        nPlayouts++;
        GameState gs2 = gs;
    	//两个的策略完全采用自己提供的策略
        if (PLAYOUT_LOOKAHEAD>0 && playoutAI!=null) {
            AI ai1 = playoutAI.clone();
            AI ai2 = playoutAI.clone();
            gs2 = gs.clone();
            ai1.reset();
            ai2.reset();
            int timeLimit = gs2.getTime() + PLAYOUT_LOOKAHEAD;
            boolean gameover =false;
                        
            while(!gameover && gs2.getTime()<timeLimit) {
                if (gs2.isComplete()) {
                    gameover = gs2.cycle();
                } else {
                    PlayerAction pa1 = ai1.getAction(player, gs2,false);
                    PlayerAction pa2 = ai2.getAction(1-player, gs2,false);
//                    System.out.println("time: " + gs2.getTime() + " resources: " + gs2.getPlayer(0).getResources() + "/" + gs2.getPlayer(1).getResources());
//                    System.out.println("  pa1: " + pa1);
//                    System.out.println("  pa2: " + pa2);
                    gs2.issue(pa1);
                    gs2.issue(pa2);
                }
            }        
        } 
        float e = f.evaluate(player, 1-player, gs2);
//        if (DEBUG>=1) System.out.println("  done: " + e);
        return e;
    }    

}
