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

define(['knockout', 'pubsub', 'utils'], function(ko,PubSub, utils) {
    return function ContentListViewModel(options) {
        var self = this;
        self.options = options;
		
		self.constants = {
            "MY_BOOKMARKS":"My Bookmarks",
            "MY_BOOKMARKS_EMPTY_MSG": "You have not bookmarked any content. To bookmark content, open it and select the <i class='fa fa-star icon'></i> icon.",
            "MY_RECENT":"My Recent",
            "MY_RECENT_EMPTY_MSG": "This page will list the 20 most recent items you've worked on.  Add new content or open content to have it appear on this list.",
            "SEARCH_RESULTS":"Search Results",
            "SEARCH_RESULTS_EMPTY_MSG": "No content was found matching your search criteria.",
            "THUMBNAIL_UNAVAILABLE_IMG":"<div class=\"perc-missing-thumb\"><i title=\"Thumbnail is not yet available for this page.\" class=\"perc-missing-thumb-i perc-font-icon icon-camera fa-5x\"></i></div>",
            "DELETE_PROMPT_MESSAGE":"Are you sure you want to delete this item?",
            "SEARCH_MAX_RESULTS":200
		};
		
        var item = function(data){
            var itemSelf = this;
            this.path = ko.observable(data.path);
            this.fileName = ko.computed(function() {
                var str = this.path();
				if(str != null){
					var start = str.lastIndexOf('/')+1;
					return str.substring(start);
				}else{
					return str;
				}
            }, this);
            this.name = ko.observable(data.name);
            this.type = ko.observable(data.type);
            this.status = ko.observable(data.status);
            this.lastModifiedDate = ko.observable(utils.formatDate(data.lastModifiedDate));
            this.lastPublishedDate = ko.observable(utils.formatDate(data.lastPublishedDate));
            this.lastModifier = ko.observable(data.lastModifier);
            this.commentsCount = ko.observable(data.commentsCount);
            this.id = ko.observable(data.id);
            this.newCommentsCount = ko.observable(data.newCommentsCount);
            this.postDate = ko.observable(utils.formatDate(data.postDate));
            this.workflow = ko.observable(data.workflow);
			this.scheduledPublishDates = ko.observable(utils.formatDate(data.scheduledPublishDate));
			this.scheduledRemovalDates = ko.observable(utils.formatDate(data.scheduledUnpublishDate));
			this.thumbnailPath = ko.observable(data.thumbnailPath);
			
			//If thumnailPath is empty use the path
			if(!data.thumbnailPath && data.type == "Image Asset"){
			    this.thumbnailPath(data.path);
			}
			
			this.isThumbnailAvailable = ko.observable(false);
			
			//ping the thumbnail path then ping the full path if that fails
            if (this.thumbnailPath()) {
				var pingThumbnail = $.ajax({
					url: this.thumbnailPath(),
					type: 'HEAD',
					error: function(){
					    itemSelf.isThumbnailAvailable(false);
					},
					success: function(){
						itemSelf.isThumbnailAvailable(true);
					}
				});
			}
			
            this.isCopyAllowed = ko.computed(function(){
                return utils.getPathType(itemSelf.path())=='site';
            });
			
            this.isBookmarkAllowed = ko.computed(function(){
                return utils.getPathType(itemSelf.path())=='site' && self.currentTab() != 'My Bookmarks';
            });

            this.isSelected = ko.observable(false);
            this.showRollover = function(data, event) {
                this.isSelected(true);
            };
            this.hideRollover = function(data, event) {
                this.isSelected(false);
            };
        }
				
		self.contentListItems = ko.observableArray();
		self.isLoading = ko.observable(false);
		self.debug = ko.observable(options.debug);
		self.emptyContentMessage = ko.observable("");
		self.currentTab = ko.observable(self.constants.MY_RECENT);
        self.searchInfo = {"id":-1};
        self.searchResults = [];
        self.hasMoreResults = ko.observable(false);
        self.showMoreResultsRow = ko.computed(function(){
            return  self.currentTab() == self.constants.SEARCH_RESULTS && self.hasMoreResults; 
        });
		self.isEmpty = ko.computed(function () {
            return self.contentListItems().length == 0 ? false : true;
        }, self);
        self.showMoreResults = function(){
            getSearchResults(self.searchInfo, true);
        }
        PubSub.subscribe("TabChanged",function(msg,tabInfo){
            var tabName = tabInfo.name;
            if(tabName == self.constants.MY_BOOKMARKS){
                getMyBookmarks();
				self.currentTab(tabName); 
            }
            else if(tabName == self.constants.MY_RECENT){
                getRecentList();     
				self.currentTab(tabName); 
            }
            else if(tabName == self.constants.SEARCH_RESULTS){
                getSearchResults(tabInfo.info, false);     
                self.currentTab(tabName); 
            }
            
        });
        function refreshView(){
            var tabName = self.currentTab();
            if(tabName == self.constants.MY_BOOKMARKS){
                getMyBookmarks();
            }
            else if(tabName == self.constants.MY_RECENT){
                getRecentList();     
            }
            else if(tabName == self.constants.SEARCH_RESULTS){
                getSearchResults(self.searchInfo, false);     
            }
        }
        function getMyBookmarks(){
            self.isLoading(true);
            self.options.cm1Adaptor.myBookmarks().done(function(results){
                var items = [];
                ko.utils.arrayForEach(results.ItemProperties, function(itemdata) {
                    items.push(new item(itemdata));
                });
				if(items.length == 0){
                    self.emptyContentMessage(self.constants.MY_BOOKMARKS_EMPTY_MSG);
                }
                self.contentListItems(items);
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        function getRecentList(){
            self.isLoading(true);
            self.options.cm1Adaptor.recentList("non-archived-item").done(function(results){
                var items = [];
                ko.utils.arrayForEach(results.data.ItemProperties, function(itemdata) {
                    items.push(new item(itemdata));
                });
				if(items.length == 0){
                    self.emptyContentMessage(self.constants.MY_RECENT_EMPTY_MSG);
                }
                self.contentListItems(items);
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
               self.isLoading(false);
            });
        }
        function getSearchResults(searchInfo, showMore){
            self.isLoading(true);
            if(self.searchInfo.id == searchInfo.id && !showMore){
                //User did not execute a new search but just clicked on search results tab display the cached results
                self.contentListItems(self.searchResults);
                self.isLoading(false);
            }
            else{
                searchInfo.criteria.SearchCriteria.maxResults = self.constants.SEARCH_MAX_RESULTS;
                self.options.cm1Adaptor.getSearchResults(searchInfo.criteria).done(function(results){
                    var items = [];
                    var children = results.PagedItemPropertiesList.childrenInPage;
                    searchInfo.criteria.SearchCriteria.startIndex = results.PagedItemPropertiesList.startIndex + self.constants.SEARCH_MAX_RESULTS;
                    self.searchInfo = searchInfo;
                    self.hasMoreResults(searchInfo.criteria.SearchCriteria.startIndex <= results.PagedItemPropertiesList.childrenCount);
                    if(!$.isArray(children)){
                        children = [children];
                    }
                    if(results.PagedItemPropertiesList.childrenCount > 0){
                        ko.utils.arrayForEach(children, function(itemdata){
                            items.push(new item(itemdata));
                        });
                    }
                    if (items.length == 0) {
                        self.emptyContentMessage(self.constants.SEARCH_RESULTS_EMPTY_MSG);
                    }
                    self.searchResults = showMore?self.searchResults.concat(items):items;
                    self.contentListItems(self.searchResults);
                }).fail(function(message){
                    PubSub.publish("OpenErrorDialog", message);
                }).always(function(){
                    self.isLoading(false);
                });
            }
        }
        function removeItemFromSearchResults(item){
            if(!$.isArray(self.searchResults))
                return;
            var index = -1;
            for(i=0;i<self.searchResults.length;i++){
                if(self.searchResults[i].id == item.id){
                    index=i;
                    break;
                }
            }
            if(index!=-1)
                self.searchResults.splice(index, 1);
        }
        /**
         * Initialize the view model.
         */
        self.init = function() {
        }
        
        self.launchOpen = function(item) {
            self.options.cm1Adaptor.openItem(item.path(),item.id()).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
			}).always(function(){
                item.isSelected(false);
            });
        };
        
        self.launchPreview = function(item) {
            self.options.cm1Adaptor.previewItem(item.path(),item.id()).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
			}).always(function(){
                item.isSelected(false);
            });
        };
        
        self.launchCopy = function(item) {
            self.options.cm1Adaptor.copyItem(item.path(),item.id()).done(function(){
                refreshView();
            }).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
            }).always(function(){
                item.isSelected(false);
            });
        };
        
        self.launchDelete = function(item) {
            var forceDelCallback = function(){
                self.options.cm1Adaptor.forceDeleteItem(item.path(), item.id(), item.name()).done(function(){
                    removeItemFromSearchResults(item);
                    refreshView();
                }).fail(function(fdmsg){
                    PubSub.publish("OpenErrorDialog", fdmsg);
                }).always(function(){
                    item.isSelected(false);
                });
            }
            var delCallback = function(){
                    self.options.cm1Adaptor.deleteItem(item.path(), item.id(), item.name()).done(function(){
                        removeItemFromSearchResults(item);
                        refreshView();
                    }).fail(function(result){
                        var message = result.content;
                        if(!result.canForceDelete){
                            PubSub.publish("OpenErrorDialog", message);
                        }
                        else{
                            var data = {message: message, forceChkBoxId:result.chkBoxId, title:"Confirmation", primaryButton:{"text":"Yes", callback:forceDelCallback}};
                            PubSub.publish("OpenForceDeleteConfirmationDialog", data);
                        }
                    }).always(function(){
                        item.isSelected(false);
                    });
            }
			var data = {message: self.constants.DELETE_PROMPT_MESSAGE, title:"Confirmation", primaryButton:{"text":"Yes", callback:delCallback}};
			PubSub.publish("OpenConfirmationDialog", data);
        };
		self.launchBookmark = function(item) {
            self.options.cm1Adaptor.bookMarkItem(item.path(),item.id()).fail(function(message){
                PubSub.publish("OpenErrorDialog", message);
			}).always(function(){
                item.isSelected(false);
            });
        };
        
		$(function() {
			$( document ).tooltip({
				show: {
					delay: 1000
				}
			});
		});
		
        //Custom binding for jquery fadeout/fadein as well as inverting the menu if not visible
        ko.bindingHandlers.fadeVisible = {
            init: function(element, valueAccessor) {
                var value = valueAccessor();
                $(element).toggle(ko.unwrap(value));
            },
            update: function(element, valueAccessor) {
                var value = valueAccessor();
                ko.unwrap(value) ? $(element).stop().delay(300).fadeIn() : $(element).stop().stop().fadeOut(0);
                if(ko.unwrap(value)) {
                    var elm = $(element);
                    var offset = $(".content-list-rollover-menu:visible").offset();
                    var containerOffset = $(".content-list").offset();
					var rowHeight = $(".row").height();
                    var t = containerOffset.top + offset.top - $(window).scrollTop();
                    var h = elm.height();
                    var docH = $(window).height();
                    
                    var isEntirelyVisible = (t + h - rowHeight <= docH);
                    
                    if(! isEntirelyVisible) {
                        $(element).addClass('preview-inverted');
                    }
                    else {
                        $(element).removeClass('preview-inverted');
                    }
                }
            }
        };
    }
});
