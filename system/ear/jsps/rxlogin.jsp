<%@ page import="java.util.*" %>
<%@ page import="com.percussion.server.PSServer" %>
<%@ page import="com.percussion.i18n.PSI18nUtils" %>
<%@ page import="com.percussion.i18n.PSLocaleManager" %>
<%@ page import="com.percussion.i18n.PSLocale" session="true" %>
<%@ page import="com.percussion.i18n.PSLocaleException" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="http://rhythmyx.percussion.com/components"
		   prefix="rxcomp"%>
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
	String autoComplete;
	if(loginComplete != null && loginComplete.equalsIgnoreCase("off") ){
		autoComplete = "autocomplete='off'";
	}else{
		autoComplete = "autocomplete='on'";
	}

	PSLocaleManager locManager = PSLocaleManager.getInstance();

%>
<!DOCTYPE html>
<html lang="<%=lang %>">
<head>
	<title>${rxcomp:i18ntext('jsp_login@Percussion Login',locale)}</title>
	<style>
		body {
			font-family: Verdana, serif; margin: 0; padding: 0; }
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
	<script>
		function setCursor() {
			// leave focus in username
			cmd = document.getElementById("perc-login-username");
			cmd.focus();
		}
	</script>
	<link rel="stylesheet" type="text/css" href="/cm/jslib/profiles/3x/libraries/bootstrap/css/bootstrap.min.css"/>
	<script
			src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;sys_lang=en-us"></script>
	<script src="/JavaScriptServlet"></script>
	<script src="/cm/jslib/profiles/3x/jquery/jquery-3.6.0.js"></script>
	<script src="/cm/jslib/profiles/3x/jquery/jquery-migrate-3.3.2.js"></script>

	<script src="/cm/jslib/profiles/3x/libraries/bootstrap/js/bootstrap.min.js"></script>
</head>
<body onload="setCursor()">
<div class="container">
	<div class='perc-login row center-block'>
		<csrf:form id="loginform" name="loginform" method="post" enctype="multipart/form-data" action="login">
			<div class='perc-login-logo'><img src="/sys_resources/images/percussion-logo.png" alt="${rxcomp:i18ntext('general@Percussion Logo Alt',locale)}" title="${rxcomp:i18ntext('general@Percussion Logo Title',locale)}"/></div>
			<div class='perc-form'>
				<div class="form-group">
					<label for="perc-login-username">${rxcomp:i18ntext('jsp_login@User name',locale)}</label>
					<input type="text" id="perc-login-username"  name="j_username"  value="<%= username %>" tabindex="1" class="form-control" <%= autoComplete %>/>
				</div>
				<div class="form-group">
					<label for="perc-login-password">${rxcomp:i18ntext('jsp_login@Password',locale)}</label>
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
				<div class="form-group">
					<label for="perc-login-select-ui">${rxcomp:i18ntext('jsp_login@SelectUI',locale)}</label>
					<input id="perc-login-select-ui" name="j_selectUI" type="checkbox">
				</div>
				<button type="submit" id="perc-login-button" form="loginform" class="btn btn-primary btn-default">${rxcomp:i18ntext('jsp_login@LoginButton',locale)}</button>
			</div>
		</csrf:form>
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
			<button type="button" id="perc-register-button" class="btn btn-primary">Request an Account</button>
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
<script>
	jQuery(function ($) {
		var checked = localStorage.getItem('perc-login-select-ui-checked') === "true";
		$('#perc-login-select-ui').prop('checked', checked);
		$('#perc-login-select-ui').on("change",function () {
			var isChecked = $(this).is(':checked');
			localStorage.setItem('perc-login-select-ui-checked', isChecked);
		});
	});
</script>
</body>
</html>
