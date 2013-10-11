<%@page import="net.svcret.admin.shared.model.DtoServiceVersionSoap11"%>
<%@page import="net.svcret.admin.shared.model.DtoConfig"%>
<%@page import="net.svcret.admin.shared.model.BaseDtoServiceVersion"%>
<%@page import="net.svcret.admin.shared.model.GService"%>
<%@page import="net.svcret.admin.shared.model.DtoDomain"%>
<%@page import="net.svcret.admin.shared.model.GDomainList"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<!doctype html>
<html>
<head>
<title>Service Registry</title>
    
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/auth.css" />  
      
</head>
  
<body>
	<h1>Service Registry</h1>
	
	<table>
		<tr>
			<td>Domain</td>
			<td>Service</td>
			<td>Version</td>
			<td>Endpoint</td>
		</tr>
	
<%
		String baseUrl = ((DtoConfig)request.getAttribute("config")).getProxyUrlBases().iterator().next();
		GDomainList domainList = (GDomainList)request.getAttribute("domainList");

		for (DtoDomain nextDomain : domainList) {
			for (GService nextService : nextDomain.getServiceList()) {
		for (BaseDtoServiceVersion nextVersion : nextService.getVersionList()) { 
			if (nextVersion.getDisplayInPublicRegistry() == Boolean.FALSE) {
		continue;
			}
			if (nextVersion.getDisplayInPublicRegistry() == null && nextService.getDisplayInPublicRegistry() == Boolean.FALSE) {
		continue;
			}
			if (nextVersion.getDisplayInPublicRegistry() == null && nextService.getDisplayInPublicRegistry() == null && nextDomain.getDisplayInPublicRegistry() != Boolean.TRUE) {
		continue;
			}
	%>
		<tr>
			<td><%=nextDomain.getName() %></td>
			<td><%=nextService.getName() %></td>
			<td><%=nextVersion.getName() %></td>
			<td>
				<%
					if (!nextVersion.isUseDefaultProxyPath()) {
						out.append("<a href=\"" + baseUrl + nextVersion.getExplicitProxyPath() + "\">Endpoint</a>");
					} else {
						out.append("<a href=\"" + baseUrl + nextVersion.getDefaultProxyPath() + "\">Endpoint</a>");
					}
				
					if (nextVersion instanceof DtoServiceVersionSoap11) {
						if (!nextVersion.isUseDefaultProxyPath()) {
							out.append(" <a href=\"" + baseUrl + nextVersion.getExplicitProxyPath() + "?wsdl\">WSDL</a>");
						} else {
							out.append(" <a href=\"" + baseUrl + nextVersion.getDefaultProxyPath() + "?wsdl\">WSDL</a>");
						}
					}
				%>
			</td>
		</tr>		
<%
			
		}
	}
}
%>
	
	</table>
	
</body>
</html>
