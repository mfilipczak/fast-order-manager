package com.cgi.fastordermanager.akka;

import static com.cgi.fastordermanager.akka.SpringExtension.SpringExtProvider;

import java.util.HashMap;
import java.util.Map;

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
public class ActorManager {

	@Autowired
	private ActorSystem actorSystem;

	private Map<String, ActorRef> actors = new HashMap<>();

	public void decompose(String orderId) {
		ActorRef runner = actors.get("decomposeOrder");
		if (runner == null) {
			runner = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("decomposeOrderActor"),
					"decomposeOrder");
			actors.put("decomposeOrder", runner);
		}
		runner.tell(orderId, ActorRef.noSender());
	}

	public void changeOrderStatus(Order order, OrderEvent event) {
		ActorRef runner = actors.get("ordermanager");
		if (runner == null) {
			runner = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("orderManagerActor"), "ordermanager");
			actors.put("ordermanager", runner);
		}
		runner.tell(new OrderManagerActor.OrderStateChanged(order, event), ActorRef.noSender());
	}

	public void startOrder(Order order) {
		ActorRef runner = actors.get("runOrder");
		if (runner == null) {
			runner = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("runOrderActor"), "runOrder");
			actors.put("runOrder", runner);
		}
		runner.tell(order, ActorRef.noSender());
	}

}
