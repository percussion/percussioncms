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

define(function() {
    var format = function (f) {
        var i = 1;
        var args = arguments;
        var len = args.length;
        var str = String(f).replace(/%[sdj%]/g, function(x) {
            if (x === '%%') return '%';
            if (i >= len) return x;
            switch (x) {
                case '%s': return String(args[i++]);
                case '%d': return Number(args[i++]);
                case '%j':
                try {
                    return JSON.stringify(args[i++]);
                } catch (_) {
                    return '[Circular]';
                }
                default:
                    return x;
            }
        });
        
        for (var x = args[i]; i < len; x = args[++i]) {
            if (isNull(x) || !isObject(x)) {
                str += ' ' + x;
            } else {
                str += ' ' + inspect(x);
            }
        }
        return str;
    };
    
    var isNullOrEmpty = function (testValue) {
        return ((testValue === null) || (testValue === undefined) || (testValue === ""));
    };
    
    var ifNullOrEmpty = function (testValue, defaultValue) {
        return (isNullOrEmpty(testValue)) ? defaultValue : testValue;
    };
    
    return {
        format: format,
        isNullOrEmpty: isNullOrEmpty,
        ifNullOrEmpty: ifNullOrEmpty
    };
});
