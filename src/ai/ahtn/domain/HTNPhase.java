package ai.ahtn.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ai.ahtn.domain.LispParser.LispElement;
import ai.ahtn.planner.AdversarialChoicePoint;
import rts.GameState;
import rts.UnitAction;
import rts.units.Unit;

public class HTNPhase extends HTNMethod {
	
	MethodDecomposition phase;
	//MK
	public HTNMethod ownedmethod = null;
	public AdversarialChoicePoint plansaved = null;
	//
	public HTNPhase(String n, Term h, MethodDecomposition m)
	{
		super(n,h,null,true);
		phase = m;
	}
	
	public MethodDecomposition getDecomposition() {
        return phase;
    }
    
    public HTNPhase clone() {
    	HTNPhase c = new HTNPhase(name, head.clone(), phase.clone());
        return c;
    }
    
    public HTNPhase cloneTrackingDescendants(MethodDecomposition descendantsToTrack[], MethodDecomposition newDescendants[]) {
    	HTNPhase c = new HTNPhase(name, head.clone(), phase.cloneTrackingDescendants(descendantsToTrack,newDescendants));
        return c;
    }
        
    
    public void renameVariables(int renamingIndex) {
        head.renameVariables(renamingIndex);
        phase.renameVariables(renamingIndex);
    }
    
    public void applyBindings(List<Binding> l) throws Exception {
        head.applyBindings(l);
        phase.applyBindings(l);
    }

    public static HTNPhase fromLispElement(LispElement e) throws Exception {
        LispElement name_e = e.children.get(1);
        LispElement head_e = e.children.get(2);
        LispElement phase_e = e.children.get(3);
        
        String name = name_e.element;
        Term head = Term.fromLispElement(head_e);
        MethodDecomposition p = MethodDecomposition.fromLispElement(phase_e);
        
        return new HTNPhase(name,head,p);
    }
    
    
    public void replaceSingletonsByWildcards() throws Exception {
        List<Symbol> singletons = findSingletons();
        head.replaceSingletonsByWildcards(singletons);
        phase.replaceSingletonsByWildcards(singletons);
    }
    
