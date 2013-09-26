package net.svcret.admin.shared.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;


public class DtoServiceVersionUrlList extends BaseDtoList<GServiceVersionUrl> {

	private static final long serialVersionUID = 1L;

	@XmlElement(name="Url")
	@Override
	public List<GServiceVersionUrl> getListForJaxb() {
		return super.getListForJaxb();
	}
	
	public GServiceVersionUrl getUrlWithId(String theName) {
		for (GServiceVersionUrl next : this) {
			if (next.getId() != null && next.getId().equals(theName)) {
				return next;
			}
		}
		return null;
	}

	public GServiceVersionUrl getUrlWithPid(long theUrlPid) {
		for (GServiceVersionUrl next : this) {
			if (next.getPid()==theUrlPid) {
				return next;
			}
		}
		return null;
	}

}
