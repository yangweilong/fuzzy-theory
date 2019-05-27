/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import util.Pair;
import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.HTNMethod;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.PredefinedOperators;
import ai.ahtn.domain.Term;
import ai.ahtn.planner.AdversarialBoundedDepthPlannerAlphaBeta;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.evaluation.EvaluationFunction;

/**
 *
 * @author santi
 * 
 */
public class AHTNAI extends AIWithComputationBudget {
    public static int DEBUG = 0;
    
    String domainFileName = null;
    DomainDefinition dd = null;

    EvaluationFunction ef = null;
    AI playoutAI = null;
    public int PLAYOUT_LOOKAHEAD = 100;
        
    List<MethodDecomposition> actionsBeingExecuted = null;
    
    public AHTNAI(String a_domainFileName, int available_time, int max_playouts, int playoutLookahead, EvaluationFunction a_ef, AI a_playoutAI) throws Exception {
    	super(available_time, max_playouts);
       domainFileName = a_domainFileName;
       dd = DomainDefinition.fromLispFile(domainFileName);
       PLAYOUT_LOOKAHEAD = playoutLookahead;
       ef = a_ef;
       playoutAI = a_playoutAI;
       
       actionsBeingExecuted = new LinkedList<>();
    }
    
    
    public void reset() {
       actionsBeingExecuted = new LinkedList<>();       
       AdversarialBoundedDepthPlannerAlphaBeta.clearStatistics();
    }

    //MK
    public void ProccessFailedAction()
    {
    	return;
    }
    public void ProccessSuccessAction(UnitAction action,GameState gs,int player)
    {
    	
    }
    //
    public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception {
    	Term goal1 = Term.fromString("(destroy-player "+player+" "+(1-player)+" "+0+")");
        Term goal2 = Term.fromString("(destroy-player "+(1-player)+" "+player+" "+0+")");
        PlayerAction pa = new PlayerAction();  
        if(gs.canExecuteAnyAction(player))
    	{
    		if(player==0)
               gs.AI1plannum = gs.AI1plannum+1;
             else
               gs.AI2plannum = gs.AI2plannum+1;
    	}
        
        if(partiallyObservable)
        {
        	PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
        	UpdateGameStateNum(POgs1,gs);
        	PlayerAction tm= GetActionbyState(player, POgs1,goal1,goal2,pa);
        	UpdateGameStateNum(gs,POgs1);
        	
        	return tm;
        }
        else
        {
        	return GetActionbyState(player, gs,goal1, goal2,pa);
        }
        
    }
    
//    public PlayerAction GetActionbyProState(int player,GameState gs,Term goal1,Term goal2,PlayerAction pa)throws Exception
//    {
//    	
//    }
    
    
    
