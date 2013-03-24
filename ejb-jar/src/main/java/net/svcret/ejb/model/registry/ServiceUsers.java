package net.svcret.ejb.model.registry;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(namespace = "urn:sail:proxy:registry", name = "ServiceUsers")
public class ServiceUsers {

	private static JAXBContext ourJaxbContext;

	@XmlElement(name = "user")
	private List<ServiceUser> myUsers;

	public ServiceUser addUser() {
		ServiceUser retVal = new ServiceUser();
		getUsers().add(retVal);
		return retVal;
	}

	/**
	 * @return the users
	 */
	public List<ServiceUser> getUsers() {
		if (myUsers == null) {
			myUsers = new ArrayList<ServiceUsers.ServiceUser>();
		}
		return myUsers;
	}

	public String toXml() throws JAXBException {
		if (ourJaxbContext == null) {
			ourJaxbContext = JAXBContext.newInstance(ServiceUsers.class);
		}

		StringWriter retVal = new StringWriter();
		Marshaller marshaller = ourJaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(this, retVal);
		return retVal.toString();
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(namespace = "urn:sail:proxy:registry", name = "ServiceUserType")
	@XmlType(propOrder= {"myUsername", "myPermissions"})
	public static class ServiceUser {
		
		@XmlElement(name = "permissions")
		private Permissions myPermissions;

		@XmlElement(name = "username", required = true)
		private String myUsername;

		/**
		 * @return the permissions
		 */
		public Permissions getPermissions() {
			if (myPermissions == null) {
				myPermissions = new Permissions();
			}
			return myPermissions;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return myUsername;
		}

		/**
		 * @param theUsername
		 *            the username to set
		 */
		public void setUsername(String theUsername) {
			myUsername = theUsername;
		}

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlType(namespace = "urn:sail:proxy:registry", name = "ServiceUserPermissionsType")
		public static class Permissions {

			@XmlElement(name = "domain")
			private List<DomainPermission> myDomainPermissions;

			public DomainPermission addDomainPermission() {
				DomainPermission retVal = new DomainPermission();
				getDomainPermissions().add(retVal);
				return retVal;
			}

			/**
			 * @return the domainPermissions
			 */
			public List<DomainPermission> getDomainPermissions() {
				if (myDomainPermissions == null) {
					myDomainPermissions = new ArrayList<DomainPermission>();
				}
				return myDomainPermissions;
			}

			@XmlAccessorType(XmlAccessType.FIELD)
			@XmlRootElement(namespace = "urn:sail:proxy:registry", name = "DomainPermission")
			@XmlType(propOrder= {"myDomainId", "myAllServices", "myAllEnvironments", "myServices", "myEnvironments"})
			public static class DomainPermission {

				@XmlElement(name = "all_environments", required = false)
				private Boolean myAllEnvironments;

				@XmlElement(name = "all_services", required = false)
				private Boolean myAllServices;

				@XmlAttribute(name = "domain_id", required = true)
				private String myDomainId;

				@XmlElement(name = "service")
				private List<ServicePermission> myServices;

				@XmlElement(name = "environment")
				private List<String> myEnvironments;

				/**
				 * @return the environments
				 */
				public List<String> getEnvironments() {
					if (myEnvironments == null) {
						myEnvironments = new ArrayList<String>();
					}
					return myEnvironments;
				}

				public ServicePermission addServicePermission() {
					ServicePermission retVal = new ServicePermission();
					getServices().add(retVal);
					return retVal;
				}

				/**
				 * @return the allEnvironments
				 */
				public Boolean getAllEnvironments() {
					return myAllEnvironments;
				}

				/**
				 * @return the domainId
				 */
				public String getDomainId() {
					return myDomainId;
				}

				/**
				 * @return the services
				 */
				public List<ServicePermission> getServices() {
					if (myServices == null) {
						myServices = new ArrayList<ServicePermission>();
					}
					return myServices;
				}

				/**
				 * @return the allServices
				 */
				public boolean isAllServices() {
					return myAllServices != null && myAllServices;
				}

				/**
				 * @param theAllEnvironments the allEnvironments to set
				 */
				public void setAllEnvironments(Boolean theAllEnvironments) {
					myAllEnvironments = theAllEnvironments;
				}

				/**
				 * @param theAllServices
				 *            the allServices to set
				 */
				public void setAllServices(Boolean theAllServices) {
					myAllServices = theAllServices;
				}

				/**
				 * @param theDomainId
				 *            the domainId to set
				 */
				public void setDomainId(String theDomainId) {
					myDomainId = theDomainId;
				}

				@XmlAccessorType(XmlAccessType.FIELD)
				@XmlType(namespace = "urn:sail:proxy:registry", name = "ServiceUserAllServicePermission")
				public static class AllServicePermission {

				}

				@XmlAccessorType(XmlAccessType.FIELD)
				@XmlType(namespace = "urn:sail:proxy:registry", name = "ServiceUserServicePermission")
				public static class ServicePermission {

					@XmlAttribute(name = "service_id", required = true)
					private String myServiceId;

					@XmlElement(name = "version", required = true)
					private List<String> myVersions;

					public void addVersion(String theVersion) {
						getVersions().add(theVersion);
					}

					/**
					 * @return the serviceId
					 */
					public String getServiceId() {
						return myServiceId;
					}

					/**
					 * @return the versions
					 */
					public List<String> getVersions() {
						if (myVersions == null) {
							myVersions = new ArrayList<String>();
						}
						return myVersions;
					}

					/**
					 * @param theServiceId
					 *            the serviceId to set
					 */
					public void setServiceId(String theServiceId) {
						myServiceId = theServiceId;
					}

				}

			}

		}


	}

}
