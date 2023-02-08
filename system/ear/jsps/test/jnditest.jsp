<%@ page import="javax.naming.*,java.sql.*,javax.sql.*"
		 import="com.percussion.services.utils.jspel.PSRoleUtilities,com.percussion.server.PSServer"
		 import="com.percussion.i18n.PSI18nUtils" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"
%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%
	String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

	if(isEnabled == null)
		isEnabled="false";

	if(isEnabled.equalsIgnoreCase("false")){
		response.sendError(HttpServletResponse.SC_NOT_FOUND);
	}
	String fullrolestr = PSRoleUtilities.getUserRoles();

	if (!fullrolestr.contains("Admin"))
		response.sendError(HttpServletResponse.SC_NOT_FOUND);

%>


<HTML>
<HEAD><TITLE>JNDI Test Page</TITLE></HEAD>
<BODY>

<P>Testing bound jdbc resources:
<ul>
<%
	try
	{
		Context ctx = new InitialContext();
		ctx = (Context) ctx.lookup("java:comp/env");
		NamingEnumeration e1 = null;
		try
		{
			e1 = ctx.list("jdbc");
		}
		catch(Exception e)
		{
		   try
		   {
				// Reset initial context, probably BEA
				ctx = new InitialContext();
				e1 = ctx.list("jdbc");
		   }
		   catch(Exception ee)
		   {
		      // Try again, probably JBoss
		      ctx = new InitialContext();
		      ctx = (Context) ctx.lookup("java:");
		      e1 = ctx.list("jdbc");
		   }
		}
		while(e1.hasMore())
		{
			NameClassPair pair = (NameClassPair) e1.next();
			out.println("<li>" + pair.getName());
			boolean dsok, connok;

			DataSource ds = null;	
			connok = false;
			try
			{
				ds = (DataSource) ctx.lookup("jdbc/" + pair.getName());
				dsok = true;
				Connection c = null;
				try
				{
					c = ds.getConnection();
					c.close();
					connok = true;
				}
				catch(Exception se)
				{
					out.println(" Exception: " + se.getMessage() + " getting connection");
					connok = false;
				}
			}
			catch(Exception e)
			{
				dsok = false;
				out.print(" Exception: " + e.getMessage() + " getting datasource");
			}
			if (connok && dsok)
			{
				out.println(" OK");
			}
		}
	}
	catch(Exception e2)
	{
		out.println(e2.getMessage());
	}
%>
</ul>
</BODY>
</HTML>
