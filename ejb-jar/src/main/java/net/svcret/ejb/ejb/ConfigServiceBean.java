package net.svcret.ejb.ejb;

import java.util.ArrayList;
import java.util.List;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.ejb.nodecomm.IBroadcastSender;
import net.svcret.ejb.model.entity.PersConfig;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ConfigServiceBean implements IConfigService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ConfigServiceBean.class);
	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";
	public static final String SYSTEM_PROPERTY_AUDITLOG_PATH = "net.svcret.auditlog.path";
	public static final String SYSTEM_PROPERTY_NODEID = "net.svcret.nodeid";

	public static final String SYSTEM_PROPERTY_NODETYPE = "net.svcret.nodetype";

	public static final String SYSTEM_PROPERTY_SECONDARY_REFRESHURLS = "net.svcret.secondarynodes.refreshurls";

	@Autowired
	private IBroadcastSender myBroadcastSender;

	private transient PersConfig myConfig;

	private long myCurrentVersion;
	@Autowired
	private IDao myDao;
	private String myNodeId;

	private RetrieverNodeTypeEnum myNodeType;

	@Override
	public PersConfig getConfig() throws UnexpectedFailureException {
		if (myConfig != null) {
			return myConfig;
		}
		PersConfig retVal = myDao.getConfigByPid(PersConfig.DEFAULT_ID);
		if (retVal == null) {
			retVal = new PersConfig();
			retVal.setDefaults();
			retVal = saveConfig(retVal);
		}

		return retVal;
	}

	@Override
	public String getFilesystemAuditLoggerPath() {
		return System.getProperty(SYSTEM_PROPERTY_AUDITLOG_PATH);
	}

	public String getNodeId() {
		return myNodeId;
	}

	public RetrieverNodeTypeEnum getNodeType() {
		return myNodeType;
	}

	@Override
	public List<String> getSecondaryNodeRefreshUrls() {
		ArrayList<String> retVal = new ArrayList<String>();

		String[] allUrls = System.getProperty(SYSTEM_PROPERTY_SECONDARY_REFRESHURLS, "").split(",");
		for (String next : allUrls) {
			if (StringUtils.isNotBlank(next)) {
				retVal.add(next.trim());
			}
		}

		return retVal;
	}

	private void incrementStateVersion() {
		long newVersion = myDao.incrementStateCounter(STATE_KEY);
		ourLog.debug("State counter is now {}", newVersion);
	}

	private void loadConfig() {
		myConfig = myDao.getConfigByPid(PersConfig.DEFAULT_ID);
		myCurrentVersion = myDao.getStateCounter(STATE_KEY);
	}

	@Override
	public void reloadConfigIfNeeded() {
		long newVersion = myDao.getStateCounter(STATE_KEY);
		if (newVersion == 0 || newVersion > myCurrentVersion) {
			loadConfig();
		}
	}

	@Override
	public PersConfig saveConfig(PersConfig theConfig) throws UnexpectedFailureException {
		PersConfig retVal = myDao.saveConfigInNewTransaction(theConfig);
		myBroadcastSender.notifyConfigChanged();
		incrementStateVersion();
		return retVal;
	}

	/**
	 * For unit tests only
	 */
	@VisibleForTesting
	public void setBroadcastSender(IBroadcastSender theBroadcastSender) {
		myBroadcastSender = theBroadcastSender;
	}

	/**
	 * For unit tests only
	 */
	@VisibleForTesting
	public void setDao(DaoBean theDao) {
		myDao = theDao;
	}

	@Required
	public void setNodeId(String theNodeId) {
		Validate.notBlank(theNodeId, "NodeType");
		myNodeId = theNodeId;
	}

	@Required
	public void setNodeType(RetrieverNodeTypeEnum theNodeType) {
		Validate.notNull(theNodeType, "NodeType");
		myNodeType = theNodeType;
	}
}
