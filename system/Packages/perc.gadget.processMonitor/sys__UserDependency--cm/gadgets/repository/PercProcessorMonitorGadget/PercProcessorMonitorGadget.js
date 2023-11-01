/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function($)
{
    /**
     * Renders the process monitor status. This is a common code for both gadget and popup dialog.
     */
    $.renderProcessMonitor = function(percJQuery, isGadget){
        $("head").append(" <link href=\"/cm/jslib/profiles/3x/libraries/fontawesome/css/all.css\" rel=\"stylesheet\" type=\"text/css\" />").append("<link href=\"/cm/gadgets/repository/PercProcessorMonitorGadget/PercProcessorMonitorGadget.css\" rel=\"stylesheet\" type=\"text/css\" />");
        $("body").empty();
        var initialHtml = "<div id=\"perc-process-monitor-gadget\">" +
            "<div id=\"perc-process-monitor-actions\"><span id=\"perc-process-monitor-status-refresh\" tabindex=\"0\" role=\"button\" title=\"" + I18N.message("perc.ui.gadgets.processmonitor@Refresh Background Process") + "\">&nbsp;</span><span id=\"perc-process-monitor-popout\"><i " + "class=\"icon-external-link\"></i></span></div>" +
            "<div id=\"perc-process-monitor-table-container\">" +
            "<table id=\"perc-process-monitor-table\" width=\"100%\">" +
            "<tr for=\"header\">" +
            "<th scope=\"col\" align=\"left\" width=\"25%\">" + I18N.message("perc.ui.gadgets.processmonitor@Process") + "</th>" +
            "<th scope=\"col\" align=\"left\" width=\"75%\">" + I18N.message("perc.ui.gadgets.processmonitor@Status") + "</th>" +
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
        })
        .on("keydown",function(event) {
            if(event.code == "Enter" || event.code == "Space"){
                document.activeElement.click();
            }
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
                        "<td scope=\"row\" for=\"error\" colspan=\"2\">" + I18N.message("perc.ui.gadgets.processmonitor@Error Fetching Processors") + " " + additonalMsg + "</td>" +
                        "</tr>";
                    $("#perc-process-monitor-table").append(statusRows);
                    return;
                }
                var rowTempl = "<tr for=\"data\">" +
                    "<td scope=\"row\" for=\"@@NAME@@\">@@NAME@@</td>" +
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
