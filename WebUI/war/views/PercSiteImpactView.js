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

/**
 * Site Impact Renderer
 */
(function($){
    //Public API for the site impact Renderer.
    $.PercSiteImpactView = {
        renderSiteImpact: renderSiteImpact,
        ITEM_TYPE_ASSET: "asset",
        ITEM_TYPE_PAGE: "page"
    };
    
    /**
     * Renders the site impact by showing the pages, templates using the supplied item.
     * @param itemId(String), assumed to be a valid guid of the item (Page or Asset)
     * @param itemType(String), The type of the item. Use ITEM_TYPE_XXX constants.
     * @param parent dom element for which the site impact needs to be appended
     */
    function renderSiteImpact(itemId, itemType, rootElement){
        var dialog;
        var rootElem = rootElement;
        //Makes a service call and gets the site impact data, passes the _renderResults as the callback
        $.PercSiteImpactService.getSiteImpactDetails(itemId, _renderResults);
        /**
         * Creates the dialog and sets the field values from the supplied result.data object.
         * On dialog open calls the addRevisionRows to add the revision rows.
         */
        function _renderResults(status, result){
            var self = this;
            
            if (status == $.PercServiceUtils.STATUS_ERROR) {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.publish.title@Error"),
                    content: defaultMsg
                });
                return;
            }
            _createSiteImpactHtml(result.data);
            $("#perc-site-impact-pages-wrapper .perc-site-impact-item").on("click",function(){
                var currentPageId = $(this).attr("id");
                $.perc_finder().launchPagePreview(currentPageId);
            });
        }
        
        /**
         * Creates the site impact tables for pages and templates.
         */
        function _createSiteImpactHtml(siteImpactData){
            var pagesHtml = "";
            var percData = [];
            var percMenu = [];
            
            
            $.each(siteImpactData.pages, function(){
                var pageName = this.name;
                var status = this.status;
                var path = this.path.replace('/Sites/', '');
                var siteName = path.substring(0, path.indexOf('/'));
                
                var row = {
                    rowContent: [[{
                        content: pageName,
                        title: this.path
                    }], siteName, status],
                    rowData: {
                        pagePath: this.path,
                        pageId: this.id,
                        templateName: this.type,
                        pageName: this.name
                    }
                };
                var percItemMenu = {
                    title: "",
                    menuItemsAlign: "left",
                    stayInsideOf: ".perc-datatable",
                    items: [{
                        label: I18N.message("perc.ui.site.impact.view@Open In Finder"),
                        callback: _openInFinder
                    }, {
                        label: I18N.message("perc.ui.site.impact.view@Preview Page"),
                        callback: _previewPage
                    }]
                };
                percData.push(row);
                percMenu.push(percItemMenu);
            });
            
            var config = {
                percColumnWidths: ["260", "150", "75"],
                percVisibleColumns: null,
                percData: percData,
                percRowDblclickCallback: function(){},
                percRowClickCallback: function(){},
                fnDrawCallback:function(oSettings){ rootElement.find('table').removeAttr('style');},
                bPaginate: false,
                percHeaders: [I18N.message("perc.ui.site.impact.view@Page Name"), I18N.message("perc.ui.site.impact.view@Site"), I18N.message("perc.ui.site.impact.view@Status")],
                aoColumns: [{
                    sType: "string"
                }, {
                    sType: "string"
                }, {
                    sType: "string"
                }],
                percMenus: percMenu
            };
            rootElement.find(".perc-site-impact-pages").PercActionDataTable(config);
            
            
            var templatesHtml = "";
            var percTemplateData = [];
            $.each(siteImpactData.templates, function(){
                var templateName = this.template.name;
                var siteName = this.site;
                var row = {
                    rowContent: [templateName, siteName]
                };
                percTemplateData.push(row);
            });
            
            var templateConfig = {
                percColumnWidths: ["*", "300"],
                percVisibleColumns: null,
                bPaginate: false,
                fnDrawCallback:function(oSettings){ rootElement.find('table').removeAttr('style');},
                percData: percTemplateData,
                percHeaders: [I18N.message("perc.ui.site.impact.view@Template Name"), I18N.message("perc.ui.site.impact.view@Site")],
                aoColumns: [{
                    sType: "string"
                }, {
                    sType: "string"
                }],
                oLanguage: {
                    sZeroRecords: I18N.message("perc.ui.site.impact.view@No Templates Found"),
                    oPaginate: {
                        sFirst: "&lt;&lt;",
                        sPrevious: "&lt;",
                        sNext: "&gt;",
                        sLast: "&gt;&gt;"
                    },
                    sInfo: " ",
                    sInfoEmpty: " "
                }
            };
            rootElement.find(".perc-site-impact-templates").PercDataTable(templateConfig);
        }
        
        /**
         * Helper method to open the item in finder, gets the page path from the event and calls the finder's open method with that path.
         * @param {Object} event
         */
        function _openInFinder(event){
            var data = event.data;
            $.PercPathService.validatePath(data.pagePath, function(status, result){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    //validatePath return the exact caseSensitive path.                    
                    $.perc_finder().open(result.split("/"), function(){
                    })
                }
            });
        }
        
        /**
         * Helper method to launch page preview for the selected page.
         * @param {Object} event
         */
        function _previewPage(event){
            var data = event.data;
            if (data) {
                var jQuery = window.parent.jQuery;
                jQuery.perc_finder().launchPagePreviewByPath(data.pagePath, data.pageId);
            }
            else {
                alert(I18N.message("perc.ui.site.impact.view@Unable To Load Preview Page"));
            }
        }
    }

})(jQuery);
