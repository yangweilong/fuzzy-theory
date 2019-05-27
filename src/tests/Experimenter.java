/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.*;
import ai.ahtn.AHTNAI;
import ai.ahtn.domain.Term;
import ai.ahtn.planner.AdversarialBoundedDepthPlannerAlphaBeta;
//import ai.ahtn.EnhanceAHTNAI;
import gui.PhysicalGameStateJFrame;
import gui.PhysicalGameStatePanel;

import java.io.File;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFrame;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.UnitActionAssignment;
import rts.units.Unit;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public class Experimenter {
    public static int DEBUG = 0;
    public static boolean GC_EACH_FRAME = true;
    
    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize,PrintStream out1) throws Exception {
        runExperiments(bots, maps, utt,iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, false,out1);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize,PrintStream out1) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, System.out, -1, true,out1);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out,PrintStream out1) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, false,out1);
    }

    public static void runExperimentsPartiallyObservable(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out,PrintStream out1) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, -1, true,out1);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean partiallyObservable,PrintStream out1) throws Exception {
        runExperiments(bots, maps, utt, iterations, max_cycles, max_inactive_cycles, visualize, out, run_only_those_involving_this_AI, false, partiallyObservable, out1);
    }

    public static void runExperiments(List<AI> bots, List<PhysicalGameState> maps, UnitTypeTable utt, int iterations, int max_cycles, int max_inactive_cycles, boolean visualize, PrintStream out, 
                                      int run_only_those_involving_this_AI, boolean skip_self_play, boolean partiallyObservable,PrintStream out1) throws Exception {
        int wins[][] = new int[bots.size()][bots.size()];
        int ties[][] = new int[bots.size()][bots.size()];
        int loses[][] = new int[bots.size()][bots.size()];
        
        double win_time[][] = new double[bots.size()][bots.size()];
        double tie_time[][] = new double[bots.size()][bots.size()];
        double lose_time[][] = new double[bots.size()][bots.size()];

        float averrepairrate[][] = new float[bots.size()][bots.size()];
        float averplantime[][] = new float[bots.size()][bots.size()];
        float repair[][] = new float[bots.size()][bots.size()];
        long savetime[][] = new long[bots.size()][bots.size()];
        long totaltime[][] = new long[bots.size()][bots.size()];
        
        double nodenum[][] = new double[bots.size()][bots.size()];
        
        List<AI> bots2 = new LinkedList<>();
        for(AI bot:bots) 
        	bots2.add(bot.clone());
        
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
            for (int ai2_idx = 0; ai2_idx < bots2.size(); ai2_idx++) 
            {
            	if(ai1_idx==ai2_idx)
            		break;//同样的AI是否比较
                if (run_only_those_involving_this_AI!=-1 &&
                    ai1_idx!=run_only_those_involving_this_AI &&
                    ai2_idx!=run_only_those_involving_this_AI) continue;
                if (skip_self_play && ai1_idx==ai2_idx) continue;
                
                for(PhysicalGameState pgs:maps) 
                {                 	 
                	 AI ai1 = bots.get(ai1_idx);
                     AI ai2 = bots.get(ai2_idx);
                     
                    for (int i = 0; i < iterations; i++) 
                    {
                    	System.gc();

//                    	ai1 = bots.get(ai1_idx);
//                        ai2 = bots2.get(ai2_idx);
                        
                        long lastTimeActionIssued = 0;

                        ai1.reset();
                        ai2.reset();

                     GameState gs = new GameState(pgs.clone(),utt);
                       
                     float repairnumAI1 = 0;
                   	 float repairnumAI2 = 0;
                   	 
                   	 float totaltimeAI1 = 0;
                   	 float totaltimeAI2 =0;
                   	 float avertimeAI1 = 0;
                   	 float avertimeAI2 =0;
                   	 
                   	 long saveplantimeplayer0 = 0;
                   	 long saveplantimeplayer1 = 0;
                   	 long replantimeplayer0 =0;
                   	 long replantimeplayer1 = 0;
                   	 
                   	float averrepairrate2 = 0;     //和不同算法对抗时的修复率
                    float averplanntime2 = 0;
                    
                    float averrepairrate1 = 0;     //和不同算法对抗时的修复率
                    float averplanntime1 = 0;
                    
                    double max_tree_nodesplayer0 = 0;
                    double max_tree_nodesplayer1 = 0;
                    double average_tree_depth0 = 0;
                    double average_tree_depth1 = 0;
                    double reIndex0=0;
                    double reIndex1=0;
                    
                        //MK
                        gs.addAI(ai1);
                        gs.addAI(ai2);
                        ai1.setPlayerID(0);
                        ai2.setPlayerID(1);
                        gs.Realworld = true;
                        float temp = 0;
                        float temp2 = 0; 
                      //  float AInum = 0;
                        //
                        PhysicalGameStateJFrame w = null;
                        if (visualize) 
                        	w = PhysicalGameStatePanel.newVisualizer(gs, 600, 600, true);//设置游戏是否可观
                       // out1.println("MATCH UP: " + ai1 + " vs " + ai2);
                        boolean gameover = false;
                        do {
                           // if (GC_EACH_FRAME) 
                           // 	System.gc();
                        	if(gs.getTime()==0)
                        	{
                        		partiallyObservable = false;
                        	}
                        	else
                        	{
                        		partiallyObservable =false;
                        	}//判断初始状态是否可观
                            PlayerAction pa1 = null, pa2 = null;
                            long temp1 = System.currentTimeMillis();
                            
                            
                            pa1 = ai1.getAction(0, gs,false);
//                          if(pa1.g.type==7)
//                           	System.out.printf("1");
//                          pa1 = ai1.getAction(0,gs,partiallyObservable);
                            temp =System.currentTimeMillis()-temp1+temp;
                            long temp3 = System.currentTimeMillis();
                            pa2 = ai2.getAction(1, gs,false);
                            temp2 =System.currentTimeMillis()-temp3+temp2;
                            // AInum++;
                            if(gs.issueSafe(pa1))
                            {
                              lastTimeActionIssued = gs.getTime();   
                            }
                                                         
                            if(gs.issueSafe(pa2))
                            {
                             	lastTimeActionIssued = gs.getTime();
                            }
                         
                            gameover = gs.cycle();
                            if (w!=null)
                            {
                                w.setStateCloning(gs);
                                w.repaint();
                                try {
                                    Thread.sleep(1);    // give time to the window to repaint
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }  
                        } while (!gameover && 
                                 (gs.getTime() < max_cycles&&(gs.getTime() - lastTimeActionIssued < max_inactive_cycles)) );//
                        
                        //MK
                          if(gs.AI1plannum!=0)
                          {
                        	  avertimeAI1 = temp/gs.AI1plannum;
                        	  averplanntime1 = averplanntime1+avertimeAI1;
                        	  
                        	  reIndex0 =gs.renamingIndex0/gs.AI1plannum;
                        	  max_tree_nodesplayer0 = gs.max_tree_nodesplayer0/gs.AI1plannum;
                             
                          }
                          if(gs.AI2plannum!=0)
                          {
                        	  avertimeAI2 = temp2/gs.AI2plannum;
                        	  averplanntime2 = averplanntime2+avertimeAI2;
                        	  
                        	  reIndex1 =gs.renamingIndex1/gs.AI2plannum;
                        	  max_tree_nodesplayer1 = gs.max_tree_nodesplayer1/gs.AI2plannum;
                          }
                                      
                          
                          saveplantimeplayer0 =  gs.saveplantimeplayer0;
                          saveplantimeplayer1 = gs.saveplantimeplayer1;
                          replantimeplayer0 =  gs.replantimeplayer0;
                          replantimeplayer1= gs.replantimeplayer1;
                          
                         
                          
                          average_tree_depth0 = gs.average_tree_depth0;
                          average_tree_depth1 = gs.average_tree_depth1;
//                        out1.println(ai1+"修复的次数： "+ gs.repairnum1+"\n");
//                        out1.println(ai2+"修复的次数： "+ gs.repairnum2+"\n");
//                          
//                        out1.println(ai1+"修复失败的次数： "+ gs.repairfailednum1+"\n");
//                        out1.println(ai2+"修复失败的次数： "+ gs.repairfailednum2+"\n");
                          
                          if(gs.repairnum1!=0)
                          {
                        	  averrepairrate1 = averrepairrate1 +1-gs.repairfailednum1/gs.repairnum1;
                        	  float rate = 1-(gs.repairfailednum1/gs.repairnum1);
//                        	  out1.println(ai1+"平均修复率： "+rate +"\n");
//                        	  if(rate<0)
//                        		  out1.println(ai2+"平均修复率： "+rate +"\n");
                        	  repair[ai1_idx][ai2_idx]++;
                          }
                          if(gs.repairnum2!=0)
                          {
                        	  averrepairrate2 = averrepairrate2 +1-gs.repairfailednum2/gs.repairnum2;
                        	  float rate = 1-(gs.repairfailednum2/gs.repairnum2);
//                        	  out1.println(ai2+"平均修复率： "+rate +"\n");
//                        	  if(rate<0)
//                        		  out1.println(ai2+"平均修复率： "+rate +"\n");
                        	  repair[ai2_idx][ai1_idx]++;
                          }
                          
                          
                          out1.println(ai1+"规划总时间： "+ temp+"\n");
                          out1.println(ai2+"规划总时间： "+ temp2+"\n");
                          
                          out1.println("规划次数： "+ gs.AI1plannum+"\n");
                          out1.println("规划次数： "+ gs.AI2plannum+"\n");
                          
                          out1.println(ai1+"规划平均时间： "+ avertimeAI1+"\n");
                          out1.println(ai2+"规划平均时间： "+ avertimeAI2+"\n");
                          
                          out1.println(ai1+"节点数目： "+max_tree_nodesplayer0 +"\n");
                          out1.println(ai2+"节点数目： "+max_tree_nodesplayer1 +"\n");
                          
//                        out1.println(ai1+"深度： "+average_tree_depth0 +"\n");
//                        out1.println(ai2+"深度： "+average_tree_depth1 +"\n");
                          
                          out1.println(ai1+"HTN深度： "+reIndex0 +"\n");
                          out1.println(ai2+"HTN深度： "+reIndex1 +"\n");
                          
                          out1.println(ai1+"保存规划时间： "+ saveplantimeplayer0+"\n");
                          out1.println(ai2+"保存规划时间： "+ saveplantimeplayer1+"\n");
                          out1.println(ai1+"重规划时间： "+ replantimeplayer0+"\n");
                          out1.println(ai2+"重规划时间： "+ replantimeplayer1+"\n");
//                         
//                          out1.println(gs.unitnum+"\n");
//                          
//                          out1.println("第"+i+"次结束"+"\n");
                          
                          averrepairrate[ai1_idx][ai2_idx] = averrepairrate[ai1_idx][ai2_idx] + averrepairrate1;
                          averrepairrate[ai2_idx][ai1_idx] = averrepairrate[ai2_idx][ai1_idx] + averrepairrate2;
                          averplantime[ai1_idx][ai2_idx] = averplantime[ai1_idx][ai2_idx] +averplanntime1;
                          averplantime[ai2_idx][ai1_idx] = averplantime[ai2_idx][ai1_idx] +averplanntime2;
                                                  
                          savetime[ai1_idx][ai2_idx] = savetime[ai1_idx][ai2_idx]+saveplantimeplayer0+replantimeplayer0;
                          savetime[ai2_idx][ai1_idx] = savetime[ai2_idx][ai1_idx]+saveplantimeplayer1+replantimeplayer1;
                          
                          totaltime[ai1_idx][ai2_idx] = totaltime[ai1_idx][ai2_idx]+(long)temp;
                          totaltime[ai2_idx][ai1_idx] = totaltime[ai2_idx][ai1_idx]+(long)temp2;
                          
                          nodenum[ai1_idx][ai2_idx] = nodenum[ai1_idx][ai2_idx]+max_tree_nodesplayer0;
                          nodenum[ai2_idx][ai1_idx] = nodenum[ai2_idx][ai1_idx]+max_tree_nodesplayer1;
                        //
                        
                        if (w!=null) w.dispose();
                        int winner = gs.winner();
//                        out.println("Winner: " + winner + "  in " + gs.getTime() + " cycles");
//                        out.println(ai1 + " : " + ai1.statisticsString());
//                        out.println(ai2 + " : " + ai2.statisticsString());
//                        out.flush();
                        if (winner == -1) {
                            ties[ai1_idx][ai2_idx]++;
                            tie_time[ai1_idx][ai2_idx]+=gs.getTime();

                            ties[ai2_idx][ai1_idx]++;
                            tie_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 0) {
                            wins[ai1_idx][ai2_idx]++;
                            win_time[ai1_idx][ai2_idx]+=gs.getTime();

                            loses[ai2_idx][ai1_idx]++;
                            lose_time[ai2_idx][ai1_idx]+=gs.getTime();
                        } else if (winner == 1) {
                            loses[ai1_idx][ai2_idx]++;
                            lose_time[ai1_idx][ai2_idx]+=gs.getTime();

                            wins[ai2_idx][ai1_idx]++;
                            win_time[ai2_idx][ai1_idx]+=gs.getTime();
                        }                        
                    }
                   
                }
            }
        }

        out.println("Wins: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(wins[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Ties: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(ties[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("Loses: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(loses[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        //MK
        out.println("Ponits: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) 
        {
        	double point = 0;
        	double tiepoint = 0;
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) 
            {
                point = point + wins[ai1_idx][ai2_idx];
                tiepoint = tiepoint +ties[ai1_idx][ai2_idx];
            }
            point = point+tiepoint/2;
            out.println(point+"\n");
        }
        out.println("averrate: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(averrepairrate[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("repair: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(repair[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("averplantime: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(averplantime[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        
        out.println("nodenum: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(nodenum[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        
        out.println("save: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(savetime[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        out.println("total: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                out.print(totaltime[ai1_idx][ai2_idx] + ", ");
            }
            out.println("");
        }
        //
        out.println("Win average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (wins[ai1_idx][ai2_idx]>0) {
                    out.print((win_time[ai1_idx][ai2_idx]/wins[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Tie average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (ties[ai1_idx][ai2_idx]>0) {
                    out.print((tie_time[ai1_idx][ai2_idx]/ties[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }
        out.println("Lose average time: ");
        for (int ai1_idx = 0; ai1_idx < bots.size(); ai1_idx++) {
            for (int ai2_idx = 0; ai2_idx < bots.size(); ai2_idx++) {
                if (loses[ai1_idx][ai2_idx]>0) {
                    out.print((lose_time[ai1_idx][ai2_idx]/loses[ai1_idx][ai2_idx]) + ", ");
                } else {
                    out.print("-, ");
                }
            }
            out.println("");
        }              
        out.flush();
     
    }
}
