/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($)
{
    /**
     * Renders the process monitor status. This is a common code for both gadget and popup dialog.
     */
    $.renderProcessMonitor = function(percJQuery, isGadget){
        $("head").append(" <link href=\"/cm/css/fontawesome/css/all.css\" rel=\"stylesheet\" type=\"text/css\" />").append("<link href=\"/cm/gadgets/repository/PercProcessorMonitorGadget/PercProcessorMonitorGadget.css\" rel=\"stylesheet\" type=\"text/css\" />");
        $("body").empty();
        var initialHtml = "<div id=\"perc-process-monitor-gadget\">" +
            "<div id=\"perc-process-monitor-actions\"><span id=\"perc-process-monitor-status-refresh\" title=\"" + I18N.message("perc.ui.gadgets.processmonitor@Refresh Background Process") + "\">&nbsp;</span><span id=\"perc-process-monitor-popout\"><i " + "class=\"icon-external-link\"></i></span></div>" +
            "<div id=\"perc-process-monitor-table-container\">" +
            "<table id=\"perc-process-monitor-table\" width=\"100%\">" +
            "<tr for=\"header\">" +
            "<th align=\"left\" width=\"25%\">" + I18N.message("perc.ui.gadgets.processmonitor@Process") + "</th>" +
            "<th align=\"left\" width=\"75%\">" + I18N.message("perc.ui.gadgets.processmonitor@Status") + "</th>" +
            "</tr>" +
            "</table>" +
            "</div>" +
            "<div id=\"perc-pm-refresh-status\"><b>" + I18N.message("perc.ui.gadgets.processmonitor@Last Refreshed") + ": </b><span id=\"perc-pm-refresh-time\"></span></div>" +
            "</div>";
        $("body").append(initialHtml);

        //If it is gadget add the event to the popout icon, if not hide the popout icon
        if(isGadget){
            $("#perc-process-monitor-popout").on("click",function(){
                var winwidth = 800, winheight=400;
                var leftpos = (screen.width - winwidth) / 2;
                var toppos = (screen.height - winheight) / 3;
                var PercProcessMonitorWindow = window.open("/cm/app/?view=popup&popuppage=PercProcessMonitor.jsp","PercProcessMonitor","toolbar=no, scrollbars=no, resizable=yes, top=" + toppos + ", left=" + leftpos + ", width=" + winwidth + ", height=" + winheight);
                PercProcessMonitorWindow.focus();
            });
        }
        else{
            $("#perc-process-monitor-popout").hide();
        }

        //Disable refresh button as we will be loading the data
        $("#perc-process-monitor-status-refresh").addClass("perc-disabled").on("click",function(){
            renderStatusRows();
        });

        //Initial call to render the status rows
        renderStatusRows();
        var processMonitorTimer;
        /**
         * Makes a call to server and renders the returned processor monitor data, if there is an error displays the error.
         */
        function renderStatusRows(){
            //disable the refresh button
            $("#perc-process-monitor-status-refresh").addClass("perc-disabled");
            $("#perc-process-monitor-table").find("tr[for=data]").remove();
            var serviceUtils = percJQuery.PercServiceUtils;
            var percUtils = percJQuery.perc_utils;
            serviceUtils.makeJsonRequest(percJQuery.perc_paths.PROCESS_STATUS_ALL, "GET", false, function(status, results){

                clearTimeout(processMonitorTimer);
                processMonitorTimer = setTimeout(renderStatusRows,30000);
                var statusRows = "";
                if(status != serviceUtils.STATUS_SUCCESS || !results.data){
                    var additonalMsg = "";
                    if(!isGadget){
                        additonalMsg = I18N.message("perc.ui.gadgets.processmonitor@Please Close Window");
                    }
                    statusRows = "<tr for=\"data\">" +
                        "<td for=\"error\" colspan=\"2\">" + I18N.message("perc.ui.gadgets.processmonitor@Error Fetching Processors") + " " + additonalMsg + "</td>" +
                        "</tr>";
                    $("#perc-process-monitor-table").append(statusRows);
                    return;
                }
                var rowTempl = "<tr for=\"data\">" +
                    "<td for=\"@@NAME@@\">@@NAME@@</td>" +
                    "<td>@@STATUS@@</td>" +
                    "</tr>";
                $.each(results.data.psMonitorList.monitor, function(){
                    var entries = this.stats.entries.entry;
                    var name = "";
                    var message = "";
                    $.each(entries, function(){
                        if(this.key === "name")
                            name =  I18N.message("perc.ui.gadgets.processmonitor@"+this.value);
                        if(this.key === "message")
                            message = I18N.message("perc.ui.gadgets.processmonitor@"+this.value);
                    });
                    statusRows += rowTempl.replace(/@@NAME@@/g, name).replace("@@STATUS@@",message);
                });
                $("#perc-process-monitor-table").append(statusRows);
                //Enable the refresh button
                $("#perc-process-monitor-status-refresh").removeClass("perc-disabled");
                $("#perc-pm-refresh-time").text($.datepicker.formatDate("M d, yy", new Date()) + " " + percUtils.formatTimeFromDate(new Date(), true));
                //If it is gadget remove the loading message
                if(isGadget){
                    miniMsg.dismissMessage(loadingMsg);
                }
            });
        }

    }
})(jQuery);
