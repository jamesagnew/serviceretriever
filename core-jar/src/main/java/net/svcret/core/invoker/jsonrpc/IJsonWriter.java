package net.svcret.core.invoker.jsonrpc;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

public interface IJsonWriter {

	void setLenient(boolean theB);

	void setSerializeNulls(boolean theB);

	void setIndent(String theString);

	JsonWriter beginObject() throws IOException;

	JsonWriter name(String theNextName) throws IOException;

	JsonWriter value(String theRpcVal) throws IOException;

	JsonWriter nullValue() throws IOException;

	JsonWriter beginArray() throws IOException;

	JsonWriter value(boolean theNextBoolean) throws IOException;

	JsonWriter value(long theNextLong) throws IOException;

	JsonWriter endArray() throws IOException;

	JsonWriter endObject() throws IOException;

	void close()throws IOException;

	JsonWriter value(double theParseDouble) throws IOException;

}
