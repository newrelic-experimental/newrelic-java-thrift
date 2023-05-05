package com.newrelic.instrumentation.thrift.server;

import java.util.Map;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.newrelicHeaders_args;

public class NewRelicHeadersProccesFunction<I> {


	public NewRelicHeadersProccesFunction() {
	}
	
	

	protected boolean isOneway() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public TBase getResult(I iface, newrelicHeaders_args args) throws TException {
		Map<String, String> headers = args.headers;
		if(headers != null && !headers.isEmpty()) {
			NRThriftHeaders thriftHeaders = new NRThriftHeaders();
			thriftHeaders.putAll(headers);
			NRThriftUtils.currentHeaders.set(thriftHeaders);
		}
		return null;
	}

	public newrelicHeaders_args getEmptyArgsInstance() {
		return new newrelicHeaders_args();
	}

	public String getMethodName() {
		return NRThriftUtils.NEWRELIC_HEADERS;
	}


}
