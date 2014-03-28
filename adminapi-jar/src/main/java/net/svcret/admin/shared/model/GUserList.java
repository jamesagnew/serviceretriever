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

	public void removeUserByPid(long thePid) {
		for (GUser next : this) {
			if (next.getPid()==thePid) {
				remove(next);
				break;
			}
		}
	}


}
