/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;
import ai.ahtn.planner.AdversarialChoicePoint;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import rts.GameState;

/**
 *
 * @author santi
 */
public class HTNMethod {
    String name;
    Term head;
    MethodDecomposition method;
    
    //MK
    public MethodDecomposition ownedmission = null;
    public HTNPhase ownedphase = null;
    public int status = UNUSED;
    public static int FAILED = 0;
    public static int SUCCESS = 1;
    public static int UNUSED = 2;
    public static int CANCEL = 3;
    public boolean essensial = true;
    public List<AdversarialChoicePoint> planlist = null;
    public String IDname="HTN";
    //
    
    public HTNMethod(String n, Term h, MethodDecomposition m,boolean flag) {
        name = n;
        head = h;
        method = m;
        essensial =flag;
    }
    
    public String getName() {
        return name;
    }
    
    public Term getHead() {
        return head;
    }

    public MethodDecomposition getDecomposition() {
        return method;
    }
    
    public HTNMethod clone() {
        HTNMethod c = new HTNMethod(name, head.clone(), method.clone(),essensial);
        return c;
    }
    
    public HTNMethod cloneTrackingDescendants(MethodDecomposition descendantsToTrack[], MethodDecomposition newDescendants[]) {
        HTNMethod c = new HTNMethod(name, head.clone(), method.cloneTrackingDescendants(descendantsToTrack,newDescendants),essensial);
        return c;
    }
        
    /*
    public HTNMethod clone(int renamingIndex) {
        HTNMethod c = new HTNMethod(name, head.clone(), method.clone());
        c.renameVariables(renamingIndex);
        return c;
    }
    */
    
    public void renameVariables(int renamingIndex) {
        head.renameVariables(renamingIndex);
        method.renameVariables(renamingIndex);
    }
    
    public void applyBindings(List<Binding> l) throws Exception {
        head.applyBindings(l);
        method.applyBindings(l);
    }

    public static HTNMethod fromLispElement(LispElement e) throws Exception {
        LispElement name_e = e.children.get(1);
        LispElement head_e = e.children.get(2);
        LispElement method_e = e.children.get(3);
        
        String name = name_e.element;
        Term head = Term.fromLispElement(head_e);
        
//        int length = head.parameters.length;
//        if(((IntegerConstant)head.parameters[length]).value==1);
//             essential = true;
        
        MethodDecomposition m = MethodDecomposition.fromLispElement(method_e);
        
        return new HTNMethod(name,head,m,true); 
    }
    
    
    public void replaceSingletonsByWildcards() throws Exception {
        List<Symbol> singletons = findSingletons();
        head.replaceSingletonsByWildcards(singletons);
        method.replaceSingletonsByWildcards(singletons);
    }
    
    public List<Symbol> findSingletons() throws Exception {
        HashMap<Symbol,Integer> appearances = new HashMap<>();
        countVariableAppearances(appearances);
        
        List<Symbol> l = new ArrayList<>();
        for(Symbol v:appearances.keySet()) 
        {
            if (appearances.get(v)==1&&v.equals("?essential")==false) l.add(v);
        }
        return l;
    }
    
    
    public void countVariableAppearances(HashMap<Symbol,Integer> appearances) throws Exception {
        head.countVariableAppearances(appearances);
        method.countVariableAppearances(appearances);
    }

    
    public String toString() {
        return "method("+name+"): " + head + ", decomposition: " + method;
    }    
    
    
    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints) {
        return method.executionCycle(gs, actions, choicePoints);
    }

    public int executionCycle(GameState gs, List<MethodDecomposition> actions, List<MethodDecomposition> choicePoints, AdversarialChoicePoint previous_cp,List<Binding> bindings) {
        return method.executionCycle(gs, actions, choicePoints, previous_cp,bindings);
    }
}
