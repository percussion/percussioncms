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

/*  This helper function handles rendering and interaction
*   with publishing logs
*/

(function($) {

    $.PercPublishLogsMinuetView = function () {

        return {
            getSiteLogs: getSiteLogs,
            refreshSiteLogs: refreshSiteLogs,
            renderSiteLogsSection: renderSiteLogsSection,
            renderSiteLogs: renderSiteLogs,
            renderPublishDetails: renderPublishDetails,
            sortLogField: sortLogField
        }

        function renderSiteLogsSection() {
            renderSiteLogsContainer();
            bindLogActions()
            renderSiteLogs();
        }

        function renderSiteLogsContainer() {
            processTemplate(serverListObject, 'templatePercPublishLogs', 'percPublishLogsTarget');
        }

        function renderSiteLogs() {
            logParams = getLogParams();
            siteLogDataDeferred = $.Deferred();
            getSiteLogs(logParams);
            siteLogDataDeferred.done(function(result) {
                serverLogObject = result;
                sortLogList('refresh', false);
                bindLogsEvents();
            });

        }

        function getSiteLogs(logParams) {
            $j.PercPublisherService(false).getPublishingLogs(logParams, getSiteLogsCallback);
        }

        function getSiteLogsCallback(status, result) {
            if(result[1] == 'success') {
                siteLogDataDeferred.resolve(result[0]);
            }
        }

        function deleteSelectedLogsCallback(status, result) {
            stopProcessRunningAlert();
            $.when(processRunningAnimationDeferred).then(function() {
                response = {};
                response.result = {};
                response.source = I18N.message("perc.ui.publish.title@Delete Publishing Logs");
                if(result[1] == 'success') {
                    response.result.status = I18N.message("perc.ui.publish.title@Delete Logs Success");
                    refreshSiteLogs();
                }
                else {
                    response.result.warning = I18N.message("perc.ui.page.dialog@Request Error");
                }
                deleteSelectedLogsResponseDeferred.resolve(response);
            });
        }

        function getPublishingLogDetailsCallback(status, result) {
            publishDetailsDataDeferred.resolve(result[0]);
        }

        function getLogParams() {
            params = {};
            requestObj = {};
            params.SitePublishLogRequest = {};
            $('.perc-report-selector').each(function() {
                propertyName = $(this).prop('name')
                propertyValue = $(this).val();
                requestObj[propertyName] = propertyValue;
            });
            requestObj.siteId = selectedSiteId;
            params.SitePublishLogRequest = requestObj;
            return params;
        }

        function renderPublishDetails(jobObject) {
            publishDetailsDataDeferred = $.Deferred();
            getPublishingLogDetails(jobObject.jobId);
            publishDetailsDataDeferred.done(function(logDetailsObject) {
                jobObject.siteName = selectedSiteData.name;
                jobObject.SitePublishItem = logDetailsObject.SitePublishItem;
                processTemplate(jobObject, 'templatePercPublishLogDetailsOverlay', 'percPublishLogDetailsOverlayTarget');
                processTemplate(jobObject, 'templatePercPublishLogDetailsList', 'percPublishLogDetailsListTarget');
                $('#percPublishLogDetailsOverlayTarget').animateCss('fadeIn faster');
                $('#percPublishLogDetails').modal('_enforceFocus');
                bindLogDetailsEvents();
            });

        }

        function refreshSiteLogs() {
            $('.perc-report-selector').trigger('change');
        }

        function getPublishingLogDetails(jobId) {
            $j.PercPublisherService(false).getPublishingLogDetails(jobId, getPublishingLogDetailsCallback);
        }

        function deleteSelectedLogsRequest() {

            //  Only trigger confirmation dialog if an item is checked
            if ($('.perc-delete-log-item:checked').length <= 0) {
                return true;
            }

            dialogConfirmationResponseDeferred = $.Deferred();
            dialogObject = createDialogObject();
            dialogObject.type = 'branded';
            dialogObject.title = I18N.message("perc.ui.page.dialog@Delete Logs Confirmation Title");
            dialogObject.message = I18N.message("perc.ui.page.dialog@Delete Logs Confirmation");

            processTemplate(dialogObject, 'templateFullScreenDialog', 'percDialogTarget');
            $('.perc-fullscreen-dialog').modal('_enforceFocus');
            $('.perc-fullscreen-dialog').animateCss('fadeIn');

            // Bind response click
            $('.perc-confirmation-button').click(function() {
                confirmationDialogCallback(this);
            });

            dialogConfirmationResponseDeferred.done(function(response) {

                if( response == 'confirm') {
                    deleteSelectedLogsResponseDeferred = $.Deferred();
                    deleteSelectedLogs();
                    deleteSelectedLogsResponseDeferred.done(function(response) {
                        processAlert(response)

                        // Only close the modal when we have a successful reponse
                        if(!(response.result.warning)) {
                            refreshSiteLogs();
                        }
                    });
                }

                hideSection('#percDialogTarget', 'fadeOut');

            });

        }

        function deleteSelectedLogs() {
            startProcessRunningAlert();
            jobIdList = [];
            $('.perc-delete-log-item').each(function() {
                if( this.checked == true) {
                    jobId = $(this).data('perc-job-id');
                    jobIdList.push(jobId);
                }
            });
            $j.PercPublisherService(false).purgeJob(jobIdList, deleteSelectedLogsCallback);
        }

        function bindLogActions() {

            $('#percToggleSelectAllLogs').click(function() {
                toggleAllLogCheckboxes(this);
            });

            $('#percDeleteServerLogs').click(function() {
                deleteSelectedLogsRequest();
            });

            $('.perc-report-selector').change(function() {
                $('#percServerLogListTarget').fadeOut('fast', function() {
                    renderSiteLogs();
                    $('#percServerLogListTarget').fadeIn('fast');
                });
            });

            $('#percSitePublishingLogList .perc-sortable-header').on('click keydown',function(event) {
                if(event.type == 'click' || event.which == 13) {
                    sortLogList('click', true, this);
                }
            });

        }

        function sortLogList(action, toggleFlag, clickEvent) {
            if(action == 'refresh') {
                activeSort = $('#percSitePublishingLogList .perc-active-sort');
                eventObj = activeSort;
            }
            if(action == 'click') {
                eventObj = clickEvent;
            }
            sortLogField(eventObj, serverLogObject, 'templatePercServerLogList', 'percServerLogListTarget', 'SitePublishJob', toggleFlag);
        }

        function bindLogsEvents() {
            $('.perc-log-details').click(function() {
                jobObject = $(this).data('perc-log-object');
                renderPublishDetails(jobObject);
            });
        }

        function bindLogDetailsEvents() {

            $('#percClosePublishItemsDetails').click(function() {
                hideSection('#percPublishLogDetailsOverlayTarget', 'fadeOut faster');
            });

            $('.perc-publish-item-details-button').click(function() {
                itemData = $(this).data('perc-publish-item-data');
                $('#percPublishItemLogDetailsTarget').animateCss('fadeOut faster', function() {
                    processTemplate(itemData, 'templatePercPublishItemLogDetails', 'percPublishItemLogDetailsTarget');
                    $('#percPublishItemLogDetailsTarget').animateCss('fadeIn faster');
                });
            });

            $('.perc-publish-item-filter-field').on('keyup', function() {
                filterString = $(this).val().toLowerCase();
                filterPublishItemsByString(filterString);
            });

            $('#percSitePublishingLogDetails .perc-sortable-header').on('click keydown', function(event) {
                if(event.type == 'click' || event.which == 13) {
                    sortLogField(this, jobObject, 'templatePercPublishLogDetailsList', 'percPublishLogDetailsListTarget', 'SitePublishItem', true);
                }
            });

        }

        function toggleAllLogCheckboxes(eventObj) {
            if (eventObj.checked) {
                $('.perc-delete-log-item').each(function () { //loop through each checkbox
                    $(this).prop('checked', true); //check
                });
            } else {
                $('.perc-delete-log-item').each(function () { //loop through each checkbox
                    $(this).prop('checked', false); //uncheck
                });
            }
        }

        function filterPublishItemsByString(filterItemString) {
            $("#percPublishLogDetailsListTarget tr").filter(function() {
                $(this).toggle($(this).text().toLowerCase().indexOf(filterItemString) > -1)
            });
        }

        function sortLogField(eventObj, dataObject, template, target, sortWithin, sortToggle) {

            // Retrieving target Id ensures we are only affecting the closest
            // table and not any of the other sortable tables
            targetId = $(eventObj).closest('table').attr('id');

            sortByProp = $(eventObj).data('perc-sort-prop');

            // Calculate a timestamp from date and time fields
            tempObject = [];
            $(dataObject[sortWithin]).each(function() {
                this.calculatedStartDate = (new Date(this.startDate + ' ' + this.startTime)).getTime();
                this.calculatedProgress = this.completedItems/this.totalItems;
                tempObject.push(this);
            });

            // Sort toggle prevents switching of sort order on refresh
            if( $(eventObj).data('perc-sort-order') == 'asc') {
                if(sortToggle == true) {
                    $(eventObj).data('perc-sort-order', 'desc');
                    sortAttr = '-' + sortByProp;
                }
                else {
                    sortAttr = sortByProp;
                }
            }
            else {
                if(sortToggle == true) {
                    $(eventObj).data('perc-sort-order', 'asc');
                    sortAttr = sortByProp;
                }
                else {
                    sortAttr = '-' + sortByProp;
                }

            }
            sortedData = dataObject[sortWithin].sort(fieldSorter([sortAttr,'-calculatedStartDate']));
            dataObject[sortWithin] = sortedData;
            $('#'+ targetId +' .perc-sortable-header').not($(eventObj)).removeClass('perc-active-sort').addClass('perc-inactive-sort').data('perc-ort-order', 'desc');
            $(eventObj).addClass('perc-active-sort').removeClass('perc-inactive-sort');
            processTemplate(dataObject, template, target);

            if(target == 'percServerLogListTarget') {
                bindLogsEvents();
            }

            if(target == 'percPublishLogDetailsListTarget') {
                bindLogDetailsEvents();
            }

        }

        function fieldSorter(fields) {
            return function (a, b) {
                return fields
                    .map(function (o) {
                        var dir = 1;
                        if (o[0] === '-') {
                            dir = -1;
                            o=o.substring(1);
                        }
                        if (a[o] > b[o]) return dir;
                        if (a[o] < b[o]) return -(dir);
                        return 0;
                    })
                    .reduce(function firstNonZeroValue (p,n) {
                        return p ? p : n;
                    }, 0);
            };
        }

    }

})($);
