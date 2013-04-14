package com.google.gwt.user.server.rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.Part;

@SuppressWarnings("deprecation")
public class RemoteServiceServlet {

	private MyServletRequest myServletRequest = new MyServletRequest(); 
	
	public HttpServletRequest getThreadLocalRequest() {
		return myServletRequest;
	}

	public class MyServletRequest implements HttpServletRequest
	{

		@Override
		public AsyncContext getAsyncContext() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getAttribute(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getCharacterEncoding() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public int getContentLength() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getContentType() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public DispatcherType getDispatcherType() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getLocalAddr() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Locale getLocale() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Enumeration<Locale> getLocales() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getLocalName() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public int getLocalPort() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getParameter(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Enumeration<String> getParameterNames() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String[] getParameterValues(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getProtocol() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public BufferedReader getReader() throws IOException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRealPath(String thePath) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRemoteAddr() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRemoteHost() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public int getRemotePort() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String thePath) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getScheme() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getServerName() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public int getServerPort() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public ServletContext getServletContext() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isAsyncStarted() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isAsyncSupported() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isSecure() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void removeAttribute(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void setAttribute(String theName, Object theO) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void setCharacterEncoding(String theEnv) throws UnsupportedEncodingException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public AsyncContext startAsync() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public AsyncContext startAsync(ServletRequest theRequest, ServletResponse theResponse) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean authenticate(HttpServletResponse theResponse) throws IOException, ServletException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getAuthType() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getContextPath() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Cookie[] getCookies() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public long getDateHeader(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getHeader(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Enumeration<String> getHeaders(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public int getIntHeader(String theName) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getMethod() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Part getPart(String theName) throws IOException, ServletException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getPathInfo() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getPathTranslated() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getQueryString() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRemoteUser() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRequestedSessionId() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getRequestURI() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public StringBuffer getRequestURL() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public String getServletPath() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public HttpSession getSession() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public HttpSession getSession(boolean theCreate) {
			return myHttpSession;
		}

		private MyHttpSession myHttpSession = new MyHttpSession();
		
		public class MyHttpSession implements HttpSession
		{

			@Override
			public long getCreationTime() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public String getId() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public long getLastAccessedTime() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public ServletContext getServletContext() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public void setMaxInactiveInterval(int theInterval) {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public int getMaxInactiveInterval() {
				throw new UnsupportedOperationException();
				
			}

			@SuppressWarnings("deprecation")
			@Override
			public HttpSessionContext getSessionContext() {
				throw new UnsupportedOperationException();
				
			}

			private Map<String, Object> myAttrs = new HashMap<String, Object>();
			
			@Override
			public Object getAttribute(String theName) {
				return myAttrs.get(theName);
			}

			@Override
			public Object getValue(String theName) {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public Enumeration<String> getAttributeNames() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public String[] getValueNames() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public void setAttribute(String theName, Object theValue) {
				myAttrs.put(theName, theValue);
			}

			@Override
			public void putValue(String theName, Object theValue) {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public void removeAttribute(String theName) {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public void removeValue(String theName) {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public void invalidate() {
				throw new UnsupportedOperationException();
				
			}

			@Override
			public boolean isNew() {
				throw new UnsupportedOperationException();
				
			}
			
		}
		
		
		@Override
		public Principal getUserPrincipal() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public boolean isUserInRole(String theRole) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void login(String theUsername, String thePassword) throws ServletException {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void logout() throws ServletException {
			throw new UnsupportedOperationException();
			
		}
		
	}

	
}



