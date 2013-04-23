package net.svcret.admin.shared.model;


public class GDomainList extends BaseGList<GDomain> {

	private static final long serialVersionUID = 1L;

//	public GDomain getDomainByPid(String theDomainId) {
//		if (StringUtil.isBlank(theDomainId)) {
//			return null;
//		}
//		
//		long pid = -1;
//		try {
//			pid = Long.parseLong(theDomainId);
//		} catch (Exception e) {
//			GWT.log("Failed to parse value: " + theDomainId, e);
//		}
//		return getDomainByPid(pid);
//	}

//	public void merge(GDomain theResult) {
//		GDomain original = getDomainByPid(theResult.getPid() + "");
//		original.merge(theResult);
//	}

	public GDomain getDomainByPid(long theDomainPid) {
		for (GDomain next : this) {
			if (next.getPid() == theDomainPid) {
				return next;
			}
		}
		return null;
	}

	public BaseGServiceVersion getServiceVersionByPid(long thePid) {
		for (GDomain nextDomain : this) {
			for (GService nextSvc : nextDomain.getServiceList()) {
				BaseGServiceVersion ver = nextSvc.getVersionList().getVersionByPid(thePid);
				if (ver!=null) {
					return ver;
				}
			}
		}
		return null;
	}

}
