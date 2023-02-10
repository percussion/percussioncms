

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
