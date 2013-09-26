package net.svcret.admin.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public abstract class BaseRpcServlet extends RemoteServiceServlet{

	private static final long serialVersionUID = 1L;

	public static ModelUpdateServiceMock getMock() {
		return ourMock;
	}

	public static boolean isMockMode() {
		if ("true".equals(System.getProperty("sail.mock"))) {
			if (ourMock == null) {
				ourMock = new ModelUpdateServiceMock();
			}
			return true;
		}
		return false;
	}

	private static ModelUpdateServiceMock ourMock;


}
