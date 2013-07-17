package net.svcret.admin.client.ui.test;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;

public class ServiceVersionTestPanel extends FlowPanel {

	private TextArea mySendMessageTextArea;
	private ServiceVersionTestResponsePanel myResponsePanel;
	private LoadingSpinner mySendSpinner;
	private long myPid;

	public ServiceVersionTestPanel(long theServiceVersionPid) {
		myPid = theServiceVersionPid;
		
		final FlowPanel mainPanel = new FlowPanel();
		mainPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(mainPanel);

		Label titleLabel = new Label("Service Version Tester");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		mainPanel.add(titleLabel);

		final FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		mainPanel.add(contentPanel);

		final LoadingSpinner initSpinner = new LoadingSpinner();
		contentPanel.add(initSpinner);
		initSpinner.show();

		myResponsePanel = new ServiceVersionTestResponsePanel();
		myResponsePanel.setVisible(false);
		add(myResponsePanel);

		Model.getInstance().loadServiceVersion(theServiceVersionPid, new IAsyncLoadCallback<BaseGServiceVersion>() {

			@Override
			public void onSuccess(BaseGServiceVersion theResult) {
				initSpinner.hideCompletely();
				
				TwoColumnGrid grid = new TwoColumnGrid();
				contentPanel.add(grid);

				grid.addRow("Service", theResult.getParentServiceName());
				grid.addRow("Version", theResult.getId());

				mySendMessageTextArea = new TextArea();
				mySendMessageTextArea.setHeight("100px");
				mySendMessageTextArea.setWidth("100%");
				grid.addRow("Message", mySendMessageTextArea);
				grid.addDescription("Enter the raw message to send here");

				HorizontalPanel controlsPanel = new HorizontalPanel();
				contentPanel.add(controlsPanel);

				PButton sendButton = new PButton("Send");
				sendButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						send();
					}
				});
				controlsPanel.add(sendButton);

				mySendSpinner = new LoadingSpinner("Sending Message...");
				mySendSpinner.hide();
				controlsPanel.add(mySendSpinner);

			}
		});

		
		
		
	}

	
	private void send() {
		mySendSpinner.show();
		myResponsePanel.setVisible(false);
		
		String messageText = mySendMessageTextArea.getText();
		AdminPortal.MODEL_SVC.testServiceVersionWithSingleMessage(messageText, myPid, new AsyncCallback<GServiceVersionSingleFireResponse>() {

			@Override
			public void onFailure(Throwable theCaught) {
				mySendSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(GServiceVersionSingleFireResponse theResult) {
				myResponsePanel.setVisible(true);
				myResponsePanel.setMessage(theResult);
			}
		});
		
	}

}
