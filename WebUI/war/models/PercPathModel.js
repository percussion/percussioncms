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

// PercPathModel.js
// Author: Jose Annunziato
// Date: 1/20/2010
// Data models related to path management.
// Paths of folders, sites, assets, etc., in the finder
(function($)
{
    // JGA
    // holds data for path item details
    // currently populated by PercAssetController.js
    // controller retrieves path item from service: PercPathService.js
    // used by the controller to retrieve a finder's path's id
    // the path's id is then used to invoke an asset's form editor
    // which needs the path's id to put the asset in the folder once it creates it
    $.PercPathItemModel = function( id,
					                folderPaths,
					                icon,
					                name,
					                type,
					                folderPath,
					                leaf,
					                path)
    {
        this.id = id;
        this.folderPaths = folderPaths;
        this.icon = icon;
        this.name = name;
        this.type = type;
        this.folderPath = folderPath;
        this.leaf = leaf;
        this.path = path;
/*        
        this.log   = function()
        {
            console.log("id          = " + this.id);
            console.log("folderPaths = " + this.folderPaths);
            console.log("icon        = " + this.icon);
            console.log("name        = " + this.name);
            console.log("type        = " + this.type);
            console.log("folderPath  = " + this.folderPath);
            console.log("leaf        = " + this.leaf);
            console.log("path        = " + this.path);
        };
        */
    };
})(jQuery);