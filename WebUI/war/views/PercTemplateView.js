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

(function($,P) {

    var TEMPLATE_CONTENT = 0;
    var TEMPLATE_LAYOUT = 1;
    var TEMPLATE_STYLE = 2;
    var isPreparedForEdit = false;

    P.templateView = function() {
        $.perc_iframe_fix($('#frame'));
        // P.headerView( $('.perc-main-div'), $("#frame") );
        var model, tab, cssController, finderIsMaximized;
        var frame  = $('#frame');

        // by default, the templates view is displayed
        // remove all scrollbars from the document in the templates view
        // they will be added back in the content, layout and style tabs
        // and then removed again when selecting the templates view
        $("body").css("overflow","hidden");

        // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
        var dirtyController = $.PercDirtyController;

        var dirty = false;
        var utils = $.perc_utils;

        // Hides the iframe when "Templates" tab is active
        function _hideIframe()
        {
            frame.contents().find("body").html("");
            frame.css("display", "none");
        }

        // A snippet to adjust the frame size on resizing the window.
        $(window).on("resize",function() {
            fixIframeHeight();
            fixBottomHeight();
        });

        // hide the iframe on loading "Design" page as Templates tab is active
        _hideIframe();

        /**
         * Setup Tabs
         */

        $("#tabs").tabs({
            active:-1,
            show : function() {
                fixBottomHeight();
            },
            beforeActivate: function( event, ui ){

                // The persist method will check to see if the editor has been
                // initialised.

                if( !gSelectTemp )
                    return false;

                // if user clicks on a tab, check if dirty status has been set
                if(dirtyController.isDirty()) {
                    // if dirty, then show a confirmation dialog
                    isPreparedForEdit = false;
                    dirtyController.confirmIfDirty(
                        function() {
                            // if they click ok, then reset dirty flag and proceed to select the tab
                            setDirty(false);
                            //Reset the JavaScript Off/On menu to JavaScript Off
                            resetJavaScriptMenu();
                            $("#tabs").tabs("option", "active", ui.newTab.index() );
                            loadTab(ui.newTab.index());
                        }
                    );
                    return false;
                }else{
                    //Reset the JavaScript Off/On menu to JavaScript Off
                    resetJavaScriptMenu()
                }
            },
            activate : function(event, ui) {
                deactivateRegionToolButton();
                loadTab(ui.newTab.index());
            }

        });

        function loadTab(index)
        {
            if (index === TEMPLATE_CONTENT)
            {
                // put back scrollbars in the content view as needed
                $("body").css("overflow","auto");
                //  Add Action dropdown menu under Content Tab (Template Editor)

                var percTemplateActions = $("#perc-dropdown-actions");
                percTemplateActions.html("");
                var actionNames = [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Edit Meta Data"), I18N.message("perc.ui.template.design.view@Edit Page")];
                var cActionStatus = [true, true, memento.isEditPage];
                percTemplateActions.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-actions",
                    percDropdownOptionLabels : actionNames,
                    percDropdownCallbacks    : [function(){}, function(){
                        $.perc_template_metadata_dialog(gSelectTemp);
                    }, function(){_openPage()}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@Actions"),I18N.message("perc.ui.template.design.view@Edit Meta Data"), I18N.message("perc.ui.template.design.view@Edit Page")],
                    percDropdownDisabledFlag : cActionStatus
                });

                //  Add View dropdown menu under Content Tab (Template Editor)
                var percTemplateView = $("#perc-dropdown-view");
                percTemplateView.html("");
                var actionNames = [I18N.message("perc.ui.template.design.view@View"), I18N.message("perc.ui.menu@JavaScript Off")];
                var disableAction = [false, true];
                percTemplateView.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-view",
                    percDropdownOptionLabels : actionNames,
                    percDropdownCallbacks    : [function(){}, function(){}],
                    percDropdownCallbackData : [I18N.message(I18N.message("perc.ui.template.design.view@View")),I18N.message("perc.ui.menu@JavaScript Off")],
                    percDropdownDisabledFlag : disableAction
                });

                model = P.templateModel( $.perc_templatemanager, gSelectTemp, setupContent);

                frame.css("display", "block");
                // enable the Metadata  button
                $(".perc-dropdown-option-EditMeta-data, .perc-dropdown-option-ViewMeta-data, #perc-metadata-button")
                    .off().perc_button()
                    .removeClass("ui-meta-pre-disabled").addClass("ui-meta-pre-enabled")
                    .on("click", function() {
                        $.perc_template_metadata_dialog(gSelectTemp);
                    });

                $("#perc-metadata-button").find("img").attr("src","/cm/images/icons/buttonMetadata.png")
                    .on("mouseenter", function(){
                        $(this).attr("src", "/cm/images/icons/buttonMetadataOver.png");
                    })
                    .on("mouseleave", function(){
                        $(this).attr("src", "/cm/images/icons/buttonMetadata.png");
                    });

                //hide preview and revisions
                $("#perc-preview-button").hide();
                $("#perc-revisions-button").hide();
                return;
            }

            if (index === TEMPLATE_LAYOUT)
            {
                // put back scrollbars in the layout view as needed
                $("body").css("overflow","auto");
                //  Add Action dropdown menu under Layout Tab (Template Editor)
                var layoutTemplateActions = $("#perc-dropdown-actions-layout");
                layoutTemplateActions.html("");
                var actionNames = [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Edit Page")];
                var lActionStatus = [true, memento.isEditPage];
                layoutTemplateActions.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-actions-layout",
                    percDropdownOptionLabels : actionNames,
                    percDropdownCallbacks    : [function(){},function(){_openPage()}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Edit Page")],
                    percDropdownDisabledFlag : lActionStatus
                });


                //  Add View dropdown menu under Layout Tab (Template Editor)
                var layoutViewDropdown = $("#perc-dropdown-view-layout");
                layoutViewDropdown.html("");
                layoutViewDropdown.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-view-layout",
                    percDropdownOptionLabels : [I18N.message("perc.ui.template.design.view@View"),I18N.message("perc.ui.menu@JavaScript Off"), I18N.message("perc.ui.template.design.view@Hide Guides")],
                    percDropdownCallbacks    : [function(){}, function(){}, function(){}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@View"),I18N.message("perc.ui.menu@JavaScript Off"), I18N.message("perc.ui.template.design.view@Hide Guides")],
                    percDropdownDisabledFlag : [false, true, true]
                });

                var percTemplateHelpLayoutMenu = true;
                $.PercTemplateService().checkImportLogExists(gSelectTemp, function(status, results)
                {
                    if(status == $.PercServiceUtils.STATUS_ERROR)
                        percTemplateHelpLayoutMenu = false;
                });

                var percTemplateHelpLayout = $("#perc-dropdown-help-layout");
                percTemplateHelpLayout.html("");
                var actionNamesHelp = [I18N.message("perc.ui.template.design.view@Help"), I18N.message("perc.ui.template.design.view@Download Report Log"), I18N.message("perc.ui.template.design.view@Video Tutorials"), I18N.message("perc.ui.template.design.view@Import FAQs"), I18N.message("perc.ui.template.design.view@Percussion Community"), I18N.message("perc.ui.template.design.view@More Help")];
                var disableActionsHelp = [false, percTemplateHelpLayoutMenu, true, true, true, true];
                percTemplateHelpLayout.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-help",
                    percDropdownOptionLabels : actionNamesHelp,
                    percDropdownCallbacks    : [function(){}, function(){viewImportLog()},
                        showVideoTutorialWindow,
                        function(){openUrl("ImportFAQs", "https://help.percussion.com/in-product/import-faqs")},
                        function(){openUrl("PercussionCommunity", "https://community.percussion.com")},
                        function(){openUrl("MoreHelp", "https://help.percussion.com")}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@Help"),I18N.message("perc.ui.template.design.view@Download Report Log"), I18N.message("perc.ui.template.design.view@Video Tutorials"), I18N.message("perc.ui.template.design.view@Import FAQs"), I18N.message("perc.ui.template.design.view@Percussion Community"), I18N.message("perc.ui.template.design.view@More Help")],
                    percDropdownDisabledFlag : disableActionsHelp
                });

                model = P.templateModel( $.perc_templatemanager, gSelectTemp, setupLayout);
                frame.css("display", "block");

                $("#perc-error-alert").css("visibility", "hidden");
                return;


            }
            if (index === TEMPLATE_STYLE)
            {

                // put back scrollbars in the style view as needed
                $("body").css("overflow","auto");

                //  Add Action dropdown menu under Style Tab (Template Editor)
                var styleTemplateActions = $("#perc-dropdown-actions-style");
                styleTemplateActions.html("");
                var sactionNames = [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Edit Page")];
                var sActionStatus = [true, memento.isEditPage];
                styleTemplateActions.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-actions-style",
                    percDropdownOptionLabels : sactionNames,
                    percDropdownCallbacks    : [function(){},function(){_openPage()}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Edit Page")],
                    percDropdownDisabledFlag : sActionStatus
                });

                //  Add View dropdown menu under Style Tab (Template Editor)
                var styleViewDropdown = $("#perc-dropdown-view-style");
                styleViewDropdown.html("");
                styleViewDropdown.PercDropdown({
                    percDropdownRootClass    : "perc-dropdown-view-style",
                    percDropdownOptionLabels : [I18N.message("perc.ui.template.design.view@View"),I18N.message("perc.ui.menu@JavaScript Off")],
                    percDropdownCallbacks    : [function(){}, function(){}],
                    percDropdownCallbackData : [I18N.message("perc.ui.template.design.view@View"),I18N.message("perc.ui.menu@JavaScript Off")],
                    percDropdownDisabledFlag : [false, true]
                });

                model = P.templateModel( $.perc_templatemanager, gSelectTemp, setupCSS);
                frame.css("display","block");
                return;
            }

        }

        function showVideoTutorialWindow() {
            var dialogMarkup = $('<div/>');
            // Initialize the dialog markup and instantiate the perc_dialog plugin
            dialogMarkup.append(
                $('<div style="height: 400px; overflow:hidden; margin-left: 68px; margin-right: 92px">')
                    .append(
                        $('<iframe id="perc_iframe_video" src="//help.percussion.com/percussion-cm1/overview/introduction-to-the-ui/" scrolling="yes" marginheight="0" marginwidth="0" frameborder="0" width="480px" height="0px" style="display: block; overflow: hidden">')
                    )
                    .append(
                        $('<img id="perc_notfound_image" src="../images/images/VideoTutorialNotFound.png" width="480px" height="0px">')
                    )
            )
            //Used a random dummy parameter to avoid cache
            dialogMarkup.append(
                $('<img height="0px" width="0px" src="//help.percussion.com/Assets/Help/header/images/PercussionSwoosh.png?dummy=' + Math.random() + '">')
                    .on("error", handleUnreachableURL)
                    .on("load", showVideoIframe)
            )

            var dialogButtons = {
                "Close Normal": {
                    id: "perc_video_tutorial_close",
                    click: function()
                    {
                        dialog.remove();
                    }
                }
            };

            var dialogOptions = {
                id: "perc_video_tutorial_dialog",
                title: "Video Tutorial",
                modal: true,
                resizable: false,
                closeOnEscape: false,
                width: 'auto',
                height: 'auto',
                percButtons: dialogButtons
            };

            dialog = $(dialogMarkup).perc_dialog(dialogOptions);
        }

        function showVideoIframe(){
            $("#perc_video_tutorial_dialog").find("#perc_iframe_video").attr("height", "400px");
        }

        function handleUnreachableURL(){
            $("#perc_video_tutorial_dialog").find("#perc_notfound_image").attr("height", "400px");
        }

        // A secondary bit for the style tabs. This was moved here in order to move the model ,controller, and view classes into the same
        // scope, from its original place in jsp/template_style.jsp
        $("#perc-styleTabs").tabs({
            collapsible: true,
            active: 0,       // We do not want any tabs to be selected, by default.
            activate: function(event, ui)
            {
                // Be sure to persist changes in current tab into template model between tab changes.
                cssController.updateTemplateObject();
                var self = $(this);
                currentTabIndex = self.tabs('option','active');
                switch(currentTabIndex)
                {
                    case 0:
                        // CSS Gallery
                        cssController.showGalleryView();
                        break;
                    case 1:
                        // CSS Theme Editor
                        cssController.showThemeView();
                        break;
                    case 2:
                        // CSS Override Editor
                        cssController.showCSSOverrideView();
                        break;
                }
            }
        });

        /**
         * Bind the click event to Edit Page menu drop down entry. The click opens the page in edit mode.
         */
        function _openPage() {
            $.PercPathService.getPathItemById(memento.pageId, function(status, data){
                if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                    clearCacheRegionCSS();
                    var querystring = $.deparam.querystring();
                    var isEditMode = true;
                    var folderPath = "";
                    if(typeof data.PathItem.folderPaths === 'undefined' ){
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.recycledPage@RecycledPage"), content: I18N.message("perc.ui.recycledPageWarning@RecycledPage")});
                        return;
                    }
                    if(Array.isArray(data.PathItem.folderPaths)){
                        folderPath = data.PathItem.folderPaths[0];
                    }else{
                        folderPath = data.PathItem.folderPaths;
                    }
                    data.PathItem.path = folderPath.replace('//', '/') + '/' + data.PathItem.name;
                    $.PercNavigationManager.handleOpenPage(data.PathItem, isEditMode);
                } else {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                }
            });
        }

        /**
         * Resets the text of the JavaScript menu to JavaScript Off.
         */
        function resetJavaScriptMenu()
        {
            //Reset the JavaScript Off/On menu to JavaScript Off
            $(".perc-dropdown-option-DisableJavaScript").text(I18N.message( "perc.ui.menu@JavaScript Off" ));
        }
        /**
         * Handle cancel button from the Style tab
         * TODO: should all cancel events be handled the same way?
         * so that we should use a .perc-cancel and trigger the same behavior?
         */
        $("#perc-css-editor-cancel").on("click",function() {
            model.load();
            dirtyController.setDirty(false, "template");
        });

        $("#perc-css-editor-save").on("click",function() {

            cssController.setOverrideCSS();

            cssController.save(function (status, data) {
                if (status === true) {
                    dirtyController.setDirty(false, "template");
                }
            });
        });

        $("#perc-wid-lib-expander").on("click",function(){
            $.fn.percWidLibMaximizer(P);
        });

        function setupContent() {
            fixIframeHeight();
            if($.PercNavigationManager.isJavascriptOff())
                model.setJavaScriptOff(true);
            P.contentView( $("#frame"), model);
            _updateLabelAndName(model);
            prepareForEditRegionCSS();
        }

        function setupLayout() {

            if(model.isResponsiveBaseTemplate())
            {
                $("#region-tool, #region-tool-help").css("visibility", "visible");
                //Hide the region inspector tools for responsive templates.
                $("#perc-region-tool-inspector, #perc-region-tool-menu").css("visibility", "hidden");
            }
            else
            {
                // show region inspector tool for base templates
                $("#region-tool, #region-tool-help, #perc-region-tool-inspector, #perc-region-tool-menu").css("visibility", "visible");
            }
            // There is no need to pass the layoutController or the sizeController from here
            // or the frame, they can all be instantiated from within the layout view
            // TODO: remove frame, layout controller and size controller from method signature
            // cant do it just yet because layout view is shared among template model and page model
            // for now pass size controller null
            var layoutController = P.layoutController( model );
            P.layoutView( $("#frame"), model, layoutController, null, function(isDirty){
                setDirty(isDirty);
            });
            fixIframeHeight();
            if($.PercNavigationManager.isJavascriptOff())
                model.setJavaScriptOff(true);


            $("#region-tool").draggable({
                helper: 'clone',
                scope:  $.perc_iframe_scope,
                scroll: true,
                containment: 'window',
                iframeFix: true,
                delay:$.dragDelay,
                refreshPositions: true
            });
            $("#w1").data("widget", {definitionId: 'widgetType' } ).draggable({});
            _updateLabelAndName(model);
            prepareForEditRegionCSS();
        }
        function setDirty(isDirty) {
            dirtyController.setDirty(isDirty, "template");
            dirty = isDirty;
        }

        function setupCSS()
        {
            if($.PercNavigationManager.isJavascriptOff())
                model.setJavaScriptOff(true);
            cssController = P.cssController( model, $("#frame"), P.CSSPreviewView( $("#frame"), model) );
            cssController.refreshCssViews();

            fixIframeHeight();
            _updateLabelAndName(model);
            prepareForEditRegionCSS();
        }
        function _updateLabelAndName(model){
            $('.perc-template-name-text').html('').text(model.getName());
            if(model.isResponsiveBaseTemplate()){
                $('.perc-template-name-label').html('').text(I18N.message("perc.ui.edit.template@Editing Responsive Template"));
                $('.perc-template-details').attr("type","template-responsive");
            }
            else{
                $('.perc-template-name-label').html('').text(I18N.message("perc.ui.edit.template@Editing Template"));
                $('.perc-template-details').attr("type","template-base");
            }
        }

        //Fixing Iframe size when clicking on sub-tabs under Style tab.
        $(".perc-style-sub-tab").on("click",function(){
            fixIframeHeight();
        });

        function log( str ) {
            return function() {
                var args = $.map( arguments, function(x){ return x; } );
                args.unshift( str );
                console.log.apply( console, args );
            };
        }

        /**
         * Method to deactivate the RegionTool button
         */
        function deactivateRegionToolButton() {
            var inspectorButton = $("#perc-region-tool-inspector");
            inspectorButton.removeClass('buttonPressed');
            //Enable the menu and remove the overaly div on switching between tabs
            $("#perc-layout-menu .perc-lib-expander-div, #perc-layout-menu #perc-dropdown-actions-layout, #perc-layout-menu #perc-dropdown-view-layout, #perc-region-library-maximizer, #perc-wid-lib-maximizer").removeClass('perc-disable-menu-item');
            $('.perc-overlay-div, #region-tool-disabled, #perc-undo-tool, #perc-undo-tool-disabled').hide();
            $("#region-tool").show();
        }

        function viewImportLog()
        {
            var url = $.perc_paths.VIEW_IMPORT_LOG + "?templateId=" + gSelectTemp;
            openUrl("perc_dropdown_option_DownloadReportLog", url);
        }

        function openUrl(buttonSuffix, url)
        {
            window.open(url, "perc_dropdown_option_" + buttonSuffix);
        }

        function clearCacheRegionCSS()
        {
            $.PercTemplateService().regionCSSClearCache(
                model.getTemplateObj().Template.theme,
                model.getTemplateObj().Template.name,
                function(status, data) {});
        }
        function prepareForEditRegionCSS()
        {
            if (!isPreparedForEdit)
            {
                if (model.isTemplate())
                {
                    $.PercTemplateService().regionCSSPrepareForEdit(
                        model.getTemplateObj().Template.theme,
                        model.getTemplateObj().Template.name,
                        function(status, data) {
                            isPreparedForEdit = true;
                        });
                }
            }
        }

        fixTemplateHeight();
    };
})(jQuery, jQuery.Percussion);
