<%@ taglib uri="/WEB-INF/tmxtags.tld" prefix="i18n" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

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
  ~      https://www.percussion.com
  ~
  ~     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
  --%>

<!-- Template for site card area on publishing page (v2 layout)-->
<script id="templatePublishingCards" type="text/x-handlebars-template">
    <div id="percPublishSiteCards" class="container">
        <div class="row">
            <div class="col">
                <h2 class="perc-site-instruction"><i18n:message key="perc.ui.publish.view@Select a Site"/></h2>
            </div>
        </div>
        <div class="row">
            <div class="col-md-4 order-sm-first order-md-2 mb-3">
                <div class="btn-group btn-group-toggle d-flex" data-toggle="buttons">
                    <label sr-only='<i18n:message key = "perc.ui.publish.title@Card"/>' class="btn btn-secondary perc-site-view-toggle-button active">
                        <input value="card" type="radio" name="percSiteView" id="percSiteCardOption" autocomplete="off" checked><i aria-hidden class="fas fa-th fa-fw"></i>&nbsp;&nbsp;<i18n:message key = "perc.ui.publish.title@Card"/>
                    </label>
                    <label sr-only='<i18n:message key = "perc.ui.publish.title@List"/>' class="btn btn-secondary perc-site-view-toggle-button">
                        <input value="list" type="radio" name="percSiteView" id="percSiteListOption" autocomplete="off"><i aria-hidden class="fas fa-th-list"></i>&nbsp;&nbsp;<i18n:message key = "perc.ui.publish.title@List"/>
                    </label>
                </div>
            </div>
            <div class="col-md-8 float-left">
                <!-- Filter Field -->
                <div class="md-form mt-0">
                    <input class="form-control perc-site-filter-field" type="text" placeholder='<i18n:message key="perc.ui.publish.title@Filter Sites"/>' aria-label='<i18n:message key="perc.ui.publish.title@Filter Sites"/>'>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col mb-3">
                <hr>
            </div>
        </div>
        <div id="sitesCardView" class="row justify-content-center" style="display:none;">
            {{#each SiteSummary}}
            <div class="col-lg-4 perc-site-card-container mb-4">
                <div aria-label="{{name}}" tabindex="0" role="button" data-perc-site-id="{{siteId}}" class="card perc-site-card perc-site-select h-100">
                    <div class="card-header perc-site-card-name text-center">
                        {{name}}
                    </div>
                    <div class="card-body perc-site-card-icon-container">
                        <h5 class="card-title perc-site-card-icon text-center align-middle">
                            <i aria-hidden class="fas fa-globe-americas fa-10x"></i>
                        </h5>
                    </div>
                </div>
            </div>
            {{/each}}
        </div>
        <div id="sitesListView" class="row" style="display:none;">
            <div class="col-lg-12">
                <div class="table-responsive-md">
                    <table class="table table-striped table-hover">
                        <tbody>
                        {{#each SiteSummary}}
                        <tr aria-label="{{name}}" role="button" tabindex="0" scope="row" data-perc-site-id="{{siteId}}" class="perc-site-select">
                            <td class="align-middle w-5"><i aria-hidden class="fas fa-globe-americas fa-2x fa-fw"></i></td>
                            <td class="align-middle">{{name}}</td>
                            <td class="align-middle d-none d-sm-table-cell">{{folderPath}}</td>
                        </tr>
                        {{/each}}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</script>

<!-- Template site details shell (v2 layout)-->
<script id="templateSiteDetailsShell" type="text/x-handlebars-template">
    <div class="container">
        <div class="row">
            <div class="col perc-site-name-container d-flex align-items-center" id="siteTitleLogoTarget"></div>
        </div>
        <div class="row">
            <div class="col">
                <hr>
            </div>
        </div>
        <div class="row">
            <div class="col perc-publish-state-nav text-right">
                <span class="float-left"><button id="percBackToSites" class="btn perc-btn-primary"><i18n:message key="perc.ui.publish.reports@Back"/></button></span>&nbsp;
                <span><button title='<i18n:message key="perc.ui.publish.view@Add New Server"/>' id="percAddServer" class="btn perc-btn-primary"><i aria-hidden class="fas fa-plus"></i></button></span>
                <span><button title='<i18n:message key="perc.ui.publish.view@Refresh Server Info"/>' id="percRefreshServerList" class="btn perc-btn-primary"><i aria-hidden class="fas fa-sync-alt"></i></button></span>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div id="percServerSection">
                    <div class="card perc-server-card">
                        <div class="card-header perc-site-card-name" data-toggle="collapse" data-target="#percServerList" aria-expanded="true" aria-controls="percServerList">
                            <div id="percServerSectionTitle" class="perc-publish-section-title">
                                <i18n:message key="perc.ui.publish.view@Servers"/>
                            </div>
                        </div>
                        <div id="percServerList" class="collapse show" aria-labelledby="percServerSectionTitle" data-parent="#percServerSection">
                            <div class="card-body" id="publishServerListTarget">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col">
                <div id="percServerStatusSectionCard" class="card perc-server-card">
                    <div class="card-header perc-site-card-name">
                        <div id="percServerStatusSectionTitle" class="perc-publish-section-title">
                            <i18n:message key="perc.ui.perc.pub.reports@Current Status"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <section id="percPublishStatusTarget"></section>
        <div class="row">
            <div class="col">
                <div id="percServerLogSectionCard" class="card perc-server-card">
                    <div class="card-header perc-site-card-name">
                        <div id="percServerLogSectionTitle" class="perc-publish-section-title">
                            <i18n:message key="perc.ui.perc.pub.reports@Logs"/>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <section id="percPublishLogsTarget"></section>
    </div>
</script>

<!-- Template for site title and logo (v2 layout) -->
<script id="templateSiteTitleLogo" type="text/x-handlebars-template">
    <span class="perc-site-logo-small"><i aria-hidden class="fas fa-globe-americas fa-3x"></i></span>&nbsp;<span class="perc-site-name-header">{{name}}</span>
</script>

<!-- Template for rows of publishing servers (v2 layout) -->
<script id="templatePublishServerList" type="text/x-handlebars-template">
    <!-- container is not needed because this is a nested template -->
    {{#each serverInfo}}
    <div class="row align-items-center perc-publish-server-row">
        <div class="col-sm-12 col-md-7 perc-server-list-col">
            <span class="perc-server-list-name">{{serverName}}{{#if (validatePropertyValue serverType 'PRODUCTION')}}&nbsp;&nbsp;<i aria-label='<i18n:message key="perc.ui.publish.view@Production"/>' class="fas fa-globe-americas"></i>{{/if}}{{#if isDefault}}&nbsp;&nbsp;<i aria-label='<i18n:message key="perc.ui.publish.view@Publish Now Server"/>' class="fas fa-star"></i>{{/if}}</span>
        </div>
        <div class="col-sm-12 col-md-5 perc-server-list-col text-md-right text-sm-left">
            <button aria-label='<i18n:message key="perc.ui.publish.title@Server Configuration"/>' class="btn perc-btn-primary perc-edit-server-properties" data-perc-server-id="{{serverId}}"><i class="fas fa-cog"></i></button>&nbsp;
            <div data-perc-server-name="{{serverName}}" class="btn-group perc-publish-button-group">
                {{#if (enableIncremental canIncrementalPublish isFullPublishRequired)}}
                <button aria-label='<i18n:message key="perc.ui.publish.view@Incremental Publish"/>' data-perc-publish-type="incremental" data-perc-server-id="{{serverId}}" type="button" class="btn perc-btn-primary perc-publish-button"><i18n:message key="perc.ui.publish.view@Incremental"/></button>
                <button type="button" class="btn perc-btn-primary dropdown-toggle dropdown-toggle-split" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    <span class="sr-only"><i18n:message key="perc.ui.publish.title@Toggle Dropdown"/></span>
                </button>
                <div class="dropdown-menu">
                    <a aria-label='<i18n:message key="perc.ui.publish.view@Full Publish"/>' data-perc-publish-type="full" href="#" class="dropdown-item perc-publish-button"><i18n:message key="perc.ui.publish.title@Full"/></a>
                </div>
                {{else}}
                <button aria-label='<i18n:message key="perc.ui.publish.view@Full Publish"/>' data-perc-publish-type="full" type="button" class="btn perc-btn-primary perc-publish-button"><i18n:message key="perc.ui.publish.title@Full"/></button>
                {{/if}}
            </div>
        </div>
    </div>
    {{/each}}
</script>

<!-- This template contains the modal for server properties (v2 layout) -->
<script id="templateServerPropertiesModal" type="text/x-handlebars-template">
    <div id="percServerPropertiesModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h4><i18n:message key="perc.ui.publish.view@Server Properties Configuration"/></h4>
                    <button type="button" class="close perc-clear-server" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>
                <div class="modal-body" id="percServerPropertiesFormTarget"></div>
                <div class="modal-footer">
                    {{#if (validatePropertyValue action 'update')}}<button aria-label='<i18n:message key="perc.ui.publish.title@Delete Server"/>' type="button" id="percDeleteServer" class="btn perc-btn-inverse mr-auto"><i class="fas fa-trash-alt"></i></button>{{/if}}
                    <button type="button" class="btn perc-btn-primary perc-clear-server" data-dismiss="modal"><i18n:message key="perc.ui.common.label@Cancel"/></button>
                    <button id="percUpdateServerProperties" data-perc-server-name="{{serverInfo.serverName}}" type="button" class="btn perc-btn-primary"><i18n:message key="perc.ui.common.label@Save"/></button>
                </div>
            </div>
        </div>
    </div>
</script>

<!-- This template contains the server properties form (v2 layout) -->
<script id="templateServerPropertiesForm" type="text/x-handlebars-template">
    <csrf:form id="percServerPropertiesForm" action="publishTemplates.jsp" method="post">
        <section id="percServerPropertiesCommonTarget"></section>
        <section id="percServerPropertiesFileFTPTarget"></section>
        <section id="percServerPropertiesFileFTPSTarget"></section>
        <section id="percServerPropertiesFileSFTPTarget"></section>
        <section id="percServerPropertiesFileAMAZONS3Target"></section>
        <section id="percServerPropertiesFileLocalTarget"></section>
        <section id="percServerPropertiesDatabaseCommonTarget"></section>
        <section id="percServerPropertiesDatabaseMSSQLTarget"></section>
        <section id="percServerPropertiesDatabaseMYSQLTarget"></section>
        <section id="percServerPropertiesDatabaseOracleTarget"></section>
        <section id="percServerPropertiesOptionalTarget"></section>
    </csrf:form>
</script>

<!-- This template contains the common properties for each configuration -->
<script id="templatePercServerPropertiesCommon" type="text/x-handlebars-template">
    <div class="form-group">
        <label for="serverName">* <i18n:message key="perc.ui.publish.view@Server Name"/>:</label>
        <input aria-required="true" class="form-control" percServerProp="serverName" id="serverName" value="{{serverInfo.serverName}}" name="serverName">
    </div>
    {{#if (validatePropertyValue action 'create')}}
    <div class="form-group">
        <label for="percServerType">* <i18n:message key="perc.ui.publish.view@Server Type"/>:</label>
        <select aria-required="true" class="form-control perc-driver-group" id="percServerType" name="serverType">
            <option value="PRODUCTION"><i18n:message key="perc.ui.publish.view@Production"/></option>
            <option value="STAGING"><i18n:message key="perc.ui.publish.view@Staging"/></option>
        </select>
    </div>
    {{/if}}
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="publishType">* <i18n:message key="perc.ui.publish.view@Publish Type"/>:</label>
                <select aria-required="true" class="form-control" id="publishType" name="publishType">
                    <option {{#if (validatePropertyValue serverInfo.type 'File')}} selected{{/if}}>File</option>
                    <option {{#if (validatePropertyValue serverInfo.type 'Database')}} selected{{/if}}>Database</option>
                </select>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="fileDriver" id="percDriverLabel">* <i18n:message key="perc.ui.publish.servers@Driver"/>:</label>
                <select aria-required="true" aria-labelledby="percDriverLabel" data-perc-driver-group="File" class="form-control perc-driver-group" id="fileDriver" name="filedriver">
                    <option value="Local" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'Local')}} selected{{/if}}>Local</option>
                    <option value="FTP" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'FTP')}} selected{{/if}}>FTP</option>
                    <option value="FTPS" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'FTPS')}} selected{{/if}}>FTPS</option>
                    <option value="SFTP" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'SFTP')}} selected{{/if}}>SFTP</option>
                    <option value="AMAZONS3" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'AMAZONS3')}} selected{{/if}}>Amazon S3</option>
                </select>
                <select aria-required="true" aria-labelledby="percDriverLabel" data-perc-driver-group="Database" class="form-control perc-driver-group" id="databaseDriver" name="filedriver">
                    <option value="MSSQL" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'MSSQL')}} selected{{/if}}>MS SQL</option>
                    <option value="MYSQL" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'MYSQL')}} selected{{/if}}>MYSQL</option>
                    <option value="Oracle" {{#if (validatePropertyValue (filterByValue serverInfo.properties 'key' 'driver') 'Oracle')}} selected{{/if}}>Oracle</option>
                </select>
            </div>
        </div>
        <div class="form-group" >
            <label for="publishServer">* <i18n:message key="perc.ui.publish.view@Select DTS Server"/>:</label>
            <select aria-required="true" class="form-control perc-region-group" percServerFileProp="publishServer" id="publishServer" name="publishServer" value="{{#filterByValue serverInfo.properties 'key' 'publishServer'}}{{/filterByValue}}">

            </select>
        </div>
    </div>


    <hr>
</script>

<!-- This template contains the optional server properties (v2 layout) -->
<script id="templatePercServerPropertiesOptional" type="text/x-handlebars-template">
    <div class="form-group">
        <p><i18n:message key="perc.ui.publish.servers@Optional Settings"/></p>
        <div class="form-row">
            <div class="form-check form-check-inline">
                <input
                        id="percPublishNowFlag"
                        class="form-check-input"
                        {{#if serverInfo.isDefault}}disabled{{else if (validatePropertyValue serverInfo.serverType 'STAGING')}}disabled{{/if}}
                type="checkbox"
                percServerProp="isDefault"
                {{#if serverInfo.isDefault}}checked{{/if}}
                name="isDefault">
                <label for="percPublishNowFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Set as Publish Now Server"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input
                        id="percIgnoreUnModifiedAssets"
                        class="form-check-input"
                        type="checkbox"
                        percServerProp="ignoreUnModifiedAssets"
                        {{#if (filterByValue serverInfo.properties 'key' 'ignoreUnModifiedAssets')}}checked{{else if (validatePropertyValue serverInfo.serverType 'PRODUCTION')}}{{#unless serverInfo.isDefault}}disabled{{/unless}}{{/if}}
                name="ignoreUnModifiedAssets">
                <label for="percIgnoreUnModifiedAssets" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Ignore Unmodified Assets"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input
                        id="percPublishRelatedItems"
                        class="form-check-input"
                        type="checkbox"
                        percServerProp="publishRelatedItems"
                        {{#if (filterByValue serverInfo.properties 'key' 'publishRelatedItems')}}checked{{/if}}
                name="publishRelatedItems">
                <label for="percPublishRelatedItems" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Publish Related Items"/></label>
            </div>
        </div>
    </div>
</script>

<!--This template contains the common server properties for database -->
<script id="templatePercServerPropertiesDatabaseCommon" type="text/x-handlebars-template">
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-database-server-name">* <i18n:message key="perc.ui.publish.view@Database Server"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-server"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerDatabaseProp="server" id="perc-database-server-name" name="server" value="{{#filterByValue serverInfo.properties 'key' 'server'}}{{/filterByValue}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-database-port">* <i18n:message key="perc.ui.publish.servers@Port"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-plug"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerDatabaseProp="port" id="perc-database-port" name="port" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'port'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
        </div>
    </div>
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-database-user-id">* <i18n:message key="perc.ui.publish.servers@User ID"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-user"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerDatabaseProp="userid" id="perc-database-user-id" name="userid" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'userid'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-database-password">* <i18n:message key="perc.ui.publish.servers@Password" />:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-lock"></i>
                        </div>
                    </div>
                    <input autocomplete="off" aria-required="true" type="password" class="form-control" percServerDatabaseProp="password" id="perc-database-password" name="password" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'password'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
        </div>
    </div>
    <div class="form-group" id="perc-database-name-container">
        <label for="perc-database-name">* <i18n:message key="perc.ui.publish.servers@Database Name"/>:</label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-database"></i>
                </div>
            </div>
            <input aria-required="true" class="form-control" percServerDatabaseProp="database" id="perc-database-name" name="database" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'database'}}{{/filterByValue}}{{/if}}">
        </div>
    </div>
</script>

<!-- This template contains the MS SQL server properties -->
<script id="templatePercServerPropertiesDatabaseMSSQL" type="text/x-handlebars-template">
    <div class="form-group">
        <label for="perc-database-owner">* <i18n:message key="perc.ui.publish.servers.mssql@Owner"/>:</label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-id-card-alt"></i>
                </div>
            </div>
            <input aria-required="true" class="form-control" percServerDatabaseProp="owner" id="perc-database-owner" name="owner" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'owner'}}{{/filterByValue}}{{/if}}">
        </div>
    </div>
</script>

<script id="templatePercServerPropertiesDatabaseOracle" type="text/x-handlebars-template">
    <div class="form-group">
        <label for="perc-database-schema">* <i18n:message key="perc.ui.publish.servers.mssql@Schema"/>:</label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-database"></i>
                </div>
            </div>
            <input aria-required="true" class="form-control" percServerDatabaseProp="schema" id="perc-database-schema" name="schema" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'schema'}}{{/filterByValue}}{{/if}}">
        </div>
    </div>
    <div class="form-group">
        <label for="perc-database-sid">* SID:</label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-id-card-alt"></i>
                </div>
            </div>
            <input aria-required="true" class="form-control" percServerDatabaseProp="sid" id="perc-database-sid" name="sid" value="{{#if (validatePropertyValue serverInfo.type 'Database')}}{{#filterByValue serverInfo.properties 'key' 'sid'}}{{/filterByValue}}{{/if}}">
        </div>
    </div>
</script>

<!-- This template contains the server properties for file local (v2 layout) -->
<script id="templatePercServerPropertiesFileLocal" type="text/x-handlebars-template">
    <div role="radiogroup" aria-required="true" class="form-group">


        <p id="percFolderLocation">* <i18n:message key="perc.ui.publish.servers.mssql@Folder Location"/></p>

        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" value="default" percServerFileProp="defaultServerFlag" id="defaultServerFlag" name="folderLocation" {{#if (filterByValue serverInfo.properties 'key' 'defaultServerFlag')}} checked{{/if}}>
            <label class="form-check-label" for="defaultServerFlag"><i18n:message key="perc.ui.publish.servers.mssql@Server Default"/></label>
        </div>
        <div class="form-check form-check-inline">
            <input class="form-check-input" type="radio" value="own" percServerFileProp="ownServerFlag" id="ownServerFlag" name="folderLocation" {{#if (filterByValue serverInfo.properties 'key' 'ownServerFlag')}} checked{{/if}}>
            <label class="form-check-label" for="ownServerFlag"><i18n:message key="perc.ui.publish.servers.local@Exact Path"/></label>
        </div>
        <div class="form-check form-check-inline" id ="percPublishSecureSiteConfigOnExactPathdiv"
             {{#if (filterByValue serverInfo.properties 'key' 'defaultServerFlag')}}style="display:none;"{{/if}}
        >
        <input id="percPublishSecureSiteConfigOnExactPath"
               class="form-check-input"
               type="checkbox"
               percServerProp="publishSecureSiteConfigOnExactPath"
               {{#if (filterByValue serverInfo.properties 'key' 'publishSecureSiteConfigOnExactPath')}}checked{{else if (validatePropertyValue serverInfo.serverType 'ownServerFlag')}}{{#unless serverInfo.ownServerFlag}}disabled{{/unless}}{{/if}}
        name="publishSecureSiteConfigOnExactPath">
        <label for="percPublishSecureSiteConfigOnExactPath" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Publish Secure Site Config"/></label>
    </div>

    </div>
    <div class="form-group">
        <div class="input-group mb-4">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-folder-open"></i>
                </div>
            </div>
            <input aria-required="true" aria-labelledby="percFolderLocation"
                   readonly="readonly"
                   {{#unless (filterByValue serverInfo.properties 'key' 'defaultServerFlag')}}
            style="display:none;"
            {{/unless}}
            class="form-control"
            percServerFileProp="defaultServer"
            type="text"
            id="defaultServer"
            name="defaultServer"
            value="{{#filterByValue serverInfo.properties 'key' 'defaultServer'}}{{/filterByValue}}">
            <input aria-required="true" aria-labelledby="percFolderLocation"
                   {{#unless (filterByValue serverInfo.properties 'key' 'ownServerFlag')}}
            style="display:none;"
            {{/unless}}
            class="form-control"
            percServerFileProp="ownServer"
            type="text"
            id="ownServer"
            name="ownServer"
            value="{{#filterByValue serverInfo.properties 'key' 'folder'}}{{/filterByValue}}">
        </div>
        <hr>
    </div>
    <div role="radiogroup" aria-required="true" class="form-group">
        <p>* <i18n:message key="perc.ui.publish.servers.local@Format"/></p>
        <div class="form-check form-check-inline">
            <input id="percFormatHTML" type="radio"
                   class="form-check-input"
                   percServerFileProp="HTML"
                   value="HTML" name="formatType"
                   {{#if (filterByValue serverInfo.properties 'key' 'HTML')}} checked{{/if}}>
            <label for="percFormatHTML" class="form-check-label">HTML</label>
        </div>
        <div class="form-check form-check-inline">
            <input id="percFormatXml" type="radio"
                   class="form-check-input"
                   percServerFileProp="XML"
                   value="HTML" name="formatType"
                   {{#if (filterByValue serverInfo.properties 'key' 'XML')}} checked{{/if}}>
            <label for="percFormatXml" class="form-check-label">XML</label>
        </div>
    </div>
    <hr>
</script>

<!-- This template contains the server properties for the Amazon S3 driver -->
<script id="templatePercServerPropertiesFileAMAZONS3" type="text/x-handlebars-template">
    <div class="form-group">
        <label for="perc-amazon-s3-bucket-location">* <i18n:message key="perc.ui.publish.servers.s3@Bucket Name"/></label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fab fa-aws"></i>
                </div>
            </div>
            <input aria-required="true" class="form-control" percServerFileProp="bucketlocation" id="perc-amazon-s3-bucket-location" name="bucketlocation" value="{{#filterByValue serverInfo.properties 'key' 'bucketlocation'}}{{/filterByValue}}">
        </div>
    </div>
    <div class="form-group" id="s3accessSecurityKey" >
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-access-key">* <i18n:message key="perc.ui.publish.servers.s3@Access Key"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-key"></i>
                        </div>
                    </div>
                    <input autocomplete="off" type="password" aria-required="true" class="form-control" percServerFileProp="accesskey" id="perc-access-key" name="accesskey" value="{{#filterByValue serverInfo.properties 'key' 'accesskey'}}{{/filterByValue}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-security-key">* <i18n:message key="perc.ui.publish.servers.s3@Security Key"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-lock"></i>
                        </div>
                    </div>
                    <input autocomplete="off" aria-required="true" type="password" class="form-control" percServerFileProp="securitykey" id="perc-security-key" name="securitykey" value="{{#filterByValue serverInfo.properties 'key' 'securitykey'}}{{/filterByValue}}">
                </div>
            </div>
        </div>
    </div>
    <div class="form-group" >
        <label for="region">* <i18n:message key="perc.ui.publish.view@Region"/>:</label>
        <select aria-required="true" class="form-control perc-region-group" percServerFileProp="region" id="region" name="region" value="{{#filterByValue serverInfo.properties 'key' 'region'}}{{/filterByValue}}">
        </select>
    </div>
    <hr>
</script>

<!-- This template contains the server properties for FTP -->
<script id="templatePercServerPropertiesFileFTP" type="text/x-handlebars-template">
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-server-ip">* <i18n:message key="perc.ui.publish.servers.ftp@Server Address"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-server"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="serverip" id="perc-server-ip" name="serverip" value="{{#filterByValue serverInfo.properties 'key' 'serverip'}}{{/filterByValue}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-server-ftp-port">* <i18n:message key="perc.ui.publish.servers@Port"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-plug"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="port" id="perc-server-ftp-port" name="port" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'port'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
        </div>
    </div>
        <div role="radiogroup" aria-required="true" class="form-group" style="display:none">
            <p id="percAuthenticationMethodLabel">* <i18n:message key="perc.ui.publish.servers.ftp@Authentication Method"/></p>
            <div class="form-check form-check-inline">
                <input type="radio"
                       id="passwordFlag"
                       class="form-check-input perc-password-key-flag"
                       percServerFileProp="passwordFlag"
                       name="passwordkey"
                       {{#if (filterByValue serverInfo.properties 'key' 'passwordFlag')}} checked{{/if}}>
                <label for="passwordFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Password"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input type="radio"
                       id="privateKeyFlag"
                       class="form-check-input perc-password-key-flag"
                       percServerFileProp="privateKeyFlag"
                       name="passwordkey"
                       {{#if (filterByValue serverInfo.properties 'key' 'privateKeyFlag')}} checked{{/if}}>
                <label for="privateKeyFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input
                        class="form-check-input"
                        id="secureFTP"
                        type="checkbox"
                        percServerFileProp="secure"
                        name="secureFTP"
                        {{#if (filterByValue serverInfo.properties 'key' 'secure')}} checked{{/if}}>
                <label for="secureFTP" class="form-check-label"><i18n:message key="perc.ui.publish.servers.ftp@Secure FTP"/></label>
            </div>
        </div>
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-ftp-user">* <i18n:message key="perc.ui.publish.servers@User ID"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-user"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="userid" id="perc-ftp-user" name="userid" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'userid'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-ftp-password"><i18n:message key="perc.ui.publish.servers@Password"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-lock"></i>
                        </div>
                    </div>
                    <input autocomplete="off" type="password" class="form-control" percServerFileProp="password" id="perc-ftp-password" name="password" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'password'}}{{/filterByValue}}{{/if}}" {{#if (filterByValue serverInfo.properties 'key' 'secure')}} disabled{{/if}}>
                </div>
            </div>
        </div>
    </div>
        <div class="form-group mb-4" style="display:none">
            <label for="FTPprivateKeyList"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/>:</label>
            <div class="input-group">
                <div class="input-group-prepend">
                    <div class="input-group-text">
                        <i aria-hidden class="fas fa-key"></i>
                    </div>
                </div>
                <select class="form-control" id="FTPprivateKeyList" percServerFileProp="privateKey" name="privateKey"></select>
            </div>
        </div>
    <hr>
</script>

<!-- This template contains the server properties for FTPS -->
<script id="templatePercServerPropertiesFileFTPS" type="text/x-handlebars-template">
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-server-ip">* <i18n:message key="perc.ui.publish.servers.ftp@Server Address"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-server"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="serverip" id="perc-FTPSserver-ip" name="serverip" value="{{#filterByValue serverInfo.properties 'key' 'serverip'}}{{/filterByValue}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-server-ftp-port">* <i18n:message key="perc.ui.publish.servers@Port"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-plug"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="port" id="perc-server-ftps-port" name="port" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'port'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
        </div>
    </div>
        <div role="radiogroup" aria-required="true" class="form-group" style="display:none">
            <p id="percFTPSAuthenticationMethodLabel">* <i18n:message key="perc.ui.publish.servers.ftp@Authentication Method"/></p>
            <div class="form-check form-check-inline">
                <input type="radio"
                       id="ftpsPasswordFlag"
                       class="form-check-input perc-password-key-flag"
                       percServerFileProp="passwordFlag"
                       name="passwordkey"
                       {{#if (filterByValue serverInfo.properties 'key' 'passwordFlag')}} checked{{/if}}>
                <label for="ftpsPasswordFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Password"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input type="radio"
                       id="ftpsPrivateKeyFlag"
                       class="form-check-input perc-password-key-flag"
                       percServerFileProp="privateKeyFlag"
                       name="passwordkey"
                       {{#if (filterByValue serverInfo.properties 'key' 'privateKeyFlag')}} checked{{/if}}>
                <label for="ftpsPrivateKeyFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/></label>
            </div>
            <div class="form-check form-check-inline">
                <input
                        class="form-check-input"
                        id="secureSFTP"
                        type="checkbox"
                        percServerFileProp="secure"
                        name="secureFTP"
                        {{#if (filterByValue serverInfo.properties 'key' 'secure')}} checked{{/if}}>
                <label for="secureFTP" class="form-check-label"><i18n:message key="perc.ui.publish.servers.ftp@Secure FTP"/></label>
            </div>
        </div>
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-ftp-user">* <i18n:message key="perc.ui.publish.servers@User ID"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-user"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="userid" id="perc-ftps-user" name="userid" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'userid'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-ftps-password"><i18n:message key="perc.ui.publish.servers@Password"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-lock"></i>
                        </div>
                    </div>
                    <input autocomplete="off" type="password" class="form-control" percServerFileProp="password" id="perc-ftps-password" name="password" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'password'}}{{/filterByValue}}{{/if}}" {{#if (filterByValue serverInfo.properties 'key' 'secure')}} disabled{{/if}}>
                </div>
            </div>
        </div>
    </div>
        <div class="form-group mb-4" style="display:none">
            <label for="sftpPrivateKeyList"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/>:</label>
            <div class="input-group">
                <div class="input-group-prepend">
                    <div class="input-group-text">
                        <i aria-hidden class="fas fa-key"></i>
                    </div>
                </div>
                <select class="form-control" id="sftpPrivateKeyList" percServerFileProp="privateKey" name="privateKey"></select>
            </div>
        </div>
    <hr>
</script>

<!-- This template contains the server properties for SFTP -->
<script id="templatePercServerPropertiesFileSFTP" type="text/x-handlebars-template">
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-server-ip">* <i18n:message key="perc.ui.publish.servers.ftp@Server Address"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-server"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="serverip" id="perc-SFTPserver-ip" name="serverip" value="{{#filterByValue serverInfo.properties 'key' 'serverip'}}{{/filterByValue}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-server-ftp-port">* <i18n:message key="perc.ui.publish.servers@Port"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-plug"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="port" id="perc-server-sftp-port" name="port" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'port'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
        </div>
    </div>
    <div role="radiogroup" aria-required="true" class="form-group">
        <p id="percSFTPAuthenticationMethodLabel">* <i18n:message key="perc.ui.publish.servers.ftp@Authentication Method"/></p>
        <div class="form-check form-check-inline">
            <input type="radio"
                   id="SFTPpasswordFlag"
                   class="form-check-input perc-password-key-flag"
                   percServerFileProp="passwordFlag"
                   name="passwordkey"
                   {{#if (filterByValue serverInfo.properties 'key' 'passwordFlag')}} checked{{/if}}>
            <label for="SFTPpasswordFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers@Password"/></label>
        </div>
        <div class="form-check form-check-inline">
            <input type="radio"
                   id="SFTPprivateKeyFlag"
                   class="form-check-input perc-password-key-flag"
                   percServerFileProp="privateKeyFlag"
                   name="passwordkey"
                   {{#if (filterByValue serverInfo.properties 'key' 'privateKeyFlag')}} checked{{/if}}>
            <label for="SFTPprivateKeyFlag" class="form-check-label"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/></label>
        </div>
    </div>
    <div class="form-group">
        <div class="form-row">
            <div class="col-md-6 col-sm-12 perc-stacked-form-input">
                <label for="perc-ftp-user">* <i18n:message key="perc.ui.publish.servers@User ID"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-user"></i>
                        </div>
                    </div>
                    <input aria-required="true" class="form-control" percServerFileProp="userid" id="perc-sftp-user" name="userid" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'userid'}}{{/filterByValue}}{{/if}}">
                </div>
            </div>
            <div class="col-md-6 col col-sm-12">
                <label for="perc-sftp-password"><i18n:message key="perc.ui.publish.servers@Password"/>:</label>
                <div class="input-group">
                    <div class="input-group-prepend">
                        <div class="input-group-text">
                            <i aria-hidden class="fas fa-lock"></i>
                        </div>
                    </div>
                    <input autocomplete="off" type="password" class="form-control" percServerFileProp="password" id="perc-sftp-password" name="password" value="{{#if (validatePropertyValue serverInfo.type 'File')}}{{#filterByValue serverInfo.properties 'key' 'password'}}{{/filterByValue}}{{/if}}" {{#if (filterByValue serverInfo.properties 'key' 'secure')}} disabled{{/if}}>
                </div>
            </div>
        </div>
    </div>
    <div class="form-group mb-4">
        <label for="privateKeyList"><i18n:message key="perc.ui.publish.servers.ftp@Private Key File"/>:</label>
        <div class="input-group">
            <div class="input-group-prepend">
                <div class="input-group-text">
                    <i aria-hidden class="fas fa-key"></i>
                </div>
            </div>
            <select class="form-control" id="privateKeyList" percServerFileProp="privateKey" name="privateKey"></select>
        </div>
    </div>
    <hr>
</script>

<!-- This template lists out the available private keys for FTP configuration -->
<script id="templatePrivateKeyOptions" type="text/x-handlebars-template">
    {{#each keyNames}}
    <option {{#if (checkCurrentPrivateKey this)}}selected{{/if}} value="{{this}}">{{this}}</option>
    {{/each}}
</script>

