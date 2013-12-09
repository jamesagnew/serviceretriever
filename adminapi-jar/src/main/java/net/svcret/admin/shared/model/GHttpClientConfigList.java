package net.svcret.admin.shared.model;

import java.util.Comparator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.StringUtil;
import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="HttpClientConfigList")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="HttpBasicAuthServerSecurity")
@XmlAccessorType(XmlAccessType.FIELD)
public class GHttpClientConfigList extends BaseDtoList<DtoHttpClientConfig> implements Comparator<DtoHttpClientConfig> {

	private static final long serialVersionUID = 1L;

	public GHttpClientConfigList(){
		setComparator(this);
	}
	
	public DtoHttpClientConfig getConfigByPid(long thePid) {
		for (DtoHttpClientConfig next : this) {
			if (next.getPid() == thePid) {
				return next;
			}
		}
		return null;
	}

	public String listConfigIds() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (DtoHttpClientConfig next : this) {
			if (b.length() > 1) {
				b.append(", ");
			}
			b.append(next.getPid());
		}
		b.append(']');
		return b.toString();
	}

	@Override
	public int compare(DtoHttpClientConfig theO1, DtoHttpClientConfig theO2) {
		if (DtoHttpClientConfig.DEFAULT_ID.equals(theO1.getId())) {
			return -1;
		}
		if (DtoHttpClientConfig.DEFAULT_ID.equals(theO2.getId())) {
			return 1;
		}
		return StringUtil.compare(theO1.getId(), theO2.getId());
	}

}
