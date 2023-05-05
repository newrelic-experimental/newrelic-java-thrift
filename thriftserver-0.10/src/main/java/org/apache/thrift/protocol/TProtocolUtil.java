package org.apache.thrift.protocol;

import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@Weave
public abstract class TProtocolUtil {

	public static void skip(TProtocol prot, byte type, int maxDepth) {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
			if(NRThriftUtils.asyncCall.get())NRThriftUtils.inNRHeaders.set(false);
		} else {
			Weaver.callOriginal();
		}
		
	}
	
	public static void skip(TProtocol prot, byte type) {
		Weaver.callOriginal();
	}
}
