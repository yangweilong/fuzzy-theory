package ai.ahtn;

import ai.ahtn.EnhanceAHTNAI.PartialObserveSTRATEGY;
import ai.ahtn.domain.Term;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;

public class POAHTNAI extends AHTNAI {

	public POAHTNAI(String a_domainFileName, int available_time, int max_playouts, int playoutLookahead,
			EvaluationFunction a_ef, AI a_playoutAI) throws Exception {
		super(a_domainFileName, available_time, max_playouts, playoutLookahead, a_ef, a_playoutAI);
		// TODO Auto-generated constructor stub
	}

	 public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception {
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
	        	//PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
	        	PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player,PartialObserveSTRATEGY.Informationset,this);//生成PO的状态信息
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
	 
	    public AI clone() {
	        try {
	            return new POAHTNAI(domainFileName, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, ef, playoutAI);
	        }catch(Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
}
