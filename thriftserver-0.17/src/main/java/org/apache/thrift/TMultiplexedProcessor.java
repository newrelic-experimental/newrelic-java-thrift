package org.apache.thrift;

import org.apache.thrift.protocol.TProtocol;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave
public abstract class TMultiplexedProcessor {

	@Trace(dispatcher = true)
	public void process(TProtocol in, TProtocol out) {
		Weaver.callOriginal();
	}

}
