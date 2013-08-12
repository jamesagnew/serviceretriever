package net.svcret.admin.client.ui.test;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionSingleFireResponse;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;

public abstract class BaseServiceVersionTestPanel extends FlowPanel {

	private ListBox myDomainBox;
	private GDomainList myDomainList;
	private String myInitialMessage;
	private ServiceVersionTestResponsePanel myResponsePanel;
	private PButton mySendButton;
	private TextArea mySendMessageTextArea;
	private LoadingSpinner mySendSpinner;
	private Long myServiceVersionPid;
	private ListBox myServiceBox;
	private ListBox myVersionBox;
	private String myInitialContentType;
	private TextBox myContentTypeBox;

	public BaseServiceVersionTestPanel() {
		// nothing
	}

	protected void setInitialContentType(String theContentType) {
		myInitialContentType = theContentType;
	}

	public BaseServiceVersionTestPanel(Long theServiceVersionPid, String theInitialMessage) {
		myServiceVersionPid = theServiceVersionPid;
		myInitialMessage = theInitialMessage;
	}

	public Long getServiceVersionPid() {
		return myServiceVersionPid;
	}

	public void setInitialMessage(String theInitialMessage) {
		myInitialMessage = theInitialMessage;
	}

	public void setServiceVersionPid(Long theServiceVersionPid) {
		myServiceVersionPid = theServiceVersionPid;
	}

	private void handleDomainBoxChange() {
		myServiceBox.clear();
		if (myDomainBox.getSelectedIndex() != -1) {
			GDomain domain = myDomainList.getDomainByPid(Long.parseLong(myDomainBox.getValue(myDomainBox.getSelectedIndex())));
			for (GService nextSvc : domain.getServiceList()) {
				myServiceBox.addItem(nextSvc.getName(), Long.toString(nextSvc.getPid()));
			}
		}
		handleServiceBoxChange();
	}

	private void handleServiceBoxChange() {
		myVersionBox.clear();
		if (myServiceBox.getSelectedIndex() != -1) {
			GDomain domain = myDomainList.getDomainByPid(Long.parseLong(myDomainBox.getValue(myDomainBox.getSelectedIndex())));
			GService service = domain.getServiceList().getServiceByPid(Long.parseLong(myServiceBox.getValue(myServiceBox.getSelectedIndex())));
			for (BaseGServiceVersion nextVer : service.getVersionList()) {
				myVersionBox.addItem(nextVer.getId(), Long.toString(nextVer.getPid()));
			}
		}

		handleVersionBoxChange();
	}

	private void handleVersionBoxChange() {
		if (myVersionBox.getSelectedIndex() == -1) {
			myServiceVersionPid = null;
			mySendButton.setEnabled(false);
		} else {
			myServiceVersionPid = Long.parseLong(myVersionBox.getValue(myVersionBox.getSelectedIndex()));
			mySendButton.setEnabled(true);
		}

		if (myInitialContentType != null) {
			myContentTypeBox.setValue(myInitialContentType);
		} else {
			Model.getInstance().loadServiceVersion(myServiceVersionPid, false, new IAsyncLoadCallback<BaseGServiceVersion>() {
				@Override
				public void onSuccess(BaseGServiceVersion theResult) {
					myContentTypeBox.setValue(theResult.getProtocol().getRequestContentType());
				}
			});
		}
	}

	private void send() {
		mySendSpinner.show();
		myResponsePanel.setVisible(false);

		String messageText = mySendMessageTextArea.getText();
		String contentType = myContentTypeBox.getValue();

		AdminPortal.MODEL_SVC.testServiceVersionWithSingleMessage(messageText, contentType, myServiceVersionPid, new AsyncCallback<GServiceVersionSingleFireResponse>() {

			@Override
			public void onFailure(Throwable theCaught) {
				mySendSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(GServiceVersionSingleFireResponse theResult) {
				mySendSpinner.hide();
				myResponsePanel.setVisible(true);
				myResponsePanel.setMessage(theResult);
			}
		});

	}

	protected void initUi() {
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

		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

			@Override
			public void onSuccess(final GDomainList theDomainList) {
				myDomainList = theDomainList;
				initSpinner.hideCompletely();

				TwoColumnGrid grid = new TwoColumnGrid();
				grid.setWidth("100%");
				grid.setMaximizeSecondColumn();
				contentPanel.add(grid);

				myDomainBox = new ListBox(false);
				grid.addRow("Domain", myDomainBox);
				myDomainBox.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						handleDomainBoxChange();
					}
				});

				myServiceBox = new ListBox(false);
				grid.addRow("Service", myServiceBox);
				myServiceBox.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						handleServiceBoxChange();
					}
				});

				myVersionBox = new ListBox(false);
				grid.addRow("Version", myVersionBox);
				myVersionBox.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent theEvent) {
						handleVersionBoxChange();
					}
				});

				myContentTypeBox = new TextBox();
				grid.addRow("Content Type", myContentTypeBox);

				mySendMessageTextArea = new TextArea();
				mySendMessageTextArea.setText(myInitialMessage);
				mySendMessageTextArea.setHeight("200px");
				mySendMessageTextArea.setWidth("100%");
				grid.addRow("Message", mySendMessageTextArea);
				grid.addDescription("Enter the raw message to send here");

				HorizontalPanel controlsPanel = new HorizontalPanel();
				contentPanel.add(controlsPanel);

				mySendButton = new PButton("Send");
				mySendButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent theEvent) {
						send();
					}
				});
				controlsPanel.add(mySendButton);

				Long domainPid = null;
				Long servicePid = null;
				if (myServiceVersionPid != null) {
					domainPid = theDomainList.getDomainPidWithServiceVersion(myServiceVersionPid);
					servicePid = theDomainList.getServicePidWithServiceVersion(myServiceVersionPid);
				}
				for (GDomain nextDomain : theDomainList) {
					myDomainBox.addItem(nextDomain.getName(), Long.toString(nextDomain.getPid()));
				}

				if (domainPid != null) {
					for (int i = 0; i < myDomainBox.getItemCount(); i++) {
						if (myDomainBox.getValue(i).equals(Long.toString(domainPid))) {
							myDomainBox.setSelectedIndex(i);
						}
					}
				}
				handleDomainBoxChange();
				if (servicePid != null) {
					for (int i = 0; i < myServiceBox.getItemCount(); i++) {
						if (myServiceBox.getValue(i).equals(Long.toString(servicePid))) {
							myServiceBox.setSelectedIndex(i);
						}
					}
					handleServiceBoxChange();
				}

				mySendSpinner = new LoadingSpinner("Sending Message...");
				mySendSpinner.hide();
				controlsPanel.add(mySendSpinner);

			}
		});
	}

}
