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

var PercServiceUtils = percJQuery.PercServiceUtils;
var currentReportType;
var currentReportEndpoint;

const percReportsList = [
    {type: 'All Files', endpoint: 'all-files'},
    {type: 'All Images', endpoint: 'all-images'},
    {type: 'Non-ADA Compliant Files', endpoint: 'non-ada-compliant-files'},
    {type: 'Non-ADA Compliant Images', endpoint: 'non-ada-compliant-images'}
]

$(document).ready(function() {
    displayAvailableReports();
    bindReportEvents();
});

function displayAvailableReports() {
    var reportListHtml = '';
    $(percReportsList).each(function(index, report) {
        reportListHtml +=  `<div class="perc-report-row">
                          <div class="perc-generate-report-container"><button class="perc-generate-report btn btn-primary" data-perc-report-type="${report.type}" data-perc-report-endpoint="${report.endpoint}">${report.type}</button></div>
                        </div>`;
    });
    $('#percReportGadgetTarget').html(reportListHtml);
}

function bindReportEvents() {
    $('.perc-generate-report').on('click', function() {
        processReport(this);
    });
}

function processReport(eventObject) {
    currentReportType = $(eventObject).data('perc-report-type');
    currentReportEndpoint = $(eventObject).data('perc-report-endpoint');
    path = constructPath(currentReportEndpoint);
    requestReport(path);
}

function constructPath(endpoint) {
    path = `/rest/assets/reports/${endpoint}`;
    return path;
}

function requestReport(path) {
    PercServiceUtils.makeRequest(path, 'GET', true, requestReportCallback);
}

function requestReportCallback(status, result) {
    if(typeof(result.data) !== 'undefined' && result.data == '') {
        showReportAlert(`The report type '${currentReportType}' is empty`);
    }
    else if (status == 'error') {
        showReportAlert(`There was an problem creating the report '${currentReportType}'`)
    }
    else {
        saveData(result);
        showReportAlert(`Successfully created report '${currentReportType}'`)
    }
}

function saveData(data) {
    fileDate = createFileDate();
    fileName = `percussion-report-${currentReportEndpoint}-${fileDate}.csv`;
    const a = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";

    const blob = new Blob([data.data], {type: "octet/stream"}),
        url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);

}

function showReportAlert(message) {
    $('#percReportAlertTarget').fadeOut('fast', function() {
        $('#percReportAlertTarget').text(message);
        $('#percReportAlertTarget').fadeIn('fast');
    })
}

function createFileDate() {
    var newDate = new Date();
    return (new Date()).toISOString().split('T')[0]+'-'+newDate.getTime();
}