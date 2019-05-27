/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.portfolio.portfoliogreedysearch;

import ai.core.AI;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitType;

/**
 *
 * @author santi
 */
public class UnitScriptsAI extends AI {

    public static int DEBUG = 0;
    
    UnitScript scriptsInput[];
    List<Unit> unitsInput;
    HashMap<Unit,UnitScript> scripts = new HashMap<>();
    HashMap<UnitType, List<UnitScript>> allScripts = null;
    UnitScript defaultScript = null;
    
    public UnitScriptsAI(UnitScript a_scripts[], List<Unit> a_units,
                         HashMap<UnitType, List<UnitScript>> a_allScripts,
                         UnitScript a_defaultScript) {
        scriptsInput = a_scripts;
        unitsInput = a_units;
        for(int i = 0;i<a_scripts.length;i++) {
            scripts.put(a_units.get(i), a_scripts[i]);
        }
        allScripts = a_allScripts;
        defaultScript = a_defaultScript;
    }
    
    
    public void reset() {
    }
  //MK
    public void ProccessFailedAction(UnitAction action,GameState gs,int player)
    {
    	
    }
    public void ProccessSuccessAction(UnitAction action,GameState gs,int player)
    {
    	
    }
    //
    public void resetScripts(GameState gs) {   
        for(Unit u:scripts.keySet()) {
            UnitScript s = scripts.get(u);
            scripts.put(u, s.instantiate(u, gs));
        }
    }
    

    public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception {
//        System.out.println("    UnitScriptsAI.getAction " + player + ", " + gs.getTime());
        PlayerAction pa = new PlayerAction();
        
        if(partiallyObservable)
        {
        	 PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player);
        	 for(Unit u:POgs1.getUnits()) {
                 if (u.getPlayer()==player && POgs1.getUnitAction(u)==null) {
                     UnitScript s = scripts.get(u);
                     if (s!=null) s = s.instantiate(u, POgs1);
                     if (s==null) {
                         // new unit, or completed script
                         s = allScripts.get(u.getType()).get(0).instantiate(u, POgs1);
                         if (s==null) s = defaultScript.instantiate(u, POgs1);
                         scripts.put(u,s);
                     }
                     UnitAction ua = s.getAction(u, POgs1);
                     if (ua!=null) {
                         pa.addUnitAction(u, ua);
                     } else {
                         pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE,null));
                     }
                 }
             }
        }
        else
        {
        for(Unit u:gs.getUnits()) {
            if (u.getPlayer()==player && gs.getUnitAction(u)==null) {
                UnitScript s = scripts.get(u);
                if (s!=null) s = s.instantiate(u, gs);
                if (s==null) {
                    // new unit, or completed script
                    s = allScripts.get(u.getType()).get(0).instantiate(u, gs);
                    if (s==null) s = defaultScript.instantiate(u, gs);
                    scripts.put(u,s);
                }
                UnitAction ua = s.getAction(u, gs);
                if (ua!=null) {
                    pa.addUnitAction(u, ua);
                } else {
                    pa.addUnitAction(u, new UnitAction(UnitAction.TYPE_NONE,null));
                }
            }
        }
        }
        return pa;
    }

    public AI clone() {
        return new UnitScriptsAI(scriptsInput, unitsInput, allScripts, defaultScript);
    }
    
}
