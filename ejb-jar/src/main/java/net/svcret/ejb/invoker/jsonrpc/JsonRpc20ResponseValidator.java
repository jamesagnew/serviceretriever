package net.svcret.ejb.invoker.jsonrpc;

import java.io.IOException;
import java.io.StringReader;

import net.svcret.ejb.Messages;
import net.svcret.ejb.api.IResponseValidator;

import org.apache.commons.io.IOUtils;

import com.google.gson.stream.JsonReader;

public class JsonRpc20ResponseValidator implements IResponseValidator {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(JsonRpc20ResponseValidator.class);

	@Override
	public ValidationResponse validate(String theBody, int theStatusCode, String theContentType) {
		if (theStatusCode != 200) {
			return new ValidationResponse(false, Messages.getString("JsonRpc20ResponseValidator.badHttpResponseCode", theStatusCode));
		}

		if (!theContentType.toLowerCase().equals("application/json")) {
			return new ValidationResponse(false, Messages.getString("JsonRpc20ResponseValidator.badContentType", theContentType));
		}

		JsonReader jsonReader = new JsonReader(new StringReader(theBody));
		try {
			jsonReader.beginObject();

//			boolean isFault = false;
			while (jsonReader.hasNext()) {

				String nextName = jsonReader.nextName();

				if (JsonRpc20ServiceInvoker.TOKEN_JSONRPC.equals(nextName)) {

					String rpcVal = jsonReader.nextString();
					if (rpcVal.equals("2.0")) {
						return new ValidationResponse(true);
					} else {
						return new ValidationResponse(false, Messages.getString("JsonRpc20ResponseValidator.badVersion", rpcVal));
					}

				} else if (JsonRpc20ServiceInvoker.TOKEN_RESULT.equals(nextName)) {

					JsonRpc20ServiceInvoker.consumeEqually(jsonReader);

				} else if (JsonRpc20ServiceInvoker.TOKEN_ERROR.equals(nextName)) {

//					isFault = true;
					break;

				} else if (JsonRpc20ServiceInvoker.TOKEN_ID.equals(nextName)) {

					String requestId = jsonReader.nextString();
					ourLog.debug("JsonRpc request ID: {}", requestId);

				}

			}

		} catch (IOException e) {
			return new ValidationResponse(false, Messages.getString("JsonRpc20ResponseValidator.parseError", e.getMessage()));
		} finally {
			IOUtils.closeQuietly(jsonReader);
		}

		return new ValidationResponse(false, Messages.getString("JsonRpc20ResponseValidator.nonJsonRpc20"));
	}
}
