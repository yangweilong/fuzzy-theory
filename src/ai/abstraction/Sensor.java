/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class Sensor extends AbstractAction  {
    
    public Sensor(Unit u) {
        super(u);
    }
    
    public boolean completed(GameState gs) {
        return false;
    }

    public UnitAction execute(GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        
        UnitAction ua = null;
        ua=new UnitAction(UnitAction.TYPE_SENSOR,true,UnitAction.DIRECTION_UP,null);
        
        return null;
    }    
    
}
