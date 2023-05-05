package org.apache.thrift.async;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.instrumentation.thrift.client.NRThriftHeaders;
import com.newrelic.instrumentation.thrift.client.NewRelicAsyncHeaderSendCall;
import com.newrelic.instrumentation.thrift.client.NewRelicPingAsyncCallback;
import com.newrelic.instrumentation.thrift.client.NewRelicPingData;

@SuppressWarnings("rawtypes")
public class AsyncHeaderSender {

	public static final ConcurrentHashMap<TNonblockingTransport,Boolean> NREnabled = new ConcurrentHashMap<>();
	
	private final SelectThread selectThread;
	private final  ConcurrentLinkedQueue<TAsyncMethodCall> pendingCalls = new ConcurrentLinkedQueue<TAsyncMethodCall>();
	private static final AsyncHeaderSender sender; 
	
	static {
		AsyncHeaderSender instance = null;
		try {
			instance = new AsyncHeaderSender();
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to start AsyncHeaderSemder due to exception");
		}
		sender = instance;
	}
	
	public static boolean sendHeaders(TAsyncMethodCall<?> method) {
		NRThriftHeaders headers = new NRThriftHeaders();
		NewRelic.getAgent().getTransaction().insertDistributedTraceHeaders(headers);
		
		if(!headers.isEmpty()) {
			try {
				// add method and sequence number
				sender.sendHeadersCall(headers, method.client, method.client.___protocolFactory, method.transport);
			} catch (TException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to send headers");
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	public static boolean isNREnabled(TAsyncMethodCall<?> method) {
		TNonblockingTransport transport = method.transport;
		
		if(NREnabled.containsKey(transport)) {
			return NREnabled.get(transport);
		}
		try {
			AsyncHeaderSender sender = new AsyncHeaderSender();
			
			boolean b = sender.pingCall(method.client, method.client.___protocolFactory, method.transport);
			NREnabled.put(transport, b);
			return b;
		} catch (IOException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Check of New Relic Ping failed");
		} catch (TException e) {
			NewRelic.getAgent().getLogger().log(Level.FINE, e, "Check of New Relic Ping failed");
		}
		
		NREnabled.put(transport, Boolean.FALSE);
		return false;
	}

	public AsyncHeaderSender() throws IOException {
		selectThread = new SelectThread();
		selectThread.start();
	}
	
	public void sendHeadersCall(NRThriftHeaders headers, TAsyncClient client, TProtocolFactory protoCallFactory, TNonblockingTransport transport) throws TException {
		long start = System.nanoTime();
		if(!isRunning()) {
			throw new TException("SelectThread is not running");
		}
		
		NewRelicPingData data = new NewRelicPingData();
		HeaderSendData sendData = new HeaderSendData(data);
		
		NewRelicAsyncHeaderSendCall method = new NewRelicAsyncHeaderSendCall(headers, client, protoCallFactory, transport, new NewRelicSenderCallback(sendData)); 
		method.prepareMethodCall();
		pendingCalls.add(method);
		selectThread.getSelector().wakeup();
		
		synchronized(data) {
			try {
				data.wait();
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Interrupted while waiting from send header response");
			}
		}
		long end = System.nanoTime();
		NewRelic.recordMetric("Custom/Newrelic/sendHeaders", TimeUnit.MILLISECONDS.convert(end-start, TimeUnit.NANOSECONDS));
	}
	
	public boolean pingCall(TAsyncClient client, TProtocolFactory protoCallFactory, TNonblockingTransport transport) throws TException {
		if(!isRunning()) {
			throw new TException("SelectThread is not running");
		}
		NewRelicPingData data = new NewRelicPingData();
		NewRelicPingAsyncCallback callback = new NewRelicPingAsyncCallback(data);
		NewRelicAsyncPingCall method = new NewRelicAsyncPingCall(client, protoCallFactory, transport, callback);
		method.prepareMethodCall();
		pendingCalls.add(method);
		selectThread.getSelector().wakeup();
		
		synchronized(data) {
			try {
				data.wait();
			} catch (InterruptedException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e, "Exception while wating for ping completion");
			}
		}
		
		return data.getResult() != null ? data.getResult() : false;
	}
	
	public static class HeaderSendData {
		
		private NewRelicPingData data = null;
		public HeaderSendData(NewRelicPingData d) {
			data = d;
		}
		
		public void setDone() {
			synchronized(data) {
				data.setResult(true);
				data.notifyAll();
			}
		}
		
		public void recordError(Throwable t) {
			NewRelic.getAgent().getLogger().log(Level.FINER, t, "Failed to send headers");
			synchronized(data) {
				data.setResult(false);
				data.notifyAll();
			}
			
		}
	}
	
	public static class NewRelicSenderCallback implements AsyncMethodCallback<Void> {
		
		private HeaderSendData data;
		
		public NewRelicSenderCallback(HeaderSendData d) {
			data = d;
			
		}

		@Override
		public void onComplete(Void response) {
			data.setDone();
		}

		@Override
		public void onError(Exception exception) {
			data.recordError(exception);
		}
		
	}

	public void stop() {
		selectThread.finish();
	}

	public boolean isRunning() {
		return selectThread.isAlive();
	}

	private class SelectThread extends Thread {
		private final Selector selector;
		private volatile boolean running;
		private final TreeSet<TAsyncMethodCall> timeoutWatchSet = new TreeSet<TAsyncMethodCall>(new TAsyncMethodCallTimeoutComparator());

		public SelectThread() throws IOException {
			this.selector = SelectorProvider.provider().openSelector();
			this.running = true;
			this.setName("TAsyncClientManager#SelectorThread " + this.getId());

			// We don't want to hold up the JVM when shutting down
			setDaemon(true);
		}

		public Selector getSelector() {
			return selector;
		}

		public void finish() {
			running = false;
			selector.wakeup();
		}

		public void run() {
			while (running) {
				try {
					try {
						if (timeoutWatchSet.size() == 0) {
							// No timeouts, so select indefinitely
							selector.select();
						} else {
							// We have a timeout pending, so calculate the time until then and select appropriately
							long nextTimeout = timeoutWatchSet.first().getTimeoutTimestamp();
							long selectTime = nextTimeout - System.currentTimeMillis();
							if (selectTime > 0) {
								// Next timeout is in the future, select and wake up then
								selector.select(selectTime);
							} else {
								// Next timeout is now or in past, select immediately so we can time out
								selector.selectNow();
							}
						}
					} catch (IOException e) {
						NewRelic.getAgent().getLogger().log(Level.FINER, e,"Caught IOException in TAsyncClientManager!");
					}
					transitionMethods();
					timeoutMethods();
					startPendingMethods();
				} catch (Exception exception) {
					NewRelic.getAgent().getLogger().log(Level.FINER, exception,"Ignoring uncaught exception in SelectThread");
				}
			}

			try {
				selector.close();
			} catch (IOException ex) {
				NewRelic.getAgent().getLogger().log(Level.FINER, ex,"Could not close selector. This may result in leaked resources!");
			}
		}

		// Transition methods for ready keys
		private void transitionMethods() {
			try {
				Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
				while (keys.hasNext()) {
					SelectionKey key = keys.next();
					keys.remove();
					if (!key.isValid()) {
						// this can happen if the method call experienced an error and the
						// key was cancelled. can also happen if we timeout a method, which
						// results in a channel close.
						// just skip
						continue;
					}
					TAsyncMethodCall methodCall = (TAsyncMethodCall)key.attachment();
					NewRelic.getAgent().getLogger().log(Level.FINE, "Transitioning method: {0}, current state: {1}", methodCall, methodCall.getState());
					methodCall.transition(key);
					NewRelic.getAgent().getLogger().log(Level.FINE, "method transitioned: {0}, current state: {1}", methodCall, methodCall.getState());

					// If done or error occurred, remove from timeout watch set
					if (methodCall.isFinished() || methodCall.getClient().hasError()) {
						timeoutWatchSet.remove(methodCall);
					}
				}
			} catch (ClosedSelectorException e) {
				NewRelic.getAgent().getLogger().log(Level.FINER, e,"Caught ClosedSelectorException in TAsyncClientManager!");
			}
		}

		// Timeout any existing method calls
		private void timeoutMethods() {
			Iterator<TAsyncMethodCall> iterator = timeoutWatchSet.iterator();
			long currentTime = System.currentTimeMillis();
			while (iterator.hasNext()) {
				TAsyncMethodCall methodCall = iterator.next();
				if (currentTime >= methodCall.getTimeoutTimestamp()) {
					iterator.remove();
					methodCall.onError(new TimeoutException("Operation " + methodCall.getClass() + " timed out after " + (currentTime - methodCall.getStartTime()) + " ms."));
				} else {
					break;
				}
			}
		}

		// Start any new calls
		private void startPendingMethods() {
			TAsyncMethodCall methodCall;
			while ((methodCall = pendingCalls.poll()) != null) {
				NewRelic.getAgent().getLogger().log(Level.FINE, "Starting method {0}", methodCall);
				// Catch registration errors. method will catch transition errors and cleanup.
				try {
					methodCall.start(selector);

					// If timeout specified and first transition went smoothly, add to timeout watch set
					TAsyncClient client = methodCall.getClient();
					if (client.hasTimeout() && !client.hasError()) {
						timeoutWatchSet.add(methodCall);
					}
				} catch (Exception exception) {
					NewRelic.getAgent().getLogger().log(Level.FINER, exception,"Caught IOException in TAsyncClientManager!");
					methodCall.onError(exception);
				}
			}
		}
	}

	/** Comparator used in TreeSet */
	private static class TAsyncMethodCallTimeoutComparator implements Comparator<TAsyncMethodCall>, Serializable {
		private static final long serialVersionUID = -4641798648363797143L;

		public int compare(TAsyncMethodCall left, TAsyncMethodCall right) {
			if (left.getTimeoutTimestamp() == right.getTimeoutTimestamp()) {
				return (int)(left.getSequenceId() - right.getSequenceId());
			} else {
				return (int)(left.getTimeoutTimestamp() - right.getTimeoutTimestamp());
			}
		}
	}

}
