package ca.uhn.sail.proxy.admin.shared.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.uhn.sail.proxy.admin.client.AdminPortal;
import ca.uhn.sail.proxy.admin.shared.util.IAsyncLoadCallback;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Model {

	private static Model ourInstance;
	private GDomainList myDomainList;

	private Timer myUpdateTimer;

	private Model() {
	}

	/**
	 * @return the domainList
	 */
	public GDomainList getDomainList() {
		if (myDomainList == null) {
			myDomainList = new GDomainList();
			updateDashboardModel();
		}
		return myDomainList;
	}

	private void updateDashboardModel() {
		GWT.log(new Date() + " - Going to update model");
		
		DomainListUpdateRequest request = new DomainListUpdateRequest();
		for (GDomain nextDom : myDomainList) {
			if (nextDom.isExpandedOnDashboard() && nextDom.isInitialized()) {
				request.addDomainToLoad(nextDom.getPid());
				for (GService nextSvc : nextDom.getServiceList()) {
					if (nextSvc.isExpandedOnDashboard() && nextSvc.isInitialized()) {
						request.addServiceToLoad(nextSvc.getPid());
						for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
							if (nextVer.isExpandedOnDashboard() && nextVer.isInitialized()) {
								request.addVersionToLoad(nextVer.getPid());
							}
						}
					}
				}
			}
		}
		
		AsyncCallback<GDomainList> updateCallback = getUpdateCallback();
		AdminPortal.MODEL_SVC.loadDomainList(request, updateCallback);
	}

	public static Model getInstance() {
		if (ourInstance == null) {
			ourInstance = new Model();
		}
		return ourInstance;
	}

	
	public AsyncCallback<GDomainList> getUpdateCallback() {
		return new AsyncCallback<GDomainList>() {

			public void onFailure(Throwable theCaught) {
				GWT.log("Failed to load data!", theCaught);
			}

			public void onSuccess(GDomainList theResult) {
				GWT.log(new Date() + " - Model updated, going to update UI");
				myDomainList.mergeResults(theResult);
				GWT.log(new Date() + " - Model updated, done updating UI");
			}
		};
	}

	public void updateNow() {
		updateDashboardModel();
	}

	public AsyncCallback<GServiceList> getAddOrEditServiceCallback(final long theDomainPid) {
		return new AsyncCallback<GServiceList>() {

			public void onFailure(Throwable theCaught) {
				GWT.log("Failed to load data!", theCaught);
			}

			public void onSuccess(GServiceList theResult) {
				GDomain domain = myDomainList.getDomainByPid(theDomainPid);
				domain.getServiceList().mergeResults(theResult);
			}
		};
	}

	public void loadAllDomainsAndServices(final IAsyncLoadCallback<GDomainList> theLoadCallback) {
		DomainListUpdateRequest req = new DomainListUpdateRequest();
		if (myDomainList == null) {
			myDomainList = new GDomainList();
		}
		
		if (myDomainList.isInitialized()) {
			List<Long> domainsToLoad = new ArrayList<Long>();
			for (GDomain next : myDomainList) {
				if (!next.getServiceList().isInitialized()) {
					domainsToLoad.add(next.getPid());
				}
			}
			
			if (domainsToLoad.isEmpty()) {
				theLoadCallback.onSuccess(myDomainList);
				return;
			}
			
			req.initDomainsToLoad();
			req.getDomainsToLoad().addAll(domainsToLoad);
			
		} else {
			
			req.setLoadAllDomains(true);
			
		}

		
		AsyncCallback<GDomainList> callback = new AsyncCallback<GDomainList>() {

			public void onFailure(Throwable theCaught) {
				getUpdateCallback().onFailure(theCaught);
			}

			public void onSuccess(GDomainList theResult) {
				getUpdateCallback().onSuccess(theResult);
				theLoadCallback.onSuccess(theResult);
			}
		};
		AdminPortal.MODEL_SVC.loadDomainList(req, callback);

	}

}

