/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ai.ahtn.domain;

import ai.ahtn.domain.LispParser.LispElement;
import ai.ahtn.domain.LispParser.LispParser;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author santi
 */
public class DomainDefinition {
    public static int DEBUG = 0;
    
    String name = null;
    List<HTNOperator> operators = new LinkedList<>();
    List<HTNMethod> methods = new LinkedList<>();
    //MK
    List<HTNPhase> phases = new LinkedList<>();  //ÿ����������������Ľ׶�
   // HashMap<Symbol,List<HTNPhase>> phasesPerGoal = new HashMap<>();
    //
    HashMap<Symbol,List<HTNMethod>> methodsPerGoal = new HashMap<>();
    
    
    public List<HTNOperator> getOperators() {
        return operators;
    }
    
    public List<HTNMethod> getMethods() {
        return methods;
    }
    //MK
    public List<HTNPhase> getPhases() {
        return phases;
    }
    
    public List<HTNPhase> getSomePhases(Symbol functor) {
    	List<HTNPhase> l = new LinkedList<>();
    	for(int i = 0; i< phases.size();i++)
    	{
    		if(phases.get(i).getName().equals(functor.toString())==true)
    		{
    			l.add(phases.get(i));
    		}
    	}
        return l;
    }
    //MK
    public void addPhase(HTNPhase phase) {
    	phases.add(phase);
    }
    //
    public void addMethod(HTNMethod m) {
        methods.add(m);
        Symbol goal = m.head.getFunctor();
        List<HTNMethod> l = methodsPerGoal.get(goal);
        if (l==null) {
            l = new LinkedList<>();
            methodsPerGoal.put(goal,l);
        }
        l.add(m);
    }

    public static DomainDefinition fromLispFile(String fileName) throws Exception {
        
        List<LispElement> l = LispParser.parseLispFile(fileName);
        if (DEBUG>=1) {
            for(LispElement e:l)
                System.out.println(e);
        }
        
        if (l.isEmpty()) return null;        
        return fromLispElement(l.get(0));
    }
        
    public static DomainDefinition fromLispElement(LispElement e) throws Exception {
        DomainDefinition dd = new DomainDefinition();

        // verify it's a domain definition:
        if (e.children.size()!=3) throw new Exception("Lisp domain definition does not have 3 arguments");
        LispElement defdomain = e.children.get(0);
        if (defdomain.element==null || !defdomain.element.equals("defdomain")) throw new Exception("Lisp domain definition does not start by 'defdomain'");
        LispElement name_e = e.children.get(1);
        if (name_e.element==null) throw new Exception("second parameter of defdomain is not a domain name");
        dd.name = name_e.element;
        
        LispElement rest_e = e.children.get(2);
        if (rest_e.children==null) throw new Exception("third parameter of defdomain is not a list");
        
        for(LispElement def_e:rest_e.children) {
            // load operators and methods:
            if (def_e.children!=null && def_e.children.size()>0) {         	
                LispElement head = def_e.children.get(0);
                if (head.element!=null && head.element.equals(":operator")) {
                    HTNOperator op = HTNOperator.fromLispElement(def_e);
                    dd.operators.add(op);
                } else if (head.element!=null && head.element.equals(":method")) {
                    HTNMethod m = HTNMethod.fromLispElement(def_e);  //������Ƕ���method�ķֽ⣬����������˳��ִ�С������׶Σ���ʱ���ݾɵĸ�ʽ
                    dd.addMethod(m);
                } 
                //MK
                else if(head.element!=null && head.element.equals(":phase"))
                {
                	 HTNPhase m = HTNPhase.fromLispElement(def_e); //�������phase�ķֽ⣬����ִ������������ִ�С�������
                     dd.addPhase(m);
                }
                //
                else {
                    throw new Exception("Element in domain definition is not an operator nor method");
                }
            } else {
                throw new Exception("Element in domain definition is not an operator�� method or Phase");
            }
        }
        
        // remove singletons:
        for(HTNMethod m:dd.getMethods()) {
            if (DEBUG>=1) {
                List<Symbol> l = m.findSingletons();
                if (!l.isEmpty()) System.out.println("Singletons in '" + m.getName() + "': " + l);
            }
           for(Symbol s:m.findSingletons())
           {
//        	   if(m.getHead().getFunctor().equals("destroy-player-rush"))
//        		   System.out.println(m);
        	   if(!s.equals("?essential"))
        		   m.replaceSingletonsByWildcards();
           } 
        }
        
        for(HTNPhase m:dd.getPhases()) {
            if (DEBUG>=1) {
                List<Symbol> l = m.findSingletons();
                if (!l.isEmpty()) System.out.println("Singletons in '" + m.getName() + "': " + l);
            }
            m.replaceSingletonsByWildcards();
        }
        
        return dd;
    }
    
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Domain: " + name + "\n");
        sb.append("Operators:\n");
        for(HTNOperator op:operators) {
            sb.append("  " + op + "\n");
        }
        sb.append("Methods:\n");
        for(HTNMethod m:methods) {
            sb.append("  " + m + "\n");
        }
        sb.append("Phase:\n");
        for(HTNPhase m:phases) {
            sb.append("  " + m + "\n");
        }
        return sb.toString();
    }
    
    
    public List<HTNMethod> getMethodsForGoal(Symbol functor) {
        return methodsPerGoal.get(functor);
    }
}
