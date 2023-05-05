package org.apache.thrift.protocol;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@Weave(originalName = "org.apache.thrift.protocol.TProtocol", type = MatchType.BaseClass)
public abstract class TProtocol_instrumentation {


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

	public void writeMessageBegin(TMessage message) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeStructBegin(TStruct struct) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldBegin(TField field) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}
	
	public void writeI32(int i32) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}
	
	public void writeStructEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldStop() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}

	}

	public void writeString(String str) throws TException  {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeMessageEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
			// clear the handlePing flag
			NRThriftUtils.inNRHeaders.set(false);
		} else {
			Weaver.callOriginal();
		}
	}
	

	public abstract TTransport getTransport();

}
