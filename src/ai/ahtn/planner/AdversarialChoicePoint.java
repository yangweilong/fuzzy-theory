/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.planner;

import ai.ahtn.domain.Binding;
import ai.ahtn.domain.Clause;
import ai.ahtn.domain.DomainDefinition;
import ai.ahtn.domain.HTNMethod;
import ai.ahtn.domain.HTNPhase;
import ai.ahtn.domain.IntegerConstant;
import ai.ahtn.domain.MethodDecomposition;
import ai.ahtn.domain.Parameter;
import ai.ahtn.domain.Variable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */

// ����ѡ��������method ��������
public class AdversarialChoicePoint
{
    public static int DEBUG = 0;
    
    // these are the root nodes:
    public MethodDecomposition maxPlanRoot;
    public MethodDecomposition minPlanRoot;
    
    // these are the choicePoint nodes (should be descendants of the root nodes, or null):
    public MethodDecomposition choicePointPlayerMax;
    public MethodDecomposition choicePointPlayerMin;
    public GameState gs;
    int lastTimeOperatorsIssued = -1;   
    int operatorDepth = 0;      // this is to keep track of the depth of the search tree
                                // keeps track of the number of instants when operators where
                                // executed (if only one operator is executed at a time, this
                                // is equivalent to the number of operators executed so far)
    
    // Method: variables to continue finding expansions after the first:
    List<HTNMethod> possibleMethods = null;
    //MK
    List<HTNPhase> possiblePhases = null;
    public DomainDefinition dd = null;
    public DomainDefinition ddenemy = null;
    public List<AdversarialChoicePoint> PlanList = new LinkedList<>();
    public List<AdversarialChoicePoint> subList = new LinkedList<>();
    public AdversarialChoicePoint parantPoint = null;
    
    public MethodDecomposition maxPlanRootRem;
    public MethodDecomposition minPlanRootRem;
    //
    
    // Condition: variables to continue finding expansions after the first:
    Clause updatedClause = null;
    boolean updatedClauseHadAnyMatches = false;
    List<Binding> lastBindings = null;  // If the bindings found for a next match are the same as
                                        // the previous one, the new match is ignored (to reduce
                                        // useless branching)
    
    // Variables to restore the execution point of the plan after backtracking:
    HashMap<MethodDecomposition, MethodDecompositionState> executionState = null;
    
    // evaluation function:
    public int minimaxType = -1;   // 0: max, 1: min, -1: not yet set.
    public float bestEvaluation = -10000;
    public MethodDecomposition bestMaxPlan = null;
    public MethodDecomposition bestMinPlan = null;
    float alpha = 0;
    float beta = 0;

    
    private AdversarialChoicePoint() {
        executionState = new HashMap<>();
    }
    
    
    public AdversarialChoicePoint(MethodDecomposition cpMax, MethodDecomposition cpMin, 
                                  MethodDecomposition rootMax, MethodDecomposition rootMin, GameState a_gs, int nops, int ltoi) {
        maxPlanRoot = rootMax;
        minPlanRoot = rootMin;
        choicePointPlayerMax = cpMax;
        choicePointPlayerMin = cpMin;
        gs = a_gs;
        operatorDepth = nops;
        lastTimeOperatorsIssued = ltoi;
        
        executionState = new HashMap<>();
        captureExecutionState(rootMax);
        captureExecutionState(rootMin);      
        
        //mk
        maxPlanRootRem = maxPlanRoot.clone();
        minPlanRootRem = minPlanRoot.clone();
    }
    
    public AdversarialChoicePoint(MethodDecomposition cpMax, MethodDecomposition cpMin, 
                                  MethodDecomposition rootMax, MethodDecomposition rootMin, GameState a_gs, int nops, int ltoi,
                                  float a, float b, boolean saveExecutionState) {
        maxPlanRoot = rootMax;
        minPlanRoot = rootMin;
        choicePointPlayerMax = cpMax;
        choicePointPlayerMin = cpMin;
        gs = a_gs;
        operatorDepth = nops;
        lastTimeOperatorsIssued = ltoi;
        alpha = a;
        beta = b;
        
//        System.out.println("new: " + alpha + " , " + beta);
        
        executionState = new HashMap<>();
        if (saveExecutionState) {
            captureExecutionState(rootMax);
            captureExecutionState(rootMin);
        } else {
            captureExecutionStateNonRecursive(rootMax);
            captureExecutionStateNonRecursive(rootMin);
            
        }
        
        //mk
        maxPlanRootRem = maxPlanRoot.clone();
        minPlanRootRem = minPlanRoot.clone();
    }    
    
