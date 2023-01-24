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
