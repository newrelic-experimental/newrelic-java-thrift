package org.apache.thrift;

import org.apache.thrift.protocol.TProtocol;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftHeaders;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@SuppressWarnings("rawtypes")
@Weave(originalName = "org.apache.thrift.ProcessFunction")
public abstract class ProcessFunction_instrumentation<I, T extends TBase> {
	
	 private final String methodName = Weaver.callOriginal();

	@Trace(dispatcher = true)
	public final void process(int seqid, TProtocol iprot, TProtocol oprot, I iface) throws TException {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Thrift","Process",methodName);
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "ThriftProcess", "Thrift","Process",methodName);
		NRThriftHeaders headers = NRThriftUtils.currentHeaders.get();
		if(headers != null) {
			NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, headers);
			NRThriftUtils.currentHeaders.remove();
		}
		Weaver.callOriginal();
	}
}
