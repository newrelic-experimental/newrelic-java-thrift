package com.newrelic.instrumentation.thrift.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.Headers;

public class NRThriftHeaders extends HashMap<String, String> implements Headers {

	private static final long serialVersionUID = 652984796494946733L;

	@Override
	public HeaderType getHeaderType() {
		return HeaderType.MESSAGE;
	}

	@Override
	public String getHeader(String name) {
		return get(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		List<String> list = new ArrayList<>();
		String value = getHeader(name);
		if(value != null && !value.isEmpty()) {
			list.add(value);
		}
		return list;
	}

	@Override
	public void setHeader(String name, String value) {
		put(name,value);
	}

	@Override
	public void addHeader(String name, String value) {
		put(name,value);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return this.keySet();
	}

	@Override
	public boolean containsHeader(String name) {
		return containsKey(name);
	}

}
