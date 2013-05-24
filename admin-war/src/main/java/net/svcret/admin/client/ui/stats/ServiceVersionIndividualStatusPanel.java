package net.svcret.admin.client.ui.stats;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.Sparkline;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.GServiceVersionDetailedStats;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;

public class ServiceVersionIndividualStatusPanel extends FlowPanel {

	private static final int COL_SECFAIL = 4;
	private static final int COL_FAIL = 3;
	private static final int COL_FAULT = 2;
	private static final int COL_SUCCESS = 1;
	private static final int COL_METHOD = 0;
	private BaseGServiceVersion myServiceVersion;

	public ServiceVersionIndividualStatusPanel(long theDomainPid, long theServicePid, long theServiceVersionPid) {
		Model.getInstance().loadServiceVersion(theDomainPid, theServicePid, theServiceVersionPid, true, new IAsyncLoadCallback<BaseGServiceVersion>() {

			@Override
			public void onSuccess(BaseGServiceVersion theResult) {
				myServiceVersion = theResult;
				initTable();
			}
		});
	}

	private void initTable() {
		
		Grid grid = new Grid();
		add(grid);
		grid.addStyleName(CssConstants.PROPERTY_TABLE);
		
		grid.resize(myServiceVersion.getMethodList().size()+1, 5);
		
		grid.setText(0, COL_METHOD, "Method");
		grid.setText(0, COL_SUCCESS, "Success");
		grid.setText(0, COL_FAULT, "Fault");
		grid.setText(0, COL_FAIL, "Fail");
		grid.setText(0, COL_SECFAIL, "Security Fails");
		
		int row=0;
		for (GServiceMethod nextMethod : myServiceVersion.getMethodList()) {
			row++;
			
			grid.setText(row, COL_METHOD, nextMethod.getName());
			
			GServiceVersionDetailedStats detailedStats = myServiceVersion.getDetailedStats();
			grid.setWidget(row, COL_SUCCESS, new Sparkline(detailedStats.getMethodPidToSuccessCount().get(nextMethod.getPid())).withWidth("120px"));
			grid.setWidget(row, COL_FAULT, new Sparkline(detailedStats.getMethodPidToFaultCount().get(nextMethod.getPid())).withWidth("120px"));
			grid.setWidget(row, COL_FAIL, new Sparkline(detailedStats.getMethodPidToFailCount().get(nextMethod.getPid())).withWidth("120px"));
			grid.setWidget(row, COL_SECFAIL, new Sparkline(detailedStats.getMethodPidToSecurityFailCount().get(nextMethod.getPid())).withWidth("120px"));
			
			
		}
		
	}

}
