package ai.ahtn;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import ai.ahtn.domain.Binding;
import ai.ahtn.domain.Clause;
import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.HTNMethod;
import ai.ahtn.domain.HTNPhase;
import ai.ahtn.domain.IntegerConstant;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.Parameter;
import ai.ahtn.domain.PredefinedOperators;
import ai.ahtn.domain.Symbol;
import ai.ahtn.domain.Term;
import ai.ahtn.planner.AdversarialBoundedDepthPlannerAlphaBeta;
import ai.ahtn.planner.AdversarialChoicePoint;
import ai.ahtn.visualization.HTNDomainVisualizer;
import ai.ahtn.visualization.HTNDomainVisualizerVertical;
import ai.core.AI;
import ai.evaluation.EvaluationFunction;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.ResourceUsage;
import rts.UnitAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import util.Pair;
import util.Sampler;

public class EnhanceAHTNAI extends AHTNAI {

	public boolean RandomFlag = false; 
	public MethodDecomposition toExecute = null;
	public static int REPAIRSTRATEGYBYMETHOD = 1;
	public static enum REPAIRSTRATEGY
	{
		FirstStrategy,
		SecondStrategy,	
		ThirdStrategy,
		ForthStrategy,
		FifthStrategy,
		NoRepair
	};
	
	public static enum PartialObserveSTRATEGY
	{
		RemFixUnit,//重新定义
		RemObservedUnit,	//重新观察到的
		Inference,
		Informationset,
		NOspecial,
		Markov
	};
	
	public static REPAIRSTRATEGY currentRS = REPAIRSTRATEGY.FifthStrategy;
	
//	public static PartialObserveSTRATEGY currentPO = PartialObserveSTRATEGY.RemFixUnit;
	public static PartialObserveSTRATEGY currentPO = PartialObserveSTRATEGY.RemFixUnit;
	
	public int repair_stragey = REPAIRSTRATEGYBYMETHOD;
	public List<HTNMethod> failedtasklist;
	PrintStream outfile;
	public EnhanceAHTNAI(String a_domainFileName, int available_time, int max_playouts, int playoutLookahead,
			EvaluationFunction a_ef, AI a_playoutAI) throws Exception {
		super(a_domainFileName, available_time, max_playouts, playoutLookahead, a_ef, a_playoutAI);
		//AIalg = 1;
		// TODO Auto-generated constructor stub
		failedtasklist = new LinkedList<HTNMethod>();
	}
	
	public PlayerAction getAction(int player, GameState gs,boolean partiallyObservable) throws Exception {		
		//处理局部观察问题
		if(partiallyObservable)
		{
		   PartiallyObservableGameState POgs1 = new PartiallyObservableGameState(gs,player,currentPO,this);
		   UpdateGameStateNum(POgs1,gs);
		   PlayerAction pa = GetActionbyState(player,POgs1);
		   for(UnitActionAssignment uaa:POgs1.getUnitActions().values()) 
   		   {
	   			 Unit u = uaa.unit;
	   			 if(u.getPlayer()!=player)
	   				 continue;
	   			 for(Unit gsu:gs.getUnits())
	   			 {
	   				 if(gsu.getID()==u.getID()&&u.getPlayer()==gsu.getPlayer())
	   				 {
	   					 gs.getUnitActions().put(gsu, new UnitActionAssignment(gsu, uaa.action, uaa.time));
							 break;
	   				 }
	   			 }
   		    }
			UpdateGameStateNum(gs,POgs1);
			return pa;
		}
		else
		{
			return GetActionbyState(player,gs);
		}
	}

