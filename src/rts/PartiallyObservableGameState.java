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
    //感知中是不考虑历史信息的影响的，例如一个实体观察不到时，立即删除，而不考虑可能只是隐藏
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
        for(Unit u:pgs.getUnits()) //判断敌方的Unit是否可观察。
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
        	removeUnit(u);   //凡是不在己方感知范围内的unit，都从自己的gamestate中移除
        }  
        
        //形成的UnitList是当前感知到的单位的列表
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
        	removeUnit(u);   //凡是不在己方感知范围内的unit，都从自己的gamestate中移除
        }
        
        
        UpdatePOGameStatebyHistory(gs,a_player,currentPO,ai);
        //下次用的第二次的历史记录
        //对本次历史信息进行更新，本次推理的信息，加入到历史信息集中
        
        
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
//    		1、	记录所有固定位置的unit
//    		2、	保留两次历史观察记录
//    		3、	从两次观察记录中删除固定的unit
//    		4、	从两次观察记录中删除死亡的unit
//    		5、	从历史观察集中除去本次观察集中已经存在的unit
//    		6、	历史记录中只保留真实看到的，不保留推测的实体
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
    		//观测与第一次历史信息记录一致，在第一次中去掉
    		
    		
    		
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
    		//观测与第二次历史信息记录一致，在第二次中去掉
    		
    		
    		for(Unit poU:ai.FirstUnitHistory)
    		{
    			if (observable(poU.getX(),poU.getY()))//如果目前可观
    			{
    				//最近的不可观察的位置
    				Unit u=UnObservablePosition(poU.getX(),poU.getY(),poU);
    				if(u==null)
    				{
    					toDelete.add(poU);
    					continue;
    				}
    				u.IsInferenced = true;
    				pgs.units.add(u);
    			}
    			else//如果目前不可观
    			{
    				for(Unit tu:ai.SecondUnitHistory)
    				{
    					if(poU.getID()==tu.getID()&&poU.getClass()==tu.getClass())
    					{
    						if(poU.getX()==tu.getX()&&poU.getY()==tu.getY())//两次历史位置相同
    						{
    							Unit u = new Unit(poU);
        						pgs.addUnit(u);
        						break;
    						}
    						else
    						{
    							//位置不同，说明实体可能已经动了
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
    				//如果上上次不可观
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

//    		1、记录所有固定位置的Unit
//    		2、将现在的观测值加入到观测集中
//    		3、对观测集进行调整，形成各单位可能的轨迹集
//    		4、形成belief_states,生成若干可能information set
//    		5、在其中选择合适的
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
    							//位置不同，说明实体可能已经动了
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
    				//如果上上次不可观
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
    		//1.将本次观察到的加入到观察值中
    		//2.将上次观察到本次未观察到的进行预估
    		//3.
    		
    		    			
    		//////////////////对第一次以及第二次进行更新，如果没有看到
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
    		}//////所有存在的单位
    		unitlist.removeAll(toDelete);
    		toDelete.clear();
    		
    		List<Unit> toReason = new LinkedList<Unit>();
    		toReason=unitlist;
    		for(Unit poU:toReason)
    		{
    			if (observable(poU.getX(),poU.getY()))
    			    toDelete.add(poU); 			
    		}//需要推理的单位
    		
    		toReason.removeAll(toDelete);
    		
    		System.out.print(unitlist);
    		System.out.print(toReason);
    		
    		//记录下需要推理的单位的历史位置    		
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
    							//位置不同，说明实体可能已经动了
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
    				//如果上上次不可观
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
    		
    		
//    			else//历史观察但现在不可观的
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
//    							//位置不同，说明实体可能已经动了
////    							Unit u=minunObservablePosition(poU.getX(),poU.getY(),poU);//如果实体位置可观，找一个最近的不可观测位置
//    							Unit u=reasonPosition(poU.getX(),poU.getY(),poU);//粒子滤波的方法
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
//    				//如果上上次不可观
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
//    		//如果是马尔科夫推理，则假定单位均向其基地移动
//    		//1。判断上次还在这次不在的Unit
//    		//2.判断其可以到达的位置
//    		//3.在其可以到达的位置使用马尔科夫方法进行判断，使用概率选择位置（随机选择位置）
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
    	//如果实体位置可观，找一个最近的不可观测位置
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
    	//如果实体位置可观，找一个最近的不可观测位置
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
    	//如果实体位置可观，找一个最近的不可观测位置
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
//    {//结合两次历史信息，生成可能的信息，
//    	List<Unit> position = new LinkedList<Unit>();
//    	
//    	return;
//    }
    
    
	public boolean observable(int x, int y) {
    	//如果unit能够被己方的任意一个单位观察到，就认为是可观的
        for(Unit u:pgs.getUnits()) {
            if (u.getPlayer() == player) 
            {
                double d = Math.sqrt((u.getX()-x)*(u.getX()-x) + (u.getY()-y)*(u.getY()-y));
                //增加感知概率，用高斯分布
//                double probility = 10;
//                double pb =Math.abs(r.nextGaussian());
                if (d<=u.getType().sightRadius) 
                	return true;
            }
        } 
        return false;
    }
    

}
