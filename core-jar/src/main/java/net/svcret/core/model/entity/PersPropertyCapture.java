package net.svcret.core.model.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.xpath.XPathExpression;

import net.svcret.admin.shared.model.DtoPropertyCapture;

@Table(name = "PX_PROP_CAP")
@Entity
public class PersPropertyCapture implements Serializable {

	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private PersPropertyCapturePk myPk;

	@Column(name = "XPATH_PATH", length = 300, nullable = true)
	private String myXpathExpression;

	@Transient
	private volatile transient XPathExpression myCompiledXpathExpression;

	@Column(name = "CAPTURE_ORDER", nullable = false)
	private int myOrder;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersPropertyCapture other = (PersPropertyCapture) obj;
		if (myPk == null) {
			if (other.myPk != null)
				return false;
		} else if (!myPk.equals(other.myPk))
			return false;
		return true;
	}

	public XPathExpression getCompiledXpathExpression() {
		return myCompiledXpathExpression;
	}

	public PersPropertyCapturePk getPk() {
		return myPk;
	}

	public String getXpathExpression() {
		return myXpathExpression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((myPk == null) ? 0 : myPk.hashCode());
		return result;
	}

	public void loadAllAssociations() {
		// nothing
	}

	public void merge(PersPropertyCapture thePers) {
		setXpathExpression(thePers.getXpathExpression());
	}

	public void setCompiledXpathExpression(XPathExpression theCompiledXpath) {
		myCompiledXpathExpression = theCompiledXpath;
	}

	public void setOrder(int theOrder) {
		myOrder = theOrder;
	}

	public void setPk(PersPropertyCapturePk thePk) {
		myPk = thePk;
	}

	public void setXpathExpression(String theXpathExpression) {
		myXpathExpression = theXpathExpression;
	}

	public DtoPropertyCapture toDto() {
		DtoPropertyCapture retVal = new DtoPropertyCapture();
		retVal.setPropertyName(getPk().getPropertyName());
		retVal.setXpathExpression(getXpathExpression());
		return retVal;
	}

	public static PersPropertyCapture fromDto(BasePersServiceVersion theSvcVer, DtoPropertyCapture theDto) {
		PersPropertyCapture retVal = new PersPropertyCapture();
		retVal.setPk(new PersPropertyCapturePk(theSvcVer, theDto.getPropertyName()));
		retVal.setXpathExpression(theDto.getXpathExpression());
		return retVal;
	}

}
