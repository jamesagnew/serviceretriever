package net.svcret.admin.client.ui.catalog;

import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoServiceVersionHl7OverHttp;
import net.svcret.admin.shared.model.DtoServiceVersionVirtual;
import net.svcret.admin.shared.model.DtoConfig;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.DtoServiceVersionJsonRpc20;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import static net.svcret.admin.client.AdminPortal.*;

public class ServiceCatalogPanel extends FlowPanel {

	private static final int COL_DOMAIN = 0;
	private static final int COL_SERVICE = 1;
	private static final int COL_VERSION = 2;
	private static final int COL_ENDPOINT = 3;
	private static final int COL_BUNDLE = 4;
	
	private LoadingSpinner myLoadingSpinner;
	private FlexTable myGrid;
	private DtoConfig myConfig;

	public ServiceCatalogPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label(MSGS.serviceCatalog_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		// TreeViewModel viewModel = new DashboardTreeViewModel();
		// Object rootValue = Model.getInstance().getDomainList();
		// myDashboardTree = new CellTree(viewModel, rootValue);
		// add(myDashboardTree);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		add(myLoadingSpinner);

		myGrid = new FlexTable();
		add(myGrid);

		myGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		myGrid.setText(0, COL_DOMAIN, "Domain");
		myGrid.setText(0, COL_SERVICE, "Service");
		myGrid.setText(0, COL_VERSION, "Version");
		myGrid.setText(0, COL_ENDPOINT, "Endpoint");
		myGrid.setHTML(0, COL_BUNDLE, "Description<br />Bundle");
		myGrid.getCellFormatter().setHorizontalAlignment(0, COL_BUNDLE, HasHorizontalAlignment.ALIGN_CENTER);
		
		Model.getInstance().loadConfig(new IAsyncLoadCallback<DtoConfig>() {

			@Override
			public void onSuccess(DtoConfig theConfig) {
				myConfig=theConfig;
		
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			
			@Override
			public void onSuccess(GDomainList theResult) {
				myLoadingSpinner.hideCompletely();
				
				initGrid(theResult);
			}
		});
			}
		});

	}

	private void initGrid(GDomainList theResult) {
		
		for (DtoDomain nextDomain : theResult) {
			for (GService nextService : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVersion : nextService.getVersionList()) {
					
					int row = myGrid.getRowCount();
					myGrid.setText(row, COL_DOMAIN, nextDomain.getName());
					myGrid.setText(row, COL_SERVICE, nextService.getName());
					myGrid.setText(row, COL_VERSION, nextVersion.getName());
					
					Widget endpoint = null;
					switch (nextVersion.getProtocol()) {
					case SOAP11:
						endpoint = new EndpointRendererSoap11(myConfig).render((DtoServiceVersionSoap11) nextVersion);
						Anchor anchor = new Anchor();
						anchor.setHref("resources/wsdl_bundle_" + nextVersion.getPid() + ".zip");
						anchor.setText("Download");
						myGrid.setWidget(row, COL_BUNDLE, anchor);
						myGrid.getCellFormatter().setHorizontalAlignment(row, COL_BUNDLE, HasHorizontalAlignment.ALIGN_CENTER);
						break;
					case JSONRPC20:
						endpoint = new EndpointRendererJsonRpc20(myConfig).render((DtoServiceVersionJsonRpc20) nextVersion);
						break;
					case HL7OVERHTTP:
						endpoint = new EndpointRendererHl7OverHttp(myConfig).render((DtoServiceVersionHl7OverHttp) nextVersion);
						break;
					case VIRTUAL:
						endpoint = new EndpointRendererVirtual(myConfig).render((DtoServiceVersionVirtual) nextVersion);
						break;
					}
					
					myGrid.setWidget(row, COL_ENDPOINT, endpoint);
					
				}
			}
		}
		
	}
	
}