	public PlayerAction GetActionbyState(int player, GameState gs) throws Exception
	{
		//
		//MK
		PlayerAction pa = new PlayerAction();
		gs.playoutAI = player;
		Term goal1 = Term.fromString("(destroy-player "+player+" "+(1-player)+" "+0+")");
	    Term goal2 = Term.fromString("(destroy-player "+(1-player)+" "+player+" "+0+")");
	    DomainDefinition tempdd = dd;
	    
	    //进行退出条件处理
	    ExitConditionProccess(player,gs);
	    //
	    
		//除掉所有由问题的action,如果涉及多个action，那么就在这里移除，否则在action失败时候就处理了
		HashMap<Unit,UnitActionAssignment> failedaction = new HashMap<Unit,UnitActionAssignment>();
		for(UnitActionAssignment uaa:gs.getUnitActions().values())
		{
			if(uaa.action.canexecuteble)
				continue;
			if(uaa.canremove)
				failedaction.put(uaa.unit, uaa);
		}
		gs.getUnitActions().remove(failedaction);
		if(gs.canExecuteAnyAction(player))
		{
			if(player==0)
                gs.AI1plannum = gs.AI1plannum+1;
             else
            	gs.AI2plannum = gs.AI2plannum+1;
		}
		//修复action
		if(failedtasklist.size()>0)
		{
			switch (currentRS)
			{
			 case FifthStrategy:
			 {
				//第二种是利用之前规划时记录的效用信息
				 long currenttime = System.currentTimeMillis();				 
				 while(!failedtasklist.isEmpty())
				 {
					PlayerAction pa2 = new PlayerAction();
					int i = failedtasklist.size();
					HTNMethod m=failedtasklist.remove(i-1); 						
					pa2 =FifthReplanRepair(player,gs,m,pa2,null);
					gs.issueSafe(pa2);
				 }
				 if(player==0)
				     gs.replantimeplayer0 =gs.replantimeplayer0+System.currentTimeMillis()-currenttime;
				 else
					 gs.replantimeplayer1 =gs.replantimeplayer1+System.currentTimeMillis()-currenttime;
				 if (gs.canExecuteAnyAction(player))
				 {
					 pa = GetActionPlanning(player,gs,goal1,goal2,tempdd,pa).m_a;
					 pa.fillWithNones(gs, player, 10);
					 gs.issueSafe(pa);
				 }
				 return new PlayerAction();
			 }
			 default:
			 {
				 if (gs.canExecuteAnyAction(player))
				 {
					 pa = GetActionPlanning(player,gs,goal1,goal2,tempdd,pa).m_a;
					 pa.fillWithNones(gs, player, 10);
					 gs.issueSafe(pa); 
				 }
				 return new PlayerAction();
			 }
			}
		}
		else
		{
			if (gs.canExecuteAnyAction(player))
			 {
				 pa = GetActionPlanning(player,gs,goal1,goal2,tempdd,pa).m_a;
				 pa.fillWithNones(gs, player, 10);
				 gs.issueSafe(pa); 
			 }
			 return new PlayerAction();
		}
		//		
		 
	}
	//MK
	public void ExitConditionProccess(int player, GameState gs) throws Exception {
		// TODO Auto-generated method stub
		for(UnitActionAssignment uaa:gs.getUnitActions().values())
		{
			if(uaa.unit.getPlayer()!=player)
				continue;
			if(uaa.action==null)
				continue;
			if(uaa.action.Topplan==null)
				continue;
			if(!uaa.action.Topplan.ProccessConditions(player, gs, uaa.action.Topplan))
			{
				uaa.action.canexecuteble = false;
				ProccessFailedAction(uaa.action,gs,player);
			}
		}
	}

