package com.newrelic.instrumentation.thrift.server;

import java.util.Map;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;

import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.AsyncIface;

@SuppressWarnings({"unchecked", "rawtypes" })
public class NewRelicAsyncIFaceImpl implements AsyncIface {
	
	protected Map<String, String> receivedHeaders = null;

	
	@Override
	public void newrelicHeaders(Map<String, String> headers, AsyncMethodCallback resultHandler) throws TException {
		receivedHeaders = headers;
		NRThriftHeaders nrHeaders = new NRThriftHeaders();
		nrHeaders.putAll(headers);
		
		NRThriftUtils.currentHeaders.set(nrHeaders);
		
		resultHandler.onComplete(null);
	}
	

	@Override
	public void pingNewRelic(AsyncMethodCallback resultHandler) throws TException {
		resultHandler.onComplete(true);
	}

}
