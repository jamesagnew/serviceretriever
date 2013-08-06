package net.svcret.admin.client.ui.test;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoLibraryMessage;


public class ReplayLibraryMessagePanel extends BaseServiceVersionTestPanel {

	public ReplayLibraryMessagePanel(long theServiceVersionPid, long theMessagePid) {
		super(theServiceVersionPid, null);
		
		AdminPortal.MODEL_SVC.loadLibraryMessage(theMessagePid, new AsyncCallback<DtoLibraryMessage>(){

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(DtoLibraryMessage theResult) {
				setInitialMessage(theResult.getMessage());
				setInitialContentType(theResult.getContentType());
				initUi();
			}});
	}


}
