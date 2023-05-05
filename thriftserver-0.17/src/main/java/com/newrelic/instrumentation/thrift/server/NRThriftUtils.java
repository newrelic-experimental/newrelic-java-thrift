package com.newrelic.instrumentation.thrift.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializable;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;
import org.apache.thrift.transport.TTransportException;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.newrelicHeaders_args;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_args;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_result;

public class NRThriftUtils {

	public static final String NEWRELIC_PING = "newrelicPing";
	public static final String NEWRELIC_HEADERS = "newrelicHeaders";
	public static boolean isServer = false;
	public static boolean isMultiSelect = false;
	
	public static final String NEWRELIC_IGNORE = "New Relic call to ping or headers";
	
	public static ConcurrentHashMap<TProtocol, NRThriftHeaders> currentSelectorHeaders = new ConcurrentHashMap<TProtocol, NRThriftHeaders>();

	public static ThreadLocal<NRThriftHeaders> currentHeaders = new ThreadLocal<>();
	public static ThreadLocal<Boolean> inNRHeaders = new ThreadLocal<Boolean>() {		
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	public static ThreadLocal<Boolean> asyncCall = new ThreadLocal<Boolean>() {		
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};
	public static ThreadLocal<TProtocol> currentIn = new ThreadLocal<>();
	public static ThreadLocal<TProtocol> currentOut = new ThreadLocal<>();
	public static ThreadLocal<AsyncFrameBuffer> currentFrameBuffer = new ThreadLocal<>();


	public static <I> void handlePing(TProtocol inProtocol, TProtocol outProtocol, int seqId)  {
		if(asyncCall.get()) {
			NewRelicPingAsyncProccesFunction f = new NewRelicPingAsyncProccesFunction();
			pingNewRelic_args args = f.getEmptyArgsInstance();
			try {
				args.read(inProtocol);

				inProtocol.readMessageEnd();
				AsyncMethodCallback<Boolean> resultHandler = f.getResultHandler(currentFrameBuffer.get(), seqId);
				
				f.start(f, args, resultHandler);
				pingNewRelic_result result = new pingNewRelic_result();
				
				
				result.success = true;
				result.setSuccess(true);

				outProtocol.writeMessageBegin(new TMessage(NEWRELIC_PING, TMessageType.REPLY, seqId));
				result.write(outProtocol);
				outProtocol.writeMessageEnd();
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to process ping due to error");
			}
			inNRHeaders.set(true);


		} else {
			NewRelicPingProccesFunction<I> f = new NewRelicPingProccesFunction<>();
			pingNewRelic_args args = f.getEmptyArgsInstance();
			try {
				args.read(inProtocol);
				inProtocol.readMessageEnd();

				TSerializable result = null;
				byte msgType = TMessageType.REPLY;

				result = f.getResult(null, args);

				outProtocol.writeMessageBegin(new TMessage(NEWRELIC_PING,msgType,seqId));
				result.write(outProtocol);
				outProtocol.writeMessageEnd();

				inNRHeaders.set(true);
			} catch (TTransportException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Exception handling New Relic ping");
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Exception handling New Relic ping");
			}
			inNRHeaders.set(true);
		}
	}

	public static void getHeaders(TProtocol inProtocol, TProtocol outProtocol, int seqId) {
		if(asyncCall.get()) {
			NewRelicHeadersAsyncProccesFunction f = new NewRelicHeadersAsyncProccesFunction();
			newrelicHeaders_args args = f.getEmptyArgsInstance();
			try {
				args.read(inProtocol);
				inProtocol.readMessageEnd();
				AsyncMethodCallback<Void> resultHandler = f.getResultHandler(null, seqId);
				
				f.start(f, args, resultHandler);
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Exception handling New Relic headers");
			}
		} else {
			NewRelicHeadersProccesFunction<Void> f = new NewRelicHeadersProccesFunction<>();
			newrelicHeaders_args args = f.getEmptyArgsInstance();
			try {
				args.read(inProtocol);
				
				f.getResult(null, args);
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e, "Exception handling New Relic headers");
			}
			
		}
		inNRHeaders.set(true);
	}

}
