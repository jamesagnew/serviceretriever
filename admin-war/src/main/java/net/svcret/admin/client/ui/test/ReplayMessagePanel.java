package net.svcret.admin.client.ui.test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessage;

public class ReplayMessagePanel extends BaseServiceVersionTestPanel {

	
	public ReplayMessagePanel(long theMessagePid) {
		super();
		
		AdminPortal.MODEL_SVC.loadRecentMessageForServiceVersion(theMessagePid, new AsyncCallback<GRecentMessage>() {
			
			@Override
			public void onSuccess(GRecentMessage theResult) {
				setInitialMessage(theResult.getRequestMessage());
				setServiceVersionPid(theResult.getServiceVersionPid());
				initUi();
			}
			
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}
		});
	}

}
