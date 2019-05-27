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
import ai.ahtn.planner.AdversarialPlan;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.evaluation.EvaluationFunction;

/**
 *
 * @author YANG
 * 
 */
public class AHTNTESTAI extends AIWithComputationBudget {
    public static int DEBUG = 0;
    
    String domainFileName = null;
    String domainFileName_enemy = null;
    DomainDefinition dd = null;
    DomainDefinition ddenemy = null;
    EvaluationFunction ef = null;
    AI playoutAI = null;
    public int PLAYOUT_LOOKAHEAD = 100;
        
    List<MethodDecomposition> actionsBeingExecuted = null;
    
    public AHTNTESTAI(String a_domainFileName, String a_domainFileName_enemy, int available_time, int max_playouts, int playoutLookahead, EvaluationFunction a_ef, AI a_playoutAI) throws Exception 
    {
       super(available_time, max_playouts);
       domainFileName = a_domainFileName;
       domainFileName_enemy = a_domainFileName_enemy;
       dd = DomainDefinition.fromLispFile(domainFileName);
       ddenemy = DomainDefinition.fromLispFile(domainFileName_enemy);
       PLAYOUT_LOOKAHEAD = playoutLookahead;
       ef = a_ef;
       playoutAI = a_playoutAI;
       
       actionsBeingExecuted = new LinkedList<>();
    }
        
    public void reset() 
    {
       actionsBeingExecuted = new LinkedList<>();       
       AdversarialBoundedDepthPlannerAlphaBeta.clearStatistics();
    }

    public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception
    {
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
   
    
    public PlayerAction GetActionbyState(int player, GameState gs,Term goal1,Term goal2,PlayerAction pa) throws Exception
    {
    	if (gs.canExecuteAnyAction(player)) //将选手的动作返回给选手
        {
//            Pair<MethodDecomposition,MethodDecomposition> plan = AdversarialBoundedDepthPlannerAlphaBeta.getBestPlanIterativeDeepening(goal1, goal2, player, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, gs, dd, ef, playoutAI,false);
            //plan即双方的任务层次网。然后判断其中哪个任务可以执行，是策略的体现
            Pair<MethodDecomposition,MethodDecomposition> plan =AdversarialPlan.getBestPlanIterativeDeepening(goal1, goal2, player, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, gs, dd, ddenemy,ef, playoutAI,false);
            
            if (plan.m_a!=null) 
            {
                MethodDecomposition toExecute = plan.m_a;
                System.out.print(toExecute);

                List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>());//(存在可执行动作，l=转换后的operator，否则，l=new)
                System.out.println(l);
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
            }//将所有动作存入actionsBeingExecuted中
            if(actionsBeingExecuted!=null)
            {
	            System.out.print(pa);
	            System.out.print(actionsBeingExecuted);
            }
            
            List<MethodDecomposition> toDelete = new LinkedList<>();
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
            
            pa.fillWithNones(gs, player, 10);
            return pa;
        } 
    	else 
    	{
            return new PlayerAction();
        }
    }
    
    public AI clone()
    {
        try {
            return new AHTNTESTAI(domainFileName,domainFileName_enemy, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, ef, playoutAI);
        }catch(Exception e) 
        {
            e.printStackTrace();
            return null;
        }
    }
    
    public String toString() 
    {
        return "AHTNTESTAI:";   //(" + domainFileName + "," + MAX_TIME + ")
    }
    
    public String statisticsString() 
    {
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

	@Override
	public void ProccessSuccessAction(UnitAction action, GameState gs, int player) {
		// TODO Auto-generated method stub
		
	}
    
    
}
