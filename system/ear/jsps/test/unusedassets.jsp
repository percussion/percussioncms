<%@ page import="com.percussion.services.contentmgr.IPSContentMgr, com.percussion.services.contentmgr.PSContentMgrLocator, com.percussion.services.utils.jspel.PSRoleUtilities, javax.jcr.Value"
import="javax.jcr.query.Query"
import="javax.jcr.query.QueryResult"
import="javax.jcr.query.Row"
import="javax.jcr.query.RowIterator"
import="javax.naming.Context"
import="javax.naming.InitialContext"
import="javax.naming.NamingException"
import="javax.servlet.jsp.JspWriter"
import="javax.sql.DataSource"
import="java.sql.Connection"
import="java.sql.ResultSet"
import="java.sql.SQLException, java.sql.Statement"
import="com.percussion.i18n.PSI18nUtils"
import="com.percussion.server.PSServer"
%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%
	String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

	if(isEnabled == null)
		isEnabled="false";

	if(isEnabled.equalsIgnoreCase("false")){
		response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
				+ "/ui/RxNotAuthorized.jsp"));
	}
	String fullrolestr = PSRoleUtilities.getUserRoles();

	if (!fullrolestr.contains("Admin"))
		response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
				+ "/ui/RxNotAuthorized.jsp"));

%>
<%!
public void writeToFile(JspWriter out, IPSContentMgr mgr, String query) throws Exception {
	query = query.substring(0,query.length()-4);
	
	Query query2 = mgr.createQuery(query, Query.SQL);
	QueryResult qresults = mgr.executeQuery(query2, -1, null);
	RowIterator rows = qresults.getRows();
	while (rows.hasNext())
	{
		Row nrow = rows.nextRow();
		Value[] nvalues = nrow.getValues();
		if(nvalues[0] != null  && nvalues[1] != null && nvalues[2]!=null ){
			String cid = nvalues[0].getString();
			String path = nvalues[1].getString();
		path = path.replace("//Folders/$System$/","//");
		String file = nvalues[2].getString();
		out.println("\"" + cid + "\", " + "\"" + path + "/" + file + "\"");
	   }
	}
}
%><%
response.setContentType("text/csv");
response.setHeader("Content-Disposition", "attachment; filename=unusedassets.csv");

IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
String fullrolestr = PSRoleUtilities.getUserRoles();

if (fullrolestr.contains("Admin") == false)
   	response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
      	+ "/ui/RxNotAuthorized.jsp"));

String dburl="java:jdbc/RhythmyxData";

String dbquery = "SELECT DISTINCT CS.CONTENTID FROM CT_PERCIMAGEASSET INNER JOIN CONTENTSTATUS CS ON CS.CONTENTID=CT_PERCIMAGEASSET.CONTENTID WHERE CS.CONTENTID NOT IN (SELECT DEPENDENT_ID FROM PSX_OBJECTRELATIONSHIP WHERE CONFIG_ID !=3) AND CS.CONTENTID NOT IN(SELECT CHILDID FROM PSX_MANAGEDLINK)";
dbquery = dbquery + " UNION SELECT DISTINCT CS.CONTENTID FROM CT_PERCFILEASSET INNER JOIN CONTENTSTATUS CS ON CS.CONTENTID=CT_PERCFILEASSET.CONTENTID WHERE CS.CONTENTID NOT IN (SELECT DEPENDENT_ID FROM PSX_OBJECTRELATIONSHIP WHERE CONFIG_ID !=3) AND CS.CONTENTID NOT IN(SELECT CHILDID FROM PSX_MANAGEDLINK)";

InitialContext ctx;
DataSource ds=null;
Connection conn=null;
Statement stmt=null;
ResultSet rs = null;

try {
	ctx = new InitialContext();
	Context envCtx = (Context) ctx.lookup("java:comp/env");				// Get the initial context
	ds = (DataSource) ctx.lookup(dburl);								// Get the Context provided by the user (JNDI Name)
	conn = ds.getConnection();
	stmt = conn.createStatement();										// Finally allow us to send information.

	rs= stmt.executeQuery(dbquery);										// Run the query, build a table. (SEE LINES 72 to EOF)
    
	String jcrQuery = "SELECT sys_contentid, jcr:path, sys_title FROM percImageAsset,percFileAsset WHERE ";
	Integer i = 0;
	
	while(rs.next()) {
		if(i % 100 == 0 && i > 1) {
			jcrQuery = jcrQuery + "rx:sys_contentid=" + rs.getInt(1) + " OR ";
			writeToFile(out, mgr, jcrQuery);
			jcrQuery = "SELECT sys_contentid, jcr:path, sys_title FROM percImageAsset,percFileAsset WHERE ";
		} 
		else {
			jcrQuery = jcrQuery + "rx:sys_contentid=" + rs.getInt(1) + " OR ";
		}

		i=i+1;
	}
	
	writeToFile(out, mgr, jcrQuery);
	
} catch (SQLException se) {
	se.getMessage();
} catch (NamingException ne) {
	ne.getMessage();
} finally{
 	try{ rs.close();}catch(Exception e){}
 	try{ stmt.close();}catch(Exception e){}
  	try{ conn.close();}catch(Exception e){}
}
%>
