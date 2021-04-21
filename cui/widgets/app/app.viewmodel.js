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

define(['knockout','pubsub', 'utils'], function(ko,PubSub, utils) {
    return function AppViewModel(options) {
        var self = this;
        self.options = options;
        
        //****Tab sections Code*******//
        var Section = function (name, selected, enabled, info){
            self = this;
            self.name = name;
            self.enabled = ko.observable(enabled);
            self.info = ko.observable(info);
            self.isSelected = ko.computed(function(){
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