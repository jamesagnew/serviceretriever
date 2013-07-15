package net.svcret.admin.shared.model;

import java.util.Comparator;

import net.svcret.admin.shared.util.StringUtil;

public class GHttpClientConfigList extends BaseGList<GHttpClientConfig> implements Comparator<GHttpClientConfig> {

	private static final long serialVersionUID = 1L;

	public GHttpClientConfigList(){
		setComparator(this);
	}
	
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

	@Override
	public int compare(GHttpClientConfig theO1, GHttpClientConfig theO2) {
		if (GHttpClientConfig.DEFAULT_ID.equals(theO1.getId())) {
			return -1;
		}
		if (GHttpClientConfig.DEFAULT_ID.equals(theO2.getId())) {
			return 1;
		}
		return StringUtil.compare(theO1.getId(), theO2.getId());
	}

}
