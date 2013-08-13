package net.svcret.admin.client.ui.config.lib;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.DtoLibraryMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EditLibraryMessagePanel extends BaseEditLibraryMessagePanel {

	public EditLibraryMessagePanel(long theMessagePid) {
		AdminPortal.MODEL_SVC.loadLibraryMessage(theMessagePid, new MyCallback());
	}

	@Override
	protected String getDialogTitle() {
		return "Edit Message";
	}

	public class MyCallback implements AsyncCallback<DtoLibraryMessage> {

		@Override
		public void onFailure(Throwable theCaught) {
			Model.handleFailure(theCaught);
		}

		@Override
		public void onSuccess(DtoLibraryMessage theResult) {
			setContents(theResult);
		}

	}

}
