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

(function($, P)
{
    var VIEW_MODE_THUMBNAILS = 0;
    var VIEW_MODE_CATALOG = 1;
    var UNASSIGNED_MAX_RESULTS = 10;
    var intervalId = 0;
    
    P.templateDesignView = function()
    {
        var frame = $('#frame');
        $.perc_iframe_fix(frame);
        var model, tab, cssController, finderIsMaximized;



        // by default, the templates view is displayed
        // remove all scrollbars from the document in the templates view
        // they will be added back in the content, layout and style tabs
        // and then removed again when selecting the templates view
        $("body").css("overflow", "hidden");

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
        $(window).on("resize",function()
        {
            fixIframeHeight();
            fixBottomHeight();
        });

        // hide the iframe on loading "Design" page as Templates tab is active
        _hideIframe();

        //Snippet for displaying the inline help when no site is selected @Story 99
        var inline_help = $("#perc-pageEditor-menu-name").text();

        if(inline_help === "")
        {
            $("#perc-site-templates-label").hide();
            $("#perc-site-templates-inline-help").show();
            $("#perc-temp-lib-expander").removeClass("expander-enabled");
            $("#perc-temp-lib-expander").addClass("expander-disabled");
            $("#perc-temp-lib-maximizer").replaceWith('<a id="perc-temp-lib-disabled" style="float: left;" href="#"></a>');
        }
        else
        {
            $("#perc-site-templates-label").show();
            $("#perc-site-templates-inline-help").hide();
            $("#perc-temp-lib-expander").removeClass("expander-disabled");
            $("#perc-temp-lib-expander").addClass("expander-enabled");
            $("#perc-temp-lib-disabled").replaceWith('<a id="perc-temp-lib-maximizer" style="float: left;" href="#"></a>');
        }

        //Add Action dropdown menu in toolbar
        var siteName = $.PercNavigationManager.getSiteName();
        var isEnable = false;
        if(typeof siteName == 'undefined')
        {
            isEnable = false;
        }
        else
        {
            isEnable = true;
        }

        var percTemplateActions = $("#perc-dropdown-actions");
        percTemplateActions.html("");
        var actionNames = [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Create Template From Page"), I18N.message("perc.ui.template.design.view@Add Template"), I18N.message("perc.ui.template.design.view@Export Template"), I18N.message("perc.ui.template.design.view@Import Template")];
        var disableAction = [true, isEnable, isEnable, false, isEnable];
        percTemplateActions.PercDropdown(
        {
            percDropdownRootClass: "perc-dropdown-template-action",
            percDropdownOptionLabels: actionNames,
            percDropdownCallbacks: [
                function() {},
                _createTemplateFromPage,
                _addTemplate,
                function() {},
                _importTemplate
            ],
            percDropdownCallbackData: [I18N.message("perc.ui.template.design.view@Actions"), I18N.message("perc.ui.template.design.view@Create Template From Page"), I18N.message("perc.ui.template.design.view@Add Template"), I18N.message("perc.ui.template.design.view@Export Template"), I18N.message("perc.ui.template.design.view@Import Template")],
            percDropdownDisabledFlag: disableAction
        });
        
        /*Work around for issue with PercDropdown.js
         * dropdownOptionItem
                .data("callback", callbacks[k])
                .data("callbackData", callbackData[k])
                
                Should be passed l, not k
         */ 
        $(".perc-dropdown-option-CreateTemplatefromPage").addClass("perc-drop-disabled").css("color", "#9FA3AA");

        //Add View dropdown menu in toolbar
        _createViewMenu(VIEW_MODE_CATALOG);

        //Setup template gallery.
        $("#tabs").tabs(
        {
            disabled: [1, 2, 3],
            show: function()
            {
                fixBottomHeight();
            },
            select: function(event, ui)
            {
                // The persist method will check to see if the editor has been
                // initialised.
                if(!gSelectTemp) return false;

                var self = $(this);

                _hideIframe();

                // remove all scrollbars in the templates view
                $("body").css("overflow", "hidden");

                return;
            }
        });

        // Publish accesor methods view operations of the design manager
        this.getCurrentTemplatesView = getCurrentView;
        this.setCurrentTemplatesView = setCurrentView;

        // Publish constants for views of the design manager
        this.VIEW_MODE_THUMBNAILS = VIEW_MODE_THUMBNAILS;
        this.VIEW_MODE_CATALOG = VIEW_MODE_CATALOG;

        //Initialize the unassigned panel.
        initializeUnassignedPanel();
        
        function setPanelPreference(showPanel, pageNumber){
            var siteName = $.PercNavigationManager.getSiteName();
            var panelSettings = JSON.parse($.cookie("perc-unassigned-panel-" + siteName + "-settings"));
            if (panelSettings != null){
                if(showPanel != null && typeof(showPanel) != "undefined")
                    panelSettings.showPanel = showPanel;
                if(pageNumber != null && typeof(pageNumber) != "undefined")
                    panelSettings.pageNumber = pageNumber;
            }
            else{
               panelSettings = {"showPanel":showPanel, "pageNumber":pageNumber};
            }
            $.cookie("perc-unassigned-panel-" + siteName + "-settings", JSON.stringify(panelSettings));
        }
        
        function initializeUnassignedPanel(){
            var panelExpanderIcon = $(".perc-panel-expander-icon");
            var panel = $(".perc-unassigned-panel");
            panelExpanderIcon.on("click", function(evt){
                if (typeof($.PercNavigationManager.getSiteName()) != "undefined"){
                    setPanelPreference(panelExpanderIcon.is(".perc-collapsed"));
                    if (panelExpanderIcon.is(".perc-collapsed")){
                        panel.removeClass("perc-closed").addClass("perc-opened");
                        panel.show("slide",{},500,function(){
                            $("#perc-assigned-templates").template_selected('updateTemplatesPagingSize');
                        });
                        panelExpanderIcon.removeClass("perc-collapsed").addClass("perc-expanded");
                        requestUnassignedPages();
                        intervalId = setInterval(requestUnassignedPages, 10000);
                    }
                    else{
                        panel.removeClass("perc-opened").addClass("perc-closed");                    
                        panel.hide("slide",{},500,function(){
                            $("#perc-assigned-templates").template_selected('updateTemplatesPagingSize');
                        });
                        panelExpanderIcon.removeClass("perc-expanded").addClass("perc-collapsed");
                        window.clearInterval(intervalId);
                        setPanelPreference(false);
                    }
                }
            });
            
            var siteName = $.PercNavigationManager.getSiteName();
            if (typeof(siteName) != "undefined"){
                $.PercPageService.getUnassignedPagesBySite(siteName, 1, UNASSIGNED_MAX_RESULTS, function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        var panelSettings = JSON.parse($.cookie("perc-unassigned-panel-" + $.PercNavigationManager.getSiteName() + "-settings"));
                        var showPanel = false;
                        var pageNumber = 1;
                        if (panelSettings != null){
                            showPanel = panelSettings.showPanel;
                            pageNumber = panelSettings.pageNumber;
                        }
                        else{
                            var unassignedResult = result.UnassignedResults;
                            if (typeof(unassignedResult.unassignedItemList.childrenInPage) != "undefined")
                                showPanel = unassignedResult.unassignedItemList.childrenInPage.length > 0;
                        }
                        if (showPanel){
                            panel.removeClass("perc-closed").addClass("perc-opened");
                            panel.show("slide",{},500,function(){});
                            panelExpanderIcon.removeClass("perc-collapsed").addClass("perc-expanded");
                            requestUnassignedPages(pageNumber);
                            intervalId = setInterval(requestUnassignedPages, 10000);
                        }
                    }
                });
            }
            else{
                panelExpanderIcon.addClass("perc-disabled");
            }
            
            //Set a filter to each of the paging selectors to allow only digits.
            var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
            $.perc_filterField(percJump, $.perc_textFilters.ONLY_DIGITS);
            
            // Pagination controls - Previous button
            $('.perc-unassigned-panel .perc-template-pages-controls .previous').on("click",
                function(evt){
                    unassignedPreviousClick(evt);
                });
            
            // Pagination controls - Next button - Click
            $('.perc-unassigned-panel .perc-template-pages-controls .next').on("click",
                function(evt){
                    unassignedNextClick(evt);
                });
            
             // Pagination controls - Text input for page selector
            $('.perc-unassigned-panel .perc-template-pages-controls').on("submit",function()
            {
                requestUnassignedPages(parseInt(percJump.val()));
                return false;
            });
        }
        
        function unassignedPreviousClick(event){
            var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
            requestUnassignedPages(parseInt(percJump.val())-1);
        }
        
        function unassignedNextClick(event){
            var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
            requestUnassignedPages(parseInt(percJump.val())+1);
        }
        
        function requestUnassignedPages(pageNumber){
            if (typeof(pageNumber) == "undefined"){
                //Use the current pageNumber
                var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
                 pageNumber = percJump.val()!=""? parseInt(percJump.val()): 1;
            }
            pageNumber = (pageNumber!=0)? pageNumber : 1;
            setPanelPreference(null, pageNumber);
            var startIndex = (((pageNumber-1) * UNASSIGNED_MAX_RESULTS) + 1);
            var siteName = $.PercNavigationManager.getSiteName();
            
            if (typeof(siteName) != "undefined"){
                $.PercPageService.getUnassignedPagesBySite(siteName, startIndex, UNASSIGNED_MAX_RESULTS, function(status, result){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS){
                        var unassignedResult = result.UnassignedResults;
                        updateProgressBar(unassignedResult.importStatus);
                        drawUnassignedPages(unassignedResult.unassignedItemList);
                        var totalPages = unassignedResult.importStatus.catalogedPageCount + unassignedResult.importStatus.importedPageCount;
                        var childrenInPage = 0;
                        if (typeof(unassignedResult.unassignedItemList.childrenInPage) != "undefined")
                            childrenInPage = $.perc_utils.convertCXFArray(unassignedResult.unassignedItemList.childrenInPage).length;
                        updatePaging(unassignedResult.unassignedItemList.startIndex, childrenInPage, totalPages);
                    }
                    else{}
                });
            }
        }
        
        function updatePaging(startIndex, childrenCount, totalItems){
            var panel = $(".perc-unassigned-panel");
            var pageRange = panel.find(".perc-panel-page-group-range");
            var pageTotal = panel.find(".perc-panel-total");
            var pageJump = panel.find(".perc-jump");
            if (childrenCount > 0)
                pageRange.text(startIndex + "-" + (startIndex+childrenCount-1));
            else
                pageRange.text(0);
            pageTotal.text(totalItems);
            var pageNumber = Math.floor((startIndex-1)/UNASSIGNED_MAX_RESULTS) + 1;
            pageJump.val(pageNumber);
            if (pageNumber <= 1){
                panel.find(".previous")
                    .removeClass('previous')
                    .addClass('previous-disabled')
                    .off('click');
            }
            else {
                panel.find(".previous-disabled")
                    .removeClass('previous-disabled')
                    .addClass('previous')
                    .off('click')
                    .on("click",
                        function(evt){
                            unassignedPreviousClick(evt);
                        });
            }
            
            var endIndex = startIndex + UNASSIGNED_MAX_RESULTS - 1;
            if(endIndex >= totalItems){
                panel.find(".next")
                    .removeClass('next')
                    .addClass('next-disabled')
                    .off('click');
            }
            else {
                panel.find(".next-disabled")
                    .removeClass('next-disabled')
                    .addClass('next')
                    .off('click')
                    .on("click", function(evt){
                            unassignedNextClick(evt);
                    });
            }
        }
        
        function updateProgressBar(importStatus){
            var progressPanel = $(".perc-panel-progress");
            var progressFinishedMessage = progressPanel.find(".perc-progress-finished");
            var progressContainer = progressPanel.find(".perc-progress-bar-container");
            var progressBar = progressContainer.find(".perc-progress-bar");
            var progressMessage = progressPanel.find(".perc-progress-message");
            var progressWidth = 0;
            var totalPages = importStatus.catalogedPageCount + importStatus.importedPageCount;
            if (totalPages > 0)
                progressWidth = ((importStatus.importedPageCount * 100) / totalPages);
            if (progressWidth == 100){
                progressMessage.show();
                progressMessage.css("margin-left", "3px").text("Congratulations.");
                progressContainer.hide();
                progressFinishedMessage.show();
                window.clearInterval(intervalId);
            }
            else{
                var message = I18N.message("perc.ui.template.design.view@Imported") + importStatus.importedPageCount + I18N.message("perc.ui.template.design.view@Of") + totalPages + I18N.message("perc.ui.template.design.view@Cataloged Pages");
                // 34 max character can show the progress container at 11px font-size.
                if (message.length > 34)
                    message = message.replace(" cataloged pages", "");
                
                progressMessage.text(message);
                progressFinishedMessage.hide();
                progressContainer.show();
                if (totalPages == 0)
                    progressContainer.css("background-color", "#D7D7D9");
                else
                    progressContainer.css("background-color", "#E6E6E9");
            }
            progressBar.css("width", progressWidth + "%");
        }

        function drawUnassignedPages(unassignedItemList){
            var panel = $(".perc-unassigned-panel");
            //This panel gets reloaded every 10 seconds, capture the previous selection and reapply
            var selectedPageId = panel.find(".perc-imported-page-selected").length === 1? panel.find(".perc-imported-page-selected").attr("id"):null;
            var pageContainer = panel.find(".perc-panel-pages-list");
            pageContainer.empty();
            var pageList = $("<ul/>");
            pageContainer.append(pageList);
            var childrenInPage = $.perc_utils.convertCXFArray(unassignedItemList.childrenInPage);
            for (var i=0; i < 10; i++){
                var page = {id:"",path:"",name:"",status:"blankSlot"};
                if(typeof(childrenInPage) != "undefined" && childrenInPage[i])
                    page = childrenInPage[i];
                var pageObj = $("<li/>")
                                .attr("id", page.id)
                                .attr("title", page.path)
                                //.append(
                                //    $("<img/>").addClass("perc-left-img")
                                //)
                                .append(
                                    $("<img/>").addClass("perc-left-img")
                                        .attr("src","../images/images/inspectButton.png")
                                        .attr("data", JSON.stringify(page))
                                )
                                .append(
                                    $("<div/>").text(page.name)
                                )
                                .append(
                                    $("<span/>").addClass("perc-imported-page-dropdown")
                                )
                                ;
                
                pageObj.find(".perc-left-img").PercImageTooltip();
                pageObj.find(".perc-imported-page-dropdown").PercDropdown(
                                        {
                                            percDropdownRootClass: "perc-imported-page-dropdown-list",
                                            percDropdownOptionLabels: ["", "Open Page", "Change Template"],
                                            percDropdownCallbacks: [function() {}, 
                                                _openThisPage,
                                                _changeTemplate
                                            ],
                                            percDropdownCallbackData:['', {'pageId': page.id, 'pagePath':page.path}, {'pageId': page.id}]
                                    });
                
                switch (page.status){
                    case "Imported":
                        pageObj.addClass("perc-imported-page").on("click", function(evt){
                            $(".perc-imported-page-selected").removeClass("perc-imported-page-selected");
                            $(this).addClass("perc-imported-page-selected");
                            var createTplPageMenuEntry = $(".perc-dropdown-option-CreateTemplatefromPage");
                            createTplPageMenuEntry.removeClass('perc-drop-disabled').attr('style', '');
                        })
                        //.find(".perc-left-img").attr("src","/Rhythmyx/sys_resources/images/finderPage.png")
                        ;
                        pageObj.find(".perc-imported-page-dropdown").show();
                        break;
                    case "Importing":
                        pageObj.find(".perc-left-img").attr("src","/Rhythmyx/sys_resources/images/running.gif");
                        pageObj.find(".perc-imported-page-dropdown").hide();
                        pageObj.prop("disabled", true);
                        break;
                    case "Cataloged":
                        pageObj.find(".perc-left-img").attr("src","/Rhythmyx/sys_resources/images/iconSpider.png");
                        pageObj.find(".perc-imported-page-dropdown").hide();
                        pageObj.addClass("perc-page-disabled");
                        break;
                    case "blankSlot":
                        pageObj.empty();
                        break;
                }
                pageList.append(pageObj);
            }
            // Make the Unassigned page draggable
            /*$('.perc-imported-page').draggable({ opacity: 0.7,
                helper: "clone",
                revert: "invalid", // when not dropped, the item will revert back to its initial position
                containment: "document",
                cursor: "move",
                drag: function(event,ui){ 
                    ui.helper.dropCallback = function(){
                        var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
                        requestUnassignedPages(parseInt(percJump.val()));
                        $(".perc-dropdown-option-CreateTemplatefromPage").addClass("perc-drop-disabled").css("color", "#9FA3AA");
                        
                    };
                //update the zindex value here
                $(ui.helper).css("z-index",'900000000');
                } 
            });*/
            //Reapply the selection
            if(selectedPageId)
            {
                panel.find(".perc-imported-page[id='"+ selectedPageId +"']").addClass("perc-imported-page-selected");
            }
        }
        
        function _openThisPage(opts) {
            $.PercNavigationManager.openPage(opts.pagePath, true);
        }
        
        function _changeTemplate(opts) {
            $.PercChangeTemplateDialog().openDialog(opts.pageId, '', $.PercNavigationManager.getSiteName(), function() { window.location.reload(); });
        }
        
        /**
         * Returns the current template design manager view by checking the following class
         * "perc-templates-detailed" presence
         *
         * @return one of the following 2 constants values:
         * $.Percussion.VIEW_MODE_CATALOG or $.Percussion.VIEW_MODE_THUMBNAILS
         */
        function getCurrentView()
        {
            if($('#perc-activated-templates').hasClass('perc-templates-detailed')) return VIEW_MODE_CATALOG;
            else return VIEW_MODE_THUMBNAILS;
        }

        /**
         * Sets the current view of template design manager. It basically adds / removes the
         * corresponding css class to check for that property and calls the views transformations
         * needed
         *
         * @param view. The current mode of the view. Should be
         * one of the two constants included in this class:
         * $.Percussion.VIEW_MODE_CATALOG or $.Percussion.VIEW_MODE_THUMBNAILS
         */
        function setCurrentView(view)
        {
            if(view == VIEW_MODE_THUMBNAILS)
            {
                $('#perc-activated-templates').removeClass('perc-templates-detailed');
                _switchToThumbnailView();
            }
            else if(view == VIEW_MODE_CATALOG)
            {
                $('#perc-activated-templates').addClass('perc-templates-detailed');
                _switchToCatalogView();
            }
        }

        /**
         * Import the template.
         */
        function _importTemplate()
        {
            createDialog();
        }

        /**
         * Add template.
         */
        function _addTemplate()
        {
            // Instance the "Add template" dialog.
            // The newTemplateName argument in the callback function will be bound
            // to the new template name
            $.PercAddTemplateDialog(function refreshAndFocusNewTemplate(newTemplateName)
            {
                if (newTemplateName !== undefined && newTemplateName !== '')
                {
                    // Invoke the jQuery widget method refresh passing the (optional) template
                    // name to fouse is after refreshing the panel
                    $("#perc-assigned-templates").template_selected('refresh', newTemplateName);
                }
            });
        }
        
        /**
         * Creates the template from the selcted page from the imported pages.
         */
        function _createTemplateFromPage()
        {
            $.PercBlockUI();
            if ($(".perc-dropdown-option-CreateTemplatefromPage").hasClass('perc-drop-disabled')) {
                $.unblockUI();
                return;
            }    
            var pageId = $(".perc-imported-page-selected").attr("id");
            if(!pageId)
            {
                $.perc_utils.alert_dialog({title:I18N.message("perc.ui.page.general@Warning"),content:I18N.message("perc.ui.template.design.view@Select Imported Page")});
                $.unblockUI();
            }
            else
            {
                $.PercWorkflowController().checkOut("percPage",  pageId, function(status)
                {
                   if(status)//Workflow controller presents the appropriate error message to the user if fails to check out.
                   {
                       $.PercTemplateService().createTemplateFromPage(pageId, $.PercNavigationManager.getSiteName(), function(status, data){
                            if(!status)
                            {
                                $.perc_utils.alert_dialog({title:I18N.message("perc.ui.publish.title@Error"),content:data.errorMessage});
                            }
                            else
                            {
                                var percJump = $(".perc-unassigned-panel .perc-template-pages-controls .perc-jump");
                                requestUnassignedPages(parseInt(percJump.val()));
                                
                                    if (data.name !== undefined && data.name !== '')
                                    {
                                        _cleanMementoView();
                                        // Invoke the jQuery widget method refresh passing the (optional) template
                                        // name to fouse is after refreshing the panel
                                        //This works, but it is seriously cheesy.
                                        $.cookie("templateImport", data.name);
                                        $.PercBlockUI();
                                        location.reload();
                                        //$("#perc-assigned-templates").template_selected('refresh', data.name);
                                    }
                            }
                       });
                   }               
                });                        
            }
        }
        
        /**
         * Creates "View" menu. The menu has a toggle option that allows the
         * user to switch between "Catalog" and "Thumbnails" modes. When the
         * user is in Thumbnails mode, the menu option will be "Catalog" and
         * clicking it will switch to Catalog mode. When the user is in
         * Catalog mode, the menu option will be "Thumbnails" and clicking
         * it will switch to Thumbnails mode.
         * @param currentMode. The current mode of the view.
         */
        function _createViewMenu(currentMode)
        {
            var viewNames = [];
            var disableView = [];
            var callbackFunctions = [];
            var callbackData = [];

            var siteName = $.PercNavigationManager.getSiteName();

            if(currentMode == VIEW_MODE_CATALOG)
            {
                viewNames = [I18N.message("perc.ui.template.design.view@View"), I18N.message("perc.ui.template.design.view@Thumbnails")];
                disableView = [true, true];
                callbackFunctions = [function()
                {},
                _switchToThumbnailView];
                callbackData = [I18N.message("perc.ui.template.design.view@View"), I18N.message("perc.ui.template.design.view@Thumbnails")];
            }
            else if(currentMode == VIEW_MODE_THUMBNAILS)
            {
                viewNames = [I18N.message("perc.ui.template.design.view@View"), I18N.message("perc.ui.template.design.view@Catalog")];
                disableView = [true, true];
                callbackFunctions = [function()
                {},
                _switchToCatalogView];
                callbackData = [I18N.message("perc.ui.template.design.view@View"), I18N.message("perc.ui.template.design.view@Catalog")];
            }

            if(siteName == null || siteName == "")
            {
                disableView[1] = false;
            }

            var percTemplateView = $("#perc-dropdown-view");
            percTemplateView.html("");

            percTemplateView.PercDropdown(
            {
                percDropdownRootClass: "perc-dropdown-template-view",
                percDropdownOptionLabels: viewNames,
                percDropdownCallbacks: callbackFunctions,
                percDropdownCallbackData: callbackData,
                percDropdownDisabledFlag: disableView
            });
        }

        /**
         * Cleans the view property from the PercNavigation memento object
         */
        function _cleanMementoView()
        {
            var memento;
            memento = $.PercNavigationManager.getMemento();
            memento.view = null;
        }

        /**
         * Shows basic information of templates.
         * Displays a wraped list of thumbnails.
         */
        function _switchToThumbnailView()
        {
            // the memento view property should be cleaned out when switching views
            // or else when performing reloading operations (like adding new template)
            // it could cause a problem with the current view
            _cleanMementoView();

            // remove inherited background color
            $(".perc-templates-layout").css("background-color", "#FFFFFF");

            // Remove carousel navigation arrows
            $(".perc-prev-templates-carousel").css("display", "none");
            $(".perc-next-templates-carousel").css("display", "none");

            $("#perc-assigned-templates").width('auto').height('auto');
            // Scroll to the begining
            $("#perc-assigned-templates").data("scrollable").begin();
            $(".perc-items").width('auto');

            $(".perc-template-item").css("margin-bottom", "20px");

            // Hide all paging from template containers
            $(".perc-template-item .perc-template-paging-container").hide();

            $(".perc-template-item-middle-droppable").height("212px");


            $("#perc-activated-templates").removeClass('perc-templates-detailed');
            //Create view menu again, to show "Catalog" option.
            _createViewMenu(VIEW_MODE_THUMBNAILS);
            fixIframeHeight();
            // Auto-scroll to selected item
            $('#perc-activated-templates-scrollable').trigger('autoscroll');
            
            //close and Hide the unasignedPanel
            closeAndHideUnassignedPanel();
        }

        function closeAndHideUnassignedPanel(){
            var container = $("#perc-template-view-container");
            var panelExpander = $(".perc-unassigned-panel-expander");
            var panelExpanderIcon = $(".perc-panel-expander-icon");
            var panel = $(".perc-unassigned-panel");
            if (panelExpanderIcon.is(".perc-expanded")){
                panel.removeClass("perc-opened").addClass("perc-closed");                    
                panel.hide();
                panelExpanderIcon.removeClass("perc-expanded").addClass("perc-collapsed");
                window.clearInterval(intervalId);
                setPanelPreference(false);
            }
            panelExpander.hide();
            container.css("margin-left", "0px");
        }
        
        function showUnassignedPanel(){
            var container = $("#perc-template-view-container");
            var panelExpander = $(".perc-unassigned-panel-expander");
            panelExpander.show();
            container.css("margin-left", "38px");
        }
        
        /**
         * Shows detailed information of templates,
         * including applied pages.
         * Displays a carousel element with horizontal scrolling.
         */
        function _switchToCatalogView()
        {
            // the memento view property should be cleaned out when switching views
            // or else when performing reloading operations (like adding new template)
            // it could cause a problem with the current view
            _cleanMementoView();

            // remove background color
            $(".perc-templates-layout").css("background-color", "");

            $(".perc-prev-templates-carousel").css("display", "inline-block");
            $(".perc-next-templates-carousel").css("display", "inline-block");

            $(".perc-items").width("20000em");

            $("#perc-assigned-templates").data("scrollable").updateItemWrap();
            $(".perc-template-item").css("margin-bottom", "0px");

            // Hide all paging from template containers
            $(".perc-template-item .perc-template-paging-container").show();

            $(".perc-template-item-middle-droppable").height($(".perc-template-item").height);

            $("#perc-activated-templates").addClass('perc-templates-detailed');
            //Create view menu again, to show "Thumbnails" option.
            _createViewMenu(VIEW_MODE_CATALOG);
            // Recaulculate the height of the bottom div and the container
            $("#perc-assigned-templates").height('auto');
            fixIframeHeight();
            // Auto-scroll to selected item
            $('#perc-activated-templates-scrollable').trigger('autoscroll');
            
            //Show Unassigned panel expander.
            showUnassignedPanel();
        }

        $("#perc-wid-lib-expander").on("click", function()
        {
            $.fn.percWidLibMaximizer(P);
        });

        // Add the templates carrousel widget that shows the templates of the current selected site.
        var templatesPanel = $("#perc-assigned-templates").template_selected();
        // Wait for the change event in the template carrousel (when one template is selected)
        templatesPanel.on('template_selected.change', function(event, data)
        {
            if (data !== undefined && data.templateId !== undefined&&
                data.templateName !== undefined)
            {
                // Update the export link in the corresponding menu entry
                var exportLink = $.perc_paths.TEMPLATE_EXPORT;
                exportLink += "/" + data.templateId + "/" + data.templateName + ".xml";
                var exportMenuEntry = $(".perc-dropdown-option-Exporttemplate");
                exportMenuEntry
                    .attr("href", exportLink)
                    .attr('style', '')
                    .removeClass('perc-drop-disabled');
            }
        });

        function setupContent()
        {
            fixIframeHeight();
            P.contentView($("#frame"), model);
        }

        function setupLayout()
        {
            fixIframeHeight();

            // There is no need to pass the layoutController or the sizeController from here
            // or the frame, they can all be instantiated from within the layout view
            // TODO: remove frame, layout controller and size controller from method signature
            // cant do it just yet because layout view is shared among template model and page model
            // for now pass size controller null
            var layoutController = P.layoutController(model);
            P.layoutView($("#frame"), model, layoutController, null, function(isDirty)
            {
                setDirty(isDirty);
            });

            $("#region-tool").draggable(
            {
                start: function(event, ui)
                {
                    utils.addAutoScroll();
                },
                stop: function(event, ui)
                {
                    utils.removeAutoScroll();
                },
                helper: 'clone',
                refreshPositions: true
            });
            $("#w1").data("widget", {
                definitionId: 'widgetType'
            }).draggable(
            {});

        }

        function setDirty(isDirty)
        {
            dirtyController.setDirty(isDirty, "template");
            dirty = isDirty;
        }

        //Fixing Iframe size when clicking on sub-tabs under Style tab.
        $(".perc-style-sub-tab").on("click", function()
        {
            fixIframeHeight();
        });

        function log(str)
        {
            return function()
            {
                var args = $.map(arguments, function(x)
                {
                    return x;
                });
                args.unshift(str);
                console.log.apply(console, args);
            };
        }

        fixTemplateHeight();

        // Create a dialog for Importing Template
        function createDialog(isError)
        {
            var self = this;

            // Wrap the import form in an iframe
            var dialogHTML = "<div id='perc-import-template-wrapper'>" + "<iframe name='perc-import-template-frame' id='perc-import-template-frame' height='41px' FRAMEBORDER='0' width='100%' src='../app/importTemplate.jsp'></iframe>" + "</div>";

            // If there is an error on file upload - load the dialog again with an error message in it.
            if(isError)
            {

                dialogHTML = "<div id='perc-import-template-wrapper'>" + "<iframe name='perc-import-template-frame' id='perc-import-template-frame' height='83px' FRAMEBORDER='0' width='100%' src='../app/importTemplate.jsp?status=PERC_ERROR&message=The file you attempted to import is not a valid XML file. Choose a valid CM1 template XML file for upload.'></iframe>" + "</div>";
            }

            // Wrap the iframe inside the dialog
            dialog = $(dialogHTML).perc_dialog(
            {
                resizable: false,
                title: I18N.message("perc.ui.template.design.view@Import Template"),
                modal: true,
                closeOnEscape: true,
                percButtons: {
                    "Import": {
                        click: function()
                        {
                            var actionUrl = $.perc_paths.TEMPLATE_IMPORT;
                            $.PercTemplateService().getSiteProperties($.PercNavigationManager.getSiteName(), function(status, result)
                            {
                                if(status)
                                {
                                    actionUrl = actionUrl + "/" + result.SiteProperties.id;
                                    var checkValue = $("#perc-import-template-frame").contents().find(".perc-template-import-field").val();

                                    // Show error message if user clicks Import without selecting any file.
                                    if(checkValue == "")
                                    {
                                        $("#perc-import-template-frame").css('height', '67px');
                                        $("#perc-import-template-frame").contents().find(".perc-import-error").show();
                                        return;
                                    }
                                    $("#perc-import-template-frame").contents().find("#perc-import-template-form").attr("action", actionUrl);
                                    $("#perc-import-template-frame").contents().find("#perc-import-template-form").trigger("submit");
                                    $("#perc-import-template-frame").on("load",function()
                                    {
                                        closeDialogOnSuccess();
                                    });
                                }
                            });
                        },
                        id: "perc-template-import"
                    },
                    "Cancel": {
                        click: function()
                        {
                            dialog.remove();
                        },
                        id: "perc-import-dialog-cancel"
                    }
                },
                id: "perc-import-dialog",
                width: 490
            });
        }

        // Close or reload the dialog based on server feedback after import.
        function closeDialogOnSuccess()
        {
            // Close the dialog if import of the template is success
            var templateName = $("#perc-import-template-frame").contents().find("body").text();

            //if import is success - reload the page and pass the name of newly created template as a parameter.
            if(templateName != "null")
            {
                dialog.remove();
                $.cookie("templateImport", templateName);
                location.reload();
            }
            // Reload the dialog with an error message if import fails
            else
            {
                dialog.remove();
                createDialog(true);
            }
        }
    };
})(jQuery, jQuery.Percussion);
