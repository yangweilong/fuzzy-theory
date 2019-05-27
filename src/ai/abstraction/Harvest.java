/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.pathfinding.PathFinding;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;

/**
 *
 * @author santi
 */
public class Harvest extends AbstractAction  {
    Unit target;
    Unit base;
    PathFinding pf;
    
    public Harvest(Unit u, Unit a_target, Unit a_base, PathFinding a_pf) {
        super(u);
        target = a_target;
        base = a_base;
        pf = a_pf;
    }
    
    public boolean completed(GameState gs) {
        if (!gs.getPhysicalGameState().getUnits().contains(target) ||
            !gs.getPhysicalGameState().getUnits().contains(base)) return true;
        return false;
    }

    
    public UnitAction execute(GameState gs) {
        if (unit.getResources()==0) {
            // go get resources:
//            System.out.println("findPathToAdjacentPosition from Harvest: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, target.getX()+target.getY()*gs.getPhysicalGameState().getWidth(), gs, null);
            if (move!=null) {
                if (gs.isUnitActionAllowed(unit, move)) return move;
                return null;
            }

            // harvest:
            if (target.getX() == unit.getX() &&
                target.getY() == unit.getY()-1) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_UP,null);
            if (target.getX() == unit.getX()+1 &&
                target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_RIGHT,null);
            if (target.getX() == unit.getX() &&
                target.getY() == unit.getY()+1) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_DOWN,null);
            if (target.getX() == unit.getX()-1 &&
                target.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_LEFT,null);
        } else {
            // return resources:
//            System.out.println("findPathToAdjacentPosition from Return: (" + target.getX() + "," + target.getY() + ")");
            UnitAction move = pf.findPathToAdjacentPosition(unit, base.getX()+base.getY()*gs.getPhysicalGameState().getWidth(), gs, null);
            if (move!=null) return move;

            // harvest:
            if (base.getX() == unit.getX() &&
                base.getY() == unit.getY()-1) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_UP,null);
            if (base.getX() == unit.getX()+1 &&
                base.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_RIGHT,null);
            if (base.getX() == unit.getX() &&
                base.getY() == unit.getY()+1) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_DOWN,null);
            if (base.getX() == unit.getX()-1 &&
                base.getY() == unit.getY()) return new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_LEFT,null);
        }
        return null;
    }    
}
