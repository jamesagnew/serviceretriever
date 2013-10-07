package net.svcret.admin.client.ui.config.svcver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.FlexTableWithTooltips;
import net.svcret.admin.client.ui.components.FlowPanelWithTooltip;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.dash.model.BaseDashModel;
import net.svcret.admin.client.ui.dash.model.UsageSparklineTooltipProvider;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.model.StatusEnum;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseUrlGrid extends FlowPanel {
	private int myColAction = -1;
	private int myColDomain = -1;
	private int myColId = -1;
	private int myColLastTransaction = -1;
	private int myColService = -1;
	private int myColUrl = -1;
	private int myColUrlStatus = -1;
	private int myColUsage = -1;
	private Label myNoUrlsLabel;
	private FlexTableWithTooltips<GServiceVersionUrl> myUrlGrid;
	private final List<GServiceVersionUrl> myUrlList = new ArrayList<GServiceVersionUrl>();

	public BaseUrlGrid() {
		int next = 1;
		if (!isHideActionColumn()) {
			myColAction = next++;
		}
		if (!isHideDomainColumn()) {
			myColDomain = next++;
		}
		if (!isHideServiceColumn()) {
			myColService = next++;
		}
		if (!isHideIdColumn()) {
			myColId = next++;
		}
		myColUrl = next++;
		myColUrlStatus = next++;
		myColLastTransaction = next++;
		myColUsage = next++;

	}

	@SuppressWarnings("unused")
	protected Widget createActionPanel(GServiceVersionUrl theUrl) {
		return null;
	}

	protected abstract Widget createUrlWidget(GServiceVersionUrl theUrl);

	protected void doUpdateUrlPanel(List<GServiceVersionUrl> theUrlList) {
		myUrlList.clear();
		myUrlList.addAll(theUrlList);

		while (myUrlGrid.getRowCount() > myUrlList.size()) {
			myUrlGrid.removeRow(myUrlGrid.getRowCount() - 1);
		}

		int row = 0;

		int peakLatency = 1;
		for (final GServiceVersionUrl next : theUrlList) {
			if (next.getMaxLatency60min() != null) {
				peakLatency = Math.max(peakLatency, next.getMaxLatency60min());
			}
		}

		for (final GServiceVersionUrl next : theUrlList) {
			row++;

			if (!isHideActionColumn()) {
				myUrlGrid.setWidget(row, myColAction, createActionPanel(next));
			}

			if (!isHideDomainColumn()) {
				myUrlGrid.setWidget(row, myColDomain, createDomainPanel(next));
			}

			if (!isHideServiceColumn()) {
				myUrlGrid.setWidget(row, myColService, createServicePanel(next));
			}

			if (!isHideIdColumn()) {
				EditableField idField = new EditableField();
				idField.setMultiline(false);
				idField.setTransparent(true);
				idField.setProcessHtml(false);
				idField.setValue(next.getId());
				if (StringUtil.isBlank(next.getId())) {
					idField.setEditorMode();
				}
				idField.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> theEvent) {
						next.setId(theEvent.getValue());
					}
				});
				myUrlGrid.setWidget(row, myColId, idField);
			}

			Widget urlField = createUrlWidget(next);
			myUrlGrid.setWidget(row, myColUrl, urlField);

			// Status
			if (next.isStatsInitialized() == false) {
				myUrlGrid.setWidget(row, myColUrlStatus, null);
				myUrlGrid.setWidget(row, myColLastTransaction, null);
			} else {

				// Status
				{
					FlowPanel statusPanel = new FlowPanel();
					statusPanel.setStyleName(CssConstants.UNSTYLED_TABLE);
					statusPanel.getElement().getStyle().setTextAlign(com.google.gwt.dom.client.Style.TextAlign.CENTER);
					Widget imageForStatus = BaseDashModel.returnImageForStatus(next.getStatus());
					imageForStatus.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					statusPanel.add(imageForStatus);
					PButton cbResetButton = null;
					String text = createUrlStatusText(next);

					if (next.getStatus() == StatusEnum.DOWN && next.getStatsNextCircuitBreakerReset() != null) {
						cbResetButton = new PButton("Reset CB");
						cbResetButton.addClickHandler(new CircuitBreakerResetActionHandler(cbResetButton, next.getPid()));
						cbResetButton.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					}

					Label urlStatusLabel = new Label(text, true);
					urlStatusLabel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					statusPanel.add(urlStatusLabel);

					if (cbResetButton != null) {
						statusPanel.add(cbResetButton);
					}

					myUrlGrid.setWidget(row, myColUrlStatus, statusPanel);
				}

				// Usage
				{
					FlowPanel grid = new FlowPanel();
					myUrlGrid.setWidget(row, myColUsage, grid);

					Widget transactionsSparkline = BaseDashModel.returnSparklineFor60MinsUsage(next);
					transactionsSparkline.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					grid.add(transactionsSparkline);

					Widget latencySparkline = BaseDashModel.returnSparklineFor60minsLatency(next.getLatency60mins(), next.getAverageLatency60min(), next.getMaxLatency60min(), peakLatency);
					if (latencySparkline != null) {
						latencySparkline.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
						grid.add(latencySparkline);
					}

					myUrlGrid.setTooltipProvider(row, myColUsage, new UsageSparklineTooltipProvider<GServiceVersionUrl>());
				}

				// Last transaction
				{
					FlowPanel lastXPanel = new FlowPanel();

					List<ResponseTypeEnum> lastResponseTypes = next.getStatsLastResponseTypesFromMostRecentToLeast();
					if (lastResponseTypes.isEmpty()) {
						lastXPanel.add(new Label("No usage"));
					} else {
						for (ResponseTypeEnum lastResponseType : lastResponseTypes) {
							lastXPanel.add(new MyPanel(next, lastResponseType));
						}
					}

					myUrlGrid.setWidget(row, myColLastTransaction, lastXPanel);
				}
			}

		}

		myNoUrlsLabel.setVisible(theUrlList.size() == 0);
	}

	@SuppressWarnings("unused")
	protected Widget createServicePanel(GServiceVersionUrl theNext) {
		return null;
	}

	@SuppressWarnings("unused")
	protected Widget createDomainPanel(GServiceVersionUrl theNext) {
		return null;
	}

	protected void init() {
		myUrlGrid = new FlexTableWithTooltips<GServiceVersionUrl>(myUrlList);
		myUrlGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		this.add(myUrlGrid);

		if (!isHideDomainColumn()) {
			myUrlGrid.setWidget(0, myColDomain, new Label("Domain"));
		}
		if (!isHideServiceColumn()) {
			myUrlGrid.setWidget(0, myColService, new Label("Service"));
		}
		if (!isHideIdColumn()) {
			myUrlGrid.setWidget(0, myColId, new Label("ID"));
		}
		myUrlGrid.setWidget(0, myColUrl, new Label("URL"));
		myUrlGrid.setWidget(0, myColUrlStatus, new Label("Status"));
		myUrlGrid.setWidget(0, myColUsage, new Label("60 Min Activity"));
		myUrlGrid.setWidget(0, myColLastTransaction, new Label("Last Transaction"));

		myNoUrlsLabel = new Label("No URLs Defined");
		this.add(myNoUrlsLabel);
	}

	protected boolean isHideActionColumn() {
		return false;
	}

	protected boolean isHideDomainColumn() {
		return false;
	}

	protected boolean isHideIdColumn() {
		return false;
	}

	protected boolean isHideServiceColumn() {
		return false;
	}

	public static String createUrlStatusText(final GServiceVersionUrl next) {
		String text = null;
		switch (next.getStatus()) {
		case ACTIVE:
			text = "Ok";
			break;
		case DOWN:
			if (next.getStatsNextCircuitBreakerReset() != null) {
				text = "Down (next circuit breaker reset at " + DateUtil.formatTimeOnly(next.getStatsNextCircuitBreakerReset()) + ")";
			} else {
				text = "Down";
			}
			break;
		case UNKNOWN:
			text = "Unknown (no requests)";
			break;
		}
		return text;
	}

	public class CircuitBreakerResetActionHandler implements ClickHandler {

		private PButton myButton;
		private long myUrlPid;

		public CircuitBreakerResetActionHandler(PButton theButton, long theUrlPid) {
			myUrlPid = theUrlPid;
			myButton = theButton;
		}

		@Override
		public void onClick(ClickEvent theEvent) {
			myButton.setEnabled(false);
			AdminPortal.MODEL_SVC.resetCircuitBreakerForServiceVersionUrl(myUrlPid, new AsyncCallback<GServiceVersionUrl>() {
				@Override
				public void onFailure(Throwable theCaught) {
					Model.handleFailure(theCaught);
				}

				@Override
				public void onSuccess(GServiceVersionUrl theResult) {
					myButton.setEnabled(true);
					for (int nextIndex = 0; nextIndex < myUrlList.size(); nextIndex++) {
						if (myUrlList.get(nextIndex).getPid() == theResult.getPid()) {
							myUrlList.set(nextIndex, theResult);
							break;
						}
					}
					doUpdateUrlPanel(myUrlList);
				}
			});
		}

	}

	private final class MyPanel extends FlowPanelWithTooltip<GServiceVersionUrl> implements IProvidesTooltip<GServiceVersionUrl> {
		private ResponseTypeEnum myResponseType;
		private GServiceVersionUrl myStatus;

		private MyPanel(GServiceVersionUrl theStatus, ResponseTypeEnum theResponseType) {
			super(theStatus);

			setTooltipProvider(this);
			myResponseType = theResponseType;
			myStatus = theStatus;

			String message = null;
			switch (myResponseType) {
			case FAIL:
				message = "Failed to Invoke: " + DateUtil.formatTimeElapsedForLastInvocation(myStatus.getStatsLastFailure());
				break;
			case FAULT:
				message = "Fault Response: " + DateUtil.formatTimeElapsedForLastInvocation(myStatus.getStatsLastFault());
				break;
			case SECURITY_FAIL:
			case THROTTLE_REJ:
				throw new IllegalArgumentException();
			case SUCCESS:
				message = "Success: " + DateUtil.formatTimeElapsedForLastInvocation(myStatus.getStatsLastSuccess());
				break;
			}

			Label label = new Label(message);
			label.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
			add(label);

			Image image = new Image(AdminPortal.IMAGES.iconI16());
			image.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
			add(image);

			getElement().getStyle().setWhiteSpace(WhiteSpace.NOWRAP);
		}

		@Override
		public Widget getTooltip(GServiceVersionUrl theStatus) {
			String message = null;
			Date date = null;
			String desc = null;
			String ct = null;
			Integer code = 0;

			switch (myResponseType) {
			case FAIL:
				message = myStatus.getStatsLastFailureMessage();
				date = myStatus.getStatsLastFailure();
				ct = myStatus.getStatsLastFailureContentType();
				code = myStatus.getStatsLastFailureStatusCode();
				desc = "failure";
				break;
			case FAULT:
				message = myStatus.getStatsLastFaultMessage();
				date = myStatus.getStatsLastFault();
				ct = myStatus.getStatsLastFaultContentType();
				code = myStatus.getStatsLastFaultStatusCode();
				desc = "fault";
				break;
			case SECURITY_FAIL:
			case THROTTLE_REJ:
				// not applicable
				break;
			case SUCCESS:
				message = myStatus.getStatsLastSuccessMessage();
				date = myStatus.getStatsLastSuccess();
				ct = myStatus.getStatsLastSuccessContentType();
				code = myStatus.getStatsLastSuccessStatusCode();
				desc = "success";
				break;
			}

			if (message != null || date != null) {
				StringBuilder b = new StringBuilder();
				b.append("Last ");
				b.append(desc);
				b.append("<br/><ul>");

				b.append("<li>");
				b.append("<b>Date:</b> ");
				b.append(DateUtil.formatTime(date));
				b.append("</li>");

				b.append("<li>");
				b.append("<b>Content-Type:</b> ");
				b.append(ct);
				b.append("</li>");

				if (code != null) {
					b.append("<li>");
					b.append("<b>HTTP Code:</b> ");
					b.append(code);
					b.append("</li>");
				}

				b.append("<li>Message: ");
				b.append(message);
				b.append("</li></ul>");
				return new HTML(b.toString());
			}

			return null;
		}
	}

}
