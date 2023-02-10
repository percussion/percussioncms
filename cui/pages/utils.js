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
