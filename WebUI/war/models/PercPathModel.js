/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
