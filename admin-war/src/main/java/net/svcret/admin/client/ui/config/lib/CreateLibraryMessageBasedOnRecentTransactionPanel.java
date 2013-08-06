package net.svcret.admin.client.ui.config.lib;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.stats.DateUtil;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.RecentMessageTypeEnum;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GRecentMessage;

public class CreateLibraryMessageBasedOnRecentTransactionPanel extends BaseEditLibraryMessagePanel {

	public CreateLibraryMessageBasedOnRecentTransactionPanel(RecentMessageTypeEnum theType, long theTransactionPid) {
		switch (theType) {
		case SVCVER:
			AdminPortal.MODEL_SVC.loadRecentMessageForServiceVersion(theTransactionPid, new MyCallback());
			break;
		case USER:
			AdminPortal.MODEL_SVC.loadRecentMessageForUser(theTransactionPid, new MyCallback());
			break;
		}
	}

	@Override
	protected String getDialogTitle() {
		return "Save Transaction in Library";
	}

	public class MyCallback implements AsyncCallback<GRecentMessage> {

		@Override
		public void onFailure(Throwable theCaught) {
			Model.handleFailure(theCaught);
		}

		@Override
		public void onSuccess(GRecentMessage theResult) {
			DtoLibraryMessage message = new DtoLibraryMessage();
			message.setAppliesToServiceVersionPids(theResult.getServiceVersionPid());
			message.setContentType(theResult.getRequestContentType());
			message.setMessage(theResult.getRequestMessage());
			message.setDescription("Transaction from " + DateUtil.formatTime(theResult.getTransactionTime()));
			setContents(message);
		}

	}

}
