package tests;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.core.PseudoContinuingAI;
import ai.*;
import ai.portfolio.PortfolioAI;
import ai.abstraction.HeavyRush;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.ahtn.AHTNAI;
import ai.ahtn.AHTNRAI;
import ai.ahtn.AHTNTESTAI;
import ai.ahtn.EnhanceAHTNAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.DownsamplingUCT;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTUnitActions;
import ai.minimax.ABCD.IDABCD;
import ai.minimax.ABCD.ABCD;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.IDRTMinimaxRandomized;
import ai.montecarlo.*;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import rts.PhysicalGameState;
import rts.units.UnitTypeTable;



public class CompareAllAIsObservableTest {

	public static void main(String args[]) throws Exception 
	{
		boolean CONTINUING = true;
        int TIME =100;
        int MAX_ACTIONS = 100;
        int MAX_PLAYOUTS = 5;
        int PLAYOUT_TIME =100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
//        String a_domainFileName = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level.lisp" ;
//        String a_domainFileNameAI = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level-phase.lisp" ;
//        String a_domainFileNameFP = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio.lisp" ;
//        String a_domainFileNameFPAI = "E:\\MK\\myProject\\ahtn2\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio-phase.lisp" ;
//        String a_domainFileNameAI_low = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio.lisp" ;
//        String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-flexible-single-target-portfolio.lisp" ;
//       String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-lowest-level.lisp"  ;
//        String a_domainFileNameAI_enemy = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-low-level.lisp"  ;
//        String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-low-level.lisp"  ;
        
//        String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-portfolio" ;
        String a_domainFileNameAI = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-flexible-portfolio-phase.lisp" ;
        String a_domainFileNameAI_enemy = "E:\\project\\YANG\\ahtntest\\ahtn\\microrts-ahtn-definition-low-level-phase.lisp";
        
        String a_domainFileNameAI_flexible = "E:\\project\\YANG\\ahtntest\\ahtn\\ahtn\\microrts-ahtn-definition-flexible-portfolio.lisp" ;
//        String a_domainFileNameAI_single_target = "E:\\project\\YANG\\ahtntest\\ahtn\\ahtn\\microrts-ahtn-definition-flexible-single-target-portfolio.lisp" ;
        String a_domainFileNameAI_portfolio = "E:\\project\\YANG\\ahtntest\\ahtn\\ahtn\\microrts-ahtn-definition-portfolio.lisp" ;
        String a_domainFileNameAI_low = "E:\\project\\YANG\\ahtntest\\ahtn\\ahtn\\microrts-ahtn-definition-low-level.lisp" ;
        String a_domainFileNameAI_lowest = "E:\\project\\YANG\\ahtntest\\ahtn\\ahtn\\microrts-ahtn-definition-lowest-level.lisp" ;
        
        
//        String a_domainFileNameAI_enemy = "E:\\project\\YANG\\ahtntest\\ahtn\\new\\microrts-ahtn-definition-lowest-level.lisp"  ;
        EvaluationFunction ef = new SimpleSqrtEvaluationFunction2();
        List<AI> bots = new LinkedList<>();
        UnitTypeTable utt = new UnitTypeTable();
        AI playout_policy =new RandomBiasedAI();
         
//       bots.add(new RandomBiasedAI());
//        bots.add(new LightRush(utt, new AStarPathFinding()));
//        bots.add(new HeavyRush(utt, new AStarPathFinding()));
//        bots.add(new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, playout_policy, new SimpleSqrtEvaluationFunction2()));
//        bots.add(new AHTNRAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
// //       bots.add(new EnhanceAHTNAI(a_domainFileNameFPAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//        bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//        bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));   
//        bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//        bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//       bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
 //       bots.add(new AHTNAI(a_domainFileNameAI_enemy,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
 //      bots.add(new AHTNTESTAI(a_domainFileNameAI,a_domainFileNameAI_enemy,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy)); 
 //       bots.add(new AHTNTESTAI(a_domainFileNameAI_enemy,a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
      
        
        
        bots.add(new AHTNAI(a_domainFileNameAI,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
//      bots.add(new AHTNAI(a_domainFileNameAI_single_target,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        bots.add(new AHTNAI(a_domainFileNameAI_enemy,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        bots.add(new AHTNAI(a_domainFileNameAI_lowest,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
        
        if (CONTINUING) {
        	// Find out which of the bots can be used in "continuing" mode:
        	List<AI> bots2 = new LinkedList<>();
        	for(AI bot:bots) {
        		if (bot instanceof AIWithComputationBudget) {
        			if (bot instanceof InterruptibleAIWithComputationBudget) {
        				bots2.add(new ContinuingAI((InterruptibleAIWithComputationBudget)bot));
        			} else {
        				bots2.add(new PseudoContinuingAI((AIWithComputationBudget)bot));        				
        			}
        		} else {
        			bots2.add(bot);
        		}
        	}
        	bots = bots2;
        }
       
        int map = 8;
        boolean flag = true;
        for(int i=1;i<2;++i)
        {
        	flag =true;
        	int turntime = i*50;
        	String StrategyName ="FifthStrategy";
        	String outfilename =bots.size()+"_"+ turntime+"_"+StrategyName+"_"+TIME+"_"+MAX_PLAYOUTS+"_"+PLAYOUT_TIME+"_"+"results.txt";
        	if(map==8||flag)
        	{
        		String mapname = "basesWorkers8x8";
                PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
                PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                // Separate the matchs by map:
                 List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
                 maps.clear();
                 maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                 Experimenter.runExperiments(bots, maps, utt, turntime, 3000, 1000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
        	}
            flag = true;
            if(map==12||flag)
            {
            	String mapname = "basesWorkers12x12";
            	PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
            	PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                // Separate the matchs by map:
            	List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperiments(bots, maps, utt, turntime, 3000, 1000, false, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }
            flag = true;
            if(map==16||flag) 
            {
            	String mapname = "basesWorkers16x16";
            	PrintStream  out = new PrintStream(new File(mapname+"_"+outfilename));
            	PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                  // Separate the matchs by map:
            	List<PhysicalGameState>  maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperiments(bots, maps, utt, turntime, 3000, 1000, false, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }           
        }
      
	}
}