    //MK
    public AdversarialChoicePoint clone()
    {
    	AdversarialChoicePoint acp = new AdversarialChoicePoint();
    	acp.bestEvaluation = bestEvaluation;
    	acp.maxPlanRoot = maxPlanRoot;
    	acp.minPlanRoot = minPlanRoot;
        acp.lastBindings = lastBindings;
        acp.minimaxType = minimaxType;
    	return acp;
    }
    
    public AdversarialChoicePoint cloneForMCTS() {
        AdversarialChoicePoint acp = new AdversarialChoicePoint();
        
        MethodDecomposition []in1 = {choicePointPlayerMax};
        MethodDecomposition []out1 = {null};        
        acp.maxPlanRoot = maxPlanRoot.cloneTrackingDescendants(in1,out1);

        MethodDecomposition []in2 = {choicePointPlayerMin};
        MethodDecomposition []out2 = {null};        
        acp.minPlanRoot = minPlanRoot.cloneTrackingDescendants(in2,out2);
        
        acp.choicePointPlayerMax = out1[0];
        acp.choicePointPlayerMin = out2[0];
        acp.gs = gs.clone();
        acp.lastTimeOperatorsIssued = lastTimeOperatorsIssued;
        acp.operatorDepth = operatorDepth;
        
        /*    
        // no need to clone these ones for the purposes of MCTS:
        List<HTNMethod> possibleMethods = null;    
        Clause updatedClause = null;
        boolean updatedClauseHadAnyMatches = false;
        List<Binding> lastBindings = null;
        HashMap<MethodDecomposition, MethodDecompositionState> executionState = null;
        public int minimaxType = -1;
        public float bestEvaluation = 0;
        public MethodDecomposition bestMaxPlan = null;
        public MethodDecomposition bestMinPlan = null;
        float alpha = 0;
        float beta = 0;
        */
        
        return acp;
    }
    
    
    public float getAlpha() {
        return alpha;
    }
    
    public float getBeta() {
        return beta;
    }
            
    public void setAlpha(float a) {
        alpha = a;
    }
    
    public void setBeta(float b) {
        beta = b;
    }
    
    public int getOperatorDepth() {
        return operatorDepth;
    }
    
    public int getLastTimeOperatorsIssued() {
        return lastTimeOperatorsIssued;
    }
    
    public void setLastTimeOperatorsIssued(int ltoi) {
        lastTimeOperatorsIssued = ltoi;
    }
    
    public float getBestEvaluation() {
        return bestEvaluation;
    }
    
    public void setBestEvaluation(float be) {
        bestEvaluation = be;
    }
    
    public int getMiniMaxType() {
        return minimaxType;
    }
    
    public GameState getGameState() {
        return gs;
    }
    
    public void captureExecutionState(MethodDecomposition md) {
        executionState.put(md, new MethodDecompositionState(md));
        if (md.getSubparts()!=null) {
            for(int i = 0;i<md.getSubparts().length;i++) {
                captureExecutionState(md.getSubparts()[i]);
            }
        }
        if (md.getMethod()!=null) {
            captureExecutionState(md.getMethod().getDecomposition());
        }
    }
    
    
    public void captureExecutionStateNonRecursive(MethodDecomposition md) {
        if (executionState.get(md)==null)
            executionState.put(md, new MethodDecompositionState(md));
    }    

    
    public void restoreExecutionState() {
        for(MethodDecomposition md:executionState.keySet()) {
            executionState.get(md).restoreState(md);
        }
    }
    
    
    public void restoreAfterPop() {
        if (choicePointPlayerMax!=null && choicePointPlayerMax.getType() == MethodDecomposition.METHOD_METHOD) 
        {
            choicePointPlayerMax.setMethod(null);
        }
        if (choicePointPlayerMin!=null && choicePointPlayerMin.getType() == MethodDecomposition.METHOD_METHOD) 
        {
            choicePointPlayerMin.setMethod(null);            
        }
        //MK
        if (choicePointPlayerMax!=null && choicePointPlayerMax.getType() == MethodDecomposition.METHOD_PHASE) {
            choicePointPlayerMax.setPhase(null);
        }
        if (choicePointPlayerMin!=null && choicePointPlayerMin.getType() == MethodDecomposition.METHOD_PHASE) {
            choicePointPlayerMin.setPhase(null);            
        }
        //
    }
    
