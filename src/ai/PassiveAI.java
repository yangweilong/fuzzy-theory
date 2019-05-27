/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai;

import ai.core.AI;

import java.io.PrintStream;
import java.util.List;
import rts.*;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class PassiveAI extends AI {
    public void reset() {
    }
    
    public AI clone() {
        return new PassiveAI();
    }
   
    public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) {
        PlayerAction pa = new PlayerAction();
        if(partiallyObservable)
        {
        	PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
        	pa.fillWithNones(POgs1, player, 10);
        }
        else
        {
        	pa.fillWithNones(gs, player, 10);
        }
        return pa;
    }  
    //MK
    public void ProccessFailedAction(UnitAction action,GameState gs,int player)
    {
    	
    }
    //

	@Override
	public void ProccessSuccessAction(UnitAction action, GameState gs, int player) {
		// TODO Auto-generated method stub
		
	}
}
