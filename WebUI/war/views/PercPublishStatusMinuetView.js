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
