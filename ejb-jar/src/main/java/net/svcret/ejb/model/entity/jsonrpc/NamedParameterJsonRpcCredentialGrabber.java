package net.svcret.ejb.model.entity.jsonrpc;

import java.io.IOException;

import net.svcret.ejb.api.ICredentialGrabber;
import net.svcret.ejb.ejb.jsonrpc.IJsonReader;
import net.svcret.ejb.ejb.jsonrpc.IJsonWriter;
import net.svcret.ejb.model.entity.soap.BaseCredentialGrabber;

import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class NamedParameterJsonRpcCredentialGrabber extends BaseCredentialGrabber implements ICredentialGrabber {

	private NamedParameterJsonRpcServerAuth myAuth;
	private IJsonReader myJsonReader;
	private IJsonWriter myJsonWriter;
	private String myUsername;
	private String myPassword;

	public NamedParameterJsonRpcCredentialGrabber(NamedParameterJsonRpcServerAuth theAuth, IJsonReader theJsonReader, IJsonWriter theJsonWriter) {
		myAuth = theAuth;
		myJsonReader = theJsonReader;
		myJsonWriter = theJsonWriter;
	}

	@Override
	public String getPassword() {
		return myPassword;
	}

	@Override
	public String getUsername() {
		return myUsername;
	}

	public IJsonReader getWrappedReader() {
		return new IJsonReader() {

			private boolean myGrabPasswordNext;
			private boolean myGrabUsernameNext;
			
			@SuppressWarnings("unused")
			private int myParamLevel; // TODO: should use this

			@Override
			public void beginArray() throws IOException {
				myJsonReader.beginArray();
			}

			@Override
			public void beginJsonRpcParams() {
				myParamLevel = 1;
				myJsonReader.beginJsonRpcParams();
			}

			@Override
			public void beginObject() throws IOException {
				myJsonReader.beginObject();
			}

			@Override
			public void close() throws IOException {
				myJsonReader.close();
			}

			@Override
			public void endArray() throws IOException {
				myJsonReader.endArray();
			}

			@Override
			public void endJsonRpcParams() {
				myParamLevel = 0;
				myJsonReader.endJsonRpcParams();
			}

			@Override
			public void endObject() throws IOException {
				myJsonReader.endObject();
			}

			@Override
			public boolean hasNext() throws IOException {
				return myJsonReader.hasNext();
			}

			@Override
			public boolean nextBoolean() throws IOException {
				reset();
				return myJsonReader.nextBoolean();
			}

			@Override
			public long nextLong() throws IOException {
				reset();
				return myJsonReader.nextLong();
			}

			@Override
			public String nextName() throws IOException {
				reset();
				String retVal = myJsonReader.nextName();
				if (retVal.equals(myAuth.getUsernameParameterName())) {
					myGrabUsernameNext = true;
				} else if (retVal.equals(myAuth.getPasswordParameterName())) {
					myGrabPasswordNext = true;
				}

				return retVal;
			}

			@Override
			public void nextNull() throws IOException {
				reset();
				myJsonReader.nextNull();
			}

			@Override
			public String nextString() throws IOException {
				String retVal = myJsonReader.nextString();
				if (myGrabUsernameNext) {
					myUsername = retVal;
				} else if (myGrabPasswordNext) {
					myPassword = retVal;
				}
				reset();
				return retVal;
			}

			@Override
			public JsonToken peek() throws IOException {
				return myJsonReader.peek();
			}

			@Override
			public void setLenient(boolean theB) {
				myJsonReader.setLenient(theB);
			}

			private void reset() {
				myGrabPasswordNext = false;
				myGrabUsernameNext = false;
			}
		};
	}

	public IJsonWriter getWrappedWriter() {
		return new IJsonWriter() {

			/*
			 * This is kind of hackish, but we return null from all of these
			 * methods since we don't want to return the un-wrapped version and
			 * have someone use it by accident
			 */

			@Override
			public JsonWriter beginArray() throws IOException {
				myJsonWriter.beginArray();
				return null;
			}

			@Override
			public JsonWriter beginObject() throws IOException {
				myJsonWriter.beginObject();
				return null;
			}

			@Override
			public void close() throws IOException {
				myJsonWriter.close();
			}

			@Override
			public JsonWriter endArray() throws IOException {
				myJsonWriter.endArray();
				return null;
			}

			@Override
			public JsonWriter endObject() throws IOException {
				myJsonWriter.endObject();
				return null;
			}

			@Override
			public JsonWriter name(String theNextName) throws IOException {
				myJsonWriter.name(theNextName);
				return null;
			}

			@Override
			public JsonWriter nullValue() throws IOException {
				myJsonWriter.nullValue();
				return null;
			}

			@Override
			public void setIndent(String theString) {
				myJsonWriter.setIndent(theString);
			}

			@Override
			public void setLenient(boolean theB) {
				myJsonWriter.setLenient(theB);
			}

			@Override
			public void setSerializeNulls(boolean theB) {
				myJsonWriter.setSerializeNulls(theB);
			}

			@Override
			public JsonWriter value(boolean theValue) throws IOException {
				myJsonWriter.value(theValue);
				return null;
			}

			@Override
			public JsonWriter value(long theNextLong) throws IOException {
				myJsonWriter.value(theNextLong);
				return null;
			}

			@Override
			public JsonWriter value(String theRpcVal) throws IOException {
				myJsonWriter.value(theRpcVal);
				return null;
			}

			@Override
			public JsonWriter value(double theParseDouble) throws IOException {
				myJsonWriter.value(theParseDouble);
				return null;
			}
		};
	}

}
