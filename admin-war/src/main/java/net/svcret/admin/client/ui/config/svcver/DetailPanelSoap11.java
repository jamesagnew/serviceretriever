package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.DtoServiceVersionSoap11;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class DetailPanelSoap11 extends BaseDetailPanel<DtoServiceVersionSoap11> {

	private LoadingSpinner myLoadWsdlSpinner;
	private TextBox myUrlTextBox;

	/**
	 * Constructor
	 */
	public DetailPanelSoap11(AbstractServiceVersionPanel theParent, DtoServiceVersionSoap11 theServiceVersion) {
		super(theParent, theServiceVersion);
	}

	private void handleLoadWsdl() {
		myLoadWsdlSpinner.show();
		final long start = System.currentTimeMillis();

		AsyncCallback<DtoServiceVersionSoap11> callback = new AsyncCallback<DtoServiceVersionSoap11>() {
			@Override
			public void onFailure(Throwable theCaught) {
				myLoadWsdlSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(DtoServiceVersionSoap11 theResult) {
				long time = System.currentTimeMillis() - start;
				myLoadWsdlSpinner.showMessage("Loaded WSDL in " + time + "ms", false);

				getServiceVersion().merge(theResult);
				updateMethodPanel();

				String navToken = NavProcessor.getTokenAddServiceVersion(getParentPanel().getDomainPid(), getParentPanel().getServicePid(), getServiceVersion().getUncommittedSessionId());
				History.newItem(navToken, false);
			}
		};
		AdminPortal.MODEL_SVC.loadWsdl(getServiceVersion(), getHttpClientConfig(), myUrlTextBox.getValue(), callback);
	}


	private void initWsdlPanel(FlowPanel thePanel) {

		//@formatter:off
		if (getParentPanel().isAddPanel()) {
			thePanel.add(new Label(
					"Every SOAP based service must have a backing WSDL, which provides " + 
					"clients with the service contract being implemented. Enter a URL to a remote WSDL " + 
					"here, and click the \"Load\" button below, and ServiceRetriever will fetch the "
					+ "WSDL and initialize your service."));
		} else {
			thePanel.add(new Label(
					"This box contains the URL to the WSDL that was used to define this " +
					"version of the service initially. Click the 'Load WSDL' button below to reload " +
					"the WSDL, updating the method definitions accordingly."));
		}
		//@formatter:on

		HtmlLabel urlLabel = new HtmlLabel("URL:", "urlTb");
		thePanel.add(urlLabel);

		myUrlTextBox = new TextBox();
		myUrlTextBox.setValue(getServiceVersion().getWsdlLocation());
		myUrlTextBox.getElement().setId("urlTb");
		myUrlTextBox.getElement().getStyle().setWidth(500, Unit.PX);
		myUrlTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> theEvent) {
				getServiceVersion().setWsdlLocation(myUrlTextBox.getValue());
			}
		});
		thePanel.add(myUrlTextBox);

		thePanel.add(new HtmlBr());

		PButton loadButton = new PButton("Load WSDL");
		loadButton.getElement().getStyle().setFloat(Float.LEFT);
		thePanel.add(loadButton);

		myLoadWsdlSpinner = new LoadingSpinner();
		myLoadWsdlSpinner.hideCompletely();
		thePanel.add(myLoadWsdlSpinner);

		loadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				handleLoadWsdl();
			}
		});

		thePanel.add(new HtmlBr());
	}

	@Override
	protected void addProtocolSpecificPanelsToTop(boolean theIsAddPanel) {
		FlowPanel wsdlPanel = new FlowPanel();
		add(wsdlPanel, "WSDL");
		initWsdlPanel(wsdlPanel);
	}

	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.SOAP11;
	}

}
