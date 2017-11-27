package com.cgi.fastordermanager.order.plan.statemachine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.configurers.ForkTransitionConfigurer;
import org.springframework.statemachine.config.configurers.JoinTransitionConfigurer;
import org.springframework.statemachine.config.configurers.StateConfigurer;

import com.cgi.fastordermanager.akka.ActorManager;
import com.cgi.fastordermanager.graph.RfsGraphEdge;
import com.cgi.fastordermanager.order.Order;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class OrderPlanStateMachineBilder {

	private static final String END_VERTEX = "-1";

	private static final String START_VERTEX = "0";

	private static final String _END = "_END";

	private static final String JOIN = "JOIN_";

	private static final String FORK = "FORK_";

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private StateMachineListener stateMachineListener;
	
	@Autowired
	private final ActorManager actorManager;

	
	public StateMachine<String, String> buildMachine(Order order,
			SimpleDirectedGraph<String, RfsGraphEdge> directedGraph) throws Exception {

		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureConfiguration().withConfiguration()
				.beanFactory(applicationContext.getAutowireCapableBeanFactory())
				.listener(stateMachineListener);
		// builder.configureConfiguration().withConfiguration().beanFactory(new
		// StaticListableBeanFactory()).listener(new StateMachineListener());
		BreadthFirstIterator<String, RfsGraphEdge> graphIterator = new BreadthFirstIterator<>(directedGraph);
		StateConfigurer<String, String> rootStc = builder.configureStates().withStates().initial(START_VERTEX).end(END_VERTEX);

		Queue<QueueElement> forkJoinQueue = new LinkedList<>();
		Map<String, StateConfigurer<String, String>> regionsMap = new HashMap<>();
		graphIterator.forEachRemaining(vertex -> {
			final StateConfigurer<String, String> stc = (regionsMap.containsKey(vertex) && !END_VERTEX.equals(vertex))
					? regionsMap.get(vertex)
					: rootStc;
			if (directedGraph.outDegreeOf(vertex) > 1) {
				QueueElement qe = new QueueElement();
				qe.setVertex(vertex);
				qe.setChildCount(directedGraph.outDegreeOf(vertex));
				stc.fork(FORK.concat(vertex));
				forkJoinQueue.add(qe);

				try {
					ForkTransitionConfigurer<String, String> ftc = builder.configureTransitions().withExternal()
							.source(vertex).target(FORK.concat(vertex)).and().withFork().source(FORK.concat(vertex));
					directedGraph.edgesOf(vertex).stream().filter(e -> vertex.equals(e.getSource())).forEach(e -> {
						ftc.target(e.getTarget());
						try {
							StateConfigurer<String, String> stc2 = stc.and().withStates().parent(FORK.concat(vertex))
									.initial(e.getTarget());
							regionsMap.put(e.getTarget(), stc2);
						} catch (Exception e1) {
							log.error(e1.getMessage());
						}
					});
				} catch (Exception e) {
					log.error(e.getMessage());
				}

			}

			if (directedGraph.inDegreeOf(vertex) > 1) {
				stc.join(JOIN.concat(vertex));
				try {
					JoinTransitionConfigurer<String, String> jtc = builder.configureTransitions().withExternal()
							.source(JOIN.concat(vertex)).target(vertex).and().withJoin().target(JOIN.concat(vertex));
					directedGraph.edgesOf(vertex).stream().filter(e -> vertex.equals(e.getTarget())).forEach(e -> {
						jtc.source(e.getSource().concat(_END));
					});

				} catch (Exception e) {
					log.error(e.getMessage());
				}
				forkJoinQueue.poll();
			}
			if (directedGraph.outDegreeOf(vertex) == 1 && directedGraph.inDegreeOf(vertex) == 1) {
				directedGraph.edgesOf(vertex).stream()
						.filter(e -> (vertex.equals(e.getSource()) && directedGraph.inDegreeOf(e.getTarget()) == 1))
						.forEach(e -> {
							try {
								builder.configureTransitions().withExternal().source(vertex.concat(_END)).target(e.getTarget());
							} catch (Exception ex) {
								log.error(ex.getMessage());
							}
						});
			}
			if (!(vertex.equals(START_VERTEX) || vertex.equals(END_VERTEX))) {
				stc.state(vertex, processRfs(vertex));
				stc.state(vertex.concat(_END));
				try {
					builder.configureTransitions().withExternal().source(vertex).target(vertex.concat(_END)).event(vertex.concat("_EVENT"));
				} catch (Exception ex) {
					log.error(ex.getMessage());
				}
				directedGraph.edgesOf(vertex).stream().filter(e -> (vertex.equals(e.getSource()))).forEach(e -> {
					regionsMap.put(e.getTarget(), stc);
				});
			}

		});


		StateMachine<String, String> machine = builder.build();
		machine.getExtendedState().getVariables().put("ORDER_ID", order.getId());

		return machine;
	}

    public Action<String, String> processRfs(String vertex) {
        return context -> {
        	Long orderId = (Long)context.getExtendedState().getVariables().get("ORDER_ID");
        	actorManager.processRfs(orderId, vertex);
        };
    }
    
	public class QueueElement {
		@Getter
		@Setter
		private String vertex;

		@Getter
		@Setter
		private int childCount;

		@Getter
		@Setter
		private StateConfigurer<String, String> stc;

		@Getter
		@Setter
		private ForkTransitionConfigurer<String, String> ftc;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "QueueElement " + vertex + " " + childCount;
		}

	}

}
