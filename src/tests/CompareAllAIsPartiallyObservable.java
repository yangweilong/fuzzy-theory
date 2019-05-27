/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAIWithComputationBudget;
import ai.core.PseudoContinuingAI;
import ai.portfolio.PortfolioAI;
import ai.*;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.evaluation.SimpleSqrtEvaluationFunction3;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.mcts.uct.DownsamplingUCT;
import ai.mcts.uct.UCT;
import ai.mcts.uct.UCTUnitActions;
import ai.minimax.ABCD.IDABCD;
import ai.minimax.RTMiniMax.IDRTMinimax;
import ai.minimax.RTMiniMax.IDRTMinimaxRandomized;
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
public class CompareAllAIsPartiallyObservable {
    
    public static void main(String args[]) throws Exception 
    {
    	boolean CONTINUING = true;
        int TIME = 100;
        int MAX_ACTIONS = 100;
        int MAX_PLAYOUTS = -1;
        int PLAYOUT_TIME = 100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
        List<AI> bots = new LinkedList<AI>();
        UnitTypeTable utt = new UnitTypeTable();

 //       bots.add(new RandomAI());
 //       bots.add(new RandomBiasedAI());
//        bots.add(new LightRush(utt, new BFSPathFinding()));
//        bots.add(new RangedRush(utt, new BFSPathFinding()));
//        bots.add(new WorkerRush(utt, new BFSPathFinding()));
//        bots.add(new PortfolioAI(new AI[]{new WorkerRush(utt, new BFSPathFinding()),
//                                          new LightRush(utt, new BFSPathFinding()),
//                                          new RangedRush(utt, new BFSPathFinding()),
//                                          new RandomBiasedAI()}, 
//                                 new boolean[]{true,true,true,false}, 
//                                 TIME, MAX_PLAYOUTS, PLAYOUT_TIME*4, new SimpleSqrtEvaluationFunction3()));
//      
//        bots.add(new IDRTMinimax(TIME, new SimpleSqrtEvaluationFunction3()));
//        bots.add(new IDRTMinimaxRandomized(TIME, RANDOMIZED_AB_REPEATS, new SimpleSqrtEvaluationFunction3()));
        bots.add(new IDABCD(TIME, MAX_PLAYOUTS, new LightRush(utt, new GreedyPathFinding()), PLAYOUT_TIME, new SimpleSqrtEvaluationFunction3(), false));
        bots.add(new IDABCD(TIME, MAX_PLAYOUTS, new LightRush(utt, new GreedyPathFinding()), PLAYOUT_TIME, new SimpleSqrtEvaluationFunction3(), false));
//
//        bots.add(new MonteCarlo(TIME, PLAYOUT_TIME, MAX_PLAYOUTS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new MonteCarlo(TIME, PLAYOUT_TIME, MAX_PLAYOUTS, MAX_ACTIONS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        // by setting "MAX_DEPTH = 1" in the next two bots, this effectively makes them Monte Carlo search, instead of Monte Carlo Tree Search
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 1, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 1, 1.00f, 0.0f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));

        bots.add(new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new DownsamplingUCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_ACTIONS, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new UCTUnitActions(TIME, PLAYOUT_TIME, MAX_DEPTH*10, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 1.00f, 0.0f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));

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
        
//        PrintStream out = new PrintStream(new File("results-PO.txt"));
        int map = 8;
        boolean flag = true;
        for(int i=1;i<2;++i)
        {
        	flag =false;
        	int turntime = i*10;
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
                 Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 3000, 1000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
        	}
            //flag = true;
            if(map==12||flag)
            {
            	String mapname = "basesWorkers12x12";
            	PrintStream out = new PrintStream(new File(mapname+"_"+outfilename));
            	PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                // Separate the matchs by map:
            	List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 3000, 1000, true, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }
            flag =false;
            if(map==16||flag) 
            {
            	String mapname = "basesWorkers16x16";
            	PrintStream  out = new PrintStream(new File(mapname+"_"+outfilename));
            	PrintStream out1 = new PrintStream(new File(mapname+"_"+"Repair"+outfilename));
                  // Separate the matchs by map:
            	List<PhysicalGameState>  maps = new LinkedList<PhysicalGameState>();        
                maps.clear();
                maps.add(PhysicalGameState.load("maps/"+mapname+".xml",utt));
                Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, turntime, 3000, 1000, false, out,out1);        //10 表示 每轮比赛10次， 3000 表示最大的frame数， 300表示 当前时间与上次动作生成时间的差值的限制
            }           
        // Separate the matchs by map:
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        

//        maps.clear();
//        maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml",utt));
//        Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, 10, 3000, 300, true, out);
//      
//        maps.clear();
//        maps.add(PhysicalGameState.load("maps/basesWorkers16x16.xml",utt));
//        Experimenter.runExperimentsPartiallyObservable(bots, maps, utt, 10, 3000, 300, true, out);
    }
    }
}
