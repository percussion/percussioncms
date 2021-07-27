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
    P.contentView = function( root, model ) {
        var widgetMenu = [];
        var deleteMenu =
            {
                name: 'delete',
                img: function(elem){
                    var imgSrc = '/cm/images/icons/editor/delete';
                    var assetInfo = model.getAssetDropCriteria()[elem.attr('widgetid')];
                    var assetId = assetInfo && assetInfo.locked?elem.attr('assetid'):"";
                    if(!assetId)
                    {
                        imgSrc = '/cm/images/icons/editor/deleteInactive';
                    }
                    return imgSrc;
                },
                tooltip: function(elem){
                    var wId = elem.attr('widgetid');
                    var assetInfo = model.getAssetDropCriteria()[wId];
                    var assetId = assetInfo && assetInfo.locked?elem.attr('assetid'):"";
                    var tooltip = "";
                    if(assetId)
                        tooltip = I18N.message("perc.ui.content.view@Delete Local Content");
                    if(assetId && assetInfo.assetShared)
                        tooltip = I18N.message("perc.ui.content.view@Remove Shared Asset");
                    return tooltip;
                },
                callback: function(elem) {
                    var wId = elem.attr('widgetid');
                    var assetInfo = model.getAssetDropCriteria()[wId];
                    var msg = I18N.message("perc.ui.content.view@Double Check Delete");
                    var title = I18N.message("perc.ui.content.view@Delete Local Content");
                    if(typeof(assetInfo) != 'undefined' && assetInfo.assetShared)
                    {
                        msg = I18N.message("perc.ui.content.view@Double Check Remove") + "\n" + I18N.message("perc.ui.content.view@Will Not Delte Asset");
                        title = I18N.message("perc.ui.content.view@Remove Shared Asset");
                    }
                    var options = {title: title,
                        question:msg,
                        type:"YES_NO",
                        cancel:function(){},
                        success:function(){model.clearAsset( elem.attr('widgetid'), elem.attr('widgetdefid'), elem.attr('assetid'), renderContent );}
                    };
                    var assetId = assetInfo && assetInfo.locked?elem.attr('assetid'):"";
                    if(assetId)
                        $.perc_utils.confirm_dialog(options);
                }};
        var editMenu =
            {
                name: 'edit',
                img: function(elem){
                    var wId = elem.attr('widgetid');
                    var assetInfo = model.getAssetDropCriteria()[wId];
                    var imgSrc = '/cm/images/icons/editor/edit';
                    if(typeof(assetInfo) != 'undefined' && assetInfo.assetShared)
                    {
                        imgSrc = '/cm/images/icons/editor/editAsset';
                    }

                    if(model.getWidgetContentTypes(wId) === "")
                        imgSrc = '/cm/images/icons/editor/editInactive';
                    else if(model.isTemplate() || model.isLandingPage())
                    {
                        var wDef = elem.attr('widgetdefid');
                        if((model.isTemplate() && typeof model.getWidgetPrefs(wDef) !== 'undefined' && model.getWidgetPrefs(wDef).attr("is_editable_on_template") === "false") || (model.isPage() && model.isLandingPage() && wDef === "percTitle")) {
                            imgSrc = '/cm/images/icons/editor/editInactive';
                        }
                    }
                    if(model.isTemplate() && assetInfo && !assetInfo.locked  && elem.attr('assetid') && typeof model.getWidgetPrefs(wDef) !== 'undefined' && model.getWidgetPrefs(wDef).attr("is_editable_on_template") !== "false")
                    {
                        imgSrc += "Middle";
                    }
                    return imgSrc;
                },
                tooltip: function(elem){
                    var wId = elem.attr('widgetid');
                    var assetInfo = model.getAssetDropCriteria()[wId];
                    var tooltip = "Edit local content";
                    if(typeof(assetInfo) != 'undefined' && assetInfo.assetShared)
                        tooltip = "Edit shared asset";
                    return tooltip;
                },
                callback: function(elem) {
                    if(model.getWidgetContentTypes(elem.attr('widgetid')) !== "" )
                    {
                        var wDef = elem.attr('widgetdefid');
                        if((model.isTemplate() && typeof model.getWidgetPrefs(wDef) !== 'undefined' && model.getWidgetPrefs(wDef).attr("is_editable_on_template") === "false") ||
                            (model.isPage() && model.isLandingPage() && wDef === "percTitle"))
                        {
                            return;
                        }
                        var assetInfo = model.getAssetDropCriteria()[elem.attr('widgetid')];
                        var assetId = assetInfo && assetInfo.locked?elem.attr('assetid'):"";
                        var isSharedAsset = assetInfo && assetInfo.assetShared;
                        var widgetData = {
                            widgetid: elem.attr('widgetid'),
                            widgetdefid: elem.attr('widgetdefid'),
                            widgetName: elem.attr('widgetName')
                        };
                        model.configureAsset( widgetData, assetId, isSharedAsset, renderContent );
                    }
                }};
        var promoteMenu = null;
        if(model.isTemplate())
        {
            promoteMenu =
                {
                    name: 'promote',
                    img: function(elem){
                        var imgSrc = null;
                        var assetInfo = model.getAssetDropCriteria()[elem.attr('widgetid')];
                        var assetId = assetInfo && !assetInfo.locked?elem.attr('assetid'):"";
                        var wDef = elem.attr('widgetdefid');
                        if(assetId && typeof model.getWidgetPrefs(wDef) !== 'undefined' && model.getWidgetPrefs(wDef).attr("is_editable_on_template") !== "false")
                        {
                            imgSrc = '/cm/images/icons/editor/promote';
                        }
                        return imgSrc;
                    },
                    tooltip: function(elem){
                        var tooltip = "";
                        var assetInfo = model.getAssetDropCriteria()[elem.attr('widgetid')];
                        var assetId = assetInfo && !assetInfo.locked?elem.attr('assetid'):"";
                        if(assetId)
                        {
                            tooltip = I18N.message("perc.ui.content.view@Promote Content");
                        }
                        return tooltip;
                    },
                    callback: function(elem) {
                        var assetInfo = model.getAssetDropCriteria()[elem.attr('widgetid')];
                        var assetId = assetInfo && !assetInfo.locked?elem.attr('assetid'):"";
                        //If there is an asset id but the assetInfo.existingAsset is false then
                        //we need to get the asset info from page asset drop criteria
                        //This code is becoming messy, @TODO refactoring.
                        if((assetInfo && !assetInfo.existingAsset) && elem.attr('assetid'))
                        {
                            assetInfo = model.getPageAssetDropCriteria()[elem.attr('widgetid')];
                        }
                        if(assetId)
                        {
                            var widgetData = {
                                widgetid: elem.attr('widgetid'),
                                widgetdefid: elem.attr('widgetdefid'),
                                widgetName: elem.attr('widgetName')
                            };
                            model.promoteAsset(assetId, widgetData, assetInfo.assetShared, renderContent);
                        }
                    }};
        }
        widgetMenu.push(deleteMenu);
        widgetMenu.push(editMenu);
        if(promoteMenu)
        {
            widgetMenu.push(promoteMenu);
        }

        var widgetDecorator = P.decorationController( allWidgets, 'perc-widget-puff', 'perc-widget-active', widgetMenu);

        $('#show-hide-decorations').off("click").on("click", function(){
            widgetDecorator.visible( !widgetDecorator.visible());
        });

        var iframe = $("#frame");
        var currentRegionId;
        var regions;
        var widgets;

        setAssetDrops();
        renderContent();

        /**
         * Listen for asset delete events and refresh view
         * register with the finder to be notified if any assets are being deleted
         * so that we can refresh the page and reflect the changes after delete
         */
        $.perc_finder().addActionListener(function(action, data){
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
                    var currentTabIndex = $("#tabs").tabs('option', 'selected');
                    if(currentTabIndex === 1) renderContent();
                } else if(currentView === $.PercNavigationManager.VIEW_EDITOR) {
                    var currentTabIndex = $("#perc-pageEditor-tabs").tabs('option', 'selected');
                    if(currentTabIndex === 0) renderContent();
                }
            }
        });
        /**
         * Returns a list of all the widgets in the iframe.
         * If the widgets have been locked in the model,
         * then they are marked as perc-locked here to enforce icon rendering
         * This method is used by the widget decorator to iterate over all the widgets
         * and apply the puff decoration and menu icons
         */
        function allWidgets() {
            var specialWidgets = model.getSpecialWidgets("Content");
            if(specialWidgets)
                root.contents().find('.perc-widget').each(function()
                {
                    var widgetid = $(this).attr("widgetid");
                    if(widgetid in specialWidgets.LockedWidgets)
                        $(this).addClass("perc-locked");
                    if(widgetid in specialWidgets.TransperantWidgets)
                        $(this).addClass("perc-widget-transperant");
                });
            return root.contents().find('.perc-widget');
        }

        function setAssetDrops(){
            allWidgets().each( function(){ addWidgetAssetDrops( $(this) ); } );
        }

        function insideIframe( $elem ) {
            return iframe[0].contentWindow.jQuery( $elem );
        }
        var overlap = 0;
        var overlapContentWidgets = 0;
        function addWidgetAssetDrops(widget) {

            var widgetid = widget.attr('widgetid');
            var ctypes = model.getWidgetContentTypes( widgetid );

            function accepts(item)
            {
                var ctype = item.data('spec') && item.data( 'spec' ).type;
                if (ctype === undefined || ctype === "")
                {
                    ctype = item.data('percRowData') && item.data( 'percRowData' ).type;
                }
                var accept = ctype && ctypes && ( ctype === ctypes || $.grep( ctypes, function(ct) { return ct === ctype; } ).length );
                return accept;
            }
            var inside = insideIframe(widget);
            if(typeof(inside.draggable) == 'function')
            {
                insideIframe(widget).draggable({
                    scope: 'default',
                    revert: true,
                    drag: function(event, ui) {
                        if(event.target.innerText.length <= 0)
                            return false;
                    }
                });
                insideIframe(widget).droppable({
                    // only interact with iframe draggables
                    scope: 'default',
                    tolerance : 'pointer',
                    // as you hover over the widget, update cursor and background
                    over : function(evt, ui) {

                        var parentRegionId = $(this).attr("id");

                        overlapContentWidgets++;

                        document.body.style.cursor="default";
                        if(parentRegionId === currentRegionId || overlapContentWidgets === 1) {
                            // clear background of all other widgets
                            widgets.each(function(){
                                $(this).css("background-color", "");
                            });
                            // highlight this widget
                            $(this).css("background-color", "#CAF589");
                        }
                        //document.body.style.cursor="not-allowed";
                    },
                    // as you hover away from the widget, update cursor and background
                    out : function(evt, ui) {
                        overlapContentWidgets--;
                        document.body.style.cursor="default";
                        $(this).css("background-color", "");
                    },
                    drop : function(event, ui) {
                        overlapContentWidgets--;
                        // when you drop on widget, update cursor and background
                        document.body.style.cursor="default";
                        $(this).css("background-color", "");

                        var draggedWidget = "";
                        var dropOnWidget =  widget;

                        widgets.each(function(){
                            if($(this).attr('widgetid') === ui.draggable[0].getAttribute('widgetid')){
                                draggedWidget = $(this);
                            }
                        });
                        var isEmptyDropOnWidget = false;
                        if(event.target.innerText.length === 0) {
                            if(ui.draggable[0].innerText.length !== 0 && evt.target.getAttribute('widgetdefid') === ui.draggable[0].getAttribute('widgetdefid')) {
                                var customText = event.target.innerHTML;
                                dropOnWidget[0].innerHTML = ui.draggable[0].innerHTML;
                                draggedWidget[0].innerHTML = customText;
                            }
                        }

                        var draggedWidgetAssetInfo = model.getAssetDropCriteria()[ui.draggable[0].getAttribute('widgetid')];
                        var dropOnWidgetAssetInfo = model.getAssetDropCriteria()[widget.attr('widgetid')];

                        var dropOnWidgetData = {
                            widgetid: widget.attr('widgetid'),
                            widgetdefid: widget.attr('widgetdefid'),
                            widgetName: widget.attr('widgetName'),
                            ownerid: draggedWidgetAssetInfo.ownerId,
                            assetid: draggedWidget.attr('assetid')
                        };

                        var draggetWidgetData = {
                            widgetid: draggedWidget.attr('widgetid'),
                            widgetdefid: draggedWidget.attr('widgetdefid'),
                            widgetName: draggedWidget.attr('widgetName'),
                            ownerid: dropOnWidgetAssetInfo.ownerId,
                            assetid: dropOnWidget.attr('assetid')
                        };

                        if(isEmptyDropOnWidget) {
                            model.setAssetRelationship( dropOnWidgetData, draggedWidget.attr('assetid'), false, renderContent );
                        } else if(dropOnWidgetData.widgetdefid===draggetWidgetData.widgetdefid){
                            model.updateAssetRelationship( dropOnWidgetData, draggedWidget.attr('assetid'), draggedWidgetAssetInfo.relationshipId, function(){
                                model.updateAssetRelationship( draggetWidgetData, dropOnWidget.attr('assetid'), dropOnWidgetAssetInfo.relationshipId, function(){
                                    updateOrphanAssetTray(ui.draggable);
                                    renderContent();
                                }, null);

                            }, function (status, message) {
                                handleUpdateAssetRelationshipError(message, ui.draggable);
                            });

                        }

                    }
                });
            }

            widget.droppable({
                scope: 'default',
                tolerance : 'pointer',
                // as you hover over the widget, update cursor and background
                over : function(evt, ui) {
                    var parentRegionId = $(this).attr("id");

                    overlap++;
                    if(accepts(ui.draggable)) {
                        document.body.style.cursor="default";
                        if(parentRegionId === currentRegionId || overlap === 1) {
                            // clear background of all other widgets
                            widgets.each(function(){
                                $(this).css("background-color", "");
                            });
                            // highlight this widget
                            $(this).css("background-color", "#CAF589");
                        }
                    } else
                        document.body.style.cursor="not-allowed";

                },
                // as you hover away from the widget, update cursor and background
                out : function(evt, ui) {
                    overlap--;
                    document.body.style.cursor="default";
                    $(this).css("background-color", "");
                },
                drop : function(evt, ui) {
                    overlap--;
                    // when you drop on widget, update cursor and background
                    document.body.style.cursor="default";
                    $(this).css("background-color", "");

                    // if item is not accepted, returnSpec
                    if(!accepts(ui.draggable))
                        return false;

                    // do not accept drops if this widget is locked
                    if(widget.hasClass('perc-locked')) {
                        return false;
                    }
                    var assetid = "";
                    var relationshipId = "";
                    var spec = ui.draggable.data('spec');

                    // This needs to be added since the percRowData from the list view has the specification in search mode
                    if (typeof(spec) == "undefined"){
                        spec = ui.draggable.data('percRowData');
                    }

                    if (typeof(spec) != "undefined"){
                        assetid = (typeof(spec.id) != "undefined")? spec.id : ui.draggable.data('percRowData').id;
                        relationshipId = (typeof(spec.relationshipId) != "undefined")? spec.relationshipId : "";
                    }

                    if( assetid )
                    {
                        var assetInfo = model.getAssetDropCriteria()[widget.attr('widgetid')];
                        var curAsset = assetInfo && assetInfo.locked?widget.attr('assetid'):"";
                        var widgetData = {
                            widgetid: widgetid,
                            widgetdefid: widget.attr('widgetdefid'),
                            widgetName: widget.attr('widgetName'),
                            relationshipId: assetInfo.relationshipId
                        };
                        var options = {title:I18N.message("perc.ui.content.view@Content Replace Warning"),
                            question:I18N.message("perc.ui.content.view@Asset Dropping") + "<br/>" + I18N.message("perc.ui.content.view@Continue"),
                            type:"YES_NO",
                            cancel:function(){},
                            success:function(){
                                if (relationshipId === "")
                                    model.setAssetRelationship( widgetData, assetid, true, renderContent );
                                else {
                                    model.updateAssetRelationship( widgetData, assetid, relationshipId, function(){
                                        updateOrphanAssetTray(ui.draggable);
                                        renderContent();
                                    }, function (status, message) {
                                        handleUpdateAssetRelationshipError(message, ui.draggable);
                                    });
                                }
                            }
                        };

                        if(curAsset)
                        {
                            $.perc_utils.confirm_dialog(options);
                        }
                        else
                        {
                            if (relationshipId === "")
                                model.setAssetRelationship( widgetData, assetid, true, renderContent );
                            else{
                                model.updateAssetRelationship( widgetData, assetid, relationshipId, function(){
                                    updateOrphanAssetTray(ui.draggable);
                                    renderContent();
                                }, function (status, message) {
                                    handleUpdateAssetRelationshipError(message, ui.draggable);
                                });
                            }
                        }
                    }
                }
            });
        }

        /**
         * Shows an alert with the error message and then refreshes the orphan
         * assets tray.
         */
        function handleUpdateAssetRelationshipError(message, draggable) {
            var settings = {
                title: I18N.message("perc.ui.page.general@Warning"),
                content: message,
                okCallBack: function() {
                    updateOrphanAssetTray(draggable);
                }
            };
            $.perc_utils.alert_dialog(settings);
        }

        function updateOrphanAssetTray(draggedAsset){
            draggedAsset.remove();
            var orphanAssetsContainer = $(".perc-orphan-assets-list");
            if(orphanAssetsContainer.find(".perc-orphan-asset").length === 0){
                $.fn.percOrphanAssetsMaximizer(P);
                $("#perc_orphan_assets_expander").addClass("perc-disabled").off();
                $("#perc_orphan_assets_maximizer").addClass("perc-disabled");
            }
        }

        function renderContent() {
            model.renderAll(root, function() {
                afterRender();
                $("#frame").attr("perc-view-type","content");
                var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
                if(frwrapper != null)
                    frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");
                var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');
                if(tbwrapper != null)
                    tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");
            });
        }

        /**
         * HTML fixes when in edit mode
         */
        function sanitizeHtml()
        {
            $.perc_utils.handleLinks(root);
            $.perc_utils.handleObjects(root);
        }

        function afterRender()
        {
            widgetDecorator.refresh();
            setAssetDrops();
            sanitizeHtml();
            root.contents().find("body")
                .css("z-index","-1000")
                .css("position","static")
                .off().on("click", function() {
                widgetDecorator.unselectAll();
            });
            overlap = 0;

            iframe = $("#frame");

            // grab all the regions and widgets
            regions = iframe.contents().find(".perc-region");
            widgets = iframe.contents().find(".perc-widget");

            regions.on("mouseover", (function(event){
                currentRegionId = $(this).attr("id");
                event.stopPropagation();
            }));
        }

        /**
         * Bind the click event to the JavaScript Off menu. Sets the JavaScriptOff to true or false depending on the
         * current status. Calls the model#initRender to reinitialize the view.
         */
        $(document).ready(function() {
            $("#perc-content-menu a.perc-dropdown-option-DisableJavaScript").off("click").on("click", function() {
                var scriptOff = I18N.message( "perc.ui.menu@JavaScript Off" );
                var scriptOn = I18N.message( "perc.ui.menu@JavaScript On" );
                if($(this).text() === scriptOff)
                {
                    $(this).text(scriptOn);
                    $(this).attr("title", I18N.message("perc.ui.content.view@Turns On JavaScript"));
                    model.setJavaScriptOff(true);
                    renderContent();
                }
                else
                {
                    $(this).text(scriptOff);
                    $(this).attr("title", I18N.message("perc.ui.content.view@Turns Off JavaScript"));
                    model.setJavaScriptOff(false);
                    renderContent();
                }
            });
        });
    };
})(jQuery, jQuery.Percussion);
