package net.svcret.ejb.ejb;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import net.svcret.ejb.api.IBroadcastSender;
import net.svcret.ejb.api.IConfigService;
import net.svcret.ejb.api.IDao;
import net.svcret.ejb.ex.ProcessingException;
import net.svcret.ejb.model.entity.PersConfig;

@Stateless
public class ConfigServiceBean implements IConfigService {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ConfigServiceBean.class);

	private static final String STATE_KEY = SecurityServiceBean.class.getName() + "_VERSION";

	@EJB
	private IBroadcastSender myBroadcastSender;

	private transient PersConfig myConfig;

	private long myCurrentVersion;

	@EJB
	private IDao myDao;

	@Override
	public PersConfig getConfig() throws ProcessingException {
		if (myConfig!=null) {
			return myConfig;
		}
		PersConfig retVal = myDao.getConfigByPid(PersConfig.DEFAULT_ID);
		if (retVal == null) {
			retVal = new PersConfig();
			retVal.setDefaults();
			retVal=saveConfig(retVal);
		}
		return retVal;
	}

	@Override
	public void reloadConfigIfNeeded() {
		long newVersion = myDao.getStateCounter(STATE_KEY);
		if (newVersion == 0 || newVersion > myCurrentVersion) {
			loadConfig();
		}
	}

	@Override
	public PersConfig saveConfig(PersConfig theConfig) throws ProcessingException {
		PersConfig retVal = myDao.saveConfig(theConfig);
		myBroadcastSender.notifyConfigChanged();
		incrementStateVersion();
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
}
