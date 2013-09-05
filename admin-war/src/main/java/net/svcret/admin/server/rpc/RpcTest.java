package net.svcret.admin.server.rpc;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class RpcTest {

	public static void main(String[] args) throws NamingException {
//		System.setProperty("org.omg.CORBA.ORBInitialHost", "home");
//		System.setProperty("java.naming.factory.initial", value)
		
		
	     Properties props = new Properties();
	     props.setProperty("java.naming.factory.initial", 
	                       "com.sun.enterprise.naming.SerialInitContextFactory");
	     props.setProperty("java.naming.factory.url.pkgs", 
	                       "com.sun.enterprise.naming");
	     props.setProperty("java.naming.factory.state",
	                       "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
	     props.setProperty("org.omg.CORBA.ORBInitialHost", "192.168.1.3");
	     props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
	     InitialContext ctx = new InitialContext(props);
	  

//		Properties p = new Properties();
//		p.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
//		p.setProperty("java.naming.provider.url", "home:3700");
//		Context ctx = new InitialContext(p);
		
		Object obj = ctx.lookup("java:global/ServiceRetriever-1.0/serviceretriever-ejb-jar-1.0/AdminServiceBean");
		System.out.println(obj);
		
	}
	
}
