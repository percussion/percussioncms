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

define(['./string-utils'], function(strutils) {
    var getHashVars = function () {
        var hashString = window.location.hash;
        
        // chop off leading '#'
        if ((hashString.length > 0) && (hashString.charAt(0) == '#')) {
            hashString = hashString.slice(1);
        }
        
        return nvpStringToObject(hashString);
    };
    
    var clearHashVar = function (propertyToRemove) {
        var currentHashVars = getHashVars();
        if (currentHashVars[propertyToRemove]) {
            delete currentHashVars[propertyToRemove];
        }
        
        var hashValue = objectToNvpString(currentHashVars);
        if (hashValue.length > 0) {
            hashValue = '#' + hashValue;
        }
        
        window.location.hash = hashValue;
    };
    
    var setHashVars = function (newVarsObject) {
        var currentHashVars = getHashVars();
        var updatedHashVars = $.extend(currentHashVars, newVarsObject);
        window.location.hash = '#' + objectToNvpString(updatedHashVars);
    };
    
    var nvpStringToObject = function (nvpString) {
        var vars = {};
        if (strutils.isNullOrEmpty(nvpString)) {
            return vars; // bail out if no string to parse.
        }
        
        var nvps = nvpString.split('&'); // name-value-pairs
        for (var i = 0; i < nvps.length; i++) {
            var nvp = nvps[i].split('=');
            vars[nvp[0]] = nvp[1];
        }
        return vars;
    };
    
    var objectToNvpString = function (object) {
        var nvpString = "";
        for (var p in object) {
            nvpString += p + '=' + escape(object[p]) + '&';
        }
        
        // chop off last '&'
        nvpString = nvpString.substr(0, nvpString.length - 1);
        return nvpString;
    };
    
    return {
        getHashVars: getHashVars,
        clearHashVar: clearHashVar,
        setHashVars: setHashVars
    };
});
