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
import rts.UnitAction;

/**
 *
 * @author santi
 */
public class ContinuingAIwithoutsensor extends AI {
    public static int DEBUG = 0;
    
    protected InterruptibleAIWithComputationBudget m_AI;
    protected boolean m_isThereAComputationGoingOn = false;
    protected GameState m_gameStateUsedForComputation = null;
    
    public ContinuingAIwithoutsensor(InterruptibleAIWithComputationBudget ai) {
        m_AI = ai;
        repairnum = ai.getRepairNum();
    }
    
    public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception
    {
    	if(partiallyObservable)
    	{
    		 PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
    		 UpdateGameStateNum(gs,POgs1);
    		 PlayerAction pa =GetActionbyState(player,POgs1);
    		 UpdateGameStateNum(POgs1,gs);	 
    		 return pa;
    	}
    	else
    	{
    		return GetActionbyState(player,gs);
    	}
    }   
    
    public PlayerAction GetActionbyState(int player,GameState gs) throws Exception
    {
    	 if (gs.canExecuteAnyAction(player)) 
         {
         	 if (DEBUG>=1) 
         		System.out.println("ContinuingAI: this cycle we need an action");
             if (!m_isThereAComputationGoingOn) 
            	 m_AI.startNewComputation(player, gs.clone());
             m_AI.computeDuringOneGameFrame();
             m_isThereAComputationGoingOn = false;
             PlayerAction pa = m_AI.getBestActionSoFar();
             return pa;
         } 
    	 else 
    	 {
             if (!m_isThereAComputationGoingOn)
             {
                 GameState gs2 = gs.clone();
                 while(gs2.winner()==-1 && 
                       !gs2.gameover() &&  
                     !gs2.canExecuteAnyAction(0) && 
                     !gs2.canExecuteAnyAction(1)) gs2.cycle();
                 if ((gs2.winner() == -1 && !gs2.gameover()) && 
                     gs2.canExecuteAnyAction(player)) {
                     if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we do not need an action, but we will be next to move");
                     m_isThereAComputationGoingOn = true;
                     m_gameStateUsedForComputation = gs2;
                     m_AI.startNewComputation(player, m_gameStateUsedForComputation);
                     m_AI.computeDuringOneGameFrame();
                 } else {
                     if (DEBUG>=1) System.out.println("ContinuingAI: this cycle we do not need an action, but we will not be next to move, so we can do nothing");
                 }
             } 
             else
             {
                 if (DEBUG>=1) System.out.println("ContinuingAI: continuing a computation from a previous frame");
                 m_AI.computeDuringOneGameFrame();
             }

             return new PlayerAction();        
         }        
    }
    
    public void reset()
    {
        m_isThereAComputationGoingOn = false;
        m_gameStateUsedForComputation = null;
        m_AI.reset();
    }
    
    public AI clone()
    {
        return new ContinuingAIwithoutsensor((InterruptibleAIWithComputationBudget) m_AI.clone());
    }

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return m_AI.toString();
	}    
	//MK
    public void ProccessFailedAction(UnitAction action,GameState gs,int player)
    {
    	m_AI.ProccessFailedAction(action, gs, player);
    }
    public void ProccessSuccessAction(UnitAction action,GameState gs,int player)
    {
    	m_AI.ProccessSuccessAction(action, gs, player);
    }
    //
}
