package com.cgi.fastordermanager.order.plan.statemachine;

import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.StateMachineBuilder;
import org.springframework.statemachine.config.configurers.StateConfigurer;
import org.springframework.statemachine.guard.Guard;

public class Main {

	public enum States2 {
		S1, S2, S3, S4, S5, SF, SI, S2I, S21, S2F, S22, S3I, S3F, S31, S32, S33, READY, FORK, JOIN, CHOICE, TASKS, T1, T1E, T2, T2E, T3, T3E, ERROR, AUTOMATIC, MANUAL
	}
	// end::snippetB[]

	// tag::snippetC[]
	public enum Events {
		RUN, FALLBACK, CONTINUE, FIX;
	}
	// end::snippetC[]

	public static void main(String[] args) throws Exception {
		StateMachineBuilder.Builder<String, String> builder = StateMachineBuilder.builder();
		builder.configureConfiguration().withConfiguration()//.listener(new StateMachineListener())
				.beanFactory(new StaticListableBeanFactory());
		StateConfigurer<String, String> stc = builder.configureStates().withStates()
				.initial("0")
				.fork("FORK_0")
				.join("JOIN_-1")
				.end("-1")
				.and()
				.withStates()
				.parent("FORK_0")
				.initial("1234", stateAction())
				.state("1234", stateAction())

				.state("123", stateAction())
				.and()
				.withStates()
				.parent("FORK_0")
				//.initial("FORK_0_2")
				.initial("12345", stateAction())
				.state("123456", stateAction());
		
		
		
		builder.configureTransitions()
		.withExternal()
		.source("0")
		.target("FORK_0")
		.and()
		.withExternal()
		.source("1234")
		.target("123")
		.event("a")
		.and()
		.withExternal()
		.source("12345")
		.target("123456")
		.and()
		.withFork()
		.source("FORK_0")
		.target("1234")
		//.target("12345")
		.and()
		.withJoin()
		.source("123456")
		.source("123")
		.target("JOIN_-1")
		.and().withExternal().source("JOIN_-1").target("-1");
		
		StateMachine<String, String> s1 = builder.build();
		s1.start();

		/*
		 * StateMachineBuilder.Builder<States2, Events> builder =
		 * StateMachineBuilder.builder();
		 * builder.configureConfiguration().withConfiguration().listener(new
		 * StateMachineListener<States2,Events>()).beanFactory(new
		 * StaticListableBeanFactory());
		 * 
		 * builder.configureStates().withStates() .initial(States2.S1) .fork(States2.S2)
		 * .join(States2.S4) .end(States2.SF) .and() .withStates() .parent(States2.S2)
		 * .initial(States2.S2I) .state(States2.S21) .state(States2.S22)
		 * .end(States2.S2F) .and() .withStates() .parent(States2.S2)
		 * .initial(States2.S3I) .state(States2.S31) .state(States2.S32)
		 * .end(States2.S3F);
		 * 
		 * builder.configureTransitions().withExternal().source(States2.S1).target(
		 * States2.S2).and().withFork() .source(States2.S2) .target(States2.S22)
		 * .target(States2.S32).and() .withJoin() .source(States2.S2F)
		 * .source(States2.S3F) .target(States2.S4) .and() .withExternal()
		 * .source(States2.S4) .target(States2.S5); StateMachine<States2, Events> s1 =
		 * builder.build(); s1.start();
		 */
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					System.out.println(Thread.activeCount() + " " + s1.isComplete());
					if (!s1.isComplete()) {
						System.err.println("CHODZI");
						s1.sendEvent("a");
					} else {
						System.err.println("NIE CHODZI");
					}
					System.out.println(s1.getState());
					// s1.getTransitions().stream().filter( t ->
					// s1.getState().getId().equals(t.getSource().getId())).map(t ->
					// t.getSource().getId() + "->"
					// +t.getTarget().getId()).forEach(System.out::println);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();
	}

	private static Guard<String, String> stateGuard() {
		return context -> true;
	}

	private static Action<String, String> stateAction() {

		return context -> System.out.println("STATE " + context);
	}
}
