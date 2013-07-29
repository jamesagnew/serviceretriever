package net.svcret.ejb.ejb;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceRegistry;

@MessageDriven(mappedName = BroadcastListenerBean.BUSINESS_BROADCAST_TOPIC, //- 
	activationConfig = { //-
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"), //-
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic") //- 
		}) //-
public class BroadcastListenerBean implements MessageListener {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BroadcastListenerBean.class);
	static final String BUSINESS_BROADCAST_TOPIC = "jms/BusinessBroadcastTopic";
	static final String UPDATE_SERVICE_REGISTRY = "UPDATE_SERVICE_REGISTRY";
	static final String UPDATE_USER_CATALOG = "UPDATE_USER_CATALOG";
	static final String UPDATE_CONFIG = "UPDATE_CONFIG";
	static final String UPDATE_MONITOR_RULES = "UPDATE_MONITOR_RULES";
	
	@Resource
	private MessageDrivenContext mdc;
	
	@EJB
	private ISecurityService mySecurityService;

	@EJB
	private IServiceRegistry myServiceRegistry;
	
	@EJB
	private IConfigService myConfigService;
	
	@Override
	public synchronized void onMessage(Message theMessage) {
		ourLog.debug("Handling new incoming broadcast");
		
		TextMessage msg = (TextMessage) theMessage;
		try {
			
			String text = msg.getText();
			if (text.startsWith(UPDATE_SERVICE_REGISTRY)) {
				ourLog.info("Received broadcast for updated service registry");
				myServiceRegistry.reloadRegistryFromDatabase();
			} else if (text.startsWith(UPDATE_USER_CATALOG)) {
				ourLog.info("Received broadcast for updated user catalog");
				mySecurityService.loadUserCatalogIfNeeded();
			} else if (text.startsWith(UPDATE_CONFIG)) {
				ourLog.info("Received broadcast for updated config");
				myConfigService.reloadConfigIfNeeded();
			} else if (text.startsWith(UPDATE_MONITOR_RULES)) {
				ourLog.info("Received broadcast for updated monitor rules");
				// Reload the service registry because the SR contains active rules
				myServiceRegistry.reloadRegistryFromDatabase();
			}
			
		} catch (JMSException e) {
			ourLog.error("Failed to process incoming message",e);
		}
	}
}