package net.svcret.admin.shared.model;

public class GHttpClientConfigList extends BaseGList<GHttpClientConfig> {

	private static final long serialVersionUID = 1L;

	public GHttpClientConfig getConfigByPid(long thePid) {
		for (GHttpClientConfig next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	public String listConfigIds() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (GHttpClientConfig next : this) {
			if (b.length() > 1) {
				b.append(", ");
			}
			b.append(next.getPid());
		}
		b.append(']');
		return b.toString();
	}

}
