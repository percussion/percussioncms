<%@ page import="com.percussion.server.PSServer, com.percussion.services.utils.jspel.PSRoleUtilities, java.io.BufferedReader, java.io.FileNotFoundException"
         import="java.io.FileReader"
%>
<%@ page import="com.percussion.i18n.PSI18nUtils" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>

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
<!DOCTYPE html>
<html lang="en">
<head>

    <title>
        Server Log
    </title>
    <!-- Link to bootstrap CSS and JS preprocessor. It's important to stay modern and maintainable -->
    <link href="/rx_resources/css/bootstrap/3.3.4/bootstrap.min.css" rel="stylesheet">


</head>
<body>

<style type="text/css">
    textarea.form-control {
        overflow-y: scroll;
    }

    #DefaultJNDIReplacer {
        position: relative;
        top: -25px;
        float: right;
        border-top: none;
        border-top-right-radius: 0px;
        border-top-left-radius: 0px;

    }

    tr {
        border-top: none;
        border-bottom: none;
    }

    .table > tbody > tr > td, .table > tbody > tr > th, .table > tfoot > tr > td, .table > tfoot > tr > th, .table > thead > tr > td, .table > thead > tr > th {
        border-top: none;
    }

    #sidebar {
        height: 200px;
        width: 200px;
        position: fixed;

        right: 10px;
    }

    #sidebar div {
        position: relative;
        top: -20px;
    }

</style>

<!-- Main container div for the entire page. This pads both the left and right sides of the page. -->
<div class="container">
    <nav class="navbar navbar-inverse">
        <a class="navbar-brand" href="#">Support Tools</a>
        <ul class="nav navbar-nav">
            <li>
                <a href="/test/sql.jsp">SQL Editor</a>
            </li>
            <li class="active">
                <a href="#">Server Log</a>
            </li>
            <li>
                <a href="/test/search.jsp">Test JSR-170 searches</a>
            </li>
            <li>
                <a href="/test/countFolderItem.jsp">Count Items in Folder</a>
            </li>
            <li>
                <a href="/test/velocitylog.jsp">Velocity Log</a>
            </li>
        </ul>
    </nav>

    <!-- THIS FORM CANNOT REDIRECT, thats why its action is a pound. We will be posting to the server and getting back the sql.  -->

</div>
<!-- <div class="container well well-sm" id="sidebar">
  <h4 id="sidebar-date"></h4>
    <div class="list-group">
  <div class="row" style="padding-bottom:10px;">
  <a href="#" id="nextStartup" class="list-group-item success">
    Next Startup
  </a>
  <a href="#" id="previousStartup" class="list-group-item warning">Previous Startup</a>
  </div>
    <div class="row" style="padding-bottom:10px;">
  <a href="#" id="nextWarning" class="list-group-item warning">
    Next Warning
  </a>
  <a href="#" id="previousWarning" class="list-group-item success">Previous Warning</a>
  </div>
    <div class="row" style="padding-bottom:10px;">
  <a href="#" id="nextSError" class="list-group-item success">
    Next Error
  </a>
  <a href="#" id="previousError" class="list-group-item success">Previous Error</a>
  </div>

</div>
</div> -->

<!-- Another container to keep the page looking good -->
<table class="table">
    <tbody>


    <%
        try {
            String jspPath = PSServer.getRxDir().toString();
            String txtFilePath = null;
            txtFilePath = jspPath + "/jetty/base/logs/server.log";

            BufferedReader reader = new BufferedReader(new FileReader(txtFilePath));
            StringBuilder sb = new StringBuilder();
            String line = "";
            String lastLine = "";
            String displayClass = "";
            int i = 0;
            sb.setLength(0); // set length of buffer to 0
            sb.trimToSize(); // trim the underlying buffer
            while ((line = reader.readLine()) != null) {
                if (line == "")
                    continue;
                if (line.contains("INFO"))
                    displayClass = "";
                if (line.contains("ERROR"))
                    displayClass = "danger";
                if (line.contains("WARN"))
                    displayClass = "warning";
                if (line.contains("start()"))
                    displayClass = "success";


                sb.append("<tr rownum='" + i + "' id='line-" + i + "' class='" + displayClass + "'><td>" + line + "</td></tr>");
                lastLine = line;
                i++;
            }
            out.println(sb.toString());
            sb.setLength(0); // set length of buffer to 0
            sb.trimToSize(); // trim the underlying buffer
        } catch (FileNotFoundException e) {
            out.println("server.log not found");
        }
    %>
    </tbody>
</table>
</body>
<script src="/cm/jslib/profiles/3x/jquery/jquery-3.6.0.js"></script>
<script src="/cm/jslib/profiles/3x/libraries/bootstrap/js/bootstrap.bundle.js"></script>
<script>
    $(function () {
        var row;
        var rownum;
        $("#DefaultJNDIReplacer").on("click", function (e) {
            e.preventDefault();
            $("#dburl").val($("#dburl").attr("placeholder"));
            $("#dbquery").val($("#dbquery").attr("placeholder"));
        })
        {
            /* Stuff to do when the mouse enters the element */
        }
        $("tr").on("mouseenter", function () {
            row = $(this);
            if (typeof row !== "undefined")
                row.addClass("active");
        })
        .on("mouseleave", function () {
            row = $(this);
            if (typeof row !== "undefined")
                row.removeClass('active');
        });

        $('#nextStartup').on("click",function (event) {

            if (typeof row === "undefined")
                rownum = 0;
            else
                rownum = row.attr("rownum")
            console.log("")
            var newrow = $('tr.success')[rownum + 1];

            if (typeof newrow !== "undefined")
                $('table').scrollTop(newrow.top);
            else
                alert("No More Startups");


        });
        $('#previousStartup').on("click", function (event) {
            var newrow;


        });

        $('#nextWarning').on("click", function (event) {

            if (row === undefined)
                rownum = 0;
            else
                rownum = row.attr("rownum")

            var newrow = $('tr.warning')[rownum + 1];
            console.log(newrow);


            var w = $(window);

            if (newrow) {
                w.scrollTop(newrow.offset().top - (newrow.height() / 2));
            }


            console.log("scrollin");
            newrow.ensureVisible();


        });
        $('#previousWarning').on("click", function (event) {
            var newrow;
        });

        $('#nextError').on("click", function (event) {
            var newrow;


        });
        $('#previousError').on("click", function (event) {
            var newrow;


        });

    })
</script>
</html>
