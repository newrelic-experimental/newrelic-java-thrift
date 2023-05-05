package com.newrelic.instrumentation.thrift.client;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncMethodCall;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;

import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.newrelicHeaders_args;


public class NewRelicAsyncHeaderSendCall extends TAsyncMethodCall<Void> {

	private java.util.Map<java.lang.String,java.lang.String> headers;

	public NewRelicAsyncHeaderSendCall(java.util.Map<java.lang.String,java.lang.String> h,TAsyncClient client, TProtocolFactory protocolFactory, TNonblockingTransport transport, AsyncMethodCallback<Void> callback) {
		super(client, protocolFactory, transport, callback, true);
		headers = h;
	}

	@Override
	protected void write_args(TProtocol protocol) throws TException {
		protocol.writeMessageBegin(new org.apache.thrift.protocol.TMessage(NRThriftUtils.NEWRELIC_HEADERS, org.apache.thrift.protocol.TMessageType.ONEWAY, 0));
		newrelicHeaders_args args = new newrelicHeaders_args();
		args.setHeaders(headers);
		args.write(protocol);
		protocol.writeMessageEnd();
	}

	@Override
	protected Void getResult() throws Exception {
		if (getState() != org.apache.thrift.async.TAsyncMethodCall.State.RESPONSE_READ) {
			throw new java.lang.IllegalStateException("Method call not finished!");
		}
		org.apache.thrift.transport.TMemoryInputTransport memoryTransport = new org.apache.thrift.transport.TMemoryInputTransport(getFrameBuffer().array());
		@SuppressWarnings("unused")
		org.apache.thrift.protocol.TProtocol prot = client.getProtocolFactory().getProtocol(memoryTransport);
		return null;
	}

}