    // "previous_cp" is usually == this, and it is the choice point where the state should be saved for restoring it later when backtracking
    public boolean nextExpansion(DomainDefinition dd, List<Binding> bindings, int renamingIndex, AdversarialChoicePoint previous_cp) throws Exception {
        if (choicePointPlayerMax!=null) {
            previous_cp.captureExecutionStateNonRecursive(choicePointPlayerMax);
            if (choicePointPlayerMax.getType() == MethodDecomposition.METHOD_METHOD)
            {
                return nextMethodExpansion(choicePointPlayerMax, gs, dd, bindings, renamingIndex);
            } else if (choicePointPlayerMax.getType() == MethodDecomposition.METHOD_CONDITION ||
                       choicePointPlayerMax.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION||
               		   choicePointPlayerMax.getType()==MethodDecomposition.METHOD_necessary) {
                return nextConditionExpansion(choicePointPlayerMax, bindings);
            }
            //MK
            else if(choicePointPlayerMax.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	 return nextPhaseExpansion(choicePointPlayerMax, gs, dd, bindings, renamingIndex);
            }
            else if(choicePointPlayerMax.getType()==MethodDecomposition.METHOD_sufficient)
            {
            	nextConditionExpansion(choicePointPlayerMax, bindings);
            	return true;
            }
           //
            else {
                throw new Exception("Wrong MethodDecomposition in choicePoint!");
            }
        } else {
            previous_cp.captureExecutionStateNonRecursive(choicePointPlayerMin);
            if (choicePointPlayerMin.getType() == MethodDecomposition.METHOD_METHOD) {
                return nextMethodExpansion(choicePointPlayerMin, gs, dd, bindings, renamingIndex);
            } else if (choicePointPlayerMin.getType() == MethodDecomposition.METHOD_CONDITION ||
                       choicePointPlayerMin.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION||
               		   choicePointPlayerMin.getType()==MethodDecomposition.METHOD_necessary) {
                return nextConditionExpansion(choicePointPlayerMin, bindings);
            }
            //MK
            else if(choicePointPlayerMin.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	 return nextPhaseExpansion(choicePointPlayerMin, gs, dd, bindings, renamingIndex);
            }
            else if(choicePointPlayerMin.getType()==MethodDecomposition.METHOD_sufficient)
            {
            	nextConditionExpansion(choicePointPlayerMin, bindings);
            	return true;
            }
           //
            else {
                throw new Exception("Wrong MethodDecomposition in choicePoint!");
            }
        }
    }
    
