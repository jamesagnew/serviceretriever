package net.svcret.ejb.model.entity.jsonrpc;

import java.io.IOException;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import net.svcret.admin.shared.model.BaseGClientSecurity;
import net.svcret.admin.shared.model.ClientSecurityEnum;
import net.svcret.admin.shared.model.DtoClientSecurityJsonRpcNamedParameter;
import net.svcret.ejb.invoker.jsonrpc.IJsonWriter;
import net.svcret.ejb.model.entity.BasePersObject;
import net.svcret.ejb.model.entity.PersBaseClientAuth;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gson.stream.JsonWriter;

@Entity
@DiscriminatorValue("JSONRPC_NAMPARM")
public class NamedParameterJsonRpcClientAuth extends PersBaseClientAuth<NamedParameterJsonRpcClientAuth> {

	private static final long serialVersionUID = 1L;

	@Column(name = "JSONRPC_NAMEDPARM_PW", length = 100, nullable = true)
	private String myPasswordParameterName;

	@Column(name = "JSONRPC_NAMEDPARM_UN", length = 100, nullable = true)
	private String myUsernameParameterName;

	public NamedParameterJsonRpcClientAuth() {
		super();
	}

	public NamedParameterJsonRpcClientAuth(String theUsername, String theUsernameParameter, String thePassword, String thePasswordParameter) {
		super();
		setUsername(theUsername);
		setUsernameParameterName(theUsernameParameter);
		setPassword(thePassword);
		setPasswordParameterName(thePasswordParameter);
	}

	public IJsonWriter createWrappedWriter(final IJsonWriter theJsonWriter) {
		return new MyJsonWriterWrapper(theJsonWriter);
	}

	@Override
	public ClientSecurityEnum getAuthType() {
		return ClientSecurityEnum.JSONRPC_NAMPARM;
	}

	public String getPasswordParameterName() {
		return myPasswordParameterName;
	}

	public String getUsernameParameterName() {
		return myUsernameParameterName;
	}

	@Override
	public void merge(BasePersObject theObj) {
		super.merge(theObj);

		NamedParameterJsonRpcClientAuth obj = (NamedParameterJsonRpcClientAuth) theObj;
		setUsernameParameterName(obj.getUsernameParameterName());
		setPasswordParameterName(obj.getPasswordParameterName());
	}

	@Override
	protected boolean relevantPropertiesEqual(NamedParameterJsonRpcClientAuth theT) {
		// @formatter:off
		return ObjectUtils.equals(getUsernameParameterName(), theT.getUsernameParameterName()) && ObjectUtils.equals(getPasswordParameterName(), theT.getPasswordParameterName())
				&& ObjectUtils.equals(getUsername(), theT.getUsername()) && ObjectUtils.equals(getPassword(), theT.getPassword());
		// @formatter:on
	}

	public void setPasswordParameterName(String thePasswordParameterName) {
		myPasswordParameterName = thePasswordParameterName;
	}

	public void setUsernameParameterName(String theUsernameParameterName) {
		myUsernameParameterName = theUsernameParameterName;
	}

	private final class MyJsonWriterWrapper implements IJsonWriter {
		private final IJsonWriter myJsonWriter;

		private boolean myHaveDeliveredUsername;
		private boolean myHaveDeliveredPassword;
		private int myParameterStackDepth;

		private boolean myNextElementIsUsername;

		private boolean myNextElementIsPassword;

		private MyJsonWriterWrapper(IJsonWriter theJsonWriter) {
			myJsonWriter = theJsonWriter;
		}

		@Override
		public JsonWriter beginArray() throws IOException {
			if (myNextElementIsUsername) {
				throw new IOException("NamedParameter JSON RPC Client auth failed- Element " + myUsernameParameterName + " contains an array");
			} else if (myNextElementIsPassword) {
				throw new IOException("NamedParameter JSON RPC Client auth failed- Element " + myPasswordParameterName + " contains an array");
			}

			if (myParameterStackDepth > 0) {
				myParameterStackDepth++;
			}

			myJsonWriter.beginArray();
			return null;
		}

		@Override
		public JsonWriter beginObject() throws IOException {
			if (myNextElementIsUsername) {
				throw new IOException("NamedParameter JSON RPC Client auth failed- Element " + myUsernameParameterName + " contains an object");
			} else if (myNextElementIsPassword) {
				throw new IOException("NamedParameter JSON RPC Client auth failed- Element " + myPasswordParameterName + " contains an object");
			}

			if (myParameterStackDepth > 0) {
				myParameterStackDepth++;
			}

			myJsonWriter.beginObject();
			return null;
		}

