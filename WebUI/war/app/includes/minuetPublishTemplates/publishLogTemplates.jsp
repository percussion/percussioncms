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

<!-- Template for site card area on publishing page (v2 layout)-->
<script id="templatePercPublishLogs" type="text/x-handlebars-template">
    <div id="percPublishLogsContainer" class="container">
        <div class="row">
            <div class="col-lg-12">
                <form id="percPublishLogsForm">
                    <div class="form-row">
                        <div class="form-group col-sm">
                            <label for="percReportDays"><i18n:message key="perc.ui.publish.title@Date Range"/></label>
                            <select class="form-control perc-report-selector form-control-sm" id="percReportDays" name="days">
                                <option value="3"><i18n:message key="perc.ui.publish.reports@3 Days"/></option>
                                <option value="5"><i18n:message key="perc.ui.publish.reports@5 Days"/></option>
                                <option value="10"><i18n:message key="perc.ui.publish.reports@10 Days"/></option>
                            </select>
                        </div>
                        <div class="form-group col-sm">
                            <label for="percReportServer"><i18n:message key="perc.ui.publish.title@Server"/></label>
                            <select class="form-control perc-report-selector form-control-sm" id="percReportServer" name="pubServerId">
                                <option value=" "><i18n:message key="perc.ui.publish.title@All"/></option>
                                {{#each serverInfo}}
                                <option value="{{serverId}}">{{serverName}}</option>
                                {{/each}}
                            </select>
                        </div>
                        <div class="form-group col-sm">
                            <label for="percReportItems"><i18n:message key="perc.ui.publish.title@Show"/></label>
                            <select class="form-control perc-report-selector form-control-sm" id="percReportItems" name="maxcount">
                                <option value="20"><i18n:message key = "perc.ui.publish.reports@20 items"/></option>
                                <option value="30"><i18n:message key = "perc.ui.publish.reports@30 items"/></option>
                                <option value="50"><i18n:message key = "perc.ui.publish.reports@50 items"/></option>
                            </select>
                        </div>
                        <div class="d-flex align-items-end form-group col-sm">
                            <button aria-label='<i18n:message key="perc.ui.perc.pub.reports@Delete Logs"/>' type="button" id="percDeleteServerLogs" class="btn btn-sm btn-block perc-btn-inverse"><i class="fas fa-trash-alt"></i></button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="table-responsive-lg">
                    <table class="table table-striped" id="percSitePublishingLogList" data-perc-publishing-log-data="" >
                        <thead>
                        <tr>
                            <th scope="col"><i18n:message key="perc.ui.publish.title@Actions"/></th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-active-sort" data-perc-sort-order="desc" data-perc-sort-prop="calculatedStartDate"><i18n:message key="perc.ui.publish.title@Date"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="pubServerName"><i18n:message key="perc.ui.publish.title@Server"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="elapsedTime"><i18n:message key="perc.ui.publish.title@Duration"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="completedItems"><i18n:message key="perc.ui.publish.title@Published"/>&nbsp;&nbsp;</th>
                            <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="status"><i18n:message key="perc.ui.publish.title@Status"/>&nbsp;&nbsp;</th>
                            <th scope="col" role="button">
                                <div class="form-check">
                                    <label class="form-check-label">
                                        <input id="percToggleSelectAllLogs" class="form-check-input" type="checkbox">
                                        <i18n:message key="perc.ui.publish.title@Delete"/>
                                    </label>
                                </div>
                            </th>
                        </tr>
                        </thead>
                        <tbody id="percServerLogListTarget"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</script>

<!-- Template for publishing log list -->
<script id="templatePercServerLogList" type="text/x-handlebars-template">
    {{#each SitePublishJob}}
    {{#if status}}
    <tr>
        <td class="align-middle"><button data-perc-log-object="{{#JSONstring this}}{{/JSONstring}}" class="btn btn-sm perc-btn-primary perc-log-details" type="button"><i18n:message key="perc.ui.publish.title@Details"/></button></td>
        <td class="align-middle">{{startDate}} {{startTime}}</td>
        <td class="align-middle">{{pubServerName}}</td>
        <td class="align-middle">{{#duration elapsedTime}}{{/duration}}</td>
        <td class="align-middle">{{completedItems}}</td>
        <td class="align-middle">{{status}}</td>
        <td class="align-middle">
            <div class="form-check form-check-inline align-middle">
                <input data-perc-job-id="{{jobId}}" class="form-check-input perc-delete-log-item" type="checkbox" value="percSelectAllLogs">
            </div>
        </td>
    </tr>
    {{/if}}
    {{/each}}
</script>

<script id="templatePercPublishLogDetailsOverlay" type="text/x-handlebars-template">
    <div id="percPublishLogDetails" role="dialog" aria-modal="true" tabindex="-1">
        <div class="container" role="document">
            <div class="row">
                <div class="col perc-publish-details-title-container">
                    <i18n:message key="perc.ui.publish.title@Publish Details"/><br><hr>
                </div>
            </div>
            <div class="row">
                <div class="col perc-publish-state-nav text-right">
                    <span class="float-left"><button id="percClosePublishItemsDetails" class="btn perc-btn-primary"><i18n:message key="perc.ui.publish.reports@Back"/></button></span>
                </div>
            </div>
            <div class="row">
                <div class="col-12 col-md-6">
                    <p><strong><i18n:message key="perc.ui.publish.title@Job ID"/>:</strong> {{jobId}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Date"/>:</strong> {{startDate}} {{startTime}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Site"/>:</strong> {{siteName}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Server"/>:</strong> {{pubServerName}}</p>
                </div>
                <div class="col-12 col-md-6">
                    <p><strong><i18n:message key="perc.ui.publish.title@Status"/>:</strong> {{status}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Duration"/>:</strong> {{#duration elapsedTime}}{{/duration}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Published"/>:</strong> {{completedItems}}</p>
                    <p><strong><i18n:message key="perc.ui.publish.title@Removed"/>:</strong> {{removedItems}}</p>
                </div>
            </div>
            <div class="row">
                <div class="col mt-3 mb-3">
                    {{SitePublishItem.length}} <i18n:message key="perc.ui.publish.title@Publish Items Attempted"/>
                </div>
            </div>
            {{#if SitePublishItem}}
            <div class="row">
                <div class="col">
                    <div class="md-form mt-0 mb-3">
                        <input class="form-control perc-publish-item-filter-field" type="text" placeholder='<i18n:message key="perc.ui.publish.title@Filter Items"/>' aria-label="Search">
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col">
                    <div class="table-responsive-lg perc-publish-details-container">
                        <table class="table table-striped" id="percSitePublishingLogDetails">
                            <thead>
                            <tr>
                                <th scope="col"><i18n:message key="perc.ui.publish.title@Details"/></th>
                                <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="status"><i18n:message key="perc.ui.publish.title@Status"/></th>
                                <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="operation"><i18n:message key="perc.ui.publish.title@Operation"/></th>
                                <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="fileLocation"><i18n:message key="perc.ui.publish.title@Location"/></th>
                                <th role="button" tabindex="0" scope="col" class="perc-sortable-header perc-inactive-sort" data-perc-sort-prop="elapsedTime"><i18n:message key="perc.ui.publish.title@Elapsed Time"/></th>
                            </tr>
                            </thead>
                            <tbody id="percPublishLogDetailsListTarget"></tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="row">
                <div class="col mt-3">
                    <hr>
                </div>
            </div>
            <div class="row">
                <div class="col mb-2">
                    <h6 class="perc-publish-item-details-title"><strong><i18n:message key="perc.ui.publish.title@Publish Item Details"/></strong></h6>
                </div>
            </div>
            <div id="percPublishItemLogDetailsTarget">
                <div class="row">
                    <div class="col">
                        <p><i18n:message key="perc.ui.publish.title@Select Publish Item"/></p>
                    </div>
                </div>
            </div>
            {{/if}}
        </div>
    </div>
</script>

<script id="templatePercPublishLogDetailsList" type="text/x-handlebars-template">
    {{#each SitePublishItem}}
    <tr>
        <td class="align-middle"><button aria-label='<i18n:message key="perc.ui.publish.title@Publish Item Details"/>' data-perc-publish-item-data="{{#JSONstring this}}{{/JSONstring}}" type="button" id="percDeleteServerLogs" class="btn btn-sm perc-btn-primary perc-publish-item-details-button"><i class="fas fa-search"></i></button></td>
        <td class="align-middle">{{status}}</td>
        <td class="align-middle perc-title-case">{{operation}}</td>
        <td class="align-middle">{{fileName}}</td>
        <td class="align-middle">{{#duration elapsedTime}}{{/duration}}</td>
    </tr>
    {{/each}}
</script>

<script id="templatePercPublishItemLogDetails" type="text/x-handlebars-template">
    <div class="row">
        <div class="col-12 col-md-6">
            <p><strong><i18n:message key="perc.ui.publish.title@Content ID"/>:</strong> {{contentid}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Revision ID"/>:</strong> {{revisionid}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Item Status ID"/>:</strong> {{itemStatusId}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Template ID"/>:</strong> {{templateid}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Filename"/>:</strong> {{fileName}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Location"/>:</strong> {{fileLocation}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Folder ID"/>:</strong> {{folderid}}</p>
        </div>
        <div class="col-12 col-md-6">
            <p><strong><i18n:message key="perc.ui.publish.title@Operation"/>:</strong> {{operation}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Status"/>:</strong> {{status}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Delivery Type"/>:</strong> {{deliveryType}}</p>
            <p><strong><i18n:message key="perc.ui.publish.title@Elapsed Time"/>:</strong> {{#duration elapsedTime}}{{/duration}}</p>
        </div>
    </div>
    <div class="row mt-3">
        <div class="col-12">
            <div class="form-group">
                <label for="percAssemblyUrl"><strong><i18n:message key="perc.ui.publish.title@Assembly URL"/>:</strong></label>
                <textarea readonly class="form-control" id="percAssemblyUrl" rows="2">{{assemblyUrl}}</textarea>
            </div>
        </div>
    </div>
    <div class="row mt-3">
        <div class="col-12">
            <div class="form-group">
                <label for="percItemErrorMessage"><strong><i18n:message key="perc.ui.publish.title@Error Details"/>:</strong></label>
                <textarea readonly class="form-control" id="percItemErrorMessage" rows="3">{{errorMessage}}</textarea>
            </div>
        </div>
    </div>
</script>
