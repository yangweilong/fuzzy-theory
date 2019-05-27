/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts.units;

import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.UnitAction;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class Unit {
    UnitType type;
    
    public static long next_ID = 0;
    
    long ID;
    int player;
    int x,y;
    int resources;
    int hitpoints = 0;
    int sightRadius=1;
//    int list history_position
    
    public boolean IsInferenced = false;
    
    public Unit(int a_player, UnitType a_type, int a_x, int a_y, int a_resources) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = a_resources;
        hitpoints = a_type.hp;
        sightRadius=a_type.sightRadius;
        ID = next_ID++;
    }
    
//    public Unit(int a_player,UnitType a_type,int a_X,int a_y,int a_resources,list history_position)
//    {
//    	
//    }
    
    public Unit(int a_player, UnitType a_type, int a_x, int a_y) {
        player = a_player;
        type = a_type;
        x = a_x;
        y = a_y;
        resources = 0;
        hitpoints = a_type.hp;
        sightRadius=a_type.sightRadius;
        ID = next_ID++;
    }
    
    
    public Unit(Unit u) {
        player = u.player;
        type = u.type;
        x = u.x;
        y = u.y;
        resources = u.resources;
        hitpoints = u.hitpoints;
        sightRadius=u.sightRadius;
        ID = u.ID;     
        IsInferenced = u.IsInferenced;
    }
            
    
    public int getPlayer() {
        return player;
    }
    
    public UnitType getType() {
        return type;
    }
    
    public void setType(UnitType a_type) {
        type = a_type;
    }
    
    public long getID() {
        return ID;
    }
    
    // note: Do not use this function unless you know what you are doing!
    public void setID(long a_ID) {
        ID = a_ID;
    }
    
    public int getPosition(PhysicalGameState pgs) {
        return x + pgs.getWidth()*y;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setX(int a_x) {
        x = a_x;
    }

    public void setY(int a_y) {
        y = a_y;
    }
    
    public int getResources() {
        return resources;
    }         
    
    public void setResources(int a_resources) {
        resources = a_resources;
    }
    
    public int getHitPoints() {
        return hitpoints;
    }
    
    public int getMaxHitPoints() {
        return type.hp;
    }
    
    public void setHitPoints(int a_hitpoints) {
        hitpoints = a_hitpoints;
    }
    
    public int getCost() {
        return type.cost;
    }
    
    public int getMoveTime() {
        return type.moveTime;
    }
    
    public int getSensorTime() {
        return type.sensorTime;
    }//yang 感知
    
    public int getSensoropenTime() {
        return type.sensoropenTime;
    }//yang 感知
    
    public void setSightRadius(int a_sightRadius) {
    	sightRadius=a_sightRadius;
    }//yang 感知
    
//    public void setSightRadius_close(int a_sightRadius) {
//    	sightRadius=a_sightRadius;
//    }//yang 感知
    
    public int getSightRadius() {
    	return sightRadius;
    }//yang 感知
    
    public int getAttackTime() {
        return type.attackTime;
    }
    
    public int getAttackRange() {
        return type.attackRange;
    }
    
    public int getDamage() {
        return type.damage;
    }
     
    public int getHarvestAmount() {
        return type.harvestAmount;
    }

    public List<UnitAction> getUnitActions(GameState s) {
        // Unless specified, generate "NONE" actions with duration 8 cycles
        return getUnitActions(s, 10);
    }

    public List<UnitAction> getUnitActions(GameState s, int duration) {
        List<UnitAction> l = new ArrayList<UnitAction>();
      
        
        PhysicalGameState pgs = s.getPhysicalGameState();
        Player p = pgs.getPlayer(player);
//        UnitType ut1 = s.getUnitTypeTable().getUnitType(p.toString());
 //       Boolean on_off=true;
        /*
        Unit uup = pgs.getUnitAt(x,y-1);
        Unit uright = pgs.getUnitAt(x+1,y);
        Unit udown = pgs.getUnitAt(x,y+1);
        Unit uleft = pgs.getUnitAt(x-1,y);
        */
        
        Unit uup = null, uright = null, udown = null, uleft = null;
        for(Unit u:pgs.getUnits())
        {
            if (u.x==x) {
                if (u.y==y-1) {
                    uup = u;
                } 
                else if (u.y==y+1) {
                    udown = u;
                }
            } 
            else 
            {
                if (u.y==y) {
                    if (u.x==x-1) {
                        uleft = u;
                    } 
                    else if (u.x==x+1) {
                        uright = u;
                    }
                }
            }
        }
        
        if (type.canAttack) {
            if (type.attackRange==1) {
                if (y>0 && uup!=null && uup.player!=player && uup.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uup.x,uup.y,null));                
                if (x<pgs.getWidth()-1 && uright!=null && uright.player!=player && uright.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uright.x,uright.y,null));                
                if (y<pgs.getHeight()-1 && udown!=null && udown.player!=player && udown.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,udown.x,udown.y,null));
                if (x>0 && uleft!=null && uleft.player!=player && uleft.player>=0) l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,uleft.x,uleft.y,null));                
            } else {
                int sqrange = type.attackRange*type.attackRange;
                for(Unit u:pgs.getUnits()) {
                    if (u.player<0 || u.player==player) continue;
                    int sq_dx = (u.x - x)*(u.x - x);
                    int sq_dy = (u.y - y)*(u.y - y);
                    if (sq_dx+sq_dy<=sqrange) {
                        l.add(new UnitAction(UnitAction.TYPE_ATTACK_LOCATION,u.x,u.y,null));
                    }
                }
            }
        }
        
        if (type.canHarvest) {
            // harvest:
            if (resources==0) {
                if (y>0 && uup!=null && uup.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_UP,null));
                if (x<pgs.getWidth()-1 && uright!=null && uright.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_RIGHT,null));
                if (y<pgs.getHeight()-1 && udown!=null && udown.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_DOWN,null));
                if (x>0 && uleft!=null && uleft.type.isResource) l.add(new UnitAction(UnitAction.TYPE_HARVEST,UnitAction.DIRECTION_LEFT,null));
            }
            // return:
            if (resources>0) {
                if (y>0 && uup!=null && uup.type.isStockpile && uup.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_UP,null));
                if (x<pgs.getWidth()-1 && uright!=null && uright.type.isStockpile && uright.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_RIGHT,null));
                if (y<pgs.getHeight()-1 && udown!=null && udown.type.isStockpile && udown.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_DOWN,null));
                if (x>0 && uleft!=null && uleft.type.isStockpile && uleft.player == player) l.add(new UnitAction(UnitAction.TYPE_RETURN,UnitAction.DIRECTION_LEFT,null));            
            }            
        }
        
        for(UnitType ut:type.produces) {
            if (p.getResources()>=ut.cost) { 
                int tup = (y>0 ? pgs.getTerrain(x, y-1):PhysicalGameState.TERRAIN_WALL);
                int tright = (x<pgs.getWidth()-1 ? pgs.getTerrain(x+1, y):PhysicalGameState.TERRAIN_WALL);
                int tdown = (y<pgs.getHeight()-1 ? pgs.getTerrain(x, y+1):PhysicalGameState.TERRAIN_WALL);
                int tleft = (x>0 ? pgs.getTerrain(x-1, y):PhysicalGameState.TERRAIN_WALL);

                if (tup==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x,y-1) == null) 
                	l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_UP,ut,null));
                if (tright==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x+1,y) == null) 
                	l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_RIGHT,ut,null));
                if (tdown==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x,y+1) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_DOWN,ut,null));
                if (tleft==PhysicalGameState.TERRAIN_NONE && pgs.getUnitAt(x-1,y) == null) l.add(new UnitAction(UnitAction.TYPE_PRODUCE,UnitAction.DIRECTION_LEFT,ut,null));
            }
        }
        
        if (type.canMove) {
            int tup = (y>0 ? pgs.getTerrain(x, y-1):PhysicalGameState.TERRAIN_WALL);
            int tright = (x<pgs.getWidth()-1 ? pgs.getTerrain(x+1, y):PhysicalGameState.TERRAIN_WALL);
            int tdown = (y<pgs.getHeight()-1 ? pgs.getTerrain(x, y+1):PhysicalGameState.TERRAIN_WALL);
            int tleft = (x>0 ? pgs.getTerrain(x-1, y):PhysicalGameState.TERRAIN_WALL);

            if (tup==PhysicalGameState.TERRAIN_NONE && uup == null) 
            	l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_UP,null));
            if (tright==PhysicalGameState.TERRAIN_NONE && uright == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_RIGHT,null));
            if (tdown==PhysicalGameState.TERRAIN_NONE && udown == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_DOWN,null));
            if (tleft==PhysicalGameState.TERRAIN_NONE && uleft == null) l.add(new UnitAction(UnitAction.TYPE_MOVE,UnitAction.DIRECTION_LEFT,null));
        }
        
        if (type.canSensor)
        {
        	l.add(new UnitAction(UnitAction.TYPE_SENSOR,true,UnitAction.DIRECTION_UP,null));
  //          l.add(new UnitAction(UnitAction.TYPE_SENSOR,null));           
        }//YANG 感知
        
