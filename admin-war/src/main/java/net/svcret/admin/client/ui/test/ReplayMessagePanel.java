package net.svcret.admin.client.ui.test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessage;

public class ReplayMessagePanel extends BaseServiceVersionTestPanel {

	public static final String MSGTYPE_USER = "U";
	public static final String MSGTYPE_SVCVER = "S";
	
	public ReplayMessagePanel(String theMsgType, long theMessagePid) {
		super();

		AsyncCallback<GRecentMessage> asyncCallback = new AsyncCallback<GRecentMessage>() {
			
			@Override
			public void onSuccess(GRecentMessage theResult) {
				setInitialMessage(theResult.getRequestMessage());
				setServiceVersionPid(theResult.getServiceVersionPid());
				initAllPanels();
			}
			
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
		};

		if (MSGTYPE_USER.equals(theMsgType)) {
			AdminPortal.MODEL_SVC.loadRecentMessageForUser(theMessagePid, asyncCallback);
		}else {
			AdminPortal.MODEL_SVC.loadRecentMessageForServiceVersion(theMessagePid, asyncCallback);
		}
	}

}
