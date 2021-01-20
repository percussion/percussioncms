<%@page import="com.percussion.soln.linkback.utils.LinkbackUtils"%>
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
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<jsp:useBean id="message" class="java.lang.String" scope="request"/>

<%!public String metaId = LinkbackUtils.LINKBACK_PARAM_NAME;

    public String bookmarkletUrl(String basePath, String target) {

        String js = "javascript:void(window.open('" + basePath + "linkback/" + target + "?" + metaId
                + "=' + (document.getElementById('" + metaId + "') ? document.getElementById('" + metaId
                + "').content : \'\')));";
        return js;
    }%>	
<%
	    String path = request.getContextPath();
	    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
	            + path + "/";
	%>


<html>
<head>
<title>Help for Linkback to Action Panel or Active Assembly Servlet</title>
<style type="text/css">
span.error {
color:red;
}
a.bmk {
border:1px outset #DDDDDD;
padding:1px;
vertical-align:1px;
}
a.bmk {
background:#D8DFFF none repeat scroll 0%;
color:darkgreen;
font-family:sans-serif;
font-size:80%;
text-decoration:none;
}
</style>
</head>
<body>
<%
    if (message != null && message.length() > 0) {
%>
<span class="error">Error: <%=message%></span>
<%
    }
%>
<h1>Help with Linkback</h1>


<p>To use linkback, you must add linkback bookmarklets to your
browser. You can add bookmarklets to all browsers supported by
Percussion CM System (Internet Explorer 6.0 and 7.0, Firefox 2.0.0.18
and 3.0, and Safari 3.1.2).</p>
<p>To add a bookmarklet to your browser:</p>
<p>In Firefox and Safari, ensure that your browser displays the
Bookmark bar, then drag the bookmarklet you want to use to the Bookmark
bar. Three bookmarklet options are listed below.</p>
<p>(To display the Bookmark bar in Firefox, go to the Menu bar and
choose View > Toolbar > Bookmarks Toolbar. If this option is checked,
the Bookmark toolbar is displayed.</p>
<p>To display the Bookmark bar in Safari, go to the Menu bar and
choose View > Show Bookmarks Toolbar.)</p>
<p>In Internet Explorer, ensure that your browser displays the Links
toolbar, then drag the bookmarklet you want to use to the Links toolbar.
Three bookmark options are listed below. (To display the Links toolbar,
right-click in the Menu bar and on the popup menu check <em>Links</em>.)
</p>
<p>To install the bookmarklets, drag one or more of these links to
your Bookmarks of Links toolbar:</p>
<ul>
	<li>to access Content Items using the Active Assembly Interface,
	add this bookmarklet: <a class="bmk"
		href="<%=bookmarkletUrl(basePath, "aa")%>"
		title="Rhythmyx Active Assembly">Rhythmyx Active Assembly</a>;</li>
	<li>to access Content Items from Content Explorer, add this
	bookmarklet: <a class="bmk" href="<%=bookmarkletUrl(basePath, "cx")%>"
		title="Rhythmyx Content Explorer">Rhythmyx Content Explorer</a>.</li>
</ul>
NOTE: If you have JavaScript disabled for security reasons, dragging and
dropping the link will not work. In that case, to add a bookmarklet,
ensure that the Bookmarks or links toolbar is displayed.
<ul>
	<li>When using Firefox, right-click on the bookmarklet you want to
	add and from the popup menu choose <em>Bookmark this link</em>. Firefox
	displays the Add Bookmark dialog. In the <strong>Create in</strong>
	field, choose <em>Bookmarks toolbar</em> and click [OK].</li>
	<li>When using Internet Explorer, right-click on the bookmarklet
	you want to add and from the popup menu choose <em>Add toFavorites</em>.
	Internet Explorer displays a Security Alert dialog. Click [Yes] to
	continue adding your bookmark. Internet Explorer displays the Add a
	Favorite dialog. In the <strong>Create in</strong> field, choose <em>Links</em>
	and click [Add].</li>
</ul>

</body>
</html>
