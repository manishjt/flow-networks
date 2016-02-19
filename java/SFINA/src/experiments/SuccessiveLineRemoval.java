/*
 * Copyright (C) 2015 SFINA Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package experiments;

import applications.BenchmarkLogReplayer;
import applications.PowerCascadeAgent;
import input.Backend;
import input.Domain;
import input.SystemParameter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import power.PowerFlowType;
import protopeer.Experiment;
import protopeer.Peer;
import protopeer.PeerFactory;
import protopeer.SimulatedExperiment;
import protopeer.util.quantities.Time;

/**
 *
 * @author Ben
 */
public class SuccessiveLineRemoval extends SimulatedExperiment{
    
    private static String expSeqNum="Case30LineRemovalRandomConsistent";
    private final static String peersLogDirectory="peerlets-log/";
    private static String experimentID="experiment-"+expSeqNum+"/";
    
    //Simulation Parameters
    private final static int bootstrapTime=2000;
    private final static int runTime=1000;
    private final static int runDuration=34;
    private final static int N=1;
    
    // SFINA parameters
    private final static HashMap<SystemParameter,Object> simulationParameters = new HashMap();

    private final static String columnSeparator=",";
    private final static String missingValue="-";
    
    private final static String configurationFilesLocation = "configuration_files/";
    private final static String timeTokenName="time_";
    private final static String inputDirectoryName="input";
    private final static String outputDirectoryName="output";
    private final static String topologyDirectoryName="topology";
    private final static String flowDirectoryName="flow";
    
    private final static String experimentConfigurationFilesLocation=configurationFilesLocation+experimentID+inputDirectoryName+"/";
    private final static String experimentOutputFilesLocation=configurationFilesLocation+experimentID+outputDirectoryName+"/";
    private final static String eventsLocation=experimentConfigurationFilesLocation+"/events.txt";
    private final static String nodesLocation="/"+topologyDirectoryName+"/nodes.txt";
    private final static String linksLocation = "/"+topologyDirectoryName+"/links.txt";
    private final static String nodesFlowLocation ="/"+flowDirectoryName+"/nodes.txt";
    private final static String linksFlowLocation ="/"+flowDirectoryName+"/links.txt";
    
    private static ArrayList<ArrayList<Integer>> attackLinks = new ArrayList();
    
    public SuccessiveLineRemoval(Backend backend, PowerFlowType flowType){
        run(backend, flowType);
    }
    
    public static void main(String args[]){
        int iterations = 1;
        ArrayList<Backend> backends = new ArrayList();
        //backends.add(Backend.MATPOWER);
        backends.add(Backend.INTERPSS);
        ArrayList<PowerFlowType> flowTypes = new ArrayList();
        flowTypes.add(PowerFlowType.AC);
        //flowTypes.add(PowerFlowType.DC);
        
        // Random
        int linkNr = 41;
        for(int i=0; i<iterations; i++){
            ArrayList<Integer> links = new ArrayList<>();
            for(int j=0; j<linkNr; j++)
                links.add(j+1);
            Collections.shuffle(links);
            attackLinks.add(links);
        }
        // Manish ranking
//        int[] manishLinks = new int[]{30,34,41,40,38,21,31,27,23,20,28,6,32,33,22,15,39,24,25,3,12,36,26,37,8,17,1,19,5,11,14,4,9,2,29,13,16,18,35,7,10};
//        for(int i=0; i<manishLinks.length; i++)
//            attackLinks.add(manishLinks[i]);
//        Collections.reverse(attackLinks);
        
        for(Backend backend : backends){
            for(PowerFlowType flowType : flowTypes){
                for(int i=0; i<iterations; i++){
                    createLinkAttackEvents(i);
                    run(backend, flowType);
                    BenchmarkLogReplayer replayer=new BenchmarkLogReplayer(expSeqNum, 0, 1000);
                }
            }
        }
    }
    
    public static void run(Backend backend, PowerFlowType flowType) {
        simulationParameters.put(SystemParameter.DOMAIN, Domain.POWER);
        simulationParameters.put(SystemParameter.BACKEND, backend);
        double toleranceParameter = 2.0;
        //simulationParameters.put(SystemParameter.CAPACITY_CHANGE_LINK, 1.0);
        
        System.out.println("Experiment "+expSeqNum+"\n");
        Experiment.initEnvironment();
        final TestBenchmarkAgent test = new TestBenchmarkAgent();
        test.init();
        final File folder = new File(peersLogDirectory+experimentID);
        clearExperimentFile(folder);
        folder.mkdir();
        //createLinkAttackEvents();
        PeerFactory peerFactory=new PeerFactory() {
            public Peer createPeer(int peerIndex, Experiment experiment) {
                Peer newPeer = new Peer(peerIndex);
//                if (peerIndex == 0) {
//                   newPeer.addPeerlet(null);
//                }
                newPeer.addPeerlet(new PowerCascadeAgent(
                        experimentID, 
                        peersLogDirectory, 
                        Time.inMilliseconds(bootstrapTime),
                        Time.inMilliseconds(runTime),                        
                        timeTokenName,
                        experimentConfigurationFilesLocation,
                        experimentOutputFilesLocation,
                        nodesLocation,
                        linksLocation,
                        nodesFlowLocation,
                        linksFlowLocation,
                        eventsLocation,
                        columnSeparator,
                        missingValue,
                        simulationParameters,
                        flowType,
                        toleranceParameter));
                return newPeer;
            }
        };
        test.initPeers(0,N,peerFactory);
        test.startPeers(0,N);
        //run the simulation
        test.runSimulation(Time.inSeconds(runDuration));
    }
    
    private static void createLinkAttackEvents(int iteration){
        try{
            File file = new File(eventsLocation);
            file.createNewFile();
            PrintWriter writer = new PrintWriter(new FileWriter(file,false));
            writer.println("time" + columnSeparator + "feature" + columnSeparator + "component" + columnSeparator + "id" + columnSeparator + "parameter" + columnSeparator + "value");
            int time = 2;
//            for (int i=0; i<30; i++){
//                for (int j=0; j<18; j++){
//                    int linkId = attackLinks.get(iteration).get(i*18+j);
//                    writer.println(time + columnSeparator + "topology" + columnSeparator + "link" + columnSeparator + linkId + columnSeparator + "status" + columnSeparator + "0");
//                }
//                time++;
//            }
            for(int i=0; i<attackLinks.get(iteration).size(); i++){
                writer.println(time + columnSeparator + "topology" + columnSeparator + "link" + columnSeparator + attackLinks.get(iteration).get(i) + columnSeparator + "status" + columnSeparator + "0");
                time++;
            }
            writer.close();
        }
        catch(IOException ex){
            ex.printStackTrace();
        }
        
    }
    
    public final static void clearExperimentFile(File experiment){
        File[] files = experiment.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    clearExperimentFile(f);
                } else {
                    f.delete();
                }
            }
        }
        experiment.delete();
    }
}
