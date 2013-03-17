package ca.uhn.sail.proxy.api;

import javax.ejb.Local;

import ca.uhn.sail.proxy.model.entity.PersDomain;

@Local
public interface IAdminService {

	PersDomain addDomain(String theId, String theName);

}
