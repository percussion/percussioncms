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

define(['knockout','pubsub', 'utils'], function(ko,PubSub, utils) {
    return function AppViewModel(options) {
        var self = this;
        self.options = options;
        
        //****Tab sections Code*******//
        var Section = function (name, selected, enabled, info){
            var self1 = this;
            self1.name = name;
            self1.enabled = ko.observable(enabled);
            self1.info = ko.observable(info);
            self1.isSelected = ko.computed(function(){
                return this === selected();
            },this);
        };
        self.selectedSection = ko.observable();
        self.sections = ko.observableArray([
            new Section("My Recent", self.selectedSection, true, null),
            new Section("My Bookmarks", self.selectedSection, true, null),
            new Section("Search Results", self.selectedSection, false, null)
        ]);
        self.searchSection = self.sections()[2];
        self.selectedSection.subscribe(function(newsec){
            PubSub.publish("TabChanged", {
                "name": newsec.name,
                "info": newsec.info()
            });
        });
        self.formatTabId = function(prefix, name){
            return utils.formatId(prefix, name);
        };
        //****************//
        
        //***Buttons click handlers****//
        self.browseLibrary = function(){
            self.options.cm1Adaptor.openLibrary();
        };
        self.addNew = function(){
			var type = "Add Content";
            PubSub.publish("OpenDialog", type);
        };

		self.myRecentOnKeydown = function(data, event){
			if(event.code == "Enter" || event.code == "Space"){
				document.activeElement.click();
			}
		};
        self.search = function(){
            var type = "Search";
            PubSub.publish("OpenDialog", type);
        };
        PubSub.subscribe("Search Executed", function(msg,criteria){
            self.searchSection.enabled(true);
            self.searchSection.info(criteria);
            self.selectedSection(self.searchSection);
        });
        //******************//
        //*****Initialization Code**********//
        self.init = function() {
            var initialScreen = getParameterByName(window.parent,"initialScreen");
            self.selectedSection(self.sections()[0]);
            if (initialScreen && initialScreen === "newitem") {
                self.addNew();
            }
            else if (initialScreen && initialScreen === "search") {
                self.search();
            }
        };
        function getParameterByName(win, name) {
            name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
            var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
                results = regex.exec(win.location.search);
            return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
        }
        //****************************//
    };
});