	//MK
	public Pair<PlayerAction,MethodDecomposition> GetActionPlanning(int player, GameState gs,Term goal1,Term goal2,DomainDefinition tempdd,PlayerAction pa) throws Exception
	{
		//开始规划	
	    Pair<MethodDecomposition,MethodDecomposition> plan = AdversarialBoundedDepthPlannerAlphaBeta.getBestPlanIterativeDeepening(goal1, goal2, player, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, gs, tempdd, ef, playoutAI,true); 
	    if (plan.m_a!=null) 
	    {
	        	    toExecute = plan.m_a;
	        	    //如果一个实体连续执行两个action，那么第二个action暂时不会被生成operator
	                HTNMethod m =null;
	                constructRelation(toExecute,m);
	                
	                List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>()); 
	                actionsBeingExecuted.clear();

	                while(!l.isEmpty())
	                {
	                    Pair<Integer,List<Term>> tmp = l.remove(0);
	                    if (tmp.m_a!=gs.getTime()) break;
	                    List<Term> actions = tmp.m_b;
	                    for(Term action:actions) 
	                    {
	                        MethodDecomposition md = new MethodDecomposition(action);
	                        //MK
	                        md.operatorownedphase = action.ownedphase;
	                        md.operatorownedphase.plansaved=plan.m_a.plansaved;
	                        md.planlist = toExecute.plansaved.PlanList;
	                        actionsBeingExecuted.add(md);
	                    }
	                }
	            } 
        //MK
        if(plan.m_a==null&&plan.m_b==null&&RandomFlag)
        {
        	//如果当前的规划算法不能提供可以执行的行动方案，那么就存在两种解决思路：当前不采取任何行动，第二种：随机产生一些行为
       	    pa =RandomAction(player,gs,pa);
       	    return new Pair<>(pa,null);
        }
        //
	            List<MethodDecomposition> toDelete = new LinkedList<>();
	            for(MethodDecomposition md:actionsBeingExecuted)
	            {	
	                if (PredefinedOperators.execute(md, gs, pa)) 
	                {
	                	toDelete.add(md);
	                }
	                for(Pair<Unit,UnitAction> ua:pa.getActions()) 
	                {
	                    if (gs.getUnit(ua.m_a.getID())==null) 
	                    {
	                        pa.removeUnitAction(ua.m_a, ua.m_b);
	                    }
	                }
	            }
	            for(Pair<Unit,UnitAction> p:pa.getActions())
	            {
	            	p.m_b.Topplan = plan.m_a;
	            }
	            actionsBeingExecuted.removeAll(toDelete);
	            return new Pair<>(pa,plan.m_a);  
	}
	
	

	public PlayerAction FirstReplanRepair(int player, GameState gs,HTNMethod tmpMethod,PlayerAction pa,MethodDecomposition tempM) throws Exception
	{
		if (!gs.canExecuteAnyAction(player)) 
	   {
			//如果所有的unit都处于忙绿状态，那么就不能进行修复
	       return pa;
	   }
		Term goal1 = tmpMethod.getHead();	
		Term goal2 = Term.fromString("(destroy-player "+(1-player)+" "+player+" "+1+")");
        Pair<MethodDecomposition,MethodDecomposition> plan = AdversarialBoundedDepthPlannerAlphaBeta.getBestPlanIterativeDeepening(goal1, goal2, player, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, gs, dd, ef, playoutAI,false);
        if (plan.m_a!=null) 
        {
           if(tempM!=null&&(plan.m_a.bestEvaluation < tempM.bestEvaluation))
        	{
        	  // outfile.println("No repair"+"\n");
        	   return pa;
        	}
       	    toExecute = plan.m_a;
       	    //如果一个实体连续执行两个acion，那么第二个action暂时不会被生成operator
               HTNMethod m =null;
               constructRelation(toExecute,m);
               List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>()); 
               //actionsBeingExecuted.clear();
               while(!l.isEmpty()) 
               {
                   Pair<Integer,List<Term>> tmp = l.remove(0);
                   if (tmp.m_a!=gs.getTime()) break;
                   List<Term> actions = tmp.m_b;
                   for(Term action:actions) 
                   {
                       MethodDecomposition md = new MethodDecomposition(action);
                       //MK
                       md.operatorownedphase = action.ownedphase;
                       md.planlist = toExecute.plansaved.PlanList;
                       //
                       actionsBeingExecuted.add(md);
                   }
                //outfile.println("repair success"+"\n");
                   //System.out.println("repair success"+"\n");
                int repairnum = getRepairNum()+1;
                setRepairNum(repairnum);
                if(player==0)
                    gs.repairnum1 = getRepairNum();
                 else
              	   gs.repairnum2 = getRepairNum();
               }
               
               List<MethodDecomposition> toDelete = new LinkedList<>();
               for(MethodDecomposition md:actionsBeingExecuted) 
               {
                   if (PredefinedOperators.execute(md, gs, pa))
                   {
                	   toDelete.add(md);
                	}
                   for(Pair<Unit,UnitAction> ua:pa.getActions()) {
                       if (gs.getUnit(ua.m_a.getID())==null) {
                           pa.removeUnitAction(ua.m_a, ua.m_b);
                       }
                   }
               }
               actionsBeingExecuted.removeAll(toDelete);
           }
        else
        {
        	//JudgePhaseFailed(tmpMethod.ownedphase,gs,player);
        	//outfile.println("repair failed"+"\n");
        	// System.out.println("repair failed"+"\n");
        	 int repairnum = getRepairNum()+1;
             setRepairNum(repairnum);
             if(player==0)
             {
            	 gs.repairnum1 = getRepairNum();
            	 gs.repairfailednum1 = gs.repairfailednum1+1;
             }
              else
              {
            	  gs.repairnum2 = getRepairNum();
            	  gs.repairfailednum2 = gs.repairfailednum2+1;
              }
           	  
        }
        return pa;  
	}
	
	public PlayerAction FifthReplanRepair(int player, GameState gs,HTNMethod tmpMethod,PlayerAction pa,MethodDecomposition tempM) throws Exception
	{
		if(tmpMethod.planlist.size()>1)
			tmpMethod.planlist.remove(0);
		while(!tmpMethod.planlist.isEmpty())
		{
			AdversarialChoicePoint caculate = tmpMethod.planlist.remove(0);
			String name = tmpMethod.IDname;
			if(caculate.maxPlanRootRem==null)
				return pa;
			if(caculate.maxPlanRootRem.getMethod()==null)
				return pa;
			if(caculate.maxPlanRootRem.getMethod().IDname.equals(name))
			{
				return pa;
			}
			else
			{
				HTNMethod m =GetFailedMethod(caculate.maxPlanRootRem.getMethod().getDecomposition(),name); 
				boolean flag = false;
				if(m==null)
					continue;
				MethodDecomposition toExecute = m.getDecomposition(); 
				List<Pair<Integer,List<Term>>> l = (toExecute!=null ? toExecute.convertToOperatorList():new LinkedList<>()); 
	               actionsBeingExecuted.clear();
	              while(!l.isEmpty()) 
	               {
	                   Pair<Integer,List<Term>> tmp = l.remove(0);
	                   if (tmp.m_a>gs.getTime()) break;
	                   List<Term> actions = tmp.m_b;
	                   for(Term action:actions) 
	                   {
	                       MethodDecomposition md = new MethodDecomposition(action);
	                       actionsBeingExecuted.add(md);
	                 }
	                   flag = true;
	               }
	               List<MethodDecomposition> toDelete = new LinkedList<>();
	               for(MethodDecomposition md:actionsBeingExecuted) 
	               {
	                   if (PredefinedOperators.execute(md, gs, pa))
	                   {
	                	   toDelete.add(md);
	                	   Term t = md.getUpdatedTerm();
	                       if (t==null) t = md.term;
	                       if(!t.getFunctor().equals("!wait")&&!t.getFunctor().equals("!wait-for-free-unit")&&!t.getFunctor().equals("!fill-with-idles"))
	                	     flag = false;
	                	}
	                   for(Pair<Unit,UnitAction> ua:pa.getActions()) 
	                   {
	                       if (gs.getUnit(ua.m_a.getID())==null) {
	                           pa.removeUnitAction(ua.m_a, ua.m_b);
	                       }
	                   }
	               }
	               actionsBeingExecuted.removeAll(toDelete);
	               if(flag)
	               {
	            	  // outfile.println("repair by plan"+"\n");
	            	   //System.out.println("repair by plan"+"\n");
	            	   int repairnum = getRepairNum()+1;
	                   setRepairNum(repairnum);
	                   if(player==0)
	                      gs.repairnum1 = getRepairNum();
	                   else
	                	   gs.repairnum2 = getRepairNum();
	            	   return pa;
	               }
			}			
		}
		return FirstReplanRepair(player,gs,tmpMethod,pa,tempM);
	}
	
	public HTNMethod GetFailedMethod(MethodDecomposition MD,String name)
	{
		switch(MD.getType())
		{
			case MethodDecomposition.METHOD_METHOD:
			    {
			    	 if(MD.getMethod().IDname.equals(name))
			    		 return MD.getMethod();
			    	 return GetFailedMethod(MD.getMethod().getDecomposition(),name);
			     }
			     case MethodDecomposition.METHOD_PHASE:
			     {
			    	 if(MD.getPhase()!=null)
			    	 {
			    		 return GetFailedMethod(MD.getPhase().getDecomposition(),name);
			    	 }
			    	 break;
			     }
			     case MethodDecomposition.METHOD_SEQUENCE:
			     case MethodDecomposition.METHOD_PARALLEL:
			     {
			    	 MethodDecomposition[] sub = MD.getSubparts();
		             for(int i = 0;i < sub.length;i++) 
		             {
		            	 HTNMethod m= GetFailedMethod(sub[i],name);
		            	 if(m==null)
		            		 continue;
		            	 else
		            		 return m;
		             }
			    	 break;
			     }
			}
	    return null;
	}
	
	public void ProccessFailedAction(UnitAction action,GameState gs,int player)
	{
		HTNPhase tmp = action.ownedphase;
		if(tmp==null)
			return;
		if(repair_stragey==EnhanceAHTNAI.REPAIRSTRATEGYBYMETHOD)
		{
			ProccessFailedPhase(action,tmp,gs,player,action.planlist);
			ProccessFailedMethod(tmp.ownedmethod,tmp,gs,player,action.planlist);
		}
	}
	 public void ProccessFailedPhase(UnitAction action,HTNPhase phase,GameState gs,int player,List<AdversarialChoicePoint> planlist)
	 {
		phase.status = HTNMethod.FAILED;
		if(phase.getDecomposition()==null)
		{
			return;
		}
		MethodDecomposition[] subment = phase.getDecomposition().getSubparts();		
		if(subment==null)
		{
			for(UnitActionAssignment uaa:gs.getUnitActions().values())
			{
					if(uaa.unit.getPlayer()!=player)
						continue;
					if(uaa.action.ownedphase==null)
					{
						   continue;
					}
					if(uaa.action.equals(action))
					{
						if(repair_stragey==EnhanceAHTNAI.REPAIRSTRATEGYBYMETHOD)
						{
							uaa.canremove = true;
						}
						continue;
					}
					if(uaa.action.ownedphase.equals(phase))
					{
						uaa.action.canexecuteble = false;
						uaa.action.causelink.add(action);
						if(repair_stragey==EnhanceAHTNAI.REPAIRSTRATEGYBYMETHOD)
						{
							uaa.canremove = true;
						}
					}
			}
			return;
		}
		for(int i = 0 ; i < subment.length; i++)
		{
			//处理所有的相关任务
			switch(subment[i].getType())
			{
			    case MethodDecomposition.METHOD_OPERATOR:
			    {
					for(UnitActionAssignment uaa:gs.getUnitActions().values())
					{
						if(uaa.unit.getPlayer()!=player)
							continue;
						if(uaa.action.equals(action))
							continue;
						if(uaa.action.ownedphase==null)
							continue;
						else if(uaa.action.ownedphase.equals(phase))
						{
							uaa.action.canexecuteble = false;
							uaa.action.causelink.add(action);
							if(repair_stragey==EnhanceAHTNAI.REPAIRSTRATEGYBYMETHOD)
							{
								uaa.canremove = true;
							}
						}
					}
					break;
			    }
			    case MethodDecomposition.METHOD_METHOD:
			    {
			    	if(subment[i].getMethod().status!=HTNMethod.FAILED&&subment[i].getMethod().status!=HTNMethod.SUCCESS)
			    	    ProccessFailedMethod(subment[i].getMethod(),phase,gs,player,planlist);
			    	break;
			    }	
			    default:
			}
		}
	 }
	 public void ProccessFailedMethod(HTNMethod method,HTNPhase p,GameState gs,int player,List<AdversarialChoicePoint> planlist)
	 {
		 MethodDecomposition[] subment = method.getDecomposition().getSubparts();
		 method.status = HTNMethod.FAILED;
		 if(subment==null)
		 {
			 ProccessFailedTask(method,gs,player,planlist);
			 return ;
		 }
		 boolean flag = true;
		 for(int i = 0; i < subment.length; i ++)
		 {
			 switch(subment[i].getType())
			 {
			   case MethodDecomposition.METHOD_PHASE:
			   {
				   if(subment[i].getPhase()==null)
				   {
					   if(DEBUG>1)
						   System.out.print("there is no action to be instanced for" + subment[i]+"\n");
				   }
				   else if(!subment[i].getPhase().equals(p))
				   {
					   //排在后面没执行的才需要处理
					   ProccessFailedPhase(null,subment[i].getPhase(),gs,player,planlist);
				   }	   
				   break;
			   }
			   default:
			 }
		 }
		 ProccessFailedTask(method,gs,player,planlist);
	 }
	 public void ProccessFailedTask(HTNMethod method,GameState gs,int player,List<AdversarialChoicePoint> planlist)
	 {
		 MethodDecomposition tmpMission = method.ownedmission;
//		 if(!method.essensial)
//			 return;
		 if(tmpMission!=null)
		 {
			 boolean failflag = true;
			 List<HTNMethod> methods = dd.getMethodsForGoal(tmpMission.getTerm().getFunctor());
			 for(int i = 0 ; i < methods.size();i++)
			 {
				 if(methods.get(i).getName().equals(method.getName()))
					 continue;
				 else 
				 {
					 failflag = false;
					 //修要修复的最小单位
					 for(int j=0;j<failedtasklist.size();j++)
					 {
						 if(failedtasklist.get(j).equals(method))
							 return;
					 }
					 method.planlist = planlist;
					 failedtasklist.add(method);
					 return;
				 }	 
			 }
			 if(tmpMission.getTerm().getFunctor().equals("destroy-player"))
			 {
				//这里应该删除掉所有的行动，并且不需要进行修复了
				return; 
			 }
			 if(failflag)  //当前的任务找不到可修复的方法，需要向上继续查找
			 {
				//当前的任务的方法都失败了,说明tmpMission所代表的任务失败了，判断阶段是否失败
				 if(method.essensial)
				   JudgePhaseFailed(method.ownedphase,gs,player,planlist,true);
				 else
				 {
					 JudgePhaseFailed(method.ownedphase,gs,player,planlist,false);
				 }
			 }
		 }
		 //		
	 }
	 
	 public void JudgePhaseFailed(HTNPhase tempPhase,GameState gs,int player,List<AdversarialChoicePoint> planlist,boolean Essentialmission)
	 {
		  boolean failflag = true;
		 //step 1 判断当前的任务是否是关键任务
		 if(Essentialmission)
		 {
			 ProccessFailedPhase(null,tempPhase,gs,player,planlist);
			 ProccessFailedMethod(tempPhase.ownedmethod,tempPhase,gs,player,planlist);
			 return;
		 }
		 //step 2 判断当前的阶段的任务是否都失败了
		 if(tempPhase==null)
			 return;
		 if(tempPhase.getDecomposition()==null)
		{
				return;
		}
		MethodDecomposition[] subment = tempPhase.getDecomposition().getSubparts();
		if(subment==null)
		{
			 ProccessFailedPhase(null,tempPhase,gs,player,planlist);
			 ProccessFailedMethod(tempPhase.ownedmethod,tempPhase,gs,player,planlist);
			return;
		}
		
		for(int i = 0 ; i < subment.length; i++)
		{
			//处理所有的相关任务	
			switch(subment[i].getType())
			{
			    case MethodDecomposition.METHOD_OPERATOR:
			    {
					for(UnitActionAssignment uaa:gs.getUnitActions().values())
					{
						if(uaa.unit.getPlayer()!=player)
							continue;
						else if(uaa.action.canexecuteble)
							failflag = false;
					}
					break;
			    }
			    case MethodDecomposition.METHOD_METHOD:
			    {
			    	if(subment[i].getMethod().status!=HTNMethod.FAILED)
			    		failflag = false;
			    	break;
			    }	
			    default:
			}
			if(!failflag)
				break;
		}
		if(failflag)
		{
		   ProccessFailedPhase(null,tempPhase,gs,player,planlist);
		   ProccessFailedMethod(tempPhase.ownedmethod,tempPhase,gs,player,planlist);
		}
	 }
	 
	 public void ProccessSuccessAction(UnitAction action,GameState gs,int player)
	 {
		 HTNPhase tmp = action.ownedphase;
		 if(tmp==null)
			return;
		 ProccessSuccessPhase(action,tmp,gs,player);
	 }
	 public void ProccessSuccessPhase(UnitAction action,HTNPhase phase,GameState gs,int player)
	 {
		phase.status = HTNMethod.SUCCESS;
		if(phase.getDecomposition()==null)
		{
			return;
		}
		MethodDecomposition[] subment = phase.getDecomposition().getSubparts();		
		if(subment==null)
		{
			for(UnitActionAssignment uaa:gs.getUnitActions().values())
			{
					if(uaa.unit.getPlayer()!=player)
						continue;
					if(uaa.action.ownedphase==null)
					{
							   continue;
					}
					if(uaa.action.equals(action))
					{
						continue;
					}
					if(uaa.action.ownedphase.equals(phase))
					{
						if(uaa.action.status!=1)
							phase.status = HTNMethod.UNUSED;
					}
			}
		}
		else
		{
			for(int i = 0 ; i < subment.length; i++)
			{
				//处理所有的相关任务
				switch(subment[i].getType())
				{
				    case MethodDecomposition.METHOD_OPERATOR:
				    {
						for(UnitActionAssignment uaa:gs.getUnitActions().values())
						{
							if(uaa.unit.getPlayer()!=player)
								continue;
							if(uaa.action.equals(action))
								continue;
							if(uaa.action.ownedphase==null)
								continue;
							else if(uaa.action.ownedphase.equals(phase))
							{
								if(uaa.action.status!=1)
									phase.status = HTNMethod.UNUSED;								
							}
						}
						break;
				    }
				    case MethodDecomposition.METHOD_METHOD:
				    {
				    	if(subment[i].getMethod().status!=HTNMethod.SUCCESS&&subment[i].getMethod().status!=HTNMethod.FAILED)
				    		phase.status = HTNMethod.UNUSED;
				    	break;
				    }	
				    default:
				}
			}
		}
		if(phase.status == HTNMethod.SUCCESS)
		{
			HTNMethod m = phase.ownedmethod;
			MethodDecomposition[] sub = m.getDecomposition().getSubparts();
			if(sub!=null)
			{
				for(int i = 0; i < sub.length; i ++)
				 {
					 switch(sub[i].getType())
					 {
					   case MethodDecomposition.METHOD_PHASE:
					   {
						   if(sub[i].getPhase()==null)
						   {
							   if(DEBUG>1)
								   System.out.print("there is no action to be instanced for" + sub[i]+"\n");
						   }
						   else if(!sub[i].getPhase().equals(phase))
						   {
							   //排在后面没执行的才需要处理
							   if(i==sub.length-1)
							   {
								   m.status = HTNMethod.SUCCESS;
								   ProccessSuccessTask(m,gs,player);
							   }
						   }	   
						   break;
					   }
					   default:
					 }
				 }
			}
		}
	 }
	 
	 public void ProccessSuccessTask(HTNMethod method,GameState gs,int player)
	 {
		 MethodDecomposition tmpMission = method.ownedmission;
		 if(tmpMission!=null)
		 {
		     JudgePhaseSuccess(method.ownedphase,gs,player);
		 }
	 }
	 public void JudgePhaseSuccess(HTNPhase tempPhase,GameState gs,int player)
	 {
		   tempPhase.status = HTNMethod.SUCCESS;
			if(tempPhase.getDecomposition()==null)
			{
				return;
			}
			MethodDecomposition[] subment = tempPhase.getDecomposition().getSubparts();		
			if(subment==null)
			{
				for(UnitActionAssignment uaa:gs.getUnitActions().values())
				{
						if(uaa.unit.getPlayer()!=player)
							continue;
						if(uaa.action.ownedphase==null)
						{
								   continue;
						}
						if(uaa.action.ownedphase.equals(tempPhase))
						{
							if(uaa.action.status!=1)
								tempPhase.status = HTNMethod.UNUSED;
						}
				}
			}
			else
			{
				for(int i = 0 ; i < subment.length; i++)
				{
					//处理所有的相关任务
					switch(subment[i].getType())
					{
					    case MethodDecomposition.METHOD_OPERATOR:
					    {
							for(UnitActionAssignment uaa:gs.getUnitActions().values())
							{
								if(uaa.unit.getPlayer()!=player)
									continue;
								else if(uaa.action.ownedphase.equals(tempPhase))
								{
									if(uaa.action.status!=1)
										tempPhase.status = HTNMethod.UNUSED;								
								}
							}
							break;
					    }
					    case MethodDecomposition.METHOD_METHOD:
					    {
					    	if(subment[i].getMethod().status!=HTNMethod.SUCCESS&&subment[i].getMethod().status!=HTNMethod.FAILED)
					    		tempPhase.status = HTNMethod.UNUSED;
					    	break;
					    }	
					    default:
					}
				}
			}
			if(tempPhase.status == HTNMethod.SUCCESS)
			{
				HTNMethod m = tempPhase.ownedmethod;
				MethodDecomposition[] sub = m.getDecomposition().getSubparts();
				if(sub!=null)
				{
					for(int i = 0; i < subment.length; i ++)
					 {
						 switch(subment[i].getType())
						 {
						   case MethodDecomposition.METHOD_PHASE:
						   {
							   if(subment[i].getPhase()==null)
							   {
								   if(DEBUG>1)
									   System.out.print("there is no action to be instanced for" + subment[i]+"\n");
							   }
							   else if(!subment[i].getPhase().equals(tempPhase))
							   {
								   //排在后面没执行的才需要处理
								   if(i==subment.length-1)
								   {
									   m.status = HTNMethod.SUCCESS;
									   ProccessSuccessTask(m,gs,player);
								   }
							   }	   
							   break;
						   }
						   default:
						 }
					 }
				}
			}
	 }
	 
	//利用差异性随机生成算法
	public PlayerAction RandomAction(int player, GameState gs, PlayerAction pa )
	{
		PhysicalGameState pgs = gs.getPhysicalGameState();
  	    double regularActionWeight = 1;
        double biasedActionWeight = 5;
      	 for(Unit u:pgs.getUnits()) 
      	 {
                 UnitActionAssignment uaa = gs.getActionAssignment(u);
                 if (uaa!=null)
                 {
                     ResourceUsage ru = uaa.action.resourceUsage(u, pgs);
                     pa.getResourceUsage().merge(ru);
                 }
           }
         
	           for(Unit u:pgs.getUnits()) 
	           {
	               if (u.getPlayer()==player) 
	               {
	                   if (gs.getActionAssignment(u)==null) 
	                   {
	                       List<UnitAction> la = u.getUnitActions(gs);
	                       UnitAction none = null;
	                       int nActions = la.size();
	                       double []distribution = new double[nActions];		
	                       int i = 0;
	                       for(UnitAction a:la) 
	                       {
	                           if (a.getType()==UnitAction.TYPE_NONE)
	                        	   none = a;
	                           if (a.getType()==UnitAction.TYPE_ATTACK_LOCATION ||
	                               a.getType()==UnitAction.TYPE_HARVEST ||
	                               a.getType()==UnitAction.TYPE_RETURN) 
	                           {
	                               distribution[i]=biasedActionWeight;
	                           } else {
	                               distribution[i]=regularActionWeight;
	                           }
	                           i++;
	                       }
                         
	                       try {
	                           UnitAction ua = la.get(Sampler.weighted(distribution));
	                           if (ua.resourceUsage(u, pgs).consistentWith(pa.getResourceUsage(), gs)) {
	                               ResourceUsage ru = ua.resourceUsage(u, pgs);
	                               pa.getResourceUsage().merge(ru);                        
	                               pa.addUnitAction(u, ua);
	                           } else {
	                               pa.addUnitAction(u, none);
	                           }
	                       } catch (Exception ex) {
	                           ex.printStackTrace();
	                           pa.addUnitAction(u, none);
	                       }
	                   }
	               }
	           }
	           return pa;
	}
	//MK
	public void constructRelation(MethodDecomposition toExecute, HTNMethod m)
	{
		if(toExecute==null)
			return;
		switch(toExecute.getType())
		{
		     case MethodDecomposition.METHOD_METHOD:
		     {
		    	 HTNPhase phase = toExecute.getPhase(); 
		    	 if(phase==null)
		    	 {
		    		 HTNMethod method = toExecute.getMethod();
		    		 method.ownedmission = toExecute;		    		 
		    		 constructRelation(method.getDecomposition(),method);
		    	 }
		    	 else
		    	     constructRelation(phase.getDecomposition(),phase);
		    	 break;
		     }
		     case MethodDecomposition.METHOD_PHASE:
		     {
		    	 HTNPhase phase = toExecute.getPhase();
		    	 if(phase==null)
		    	 {
		    		 if(DEBUG>1)
		    		 System.out.print("no action in"+toExecute);
		    		 break;
		    	 }
		    	 phase.ownedmethod = m;		    	 
		    	 constructRelation(phase.getDecomposition(),phase);
		    	 break;
		     }
		     case MethodDecomposition.METHOD_SEQUENCE:
		     case MethodDecomposition.METHOD_PARALLEL:
		     {
		    	 MethodDecomposition[] sub = toExecute.getSubparts();
	             for(int i = 0;i < sub.length;i++) 
	             {
	            	 constructRelation(sub[i],m);
	             }
		    	 break;
		     }
		     default:
		}
	}
	
	public void constructRelation(MethodDecomposition toExecute, HTNPhase p)
	{
		switch(toExecute.getType())
		{
		     case MethodDecomposition.METHOD_METHOD:
		     {
		    	 HTNMethod submethod = toExecute.getMethod();
		    	 submethod.ownedmission = toExecute;
		    	 submethod.ownedphase = p;
		    	 constructRelation(submethod.getDecomposition(),submethod);
		    	 break;
		     }
		     case MethodDecomposition.METHOD_OPERATOR:
		     {
		    	 toExecute.getUpdatedTerm().ownedphase = p; 
		    	 break;
		     }
		     case MethodDecomposition.METHOD_SEQUENCE:
		     case MethodDecomposition.METHOD_PARALLEL:
		     {
		    	 MethodDecomposition[] sub = toExecute.getSubparts();
	             for(int i = 0;i < sub.length;i++) 
	             {
	            	 constructRelation(sub[i],p);
	             }
		    	 break;
		     }
		    default:
		}
	}
	//
	public void reset() 
	{
	       super.reset();
	       this.failedtasklist.clear();
	       this.toExecute = null;
	       this.setRepairNum(0);
	}
	 public AI clone() {
	        try {
	            return new EnhanceAHTNAI(domainFileName, MAX_TIME, MAX_ITERATIONS, PLAYOUT_LOOKAHEAD, ef, playoutAI);
	        }catch(Exception e) {
	            e.printStackTrace();
	            return null;
	        }
	    }
	 public String toString() {
	        return "EnhanceAHTNAI:"+ domainFileName;   //(" + domainFileName + "," + MAX_TIME + ")
	    }
	 
}
