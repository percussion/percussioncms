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

/**
 *
 * @param {Object} !window['Node']
 */
(function($, P){

    var splittedRegionContent = [];
    var undoBuffer = [];
    var allNonInspectableElements = [];

    //Ensure node is defined for IE8 browser
    if (!window['Node']) {
        window.Node = new Object();
        Node.ELEMENT_NODE = 1;
        Node.ATTRIBUTE_NODE = 2;
        Node.TEXT_NODE = 3;
        Node.CDATA_SECTION_NODE = 4;
        Node.ENTITY_REFERENCE_NODE = 5;
        Node.ENTITY_NODE = 6;
        Node.PROCESSING_INSTRUCTION_NODE = 7;
        Node.COMMENT_NODE = 8;
        Node.DOCUMENT_NODE = 9;
        Node.DOCUMENT_TYPE_NODE = 10;
        Node.DOCUMENT_FRAGMENT_NODE = 11;
        Node.NOTATION_NODE = 12;
    }

    //This is a global callback intialized in inspection tool click event callback.
    //this callback is called in afterRender method to add inspection tool related events.
    var reactivateInspectionToolCallback = null;
    var gTemplateInspectionRules = null;
    var _layoutFunctions = null;
    var _model = null;
    var _iframe = null;
    $.PercInspectionToolHandler = {
        init: init,
        saveCallback: saveCallback,
        cancelCallback:cancelCallback,
        afterRenderCallback:afterRenderCallback,
        clearItoolMarkup:clearItoolMarkup
    };

    function init(layoutFunctions, model, iframe){
        _layoutFunctions = layoutFunctions;
        _model = model;
        _iframe = iframe;
        // Render and update the state of Inspect Tool Drop down Menu items on load
        updateInspectToolMenu(false);
        //Bind the Show Error Dialog button

        /**
         * Bind the click event on the Region Tool Inspector button. On click remove all click events
         * bind to .perc-region and .perc-widget
         */
        $("#perc-region-tool-inspector").off("click").on("click",function(event){
            updateRegionInspecToolButton();
            event.stopPropagation();
        });

        //Bind the Undo button

        $("#perc-undo-tool").off("click").on("click",function(){
            performUndo();
        });


        // Bind the mouseOver functionality to Region Inspect Menu Items (Make Row)

        $('#perc-region-tool-menu').on('mouseover', '.perc-dropdown-option-Stacked', function(){
            itoolPreviewMouseOver('perc-row');
        });

        $('#perc-region-tool-menu').on('mouseout', '.perc-dropdown-option-Stacked', function() {
            itoolPreviewMouseOut('perc-row');
        });

        // Bind the mouseOver functionality to Region Inspect Menu Items (Make Row)

        $('#perc-region-tool-menu').on('mouseover', '.perc-dropdown-option-SidebySide', function(){
            itoolPreviewMouseOver('perc-col');
        });

        $('#perc-region-tool-menu').on('mouseout', '.perc-dropdown-option-SidebySide', function(){
            itoolPreviewMouseOut('perc-col');
        });

        $("#perc-region-tool-inspector").on("hover",
            function(e){
                var relatedTarget = $(e.relatedTarget);
                //Internet explorer handle different the relatedTarget on <a> tag
                if ($.browser.msie && relatedTarget.is("ul"))
                    relatedTarget = relatedTarget.find("a:first");
                var menu = $("#perc-region-tool-menu").find("li.current");
                if (!relatedTarget.is("a.perc-dropdown-option") && !relatedTarget.is("span.sf-sub-indicator")) {
                    menu.showSuperfishUl().siblings().hideSuperfishUl();
                }
                else{
                    clearTimeout(menu.parents(['ul.','sf-js-enabled',':first'].join(''))[0].sfTimer);
                }
                if (!$(this).is(".buttonPressed"))
                    $(this).css("background-position", "0px -34px");
            },
            function(e){
                var relatedTarget = $(e.relatedTarget);
                //Internet explorer handle different the relatedTarget on <a> tag
                if ($.browser.msie && relatedTarget.is("ul"))
                    relatedTarget = relatedTarget.find("a:first");
                if (!relatedTarget.is("a.perc-dropdown-option") && !relatedTarget.is("span.sf-sub-indicator")) {
                    $("#perc-region-tool-menu").find("li.current").hideSuperfishUl();
                }
                if (!$(this).is(".buttonPressed"))
                    $(this).css("background-position", "0px 0px");
            }
        );
    }

    /**
     * Callback function that handles the after effects of saving the temaplte
     */
    function saveCallback()
    {
        clearUndoBuffer();
        updateUndoButton();
        if (!($('#perc-region-tool-inspector').hasClass('buttonPressed')))
        {
            $("#perc-undo-tool").hide();
            $("#perc-undo-tool-disabled").hide();
        }
    }

    /**
     * Callback function that handles the afer efects of cancelling a template
     */
    function cancelCallback()
    {
        deactivateInspectionToolButton();
        clearUndoBuffer();
        _layoutFunctions.enableToolbarMenu();
    }

    function afterRenderCallback()
    {
        if(reactivateInspectionToolCallback)
        {
            reactivateInspectionToolCallback();
        }
    }


    //Undo related functions
    function updateUndoButton(){

        if (undoBuffer.length > 0) {
            $("#perc-undo-tool").show();
            $("#perc-undo-tool-disabled").hide();
        }
        else {
            $("#perc-undo-tool").hide();
            $("#perc-undo-tool-disabled").show();
        }
    }

    function performUndo(){
        var tObj = undoBuffer.pop();
        _model.renderByObject(tObj, function(htmlContent){
            window.frames[_iframe.attr("id")].jQuery('body').empty().append(htmlContent).ready(function(){
                _layoutFunctions.afterRender(function(){
                    if (splittedRegionContent.length > 0) {
                        assignWidgetContent();
                        splittedRegionContent.pop();
                        updateUndoButton();
                        _layoutFunctions.removeDropsSortingResizing(true);
                    }
                    if(reactivateInspectionToolCallback)
                    {
                        reactivateInspectionToolCallback();
                    }
                });
            });
        });
    }

    function applyMarkup()
    {
        applyNonInspectableMarkup();
        applyInspectableMarkup();
    }
    function applyNonInspectableMarkup()
    {
        //Markup empty, multi, non-html, page widget, empty html widget regions
        var iframeJQuery = window.frames[0].jQuery;
        iframeJQuery(".perc-region.perc-region-leaf").filter(function(){
            var regWidgets = iframeJQuery(this).find(".perc-widget");
            return regWidgets.lenght !==1 || //Select if there are more than 1 widget
                regWidgets.eq(0).attr("widgetdefid") !== 'percRawHtml' || //Select if the widget is not html
                regWidgets.eq(0).hasClass(".perc-locked") || //Select if the widget is locked
                regWidgets.eq(0).children().length===0 || //If there are no elements under widget
                regWidgets.eq(0).find(".html-sample-content").length === 1 || //Select if the widget has sample content
                (regWidgets.eq(0).children().length === 1 && !regWidgets.eq(0).children().eq(0).is("div"))|| //Select if there is only one element and it is not div
                (regWidgets.eq(0).children().length === 1 && regWidgets.eq(0).children().eq(0).is("div") && regWidgets.eq(0).children().eq(0).children().length === 0 && regWidgets.eq(0).children().eq(0).outerHeight(true) === 0 && regWidgets.eq(0).children().eq(0).innerHeight() === 0);
        }).addClass("perc-region-itool-unselectable");
    }

    function applyInspectableMarkup()
    {
        var iframeJQuery = window.frames[0].jQuery;
        iframeJQuery(".perc-region.perc-region-leaf:not('.perc-region-itool-unselectable')").find(".perc-widget").each(function(){
            iframeJQuery(this).contents().filter(function(){
                return this.nodeType === Node.TEXT_NODE && iframeJQuery(this).text().trim().length > 0;
            }).wrap('<span class="perc-itool-text-node-wrapper"/>');

            if(iframeJQuery(this).children().lenght === 1) //Drill into
            {
                var currentElem = iframeJQuery(this);
                while (currentElem.children().length === 1 && currentElem.children().eq(0).is("div"))
                {
                    currentElem.addClass("perc-itool-region-elem");
                    currentElem = currentElem.find("> div");
                }
                currentElem.children().addClass("perc-itool-selectable-elem");
            }
            else
            {
                iframeJQuery(this).children().each(function(){
                    if(iframeJQuery(this).outerHeight(true) === 0 && iframeJQuery(this).innerHeight() === 0)
                    {
                        iframeJQuery(this).addClass("perc-zero-size-elem");
                    }
                    else
                    {
                        iframeJQuery(this).addClass("perc-itool-selectable-elem");
                    }
                });
            }
        });

    }

    function clearInspectableMarkup()
    {
        var iframeContents = _iframe.contents();
        iframeContents.find(".perc-itool-selectable-elem").off("mouseenter");
        iframeContents.find(".perc-region-itool-unselectable").off("mouseenter");
        iframeContents.find(".perc-region-itool-unselectable").off("mouseleave");
        iframeContents.find(".perc-itool-selectable-elem").removeClass("perc-itool-selectable-elem");
        iframeContents.find(".perc-region-itool-unselectable").removeClass("perc-region-itool-unselectable");
        iframeContents.find(".perc-itool-region-elem").removeClass("perc-itool-region-elem");
    }


    function isUndoAvailable(){
        return undoBuffer.length > 0;
    }

    function clearUndoBuffer(){
        undoBuffer.length = 0;
        splittedRegionContent.length = 0;
    }

    function _initGlobalTemplateInspectionRules()
    {
        var singleFormElementRule = {"apply": function(inspectableElemenets){
                var response = {"status":false,
                    //TODO: I18N TEST
                    message:I18N.message("perc.ui.iframe.view@Page Form Based Sorry"),
                    consoleMsg:I18N.message("perc.ui.iframe.view@Page Form Based Info")
                };
                if(!(inspectableElemenets.lenght ===1 && inspectableElemenets.eq(0).children().length === 1 && inspectableElemenets.eq(0).children().eq(0).is("form")))
                {
                    response.status = true;
                    response.message = "";
                    response.consoleMsg = "";
                }
                return response;
            }};
        gTemplateInspectionRules.push(singleFormElementRule);

        var singleTableElementRule = {"apply": function(inspectableElemenets){
                var response = {"status":false,
                    //TODO: I18N TEST
                    message:I18N.message("perc.ui.iframe.view@Page Table Based Sorry"),
                    consoleMsg:I18N.message("perc.ui.iframe.view@Page Table Based Info")
                };
                if(!(inspectableElemenets.length ===1 && inspectableElemenets.eq(0).children().length === 1 && inspectableElemenets.eq(0).children().eq(0).is("table")))
                {
                    response.status = true;
                    response.message = "";
                    response.consoleMsg = "";
                }
                return response;
            }};
        gTemplateInspectionRules.push(singleTableElementRule);

        var singleNonDivElementRule = {"apply": function(inspectableElemenets){
                var response = {"status":false,
                    //TODO: I18N TEST
                    message:I18N.message("perc.ui.iframe.view@Page Not Inspectable Sorry"),
                    consoleMsg:I18N.message("perc.ui.iframe.view@Page Not Inspectable Info")
                };
                var filteredInspectableElems = inspectableElemenets.filter(function(){
                    var curElem =  window.frames[0].jQuery(this);
                    var isOneNonDivElem = curElem.children().length === 1 && !curElem.children().eq(0).is("div");
                    return !(isOneNonDivElem);
                });
                if(filteredInspectableElems.length >0)
                {
                    response.status = true;
                    response.message = "";
                    response.consoleMsg = "";
                }
                return response;
            }};
        gTemplateInspectionRules.push(singleNonDivElementRule);
    }

    /**
     * Detects and logs whether whether the template is not inspectable due to a known issue.
     * Initializes the global inspection rules if they are not intialized yet.
     */
    function detectAndLogInspectionInfo()
    {
        //If the console object is not present, no need for detetction
        if(!window.console)
            return;
        if(gTemplateInspectionRules == null)
        {
            gTemplateInspectionRules = [];
            _initGlobalTemplateInspectionRules();
        }
        var inspectableElemenets = _iframe.contents().find(".perc-widget[widgetdefid='percRawHtml']:not('.perc-locked')").filter(":only-child").filter(function(){
            var curElem = window.frames[0].jQuery(this);
            var isSampleContent = curElem.find(".html-sample-content").length === 1;
            return !(isSampleContent);
        });

        for(i=0;i<gTemplateInspectionRules.length;i++)
        {
            var resp = gTemplateInspectionRules[i].apply(inspectableElemenets);
            if(!resp.status)
            {
                console.log(resp.consoleMsg);
                break;
            }
        }
    }

    // Update the model with new regions and assign the width and height to each new region
    function updateRegionProperties(updateRegionsArray){
        _model.render(function(htmlContent){
            window.frames[_iframe.attr("id")].jQuery('body').empty().append(htmlContent).ready(function(){
                _layoutFunctions.afterRender(function(){
                    //Handle updates
                    $.each(updateRegionsArray, function(){
                        var regItem = this;
                        _model.editRegion(regItem.regionId, function(){
                            this.width = regItem.width;
                            this.height = regItem.height;
                        });
                    });
                    refreshRenderAddContent();
                });
            });
        });
    }

    // Refresh the iframe layout and assign the content to newly created widgets
    function refreshRenderAddContent(){
        _model.render(function(htmlContent){
            window.frames[_iframe.attr("id")].jQuery('body').empty().append(htmlContent).ready(function(){
                _layoutFunctions.afterRender(function(){
                    if(reactivateInspectionToolCallback)
                    {
                        reactivateInspectionToolCallback();
                    }
                });

                // If splittedRegionContent array is available - iterate over the array and replace the sample content with actual content
                if (splittedRegionContent.length > 0) {
                    assignWidgetContent();
                }
            });
        });
    }

    // Assign the content to newly created widgets.
    function assignWidgetContent(){
        $.each(splittedRegionContent, function(){
            $.each(this, function(){
                var tempContent = this;
                var widgetDiv = _iframe.contents().find('div[widgetid ="' + tempContent.widgetId + '"]');
                try
                {
                    widgetDiv.html(tempContent.content);
                }
                catch(err)
                {
                    //TODO: I18N TEST
                    $.perc_utils.info(I18N.message("perc.ui.iframe.view@Failed to fill widget content") + tempContent.widgetId + I18N.message("perc.ui.iframe.view@Error is") + err);
                }
                widgetDiv.attr('ownerId', tempContent.ownerid);
                if (tempContent.isTransparent) {
                    widgetDiv.addClass('perc-widget-transperant');
                }
                //Remove the height and width attribute of Widget's Region puff
                widgetDiv.parents('.perc-region-puff:first').removeAttr('style').addClass('perc-new-splitted-region');

            });

        });
        _layoutFunctions.removeDropsSortingResizing(true);
        _layoutFunctions.setLayoutDirty(true);
        clearItoolMarkup();
        activateInspectionToolButton();
    }

    /**
     * Toggle the state of the Inspect Too button on click
     *
     */
    function updateRegionInspecToolButton(){
        var inspectorButton = $("#perc-region-tool-inspector");
        var msg = I18N.message("perc.ui.iframe.view@Template Unsaved Changes") +
            I18N.message("perc.ui.iframe.view@Click Dont Save");
        callbackFunction = function(){
            updateUndoButton();
            clearUndoBuffer();
            if (inspectorButton.hasClass('buttonPressed')) {
                deactivateInspectionToolButton();
                //Update the state of menu items in toolbar
                _layoutFunctions.enableToolbarMenu();
            }
            else {
                // checkout the page to current user before entering the inspect mode.
                $.PercWorkflowController().checkOut("percPage",  memento.pageId, function(status)
                {
                    if(status)//Workflow controller presents the appropriate error message to the user if fails to check out.
                    {
                        activateInspectionToolButton();
                        //Update the state of menu items in toolbar
                        _layoutFunctions.disableToolbarMenu();
                    }
                });


            }
            reactivateInspectionToolCallback = null;
        };

        var options = {
            question: msg,
            dontSaveCallback: function(){
                reactivateInspectionToolCallback = callbackFunction;
                _layoutFunctions.cancel();
            }
        };
        var lisDirty = _layoutFunctions.dirtyController.isDirty();
        _layoutFunctions.dirtyController.confirmIfDirty(function(){
            callbackFunction();
        }, function(){
        }, options);
    }

    function removeWidgetRegionDecorations()
    {
        _layoutFunctions.widgetDecorator.unselectAll();
        _layoutFunctions.regionDecorator.unselectAll();
        _layoutFunctions.regionDecorator.removeDecorations();
        _layoutFunctions.widgetDecorator.removeDecorations();
    }
    /**
     * Method to activate the RegionTool button and to unbind the click events on .perc-region and .perc-widget
     */
    function activateInspectionToolButton(){
        updateUndoButton();
        detectAndLogInspectionInfo();
        applyMarkup();
        removeWidgetRegionDecorations();
        var inspectorButton = $("#perc-region-tool-inspector");
        inspectorButton.addClass('buttonPressed');
        inspectorButton.css("background-position", "0px -68px");
        var iframeContents = _iframe.contents();
        //Add events to html widget content.
        var iframeJQuery = window.frames[0].jQuery;

        // Gray out all region puffs
        iframeContents.find('.perc-region-puff').addClass('perc-region-puff-gray');
        iframeContents.find('.perc-new-splitted-region').removeClass('perc-region-puff-gray');

        var highligtherDiv = iframeContents.find("#itool-placeholder-highlighter");
        if (highligtherDiv.length < 1) {
            iframeContents.find(".perc-region:first").append("<div id='itool-placeholder-highlighter' style='display:none;position:absolute;z-index:60000;opacity:0.8;filter:alpha(opacity=80);'></div>");
            highligtherDiv = iframeContents.find("#itool-placeholder-highlighter");
        }

        //Add events to non inspectable events
        addNonInspectableEvents(iframeContents, iframeJQuery, highligtherDiv);
        //Add events to inspectable elements
        addInspectableEvents(iframeContents, iframeJQuery, highligtherDiv);

        iframeContents.find(".perc-region:first").addClass('perc-itool-custom-cursor').on('mouseleave',function(){
            highligtherDiv.hide();
            iframeContents.find(".perc-itool-highlighter").removeClass("perc-itool-highlighter");
        });

        _layoutFunctions.removeDropsSortingResizing(true);

    }

    function addNonInspectableEvents(iframeContents, iframeJQuery, highligtherDiv)
    {
        //Bind mouseenter and mouseleave event to all non-inspectable elements (including non-html widgets)
        iframeContents.find(".perc-region-itool-unselectable").on("mouseenter", function(){
            if(iframeContents.find(".perc-itool-selected-elem").length > 0)
                return;
            var currentElem = iframeJQuery(this);
            currentElem.css('background-color', 'orange').attr('title', 'You cannot further sub-divide this region.').css('cursor', 'default').css('z-index', '1000000');
            //currentElem.PercTooltip();

            //Higlight region of currently selected element with dashed blue border
            iframeContents.find('.perc-region-puff').addClass('perc-region-puff-gray');
            currentElem.closest('.perc-region-puff').removeClass('perc-region-puff-gray');

            iframeContents.find(".perc-itool-highlighter").removeClass("perc-itool-highlighter");
            currentElem.find('div').each(function() {
                var titleValue = iframeJQuery(this).attr('title');
                iframeJQuery(this).attr('tempTitle', titleValue);
                iframeJQuery(this).removeAttr('title');
            });
            highligtherDiv.hide();
        }).on("mouseleave",function(){
            var currentElem = iframeJQuery(this);
            currentElem.find('div').each(function() {
                var tempTitleValue = iframeJQuery(this).attr('tempTitle');
                iframeJQuery(this).attr('title', tempTitleValue);
                iframeJQuery(this).removeAttr('tempTitle');
            });
            currentElem.css('background-color', 'transparent').removeAttr('title');
        });

    }

    function addInspectableEvents(iframeContents, iframeJQuery, highligtherDiv)
    {
        //Bind mouseenter and mouseleave event to all children of inspectable elements
        iframeJQuery('body').on('mouseenter', '.perc-itool-selectable-elem, .perc-itool-multi-selectable-highlighter', function(event){
            //iframeContents.find(".perc-itool-selectable-elem, .perc-itool-multi-selectable-highlighter").mouseenter(function(event){
            var currentElem = iframeJQuery(this);
            var isValidElem = iframeContents.find(".perc-itool-selected-elem").length === 0 ||
                event.shiftKey && iframeJQuery(this).hasClass("perc-itool-multi-selectable-highlighter") ||
                event.shiftKey && iframeJQuery(this).hasClass("perc-itool-multi-selectable");
            if(!isValidElem)
                return;
            //Hide the  perc-itool-multi-selectable-highlighter div so that mousenter event can be fired on below div (.perc-itool-selectable-elem)
            if(currentElem.hasClass('perc-itool-multi-selectable-highlighter')) {
                currentElem.hide();
            }
            iframeContents.find(".perc-itool-highlighter").removeClass("perc-itool-highlighter");
            highligtherDiv.show();

            //Higlight region of currently selected element with dashed blue border
            iframeContents.find('.perc-region-puff').addClass('perc-region-puff-gray');
            currentElem.closest('.perc-region-puff').removeClass('perc-region-puff-gray');

            highligtherDiv.offset({
                "top": currentElem.offset().top,
                "left": currentElem.offset().left
            });
            highligtherDiv.height(currentElem.innerHeight()).width(currentElem.innerWidth());

            currentElem.addClass("perc-itool-highlighter");

            highligtherDiv.off("click").on("click",function(event){
                event.preventDefault();
                itoolSelectionHandler(event);
            });
        });

        //Show the hidden 'perc-itool-multi-selectable-highlighter' sibling on mouseleave of highlighter div
        iframeJQuery('body').on('mouseleave', '#itool-placeholder-highlighter', function() {
            iframeContents.find('.perc-itool-multi-selectable-highlighter').show();
            iframeJQuery(this).hide();
        });
    }
    /**
     * Utility method called when eleement is selected in inspect mode
     */
    function itoolSelectionHandler(){
        var iframeContents = _iframe.contents();
        var iframeJQuery = window.frames[0].jQuery;

        //Mark the highlighted element as selected
        var curSelectedElem = iframeContents.find(".perc-itool-highlighter").removeClass("perc-itool-highlighter").addClass("perc-itool-selected-elem");
        //Hide the overlay highlighter
        iframeContents.find("#itool-placeholder-highlighter").hide();

        iframeJQuery(".perc-text-node-wrapper").contents().unwrap();

        //Add classes to recognize multiselectable elements
        if(curSelectedElem.prev() && !curSelectedElem.prev().hasClass("perc-itool-selected-elem"))
        {
            curSelectedElem.prev().addClass("perc-itool-multi-selectable");
        }
        if(curSelectedElem.next() && !curSelectedElem.next().hasClass("perc-itool-selected-elem"))
        {
            curSelectedElem.next().addClass("perc-itool-multi-selectable");
        }

        highlightSelectedElements(curSelectedElem, false);
        highlightSiblings(curSelectedElem);

        if(curSelectedElem.hasClass("perc-itool-multi-selectable"))
            curSelectedElem.removeClass("perc-itool-multi-selectable");

        updateInspectToolMenu(true);

    }

    /**
     *  Find all the siblings of selected element and put an aboslute Div on each of them with gray border.
     */
    function highlightSiblings(curSelectedElem) {
        var iframeContents = _iframe.contents();
        $(curSelectedElem[0]).siblings().filter(":not('.perc-itool-selected-elem')").each(function(index){
            currentSibling = $(this);
            //Push the zero size div to nearest available sibling
            if(currentSibling.hasClass('perc-zero-size-elem')) {
                if(currentSibling.prev().length > 0) {
                    currentSibling.appendTo(currentSibling.prev());
                    return;
                }
                else{
                    currentSibling.appendTo(currentSibling.next());
                    return;
                }
            }
            iframeContents.find(".perc-region:first").append("<div class = 'perc-sib-highlighter-div-" +index+ " perc-sibling-div' style='border:5px solid grey; display:none;position:absolute;z-index:50000;opacity:1;filter:alpha(opacity=100);'></div>");

            var sibHighlighterDiv = iframeContents.find('.perc-sib-highlighter-div-'+index);
            if(currentSibling.hasClass("perc-itool-multi-selectable"))
                sibHighlighterDiv.addClass("perc-itool-multi-selectable-highlighter");

            _divHighlighter(sibHighlighterDiv, currentSibling);
        });
    }

    /**
     * A utility method to highlight the selected element.
     * @param curSeletedElem - a jquery handler of currently selected element
     * @param isMultiSelected - a flag to identify whether the selected element is a wrapper div containing multiple selected element or selected element itself
     *
     */
    function highlightSelectedElements(curSelectedElem, isMultiSelected)
    {
        var iframeContents = _iframe.contents();
        var greenborderElem = $("<div class='perc-itool-selected-green-border' style='border:5px solid #A2C437; display:none;position:absolute;z-index:50000;opacity:1;filter:alpha(opacity=100);'></div>");
        iframeContents.find(".perc-region:first").append(greenborderElem);
        greenborderElem.on('click',function(){
            if(curSelectedElem.prev().hasClass("perc-itool-selected-elem") &&
                curSelectedElem.next().hasClass("perc-itool-selected-elem"))
                return;
            clearItoolSelection();
        });
        if(isMultiSelected) {
            greenborderElem.css('border-color','#00afea');
        }
        //Show it and position
        greenborderElem.show();
        greenborderElem.offset({
            "top": curSelectedElem.offset().top,
            "left": curSelectedElem.offset().left
        }).height(curSelectedElem.innerHeight() -10 ).width(curSelectedElem.innerWidth()-10);
    }

    /**
     *  Utility method to updated the DOM on mouser over to preview the Stacked or Sibe by Side action
     *  @param (className) : this can be 'perc-row'(if user selects Stacked operation) 'perc-col' (if user selects Side by Side operation)
     *  @callback : callback function.
     */
    function itoolPreviewMouseOver(className, callback) {
        var iframeJQuery = window.frames[0].jQuery;
        var iframeContents = _iframe.contents();
        var selElems = iframeJQuery(".perc-itool-selected-elem");
        //As this function gets fired on mouse over which can happen more than once as user moves the mouse on the menu
        //The following code is put in to detect whether we already handled the mouse over if yes then returns
        //If the selected elements length is zero return from here or
        //if selected element is only one and if it is div and if it has perc-itools-wrapper return from here
        //if the selected element's parent has a perc-itools-wrapper return from here.
        if(selElems.length === 0 ||
            (selElems.length === 1 && selElems.is("div") && iframeJQuery(selElems).hasClass("perc-itools-wrapper")) ||
            (iframeJQuery(selElems[0]).parent().hasClass("perc-itools-wrapper")))
            return;

        selElems.siblings(":not('.perc-itool-selected-elem')").each(function(){
            if(iframeJQuery(this).is("div"))
            {
                iframeJQuery(this).addClass('perc-itools-wrapper perc-make-me-region').addClass(className);
            }
            else
            {
                iframeJQuery(this).wrap("<div class='perc-widget perc-itool-nov-div-wrapper perc-itools-wrapper perc-itool-region-puff'></div>");
            }
        });

        iframeContents.find('.perc-sibling-div').remove();
        if(selElems.length === 1 && selElems.is("div"))
        {
            selElems.addClass('perc-itool-region-puff-self perc-make-me-region perc-self-wrapper perc-itools-wrapper').addClass(className);
            highlightSiblings(selElems);
        }
        else
        {
            selElems.wrapAll("<div  class='perc-widget perc-itool-multi-wrapper perc-itool-nov-div-wrapper perc-itool-region-puff-self perc-self-wrapper perc-itools-wrapper " + className + "'></div>");
            highlightSiblings(iframeJQuery(selElems[0]).parent());
        }
        //Change border color  of all siblings to blue from grey.
        iframeContents.find('.perc-sibling-div').css({'border-color':'#00afea'});
        iframeJQuery('.perc-itool-selected-green-border').remove();
        iframeJQuery(selElems).each(function()
        {
            highlightSelectedElements(iframeJQuery(this), false);
        });

        // If user performs 'Side by Side' action - add the overflow:auto and height 100% to its parent so that below content don't overlap on columns
        if(className === 'perc-col') {
            if(selElems.length === 1) {

                selElems.parent().addClass('perc-clear');
            }

            else {

                selElems.parent().parent().addClass('perc-clear');
            }

        }

        // If there is only one selected element,turn its border into blue. If there are mulitple selected element keep their border green but turn its wrapper div border to blue
        if(selElems.length === 1) {
            iframeJQuery('.perc-itool-selected-green-border').css('border-color', '#00afea');
        }
        else {
            highlightSelectedElements(iframeJQuery('.perc-itool-multi-wrapper'), true);
            iframeJQuery('.perc-itool-selected-green-border').parent('.perc-itool-selected-green-border').css('border-color', '#00afea');
        }

        if (callback) {
            callback();
        }
    }
    /**
     *  Utility method to updated the DOM on mouser over to preview the Stacked or Sibe by Side action
     *  @param (className) : this can be 'perc-row'(if user selects Stacked operation) 'perc-col' (if user selects Side by Side operation)
     *  @callback : callback function.
     */
    function itoolPreviewMouseOut(className, callack) {
        var iframeContents = _iframe.contents();
        var iframeJQuery = window.frames[0].jQuery;
        var selElems = iframeJQuery(".perc-itool-selected-elem");
        if(selElems.length===0)
            return;
        if(selElems.length>1)
            selElems.unwrap();
        //Unwrap the non-div elements
        iframeJQuery('.perc-itool-nov-div-wrapper').contents().unwrap();
        if(className === 'perc-col') {
            selElems.parent().removeClass('perc-clear');
        }
        iframeJQuery(".perc-itools-wrapper").removeClass("perc-itools-wrapper perc-make-me-region").removeClass(className);
        iframeJQuery(".perc-itool-region-puff-self").removeClass("perc-itool-region-puff-self perc-make-me-region perc-self-wrapper").removeClass(className);iframeContents.find('.perc-sibling-div').remove();
        highlightSiblings(selElems);
        iframeJQuery('.perc-itool-selected-green-border').remove();
        iframeJQuery(selElems).each(function()
        {
            highlightSelectedElements(iframeJQuery(this), false);
        });
    }

    /** Utlitiy method to higlight the siblings of selected Div via the the absolute positioned Div with Blue border.
     *  param @highligheterDiv = id of absolute positioned div
     *  param @ selectedElement = id of an element around which blue border need to be shown
     */

    function _divHighlighter(highligheterDiv, selectedElement) {
        highligheterDiv.show();
        highligheterDiv.offset({
            "top": selectedElement.offset().top,
            "left": selectedElement.offset().left
        });
        highligheterDiv.height(selectedElement.innerHeight() - 10).width(selectedElement.innerWidth() - 10);
    }

    /**
     * Clears all the markup added by the inspection tool and unbind the mouse-enter event
     */
    function clearItoolMarkup(){
        var iframeContents =  _iframe.contents();
        var iframeJQuery = window.frames[0].jQuery;
        iframeJQuery(".perc-itool-highlighter").removeClass("perc-itool-highlighter");
        iframeJQuery(".perc-itool-selected-elem").removeClass("perc-itool-selected-elem");
        iframeJQuery("#itool-placeholder-highlighter").remove();
        iframeJQuery(".perc-itool-selected-green-border").remove();
        iframeJQuery(".perc-itool-selected-elem-off").removeClass("perc-itool-selected-elem-off");
        iframeJQuery(".perc-text-node-wrapper").contents().unwrap();
        iframeContents.find("#perc-next-sib-wrapper").remove();
        iframeContents.find("#perc-pre-sib-wrapper").remove();
        iframeContents.find(".perc-pre-sib-wrapper").removeClass("perc-pre-sib-wrapper");
        iframeContents.find(".perc-pre-next-wrapper").removeClass("perc-pre-next-wrapper");
        iframeContents.find('.perc-sibling-div').remove();
        iframeJQuery(".perc-itool-multi-selectable").removeClass('perc-itool-multi-selectable');
        iframeContents.find('.perc-region-puff').removeClass('perc-region-puff-gray');
        iframeContents.find(".perc-itool-selectable-elem").removeClass("perc-itool-selectable-elem");
        //Move out the zero size div element from its parent
        var zeroElemParent = iframeJQuery('.perc-zero-size-elem').parent().parent();
        var zeroSizeElement = iframeJQuery('.perc-zero-size-elem').detach();
        zeroSizeElement.appendTo(zeroElemParent);
        iframeJQuery('.perc-zero-size-elem').removeClass('perc-zero-size-elem');
        clearInspectableMarkup();
    }

    /**
     * Only Clears all the markup added by the inspection tool
     */
    function clearItoolSelection() {
        var iframeContents =  _iframe.contents();
        var iframeJQuery = window.frames[0].jQuery;
        iframeJQuery(".perc-itool-selected-elem").removeClass("perc-itool-selected-elem");
        iframeJQuery("#itool-placeholder-highlighter").remove();
        iframeJQuery(".perc-itool-selected-elem-off").removeClass("perc-itool-selected-elem-off");
        iframeJQuery(".perc-text-node-wrapper").contents().unwrap();
        iframeJQuery(".perc-itool-multi-selectable").removeClass('perc-itool-multi-selectable');
        //Move out the zero size div element from its parent
        var zeroElemParent = iframeJQuery('.perc-zero-size-elem').parent().parent();
        var zeroSizeElement = iframeJQuery('.perc-zero-size-elem').detach();
        zeroSizeElement.appendTo(zeroElemParent);
        iframeContents.find(".perc-itool-selected-green-border").remove();
        iframeContents.find(".perc-pre-sib-wrapper").removeClass("perc-pre-sib-wrapper");
        iframeContents.find(".perc-pre-next-wrapper").removeClass("perc-pre-next-wrapper");
        iframeContents.find(".perc-itool-region-puff").removeClass("perc-itool-region-puff");
        iframeContents.find('.perc-sibling-div').remove();
        updateInspectToolMenu(false);
        activateInspectionToolButton();
    }

    /**
     * Method to deactivate the RegionTool button and to bind the click events back to .perc-region and .perc-widget
     */
    function deactivateInspectionToolButton(){
        clearUndoBuffer();
        updateUndoButton();
        var inspectorButton = $("#perc-region-tool-inspector");
        inspectorButton.removeClass('buttonPressed');
        inspectorButton.css("background-position", "0px -34px");
        _iframe.contents().find(".perc-itool-custom-cursor").removeClass('perc-itool-custom-cursor');
        _layoutFunctions.widgetDecorator.addClicks();
        _layoutFunctions.regionDecorator.addClicks();
        clearItoolMarkup();
        updateInspectToolMenu(false);
        _layoutFunctions.removeDropsSortingResizing(false);

    }

    /**
     * Update the Region Tool Inspect Menu on element selection
     */
    function updateInspectToolMenu(status){

        var layoutRegionToolMenu = $("#perc-region-tool-menu");
        layoutRegionToolMenu.html("");
        layoutRegionToolMenu.PercDropdown({
            percDropdownRootClass: "perc-region-tool-menu",
            percDropdownOptionLabels: ["", "Stacked", "Side by Side"],
            percDropdownCallbacks: [function(){
            }, _splitRegionHorizontal, _splitRegionVertical],
            percDropdownCallbackData: ["", "Stacked", "Side by Side"],
            percDropdownDisabledFlag: [false, status, status],
            percDropdownItemImage: ['', '../images/images/menuIconStacked.png', '../images/images/menuIconSide.png'],
            percDropdownDisabledItemImage: ['', '../images/images/menuIconStackedGray.png', '../images/images/menuIconSideGray.png']
        });
    }

    function _splitRegionHorizontal(){
        _guardSplitRegions(true);
    }

    function _splitRegionVertical(){
        _guardSplitRegions(false);
    }

    // The gurad function will make sure that before we execute split functionality- we have identified the siblings
    // of selected element i.e we are replicating the mouserOver functionality.
    // This is especially done for the autotest where mouseover can't be fire.
    function _guardSplitRegions(flag){
        var iframeJQuery = window.frames[0].jQuery;
        if (iframeJQuery('.perc-itools-wrapper').length === 0) {
            if (flag) {
                itoolPreviewMouseOver('perc-row',function(){
                    _splitRegions(flag);
                });
            }
            else {
                itoolPreviewMouseOver('perc-col',function(){
                    _splitRegions(flag);
                });
            }
        }
        else {
            _splitRegions(flag);
        }
    }

    function _splitRegions(isHorizontal){
        updateInspectToolMenu(false);
        var currTemplObj = jQuery.extend(true, {}, _model.getTemplateObj());
        undoBuffer.push(currTemplObj);
        clearItoolMarkup();
        var regDirection = isHorizontal ? 'south' : 'left';
        var iframeJQuery = window.frames[0].jQuery;
        iframeJQuery(".perc-zero-size-div").removeClass("perc-zero-size-div");
        var selectedEle = iframeJQuery('.perc-itool-region-puff-self');
        var parentRegionPuff = selectedEle.parents('.perc-region-puff:first').attr('id');
        var oriWidgetId = selectedEle.parents(".perc-widget[widgetdefid='percRawHtml']").attr('widgetid');
        var isTransparent = selectedEle.parents(".perc-widget[widgetdefid='percRawHtml']").hasClass('perc-widget-transperant');
        var widOwnerId = selectedEle.parents('.perc-widget-puff:first').attr('ownerId');
        iframeJQuery.fn.reverse = [].reverse;
        var selfRegion = iframeJQuery('.perc-self-wrapper');

        // If the selected element was drilled down (because of several child-only divs in the upper levels)
        // then the ancient divs need to be retrieved to convert them to regions as well
        var parentElems = [];
        var parentElement = iframeJQuery('.perc-itools-wrapper').parent();
        while (parentElement.not(".perc-widget[widgetdefid='percRawHtml']").length > 0)
        {
            parentElems.push(parentElement);
            parentElement = parentElement.parent();
        }
        parentElems = iframeJQuery(parentElems).reverse();

        //Capture content into global arrayVariable of widget id and $element
        var wrappedElems = isHorizontal ? iframeJQuery('.perc-itools-wrapper') : iframeJQuery('.perc-itools-wrapper').reverse();

        var updateRegionsArray = [];

        // Perform the conversion of the ancient divs into regions (the parent is always the last div just converted)
        // TODO: refactor this to use the portion of code below: wrappedElems.each(function(index){
        // Need to add the corresponding classes to mark the region and resolve the parent problem (the code below
        // always adds the content to the same las region created.
        var lastParentRegionPuff = parentRegionPuff;
        parentElems.each(function(index){
            var thisRegion = $(this);
            var thisRegionId = thisRegion.attr("id");
            var originalRegionId;

            var classesToApply = "";
            var regionClasses = thisRegion.attr('class');
            userRegionClasses = regionClasses ? regionClasses.split(" ") : [];
            for (i = 0; i < userRegionClasses.length; i++) {
                var uclass = userRegionClasses[i];
                if (uclass.indexOf("perc-") === -1)
                    classesToApply += uclass + " ";
            }
            // We use "center" direction, since the temp regions are not deleted later in this case, so they
            // need to be removed once the addRegion method clones the region created. If using south, the
            // temp region is also created (without any inner content) as well as the user region (both as siblings)
            _layoutFunctions.layoutController.addRegion(lastParentRegionPuff, "center", function(region){
                originalRegionId = region.regionId;
                _model.editRegion(region.regionId, function(){
                    if (thisRegionId && thisRegionId !== '')
                        this.regionId = thisRegionId;
                    this.cssClass = classesToApply;

                    if (!isHorizontal) {
                        var regItem = {};
                        regItem.regionId = this.regionId;
                        regItem.width = thisRegion.width();
                        regItem.height = thisRegion.height();
                        updateRegionsArray.push(regItem);
                    }
                });
            });

            lastParentRegionPuff = thisRegionId && thisRegionId !== '' ? thisRegionId : originalRegionId;
            iframeJQuery(lastParentRegionPuff).addClass('perc-region-puff-gray');
        });

        wrappedElems.each(function(index){
            var widgetContent = $(this);
            var regionId = "";
            var classesToApply = "";
            if(widgetContent.is('.perc-make-me-region')){
                var selfRegion = $(this);
                regionId = selfRegion.attr('id');
                var regionClasses = selfRegion.attr('class');
                userRegionClasses = regionClasses ? regionClasses.split(" ") : [];
                for (i = 0; i < userRegionClasses.length; i++) {
                    var uclass = userRegionClasses[i];
                    if (uclass.indexOf("perc-") === -1)
                        classesToApply += uclass + " ";
                }
            }
            _layoutFunctions.layoutController.addRegion(lastParentRegionPuff, regDirection, function(region){
                _model.editRegion(region.regionId, function(){
                    if (regionId && regionId !== '')
                        this.regionId = regionId;
                    this.cssClass = classesToApply;

                    if (!isHorizontal) {
                        var regItem = {};
                        regItem.regionId = this.regionId;
                        regItem.width = widgetContent.width();
                        regItem.height = widgetContent.height();
                        updateRegionsArray.push(regItem);
                    }
                });
                var splittedContent = [];
                _layoutFunctions.layoutController.addWidget(region.regionId, "percRawHtml", null, function(widgetId){
                    var scontent = {};
                    scontent.widgetId = widgetId;
                    scontent.content = widgetContent.html();
                    scontent.isTransparent = isTransparent;
                    scontent.ownerid = widOwnerId;
                    splittedContent.push(scontent);
                });
                splittedRegionContent.push(splittedContent);
            });
        });
        _layoutFunctions.layoutController.removeWidgetParentRegion(oriWidgetId, true);
        updateRegionProperties(updateRegionsArray);

    }

})(jQuery, jQuery.Percussion);
