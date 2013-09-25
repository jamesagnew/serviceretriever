package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.GRecentMessageLists;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ServiceVersionRecentMessagePanel extends BaseRecentMessagesPanel {

	private long myServiceVersionPid;

	public ServiceVersionRecentMessagePanel(long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
		
		AdminPortal.MODEL_SVC.loadRecentTransactionListForServiceVersion(myServiceVersionPid, new AsyncCallback<GRecentMessageLists>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final GRecentMessageLists theResult) {
				Model.getInstance().loadServiceVersion(myServiceVersionPid, new IAsyncLoadCallback<BaseDtoServiceVersion>() {
					@Override
					public void onSuccess(BaseDtoServiceVersion theSvcVer) {
						String title = ("Displaying recent transactions for version " + theSvcVer.getId());
						setRecentMessages(theResult, "Service Version", title);
					}
				});
			}

		});

	}

	@Override
	protected String getDialogTitle() {
		return "Recent Transactions for Service Version";
	}

	
}
