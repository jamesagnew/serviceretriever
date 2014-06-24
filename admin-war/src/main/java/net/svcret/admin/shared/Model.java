package net.svcret.admin.shared;

import java.util.Date;
import java.util.Set;

import net.svcret.admin.client.AdminPortal;
import net.svcret.admin.client.ui.layout.TopBarPanel;
import net.svcret.admin.shared.model.*;
import net.svcret.admin.shared.model.DtoMethod;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class Model {

	private static Model ourInstance;
	private DtoAuthenticationHostList myAuthHostList;
	private DtoConfig myConfig;
	private DtoDomainList myDomainList;
	private boolean myDomainListInitialized = false;
	private GHttpClientConfigList myHttpClientConfigList;
	private Long myLocalTimezoneOffsetInMillis;
	private GMonitorRuleList myMonitorRuleList;

	private Model() {
		initLists();
	}

	public void addDomain(DtoDomain theDomain) {
		DtoDomain domain = myDomainList.getDomainByPid(theDomain.getPid());
		if (domain != null) {
			domain.merge(theDomain);
		} else {
			myDomainList.add(theDomain);
		}
	}

	public void addHttpClientConfig(DtoHttpClientConfig theConfig) {
		DtoHttpClientConfig existing = myHttpClientConfigList.getConfigByPid(theConfig.getPid());
		if (existing != null) {
			existing.merge(theConfig);
		} else {
			myHttpClientConfigList.add(theConfig);
		}
	}

	public void addOrUpdateServiceVersion(long theDomainPid, long theServicePid, BaseDtoServiceVersion theServiceVersion) {
		DtoDomain domain = myDomainList.getDomainByPid(theDomainPid);
		if (domain == null) {
			GWT.log("Unknown domain! " + theDomainPid);
			return;
		}

		GService service = domain.getServiceList().getServiceByPid(theServicePid);
		if (service == null) {
			GWT.log("Unknown service! " + theServicePid);
			return;
		}

		BaseDtoServiceVersion existing = service.getVersionList().getVersionByPid(theServiceVersion.getPid());
		if (existing != null) {
			existing.merge(theServiceVersion);
		} else {
			service.getVersionList().add(theServiceVersion);
		}
	}

	public void addService(long theDomainPid, GService theResult) {
		DtoDomain domain = myDomainList.getDomainByPid(theDomainPid);
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

	public void flushStats() {
		if (myDomainList != null) {
			for (DtoDomain nextDomain : myDomainList) {
				nextDomain.flushStats();
				for (GService nextSvc : nextDomain.getServiceList()) {
					nextSvc.flushStats();
					for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
						nextVer.flushStats();
						for (DtoMethod nextMethod : nextVer.getMethodList()) {
							nextMethod.flushStats();
						}
					}
				}
			}
		}
	}

	private MyModelUpdateCallbackHandler getCalbackWithHttpClientConfigListCallback(IAsyncLoadCallback<GHttpClientConfigList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myHttpClientConfigListCallback = theCallback;
		return retVal;
	}

	private MyModelUpdateCallbackHandler getCallbackWithAuthHostListCallback(IAsyncLoadCallback<DtoAuthenticationHostList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myAuthHostListCallback = theCallback;
		return retVal;
	}

	private MyModelUpdateCallbackHandler getCallbackWithDomainListCallback(IAsyncLoadCallback<DtoDomainList> theCallback) {
		MyModelUpdateCallbackHandler retVal = new MyModelUpdateCallbackHandler();
		retVal.myDomainListCallback = theCallback;
		return retVal;
	}

	private void initLists() {
		if (myDomainList == null) {
			myDomainList = new DtoDomainList();
			myHttpClientConfigList = new GHttpClientConfigList();
			myAuthHostList = new DtoAuthenticationHostList();
		}
	}

	public void invalidateDomainList() {
		myDomainListInitialized = false;
	}

	public void loadAuthenticationHost(final long theAuthHostPid, final IAsyncLoadCallback<BaseDtoAuthenticationHost> theIAsyncLoadCallback) {
		loadAuthenticationHosts(new IAsyncLoadCallback<DtoAuthenticationHostList>() {
			@Override
			public void onSuccess(DtoAuthenticationHostList theResult) {
				theIAsyncLoadCallback.onSuccess(theResult.getAuthHostByPid(theAuthHostPid));
			}
		});
	}

	public void loadAuthenticationHosts(IAsyncLoadCallback<DtoAuthenticationHostList> theCallback) {
		if (myAuthHostList.getLastMerged() != null) {
			theCallback.onSuccess(myAuthHostList);
		} else {
			MyModelUpdateCallbackHandler callback = getCallbackWithAuthHostListCallback(theCallback);
			ModelUpdateRequest req = new ModelUpdateRequest();
			req.setLoadAuthHosts(true);
			AdminPortal.MODEL_SVC.loadModelUpdate(req, callback);
		}
	}

	public void loadConfig(final IAsyncLoadCallback<DtoConfig> theIAsyncLoadCallback) {
		if (myConfig != null) {
			theIAsyncLoadCallback.onSuccess(myConfig);
			return;
		}

		AdminPortal.MODEL_SVC.loadConfig(new AsyncCallback<DtoConfig>() {
			@Override
			public void onFailure(Throwable theCaught) {
				handleFailure(theCaught);
			}

			@Override
			public void onSuccess(DtoConfig theResult) {
				myConfig = theResult;
				theIAsyncLoadCallback.onSuccess(theResult);
			}
		});
	}

	public void loadDomainList(final IAsyncLoadCallback<DtoDomainList> theCallback) {
		if (myDomainListInitialized) {
			theCallback.onSuccess(myDomainList);
		} else {
			ModelUpdateRequest req = new ModelUpdateRequest();
			AsyncCallback<ModelUpdateResponse> callback = new MyModelUpdateCallbackHandler() {
				@Override
				public void onSuccess(ModelUpdateResponse theResult) {
					super.onSuccess(theResult);
					myDomainListInitialized = true;
					myDomainList.mergeResults(theResult.getDomainList());
					theCallback.onSuccess(myDomainList);
				}
			};
			AdminPortal.MODEL_SVC.loadModelUpdate(req, callback);
		}
	}

	public void loadDomainListAndStats(IAsyncLoadCallback<DtoDomainList> theCallback) {
		ModelUpdateRequest request = new ModelUpdateRequest();
		for (DtoDomain nextDom : myDomainList) {
			request.addDomainToLoadStats(nextDom.getPid());
			for (GService nextSvc : nextDom.getServiceList()) {
				if (nextDom.isExpandedOnDashboard()) {
					request.addServiceToLoadStats(nextSvc.getPid());
					for (BaseDtoServiceVersion nextVer : nextSvc.getVersionList()) {
						if (nextSvc.isExpandedOnDashboard()) {
							request.addVersionToLoadStats(nextVer.getPid());
							for (DtoMethod nextMethod : nextVer.getMethodList()) {
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

	public void loadLocalTimezoneOffsetInMillis(final IAsyncLoadCallback<Long> theIAsyncLoadCallback) {
		if (myLocalTimezoneOffsetInMillis == null) {
			AdminPortal.SVC_MISCCONFIG.loadLocalTimzoneOffsetInMillis(new AsyncCallback<Long>() {
				@Override
				public void onFailure(Throwable theCaught) {
					handleFailure(theCaught);
				}

				@Override
				public void onSuccess(Long theResult) {
					myLocalTimezoneOffsetInMillis = theResult;
					theIAsyncLoadCallback.onSuccess(theResult);
				}
			});
		} else {
			theIAsyncLoadCallback.onSuccess(myLocalTimezoneOffsetInMillis);
		}
	}

//	public void loadMonitorRule(final long theRulePid, final IAsyncLoadCallback<BaseGMonitorRule> theIAsyncLoadCallback) {
//		loadMonitorRuleList(new IAsyncLoadCallback<GMonitorRuleList>() {
//			@Override
//			public void onSuccess(GMonitorRuleList theResult) {
//				BaseGMonitorRule rule = theResult.getRuleByPid(theRulePid);
//				if (rule == null) {
//					handleFailure(new Exception("Unknown rule: " + theRulePid));
//					return;
//				}
//
//				theIAsyncLoadCallback.onSuccess(rule);
//			}
//		});
//	}

	public void loadMonitorRuleList(final IAsyncLoadCallback<GMonitorRuleList> theIAsyncLoadCallback) {
		if (myMonitorRuleList == null) {
			AdminPortal.MODEL_SVC.loadMonitorRuleList(new AsyncCallback<GMonitorRuleList>() {

				@Override
				public void onFailure(Throwable theCaught) {
					handleFailure(theCaught);
				}

				@Override
				public void onSuccess(GMonitorRuleList theResult) {
					myMonitorRuleList = theResult;
					theIAsyncLoadCallback.onSuccess(theResult);
				}
			});
		} else {
			theIAsyncLoadCallback.onSuccess(myMonitorRuleList);
		}
	}

	public void loadServiceList(final long theDomainPid, final IAsyncLoadCallback<GServiceList> theCallback) {
		loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theResult) {
				theCallback.onSuccess(myDomainList.getDomainByPid(theDomainPid).getServiceList());
			}
		});
	}

	public void loadServiceVersion(final long theVersionPid, final boolean theLoadDetailedStats, final IAsyncLoadCallback<BaseDtoServiceVersion> theCallback) {
		loadServiceVersion(theVersionPid, new IAsyncLoadCallback<BaseDtoServiceVersion>() {

			@Override
			public void onSuccess(BaseDtoServiceVersion theResult) {
				final BaseDtoServiceVersion version = theResult;
				if (version == null) {
					GWT.log("Unknown version! " + theVersionPid);
					return;
				}

				if (theLoadDetailedStats) {
					AdminPortal.MODEL_SVC.loadServiceVersionDetailedStats(theVersionPid, new AsyncCallback<GServiceVersionDetailedStats>() {
						@Override
						public void onFailure(Throwable theCaught) {
							handleFailure(theCaught);
						}

						@Override
						public void onSuccess(GServiceVersionDetailedStats theDetailedStats) {
							version.setDetailedStats(theDetailedStats);
							theCallback.onSuccess(version);
						}
					});
				} else {
					theCallback.onSuccess(version);
				}

			}
		});
	}

	public void loadServiceVersion(final long theServiceVersionPid, final IAsyncLoadCallback<BaseDtoServiceVersion> theCallback) {
		loadDomainList(new IAsyncLoadCallback<DtoDomainList>() {
			@Override
			public void onSuccess(DtoDomainList theResult) {
				BaseDtoServiceVersion serviceVersion = theResult.getServiceVersionByPid(theServiceVersionPid);
				if (serviceVersion == null) {
					throw new Error("Unknown version: " + theServiceVersionPid);
				}
				theCallback.onSuccess(serviceVersion);
			}
		});
	}

	public void mergeDomainList(DtoDomainList theResult) {
		myDomainListInitialized = true;
		myDomainList.mergeResults(theResult);
	}

	public void saveAuthenticationHost(BaseDtoAuthenticationHost theAuthHost, final IAsyncLoadCallback<DtoAuthenticationHostList> theIAsyncLoadCallback) {
		AsyncCallback<DtoAuthenticationHostList> callback = new AsyncCallback<DtoAuthenticationHostList>() {
			@Override
			public void onFailure(Throwable theCaught) {
				Model.handleFailure(theCaught);
			}

			@Override
			public void onSuccess(DtoAuthenticationHostList theResult) {
				theIAsyncLoadCallback.onSuccess(theResult);
			}
		};
		AdminPortal.MODEL_SVC.saveAuthenticationHost(theAuthHost, callback);

	}

	public void setAuthenticationHostList(DtoAuthenticationHostList theResult) {
		myAuthHostList = theResult;
	}

	public void setMonitorRuleList(GMonitorRuleList theResult) {
		myMonitorRuleList = theResult;
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
		b.append(theCaught.getMessage());
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

	private class MyModelUpdateCallbackHandler implements AsyncCallback<ModelUpdateResponse> {
		public IAsyncLoadCallback<DtoAuthenticationHostList> myAuthHostListCallback;
		private IAsyncLoadCallback<DtoDomainList> myDomainListCallback;
		private IAsyncLoadCallback<GHttpClientConfigList> myHttpClientConfigListCallback;

		@Override
		public void onFailure(Throwable theCaught) {
			handleFailure(theCaught);
		}

		@Override
		public void onSuccess(ModelUpdateResponse theResult) {
			TopBarPanel.getInstance().setNodeStatuses(theResult.getNodeStatuses());
			
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

	public void loadDomainListAndUrlStats(IAsyncLoadCallback<DtoDomainList> theCallback) {
		ModelUpdateRequest request = new ModelUpdateRequest();
		request.setLoadAllUrlStats(true);
		AdminPortal.MODEL_SVC.loadModelUpdate(request, getCallbackWithDomainListCallback(theCallback));

	}

	public void loadDomainListAndUrlStats(Set<Long> theUrlPids, IAsyncLoadCallback<DtoDomainList> theCallback) {
		ModelUpdateRequest request = new ModelUpdateRequest();
		for (Long next : theUrlPids) {
			request.addUrlToLoadStats(next);
		}
		AdminPortal.MODEL_SVC.loadModelUpdate(request, getCallbackWithDomainListCallback(theCallback));
	}

}
