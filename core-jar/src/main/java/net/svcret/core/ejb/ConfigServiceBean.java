package net.svcret.core.ejb;

import javax.annotation.PostConstruct;

import net.svcret.admin.api.UnexpectedFailureException;
import net.svcret.admin.shared.model.RetrieverNodeTypeEnum;
import net.svcret.admin.shared.util.Validate;
import net.svcret.core.api.IConfigService;
import net.svcret.core.api.IDao;
import net.svcret.core.dao.DaoBean;
import net.svcret.core.ejb.nodecomm.IBroadcastSender;
import net.svcret.core.model.entity.PersConfig;
import net.svcret.core.security.SecurityServiceBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.annotations.VisibleForTesting;

@Service
public class ConfigServiceBean implements IConfigService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ConfigServiceBean.class);
	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";

	@Autowired
	private IBroadcastSender myBroadcastSender;

	private transient PersConfig myConfig;

	private long myCurrentVersion;
	@Autowired
	private IDao myDao;
	private String myNodeId;

	private RetrieverNodeTypeEnum myNodeType;

	@Autowired
	private PlatformTransactionManager myPlatformTransactionManager;

	@Override
	public PersConfig getConfig() {
		return myConfig;
	}


	@Override
	public String getNodeId() {
		return myNodeId;
	}
	
	@Override
	public RetrieverNodeTypeEnum getNodeType() {
		return myNodeType;
	}

	private void incrementStateVersion() {
		long newVersion = myDao.incrementStateCounter(STATE_KEY);
		ourLog.debug("State counter is now {}", newVersion);
	}

	private void loadConfig() {
		myConfig = myDao.getConfigByPid(PersConfig.DEFAULT_ID);
		myCurrentVersion = myDao.getStateCounter(STATE_KEY);
		if (myConfig == null) {
			myConfig = new PersConfig();
			myDao.saveConfigInNewTransaction(myConfig);
		}
	}

	@PostConstruct
	public void postConstruct() {
		TransactionTemplate tmpl = new TransactionTemplate(myPlatformTransactionManager);
		tmpl.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				loadConfig();
			}
		});
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


	@VisibleForTesting
	public void setConfigForUnitTest() {
		myConfig = new PersConfig();
	}
}
