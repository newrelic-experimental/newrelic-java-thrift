package com.newrelic.instrumentation.thrift.server;

import com.newrelic.agent.bridge.AgentBridge;
import com.newrelic.api.agent.Token;
import com.newrelic.api.agent.Trace;

public class NRRunnableWrapper implements Runnable {
	
	private Token token = null;
	private Runnable actual = null;
	private static boolean isTransformed = false;
	
	public NRRunnableWrapper(Token t,Runnable r) {
		token = t;
		actual = r;
		if(!isTransformed) {
			isTransformed = true;
			AgentBridge.instrumentation.retransformUninstrumentedClass(getClass());
		}
	}

	@Override
	@Trace(async=true)
	public void run() {
		if(token != null) {
			token.linkAndExpire();
			token = null;
		}
		actual.run();
	}

}
