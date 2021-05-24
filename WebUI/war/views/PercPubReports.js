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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/**
 * Helper functions to render the reports for given site
 */
(function($)
{
    var pubStatusId = 0;
    var prevStatusCount = -1;
    this.service = $j.PercPublisherService(false);
    this.utilService = $j.PercUtilService;
    var dirtyController = $.PercDirtyController;
    siteName = $j.PercNavigationManager.getSiteName();
    // Custom column sorting for Publishing Log Details Elapsed Time column
    $j.fn.dataTableExt.afnSortData['dom-elapsed-time'] = function(oSettings, iColumn)
    {
        var aData = [];
        $('td:eq(' + iColumn + ')', oSettings.oApi._fnGetTrNodes(oSettings)).each(function()
        {
            var data = $(this).text();
            var dataNum = new Number(data.substring(0, data.indexOf('s')));
            aData.push(dataNum);
        });
        return aData;
    }
    //Convert milliseconds to HH:MM:SS form
    milliSecondsToHHMMSS = function(t)
    {
        t = t / 1000;
        var h = Math.floor(t / 3600);
        t %= 3600;
        var m = Math.floor(t / 60);
        var s = Math.floor(t % 60);
        h = h > 0 ? h : 0;
        m = m > 0 ? m : 0;
        s = s > 0 ? s : 0;

        /* Converts the supplied number to a string with leading 0 if less than 10.

         @param val assumed to be a number
         */
        function _formatUnit(val)
        {
            return (val <= 9 ? "0" : "") + val;
        }
        return _formatUnit(h) + ":" + _formatUnit(m) + ":" + _formatUnit(s) + " s";
    };

    //Logic to toggle (disable/enable) Delete button
    onDeleteToggle = function()
    {
        var disable = true;
        $j("#perc-publish-logs .perc-log-delete").each(function(i)
        {
            if ($j(this).is(":checked"))
            {
                disable = false;
                return true;
            }
        });

        if ((!disable && !$j("#perc-publish-log-delete").hasClass("perc-disabled")) ||
            (disable && $j("#perc-publish-log-delete").hasClass("perc-disabled")))
        {
            //Delete button has already been enabled or disabled, nothing to do
            return;
        }

        if (disable)
        {
            disableDelete();
        }
        else
        {
            enableDelete();
        }
    };
    //Helper function to disable delete button
    disableDelete = function()
    {
        $j("#perc-publish-log-delete").addClass("perc-disabled");
        $j("#perc-publish-log-delete").off('click');
    };

    //Helper function to enable delete button
    enableDelete = function()
    {
        $j("#perc-publish-log-delete").removeClass("perc-disabled");
        $j("#perc-publish-log-delete").on("click", function(evt)
        {
            purgeLogs();
        });
    };

    //Helper function to take care of the un/fold action
    FoldToggle = function(self, onFold)
    {
        if ($j(self).hasClass("perc-opened"))
        {
            $j(self).removeClass("perc-opened");
            $j(self).parent().find(".perc-container").removeClass("perc-visible").addClass("perc-hidden");
        }
        else
        {
            if (onFold)
            {
                onFold();
            }
            $j(self).addClass("perc-opened");
            $j(self).parent().find(".perc-container").removeClass("perc-hidden").addClass("perc-visible");
        }
    };

    /* Publish the selected site
     1) Makes the ajax call
     2) Shows the message indicating success/error
     3) If successful, opens the current logs widget
     */
    publishCurrentSite = function()
    {
        var siteVal = siteName;
        if (siteVal === "base")
        {
            $.perc_utils.alert_dialog({
                content: I18N.message("perc.ui.perc.pub.reports@Invalid Site"),
                title: I18N.message("perc.ui.perc.pub.reports@Site Publish")
            });
            $j("perc-manual-publish-widget").find("perc-foldable").on("click", FoldToggle(this));
            return true;
        }
        this.service = $j.PercPublisherService(false);
        this.service.publishSite(siteVal, function(status, result)
        {
            if (result[1] === "success")
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Site") + siteVal + I18N.message("perc.ui.perc.pub.reports@Started Publishing"),
                    title: I18N.message("perc.ui.perc.pub.reports@Site Publish"),
                    id: "perc-publish-success"
                });
                //Getting information on the jobs and opening up that widget
                var foldable = $j("#perc-publish-jobs-widget").find(".perc-foldable");
                if (!foldable.hasClass("perc-opened"))
                {
                    foldable.trigger("click");
                }
                else
                {
                    //Start refreshing the status data
                    startPublishCurrentStatus();
                }
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Publish") + siteVal,
                    title: I18N.message("perc.ui.perc.pub.reports@Site Publish"),
                    id: "perc-publish-failure"
                });
            }
        });
    };
    //Gets current status of all publishing jobs
    publishCurrentStatus = function()
    {
        var siteId = $("#perc-site-id").val();
        this.service.getJobStatus(siteId, function(status, result)
        {
            if (result[1] == "success")
            {
                var result0 = eval(result[0]);
                renderLogs(result0.SitePublishJob);
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Retrieve Job Status"),
                    title: I18N.message("perc.ui.perc.pub.reports@Current Status Of Jobs")
                });
            }
        });

        function renderLogs(results)
        {
            var logString = "<table class='perc-table-statusofcurrentjobs' style='width:100%;'><thead><tr>" +
                "<th style='text-align:left;' class='perc-header-sitename'>" +
                I18N.message("perc.ui.publish.title@SiteName") +
                "</th>" +
                "<th style='text-align:left;' class='perc-header-status'>" +
                I18N.message("perc.ui.publish.title@Status") +
                "</th>" +
                "<th style='text-align:left;' class='perc-header-time'>" +
                I18N.message("perc.ui.publish.title@Time") +
                "</th>" +
                "<th style='text-align:left;' class='perc-header-elapsedtime'>" +
                I18N.message("perc.ui.publish.title@Elapsed Time") +
                "</th>" +
                "<th style='text-align:left;' class='perc-header-completed' style='background-position:left center;'>" +
                '%' + I18N.message("perc.ui.publish.title@Percent Completed") +
                "</th>" +
                "<th style='text-align:left; width: 60px' class='perc-header-elapsedtime'>" +
                I18N.message("perc.ui.publish.title@Action") +
                "</th>" +
                "</tr></thead><tbody>";
            var resLength = results.length;
            if (resLength < prevStatusCount)
            {
                publishLogs();
            }
            prevStatusCount = resLength;
            if (resLength == 0)
            {
                logString += "<tr><td>No jobs currently running</td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td style='width:60px;' class = 'perc-ellipsis'></td>" +
                    "</tr>";
                stopPublishCurrentStatus();
            }
            else
            {
                for (r in results)
                {
                    var queued = 0;
                    var totalItems = results[r].totalItems;
                    if (totalItems > 0)
                    {
                        queued = (results[r].completedItems / totalItems * 100).toFixed(0);

                    }
                    if(results[r].isStopping)
                        var $stopPubButton=$("<div><div class = 'perc-stop-button perc-stop-inactive' id='"+results[r].jobId+"'></div></div>");
                    else
                        var $stopPubButton=$("<div><div class = 'perc-stop-button perc-stop-active' id='"+results[r].jobId+"'></div></div>");

                    logString += "<tr>" + "<td><b>" + results[r].siteName + "</b></td>" +
                        "<td class = 'perc-ellipsis'>" +
                        results[r].status +
                        "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        results[r].startTime +
                        "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        milliSecondsToHHMMSS(results[r].elapsedTime) +
                        "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        queued +
                        "</td>" +
                        "<td width = '60px' class = 'perc-ellipsis'>" +
                        $stopPubButton.html() +
                        "</td>" +
                        "</tr>";
                }
            }
            logString += "</tbody></table>";
            $j("#perc-publish-current-jobs").html(logString);
            $j("#perc-publish-current-jobs").find("table").dataTable({
                "bFilter": false,
                "bInfo": false,
                "bPaginate": false,
                "bAutoWidth": false,
                "aoColumns": [{
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "date",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "date",
                    "sSortDataType": "dom-text"
                }, {
                    "fnRender": function(oObj)
                    {
                        if (oObj.aData[4] > 0)
                        {
                            var barWdith = oObj.aData[4];
                            return "<div class = 'perc-que-bar' style = 'width:" + barWdith + "%'>" + oObj.aData[4] + "% Completed</div>";
                        }
                        else
                        {
                            var barWdith = 0;
                            return "<div style = 'width:" + barWdith + "%'>" + oObj.aData[4] + "</div>";
                        }

                    },
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }],
                fnDrawCallback: function()
                {

                }
            });
        }
    };

    //Gets currently publish logs
    //Makes POST call with {maxcount, days} parameters
    publishLogs = function()
    {
        var self = this;
        var serverId = $j("#perc-servers").val();
        var days = $j("#perc-view-last").val();
        var maxCount = $j("#perc-show").val();
        var siteId = $("#perc-site-id").val();
        var pubObject = {
            SitePublishLogRequest: {
                siteId: siteId,
                maxcount: maxCount,
                days: days,
                pubServerId: serverId
            }
        };
        this.service.getPublishingLogs(pubObject, function(status, result)
        {
            if (status)
            {
                renderLogs(result[0].SitePublishJob);
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Retrieve Logs"),
                    title: I18N.message("perc.ui.perc.pub.reports@Logs Of Published Jobs")
                });
            }
        });
        function renderLogs(results)
        {
            var logString = "<table class='perc-table-publishinglog' style='width:100%;'><thead><tr>" +
                "<th style='text-align: left;' class='perc-header-status'>" +
                I18N.message("perc.ui.publish.title@Status") +
                "</th>" +
                "<th style='text-align: left;' class='perc-header-date'>" +
                I18N.message("perc.ui.publish.title@Date") +
                "</th>" +
                "<th style='text-align: left;' class='perc-header-servername'>" +
                I18N.message("perc.ui.publish.title@Server") +
                "</th>" +
                "<th style='text-align: left;' class='perc-header-elapsedtime'>" +
                I18N.message("perc.ui.publish.title@Elapsed Time") +
                "</th>" +
                "<th style='text-align: left;' class='perc-header-published'>" +
                I18N.message("perc.ui.publish.title@Published") +
                "</th>" +
                "<th style='text-align: left;' class='perc-header-actions'>" +
                I18N.message("perc.ui.publish.title@Actions") +
                "</th>" +
                "<th style='text-align: right;' class='perc-header-todelete'>" +
                I18N.message("perc.ui.publish.title@To Delete") +
                "</th>" +
                "</tr></thead><tbody>";
            if (results.length == 0)
            {
                logString += "<tr><td>No logs currently exist</td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td class = 'perc-ellipsis'></td>" +
                    "<td style='text-align: right;' class = 'perc-ellipsis'></td>" +
                    "</tr>";
            }
            else
            {
                for (r in results)
                {
                    logString += "<tr>" + "<td class = 'perc-ellipsis'>" + results[r].status + "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        results[r].startDate +
                        " " +
                        results[r].startTime +
                        "</td>" +
                        "<td class = 'perc-ellipsis'><b>" +
                        results[r].pubServerName +
                        "</b></td>" +
                        "<td class = 'perc-ellipsis'>" +
                        milliSecondsToHHMMSS(results[r].elapsedTime) +
                        "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        results[r].completedItems +
                        "</td>" +
                        "<td class = 'perc-ellipsis'>" +
                        "<a id='perc-view-detail-" +
                        r +
                        "' class='perc-view-detail' href='#'>View Details</a></td>" +
                        "<td style='text-align:right;' class = 'perc-ellipsis'>" +
                        "<input class='perc-log-delete' type='checkbox' value='" +
                        results[r].jobId +
                        "' onchange='onDeleteToggle();'></input></td>";
                    $j("#perc-publish-logs").append(logString);
                }
            }
            logString += "</tbody></table>";
            $j("#perc-publish-logs").html(logString);

            /************* Attaching onclick events to all `View details' links *************************/

            /* This constant is the key name of a storage location used by the
             _handleViewDetails function. */
            var _ROW_DATA_NAME = "rowData";

            /** Calls the appropriate function when the 'View Details' link is clicked.
             Expects the data for a single row in the summary table to be assigned
             with the _ROW_DATA_NAME key in the object that calls this method.
             */
            function _handleViewDetails()
            {
                var rowData = $(this).data(_ROW_DATA_NAME);
                getLogDetails(rowData.jobId, rowData);
            }

            for (r in results)
            {

                var node = $j("#perc-view-detail-" + r);
                node.data(_ROW_DATA_NAME, results[r]);
                node.on("click",_handleViewDetails);
            }

            /************* Invoking dataTable plugin to make the columns sortable ****************************/
            $j("#perc-publish-logs").find("table").dataTable({
                "bFilter": false,
                "bInfo": false,
                "aaSorting": [[1, "desc"]],
                "bPaginate": false,
                "bAutoWidth": false,
                "aoColumns": [{
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "date",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "numeric",
                    "sSortDataType": "dom-text"
                }, {
                    "bSortable": false
                }, {
                    "bSortable": false
                }],
                fnDrawCallback: function()
                {

                }
            });
        }
    };
    //Purges selected jobids 1) Makes the Ajax call 2) Shows the confirmation dialog after call
    //Makes POST call to delete logs
    purgeLogs = function()
    {
        var selectedJobList = new Array();
        $j.each($("#perc-publish-logs input:checked"), function()
        {
            //Populating selected jobids
            selectedJobList.push($(this).val());
        });
        var title = I18N.message("perc.ui.perc.pub.reports@Delete Logs");
        var options = {
            title: title,
            question: I18N.message("perc.ui.perc.pub.reports@Are You Sure Delete Logs"),
            cancel: function()
            {
            },
            success: function()
            {
                //Call the service to delete the selected jobs
                this.service = $j.PercPublisherService(false);
                this.service.purgeJob(selectedJobList, function(status, result)
                {
                    if (status)
                    {
                        disableDelete();
                    }
                    else
                    {
                        $.perc_utils.alert_dialog({
                            content: I18N.message("perc.ui.perc.pub.reports@Unable To Delete Logs"),
                            title: title
                        });
                    }
                    //Simulating consecutive click events on publish logs (to refresh)
                    $j("#perc-publish-logs-widget").find(".perc-foldable").trigger("click").trigger("click");
                });
            }
        };
        $.perc_utils.confirm_dialog(options);
    };
    //Get detailed log info 1) Makes the Ajax call 2) Attaches callback that populates logs.
    //Makes GET call with jobid
    getLogDetails = function(val, publishRecord)
    {
        this.service.getPublishingLogDetails(val, function(status, result)
        {
            if (status)
            {
                renderLogs(result[0].SitePublishItem, publishRecord);
                $j.fn.perc_toggle("#perc-publish-log-details-widget");
                $j.fn.perc_toggle("#perc-publish-logs-widget");
                if ($j("#perc-publish-log-details-widget").find(".perc-opened").length === 0)
                {
                    $j("#perc-publish-log-details-widget").find(".perc-foldable").trigger("click");
                }
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Get Log Details")
                });
            }
        });
        function renderLogs(results, publishRecord)
        {
            /************************* Creating the first table with all the stats **************************/
            var logString = "<table class='perc-publish-detail-first-table' width='50%'>" + "<tr>" +
                "<td>Site Name:</td>" +
                "<td>" +
                publishRecord.siteName +
                "</td>" +
                "<td>Published:</td>" +
                "<td>" +
                publishRecord.completedItems +
                "</td>" +
                "</tr><tr>" +
                "<td>Date:</td>" +
                "<td>" +
                publishRecord.startDate +
                "</td>" +
                "<td>Removed:</td>" +
                "<td>" +
                publishRecord.removedItems +
                "</td>" +
                "</tr><tr>" +
                "<td>Job ID:</td>" +
                "<td>" +
                publishRecord.jobId +
                "</td>" +
                "<td>Elapsed Time:</td>" +
                "<td>" +
                milliSecondsToHHMMSS(publishRecord.elapsedTime) +
                "</td>" +
                "<td></td>" +
                "<td>" +
                "</td>" +
                "<td></td>" +
                "<td>" +
                "</td>" +
                "<td></td>" +
                "<td>" +
                "</td>" +
                "</tr>" +
                "</table>";
            logString += "<BR/><H2>Publishing Logs:</H2>";
            /************************* Creating the second table with specific log entries **************************/
            logString += "<table class='perc-identifier-next' style='width:100%;'><thead><tr style='text-align:left;'>" +
                "<th class='perc-header-status' style=\"width: 12%; text-align: left;'\">" +
                I18N.message("perc.ui.publish.title@Status") +
                "</th>" +
                "<th class='perc-header-filenameandlocation' style=\"width: 58%; text-align: left;'\">" +
                I18N.message("perc.ui.publish.title@File Name and Location") +
                "</th>" +
                "<th class='perc-header-elapsedtime' style=\"width: 12%; text-align: left;'\">" +
                I18N.message("perc.ui.publish.title@Elapsed Time") +
                "</th>" +
                "<th class='perc-header-error' style=\"width: 18%\">" +
                I18N.message("perc.ui.publish.title@Error") +
                "</th>" +
                "</tr></thead><tbody>";
            for (r in results)
            {
                var errMsg = results[r].errorMessage;
                if (errMsg === undefined || errMsg == null)
                {
                    errMsg = "";
                }
                logString += "<tr>" + "<td>" + results[r].status + "</td>" +
                    "<td title='" + results[r].fileLocation + "' class='perc-datatable-columnrow perc-ellipsis'>" +
                    results[r].fileLocation +
                    "</td>" +
                    "<td>" +
                    results[r].elapsedTime / 1000 +
                    " s</td>" +
                    "<td>" +
                    errMsg +
                    "</td>" +
                    "</tr>";
            }
            logString += "</tbody></table>";

            /* using dataTable plugin to be able to sort and manipulate data more */
            $j("#perc-publish-logs-details").html(logString);
            $j("#perc-publish-logs-details").find(".perc-identifier-next").dataTable({
                "bFilter": false,
                "bInfo": false,
                "bPaginate": false,
                "bAutoWidth": false,
                "aoColumns": [{
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }, {
                    "sType": "numeric",
                    "sSortDataType": "dom-elapsed-time"
                }, {
                    "sType": "string",
                    "sSortDataType": "dom-text"
                }],
                fnDrawCallback: function()
                {

                }
            });
        }
    };

    //Starts updating the current publishing status at intervals of 500ms.
    startPublishCurrentStatus = function()
    {
        pubStatusId = setInterval(publishCurrentStatus, 500);
    };

    //Stops updating the current publishing status.
    stopPublishCurrentStatus = function()
    {
        if (pubStatusId > 0)
        {
            clearInterval(pubStatusId);
            pubStatusId = 0;
        }
    };

    /**
     * Get servers list for a given site
     * @param siteName : Name fo the site
     * @param callback: callback function
     */
    function getServersList(siteName)
    {

        $("#perc-servers").html('');
        service.getServersList(siteName, function(status, result)
        {
            var serversListContainer = $("#perc-servers");
            $("#perc-servers").append($('<option></option>').val(' ').html('All'));
            if (status)
            {
                var serverProperties = JSON.parse(result[0]);
                var servers = [];
                if (!Array.isArray(serverProperties.serverInfo))
                {
                    servers.push(serverProperties.serverInfo);
                    servers = serverProperties.serverInfo;
                }
                else
                {
                    servers = serverProperties.serverInfo;
                }

                $.each(servers, function()
                {
                    //TODO - Do appending outside the loop
                    $("#perc-servers").append($('<option></option>').val(this.serverId).html(this.serverName));
                });
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Get List Of Servers") + siteName,
                    title: I18N.message("perc.ui.publish.title@Error")
                });
            }
        });

    }

    /** Stop the publishng server.
     *
     */
    stopPublishing = function()
    {
        var pubId = $(this).attr('id');
        service.stopPubJob(pubId, function(status, result)
        {
            if (status)
            {
            }
            else
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.pub.reports@Unable To Stop The Publishing Server"),
                    title: I18N.message("perc.ui.publish.title@Error")
                });
            }
        });
    }
    /****************** Binding onclick events with all the widgets and buttons toolbar ************************/
    $j(document).ready(function()
    {

        $j("#perc-publish-jobs-widget").find(".perc-foldable").on("click", function()
        {
            if ($j(this).hasClass("perc-opened"))
            {
                stopPublishCurrentStatus();
            }
            FoldToggle(this, function()
            {
                startPublishCurrentStatus();
            });
        });
        $j("#perc-publish-logs-widget").find(".perc-foldable").on("click", function(evt)
        {
            FoldToggle(this, publishLogs);
            disableDelete();
        });
        $j("#perc-publish-log-details-widget").find(".perc-foldable").on("click", function(evt)
        {
            FoldToggle(this);
        });
        $j("#perc-publish-back").on("click", function(evt)
        {
            $j.fn.perc_toggle("#perc-publish-log-details-widget");
            $j.fn.perc_toggle("#perc-publish-logs-widget");
        });

        $j("#perc-publish-current-jobs").on('click', '.perc-stop-active', stopPublishing);

    });
})(jQuery);
