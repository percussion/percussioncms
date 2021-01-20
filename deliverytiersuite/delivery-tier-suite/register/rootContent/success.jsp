<%--
  ~     Percussion CMS
  ~     Copyright (C) Percussion Software, Inc.  1999-2020
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     (at your option) any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~      Mailing Address:
  ~
  ~      Percussion Software, Inc.
  ~      PO Box 767
  ~      Burlington, MA 01803, USA
  ~      +01-781-438-9900
  ~      support@percussion.com
  ~      https://www.percusssion.com
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<!DOCTYPE html>
<html lang="en" xml:lang="en"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:dcterms="http://purl.org/dc/terms/"
xmlns:perc="http://percussion.com/"
xmlns:xsd="http://www.w3.org/2001/XMLSchema">
    <head>
          <title>Success</title>
          <meta name="robots" content="noindex, nofollow" />
          <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
          <meta name="generator" content="Percussion"/>
                <link rel="stylesheet" href="resources/styles/success.css" type="text/css" media="all" />
                <script src="resources/js/jquery.js"></script>
                <script src="resources/js/jquery-ui.js"></script>
                <script src="resources/js/perc_constants.js" ></script>
        <script>
            jQuery(document).ready(function(){
                var firstName = "<%=request.getParameter("field-registration-first-name") %>";
                var lastName = "<%=request.getParameter("field-registration-last-name") %>";
                var email = "<%=request.getParameter("field-registration-email") %>";
                $("#perc-contact-name").text(firstName + " " + lastName);
                $("#perc-contact-email").text(email);
                
                // Include help URL
                var helpURL = $.perc_constants.HELP_URL;
                $(".perc-help-link-anchor").attr("href", helpURL);                
            });
        </script>
    </head>
    <body>
        <div class="perc-title">Thank you!</div>
        
        <div class="perc-main-body">
            <div class="perc-main-message">Hi <span class="perc-contact-name" id="perc-contact-name"></span>!</div>        
            <div class="perc-activation-message">
                We have sent your Activation code to:<br>
                <span class="perc-contact-email" id="perc-contact-email"></span>
            </div>
            <div class="perc-retrieve-message">
                Please, retrieve it to complete activation.
            </div>
            <div class="perc-thankyou-message">
                Thank you for registering!
            </div>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <br/>
            <div class="perc-tryagain">If you didn't receive your activation code, <br/>request a <a href="register.html">new code now</a> or <a class="perc-help-link-anchor" href="http://help.percussion.com" target="_blank">view our help site</a>.</div>
        </div>
    </body>
</html>
