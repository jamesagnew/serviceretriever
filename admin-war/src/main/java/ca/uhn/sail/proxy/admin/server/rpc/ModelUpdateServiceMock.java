package ca.uhn.sail.proxy.admin.server.rpc;

import java.util.ArrayList;
import java.util.Date;

import ca.uhn.sail.proxy.admin.client.rpc.ModelUpdateService;
import ca.uhn.sail.proxy.admin.shared.ServiceFailureException;
import ca.uhn.sail.proxy.admin.shared.model.BaseGServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.DomainListUpdateRequest;
import ca.uhn.sail.proxy.admin.shared.model.GDomain;
import ca.uhn.sail.proxy.admin.shared.model.GDomainList;
import ca.uhn.sail.proxy.admin.shared.model.GService;
import ca.uhn.sail.proxy.admin.shared.model.GServiceList;
import ca.uhn.sail.proxy.admin.shared.model.GServiceMethod;
import ca.uhn.sail.proxy.admin.shared.model.GSoap11ServiceVersion;
import ca.uhn.sail.proxy.admin.shared.model.StatusEnum;

public class ModelUpdateServiceMock implements ModelUpdateService {

	private ArrayList<GDomain> myAddedDomains;
	private static long ourNextPid = 1000000L;

	public ModelUpdateServiceMock() {
		myAddedDomains = new ArrayList<GDomain>();
	}
	
	public GDomainList loadDomainList(DomainListUpdateRequest theRequest) {
		GDomainList retVal = new GDomainList();
		
		GDomain dom = new GDomain();
		dom.setPid(1L);
		dom.setId("domain1");
		dom.setName("Domain 1");
		dom.setStatus(StatusEnum.ACTIVE);
		dom.setTransactions60mins(random60mins());
		retVal.add(dom);

		retVal.addAll(myAddedDomains);
		
		if (theRequest.getDomainsToLoad().contains(1L) || theRequest.isLoadAllDomains()) {
			GService svc = new GService();
			svc.setId("svc1a");
			svc.setName("Service 1-A");
			svc.setPid(10L);
			svc.setStatus(StatusEnum.ACTIVE);
			svc.setTransactions60mins(random60mins());
			dom.initChildList();
			dom.getServiceList().add(svc);

			if (theRequest.getServicesToLoad().contains(10L)) {
				svc.initChildList();
				BaseGServiceVersion ver = new GSoap11ServiceVersion();
				ver.setActive(true);
				ver.setId("Version 1-A-1");
				ver.setPid(100L);
				ver.setName("Version 1-A-1");
				ver.setLastAccess(new Date());
				ver.setUrlsActive(2);
				ver.setUrlsFailed(1);
				ver.setUrlsUnknown(10);
				ver.setStatus(StatusEnum.UNKNOWN);
				ver.setTransactions60mins(random60mins());
				svc.getVersionList().add(ver);
				
				if (theRequest.getVersionsToLoad().contains(100L)) {
					GServiceMethod met = new GServiceMethod();
					met.setPid(1000L);
					met.setId("Method 1");
					met.setName("Method 1");
					met.setTransactions60mins(random60mins());
					ver.initChildList();
					ver.getMethodList().add(met);
				}
				
			}

			svc = new GService();
			svc.setId("svc1b");
			svc.setName("Service 1-B");
			svc.setPid(11L);
			svc.setStatus(StatusEnum.ACTIVE);
			svc.setTransactions60mins(random60mins());
			dom.getServiceList().add(svc);

			if (theRequest.getServicesToLoad().contains(10L)) {
				svc.initChildList();
			}
			
		}

		dom = new GDomain();
		dom.setPid(2L);
		dom.setId("domain2");
		dom.setName("Domain 2");
		dom.setStatus(StatusEnum.DOWN);
		dom.setTransactions60mins(random60mins());
		retVal.add(dom);		

		if (theRequest.getDomainsToLoad().contains(2L) || theRequest.isLoadAllDomains()) {
			dom.initChildList();
		}
		
//		try {
//			Thread.sleep(2000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		
		return retVal;
	}

	private int[] random60mins() {
		int[] retVal = new int[60];
		for (int i = 0; i < 60;i++) {
			retVal[i] = (int) (Math.random() * 100.0);
		}
		return retVal;
	}

	public GDomain addDomain(String theId, String theName) throws ServiceFailureException {
		GDomain retVal = new GDomain();
		retVal.setId(theId);
		retVal.setName(theName);
		retVal.setPid(ourNextPid++);
		myAddedDomains.add(retVal);
		return retVal;
	}

	public GDomain saveDomain(long thePid, String theId, String theName) {
		GDomain retVal = new GDomain();
		retVal.setPid(thePid);
		retVal.setId(theId);
		retVal.setName(theName);
		return retVal;
	}

	public GServiceList addService(long theDomainPid, String theId, String theName, boolean theActive) {
		GServiceList retVal = new GServiceList();
		
		GService svc = new GService();
		svc.setPid(ourNextPid++);
		svc.setId(theId);
		svc.setName(theName);
		svc.setStatus(StatusEnum.ACTIVE);
		svc.setTransactions60mins(random60mins());
		svc.setActive(theActive);
		
		retVal.add(svc);
		
		return retVal;
	}

}
