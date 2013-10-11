package net.svcret.admin.client.ui.dash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import net.svcret.admin.client.nav.NavProcessor;
import net.svcret.admin.client.ui.config.svcver.BaseUrlGrid;
import net.svcret.admin.shared.model.BaseDtoServiceVersion;
import net.svcret.admin.shared.model.DtoDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceVersionUrl;

public class UrlDashboardGrid extends BaseUrlGrid {

	private List<GServiceVersionUrl> myUrls = new ArrayList<GServiceVersionUrl>();

	private Map<Long, DtoDomain> myUrlToDomain = new HashMap<Long, DtoDomain>();

	private Map<Long, GService> myUrlToService = new HashMap<Long, GService>();

	private Map<Long, BaseDtoServiceVersion> myUrlToServiceVersion = new HashMap<Long, BaseDtoServiceVersion>();

	public UrlDashboardGrid(GDomainList theDomainList) {
		init();
		
		for (DtoDomain nextDomain : theDomainList) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
					for (GServiceVersionUrl nextUrl : nextVer.getUrlList()) {
						myUrlToDomain.put(nextUrl.getPid(), nextDomain);
						myUrlToService.put(nextUrl.getPid(), nextSvc);
						myUrlToServiceVersion.put(nextUrl.getPid(), nextVer);
						myUrls.add(nextUrl);
					}
				}
			}
		}

		Collections.sort(myUrls, new Comparator<GServiceVersionUrl>() {
			@Override
			public int compare(GServiceVersionUrl theO1, GServiceVersionUrl theO2) {
				DtoDomain domain1 = myUrlToDomain.get(theO1.getPid());
				DtoDomain domain2 = myUrlToDomain.get(theO2.getPid());
				int retVal = domain1.getName().compareToIgnoreCase(domain2.getName());
				if (retVal == 0) {
					GService service1 = myUrlToService.get(theO1.getPid());
					GService service2 = myUrlToService.get(theO2.getPid());
					retVal = service1.getName().compareToIgnoreCase(service2.getName());
					if (retVal == 0) {
						BaseDtoServiceVersion svcVer1 = myUrlToServiceVersion.get(theO1.getPid());
						BaseDtoServiceVersion svcVer2 = myUrlToServiceVersion.get(theO2.getPid());
						retVal = svcVer1.getId().compareToIgnoreCase(svcVer2.getId());
						if (retVal==0) {
							retVal = theO1.getId().compareToIgnoreCase(theO2.getId());
						}
					}
				}

				return retVal;
			}
		});

		doUpdateUrlPanel(myUrls);
	}


	@Override
	protected Widget createDomainPanel(GServiceVersionUrl theUrl) {
		DtoDomain domain = myUrlToDomain.get(theUrl.getPid());

		SafeHtmlBuilder b = new SafeHtmlBuilder();
		b.appendHtmlConstant("<a href=\"#" + NavProcessor.getTokenEditDomain(domain.getPid()) + "\">");
		b.appendEscaped(domain.getName());
		b.appendHtmlConstant("</a> ");

		return new HTML(b.toSafeHtml());
	}
	@Override
	protected Widget createServicePanel(GServiceVersionUrl theUrl) {
		DtoDomain domain = myUrlToDomain.get(theUrl.getPid());
		GService service = myUrlToService.get(theUrl.getPid());
		BaseDtoServiceVersion version = myUrlToServiceVersion.get(theUrl.getPid());

		SafeHtmlBuilder b = new SafeHtmlBuilder();
		b.appendHtmlConstant("<a href=\"#" + NavProcessor.getTokenEditService(domain.getPid(), service.getPid()) + "\">");
		b.appendEscaped(service.getName());
		b.appendHtmlConstant("</a> ");

		b.appendHtmlConstant("<a href=\"#" + NavProcessor.getTokenEditServiceVersion(version.getPid()) + "\">");
		b.appendEscaped(version.getId());
		b.appendHtmlConstant("</a> ");

		return new HTML(b.toSafeHtml());
	}
	@Override
	protected Widget createUrlWidget(GServiceVersionUrl theUrl) {
		Anchor retVal = new Anchor(theUrl.getId());
		retVal.setHref(theUrl.getUrl());
		return retVal;
	}
	@Override
	protected boolean isHideActionColumn() {
		return true;
	}

	@Override
	protected boolean isHideDomainColumn() {
		return false;
	}


	@Override
	protected boolean isHideIdColumn() {
		return true;
	}

	

	@Override
	protected boolean isHideServiceColumn() {
		return false;
	}

}
