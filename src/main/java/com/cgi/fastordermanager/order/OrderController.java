package com.cgi.fastordermanager.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.fastordermanager.akka.ActorManager;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrderController {

	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ActorManager actorManager;
	
	@RequestMapping(value = "/createOrder", 
			method = RequestMethod.POST, 
			produces = MediaType.APPLICATION_JSON_VALUE, 
			headers = "Accept=application/json,application/xml")
	@Transactional
	public @ResponseBody HttpEntity<Void> setCurrentDataList(@RequestBody Order order) {
		actorManager.startOrder(order);
		return ResponseEntity.accepted().build();
	}
	
	@RequestMapping(value = "/orders/report",
	produces = MediaType.APPLICATION_JSON_VALUE,
	method = RequestMethod.GET)
    public ResponseEntity<?> report() {
        List<OrderAnswerStatistics> dtos = orderRepository.findOrderStateCount();
        return ResponseEntity.ok(new Resources<>(dtos));
    }
}
