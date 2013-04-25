package net.svcret.admin.client.ui.config;

import static net.svcret.admin.client.AdminPortal.*;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GConfig;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class ConfigPanel extends FlowPanel {

	private LoadingSpinner mySpinner;
	private GConfig myConfig;
	private TextBox myUrlBaseTextBox;

	public ConfigPanel() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label(MSGS.configPanel_Title());
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(contentPanel);

		HorizontalPanel savePanel = new HorizontalPanel();
		contentPanel.add(savePanel);
		
		PButton saveButton = new PButton(IMAGES.iconSave(), MSGS.actions_Save());
		saveButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				save();
			}
		});
		savePanel.add(saveButton);
		
		mySpinner=new LoadingSpinner();
		mySpinner.show();
		savePanel.add(mySpinner);
		
		Model.getInstance().loadConfig(new IAsyncLoadCallback<GConfig>() {
			@Override
			public void onSuccess(GConfig theResult) {
				initConfig(theResult);
			}
		});
		
	}

	private void initConfig(GConfig theResult) {
		myConfig = theResult;
		initClientConfig();
	}

	private void initClientConfig() {
		FlowPanel listPanel = new FlowPanel();
		listPanel.setStylePrimaryName(CssConstants.MAIN_PANEL);
		add(listPanel);

		Label titleLabel = new Label("Client Config");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		listPanel.add(titleLabel);

		FlowPanel contentPanel = new FlowPanel();
		contentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		listPanel.add(contentPanel);

		TwoColumnGrid props = new TwoColumnGrid();
		listPanel.add(props);
		
		myUrlBaseTextBox = new TextBox();
		myUrlBaseTextBox.setValue(myConfig.getProxyUrlBases().iterator().next());
		props.addRow(MSGS.configPanel_UrlBase(), myUrlBaseTextBox);
		props.addDescription(MSGS.configPanel_UrlBaseDesc());
	}

	protected void save() {
		
		myConfig.getProxyUrlBases().add(myUrlBaseTextBox.getValue());
		while (myConfig.getProxyUrlBases().size() > 1) {
			myConfig.getProxyUrlBases().remove(1);
		}
		
		mySpinner.show();
		MODEL_SVC.saveConfig(myConfig, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(Void theResult) {
				mySpinner.showMessage("Saved", false);
			}
		});
		
	}
	
}
