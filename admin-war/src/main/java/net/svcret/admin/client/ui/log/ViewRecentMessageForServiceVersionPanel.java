package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.nav.PagesEnum;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GRecentMessage;

import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ViewRecentMessageForServiceVersionPanel extends BaseViewRecentMessagePanel {


	private long mySvcVerPid;

	public ViewRecentMessageForServiceVersionPanel(long theSvcVerPid, long theMsgPid) {
		super();
		
		mySvcVerPid = theSvcVerPid;
		loadMessage(theMsgPid);
	}

	private void loadMessage(long theMsgPid) {
		MODEL_SVC.loadRecentMessageForServiceVersion(theMsgPid, new AsyncCallback<GRecentMessage>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GRecentMessage theResult) {
				if (theResult != null) {
					setMessage(theResult);
				} else {
					History.newItem(NavProcessor.removeTokens(NavProcessor.getTokenServiceVersionRecentMessages(mySvcVerPid,true), PagesEnum.RSV,PagesEnum.RUS));
				}
			}
		});
	}

	@Override
	protected String getPanelTitle() {
		return MSGS.viewRecentMessageServiceVersion_Title();
	}

}
