/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.evaluation;

import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.units.*;
//计算时不是客观的资源而是自己观察到的
/**
 *
 * @author santi
 */
public class SimpleSqrtEvaluationFunction_po_only extends EvaluationFunction {    
    public static float RESOURCE = 20;
    public static float RESOURCE_IN_WORKER = 10;
    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        return base_score(maxplayer,gs) - base_score(minplayer,gs);
    }
    
    public float base_score(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        PartiallyObservableGameState POgs = new PartiallyObservableGameState(gs,player);
        
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        for(Unit u:POgs.getUnits())
        {
        	if (u.getPlayer()==player) {
                score += u.getResources() * RESOURCE_IN_WORKER;
                score += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();
            }
        }
        
//        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer()==player) {
//                score += u.getResources() * RESOURCE_IN_WORKER;
//                score += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();
//            }
//        }
        

//        for(int i=0;i<pgs.getHeight();i++)
//        {
//        	for(int j=0;j<pgs.getWidth();j++)
//        		if(POgs.observable(i, j)==true)
//        			score+=200;
//        }
        return score;
    }  
    
    public float base_score_min(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        float score = gs.getPlayer(player).getResources()*RESOURCE;
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==player) {
                score += u.getResources() * RESOURCE_IN_WORKER;
                score += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();
            }
        }
      
        return score;
    }
    
    public float upperBound(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int free_resources = 0;
        int player_resources[] = {gs.getPlayer(0).getResources(),gs.getPlayer(1).getResources()};
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer()==-1) free_resources+=u.getResources();
            if (u.getPlayer()==0) {
                player_resources[0] += u.getResources();
                player_resources[0] += u.getCost();
            }
            if (u.getPlayer()==1) {
                player_resources[1] += u.getResources();
                player_resources[1] += u.getCost();                
            }
        }
//        System.out.println(free_resources + " + [" + player_resources[0] + " , " + player_resources[1] + "]");
//        if (free_resources + player_resources[0] + player_resources[1]>62) {
//            System.out.println(gs);
//        }
        return (free_resources + Math.max(player_resources[0],player_resources[1]))*UNIT_BONUS_MULTIPLIER;
    }
}
