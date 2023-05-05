package org.apache.thrift;

import org.apache.thrift.async.AsyncMethodCallback;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransactionNamePriority;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type =MatchType.BaseClass, originalName = "org.apache.thrift.AsyncProcessFunction")
public abstract class AsyncProcessFunction_instrumentation<I, T, R> {
	
	public abstract String getMethodName();

	@Trace
	public void start(I iface, T args, AsyncMethodCallback<R> resultHandler) {
		String mName = getMethodName();
		NewRelic.getAgent().getTransaction().setTransactionName(TransactionNamePriority.FRAMEWORK_HIGH, false, "Thrift-Async", "Thrift","Async",mName);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom", "Thrift","AsyncProcess",mName);
		Weaver.callOriginal();
	}
	
}
