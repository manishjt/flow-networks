/*
 * Copyright (C) 2016 SFINA Team
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
package core.Archive;

import core.SimulationAgentInterface;
import core.SimulationAgentInterface;
import event.Event;
import protopeer.BasePeerlet;

/**
 *
 * @author mcb
 */
public class SimpleTimeSteppingAgent extends BasePeerlet implements TimeSteppingAgentInterface_old {
   
    @Override
    public void agentFinishedActiveState() {
        if(!getCommandReceiver().isConverged())
            getCommandReceiver().progressToNextIteration();
        else
            getCommandReceiver().progressToNextTimeStep();
    }

    @Override
    public void agentFinishedBootStrap() {
        getCommandReceiver().progressToNextTimeStep();
    }
    
    public SimulationAgentInterface getSimulationAgent() {
        return (SimulationAgentInterface) getPeer().getPeerletOfType(SimulationAgentInterface.class);
    }
        
    public CommandReceiver getCommandReceiver(){
        return (CommandReceiver) getPeer().getPeerletOfType(TimeSteppingAgentInterface_old.CommandReceiver.class);
    }

    @Override
    public void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
       //((SimulationAgent) getPeer().getPeerletOfType(SimulationAgent.class)).runBootstraping();
    }
    
   
    
    
    
    
    
    
}