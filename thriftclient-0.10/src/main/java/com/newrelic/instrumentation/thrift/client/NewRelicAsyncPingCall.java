package com.newrelic.instrumentation.thrift.client;

import java.util.logging.Level;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.pingNewRelic_args;


public class NewRelicAsyncPingCall extends TAsyncMethodCall<Boolean> {

	public NewRelicAsyncPingCall(TAsyncClient client, TProtocolFactory protocolFactory,TNonblockingTransport transport, AsyncMethodCallback<Boolean> callback) {
		super(client, protocolFactory, transport, callback, false);
	}

	@Override
	protected void write_args(TProtocol prot) throws TException {
		prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage("ping", org.apache.thrift.protocol.TMessageType.CALL, 0));
		pingNewRelic_args args = new pingNewRelic_args();
		args.write(prot);
		prot.writeMessageEnd();
	}

	@Override
	protected Boolean getResult() throws Exception {
		if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
			throw new java.lang.IllegalStateException("Method call not finished!");
		}
		org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
		org.apache.thrift.protocol.TProtocol inProtocol = client.getProtocolFactory().getProtocol(memoryTransport);
		TMessage inMsg = inProtocol.readMessageBegin();
		if (inMsg.type == TMessageType.EXCEPTION) {
			TApplicationException x = new TApplicationException();
			x.read(inProtocol);
			inProtocol.readMessageEnd();
			NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable)x, "Failed to find NewRelic Thrift handler due to error in message send");
			
			return false;
		} 
		NewRelicHeaders.pingNewRelic_result result = new NewRelicHeaders.pingNewRelic_result();
		result.read(inProtocol);
		inProtocol.readMessageEnd();
		boolean b = result.success;

		return b;
	}

}
