<%@ page import="java.util.*,com.percussion.i18n.PSTmxResourceBundle"%>
    <%@ page import="com.percussion.services.utils.jspel.PSRoleUtilities" %>
    <%@ page pageEncoding="UTF-8" contentType="text/javascript; charset=UTF-8" %>
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

<% response.setHeader("x-content-type-options","nosniff");%>
    <%--
    This Tmx jsp makes it possible for the tmx key/value pairs to be obtained by the
    server by any type of client.

    Query Parameters:
    =================

    sys_lang:  The locale (defaults to en_us)

    prefix:  This is a comma delimited list of prefixes to filter
             what the server returns to the client. If this value is * then all key/values
             will be returned. (defaults to javascript.  to maintain backward compatibility)

    mode:   This is the output return mode (defaults to js)

          Modes:

          js:   Returns the key/values in a javascript object and also defines
                a function to get the value, call psxGetLocalMessage(key, args)  or
                I18N.message(key, args) to
                retrieve the key value. This should be called as an external javascript
                include (i.e. <script src="/Rhythmyx/tmx/tmx.jsp?sys_lang=en_us&mode=js"></script>)

          json: Returns the key/values in a json object of the following format:
                  {tmxmessages: {"name1": "value1", "name2": "value2"}}

          xml:  Returns the key/values as xml in the following format:

                 <!ELEMENT message (#PCDATA)>
                 <!ATTLIST message
                            key CDATA #REQUIRED
                 >
                 <!ELEMENT tmxmessages (message+)>


    --%>
    <%!
        public boolean accept(String[] prefixes, String key)
        {
            for(int i = 0; i < prefixes.length; i++)
            {
                if(key.startsWith(prefixes[i]))
                    return true;
            }
            return false;
        }
    %>
    <%
        String jspstart = "<" + "%";
        String jspend = "%" + ">";
        String locale = request.getParameter("sys_lang");
        if(locale == null) {
          locale= PSRoleUtilities.getUserCurrentLocale();
          String lang="en";
          if(locale==null){
              locale="en-us";
          }else{
              if(locale.contains("-"))
                  lang=locale.split("-")[0];
              else
                  lang=locale;
          }
        }
        String prefix = request.getParameter("prefix");
        String mode = request.getParameter("mode");
        if(prefix == null || prefix.length() == 0)
            prefix = "javascript.";
        if(mode == null || mode.length() == 0)
            mode = "js";
        String[] prefixes = prefix.split(",");
        PSTmxResourceBundle tmxBundle = PSTmxResourceBundle.getInstance();
        Iterator keys = tmxBundle.getKeys(locale);
        Map accepted = new HashMap();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            if(!prefix.equals("*") && !accept(prefixes, key))
                continue;
            String val = tmxBundle.getString(key, locale).replaceAll("\"", "\\\"");
            val = val.replaceAll("\n", "\\\\n").replaceAll("\t", "");
            accepted.put(key, val);
        }

        if(mode.equals("js"))
        {
            response.setContentType("text/javascript");
        // START JAVASCRIPT MODE
    %>
    if (!this.I18N) {
        this.I18N = {};
    }
    var __tmxMessageMap = {
        <%

            Iterator keyset = accepted.keySet().iterator();
            while(keyset.hasNext())
            {
                String key = (String)keyset.next();

        %>"<%= key %>": "<%= (String)accepted.get(key) %>"<%if(keyset.hasNext()) out.print(",");%>
        <%}%>
    };

    function psxGetLocalMessage(key, args) {
        var localemsg = "";
        //Check whether msg exists or not.
        if (key) {
            //Look for the message for keyword msg in user locale
            localemsg = ___psxReplaceMsgTokens(__tmxMessageMap[key], args);
            if (!localemsg) //not found in default map too, return key itself.
                localemsg = ___psxTrimKey(key);
        }
        else {
            //If key it self does not exist, then return error message.
            localemsg = "Key is missing";
        }
        return localemsg;
    }

    I18N.message = psxGetLocalMessage;

    function ___psxReplaceMsgTokens(msg, args) {
        if (!msg || args == null || args == undefined || args.length == 0)
            return msg;
        var token = "";
        for (i = 0; i < args.length; i++) {
            token = new RegExp("\\{" + i + "\\}", "g");
            //args variable should be of array type.
            // If args is string instead of char at ith index entire string should be replaced in place of token
            if(typeof args === "string"){
                msg = msg.replace(token, args);
            }else{
                msg = msg.replace(token, args[i]);
            }
        }
        return msg;
    }

    function ___psxTrimKey(key) {
        var atsign = key.indexOf('@');
        var isDebug = window.location.href.indexOf("debug=true") != -1;
        if (!isDebug && atsign >= 0 && (key.length - atsign) > 1) {
            return key.substring(atsign + 1);
        }
        else {
            // Key is broken
            return key;
        }
    }

    <%
        // END JAVASCRIPT MODE
        }
        else if(mode.equals("json"))
        {
            response.setContentType("application/json");
            Iterator keyset = accepted.keySet().iterator();
            out.print("{\"tmxmessages\": {");
            while(keyset.hasNext())
            {
                String key = (String)keyset.next();
                out.print("\"");
                out.print(key);
                out.print("\": \"");
                out.print((String)accepted.get(key));
                out.print("\"");
                if(keyset.hasNext())
                    out.print(",");
            }
            out.print("}}");
        }
        else if(mode.equals("xml"))
        {
            response.setContentType("text/xml");
            Iterator keyset = accepted.keySet().iterator();
            out.println("<tmxmessages>");
            while(keyset.hasNext())
            {
                String key = (String)keyset.next();
                out.print("<message key=\"");
                out.print(key
                    .replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;"));
                out.print("\">");
                out.print(((String)accepted.get(key))
                    .replaceAll("&", "&amp;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("\"", "&quot;"));
                out.println("</message>");
            }
            out.println("</tmxmessages>");
        }
    %>
