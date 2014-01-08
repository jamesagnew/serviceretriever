package net.svcret.admin.shared.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import net.svcret.admin.shared.util.XmlConstants;

@XmlType(namespace = XmlConstants.DTO_NAMESPACE, name = "MonitorRuleActiveCheckOutcomeList")
@XmlAccessorType(XmlAccessType.FIELD)
public class DtoMonitorRuleActiveCheckOutcomeList extends BaseDtoObject {

	private static final long serialVersionUID = 1L;

	@XmlElement(name = "Outcome")
	@XmlElementWrapper(name="Outcomes")
	private List<DtoMonitorRuleActiveCheckOutcome> myOutcomes;

	@XmlElement(name = "Url")
	private String myUrl;

	@XmlElement(name = "UrlId")
	private String myUrlId;

	@XmlElement(name = "UrlPid")
	private long myUrlPid;

	public List<DtoMonitorRuleActiveCheckOutcome> getOutcomes() {
		if (myOutcomes == null) {
			myOutcomes = new ArrayList<DtoMonitorRuleActiveCheckOutcome>();
		}
		return Collections.unmodifiableList(myOutcomes);
	}

	public String getUrl() {
		return myUrl;
	}

	public String getUrlId() {
		return myUrlId;
	}

	public long getUrlPid() {
		return myUrlPid;
	}

	public void setUrl(String theUrl) {
		myUrl = theUrl;
	}

	public void setUrlId(String theUrlId) {
		myUrlId = theUrlId;
	}

	public void setUrlPid(long theUrlPid) {
		myUrlPid = theUrlPid;
	}

	public void setOutcomes(List<DtoMonitorRuleActiveCheckOutcome> theOutcomesList) {
		myOutcomes = theOutcomesList;
		Collections.sort(myOutcomes, new Comparator<DtoMonitorRuleActiveCheckOutcome>() {
			@Override
			public int compare(DtoMonitorRuleActiveCheckOutcome theO1, DtoMonitorRuleActiveCheckOutcome theO2) {
				return theO1.getTransactionTime().compareTo(theO2.getTransactionTime());
			}
		});
	}

	public Collection<DtoMonitorRuleActiveCheckOutcome> getOutcomesFromMostRecent() {
		ArrayList<DtoMonitorRuleActiveCheckOutcome> retVal = new ArrayList<DtoMonitorRuleActiveCheckOutcome>(myOutcomes);
		Collections.reverse(retVal);
		return retVal;
	}

}
