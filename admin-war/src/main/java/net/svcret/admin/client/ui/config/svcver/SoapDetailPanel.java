package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.HtmlBr;
import net.svcret.admin.client.ui.components.HtmlLabel;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.shared.model.GSoap11ServiceVersion;
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

public class SoapDetailPanel extends BaseDetailPanel<GSoap11ServiceVersion> {

	private LoadingSpinner myLoadWsdlSpinner;
	private TextBox myUrlTextBox;

	/**
	 * Constructor
	 */
	public SoapDetailPanel(AbstractServiceVersionPanel theParent, GSoap11ServiceVersion theServiceVersion) {
		super(theParent, theServiceVersion);
	}


	private void handleLoadWsdl() {
		myLoadWsdlSpinner.show();
		final long start = System.currentTimeMillis();

		AsyncCallback<GSoap11ServiceVersion> callback = new AsyncCallback<GSoap11ServiceVersion>() {
			@Override
			public void onFailure(Throwable theCaught) {
				myLoadWsdlSpinner.showMessage(theCaught.getMessage(), false);
			}

			@Override
			public void onSuccess(GSoap11ServiceVersion theResult) {
				long time = System.currentTimeMillis() - start;
				myLoadWsdlSpinner.showMessage("Loaded WSDL in " + time + "ms", false);

				getServiceVersion().merge(theResult);
				updateMethodPanel();
				updateUrlPanel();

				String navToken = NavProcessor.getTokenAddServiceVersion(true, getParentPanel().getDomainPid(), getParentPanel().getServicePid(), getServiceVersion().getUncommittedSessionId());
				History.newItem(navToken, false);
			}
		};
		AdminPortal.MODEL_SVC.loadWsdl(getServiceVersion(), myUrlTextBox.getValue(), callback);
	}



	private void initWsdlPanel(FlowPanel wsdlPanel) {
		wsdlPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("WSDL Location");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		wsdlPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		wsdlPanel.add(contentPanel);

		contentPanel.add(new Label("Every SOAP based service must have a backing WSDL, which provides " + "clients with the service contract being implemented. Enter a URL to a remote WSDL " + "here, and click the \"Load\" button below, and ServiceRetriever will fetch the "
				+ "WSDL and initialize your service."));

		HtmlLabel urlLabel = new HtmlLabel("URL:", "urlTb");
		contentPanel.add(urlLabel);

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
		contentPanel.add(myUrlTextBox);

		contentPanel.add(new HtmlBr());

		PButton loadButton = new PButton("Load WSDL");
		loadButton.getElement().getStyle().setFloat(Float.LEFT);
		contentPanel.add(loadButton);

		myLoadWsdlSpinner = new LoadingSpinner();
		myLoadWsdlSpinner.hideCompletely();
		contentPanel.add(myLoadWsdlSpinner);

		loadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				handleLoadWsdl();
			}
		});

		contentPanel.add(new HtmlBr());
	}




	@Override
	protected void addProtocolSpecificPanelsToTop() {
		FlowPanel wsdlPanel = new FlowPanel();
		add(wsdlPanel);
		initWsdlPanel(wsdlPanel);
	}


	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.SOAP11;
	}

}
