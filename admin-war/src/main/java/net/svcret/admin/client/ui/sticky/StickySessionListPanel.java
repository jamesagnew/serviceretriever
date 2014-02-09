package net.svcret.admin.client.ui.sticky;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.client.ui.dash.model.ActionPButton;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomainList;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public class StickySessionListPanel extends FlowPanel {

	private FlowPanel myContentPanel;
	private LoadingSpinner myLoadingSpinner;
	private PCellTable<DtoStickySessionUrlBinding> myGrid;
	private ListDataProvider<DtoStickySessionUrlBinding> myDataProvider;

	public StickySessionListPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Sticky Sessions");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		add(myContentPanel);

		HorizontalPanel p = new HorizontalPanel();
		myContentPanel.add(p);

		ActionPButton refreshButton = new ActionPButton(AdminPortal.IMAGES.iconReload16(), AdminPortal.MSGS.actions_Refresh());
		refreshButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent theEvent) {
				refreshData();
			}
		});
		p.add(refreshButton);

		myLoadingSpinner = new LoadingSpinner();
		myContentPanel.add(myLoadingSpinner);

		refreshData();

	}

	private void refreshData() {
		myLoadingSpinner.show();
		AdminPortal.SVC_HTTPCLIENTCONFIG.getAllStickySessions(new AsyncCallback<Collection<DtoStickySessionUrlBinding>>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final Collection<DtoStickySessionUrlBinding> theStickySessions) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {

					@Override
					public void onSuccess(DtoDomainList theDomainList) {
						initUi(theDomainList, theStickySessions);
					}

				});
			}
		});
	}

	private void initUi(final DtoDomainList theDomainList, Collection<DtoStickySessionUrlBinding> theStickySessions) {
		if (myGrid == null) {
			myGrid = new PCellTable<DtoStickySessionUrlBinding>();
			myContentPanel.add(myGrid);

			myDataProvider = new ListDataProvider<DtoStickySessionUrlBinding>();

			myGrid.setEmptyTableWidget(new Label("No sticky sessions currently"));

			// Create a Pager to control the table.
			SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
			SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
			pager.setDisplay(myGrid);
			pager.setPageSize(20);
			add(pager);

			Column<DtoStickySessionUrlBinding, SafeHtml> createdColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					Date time = theObject.getCreated();
					return (DateUtil.formatTimeElapsedForMessage(time));
				}
			};
			myGrid.addColumn(createdColumn, "Created");

			Column<DtoStickySessionUrlBinding, SafeHtml> lastAccessedColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					Date time = theObject.getLastAccessed();
					return (DateUtil.formatTimeElapsedForMessage(time));
				}
			};
			myGrid.addColumn(lastAccessedColumn, "Last Accessed");

			// Last Requesting IP
			Column<DtoStickySessionUrlBinding, SafeHtml> lastIpColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					if (theObject.getRequestingIp() != null) {
						b.appendEscaped(theObject.getRequestingIp());
					}
					return b.toSafeHtml();
				}
			};
			myGrid.addColumn(lastIpColumn, "Last Requesting IP");

			Column<DtoStickySessionUrlBinding, SafeHtml> svcVerColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					GService service = theDomainList.getServiceWithServiceVersion(theObject.getServiceVersionPid());
					BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theObject.getServiceVersionPid());
					if (service == null || svcVer == null) {
						return SafeHtmlUtils.fromTrustedString("Unknown");
					}

					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.appendEscaped(service.getId());
					b.appendHtmlConstant(" / ");
					b.appendEscaped(svcVer.getId());
					return b.toSafeHtml();
				}
			};
			myGrid.addColumn(svcVerColumn, "Service Version");

			// URL
			Column<DtoStickySessionUrlBinding, SafeHtml> urlColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.appendHtmlConstant("<a href=\"" + theObject.getUrlHref() + "\">");
					b.appendEscaped(theObject.getUrlId());
					b.appendHtmlConstant("</a>");
					return b.toSafeHtml();
				}
			};
			myGrid.addColumn(urlColumn, "Last Requesting IP");

			// Session ID
			Column<DtoStickySessionUrlBinding, SafeHtml> sessionIdColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
				@Override
				public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
					SafeHtmlBuilder b = new SafeHtmlBuilder();
					b.appendEscaped(theObject.getSessionId());
					return b.toSafeHtml();
				}
			};
			myGrid.addColumn(sessionIdColumn, "Session ID");

			myDataProvider.addDataDisplay(myGrid);

			// Sorting
			ListHandler<DtoStickySessionUrlBinding> columnSortHandler = new ListHandler<DtoStickySessionUrlBinding>(myDataProvider.getList());
			createdColumn.setSortable(true);
			columnSortHandler.setComparator(createdColumn, new Comparator<DtoStickySessionUrlBinding>() {
				@Override
				public int compare(DtoStickySessionUrlBinding theO1, DtoStickySessionUrlBinding theO2) {
					return theO1.getCreated().compareTo(theO2.getCreated());
				}
			});
			lastAccessedColumn.setSortable(true);
			columnSortHandler.setComparator(lastAccessedColumn, new Comparator<DtoStickySessionUrlBinding>() {
				@Override
				public int compare(DtoStickySessionUrlBinding theO1, DtoStickySessionUrlBinding theO2) {
					return theO1.getLastAccessed().compareTo(theO2.getLastAccessed());
				}
			});
			svcVerColumn.setSortable(true);
			columnSortHandler.setComparator(svcVerColumn, new Comparator<DtoStickySessionUrlBinding>() {
				@Override
				public int compare(DtoStickySessionUrlBinding theO1, DtoStickySessionUrlBinding theO2) {
					String s1 = toString(theO1);
					String s2 = toString(theO2);
					return s1.compareTo(s2);
				}

				private String toString(DtoStickySessionUrlBinding theObject) {
					GService service = theDomainList.getServiceWithServiceVersion(theObject.getServiceVersionPid());
					BaseDtoServiceVersion svcVer = theDomainList.getServiceVersionByPid(theObject.getServiceVersionPid());
					if (service == null || svcVer == null) {
						return "Unknown";
					}

					return service.getId() + " / " + svcVer.getId();
				}
			});
			urlColumn.setSortable(true);
			columnSortHandler.setComparator(urlColumn, new Comparator<DtoStickySessionUrlBinding>() {
				@Override
				public int compare(DtoStickySessionUrlBinding theO1, DtoStickySessionUrlBinding theO2) {
					return theO1.getSessionId().compareTo(theO2.getSessionId());
				}
			});

			myGrid.addColumnSortHandler(columnSortHandler);
			myGrid.getColumnSortList().clear();
			myGrid.getColumnSortList().push(new ColumnSortInfo(lastAccessedColumn, false));
		}

		myDataProvider.getList().clear();
		myDataProvider.getList().addAll(theStickySessions);
		myDataProvider.refresh();

		myLoadingSpinner.hideCompletely();
	}

}
