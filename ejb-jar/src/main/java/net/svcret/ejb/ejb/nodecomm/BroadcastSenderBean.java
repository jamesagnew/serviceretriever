package net.svcret.ejb.ejb.nodecomm;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import net.svcret.ejb.ex.UnexpectedFailureException;
import net.svcret.ejb.model.entity.PersStickySessionUrlBinding;

import org.apache.commons.lang3.Validate;

@Stateless
public class BroadcastSenderBean implements IBroadcastSender {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(BroadcastSenderBean.class);

	@Resource(mappedName = "jms/TopicConnectionFactory")
	private TopicConnectionFactory myConnectionFactory;

	@Resource(mappedName = BroadcastListenerBean.BUSINESS_BROADCAST_TOPIC)
	private Topic myBusinessBroadcastTopic;

	private void sendMessage(String theText) throws UnexpectedFailureException {
		Long theLongArgument = null;

		sendMessage(theText, theLongArgument);

	}

	private void sendMessage(String theText, Serializable theLongArgument) throws UnexpectedFailureException {
		Validate.notBlank(theText);

		ourLog.info("Sending broadcast message: {}", theText);

		TopicConnection con = null;
		TopicSession ses = null;
		TopicPublisher sender = null;
		try {
			con = myConnectionFactory.createTopicConnection();
			ses = con.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			sender = ses.createPublisher(myBusinessBroadcastTopic);
			TextMessage msg = ses.createTextMessage(theText);

			if (theLongArgument != null) {
				msg.setObjectProperty(BroadcastListenerBean.MSG_ARG0, theLongArgument);
			}

			sender.send(msg);
		} catch (JMSException e) {
			throw new UnexpectedFailureException(e);
		} finally {
			if (sender != null) {
				try {
					sender.close();
				} catch (JMSException e) {
					ourLog.debug("Failed to close sender", e);
				}
			}
			if (ses != null) {
				try {
					ses.close();
				} catch (JMSException e) {
					ourLog.debug("Failed to close session", e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (JMSException e) {
					ourLog.debug("Failed to close connection", e);
				}
			}
		}

		ourLog.info("Done sending broadcast message: {}", theText);
	}

	@Override
	public void notifyUserCatalogChanged() throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.UPDATE_USER_CATALOG);
	}

	@Override
	public void notifyServiceCatalogChanged() throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.UPDATE_SERVICE_REGISTRY);
	}

	@Override
	public void notifyConfigChanged() throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.UPDATE_CONFIG);
	}

	@Override
	public void monitorRulesChanged() throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.UPDATE_MONITOR_RULES);
	}

	@Override
	public void notifyUrlStatusChanged(Long thePid) throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.URL_STATUS_CHANGED);
	}

	@Override
	public void notifyNewStickySession(PersStickySessionUrlBinding theExisting) throws UnexpectedFailureException {
		sendMessage(BroadcastListenerBean.STICKY_SESSION_CHANGED, theExisting.toDao());
	}

}
