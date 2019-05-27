package ai.ahtn;

import ai.ahtn.EnhanceAHTNAI.PartialObserveSTRATEGY;
import ai.ahtn.domain.Term;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;

public class AHTNRAI extends EnhanceAHTNAI {

	public AHTNRAI(String a_domainFileName, int available_time, int max_playouts, int playoutLookahead,
			EvaluationFunction a_ef, AI a_playoutAI) throws Exception {
		super(a_domainFileName, available_time, max_playouts, playoutLookahead, a_ef, a_playoutAI);
		// TODO Auto-generated constructor stub
	}

	 public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception {
	    	
	        PlayerAction pa = new PlayerAction();  
	        
	        if(partiallyObservable)
	        {
	        	//PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
	        	PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player,PartialObserveSTRATEGY.NOspecial,this);
	        	UpdateGameStateNum(POgs1,gs);
	        	PlayerAction tm= GetActionbyState(player, POgs1);
	        	UpdateGameStateNum(gs,POgs1);
	        	return tm;
	        }
	        else
	        {
	        	return GetActionbyState(player, gs);
	        }
	        
	    }
	 
	    public AI clone() {
	        try {
	            return new POAHTNAI(domainFileName, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, ef, playoutAI);
	        }catch(Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}
