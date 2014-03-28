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
 
        <form action="j_security_check" method="post" onsubmit="return prepareSubmit(this);">
          <fieldset>
            <legend>Login</legend>
                          
            <div>
              <label for="j_username">Username</label> 
              <input type="text" id="j_username" name="j_username"/>
            </div>
            <div>
              <label for="j_password">Password</label> 
              <input type="password" id="j_password" name="j_password"/>
            </div>
              
            <div class="buttonRow">
              <input type="submit" value="Login" />
            </div>
            
            </fieldset>
        </form> 
      </div>
  </center>
  
  <script type="text/javascript">
  
		function prepareSubmit(form) {
		    // Extract the fragment from the browser's current location.
		    var hash = decodeURIComponent(self.document.location.hash);
		 
		    // The fragment value may not contain a leading # symbol
		    if (hash && hash.indexOf("#") === 0) {
		        hash = hash.substring(1);
		        setCookie("sr-first-page", hash);
		    } else {
		    	setCookie("sr-first-page", "");
		    }
		   
		    // Append the fragment to the current action so that it persists to the redirected URL.
		    form.action = form.action + "#" + hash;
		    return true;
		}
  
		function setCookie(c_name,value,exdays) {
			var exdate=new Date();
			exdate.setDate(exdate.getDate() + exdays);
			var c_value=escape(value) + ((exdays==null) ? "" : "; expires="+exdate.toUTCString());
			document.cookie=c_name + "=" + c_value;
		}	
  
  </script>
  
</body>
</html>
