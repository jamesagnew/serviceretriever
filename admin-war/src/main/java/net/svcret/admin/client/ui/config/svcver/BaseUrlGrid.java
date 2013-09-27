package net.svcret.admin.client.ui.config.svcver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.EditableField;
import net.svcret.admin.client.ui.components.FlowPanelWithTooltip;
import net.svcret.admin.client.ui.components.IProvidesTooltip;
import net.svcret.admin.client.ui.components.PButton;
import net.svcret.admin.client.ui.dash.model.BaseDashModel;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.enm.ResponseTypeEnum;
import net.svcret.admin.shared.model.GServiceVersionUrl;
import net.svcret.admin.shared.util.StringUtil;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class BaseUrlGrid extends FlowPanel {
	private static final int COL_URL_ID = 1;

	private static final int COL_URL_LAST_TRANSACTION = 5;
	private static final int COL_URL_STATUS = 3;
	private static final int COL_URL_URL = 2;
	private static final int COL_URL_USAGE = 4;
	private static final int NUM_COLS = 6;
	private Label myNoUrlsLabel;

	private Grid myUrlGrid;
	private List<GServiceVersionUrl> myUrlList;

	public BaseUrlGrid() {
	}

	protected abstract Widget createActionPanel(GServiceVersionUrl theUrl);

	protected void doUpdateUrlPanel(List<GServiceVersionUrl> theUrlList) {
		myUrlList = new ArrayList<GServiceVersionUrl>(theUrlList);

		myUrlGrid.resize(theUrlList.size() + 1, NUM_COLS);

		int row = 0;

		int peakLatency = 1;
		for (final GServiceVersionUrl next : theUrlList) {
			if (next.getMaxLatency60min() != null) {
				peakLatency = Math.max(peakLatency, next.getMaxLatency60min());
			}
		}

		for (final GServiceVersionUrl next : theUrlList) {
			row++;

			myUrlGrid.setWidget(row, 0, createActionPanel(next));

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
			myUrlGrid.setWidget(row, COL_URL_ID, idField);

			EditableField urlField = new EditableField();
			urlField.setMultiline(false);
			urlField.setTransparent(true);
			urlField.setProcessHtml(false);
			urlField.setMaxFieldWidth(180);
			urlField.setLabelIsPlainText(true);
			urlField.setShowTooltip(true);
			urlField.setValue(next.getUrl());
			if (StringUtil.isBlank(next.getId())) {
				urlField.setEditorMode();
			}
			urlField.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> theEvent) {
					next.setUrl(theEvent.getValue());
				}
			});
			myUrlGrid.setWidget(row, COL_URL_URL, urlField);

			// Status
			if (next.isStatsInitialized() == false) {
				myUrlGrid.setWidget(row, COL_URL_STATUS, null);
				myUrlGrid.setWidget(row, COL_URL_LAST_TRANSACTION, null);
			} else {

				// Status
				{
					FlowPanel statusPanel = new FlowPanel();
					statusPanel.setStyleName(CssConstants.UNSTYLED_TABLE);
					statusPanel.getElement().getStyle().setTextAlign(com.google.gwt.dom.client.Style.TextAlign.CENTER);
					Widget imageForStatus = BaseDashModel.returnImageForStatus(next.getStatus());
					imageForStatus.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					statusPanel.add(imageForStatus);
					String text = null;
					PButton cbResetButton = null;
					switch (next.getStatus()) {
					case ACTIVE:
						text = "Ok";
						break;
					case DOWN:
						if (next.getStatsNextCircuitBreakerReset() != null) {
							text = "Down (next circuit breaker reset at " + DateUtil.formatTimeOnly(next.getStatsNextCircuitBreakerReset()) + ")";
							cbResetButton = new PButton("Reset CB");
							cbResetButton.addClickHandler(new CircuitBreakerResetActionHandler(cbResetButton, next.getPid()));
							cbResetButton.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
						} else {
							text = "Down";
						}
						break;
					case UNKNOWN:
						text = "Unknown (no requests)";
						break;
					}
					Label urlStatusLabel = new Label(text, true);
					urlStatusLabel.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					statusPanel.add(urlStatusLabel);

					if (cbResetButton != null) {
						statusPanel.add(cbResetButton);
					}

					myUrlGrid.setWidget(row, COL_URL_STATUS, statusPanel);
				}

				// Usage
				{
					FlowPanel grid = new FlowPanel();
					myUrlGrid.setWidget(row, COL_URL_USAGE, grid);

					Widget transactionsSparkline = BaseDashModel.returnSparklineFor60MinsUsage(next);
					transactionsSparkline.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
					grid.add(transactionsSparkline);

					Widget latencySparkline = BaseDashModel.returnSparklineFor60minsLatency(next.getLatency60mins(), next.getAverageLatency60min(),
							next.getMaxLatency60min(), peakLatency);
					if (latencySparkline != null) {
						latencySparkline.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
						grid.add(latencySparkline);
					}
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

					myUrlGrid.setWidget(row, COL_URL_LAST_TRANSACTION, lastXPanel);
				}
			}

		}

		myNoUrlsLabel.setVisible(theUrlList.size() == 0);
	}

	protected void init() {
		this.add(new Label("Each proxied service will have one or more implementation URLs. " + "When a client attempts to invoke a service that has been proxied, the ServiceProxy will "
				+ "forward this request to one of these implementations. Specifying more than one " + "implementation URL means that if one is unavailable, another can be tried (i.e. redundancy)."));

		myUrlGrid = new Grid(1, NUM_COLS);
		myUrlGrid.addStyleName(CssConstants.PROPERTY_TABLE);
		this.add(myUrlGrid);

		// myUrlGrid.setWidget(0, 0, new Label("Action"));
		myUrlGrid.setWidget(0, COL_URL_ID, new Label("ID"));
		myUrlGrid.setWidget(0, COL_URL_URL, new Label("URL"));
		myUrlGrid.setWidget(0, COL_URL_STATUS, new Label("Status"));
		myUrlGrid.setWidget(0, COL_URL_USAGE, new Label("60 Min Activity"));
		myUrlGrid.setWidget(0, COL_URL_LAST_TRANSACTION, new Label("Last Transaction"));

		myNoUrlsLabel = new Label("No URLs Defined");
		this.add(myNoUrlsLabel);
	}

	public class CircuitBreakerResetActionHandler implements ClickHandler {

		private long myUrlPid;
		private PButton myButton;

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
