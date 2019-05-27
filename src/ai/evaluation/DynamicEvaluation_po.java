
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//��SimpleEvaluationFunction�ı����
package ai.evaluation;

import rts.GameState;
import rts.PhysicalGameState;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.*;

/**
 *
 * @author santi
 */
public class DynamicEvaluation_po extends EvaluationFunction {    
    public static float RESOURCE = 20;
    public static float RESOURCE_IN_WORKER = 10;
    public static float RESOURCE_IN_BASE = 15;
    public static float UNIT_BONUS_MULTIPLIER = 40.0f;
    public static float UNIT_VALUE = 50;
    public static double max_unitnumber =15;//yang
    public static double max_gametime =3000;//yang
    
    public float evaluate(int maxplayer, int minplayer, GameState gs) {
        return base_score(maxplayer,gs) - base_score(minplayer,gs);
    }
    
	    public float base_score(int player, GameState gs) {
	    PhysicalGameState pgs = gs.getPhysicalGameState();
	    float score = gs.getPlayer(player).getResources()*RESOURCE;
//	    float weight1=1,weight2=1,weight3=1;
	        
	   // score=(weight1)*strategy_score(player,gs)+(weight2)*tactical_score(player,gs)+weight3*reactive_score(player,gs);   //��ֵ��������������εĹ�ֵ֮��//yang
	    score=strategy_score(player,gs);
	    return score;
    }    
 
	    
	public double DynamicChange(int player,GameState gs)//��̬�仯��������,ͨ��������״̬����Ϸʱ�䣬����ƫ�÷���AI�����ͣ�0-1�ֱ��Ǳ����ͺͼ�����//yang
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
		double perference = 0.3;
		double weight = 1;
//		double situation = 0;
		double gametime = 0;
		double Unitnumber = 0;
		
		
		for(Unit u:pgs.getUnits())
	 	{
	 		   if(u.getPlayer()==player)
	 		   {
	 			   Unitnumber++;
	 		   }
	 	}
		
		gametime = gs.getTime();
		
