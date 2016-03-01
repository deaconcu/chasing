package com.prosper.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

	private ObjectMapper objectMapper = new ObjectMapper();

	public ObjectMapper getObjectMapper() {
		return objectMapper;
	}

}
