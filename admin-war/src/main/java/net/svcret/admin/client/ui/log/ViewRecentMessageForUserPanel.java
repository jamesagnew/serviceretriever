package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessage;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ViewRecentMessageForUserPanel extends BaseViewRecentMessagePanel {


	private long myUserPid;

	public ViewRecentMessageForUserPanel(long theUserPid, long theMessagePid) {
		super();
		
		myUserPid = theUserPid;
		loadMessage(theMessagePid);
	}

	@Override
	protected String getPanelTitle() {
		return MSGS.viewRecentMessageUser_Title();
	}

	private void loadMessage(long thePid) {
		MODEL_SVC.loadRecentMessageForUser(thePid, new AsyncCallback<GRecentMessage>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessage theResult) {
				if (theResult == null) {
					History.newItem(NavProcessor.removeTokens(NavProcessor.getTokenUserRecentMessages(myUserPid, true), PagesEnum.RSV,PagesEnum.RUS));
				}else {
					setMessage(theResult);
				}
			}
		});
	}

}
