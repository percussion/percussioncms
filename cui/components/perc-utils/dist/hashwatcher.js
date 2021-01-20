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

define(
    [
        './string-utils', 
        './hash-utils'
    ],
    function(strutils, hashutils) {
        var HashMap = function (hashKey, observable) {
            this.hashKey = hashKey;
            this.observables = [observable];
        }
        
        function Hashwatcher() {
            var self = this;
            self._hashMaps = {};
            
            window.addEventListener('hashchange', function () {
                var hashVars = hashutils.getHashVars();
                for (var hashKey in self._hashMaps) {
                    var hashMap = self._hashMaps[hashKey];
                    var observables = hashMap.observables;
                    for (var i = 0; i < observables.length; i++) {
                        var observable = observables[i];
                        var hashValue = hashVars[hashKey];
                        if (!strutils.isNullOrEmpty(hashValue)) {
                            observable(hashValue);
                        }
                    }
                }
            });
        }
        
        // Adds a hash-to-observable mapping to our watcher.
        // Params:
        //   options - (optional) ie: { initObservable: true, defaultValue: 'Top' }
        //           default: { initObservable: false }
        Hashwatcher.prototype.addHashSync = function(hashKey, observable, options) {
            // handle args and defaults
            var initObservable = false;
            var defaultValue = undefined;
            if (options) {
                initObservable = options.initObservable;
                defaultValue = options.defaultValue; // ok if undefined
            }
            
            // create our mapping as needed (avoid duplicates)
            if (this._hashMaps.hasOwnProperty(hashKey)) {
                if (this._hashMaps[hashKey].observables.indexOf(observable) > 0) {
                    this._hashMaps[hashKey].observables.push(observable);
                }
                else {
                    return; // no further action needed.  Observable already sync'd.
                }
            }
            else {
                this._hashMaps[hashKey] = new HashMap(hashKey, observable);
            }
            
            // setup subscription, etc.
            var hashMap = this._hashMaps[hashKey];
            observable.subscribe(function(newValue) {
                if (strutils.isNullOrEmpty(newValue)) {
                    hashutils.clearHashVar(hashMap.hashKey);
                }
                if (!strutils.isNullOrEmpty(newValue)) {
                    var hashVars = {};
                    hashVars[hashMap.hashKey] = newValue;
                    hashutils.setHashVars(hashVars);
                }
            });
            
            if (initObservable) {
                var hash = hashutils.getHashVars();
                var hashValue = hash[hashKey];
                if (defaultValue != undefined) {
                    hashValue = strutils.ifNullOrEmpty(hashValue, defaultValue);
                }
                if (!strutils.isNullOrEmpty(hashValue)) {
                    // set our observable to initial value;
                    observable(hashValue);
                }
            }
        };
        
        return Hashwatcher;
    }
);
