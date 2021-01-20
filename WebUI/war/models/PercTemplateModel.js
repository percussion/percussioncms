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

(function($, P)
{
    var modelObject;
    P.templateModel = function(templateManager, templateId, callback)
    {
        var utils = $.perc_utils;
        var templObj;

        var root;

        var tempElement = $('<div/>');

        // This is a CSS Object to compartmentalize editing CSS operations easily
        // within the CSS editing controller. The members here are marshalled back
        // into the template object upon save.
        var cssObj = {
            themeName: null,
            cssOverride: null
        };

        var regionIds = {};
        var regcount = 1;

        //Local variable to hold the javascript off status.
        var javaScriptOff = false;

        var assetDropCriteria = {};
        var pageAssetDropCriteria = {};

        // TODO: move this service to the controller
        // but leave it here for now because Jason's design seems to depend
        // on the model having access to the service layer
        var templateService = $.PercTemplateService();

        var widgetContentTypes = {};
        var widgetPrefs = {};

        load();

        modelObject = {
            save: save,
            load : load,
            editWidget : editWidget,
            editRegion : editRegion,
            editRegionParent : editRegionParent,
            eachRegion : eachRegion,
            getRegionPosition : getRegionPosition,
            newRegion : newRegion,
            newResponsiveRegion : newResponsiveRegion,
            getRoot : getRoot,
            render: render,
            renderAll: renderAll,
            getTemplateRegionIds : getTemplateRegionIds,
            canOverride : canOverride,
            getName : getName,
            getAssetDropCriteria: getAssetDropCriteria,
            getPageAssetDropCriteria : getPageAssetDropCriteria,
            loadAssetDropCriteria : loadAssetDropCriteria,
            getWidgetContentTypes: getWidgetContentTypes,
            getWidgetPrefs: getWidgetPrefs,
            clearAsset : clearAsset,
            configureAsset : configureAsset,
            promoteAsset : promoteAsset,
            setAssetRelationship : setAssetRelationship,
            getSpecialWidgets : getSpecialWidgets,
            getType : getType,
            getTemplateType : getTemplateType,
            isPage : isPage,
            isTemplate : isTemplate,
            isTemplateLeaf : isTemplateLeaf,
            containsRegionId : containsRegionId,
            getWidgetByName: getWidgetByName,
            getId:getId,
            encodeHtml:encodeHtml,
            // CSS Code
            getCSSObj : getCSSObj,
            setCSSObj : setCSSObj,
            getThemeList : getThemeList,
            getThemeCSS : getThemeCSS,

            //JavaScript Off methods
            isJavaScriptOff : isJavaScriptOff,
            setJavaScriptOff : setJavaScriptOff,
            findWidgetParentRegion : findWidgetParentRegion,
            getTemplateObj : getTemplateObj,
            renderByObject : renderByObject,
            isResponsiveBaseTemplate : isResponsiveBaseTemplate
        };

        return modelObject;

        function isResponsiveBaseTemplate()
        {
            if(templObj === undefined){
               return false;
            }
            var srcTpl = templObj.Template.sourceTemplateName;
            return srcTpl.indexOf("perc.resp.") === 0;
        }

        function getId()
        {
            return templateId;
        }

        function getTemplateObj()
        {
            return templObj;
        }

        function isJavaScriptOff()
        {
            return javaScriptOff;
        }

        function setJavaScriptOff(so)
        {
            javaScriptOff = so;
        }

        function encodeHtml(html)
        {
            return tempElement.text(html).html();
        }
        /**
         * Searches through all the regions starting at the roow
         * to see if any of the regions's id is equal to regionId
         */
        function containsRegionId(regionId) {
            var contains = false;
            eachRegion(function(){
                if(this.regionId === regionId) {
                    contains = true;
                    return false;
                }
            });
            return contains;
        }

        function load() {
            // TODO JGA: templateManager is defined in perc_template_manager.js
            // this is Jason's service client which needs to be merge into
            // PercTemplateService.js
            templateManager.load_template( templateId, function(xml)
            {
                //Get the template object as a JSON object
                templObj = utils.unxml( $.perc_schemata.template, $.xmlDOM(xml) );
                root = P.regionsFromTree( templObj.Template.regionTree.rootRegion, templObj.Template.regionTree.regionWidgetAssociations, "template" );
                eachRegion( function(){ regionIds[ this.regionId ] = true; } );
                loadWidgetPrefs();

                // Load the CSS Data.
                cssObj.themeName = templObj.Template.theme;
                cssObj.cssOverride = templObj.Template.cssOverride;

                // JGA {
                // TODO: right now get_widget_ctypes only loads the content types
                // I've added the ability to load the whole assetDropCriteria
                // so that we dont only have the content types but the whole thing
                // including locks. We need to remove get_widget_ctypes and just use the new asset drop criteria model
                //    $.perc_pagemanager.get_widget_ctypes( templateId, false, storeWidgetDrops );
                //    function storeWidgetDrops(ctypes){
                //        widgetContentTypes = ctypes;
                //        callback();
                //    }
                // template service loading the asset drop criteria
                loadPageAssetDropCriteria(function(){});
                loadAssetDropCriteria(function(adc){

                    for(var widgetId in assetDropCriteria)
                    {
                        widgetContentTypes[widgetId] = assetDropCriteria[widgetId].supportedContentTypes;
                    }

                    callback();
                });

                $.PercNavigationManager.setTemplateModel(modelObject);
                // } JGA
            });
        }

        function getName()
        {
            return templObj.Template.name;
        }
        function isTemplateLeaf() {

        }

        function getType() {
            return "template";
        }

        function getTemplateType() {
            return templObj.Template.type;
        }

        function isPage() {
            return false;
        }

        function isTemplate() {
            return true;
        }

        function canOverride(region) {
            return false;
        }

        //==============================
        //
        //  CSS methods
        //
        //==============================

        // Code for dealing with the Style Tab, below.

        // Get the CSS object, called from controller.
        function getCSSObj()
        {
            return cssObj;
        }

        // Set the CSS Object, called from the controller.
        function setCSSObj(newcssObj)
        {
            cssObj = newcssObj;
        }


        // Get the List of themes available to the system from
        // the theme summary service.
        function getThemeList(callback)
        {
            // Currently a thin wrapper
            templateService.getThemeList(function(status, data) {
                if (status == true)
                {
                    callback(true, data);
                }
                else
                {
                    callback(false, data);
                }
            });
        }

        // Get the CSS contents of the theme specified in the CSS Object.
        // Do nothing if Theme name is blank
        function getThemeCSS(callback)
        {
            themeName = getCSSObj().themeName;

            if (themeName !== null && themeName !== "")
            {
                templateService.getThemeCSS(themeName, function(status, data)
                {
                    if (status === true)
                    {
                        callback(true, data);
                    }
                    else
                    {
                        callback(false, data);
                    }
                });
            }
            else
            {
                data = {};
                data.text = function()
                {
                    return "No Theme has been selected.";
                }
                callback(false, data);
            }
        }



        //==============================
        //
        //  Asset methods
        //
        //==============================

        function setAssetRelationship(widgetData, assetid, isResource, callback )
        {
            $.PercAssetService.set_relationship( assetid, widgetData, templateId, "1", isResource, null, callback, utils.show_error);
        }

        function getAssetDropCriteria()
        {
            return assetDropCriteria;
        }

        function getPageAssetDropCriteria()
        {
            return pageAssetDropCriteria;
        }

        /**
         * Load the WidgetPref for all available widgets.
         *
         */
        function loadWidgetPrefs(){
            eachRegion( function()
            {
                $.each( this.widgets, function()
                {
                    var defid = this.definitionId;
                    if(!widgetPrefs[defid])
                    {
                        $.perc_widget_definition_client.restGetWidgetPrefs(defid, function(wPrefs) {
                            widgetPrefs[defid] = wPrefs;
                        });
                    }
                });
            });
        }

        /**
         * Load asset drop criteria from the server for this template.
         * @param callback {function} the callback function to be executed
         * when server request is succeful.
         */
        function loadAssetDropCriteria(callback){
            templateService.getAssetDropCriteria(templateId, false,
                function(adc){
                    assetDropCriteria = adc;
                    callback(adc);
                },
                function(request){
                    var defaultMsg =
                        $.PercServiceUtils.extractDefaultErrorMessage(request);
                    $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                });
        }

        /**
         * Load asset drop criteria from the server for this template.
         * @param callback {function} the callback function to be executed
         * when server request is succeful.
         */
        function loadPageAssetDropCriteria(callback){
            if(typeof(gPageId) != 'undefined' && gPageId)
            {
                templateService.getAssetDropCriteria(gPageId, true,
                    function(adc){
                        pageAssetDropCriteria = adc;
                        callback();
                    },
                    function(request){
                        $.unblockUI();

                        var memento = {'templateId' : gSelectTemp, 'pageId' : null};
                        // Use the PercNavigationManager to switch to the template editor
                        var querystring = $j.deparam.querystring();
                        $j.PercNavigationManager.goToLocation(
                            $j.PercNavigationManager.VIEW_EDIT_TEMPLATE,
                            querystring.site,
                            null,
                            null,
                            null,
                            querystring.path,
                            null,
                            memento);
                        /*
                        var defaultMsg =
                                $.PercServiceUtils.extractDefaultErrorMessage(request);
                             $.perc_utils.alert_dialog({title: 'Error', content: defaultMsg});
                        */
                    });
            }
            else
            {
                callback();
            }
        }

        function getWidgetContentTypes (widgetid)
        {
            return widgetContentTypes[ widgetid ];
        }

        function getWidgetPrefs (widgetDefId)
        {
            return widgetPrefs[ widgetDefId ];
        }
        function clearAsset( widgetId, widgetDefinitionId, assetId, callback )
        {
            if(assetId)
                $.PercAssetService.clear_asset( templateId, widgetId, widgetDefinitionId, assetId, callback );
        }

        /*
         * @param widgetData contain: widgetId, must not be null.
         *                            widgetDefinitionId, must not be null.
         *                            widgetName, could be null/empty.
         */
        function configureAsset( widgetData, assetId, isSharedAsset, callback )
        {
            //Internal check in function, checks in both local content and as well as assets
            var wfCallBack = function(aId, producesResource)
            {
                $.PercWorkflowController().checkIn(aId, function(status)
                {
                    callback();
                });
            };
            var cancelCallback = function(aId)
            {
                if(aId)
                {
                    $.PercWorkflowController().checkIn(aId, function(status)
                    {
                        //Just check in we have already closed the dialog.
                    });
                }

            }
            //If asset exists check it out.
            if(assetId)
            {
                $.PercWorkflowController().checkOut("percAsset",  assetId, function(status)
                {
                    if(status)
                    {
                        $.perc_asset_edit_dialog(wfCallBack, cancelCallback, assetId, widgetData, templateId, "template");
                    }
                    else
                    {
                        //Unable to check out the asset.
                        callback();
                    }
                });
            }
            else
            {
                $.perc_asset_edit_dialog(wfCallBack, cancelCallback, assetId, widgetData, templateId, "template");
            }
        }

        /**
         * Method used for promoting page level content on to the template. Makes a call to service to promote the content.
         * Uses template id as the owner id.
         * @param {Object} assetId guid of the asset that needs to be promoted to the template. Assumed not null.
         * @param {Object} widgetData Expected to be an object that provides widgetId, widgetdefid and widgetName.
         * @param {Object} isResource flag to indicate whether the asset is a shared asset or not.
         * @param {Object} callback expected to be a function to callback after success, if there is a failure
         * shows the error message and doesn't call the callback.
         */
        function promoteAsset(assetId, widgetData, isResource, callback)
        {
            $.PercAssetService.promoteAsset(assetId, widgetData, templateId, "1", isResource, function(status, message){
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback();
                }
                else
                {
                    $.perc_utils.alert_dialog({title: 'Error', content: message});
                }
            });
        }

        //==============================
        //
        //  Widget methods
        //
        //==============================

        /**
         * No widgets are locked when editing the template, whether it is layout view or content view.
         * {"LockedWidgets":{},"TransperantWidgets":{},"ContentWidgets":{}}
         * LockedWidgets: widget that need to be locked. (i.e. Page widgets)
         * TransperantWidgets: widget that need to have a gray transparency. (i.e. Template widgets with Page Assets)
         * ContentWidgets: Template widgets with template assets/content.
         */
        function getSpecialWidgets(viewType)
        {
            var specialWidgets = {"LockedWidgets":{},"TransperantWidgets":{},"ContentWidgets":{}};
            var lockedWidgets = {};
            var transparentWidgets = {};
            var contentWidgets = {};

            $.each(pageAssetDropCriteria,function(){
                var twadc = assetDropCriteria[this.widgetId];
                if(!twadc)
                {
                    lockedWidgets[this.widgetId] = this.widgetId;
                    transparentWidgets[this.widgetId] = this.widgetId;
                }
                else if (!twadc.locked && this.locked)
                {
                    transparentWidgets[this.widgetId] = this.widgetId;
                }
            });
            $.each(assetDropCriteria,function(){
                //this.locked is existingAsset property.
                if (this && this.locked){
                    contentWidgets[this.widgetId] = this.widgetId;
                }
            });
            specialWidgets.LockedWidgets = lockedWidgets;
            specialWidgets.TransperantWidgets = transparentWidgets;
            specialWidgets.ContentWidgets = contentWidgets;
            return specialWidgets;
        }

        function getWidgetByName(widgetName)
        {
            var widget = null;
            eachRegion( function()
            {
                $.each( this.widgets, function()
                {
                    if( typeof(this.name) == "string" && this.name.toUpperCase() == widgetName.toUpperCase())
                    {
                        widget = this;
                    }
                });
            });
            return widget;
        }


        function editWidget(widgetId, callback)
        {
            var found = false;
            eachRegion( function()
            {
                $.each( this.widgets, function()
                {
                    if( this.id === widgetId)
                    {
                        callback.call( this );
                        found = true;
                    }
                });
                return !found;
            });
            return found;
        }

        //=======================
        //
        //  Region methods
        //
        //=======================

        function getRoot()
        {
            return root;
        }

        // Creates a new region instance.
        // Used when adding a new region with the new region drag tool.
        // Invoked from PercLayoutController.addRegion(regionId, direction)
        function newRegion()
        {
            var regionId = 'temp-region-' + regcount;
            while( regionIds[ regionId ] )
            {
                regcount++;
                regionId = 'temp-region-' + regcount;
            }
            regionIds[ regionId ] = true;
            return { children: [], widgets: [], vertical: true, regionId: regionId, fixed: false, width: '', height: '', owner: 'template' };
        }

        function newResponsiveRegion(isRow, cols)
        {
            var regionId = 'temp-region-' + regcount;
            while( regionIds[ regionId ] )
            {
                regcount++;
                regionId = 'temp-region-' + regcount;
            }
            regionIds[ regionId ] = true;
            var large = isRow?"":cols?"large-"+cols:"large-1";
            return { children: [], widgets: [], regionId: regionId, row: isRow, columns: !isRow, large: large, owner: 'template' };
        }

        function getTemplateRegionIds() {
            return regionIds;
        }

        // Applies callback function only to the region with regionId.
        // Callback function has access to the region instance through the 'this' keyword.
        function editRegion(regionId, callback)
        {
            var found = false;
            eachRegion( function()
            {
                if( this.regionId == regionId )
                {
                    callback.call( this );
                    found = true;
                    return false;
                }
                else
                {
                    return true;
                }
            });
            return found;
        }

        function editRegionParent(regionId, callback)
        {
            var found = false;
            eachRegion( function()
            {
                var r = [];
                if(this.children)
                {
                    r = $.grep( this.children, function(c)
                    {
                        return c.regionId == regionId;
                    });
                }
                if( r.length > 0 )
                {
                    callback.call( this );
                    found = true;
                    return false;
                }
                return true;
            } );
            return found;
        }

        function getRegionPosition(regionId){
            var position = -1;
            var parentReg = null;
            editRegionParent(regionId, function(){
                parentReg = this;
                var found = false;
                $.each(parentReg.children, function() {
                    position++;
                    if(this.regionId === regionId) {
                        found = true;
                        return false;
                    }
                });
            });
            return position;
        }
        // Map function that recursively iterates over all of the regions applying the callback function
        // Callback function has access to the region instance through the 'this' keyword
        function eachRegion( callback )
        {
            _eachRegion( root, callback );
        }

        function _eachRegion( tree, callback )
        {
            if( false === callback.call( tree ) )
            {
                return;
            }

            if(typeof(tree) == 'undefined' || typeof(tree.children) == 'undefined' || typeof(tree.children.length) == 'undefined')
                return;
            $.each( tree.children, function()
            {
                _eachRegion( this, callback );
            });
        }
        function findWidgetParentRegion(widgetId, callback)
        {
            var found = false;
            eachRegion( function()
            {
                var parentRegionId = this.regionId;
                $.each( this.widgets, function()
                {
                    if( this.id == widgetId)
                    {
                        callback( parentRegionId );
                        found = true;
                    }
                });
                return !found;
            });
            return found;
        }
        //===========================================
        //
        //  Service Calls to save and render template
        //
        //  save, render, renderAll
        //
        //===========================================

        function save(postCallback, newWidgetObj, requireMigration)
        {
            updateTemplObj();
            var saveAssetsCallback = function(){
                //Call template service to save the new assets
                $.PercTemplateService().updateInspectedElements(newWidgetObj, function(status, result){

                    if(status) {
                        postCallback();
                    }
                    else {
                        //Display error message
                    }
                });
            };

            if(newWidgetObj) {
                callback = saveAssetsCallback;
            }
            else {
                callback = postCallback;
            }

            templateManager.save_template( templateId, requireMigration, utils.rexml( $.perc_schemata.template, templObj), callback );
        }

        function renderAll(frame, callback)
        {
            function finish()
            {
                callback();
            }
            var rootPath = javaScriptOff ? $.perc_paths.TEMPLATE_RENDER_SCRIPTSOFF : $.perc_paths.TEMPLATE_RENDER;

            var renderPath = rootPath + "/" + templateId + "?timestamp=" + new Date().getTime();
            if(typeof(gPageId) != 'undefined' && gPageId)
            {
                rootPath = javaScriptOff ? $.perc_paths.PAGE_EDIT_SCRIPTSOFF : $.perc_paths.PAGE_EDIT;
                renderPath = rootPath + "/" + gPageId + "?editType=TEMPLATE&timestamp=" + new Date().getTime();
            }

            // set the frame's src attribute and then
            // bind the document's load() event to notify us when to continue
            // When the document loads and is rendered, we are ready to decorate it
            $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
            frame.contents().remove();
            frame.attr("src", renderPath);
            frame.unbind().load(function()
            {
                loadAssetDropCriteria(function(){
                    loadPageAssetDropCriteria(function(){
                        $.unblockUI();
                        callback();
                    });

                });
            });
        }

        function render(callback)
        {
            updateTemplObj();
            templateManager.render_region( root.children[0].regionId,
                utils.rexml( $.perc_schemata.template, templObj ), gPageId,
                function(data) {
                    callback( $(data).find('result').text() );
                });
        }

        function renderByObject(tObj, callback)
        {
            templObj = tObj;
            root = P.regionsFromTree( templObj.Template.regionTree.rootRegion, templObj.Template.regionTree.regionWidgetAssociations, "template" );

            templateManager.render_region( root.children[0].regionId,
                utils.rexml( $.perc_schemata.template, templObj ), gPageId,
                function(data) {
                    callback( $(data).find('result').text() );
                });
        }

        // Update the template object before sending to the REST service
        // recreate the rootRegion and regionWidgetAssociations by recursively traversing the model
        // and making sure that updated template object is what the REST service expects
        function updateTemplObj()
        {
            templObj.Template.regionTree.rootRegion = P.treeFromRegions( root, true );
            templObj.Template.regionTree.regionWidgetAssociations = createRegionWidgetAssociations( root );
            // templObj.Template.cssRegion = "";
            var cssObj = getCSSObj();
            templObj.Template.cssOverride = cssObj.cssOverride;
            templObj.Template.theme = cssObj.themeName;
        }

        // At save and at render time, recreate the regionWidgetAssociations
        // of the template object to send to the REST service
        // Recursively iterate over all the region's widget property
        // and create the JSON object to populate the template object to send to the REST service
        function createRegionWidgetAssociations( root )
        {
            if( root.widgets.length > 0 )
            {
                return [{_tagName: 'regionWidget', regionId: root.regionId,
                    widgetItems: $.map( root.widgets, function(w) { return P.widgetItemFromWidget( w ); }) }];
            }
            else
            {
                return $.map( root.children, function(c){ return createRegionWidgetAssociations( c ); } );
            }
        }
    };

    // At load time, recursively parse the rootRegion and regionWidgetAssociation branch
    // and populate the model
    // JGA: added owner
    P.regionsFromTree = function( tree, regionWidgetAssociations, owner) {
        return modelObject.isResponsiveBaseTemplate()?P.respRegionsFromTree(tree, regionWidgetAssociations, owner):
            P.baseRegionsFromTree(tree, regionWidgetAssociations, owner);
    };
    P.baseRegionsFromTree = function(tree, regionWidgetAssociations, owner){
        // parse the widgets from the regionWidgetAssociation branch
        // so we can copy them over to the region's widget's property
        var regId = tree.regionId;
        var widgets = $.grep( regionWidgetAssociations, function(a) { return a.regionId == regId; } );
        if( widgets[0] ) {
            widgets = $.map( widgets[0].widgetItems, function(w){ return P.widgetFromWidgetItem( w ); } );
        } else {
            widgets = [];
        }

        // parse the startTag
        var openTag = tree.startTag || "perc-vertical perc-fixed";

        // see if region's properties have been fixed by the user
        var fixed = !!openTag.match( /perc-fixed/ );

        // is this a vertical or horizontal region
        var vertical = !!openTag.match( /perc-vertical/ );

        // parse region height
        var height = openTag.match( /height[^:]*:([^;]*);/ );
        height = height ? $.trim(height[1]) : "";

        // parse region width
        var width = openTag.match( /width[^:]*:([^;]*);/ );
        width = width ? $.trim(width[1]) : "";

        // parse region padding
        var padding = openTag.match( /padding[^:]*:([^;]*);/ );
        padding = padding? $.trim(padding[1]):"";

        // parse region margin
        var margin = openTag.match( /margin[^:]*:([^;]*);/ );
        margin = margin? $.trim(margin[1]):"";

        // parse region noAutoResize - try the legacy invalid HTML attribute 1st
        var noAutoResize = $(openTag).attr("noAutoResize");
        try {
            var tempNoAutoResize = noAutoResize === "" || noAutoResize === undefined ? false : noAutoResize;
            if (noAutoResize !== undefined) {
                //Remove the invalid HTML attribute and replace with a valid one.
                $(openTag).removeAttr("noAutoResize");
                $(openTag).attr("data-noautoresize", tempNoAutoResize);
                noAutoResize = tempNoAutoResize;
            } else {
                noAutoResize = $(openTag).attr("data-noautoresize");
                noAutoResize = noAutoResize === "" || noAutoResize === undefined ? false : noAutoResize;
            }
        }catch(err){
            console.log(err);
        }

        // parse default styling
        var vspan = openTag.match( /vspan_[0-9]*/ );
        vspan = vspan ? $.trim(vspan[0]):"";
        var hspan = openTag.match( /hspan_[0-9]*/ );
        hspan = hspan ? $.trim(hspan[0]):"";

        // recursively parse child regions
        var subTrees = $.grep( tree.children, function(c){ return c._tagName == 'region'; } );
        var subRegions = $.map( subTrees, function(r){ return P.baseRegionsFromTree( r, regionWidgetAssociations, owner ); });
        subRegions = subRegions ? subRegions : [];

        // populate the region model and return to parent region
        var region = { regionId: regId, noAutoResize : noAutoResize, vertical: vertical, children: subRegions, widgets: widgets, fixed: fixed, width: width, height: height, owner: owner, margin: margin, padding:padding, vspan : vspan, hspan : hspan, cssClass : tree.cssClass, attributes: tree.attributes };

        return region;

    }
    P.respRegionsFromTree = function(tree, regionWidgetAssociations, owner){
        // parse the widgets from the regionWidgetAssociation branch
        // so we can copy them over to the region's widget's property
        var regId = tree.regionId;
        var widgets = $.grep( regionWidgetAssociations, function(a) { return a.regionId == regId; } );
        if( widgets[0] ) {
            widgets = $.map( widgets[0].widgetItems, function(w){ return P.widgetFromWidgetItem( w ); } );
        } else {
            widgets = [];
        }

        // parse the startTag
        var openTag = tree.startTag || "row";

        // is this a row
        var row = !!openTag.match( /row/ );

        //is this a column
        var columns = !!openTag.match( /columns/ );

        // parse number of columns
        var large = openTag.match( /large-[0-9]*/ );
        large = large? $.trim(large[0]):"";

        // parse region padding
        var padding = openTag.match( /padding[^:]*:([^;]*);/ );
        padding = padding? $.trim(padding[1]):"";

        // parse region margin
        var margin = openTag.match( /margin[^:]*:([^;]*);/ );
        margin = margin? $.trim(margin[1]):"";

        // recursively parse child regions
        var subTrees = $.grep( tree.children, function(c){ return c._tagName == 'region'; } );
        var subRegions = $.map( subTrees, function(r){ return P.respRegionsFromTree( r, regionWidgetAssociations, owner ); });
        subRegions = subRegions ? subRegions : [];

        // populate the region model and return to parent region
        var region = { regionId: regId, row: row, children: subRegions, widgets: widgets, columns: columns, large: large, owner: owner, margin: margin, padding:padding, cssClass : tree.cssClass, attributes: tree.attributes};

        return region;

    }
    // At load time, parse widgetItem and its properties into widget model
    P.widgetFromWidgetItem = function( widgetItem ) {
        var properties = [];
        $.each( widgetItem.properties, function(){
            properties[ this.name ] = JSON.parse( this.value || '' );
        });
        var cssProperties = [];
        if(widgetItem.cssProperties)
        {
            $.each( widgetItem.cssProperties, function(){
                cssProperties[ this.name ] = JSON.parse( this.value || '' );
            });
        }
        return {
            id: widgetItem.id,
            definitionId: widgetItem.definitionId,
            name: widgetItem.name,
            description: widgetItem.description,
            properties: properties,
            cssProperties: cssProperties
        };
    };

    // At save or render time, recursively recreate template object to send to server starting at the root region
    P.treeFromRegions = function( region, isTop ) {
        return modelObject.isResponsiveBaseTemplate()?P.respTreeFromRegions(region, isTop):
            P.baseTreeFromRegions(region, isTop);
    };
    P.baseTreeFromRegions = function( region, isTop ) {

        // build the HTML markup for the node from the model
        var leaf = region.children.length == 0 ? ' perc-region-leaf' : '';
        var fixed = region.fixed ? ' perc-fixed ' : '';
        var direction = region.vertical ? ' perc-vertical ' : ' perc-horizontal ui-helper-clearfix ' ;
        var padding = region.padding?'padding: '+region.padding+';':'';
        var margin = region.margin?'margin: '+region.margin+';':'';
        var hspan = region.hspan?' '+region.hspan+' ':' ';
        var vspan = region.vspan?' '+region.vspan+' ':' ';
        var cssClass = region.cssClass?' '+ region.cssClass + ' ':' ';
        var noAutoResize = region.noAutoResize ? " perc-noAutoResize " : " ";
        var startTag = '<div ';
        if(region.height || region.width || region.padding || region.margin) {
            startTag += 'style="';
            if(region.height) {
                if(!isNaN(region.height))
                    region.height += "px";
                startTag +='height:'+region.height+';';
            }
            if(region.width) {
                if(!isNaN(region.width))
                    region.width += "px";
                startTag +='width:'+region.width+';';
            }
            startTag += padding + margin
                + '"'
        }

        startTag += ' class="' + modelObject.encodeHtml(('perc-region'+leaf+noAutoResize+fixed+direction+hspan+vspan+cssClass).replace(/\s\s+/g, ' ')) +'" '
            +  ' data-noautoresize="'+modelObject.encodeHtml(region.noAutoResize) + '" id="'+modelObject.encodeHtml(region.regionId)+'">'
            +  ' <div class="'+modelObject.encodeHtml(direction)+'"';

        var treeAttributes = [];
        if( region.attributes ) {
            for(var i=0,len=region.attributes.length; i<len; i++){
                treeAttributes.push( {_tagName: 'attribute', name: region.attributes[i].name, value: region.attributes[i].value  } );
                startTag+=" "+modelObject.encodeHtml(region.attributes[i].name)+'="'+modelObject.encodeHtml(region.attributes[i].value)+'"';

            }
        }

        startTag += '>';

        var endTag = '</div></div>';



        var children;
        if( leaf ) {

            children = [{_tagName: 'code', templateCode: '#region("'+region.regionId+'","","","","")' }];

        } else {

            children = $.map( region.children, function(c){ return P.baseTreeFromRegions( c ); } );
            if( isTop ) {
                var startChild = {_tagName: 'code', templateCode: '#perc_templateHeader()'};
                var endChild = {_tagName: 'code', templateCode: '#perc_templateFooter()'};
                children.unshift(startChild);
                children.push(endChild);
            }
        }

        if( isTop ) {

            return { children: children, regionId: region.regionId };

        } else {

            return { _tagName: 'region', noAutoResize:region.noAutoResize, regionId: region.regionId, startTag: startTag, endTag: endTag, children: children, cssClass: region.cssClass, attributes: treeAttributes};

        }
    };

    P.respTreeFromRegions = function( region, isTop ) {

        // build the HTML markup for the node from the model
        var leaf = region.children.length == 0 ? ' perc-region-leaf' : '';
        var row = region.row ? ' row ' : '' ;
        var columns = region.columns ? ' columns ' : '' ;
        var large = region.large?' '+region.large+' ':'';
        var padding = region.padding?'padding: '+region.padding+';':'';
        var margin = region.margin?'margin: '+region.margin+';':'';
        var cssClass = region.cssClass?' '+ region.cssClass + ' ':' ';
        var startTag = '<div ';
        if(region.padding || region.margin) {
            startTag += 'style="';
            startTag += padding + margin
                + '"'
        }

        startTag += ' class="perc-region'+modelObject.encodeHtml(leaf+row+columns+large+cssClass)+'" '
            + 'id="'+modelObject.encodeHtml(region.regionId)+'"';


        var treeAttributes = [];
        if( region.attributes ) {
            for(var i=0,len=region.attributes.length; i<len; i++){
                treeAttributes.push( {_tagName: 'attribute', name: region.attributes[i].name, value: region.attributes[i].value  } );
                startTag+=" "+modelObject.encodeHtml(region.attributes[i].name)+'="'+modelObject.encodeHtml(region.attributes[i].value)+'"';
            }
        }


        startTag +='>';

        var endTag = ' </div> ';


        var children;
        if( leaf ) {

            children = [{_tagName: 'code', templateCode: '#region("'+region.regionId+'","","","","")' }];

        } else {

            children = $.map( region.children, function(c){ return P.respTreeFromRegions( c ); } );
            if( isTop ) {
                var startChild = {_tagName: 'code', templateCode: '#perc_templateHeader()'};
                var endChild = {_tagName: 'code', templateCode: '#perc_templateFooter()'};
                children.unshift(startChild);
                children.push(endChild);
            }
        }

        if( isTop ) {

            return { children: children, regionId: region.regionId };

        } else {

            return { _tagName: 'region', regionId: region.regionId, startTag: startTag, endTag: endTag, children: children, cssClass: region.cssClass, attributes: treeAttributes};

        }
    };

    P.widgetItemFromWidget = function( widget ) {
        var properties = [];

        //Used in the client side just for render purposes.
        if(typeof widget !== 'undefined') {
            delete widget.properties.sys_perc_name;
            delete widget.properties.sys_perc_description;

            if (widget.properties) {
                for (var prop in widget.properties) {
                    properties.push({_tagName: 'property', name: prop, value: JSON.stringify(widget.properties[prop])});
                }
            }
            var cssProperties = [];
            if (widget.cssProperties) {
                for (var prop2 in widget.cssProperties) {
                    cssProperties.push({
                        _tagName: 'property',
                        name: prop2,
                        value: JSON.stringify(widget.cssProperties[prop2])
                    });
                }
            }
            return {
                _tagName: 'widgetItem',
                id: widget.id,
                definitionId: widget.definitionId,
                name: widget.name,
                description: widget.description,
                properties: properties,
                cssProperties: cssProperties
            };
        }
        return {};
    };

    // LT
    // Data model for TemplateMetadata used to edit metadata on a template
    $.PercTemplateMetadataModel = function( id,
                                            additionalHeadContent,
                                            beforeBodyCloseContent,
                                            afterBodyStartContent,
                                            protectedRegion,
                                            protectedRegionText)
    {
        this.id = id;
        this.additionalHeadContent = additionalHeadContent;
        this.beforeBodyCloseContent = beforeBodyCloseContent;
        this.afterBodyStartContent = afterBodyStartContent;
        this.protectedRegion = protectedRegion;
        this.protectedRegionText = protectedRegionText;
    }

})(jQuery, jQuery.Percussion);
