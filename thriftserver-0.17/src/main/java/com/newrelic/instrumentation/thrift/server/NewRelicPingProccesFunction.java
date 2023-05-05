package com.newrelic.instrumentation.thrift.server;

import org.apache.thrift.TException;

import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_args;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_result;

public class NewRelicPingProccesFunction<I> {
	
	public NewRelicPingProccesFunction() {
	}

	protected boolean isOneway() {
		return false;
	}

	public pingNewRelic_result getResult(I iface, pingNewRelic_args args) throws TException {
		pingNewRelic_result result = new pingNewRelic_result();
		result.success = true;
		result.setSuccess(true);
		return result;
	}

	public pingNewRelic_args getEmptyArgsInstance() {
		return new pingNewRelic_args();
	}

	public String getMethodName() {
		return NRThriftUtils.NEWRELIC_PING;
	}


}
