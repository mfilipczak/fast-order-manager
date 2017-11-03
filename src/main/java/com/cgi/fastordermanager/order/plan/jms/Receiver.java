package com.cgi.fastordermanager.order.plan.jms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.order.plan.statemachine.StateMachineManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class Receiver {

	@Autowired
	private final ActorManager actorManager;
	
    @JmsListener(destination = "esb.rply")
    public void receiveMessage(com.cgi.fastordermanager.order.plan.jms.RfsMessage rfs) {
        log.info("Received rply <" + rfs.rfsId + ">");
        actorManager.processRfsRply(rfs.orderId, rfs.id);
    }

}