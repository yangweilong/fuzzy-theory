/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rts;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Random;

import ai.ahtn.EnhanceAHTNAI.PartialObserveSTRATEGY;

import ai.core.AI;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */

public class PartiallyObservableGameState extends GameState {
    int player;   // the observer player
    
    // creates a partially observable game state, from the point of view of 'player':
    public PartiallyObservableGameState(PhysicalGameState a_pgs, UnitTypeTable a_utt, int a_player) {
        super(a_pgs, a_utt);
        player = a_player;
        Random r = new Random();
    }
    //��֪���ǲ�������ʷ��Ϣ��Ӱ��ģ�����һ��ʵ��۲첻��ʱ������ɾ�����������ǿ���ֻ������
    public PartiallyObservableGameState(GameState gs, int a_player) 
    {
        super(gs.getPhysicalGameState().cloneKeepingUnits(), gs.getUnitTypeTable());
        time = gs.time;
        player = a_player;
        
        Realworld = false; 
        unitActions.putAll(gs.unitActions);
//        for(UnitActionAssignment uaa:gs.unitActions.values()) 
//        {
//            Unit u = uaa.unit;
//
//            int idx = gs.pgs.getUnits().indexOf(u);
//            if (idx==-1) 
//            {
//            	//MK
//            	continue;
//            	//
//            } else {
//                Unit u2 = pgs.getUnits().get(idx);
//                unitActions.put(u2,new UnitActionAssignment(u2, uaa.action, uaa.time));
//            }                
//        }    
        
        List<Unit> toDelete = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) //�жϵз���Unit�Ƿ�ɹ۲졣
        {
            if (u.getPlayer() != player) 
            {
                if (!observable(u.getX(),u.getY())) 
                {
                    toDelete.add(u);
                }
            }
        }
        for(Unit u:toDelete)
        {
        	removeUnit(u);   //���ǲ��ڼ�����֪��Χ�ڵ�unit�������Լ���gamestate���Ƴ�
        }  
        