    public boolean nextExpansionTest(DomainDefinition dd, DomainDefinition ddenemy,List<Binding> bindings, int renamingIndex, AdversarialChoicePoint previous_cp) throws Exception {
        if (choicePointPlayerMax!=null) {
            previous_cp.captureExecutionStateNonRecursive(choicePointPlayerMax);
            if (choicePointPlayerMax.getType() == MethodDecomposition.METHOD_METHOD)
            {
                return nextMethodExpansion(choicePointPlayerMax, gs, dd, bindings, renamingIndex);
            } else if (choicePointPlayerMax.getType() == MethodDecomposition.METHOD_CONDITION ||
                       choicePointPlayerMax.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION||
               		   choicePointPlayerMax.getType()==MethodDecomposition.METHOD_necessary) {
                return nextConditionExpansion(choicePointPlayerMax, bindings);
            }
            //MK
            else if(choicePointPlayerMax.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	 return nextPhaseExpansion(choicePointPlayerMax, gs, dd, bindings, renamingIndex);
            }
            else if(choicePointPlayerMax.getType()==MethodDecomposition.METHOD_sufficient)
            {
            	nextConditionExpansion(choicePointPlayerMax, bindings);
            	return true;
            }
           //
            else {
                throw new Exception("Wrong MethodDecomposition in choicePoint!");
            }
        } else {
            previous_cp.captureExecutionStateNonRecursive(choicePointPlayerMin);
            if (choicePointPlayerMin.getType() == MethodDecomposition.METHOD_METHOD) {
                return nextMethodExpansion(choicePointPlayerMin, gs, ddenemy, bindings, renamingIndex);
            } else if (choicePointPlayerMin.getType() == MethodDecomposition.METHOD_CONDITION ||
                       choicePointPlayerMin.getType() == MethodDecomposition.METHOD_NON_BRANCHING_CONDITION||
               		   choicePointPlayerMin.getType()==MethodDecomposition.METHOD_necessary) {
                return nextConditionExpansion(choicePointPlayerMin, bindings);
            }
            //MK
            else if(choicePointPlayerMin.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	 return nextPhaseExpansion(choicePointPlayerMin, gs, ddenemy, bindings, renamingIndex);
            }
            else if(choicePointPlayerMin.getType()==MethodDecomposition.METHOD_sufficient)
            {
            	nextConditionExpansion(choicePointPlayerMin, bindings);
            	return true;
            }
           //
            else {
                throw new Exception("Wrong MethodDecomposition in choicePoint!");
            }
        }
    }
    
    private boolean nextConditionExpansion(MethodDecomposition choicePoint, List<Binding> bindings) throws Exception {
        if (DEBUG>=1) System.out.println("AdversarialChoicePoint.nextExpansionCondition: testing " + choicePoint.getClause());

        if (updatedClause==null) {
            updatedClause = choicePoint.getClause().clone();
            updatedClause.applyBindings(bindings);
            updatedClauseHadAnyMatches = false;
            lastBindings = null;
        }
              	
        while(true) {
//            System.out.println("  Condition: " + updatedClause);
//            System.out.println("    " + bindings);
            List<Binding> l = updatedClause.nextMatch(gs);
            if (DEBUG>=1) System.out.println("AdversarialChoicePoint.nextConditionExpansion: bindings: " + l);
            if (l==null) {
                if (updatedClauseHadAnyMatches) {
                    updatedClause = null;
                    return false;
                } else {
                    choicePoint.setExecutionState(2); 
                    updatedClauseHadAnyMatches = true;
                    return true;
                }
            } else {
                if (lastBindings!=null && l.equals(lastBindings)) {
//                    System.out.println(this.hashCode() + " - !!! " + updatedClause + "\n    " + lastBindings + " ==\n    " + l);
                    continue;
                } else {
//                    System.out.println(this.hashCode() + " - ok " + l);
                }
                lastBindings = new ArrayList<>();
                lastBindings.addAll(l);

                updatedClauseHadAnyMatches = true;
                choicePoint.setExecutionState(1);
                for(Binding b:l) bindings.add(0,b);
                return true;
            }                
        }
    }

    public boolean nextMethodExpansion(MethodDecomposition choicePoint, GameState gs, DomainDefinition dd, List<Binding> bindings, int renamingIndex) throws Exception {
        if (DEBUG>=1) System.out.println("AdversarialChoicePoint.nextMethodExpansion: testing " + choicePoint.getTerm());
        if (possibleMethods==null)
        {
            List<HTNMethod> methods = dd.getMethodsForGoal(choicePoint.getTerm().getFunctor());
            if (methods==null)
            	   throw new Exception("No methods for: " + choicePoint.getTerm().getFunctor());
                possibleMethods = new ArrayList<>();
                possibleMethods.addAll(methods);
            if (DEBUG>=1) System.out.println("AdversarialChoicePoint.nextMethodExpansion: Goal: " + choicePoint.getTerm());        
            if (DEBUG>=1) System.out.println("AdversarialChoicePoint.nextMethodExpansion: Methods found: " + methods.size());
        }
        
        // if no more methods, reset, and return false:
        if (possibleMethods.isEmpty()) {
            possibleMethods = null;
            choicePoint.setMethod(null);
            return false;
        }
        
        // otherwise, nextExpansion:
        HTNMethod m = possibleMethods.remove(0);  
        // clone the method (this clones the method, and applies any required variable substitution,
        setMethod(choicePoint, m, gs, bindings, renamingIndex);      
        
        return true;
    } 
    
