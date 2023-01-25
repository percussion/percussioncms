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
