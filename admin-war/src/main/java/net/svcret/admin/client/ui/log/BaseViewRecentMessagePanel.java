package net.svcret.admin.client.ui.log;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.GRecentMessage;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

public abstract class BaseViewRecentMessagePanel extends BaseViewSavedTransactionPanel {

	private DtoDomainList myDomainList;
	private GRecentMessage myResult;

	public void setMessage(final GRecentMessage theResult) {
		IAsyncLoadCallback<DtoDomainList> callback = new IAsyncLoadCallback<DtoDomainList>() {

			@Override
			public void onSuccess(DtoDomainList theDomainList) {
				myResult = theResult;
				myDomainList = theDomainList;
				
				BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theResult.getServiceVersionPid());
				setSavedTransaction(theResult, svcVer);
				
			}

		};
		Model.getInstance().loadDomainList(callback);

	}


	@Override
	protected void addToStartOfTopGrid() {
		getTopGrid().addHeader("Service Information");

		addServiceVersionInfoToPropertyGrid(getTopGrid(), myDomainList, myResult.getServiceVersionPid());
	}

	@Override
	protected void addToTransactionSectionOfGrid() {
		if (myResult.getAuthorizationOutcome() != null) {
			getTopGrid().addRow("Authorization", new Label(myResult.getAuthorizationOutcome().getDescription()));
		}
		if (StringUtil.isNotBlank(myResult.getRequestHostIp())) {
			getTopGrid().addRow(MSGS.recentMessagesGrid_ColIp(), new Label(myResult.getRequestHostIp()));
		}
	}

	public static void addServiceVersionInfoToPropertyGrid(TwoColumnGrid theGrid, DtoDomainList theDomainList, long theServiceVersionPid) {
		DtoDomain domain = theDomainList.getDomainWithServiceVersion(theServiceVersionPid);
		GService service = theDomainList.getServiceWithServiceVersion(theServiceVersionPid);
		BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theServiceVersionPid);

		theGrid.addRow("Domain", domain.getName());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditDomain(domain.getPid())));
		theGrid.addRow("Service", service.getName());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditService(domain.getPid(), service.getPid())));
		theGrid.addRow("Version", svcVer.getId());
		theGrid.addWidgetToRight(new Hyperlink(AdminPortal.MSGS.actions_Edit(), NavProcessor.getTokenEditServiceVersion(svcVer.getPid())));
	}

}
