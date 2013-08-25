package net.svcret.admin.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public abstract class BaseRpcServlet extends RemoteServiceServlet{

	private static final long serialVersionUID = 1L;

	protected ModelUpdateServiceMock getMock() {
		return myMock;
	}

	protected boolean isMockMode() {
		if ("true".equals(System.getProperty("sail.mock"))) {
			if (myMock == null) {
				myMock = new ModelUpdateServiceMock();
			}
			return true;
		}
		return false;
	}

	private ModelUpdateServiceMock myMock;


}
