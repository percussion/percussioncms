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

(function($) {

    $.PercPublishStatusMinuetView = function () {

        return {
            renderPublishStatusSection: renderPublishStatusSection
        };

        function renderPublishStatusSection() {

            renderPublishStatusContainer();
            renderPublishStatusList('refresh', false);
            var refreshPublishStatusInterval = setInterval(function(){
                // clear interval if there is no selected site
                if(!(selectedSiteData.siteId)) {
                    clearInterval(refreshPublishStatusInterval);
                    return true;
                }
                renderPublishStatusList('refresh', false);
            }, 5000);

        }

        function getPublishStatus() {
            $.PercPublisherService(false).getJobStatus(selectedSiteData.siteId, getPublishStatusCallback);
        }

        function getPublishStatusCallback(status, result) {
            if(status === true && result[1] === 'success') {
                sitePublishStatusDeferred.resolve(result[0]);
            }
        }

        function renderPublishStatusContainer() {
            currentJobs = [];
            emptyObject = {};
            processTemplate(emptyObject, 'templatePercPublishStatus', 'percPublishStatusTarget');
            bindStatusEvents();
        }

        function renderPublishStatusList(action, toggleFlag, clickEvent) {
            if(action === 'refresh') {
                activeSort = $('#percPublishStatusList .perc-active-sort');
                eventObj = activeSort;
            }
            if(action === 'click') {
                eventObj = clickEvent;
            }

            sitePublishStatusDeferred = $.Deferred();
            getPublishStatus();
            sitePublishStatusDeferred.done(function(result) {
                publishStatusObject = result;
                previousJobs = currentJobs;
                currentJobs = [];

                // Make an array of the job ids so we can track when jobs are added or removed
                $(publishStatusObject.SitePublishJob).each(function() {
                    currentJobs.push(this.jobId);
                });

                // If the jobIds have changed, refresh the logs and apply animations to status
                // otherwise, just update the status without animation

                if(JSON.stringify(currentJobs) != JSON.stringify(previousJobs)) {

                    $('#percPublishStatusListTarget').animateCss('fadeOut', function() {
                        $.PercPublishLogsMinuetView(false).sortLogField(eventObj, publishStatusObject, 'templatePercPublishStatusList', 'percPublishStatusListTarget', 'SitePublishJob', toggleFlag);
                        $('#percPublishStatusListTarget').animateCss('fadeIn');
                    });

                    setTimeout(function() {
                        $.PercPublishLogsMinuetView(false).refreshSiteLogs();
                    }, 1000);

                }
                else {
                    $.PercPublishLogsMinuetView(false).sortLogField(eventObj, publishStatusObject, 'templatePercPublishStatusList', 'percPublishStatusListTarget', 'SitePublishJob', toggleFlag);
                }

                bindStatusActions();
            });

        }

        function stopPublishingJob(eventObj) {
            startProcessRunningAlert();
            jobId = $(eventObj).data('perc-job-id');
            stopPublishingJobDeferred = $.Deferred();
            $.PercPublisherService(false).stopPubJob(jobId, stopPublishingJobCallback);
            stopPublishingJobDeferred.done(function(response) {
                processAlert(response);
            });
        }

        function stopPublishingJobCallback(status, result) {
            stopProcessRunningAlert();
            $.when(processRunningAnimationDeferred).then(function() {
                response = {};
                response.result = {};
                response.source = I18N.message("perc.ui.publish.title@Stop Publishing Job");
                if(result[1] == 'success') {
                    response.result.warning = false;
                    response.result.status = I18N.message("perc.ui.publish.title@Successfully Stopped Publishing Job");

                }
                else{
                    responseText = JSON.parse(result[0].responseText);
                    response.result.warning = findVal(responseText, 'defaultMessage');
                }
                stopPublishingJobDeferred.resolve(response);
            });
        }

        function bindStatusEvents() {
            $('#percPublishStatusList .perc-sortable-header').on('click keypress', function(event) {
                if(event.type === 'click' || event.which === 13) {
                    renderPublishStatusList('click', true, this);
                }
            });
        }

        function bindStatusActions() {
            $('.perc-stop-job-button').on("click", function(evt) {
                stopPublishingJob(this);
            });
        }

    };

    Handlebars.registerHelper('publishProgress', function(completedItems, totalItems) {
        progress =  {};
        if(completedItems === 0 && totalItems === 0) {
            progress = 100;
        }
        else {
            progress = Math.round((completedItems/totalItems)*100);
        }

        return progress;

    });

})($);
