package net.svcret.admin.client.ui.config.monitor;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoMonitorRule;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class EditMonitorRulePanel extends BaseMonitorRulePanel {

	public EditMonitorRulePanel(long theRulePid) {
		AdminPortal.MODEL_SVC.loadMonitorRule(theRulePid, new AsyncCallback<BaseDtoMonitorRule>(){
			@Override
			public void onSuccess(BaseDtoMonitorRule theResult) {
				setRule(theResult);
			}

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}});
		
	}

	@Override
	protected String getPanelTitle() {
		return "Edit Monitor Rule";
	}

}
