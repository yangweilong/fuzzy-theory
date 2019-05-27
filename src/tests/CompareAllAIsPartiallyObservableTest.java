/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.ContinuingAIwithoutsensor;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.core.PseudoContinuingAI;
import ai.portfolio.PortfolioAI;
import ai.*;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.ahtn.AHTNAI;
import ai.ahtn.EnhanceAHTNAI;
import ai.ahtn.POAHTNRAI;
import ai.ahtn.POAHTNAI;
import ai.ahtn.AHTNRAI;
//import ai.ahtn.EnhanceAHTNAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.evaluation.SimpleSqrtEvaluationFunction2_po;
import ai.evaluation.SimpleSqrtEvaluationFunction_po;
import ai.evaluation.SimpleSqrtEvaluationFunction_po_only;
import ai.evaluation.SimpleSqrtEvaluationFunction2_po_info;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.DownsamplingUCT;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTwithoutpo;
import ai.mcts.uct.UCTUnitActions;
import ai.minimax.ABCD.IDABCD;
import ai.minimax.ABCD.IDABCDwithoutsensor;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.IDRTMinimaxRandomized;
import ai.minimax.RTMiniMax.RTMinimax;
import ai.montecarlo.*;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import rts.PhysicalGameState;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class CompareAllAIsPartiallyObservableTest {
    
    public static void main(String args[]) throws Exception 
    {
    	boolean CONTINUING = true;
        int TIME = 200;
        int MAX_ACTIONS = 100;
        int MAX_PLAYOUTS = 5;
        int PLAYOUT_TIME = 100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
//      String a_domainFileName = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level.lisp" ;
//      String a_domainFileName_po = "E:\\project\\YANG\\ahtntest\\microrts-ahtn-definition-low-level-phase-sensor.lisp" ;
        String a_domainFileNameAI_low = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level-phase.lisp" ;
        String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio-phase.lisp" ;
        String a_domainFileNameAI_low_po = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level-phase-sensor.lisp" ;
//      String a_domainFileNameP = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-portfolio.lisp" ;
//      String a_domainFileNamePAI = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-portfolio-phase.lisp" ;
//      String a_domainFileNameFP = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio.lisp" ;
//      String a_domainFileNameFPAI = "E:\\project\\YANG\\ahtn\\microrts-ahtn-definition-flexible-portfolio-phase.lisp" ;
        EvaluationFunction ef = new SimpleSqrtEvaluationFunction2();
        EvaluationFunction ef2 = new SimpleSqrtEvaluationFunction2_po();
        List<AI> bots = new LinkedList<>();
        UnitTypeTable utt = new UnitTypeTable();
//      AI playout_policytest = new WorkerRush(utt, new BFSPathFinding());
        AI playout_policy =new RandomAI();
//      bots.add(new RandomAI());
//      bots.add(new LightRush(utt, new AStarPathFinding()));
//      bots.add(new HeavyRush(utt, new AStarPathFinding()));
//      bots.add(new UCTwithoutpo(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, playout_policy,ef));
//      bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new POAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new POAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
 //     bots.add(new AHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
 //     bots.add(new EnhanceAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
 //     bots.add(new AHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
//      bots.add(new EnhanceAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
//      bots.add(new EnhanceAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
 //     bots.add(new POAHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
 //     bots.add(new AHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
 //       bots.add(new EnhanceAHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//        bots.add(new AHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));
//        bots.add(new EnhanceAHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef2,playout_policy));//估值与believe state，实验1.3
        
        
//        
//        bots.add(new RandomAI());
//        bots.add(new RandomBiasedAIwithSensor());//WITHOUT
        
        
 //     bots.add(new LightRush(utt, new BFSPathFinding()));
//      bots.add(new UCTwithoutpo(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, playout_policy, new SimpleSqrtEvaluationFunction2()));
//	    bots.add(new POAHTNRAI(a_domainFileNameAI_low_po,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction2(),playout_policy));           
//	    bots.add(new POAHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction2(),playout_policy));
       
//        bots.add(new POAHTNRAI(a_domainFileNameAI_low_po,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction2(),playout_policy));
//        bots.add(new POAHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction2(),playout_policy));
//        bots.add(new POAHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction2_po(),playout_policy));
         
//      bots.add(new RandomAI());
  //    bots.add(new RandomAIwithoutsensor());
//      bots.add(new RandomAIwithoutsensor());
  //    bots.add(new RandomAIwithoutsensor());
 //     bots.add(new RandomBiasedAI());

//      bots.add(new RandomBiasedAIwithSensor());
//      bots.add(new RandomBiasedAIwithSensor());
//      bots.add(new RandomBiasedAI());
       
 //     bots.add(new HeavyRush(utt, new BFSPathFinding()));
 //     bots.add(new WorkerRush(utt, new BFSPathFinding()));
 //     bots.add(new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, playout_policy, new SimpleSqrtEvaluationFunction2()));
       
       
 //     bots.add(new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, playout_policy, new SimpleSqrtEvaluationFunction2()));
 //     bots.add(new IDABCD(TIME, MAX_PLAYOUTS, playout_policy, PLAYOUT_TIME, new SimpleSqrtEvaluationFunction2(), false));
 //     bots.add(new IDABCDwithoutsensor(TIME, MAX_PLAYOUTS, playout_policy, PLAYOUT_TIME, new SimpleSqrtEvaluationFunction2(), false));
 //     bots.add(new IDABCD(TIME, MAX_PLAYOUTS, playout_policy, PLAYOUT_TIME, new SimpleSqrtEvaluationFunction2(), false));
        
 //     bots.add(new IDABCDwithoutsensor(TIME, MAX_PLAYOUTS, playout_policy, PLAYOUT_TIME, new SimpleSqrtEvaluationFunction2(), false));
 //     bots.add(new POAHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction(),playout_policy));   
 //     bots.add(new POAHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction(),playout_policy));
  //    bots.add(new POAHTNRAI(a_domainFileNameAI_low_po,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,new SimpleSqrtEvaluationFunction(),playout_policy));   
 //     bots.add(new POAHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new POAHTNRAI(a_domainFileNamePAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));   
        
//      bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
       
//      bots.add(new POAHTNRAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        bots.add(new POAHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        bots.add(new POAHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));//sensor实验
        
//      bots.add(new POAHTNRAI(a_domainFileNameAI_po,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//
//      bots.add(new AHTNAI(a_domainFileName,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new AHTNAI(a_domainFileNameP,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new AHTNAI(a_domainFileNameFP,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy)); 
      
 //    int AI_i=0;
     if (CONTINUING) 
     {
        	// Find out which of the bots can be used in "continuing" mode:
        	List<AI> bots2 = new LinkedList<>();
        	for(AI bot:bots) 
        	{
        		if (bot instanceof AIWithComputationBudget)
        		{
        			if (bot instanceof InterruptibleAIWithComputationBudget) 
        			{
//        				if(AI_i==0)
//        					bots2.add(new ContinuingAIwithoutsensor((InterruptibleAIWithComputationBudget)bot));//第一个是有感知动作
//        				else
        					bots2.add(new ContinuingAI((InterruptibleAIWithComputationBudget)bot));//第二个没有感知动作
        			}
        			else
        			{
        				bots2.add(new PseudoContinuingAI((AIWithComputationBudget)bot));        				
        			}
        		}
        		else 
        		{
        			bots2.add(bot);
        		}
 //       		AI_i++;
        	}
        	bots = bots2;
        }         
        
        int map = 16;
        boolean flag = false;
        for(int i=1;i<2;++i)
        {  
        	int turntime = i*20;
        	String StrategyName ="FifthStrategy";
        	String outfilename =bots.size()+"_"+ turntime+"_"+StrategyName+"_"+TIME+"_"+MAX_PLAYOUTS+"_"+PLAYOUT_TIME+"_"+"PO_results.txt";
        	if(map==8||flag)
        	{
        		String mapname = "basesWorkers8x8";
                PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
                PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                // Separate the matchs by map:
                 List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
                 maps.clear();
                 maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                 Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 3000, 1000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
        	}
            flag = false;
            if(map==12||flag)
            {
            	String mapname = "basesWorkers12x12";
            	PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
            	PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                // Separate the matchs by map:
            	List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 5000, 3000, false, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }
            
            if(map==16||flag) 
            {
            	String mapname = "basesWorkers16x16";
            	PrintStream  out = new PrintStream(new File(mapname+"_"+outfilename));
            	 PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                  // Separate the matchs by map:
            	List<PhysicalGameState>  maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 10000, 3000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }
         
           flag=false;   
           if(map==24||flag) 
           {
        	   String mapname = "basesWorkers24x24";
        	   PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
        	   PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
               // Separate the matchs by map:
        	   List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
               maps.clear();
               maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
               Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 30000, 1000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
           }
           
        }
    }
}
