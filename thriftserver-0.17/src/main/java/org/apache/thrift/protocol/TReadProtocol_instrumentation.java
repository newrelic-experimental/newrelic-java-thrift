package org.apache.thrift.protocol;

import org.apache.thrift.TException;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@Weave(type = MatchType.Interface, originalName = "org.apache.thrift.protocol.TReadProtocol")
public abstract class TReadProtocol_instrumentation {

	public TMessage readMessageBegin() throws TException {
		TMessage message = Weaver.callOriginal();
		if(message != null && NRThriftUtils.isServer) {
			String name = message.name;
			if(name != null && !name.isEmpty()) {
				if(name.equals(NRThriftUtils.NEWRELIC_HEADERS) || name.equals(NRThriftUtils.NEWRELIC_PING)) {
					if(name.equals(NRThriftUtils.NEWRELIC_PING)) {
						NRThriftUtils.handlePing(NRThriftUtils.currentIn.get(), NRThriftUtils.currentOut.get(), message.seqid);
					} else if(name.equals(NRThriftUtils.NEWRELIC_HEADERS)) {
						NRThriftUtils.getHeaders(NRThriftUtils.currentIn.get(), NRThriftUtils.currentOut.get(), message.seqid);
					}
					message =  new TMessage(message.name, TMessageType.ONEWAY, message.seqid);
				} else {
					NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "ThriftProcessing", "Thrift-Processor",name);
					NewRelic.getAgent().getTracedMethod().setMetricName("Custom","Thrift","Processing",name);
				}
			}
		}
		return message;
	}

	public void readMessageEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}


}
