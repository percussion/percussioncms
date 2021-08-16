<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         import="javax.jcr.query.Query"
         import="javax.jcr.query.QueryResult"
         import="javax.jcr.query.RowIterator"
         import="javax.jcr.query.Row"
         import="javax.jcr.Value"
         import="com.percussion.services.contentmgr.IPSContentMgr, com.percussion.services.contentmgr.PSContentMgrLocator"
         import="java.util.Map, java.util.HashMap"
         import="org.jsoup.Jsoup"
         import="org.jsoup.safety.Whitelist"
         import="org.owasp.encoder.Encode"
         import="com.percussion.server.PSServer"
         import="com.percussion.services.utils.jspel.*"
         import="com.percussion.i18n.PSI18nUtils"
%>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%
   String fullrolestr = PSRoleUtilities.getUserRoles();

   String isEnabled = PSServer.getServerProps().getProperty("enableDebugTools");

   if(isEnabled == null)
      isEnabled="false";

   if(isEnabled.equalsIgnoreCase("false")){
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
              + "/ui/RxNotAuthorized.jsp"));
   }

   if (!fullrolestr.contains("Admin"))
      response.sendRedirect(response.encodeRedirectURL(request.getContextPath()
              + "/ui/RxNotAuthorized.jsp"));

%>

<!DOCTYPE html>
<html lang="en">
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
   <title>JCR Query Debugger</title>
</head>
<body>
<%
   IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
   String[] allNames = expandParam(request.getParameterValues("qname"), 6);
   String[] allValues = expandParam(request.getParameterValues("qvalue"),
           6);
   String lastquery = request.getParameter("querybody");
   if (lastquery == null || lastquery.trim().length() == 0)
   {
      lastquery = "select rx:displaytitle, jcr:path from rx:rffgeneric";
   }

%>
<h1>JCR Query Debugger</h1>
<p>
   Use this page to debug JSR-170 SQL-like queries
</p>
<csrf:form method="POST" action="/test/search.jsp">
   Parameters
   <br>

   <table title="Query Parameters" style="border-width:1px;">
      <caption>Query Parameters</caption>
      <thead>
      <tr><th>Name</th><th>Value</th></tr>
      </thead>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[0] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[0] %>" /> </td>
      </tr>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[1] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[1] %>" /> </td>
      </tr>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[2] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[2] %>" /> </td>
      </tr>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[3] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[3] %>" /> </td>
      </tr>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[4] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[4] %>" /> </td>
      </tr>
      <tr>
         <td><input type="text" name="qname" maxlength="30" value="<%= allNames[5] %>" /> </td>
         <td><input type="text" name="qvalue" maxlength="256" value="<%= allValues[5] %>" /> </td>
      </tr>

   </table>
   <br/>
   <textarea name="querybody" rows="5" cols="60">
<%=sanitizeForHtml(lastquery)%>
</textarea>
   <br/>
   <input type="submit" name="execute" value="execute" label="Execute" />
</csrf:form>
<p>
   <%if (request.getMethod().equals("POST")
           && request.getParameter("execute").equals("execute"))
   {
      try
      {
         out.println("<pre>");
         Map pmap = buildParamMap(allNames, allValues, out);
         String qry = request.getParameter("querybody");
         if (qry != null && qry.trim().length() > 0)
         {
            out.println("\nQuery is " + qry.trim());
         }
         long before = System.currentTimeMillis();
         //Checking for SQL Injection and return nothing incase found
         String sqlStmtTest  = qry;
         Pattern p = Pattern.compile("(.*?)" + "=" + "(.*)");
         while (sqlStmtTest != null) {
            Matcher m = p.matcher(sqlStmtTest);
            if (m.matches()) {
               String firstSubString = m.group(1).trim();
               String firstWord = firstSubString.substring(firstSubString.lastIndexOf(" ") + 1);// may be empty
               String secondSubString = m.group(2).trim();
               String secondWord = secondSubString;
               int i = secondSubString.indexOf(' ');
               if (i != -1) {
                  secondWord = secondSubString.substring(0, i);
               }
               // e.g 1 = 1 if found then return else check in rest string
               if (firstWord != null && firstWord.trim().equals(secondWord)) {
                  return;
               } else {
                  sqlStmtTest = secondSubString.trim();
               }
            } else {
               break;
            }
         }
         Query query = mgr.createQuery(qry, Query.SQL);
         QueryResult qresults = mgr.executeQuery(query, -1, pmap);
         long after = System.currentTimeMillis();
         long elapsed = after - before;
         out.println("Elapsed time is " + String.valueOf(elapsed) + " milliseconds ");
         out.println("</pre>");
         String[] columns = qresults.getColumnNames();
         out.println("<table>");
         printTableHeader(columns, out);
         printTableRows(qresults, out);
         out.println("</table>");
      } catch (Exception ex)
      {
         out.println("<pre> Unexpected Exception \n");
         out.println(ex.toString());
         StackTraceElement[] stacktrace = ex.getStackTrace();
         for (int m = 0; m < stacktrace.length; m++)
         {
            out.println(stacktrace[m].toString());
         }
         out.println("</pre>");
      }
   }
   %>

   <%! private String[] expandParam(String[] inParam, int size)
   {
      int ilen = 0;
      if (inParam != null)
      {
         ilen = inParam.length;
      }
      if (ilen >= size)
      {
         return inParam;
      }
      String[] outParam = new String[size];
      java.util.Arrays.fill(outParam, "");
      if (ilen > 0)
      {
         System.arraycopy(inParam, 0, outParam, 0, ilen);
      }
      return outParam;
   }
      private Map buildParamMap(String[] allNames, String[] allValues,
                                javax.servlet.jsp.JspWriter out)
              throws java.io.IOException
      {
         Map pmap = new HashMap();
         for (int i = 0; i < allNames.length; i++)
         {
            String pNameLoop = sanitizeForHtml(allNames[i]);
            String pValLoop = sanitizeForHtml(allValues[i]);
            if (pNameLoop.trim().length() > 0 && pValLoop.trim().length() > 0)
            {
               out.println(pNameLoop + " " + pValLoop);
               pmap.put(pNameLoop, pValLoop);
            }
         }
         return pmap;
      }
      private void printTableHeader(String[] columns,
                                    javax.servlet.jsp.JspWriter out)
              throws java.io.IOException
      {
         out.println("<thead><tr>");
         for (int j = 0; j < columns.length; j++)
         {
            out.println("<th>" + sanitizeForHtml(columns[j]) + "</th>");
         }
         out.println("</tr></thead>");
      }
      private void printTableRows(QueryResult qresults,
                                  javax.servlet.jsp.JspWriter out)
              throws java.io.IOException, javax.jcr.RepositoryException
      {
         RowIterator rows = qresults.getRows();
         while (rows.hasNext())
         {
            out.println("<tr>");
            Row nrow = rows.nextRow();
            Value[] nvalues = nrow.getValues();
            for (int k = 0; k < nvalues.length; k++)
            {
               String kval = "&nbsp;";
               if (nvalues[k] != null)
               {
                  kval = Encode.forHtml(nvalues[k].getString());
               }
               out.println("<td>" + kval + "</td>");
            }
            out.println("</tr>");
         }
      }

      private String sanitizeForHtml(String input){
         String ret = null;
         if(input != null){
            ret = Jsoup.clean(input, Whitelist.none());
         }
         return ret;
      }
   %>
</p>


</body>
</html>
