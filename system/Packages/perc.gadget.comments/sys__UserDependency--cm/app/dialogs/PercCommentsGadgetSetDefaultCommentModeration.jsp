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

<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <title>Default Comment Moderation Dialog</title>
        <link rel="stylesheet" type="text/css" href="/cm/themes/smoothness/jquery-ui-1.8.9.custom.css" />
        <link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/common/perc_common_gadget.css" />
        <link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/common/css/PercDataTable.css" />
        <link rel="stylesheet" type="text/css" href="/cm/gadgets/repository/perc_comments_gadget/PercCommentsGadgetSetDefaultCommentModeration.css" />
    </head>
    <body style="background:#E6E6E9">
<%
    String state = request.getParameter("state");
    if(state == null)
        state = "";
    String site  = request.getParameter("site");
    if(site == null)
        site = "Unknown";
        
    String approved = state.equals("APPROVED") ? "CHECKED='true'" : "";
    String rejected = state.equals("REJECTED") ? "CHECKED='true'" : "";
%>
        <div class='perc-dialog-instructions'>Default moderation for <span class='perc-site-name'><%=site%></span> is:
            <div class='perc-dialog-controls'>
                <div class='perc-dialog-control'><input id='perc-dialog-approve' <%=approved%> type='radio' name='perc-moderate'><span>Approved</span></div>
                <div class='perc-dialog-control'><input id='perc-dialog-reject'  <%=rejected%> type='radio' name='perc-moderate'><span>Rejected</span></div>
            </div>
        </div>
    </body>
</html>
