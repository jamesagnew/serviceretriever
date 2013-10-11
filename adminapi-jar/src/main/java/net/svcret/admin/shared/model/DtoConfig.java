package net.svcret.admin.shared.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace=XmlConstants.DTO_NAMESPACE, name="Config")
@XmlRootElement(namespace=XmlConstants.DTO_NAMESPACE, name="Config")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@XmlElementWrapper(name="config_ProxyUrlBases")
	@XmlElement(name="UrlBase")
	private List<String> myProxyUrlBases;
	
	@XmlElement(name="config_TruncateRecentDatabaseTransactionsToBytes", required=false)
	private Integer myTruncateRecentDatabaseTransactionsToBytes;

	/**
	 * @return the proxyUrlBase
	 */
	public List<String> getProxyUrlBases() {
		if (myProxyUrlBases == null) {
			myProxyUrlBases = new ArrayList<String>();
		}
		return myProxyUrlBases;
	}

	public Integer getTruncateRecentDatabaseTransactionsToBytes() {
		return myTruncateRecentDatabaseTransactionsToBytes;
	}

	public void setTruncateRecentDatabaseTransactionsToBytes(Integer theTruncateRecentDatabaseTransactionsToBytes) {
		myTruncateRecentDatabaseTransactionsToBytes=theTruncateRecentDatabaseTransactionsToBytes;
	}

}
