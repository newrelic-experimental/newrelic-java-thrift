package org.apache.thrift.async;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "org.apache.thrift.async.TAsyncClientManager")
public abstract class TAsyncClientManager_instrumentation {
	
	@Trace
	public void call(TAsyncMethodCall<?> method) {
		if(AsyncHeaderSender.isNREnabled(method)) {
			AsyncHeaderSender.sendHeaders(method);
		}
		Weaver.callOriginal();
	}
	
}
