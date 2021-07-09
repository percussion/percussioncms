<%@ page import="java.util.*" %>
<%@ page import="com.percussion.server.PSServer" %> 
<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ page import="com.percussion.i18n.PSLocaleManager" %>
<%@ page import="com.percussion.i18n.PSLocale" session="true" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
    prefix="rxcomp"%>
<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2021 Percussion Software, Inc.
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU Affero General Public License for more details.
  ~
  ~     Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<%
    String username = request.getParameter("j_username");
    String password = request.getParameter("j_password");
    String locale = request.getParameter("j_locale");
    String error = request.getParameter("j_error");
    String lang="en";
  
    if (username == null)
    username = "";
    if (password == null)
    password = "";
   
    if(locale==null){
		locale= PSI18nUtils.getSystemLanguage();
	}else{
		if(locale.contains("-"))
			lang=locale.split("-")[0];
		else
			lang=locale;
	}
    
    pageContext.setAttribute("locale",locale);

    String loginComplete = PSServer.getServerProps().getProperty("loginAutoComplete");
    String userVal = "";
    String passVal = "";
    String autoComplete = "";
        if(loginComplete != null && loginComplete.equalsIgnoreCase("off") ){
            autoComplete = "autocomplete='off'";
        }
     PSLocaleManager locManager = PSLocaleManager.getInstance();
%>
<!DOCTYPE html>
<html lang="<%=lang %>">
    <head>
        <title>${rxcomp:i18ntext('jsp_login@Percussion Login',locale)}</title> 	
        <style>
        body {background-color: #6C717C; font-family: Verdana; margin: 0; padding: 0; }
        .perc-login-logo {color: #121212; margin-top: 50px; margin-bottom: 50px;}
        #loginform .perc-form    { }
        #perc-forgot {color: #fff;}
		#perc-forgot-username{display:none;}
		#perc-forgot-pass{display:none;}
		#perc-register{display:none;}
		
        #perc-forgot:hover   {cursor:pointer;}
        input { padding: 0; }
        img:hover    {cursor: pointer;}
        .error {font-weight:bold; margin-top:10px; }
        .btn-primary {
			background-color: #133c55 !important;
			border-color: #FFFFFF !important;
			color: #FFFFFF;
		}
		</style>
        <script language="javascript">
        function setCursor() {
            // leave focus in username
            cmd = document.getElementById("perc-login-username");
            cmd.focus();    
        }
        </script>
	<link rel="stylesheet" type="text/css" href="/cm/cui/components/twitter-bootstrap-3.0.0/dist/css/bootstrap.min.css"/>
    <script
            src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=en-us"></script>
    <script src="/cm/cui/components/jquery/jquery.min.js"></script>
	<script src="/cm/cui/components/jquery-migrate/jquery-migrate.min.js"></script>

		<script src="/cm/cui/components/twitter-bootstrap-3.0.0/dist/js/bootstrap.min.js"></script>
    </head>
    <body onload="setCursor()">
	<div class="container">
	<div class='perc-login row center-block'>
		<form id="loginform" name="loginform" method="post" enctype="multipart/form-data">
			<div class='perc-login-logo'><img src="/sys_resources/images/percussion-logo.png" alt="${rxcomp:i18ntext('general@Percussion Logo Alt',locale)}" title="${rxcomp:i18ntext('general@Percussion Logo Title',locale)}"/></div>
			<div class='perc-form'> 
			   <div class="form-group">
			   <label for="perc-login-username" form="loginform">${rxcomp:i18ntext('jsp_login@User name',locale)}</label>
				<input type="text" id="perc-login-username"  name="j_username"  value="<%= username %>" tabindex="1" class="form-control" <%= autoComplete %>/>
				</div>
				<div class="form-group">
				<label for="perc-login-password" form="loginform">${rxcomp:i18ntext('jsp_login@Password',locale)}</label>
				<input type="password" id="perc-login-password" name="j_password" value="<%= password %>" tabindex="2" class="form-control" <%= autoComplete %>/>
				</div>
				<div class="form-group">
				<label for="perc-login-locale">${rxcomp:i18ntext('jsp_login@Locale',locale)}</label>
				<select id="perc-login-locale" name="j_locale" class="form-control">
				  <%  
					  Iterator<PSLocale> locales = locManager.getLocales();
					  while (locales.hasNext())
					  { 
				  	     PSLocale loc= locales.next();
					  String selected = "false";
					  if(loc.getName().equalsIgnoreCase("en-us"))
						  selected = "true";
				  %>
					<option selected="<%= selected %>" value="<%=loc.getName() %>"><%= loc.getDisplayName() %></option>
				  <% }%>
				</select>
				</div>
				<button type="submit" id="perc-login-button" form="loginform" class="btn btn-primary btn-default">${rxcomp:i18ntext('jsp_login@LoginButton',locale)}</button>
			</div>
		</form>
	</div>
	<div class="row">
		<div class="col-sm-4">
			<hr/>
		</div>
	</div>
	<div class="row">
	  <div id="perc-forgot-username" class="col-sm-4">
	  <p>
	  ${rxcomp:i18ntext('jsp_login@Forgot your username',locale)} <a href="#" title="${rxcomp:i18ntext('jsp_login@Forgot your username',locale)}">${rxcomp:i18ntext('general@click here',locale)}</a>
	  </p>
	  </div>
	  <div id="perc-forgot-pass" class="col-sm-8">
	  <p>
	   ${rxcomp:i18ntext('jsp_login@Forgot your password',locale)} <a href="#" title="${rxcomp:i18ntext('jsp_login@Forgot your password',locale)}">${rxcomp:i18ntext('general@click here',locale)}</a>
	  </p>
	  </div>
	</div>
			<div class="row">
			<div id="perc-register" class="span 12">
			<p>Don't have a login?  Click the button below to request access from your System Administrator.</p> 
			<button type="button" id="perc-register-button"class="btn btn-primary">Request an Account</button>
			</div>
			</div>
<%
if (error != null)
{
%>
			<div class="error"><%=error%></div>
<%  } 
%>
		
	</div>
     </div>
    </body>
</html>
