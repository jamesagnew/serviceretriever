package net.svcret.ejb.ejb.nodecomm;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;

import net.svcret.admin.shared.model.DtoStickySessionUrlBinding;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IRuntimeStatus;
import net.svcret.ejb.api.ISecurityService;
import net.svcret.ejb.api.IServiceRegistry;

@MessageDriven(mappedName = BroadcastListenerBean.BUSINESS_BROADCAST_TOPIC, // -
activationConfig = { // -
@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"), // -
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic") // -
})
// -
public class BroadcastListenerBean implements MessageListener {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BroadcastListenerBean.class);
	static final String BUSINESS_BROADCAST_TOPIC = "jms/BusinessBroadcastTopic";
	static final String UPDATE_CONFIG = "UPDATE_CONFIG";
	static final String UPDATE_MONITOR_RULES = "UPDATE_MONITOR_RULES";
	static final String UPDATE_SERVICE_REGISTRY = "UPDATE_SERVICE_REGISTRY";
	static final String UPDATE_USER_CATALOG = "UPDATE_USER_CATALOG";
	static final String URL_STATUS_CHANGED = "URL_STATUS_CHANGED";
	static final String STICKY_SESSION_CHANGED ="STICKY_SESSION_CHANGED";
	static final String MSG_ARG0 = "MSG_ARG0";

	@Resource
	private MessageDrivenContext mdc;

	@EJB
	@Autowired
	private IConfigService myConfigService;

	@EJB
	@Autowired
	private IRuntimeStatus myRuntimeStatus;

	@EJB
	@Autowired
	private ISecurityService mySecurityService;

	@EJB
	@Autowired
	private IServiceRegistry myServiceRegistry;

	@Override
	public synchronized void onMessage(Message theMessage) {
		ourLog.debug("Handling new incoming broadcast");

		TextMessage msg = (TextMessage) theMessage;

		try {
			long age = System.currentTimeMillis() - msg.getJMSTimestamp();
			ourLog.info("Handing incoming JMS broadcase with age of {}ms", age);

//			if (age < 5000) {
//				long sleep = 5000 - age;
//				ourLog.debug("Going to delay message processing by {}ms", sleep);
//				try {
//					Thread.sleep(sleep);
//				} catch (InterruptedException e) {
//					// ignore
//				}
//			} else if (age >= 5000) {
//				// nothing
//			}

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
			} else if (text.startsWith(URL_STATUS_CHANGED)) {
				Long pid = (Long) msg.getObjectProperty(MSG_ARG0);
				ourLog.info("Received broadcast for updated URL status: {}", pid);
				myRuntimeStatus.reloadUrlStatus(pid);
			} else if (text.startsWith(STICKY_SESSION_CHANGED)) {
				DtoStickySessionUrlBinding binding = (DtoStickySessionUrlBinding) msg.getObjectProperty(MSG_ARG0);
				ourLog.info("Received broadcast for updated sticky session: {}", binding);
				myRuntimeStatus.updatedStickySessionBinding(binding);
			}

		} catch (JMSException e) {
			ourLog.error("Failed to process incoming message", e);
		}
	}
}