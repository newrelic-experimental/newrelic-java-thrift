package com.newrelic.instrumentation.thrift.server;

import java.util.logging.Level;

import org.apache.thrift.TException;
import org.apache.thrift.TSerializable;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.AbstractNonblockingServer.AsyncFrameBuffer;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_args;
import com.newrelic.instrumentation.thrift.server.NewRelicHeaders.pingNewRelic_result;

public class NewRelicPingAsyncProccesFunction  {
	
	NewRelicAsyncIFaceImpl impl = new NewRelicAsyncIFaceImpl();
	
	public NewRelicPingAsyncProccesFunction() {
		
	}

	protected boolean isOneway() {
		return false;
	}
	   
	public String getMethodName() {
	        return NRThriftUtils.NEWRELIC_PING;
	    }

 	
    @SuppressWarnings("rawtypes")
	public AsyncMethodCallback<Boolean> getResultHandler(final AsyncFrameBuffer fb, final int seqid) {
        final NewRelicPingAsyncProccesFunction  fcall = this;
        return new AsyncMethodCallback<Boolean>() { 
          public void onComplete(Boolean o) {
            pingNewRelic_result result = new pingNewRelic_result();
            result.success = o;
            result.setSuccessIsSet(true);
            try {
              fcall.sendResponse(fb,result, org.apache.thrift.protocol.TMessageType.REPLY,seqid);
              return;
            } catch (Exception e) {
            	NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Exception writing to internal frame buffer");
            }
            fb.close();
            
          }
		public void onError(Exception e) {
            byte msgType = org.apache.thrift.protocol.TMessageType.REPLY;
            org.apache.thrift.TBase msg;
            {
              msgType = org.apache.thrift.protocol.TMessageType.EXCEPTION;
              msg = (org.apache.thrift.TBase)new org.apache.thrift.TApplicationException(org.apache.thrift.TApplicationException.INTERNAL_ERROR, e.getMessage());
            }
            try {
              fcall.sendResponse(fb,msg,msgType,seqid);
              return;
            } catch (Exception ex) {
            	NewRelic.getAgent().getLogger().log(Level.FINEST, e, "Exception writing to internal frame buffer");
            }
            fb.close();
          }
        };
	}
	
	public pingNewRelic_args getEmptyArgsInstance() {
		return new pingNewRelic_args();
	}

	public <I> void start(I iface, pingNewRelic_args args, AsyncMethodCallback<Boolean> resultHandler) throws TException {
		NewRelic.getAgent().getTransaction().ignore();
    	
		impl.pingNewRelic(resultHandler);
	}

    public void sendResponse(final AbstractNonblockingServer.AsyncFrameBuffer fb, final TSerializable result, final byte type, final int seqid) throws TException {
        TProtocol oprot = fb.getOutputProtocol();

        oprot.writeMessageBegin(new TMessage(getMethodName(), type, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();

        //fb.responseReady();
    }

}
