package ca.uhn.sail.proxy.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import ca.uhn.sail.proxy.model.registry.ServiceEnvironment;
import ca.uhn.sail.proxy.model.registry.ServiceUsers;
import ca.uhn.sail.proxy.model.registry.ServiceUsers.ServiceUser;
import ca.uhn.sail.proxy.model.registry.ServiceUsers.ServiceUser.Permissions;
import ca.uhn.sail.proxy.model.registry.ServiceUsers.ServiceUser.Permissions.DomainPermission;
import ca.uhn.sail.proxy.model.registry.ServiceUsers.ServiceUser.Permissions.DomainPermission.ServicePermission;
import ca.uhn.sail.proxy.model.registry.ServiceVersion;
import ca.uhn.sail.proxy.model.registry.ServiceVersion.Url;
import ca.uhn.sail.proxy.model.registry.Services;
import ca.uhn.sail.proxy.model.registry.Services.Service;
import ca.uhn.sail.proxy.model.registry.WsSecUsernameClientAuthentication;
import ca.uhn.sail.proxy.model.registry.WsSecUsernameServerAuthentication;

public class ConfigWriter {

	public static void main(String[] args) throws IOException, JAXBException {
		
		Services svcs = new Services();
		
		Service svc = svcs.addService();
		svc.setDomainId("CDR");
		svc.setServiceId("PatientService");
		svc.setServiceName("CDR Patient Service");
		
		ServiceVersion version = new ServiceVersion();
		version.setVersionId("1.0");
		version.setActive(true);
		version.setWsdlUrl("http://uhnvesb01d.uhn.on.ca:18780/tst-uhn-ehr-ws/services/ehrPatientService?wsdl");
		version.getUrls().add(new Url(ServiceEnvironment.DEV, "dev1", "http://uhnvesb01d.uhn.on.ca:18780/tst-uhn-ehr-ws/services/ehrPatientService"));
		version.getUrls().add(new Url(ServiceEnvironment.DEV, "dev2", "http://uhnvesb02d.uhn.on.ca:18780/tst-uhn-ehr-ws/services/ehrPatientService"));
		svc.getVersions().add(version);
		
		WsSecUsernameClientAuthentication user = new WsSecUsernameClientAuthentication();
		user.setUsername("theUsername");
		user.setPassword("thePassword");
		version.getClientAuthentication().getElements().add(user);

		version.getServerAuthentication().getElements().add(new WsSecUsernameServerAuthentication("ldap_auth"));
		
		File f = new File("src/main/resources/service.xml");
		FileWriter w = new FileWriter(f, false);
		w.append(svcs.toXml());
		w.close();
		
		ServiceUsers users = new ServiceUsers();
		ServiceUser suser = users.addUser();
		suser.setUsername("theUsername");
		Permissions perms = suser.getPermissions();
		
		DomainPermission perm = perms.addDomainPermission();
		perm.setDomainId("CDR");
		perm.setAllServices(false);
		perm.setAllEnvironments(false);
		perm.getEnvironments().add("DEV");
		
		ServicePermission svcPerm = perm.addServicePermission();
		svcPerm.setServiceId("PatientService");
		svcPerm.addVersion("1.0");

		f = new File("src/main/resources/user.xml");
		w = new FileWriter(f, false);
		w.append(users.toXml());
		w.close();

	}
	
}
