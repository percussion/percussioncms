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

/**
 * PercFinderView.js
 *
 * Handles user interaction with Finder on any but Editor page.
 * Most of the stuff has been duplicated over from PercPageView.js,
 * so when refactoring at a higher level view, please get rid of this file.
 *
 */
(function($, P)
{

    P.PercFinderView = function()
    {
        var percFinderListviewContainerInitialHeight;
        var percFinderListviewContainer;
        var PERC_FINDER_VIEW_COLUMN = "column";
        var PERC_FINDER_VIEW_LIST = "list";
        var PERC_FINDER_SEARCH_RESULTS = "search";
        var PERC_FINDER_SEARCH_TYPE_MYPAGES = "MyPages";
        var PERC_FINDER_RESULT = "result";
        var MAX_RESULTS;
        var utils = $.perc_utils;
        var currentContentPath;
        var currentContentId; // current page id being edited
        var finderButtons; // buttons on the top right of the finder: new site, page, delete site
        var dirty = false; // dirty page
        var finder = $.perc_finder(); // the finder, the miller column
        var contentId = null;
        var pageView = P.pageView();
        var dialogFlag = true;

        // Expose the setView method (so we can use it from perc_finder.js, for example)
        this.setView = setView;

        // choose column or list view
        var chooseColumnView = $("#perc-finder-choose-columnview").on("click",
            function(evt){
                setViewColumn(evt);
            });

        var chooseListView = $("#perc-finder-choose-listview").on("click",
            function(evt){
                setViewList(evt);
            });
        var chooseSearchView = $("#perc-finder-search-submit").on("click",
            function(evt){
                setViewSearch(evt);
            });
        var chooseMyPagesView = $("#perc-finder-choose-mypagesview").on("click",
            function(evt){
                setMyPagesView(evt);
            });

        setColumnViewButtonOn();

        // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
        var dirtyController = $.PercDirtyController;

        // Interface to local API to pass around to Finder and Page Edit Dialog
        // so they can call back and update the Content tab
        var percFinderViewAPI = {
            reload: function()
            {
            },
            getContentId: function()
            {
                return currentContentId;
            },
            clear: function()
            {
                $('#frame').each(function()
                {
                    $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                });
            },
            confirm_if_dirty: confirm_if_dirty,
            save: save
        };

        /**
         * Helper function to enable or disable the search bar of the finder. This method is exposed to be used by perc_finder.js
         * @param flag(boolean) if <code>true</code> the search bar (including the button) is disabled, otherwise the is enabled.
         */
        this.disableSearchBar = function disableSearchBar(disable)
        {
            if (disable)
            {
                $("#perc-finder-item-search").attr('disabled', true);
                $("#perc-finder-item-search").val("");
                chooseSearchView.off("click");
            }
            else
            {
                $("#perc-finder-item-search").prop('disabled',false);
                chooseSearchView.on("click",function(evt){
                    setViewSearch(evt);
                });
            }
        };

        /**
         * Helper function to enable or disable the List View button of the finder. This method is exposed to be used by perc_finder.js
         * @param flag(boolean) if <code>true</code> the button is disabled, otherwise the button is enabled.
         */
        this.disableListViewButton = function disableListViewButton(disable)
        {
            if (disable)
            {
                chooseListView.off("click");
            }
            else
            {
                chooseListView.on("click",function(evt){
                    setViewList(evt);
                });
            }
        };

        var currentFinderView = PERC_FINDER_VIEW_COLUMN;
        // Expose the current view properties
        this.getCurrentFinderView = function()
        {
            return currentFinderView;
        };
        this.PERC_FINDER_VIEW_COLUMN = PERC_FINDER_VIEW_COLUMN;
        this.PERC_FINDER_VIEW_LIST = PERC_FINDER_VIEW_LIST;
        this.PERC_FINDER_SEARCH_RESULTS = PERC_FINDER_SEARCH_RESULTS;
        this.PERC_FINDER_RESULT = PERC_FINDER_RESULT;
        var pagingBar = "";

        // Bind the Enter key and Esc key on Input search field
        $("#perc-finder-item-search").on("focus",function(evt)
        {
            $(this).css('color', '#FFFFFF').css('background-color', '#5a5d69');
        }).on('keyup', function(evt)
        {
            if (evt.keyCode === 13)
            {
                $("#perc-finder-item-search").trigger("blur");
                performSearch();
                $("#perc-finder-item-search").trigger("focus");
                evt.preventDefault();
                evt.stopPropagation();
            }
            if (evt.keyCode === 27 || evt.keyCode === 9)
            {
                $("#perc-finder-item-search").css('color', '#CCCCCC').css('background-color', '#41434F').trigger("blur");
                evt.preventDefault();
                evt.stopPropagation();
            }
            $('.perc-finder-message').fadeOut(function () { $(this).empty(); });
        });

        //Set the newStartIndex and refresh the view
        function pagePrevious(event)
        {
            percFinderListviewContainer = $(".perc-finder").find('#perc-finder-listview');
            percFinderListviewContainer.data('startIndex', percFinderListviewContainer.data('startIndex') - MAX_RESULTS);
            percFinderListviewContainer.data('callback', updatePagingBar);
            refreshView();
        }

        //Set the newStartIndex and refresh the view
        function pageNext(event)
        {
            var percFinderListviewContainer = $(".perc-finder").find('#perc-finder-listview');
            percFinderListviewContainer.data('startIndex', percFinderListviewContainer.data('startIndex') + MAX_RESULTS);
            percFinderListviewContainer.data('callback', updatePagingBar);
            refreshView();
        }

        //Perform a refresh of the current view
        //This function is called after paging, sorting and new search when the user is already in search view
        function refreshView()
        {
            percFinderListviewContainer = $(".perc-finder").find('#perc-finder-listview');
            $(".perc-fixedtableheader").remove();
            percFinderListviewContainer.empty();
            percFinderListviewContainer.css('text-align', 'left');
            percFinderListviewContainer.append($('<div class="perc-finder-panel-loading"><span class="icon-spinner icon-spin icon-2x"></span>&nbsp;Loading...</div>'));
            var column = percFinderListviewContainer.data('sortColumn');
            var order = percFinderListviewContainer.data('sortOrder');
            var startIndex = percFinderListviewContainer.data('startIndex');
            var callbackFunction = percFinderListviewContainer.data('callback');
            var config = {
                "startIndex": startIndex,
                "maxResults": MAX_RESULTS,
                "callback": callbackFunction,
                "sortFunction": sortView,
                "sortColumn": column,
                "sortOrder": order
            };
            percFinderListviewContainer = $("#perc-finder-listview");
            percFinderListviewContainerInitialHeight = percFinderListviewContainer.height();
            if (currentFinderView === PERC_FINDER_VIEW_LIST)
            {
                config.path = percFinderListviewContainer.data('path');

                fillListView(config, function()
                {
                    expandTableBorders(true);
                });
            }
            else if (currentFinderView === PERC_FINDER_SEARCH_RESULTS || currentFinderView === PERC_FINDER_SEARCH_TYPE_MYPAGES)
            {
                var searchQuery = $("#perc-finder-listview").data('searchQuery');
                var searchType = percFinderListviewContainer.data('searchType');
                config.searchCriteria = {
                    "SearchCriteria": {
                        "query": searchQuery,
                        "searchType":searchType,
                        "startIndex": startIndex,
                        "maxResults": MAX_RESULTS,
                        "sortColumn": column,
                        "sortOrder": order
                    }
                };
                fillResultView(config, function()
                {
                    expandTableBorders(true);
                });
            }
        }

        function expandTableBorders(expand)
        {
            if (percFinderListviewContainer)
            {
                var finder = $(".perc-finder");

                var lastRow = percFinderListviewContainer.find("tr:last");
                lastRowHeight = lastRow.height();
                var containerElement = $("#perc-finder-listview table:first");
                var containerHeight = containerElement.height();
                var fixedColumnHeight = $(".perc-view-column-fixed").height();
                var differenceValue = fixedColumnHeight - containerHeight;
                if (expand)
                {
                    if (lastRowHeight > 0)
                        lastRow.height(lastRowHeight + differenceValue);
                }
                else
                {
                    lastRow.height(20);
                }
            }
        }
        //Set the content of the paging bar (used by showPagingBar and updatePagingBar)
        function fillPagingBar(dir)
        {
            var startIndex = dir.data('startIndex');
            var totalResult = dir.data('totalResult');
            var endIndex = ((startIndex - 1 + MAX_RESULTS > totalResult) ? totalResult : (startIndex - 1 + MAX_RESULTS));
            var itemText = "0 Items";
            if (totalResult !== 0)
                itemText = startIndex + " - " + endIndex + " of " + totalResult + " Total";

            //Fill text info.
            pagingBar.find(".perc-pagingbar-items").text(itemText);

            //Enable/disable navigation buttons
            //Check if have next items
            if ((totalResult - (startIndex - 1 + MAX_RESULTS)) > 0)
                pagingBar.find('.perc-pagingbar-next').removeClass('perc-disabled-navigator').off('click').on("click",
                    function(evt){
                        pageNext(evt);
                    });
            else
                pagingBar.find('.perc-pagingbar-next').addClass('perc-disabled-navigator').off('click');

            //Check if have previous items
            if (startIndex > MAX_RESULTS)
                pagingBar.find('.perc-pagingbar-previous').removeClass('perc-disabled-navigator').off('click').on("click",
                    function(evt){
                        pagePrevious(evt);
                    });
            else
                pagingBar.find('.perc-pagingbar-previous').addClass('perc-disabled-navigator').off('click');
        }

        //Attach the paging bar
        function showPagingBar(dir)
        {
            if (currentFinderView === PERC_FINDER_VIEW_LIST || currentFinderView === PERC_FINDER_SEARCH_RESULTS)
            {
                var finderDiv = $(".perc-finder");
                //Generate the HTML Bar
                pagingBar = $('<div class="perc-pagingbar-finder"/>').append($('<div class="perc-pagingbar-navigator" />').append($('<a class="perc-pagingbar-previous" />').attr('title', 'Previous')).append($('<a class="perc-pagingbar-next"/>').attr('title', 'Next'))).append($('<span class="perc-pagingbar-items" />')
                );

                fillPagingBar(dir);
                var newHeight = finderDiv.height();
                finderDiv.append(pagingBar);
                fixIframeHeight();
                fixHeight();
            }
            // Update the lower part of Navigation/User page.
            var currentView = $.PercNavigationManager.getView();
            if (currentView === $.PercNavigationManager.VIEW_SITE_ARCH || currentView === $.PercNavigationManager.VIEW_USERS)
            {
                fixIframeHeight();
                fixBottomHeight();
                fixHeight();
                if ($("#perc_site_map").length > 0)
                {
                    $("#perc_site_map").perc_site_map('layoutAll');
                }
            }
            // make sure the finder adjusts height based on paging bar showing or hiding
            finder.update_finder_height();
        }

        //Update an existing paging bar
        function updatePagingBar(dir)
        {
            if (currentFinderView === PERC_FINDER_VIEW_LIST || currentFinderView === PERC_FINDER_SEARCH_RESULTS)
            {
                fillPagingBar(dir);
            }
        }

        //Create the new container and change view
        function addListViewContainer()
        {
            var finderTable = $(".perc-finder-table");
            var allColumns = finderTable.find("td.mcol-direc");
            var lastColumn = finderTable.find("td.mcol-direc:last");

            $(allColumns[0]).siblings().hide(); //Hide all columns
            var newColumn = $.perc_finderInstance.insertAfter(lastColumn); //Add a new column to contains the List View or Result
            newColumn.css("width", "100%");
            var newColumnContent = newColumn.find(".mcol-direc-wrapper");
            newColumnContent.find(".ui-resizable-handle").remove(); //Remove the div for resize width

            //Add List View container
            percFinderListviewContainer = $("<div id='perc-finder-listview'>");
            newColumnContent.css("width", "100%");
            newColumnContent.append(percFinderListviewContainer);

            return percFinderListviewContainer;
        }

        //Call the List View service with a specific config
        function fillListView(config, callback)
        {
            //Get and set the display format
            $.PercPathService.getDisplayFormat(function(status, displayFormat)
            {
                config.displayFormat = displayFormat;
                config.folderDblClickCallback = function(contentPath)
                {
                    _updateListViewContainerData(contentPath, 1, updatePagingBar);
                };
                $.PercPathService.getContentForPath(displayFormat.SimpleDisplayFormat, config, function(status, content)
                {
                    if (status)
                    {
                        percFinderListviewContainer.PercFinderListView(config, content);
                        var newPath = $("#mcol-path-summary").val().trim();
                        $.PercPathService.getPathItemForPath(newPath, function(status, content)
                        {
                            if (content.PathItem.type === "Folder" || content.PathItem.type === "FSFolder")
                            {
                                $(".perc-finder-menu #perc-finder-delete").removeClass('ui-enabled').addClass('ui-disabled').off('click');
                                if (callback)
                                    callback();
                                return;
                            }
                            var index = newPath.lastIndexOf('/');
                            newPath = newPath.substring(index + 1);
                            $('#perc-finder-listview td.perc-first').each(function()
                            {
                                var self = $(this);
                                var selectedText = self.find('span').text();
                                if (newPath === selectedText)
                                {
                                    self.parent().trigger("click");
                                    finder.scrollIntoView(self);
                                }
                            });
                            if (callback)
                                callback();
                        });
                    }
                    else
                    {
                        var error = $("<span style='font-weight: normal; margin-top: 15px; display:block'/>").text(result);
                        $("#perc-finder-listview").css('text-align', 'center').append(error);
                        $(".perc-finder-panel-loading").remove();
                    }

                    percFinderListviewContainer = $("#perc-finder-listview");
                    percFinderListviewContainerInitialHeight = percFinderListviewContainer.height();
                });
            });
        }

        //Call the Result View service with a specific config
        function fillResultView(config, callback)
        {
            //Get and set the display format
            $.PercPathService.getDisplayFormat(function(status, displayFormat)
            {
                config.displayFormat = displayFormat;
                config.searchCriteria.SearchCriteria.formatId = displayFormat.SimpleDisplayFormat.id;
                config.folderDblClickCallback = function(contentPath)
                {
                    _updateListViewContainerData(contentPath, 1, updatePagingBar);
                };
                $.PercSearchService.getSearchResult(config.searchCriteria, function(status, result)
                {
                    if (status)
                    {
                        $("#perc-finder-listview").PercFinderListView(config, result);
                        if (callback)
                            callback();
                    }
                    else
                    {
                        var error = $("<span style='font-weight: normal; margin-top: 15px; display:block'/>").text(result);
                        $("#perc-finder-listview").css('text-align', 'center').append(error);
                        $(".perc-finder-panel-loading").remove();
                    }
                });
            });
        }

        /**
         * Checks if the we are switching to the search view. If we are switching the view calls
         * setView, if not re-fills the result view.
         */
        function performSearch()
        {
            if (currentFinderView === PERC_FINDER_SEARCH_RESULTS)
            {
                // URL-encode the text to avoid jQuery bug:
                var encodedSearchText = encodeURIComponent($("#perc-finder-item-search").val().trim());
                var percFinderListviewContainer = $(".perc-finder").find('#perc-finder-listview');
                percFinderListviewContainer.data('searchQuery', encodedSearchText);
                percFinderListviewContainer.data('startIndex', 1);
                percFinderListviewContainer.data('sortColumn', "sys_title");
                percFinderListviewContainer.data('sortOrder', "asc");
                percFinderListviewContainer.data('callback', updatePagingBar);
                refreshView();
            }
            else
            {
                setView(PERC_FINDER_SEARCH_RESULTS);
            }
            setSearchViewButtonsStates();
            $("#mcol-path-summary").val("");
        }

        //Set the new column and order to sort and refresh the view
        function sortView(column, order)
        {
            var percFinderListviewContainer = $(".perc-finder").find('#perc-finder-listview');
            percFinderListviewContainer.data('startIndex', 1);
            percFinderListviewContainer.data('sortColumn', column);
            percFinderListviewContainer.data('sortOrder', order);
            percFinderListviewContainer.data('callback', updatePagingBar);
            refreshView();
        }

        /**
         * Calls the setView method to set the finder in Column mode.
         * It is generally bound to click events on buttons that switch views.
         */
        function setViewColumn(event)
        {
            setView(PERC_FINDER_VIEW_COLUMN);
        }

        /**
         * Calls the setView method to set the finder in List mode.
         * It is generally bound to click events on buttons that switch views.
         */
        function setViewList(event)
        {
            setView(PERC_FINDER_VIEW_LIST);
        }

        /**
         * Calls the setView method to set the finder in Search results mode.
         * It is generally bound to click events on buttons that switch views.
         */
        function setViewSearch(event)
        {
            performSearch();
        }

        function setMyPagesView(event)
        {
            setView(PERC_FINDER_SEARCH_TYPE_MYPAGES, true);
        }

        /** set the button state for the active view */
        function set_view_options_button_state(view) {
            $('.perc-finder-view-options a').removeClass('ui-active');
            switch (view) {
                case PERC_FINDER_VIEW_COLUMN:
                    chooseColumnView.addClass('ui-active');
                    break;
                case PERC_FINDER_VIEW_LIST:
                case PERC_FINDER_SEARCH_RESULTS:
                    chooseListView.addClass('ui-active');
                    break;
                case PERC_FINDER_SEARCH_TYPE_MYPAGES:
                    chooseMyPagesView.addClass('ui-active');
                    break;
                default:
                    chooseColumnView.addClass('ui-active');
            }
        }

        /**
         * Sets the finder view to their column, list or search results view
         * @param view a String property representing the desired view.
         * @param forceSet if true sets the view even if the currentFinderView is same as supplied view.
         */
        function setView(view, forceSet)
        {
            var force = forceSet?true:false;
            MAX_RESULTS = $.perc_finderInstance.maxResults;

            // clear the view icon active state
            set_view_options_button_state(view);

            if (view === currentFinderView && !force)
                return;

            if ((currentFinderView === PERC_FINDER_SEARCH_RESULTS && view === PERC_FINDER_VIEW_LIST) ||
                (currentFinderView === PERC_FINDER_SEARCH_TYPE_MYPAGES && view === PERC_FINDER_VIEW_LIST))
                return;

            currentFinderView = view;
            var finderTable = $(".perc-finder-table");
            var allColumns = finderTable.find("td.mcol-direc");
            var lastColumn = finderTable.find("td.mcol-direc:last");

            allColumns.find("#perc-finder-listview").parents("td").remove(); //Remove the column of List View.
            //Remove the Paging Bar and resize the Finder.
            var finderDiv = $(".perc-finder");
            if (pagingBar)
            {
                pagingBar.remove();
                fixIframeHeight();
                fixHeight();

                pagingBar = null;
                // Update the lower part of Navigation/User page.
                var currentView = $.PercNavigationManager.getView();
                if (currentView === $.PercNavigationManager.VIEW_SITE_ARCH || currentView === $.PercNavigationManager.VIEW_USERS)
                {
                    fixIframeHeight();
                    fixBottomHeight();
                    fixHeight();

                    if ($("#perc_site_map").length > 0)
                    {
                        $("#perc_site_map").perc_site_map('layoutAll');
                    }
                }
            }

            $('.perc-finder-body').attr('perc-view', view);

            if (view === PERC_FINDER_VIEW_COLUMN)
            {
                allColumns.show();
                finder.open(finder.getCurrentPath());
                setColumnViewButtonOn();
            }
            else if (view === PERC_FINDER_VIEW_LIST)
            {
                percFinderListviewContainer = addListViewContainer();
                var contentPath = lastColumn.data('path');
                //if the column doesn't have path data is a summary column
                if (typeof(contentPath) === 'undefined')
                {
                    lastColumn = lastColumn.prev();
                    contentPath = lastColumn.data('path').join("/");
                }
                else
                {
                    contentPath = contentPath.join("/");
                }
                _updateListViewContainerData(contentPath, lastColumn.data('startIndex'), showPagingBar);
                setListViewButtonOn();

            }
            else if (view === PERC_FINDER_SEARCH_RESULTS || view === PERC_FINDER_SEARCH_TYPE_MYPAGES)
            {
                $(".perc-view-column-fixed a").removeClass('mcol-opened');
                $("#perc-finder-listing-Search").addClass('mcol-opened');

                var percFinderListviewContainer = addListViewContainer();
                var searchQuery = encodeURIComponent($("#perc-finder-item-search").val().trim());
                if (searchQuery === null)
                {
                    // The original code appends the following message "unformatted (it lacks the
                    // columns of the search list view) and hardcoded":
                    //$("#perc-finder-listview").append("<p>Please enter the keyword to search for</p>");
                    //return;

                    // Instead we can make an "empty string search", that shows the same result as
                    // searching for an emtpy string in the search list view
                    searchQuery = "";
                }
                percFinderListviewContainer.data('searchQuery', searchQuery);
                percFinderListviewContainer.data('startIndex', 1);
                percFinderListviewContainer.data('sortColumn', "sys_title");
                percFinderListviewContainer.data('sortOrder', "asc");
                percFinderListviewContainer.data('callback', showPagingBar);
                if(view === PERC_FINDER_SEARCH_TYPE_MYPAGES)
                {
                    percFinderListviewContainer.data('searchType', PERC_FINDER_SEARCH_TYPE_MYPAGES);
                }
                refreshView();
                setListViewButtonOn(view);
                setSearchViewButtonsStates();
                finder.setStateButtonsDesignNode(false);
                $("#mcol-path-summary").val(searchQuery);
            }
        }

        /**
         * Updates the list view containers data and calls the refreshView method to refresh the list view.
         * @param {String} contentPath the root path of the list view
         * @param {int} startIndex the starting index of the list view
         * @param {function} pagingBarCallback the pagination bar call back.
         */
        function _updateListViewContainerData(contentPath, startIndex, pagingBarCallback)
        {
            percFinderListviewContainer.data('path', contentPath);
            percFinderListviewContainer.data('startIndex', startIndex);
            percFinderListviewContainer.data('sortColumn', 'sys_title');
            percFinderListviewContainer.data('sortOrder', 'asc');
            percFinderListviewContainer.data('callback', pagingBarCallback);

            refreshView();
        }

        /**
         * Sets the corresponding styles to column and list view button. Called when changing
         * views from the setView() method.
         * @param view If the current view is Search result, prevent the default behavior of the button.
         */
        function setListViewButtonOn(view)
        {
            $("#perc-finder-choose-view #perc-finder-choose-columnview").removeClass("ui-enabled");
            $("#perc-finder-choose-view #perc-finder-choose-listview").addClass("ui-enabled");

        }

        /**
         * Sets the corresponding styles to column and list view button. Called when changing
         * views from the setView() method.
         */
        function setColumnViewButtonOn()
        {
            $("#perc-finder-choose-view #perc-finder-choose-columnview").addClass("ui-enabled");
            $("#perc-finder-choose-view #perc-finder-choose-listview").removeClass("ui-enabled");
        }

        /**
         * Sets the corresponding styles to column and list view button. Called when changing
         * views from the setView() method.
         */
        function setViewButtonsOff()
        {
            $("#perc-finder-choose-view #perc-finder-choose-listview").removeClass("ui-enabled");
        }

        function setSearchViewButtonsStates()
        {
            $.percFinderButtons().disableAllButtonsButSite();
        }
        /**
         * Calls the views confirm_if_dirty method.
         */
        function confirm_if_dirty(callback, errorCallback, options)
        {
            // use the singleton to display a confirmation dialog if they want to discard changes or not
            dirtyController.confirmIfDirty(callback, errorCallback, options);
        }

        function openAsset(assetId, isEditMode)
        {
            $.PercRecentListService.setRecent($.PercRecentListService.RECENT_TYPE_ITEM,assetId)
                .done(function(){
                    $.perc_utils.info(I18N.message("perc.ui.finder.view@Added Asset") + assetId + I18N.message("perc.ui.finder.view@Recent Item List"));
                })
                .fail(function(message){
                    $.perc_utils.error(message);
                });
            var aName = $.PercNavigationManager.getName();
            var handler = isEditMode ? $.PercAssetController.getAssetEditorForAssetId : $.PercAssetController.getAssetViewForAssetId;
            handler(assetId, function(status, assetEditorUrl)
            {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    $.PercIFrameView.renderAssetEditor(finder, null, assetEditorUrl, null, null, false);
                    addTransitionButtons("percAsset");
                    $("#perc-revisions-button").off("click").perc_button().removeClass("ui-meta-pre-disabled").addClass("ui-meta-pre-enabled").on("click",function()
                    {
                        var isEditMode = $.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT;
                        _openRevisions(assetId, aName, isEditMode);
                    });

                    $("#perc-pubhistory-button").off("click").perc_button().removeClass("ui-meta-pre-disabled").addClass("ui-meta-pre-enabled").on("click",function()
                    {
                        _openPublishingHistory(assetId, aName);
                    });

                    // Add Publishing dropdown
                    //if ($.PercNavigationManager.getMode() == $.PercNavigationManager.MODE_EDIT)
                    //{
                    $.PercItemPublisherService.getPublishActions(assetId, function(status, result)
                    {
                        if (status)
                        {
                            var pubActions = eval("(" + result + ")").PSPublishingActionList;
                            if (pubActions.length > 0)
                            {
                                var actionNames = ["Publishing"];
                                var disableAction = [false];
                                $.each(pubActions, function()
                                {
                                    actionNames.push(this.name);
                                    disableAction.push(this.enabled);

                                });
                                //Add Publishing dropdown menu in toolbar
                                var publishNowDropdown = $("#perc-dropdown-publish-now");
                                publishNowDropdown.PercDropdown({
                                    percDropdownRootClass: "perc-dropdown-publish-now",
                                    percDropdownOptionLabels: actionNames,
                                    percDropdownCallbacks: [function()
                                    {
                                    },
                                        _publishItem, _openSchedule, _publishItem, _publishItem, _publishItem],
                                    percDropdownCallbackData: ["Publishing", {
                                        assetId: assetId,
                                        aName: aName,
                                        trName: I18N.message("perc.ui.finder.view@Publish")
                                    }, {
                                        assetId: assetId,
                                        aName: aName
                                    }, {
                                        assetId: assetId,
                                        aName: aName,
                                        trName: I18N.message("perc.ui.finder.view@Take Down")
                                    }, {
                                        assetId: assetId,
                                        aName: aName,
                                        trName: I18N.message("perc.ui.finder.view@Stage")
                                    }, {
                                        assetId: assetId,
                                        aName: aName,
                                        trName: I18N.message("perc.ui.finder.view@Remove From Staging")
                                    },
                                        assetId],
                                    percDropdownDisabledFlag: disableAction
                                });
                            }
                        }
                    });
                    //}

                }
                else
                {
                    // could not open the asset editor
                    var dlgTitle = I18N.message("perc.ui.publish.title@Error");
                    var dlgContent = assetEditorUrl;
                    if (assetEditorUrl.indexOf("must be checked out by the current user") !== -1)
                    {
                        dlgTitle = I18N.message("perc.ui.webmgt.contentbrowser.warning.title@Open Asset");
                        dlgContent = I18N.message("perc.ui.webmgt.contentbrowser.warning@Asset Overridden", [contentName]);
                    }
                    else if (assetEditorUrl.indexOf("Item not found") !== -1)
                    {
                        dlgTitle = I18N.message("perc.ui.webmgt.contentbrowser.warning.title@Open Asset");
                        dlgContent = I18N.message("perc.ui.webmgt.contentbrowser.warning@Asset Deleted", [contentName]);
                    }
                    $.perc_utils.alert_dialog({
                        title: dlgTitle,
                        content: dlgContent,
                        okCallBack: function()
                        {
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                        }
                    });
                }
            });
        }

        /**
         * Opens the revision dialog for the supplied assetId.
         */
        function _openRevisions(assetId, assetName, isEditMode)
        {
            var mode = isEditMode ? $.PercRevisionDialog.ITEM_MODE_VIEW : $.PercRevisionDialog.ITEM_MODE_EDIT;
            $.PercRevisionDialog.open(assetId, assetName, $.PercRevisionDialog.ITEM_TYPE_ASSET, mode);
        }

        /**
         * Opens the publishing history dialog for the supplied pageId.
         */
        function _openPublishingHistory(assetId, assetName)
        {
            $.PercPublishingHistoryDialog.open(assetId, assetName, $.PercPublishingHistoryDialog.ITEM_TYPE_ASSET);
        }

        //****Workflow related functions these needs to be moved common place as Page and Asset editors share this code.***//
        //**Look for Workflow functions end **/
        /**
         * If newId exists tries to check out the page and if succeeds checks in the current page. If there is a open
         * page or asset, checks it in.
         * navigation managers notify complete method to reload the page otherwise calls with false.
         */
        function checkOutCheckInPage(newId, notificationId, notifyComplete, pathType)
        {
            if (newId != null)
            {
                var type = "percPage";
                if (pathType === $.PercNavigationManager.PATH_TYPE_ASSET)
                    type = "percAsset";
                $.PercWorkflowController().checkOut(type, newId, function(status)
                {
                    if (status)
                    {
                        //We have successfully checked out the new page
                        //Check in the current page if exists
                        if (contentId && (newId !== contentId))
                        {
                            $.PercWorkflowController().checkIn(contentId, function(status)
                            {
                                notifyComplete(notificationId, true);
                            });
                        }
                        else
                        {
                            notifyComplete(notificationId, true);
                        }
                    }
                    else
                    {
                        notifyComplete(notificationId, false);
                    }
                });
            }
            else
            {
                $.PercWorkflowController().checkIn(contentId, function(status)
                {
                    notifyComplete(notificationId, true);
                });
            }
        }

        /**
         * Makes a call to workflow controller to determine if the specified item is
         * checked out to the current user.  Invokes the appropriate callback based
         * on the result.
         *
         * @param contentId the id of the item.
         * @param yesCallback function to perform if the item is checked out to current user.
         * @param noCallback function to perform if the item is not checked out to current user.
         */
        function doIfCheckedOutToCurrentUser(contentId, yesCallback, noCallback)
        {
            $.PercWorkflowController().isCheckedOutToCurrentUser(contentId, function(result)
            {
                if (result)
                {
                    yesCallback();
                }
                else
                {
                    noCallback();
                }
            });
        }

        /**
         * Makes a call to workflow controller to determine if the specified item exists.
         * Invokes the appropriate callback based on the result.
         *
         * @param contentId the id of the item.
         * @param existsCallback function to perform if the item exists.
         * @param doesNotExistCallback function to perform if the item does not exist.
         */
        function doIfItemExists(contentId, existsCallback, doesNotExistCallback)
        {
            $.PercWorkflowController().doesItemExist(contentId, function(result)
            {
                if (result)
                {
                    existsCallback();
                }
                else
                {
                    doesNotExistCallback();
                }
            });
        }

        /**
         * Saves the currently opened object depending on the specified type.
         *
         * @param type the object type (asset, page, template).
         */
        function save(type, callback)
        {
            if (type === "asset")
            {
                var newAsset = true;
                if ($.PercNavigationManager.getId())
                {
                    newAsset = false;
                }
                $.PercIFrameView.saveContent(newAsset);
                callback();
            }
            else if (type === "page" || type === "template")
            {
                if (typeof layoutModel !== 'undefined' && layoutModel != null)
                {
                    $.PercBlockUI();
                    layoutModel.save(function()
                    {
                        $.unblockUI();
                        callback();
                    });
                }
            }
        }

        /**
         * Makes a call to workflow controller and gets the transtions and renders them as buttons.
         * Adds the click events to workflow controller's transtion method after transition is done, reloads the page
         * by calling navigation manager go to method.
         * Adds save and close buttons for assets and just close button for page. The close button closes the page/asset
         * and uses the navigation manager to switch to the dashboard.
         */
        function addTransitionButtons(itemType)
        {
            //Inner function to add the save and close buttons inside the workflow transition callback function as we
            //want these to be added as the last buttons. If the contentid is null then just adds the save and close
            //buttons.
            function addSaveAndCloseButtons()
            {
                //Add save button if it is asset view
                if ($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT &&
                    view === $.PercNavigationManager.VIEW_EDIT_ASSET)
                {
                    var saveButton = '<button style="float: right;" name="perc_wizard_save" title="Save" class="btn btn-primary" id="perc-save-content">Save</button>';
                    $("#perc-content-menu").append($(saveButton));
                    $("#perc-save-content").on("click",function()
                    {
                        var newAsset = !contentId || contentId == null;
                        if (!newAsset)
                        {
                            doIfItemExists(contentId, function()
                            {
                                doIfCheckedOutToCurrentUser(contentId, function()
                                {
                                    $.PercIFrameView.saveContent(newAsset);
                                }, function()
                                {
                                    //an Admin has overridden the current editor in another session
                                    $.perc_utils.alert_dialog({
                                        title: I18N.message("perc.ui.common.label@Save"),
                                        content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Overridden", ["asset"]),
                                        okCallBack: function()
                                        {
                                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                        }
                                    });
                                });
                            }, function()
                            {
                                $.perc_utils.alert_dialog({
                                    title: I18N.message("perc.ui.common.label@Save"),
                                    content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Deleted", ["asset"]),
                                    okCallBack: function()
                                    {
                                        $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                    }
                                });
                            });
                        }
                        else
                        {
                            $.PercIFrameView.saveContent(newAsset);
                        }
                    });
                }
                //Add edit button if this is readonly mode
                if ($.PercNavigationManager.getMode() !== $.PercNavigationManager.MODE_EDIT)
                {
                    var editButton = '<button name="perc_page_edit" class="btn btn-primary" id="perc-page-edit">' +I18N.message("perc.ui.finder.view@Edit") + '</button>';
                    $("#perc-content-menu").append($(editButton));
                    currentContentPath = $.PercNavigationManager.getPath();
                    $.PercPathService.getPathItemForPath(currentContentPath, function(message, item)
                    {
                        var currentItem = item.PathItem;
                        $("#perc-page-edit").data("currentItem", currentItem).on("click",function()
                        {
                            var item = $(this).data("currentItem");
                            if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR)
                            {
                                $.PercNavigationManager.handleOpenPage(item, true);
                            }
                            else if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDIT_ASSET)
                            {
                                //We are in AssetEditor but finder is having site selected, so need to find Asset
                                if(item.name === "Sites"){
                                    $.PercPathService.getPathItemById(contentId, function(status, data){
                                        if(status === $.PercServiceUtils.STATUS_SUCCESS) {
                                            $.PercNavigationManager.handleOpenAsset(data.PathItem, true);
                                        } else {
                                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});

                                        }
                                    });
                                }else{
                                    //CMS-8107 : item.name always returned the asset name.
                                    $.PercNavigationManager.handleOpenAsset(item, true);
                                }
                            }
                            else
                            {
                                // This should never happen.
                                var eMsg = I18N.message("perc.ui.finder.view@Cannot Open Unknown View");
                                $.perc_utils.alert_dialog({
                                    title: I18N.message("perc.ui.publish.title@Error"),
                                    content: eMsg
                                });
                            }
                        });
                    });

                }
                //Add close button, this will close the editor and switch to the dashboard
                var closeButton = "<button class='btn btn-primary' id='perc-page-close'>" +I18N.message("perc.ui.change.pw@Close") + "</button>";
                $("#perc-content-menu").append($(closeButton));
                $("#perc-page-close").on("click",function()
                {
                    if (contentId)
                    {
                        doIfItemExists(contentId, function()
                        {
                            doIfCheckedOutToCurrentUser(contentId, function()
                            {
                                confirm_if_dirty(function()
                                {
                                    $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                                    if ($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT)
                                    {
                                        $.PercWorkflowController().checkIn(contentId, function(status)
                                        {
                                            contentId = null;
                                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                            $.unblockUI();
                                        });
                                    }
                                    else
                                    {
                                        contentId = null;
                                        $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                        $.unblockUI();
                                    }
                                });
                            }, function()
                            {
                                //just close the content browser, an Admin has overridden the current editor in another session
                                $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                            });
                        }, function()
                        {
                            //just close the content browser, the item has been deleted in another session
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                        });
                    }
                });
            }
            /**
             * Adds a comment icon to the menu bar if comment exists.
             */
            function addCommentsIcon(){
                $.PercRevisionService.getLastComment(contentId, function(status, result){
                    if(status === $.PercServiceUtils.STATUS_SUCCESS){
                        if(result.data && result.data.length > 0){
                            var commentIcon = $("<a style='float: right;' tooltip='" + result.data + "' class='perc-last-comment-menubar'><span class='perc-font-icon icon-comment'/></a>");
                            commentIcon.tooltip({
                                delay: 500,
                                left:-150,
                                top:25,
                                bodyHandler: function(){
                                    return "<p style='padding:5px 20px 5px 20px;'>" + result.data + "</p>";
                                }
                            });
                            $("#tooltip").css("margin-right","20px");
                            $("#perc-content-menu").append(commentIcon);
                        }
                        else{
                            $.perc_utils.info(I18N.message("perc.ui.finder.view@No Comment"));
                        }
                    }
                    else{
                        $.perc_utils.info(I18N.message("perc.ui.finder.view@Failed To Get Comment Info") + contentId + I18N.message("perc.ui.finder.view@See Server Log"));
                    }
                });
            }

            //get the transition actions and add them.
            if ($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT &&
                contentId)
            {
                $.PercWorkflowController().getTransitions(contentId, function(status, results)
                {
                    if (status)
                    {
                        var dropdownLabels = [];
                        var dropdownParams = [];
                        var dropdownActions = [];
                        var dropdownButtonImage = "";
                        var dropdownButtonImageOver = "";
                        results.unshift(results[0]);

                        $.each(results, function(index)
                        {

                            var trName = results[index].name;
                            var trClass = results[index].cssClass;
                            var trAlt = results[index].alt;
                            trClass += " perc-wf-button";
                            var trNameNormal = trName.toLowerCase().replace(/[^a-zA-Z0-9\/]/g, '_');
                            var trId = "perc_item_transition_" + trNameNormal;
                            var baseImageName = "/cm/images/images/splitButtonWf" + trNameNormal;
                            var imageExt = ".gif";
                            var regImageFilename = baseImageName + imageExt;
                            var overImageFilename = baseImageName + "Over" + imageExt;

                            if (index === 0)
                            {
                                defaultButtonImage = regImageFilename;
                                defaultButtonImageOver = overImageFilename;
                                dropdownButtonImage = '/cm/images/images/splitButtonArrow.gif';
                                dropdownButtonImageOver = '/cm/images/images/splitButtonArrowOn.gif';

                            }
                            var param = {
                                name: trName,
                                contentId: contentId,
                                itemType: itemType
                            };
                            dropdownParams.push(param);
                            dropdownLabels.push(trName);
                            dropdownActions.push(handlePageWorkflowDropdownAction);
                        });

                        // Add workflow dropdown
                        var pageWorkflowDropdown = $("#perc-dropdown-page-workflow");
                        pageWorkflowDropdown.append($('<button />').html(I18N.message("perc.ui.edit.workflow.step.dialog@" + $.perc_textFilters.IDNAMECDATA(dropdownLabels[0]))).css('display', 'inline-block').addClass('btn btn-primary perc-workflow-split-button-left perc-workflow-split-button-' + $.perc_textFilters.IDNAMECDATA(dropdownLabels[0]))).append($('<div />').addClass('perc-workflow').css('display', 'inline-block'));
                        pageWorkflowDropdown.children('div').eq(0).PercDropdown({
                            percDropdownRootClass: "perc-workflow",
                            percDropdownOptionLabels: dropdownLabels,
                            percDropdownCallbacks: dropdownActions,
                            percDropdownCallbackData: dropdownParams,
                            percDropdownTitleImage: dropdownButtonImage,
                            percDropdownTitleImageOver: dropdownButtonImageOver,
                            percDropdownShowExpandIcon: false,
                            percDropdownResizeToElement: "#perc-dropdown-page-workflow"
                        });
                        pageWorkflowDropdown.find('.perc-dropdown-title').off('click');
                        pageWorkflowDropdown.children('a, button').on("click",function()
                        {
                            dropdownActions[0](dropdownParams[0]);
                        });
                        addSaveAndCloseButtons();
                    }
                });
            }
            else
            {
                addSaveAndCloseButtons();
                addCommentsIcon();
            }
        }
        /*** Workflow functions end **/
        /* ===========================
         * Create and configure Finder
         * ===========================
         */
        // get finder reference
        //finder = $.perc_finder();

        finder.addPathChangedListener(function(p)
        {
            //If the path change, return to the default view (column view)
            if (currentFinderView === PERC_FINDER_VIEW_COLUMN)
                return;
            if (finder.flagChangeView)
                setView(PERC_FINDER_VIEW_COLUMN);
        });

        $.PercNavigationManager.registerFinder(finder);
        $.PercNavigationManager.addLocationChangeListener(function(url, id, notifyComplete, params)
        {
            // Note: notifyComplete MUST be called by the listener so that the Navigation
            // Manager knows that local processing is done by the listener and knows
            // if we should continue and actually do the location change.

            var newId = params.id;
            var modeSwitch = $.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_READONLY &&
                params.mode === $.PercNavigationManager.MODE_EDIT;
            //Alert the user if he tries to open the same page/asset.
            if (!modeSwitch && !$.PercNavigationManager.isReopenAllowed() && contentId && contentId === newId)
            {
                var options = {
                    title: I18N.message("perc.ui.finder.view@Open") + type,
                    content: I18N.message("perc.ui.finder.view@The") + type + " '" + $.PercNavigationManager.getName() + I18N.message("perc.ui.finder.view@Already Open")
                };
                $.perc_utils.alert_dialog(options);
                notifyComplete(id, false);
                return;
            }

            //Check out the new page before opening it.


            // get dirty state from the singleton where the page, template, and/or asset have updated the status
            // if they have become dirty
            dirty = dirtyController.isDirty();
            if (dirty)
            {
                // confirm
                confirm_if_dirty(function()
                {
                    if (params.mode === $.PercNavigationManager.MODE_EDIT && contentId != null)
                    {
                        checkOutCheckInPage(newId, id, notifyComplete, params.pathType);
                    }
                    else
                    {
                        // Nothing to checkout before checkin
                        checkOutCheckInPage(null, id, notifyComplete, params.pathType);
                        notifyComplete(id, true);
                    }

                });
            }
            else
            {
                if (params.mode === $.PercNavigationManager.MODE_EDIT && contentId != null)
                {
                    checkOutCheckInPage(newId, id, notifyComplete, params.pathType);
                }
                else
                {
                    // Nothing to checkout before checkin
                    checkOutCheckInPage(null, id, notifyComplete, params.pathType);
                    notifyComplete(id, true);
                }
            }
        });
        $(".perc-finder").append(finder.elem);

        // resize the width/height of the finder implemented in perc_finder.js
        finder.on('resize', function (event, ui) {
            //Refresh the arch view
            if ($("#perc_site_map").length > 0) {
                $("#perc_site_map").perc_site_map('layoutAll');
            }
        });

        // initialize the finder height
        finder.update_finder_height();

        finderButtons = $.percFinderButtons().createButtons(finder, percFinderViewAPI);
        // Is there a page specified to load
        var contentId = $.PercNavigationManager.getId();
        var pageMode = $.PercNavigationManager.getMode();
        var contentName = $.PercNavigationManager.getName();
        var type = $.PercNavigationManager.getPathType();
        var view = $.PercNavigationManager.getView();

        function checkForMigrationWarnings(contentId)
        {
            /** callback handler invoked iff contentId has empty migrations */
            function onPageHasEmptyMigrationWidgets()
            {
                // show modal and clear flag
                $.perc_utils.alert_dialog({

                    title: I18N.message(I18N.message("perc.ui.page.general@Warning")),
                    content: I18N.message("perc.ui.finder.view@Content Migration Failure"),
                    okCallBack: function()
                    {
                        $.PercPageService.clearFlagShowMigrationEmptyMessage(contentId);
                    }
                });
            }
            $.PercPageService.checkForEmptyMigrationWidgets(contentId, onPageHasEmptyMigrationWidgets);
        }
        if (type === $.PercNavigationManager.PATH_TYPE_PAGE && contentId && contentName)
        {
            //Make sure to check out the page, if not able to check out do not proceed further
            if (pageMode === $.PercNavigationManager.MODE_EDIT)
            {
                $.PercWorkflowController().checkOut("percPage", contentId, function(status)
                {
                    if (!status)
                    {
                        contentId = null;
                        contentName = null;
                        var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
                        if(frwrapper != null)
                            frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");
                        var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');
                        if(tbwrapper != null)
                            tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");

                    }
                    else
                    {
                        checkForMigrationWarnings(contentId);
                        pageView.openPage(contentId, contentName);
                        addTransitionButtons("percPage");
                    }
                });
            }
            else
            {
                pageView.openPage(contentId, contentName);
                addTransitionButtons("percPage");
            }
        }
        else if (view === $.PercNavigationManager.VIEW_EDIT_ASSET && contentId)
        {
            var assetId = contentId;
            // set the name of the asset label
            var path = $.PercNavigationManager.getPath();
            //$("#perc-pageEditor-menu-name").html(assetName);
            $("#perc-page-button").html('Asset:').append("<span id='perc-pageEditor-menu-name' title = " + contentName + "> " + contentName + "</span>");
            // render asset editor
            if (pageMode === $.PercNavigationManager.MODE_EDIT)
            {
                //Make sure to check out the asset before opening it.
                $.PercWorkflowController().checkOut("percAsset", assetId, function(status)
                {
                    if (status)//Workflow controller presents the appropriate error message to the user if fails to check out.
                    {
                        openAsset(assetId, true);
                    }
                });
            }
            else
            {
                openAsset(assetId, false);
            }

        }
        else if (view === $.PercNavigationManager.VIEW_EDIT_ASSET && !contentId)
        {
            var memento = $.PercNavigationManager.getMemento();
            if (memento.widgetId) {
                $.PercNewAssetDialog.openViewer(memento.folderPath, memento.widgetId);
            }
            else{
                $.PercNewAssetDialog.open();
            }
        }
        else if(!contentId)
        {
            var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');
            if(frwrapper != null)
                frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");
            var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');
            if(tbwrapper != null)
                tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");
        }
        //Snippet for displaying inline help when content area is empty. Story 99.


        if (contentName != null)
        {
            $("#perc-editor-inline-help").hide();
        }

        function handlePageWorkflowDropdownAction(params)
        {

            var contentId = params.contentId;
            var itemType = params.itemType;
            var trName = params.name;

            confirm_if_dirty(function()
            {
                var type = view === $.PercNavigationManager.VIEW_EDIT_ASSET ? "asset" : "page";
                doIfItemExists(params.contentId, function()
                {
                    doIfCheckedOutToCurrentUser(contentId, function()
                    {

                        checkIfLinkedPage(contentId,itemType,type,trName);



                    }, function()
                    {
                        //an Admin has overridden the current editor in another session
                        $.perc_utils.alert_dialog({
                            title: trName,
                            content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Overridden", [type]),
                            okCallBack: function()
                            {
                                $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                            }
                        });
                    });
                }, function()
                {
                    $.perc_utils.alert_dialog({
                        title: trName,
                        content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Overridden", [type]),
                        okCallBack: function()
                        {
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                        }
                    });
                });
            });
        }

        function showCommentsDialog(pageId,itemType,trName){
            var buttons = {};
            buttons[trName] = {
                click: function()
                {
                    userComment = $("#perc-workflow-comment").val();
                    commentDialog.remove();
                    //html encode the string - see cms-3609
                    userComment = $('<div/>').text(userComment).html();
                    userComment = encodeURIComponent(userComment);

                    commentDialog.remove();

                    doTransition(contentId,itemType,trName,userComment);

                },
                id: "perc-workflow-comment-ok"
            };
            buttons.Cancel = {
                click: function()
                {

                    commentDialog.remove();
                },
                id: "perc-workflow-comment-cancel"
            };
            var userComment = "";
            var commentDialog = $("<div><div class='perc-workflow-comment-label' data='" + trName+ "'>" +I18N.message("perc.ui.finder.view@Enter Comments Limit") + "</div><textarea id=\"perc-workflow-comment\" name=\"perc-workflow-comment\" maxlength=\"500\"></textarea></div>")
                .perc_dialog(
                    {
                        dialogClass: 'perc-workflow-comment-dialog',
                        title: I18N.message("perc.ui.finder.view@Enter Comments"),
                        modal: true,
                        resizable: false,
                        "percButtons" : buttons,
                        width:400,
                        id: "perc-workflow-comment-dialog"
                    });
        }

        function doTransition(contentId,itemType,trName,userComment){



            $.PercBlockUI();
            $.PercWorkflowController().transition(contentId, itemType, trName, userComment, function(status)
            {
                if (status)
                {
                    contentId = null;
                    $.unblockUI();
                    if(type === "page" && trName === "Archive"){
                        $.PercRedirectHandler.createRedirect($.PercNavigationManager.getPath(), "", "page")
                            .fail(function(errMsg){
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.finder.view@Redirect Creation Error"), content: errMsg, okCallBack: function(){
                                        $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                    }});
                            })
                            .done(function(){
                                $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                            });
                    }
                    else{
                        $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                    }

                }



            });
            $.unblockUI();

        }

        function checkIfLinkedPage(pageId,itemType,type,trName) {
            if(type === "page" && trName === "Archive" ){


                var findLinkedItemsUrl = $.perc_paths.ITEM_LINKED_TO_ITEM + "/" + pageId;
                var takeDownUrl =  $.perc_paths.PAGE_TAKEDOWN ;
                takeDownUrl+="/" + pageId;

                $.PercServiceUtils.makeJsonRequest(findLinkedItemsUrl, $.PercServiceUtils.TYPE_GET, false, function(status, result) {
                    if (status === $.PercServiceUtils.STATUS_ERROR) {
                        var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result);
                        console.error(defaultMsg);
                        showCommentsDialog(pageId,itemType,trName);

                    }
                    else {
                        if (result.data != null && result.data.ArrayList != null && result.data.ArrayList.length > 0) {
                            relatedLinkArchiveConfirm(result.data,pageId,itemType,trName);
                        }else {
                            showCommentsDialog(pageId,itemType,trName);
                        }

                    }
                }, null);
            }else{
                showCommentsDialog(pageId,itemType,trName);
            }

        }

        function relatedLinkArchiveConfirm(data,pageId,itemType,trName)
        {
            var title = I18N.message("perc.ui.publish.title@Remove From Site");
            var options = {
                title: title,
                question: createDialogQuestion(data),
                cancel: function()
                {

                },
                success: function()
                {
                    showCommentsDialog(pageId,itemType,trName);
                }
            };
            $.perc_utils.confirm_dialog(options);
        }

        function createDialogQuestion(data) {
            var dialog = I18N.message("perc.ui.publish.question@Remove From Site") + '<br /><br />';
            $.each(data.ArrayList, function (index, value) {
                if (index > 9) {
                    return false;
                }
                dialog += value.pagePath + '<br />';
            });
            return dialog;
        }


        /**
         * Schedule the item(page/asset) for the supplied pageId/assetId.
         */
        function _openSchedule(callbackData)
        {
            var itemId = callbackData.assetId;
            var assetName = callbackData.aName;
            if (dialogFlag)
            {
                $.PercScheduleDialog.open(itemId, assetName);
                $(".ui-datepicker-trigger").trigger("click");
                $("#ui-datepicker-div").css('z-index', 9501).css('display', 'none');
                $("#ui-timepicker-div").css('z-index', 9501).css('display', 'none');
                $("#perc-schedule-dialog-cancel").trigger("click");
                $.PercScheduleDialog.open(itemId, assetName);
                dialogFlag = false;
            }
            else
            {
                $("#ui-datepicker-div").css('z-index', 100000);
                $("#ui-timepicker-div").css('z-index', 100000);
                $.PercScheduleDialog.open(itemId, assetName);
            }
        }
        /**
         * Check if Publish date is set for item before doing immediate publishing.
         */
        function _confirmPublish(scheduleDates)
        {
            var startDate = scheduleDates.startDate;
            var itemType = view === $.PercNavigationManager.VIEW_EDIT_ASSET ? "Asset" : "Page";
            var itemId = scheduleDates.itemId;
            if (startDate !== "")
            {
                var settings = {
                    id: "perc-confirm-publish-dialog",
                    title: I18N.message("perc.ui.page.general@Warning"),
                    question: I18N.message("perc.ui.finder.view@Item Scheduled Published") + startDate + I18N.message("perc.ui.finder.view@Continue To Publish"),
                    success: function()
                    {
                        $.PercBlockUI();
                        $.PercItemPublisherService.publishItem(itemId, itemType, _afterPublish);
                    },
                    cancel: function()
                    {
                    },
                    yes: I18N.message("perc.ui.finder.view@Continue Anyway")
                };
                utils.confirm_dialog(settings);
            }
            else
            {
                $.PercBlockUI();
                $.PercItemPublisherService.publishItem(itemId, itemType, _afterPublish);
            }
        }
        /**
         * Publish/Take Down the item(page/asset) for the supplied pageId/assetId.
         */
        function _publishItem(callbackData)
        {
            var itemId = callbackData.assetId;
            var trName = callbackData.trName;
            var view = $.PercNavigationManager.getView();
            var itemType = view === $.PercNavigationManager.VIEW_EDIT_ASSET ? "Asset" : "Page";
            confirm_if_dirty(function()
            {
                doIfItemExists(itemId, function()
                {
                    /*doIfCheckedOutToCurrentUser(itemId, function()
                    {*/
                    if (trName === I18N.message("perc.ui.navMenu.publish@Publish"))
                    {
                        $.PercItemPublisherService.getScheduleDates(itemId, function(status, result)
                        {
                            if (status)
                            {
                                var scheduleDates = eval("(" + result + ")").ItemDates;
                                _confirmPublish(scheduleDates);
                            }
                            else
                            {
                                $.perc_utils.alert_dialog({
                                    content: I18N.message("perc.ui.finder.view@Get Saved Schedule"),
                                    title: I18N.message("perc.ui.publish.title@Error")
                                });
                                return false;
                            }

                        });
                    }
                    else if (trName === I18N.message("perc.ui.page.menu@Take Down"))
                    {
                        $.PercBlockUI();
                        $.PercItemPublisherService.takeDownItem(itemId, itemType, _afterPublish);
                    }
                    else if(trName === I18N.message("perc.ui.page.menu@Stage"))
                    {
                        $.PercBlockUI();
                        $.PercItemPublisherService.publishToStaging(itemId, itemType, _afterPublish);
                    }
                    else if(trName === I18N.message("perc.ui.page.menu@Remove from Staging"))
                    {
                        $.PercBlockUI();
                        $.PercItemPublisherService.removeFromStaging(itemId, itemType, _afterPublish);
                    }

                    /*}, function()
                    {
                        //an Admin has overridden the current editor in another session
                        $.perc_utils.alert_dialog({
                            title: trName,
                            content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Overridden", [itemType]),
                            okCallBack: function()
                            {
                                $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                            }
                        });
                    });*/
                }, function()
                {
                    $.perc_utils.alert_dialog({
                        title: trName,
                        content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Deleted", [itemType]),
                        okCallBack: function()
                        {
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                        }
                    });
                });
            });

        }

        function _afterPublish(success, results)
        {
            $.unblockUI();
            if (!success)
            {
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results[0]);
                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.publish.title@Error"),
                    content: defMsg
                });
            }
            else
            {
                var SitePublishResponse = results[0].SitePublishResponse;
                if (SitePublishResponse.status === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_FORBIDDEN)
                {
                    $.perc_utils.alert_dialog({
                        title: I18N.message("perc.ui.finder.view@Server Publish"),
                        content: I18N.message("perc.ui.publish.errordialog.message@Publish Not Allowed")
                    });
                }
                else if (SitePublishResponse.status === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_BADCONFIG_MULTIPLE_SITES)
                {
                    $.perc_utils.alert_dialog({
                        title: I18N.message("perc.ui.page.general@Warning"),
                        content: I18N.message("perc.ui.publish.errordialog.message@Bad configuration multiple sites", [SitePublishResponse.warningMessage])
                    });
                }
                else if ( SitePublishResponse.status === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_NOSTAGING_SERVERS)
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog(
                        {
                            title: I18N.message("perc.ui.finder.view@Server Publish"),
                            content: I18N.message("perc.ui.finder.view@No Staging Servers Available")
                        });
                }
                else if (typeof(SitePublishResponse.warningMessage) != "undefined" && SitePublishResponse.warningMessage !== "")
                {
                    $.perc_utils.alert_dialog({
                        title: I18N.message("perc.ui.page.general@Warning"),
                        content: SitePublishResponse.warningMessage,
                        okCallBack: function()
                        {
                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                        }
                    });
                }
                else
                {
                    $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                }
            }
        }
    };

})(jQuery, jQuery.Percussion);
