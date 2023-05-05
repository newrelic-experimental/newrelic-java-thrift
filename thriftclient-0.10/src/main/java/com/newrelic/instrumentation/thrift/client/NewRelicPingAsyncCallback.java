package com.newrelic.instrumentation.thrift.client;

import java.util.logging.Level;

import org.apache.thrift.async.AsyncMethodCallback;

import com.newrelic.api.agent.NewRelic;

public class NewRelicPingAsyncCallback implements AsyncMethodCallback<Boolean> {
	
	private NewRelicPingData data;
	
	public NewRelicPingAsyncCallback(NewRelicPingData d) {
		data = d;
	}

	@Override
	public void onComplete(Boolean response) {
		data.setResult(response);
		synchronized(data) {
			data.notifyAll();
		}
	}

	@Override
	public void onError(Exception exception) {
		data.setResult(false);
		NewRelic.getAgent().getLogger().log(Level.FINER, exception, "Call to New Relic Ping resulted in an exception");
		synchronized(data) {
			data.notifyAll();
		}
	}

}
