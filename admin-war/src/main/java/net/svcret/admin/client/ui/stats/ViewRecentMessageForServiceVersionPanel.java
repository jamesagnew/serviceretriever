package net.svcret.admin.client.ui.stats;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessage;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ViewRecentMessageForServiceVersionPanel extends BaseViewRecentMessagePanel {


	public ViewRecentMessageForServiceVersionPanel(long thePid) {
		super(thePid);
	}

	@Override
	protected void loadMessage(long thePid) {
		MODEL_SVC.loadRecentMessageForServiceVersion(thePid, new AsyncCallback<GRecentMessage>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessage theResult) {
				setMessage(theResult);
			}
		});
	}

}
