<%@ page import="javax.naming.*,java.sql.*,javax.sql.*" %>
<%--
  ~     Percussion CMS
  ~     Copyright (C) 1999-2020 Percussion Software, Inc.
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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

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
					out.println(" Exception: " + se.getLocalizedMessage() + " getting connection");
					connok = false;
				}
			}
			catch(Exception e)
			{
				dsok = false;
				out.print(" Exception: " + e.getLocalizedMessage() + " getting datasource");
			}
			if (connok && dsok)
			{
				out.println(" OK");
			}
		}
	}
	catch(Exception e2)
	{
		out.println(e2.getLocalizedMessage());
	}
%>
</ul>
</BODY>
</HTML>
