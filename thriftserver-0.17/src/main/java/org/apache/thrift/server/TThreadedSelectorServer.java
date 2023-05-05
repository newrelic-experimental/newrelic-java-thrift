package org.apache.thrift.server;

import org.apache.thrift.transport.TNonblockingServerTransport;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRRunnableWrapper;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@Weave
public abstract class TThreadedSelectorServer extends AbstractNonblockingServer {
	
	@Weave
	public static class Args extends AbstractNonblockingServerArgs<Args> {

	    public Args(TNonblockingServerTransport transport) {
	        super(transport);
	      }

	}

	public TThreadedSelectorServer(Args args) {
		super(args);
		NRThriftUtils.isMultiSelect = true;
	}

	protected Runnable getRunnable(FrameBuffer frameBuffer) {
		Runnable r = Weaver.callOriginal();
		
		return new NRRunnableWrapper(NewRelic.getAgent().getTransaction().getToken(), r);
	}

}
