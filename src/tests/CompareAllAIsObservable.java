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
import ai.*;
import ai.portfolio.PortfolioAI;
import ai.abstraction.LightRush;
import ai.abstraction.RangedRush;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.abstraction.pathfinding.GreedyPathFinding;
import ai.ahtn.AHTNAI;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.SimpleSqrtEvaluationFunction2;
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
public class CompareAllAIsObservable {
    
    public static void main(String args[]) throws Exception 
    {
    	boolean CONTINUING = true;
        int TIME = 100;
        int MAX_ACTIONS = 100;
        int MAX_PLAYOUTS = -1;
        int PLAYOUT_TIME = 100;
        int MAX_DEPTH = 10;
        int RANDOMIZED_AB_REPEATS = 10;
        
        List<AI> bots = new LinkedList<>();
        UnitTypeTable utt = new UnitTypeTable();
        String a_domainFileNameAI_low = "E:\\project\\poahtn\\data\\ahtn\\microrts-ahtn-definition-low-level.lisp" ;
        AI playout_policy =new RandomAI();
        EvaluationFunction ef = new SimpleSqrtEvaluationFunction2();
        
        bots.add(new RandomAI());
//        bots.add(new RandomBiasedAI());
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
//        bots.add(new IDABCD(TIME, MAX_PLAYOUTS, new LightRush(utt, new GreedyPathFinding()), PLAYOUT_TIME, new SimpleSqrtEvaluationFunction3(), false));

 //       bots.add(new MonteCarlo(TIME, PLAYOUT_TIME, MAX_PLAYOUTS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new MonteCarlo(TIME, PLAYOUT_TIME, MAX_PLAYOUTS, MAX_ACTIONS, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        // by setting "MAX_DEPTH = 1" in the next two bots, this effectively makes them Monte Carlo search, instead of Monte Carlo Tree Search
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 1, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, 1, 1.00f, 0.0f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//
//        bots.add(new UCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new DownsamplingUCT(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_ACTIONS, MAX_DEPTH, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new UCTUnitActions(TIME, PLAYOUT_TIME, MAX_DEPTH*10, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 0.33f, 0.0f, 0.75f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
//        bots.add(new NaiveMCTS(TIME, MAX_PLAYOUTS, PLAYOUT_TIME, MAX_DEPTH, 1.00f, 0.0f, 0.25f, new RandomBiasedAI(), new SimpleSqrtEvaluationFunction3()));
        bots.add(new AHTNAI(a_domainFileNameAI_low,TIME,MAX_PLAYOUTS,PLAYOUT_TIME,ef,playout_policy));
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
        PrintStream out = new PrintStream(new File("results.txt"));
        // Separate the matchs by map:
        List<PhysicalGameState> maps = new LinkedList<PhysicalGameState>();        
//      PrintStream out = new PrintStream(new File("results-PO.txt"));
        
        
        maps.clear();
        maps.add(PhysicalGameState.load("maps/basesWorkers8x8.xml",utt));
//      Experimenter.runExperimentsPartiallyObservable(bots, maps, 10, 3000, 300, true, out);
        Experimenter.runExperiments(bots, maps, utt, 10, 3000, 300, true, out);
      
//        maps.clear();
//        maps.add(PhysicalGameState.load("maps/melee12x12mixed12.xml",utt));
//       // Experimenter.runExperiments(bots, maps, utt, 10, 3000, 300, true, out);
//
//        maps.clear();
//        maps.add(PhysicalGameState.load("maps/melee8x8mixed6.xml",utt));
//      //  Experimenter.runExperiments(bots, maps, utt, 10, 3000, 300, true, out);
//
//        maps.clear();
//        maps.add(PhysicalGameState.load("maps/melee4x4light2.xml",utt));
      // Experimenter.runExperiments(bots, maps, utt, 10, 3000, 300, true, out);
    }
}
