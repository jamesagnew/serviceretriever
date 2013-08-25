package net.svcret.admin.server.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.svcret.admin.shared.ConstantsHttpClientConfig;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class HttpClientConfigUploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(HttpClientConfigUploadServlet.class);

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ourLog.info("Handling post with params: " + request.getParameterMap());

		ServletFileUpload upload = new ServletFileUpload();
		upload.setFileSizeMax(10 * FileUtils.ONE_MB);

		String configPid = null;
		byte[] keystoreBytes = null;
		String password = null;
		String typeString = null;

		try {
			FileItemIterator iter = upload.getItemIterator(request);

			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();

				if (ConstantsHttpClientConfig.FIELD_TYPE.equals(name)) {
					typeString = IOUtils.toString(stream);
				} else if (ConstantsHttpClientConfig.FIELD_FILEUPLOAD.equals(name)) {
					keystoreBytes = IOUtils.toByteArray(stream);
				} else if (ConstantsHttpClientConfig.FIELD_PASSWORD.equals(name)) {
					password = URLDecoder.decode(IOUtils.toString(stream), "US-ASCII");
				} else if (ConstantsHttpClientConfig.FIELD_CONFIG_PID.equals(name)) {
					configPid = URLDecoder.decode(IOUtils.toString(stream), "US-ASCII");
				}

			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		ourLog.info("Read {} file for config PID {} with {} bytes and password {}", new Object[] { typeString, configPid, keystoreBytes.length, password });

		HttpClientConfigServiceImpl.SessionUploadedKeystore sessionKeystore = new HttpClientConfigServiceImpl.SessionUploadedKeystore();
		sessionKeystore.setKeystore(keystoreBytes);
		sessionKeystore.setPassword(password);

		String sessionVar;
		if (ConstantsHttpClientConfig.TYPE_KEYSTORE.equals(typeString)) {
			sessionVar = HttpClientConfigServiceImpl.SESSVAR_PREFIX_KEYSTORE + configPid;
		} else if (ConstantsHttpClientConfig.TYPE_TRUSTSTORE.equals(typeString)) {
			sessionVar = HttpClientConfigServiceImpl.SESSVAR_PREFIX_TRUSTSTORE + configPid;
		}else {
			throw new ServletException("Unknown type");
		}

		ourLog.info("Saving session attribute: {}", sessionVar);
		HttpSession session = request.getSession(true);
		session.setAttribute(sessionVar, sessionKeystore);
		
		response.setStatus(200);
		response.setContentType("text/plain");
		response.getWriter().print("Success");

	}
}