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

$(document).ready(function() {
    initiateSitewideFrameworkGadget();
});

function initiateSitewideFrameworkGadget() {
    retrieveSiteDataDeferred = $.Deferred();
    retrieveSiteData();
    retrieveSiteDataDeferred.done(function(sitePropertiesData) {
        renderFrameworkSettings(sitePropertiesData);
        renderSubmit();
    });
}

function retrieveSiteData() {

    // Start by getting the available sites
    getSitesDeferred = $.Deferred();
    $.PercSiteService.getSites(function(status, result) {
        if(status != 'error' && result.SiteSummary.length > 0) {
            getSitesDeferred.resolve(result);
        }
    });

    // Once the sites are retrieved, get the site properties for each site
    getSitesDeferred.done(function(sitesObject) {
        siteProperties = [];
        getSitePropertiesDeferred = [];
        $(sitesObject.SiteSummary).each(function(index, site) {
            getSitePropertiesDeferred[index] = $.Deferred();
            $.PercSiteService.getSiteProperties(site.name, function(status, result) {
                if(status != 'error') {
                    siteProperties.push(result.SiteProperties);
                    getSitePropertiesDeferred[index].resolve();
                }
            });
        });
        Promise.all(getSitePropertiesDeferred).then(function() {
            //  sort the array first by name before resolving deferred
            //siteProperties.sort((a, b) => (a.name.toLowerCase() > b.name.toLowerCase()) ? 1 : -1)

            for (var i=0; i< siteProperties.length; i++){
                var index = siteProperties.findIndex(v => typeof v.name === 'undefined' || v.name === null);
                if(index>-1){
                    siteProperties.splice(index, 1);
                }
            }

            siteProperties.sort((a, b) => ((a.name!=null ? a.name.toLowerCase() : '') > (b.name!=null ? b.name.toLowerCase() : '')) ? 1 : -1);
            retrieveSiteDataDeferred.resolve(siteProperties);
        });
    });

}

function renderFrameworkSettings(sitePropertiesData) {
    $('#percSitewideFrameworkTarget').empty();

    var gadgetHtml = '';
    $(sitePropertiesData).each(function(index, site) {

        var sitePropertiesJsonString = JSON.stringify(site);

        sitePropertiesJsonString = sitePropertiesJsonString.replace(/[\"&'\/<>]/g, function (a) {
            return {
                '"': '&quot;', '&': '&amp;', "'": '&#39;',
                '/': '&#47;',  '<': '&lt;',  '>': '&gt;'
            }[a];
        });

        gadgetHtml =  `<div style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-row">
                        <div style="font-family: 'Open Sans',sans-serif" id="perc-site-framework-container-${site.name}" class="perc-site-framework-container">
                          <h4 style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-site-name">${site.name}</h4>
                          <div style="font-family: 'Open Sans',sans-serif" data-perc-site-properties='${sitePropertiesJsonString}' class="perc-framework-contents">
                            <div  style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-checkbox perc-site-framework-foundation-override">
                              <input type="checkbox" id="perc-foundation-override-${site.name}" name="foundation-override-${site.name}" ${(site.overrideSystemFoundation === true) ? 'checked' : ''}>
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-foundation-override-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@Override Percussion Foundation")}</label>
                            </div>
                            <div style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-checkbox perc-site-framework-jquery-override">
                              <input type="checkbox" id="perc-jquery-override-${site.name}" name="jquery-override-${site.name}" ${(site.overrideSystemJQuery === true) ? 'checked' : ''}>
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-jquery-override-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@Override Percussion jQuery")}</label>
                            </div>
                            <div  style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-checkbox perc-site-framework-jqueryui-override">
                              <input type="checkbox" id="perc-jqueryui-override-${site.name}" name="jqueryui-override-${site.name}" ${(site.overrideSystemJQueryUI === true) ? 'checked' : ''}>
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-jqueryui-override-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@Override Percussion jQuery UI")}</label>
                            </div>
                            <div  style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-textarea-container perc-site-framework-additional-head-content-container">
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-site-framework-additional-head-content-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@Additional Head Content")}</label>
                              <textarea class="perc-site-framework-textarea perc-site-framework-additional-head-content">${( site.siteAdditionalHeadContent == null ) ? '' : site.siteAdditionalHeadContent}</textarea>
                            </div>
                            <div style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-textarea-container perc-site-framework-after-body-open-content-container">
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-site-framework-after-body-open-content-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@After Body Open Content")}</label>
                              <textarea class="perc-site-framework-textarea perc-site-framework-after-body-open-content">${( site.siteAfterBodyOpenContent == null ) ? '' : site.siteAfterBodyOpenContent}</textarea>
                            </div>
                            <div style="font-family: 'Open Sans',sans-serif" class="perc-site-framework-textarea-container perc-site-framework-before-body-close-content-container">
                              <label style="font-family: 'Open Sans',sans-serif" for="perc-site-framework-before-body-close-content-${site.name}">${I18N.message("perc.ui.gadgets.sitewideFramework@Before Body Close Content")}</label>
                              <textarea class="perc-site-framework-textarea perc-site-framework-before-body-close-content">${( site.siteBeforeBodyCloseContent == null ) ? '' : site.siteBeforeBodyCloseContent}</textarea>
                            </div>
                          </div>
                        </div>
                      </div>`;

        $('#percSitewideFrameworkTarget').append(gadgetHtml);

        var siteName = site.name;

        //CMS-7631 : Escape the "." character in the site name.
        if(siteName.indexOf('.') != -1){
            siteName= siteName.replace(/[^\w\s]/gi, '\\$&');
        }

        // Each container needs to have separate accordion binding
        $(`#perc-site-framework-container-`+siteName).accordion({
            active: false,
            collapsible: true
        });

    });

}

