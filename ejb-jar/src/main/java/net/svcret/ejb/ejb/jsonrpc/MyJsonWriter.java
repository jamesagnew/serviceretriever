package net.svcret.ejb.ejb.jsonrpc;

import java.io.Writer;

import com.google.gson.stream.JsonWriter;

public class MyJsonWriter extends JsonWriter implements IJsonWriter {

	public MyJsonWriter(Writer theOut) {
		super(theOut);
	}

}
