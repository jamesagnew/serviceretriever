package net.svcret.admin.shared.model;


public class GServiceVersionUrlList extends BaseGList<GServiceVersionUrl> {

	private static final long serialVersionUID = 1L;

	public GServiceVersionUrl getUrlWithId(String theName) {
		for (GServiceVersionUrl next : this) {
			if (next.getId() != null && next.getId().equals(theName)) {
				return next;
			}
		}
		return null;
	}

}