    public PlayerAction GetActionbyState(int player, GameState gs,Term goal1,Term goal2,PlayerAction pa) throws Exception
    {
    	if (gs.canExecuteAnyAction(player)) //将选手的动作返回给选手
        {
            Pair<MethodDecomposition,MethodDecomposition> plan = AdversarialBoundedDepthPlannerAlphaBeta.getBestPlanIterativeDeepening(goal1, goal2, player, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, gs, dd, ef, playoutAI,false);
            //plan即双方的任务层次网。然后判断其中哪个任务可以执行，是策略的体现
            //MK
            if(player==0)
            {
            	gs.max_tree_nodesplayer0 = gs.max_tree_nodesplayer0+AdversarialBoundedDepthPlannerAlphaBeta.average_tree_nodes;
            	gs.average_tree_depth0 = gs.average_tree_depth0 + AdversarialBoundedDepthPlannerAlphaBeta.average_tree_depth;
            	gs.renamingIndex0 = gs.renamingIndex0+AdversarialBoundedDepthPlannerAlphaBeta.reInex;
            }
            else
            {
            	gs.max_tree_nodesplayer1 = gs.max_tree_nodesplayer1+AdversarialBoundedDepthPlannerAlphaBeta.average_tree_nodes;
            	gs.average_tree_depth1 = gs.average_tree_depth1 + AdversarialBoundedDepthPlannerAlphaBeta.average_tree_depth;
            	gs.renamingIndex1 = gs.renamingIndex1+AdversarialBoundedDepthPlannerAlphaBeta.reInex;
            }
            //记录
            
            if (plan.m_a!=null) 
            {
                MethodDecomposition toExecute = plan.m_a;
                List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>());//(存在可执行动作，l=转换后的operator，否则，l=new)
//                if(l.size()>1)
//                	 System.out.println("Detailed Max plan:");;
//                System.out.println(l+"\n");;
                if (DEBUG>=1) //DEBUG=0
                {
                    List<Pair<Integer,List<Term>>> l2 = (plan.m_b!=null ? plan.m_b.convertToOperatorList():new LinkedList<>());        
                    System.out.println("---- ---- ---- ----");
                    System.out.println(gs);
                    System.out.println("Max plan:");
                    for(Pair<Integer, List<Term>> a:l) System.out.println("  " + a.m_a + ": " + a.m_b);
                    System.out.println("Min plan:");
                    for(Pair<Integer, List<Term>> a:l2) System.out.println("  " + a.m_a + ": " + a.m_b);
                }
                if (DEBUG>=2) 
                {
                    System.out.println("Detailed Max plan:");
                    plan.m_a.printDetailed();                    
                }
                
                actionsBeingExecuted.clear();
                while(!l.isEmpty()) 
                {
                    Pair<Integer,List<Term>> tmp = l.remove(0);//去掉一个节点
                    if (tmp.m_a!=gs.getTime()) 
                    	break;
                    List<Term> actions = tmp.m_b;//所有执行的动作
                    for(Term action:actions) 
                    {
                        MethodDecomposition md = new MethodDecomposition(action);
                        actionsBeingExecuted.add(md);
                    }
                }
            }

            if (DEBUG>=1) 
            {
                System.out.println("Actions being executed:");
                for(MethodDecomposition md:actionsBeingExecuted) {
                    System.out.println("    " + md.getTerm());
                }
            }

            List<MethodDecomposition> toDelete = new LinkedList<>();
            System.out.print(pa);
            for(MethodDecomposition md:actionsBeingExecuted)
            {
                if (PredefinedOperators.execute(md, gs, pa)) //将策略转化为动作
                {
                	toDelete.add(md);
                	System.out.print(pa);
                }
                	
                for(Pair<Unit,UnitAction> ua:pa.getActions()) 
                {
                    if (gs.getUnit(ua.m_a.getID())==null) 
                    {
                        pa.removeUnitAction(ua.m_a, ua.m_b);
                    }
                }
            }
            actionsBeingExecuted.removeAll(toDelete);
            System.out.print(pa);
//            System.out.println(actionsBeingExecuted+"\n");
//            System.out.println(toDelete+"\n");
            
            if (DEBUG>=1) {
                System.out.println("Result in the following unit actions:");
                System.out.println("    " + pa);
            }
            pa.fillWithNones(gs, player, 10);
            return pa;
        } 
    	else 
    	{
            return new PlayerAction();
        }
    }
    public AI clone() {
        try {
            return new AHTNAI(domainFileName, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, ef, playoutAI);
        }catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String toString() {
        return "AHTNAI:";   //(" + domainFileName + "," + MAX_TIME + ")
    }
    
    public String statisticsString() {
        return "Max depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_iterative_deepening_depth +
               ", Average depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_iterative_deepening_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_iterative_deepening_runs) +
               ", Max tree leaves: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_leaves + 
               ", Average tree leaves: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_leaves/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max tree nodes: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_nodes + 
               ", Average tree nodes: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_nodes/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max tree depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_tree_depth + 
               ", Average tree depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_tree_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_trees) +
               ", Max time depth: " + AdversarialBoundedDepthPlannerAlphaBeta.max_time_depth + 
               ", Average time depth: " + (AdversarialBoundedDepthPlannerAlphaBeta.average_time_depth/AdversarialBoundedDepthPlannerAlphaBeta.n_trees);
    }
    
    
}
