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

var layoutModel;

(function($,P) {
    var NORTH=0,SOUTH=1,EAST=2,WEST=3,AFTER=4,BELOW=5,CENTER=6;
    var PUFF_PADDING = 10;
    var layout;
    var requireMigration = false;

    P.layoutView = function( iframe, model, layoutController) {

        $.perc_utils.debug("Calling layoutView()");

        layout = iframe.contents();

        // make the model available to everyone
        layoutModel = model;
        var sizeController = P.sizeController(model);
        var puff = true;
        var dragOverActive = {};
        var currentDragOverRegion = null;

        // HTML used to render each of the region in the Explore Regions tray
        var regionToolDivHtml = "";
        regionToolDivHtml += "<div id='_REGION_ID_' class='perc-region-library-tool' name='_REGION_ID_' style='overflow: hidden;white-space: nowrap;text-overflow: ellipsis;'>";
        regionToolDivHtml += "<div id='_REGION_ID_-2'>";
        regionToolDivHtml += "_REGION_LABEL_";
        regionToolDivHtml += "</div>";
        regionToolDivHtml += "</div>";

        // keep track of last region acted upon such as last added, resize, re-ordered
        // this is then used to highlight this region for feedback
        var currentRegion=null;

        // keep track of count of drop widgets overlapping each other
        var wOverlap = 0;

        // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
        var dirtyController = $.PercDirtyController;

        //An array object of widget definition summaries, See PSWidgetSummary.java for the fields.
        //Filled in populateWidgetLibrary() method during the initialization.
        var widgetDefs = {};

        //An unfiltered array of all widget defs
        var allWidgetDefs = {};

        // add a div at the end of the frame for styling the widget drops
        iframe.contents().find("body").append("<div id='placeholder-region'></div>");
        /**
         * Array of menus that decorate a the top right of a widget DIV when you select it
         *
         * Each menu has 4 attributes:
         * name : a string identifying name
         * img : a string denoting a relative path to a PNG image
         *         or a function that returns a string denoting a relative path to a PNG image.
         *         Leave out the PNG extension
         * callback : function(htmlElement) a function that will be invoked when menu is clicked.
         *         The HTML element will be passed as an argument to the callback
         *         The HTML element will be either the widget DIV or the region DIV being decorated
         * tooltip : a string displayed as a tooltip when you hover over the menu
         * 09/17/2012 Luis A. Mendez
         *  widgetType: specify the type of the widget that will have the menuItem. (Custom menu items by widget type.)
         */
        var widgetMenu = [
            {
                name : 'delete',
                img : '/cm/images/icons/editor/delete',
                callback : function(widget) {
                    var regionId = widget.closest('.perc-region').attr('id');
                    layoutController.removeWidget( regionId, widget.attr('widgetid') );
                    setLayoutDirty( true );
                    refreshRender();
                },
                tooltip : I18N.message("perc.ui.layout.view@Delete widget")
            },
            {
                //Note this uses allWidgetDefs to account for Pages with Widgets that have been deleted or are disabled or currently filtered in widget tray.
                name : 'configure',
                img : function (elem){
                    if (allWidgetDefs[elem.attr('widgetdefid')].id === "percRawHtml")
                        var imgSrc = '/cm/images/icons/editor/buttonConfigure2Gray';
                    else
                        var imgSrc = '/cm/images/icons/editor/configureInactive';

                    //if(!widgetDefs[elem.attr('widgetdefid')].hasUserPrefs)
                    if(layoutModel.isTemplate() || allWidgetDefs[elem.attr('widgetdefid')].hasUserPrefs)
                    {
                        if (allWidgetDefs[elem.attr('widgetdefid')].id === "percRawHtml")
                            var imgSrc =  '/cm/images/icons/editor/buttonConfigure2';
                        else
                            var imgSrc =  '/cm/images/icons/editor/configure';
                    }
                    return imgSrc;
                },
                callback : function(elem){
                    //if(widgetDefs[elem.attr('widgetdefid')].hasUserPrefs)
                    if(layoutModel.isTemplate() || allWidgetDefs[elem.attr('widgetdefid')].hasUserPrefs)
                        configureWidget(elem);
                },
                tooltip : I18N.message("perc.ui.layout.view@Configure")
            },
            {
                widgetType: 'percRawHtml',
                name : 'convert',
                img : '/cm/images/icons/editor/buttonConvert',
                callback : function(widget) {
                    var widgetObj = {
                        id : widget.attr('widgetid'),
                        name: widget.attr('widgetname'),
                        assetId : widget.attr('assetid'),
                        regionId : widget.closest('.perc-region').attr('id'),
                        ownerId : widget.attr('ownerId')
                    };

                    $.perc_utils.confirm_dialog({
                        id: "convertHTMLWidget",
                        title: I18N.message("perc.ui.layout.view@Convert HTML Widget"),
                        question: I18N.message("perc.ui.layout.view@HTML To Rich Text"),
                        showAgainCheck: true,
                        width: 500,
                        cancel:
                            function(){},
                        success:
                            function(){
                                layoutController.convertHTMLWidget(widgetObj, function(status, message){
                                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                                        refreshRender();
                                        setLayoutDirty( true );
                                    }
                                    else
                                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.layout.view@Conversion Error"), content: message});
                                });
                            }
                    });
                },
                tooltip : I18N.message("perc.ui.layout.view@Convert to Rich Text")
            }
        ];

        /**
         * Array of menus that decorate a the top right of a region DIV when you select it
         */
        var regionMenu = [
            {
                img: function (elem) {

                    // elem is the DIV to which we add the menu
                    // if the DIV's parent is not a DIV, then this must be the top most DIV
                    if($(elem).parent()[0].nodeName !== "DIV")

                    // if this is the top DIV, then we dont want to add a delete menu: return null
                        return null;

                    else

                    // otherwise use the delete icon
                        return '/cm/images/icons/editor/delete';
                },
                callback: function(region) {
                    deleteRegion( region );
                },
                tooltip: I18N.message("perc.ui.layout.view@Delete Region")
            },
            {
                img: function (elem) {

                    // elem is the DIV to which we add the menu
                    // if the DIV's parent is not a DIV, then this must be the top most DIV
                    if($(elem).parent()[0].nodeName !== "DIV")

                    // if this is the top DIV, then use one delete icon
                        return '/cm/images/images/buttonConfigStandalone';
                    else

                    // otherwise use default icon
                        return '/cm/images/icons/editor/configure';
                },
                callback: function(region) {
                    $.PercEditRegionPropertiesDialog().editRegionProperties( region );
                },
                tooltip: I18N.message("perc.ui.layout.view@Configure Region")
            }
        ];

        /**
         * Decorators that apply region puffs and selected CSS
         */
        var widgetDecorator = P.decorationController( allWidgets, 'perc-widget-puff', 'perc-widget-selected', widgetMenu);
        var regionDecorator = layoutModel.isTemplate() ? P.decorationController( allRegions, 'perc-region-puff', 'perc-region-selected', regionMenu)
            : P.decorationController( allRegions, 'perc-region-puff', 'perc-region-selected', []);


        // let both decorators know about each other so that they can unselect each other
        widgetDecorator.setOtherDecorator(regionDecorator);
        regionDecorator.setOtherDecorator(widgetDecorator);

        // toggles size input fields in the region property editor dialog
        // size fields are width, height, padding, and margin
        $('#perc-region-auto-resize').on("click",function(){
            var checked = $(this).is(":checked");
            updateSizeFields(checked);
        });

        // populates Explore Regions tray and toggles it open/close
        $("#perc-region-library-expander").off("click").on("click",function(){
            $.fn.percRegionLibraryMaximizer(P);
            populateRegionLibrary();
        });

        // populates Orphan Assets tray and toggles it open/close
        $("#perc-orphan-assets-expander").off("click").on("click",function(){
            $.fn.percOrphanAssetsMaximizer(P);
            populateOrphanAssets();
        });

        function saveLayout(afterSave) {
            callback = function(){
                if (widObj && layoutModel.isTemplate())
                {
                    layoutModel.loadAssetDropCriteria(function(){
                        refreshRender(afterSave);
                    });
                }
                else if (afterSave)
                {
                    afterSave();
                }
            };
            var widObj = newWidgetObject();
            $.PercBlockUI();
            layoutModel.save(
                function(){
                    if (layoutModel.isTemplate())
                        $.PercRegionCSSHandler.mergeRegionCSS();
                    $.unblockUI();
                    callback();
                },
                widObj,
                requireMigration
            );
            setLayoutDirty(false);
        }

        function newWidgetObject() {
            var ieData = null;
            var newAssets = [];
            iframe.contents().find(".perc-widget[widgetdefid='percRawHtml'][assetid='']").each(function(){
                var curWidget = $(this);
                if(curWidget.hasClass('perc-widget-transperant') && curWidget.find(".html-sample-content").length < 1) {
                    requireMigration = true;
                }
                if(curWidget.find(".html-sample-content").length < 1)
                {
                    var newAsset = {};
                    newAsset.widgetId = curWidget.attr("widgetid");
                    var updatedHtml = "";
                    try
                    {
                        updatedHtml = $(curWidget.html()).wrapAll("<div></div>").parent();
                    }
                    catch (err)
                    {
                        $.perc_utils.info(I18N.message("perc.ui.layout.view@Failed To Fill Widget") + newAsset.widgetId + I18N.message("perc.ui.layout.view@Error Is") + err);
                        updatedHtml = $("").wrapAll("<div></div>").parent();
                    }
                    updatedHtml.find('a[tempURL]').each(function(){
                        var realURL = $(this).attr('tempURL');
                        $(this).attr('href', realURL);
                        $(this).removeAttr('tempURL');
                    });
                    newAsset.content = updatedHtml.html();
                    newAsset.ownerId = curWidget.attr("ownerid");
                    newAssets.push(newAsset);
                }
            });
            if(newAssets.length > 0)
            {
                ieData = {"InspectedElementsData":{}};
                ieData.InspectedElementsData.newAssets = newAssets;
            }
            return ieData;
        }


        /**
         * Button or link that toggles region puff decoration
         */
        $('.perc-dropdown-option-HideGuides').off("click").on("click", function() {
            regionDecorator.visible( !regionDecorator.visible());
            widgetDecorator.visible( !widgetDecorator.visible());
            puff = !puff;
            refreshRender(); // come back here.
        });

        /**
         * Save button invokes model to save itself which uses page or template manager services that
         * communicate with REST service to persiste the page or template
         */
        $('#perc-save').off("click").on("click", function() {
            if(!layoutModel.isResponsiveBaseTemplate())
                $.PercInspectionToolHandler.clearItoolMarkup();
            saveLayout();
            if(!layoutModel.isResponsiveBaseTemplate())
                $.PercInspectionToolHandler.saveCallback();
        });

        /**
         * Bind the click event to the JavaScript Off menu. Checks whether the view is dirty or not, if dirty then
         * warns the user. Sets the JavaScriptOff to true or false depending on the current status. Calls the
         * initRender to reinitialize the view.
         */
        $("#perc-layout-menu a.perc-dropdown-option-DisableJavaScript").off("click").on("click",function() {
            var __this = this;
            var handleScripts = function()
            {
                var scriptOff = I18N.message( "perc.ui.menu@JavaScript Off" );
                var scriptOn = I18N.message( "perc.ui.menu@JavaScript On" );
                if($(__this).text() === scriptOff)
                {
                    $(__this).text(scriptOn);
                    $(__this).attr("title", "Turns on JavaScript");
                    layoutModel.setJavaScriptOff(true);
                    //layoutModel.load();
                    initRender();
                }
                else
                {
                    $(__this).text(scriptOff);
                    $(__this).attr("title", "Turns off JavaScript");
                    layoutModel.setJavaScriptOff(false);
                    //layoutModel.load();
                    initRender();
                }
                setLayoutDirty(false);
            };
            if(dirtyController.isDirty())
            {
                var msg = layoutModel.isTemplate?"This template" : "This page";
                msg += I18N.message("perc.ui.layout.view@Contains Unsaved Changes") +
                    I18N.message("perc.ui.css.preview.view@Dont Save");
                var options = {
                    question: msg
                };

                // If dirty, then show a confirmation dialog
                dirtyController.confirmIfDirty(
                    function() { handleScripts(); },
                    function(){},
                    options
                );
            }
            else
            {
                handleScripts();
            }
        });

        /**
         * Reload model and re-render if they cancel
         */
        $('#perc-layout-cancel').off("click").on("click", function() {
            cancel();
            if(!layoutModel.isResponsiveBaseTemplate())
                $.PercInspectionToolHandler.cancelCallback();
        });

        function cancel(callback) {
            layoutModel.load();
            initRender();
            setLayoutDirty(false);
            if(callback){
                callback();
            }
            if (layoutModel.isTemplate())
                $.PercRegionCSSHandler.prepareForEditRegionCSS();
        }

        initDeleteRegionDialog();

        //Initially load all widgets - populate widget library will handle filters
        initWidgetLibrary("all","no");
        populateWidgetLibrary();
        $(".perc-widget-type").off("change").on("change",
            function(evt){
                populateWidgetLibrary(evt);
            });
        $(".perc-widget-category").off().on("change",
            function(evt){
                filterWidgetLibrary(evt);
            });
        initRender();

        /**
         * Listen for asset delete events and refresh view
         */
        $.perc_finder().addActionListener(function(action, data) {

            // verify that we are deleting an asset
            if(action === $.perc_finder().ACTIONS.DELETE && (data.type === 'asset' || data.type === 'page')) {

                if (typeof(data.isOpen) != 'undefined' && data.isOpen)
                {
                    // current item is open for edit, no need to refresh as it will be cleared
                    return;
                }

                // find out where we are, what view and what tab within that view
                var currentView     = $.PercNavigationManager.getView();
                if(currentView === $.PercNavigationManager.VIEW_DESIGN) {
                    currentTabIndex = $("#tabs").tabs('option','active');
                    if(currentTabIndex === 2) initRender();
                } else if(currentView  === $.PercNavigationManager.VIEW_EDITOR) {
                    currentTabIndex = $("#perc-pageEditor-tabs").tabs('option','active');
                    if(currentTabIndex === 1) initRender();
                }
            }
        });

        /**
         * Initial rendering when the layout is first loaded up.
         * The model is either PercPageModel or PercTemplateModel depending on whether we reached here from
         * the Editor (Page) or Admin (Template)
         * The model is invoked to render and update the iframe at the bottom.
         * The model uses the page manager or template manager service to make an ajax call to render the page or template.
         * After the iframe is rendered, afterRender() is called to decorate the regions and/or widgets.
         */
        function initRender() {
            layoutModel.renderAll(iframe, function() {
                afterRender();
                $("#frame").attr("perc-view-type","layout");
                var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
                if(frwrapper != null)
                    frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");
                var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');
                if(tbwrapper != null)
                    tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");
            });
        }

        /**
         * Consecutive rendering of the layout when adding or moving a region or widget, or resizing a region.
         * The model is either PercPageModel or PercTemplateModel depending on whether we reached here from
         * the Editor (Page) or Admin (Template)
         * The model is invoked to retrieve an updated version of the page or template.
         * The html is passed back in a callback.
         * The model uses the page manager or template manager service to make an ajax call to refresh the page or template.
         * After the iframe is rendered, afterRender() is called to decorate the regions and/or widgets.
         */
        function refreshRender(callback) {
            layoutModel.render( function(htmlContent) {
                window.frames[iframe.attr("id")].jQuery('body').empty().append(htmlContent).ready(function()
                {
                    afterRender(callback);
                });
            });
        }

        /**
         * After the initial rendering of the iframe or after refreshing the iframe,
         * decorate the regions and widgets with borders, drops, resizing, and fix the widths.
         */
        function afterRender(callback) {
            iframe.contents().find("div").off();
            widgetDecorator.refresh();
            regionDecorator.refresh();

            if(puff) {
                if(!layoutModel.isResponsiveBaseTemplate())
                    updateWidths();
                $('.perc-dropdown-option-HideGuides').html("Hide Guides");
            } else {
                $('.perc-dropdown-option-HideGuides').html("Show Guides");
            }

            iframe.contents().find(".perc-region").each(function() {

                var region = $(this);
                region.attr("title", region.attr("id"));
                getFixedNoAutoResize(region);
                // mark leaf regions
                var subregions = region.find(".perc-region");
                if(subregions.length === 0)
                    region.addClass("perc-region-leaf");

                // the code below is commented for bug CML-4549
                // adding the padding causes the broken layout

                // make sure that the padding around the regions is at least 10px
                // var paddingTop    = region.css("padding-top").replace("px","");
                // var paddingBottom = region.css("padding-bottom").replace("px","");
                // var paddingLeft   = region.css("padding-left").replace("px","");
                // var paddingRight  = region.css("padding-right").replace("px","");

                // if(paddingTop < 10)
                //     region.css("padding-top", "10px");
                // if(paddingBottom < 10)
                //     region.css("padding-bottom", "10px");
                // if(paddingLeft < 10)
                //     region.css("padding-left", "10px");
                // if(paddingRight < 10)
                //     region.css("padding-right", "10px");
            });

            addDropsSortingResizing();
            sanitizeHtml();

            // deselect all widgets and regions if the user clicks in the background
            iframe.contents().find("body")
                .css("z-index","-1000")
                .css("position","static")
                .off("click")
                .on("click",function(event){
                    widgetDecorator.unselectAll();
                    regionDecorator.unselectAll();
                    populateRegionLibrary();
                })
                .append("<div id='perc-widget-drop-feedback'></div>");

            percWidgetDropFeedback = iframe.contents().find("#perc-widget-drop-feedback");

            // initialize widget overlap counter after every render and hide all drop feedbacks
            wOverlap = 0;
            percWidgetDropFeedback.hide();

            if(currentRegion){
                var currentRegionDiv = iframe.contents().find("#"+currentRegion);
                currentRegionDiv.trigger("click");
                currentRegion=null;
            }

            // get the list of regions from the layout and populate the "Explore Regions" tray
            populateRegionLibrary();

            // if region is clicked, also highlight the region-tool in "Explore Regions" tray
            iframe.contents().find(".perc-region").on("click",function(){
                var regionId = "perc-re-" + $(this).attr("id");
                $(".perc-region-library-tool").removeClass("perc-region-selected");
                $(".perc-region-library-tool[name="+regionId+"]").addClass("perc-region-selected");
            });
            iframe.contents().find(".sf-menu").superfish({
                pathClass:  'current',
                autoArrows:  false,
                delay:400,
                pathLevels: 0
            });

            // show error indicator only if there are errors
            var inEditTemplateView = $.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDIT_TEMPLATE;
            var visibility = (checkForErrors() && inEditTemplateView)? "visible" : "hidden";
            $("#perc-error-alert").css("visibility", visibility);
            $.PercInspectionToolHandler.afterRenderCallback();
            if(callback)
            {
                callback();
            }
        }

        var percWidgetDropFeedback;

        /**
         * HTML fixes when in layout mode
         */
        function sanitizeHtml()
        {
            $.perc_utils.handleLinks(iframe);
            $.perc_utils.handleObjects(iframe);
        }

        /**
         * Add support for dropping regions and widgets, moving regions and widgets, and resizing regions
         */
        function addDropsSortingResizing() {
            layoutModel.eachRegion(function() {
                if(layoutModel.canOverride(this) || layoutModel.isTemplate()) {

                    var regionId = this.regionId;
                    if(regionId !== "percRoot") {
                        var region = $(iframe.contents().find('#'+regionId)[0]);

                        // we can only add regions in a template, not in a page
                        if(layoutModel.isTemplate()) {
                            addRegionToolDrops( region );
                            addResizing( region );
                        }

                        //only leaf regions should accept widget drops
                        if (region.find(".perc-region").length === 0)
                            addWidgetLibraryDrops( region );
                        addSorting( region );
                    }
                }
            });
        }

        /**
         * Add drop event to a region to accept a widget
         */
        function addWidgetLibraryDrops(region) {
            var regionId   = region.attr("id");
            var widgets    = region.find(".perc-widget");
            var lastWidget = region.find(".perc-widget:last");
            var lastWidgetId = lastWidget.attr("widgetid");

            // you can drop a widget on the region and it will be added to the end
            region.droppable({
                accept: '.perc-widget-tool',
                tolerance: 'pointer',
                greedy: true,
                scope: $.perc_iframe_scope,
                drop: function(event,ui) {
                    regionDrop(ui, region);
                },
                over : function(event, ui) {
                    regionOver(region);
                },
                out : function(event, ui) {
                    regionOut(region);
                }
            });
            // if the region already has widgets, then you can drop on the existing widgets themselves
            // the new widget will be added on top of the existing widget
            widgets.droppable({
                accept: '.perc-widget-tool',
                tolerance: 'pointer',
                greedy: true,
                scope: $.perc_iframe_scope,
                drop: function(event,ui) {
                    widgetDrop(ui, $(this), region);
                },
                over: function(event, ui) {
                    widgetOver($(this), region);
                },
                out : function(event, ui) {
                    widgetOut($(this), region);
                }
            });
        }

        /**
         * Show widget is not responsive dialog if needed
         *
         * @param {Object} widget
         */
        function notifyIfWidgetNotResponsive(widgetType) {
            var dialogAlreadyShowing;
            if (!widgetType || widgetDefs[widgetType].responsive) {
                return;
            }
            // drop on a widget calls notify twice from widgetDrop and sectionDrop
            dialogAlreadyShowing = $('#nonResponsiveWidgetWarning').length;
            if (dialogAlreadyShowing) {
                return;
            }
            $.perc_utils.confirm_dialog({
                id: 'nonResponsiveWidgetWarning',
                title: "Responsive Design",
                question: I18N.message("perc.ui.layout.view@Unresponsive Widget"),
                type: "OK",
                cancel: function(){},
                success: function(){},
                showAgainCheck: true,
                dontShowAgainAction: null
            });
        }

        /**
         * Decorates the region being dragged over for a widget drop.
         * @param regionId the region that the draggable is currently over.
         * Cannot be <code>null</code>.
         */
        function onRegionOver(regionId){
            var region = iframe.contents().find("#" + regionId);
            var regionBottom = region.offset().top + region.height();
            var widgets      = region.find(".perc-widget");
            currentDragOverRegion = regionId;
            if(widgets.length === 0) {
                region.css("background-color","#CAF589");
            } else if(wOverlap === 0){
                var lastWidget = region.find(".perc-widget:last");
                var lastWidgetId = lastWidget.attr("widgetid");
                var offset = lastWidget.offset();
                var bottom = offset.top + lastWidget.height();
                var height = 5;    // TODO: put this in css instead of here
                if(bottom > regionBottom)
                    return;

                percWidgetDropFeedback.show();
                percWidgetDropFeedback.height(height)
                    .width(lastWidget.width())
                    .css("background-color", "#CAF589")
                    .css("left",offset.left)
                    .css("top", bottom)
                    .css("z-index", "2000")
                    .css("position","absolute");
            }
        }

        /**
         * Removes drag over decoration from specified region.
         * @param regionId a region that the draggable is not currently over.
         * Cannot be <code>null</code>.
         */
        function onRegionOut(regionId){
            var region = iframe.contents().find("#" + regionId);
            region.css("background-color","");
        }

        /**
         * Add drop divs at the north, south, east, west of the region
         * These drops accept the new region tool to add a region to
         * the top, bottom, left or right of the parent region.
         * This method relies on makeFeedbackDroppable() to create the each of the html DIV drop zones
         */
        function addRegionToolDrops(region) {
            //Set the region to have four thin divs at its outer edges
            //which take drop events from the region tool.

            //remove old ones.
            region.children('perc-region-feedback').remove();
            var regionId = region.attr('id');
            var dirs = $.map( ["north","south","east","west","after","below","center"],
                function(dir){
                    return makeFeedbackDroppable( regionId, dir );
                });

            dirs[NORTH].css ({top:    '2px',  left:  '10px', width:  '100%', height: '10px'}).addClass('north');
            dirs[SOUTH].css ({bottom: '2px',  left:  '10px', width:  '100%', height: '10px'}).addClass('south');
            dirs[EAST].css  ({top:    '10px', right: '2px',  height: '100%', width:  '10px'}).addClass('east');
            dirs[WEST].css  ({top:    '10px', left:  '2px',  height: '100%', width:  '10px'}).addClass('west');
            dirs[AFTER].css ({top:    '0px',  right: '-4px', height: '100%', width:  '6px' }).addClass('after');
            dirs[BELOW].css ({bottom: '-4px', left:  '0px',  width:  '100%', height:  '6px'}).addClass('below');
            dirs[CENTER].css({top:    '12px', left:  '12px', width:  '100%', height: '100%'}).addClass('center');

            // Append the drop regions to the document then shorten the east and west widths
            // and north and south heights so that they dont overlap
            var parentRegion = region.parent().parent(".perc-region");
            var siblingRegion = region.next(".perc-region");
            var horizontalParent = parentRegion.hasClass("perc-horizontal");
            var isParent = region.find(".perc-region").length>0;
            $.each( dirs, function(index) {
                if(index === AFTER && (!horizontalParent || siblingRegion.length === 0))
                    return true;
                if(index === BELOW && (horizontalParent || siblingRegion.length === 0))
                    return true;
                if(index === CENTER && isParent)
                    return true;
                // uncomment if you don't want border drop areas on leafs (Paul's suggestion)
                // if(index >= NORTH && index <= WEST && !isParent)
                //    return true;
                region.append( this );
                if(index === NORTH || index === SOUTH ) {
                    // if it's north or south, then shrink the width by 20px
                    var newWidth = dirs[index].width() - 20;
                    dirs[index].css('width', newWidth);
                } else if(index === EAST || index === WEST) {
                    // if it's east or west, then shrink the height by 20px
                    var newHeight = dirs[index].height() - 20;
                    dirs[index].css('height', newHeight);
                } else if(index === CENTER) {
                    var newHeight = dirs[index].height() - 24;
                    var newWidth = dirs[index].width() - 24;
                    dirs[index]
                        .css('height', newHeight)
                        .css('width', newWidth);
                }
            });
        }

        /**
         * Creates the drop zone to the top, bottom, right or left of the region.
         * These are used to highlight the drop zones as you hover the new region tool over the drop zone.
         */
        function makeFeedbackDroppable( regionId, direction ) {
            return $("<div/>")
                .addClass("perc-region-feedback")
                .css({'position':'absolute'}).droppable({
                    scope: $.perc_iframe_scope,
                    accept: '#region-tool',
                    tolerance: 'pointer',
                    greedy: true,
                    hoverClass: 'perc-show-feedback',
                    over : function(event, ui) {
                        // provide feeback when hovering region tool over a region so that you know what region will be split
                        iframe.contents().find(".perc-region").removeClass("perc-hover-region-feedback");
                        // add feedback to this region, i.e., the parent of the feedback div that lights up
                        $(this).parent().addClass("perc-hover-region-feedback");
                    },
                    out : function() {
                        // provide feeback when hovering region tool over a region so that you know what region will be split
                        // remove feedback when hover out but only if you've reached the top region
                        var grandParent = $(this).parent().parent()[0];
                        var grandParentTag = grandParent.tagName;
                        if(grandParentTag === "BODY") {
                            layout
                                .find(".perc-region")
                                .removeClass("perc-hover-region-feedback");
                        }
                    },
                    drop: function(event, ui) {
                        // remove region border feedback
                        iframe.contents().find(".perc-region").removeClass("perc-hover-region-feedback");
                        // dont accept the drop if it's not on the visible portion of the layout
                        var toolbar = $("#tabs-3");
                        if(toolbar.offset().top + toolbar.height() > event.clientY) {
                            return false;
                        }
                        // add the region when the drop occurs
                        layoutController.addRegion( regionId, direction, function(region){
                            if(region != null){
                                currentRegion=region.regionId;
                                setLayoutDirty( true );
                                refreshRender();
                            }
                        });
                    }
                });
        }
        /**
         * Add sorting/reordering of regions and widgets
         */
        function addSorting( region ) {
            if( region.is('.perc-region-leaf')) {
                addWidgetSorting( region );
            } else {
                addRegionSorting(region);
            }
        }

        /**
         * Add sorting/reordering of regions
         */
        var originalHeights;
        function addRegionSorting(region) {
            // sorting is configured differently for horizontal and vertical regions
            // because when sorting vertical regions, they are by default the same size
            // which is what jquery sorting expects. For horizontal sorting, the regions
            // can be of different height and sorting breaks. So we need to make the
            // regions all of the same size and then apply the sorting

            // make horizontal regions sortable
            if(region.hasClass("perc-horizontal")) {
                // if this region si a horizontal region, then the child regions are floated left
                insideIframe( region ).sortable({
                    delay : 500,
                    containment : "parent", // we can only sort within the parent
                    tolerance : "pointer",
                    // we only sort direct children
                    items : "> .perc-horizontal > .perc-region",
                    axis : "x",             // we can only move left right
                    start : function(event,ui) {
                        // we'll compute the largest of the sibling heights to make them all the same
                        // we'll store them temporarily so that we can reset them when we stop sorting
                        originalHeights = {};
                        // get the children of the enclosing horizontal region and iterate over the children
                        var children = $(this).find(".perc-horizontal > .perc-region");
                        // look for the biggest height
                        var biggestHeight = 0;
                        children.each(function() {
                            var child = $(this);
                            var childId = child.attr("id");
                            var childHeight = child.height();
                            if(childHeight > biggestHeight)
                                biggestHeight = childHeight;
                            // store originalHeights so we can put them back
                            originalHeights[childId] = childHeight;
                        });
                        // set the height of all the children
                        children.height(biggestHeight);
                        // make the placeholder visible and color it green
                        // make the width the same as the original item being sorted
                        ui.placeholder
                            .width(ui.item.width())
                            .css("visibility", "visible")
                            .css("background-color", "#CAF589");
                    },
                    stop : function(event, ui) {
                        // when we stop sorting we need to put the heights back
                        var children = $(this).children(".perc-horizontal > .perc-region");
                        children.each(function() {
                            var child = $(this);
                            var childId = child.attr("id");
                            var originalChildHeight = originalHeights[childId];
                            // put back the originalHeights
                            child.height(originalChildHeight);
                        });

                        // get the new position
                        var newPosition = ui.item.prevAll('.perc-region').length;

                        currentRegion = ui.item.attr('id');
                        // move the region in the model to the new position
                        layoutController.moveRegion( region.attr('id'), ui.item.attr('id'), newPosition );

                        setLayoutDirty(true);
                        refreshRender();
                    }
                });
            } else {
                var inside = insideIframe(region);
                if(typeof(inside.sortable) == 'function')
                {
                    insideIframe( region ).sortable({
                        delay : 500,
                        containment : "parent",
                        tolerance : "pointer",

                        // only sort children
                        items : "> .perc-vertical > .perc-region",
                        placeholder: "placeholder-region",
                        axis : "y",
                        forcePlaceholderSize  : true,
                        stop : function(event, ui) {
                            var pos = ui.item.prevAll('.perc-region').length;
                            currentRegion = ui.item.attr('id');
                            layoutController.moveRegion( region.attr('id'), ui.item.attr('id'), pos );
                            setLayoutDirty(true);
                            refreshRender();
                        }
                    });
                }
            }
        }

        /**
         * Add sorting/reordering of widgets
         */
        function addWidgetSorting( region ) {
            var inside = insideIframe(region);
            if(typeof(inside.sortable) == 'function')
            {
                insideIframe( region ).sortable({
                    connectWith: '.perc-region-leaf',
                    dropOnEmpty: true,
                    placeholder: 'placeholder-widget',
                    items : "div.perc-widget:not('.perc-locked')",
                    tolerance: 'pointer',
                    start : function(event,ui) {
                        // temporarily set overflow to visible so that we dont scroll if the content is too big
                        $(this).css("overflow","visible");
                    },
                    stop: function(event, ui) {
                        var newPosition = ui.item.prevAll('.perc-widget').length;
                        var widgetid = ui.item.attr('widgetid');
                        var fromRegionId = region.attr('id');
                        var toRegionId = fromRegionId;

                        // if source region contains the widget...
                        if( region.children().children( "[widgetid="+widgetid+"]" ).length > 0 ) {
                            // ...then go ahead and move the widget from the region to the new region
                            layoutController.moveWidget( fromRegionId, widgetid, toRegionId, newPosition );
                            setLayoutDirty( true );
                            refreshRender();
                        }
                    },
                    receive: function(event, ui) {
                        var newPosition = ui.item.prevAll('.perc-widget').length;
                        var widgetid = ui.item.attr('widgetid');
                        var fromRegionId = ui.sender.attr('id');
                        var toRegionId = region.attr('id');

                        layoutController.moveWidget( fromRegionId, widgetid, toRegionId, newPosition );
                        setLayoutDirty( true );
                        refreshRender();
                    }
                });
            }
        }

        /**
         * Add region resizing
         */
        function addResizing(regionResizing){
            layoutModel.isResponsiveBaseTemplate()?_addRespRegionResizing(regionResizing):_addBaseRegionResizing(regionResizing);
        }
        /**
         * Add region resizing
         */
        function _addRespRegionResizing(regionResizing){
            var regionId = regionResizing.attr("id");
            var curRegion = null;
            var nextRegion = null;
            layoutModel.editRegion(regionId,function(){
                curRegion = this;
            });
            if(regionResizing.next().length>0){
                layoutModel.editRegion(regionResizing.next().attr("id"),function(){
                    nextRegion = this;
                });
            }

            if(curRegion.row || (curRegion.columns && curRegion.large === "large-12")){
                regionResizing.css("border","1px solid #33C9ED");
                return;
            }
            var parentReg = null;
            layoutModel.editRegionParent(regionId, function(){
                parentReg = this;
            });
            var minw = Math.floor(($(iframe.contents().find('#'+parentReg.regionId)[0]).width())/12);
            var position = layoutModel.getRegionPosition(regionId);
            //Only east side of the border is used for resizing.
            //Avoid resizing of the last child, if both current child and next child have size of 1 we can't resize.
            if(position === parentReg.children.length - 1 || (nextRegion!=null && (curRegion.large==="large-1" && nextRegion.large === "large-1")))
                return;
            var curRegWidth = regionResizing.outerWidth();
            var nextRegWidth = regionResizing.next().outerWidth();
            var widthAdj = nextRegWidth - regionResizing.next().innerWidth();
            var totalSize = curRegWidth + nextRegWidth;
            insideIframe( regionResizing ).resizable({
                handles: "e",
                grid:minw,
                start: function(event,ui) {
                    //If right side region
                    $.each(parentReg.children, function(){
                        $(iframe.contents().find('#'+this.regionId)[0]).css("min-width",minw + "px");
                    });
                    regionResizing.resizable('option','maxWidth',totalSize - minw);
                    console.log("regionResizing.width()" + regionResizing.width());
                    console.log("minw" + minw);
                    console.log("regionResizing.next().width()" + regionResizing.next().width());
                    console.log("parentWidth", $(iframe.contents().find('#'+parentReg.regionId)[0]).width());
                },
                resize: function(event,ui) {
                    regionResizing.next().width(totalSize-regionResizing.outerWidth()-widthAdj);
                },
                stop: function(event,ui) {
                    // when done set the page to dirty so that we cant navigate away without a confirmation
                    setLayoutDirty( true );
                    $.each(parentReg.children, function(){
                        $(iframe.contents().find('#'+this.regionId)[0]).css("min-width","");
                    });
                    regL = Math.round(regionResizing.outerWidth()/minw);
                    regR = Math.round(regionResizing.next().outerWidth()/minw);
                    regL = regL===0?1:regL;
                    regR = regR===0?1:regR;
                    var lCols = parseInt(curRegion.large.split("-")[1],10);
                    var rCols = parseInt(nextRegion.large.split("-")[1],10);
                    if((lCols + rCols) > (regL + regR)){
                        regL < regR ? regL++ : regR++;
                    }
                    else if((lCols + rCols) < (regL + regR)){
                        regL > regR ? regL-- : regR--;
                    }
                    curRegion.large = "large-" + regL;
                    nextRegion.large = "large-" + regR;
                    currentRegion=regionResizing.attr("id");
                    refreshRender();
                }
            });

        }

        /***
         * Cleanup the no autoresize attribute.
         * @param activeRegion the current region element
         * @returns a string containing true or false
         */
        function getFixedNoAutoResize(activeRegion){
            var noautoresize = activeRegion.attr("noautoresize");
            if(noautoresize !== undefined){
                //remove the bad attribute
                activeRegion.removeAttr("noautoresize");
                activeRegion.attr("data-noautoresize",noautoresize);
            }else{
                return "false";
            }
            return noautoresize;
        }

        /**
         * Add region resizing
         */
        function _addBaseRegionResizing( regionResizing ) {

            // dont add resizing if noautoresize attribute is true
            var noautoresize = getFixedNoAutoResize(regionResizing);


            if(noautoresize === "true") {
                regionResizing.css("border","1px solid #33C9ED");
                return;
            }

            var rightSiblingRegion;// when resizing we change the size of the regionResizing plus
                                   // its inmediate right sibling
            var totalSize;         // the sum of resizing regionResizing and its sibling
            var ratio;             // the ratio by which we change the right sibling based on the total original size
                                   // and the resize value
            insideIframe( regionResizing ).resizable({
                handles: 'e, s',   // resize left/right and up/down
                start: function(event,ui) {
                    if(regionResizing.parent().hasClass("perc-vertical"))
                        return;
                    // get the sibling
                    rightSiblingRegion = regionResizing.next();
                    // if the sibling is not auto resizable, return
                    if(getFixedNoAutoResize(rightSiblingRegion) === "true")
                        return;
                    // calculate the total width of this regionResizing plus sibling
                    totalSize = regionResizing.width() + rightSiblingRegion.width();
                    // set a max for the resize to the total size
                    regionResizing.resizable('option','maxWidth',totalSize );
                },
                resize: function(event,ui) {
                    if(regionResizing.parent().hasClass("perc-vertical"))
                        return;
                    // as we resize, change also the sibling's width
                    rightSiblingRegion = regionResizing.next();
                    // if the sibling is not auto resizable, return
                    if(getFixedNoAutoResize(rightSiblingRegion) === "true")
                        return;
                    ratio = ui.size.width / totalSize;
                    rightSiblingRegion.width( totalSize - regionResizing.width() );
                },
                stop: function(event,ui) {
                    // when done set the page to dirty so that we cant navigate away without a confirmation
                    setLayoutDirty( true );
                    // change the region's model width and height
                    layoutModel.editRegion(regionResizing.attr("id"), function(){
                        this.width  = ui.size.width;
                        this.height = ui.size.height;
                    });
                    if(regionResizing.parent().hasClass("perc-horizontal")) {
                        if(getFixedNoAutoResize(rightSiblingRegion) === "true")
                            return;
                        // if the width changed, update the sibling's width as well
                        if(ui.originalSize.width !== ui.size.width){
                            layoutModel.editRegion(rightSiblingRegion.attr("id"), function(){
                                this.width = totalSize - ui.size.width;
                            });
                        }
                    }
                    currentRegion=regionResizing.attr("id");
                    // update UI by posting model to server and getting new HTML and replacing iframe body content
                    refreshRender();
                }
            });
        }

        function updateWidths() {
            sizeController.puff();
        }

        function insideIframe( $elem ) {
            return iframe[0].contentWindow.jQuery( $elem[0] );
            //return jQuery(iframe[0].contentWindow.jQuery( $elem[0] ));
        }

        /**
         * Initialize the delete region dialog
         */
        function initDeleteRegionDialog() {
            if( !$('#perc-delete-region-dialog').data('dialog') ) {
                $('#perc-delete-region-dialog').perc_dialog( {
                    modal : true,
                    zIndex : 100000,
                    autoOpen: false,
                    width: 500,
                    resizable: false,
                    buttons: {},
                    percButtons: {
                        "Ok": {
                            click:function() {
                                $('#perc-delete-region-dialog').dialog("close");
                            },
                            id: 'perc-delete-region-ok'
                        },
                        "Cancel":   {
                            click:function() {
                                $('#perc-delete-region-dialog').dialog("close");
                            },
                            id: 'perc-delete-region-cancel'
                        }
                    }
                });
            }
        }
        /**
         * Handles click on region's delete button
         * @param regionDiv (DIV) jQuery object representing the HTML DIV whose delete button was clicked.
         * Displays the delete confirmation dialog and allows chosing whether to delete content or not depending on
         * relation of region being deleted with parent and its child regions. Note that the ID of the HTML DIV
         * is the same as the region name editable with the region property dialog.
         */
        function deleteRegion(regionDiv) {
            var regionId = regionDiv.attr('id');

            var regionIdDialogLabel = $('#perc-region-id-label');
            var deleteRegionDialog = $('#perc-delete-region-dialog');
            var deleteContentChoice = $('#perc-delete-content-choice');

            // set the region name in the dialog
            regionIdDialogLabel.html(regionId);

            deleteRegionDialog.dialog('open');
            deleteContentChoice.attr('checked','checked');

            var deletingRegionContentCanBeAdopted = layoutModel.isResponsiveBaseTemplate()?respRegionContentCheck(regionDiv):baseRegionContentCheck(regionDiv);

            // if the content can be salvaged, then enable the delete content checkbox to allow users to uncheck it if they want
            // otherwise disable it and leave it checked as default
            if(deletingRegionContentCanBeAdopted)
                deleteContentChoice.removeAttr('disabled');
            else
                deleteContentChoice.attr('disabled','disabled');

            // handle ok button of dialog
            deleteRegionDialog.parent().find('.perc-ok').off("click").on("click", function() {

                // if OK, then find out what their choice was to delete or not the content
                var deleteContent = $('#perc-delete-content-choice:checked').length!==0;

                layoutController.removeRegion( regionId, deleteContent );

                deleteRegionDialog.dialog("close");
                setLayoutDirty( true );
                refreshRender();
            });
        }

        function baseRegionContentCheck(regionDiv){
            // get the direction classes for parent and region being deleted
            var parentDiv = regionDiv.parent();
            var isRegionVertical = regionDiv.hasClass("perc-vertical");
            var isParentVertical = parentDiv.hasClass("perc-vertical");

            // find out if region being deleted has a single child or if its a leaf
            var childRegionCount  = $(regionDiv).children().children(".perc-region").length;
            var widgetCount       = $(regionDiv).children().children(".perc-widget").length;
            var siblingCount      = $(parentDiv).children(".perc-region").length;
            var hasOneChildRegion = childRegionCount === 1;
            var isLeaf            = childRegionCount === 0;// && widgetCount == 0;
            var isOnlyChild       = siblingCount === 1;
            var hasWidgets        = widgetCount > 0;
            var hasSiblings       = siblingCount > 1;
            var hasSameDirectionAsParent = isRegionVertical === isParentVertical;

            // find out if the content of the region being deleted can be salvaged at all
            // if it's a leaf it cant be salvaged because there is no content
            // if it has a single child, the child can be salvaged
            // if the direction of the region being deleted is the same as the parent, the content can be salvaged
            return ( isLeaf && hasWidgets && !hasSiblings ) ||
                hasOneChildRegion ||
                ( !isLeaf && hasSiblings && hasSameDirectionAsParent );
        }
        function respRegionContentCheck(regionDiv){
            return false;
        }

        /**
         * Callback to edit the widget
         */
        function configureWidget( elem ) {
            var widgetId = elem.attr('widgetid'), widgetDefinitionId = elem.attr('widgetdefid');
            var setProp = function(name,value) {
                layoutModel.editWidget( widgetId, function() {
                    if (name === "sys_perc_name"){
                        this.name = value;
                    }
                    else if (name === "sys_perc_description"){
                        this.description = value;
                    }
                    else {
                        this.properties[ name ] = value;
                    }
                });
                setLayoutDirty(true);
            };
            var properties;
            layoutModel.editWidget( widgetId, function(){
                properties = this.properties;
                if(layoutModel.isTemplate()){
                    properties.sys_perc_name = (typeof(this.name) != "undefined") ? this.name : "";
                    properties.sys_perc_description = (typeof(this.description) != "undefined") ? this.description : "";
                }
            });

            P.widgetPropertiesDialog( setProp, properties, widgetDefinitionId, refreshRender, "UserPref", layoutModel.getWidgetByName);
        }

        /**
         * Retrieves all the regions in the iframe.
         * This method is passed to the region decorator to iterate and
         * decorate all the regions
         * It is also used here locally to iterate over the regions in addDropsSortingResizing()
         */
        function allRegions() {
            return iframe.contents().find('.perc-region');
        }

        /**
         * Retrieves all the widgets in the iframe
         * This method is passed to the widget decorator to iterate and decorate all the widgets
         */
        function allWidgets(){
            // lock widgets that have been defined by the template
            // add class perc-locked so that decoration controller
            // will remove the edit icons and add the locked icon
            var specialWidgets = layoutModel.getSpecialWidgets("Layout");
            if(specialWidgets)
                iframe.contents().find('.perc-widget').each(function() {
                    var self = $(this);
                    var widgetid = self.attr("widgetid");
                    var isTransparent = self.hasClass('perc-widget-transperant');
                    var assetid = self.attr("assetid");
                    if(widgetid in specialWidgets.LockedWidgets)
                        self.addClass("perc-locked");
                    if (widgetid in specialWidgets.TransperantWidgets)
                    {
                        self.addClass("perc-widget-transperant");
                    }
                    else if (layoutModel.isTemplate() && assetid !== "" && !(widgetid in specialWidgets.ContentWidgets))
                    {
                        self.addClass("perc-widget-transperant");
                    }
                });
            return iframe.contents().find(".perc-widget");
        }

        /*
         * Setup the widget library
         */

        /**
         * Fill the widget library with the various widget tools
         */
        function populateWidgetLibrary(event) {
            $('.perc-widget-list').empty();
            $.getJSON($.perc_paths.WIDGETS_ALL + "/type/" + $('.perc-widget-type').val() + "?filterDisabledWidgets=yes", function(js) {
                $.each( js['WidgetSummary'], function( ) {
                    widgetDefs[this.id] = this;
                    $('.perc-widget-list').append( createWidgetLibraryItem( this ) );
                });
                manageCategories();
                addWidgetInfoToolTips(widgetDefs);
                $(".perc-widget-list").width("auto");

                fixIframeHeight();
            });
        }

        /**
         * Fill the widget library with the various widget tools
         */
        function initWidgetLibrary(typeFilter, disabledFilter) {
            $('.perc-widget-list').empty();
            $.getJSON($.perc_paths.WIDGETS_ALL + "/type/" + typeFilter + "?filterDisabledWidgets=" + disabledFilter, function(js) {
                $.each( js['WidgetSummary'], function( ) {
                    allWidgetDefs[this.id] = this;

                });
                addWidgetInfoToolTips(allWidgetDefs);
            });
        }
        function addWidgetInfoToolTips(widgetDefs){
            var cb = function(status, results){
                if (status === $.PercServiceUtils.STATUS_ERROR) {
                    var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                    $.perc_utils.debug(I18N.message("perc.ui.publish.title@Error") + defMsg);
                    return;
                }
                $(results.data.WidgetPackageInfoResult.packageInfoList).each(function(){
                    var widgetInfo = this;
                    var widgetDef = widgetDefs[widgetInfo.widgetName];
                    var wdgElem = $("div.perc-widget-tool[id=widget-" + widgetInfo.widgetName + "-0]").closest("a");
                    wdgElem.tooltip({
                        delay: 800,
                        bodyHandler: function(){
                            //TODO: I18N HTML Below
                            var infoHtml =
                                "<p> Name: " + widgetDef.label + "</p>" +
                                "<p> Description: " + widgetDef.description + "</p>" +
                                "<p class=\"perc-widget-is-responsive-label\" status=\"" + (widgetDef.responsive ? "Yes" : "No") + "\"> Is Responsive: " + (widgetDef.responsive ? "Yes" : "No") + "</p>" +
                                "<p> Provider URL: " + widgetInfo.providerUrl + "</p>" +
                                "<p> Version: " + widgetInfo.version + "</p>";
                            return infoHtml;
                        }
                    });

                });

            };
            var url = $.perc_paths.WIDGET_INFO;
            var widgetIds = [];
            for(var i in widgetDefs)
                widgetIds.push(i);
            var dataObj = {"WidgetPackageInfoRequest":{"widgetNames":widgetIds}};
            $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_POST,false,cb,dataObj);
        }

        /**
         * Calculate the corresponding categories of the selected type
         */
        function manageCategories(){
            var typeSelected = $('.perc-widget-type').val();
            //Get the predefined categories
            var predefinedCategories = [];
            $('.perc-widget-category-predefined').text(function(i, text){
                predefinedCategories[i] = text.toLowerCase();
            });

            //Get the custom categories
            var customCategories = [];
            $.each($('.perc-widget-list .perc-widget-tool'), function(i, widget){
                if (typeof($(widget).data('widget').type) != "undefined") {
                    var type = $(widget).data('widget').type;
                    if (type.toLowerCase() === "custom"){
                        var category = $(widget).data('widget').category;
                        if (typeof(category) != "undefined"){
                            $.each(category.split(","), function(index,value){
                                if (value !== "" && $.inArray(value, customCategories) === -1) { customCategories.push(value); }
                            });
                        }
                    }
                }
            });

            //When change the type selected set all category as default
            var categoryFilter = $('.perc-widget-category');
            categoryFilter.val('all');

            //Determine the options for the category filter
            $('.perc-widget-category-custom').remove();
            if (typeSelected === "all"){
                $('.perc-widget-category-predefined').show();
                if (customCategories.length>0){
                    categoryFilter.append($('<option />').addClass('perc-widget-category-custom').val('other').text('Other'));
                }
            }
            if (typeSelected === "percussion" || typeSelected === "community"){
                $('.perc-widget-category-predefined').show();
            }
            if (typeSelected === "custom"){
                $('.perc-widget-category-predefined').hide();
                $.each(customCategories, function(index, value){
                    categoryFilter.append($('<option />').addClass('perc-widget-category-custom').val(value).text(value.charAt(0).toUpperCase() + value.slice(1)));
                });
            }
        }

        /**
         * Filter the widget library by category
         */
        function filterWidgetLibrary(event) {
            $.each($('.perc-widget-list .perc-widget-tool'), function( ) {
                if(containsCategory(this))
                    $(this).parent("a").show();
                else
                    $(this).parent("a").hide();
            });
            $(".perc-widget-list").width("auto");
            fixIframeHeight();
        }

        /**
         * Check if the widget belong to the current category selected.
         */
        function containsCategory(widget){
            var selectedCategory = $('.perc-widget-category').val();
            if (selectedCategory === "all") { return true; }
            if (selectedCategory === "other"){
                var type = $(widget).data('widget').type;
                return (type.toLowerCase() === "custom");
            }
            if(typeof($(widget).data('widget').category) == 'undefined') { return false; }
            var category = $(widget).data('widget').category.split(",");
            var predefinedCategories = [];
            $('.perc-widget-category-predefined').text(function(i, text){
                predefinedCategories[i] = text.toLowerCase();
            });
            return ($.inArray(selectedCategory, category) !== -1);
        }

        var regionOverflows = {};
        function hideRegionOverflow() {
            // grab all the regions
            var regiones = iframe[0].contentWindow.$(".perc-region");

            // store their overflows temporarily
            // and override the overflows to hidden
            regionOverflows = {};
            regiones.each(function(){
                var regionId  = $(this).attr("id");
                var reg = iframe[0].contentWindow.$(this);
                var overflow = reg.css("overflow");
                regionOverflows[regionId] = overflow;
                $(this).css("overflow","hidden");
            });
        }

        function restoreRegionOverflow() {
            // grab all the regions
            var regiones = iframe[0].contentWindow.$(".perc-region");
            // restore their overflows
            regiones.each(function(){
                var regionId  = $(this).attr("id");
                var overflow  = regionOverflows[regionId];
                $(this).css("overflow", overflow);
            });
        }

        /**
         * Create the a widget library tool html to append
         */
        function createWidgetLibraryItem(w) {
            var wdg;
            wdg = $("<a/>")
                .addClass("perc-tooltip-custom")
                .css("overflow","hidden")
                .append($("<div/>")
                    .css({'position': 'relative'})
                    .addClass("perc-widget-tool")
                    .append($("<img src=\"/Rhythmyx" + w['icon'] + "\" alt=\"\"></img>") )
                    .append($("<div/>")
                        .append($("<nobr/>")
                            .append(w['label']))
                        .addClass("perc-widget-label")
                        .css("overflow", "hidden") )
                    .attr('id',"widget-" + w['id'] + "-" + $('.perc-widget').length)
                    .draggable({
                        appendTo: 'body',
                        refreshPositions: true,
                        helper: 'clone',
                        revert: false,
                        start: function(){
                            $.perc_utils.addAutoScroll();
                            currentDragOverRegion = null;
                            hideRegionOverflow();
                            $.percHideBodyScrollbars();
                        },
                        stop: function() {
                            $.perc_utils.removeAutoScroll();
                            restoreRegionOverflow();
                            $.percShowBodyScrollbars();
                        }
                    })
                    .data( 'widget', w ));

            //Setting the tooltip on entire block
            wdg.attr('alt',wdg.find('nobr').html());
            //Adds widget information to the widget tooltips
            wdg.attr('title',wdg.find('nobr').html());
            return wdg;
        }

        /**
         * This method is called when a modification is made to the layout or when the layout is saved
         * When a layout is modified by adding a new region or widget, this method is called with isDirty = true
         * When a layout is saved, this method is called with isDirty = false
         * @param isDirty If isDirty = true, an onbeforeunload event is bound to the window to call a confirmation method
         * if the user tries to navigate away from the window. If isDirty = false, the onbeforeunload event is cleared
         * so no confirmation is shown if the user navigates away
         */
        function setLayoutDirty(isDirty) {
            dirty = isDirty;

            var modelType = layoutModel.isTemplate() ? "template" : "page";
            dirtyController.setDirty(isDirty, modelType, saveLayout);
        }

        /**
         * Make a service call to get all orphaned asset for a given page
         * @param pageId - Id of the page.
         */

        function populateOrphanAssets() {
            var orphanAssetsContainer = $("#perc-orphan-assets");
            orphanAssetsContainer.html('<a class="mcol-listing perc-listing-type-percRichTextAsset perc-listing-category-ASSET ui-draggable ui-droppable mcol-opened" alt="RichText1" id="perc-finder-listing-16777215-101-721" title="RichText1"><img style="float: left;" src="/Rhythmyx/rx_resources/images/ContentTypeIcons/filetypeIconsRichText.png"><div style="cursor: default; text-overflow: ellipsis; overflow: hidden;" class="perc-finder-item-name" unselectable="on">RichText1</div></a>');
        }
        /**
         * Get a hold of all the regions in the layout and create a tray with all the names of the regions
         * so that you can click on them and select the regions in the layout
         */
        function populateRegionLibrary() {
            // get the list of regions from the layout below and populate the tray in alphabetical order
            var regionLibrary = $("#perc-region-library");
            var regions = iframe.contents().find(".perc-region");
            regionLibrary.empty();
            var regionLibraryHtml = "";
            var regionIdsArray = [];
            regions.each(function(){
                var regionId = $(this).attr("id");
                regionIdsArray.push(regionId);
            });
            regionIdsArray.sort();
            for(r=0; r<regionIdsArray.length; r++){
                regionLibraryHtml += regionToolDivHtml.replace(/_REGION_ID_/g, "perc-re-" + regionIdsArray[r]).replace(/_REGION_LABEL_/g, regionIdsArray[r]);
            }
            regionLibrary.html(regionLibraryHtml);

            // before the tray is populated, a region might have been selected in the layout below
            // get the regionId of the selected region if any and highlight it also in the tray when it opens
            var selectedRegionId = "perc-re-" + iframe.contents().find(".perc-region-selected").attr("id");
            $(".perc-region-library-tool[name="+selectedRegionId+"]").addClass("perc-region-selected");

            // bind click events on each of the regions in the tray so that clicking on them highlitghts the region in the layout
            $(".perc-region-library-tool")
                .on("click",function(){
                    var regionTool = $(this);
                    $(".perc-region-library-tool").removeClass("perc-region-selected");
                    regionTool.addClass("perc-region-selected");
                    var regionId = String(regionTool.attr("name")).replace(/^perc-re-/, '');
                    var region = iframe.contents().find("#"+regionId);
                    region.trigger("click");
                });

            $.PercTextOverflow($(".perc-region-library-tool div"), 122);
            fixIframeHeight();
        }

        /**
         *  Queue of Potentially Overlapping Regions
         *
         *  regionQueue keeps track of region IDs as a widget is dragged over potentially overlapping regions.
         *  When a widget enters a region, the region is enqueued. When a widget exits the region, the region is dequeued
         */
        var regionQueue  = [];

        /**
         *  Stack of Potentially Overlapping Widgets
         *
         *  widgetStacks keeps track of widget IDs as a widget is dragged over potentially overlapping regions and widgets.
         *  When a widget enters a region, an empty stack is added with the region's ID used as key.
         *  When a widget enters another widget, the widget's ID is pushed onto the stack keyed by the parent region's ID.
         *  When a widget exits  another widget, the widget's ID is poped  off  the stack keyed by the parent region's ID.
         */
        var widgetStacks = {};

        /**
         * Called from region.draggable.over. Enqueues region over which we are hovering to a regionQueue
         * and adds an empty stack of widgets keyed by region's ID.
         * @param region over which widget is hovering
         */
        function regionOver(region) {
            var regionId = region.attr("id");
            regionQueue.push(regionId);
            if(!widgetStacks[regionId])
                widgetStacks[regionId] = [];
            updateDropFeedback();
        }

        /**
         * Called from region.draggable.out. Dequeues region over which we are exiting from regionQueue
         * and removes widget from stack keyed by region's ID.
         * @param region over which widget is exiting
         */
        function regionOut(region) {
            var regionId = region.attr("id");
            regionQueue.shift();
            if(widgetStacks[regionId])
                delete widgetStacks[regionId];
            updateDropFeedback();
        }

        /**
         * Called from region.draggable.drop. Clears regionQueue and widgetStacks. Delegates drop to performDrop()
         * @param ui from region.draggable.drop
         * @param region over which widget is exiting
         */
        function regionDrop(ui, region) {
            var widgetType = ui.draggable.data('widget').id;
            performDrop(widgetType, region);
            regionQueue  = [];
            widgetStacks = {};
            updateDropFeedback();
        }

        /**
         * Called from widget.draggable.over. Pushes widget onto widgetStacks keyed by region ID.
         * @param widget over which widget is hovering
         * @param region over which widget is hovering
         */
        function widgetOver(widget, region) {
            var regionId = region.attr("id");
            var widgetId = widget.attr("widgetid");
            if(widgetStacks[regionId]) {
                widgetStacks[regionId].push(widgetId);
            }
            updateDropFeedback();
        }
        /**
         * Called from widget.draggable.out. Removes widget region over which we are exiting from widgetStacks keyed by region ID.
         * @param region over which widget is exiting
         */
        function widgetOut(widget,region) {
            var regionId = region.attr("id");
            var widgetId = widget.attr("widgetid");
            if(widgetStacks[regionId]) {
                removeFromWidgetStack(widgetStacks[regionId], widgetId);
            }
            updateDropFeedback();
        }
        /**
         * Called from widget.draggable.drop. Clears regionQueue and widgetStacks. Delegates drop to performDrop()
         * @param ui from region.draggable.drop
         * @param region over which widget is exiting
         */
        function widgetDrop(ui, widget, region) {
            var widgetType = ui.draggable.data('widget').id;
            performDrop(widgetType, region, widget);
            regionQueue  = [];
            widgetStacks = {};
            updateDropFeedback();
        }

        /**
         * Updates the location, show/hide of the highlight feedback of where the drop will occur.
         * If hovering over a widget it delegates to highlightTopOfWidget.
         * If hovering over a region it delegates to highlightRegionWidgetDrop.
         */
        function updateDropFeedback() {
            layout = iframe.contents();
            percWidgetDropFeedback.hide();
            var currentRegionId = regionQueue[0];
            var currentWidgetId = null;
            if(widgetStacks[currentRegionId])
                currentWidgetId = widgetStacks[currentRegionId][0];
            if(currentWidgetId && currentRegionId) {
                var widget = layout.find(".perc-widget[widgetid='"+currentWidgetId+"']");
                highlightTopOfWidget(widget);
            } else if(currentRegionId) {
                var region = layout.find("#"+currentRegionId);
                highlightRegionWidgetDrop(region);
            } else {
                // noop
            }
        }

        /**
         * If region contains no children, then delegates to highlightTopOfRegion.
         * Otherwise delegates to highlightBottomOfWidget.
         * @param region to be highlighted
         */
        function highlightRegionWidgetDrop(region) {
            var childWidgets = region.find(".perc-widget");
            if(childWidgets.length === 0) {
                highlightTopOfRegion(region);
            } else {
                var lastWidget = $(childWidgets[childWidgets.length-1]);
                highlightBottomOfWidget(lastWidget);
            }
        }

        /**
         * Calculates and highlights top of region
         * @param region to be highlighted
         */
        function highlightTopOfRegion(region) {
            var regionOffset = region.offset();
            var regionTop    = regionOffset.top + PUFF_PADDING;
            var regionLeft   = regionOffset.left + PUFF_PADDING;
            var regionWidth  = region.width();
            highlightWidgetDropFeedback(regionLeft, regionTop, regionWidth);
        }

        /**
         * Calculates and highlights bottom of widget
         * @param widget to be highlighted
         */
        function highlightBottomOfWidget(widget) {
            var widgetOffset = widget.offset();
            var widgetLeft   = widgetOffset.left;
            var widgetWidth  = widget.css("width");
            var widgetTop    = widgetOffset.top;
            var widgetHeight = widget.height();
            var widgetBottom = widgetTop + widgetHeight;
            highlightWidgetDropFeedback(widgetLeft, widgetBottom, widgetWidth);
        }

        /**
         * Calculates and highlights top of widget
         * @param widget to be highlighted
         */
        function highlightTopOfWidget(widget) {
            var widgetOffset = widget.offset();
            var widgetTop    = widgetOffset.top;
            var widgetLeft   = widgetOffset.left;
            var widgetWidth  = widget.css("width");
            highlightWidgetDropFeedback(widgetLeft, widgetTop, widgetWidth);
        }

        /**
         * Repositions, resizes, and shows DIV percWidgetDropFeedback
         */
        function highlightWidgetDropFeedback(left, top, width) {
            percWidgetDropFeedback
                .css("background-color", "#CAF589")
                .css("left",left)
                .css("top", top)
                .css("z-index", "50000")
                .css("position","absolute")
                .css("height", "5px")
                .css("width", width)
                .show();
        }

        /**
         * Actually performs the drop action by adding the widget to a region or above a widget.
         * @param widgetType one of various types of widgets from the widget tray, e.g., percEvent, percFile, percFlash, percForm, percRawHtml
         * @param region parent region of the new widget
         * @param widget (optional) if provided, the new widget will go on top of this widget
         */
        function performDrop(widgetType, region, widget) {

            var currentRegionId = regionQueue[0];
            var currentRegion   = layout.find("#"+currentRegionId);
            var currentWidgetId = null;
            var currentWidget   = null;

            if(widgetStacks[currentRegionId]) {
                currentWidgetId = widgetStacks[currentRegionId][0];
                currentWidget   = layout.find(".perc-widget[widgetid="+currentWidgetId+"]");
            }

            if(currentWidgetId && currentRegionId) {
                layoutController.addWidget(currentRegionId,widgetType,currentWidgetId);
                setLayoutDirty(true);
                refreshRender();
            } else if(currentRegionId) {
                layoutController.addWidget(currentRegionId,widgetType);
                setLayoutDirty(true);
                refreshRender();
            }

            if (layoutModel.isResponsiveBaseTemplate()) {
                notifyIfWidgetNotResponsive(widgetType);
            }

        }

        /**
         * Utility function to remove a widget from a stack
         */
        function removeFromWidgetStack(stack, id) {
            for(i=0; i<stack.length; i++) {
                if(stack[i] === id) {
                    stack.splice(i,1);
                }
            }
        }

        /**
         * Utility function to print a string version of the regionQueue and widgetStacks
         */
        function printQueueAndStacks() {
            console.log([JSON.stringify(regionQueue), JSON.stringify(widgetStacks)]);
        }


        /**
         * Remove support for dropping regions and widgets, moving regions and widgets, and resizing regions
         */
        function removeDropsSortingResizing(status){
            layoutModel.eachRegion(function(){
                if (layoutModel.canOverride(this) || layoutModel.isTemplate()) {
                    var regionId = this.regionId;
                    if (regionId !== "percRoot") {
                        var region = $(iframe.contents().find('#' + regionId)[0]);

                        // we can only add regions in a template, not in a page
                        if (layoutModel.isTemplate()) {
                            removeResizing(region, status);
                        }
                        removeSorting(region, status);
                    }
                }
            });
        }

        /**
         * Add sorting/reordering of regions and widgets
         */
        function removeSorting(region, status){
            if (region.is('.perc-region-leaf')) {
                removeWidgetSorting(region, status);
            }
            else {
                removeRegionSorting(region, status);
            }
        }


        /**
         * Remove sorting/reordering of regions
         */
        function removeRegionSorting(region, status){

            insideIframe(region).sortable({
                disabled: status,
                enable: !status
            });
        }
        /**
         * Remove sorting/reordering of widgets
         */
        function removeWidgetSorting(region, status){
            insideIframe(region).sortable({
                disabled: status,
                enable: !status
            });
        }
        function removeResizing(regionResizing, status){
            insideIframe(regionResizing).resizable({
                disabled: status,
                enable: !status
            });
        }

        $("#perc-error-alert").off("click").on("click",function(){
            $.PercLayoutErrorDialog().openLayoutErrorDialog();
        });

        /**
         * Checks if there are errors in the javascript imports or in the stylesheets files.
         */
        function checkForErrors(){
            // check first if there is a js error
            var percJSErrors = document.getElementById('frame').contentWindow['percGlobalErrors'];
            if (percJSErrors.length > 0) {
                return true;
            }

            // check if there is a css error
            var iframeDoc = document.getElementById('frame').contentWindow.document;
            var ss = iframeDoc.styleSheets;
            for (var i = 0; i < ss.length; i++) {
                try {
                    var myrules = ss[i].cssRules ? ss[i].cssRules : ss[i].rules;
                    var cssRulesSize = myrules.length;
                    if (cssRulesSize === 0) {
                        return true;
                    }
                }
                catch (err) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Disable all the toolbar action when in inspect mode.
         */
        function disableToolbarMenu(){
            //Disable the Widget Tray
            if ($("#perc-wid-lib-expander").hasClass('perc-whitebg')) {
                $.fn.percWidLibMaximizer(P);
            }
            //Disable the Region Tray
            if ($("#perc-region-library-expander").hasClass('perc-whitebg')) {
                $.fn.percRegionLibraryMaximizer(P);
            }
            //Disable all the menu
            $("#perc-layout-menu .perc-lib-expander-div, #perc-layout-menu #perc-dropdown-actions-layout, #perc-layout-menu #perc-dropdown-help-layout, #perc-layout-menu #perc-dropdown-view-layout, #perc-region-library-maximizer, #perc-wid-lib-maximizer").addClass('perc-disable-menu-item');
            $('.perc-overlay-div, #region-tool-disabled').show();
            $("#region-tool").hide();

        }

        /**
         * Enable all the toolbar action when in inspect mode.
         */
        function enableToolbarMenu(){
            //Enable all the menu
            $("#perc-layout-menu .perc-lib-expander-div, #perc-layout-menu #perc-dropdown-actions-layout, #perc-layout-menu #perc-dropdown-help-layout, #perc-layout-menu #perc-dropdown-view-layout, #perc-region-library-maximizer, #perc-wid-lib-maximizer").removeClass('perc-disable-menu-item');
            $('.perc-overlay-div, #region-tool-disabled').hide();
            $("#region-tool").show();

            //Hide the undo button
            $("#perc-undo-tool, #perc-undo-tool-disabled").hide();
        }

        //Initialize inspection tool handler
        var layoutFunctions = {
            removeDropsSortingResizing:removeDropsSortingResizing,
            afterRender:afterRender,
            setLayoutDirty:setLayoutDirty,
            enableToolbarMenu:enableToolbarMenu,
            disableToolbarMenu:disableToolbarMenu,
            dirtyController:dirtyController,
            widgetDecorator:widgetDecorator,
            regionDecorator:regionDecorator,
            layoutController:layoutController,
            cancel:cancel,
            refreshRender:refreshRender
        };
        if(!layoutModel.isResponsiveBaseTemplate())
        {
            $.PercInspectionToolHandler.init(layoutFunctions, model, iframe);
        }
        $.PercEditRegionPropertiesDialog().initEditRegionPropertiesDialog(layoutFunctions, layoutModel, iframe);

    };
})(jQuery, jQuery.Percussion);
