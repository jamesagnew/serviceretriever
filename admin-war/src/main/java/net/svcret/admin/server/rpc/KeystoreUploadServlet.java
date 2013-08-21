package net.svcret.admin.server.rpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class KeystoreUploadServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(KeystoreUploadServlet.class);

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		upload.setFileSizeMax(10 * FileUtils.ONE_MB);

		try {
			FileItemIterator iter = upload.getItemIterator(request);

			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getFieldName();
				InputStream stream = item.openStream();

				byte[] keystoreBytes = IOUtils.toByteArray(stream);
				ourLog.info("Read keystore file with {} bytes", keystoreBytes);
				
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}