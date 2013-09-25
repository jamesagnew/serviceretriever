package net.svcret.admin.client.ui.sticky;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.components.CssConstants;
import net.svcret.admin.client.ui.components.LoadingSpinner;
import net.svcret.admin.client.ui.components.PCellTable;
import net.svcret.admin.shared.DateUtil;
import net.svcret.admin.shared.IAsyncLoadCallback;
import net.svcret.admin.shared.Model;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;

public class StickySessionListPanel extends FlowPanel {

	private FlowPanel myContentPanel;
	private LoadingSpinner myLoadingSpinner;

	public StickySessionListPanel() {
		setStylePrimaryName(CssConstants.MAIN_PANEL);

		Label titleLabel = new Label("Sticky Sessions");
		titleLabel.setStyleName(CssConstants.MAIN_PANEL_TITLE);
		add(titleLabel);

		myContentPanel = new FlowPanel();
		myContentPanel.addStyleName(CssConstants.CONTENT_INNER_PANEL);
		add(myContentPanel);

		myLoadingSpinner = new LoadingSpinner();
		myLoadingSpinner.show();
		myContentPanel.add(myLoadingSpinner);

		AdminPortal.SVC_HTTPCLIENTCONFIG.getAllStickySessions(new AsyncCallback<Collection<DtoStickySessionUrlBinding>>() {

			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(final Collection<DtoStickySessionUrlBinding> theStickySessions) {
				Model.getInstance().loadDomainList(new IAsyncLoadCallback<GDomainList>() {

					@Override
					public void onSuccess(GDomainList theDomainList) {
						initUi(theDomainList, theStickySessions);
					}

				});
			}
		});

	}

	private void initUi(final GDomainList theDomainList, Collection<DtoStickySessionUrlBinding> theStickySessions) {
		final CellTable<DtoStickySessionUrlBinding> grid = new PCellTable<DtoStickySessionUrlBinding>();
		myContentPanel.add(grid);

		ListDataProvider<DtoStickySessionUrlBinding> dataProvider = new ListDataProvider<DtoStickySessionUrlBinding>();

		grid.setEmptyTableWidget(new Label("No sticky sessions currently"));

		// Create a Pager to control the table.
		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
		SimplePager pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
		pager.setDisplay(grid);
		pager.setPageSize(20);
		add(pager);

		Column<DtoStickySessionUrlBinding, SafeHtml> createdColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
				Date time = theObject.getCreated();
				return (DateUtil.formatTimeElapsedForMessage(time));
			}
		};
		grid.addColumn(createdColumn, "Created");

		Column<DtoStickySessionUrlBinding, SafeHtml> lastAccessedColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
				Date time = theObject.getLastAccessed();
				return (DateUtil.formatTimeElapsedForMessage(time));
			}
		};
		grid.addColumn(lastAccessedColumn, "Last Accessed");

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
		grid.addColumn(svcVerColumn, "Service Version");

		Column<DtoStickySessionUrlBinding, SafeHtml> sessionIdColumn = new Column<DtoStickySessionUrlBinding, SafeHtml>(new SafeHtmlCell()) {
			@Override
			public SafeHtml getValue(DtoStickySessionUrlBinding theObject) {
				SafeHtmlBuilder b = new SafeHtmlBuilder();
				b.appendEscaped(theObject.getSessionId());
				return b.toSafeHtml();
			}
		};
		grid.addColumn(sessionIdColumn, "Session ID");

		dataProvider.addDataDisplay(grid);

		// Sorting
		ListHandler<DtoStickySessionUrlBinding> columnSortHandler = new ListHandler<DtoStickySessionUrlBinding>(dataProvider.getList());
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

				return service.getId()+" / "+svcVer.getId();
			}
		});
		sessionIdColumn.setSortable(true);
		columnSortHandler.setComparator(sessionIdColumn, new Comparator<DtoStickySessionUrlBinding>() {
			@Override
			public int compare(DtoStickySessionUrlBinding theO1, DtoStickySessionUrlBinding theO2) {
				return theO1.getSessionId().compareTo(theO2.getSessionId());
			}
		});

		grid.addColumnSortHandler(columnSortHandler);
		grid.getColumnSortList().clear();
		grid.getColumnSortList().push(new ColumnSortInfo(lastAccessedColumn, true));

		dataProvider.getList().addAll(theStickySessions);
		dataProvider.refresh();
		
		myLoadingSpinner.hideCompletely();
	}

}
