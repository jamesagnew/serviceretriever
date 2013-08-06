package net.svcret.admin.client.ui.config.svcver;

import java.util.Collection;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.DtoLibraryMessage;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class ServiceVersionMessageLibraryPanel extends FlowPanel {

	private FlowPanel myTopPanel;
	private FlowPanel myContentPanel;
	private LoadingSpinner myLoadingSpinner;
	private BaseGServiceVersion myServiceVersion;

	public ServiceVersionMessageLibraryPanel(final long thePid) {
		myTopPanel = new FlowPanel();
		add(myTopPanel);

		myTopPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Message Library");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		myTopPanel.add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		myTopPanel.add(myContentPanel);

		Label intro = new Label("The message library stores transactions for a specific version of a " +
				"service so that they can be used as examples, and replayed for testing.");
		myContentPanel.add(intro);
		
		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		myContentPanel.add(myLoadingSpinner);

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(final GDomainList theDomainList) {
				myServiceVersion = theDomainList.getServiceVersionByPid(thePid);
				
				AdminPortal.MODEL_SVC.loadLibraryMessagesForServiveVersion(thePid, new AsyncCallback<Collection<DtoLibraryMessage>>() {
					@Override
					public void onFailure(Throwable theCaught) {
						Model.handleFailure(theCaught);
					}

					@Override
					public void onSuccess(Collection<DtoLibraryMessage> theMessages) {
						myLoadingSpinner.hideCompletely();
						myContentPanel.add(new ServiceVersionMessageLibraryTablePanel(myServiceVersion, theDomainList, theMessages));
					}
				});
			}
		});
		

		
	}
}
