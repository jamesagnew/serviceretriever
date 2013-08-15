package net.svcret.ejb.ejb;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import net.svcret.admin.shared.enm.MonitorRuleTypeEnum;
import net.svcret.ejb.api.IMonitorNotifier;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.BasePersServiceVersion;
import net.svcret.ejb.model.entity.PersMonitorRuleFiring;
import net.svcret.ejb.model.entity.PersMonitorRuleFiringProblem;
import net.svcret.ejb.util.SLF4JLogChute;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

@Stateless
public class MonitorNotifierService implements IMonitorNotifier {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MonitorNotifierService.class);

	@Resource(name = "mail/svcret")
	private Session mySession;

	String generateEmail(PersMonitorRuleFiring theFiring) {
		VelocityEngine ve = new VelocityEngine();
		ve.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new SLF4JLogChute(ourLog));

		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		ve.setProperty("classpath." + RuntimeConstants.RESOURCE_LOADER + ".class", ClasspathResourceLoader.class.getName());
		ve.setProperty(RuntimeConstants.VM_LIBRARY, "");

		ve.init();
		Template t = ve.getTemplate("/net/svcret/ejb/vm/MonitorRuleTemplate.vm");

		Context context = new VelocityContext();
		context.put("ruleName", theFiring.getRule().getRuleName());
		context.put("firingDate", FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(theFiring.getStartDate()));
		context.put("firingTime", FastDateFormat.getTimeInstance(FastDateFormat.MEDIUM).format(theFiring.getStartDate()));

		List<Problem> problems = new ArrayList<Problem>();
		for (PersMonitorRuleFiringProblem next : theFiring.getProblems()) {
			problems.add(new Problem(next));
		}
		Collections.sort(problems);
		context.put("problems", problems);

		StringWriter writer = new StringWriter();
		t.merge(context, writer);
		return writer.getBuffer().toString();
	}

	@Override
	public void notifyFailingRule(PersMonitorRuleFiring theFiring) throws ProcessingException {

		try {
			MimeMessage msg = new MimeMessage(mySession);

			Collection<String> notifyEmails = new TreeSet<String>(theFiring.getRule().getNotifyEmails());
			for (String nextAddress : notifyEmails) {
				InternetAddress addressTo = new InternetAddress(nextAddress);
				msg.addRecipient(Message.RecipientType.TO, addressTo);
			}
			
			ourLog.info("Alert Email will be sent to addresses: {}", notifyEmails);

			String emailContent = generateEmail(theFiring);

			msg.setSubject("SR Monitor Problem");
			msg.setContent(emailContent, "text/html");
			msg.setSentDate(new Date());
			
			Transport.send(msg);
			
		} catch (MessagingException e) {
			throw new ProcessingException("Failed to send email");
		}

	}

	public static final class Problem implements Comparable<Problem> {

		private String myDomain;
		private String myService;
		private String myVersion;
		private String myUrlId;
		private String myUrl;
		private MonitorRuleTypeEnum myRuleType;
		private TreeSet<String> myIssues;

		public String getRuleType() {
			return myRuleType.name();
		}

		public String getRuleTypeName() {
			return myRuleType.getFriendlyName();
		}

		public Problem(PersMonitorRuleFiringProblem theProblem) {
			myDomain = theProblem.getServiceVersion().getService().getDomain().getDomainName();
			myService = theProblem.getServiceVersion().getService().getServiceName();
			myVersion = theProblem.getServiceVersion().getVersionId();
			
			myUrlId = theProblem.getUrl().getUrlId();
			myUrl = theProblem.getUrl().getUrl();
			
			myRuleType = theProblem.getFiring().getRule().getRuleType();
			
			myIssues = new TreeSet<String>();
			if (theProblem.getLatencyAverageMillisPerCall() != null) {
				StringBuilder b = new StringBuilder();
				b.append("Service had latency of ");
				b.append(theProblem.getLatencyAverageMillisPerCall());
				b.append("ms/call ");
				b.append(" (threshold is ");
				b.append(theProblem.getLatencyThreshold());
				b.append("ms)");
				if (theProblem.getLatencyAverageOverMinutes() != null) {
					b.append(" over ");
					b.append(theProblem.getLatencyAverageOverMinutes());
					b.append(" minutes");
				}
				myIssues.add(b.toString());
			}
			
			if (theProblem.getFailedUrlMessage()!=null) {
				myIssues.add("URL failed with message: " + theProblem.getFailedUrlMessage());
			}
			
			if (theProblem.getCheckFailureMessage()!=null) {
				myIssues.add("Check failed with message: " + theProblem.getCheckFailureMessage());
			}
		}

		public TreeSet<String> getIssues() {
			return myIssues;
		}

		public String getUrlId() {
			return myUrlId;
		}

		public String getUrl() {
			return myUrl;
		}

		public String getDomainName() {
			return myDomain;
		}

		public String getServiceName() {
			return myService;
		}

		public String getVersionId() {
			return myVersion;
		}

		@Override
		public int compareTo(Problem theO) {
			int retVal = StringUtils.defaultString(getDomainName()).compareTo(StringUtils.defaultString(theO.getDomainName()));
			if (retVal != 0) {
				return retVal;
			}
			retVal = StringUtils.defaultString(getServiceName()).compareTo(StringUtils.defaultString(theO.getServiceName()));
			if (retVal != 0) {
				return retVal;
			}
			retVal = StringUtils.defaultString(getVersionId()).compareTo(StringUtils.defaultString(theO.getVersionId()));
			return retVal;
		}

	}

}
