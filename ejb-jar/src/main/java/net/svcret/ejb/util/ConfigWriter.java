package net.svcret.ejb.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import net.svcret.ejb.model.registry.ServiceEnvironment;
import net.svcret.ejb.model.registry.ServiceUsers;
import net.svcret.ejb.model.registry.ServiceVersion;
import net.svcret.ejb.model.registry.Services;
import net.svcret.ejb.model.registry.WsSecUsernameClientAuthentication;
import net.svcret.ejb.model.registry.WsSecUsernameServerAuthentication;
import net.svcret.ejb.model.registry.ServiceUsers.ServiceUser;
import net.svcret.ejb.model.registry.ServiceUsers.ServiceUser.Permissions;
import net.svcret.ejb.model.registry.ServiceUsers.ServiceUser.Permissions.DomainPermission;
import net.svcret.ejb.model.registry.ServiceUsers.ServiceUser.Permissions.DomainPermission.ServicePermission;
import net.svcret.ejb.model.registry.ServiceVersion.Url;
import net.svcret.ejb.model.registry.Services.Service;


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
