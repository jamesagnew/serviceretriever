package net.svcret.admin.shared.model;


public class GUserList extends BaseDtoList<GUser> {

	private static final long serialVersionUID = 1L;

	public GUser getUserByPid(long thePid) {
		for (GUser next : this) {
			if (next.getPid()==thePid) {
				return next;
			}
		}
		return null;
	}


}
