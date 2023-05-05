package org.apache.thrift;

import java.util.Map;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.TransportType;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftHeaders;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@SuppressWarnings("rawtypes")
@Weave(originalName = "org.apache.thrift.TBaseAsyncProcessor",type = MatchType.ExactClass)
public abstract class TBaseAsyncProcessor_instrumentation<I> {

	final Map<String,AsyncProcessFunction<I, ? extends TBase,?>> processMap = Weaver.callOriginal();

	public TBaseAsyncProcessor_instrumentation(I iface, Map<String, AsyncProcessFunction<I, ? extends TBase,?>> processMap) {
		if(!NRThriftUtils.isServer) NRThriftUtils.isServer = true; 
	}

	@Trace
	public boolean process(TProtocol in, TProtocol out) {
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","AsyncProcessor",getClass().getName(),"invoke","protocols");
		return Weaver.callOriginal();
	}

	@Trace(dispatcher = true)
	public boolean process(final AsyncFrameBuffer fb)  {

		NRThriftHeaders headers = null;
		if(NRThriftUtils.isMultiSelect) {
			headers = NRThriftUtils.currentSelectorHeaders.remove(fb.getInputProtocol());
		} else {
			headers = NRThriftUtils.currentHeaders.get();
			NRThriftUtils.currentHeaders.remove();
		}
		if(headers != null) {
			NewRelic.getAgent().getTransaction().acceptDistributedTraceHeaders(TransportType.Other, headers);

		}
		NRThriftUtils.asyncCall.set(true);
		NRThriftUtils.currentIn.set(fb.getInputProtocol());
		NRThriftUtils.currentOut.set(fb.getOutputProtocol());
		NRThriftUtils.currentFrameBuffer.set(fb);
		NewRelic.getAgent().getTracedMethod().setMetricName("Custom","AsyncProcessor",getClass().getName(),"invoke","buffer");
		boolean b = Weaver.callOriginal();
		NRThriftUtils.asyncCall.set(false);
		NRThriftUtils.currentIn.remove();;
		NRThriftUtils.currentOut.remove();;
		NRThriftUtils.currentFrameBuffer.remove();
		return b;

	}


}
