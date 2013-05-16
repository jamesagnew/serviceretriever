package net.svcret.ejb.ejb.jsonrpc;

import java.io.Reader;

import com.google.gson.stream.JsonReader;

public class MyJsonReader extends JsonReader implements IJsonReader {

	public MyJsonReader(Reader theIn) {
		super(theIn);
	}

	@Override
	public void beginJsonRpcParams() {
		// ignore
	}

	@Override
	public void endJsonRpcParams() {
		// ignore
	}

}
