package net.svcret.proxy.app;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.webapp.WebAppContext;

public class AdminApp {

	private Server myServer;

	public AdminApp() {
		
		myServer = new Server(8888);
		Context context = new Context(myServer, "/", Context.SESSIONS);

		WebAppContext webapp = new WebAppContext();
	    webapp.setContextPath("/");
	    webapp.setWar(warURL);
	    server.setHandler(webapp);
	    
		
	}
	
}
