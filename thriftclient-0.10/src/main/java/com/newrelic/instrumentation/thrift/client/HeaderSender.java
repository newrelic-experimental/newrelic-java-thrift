package com.newrelic.instrumentation.thrift.client;

import java.util.HashMap;
import java.util.logging.Level;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.newrelicHeaders_args;
import com.newrelic.instrumentation.thrift.client.NewRelicHeaders.pingNewRelic_args;

public class HeaderSender {
	public static HashMap<TProtocol, Boolean> NREnabledConnections = new HashMap<>();

	private static int seq_id = 0;

	public static boolean checkIfNewRelicPresent(TProtocol inProtocol, TProtocol outProtocol) {
		if (NREnabledConnections.containsKey(outProtocol))
			return ((Boolean)NREnabledConnections.get(outProtocol)).booleanValue(); 
		if (outProtocol != null)
			try {
				TMessage outMsg = new TMessage(NRThriftUtils.NEWRELIC_PING, (byte)1, ++seq_id);
				outProtocol.writeMessageBegin(outMsg);
				NewRelicHeaders.pingNewRelic_args args = new pingNewRelic_args();
				args.write(outProtocol);
				outProtocol.writeMessageEnd();
				outProtocol.getTransport().flush();
				TMessage inMsg = inProtocol.readMessageBegin();
				if (inMsg.type == TMessageType.EXCEPTION) {
					TApplicationException x = new TApplicationException();
					x.read(inProtocol);
					inProtocol.readMessageEnd();
					NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable)x, "Failed to find NewRelic Thrift handler due to error in message send");
					NREnabledConnections.put(outProtocol, Boolean.valueOf(false));
					return false;
				} 
				if (inMsg.seqid != outMsg.seqid) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to find NewRelic Thrift handler sequence numbers out of seqeunce");
					return false;
				} 
				NewRelicHeaders.pingNewRelic_result result = new NewRelicHeaders.pingNewRelic_result();
				result.read(inProtocol);
				inProtocol.readMessageEnd();
				boolean b = result.success;
				NREnabledConnections.put(outProtocol, Boolean.valueOf(b));
				return b;
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable)e, "Failed to find NewRelic Thrift handler due to error calling send");
			}  
		NREnabledConnections.put(outProtocol, Boolean.valueOf(false));
		return false;
	}

	public static void attemptToSendHeaders(TProtocol outProtocol, NRThriftHeaders headers) {
		if (NREnabledConnections.containsKey(outProtocol) && 
				headers != null && !headers.isEmpty())
			try {
				TMessage outMsg = new TMessage(NRThriftUtils.NEWRELIC_HEADERS, (byte)4, ++seq_id);
				outProtocol.writeMessageBegin(outMsg);
				NewRelicHeaders.newrelicHeaders_args args = new newrelicHeaders_args();
				args.headers = headers;
				args.write(outProtocol);
				outProtocol.writeMessageEnd();
				outProtocol.getTransport().flush();
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable)e, "Sending of New Relic headers failed due to thrown exception");
			}  
	}
}
