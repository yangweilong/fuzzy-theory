/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.io.PrintStream;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;

/**
 *
 * @author santi
 * 
 * A "InterruptibleAI" is one that can divide the computation across multiple game frames. As such, it must implement the three
 * basic methods described below, does cannot modify the getAction method
 */
public abstract class InterruptibleAIWithComputationBudgetWithoutPO extends AIWithComputationBudget {
    
    public InterruptibleAIWithComputationBudgetWithoutPO(int mt, int mi) {
        super(mt,mi);
    }
    
    public final PlayerAction getAction(int player, GameState gs,boolean POgs1) throws Exception
    {
    	if(gs.canExecuteAnyAction(player))
		{
			if(player==0)
                gs.AI1plannum = gs.AI1plannum+1;
             else
            	 gs.AI2plannum = gs.AI2plannum+1;
		}
        if (gs.canExecuteAnyAction(player)) 
        {
        	if(POgs1)
        	{
        		PartiallyObservableGameState POGS = new PartiallyObservableGameState(gs,player);
	            startNewComputation(player,POGS.clone());
	            computeDuringOneGameFrame();
	            return getBestActionSoFar();
        	}
        	else
        	{
        		startNewComputation(player,gs.clone());
	            computeDuringOneGameFrame();
	            return getBestActionSoFar();
        	}
        } 
        else
        {
            return new PlayerAction();        
        }       
    }
    
    
 
    
    
    public abstract void startNewComputation(int player, GameState gs) throws Exception;
    public abstract void computeDuringOneGameFrame() throws Exception;
    public abstract PlayerAction getBestActionSoFar() throws Exception;
    
}