function renderSubmit() {
    submitHtml = `<div style="font-family: 'Open Sans',sans-serif" class="perc-sitewide-framework-submit-container">
                    <button id="percSitewideFrameworkSubmit" class="perc-submit-sitewide-framework btn btn-primary">${I18N.message("perc.ui.common.label@Submit")}</button>
                  </div>`;

    $('#percSitewideFrameworkTarget').append(submitHtml);

    $('#percSitewideFrameworkSubmit').on('click', function(evt){
        showSitewideWarningDialog(evt);
    });
}

function percSitewideFrameworkSubmitHandler() {
    percJQuery.PercBlockUI();
    frameworkUpdates = collectFrameworkUpdates();

    completedUpdatesDeferrred = [];
    processFrameworkUpdates(frameworkUpdates);

    Promise.all(completedUpdatesDeferrred).then(function(responses) {
        percJQuery.unblockUI();
        // check responses for errors
        errorList = [];
        $(responses).each(function(index, response) {
            if(response.status == 'error') {
                console.info(response.result);
                errorList.push(response);
            }
        });

        if(errorList.length > 0) {
            showSitewideErrorDialog(errorList);
        }
        // initiate refresh
        initiateSitewideFrameworkGadget();
    });

}

function showSitewideWarningDialog(event) {
    errorDialogHtml = `<div><h4>${I18N.message("perc.ui.gadgets.sitewideFramework@Notfication detail")}</h4><br></div>`;
    var dialog = percJQuery(errorDialogHtml).perc_dialog({
        title: I18N.message("perc.ui.gadgets.sitewideFramework@Update Warning"),
        buttons: {},
        percButtons:{
            "Close":{
                click: function(){
                    dialog.remove();
                    percSitewideFrameworkSubmitHandler();
                },
                id: "perc-site-framework-errors-close"
            }
        },
        id: "perc-site-framework-errors-dialog",
        modal: true
    });
}

function showSitewideErrorDialog(errorList) {
    errorDialogHtml = `<div><h4>${I18N.message("perc.ui.gadgets.sitewideFramework@Error detail")}</h4><br>`;
    errorDialogHtml += '<ul style="list-style-type:disc;">';
    $(errorList).each(function(index, error) {
        errorDialogHtml += `<li>${error.result}</li>`;
    });
    errorDialogHtml += '</ul></div>';
    var dialog = percJQuery(errorDialogHtml).perc_dialog({
        title: I18N.message("perc.ui.gadgets.sitewideFramework@Error title"),
        buttons: {},
        percButtons:{
            "Close":{
                click: function(){
                    dialog.remove();
                },
                id: "perc-site-framework-errors-close"
            }
        },
        id: "perc-site-framework-errors-dialog",
        modal: true
    });
}

function collectFrameworkUpdates() {
    frameworkUpdates = [];
    $('.perc-framework-contents').each(function() {
        frameworkObject = {
            SiteProperties: {}
        };
        $this = $(this);
        frameworkObject.SiteProperties = $this.data('perc-site-properties');

        var siteName1 = frameworkObject.SiteProperties.name;

        //CMS-7631 : Escape the "." character in the site name.
        if(siteName1.indexOf('.') != -1){
            siteName1= siteName1.replace(/[^\w\s]/gi, '\\$&');
        }

        frameworkObject.SiteProperties.overrideSystemFoundation = ( $(`#perc-foundation-override-`+siteName1).prop('checked')) ? true : false;
        frameworkObject.SiteProperties.overrideSystemJQuery = ( $(`#perc-jquery-override-`+siteName1).prop('checked')) ? true : false;
        frameworkObject.SiteProperties.overrideSystemJQueryUI = ( $(`#perc-jqueryui-override-`+siteName1).prop('checked')) ? true : false;
        frameworkObject.SiteProperties.siteAdditionalHeadContent = ( $this.find('.perc-site-framework-additional-head-content').val() == '' ) ? null : $this.find('.perc-site-framework-additional-head-content').val();
        frameworkObject.SiteProperties.siteAfterBodyOpenContent = ( $this.find('.perc-site-framework-after-body-open-content').val() == '' ) ? null : $this.find('.perc-site-framework-after-body-open-content').val();
        frameworkObject.SiteProperties.siteBeforeBodyCloseContent = ( $this.find('.perc-site-framework-before-body-close-content').val() == '' ) ? null : $this.find('.perc-site-framework-before-body-close-content').val();
        frameworkUpdates.push(frameworkObject);
    });

    return frameworkUpdates;

}

function processFrameworkUpdates(frameworkUpdates) {
    // Loop through each set of site properties and submit the changes individually
    $(frameworkUpdates).each(function(index, siteProps) {
        completedUpdatesDeferrred[index] = $.Deferred();
        $.PercSiteService.updateSiteProperties(siteProps, function(status, result) {
            completedUpdatesDeferrred[index].resolve({status: status, result: result});
        });
    });

}