        //�γɵ�UnitList�ǵ�ǰ��֪���ĵ�λ���б�
    }

    public PartiallyObservableGameState(GameState gs, int a_player, PartialObserveSTRATEGY currentPO,AI ai) 
    {
        super(gs.getPhysicalGameState().cloneKeepingUnits(), gs.getUnitTypeTable());
        time = gs.time;
        player = a_player;
        
        Realworld = false; 
        unitActions.putAll(gs.unitActions);
        
		 
        List<Unit> toDelete = new LinkedList<Unit>();
        for(Unit u:pgs.getUnits()) 
        {
            if (u.getPlayer() != player) 
            {
                if (!observable(u.getX(),u.getY())) 
                {
                	if(currentPO==PartialObserveSTRATEGY.NOspecial)
                	{
                		toDelete.add(u);
                	}
                	else
                	{
                		if(u.getType().name.equals("Resource")==false&&u.getType().name.equals("Base")==false&&u.getType().name.equals("Barracks")==false)
                		{
                			toDelete.add(u);
                		}
                	}
                }
            }
        }
        for(Unit u:toDelete)
        {
        	removeUnit(u);   //���ǲ��ڼ�����֪��Χ�ڵ�unit�������Լ���gamestate���Ƴ�
        }
        
        
        UpdatePOGameStatebyHistory(gs,a_player,currentPO,ai);
        //�´��õĵڶ��ε���ʷ��¼
        //�Ա�����ʷ��Ϣ���и��£������������Ϣ�����뵽��ʷ��Ϣ����
        
        
        ai.SecondUnitHistory.clear();
        for(Unit tu:ai.FirstUnitHistory)
        {
        	for(Unit eu:gs.getUnits())
        	{
        		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
        		{
        			Unit tempu = new Unit(tu).clone();
                	ai.SecondUnitHistory.add(tempu);
        		}
        	}
        } 
       
      	ai.FirstUnitHistory.clear();
        for(Unit u:pgs.getUnits()) 
        {
        	if(u.IsInferenced||u.getPlayer()==player) continue;
        	if(u.getType().name.equals("Resource")==true||u.getType().name.equals("Base")==true||u.getType().name.equals("Barracks")==true)
        		continue;
        	Unit tempu = new Unit(u).clone();
        	ai.FirstUnitHistory.add(tempu);
        	
        }         
    }
    
    public void UpdatePOGameStatebyHistory(GameState gs, int a_player, PartialObserveSTRATEGY currentPO,AI ai) {
		// TODO Auto-generated method stub 
    	if(currentPO==PartialObserveSTRATEGY.RemObservedUnit)
    	{
//    		1��	��¼���й̶�λ�õ�unit
//    		2��	����������ʷ�۲��¼
//    		3��	�����ι۲��¼��ɾ���̶���unit
//    		4��	�����ι۲��¼��ɾ��������unit
//    		5��	����ʷ�۲켯�г�ȥ���ι۲켯���Ѿ����ڵ�unit
//    		6��	��ʷ��¼��ֻ������ʵ�����ģ��������Ʋ��ʵ��
    		List<Unit> toDelete = new LinkedList<Unit>();
    		
    		for(Unit tu:ai.FirstUnitHistory)
            {
            	for(Unit eu:gs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }
    		ai.FirstUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		//�۲����һ����ʷ��Ϣ��¼һ�£��ڵ�һ����ȥ��
    		
    		
    		
    		for(Unit tu:ai.SecondUnitHistory)
            {
            	for(Unit eu:pgs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }
    		ai.SecondUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		//�۲���ڶ�����ʷ��Ϣ��¼һ�£��ڵڶ�����ȥ��
    		
    		
    		for(Unit poU:ai.FirstUnitHistory)
    		{
    			if (observable(poU.getX(),poU.getY()))//���Ŀǰ�ɹ�
    			{
    				//����Ĳ��ɹ۲��λ��
    				Unit u=UnObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    			else//���Ŀǰ���ɹ�
    			{
    				for(Unit tu:ai.SecondUnitHistory)
    				{
    					if(poU.getID()==tu.getID()&&poU.getClass()==tu.getClass())
    					{
    						if(poU.getX()==tu.getX()&&poU.getY()==tu.getY())//������ʷλ����ͬ
    						{
    							Unit u = new Unit(poU);
        						pgs.addUnit(u);
        						break;
    						}
    						else
    						{
    							//λ�ò�ͬ��˵��ʵ������Ѿ�����
    							Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    							if(u==null)
    							{
    		    					toDelete.add(poU);
    		    					continue;
    		    				}
    		    				u.IsInferenced = true;
    		    				pgs.units.add(u);
    						}
    					}
    				}
    				//������ϴβ��ɹ�
    				Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    		}
    		ai.FirstUnitHistory.remove(toDelete);
    		return;
    	}
    	
    	else if(currentPO==PartialObserveSTRATEGY.Inference)
    	{

//    		1����¼���й̶�λ�õ�Unit
//    		2�������ڵĹ۲�ֵ���뵽�۲⼯��
//    		3���Թ۲⼯���е������γɸ���λ���ܵĹ켣��
//    		4���γ�belief_states,�������ɿ���information set
//    		5��������ѡ����ʵ�
//    		
    		List<Unit> toDelete = new LinkedList<Unit>();
    		for(Unit tu:ai.FirstUnitHistory)
            {
            	for(Unit eu:gs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }
    		
    		
    		ai.FirstUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		for(Unit tu:ai.SecondUnitHistory)
            {
            	for(Unit eu:pgs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }
    		ai.SecondUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		
    		
    		for(Unit poU:ai.FirstUnitHistory)
    		{
    			if (observable(poU.getX(),poU.getY()))
    			{
    				Unit u=UnObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    			else
    			{
    				for(Unit tu:ai.SecondUnitHistory)
    				{
    					if(poU.getID()==tu.getID()&&poU.getClass()==tu.getClass())
    					{
    						if(poU.getX()==tu.getX()&&poU.getY()==tu.getY())
    						{
    							Unit u = new Unit(poU);
        						pgs.addUnit(u);
        						break;
    						}
    						else
    						{
    							//λ�ò�ͬ��˵��ʵ������Ѿ�����
    							Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    							if(u==null)
    							{
    		    					toDelete.add(poU);
    		    					continue;
    		    				}
    		    				u.IsInferenced = true;
    		    				pgs.units.add(u);
    						}
    					}
    				}
    				//������ϴβ��ɹ�
    				Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    		}
    		ai.FirstUnitHistory.remove(toDelete);
    		boolean flag = false;
    		for(Unit u:pgs.getUnits()) 
    	    {
    	         if (u.getPlayer() == player||u.IsInferenced) 
            	 {
            		 continue;
            	 }
    			 if(u.getType().name.equals("Barracks")==true)
    			 {
    				 flag = true;
    				 break;
    			 }
    	    }         
    		if(!flag)
    		{
    			for(Unit u:pgs.getUnits()) 
    			{
    				if (u.getPlayer() == player||u.IsInferenced) 
               	   {
               		 continue;
               	   }
    			   if(u.getType().name.equals("Light")==true||u.getType().name.equals("Heavy")==true||u.getType().name.equals("Ranged")==true)
          	       {	
    				   UnitType ut = gs.getUnitTypeTable().getUnitType("Barracks");
      		           Unit tempu = minObservablePosition(u.getX(),u.getY(),u);
      		           tempu.IsInferenced = true;
      		           tempu.setType(ut);
      		           break;
          	       }
    			}
    		}
    		return;
    	}
    	else if(currentPO==PartialObserveSTRATEGY.Informationset)
    	{
    		//
    		//1.�����ι۲쵽�ļ��뵽�۲�ֵ��
    		//2.���ϴι۲쵽����δ�۲쵽�Ľ���Ԥ��
    		//3.
    		
    		    			
    		//////////////////�Ե�һ���Լ��ڶ��ν��и��£����û�п���
    		Boolean exist_flag=true;
    		List<Unit> unitlist = new LinkedList<Unit>();
    		List<Unit> toDelete = new LinkedList<Unit>();
    		for(Unit FirstU:ai.FirstUnitHistory)
    		{
    			unitlist.add(FirstU);
    		}
    		for(Unit SecondU:ai.SecondUnitHistory)
    		{
    			for(Unit exist:unitlist)
    			{
    				if(SecondU.getID()==exist.getID()&&SecondU.getClass()==exist.getClass())
    					exist_flag=false;
    					break;
    			}
    			if(exist_flag==true)
    				unitlist.add(SecondU);
    		}	
    		for(Unit u:unitlist)
    		{
    			if(u.getHitPoints()==0)
    				toDelete.add(u);
    		}//////���д��ڵĵ�λ
    		unitlist.removeAll(toDelete);
    		toDelete.clear();
    		
    		List<Unit> toReason = new LinkedList<Unit>();
    		toReason=unitlist;
    		for(Unit poU:toReason)
    		{
    			if (observable(poU.getX(),poU.getY()))
    			    toDelete.add(poU); 			
    		}//��Ҫ����ĵ�λ
    		
    		toReason.removeAll(toDelete);
    		
    		System.out.print(unitlist);
    		System.out.print(toReason);
    		
    		//��¼����Ҫ����ĵ�λ����ʷλ��    		
    		for(Unit u:toReason)
    		{
    			for(Unit tu:ai.FirstUnitHistory)
    			{
    				if(u.getID()==tu.getID()&&u.getClass()==tu.getClass())
            		{
//            			.add(tu);
            		}
            	}
    				
    			}
    		}
    		
    		
    		
    		List<Unit> toDelete = new LinkedList<Unit>();
    		for(Unit tu:ai.FirstUnitHistory)
            {
            	for(Unit eu:gs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }   		
    		
    		ai.FirstUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		for(Unit tu:ai.SecondUnitHistory)
            {
            	for(Unit eu:gs.getUnits())
            	{
            		if(eu.getID()==tu.getID()&&eu.getClass()==tu.getClass())
            		{
            			toDelete.add(tu);
            		}
            	}
            }
    		ai.SecondUnitHistory.removeAll(toDelete);
    		toDelete.clear();
    		
    		
    		for(Unit poU:ai.FirstUnitHistory)
    		{
    			if (observable(poU.getX(),poU.getY()))
    			{
    				Unit u=UnObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    			else
    			{
    				for(Unit tu:ai.SecondUnitHistory)
    				{
    					if(poU.getID()==tu.getID()&&poU.getClass()==tu.getClass())
    					{
    						if(poU.getX()==tu.getX()&&poU.getY()==tu.getY())
    						{
    							Unit u = new Unit(poU);
        						pgs.addUnit(u);
        						break;
    						}
    						else
    						{
    							//λ�ò�ͬ��˵��ʵ������Ѿ�����
    							Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    							if(u==null)
    							{
    		    					toDelete.add(poU);
    		    					continue;
    		    				}
    		    				u.IsInferenced = true;
    		    				pgs.units.add(u);
    						}
    					}
    				}
    				//������ϴβ��ɹ�
    				Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    		}
    		ai.FirstUnitHistory.remove(toDelete);
    		boolean flag = false;
    		for(Unit u:pgs.getUnits()) 
    	    {
    	         if (u.getPlayer() == player||u.IsInferenced) 
            	 {
            		 continue;
            	 }
    			 if(u.getType().name.equals("Barracks")==true)
    			 {
    				 flag = true;
    				 break;
    			 }
    	    }         
    		if(!flag)
    		{
    			for(Unit u:pgs.getUnits()) 
    			{
    				if (u.getPlayer() == player||u.IsInferenced) 
               	   {
               		 continue;
               	   }
    			   if(u.getType().name.equals("Light")==true||u.getType().name.equals("Heavy")==true||u.getType().name.equals("Ranged")==true)
          	       {	
    				   UnitType ut = gs.getUnitTypeTable().getUnitType("Barracks");
      		           Unit tempu = minObservablePosition(u.getX(),u.getY(),u);
      		           tempu.IsInferenced = true;
      		           tempu.setType(ut);
      		           break;
          	       }
    			}
    		}
    		return;
    		
    		
//    			else//��ʷ�۲쵫���ڲ��ɹ۵�
//    			{
//    				for(Unit tu:ai.SecondUnitHistory)
//    				{
//    					if(poU.getID()==tu.getID()&&poU.getClass()==tu.getClass())
//    					{
//    						if(poU.getX()==tu.getX()&&poU.getY()==tu.getY())
//    						{
//    							Unit u = new Unit(poU);
//        						pgs.addUnit(u);
//        						break;
//    						}
//    						else
//    						{
//    							//λ�ò�ͬ��˵��ʵ������Ѿ�����
////    							Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);//���ʵ��λ�ÿɹۣ���һ������Ĳ��ɹ۲�λ��
//    							Unit u=reasonPosition(poU.getX(),poU.getY(),poU);//�����˲��ķ���
//    							if(u==null)
//    							{
//    		    					toDelete.add(poU);
//    		    					continue;
//    		    				}
//    		    				u.IsInferenced = true;
//    		    				pgs.units.add(u);
//    						}
//    					}
//    				}
//    				//������ϴβ��ɹ�
//    				Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);
//    				if(u==null)
//    				{
//    					toDelete.add(poU);
//    					continue;
//    				}
//    				u.IsInferenced = true;
//    				pgs.units.add(u);
//    			}
//    		}
//    		ai.FirstUnitHistory.remove(toDelete);
//    		boolean flag = false;
//    		for(Unit u:pgs.getUnits()) 
//    	    {
//    	         if (u.getPlayer() == player||u.IsInferenced) 
//            	 {
//            		 continue;
//            	 }
//    			 if(u.getType().name.equals("Barracks")==true)
//    			 {
//    				 flag = true;
//    				 break;
//    			 }
//    	    }         
//    		if(!flag)
//    		{
//    			for(Unit u:pgs.getUnits()) 
//    			{
//    				if (u.getPlayer() == player||u.IsInferenced) 
//               	   {
//               		 continue;
//               	   }
//    			   if(u.getType().name.equals("Light")==true||u.getType().name.equals("Heavy")==true||u.getType().name.equals("Ranged")==true)
//          	       {	
//    				   UnitType ut = gs.getUnitTypeTable().getUnitType("Barracks");
//      		           Unit tempu = minObservablePosition(u.getX(),u.getY(),u);
//      		           tempu.IsInferenced = true;
//      		           tempu.setType(ut);
//      		           break;
//          	       }
//    			}
//    		}
//    		
    		
    	}
//    	else if(currentPO==PartialObserveSTRATEGY.Markov)
//    	{ 
//    		//���������Ʒ�������ٶ���λ����������ƶ�
//    		//1���ж��ϴλ�����β��ڵ�Unit
//    		//2.�ж�����Ե����λ��
//    		//3.������Ե����λ��ʹ������Ʒ򷽷������жϣ�ʹ�ø���ѡ��λ�ã����ѡ��λ�ã�
//    		
//
//    		return;
//    	}
//    	else if(currentPO==PartialObserveSTRATEGY.NOspecial)
//    	{
//    		
//    		return;
//    	}
//	}
    
    public Unit UnObservablePosition(int x, int y,Unit poU)
    {
    	//���ʵ��λ�ÿɹۣ���һ������Ĳ��ɹ۲�λ��
		int i=0;
		while(true)
		{
			if(!observable(x-i,y)&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y);
				return u;
			}
			if(!observable(x-i,y-i)&&(x-i)>=0&&(y-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y-i);
				return u;
			}
			if(!observable(x,y-i)&&(y-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y-i);
				return u;
			}
			if(!observable(x+i,y-i)&&(y-i>=0)&&(x+i)<pgs.width)
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y-i);
				return u;
			}
			if(!observable(x+i,y)&&(x+i)<pgs.width)
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y);
				return u;
			}
			if(!observable(x+i,y+i)&&(x+i<pgs.width)&&(y+i)<pgs.height)
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y+i);
				return u;
			}
			if(!observable(x,y+i)&&(y+i)<pgs.height)
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y+i);
				return u;
			}
			if(!observable(x-i,y+i)&&(y+i)<pgs.height&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y+i);
				return u;
			}
			i++;
			if(i>pgs.height)
			{
				return null;
			}
		}		
    }
    
    public Unit minunObservablePosition(int x, int y,Unit poU)
    {
    	//���ʵ��λ�ÿɹۣ���һ������Ĳ��ɹ۲�λ��
		int i=0;		
		while(true)
		{
			if(observable(x-i,y)&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i+1);
				u.setY(y);
				return u;
			}
			if(observable(x-i,y-i)&&(x-i)>=0&&(y-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i+1);
				u.setY(y-i+1);
				return u;
			}
			if(observable(x,y-i)&&(y-i>=0))
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y-i+1);
				return u;
			}
			if(observable(x+i,y-i)&&(y-i>=0)&&(x+i)<pgs.width)
			{
				Unit u = new Unit(poU);
				u.setX(x+i-1);
				u.setY(y-i+1);
				return u;
			}
			if(observable(x+i,y)&&(x+i<pgs.width))
			{
				Unit u = new Unit(poU);
				u.setX(x+i-1);
				u.setY(y);
				return u;
			}
			if(observable(x+i,y+i)&&(x+i<pgs.width)&&(y+i<pgs.height))
			{
				Unit u = new Unit(poU);
				u.setX(x+i-1);
				u.setY(y+i-1);
				return u;
			}
			if(observable(x,y+i)&&(y+i)<pgs.height)
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y+i-1);
				return u;
			}
			if(observable(x-i,y+i)&&(y+i)<pgs.height&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i+1);
				u.setY(y+i-1);
				return u;
			}
			i++;
			if(i>pgs.height)
			{
				return null;
			}
		}
		
    }
    
    public Unit minObservablePosition(int x, int y,Unit poU)
    {
    	//���ʵ��λ�ÿɹۣ���һ������Ĳ��ɹ۲�λ��
		int i=0;
		List<Unit> position = new LinkedList<Unit>();
		boolean flag = true;
		while(flag)
		{
			if(!observable(x-i,y)&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y);
				position.add(u);
				flag=false;				
			}
			if(!observable(x-i,y-i)&&(x-i)>=0&&(y-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y-i);
				position.add(u);
				flag=false;	
			}
			if(!observable(x,y-i)&&(y-i>=0))
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y-i);
				position.add(u);
				flag=false;	
			}
			if(!observable(x+i,y-i)&&(y-i>=0)&&(x+i)<pgs.width)
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y-i);
				position.add(u);
				flag=false;	
			}
			if(!observable(x+i,y)&&(x+i<pgs.width))
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y);
				position.add(u);
				flag=false;	
			}
			if(!observable(x+i,y+i)&&(x+i<pgs.width)&&(y+i<pgs.height))
			{
				Unit u = new Unit(poU);
				u.setX(x+i);
				u.setY(y+i);
				position.add(u);
				flag=false;	
			}
			if(!observable(x,y+i)&&(y+i)<pgs.height)
			{
				Unit u = new Unit(poU);
				u.setX(x);
				u.setY(y+i);
				position.add(u);
				flag=false;	
			}
			if(!observable(x-i,y+i)&&(y+i)<pgs.height&&(x-i)>=0)
			{
				Unit u = new Unit(poU);
				u.setX(x-i);
				u.setY(y+i);
				position.add(u);
				flag=false;	
			}
			i++;
			if(i>pgs.height)
			{
				return null;
			}
		}
		i=0;
		while(i<pgs.height)
		{
			for(int j=0;j<position.size();j++)
			{
				Unit u= position.get(j);
				if(observable(u.getX()-i,u.getY())&&(u.getX()-i)>=0)
				{
					return u;
				}
				if(observable(u.getX(),u.getY()-i)&&(u.getY()-i>=0))
				{
					return u;
				}
				if(observable(u.getX()+i,u.getY())&&(u.getX()+i<pgs.width))
				{
					return u;
				}
				if(observable(u.getX(),u.getY()+i)&&(u.getY()+i)<pgs.height)
				{
					return u; 
				}
			}
			for(int j=0;j<position.size();j++)
			{
				Unit u = position.get(j);
				if(observable(u.getX()-i,u.getY()-i)&&(u.getX()-i)>=0&&(u.getY()-i)>=0)
				{
					return u;
				}
				if(observable(u.getX()-i,u.getY()+i)&&(u.getY()+i)<pgs.height&&(u.getX()-i)>=0)
				{
					return u; 
				}
				if(observable(u.getX()+i,u.getY()+i)&&(u.getX()+i<pgs.width)&&(u.getY()+i<pgs.height))
				{
					return u; 
				}
				if(observable(u.getX()+i,u.getY()-i)&&(u.getY()-i>=0)&&(u.getX()+i)<pgs.width)
				{
					return u;
				}
			}			
			i++;
		}
		return null;
    }
   
//    public Unit reasonPosition(int x,int y ,Unit poU)
//    {//���������ʷ��Ϣ�����ɿ��ܵ���Ϣ��
//    	List<Unit> position = new LinkedList<Unit>();
//    	
//    	return;
//    }
    
    
	public boolean observable(int x, int y) {
    	//���unit�ܹ�������������һ����λ�۲쵽������Ϊ�ǿɹ۵�
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() == player) 
            {
                double d = Math.sqrt((u.getX()-x)*(u.getX()-x) + (u.getY()-y)*(u.getY()-y));
                //���Ӹ�֪���ʣ��ø�˹�ֲ�
//                double probility = 10;
//                double pb =Math.abs(r.nextGaussian());
                if (d<=u.getType().sightRadius) 
                	return true;
            }
        } 
        return false;
    }
    

}
