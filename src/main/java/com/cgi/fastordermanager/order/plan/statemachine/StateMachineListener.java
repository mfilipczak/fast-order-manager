package com.cgi.fastordermanager.order.plan.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class StateMachineListener extends StateMachineListenerAdapter<String,String> {


    @Override
    public void stateChanged(State<String,String> from, State<String,String> to) {
        log.info(String.format("Transitioned from %s to %s%n", from == null ? "none" : from.getId(), to.getId()));
    }
    
    @Override
    public void stateEntered(State<String, String> state) {
    	System.err.println("ENTER:" + state.getId() +":"+System.currentTimeMillis());
  //  	stateMachineManager.sendEvent(state.getId(), state.getId().concat("_EVENT"));
        log.info(String.format("Entered to %s%n", state.getId()));
        
    }
    
    @Override
    public void stateExited(State<String, String> state) {
    	//System.out.println("EXIT:" + state.getId() +":"+System.currentTimeMillis());
        log.info(String.format("Exited from %s%n", state.getId()));
    }
    
	@Override
	public void stateMachineStarted(StateMachine<String, String> stateMachine) {
		 log.info(String.format("Started " + stateMachine.getId()));
	}

	@Override
	public void stateMachineStopped(StateMachine<String, String> stateMachine) {
		log.info(String.format("Stopped " + stateMachine.getId()));
	}
	
}