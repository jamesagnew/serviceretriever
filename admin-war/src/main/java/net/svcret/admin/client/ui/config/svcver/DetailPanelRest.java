package net.svcret.admin.client.ui.config.svcver;

import net.svcret.admin.client.ui.components.TwoColumnGrid;
import net.svcret.admin.shared.model.DtoServiceVersionRest;
import net.svcret.admin.shared.model.ServiceProtocolEnum;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;

public class DetailPanelRest extends BaseDetailPanel<DtoServiceVersionRest> {

	/**
	 * Constructor
	 */
	public DetailPanelRest(AbstractServiceVersionPanel theParent, DtoServiceVersionRest theServiceVersion) {
		super(theParent, theServiceVersion);
	}


	@Override
	protected void addProtocolSpecificPanelsToTop(boolean theIsAddPanel) {
		FlowPanel restPanel = new FlowPanel();
		add(restPanel, "REST Props");
		initRestPanel(restPanel);
	}

	private void initRestPanel(FlowPanel theRestPanel) {
		
		TwoColumnGrid grid = new TwoColumnGrid();
		theRestPanel.add(grid);
		
		CheckBox rewriteCheckbox = new CheckBox();
		rewriteCheckbox.setValue(getServiceVersion().isRewriteUrls());
		rewriteCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> theArg0) {
				getServiceVersion().setRewriteUrls(theArg0.getValue());
			}
		});
		
		grid.addRow("Rewrite URLs", rewriteCheckbox);
		grid.addDescription("If enabled, URLs contained within request messages will be rewritten to match the target URL, and URLs contained within response messages will be rewritten to match Service Retriever's URL");
		
	}


	@Override
	public ServiceProtocolEnum getProtocol() {
		return ServiceProtocolEnum.HL7OVERHTTP;
	}

}
