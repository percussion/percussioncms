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

var PercServiceUtils = percJQuery.PercServiceUtils;
var currentReportType;
var currentReportEndpoint;

const percReportsList = [
    {type: 'All Files', endpoint: 'all-files'},
    {type: 'All Images', endpoint: 'all-images'},
    {type: 'Non-ADA Compliant Files', endpoint: 'non-ada-compliant-files'},
    {type: 'Non-ADA Compliant Images', endpoint: 'non-ada-compliant-images'}
];

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
    //It opens dialog if email is empty
    percJQuery.perc_ChangeUserEmailDialog.openDialogIfEmptyEmail();
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
    PercServiceUtils.makeRequest(path, 'GET', false, requestReportCallback);
}

function requestReportCallback(status, result) {
    if (status == 'error') {
        showReportAlert(`There was an problem creating the report '${currentReportType}'`);
    }
    else {
        showReportAlert(`Create report request generated Successfully'${currentReportType}'`);
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
    });
}

function createFileDate() {
    var newDate = new Date();
    return (new Date()).toISOString().split('T')[0]+'-'+newDate.getTime();
}
