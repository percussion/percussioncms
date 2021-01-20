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

<script id="templateIncrementalPublishPreviewOverlay" type="text/x-handlebars-template">
  <div id="percIncrementalPublishPreviewOverlay" role="dialog" aria-modal="true" tabindex="-1">
      <div class="container" role="document">
        <div class="row">
            <div class="col perc-publish-details-title-container">
                <i18n:message key="perc.ui.publish.incrementalPreview.title@Incremental Publish Preview"/><br><hr>
            </div>
        </div>
        <div class="row">
            <div class="col text-right">
                <span class="float-left"><button id="percCloseIncrementalPublishPreviewOverlay" class="btn perc-btn-primary"><i18n:message key="perc.ui.common.label@Back"/></button></span>
                {{#if PagedItemList.childrenInPage}}
                <span class="float-right"><button id="percIncrementalPublishConfirm" class="btn perc-btn-primary"><i18n:message key="perc.ui.common.label@Continue"/></button></span>
                {{/if}}
            </div>
        </div>
        <div class="row">
            <div class="col mt-3 mb-3">
                {{#if PagedItemList.childrenInPage}}
                <i18n:message key="perc.ui.publish.incrementalPreview@Items queued for incremental"/>: {{PagedItemList.childrenCount}}
                {{/if}}
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div class="table-responsive-lg perc-incremental-publish-content-container">
                  <table class="table table-striped table-fixed" id="percIncrementalPreviewList">
                      <thead>
                        <tr>
                            <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Filename"/></th>
                            <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Title"/></th>
                            <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Modified"/></th>
                            <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Modified By"/></th>
                            <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Published"/></th>
                        </tr>
                      </thead>
                      <tbody id="percIncrementalPublishQueueList">
                        {{#if PagedItemList.childrenInPage}}
                        {{#each PagedItemList.childrenInPage}}
                        <tr>
                          <td>{{#filterByValue columnData.column 'name' 'sys_title'}}{{/filterByValue}}</td>
                          <td>{{name}}</td>
                          <td>{{#stringToDate (filterByValue columnData.column 'name' 'sys_contentlastmodifieddate')}}{{/stringToDate}}</td>
                          <td>{{#filterByValue columnData.column 'name' 'sys_contentlastmodifier'}}{{/filterByValue}}</td>
                          <td>{{#filterByValue columnData.column 'name' 'sys_contentpublicationdate'}}{{/filterByValue}}</td>
                        </tr>
                        {{/each}}
                        {{else}}
                        <tr>
                          <td colspan="5">
                            <i18n:message key="perc.ui.publish.incrementalPreview@No items queued for incremental"/>
                          </td>
                        </tr>
                        {{/if}}
                      </tbody>
                  </table>
                </div>
            </div>
        </div>
        <incrementalRelatedItems id="percIncrementalRelatedItemsTarget"></incrementalRelatedItems>
      </div>
  </div>
</script>

<script id="templateIncrementalPublishRelatedItems" type="text/x-handlebars-template">
  <div class="row">
    <div class="col">
      <hr>
    </div>
  </div>
  <div class="row">
      <div class="col mt-3 mb-3">
          {{#if PagedItemList.childrenInPage}}
          <i18n:message key="perc.ui.publish.incrementalPreview@Related items for approval"/>
          {{/if}}
      </div>
  </div>
  <div class="row">
    <div class="col">
      <div class="table-responsive-lg perc-incremental-publish-related-content-container">
        <table class="table table-striped table-fixed" id="percIncrementalRelatedPreviewList">
            <thead>
              <tr>
                  <th role="button" tabindex="0" scope="col">
                    <div class="form-check form-check-inline">
                      <input class="form-check-input" type="checkbox" id="percSelectAllRelatedItems">
                    </div>
                  </th>
                  <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Filename"/></th>
                  <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Title"/></th>
                  <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Modified"/></th>
                  <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Modified By"/></th>
                  <th role="button" tabindex="0" scope="col"><i18n:message key="perc.ui.publish.title@Last Published"/></th>
              </tr>
            </thead>
            <tbody id="percIncrementalPublishRelatedItemsList">
              {{#if PagedItemList.childrenInPage}}
              {{#each PagedItemList.childrenInPage}}
              <tr data-perc-related-item="{{#JSONstring this}}{{/JSONstring}}">
                <td>
                  <div class="form-check form-check-inline">
                    <input class="form-check-input" type="checkbox">
                  </div>
                </td>
                <td>{{#filterByValue columnData.column 'name' 'sys_title'}}{{/filterByValue}}</td>
                <td>{{name}}</td>
                <td>{{#stringToDate (filterByValue columnData.column 'name' 'sys_contentlastmodifieddate')}}{{/stringToDate}}</td>
                <td>{{#filterByValue columnData.column 'name' 'sys_contentlastmodifier'}}{{/filterByValue}}</td>
                <td>{{#filterByValue columnData.column 'name' 'sys_contentpublicationdate'}}{{/filterByValue}}</td>
              </tr>
              {{/each}}
              {{else}}
              <tr>
                <td colspan="6">
                  <i18n:message key="perc.ui.publish.incrementalPreview@No related items available for approval"/>
                </td>
              </tr>
              {{/if}}
            </tbody>
        </table>
      </div>
    </div>
  </div>
</script>
