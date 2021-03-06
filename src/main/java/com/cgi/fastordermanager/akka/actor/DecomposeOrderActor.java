package com.cgi.fastordermanager.akka.actor;
import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Access;
import javax.persistence.AccessType;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.cgi.fastordermanager.ContextEntity;
import com.cgi.fastordermanager.DefaultStateMachineAdapter;
import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.order.AttributeRepository;
import com.cgi.fastordermanager.order.CfsRepository;
import com.cgi.fastordermanager.order.Order;
import com.cgi.fastordermanager.order.OrderEvent;
import com.cgi.fastordermanager.order.OrderRepository;
import com.cgi.fastordermanager.order.OrderState;
import com.cgi.fastordermanager.order.ProductState;
import com.cgi.fastordermanager.order.Rfs;
import com.cgi.fastordermanager.order.Attribute;
import com.cgi.tpc.model.CfsDef;
import com.cgi.tpc.model.RfsDef;

import akka.actor.AbstractActor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Access(AccessType.FIELD)
@Scope("prototype")
@AllArgsConstructor
@Configuration
public class DecomposeOrderActor extends AbstractActor {

	@Autowired
	private final ActorManager actorManager;

	@Autowired
	private final OrderRepository orderRepository;
	
	@Autowired
	private final CfsRepository cfsRepository;
	
	@Autowired
	private final AttributeRepository attributeRepository;

	@Autowired
	final DefaultStateMachineAdapter<OrderState, OrderEvent, ContextEntity<OrderState, OrderEvent, ? extends Serializable>> orderStateMachineAdapter;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(String.class, orderId -> {
			Order order = orderRepository.findByExternalId(orderId);
			log.info("Received order to start: {}", order);
			String url = "http://localhost:8181/rfs";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RestTemplate restTemplate = new RestTemplate();
			List<CfsDef> cfsList = new ArrayList<>();
			order.getCfs().stream().forEach(cfs -> {
				cfsList.clear();
				CfsDef d = new CfsDef();
				d.setCfsId(cfs.getCfsId());
				cfsList.add(d);
				HttpEntity<Object> requestEntity = new HttpEntity<Object>(cfsList, headers);
				List<RfsDef> rfsList = Arrays.asList(restTemplate.postForObject(url, requestEntity, RfsDef[].class));
				List<Rfs> list = new ArrayList<>();
				cfs.setRfs(list);

				rfsList.stream().forEach(r -> {
					Rfs rfs = new Rfs();
					rfs.setRfsId(r.getRfsId());
					rfs.setName(r.getName());
					rfs.setCurrentState(ProductState.PENDING);
					rfs.setBlockedRfs(r.getBlockedRfs().stream().map(b->b.getRfsId()).collect(Collectors.joining("#")));
					/* for now just copy attributes for future use bsf with beanshell fex.*/
					List<Attribute> attributes = new ArrayList<>();
					cfs.getAttributes().parallelStream().forEach(a -> {
						Attribute attribute = new Attribute();
						BeanUtils.copyProperties(a, attribute);
						attributes.add(attribute);
					});
					rfs.setAttributes(attributes);
					list.add(rfs);
				});

			});
			cfsRepository.save(order.getCfs());
			actorManager.changeOrderStatus(order, OrderEvent.PROCESS);
			log.info("Order {} ", order.getCurrentState());
		}).matchAny(o -> log.info("received unknown message")).build();
	}
}