    //MK
    public boolean nextPhaseExpansion(MethodDecomposition choicePoint, GameState gs, DomainDefinition dd, List<Binding> bindings, int renamingIndex) throws Exception {
          	
        if (possiblePhases==null)
        {
            //List<HTNPhase> phases = dd.getPhasesForGoal(choicePoint.getTerm().getFunctor());
            List<HTNPhase> phases = dd.getSomePhases(choicePoint.getTerm().getFunctor());
            if (phases==null) 
            	   throw new Exception("No phase for: " + choicePoint.getTerm().getFunctor());
                possiblePhases = new ArrayList<>();
                possiblePhases.addAll(phases);
        }
        
        // if no more methods, reset, and return false:
        if (possiblePhases.isEmpty()) {
        	possiblePhases = null;
            choicePoint.setPhase(null);
            return false;
        }
        
        // otherwise, nextExpansion:
        HTNPhase p = possiblePhases.remove(0);
        
        // clone the method (this clones the method, and applies any required variable substitution,
        setPhase(choicePoint, p, gs, bindings, renamingIndex);      
        
        return true;
    } 
    //
    public void setMethod(MethodDecomposition choicePoint, HTNMethod original, GameState gs, List<Binding> bindings, int renamingIndex) throws Exception {        
        // 1. Clone method:
        HTNMethod m = original.clone();
        choicePoint.setMethod(m);
        
        // 2. Find variable bindings:���������������Ĳ���ֵ���а�
        List<Binding> bindings2 = m.getHead().simpleUnificationDestructiveNoSharedVariables(choicePoint.getTerm(), gs);
        if (DEBUG>=1) System.out.println("AdversarialChoicePoint.setMethod: bindings2 " + bindings2);
        
        // 3. Apply bindings to the rest of the method:
        m.applyBindings(bindings2);
        m.applyBindings(bindings);
        m.renameVariables(renamingIndex);
        
        Parameter[] p = m.getHead().getParameter();
//        System.out.println(m);
        if(((IntegerConstant)p[m.getHead().getParameter().length-1]).value==0)
        	m.essensial = false;
 
    }
    
    //MK
    public void setPhase(MethodDecomposition choicePoint, HTNPhase original, GameState gs, List<Binding> bindings, int renamingIndex) throws Exception {        
        // 1. Clone method:
        HTNPhase m = original.clone();
        choicePoint.setPhase(m);
       
        // 2. Find variable bindings:���������������Ĳ���ֵ���а�
        List<Binding> bindings2 = m.getHead().simpleUnificationDestructiveNoSharedVariablesPhase(choicePoint.getTerm(), gs);
        
        // 3. Apply bindings to the rest of the phase:
        m.applyBindings(bindings2);
        m.applyBindings(bindings);
        m.renameVariables(renamingIndex);
    }
    //
    