		@Override
		public void close() throws IOException {
			myJsonWriter.close();
		}

		@Override
		public JsonWriter endArray() throws IOException {
			beforeEndObjOrArray();
			myJsonWriter.endArray();
			return null;
		}

		private void beforeEndObjOrArray() throws IOException {
			if (myParameterStackDepth > 0) {
				myParameterStackDepth--;
				if (myParameterStackDepth == 1) {
					if (!myHaveDeliveredUsername) {
						myJsonWriter.name(getUsernameParameterName());
						myJsonWriter.value(getUsername());
					}
					if (!myHaveDeliveredPassword) {
						myJsonWriter.name(getPasswordParameterName());
						myJsonWriter.value(getPassword());
					}
				}
			}
		}

		@Override
		public JsonWriter endObject() throws IOException {
			beforeEndObjOrArray();
			myJsonWriter.endObject();
			return null;
		}

		@Override
		public JsonWriter name(String theNextName) throws IOException {
			if ("params".equals(theNextName) && myParameterStackDepth == 0) {
				myParameterStackDepth = 1;
			} else if (myParameterStackDepth > 0) {
				if (myUsernameParameterName.equals(theNextName)) {
					myNextElementIsUsername = true;
					myHaveDeliveredUsername = true;
					myJsonWriter.name(theNextName);
					myJsonWriter.value(getUsername());
					return null;
				} else if (myPasswordParameterName.equals(theNextName)) {
					myNextElementIsPassword = true;
					myHaveDeliveredPassword = true;
					myJsonWriter.name(theNextName);
					myJsonWriter.value(getPassword());
					return null;
				}
			}

			myJsonWriter.name(theNextName);
			return null;
		}

		@Override
		public JsonWriter nullValue() throws IOException {
			if (myNextElementIsUsername) {
				myNextElementIsUsername = false;
				return null;
			} else if (myNextElementIsPassword) {
				myNextElementIsPassword = false;
				return null;
			}
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
			if (myNextElementIsUsername) {
				myNextElementIsUsername = false;
				return null;
			} else if (myNextElementIsPassword) {
				myNextElementIsPassword = false;
				return null;
			}
			myJsonWriter.value(theValue);
			return null;
		}

		@Override
		public JsonWriter value(double theParseDouble) throws IOException {
			if (myNextElementIsUsername) {
				myNextElementIsUsername = false;
				return null;
			} else if (myNextElementIsPassword) {
				myNextElementIsPassword = false;
				return null;
			}
			myJsonWriter.value(theParseDouble);
			return null;
		}

		@Override
		public JsonWriter value(long theNextLong) throws IOException {
			if (myNextElementIsUsername) {
				myNextElementIsUsername = false;
				return null;
			} else if (myNextElementIsPassword) {
				myNextElementIsPassword = false;
				return null;
			}
			myJsonWriter.value(theNextLong);
			return null;
		}

		@Override
		public JsonWriter value(String theRpcVal) throws IOException {
			if (myNextElementIsUsername) {
				myNextElementIsUsername = false;
				return null;
			} else if (myNextElementIsPassword) {
				myNextElementIsPassword = false;
				return null;
			}
			myJsonWriter.value(theRpcVal);
			return null;
		}
	}

	@Override
	protected PersBaseClientAuth<?> doMerge(PersBaseClientAuth<?> theObj) {
		NamedParameterJsonRpcClientAuth obj = (NamedParameterJsonRpcClientAuth) theObj;
		NamedParameterJsonRpcClientAuth retVal = new NamedParameterJsonRpcClientAuth();
		
		retVal.setUsernameParameterName(obj.getUsernameParameterName());
		retVal.setUsername(obj.getUsername());
		
		retVal.setPasswordParameterName(obj.getPasswordParameterName());
		retVal.setPassword(obj.getPassword());
		
		return retVal;
	}

	@Override
	protected BaseGClientSecurity createDtoAndPopulateWithTypeSpecificFields() {
		DtoClientSecurityJsonRpcNamedParameter retVal = new DtoClientSecurityJsonRpcNamedParameter();
		retVal.setUsernameParameterName(getUsernameParameterName());
		retVal.setUsername(getUsername());
		retVal.setPasswordParameterName(getPasswordParameterName());
		retVal.setPassword(getPassword());
		return retVal;
	}

}
