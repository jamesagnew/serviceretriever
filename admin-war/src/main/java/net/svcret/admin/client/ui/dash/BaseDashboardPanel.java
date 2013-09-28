package net.svcret.admin.client.ui.dash;

import java.util.Date;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.MyResources;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.GDomainList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public abstract class BaseDashboardPanel extends FlowPanel implements IDestroyable {

	private Date myLastUpdate;
	private Label myLastUpdateLabel;
	private Timer myLastUpdateTimer;
	private Image myReloadButton;
	private Timer myTimer;
	private Label myTimeSinceLastUpdateLabel;
	private boolean myUpdating;

	public BaseDashboardPanel() {
		Model.getInstance().flushStats();

		setStylePrimaryName(CssConstants.MAIN_PANEL);

		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titlePanel);

		Label titleLabel = new Label("Service Dashboard");
		titleLabel.addStyleName(CssConstants.MAIN_PANEL_TITLE_TEXT);
		titlePanel.add(titleLabel);

		HTML spacer = new HTML("&nbsp;");
		titlePanel.add(spacer);
		titlePanel.setCellWidth(spacer, "100%");

		myLastUpdateLabel = new Label();
		myLastUpdateLabel.addStyleName(CssConstants.MAIN_PANEL_UPDATE);
		titlePanel.add(myLastUpdateLabel);
		titlePanel.setCellVerticalAlignment(myLastUpdateLabel, HasVerticalAlignment.ALIGN_MIDDLE);

		myTimeSinceLastUpdateLabel = new Label();
		myTimeSinceLastUpdateLabel.addStyleName(MyResources.CSS.dashboardTimeSinceLastUpdateLabel());
		titlePanel.add(myTimeSinceLastUpdateLabel);

		myReloadButton = new Image(AdminPortal.IMAGES.iconReload16());
		myReloadButton.addStyleName(MyResources.CSS.dashboardReloadButton());
		myReloadButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
				myUpdating = true;
				Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(GDomainList theResult) {
						myUpdating = false;
						updateView(theResult);
					}

				});
			}
		});
		titlePanel.add(myReloadButton);

		/*
		 * Set up timer for automatic updates
		 */

		myTimer = new Timer() {
			@Override
			public void run() {
				if (myUpdating) {
					return;
				}
				myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
				myUpdating = true;
				Model.getInstance().loadDomainListAndStats(new IAsyncLoadCallback<GDomainList>() {
					@Override
					public void onSuccess(GDomainList theResult) {
						myUpdating = false;
						updateView(theResult);
					}
				});
			}
		};
		myTimer.scheduleRepeating(30 * 1000);

		myLastUpdateTimer = new Timer() {

			@Override
			public void run() {
				if (myLastUpdate != null) {
					myTimeSinceLastUpdateLabel.setText(DateUtil.formatTimeElapsedForLastInvocation(myLastUpdate, true));
				}
			}
		};
		myLastUpdateTimer.scheduleRepeating(1000);

	}

	@Override
	public void destroy() {
		myTimer.cancel();
		myLastUpdateTimer.cancel();
	}

	protected void updateView() {
		if (myUpdating) {
			return;
		}
		myReloadButton.setResource(AdminPortal.IMAGES.dashboardSpinner());
		myUpdating = true;
		Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				myUpdating = false;
				updateView(theResult);
			}
		});
	}

	protected abstract void updateView(GDomainList theResult);

	protected void updatingFinished() {
		myReloadButton.setResource(AdminPortal.IMAGES.iconReload16());
		myLastUpdateLabel.setText("Updated " + DateTimeFormat.getFormat(PredefinedFormat.TIME_MEDIUM).format(new Date()));
		myLastUpdate = new Date();
	}

}