    // returns "true" if the alpha-beta test determines it's time to prune the tree:
    public boolean processEvaluation(float f, MethodDecomposition maxPlan, MethodDecomposition minPlan, boolean clone) {
        switch(minimaxType) {
            case -1:
                if (choicePointPlayerMax!=null) {
                    // in "max" choicepoints, we can only accept values that come when "max" can find a plan
                    bestEvaluation = f;
                    bestMaxPlan = (maxPlan==null ? null:(clone ? maxPlan.clone():maxPlan));
                    bestMinPlan = (minPlan==null ? null:(clone ? minPlan.clone():minPlan));
                    minimaxType = 0;
                } else {
                    bestEvaluation = f;
                    bestMaxPlan = (maxPlan==null ? null:(clone ? maxPlan.clone():maxPlan));
                    bestMinPlan = (minPlan==null ? null:(clone ? minPlan.clone():minPlan));
                    minimaxType = 1;
                }
                break;
            case 0:
                if ((bestMaxPlan==null && (maxPlan!=null || f>bestEvaluation)) ||
                    (bestMaxPlan!=null && minPlan!=null && f>bestEvaluation)) {
                    bestEvaluation = f;
                    bestMaxPlan = (maxPlan==null ? null:(clone ? maxPlan.clone():maxPlan));
                    bestMinPlan = (minPlan==null ? null:(clone ? minPlan.clone():minPlan));
                }
                break;
            case 1:
                if ((bestMinPlan==null && (minPlan!=null || f<bestEvaluation)) ||
                    (bestMinPlan!=null && minPlan!=null && f<bestEvaluation)) {
                    bestEvaluation = f;
                    bestMaxPlan = (maxPlan==null ? null:(clone ? maxPlan.clone():maxPlan));
                    bestMinPlan = (minPlan==null ? null:(clone ? minPlan.clone():minPlan));
                }
                break;
        }

        // alpha-beta prunning:
        if (minimaxType==0) 
        {
            // max node:
            alpha = Math.max(alpha, f);
//            System.out.println(alpha + " <= " + beta);
            if (beta<=alpha) 
            {
                // beta cutoff:
                return true;
            }
        } 
        else if (minimaxType==1) 
        {
            // min node:
            beta = Math.min(beta, f);
//            System.out.println(alpha + " <= " + beta);
            if (beta<=alpha) 
            {
                // alpha cutoff:
                return true;
            }
        }       
        return false;
    }
    
    
    
    public String toString() {
        String tmp = "( ";
        if (choicePointPlayerMax==null) {
            tmp += "null";
        } else {
            if (choicePointPlayerMax.getType()==MethodDecomposition.METHOD_METHOD) {
                tmp+= "choicePointPlayerMax(" + choicePointPlayerMax.getTerm() + ")";
            } else if (choicePointPlayerMax.getType()==MethodDecomposition.METHOD_CONDITION) {
                tmp+= "choicePointPlayerMax(" + choicePointPlayerMax.getClause() + ")";
            } 
            //MK
            else if(choicePointPlayerMax.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	tmp +="choicePointPlayerMax(" + choicePointPlayerMax.getTerm() + ")";
            }
            //
            else {
                tmp+= "choicePointPlayerMax(???)";
            }
        }
        tmp += " , ";
        if (choicePointPlayerMin==null) {
            tmp += "null";
        } else {
            if (choicePointPlayerMin.getType()==MethodDecomposition.METHOD_METHOD) {
                tmp+= "choicePointPlayerMin(" + choicePointPlayerMin.getTerm() + ")";
            } else if (choicePointPlayerMin.getType()==MethodDecomposition.METHOD_CONDITION) {
                tmp+= "choicePointPlayerMin(" + choicePointPlayerMin.getClause() + ")";
            } 
            //MK
            else if(choicePointPlayerMin.getType()==MethodDecomposition.METHOD_PHASE)
            {
            	tmp +="choicePointPlayerMin(" + choicePointPlayerMax.getTerm() + ")";
            }
            //
            else {
                tmp+= "choicePointPlayerMin(???)";
            }
        }
        return tmp + ")";
    }
 
}

 class AdversarialChoicePointComparator implements Comparator
{
	@Override
	public int compare(Object arg0, Object arg1) 
	{
		// TODO Auto-generated method stub
		AdversarialChoicePoint a0 = (AdversarialChoicePoint)arg0;
		AdversarialChoicePoint a1 = (AdversarialChoicePoint)arg1;
		int result = (a0.bestEvaluation<=a1.bestEvaluation)?1:-1;	
		return result;
	}
	
}
 
 class AdversarialChoicePointComparatorL implements Comparator
{
	@Override
	public int compare(Object arg0, Object arg1) 
	{
		// TODO Auto-generated method stub
		AdversarialChoicePoint a0 = (AdversarialChoicePoint)arg0;
		AdversarialChoicePoint a1 = (AdversarialChoicePoint)arg1;
		int result = (a0.bestEvaluation>=a1.bestEvaluation)?1:-1;	
		return result;
	}
	
}
