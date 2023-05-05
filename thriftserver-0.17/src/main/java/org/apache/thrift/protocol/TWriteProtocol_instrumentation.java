package org.apache.thrift.protocol;

import org.apache.thrift.TException;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;
import com.newrelic.instrumentation.thrift.server.NRThriftUtils;

@Weave(type = MatchType.Interface, originalName = "org.apache.thrift.protocol.TWriteProtocol")
public abstract class TWriteProtocol_instrumentation {

	public void writeMessageBegin(TMessage message) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeStructBegin(TStruct struct) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldBegin(TField field) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}
	
	public void writeI32(int i32) throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}
	
	public void writeStructEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeFieldStop() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}

	}

	public void writeString(String str) throws TException  {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
		} else {
			Weaver.callOriginal();
		}
	}

	public void writeMessageEnd() throws TException {
		if(NRThriftUtils.inNRHeaders.get()) {
			// Don't read since handlePing has already done this
			// clear the handlePing flag
			NRThriftUtils.inNRHeaders.set(false);
		} else {
			Weaver.callOriginal();
		}
	}
	

}
