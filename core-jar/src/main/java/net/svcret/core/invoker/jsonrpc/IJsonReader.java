package net.svcret.core.invoker.jsonrpc;

import java.io.Closeable;
import java.io.IOException;

import com.google.gson.stream.JsonToken;

public interface IJsonReader extends Closeable{

	void setLenient(boolean theB);

	void beginObject() throws IOException;

	boolean hasNext() throws IOException;

	String nextName() throws IOException;

	String nextString() throws IOException;

	void nextNull() throws IOException;

	void beginArray() throws IOException;

	JsonToken peek() throws IOException;

	boolean nextBoolean() throws IOException;

	long nextLong() throws IOException;

	void endArray() throws IOException;

	void endObject() throws IOException;

	void close() throws IOException;

	void beginJsonRpcParams();

	void endJsonRpcParams();

}