		perference=(Unitnumber/max_unitnumber+gametime/max_gametime)*weight;
		
		
		return perference;
	}
	
	
    public float strategy_score(int player,GameState gs)
    {//ս�Բ㡪��ƫ�򹥻����˻�չ�Լ���//yang 
    	
    	PhysicalGameState pgs = gs.getPhysicalGameState();
    	float strategy_score=0;//ս�Բ��ֵ
    	
        float economic=0;
        float military=0;
        float mapvalue=0;
        float building=0;
        //ս�Բ�����ĸ����棺���á����¡���������ͼ
    	//�ĸ������Ȩ�ر仯����Ϸʱ�䡢��Ϸ˫�������Ա����
        
        //����Unit��λ��
        economic=economic(player,gs);
        military=military(player,gs);
        mapvalue=mapvalue(player,gs);
 //      strategy_score=(float)(DynamicChange(player,gs)*military+(1-DynamicChange(player,gs))*economic);
       strategy_score=(float)(military+economic+mapvalue);
//        
//        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer()==player) {
//            	strategy_score += u.getResources() * RESOURCE_IN_WORKER;
//            	strategy_score += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();
//            }
//        }
        
        return strategy_score;
    }
    
    public float economic(int player,GameState gs)//�����ɻ�õ���Դ�����ĵ���Դ���
    {//ս����1//yang//attack_value
    	PhysicalGameState pgs = gs.getPhysicalGameState();
        float economic_score =0;
        float total_resource=0;
        float base_resource = 0;
        float carry_resource = 0;
//        tactical_score=reactive_score(player,gs);    
        for(Unit u:pgs.getUnits()) 
        {
		       if (u.getPlayer()==player)
		       {
		    	 //  tactical_score+=u.getAttackRange()*u.getAttackTime()*u.getDamage();
			    	if(u.getType().name.equals("Resource")==true)
			    		total_resource=20-u.getResources();			    		
			    	if(u.getType().name.equals("Base")==true)
			    		base_resource+=u.getResources();			    	
			    	else
			    		carry_resource+=u.getResources();
			    	
//		    	   tactical_score += UNIT_BONUS_MULTIPLIER * (u.getCost()*u.getHitPoints())/(float)u.getMaxHitPoints();		             
//		    	   tactical_score += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();//LTD2 
		        }
		}
        // score += u.getResources() * RESOURCE_IN_WORKER;
        economic_score =RESOURCE*total_resource+RESOURCE_IN_BASE*base_resource+RESOURCE_IN_WORKER*carry_resource;
       
    	return economic_score;
    }
    
    public float military(int player,GameState gs)//�����ɵ�λ������λ�Ĺ�����������ֵ�ȹ���
    {//ս����2//yang//develop_value
    	PhysicalGameState pgs = gs.getPhysicalGameState();
        float tactical_score2 =0;
        double distance=0;
        
        for(Unit u:pgs.getUnits()) 
        {
		       if (u.getPlayer()==player)
		       {
		    	   //if(u.getType().name.equals("Resource")==false)
		    	  // tactical_score2+=100;
		    	   //tactical_score2+=(30)*Math.abs((u.getX()-u.getBasePosition_x(gs))^2+(u.getY()-u.getBasePosition_y(gs))^2);	             
		    	   tactical_score2 += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();//LTD2
//		    	   tactical_score2-=(10)*Math.abs((u.getX()-u.getBasePosition_x(gs))^2+(u.getY()-u.getBasePosition_y(gs))^2);
		        }
		}         
    	return tactical_score2;
    }
        
    public float mapvalue(int player,GameState gs)//��ͼֵ�ɸ�֪��Χȷ��
    {//ս����2//yang//develop_value
    	PhysicalGameState pgs = gs.getPhysicalGameState();
        float mapvalue =0;
//        int upperX=0;
//        int upperY=0;
//        int lowerX=0;
//        int lowerY=0;
        
        for(int i=0;i<16;i++)
        {
        	for(int j=0;j<16;j++)
        		if(gs.observable(i, j)==true)
        			mapvalue++;
        }
        float score = gs.getPlayer(player).getResources()*RESOURCE;
//        double distance=0;
//        for(Unit u:pgs.getUnits()) {
//            if (u.getPlayer() == player) 
//            {
//                double d = Math.sqrt((u.getX()-x)*(u.getX()-x) + (u.getY()-y)*(u.getY()-y));
//                //���Ӹ�֪���ʣ��ø�˹�ֲ�
////                double probility = 10;
////                double pb =Math.abs(r.nextGaussian());
//                if (d<=u.getType().sightRadius) 
//                	return true;
//            }
//        }
//        
//        for(Unit u:pgs.getUnits()) 
//        {
//		       if (u.getPlayer()==player)
//		       {
//		    	   upperX=u.getX()+u.getType().sightRadius;
//		    	   lowerX=u.getX()-u.getType().sightRadius
//		    	   u.getX()
//		    	   u.getY()		    		
//		    	   mapvalue=1;
//		    	  // tactical_score2+=100;
//		    	   //tactical_score2+=(30)*Math.abs((u.getX()-u.getBasePosition_x(gs))^2+(u.getY()-u.getBasePosition_y(gs))^2);
//////		    mapvalue += UNIT_BONUS_MULTIPLIER * (u.getCost()*Math.sqrt(u.getHitPoints()))/(float)u.getMaxHitPoints();//LTD2
////		    	   tactical_score2-=(10)*Math.abs((u.getX()-u.getBasePosition_x(gs))^2+(u.getY()-u.getBasePosition_y(gs))^2);
//		        }
//		}         
    	return mapvalue;
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
        return (free_resources + Math.max(player_resources[0],player_resources[1]))*UNIT_BONUS_MULTIPLIER;
    }
}
