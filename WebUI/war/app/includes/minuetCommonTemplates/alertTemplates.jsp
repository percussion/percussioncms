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

<script id="templateFullScreenDialog" type="text/x-handlebars-template">
    <div role="dialog" aria-modal="true" tabindex="-1" class="perc-fullscreen-dialog {{#if (validatePropertyValue type 'warning')}}perc-background-inverse{{else}}perc-background-branded{{/if}} align-middle">
        <div role="document" class="container-fluid d-flex h-100">
            <div class="row justify-content-center align-self-center perc-alert-container">
                <div class="container">
                    {{#if title}}
                    <div class="row">
                        <div class="col">
                            <span class="perc-dialog-title">{{title}}</span>
                            <hr class="perc-divider-white mb-5">
                        </div>
                    </div>
                    {{/if}}
                    <div class="row">
                        <div class="col perc-dialog-message">
                            <p class="text-left">{{message}}</p>
                        </div>
                    </div>
                    <div class="row">
                        <div class="col">
                            <button data-perc-confirmation-result="cancel" type="button" class="btn btn-block perc-confirmation-button{{#if (validatePropertyValue type 'warning')}} perc-confirmation-button-warning{{else}} perc-confirmation-button-branded{{/if}}"><i18n:message key="perc.ui.common.label@Cancel"/></button>
                        </div>
                        <div class="col">
                            <button data-perc-confirmation-result="confirm" type="button" class="btn btn-block perc-confirmation-button{{#if (validatePropertyValue type 'warning')}} perc-confirmation-button-warning{{else}} perc-confirmation-button-branded{{/if}}"><i18n:message key="perc.ui.common.label@Confirm"/></button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>

<!-- This template processes warning and success messages (v2 layout) -->
<script id="templateResponseFooterAlert" type="text/x-handlebars-template">
    <div class="container-fluid">
        <div class="row">
            <div class="alert alert-{{#unless result.warning}}success{{else}}warning{{/unless}} alert-dismissible" role="alert">
                <span role="alertdialog" class="perc-alert-message-strong">{{source}}:&nbsp;&nbsp;</span>{{#unless result.warning}}{{result.status}}{{else}}{{result.warning}}{{/unless}}
                <button id="percDismissFooterAlert" type="button" class="close" aria-label="Dismiss Alert">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        </div>
    </div>
</script>

<script id="templateProcessRunningFooterAlert" type="text/x-handlebars-template">
    <div id="percProcessRunningAlert" class="container-fluid">
        <div class="row">
            <div class="alert perc-alert-process-running" role="alert">
                <span class="perc-alert-message-strong float-left"><i18n:message key="perc.ui.common.label@Processing"/></span><span class="float-right"><i class="fa fa-cog fa-spin fa-fw"></i></span>
            </div>
        </div>
    </div>
</script>

<script id="templatePercSessionExpiringDialog" type="text/x-handlebars-template">
    <div class="perc-fullscreen-dialog {{#if (validatePropertyValue type 'warning')}}perc-background-inverse{{else}}perc-background-branded{{/if}} align-middle">
        <div class="container-fluid d-flex h-100">
            <div class="row justify-content-center align-self-center perc-alert-container">
                <div class="container">
                    <div class="row">
                        <div class="col perc-dialog-message">
                            <span class="perc-dialog-title"><i18n:message key="perc.ui.session.timeout@Session Inactivity"/></span>
                            <hr class="perc-divider-white mb-5">
                            <p class="text-left">{{message}}</p>
                            <p id="logouttime" class="text-left"></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</script>
