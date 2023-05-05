package org.apache.thrift;

import org.apache.thrift.protocol.TProtocol_instrumentation;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class TMultiplexedProcessor {

	@Trace(dispatcher = true)
	public boolean process(TProtocol_instrumentation in, TProtocol_instrumentation out) {
		return Weaver.callOriginal();
	}

}
