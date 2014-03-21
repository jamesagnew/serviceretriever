package net.svcret.app.beans;

import java.io.File;
import java.io.IOException;

import net.svcret.core.api.IServiceOrchestrator;
import net.svcret.core.api.IServiceRegistry;
import net.svcret.core.server.BaseServerBean;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

public class AdminServerBean extends BaseServerBean {

	@Autowired
	private IServiceOrchestrator myServiceOrchestrator;

	@Autowired
	private IServiceRegistry myServiceRegistry;

	private Resource myWar;

	private String myBaseTempDir;

	@Required
	public void setJaasConfig(String theConfig) {
		System.setProperty("java.security.auth.login.config", theConfig);
	}

	@Required
	public void setBaseTempDir(String theDir) {
		myBaseTempDir = theDir;
		new File(myBaseTempDir).mkdirs();
	}

	@Required
	public void setWar(Resource theWar) throws IOException {
		if (!theWar.getFile().exists()) {
			throw new IllegalArgumentException("Unknown WAR location: " + theWar.getFile().getAbsolutePath());
		}

		myWar = theWar;
	}

	@Override
	protected void configureServerBeforeStarting() throws Exception {
		WebAppContext adminCtx = new WebAppContext();
		adminCtx.setAttribute(WebAppContext.BASETEMPDIR, myBaseTempDir);
		adminCtx.setWar("file:" + myWar.getFile().getAbsolutePath());
		adminCtx.setDescriptor("file:");
		adminCtx.setContextPath("/");
		getProxyServer().setHandler(adminCtx);

		JAASLoginService loginService = new JAASLoginService();
		getProxyServer().addBean(loginService, true);
		loginService.setName("svcret-realm");
		loginService.setLoginModuleName("svcret-realm");
	}

}
