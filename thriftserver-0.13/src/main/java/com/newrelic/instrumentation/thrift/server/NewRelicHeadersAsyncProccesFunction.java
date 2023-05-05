package com.newrelic.instrumentation.thrift.server;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.newrelicHeaders_args;

public class NewRelicHeadersAsyncProccesFunction {
	

	public NewRelicHeadersAsyncProccesFunction() {
	}

    public void start(Object iface, newrelicHeaders_args args, org.apache.thrift.async.AsyncMethodCallback<Void> resultHandler) throws TException {
    	long startTime = System.nanoTime();
    	NewRelic.getAgent().getTransaction().ignore();
		Map<String, String> headers = args.headers;
		NRThriftHeaders nrHeaders = new NRThriftHeaders();
		nrHeaders.putAll(headers);
		if(NRThriftUtils.isMultiSelect) {
			NRThriftUtils.currentSelectorHeaders.put(NRThriftUtils.currentIn.get(), nrHeaders);
		} else {
			NRThriftUtils.currentHeaders.set(nrHeaders);
		}
		long endTime = System.nanoTime();
		NewRelic.recordMetric("Custom/Newrelic/Header/Processing", TimeUnit.MILLISECONDS.convert(endTime-startTime, TimeUnit.NANOSECONDS));
      }

    public newrelicHeaders_args getEmptyArgsInstance() {
        return new newrelicHeaders_args();
      }

    public AsyncMethodCallback<Void> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
        
        return new AsyncMethodCallback<Void>() { 
          public void onComplete(Void o) {
          }
          public void onError(Exception e) {
          }
        };
      }

}
