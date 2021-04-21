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

// assumed dependencies:
//  - require.js

define
(
    [
       'jquery'
    ],
    function ($) {
        function formatDate(date) {
            if(!date)
                return "";
            var year = date.substr(0,4);
            var months = [ "January", "February", "March", "April", "May", "June", 
               "July", "August", "September", "October", "November", "December" ];
            var month = months[parseInt(date.substr(5,2))-1];
            var day = date.substr(8,2);
            return month + " " + day + ", " + year;
        }
        function getPathType(path){
            var pathType = "unknown";
            if(!path)
                return pathType;
            var pathLower = path.toLowerCase();
            if (pathLower.match("^//sites/") || pathLower.match("^/sites/") || 
                pathLower.match("^sites/")) {
                    pathType = "site";
            }
            else if(pathLower.match("^//assets/") || pathLower.match("^/assets/") || 
                pathLower.match("^assets/")) {
                    pathType = "asset";
            }
            return pathType;            
        }
        function getNormalizedPath(path){
            var normPath = path, pathLower = path.toLowerCase();

            if(pathLower.match("^//sites/") || pathLower.match("^//assets/") || pathLower.match("^/sites/") || pathLower.match("^/assets/") || pathLower.match("^sites/") || pathLower.match("^assets/")){
                if(pathLower.match("^sites/") || pathLower.match("^assets/"))
                    normPath = "/" + normPath;
                else if(pathLower.match("^//sites/") || pathLower.match("^//assets/"))
                    normPath = normPath.substring(1);
            }
            return normPath;            
        }
        function formatId(prefix, name){
            return prefix + '-' + name.toLowerCase().replace(' ','-');
        }

        var api = {
            formatDate : formatDate,
            getPathType: getPathType,
            formatId: formatId,
            getNormalizedPath : getNormalizedPath
        };
        return api;
    }
);