//        if (type.canSensor_open) {
//        	l.add(new UnitAction(UnitAction.TYPE_SENSOR_OPEN,true,UnitAction.DIRECTION_UP,null));
//        //	l.add(new UnitAction(UnitAction.TYPE_SENSOR_OPEN,true,UnitAction.DIRECTION_UP,null));
// //           l.add(new UnitAction(UnitAction.TYPE_SENSOR,null));           
//        }//YANG 感知
        
        
        
        // units can always stay idle:
        
        l.add(new UnitAction(UnitAction.TYPE_NONE, duration,null));
                        
        return l;
    }
    
    public String toString() {
        return type.name + "(" + ID + ")" + 
               "(" + player + ", (" + x + "," + y + "), " + hitpoints + ", " + resources + ")";
    }
    
    public Unit clone() {
        return new Unit(this);
    }
    
    /*
    public boolean equals(Object o) {
        return ID == ((Unit)o).ID;
    }
    */
    
    public int hashCode() {
        return (int)ID;
    }    
    
    
    public void toxml(XMLWriter w) {
       w.tagWithAttributes(this.getClass().getName(), "type=\"" + type.name + "\" " + 
                                                      "ID=\"" + ID + "\" " + 
                                                      "player=\"" + player + "\" " + 
                                                      "x=\"" + x + "\" " + 
                                                      "y=\"" + y + "\" " + 
                                                      "resources=\"" + resources + "\" " + 
                                                      "hitpoints=\"" + hitpoints + "\" ");
       w.tag("/" + this.getClass().getName());
    }

    
    public Unit(Element e, UnitTypeTable utt) {
        String typeName = e.getAttributeValue("type");
        String IDStr = e.getAttributeValue("ID");
        String playerStr = e.getAttributeValue("player");
        String xStr = e.getAttributeValue("x");
        String yStr = e.getAttributeValue("y");
        String resourcesStr = e.getAttributeValue("resources");
        String hitpointsStr = e.getAttributeValue("hitpoints");
        
        type = utt.getUnitType(typeName);
        ID = Integer.parseInt(IDStr);
        if (ID>=next_ID) next_ID = ID+1;
        player = Integer.parseInt(playerStr);
        x = Integer.parseInt(xStr);
        y = Integer.parseInt(yStr);
        resources = Integer.parseInt(resourcesStr);
        hitpoints = Integer.parseInt(hitpointsStr);
    }    
    
}
