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

(function($,P) {

    /**
     * Controller to handle resizing page regions so that they fit
     * together. Works with page model or template model.
     */

    P.sizeController = function( model ) {

        // constants governing look and feel of puffy regions
        var paddingSize        = 10;
        var borderSize         = 1;
        var regionWidthOffset  = (paddingSize + borderSize) * 2;
        var regionHeightOffset = (paddingSize + borderSize) * 2;
        var iframe = $("#frame");
        
        return {
            puff : puff
        };
        
        // decorates the regions with borders and padding
        // and increases their width to fit content
        // used to allow access to nested regions
        // to allow dragging and dropping of new regions 
        function puff() {
            
            // calculate all region's depths
            // depth = parent regions
            // create a depth map indexed by region id
            var regionDepths = {};
            insideIframe(".perc-region").each(function() {
                var region = insideIframe(this);
                var regionId = region.attr("id");
                var regionParents = region.parents(".perc-region");
                regionDepths[regionId] = regionParents.length;
            });

            // TODO: I built a map above but then I create an array out of it making the map irrelevant
            // Remove the map implementation and just create this array in the first place
            var regionDepthsSorted = [];
            sortDepths(regionDepths,regionDepthsSorted);
            function sortDepths(regionDepths, regionDepthsSorted) {
                for (id in regionDepths) {
                    regionDepthsSorted.push({regionId: id, regionDepth: regionDepths[id]});
                }
                regionDepthsSorted.sort(function (regionx, regiony) {return regiony.regionDepth - regionx.regionDepth;});
            }

            // iterate over all regions sorted by decreasing depth
            for(i in regionDepthsSorted) {
                var regionId = regionDepthsSorted[i].regionId;
                var regionDiv = insideIframe("#"+regionId);
                puffFromRegionToParent(regionDiv);
            }

            // puff a region by iterating over its parents
            function puffFromRegionToParent(regionDiv) {
            
                // grow the parent based on the region's width
                var parent = regionDiv.parent().parent(".perc-region"); // TODO: try: var parent = $(regionDiv.parents(".perc-region")[0]);
                var children = insideIframe(parent.children().children(".perc-region")); // TODO: var children = insideIframe(parent.children(".perc-region"));
                if(children.length == 0)
                    return;
                
                var direction = parent.hasClass("perc-vertical") ? "vertical": "horizontal";
                if(direction == "vertical") {
                
                    // if the parent is vertical, then the parent's width is the largest's child's + regionWidthOffset
                    // find the largest subregion. Parent's children are region's siblings (of course)
                    var biggestWidth = -1;
                    var totalHeight  = 0;
                    children.each(function() {
                        var child = insideIframe(this);
                        if(child.width() + regionWidthOffset > biggestWidth)
                            biggestWidth = child.width() + regionWidthOffset;
                        var childHeight = insideIframe(child).outerHeight(true);
                        totalHeight += childHeight;
                    });
                    
                    // set the parent's width only if the content is bigger
                    if(biggestWidth > parent.width())
                        parent.css("width", biggestWidth);
                    if(totalHeight > parent.height())
                           parent.css("height", totalHeight);

                } else {
                
                    // if the parent is horizontal, then the width is the children's total width + regionWidthOffset
                    var totalChildrenWidth = 0;
                    children.each(function() {
                        // add up all the child region's width
                        // a region's real width is the DOM width + regionWidthOffset
                        var childWidth = insideIframe(this).outerWidth(true);
                        totalChildrenWidth += childWidth;
                    });

                    // set the parent's width only if the content is bigger
                    if(totalChildrenWidth > parent.width())
                        parent.css("width", totalChildrenWidth);
                    parent.css("height","auto");
                }
            }
       }
        
        function skipIfLeaf(regionDiv) {
            var regionId = regionDiv.attr("id");
            var skip = false;
            model.editRegion( regionId, function() {
                if((this.width || this.height) && this.children.length == 0)
                    skip = true;
            });
            return skip;
        }
        
        function insideIframe( elem ) {
            return iframe[0].contentWindow.jQuery( elem );
        }
    };
})(jQuery, jQuery.Percussion);
