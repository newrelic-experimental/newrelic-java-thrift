package org.apache.thrift;

import java.util.Map;

import org.apache.thrift.protocol.TProtocol;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@SuppressWarnings({ "unused", "rawtypes" })
@Weave(type = MatchType.ExactClass, originalName = "org.apache.thrift.TBaseProcessor")
public abstract class TBaseProcessor_instrumentation<I> {
	
	private final Map<String,ProcessFunction<I, ? extends TBase>> processMap = Weaver.callOriginal();

	protected TBaseProcessor_instrumentation(I iface, Map<String, ProcessFunction<I, ? extends TBase>> processFunctionMap) {
		if(!NRThriftUtils.isServer) NRThriftUtils.isServer = true; 
		
	}
	
	public boolean process(TProtocol in, TProtocol out) {
		NRThriftUtils.asyncCall.set(false);
		NRThriftUtils.currentIn.set(in);
		NRThriftUtils.currentOut.set(out);
		boolean b = Weaver.callOriginal();
		NRThriftUtils.currentIn.remove();
		NRThriftUtils.currentOut.remove();
		return b;
	}
}
