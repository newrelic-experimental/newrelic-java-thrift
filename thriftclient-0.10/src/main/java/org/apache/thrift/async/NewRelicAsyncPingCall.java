package org.apache.thrift.async;

import java.util.logging.Level;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.client.NRThriftUtils;
import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.pingNewRelic_args;
import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.pingNewRelic_result;

public class NewRelicAsyncPingCall extends TAsyncMethodCall<Boolean> {

	protected NewRelicAsyncPingCall(TAsyncClient client, TProtocolFactory protocolFactory,TNonblockingTransport transport, AsyncMethodCallback<Boolean> callback) {
		super(client, protocolFactory, transport, callback, false);
	}

	@Override
	protected void write_args(TProtocol prot) throws TException {
        prot.writeMessageBegin(new org.apache.thrift.protocol.TMessage(NRThriftUtils.NEWRELIC_PING, org.apache.thrift.protocol.TMessageType.CALL, 0));
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
        org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
        
        TMessage msg = prot.readMessageBegin();
        if(msg.type == TMessageType.EXCEPTION) {
        	TApplicationException x = new TApplicationException();
            x.read(prot);
            prot.readMessageEnd();
            NewRelic.getAgent().getLogger().log(Level.FINE, x, "Error on call to ping");
        }
        pingNewRelic_result result = new pingNewRelic_result();
        result.read(prot);
        prot.readMessageEnd();
		return result.isSuccess();
	}

}
