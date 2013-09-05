<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN">
<html>
<head>
<title>Login</title>
    
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/css/auth.css" />  
      
</head>
  
<body>
  <center>
      <div class="login">
         
        <!-- did we already try to login and it failed? -->
        <c:if test="false">
            <div class="authError">
                Invalid User Name or Password. Please try again.
            </div>
        </c:if>
 
        <form action="j_security_check" method="post">
          <fieldset>
            <legend>Login</legend>
                          
            <div>
              <label for="email">Email</label> 
              <input type="text" id="j_username" name="j_username"/>
            </div>
            <div>
              <label for="password">Password</label> 
              <input type="password" id="j_password" name="j_password"/>
            </div>
              
            <div class="buttonRow">
              <input type="submit" value="Login" />
            </div>
            
            </fieldset>
        </form> 
      </div>
  </center>
</body>
</html>
