package org.apache.thrift;

import org.apache.thrift.protocol.TProtocol;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.client.HeaderSender;
import com.newrelic.instrumentation.thrift.client.NRThriftHeaders;

@SuppressWarnings("rawtypes")
@Weave(type=MatchType.BaseClass, originalName = "org.apache.thrift.TServiceClient")
public abstract class TServiceClient_instrumentation {
	
	protected TProtocol oprot_ = Weaver.callOriginal();
	protected TProtocol iprot_ = Weaver.callOriginal();

	@Trace
	protected void sendBase(String methodName, TBase args)  {
		
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","TServiceClient",getClass().getSimpleName(),"sendBase",methodName);
		Weaver.callOriginal();
	}
	
	@Trace
	protected void receiveBase(TBase result, String methodName) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","TServiceClient",getClass().getSimpleName(),"receiveBase",methodName);
		Weaver.callOriginal();
	}
	
	@SuppressWarnings("unused")
	private void sendBase(String methodName, TBase<?,?> args, byte type) {
		if(HeaderSender.checkIfNewRelicPresent(iprot_, oprot_)) {
			NRThriftHeaders headers = new NRThriftHeaders();
			NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
			HeaderSender.attemptToSendHeaders(oprot_, headers);
		}
		Weaver.callOriginal();
	}
}
