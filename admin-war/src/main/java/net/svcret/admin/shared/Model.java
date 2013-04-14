package net.svcret.admin.shared;

import java.util.Date;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.shared.model.BaseGServiceVersion;
import net.svcret.admin.shared.model.GAuthenticationHostList;
import net.svcret.admin.shared.model.GDomain;
import net.svcret.admin.shared.model.GDomainList;
import net.svcret.admin.shared.model.GHttpClientConfig;
import net.svcret.admin.shared.model.GHttpClientConfigList;
import net.svcret.admin.shared.model.GLocalDatabaseAuthHost;
import net.svcret.admin.shared.model.GService;
import net.svcret.admin.shared.model.GServiceList;
import net.svcret.admin.shared.model.GServiceMethod;
import net.svcret.admin.shared.model.ModelUpdateRequest;
import net.svcret.admin.shared.model.ModelUpdateResponse;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Model {

	private static Model ourInstance;
	private GDomainList myDomainList;
	private boolean myDomainListInitialized = false;
	private GHttpClientConfigList myHttpClientConfigList;
	private GAuthenticationHostList myAuthHostList;

	private Model() {
		initLists();
	}

	public void addDomain(GDomain theDomain) {
		GDomain domain = myDomainList.getDomainByPid(theDomain.getPid());
		if (domain != null) {
			domain.merge(theDomain);
		} else {
			myDomainList.add(theDomain);
		}
	}

	public void addService(long theDomainPid, GService theResult) {
		GDomain domain = myDomainList.getDomainByPid(theDomainPid);
		if (domain == null) {
			GWT.log("No domain in memory with PID: " + theDomainPid);
			return;
		}

		GService service = domain.getServiceList().getServiceByPid(theResult.getPid());
		if (service != null) {
			service.merge(theResult);
		} else {
			domain.getServiceList().add(theResult);
		}
	}

	public void addServiceVersion(long theDomainPid, long theServicePid, BaseGServiceVersion theServiceVersion) {
		GDomain domain = myDomainList.getDomainByPid(theDomainPid);
		if (domain == null) {
			GWT.log("Unknown domain! " + theDomainPid);
			return;
		}

		GService service = domain.getServiceList().getServiceByPid(theServicePid);
		if (service == null) {
			GWT.log("Unknown service! " + theServicePid);
			return;
		}

		service.getVersionList().add(theServiceVersion);
	}

	private void initLists() {
		if (myDomainList == null) {
			myDomainList = new GDomainList();
			myHttpClientConfigList = new GHttpClientConfigList();
			myAuthHostList = new GAuthenticationHostList();
		}
	}

	public void loadDomainList(final IAsyncLoadCallback<GDomainList> theCallback) {
		if (myDomainListInitialized) {
			theCallback.onSuccess(myDomainList);
		} else {
			ModelUpdateRequest req = new ModelUpdateRequest();
			AsyncCallback<ModelUpdateResponse> callback = new AsyncCallback<ModelUpdateResponse>() {
				@Override
				public void onFailure(Throwable theCaught) {
					handleFailure(theCaught);
				}

				@Override
				public void onSuccess(ModelUpdateResponse theResult) {
					myDomainListInitialized = true;
					myDomainList.mergeResults(theResult.getDomainList());
					theCallback.onSuccess(myDomainList);
				}
			};
			AdminPortal.MODEL_SVC.loadModelUpdate(req, callback);
		}
	}

	public void loadDomainListAndStats(IAsyncLoadCallback<GDomainList> theCallback) {
		ModelUpdateRequest request = new ModelUpdateRequest();
		for (GDomain nextDom : myDomainList) {
			request.addDomainToLoadStats(nextDom.getPid());
			for (GService nextSvc : nextDom.getServiceList()) {
				if (nextDom.isExpandedOnDashboard()) {
					request.addServiceToLoadStats(nextSvc.getPid());
					for (BaseGServiceVersion nextVer : nextSvc.getVersionList()) {
						if (nextSvc.isExpandedOnDashboard()) {
							request.addVersionToLoadStats(nextVer.getPid());
							for (GServiceMethod nextMethod : nextVer.getMethodList()) {
								request.addVersionMethodToLoadStats(nextMethod.getPid());
							}
						}
					}
				}
			}
		}

		GWT.log(new Date() + " - Going to update model with: " + request.toString());

		AdminPortal.MODEL_SVC.loadModelUpdate(request, getCallbackWithDomainListCallback(theCallback));
	}

	public void loadHttpClientConfigs(IAsyncLoadCallback<GHttpClientConfigList> theCallback) {
		if (myHttpClientConfigList.getLastMerged() != null) {
			theCallback.onSuccess(myHttpClientConfigList);
		} else {
			ModelUpdateRequest req = new ModelUpdateRequest();
			req.setLoadHttpClientConfigs(true);
			AdminPortal.MODEL_SVC.loadModelUpdate(req, getCalbackWithHttpClientConfigListCallback(theCallback));
		}
	}

	public void loadService(final long theDomainPid, final long theServicePid, final IAsyncLoadCallback<GService> theIAsyncLoadCallback) {
		loadServiceList(theDomainPid, new IAsyncLoadCallback<GServiceList>() {
			@Override
			public void onSuccess(GServiceList theResult) {
				GService service = theResult.getServiceByPid(theServicePid);
				theIAsyncLoadCallback.onSuccess(service);
			}
		});
	}

	public void loadServiceList(final long theDomainPid, final IAsyncLoadCallback<GServiceList> theCallback) {
		loadDomainList(new IAsyncLoadCallback<GDomainList>() {
			@Override
			public void onSuccess(GDomainList theResult) {
				theCallback.onSuccess(myDomainList.getDomainByPid(theDomainPid).getServiceList());
			}
		});
	}

	public void loadServiceVersion(long theDomainPid, long theServicePid, final long theVersionPid, final IAsyncLoadCallback<BaseGServiceVersion> theCallback) {
		loadService(theDomainPid, theServicePid, new IAsyncLoadCallback<GService>() {

			@Override
			public void onSuccess(GService theResult) {
				BaseGServiceVersion version = theResult.getVersionList().getVersionByPid(theVersionPid);
				if (version == null) {
					GWT.log("Unknown version! " + theVersionPid);
					return;
				}
				theCallback.onSuccess(version);
			}
		});
	}

	public static Model getInstance() {
		if (ourInstance == null) {
			ourInstance = new Model();
		}
		return ourInstance;
	}

	public static void handleFailure(Throwable theCaught) {
		GWT.log("Failed to load data!", theCaught);
		StringBuilder b = new StringBuilder();
		b.append("Failure: ");
		b.append(theCaught.toString());
		b.append("\n");
		
		int i = 0;
		for (StackTraceElement next : theCaught.getStackTrace()) {
			b.append(next.getMethodName());
			b.append("\n");
			if (i++ > 5) {
				break;
			}
		}
		
		Window.alert(b.toString());
	}

	private final class MyModelUpdateCallbackHandler implements AsyncCallback<ModelUpdateResponse> {
		private IAsyncLoadCallback<GDomainList> myDomainListCallback;
		private IAsyncLoadCallback<GHttpClientConfigList> myHttpClientConfigListCallback;
		public IAsyncLoadCallback<GAuthenticationHostList> myAuthHostListCallback;

		@Override
		public void onFailure(Throwable theCaught) {
			handleFailure(theCaught);
		}

		@Override
		public void onSuccess(ModelUpdateResponse theResult) {
			if (theResult.getDomainList() != null) {
				myDomainList.mergeResults(theResult.getDomainList());
			}
			if (theResult.getHttpClientConfigList() != null) {
				myHttpClientConfigList.mergeResults(theResult.getHttpClientConfigList());
			}

			if (theResult.getAuthenticationHostList() != null) {
				myAuthHostList.mergeResults(theResult.getAuthenticationHostList());
			}

			if (myDomainListCallback != null) {
				myDomainListCallback.onSuccess(myDomainList);
			}

			if (myHttpClientConfigListCallback != null) {
				myHttpClientConfigListCallback.onSuccess(myHttpClientConfigList);
			}
			
			if (myAuthHostListCallback != null) {
				myAuthHostListCallback.onSuccess(myAuthHostList);
			}
		}

	}

	private MyModelUpdateCallbackHandler getCallbackWithDomainListCallback(IAsyncLoadCallback<GDomainList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myDomainListCallback = theCallback;
		return retVal;
	}

	private MyModelUpdateCallbackHandler getCalbackWithHttpClientConfigListCallback(IAsyncLoadCallback<GHttpClientConfigList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myHttpClientConfigListCallback = theCallback;
		return retVal;
	}

	private MyModelUpdateCallbackHandler getCallbackWithAuthHostListCallback(IAsyncLoadCallback<GAuthenticationHostList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myAuthHostListCallback = theCallback;
		return retVal;
	}

	public void addHttpClientConfig(GHttpClientConfig theConfig) {
		GHttpClientConfig existing = myHttpClientConfigList.getConfigByPid(theConfig.getPid());
		if (existing != null) {
			existing.merge(theConfig);
		} else {
			myHttpClientConfigList.add(theConfig);
		}
	}

	public void loadAuthenticationHosts(IAsyncLoadCallback<GAuthenticationHostList> theCallback) {
		if (myAuthHostList.getLastMerged() != null) {
			theCallback.onSuccess(myAuthHostList);
		} else {
			MyModelUpdateCallbackHandler callback = getCallbackWithAuthHostListCallback(theCallback);
			ModelUpdateRequest req = new ModelUpdateRequest();
			req.setLoadAuthHosts(true);
			AdminPortal.MODEL_SVC.loadModelUpdate(req, callback);
		}
	}

	public void saveAuthenticationHost(GLocalDatabaseAuthHost theAuthHost, final IAsyncLoadCallback<GAuthenticationHostList> theIAsyncLoadCallback) {
		AsyncCallback<GAuthenticationHostList> callback = new AsyncCallback<GAuthenticationHostList>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(GAuthenticationHostList theResult) {
				theIAsyncLoadCallback.onSuccess(theResult);
			}
		};
		AdminPortal.MODEL_SVC.saveAuthenticationHost(theAuthHost, callback);
		
	}

	public void mergeDomainList(GDomainList theResult) {
		myDomainListInitialized = true;
		myDomainList.mergeResults(theResult);
	}

}
