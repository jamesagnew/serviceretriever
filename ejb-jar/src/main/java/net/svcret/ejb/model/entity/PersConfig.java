package net.svcret.ejb.model.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name="PX_CONFIG")
public class PersConfig {

	public static final long DEFAULT_ID = 1L;

	@Id
	@Column(name="PID")
	private long myPid = DEFAULT_ID;
	
	@Version()
	@Column(name = "OPTLOCK")
	private int myOptLock;

	@OneToMany(cascade=CascadeType.ALL, mappedBy="myConfig")
	private Collection<PersConfigProxyUrlBase> myProxyUrlBases;

	/**
	 * @return the proxyUrlBases
	 */
	public Collection<PersConfigProxyUrlBase> getProxyUrlBases() {
		if (myProxyUrlBases==null) {
			myProxyUrlBases=new ArrayList<PersConfigProxyUrlBase>();
		}
		return Collections.unmodifiableCollection(myProxyUrlBases);
	}

	public void setDefaults() {
		addProxyUrlBase(new PersConfigProxyUrlBase("http://localhost:8080/service"));
	}

	public void addProxyUrlBase(PersConfigProxyUrlBase theBase) {
		theBase.setConfig(this);
		getProxyUrlBases();
		myProxyUrlBases.add(theBase);
	}
	
}
