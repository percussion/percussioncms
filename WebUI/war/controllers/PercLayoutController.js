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

(function($,P)
{
    /** 
     * Layout controller - handles adding/moving/removing regions and
     * widgets from the model, based on region or widget id. Called from
     * the layout view (for example, when you click the delete button on a
     * region).
     * 
     * Author: Jason Priestly
     * Heavily refactored by Jose Annunziato
     */
    P.layoutController = function( model ) {
        
        var iframe = $("#frame");

        return {
            //These functions all do what they say they do
            addRegion: addRegion,
            moveRegion: moveRegion,
            removeRegion: removeRegion,
            removeWidgetParentRegion: removeWidgetParentRegion,
            addWidget: addWidget,
            moveWidget: moveWidget,
            removeWidget: removeWidget,
            convertHTMLWidget: convertHTMLWidget
        };

        function convertHTMLWidget(widgetObj, callback){
            var newWidgetId = generateWidgetId();

            var widget = { _tagName: 'widgetItem', definitionId: "percRichText", name: widgetObj.name, id: newWidgetId, properties: []};
            _addWidget( widgetObj.regionId, widget, widgetObj.id );
            
            if (typeof(widgetObj.assetId) != "undefined" && widgetObj.assetId){
                var assetInfo = model.getAssetDropCriteria()[widgetObj.id];
                var obj = {
                    "PSCreateAssetRequest":{
                        "originalAssetId": widgetObj.assetId,
                        "ownerId": widgetObj.ownerId,
                        "widgetId": newWidgetId,
                        "widgetName": widgetObj.name,
                        "targetAssetType": "percRichTextAsset",
                        "sharedAsset": assetInfo.assetShared
                    }
                }
                
                // Make the relationships
                $.PercServiceUtils.makeJsonRequest(
                    $.perc_paths.CONVERT_WIDGET,
                    $.PercServiceUtils.TYPE_POST,
                    false,
                    function(status, result)
                    {
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            removeWidget(widgetObj.regionId, widgetObj.id);
                            callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                        }
                        else
                        {
                            removeWidget(widgetObj.regionId, newWidgetId);
                            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                            callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                        }
                    },
                    obj
                );
            } else{
                removeWidget(widgetObj.regionId, widgetObj.id);
                callback($.PercServiceUtils.STATUS_SUCCESS,"");
            }
        }
        
        /**
         * Handle new region tool drop gesture
         * @param regionId (string) unique region name of region which received the drop,
         * also happens to be the ID attribute of the corresponding HTML DIV
         * @param direction (string) where the drop occured "north", "south", "east", "west", "after", "center", "below"
         * @param callback (function(regionObj)) optional callback function to call when done
         */
        function addRegion(regionId, direction, callback) {
            model.isResponsiveBaseTemplate()?_addResponsiveRegion(regionId, direction, callback):_addBaseRegion(regionId, direction, callback);
        }
        /*******Responsive region manipulation functions start ************/
        function _addResponsiveRegion(regionId, direction, callback){
            var parentRegion = null;
            model.editRegion( regionId, function() {
                parentRegion = this;
            });
            var newRegion = null;
            if(!_isInsertable(parentRegion)){
                $.perc_utils.alert_dialog({title: 'Region creation warning', content: "Reached maximum allowed columns(12) for the region."});
                callback(null);
                return;
            }
            var referenceRegion = null;
            if(direction == "after" || direction == "before" || direction == "above" || direction == "below"){
                referenceRegion = parentRegion;
                model.editRegionParent(regionId, function(){
                    parentRegion = this;
                });
            }
            if(parentRegion.children && parentRegion.children.length == 0){
                newRegion = _addToLeafRegion(parentRegion);
            }
            else{
                newRegion = parentRegion.row?_addToRow(parentRegion, direction, referenceRegion):_addToColumn(parentRegion, direction, referenceRegion);
            }
            callback(newRegion);
        }
        function _addToColumn(parentRegion, direction, referenceRegion){
            var newReg = null;
            switch(direction){
                case "north": 
                    var newRow = model.newResponsiveRegion(true);
                    parentRegion.children.unshift( newRow );
                    newReg = newRow;
                    break;
                case "south":
                    var newRow = model.newResponsiveRegion(true);
                    parentRegion.children.push( newRow );
                    newReg = newRow;
                    break;
                case "below":
                    var newRow = model.newResponsiveRegion(true);
                    var pos = _getRegionPosition(parentRegion, referenceRegion.regionId); 
                    parentRegion.children.splice(pos,0,newRow);
                    newReg = newRow;
                    break;
                case "east":
                case "west":
                    var wrapperCol = model.newResponsiveRegion(false, 11);
                    wrapperCol.children = parentRegion.children;
                    var wrapperRow = model.newResponsiveRegion(true);
                    wrapperRow.children = [wrapperCol];
                    parentRegion.children = [wrapperRow];
                    var newCol = model.newResponsiveRegion(false);
                    direction == "east"?wrapperRow.children.push(newCol):wrapperRow.children.unshift(newCol);
                    newReg = newCol;
                    break;
            }
            return newReg;
        }
        function _addToRow(parentRegion, direction, referenceRegion){
            var newReg = null;
            switch(direction){
                case "east": 
                    var newCol = model.newResponsiveRegion(false, 1);
                    parentRegion.children.push( newCol );
                    _adjustSiblingColumns(parentRegion, newCol);
                    newReg = newCol;
                    break;
                case "west":
                    var newCol = model.newResponsiveRegion(false, 1);
                    parentRegion.children.unshift( newCol );
                    _adjustSiblingColumns(parentRegion, newCol);
                    newReg = newCol;
                    break;
                case "after":
                    var newCol = model.newResponsiveRegion(false, 1);
                    var pos = _getRegionPosition(parentRegion, referenceRegion.regionId); 
                    parentRegion.children.splice(pos,0,newCol);
                    _adjustSiblingColumns(parentRegion, newCol);
                    newReg = newCol;
                    break;
                case "north":
                case "south":
                    var wrapperRow = model.newResponsiveRegion(true);
                    wrapperRow.children = parentRegion.children;
                    var wrapperCol = model.newResponsiveRegion(false, 12);
                    wrapperCol.children = [wrapperRow];
                    parentRegion.children = [wrapperCol];
                    var newRow = model.newResponsiveRegion(true);
                    direction == "north"?wrapperCol.children.unshift(newRow):wrapperCol.children.push(newRow);
                    newReg = newRow;
                    break;
            }
            return newReg;
        }
        function _adjustSiblingColumns(parentRegion, newCol){
            var newColPos = _getRegionPosition(parentRegion, newCol.regionId);
            var children = parentRegion.children;
            var adjusted = false;
            //adjust the right columns
            for(var i=newColPos + 1; i < children.length; i++){
                var large = children[i].large;
                if(large && large.length > 0){
                    var cols = large.split("-")[1];
                    cols = parseInt(cols,10);
                    if(cols > 1){
                        children[i].large = "large-" + (cols - 1);
                        adjusted = true;
                        break;
                    }
                }
            }
            //if not adjusted adjust the left columns
            if(!adjusted){
                for(var i=newColPos - 1; i >= 0; i--){
                    var large = children[i].large;
                    if(large && large.length > 0){
                        var cols = large.split("-")[1];
                        cols = parseInt(cols,10);
                        if(cols > 1){
                            children[i].large = "large-" + (cols - 1);
                            adjusted = true;
                            break;
                        }
                    }
                }
                
            }
        }
        function _isInsertable(parentRegion){
            return !(parentRegion.row && parentRegion.children && parentRegion.children.length == 12);
        }
        function _addToLeafRegion(parentRegion){
            var newReg;
            if(parentRegion.row){
                var newCol = model.newResponsiveRegion(false, 12);
                newCol.widgets = parentRegion.widgets;
                parentRegion.widgets = [];
                parentRegion.children = [newCol]; 
                newReg = newCol;
            }
            else{
                var newRow = model.newResponsiveRegion(true);
                parentRegion.children = [newRow];                
                newRow.widgets = parentRegion.widgets;
                parentRegion.widgets = [];
                newReg = newRow;
            }
            return newReg;
        }
        
        /**
         * Remove region from the model and move content to the parent region
         * @param regionId (string) unique name of the region being removed.
         * Happens to be same as ID attribute of DIV representing the region in HTML
         * @param deleteContent (bool) whether the content of the region should be removed as well
         */
        function _removeRespRegion(regionId, deleteContent){
            model.editRegion(regionId, function()
            {
                var region = this;
                var widgets = this.widgets;
                
                model.editRegionParent( regionId, function()
                {
                    var parentRegion = this;
                    // Can't delete the last region under root.
                    if( parentRegion.regionId === model.getRoot().regionId && parentRegion.children.length === 1 )
                        return;
                    var onlyChild=parentRegion.children.length === 1;
                    var adjColPos = _getRegionPosition(parentRegion, region.regionId);
                    adjColPos = adjColPos == parentRegion.children.length-1?adjColPos-1:adjColPos;
                    var deletedRegionSummary = deleteRegion( parentRegion, regionId );
                    if(region.columns && !onlyChild){
                        var children = parentRegion.children;
                        var large = children[adjColPos].large;
                        if(large && large.length > 0){
                            var delCols = region.large.split("-")[1];
                            delCols = parseInt(delCols, 10);
                            var cols = large.split("-")[1];
                            cols = parseInt(cols, 10);
                            children[adjColPos].large = "large-" + (cols + delCols);
                        }
                    }
                });
           });           
        }
        
        /**
         * Gets the position of the region from the parent.
         * @param {Object} parentReg region object parent of the supplied regId
         * @param {Object} regId
         * Throws an error if not found.
         */
        function _getRegionPosition(parentReg, regId){
            var pos = 0;
            var found = false;
            $.each(parentReg.children, function() {
                if(this.regionId === regId) {
                    found = true;
                    return false;
                }
                pos++;
            });
            if(!found){
                throw "Failed to find the position of a region with id " + regId + "as it does not exist under supplied parent region.";
            }
            return pos;
        }

        /*******Responsive region manipulation functions end ************/
       
        function _addBaseRegion(regionId, direction, callback) {
            // if adding after or below, then we add to the parent and then
            // reposition the new region after the original region
            var originalRegionId = regionId;
            if(direction === "after" || direction === "below") {
                var parentRegion = iframe.contents().find("#"+regionId).parent().parent(".perc-region");
                regionId = parentRegion.attr("id");
            }
            
            //Add a region to the given region, based on the direction
            //parameter ('east', 'west', 'north', 'south', 'after', or 'center').
            model.editRegion( regionId, function() {
                
                var regionAddingTo = this;
                if(model.isPage())
                    regionAddingTo.owner = "page";

                // dropped region tool in the center
                if(direction === "center"){
                    
                    // this is the new region that we're adding
                    // copy regionAddingTo to newRegion
                    var newRegion = model.newRegion();
                    $.perc_utils.copyRegionObject(regionAddingTo, newRegion);
                    
                    // force new region to have a dimension
                    newRegion.width  = getRegionWidth(regionAddingTo);
                    newRegion.height = getRegionHeight(regionAddingTo);
                    
                    // widgets were moved to new region so clear them from original region
                    regionAddingTo.widgets = [];
                        
                    // new region is now the sole child of the original region
                    regionAddingTo.children = [newRegion];
                        
                    // optionally return with the new region
                    if(callback && typeof(callback) === 'function')
                        callback(newRegion);
                    return;
                }
            
                //Is the new region going to be vertically aligned with its siblings?
                var vertical = ( direction === "north" || direction === "south" || direction === "below" );
                var horizontal = !vertical;

                //Is the new region going to be at the end of the child list, or the start?
                var append = (direction === "south" || direction === "east");

                //If the region needs a different alignment than the
                //current region, or if there are currently no region
                //children, then the region must be split into two
                //regions before adding the child.
                var split = ( (vertical != regionAddingTo.vertical && regionAddingTo.children.length > 1) || regionAddingTo.children.length === 0 );
                
                //--------
                // SPLIT
                //--------
                if( split )
                {
                    //Create a clone of the region to sit inside the
                    //region, and add all the region's widgets to the
                    //clone. Copy regionAddingTo to clone
                    var clone = model.newRegion();
                    $.perc_utils.copyRegionObject(regionAddingTo, clone);
                    
                    regionAddingTo.widgets = [];

                    //This is the new region that we're adding
                    var newRegion = model.newRegion();

                    regionAddingTo.children = append ? [clone, newRegion] : [newRegion, clone];
                    
                    if(horizontal) {
                        // if horizontal, the width of the two new regions is half of the original region
                        newRegion.width  = Math.floor(getRegionWidth(regionAddingTo) / 2);
                        newRegion.height = getRegionHeight(regionAddingTo);

                        // if you have the following layout where R5 is 640px
                        // and it's a vertical region with two sub regions R4 and R3
                        //    +-------------+
                        //    |     R5      |
                        //    |+-----------+|
                        //    ||    R4     ||
                        //    |+-----------+|
                        //    ||    R3     ||
                        //    ||+----+----+||
                        //    ||| R1 | R2 |||
                        //    ||+----+----+||
                        //    |+-----------+|
                        //    +-------------+
                        // and then you drop a region on the WEST of R5, the direction of R5
                        // would need to change from vertical to horizontal so we need an additional
                        // region R6 to keep direction of R4 and R3 and then a brand new region R7 on the
                        // left of R6 within R5. The width of R6 should be unchanged (not halved) when R7
                        // is added because R6 already has children. Halving R6 would entail recursively
                        // halving all of R6's children
                        //    +--------------------+
                        //    |         R5         |
                        //    |+----+-------------+|
                        //    ||    |  R6 (clone) ||
                        //    ||    |+-----------+||
                        //    ||    ||    R4     |||
                        //    ||    |+-----------+||
                        //    || R7 ||    R3     |||
                        //    ||    ||+----+----+|||
                        //    ||    ||| R1 | R2 ||||
                        //    ||    ||+----+----+|||
                        //    ||    |+-----------+||
                        //    |+----+-------------+|
                        //    +--------------------+
                        // so, we only change the width of R6 (clone) if it has no children
                        // or if the width attribute has not been set at all
                        if(clone.children.length === 0 || clone.width === "")
                            clone.width = newRegion.width;
                        clone.height = newRegion.height;
                    } else {
                        // if vertical split, the widths are left blank
                        // so regions take up all the width of parent region
                        clone.width = "";
                        clone.height = "";

                        newRegion.width = "";
                        newRegion.height = "";
                    }
                    
                    // update parent region's direction which might have changed
                    // by splitting vertically or horizontally
                    regionAddingTo.vertical = vertical;

                    // optionally callback with the new region
                    if(callback && typeof(callback) === 'function')
                        callback(newRegion);
                    
                } else {
                    //-------
                    // APPEND
                    //-------
                    
                    //We don't need to split the region - just add the new
                    //region to the region children directly
                    regionAddingTo.vertical = vertical;
                    var newRegion = model.newRegion();
                    
                    if(vertical) {
                        // if vertical append, new region's width is blank to take up all parent's width
                        newRegion.width  = "";
                        newRegion.height = "";
                        
                        // if the vertical append is by dropping in between two existing regions
                        // iterate over the parent's child regions looking for the original region
                        // that received the below drop. Insert the new region to below the
                        // the region that received the drop
                        if(direction === "below") {
                            var originalRegionPosition = 0;
                            $.each(regionAddingTo.children, function() {
                                originalRegionPosition++;
                                if(this.regionId === originalRegionId) {
                                    return false;
                                }
                            });
                            regionAddingTo.children.splice(originalRegionPosition,0,newRegion);
                        }
                    } else {
                        //------------------
                        // HORIZONTAL APPEND
                        //------------------
                        // if horizontal append, new region's width is half of first or last
                        // we are inserting after the original region
                        // the width of the new region will be half of the original region
                        // and we change the width of the original region to half as well
                        if(direction === "after") {
                            var originalRegionWidth = getRegionWidth(originalRegionId);
                            var newRegionWidth  = Math.floor(originalRegionWidth / 2);
                            var newRegionHeight = getRegionHeight(originalRegionId);
                            newRegion.width     = newRegionWidth;
                            newRegion.height    = newRegionHeight;
                            
                            // iterate over the parent's child regions looking for the original region
                            // that received the after drop. calculate its position
                            var originalRegionPosition = 0;
                            $.each(regionAddingTo.children, function() {
                                originalRegionPosition++;
                                if(this.regionId === originalRegionId) {
                                    if(!this.noAutoResize)
                                        this.width   = Math.floor(originalRegionWidth/2);
                                    newRegion.height = newRegionHeight;
                                    return false;
                                }
                            });
                            
                            // insert the new region to the right of the original region
                            regionAddingTo.children.splice(originalRegionPosition,0,newRegion);

                        } else {
                            var region;
                            if(append) {
                                // if adding to the end, width is half of the last child
                                region = regionAddingTo.children[regionAddingTo.children.length - 1];
                            } else {
                                // if adding to the beginning, width is half of the first child
                                region = regionAddingTo.children[0];
                            }
                            var regionWidth  = getRegionWidth(region);
                            var regionHeight = getRegionHeight(region);
                
                            newRegion.width  = Math.floor(regionWidth / 2);
                            newRegion.height = regionHeight;

                            if(!region.noAutoResize){
                                // dont change the width of the original region if it already has children
                                if(region.children.length === 0)
                                    region.width = newRegion.width;
                                region.height = newRegion.height;
                            }
                        }
                    }
                    
                    // add the region at the beginning or end of the parent region
                    if(direction === "after" || direction === "below") {
                        // dont care, the new region has already been spliced in
                    } else {
                        if( append )
                            regionAddingTo.children.push( newRegion );
                        else 
                            regionAddingTo.children.unshift( newRegion );
                    }

                    // optionally callback with the new region
                    if(callback && typeof(callback) === 'function')
                        callback(newRegion);
                }
            });
            
        }
        
        /**
         * Remove region from the model and move content to the parent region
         * @param regionId (string) unique name of the region being removed.
         * Happens to be same as ID attribute of DIV representing the region in HTML
         * @param deleteContent (bool) whether the content of the region should be removed as well
         */
        function removeRegion(regionId, deleteContent){
            model.isResponsiveBaseTemplate()?_removeRespRegion(regionId, deleteContent):_removeBaseRegion(regionId, deleteContent);
        }
        
        /**
         * Remove region from the model and move content to the parent region
         * @param regionId (string) unique name of the region being removed.
         * Happens to be same as ID attribute of DIV representing the region in HTML
         * @param deleteContent (bool) whether the content of the region should be removed as well
         */
        function _removeBaseRegion(regionId, deleteContent)
        {
            model.editRegion(regionId, function()
            {
	            var region = this;
	            var widgets = this.widgets;
	            
	            model.editRegionParent( regionId, function()
	            {
	                var parentRegion = this;
	
	                // Can't delete the last region under root.
	                if( parentRegion.regionId === model.getRoot().regionId && parentRegion.children.length === 1 )
	                    return;
	
	                // if the region being deleted is an only child, then the parent inherits the direction of the child
	                var onlyChild=false;
	                if(parentRegion.children.length === 1){
	                    parentRegion.vertical = parentRegion.children[0].vertical;
	                    onlyChild=true;
	                }
	                
	                // remove region with regionId from parentRegion
	                // summary contains the region deleted, its original index, and the region's children if any
	                var deletedRegionSummary = deleteRegion( parentRegion, regionId );
	                parentRegion.owner = "template";
	
	                // if we are not deleting the content and there are subregions
	                // move those sub regions to the parent region
	                if(!deleteContent && deletedRegionSummary.children.length>0) {
	                    // child regions of the deleted region
	                    var children = deletedRegionSummary.children;
	                    // original position of deleted region
	                    var index = deletedRegionSummary.index;
	                    for(c=0; c<children.length; c++){
	                        // get each child and insert it into the parent's list of regions starting at the index of the deleted region
	                        var child = children[c];
	                        parentRegion.children.splice(index, 0, child);
	                        index++;
	                    }
	                }
	                // Copy widgets to parent if appropriate
	                if(!deleteContent && typeof(widgets) != 'undefined' && widgets.length > 0)
	                {
	                   parentRegion.widgets = new Array();
	                   for(c = 0; c < widgets.length; c++)
	                   {
	                      parentRegion.widgets.push(widgets[c]);
	                   }
	                }
	            });
            });
        }
        function removeWidgetParentRegion(widgetId, deleteContent)
        {
             var pRegionId = null;
             model.findWidgetParentRegion(widgetId, function(parentRegionId){
                 pRegionId = parentRegionId;
             });
             if(pRegionId)                         
                 removeRegion(pRegionId, deleteContent);
        }
        
        function generateWidgetId(){
            var newWidgetId = "";
            while(model.editWidget(newWidgetId) || newWidgetId == "")
                newWidgetId = Math.floor( Math.random()*1000000000 );
            return newWidgetId + "";
        }
        
        //Add a new widget to region with id 'regionId'
        function addWidget( regionId, widgetDefId, beforeWidgetId, callback)
        {
            var widget = { _tagName: 'widgetItem', definitionId: widgetDefId, id: generateWidgetId(), properties: []};
            _addWidget( regionId, widget, beforeWidgetId );
            if(callback) {
                callback(widget.id);
            }
        }

        function _addWidget( regionId, widget, beforeWidgetId ) {
            
            if( regionId === model.getRoot().regionId ) {
                $.perc_utils.debug( "Add widget: widget can not be added to root region" );
                return;
            }
            model.editRegion( regionId, function() {
                
                //can't add a widget to a region with children
                if( this && !(this.children && this.children.length > 0)) {
                    
                    if(!this.widgets || this.widgets.length === 0 || !beforeWidgetId) {
                        var regiontoadd = this;
                        if(model.isResponsiveBaseTemplate() && this.row){
                            if(model.isPage()){
                                $.perc_utils.alert_dialog({title: 'Add widget warning', content: "Widgets cannot be added to this region due to a template configuration issue.  Please contact your administrator to update the template."});
                                return;
                            }
                            else{
                                addRegion(regionId,"center", function(newRegion){
                                    regiontoadd = newRegion;
                                })
                            }
                        }
                        
                        // if there are no widgets, create the array
                        if(!regiontoadd.widgets)
                           regiontoadd.widgets = new Array();
                        // and push the widget at the front
                        regiontoadd.widgets.push( widget );
                    } else {
                        // if there are widgets, then look for the afterWidgetId in the list
                        for(widgetIndex in this.widgets) {
                            if(beforeWidgetId === this.widgets[widgetIndex].id) {
                                // and put the new widget in front
                                this.widgets.splice(widgetIndex,0,widget);
                                break;
                            }
                        }
                    }
                }
                // the region becomes a page region
                this.owner = "page";
            });
        }

        function removeWidget( regionId, widgetId )
        {
            model.editRegion( regionId, function()
            {
                this.widgets = $.grep( this.widgets, function(w) {
                    var b1 = true;
                    if(typeof w !== 'undefined'){
                        return w.id != widgetId;
                    }
                     return b1;

                } );
                // if this is the last widget we are removing, then the region becomes a template region again
                if(this.widgets.length === 0)
                   this.owner = "template";
            });
        }
       
        //Move widget with id 'widgetId' from region 'fromRegion' to region
        //'toRegion', such that its position in the widget array of
        //'toRegion' is 'position'.
        function moveWidget(fromRegion, widgetId, toRegion, position) {
            
            model.editRegion( fromRegion, function() {
                
                var fromRegionObject = this;
                var toRegionObject = null;
                model.editRegion( toRegion, function() {
                    
                    toRegionObject = this;
                    if( position < 0 || position > toRegionObject.widgets.length )
                        return;

                    var widget = $.grep( fromRegionObject.widgets, function(w) {
                        var b = false;
                        if(typeof w !== 'undefined'){
                            return w.id === widgetId;
                        }
                        return b;
                    });
                    
                    removeWidget( fromRegion, widgetId );
                    toRegionObject.widgets.splice( position, 0, widget[0] );
                    toRegionObject.owner = "page";
                });
                // if removing a widget from a region leaves it with no widgets
                // then it is no longer a page region,
                // it goes back to being a template region
                if(fromRegionObject.widgets.length === 0)
                    fromRegionObject.owner = "template";
            });
        }        
        

        /**
         * Move region within its parent region, so that it occupies position 'position'
         * @param parentRegionId The parent regiond id, within which the region is moved.
         * @param movedRegion The moved region.
         * @param newPosition, the position to which it has been moved. 
         */
        function moveRegion(parentRegionId, movedRegion, newPosition)
        {
            model.editRegion( parentRegionId, function()
            {
                var parentRegion = this;
                if( newPosition < 0 || newPosition >= parentRegion.children.length )
                {
                    return;
                }

                var oldPosition;
                $.each( parentRegion.children, function(ii)
                {
                    if( this.regionId === movedRegion )
                        oldPosition = ii;
                });

                // get a hold of the original region before moving it
                var tmp = parentRegion.children[ oldPosition ];
                // we need to move the other regions around to vacate the space for the region being moved
                if(oldPosition > newPosition) {
                    // we move all the regions down to vacate the space
                    for(p=oldPosition; p>newPosition; p--)
                        parentRegion.children[ p ] = parentRegion.children[ p-1 ];
                } else {
                    // or we move all the regions up to vacate the space
                    for(p=oldPosition; p<newPosition; p++)
                        parentRegion.children[ p ] = parentRegion.children[ p+1 ];
                }
                // we put the region being moved into the new position after having moved all the other regions out of the way
                parentRegion.children[ newPosition ] = tmp;
            });
        }

        //utility functions
        function replaceRegion(parentRegion, regionId, newRegion)
        {
            $.each( parentRegion.children, function(idx)
            {
                if( this.regionId === regionId )
                {
                    parentRegion.children.splice( idx, 1, newRegion );
                    return false;
                }
            });
        }

        /**
         * Delete region with a regionId from parentRegion object
         * @param parentRegion (object) region object representing the parent of the region being deleted
         * @param regionId (string) ID attribute of DIV representing region being deleted
         */
        function deleteRegion(parentRegion, regionId)
        {
            var deletedRegionSummary;
            var deletedRegion;
            var deletedChildren;
            var deletionIndex;
            // iterate over all the parentRegion's child regions looking for the region to delete
            $.each( parentRegion.children, function(idx)
            {
                // splice it out when you find it and record the original region, its children, and index position
                if( this.regionId === regionId )
                {
                    deletedRegion = this;
                    deletedChildren = this.children;
                    deletionIndex = idx;
                    parentRegion.children.splice( idx, 1 );
                    return false;
                }
            });
            // return a summary of the region just deleted
            deletedRegionSummary = {'index':deletionIndex, 'region':deletedRegion, 'children':deletedChildren};
            return deletedRegionSummary;
        }
        // get the region width from either the model parsed from the server
        // or if it's blank, get it from the template
        function getRegionWidth(region) {
            var regionWidth = region.width;
            var regionId = region.regionId;

            if(!isObject(region)) {
                regionWidth = "";
                regionId = region;
            }
            
            // regionWidth can be:
            // undefined
            // "" - blank
            // "auto" - 
            // 123 - a number
            // 123px - pixels
            // 12% - percent
            
            if(regionWidth === "") {
                // get the width from the browser
                var regionId = "#"+regionId;
                var regionDiv = insideIframe(regionId);
                var regionWidth = regionDiv.width();
                return regionWidth;
            } else if(regionWidth === "auto") {
                return regionWidth
            } else if(!isNaN(regionWidth)) {
                // is a number
                return regionWidth;
            } else if(isNaN(regionWidth)) {
                regionWidth = regionWidth.replace("px", "");
                return regionWidth;
            }        
            
            // otherwise return unchanged
            
            return regionWidth;
        }
        
        // ditto for height
        function getRegionHeight(region) {
            var regionHeight = region.height;
            var regionId = region.regionId;

            if(!isObject(region)) {
                regionHeight = "";
                regionId = region;
            }
            
            // regionWidth can be:
            // undefined
            // "" - blank
            // "auto" - 
            // 123 - a number
            // 123px - pixels
            // 12% - percent
            
            if(regionHeight === "" || regionHeight === "auto") {
                // get the height from the browser
                var regionId = "#"+regionId;
                var regionDiv = insideIframe(regionId);
                regionHeight = regionDiv.height();
                return regionHeight;
            } else if(!isNaN(regionHeight)) {
                // is a number
                return regionHeight;
            } else if(isNaN(regionHeight) && typeof regionHeight === "string") {
                regionHeight = regionHeight.replace("px", "");
                return regionHeight;
            }        
            
            // otherwise return unchanged
            
            return regionHeight;
        }

        function isObject(o) {
            return (o && "object" === typeof o);
        }
        
        function insideIframe( elem ) {
            return iframe[0].contentWindow.jQuery( elem );
        }
        
    };
})(jQuery, jQuery.Percussion);
