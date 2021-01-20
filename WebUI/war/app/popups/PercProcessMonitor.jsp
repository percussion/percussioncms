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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
 <head>
    <title>Process Monitor</title>
    <link rel="stylesheet" type="text/css" href="/cm/cssMin/perc_dashboard.packed.css"/>
    <script src="/Rhythmyx/tmx/tmx.jsp?mode=js&amp;prefix=perc.ui.&amp;"></script>
    <script src="/cm/jslibMin/perc_dashboard.packed.js"></script>
    <script src="/cm/gadgets/repository/PercProcessorMonitorGadget/PercProcessorMonitorGadget.js"  ></script>
    <script>
        jQuery(document).ready(function(){
            jQuery.renderProcessMonitor(jQuery,false);
        });
    </script>
    <style>
        body{
            margin:15px;
        }
        div#perc-process-monitor-actions
        {
            width:66px;
        }
    </style>
 </head>

 <body>

 </body>
</html>
