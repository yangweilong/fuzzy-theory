/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.io.PrintStream;

import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;

/**
 *
 * @author santi
 */
public class PseudoContinuingAI extends AI {
    public static int DEBUG = 0;
    
    protected AIWithComputationBudget m_AI;
    protected int n_cycles_to_think = 1;
    
    public PseudoContinuingAI(AIWithComputationBudget ai) {
    	//ai.AIalg = i;
        m_AI = ai;
       // AIalg = i;
        repairnum = ai.getRepairNum();
    }
    
    public PlayerAction getAction(int player, GameState gs,boolean POgs1) throws Exception
    {
        if (gs.canExecuteAnyAction(player)) 
        {
            if (DEBUG>=1) System.out.println("PseudoContinuingAI: n_cycles_to_think = " + n_cycles_to_think);
            int MT = m_AI.MAX_TIME;
            int MI = m_AI.MAX_ITERATIONS;
            if (MT>0) m_AI.MAX_TIME = MT * n_cycles_to_think;
            if (MI>0) m_AI.MAX_ITERATIONS = MI * n_cycles_to_think;
            PlayerAction action = m_AI.getAction(player,gs,POgs1);
            m_AI.MAX_TIME = MT;
            m_AI.MAX_ITERATIONS = MI;
            n_cycles_to_think = 1;   
            return action;
        }
        else
        {
            if (n_cycles_to_think==1) 
            {
                GameState gs2 = gs.clone();
                while(gs2.winner()==-1 && 
                      !gs2.gameover() &&  
                    !gs2.canExecuteAnyAction(0) && 
                    !gs2.canExecuteAnyAction(1)) 
                	gs2.cycle();
                if ((gs2.winner() == -1 && !gs2.gameover()) && 
                    gs2.canExecuteAnyAction(player)) {
                    n_cycles_to_think++;
                }            
            } else {
                n_cycles_to_think++;
            }
            
            return new PlayerAction();        
        }        
    }   
    
    public void reset()
    {
        n_cycles_to_think = 1;
        m_AI.reset();
    }
    
    public AI clone()
    {
        return new PseudoContinuingAI((AIWithComputationBudget) m_AI.clone());
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
