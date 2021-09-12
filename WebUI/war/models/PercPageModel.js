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

(function($, P) {

    P.pageModel = function(pageManager, templateManager, pageId, callback) {

        var utils = $.perc_utils;
        var pageObj;

        var templateRootRegion;
        var templateModel;
        //Holds the locked widgets in content mode initialized in _initLockedWidgetIds method.
        var lockedContentWidgets;
        //Holds the locked widgets in layout mode initialized in _initLockedWidgetIds method.
        var lockedLayoutWidgets;
        var regionIds = {};
        var regionCount = 1;

        //Local variable to hold the javascript off status.
        var javaScriptOff = false;
        var widgetContentTypes = {};
        var assetDropCriteria = {};

        load();

        // public interface to the page model
        return {
            save: save,
            load : load,
            editWidget : editWidget,
            editRegion : editRegion,
            editRegionParent : editRegionParent,
            eachRegion : eachRegion,
            newRegion : newRegion,
            getRoot : getRoot,
            render: render,
            renderAll: renderAll,

            clearAsset : clearAsset,
            promoteAsset : promoteAsset,
            clearOrphanAssets : clearOrphanAssets,
            configureAsset : configureAsset,
            getAssetDropCriteria: getAssetDropCriteria,
            getWidgetContentTypes: getWidgetContentTypes,
            setAssetRelationship : setAssetRelationship,
            updateAssetRelationship : updateAssetRelationship,
            canOverride : canOverride,
            getType : getType,
            isPage : isPage,
            isLandingPage : isLandingPage,
            isTemplate : isTemplate,
            isTemplateLeaf : isTemplateLeaf,

            getTemplateModel : getTemplateModel,
            getSpecialWidgets : getSpecialWidgets,
            getModel : getModel,
            isJavaScriptOff : isJavaScriptOff,
            setJavaScriptOff : setJavaScriptOff,
            findWidgetParentRegion : findWidgetParentRegion,
            isResponsiveBaseTemplate : isResponsiveBaseTemplate
        };
        function isResponsiveBaseTemplate()
        {
            return getTemplateModel().isResponsiveBaseTemplate();
        }

        function load() {

            // get the page from the REST service
            pageManager.load_page( pageId, function(xml) {

                // parse XML into a JSON object
                pageObj = utils.unxml( $.perc_schemata.page, $.xmlDOM( xml ) );

                // get the template for this page
                var templateId = pageObj.Page.templateId;
                templateModel = P.templateModel( templateManager, templateId, initialize );

                // call back after template is loaded
                var self = this;
                function initialize() {

                    // get page regions
                    // Note that pageRegions here contains the widgets within it. No need for regionWidgetAssociations
                    // That branch has been parsed and copied into a widgets attribute in the region schema.
                    // The parsing of regions is done in P.regionsFromTree defined in PercTemplateModel
                    var pageRegions = regionsFromBranches( pageObj.Page.regionBranches.regions, pageObj.Page.regionBranches.regionWidgetAssociations );
                    templateRootRegion = templateModel.getRoot();

                    _initLockedWidgetIds();
                    overrideTemplateRegions( templateRootRegion, pageRegions );
                    createTemplateRegionIdArray(templateRootRegion);
                    loadAssetDropCriteria(function(adc){
                        for(var widgetId in assetDropCriteria)
                        {
                            widgetContentTypes[widgetId] = assetDropCriteria[widgetId].supportedContentTypes;
                        }

                        callback();
                    });

                }


            });
        }

        function getType() {
            return "page";
        }

        function isPage() {
            return true;
        }

        function isLandingPage() {
            return (pageObj.Page.category === "LANDING_PAGE");
        }

        function isJavaScriptOff()
        {
            return javaScriptOff;
        }
        function setJavaScriptOff(so)
        {
            javaScriptOff = so;
        }
        function isTemplate() {
            return false;
        }

        function regionsFromBranches( pageRegions, assocs ) {

            return utils.o_a( $.map( pageRegions, function(tree) {

                var region = P.regionsFromTree( tree, assocs, "page");
                return { k : region.regionId, v: region };

            }));
        }

        // Iterate over the template regions and page regions
        // and override template regions with page regions
        // if page region id and template region id matches
        // and template regions has no sub regions
        // Note that pageRegions here contains the widgets within it. No need to look at the regionWidgetAssociations
        // That branch has been parsed and copied into a widgets attribute in the region schema
        function overrideTemplateRegions( templateRootRegion, pageRegions ) {

            eachRegion(function() {
                if(isTemplateLeaf(this)){
                    // if the page region's id is the same as a template's region id,
                    // then the page region overrides the template region
                    if( this.regionId in pageRegions && this.children.length === 0 ) {
                        // put the page widgets in the template region
                        overrideTemplateRegionWithPageRegion(this, pageRegions[this.regionId]);
                        // TODO: anything else that needs to be overriden? region properties? region id?
                    }
                }
            });
        }

        function overrideTemplateRegionWithPageRegion(templateRegion, pageRegion) {
            templateRegion.children = pageRegion.children;
            templateRegion.fixed = pageRegion.fixed;
            templateRegion.vertical = pageRegion.vertical;
            templateRegion.widgets = pageRegion.widgets;
            templateRegion.height = pageRegion.height;
            templateRegion.width = pageRegion.width;
            templateRegion.owner = "page";
        }

        // recursively iterate over all template regions
        // and create an array indexed by region ids
        // to make it easier to add new regions
        // and keep track of region ids
        function createTemplateRegionIdArray(tree) {
            regionIds[ tree.regionId ] = true;
            $.each( tree.children, function()
            {
                createTemplateRegionIdArray( this );
            });
        }

        //$.perc_templatemanager.load_template( templateId, function(xml) {

        function getModel() {
            return pageObj;
        }

        /**
         * Initializes the locked lay out and content widget objects. If the widget is from template then it will be locked
         * in lay out mode. If the widget is from template and if it has content then it is locked in content mode.
         */
        function _initLockedWidgetIds() {

            lockedLayoutWidgets = {};
            lockedContentWidgets = {};
            var adc = templateModel.getAssetDropCriteria();
            templateModel.eachRegion( function()
            {
                $.each( this.widgets, function()
                {
                    lockedLayoutWidgets[this.id] = this.id;
                    if(adc && adc[this.id] && adc[this.id].locked)
                        lockedContentWidgets[this.id] = this.id;
                });
            });
        }

        /**
         * Returns the special widgets, widget ids that are locked and that needs to be transferant based on the supplied type.
         * @viewType if the value is "Content" then returns the widgets that needs to be locked in content tab, if the
         * value is "Layout" then returns the widgets that needs to be locked in layout mode.
         * @return Object, in the form of {widgetid:widgetid, widgetid:widgetid, etc...}, Returns empty object if no
         * locked widgets exist.
         */
        function getSpecialWidgets(viewType) {

            var specialWidgets = {"LockedWidgets":{},"TransperantWidgets":{}};
            if (viewType === "Content")
            {
                specialWidgets.LockedWidgets = lockedContentWidgets;
            }
            else if (viewType === "Layout")
            {
                specialWidgets.LockedWidgets = lockedLayoutWidgets;
            }
            return specialWidgets;
        }

        function getTemplateModel()
        {
            return templateModel;
        }

        function getRoot()
        {
            return templateRootRegion;
        }

        //==============================
        //
        //  Asset methods
        //
        //==============================

        function setAssetRelationship(widgetData, assetid, isResource, callback )
        {
            $.PercAssetService.set_relationship( assetid, widgetData, pageId, "1", isResource, null, callback, utils.show_error);
        }

        function updateAssetRelationship(widgetData, assetid, relationshipId, callback, errorCallback )
        {
            $.PercAssetService.update_relationship(assetid, relationshipId, widgetData, pageId, callback, errorCallback);
        }

        /**
         * Load asset drop criteria from the server for this page.
         * @param callback {function} the callback function to be executed
         * when server request is succeful.
         */
        function loadAssetDropCriteria(callback){
            $.PercTemplateService().getAssetDropCriteria(pageId, true,
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

        function getAssetDropCriteria()
        {
            return assetDropCriteria;
        }
        function getWidgetContentTypes(widgetid)
        {
            return widgetContentTypes[ widgetid ];
        }
        function clearAsset( widgetId, widgetDefinitionId, assetId, callback )
        {
            if(assetId)
                $.PercAssetService.clear_asset( pageId, widgetId, widgetDefinitionId, assetId, callback );
        }

        /**
         * This action has been implemented for templates only it is not supposed to be called for pages.
         */
        function promoteAsset(assetId, widgetData, isResource, callback)
        {
            $.perc_utils.alert_dialog({title: 'Error', content: "Promote action has not been implmented for page."});
        }

        function clearOrphanAssets( widgetId, widgetDefinitionId, assetId, callback )
        {
            if(assetId)
                $.PercAssetService.clear_orphan_assets( pageId, widgetId, widgetDefinitionId, assetId, callback );
        }

        /**
         * Checks whether the page is still checked out to the current user or not, if not then, does not let the user
         * Checks out the asset if exists, if check out succeeds opens the asset editing dialog.
         * Checks back in the asset if it produces resource type asset.
         * @param widgetData contain: widgetId, must not be null.
         *                            widgetDefinitionId, must not be null.
         *                            widgetName, could be null/empty.
         * @param assetId, may be null, if asset does not exist.
         * @param callback, must not be null, calls this function after finished with editing or if the asset could not be checked out.
         */
        function configureAsset( widgetData, assetId, isSharedAsset, callback )
        {
            if(isSharedAsset)
            {
                //Check Permission
                $.PercFolderHelper().getAccessLevelById(assetId,false,function(status, result, errorCode){
                    if(status === $.PercFolderHelper().PERMISSION_ERROR)
                    {
                        var msg = "";
                        if (errorCode === "cannot.find.item")
                        {
                            //msg = "The content you are attempting to edit has been deleted.";
                            msg = I18N.message( 'perc.ui.common.error@Content Deleted' );
                            $('#frame').attr("src", $('#frame').attr("src"));
                        }
                        else
                        {
                            msg = result;
                        }

                        $.perc_utils.alert_dialog({title: 'Error', content: msg});
                    }
                    else
                    {
                        if(result === $.PercFolderHelper().PERMISSION_READ)
                        {
                            $.perc_utils.alert_dialog({title: 'Warning', content: "You do not have permission to edit " +
                                    "this asset as it is from a read only folder."});
                        }
                        else
                        {
                            _editAsset( widgetData, assetId, isSharedAsset, callback );
                        }
                    }
                });
            }
            else
            {
                _editAsset( widgetData, assetId, isSharedAsset, callback );
            }
        }

        /**
         * Helper function to do the actual work for the public method configureAsset, see #configureAsset for the details
         * of the parameters.
         */
        function _editAsset( widgetData, assetId, isSharedAsset, callback )
        {
            //Internal check in function
            var checkIn = function(aId, producesResource)
            {
                if(producesResource || isSharedAsset)
                {
                    $.PercWorkflowController().checkIn(aId, function(status)
                    {
                        callback();
                    });
                }
                else
                {
                    callback();
                }
            };
            //We have to check the assets back if they are shared assets on cancel as we check out while openeing the asset dialog.
            var cancelCallback = function(aId)
            {
                if(aId && isSharedAsset)
                {
                    $.PercWorkflowController().checkIn(aId, function(status)
                    {
                        //The dialog is already closed nothing to do now.
                    });
                }
            };
            $.PercWorkflowController().isCheckedOutToCurrentUser(pageId, function(status){
                if(status)
                {
                    //If asset exists check it out.
                    if(assetId)
                    {
                        $.PercWorkflowController().checkOut("percAsset",  assetId, function(status)
                        {
                            if(status)
                            {
                                $.perc_asset_edit_dialog(checkIn, cancelCallback, assetId, widgetData, pageId, "page");
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
                        $.perc_asset_edit_dialog(checkIn, cancelCallback, assetId, widgetData, pageId, "page");
                    }
                }
                else
                {
                    var options = {title:"Edit Page Error",
                        question:"The page you opened has been modified in a different session. <br/><br/>Do you want to reload the page?",
                        cancel:function(){},
                        success:function(){
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, false);
                        }
                    };
                    $.perc_utils.confirm_dialog(options);
                }
            });
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

        function newRegion(optionalPrefix)
        {
            var regionId = 'temp-region-' + regionCount;

            while( regionIds[ regionId ] )
            {
                regionCount++;
                regionId = 'temp-region-' + regionCount;
            }

            if(optionalPrefix)
                regionId = optionalPrefix + regionId;

            regionIds[ regionId ] = true;
            return { children: [], widgets: [], vertical: true, regionId: regionId, fixed: false, width: 200, height: 'auto', owner: "page" };
        }

        /**
         * Recursively searches for region with regionId and then passes the region to the callback function.
         * Uses eachRegion() to iterate over the regions, and then eachRegion() uses _eachRegion() to do the recursion.
         * @param regionId(String) id of the region we are looking for
         * @param callback(function(region)) call back function to call when region is found. Region is passed as param
         * @see eachRegion
         * @see _eachRegion
         */
        function editRegion(regionId, callback)
        {
            var found = false;
            eachRegion( function()
            {
                if( this.regionId === regionId )
                {
                    // jga { if region is not overridable return
                    var overridable = canOverride(this);
                    if(!overridable) {
                        found = true;
                        return false;
                    }
                    // } jga
                    callback.call ( this );
                    // JGA { this line was setting all template leaf regions
                    // to owner = page which is not true until a template
                    // region is overriden by adding a page widget or page subregion.
                    // This was doing it for all regions when this method is used to
                    // iterate over all regions to apply decorations.
                    // this.owner = "page";
                    // } JGA
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
                var r = $.grep( this.children, function(c)
                {
                    return c.regionId === regionId;
                });
                if( r.length > 0 )
                {
                    callback.call( this );
                    // JGA { this line was setting all template leaf regions
                    // to owner = page which is not true until a template
                    // region is overriden by adding a page widget or page subregion.
                    // This was doing it for all regions when this method is used to
                    // iterate over all regions to apply decorations.
                    // this.owner = "page";
                    // } JGA
                    found = true;
                    return false;
                }
                return true;
            });
            return found;
        }

        function eachRegion( callback )
        {
            _eachRegion( templateRootRegion, callback );
        }

        function _eachRegion( region, callback )
        {
            //JB: overiding code is causing issues with the property saving.
            //Submitted a bug for locking the template regions in pages.http://bugs/browse/CML-703
            //   if( canOverride( region ) &&
            //       false === callback.call( region ) ) {
            //      return;
            //   }
            callback.call(region);
            $.each( region.children, function()
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
                    if( this.id === widgetId)
                    {
                        callback( parentRegionId );
                        found = true;
                    }
                });
                return !found;
            });
            return found;
        }
        // checks to see if the region can be overriden
        // a region can be overriden if it's a region that's been created while editing a page, i.e., owner = "page"
        // or if it did not already have
        function canOverride( region )
        {
            return region.owner === "page" || (region.owner === "template" && region.children.length === 0 && region.widgets.length === 0);//(region.owner === "template" && region.children.length == 0 && region.widgets.length == 0);
        }

        function isTemplateLeaf(region) {
            region.templateLeaf = region.templateLeaf || (region.owner === "template" && region.children.length === 0 && region.widgets.length === 0);
            return region.templateLeaf;
        }
        //===========================================
        //
        //  Service Calls to save and render template
        //
        //  save, render, renderAll
        //
        //===========================================

        function save(postCallback)
        {
            updatePageObj();

            $.PercWorkflowController().isCheckedOutToCurrentUser(pageId, function(status){
                if(status)
                {
                    pageManager.save_page( pageId, utils.rexml( $.perc_schemata.page, pageObj ), postCallback );
                }
                else
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog({title: I18N.message( "perc.ui.common.label@Save" ), content: I18N.message( "perc.ui.webmgt.contentbrowser.warning@Action Not Performed Saved", ["page"] )});
                }
            });
        }

        /**
         * Loads and renders the iFrame at the bottom of the screen.
         * Waits until done and then calls back.
         */
        var percDecorationCssLinks = "<link rel='stylesheet' type='text/css' href='/cm/css/perc_decoration.css'/>";

        var jQueryScripts  = "<script src='/cm/jslib/profiles/3x/jquery/jquery-3.6.0.js'></script>";
        jQueryScripts += "<script src='/cm/jslib/profiles/3x/jquery/libraries/jquery-ui/jquery-ui.js'></script>";
        var jQueryCssLinks = "<link rel='stylesheet' type='text/css' href='/cm/themes/smoothness/jquery-ui-1.8.9.custom.css'/>";

        function renderAll(frame, callback)
        {
            //If the javascript is off then set the path to the page edit scriptsoff otherwise to page edit.
            var rootPath = javaScriptOff ? $.perc_paths.PAGE_EDIT_SCRIPTSOFF : $.perc_paths.PAGE_EDIT;
            var renderPath = rootPath + "/" + pageId + "?timestamp=" + new Date().getTime();
            // set the frame's src attribute and then
            // bind the document's load() event to notify us when to continue
            // When the document loads and is rendered, we are ready to decorate it
            frame.contents().remove();
            frame.attr("src", renderPath);
            frame.off().on("load",function(evt) {
                loadAssetDropCriteria(function(){
                    callback();
                });

            });


        }

        /**
         * Retrieves HTML for pageObj from server
         * Used by PercLayoutView.js to render after any modification to the page
         * such as adding widgets, regions, moving, deleting, etc.
         *
         * Might have similar race conditions as renderAll but frame is not rendered here
         * It's rendered back in the PercLayoutView.js
         */
        function render(callback)
        {
            updatePageObj();
            pageManager.render_region( templateRootRegion.children[0].regionId,
                utils.rexml( $.perc_schemata.page, pageObj ),
                function(data)
                {
                    callback( $(data).find('result').text() );
                });
        }

        function updatePageObj()
        {
            pageObj.Page.regionBranches.regions = branchesFromRoot( templateRootRegion );
            pageObj.Page.regionBranches.regionWidgetAssociations = createRegionWidgetAssociations();
        }

        function branchesFromRoot( root )
        {
            if( root.owner === "page" )
            {
                var branch = P.treeFromRegions( root, false );
                delete branch.startTag;
                delete branch.endTag;
                return branch;
            }
            else
            {
                return $.map( root.children, function(c)
                {
                    return branchesFromRoot( c );
                });
            }
        }

        function createRegionWidgetAssociations()
        {
            var widgets = [];
            eachRegion( function()
            {
                if( this.widgets && this.widgets.length > 0 )
                {
                    var widgetItemsMap = $.map( this.widgets, P.widgetItemFromWidget );
                    widgets.push( {_tagName: 'regionWidget', regionId: this.regionId,
                        widgetItems: widgetItemsMap} );
                }
            });

            return widgets;
        }
    };
})(jQuery, jQuery.Percussion);
