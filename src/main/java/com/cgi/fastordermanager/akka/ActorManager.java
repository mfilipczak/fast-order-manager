package com.cgi.fastordermanager.akka;

import static com.cgi.fastordermanager.akka.SpringExtension.SpringExtProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.cgi.fastordermanager.akka.actor.OrderManagerActor;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class ActorManager{

	@Autowired
	private ActorSystem actorSystem;
	
	
	public void decompose(Long orderId) {
		System.err.println("Dekomposition:" + orderId);
	}
	
	
	public void changeOrderStatus(Order order, OrderEvent event) {
		ActorRef runner = actorSystem.actorOf(
				SpringExtProvider.get(actorSystem).props("orderManagerActor"), "ordermanager");
		runner.tell(new OrderManagerActor.OrderStateChanged(order, event), ActorRef.noSender());
	}
	
	public void  startOrder(Order order) {
		ActorRef runner = actorSystem.actorOf(
				SpringExtProvider.get(actorSystem).props("runOrderActor"), "runOrder");
		runner.tell(order, ActorRef.noSender());
	}
	

}