    public List<Symbol> findSingletons() throws Exception {
        HashMap<Symbol,Integer> appearances = new HashMap<>();
        countVariableAppearances(appearances);
        
        List<Symbol> l = new ArrayList<>();
        for(Symbol v:appearances.keySet()) {
            if (appearances.get(v)==1) l.add(v);
        }
        return l;
    }
    
    
    public void countVariableAppearances(HashMap<Symbol,Integer> appearances) throws Exception {
        head.countVariableAppearances(appearances);
        phase.countVariableAppearances(appearances);
    }

    
    public String toString() {
        return "phase("+name+"): " + head + ", decomposition: " + phase;
    }    
    
    
    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints) {
        return phase.executionCycle(gs, actions, choicePoints);
    }

    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints, AdversarialChoicePoint previous_cp,List<Binding> bindings) {
        return phase.executionCycle(gs, actions, choicePoints, previous_cp,bindings);
    }

	public boolean ProccessConditions(int player, GameState gs,MethodDecomposition toExcute) throws Exception {
		// TODO Auto-generated method stub
		HTNPhase currentphase = toExcute.getPhase();
		MethodDecomposition[] submint = currentphase.getDecomposition().getSubparts();
		if(submint==null)
		{
			HTNMethod m = currentphase.getDecomposition().getMethod();
			if(m==null)
				return true;
			MethodDecomposition tmp = m.getDecomposition();
			if(!tmp.ProccessConditions(player, gs, tmp))
				return false;
			return true;
		}
		for(int i=0;i<submint.length;i++)
		{
			switch(submint[i].getType())
			{
			  case MethodDecomposition.METHOD_CONDITION:
			  case MethodDecomposition.METHOD_NON_BRANCHING_CONDITION:
			  {
				  break;
			  }
			  case MethodDecomposition.METHOD_necessary:
			  {
				  break;
			  }
			  case MethodDecomposition.METHOD_sufficient:
			  {
				  if(check(submint[i],gs))
				  {
					  if(!checkphase(player,gs,currentphase))
						  return false;
				  }
				  return true;
			  }
			  case MethodDecomposition.METHOD_METHOD:			  
			  case MethodDecomposition.METHOD_SEQUENCE:
			  case MethodDecomposition.METHOD_PARALLEL:
			  {
				  if(!submint[i].ProccessConditions(player, gs, submint[i]))
					  return false;
			  }
			}
		}
		return true;
	}

	public boolean checkphase(int player, GameState gs,HTNPhase currentphase) throws Exception {
		// TODO Auto-generated method stub
		MethodDecomposition[] submint = currentphase.getDecomposition().getSubparts();
		int flag = -1;
		for(int i=0;i<submint.length;i++)
		{
			switch(submint[i].getType())
			{
			  case MethodDecomposition.METHOD_PARALLEL:
			  {
				  for(int j = 0;j<submint[i].subelements.length;j++)
				  {
					  MethodDecomposition toExcute =submint[i].subelements[j];
					  boolean suc = toExcute.ProccessConditions(player, gs, toExcute);
					  if(toExcute.getMethod()!=null)
					  {
						  if(suc)
							  flag = 1;
						  if(suc!=false&&toExcute.getMethod().essensial==true)
							  return false;
						  if(suc!=false&&toExcute.getMethod().essensial==false)
						  {
							  if(flag!=1)
							      flag = 2;
						  }  
					  }
					  else
					  {
						  Parameter p[] = submint[i].subelements[j].getUpdatedTerm().getParameter();
						  int type =  ((IntegerConstant)p[p.length-1]).value;	
						  if(suc)
							  flag = 1;
						  if(suc!=false&&type==0)
							  return false;
						  if(suc!=false&&type==1)
						  {
							  if(flag!=1)
							      flag = 2;
						  }  
					  }
				  }
				  break;
			  }
			  case MethodDecomposition.METHOD_OPERATOR:
			  {
				  Parameter p[] = submint[i].getUpdatedTerm().getParameter();
				  int type =  ((IntegerConstant)p[p.length-1]).value;		
				  if(submint[i].getUpdatedTerm().status==Term.SUCCESS)
					  flag =1;
				  if(submint[i].getUpdatedTerm().status!=Term.SUCCESS&&type==0)
				  {
					  return false;
				  }
				  if(submint[i].getUpdatedTerm().status!=Term.SUCCESS&&type==1)
				  {
					  if(flag!=1)
					      flag = 2;
				  }
				  break;
			  }
			  case MethodDecomposition.METHOD_METHOD:
			  {
				  if(submint[i].getMethod().status==HTNMethod.SUCCESS)
					  flag =1;
				  if(submint[i].getMethod().status!=HTNMethod.SUCCESS&&submint[i].getMethod().essensial==true)
					  return false;
				  if(submint[i].getMethod().status!=HTNMethod.SUCCESS&&submint[i].getMethod().essensial==false)
				  {
					  if(flag!=1)
					      flag = 2;
				  }
				  break;
			  }
			}
		}
		if(flag==2)
			return false;
		return true;
	}

	public boolean check(MethodDecomposition md, GameState gs) throws Exception {
		// TODO Auto-generated method stub\
		//
		//时间有限，只针对一种条件
		 Parameter p1 = md.clause.term.parameters[0];
         Parameter p2 = md.clause.term.parameters[1];
         if (p1 instanceof IntegerConstant) 
         {
             Unit u1 = gs.getUnit(((IntegerConstant)p1).value);
             if(u1==null||u1.getType().name!="Base")
             {
           	    return false;
             }
             if (p2 instanceof IntegerConstant) {
                 int d = ((IntegerConstant)p2).value;
                 int posx = u1.getX() + UnitAction.DIRECTION_OFFSET_X[d];
                 int posy = u1.getY() + UnitAction.DIRECTION_OFFSET_Y[d];
                 if (posx<0 || posx>=gs.getPhysicalGameState().getWidth() ||
                     posy<0 || posy>=gs.getPhysicalGameState().getHeight() ||
                     !gs.free2(posx, posy,u1)) 
               	  return true;
             } 
         }  
         return false;
	}
}
