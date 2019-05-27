/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public abstract class AI {
    public abstract void reset();
    public abstract PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception;
    public abstract AI clone();   // this function is not supposed to do an exact clone with all the internal state, etc.
                                  // just a copy of the AI witht he same configuration.
    
    // This method can be used to report any meaningful statistics once the game is over 
    // (for example, average nodes explored per move, etc.)
//    public abstract PlayerAction getAction(int player, GameState gs) throws Exception;
    //MK
    public abstract void ProccessFailedAction(UnitAction action,GameState gs,int player);
    public abstract void ProccessSuccessAction(UnitAction action,GameState gs,int player);
    int playerID;
    int repairnum = 0;

	//ÀúÊ·¼ÇÂ¼
    public List<Unit> FirstUnitHistory = new LinkedList<Unit>();
    public List<Unit> FirstInferenceUnitHistory = new LinkedList<Unit>();
    public List<Unit> SecondUnitHistory = new LinkedList<Unit>();
    public List<Unit> SecondInferenceUnitHistory = new LinkedList<Unit>();
    public List<Unit> ThirdUnitHistory = new LinkedList<Unit>();
    public List<Unit> ThirdInferenceUnitHistory = new LinkedList<Unit>();
	//
    public List<Unit> CurrentObservedState = new LinkedList<Unit>();
    public List<Unit> ReasonState = new LinkedList<Unit>();
    public List<Unit> HistoryState = new LinkedList<Unit>();
    
    public void setPlayerID(int ID)
    {
    	playerID = ID;
    }
    public int getPlayerID()
    {
    	return playerID;
    }
    //
    public void setRepairNum(int num)
    {
    	repairnum = num;
    }
    public int getRepairNum()
    {
    	return repairnum;
    }
    public String statisticsString() {
        return null;
    }
    
    public void printStats() {
        String stats = statisticsString();
        if (stats!=null) System.out.println(stats);        
    }
    
    public String toString() {
        return this.getClass().getSimpleName();
    }  
    
    public void UpdateGameStateNum (GameState pgs, GameState gs)
    {
    	pgs.repairnum1 = gs.repairnum1;
        pgs.repairnum2 = gs.repairnum2;
        pgs.repairfailednum1 = gs.repairfailednum1;
        pgs.repairfailednum2 = gs.repairfailednum2;
        pgs.saveplantimeplayer0 = gs.saveplantimeplayer0;
        pgs.saveplantimeplayer1 = gs.saveplantimeplayer1;
        pgs.replantimeplayer0 =gs.replantimeplayer0;
        pgs.replantimeplayer1 = gs.replantimeplayer1;
        pgs.AI1plannum = gs.AI1plannum;
        pgs.AI2plannum = gs.AI2plannum;
        pgs.max_tree_nodesplayer0 = gs.max_tree_nodesplayer0;
        pgs.max_tree_nodesplayer1 = gs.max_tree_nodesplayer1;
        pgs.average_tree_depth0 = gs.average_tree_depth0;
        pgs.average_tree_depth1 = gs.average_tree_depth1 ;
        pgs.renamingIndex0=gs.renamingIndex0;
        pgs.renamingIndex1=gs.renamingIndex1;
        pgs.unitnum = gs.unitnum;
    }
    
} 
