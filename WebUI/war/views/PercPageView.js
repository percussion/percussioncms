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
 * PercPageView.js
 *
 * Handles user interaction with Edit page.
 *
 * (*) Iframe
 * (*) Handles dirty page and confirmation when changing tabs
 * (*) Loads content and layout tabs
 *
 */
(function($, P)
{
    var CONTENT_TAB = 0;
    var LAYOUT_TAB = 1;
    var STYLES_TAB = 2;
    var view = $.PercNavigationManager.getView();
    var dialogFlag = true;
    var querystring = $.deparam.querystring();
    var pageMode = $.PercNavigationManager.getMode();
    var isAdmin = $.PercNavigationManager.isAdmin();
    var isDesigner = $.PercNavigationManager.isDesigner();

    P.pageView = function()
    {
        $(".perc-page-status").hide();
        $(".perc-page-name").hide();

        // singleton to keep track of dirty state across various types of resources such as pages, templates and assets
        var dirtyController = $.PercDirtyController;

        if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR)
        {
            $.perc_iframe_fix($('#frame'));
        }

        var utils = $.perc_utils;
        var pageModel;
        var currentPageId; // current page id being edited
        var unassigned = "UNASSIGNED"; // UNASSIGNED template type
        var dirty = false; // dirty page
        // Interface to local API to pass around to Finder and Page Edit Dialog
        // so they can call back and update the Content tab
        var pageViewAPI = {
            resetPageName: resetPageName,
            reload: function()
            {
                currentTabIndex = $('#perc-pageEditor-tabs').tabs('option','active');
                if(typeof currentTabIndex === 'undefined'){
                    currentTabIndex = CONTENT_TAB;
                }
                loadTab(currentTabIndex,true);
            },
            getPageId: function()
            {
                return currentPageId;
            },
            clear: function()
            {
                $('#frame').each(function()
                {
                    this.open();
                    this.close();
                });
            },
            confirm_if_dirty: confirm_if_dirty,
            openPage: openPage,
            isDirty: isDirty
        };

        // hide region tool for pages
        $("#region-tool, #region-tool-help, #perc-region-tool-inspector, #perc-region-tool-menu, #perc-error-alert").css("visibility", "hidden");

        function openPage(pageId, pageName)
        {
            try {
                $.PercRecentListService.setRecent($.PercRecentListService.RECENT_TYPE_ITEM, pageId).done(function(){
                    $.perc_utils.info(I18N.message("perc.ui.page.recent@Added Recent Page", pageId));
                }).fail(function(message){
                    $.perc_utils.error(message);
                });
            }
            catch(err){
                $.perc_utils.error(I18N.message("perc.ui.page.recent@Error Adding Recent Page",pageId));
            }
            currentPageId = pageId;
            $(".perc-page-status").show();
            $(".perc-page-name").show();
            getPageStatus(currentPageId);
            var pglabel = $.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT?I18N.message('perc.ui.page.label@Editing Page'):I18N.message('perc.ui.page.label@Viewing Page');
            var pgType = $.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT?"page-editing":"page-viewing";
            if (pageName)
            {
                var titleValue = $.PercNavigationManager.getPath();
                $(".perc-page-name-name").html('<span class = "perc-title-value"><span class = "perc-page-name-text">' + pageName + '</span></span>');
                $(".perc-title-value").attr("title", titleValue);
                $(".perc-title-value").prepend('<span class = "perc-page-name-label">' + pglabel + '</span>');
                $(".perc-page-details").attr("type",pgType);
                $("#perc-pageEditor-menu-name").html(pageName);
                _addMyPagesAction(pageId);
                $(".perc-page-details").show();
            }

            /* If the default tab happens to already be selected, the content will not be loaded, so we do that below. */
            var $tabs = $("#perc-pageEditor-tabs");
            var currentTabIndex = $tabs.tabs('option','active');
            var defaultTabIndex = 0;
            /* which tab to show when a page is first opened, 0 is content tab, 1 is layout tab */
            if(currentTabIndex === null || typeof currentTabIndex === 'undefined'){
                currentTabIndex = defaultTabIndex;
                $tabs.tabs().trigger("activate",currentTabIndex);
            }

            if ($.PercNavigationManager.getMode() == $.PercNavigationManager.MODE_EDIT)
            {
                pageModel = P.pageModel($.perc_pagemanager, $.perc_templatemanager, pageId, function()
                {
                    // enable all tabs and select the first tab
                    $tabs.tabs(  "option", "disabled", [] ).trigger('activate', defaultTabIndex);
                    $("#perc-pageEditor-tabs").find("li").each(function(i)
                    {
                        // Don't enable Layout and Style tabs if template type is UNASSIGNED
                        if (pageModel.getTemplateModel().getTemplateType() !== unassigned)
                        {
                            $(this).removeClass('ui-state-disabled');
                        }
                    });

                    // Don't enable Edit Template action if template type is UNASSIGNED
                    var enableMenu = pageModel.getTemplateModel().getTemplateType() !== unassigned;
                    if (isAdmin || isDesigner)
                    {
                        actionsDropdown.PercDropdown(
                            {
                                percDropdownRootClass: "perc-dropdown-page-actions",
                                percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Edit Metadata"), I18N.message("perc.ui.page.menu@Edit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                                percDropdownCallbacks: [function()
                                {}, function()
                                {
                                    $.perc_page_edit_dialog($.perc_finder(), pageViewAPI, currentPageId);
                                },
                                    _loadTemplate, _changeTemplate],
                                percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Edit Metadata"), I18N.message("perc.ui.page.menu@Edit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                                percDropdownDisabledFlag: [false, true, enableMenu, true]
                            });
                    }
                    else
                    {
                        actionsDropdown.PercDropdown(
                            {
                                percDropdownRootClass: "perc-dropdown-page-actions",
                                percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Edit Metadata"), I18N.message("perc.ui.page.menu@Change Template")],
                                percDropdownCallbacks: [function()
                                {}, function()
                                {
                                    $.perc_page_edit_dialog($.perc_finder(), pageViewAPI, currentPageId);
                                },
                                    _changeTemplate],
                                percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Edit Metadata"), I18N.message("perc.ui.page.menu@Change Template")],
                                percDropdownDisabledFlag: [false, true, true]
                            });
                    }

                    addMoreMenus();
                });
            }

            // Enable the Metadata
            $("#perc-metadata-button").off("click").perc_button().removeClass("ui-meta-pre-disabled").addClass("ui-meta-pre-enabled").on("click",function()
            {
                $.perc_page_edit_dialog($.perc_finder(), pageViewAPI, currentPageId);
            });

            // Add view dropdown on content tab
            var actionsDropdown = $("#perc-dropdown-actions");
            var viewDropdown = $("#perc-dropdown-view");


            if ($.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_READONLY)
            {
                //Action drop down menu in page readonly mode
                actionsDropdown.PercDropdown(
                    {
                        percDropdownRootClass: "perc-dropdown-page-actions",
                        percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@View Metadata")],
                        percDropdownCallbacks: [function()
                        {}, function()
                        {
                            $.perc_page_edit_dialog($.perc_finder(), pageViewAPI, currentPageId);
                        }],
                        percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@View Metadata")],
                        percDropdownDisabledFlag: [false, true]
                    });

                addMoreMenus();
            }

            /**
             * Sets the appropriate class to my pages, to indicate whether the page has already been added to the user pages or not,
             * Adds either Add to my pages click event or remove from my pages click event.
             * @param {Object} pageId assumed to be the currently opened page id.
             */
            function _addMyPagesAction(pageId)
            {
                $.PercPageService.isMyPage(pageId, function(status, result){
                    if(status === $.PercServiceUtils.STATUS_ERROR)
                    {
                        $(".perc-my-pages-action").addClass("perc-my-pages-error").attr("title", I18N.message("perc.ui.page.mypages@Error retrieving status"));
                    }
                    else
                    {
                        if(result)
                        {
                            $(".perc-my-pages-action").addClass("perc-remove-from-my-pages").attr("title", I18N.message("perc.ui.page.mypages@Remove from My Pages")).on("click",function(){_removeFromMyPages(pageId);});

                        }
                        else
                        {
                            $(".perc-my-pages-action").addClass("perc-add-to-my-pages").attr("title", I18N.message("perc.ui.page.mypages@Add to My Pages")).on("click",function(){_addToMyPages(pageId);});
                        }
                    }
                });
            }

            /**
             * Makes a call to the service to add to my pages and if successful toggles the class and click event to remove pages.
             * @param {Object} pageId assumed to be the currently opened page id.
             */
            function _addToMyPages(pageId)
            {
                $.PercPageService.addToMyPages(pageId, function(status, result){
                    if(status == $.PercServiceUtils.STATUS_ERROR)
                    {
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"),content: result});
                    }
                    else
                    {
                        $(".perc-my-pages-action").removeClass("perc-add-to-my-pages").addClass("perc-remove-from-my-pages").attr("title", I18N.message("perc.ui.page.mypages@Remove from My Pages")).off("click").on("click",function(){_removeFromMyPages(pageId);});
                    }
                });
            }

            /**
             * Makes a call to the service to remove from my pages and if successful toggles the class and click event to add pages.
             * @param {Object} pageId assumed to be the currently opened page id.
             */
            function _removeFromMyPages(pageId)
            {
                $.PercPageService.removeFromMyPages(pageId, function(status, result){
                    if(status === $.PercServiceUtils.STATUS_ERROR)
                    {
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"),content: result});
                    }
                    else
                    {
                        $(".perc-my-pages-action").removeClass("perc-remove-from-my-pages").addClass("perc-add-to-my-pages").attr("title", I18N.message("perc.ui.page.mypages@Add to My Pages")).off("click").on("click",function(){_addToMyPages(pageId);});
                    }
                });
            }

            function addMoreMenus()
            {
                if (isAdmin || isDesigner)
                {
                    // Add action dropdown on layout tab
                    var layoutActionsDropdown = $("#perc-dropdown-actions-layout");
                    layoutActionsDropdown.PercDropdown(
                        {
                            percDropdownRootClass: "perc-dropdown-actions-layout",
                            percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Edit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownCallbacks: [function()
                            {},
                                _loadTemplate, _changeTemplate],
                            percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Edit TemplateEdit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownDisabledFlag: [false, true, true]
                        });

                    // Add action dropdown on style tab
                    var styleActionsDropdown = $("#perc-dropdown-actions-style");
                    styleActionsDropdown.PercDropdown(
                        {
                            percDropdownRootClass: "perc-dropdown-actions-style",
                            percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Edit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownCallbacks: [function()
                            {},
                                _loadTemplate, _changeTemplate],
                            percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Edit Template"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownDisabledFlag: [false, true, true]
                        });
                }
                else
                {
                    // Add action dropdown on layout tab
                    let layoutActionsDropdown = $("#perc-dropdown-actions-layout");
                    layoutActionsDropdown.PercDropdown(
                        {
                            percDropdownRootClass: "perc-dropdown-layout-actions",
                            percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownCallbacks: [function()
                            {},
                                _changeTemplate],
                            percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownDisabledFlag: [false, true]
                        });

                    // Add action dropdown on style tab
                    let styleActionsDropdown = $("#perc-dropdown-actions-style");
                    styleActionsDropdown.PercDropdown(
                        {
                            percDropdownRootClass: "perc-dropdown-actions-style",
                            percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@Actions"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownCallbacks: [function()
                            {},
                                _changeTemplate],
                            percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Action"), I18N.message("perc.ui.page.menu@Change Template")],
                            percDropdownDisabledFlag: [false, true]
                        });
                }

                // Add view dropdown on layout tab
                var layoutViewDropdown = $("#perc-dropdown-view-layout");
                layoutViewDropdown.PercDropdown(
                    {
                        percDropdownRootClass: "perc-dropdown-view-layout",
                        percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@View"), I18N.message("perc.ui.menu@JavaScript Off"), I18N.message("perc.ui.page.menu@Hide Guides")],
                        percDropdownCallbacks: [function()
                        {}, function()
                        {}, function()
                        {}],
                        percDropdownCallbackData: [I18N.message("perc.ui.page.menu@View"), I18N.message("perc.ui.page.menu@View"), I18N.message("perc.ui.page.menu@View")],
                        percDropdownDisabledFlag: [false, true, true]
                    });
                //Add View dropdown under Style tab
                var styleViewDropdown = $("#perc-dropdown-view-style");
                styleViewDropdown.PercDropdown(
                    {
                        percDropdownRootClass: "perc-dropdown-view-style",
                        percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@View"), I18N.message("perc.ui.menu@JavaScript Off")],
                        percDropdownCallbacks: [function()
                        {}, function()
                        {}],
                        percDropdownCallbackData: [I18N.message("perc.ui.page.menu@View"), I18N.message("perc.ui.menu@JavaScript Off")],
                        percDropdownDisabledFlag: [false, true]
                    });
                // Add Publishing dropdown
                $.PercItemPublisherService.getPublishActions(currentPageId, function(status, result)
                {
                    if (status)
                    {
                        var pubActions = eval("(" + result + ")").PSPublishingActionList;
                        if (pubActions.length > 0)
                        {
                            var actionNames = [I18N.message("perc.ui.page.menu@Publishing")];
                            var disableAction = [false];
                            $.each(pubActions, function()
                            {
                                actionNames.push(this.name);
                                disableAction.push(this.enabled);
                            });
                            //Add Publishing dropdown menu in toolbar
                            var publishNowDropdown = $("#perc-dropdown-publish-now");
                            publishNowDropdown.PercDropdown(
                                {
                                    percDropdownRootClass: "perc-dropdown-publish-now",
                                    percDropdownOptionLabels: actionNames,
                                    percDropdownCallbacks: [function()
                                    {},
                                        _publishItem, _openSchedule, _publishItem, _publishItem, _publishItem],
                                    percDropdownCallbackData: [I18N.message("perc.ui.page.menu@Publishing"), {
                                        pageId: currentPageId,
                                        pageName: pageName,
                                        trName: I18N.message("perc.ui.page.menu@Publish")
                                    }, {
                                        pageId: currentPageId,
                                        pageName: pageName,
                                        trName: I18N.message("perc.ui.page.menu@Publish")
                                    }, {
                                        pageId: currentPageId,
                                        pageName: pageName,
                                        trName: I18N.message("perc.ui.page.menu@Take Down")
                                    }, {
                                        pageId: currentPageId,
                                        pageName: pageName,
                                        trName: I18N.message("perc.ui.page.menu@Stage")
                                    }, {
                                        pageId: currentPageId,
                                        pageName: pageName,
                                        trName: I18N.message("perc.ui.page.menu@Remove from Staging")
                                    },
                                        currentPageId],
                                    percDropdownDisabledFlag: disableAction
                                });
                        }
                    }
                });
                $.PercAssetService.getUnusedAssets(currentPageId, populateOrphanAssets);
                // View dropdowm in editmode
                if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR){
                    var viewDropDownData =
                        {
                            percDropdownRootClass: "perc-dropdown-page-view",
                            percDropdownOptionLabels: [I18N.message("perc.ui.page.menu@View"), I18N.message("erc.ui.page.menu@Revisions"), I18N.message("perc.ui.page.menu@Comments"), I18N.message("perc.ui.page.menu@Preview"), I18N.message("perc.ui.page.menu@Publishing History"), I18N.message("perc.ui.menu@JavaScript Off")],
                            percDropdownCallbacks: [$.noop,_openRevisions, _openComments, _previewPage, _openPublishingHistory, $.noop],
                            percDropdownCallbackData: [I18N.message("perc.ui.page.menu@View"), {
                                pageId: currentPageId,
                                pageName: pageName
                            },{
                                pageId: currentPageId,
                                pageName: pageName
                            },
                                currentPageId, {
                                    pageId: currentPageId,
                                    pageName: pageName
                                },I18N.message("perc.ui.menu@JavaScript Off")],
                            percDropdownDisabledFlag: [false, true, true, true, true, true]
                        };

                    viewDropdown.PercDropdown(viewDropDownData);
                }

                // Add workflow transition buttons
                if (currentTabIndex === defaultTabIndex) loadTab(defaultTabIndex);

                // Init Orphan Assets Menu Actions
                initOrphanAssetsMenu();
            }
        }



        //handle Hover In image for unusedAssets
        function handleIn()
        {
            var self = $(this);
            self.attr("src", self.data("overIconSrc"));
        }

        //handle Hover Out image for unusedAssets
        function handleOut()
        {
            var self = $(this);
            self.attr("src", self.data("iconSrc"));
        }

        //Populate the Unused Assets tray
        function populateOrphanAssets(status, unusedAssets)
        {
            $("#perc_orphan_assets_expander").show();

            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                $("#perc_orphan_assets_maximizer").addClass("perc-disabled");
                $("#perc_orphan_assets_expander").addClass("perc-disabled");
                $.perc_utils.alert_dialog(
                    {
                        title: I18N.message("perc.ui.publish.title@Error"),
                        content: unusedAssets
                    });
                return;
            }

            if (unusedAssets.length > 0)
            {
                var orphanAssetsContainer = $(".perc-orphan-assets-list");
                orphanAssetsContainer.empty();
                var htmlAssets = "";
                for (let i = 0; i < unusedAssets.length; i++)
                {
                    var asset = unusedAssets[i];
                    var hoverText = I18N.message("perc.ui.page.general@Local");
                    var folderPaths = "";
                    if (typeof(asset.folderPaths) != "undefined" && asset.folderPaths.length > 0)
                    {
                        hoverText = asset.folderPaths[0].replace("//Folders/$System$", "") + "/" + asset.name;
                        folderPaths = asset.folderPaths;
                    }
                    orphanAssetsContainer.append(
                        $("<a />").attr("alt", hoverText).attr("title", hoverText).addClass("perc-orphan-asset").data('spec', {
                            "type": asset.type,
                            "id": asset.id,
                            "relationshipId": asset.relationshipId
                        }).append(
                            $("<div />").css("position", "relative").addClass("perc-widget-tool").append(
                                $("<img />").attr("src", asset.icon).data("iconSrc", asset.icon).data("overIconSrc", asset.overIcon).on('mouseenter',
                                    function(e){
                                        handleIn(e);
                                    }).on('mouseleave', function(e){
                                    handleOut(e);
                                })).append(
                                $("<div />").css("overflow", "hidden").addClass("perc-asset-label").append(
                                    $("<nobr />").html(asset.title)))).css('cursor', 'pointer').data('assetId', asset.id).data('assetType', asset.type).data('assetFolderPaths', folderPaths).data('widgetId', asset.widgetId).off("click").on("click",
                            function(evt){
                                selectOrphanAsset(evt);
                            })
                    );
                }
                // populates Orphan Assets tray and toggles it open/close
                $("#perc_orphan_assets_expander").off("click").on("click",function()
                {
                    clearSelection();
                    $.fn.percOrphanAssetsMaximizer(P);
                });

                // Set drag&drop behavior.
                orphanAssetsContainer.find('.perc-orphan-asset').draggable(
                    {
                        helper: function()
                        {
                            var helper = $(this).clone();
                            helper.find(".perc-asset-label").css('padding', "0px 0px 5px 5px");
                            return helper;
                        },
                        appendTo: 'body',
                        refreshPositions: true,
                        zIndex: 9990,
                        revert: true,
                        revertDuration: 0,
                        start: function()
                        {
                            $("body").css("overflow", "hidden");
                        },
                        stop: function()
                        {
                            $("body").css("overflow", "auto");
                        },
                        delay: 25
                    });

                $("#perc_orphan_assets_maximizer").removeClass("perc-disabled");
                $("#perc_orphan_assets_expander").removeClass("perc-disabled");
            }
            else
            {
                $("#perc_orphan_assets_maximizer").addClass("perc-disabled");
                $("#perc_orphan_assets_expander").addClass("perc-disabled");
            }
        }
        // A snippet to adjust the frame size on resizing the window.
        $(window).on("resize",function()
        {
            fixIframeHeight();
        });

        //------------------------//
        // Orphan Assets
        //------------------------//
        function initOrphanAssetsMenu()
        {
            $("#perc_asset_library").disableSelection();
            $(".perc-ui-delete-asset").attr("src", "/cm/images/icons/editor/deleteInactive.png").off();
            $(".perc-ui-edit-asset").attr("src", "/cm/images/icons/editor/editInactive.png").off();
        }

        function deleteOrphanAsset(event)
        {
            var assets = $("#perc_asset_library").find(".perc-orphan-assets-list");
            var unused = assets.find(".perc-orphan-assets-selected");
            var orphanIds = [];
            var orphanTypes = [];
            var orphanIsShared = [];
            var orphanWidgetsIds = [];
            for (var i = 0; i < unused.length; i++)
            {
                orphanIds.push($(unused[i]).data('assetId'));
                orphanTypes.push($(unused[i]).data('assetType'));
                orphanWidgetsIds.push($(unused[i]).data('widgetId'));
                var folderPaths = $(unused).data('assetFolderPaths');
                if (typeof(folderPaths) != "undefined") orphanIsShared.push(true);
                else orphanIsShared.push(false);
            }

            var title = I18N.message("perc.ui.page.unused@Remove Unused Asset");
            var assetmsg = (i < 2) ? I18N.message("perc.ui.page.general@asset") : I18N.message("perc.ui.page.general@assets");
            var msg = I18N.message("perc.ui.page.unused@Remove Unused Assets Message", assetmsg);

            var options = {
                id: 'perc-orphan-asset-delete-dialog',
                title: title,
                question: msg,
                type: "YES_PREFERRED_NO",
                cancel: function()
                {},
                success: function()
                {
                    pageModel.clearOrphanAssets(orphanWidgetsIds, orphanTypes, orphanIds, orphanAssetDeleted);
                }
            };
            $.perc_utils.confirm_dialog(options);
        }

        function orphanAssetDeleted()
        {
            var assets = $("#perc_asset_library").find(".perc-orphan-assets-list");
            var unused = assets.find(".perc-orphan-assets-selected");
            $(unused).remove();

            clearSelection();

            // Is the orphan assets list empty?
            var remainingAssets = $("#perc_asset_library").find(".perc-orphan-assets-list").find("a");
            if (remainingAssets.length === 0)
            {
                $.fn.percOrphanAssetsMaximizer(P);
                $("#perc_orphan_assets_expander").addClass("perc-disabled").off();
                $("#perc_orphan_assets_maximizer").addClass("perc-disabled");
            }
        }

        function editOrphanAsset(event)
        {
            var assets = $("#perc_asset_library").find(".perc-orphan-assets-list");
            var unused = assets.find(".perc-orphan-assets-selected");

            var orphanId = $(unused).data('assetId');
            var orphanType = $(unused).data('assetType');
            var orphanWidgetId = $(unused).data('widgetId');

            var orphanIsShared = false;

            var widgetData = {
                widgetid: orphanWidgetId,
                widgetdefid: orphanType
            };

            pageModel.configureAsset(widgetData, orphanId, orphanIsShared, orphanAssetEdited);
        }

        function orphanAssetEdited() {}

        function selectOrphanAsset(e)
        {
            if (e.shiftKey) $(this).toggleClass("perc-orphan-assets-selected");
            else
            {
                if ($(this).is(".perc-orphan-assets-selected")) clearSelection();
                else
                {
                    clearSelection();
                    $(this).toggleClass("perc-orphan-assets-selected");
                }
            }

            // Manage button icons and events
            var parent = $(this).parent();
            var selected = $(parent).find(".perc-orphan-assets-selected");
            if (selected.length > 0)
            {
                $(".perc-ui-delete-asset").attr("src", "/cm/images/icons/editor/delete.png").off("click").on("click",
                    function(evt){
                        deleteOrphanAsset(evt);
                    });
                $(".perc-ui-edit-asset").attr("src", "/cm/images/icons/editor/edit.png").on("click",
                    function(evt){
                        editOrphanAsset(evt);
                    });
            }
            else
            {
                $(".perc-ui-delete-asset").attr("src", "/cm/images/icons/editor/deleteInactive.png").off();
                $(".perc-ui-edit-asset").attr("src", "/cm/images/icons/editor/editInactive.png").off();
            }
            if (selected.length >= 2) $(".perc-ui-edit-asset").attr("src", "/cm/images/icons/editor/editInactive.png").off();
        }

        function clearSelection()
        {
            $(".perc-orphan-assets-list").find('a').removeClass("perc-orphan-assets-selected");
            $(".perc-ui-delete-asset").attr("src", "/cm/images/icons/editor/deleteInactive.png").off();
            $(".perc-ui-edit-asset").attr("src", "/cm/images/icons/editor/editInactive.png").off();
        }

        // Declare Content and Layout tabs
        $("#perc-pageEditor-tabs").tabs(
            {
                // Disable all Layout and Style tabs at load time
                disabled: [1, 2],
                activate: function(event)
                {
                    var idx = $('#perc-pageEditor-tabs').tabs('option','active');
                    // Ask for confirmation to navigate away from tab if the page has been modified
                    if (dirtyController.isDirty())
                    {
                        // if dirty, then show a confirmation dialog
                        dirtyController.confirmIfDirty(function()
                        {
                            // if they click ok, then reset dirty flag and proceed to select the tab
                            setDirty(false);
                            $('#perc-pageEditor-tabs').tabs("option", "active", idx );
                            //Reset the JavaScript Off/On menu to JavaScript Off
                            resetJavaScriptMenu();
                        });
                        return false;
                    }
                    else
                    {
                        //Reset the JavaScript Off/On menu to JavaScript Off
                        resetJavaScriptMenu();
                    }

                    loadTab(idx, true);
                }
            });

        if ($.PercNavigationManager.getView() === $.PercNavigationManager.VIEW_EDITOR && $.PercNavigationManager.getMode() === $.PercNavigationManager.MODE_EDIT)
        {
            $("#perc-wid-lib-expander").on("click", function()
            {
                $.fn.percWidLibMaximizer(P);
            });
        }

        function loadTab(index, addWrapper)
        {
            if(addWrapper){
                var viewWrapper = $.PercComponentWrapper("perc-action-page-tab-selected",["perc-ui-component-editor-toolbar","perc-ui-component-editor-frame"]);
                var isWrapperSet = $.PercViewReadyManager.setWrapper(viewWrapper);
                if(!isWrapperSet){
                    $.PercViewReadyManager.showRenderingProgressWarning();
                    return;
                }
            }
            if (index === CONTENT_TAB)
            {
                loadContent(currentPageId);
            }
            else if (index === LAYOUT_TAB)
            {
                loadLayout(currentPageId);
            }
            else if (index === STYLES_TAB)
            {
                loadCss(currentPageId);
            }
        }

        /**
         * Displays the template tray and let user change the template for selected page.
         */
        function _changeTemplate()
        {
            var successCallBack = function()
            {
                window.location.reload();
            };

            // Ask for confirmation to navigate away from tab if the page has been modified
            if (dirtyController.isDirty())
            {
                // if dirty, then show a confirmation dialog
                dirtyController.confirmIfDirty(function()
                {
                    // if they click ok, then reset dirty flag and proceed to select the tab
                    setDirty(false);
                    openChangeTemplateDialog();
                });
                return false;
            }
            else
            {
                openChangeTemplateDialog();
            }

            function openChangeTemplateDialog()
            {
                $.PercChangeTemplateDialog().openDialog(currentPageId, pageModel.getTemplateModel().getId(), $.PercNavigationManager.getSiteName(), successCallBack);
            }
        }

        /**
         * Get the template Id based on pageId and load the Template in Edit mode.
         */
        function _loadTemplate()
        {
            var memento = {
                'templateId': pageModel.getTemplateModel().getId(),
                'pageId': currentPageId,
                'tabId': "perc-tab-layout"
            };
            $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_EDIT_TEMPLATE, $.PercNavigationManager.getSiteName(), null, null, null, $.PercNavigationManager.getPath(), null, memento);
        }

        /**
         * Resets the text of the JavaScript menu to JavaScript Off.
         */
        function resetJavaScriptMenu()
        {
            //Reset the JavaScript Off/On menu to JavaScript Off
            $(".perc-dropdown-option-DisableJavaScript").text(I18N.message("perc.ui.menu@JavaScript Off"));
        }

        function resetPageName()
        {
            $.ajax(
                {
                    url: $.perc_paths.PAGE_CREATE + "/" + currentPageId,
                    success: function(data)
                    {
                        $("#perc-pageEditor-menu-name").html(data.Page.name);
                    },
                    type: 'GET',
                    dataType: 'json'
                });
        }

        function confirm_if_dirty(callback, errorCallback, options)
        {
            options = options || {};
            errorCallback = errorCallback ||
                function()
                {};

            if (dirtyController.isDirty())
            {
                dirtyController.confirmIfDirty(callback, errorCallback, options);
            }
            else
            {
                //Page is not dirty, proceed
                callback();
            }
        }

        function loadContent(pageId)
        {
            if ($.PercNavigationManager.getMode() == $.PercNavigationManager.MODE_EDIT)
            {
                pageModel = P.pageModel($.perc_pagemanager, $.perc_templatemanager, pageId, setupContent);
                $.PercAssetService.getUnusedAssets(pageId, populateOrphanAssets);
                if ($.PercNavigationManager.isJavascriptOff()) pageModel.setJavaScriptOff(true);
            }
            else
            {
                //Load preview content into the iFrame for readonly mode
                var previewPath = $.perc_paths.PAGE_PREVIEW + currentPageId;
                $("#frame").contents().remove();
                $("#frame").attr("src", previewPath);
                $("#frame").off("load");
                $("#frame").on("load", function()
                {
                    fixIframeHeight();
                    window.setTimeout(function()
                    {
                        $.perc_utils.handleLinks($("#frame"));
                    }, 500);

                    var frwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-frame');

                    if(frwrapper != null)
                        frwrapper.handleComponentProgress('perc-ui-component-editor-frame', "complete");

                    var tbwrapper = $.PercViewReadyManager.getWrapper('perc-ui-component-editor-toolbar');

                    if(tbwrapper != null)
                        tbwrapper.handleComponentProgress('perc-ui-component-editor-toolbar', "complete");
                });
            }
        }

        function loadLayout(pageId)
        {
            pageModel = P.pageModel($.perc_pagemanager, $.perc_templatemanager, pageId, setupLayout);
            if ($.PercNavigationManager.isJavascriptOff()) pageModel.setJavaScriptOff(true);
        }

        function loadCss(pageId)
        {
            pageModel = P.pageModel($.perc_pagemanager, $.perc_templatemanager, pageId, setUpCss);
            if ($.PercNavigationManager.isJavascriptOff()) pageModel.setJavaScriptOff(true);
        }

        function setupContent()
        {
            fixIframeHeight();
            P.contentView($("#frame"), pageModel);
        }

        function setupLayout()
        {
            fixIframeHeight();
            var layoutController = P.layoutController(pageModel);
            var sizeController = P.sizeController(pageModel);
            P.layoutView($("#frame"), pageModel, layoutController, sizeController, function(isDirty)
            {
                setDirty(isDirty);
            });
            $("#region-tool").draggable(
                {
                    helper: 'clone'
                });
        }

        function setDirty(isDirty)
        {
            dirtyController.setDirty(isDirty, "page");
            dirty = isDirty;
        }

        function isDirty()
        {
            return dirty;
        }

        /**
         * Sets up the css tab content by pasing the pageModel and binds the click event for the save button.
         */
        function setUpCss()
        {
            fixIframeHeight();
            var cssController = P.cssController(pageModel, $("#frame"), P.CSSPreviewView($("#frame"), pageModel));
            $("#perc-css-editor-save").on("click",function()
            {
                cssController.save(function()
                {});
            });

            $("#perc-css-editor-cancel").on("click",function()
            {
                pageModel.load();
                dirtyController.setDirty(false, "template");
            });
        }

        /**
         * Schedule the item(page/asset) for the supplied pageId/assetId.
         */
        function _openSchedule(callbackData)
        {
            var itemId = callbackData.pageId;
            var pageName = callbackData.pageName;
            if (dialogFlag)
            {
                $.PercScheduleDialog.open(itemId, pageName);
                $(".ui-datepicker-trigger").trigger("click");
                $("#ui-datepicker-div").css('z-index', 9501).css('display', 'none');
                $("#ui-timepicker-div").css('z-index', 9501).css('display', 'none');
                $("#perc-schedule-dialog-cancel").trigger("click");
                $.PercScheduleDialog.open(itemId, pageName);
                dialogFlag = false;
            }
            else
            {
                $("#ui-datepicker-div").css('z-index', 100000);
                $("#ui-timepicker-div").css('z-index', 100000);
                $.PercScheduleDialog.open(itemId, pageName);
            }
        }

        /**
         * Get the current status of the item.
         */
        function getPageStatus(pageId)
        {
            var pagePath = $.PercNavigationManager.getPath();
            $.perc_pathmanager.getItemProperties(pagePath, function(status, itemProps)
            {
                var pageStatus = itemProps.status;

                $(".perc-page-status-status").html(pageStatus);
                $.PercItemPublisherService.getScheduleDates(pageId, function(status, result)
                {
                    var scheduleDates = eval("(" + result + ")").ItemDates;
                    if (scheduleDates.startDate && pageStatus === I18N.message("perc.ui.page.general@Pending"))
                    {
                        $(".perc-page-status-status").html(I18N.message("perc.ui.page.general@Approved For") + " " + scheduleDates.startDate);
                    }
                });
            });
        }

        /**
         * Checks if Publish date is set for item before doing immediate publishing.
         * @param Object scheduleDates that respects the following form (all String members
         * could be the empty String ""):
         * <pre>
         * {
         *   endDate   : "04/30/2012 12:00 am",
         *   itemId    : "16777215-101-759",
         *   startDate : "04/24/2012 12:00 am"
         * }
         * </pre>
         */
        function _confirmPublish(scheduleDates)
        {
            var startDate = scheduleDates.startDate;
            var itemType = view == $.PercNavigationManager.VIEW_EDIT_ASSET ? "Asset" : "Page";
            var itemId = scheduleDates.itemId;
            if (startDate !== "")
            {
                var settings = {
                    id: "perc-confirm-publish-dialog",
                    title: I18N.message("perc.ui.page.general@Warning"),
                    question: I18N.message("perc.ui.page.confirmpublish@This item is scheduled",startDate),
                    success: function()
                    {
                        _immediateItemPublish(itemId, itemType);
                    },
                    cancel: function()
                    {},
                    yes: I18N.message("perc.ui.page.confirmpublish@Continue Anyway")
                };
                utils.confirm_dialog(settings);
            }
            else
            {
                _immediateItemPublish(itemId, itemType);
            }
        }

        /**
         * Invokes the publishing service. If an error is returned it shows it with a dialog and
         * stops the publishing proccess.
         * @param String itemId
         * @param String itemType
         */
        function _immediateItemPublish(itemId, itemType)
        {
            $.PercBlockUI();
            $.PercItemPublisherService.publishItem(itemId, itemType, _afterPublish);
        }

        /**
         * Publish/Take Down the item(page/asset) for the supplied pageId/assetId.
         */
        function _publishItem(callbackData)
        {
            var itemId = callbackData.pageId;
            var trName = callbackData.trName;
            var itemType = view === $.PercNavigationManager.VIEW_EDIT_ASSET ? "Asset" : "Page";
            var siteName = $.PercNavigationManager.getSiteName();

            // The user can create a page without selecting a site
            if (siteName === undefined && itemType === "Page")
            {
                // Retrieve the page path folder by getting its data using its id,
                // Stripe the //Sites prefix from it and retrieve the site's name
                var currentItemPath = $.perc_finder().getPathItemById(itemId).folderPath;
                siteName = currentItemPath.replace('/' + $.perc_paths.SITES_ROOT, '').split('/')[1];
            }

            confirm_if_dirty(function()
            {
                doIfItemExists(itemId, function()
                    {
                        /*doIfCheckedOutToCurrentUser(itemId, function()
                            { */
                        doIfDefaultServerNotModified(siteName, function()
                            {
                                if (trName === I18N.message("perc.ui.page.menu@Publish"))
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
                                            $.perc_utils.alert_dialog(
                                                {
                                                    content: I18N.message("perc.ui.page.confirmpublish@Unable to get the saved publish dates"),
                                                    title: I18N.message("perc.ui.labels@Error")
                                                });
                                            return false;
                                        }
                                    });
                                }
                                else if (trName === I18N.message("perc.ui.page.menu@Take Down"))
                                {
                                    // $.PercBlockUI();
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
                            },
                            function()
                            {
                                //an Admin has overridden the current editor in another session
                                $.perc_utils.alert_dialog(
                                    {
                                        title: trName,
                                        content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Overridden"),
                                        okCallBack: function()
                                        {
                                            $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                                        }
                                    });
                            });
                    },
                    function()
                    {
                        $.perc_utils.alert_dialog(
                            {
                                title: trName,
                                content: I18N.message("perc.ui.webmgt.contentbrowser.warning@Action Not Performed Deleted"),
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
            if (!success)
            {
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results[0]);
                $.unblockUI();
                $.perc_utils.alert_dialog(
                    {
                        title: I18N.message('perc.ui.labels@Error'),
                        content: defMsg
                    });
            }
            else
            {
                var publishStatus = results[0].SitePublishResponse.status;
                if (publishStatus === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_FORBIDDEN)
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog(
                        {
                            title: I18N.message("perc.ui.publish.errordialog.title@Server Publish"),
                            content: I18N.message("perc.ui.publish.errordialog.message@Publish Not Allowed")
                        });
                }
                else if ( publishStatus === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_BADCONFIG)
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog(
                        {
                            title: I18N.message("perc.ui.publish.errordialog.title@Server Publish"),
                            content: I18N.message("perc.ui.publish.errordialog.message@Bad configuration")
                        });
                }
                else if ( publishStatus === $.PercItemPublisherService.PUBLISHER_JOB_STATUS_NOSTAGING_SERVERS)
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog(
                        {
                            title: I18N.message("perc.ui.publish.errordialog.title@Server Publish"),
                            content: I18N.message("")
                        });
                }
                else
                {
                    $.PercNavigationManager.goTo($.PercNavigationManager.VIEW_EDITOR, true);
                    $.unblockUI();
                }
            }
        }

        /**
         * Opens the revision dialog for the supplied pageId.
         */
        function _openRevisions(callbackData)
        {
            var pageId = callbackData.pageId;
            var pageName = callbackData.pageName;
            $.PercRevisionDialog.open(pageId, pageName, $.PercRevisionDialog.ITEM_TYPE_PAGE, $.PercRevisionDialog.ITEM_MODE_VIEW);
        }

        /**
         * Opens the revision dialog for the supplied pageId.
         */
        function _openComments(callbackData)
        {
            var pageId = callbackData.pageId;
            var pageName = callbackData.pageName;
            $.PercCommentsDialog.open(pageId, pageName, $.PercCommentsDialog.ITEM_TYPE_PAGE);
        }

        /**
         * Opens the publishing history dialog for the supplied pageId.
         */
        function _openPublishingHistory(callbackData)
        {
            var pageId = callbackData.pageId;
            var pageName = callbackData.pageName;
            $.PercPublishingHistoryDialog.open(pageId, pageName, $.PercPublishingHistoryDialog.ITEM_TYPE_PAGE);
        }

        function _openOptimizer(callbackData){
            $.PercPageOptimizerDialog.open(callbackData.pageId, callbackData.pageName);
        }
        function _previewPage(currentPageId)
        {
            confirm_if_dirty(function()
            {
                jQuery.perc_finder().launchPagePreview(currentPageId);
            });
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
         * Check if the default publish server is modified and needs a CM1 restart before publish
         * or remove from site.
         *
         * @param SiteName
         * @param callback function to perform if we dont need a CM1 restart.
         */
        function doIfDefaultServerNotModified(siteName, callback)
        {
            $.PercNavigationManager.loadSiteProperties(siteName, function(siteProperties)
            {
                $.PercItemPublisherService.isDefaultServerModified(siteProperties.id, function(status, result)
                {
                    if (status)
                    {
                        if (result)
                        {
                            $.perc_utils.alert_dialog(
                                {
                                    content: I18N.message("perc.ui.page.dialog@Restart Required"),
                                    title: I18N.message("perc.ui.uploadtheme.dialog.title@Warning")
                                });
                        }
                        else
                        {
                            callback();
                        }
                    }
                });
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
         * Return the public API for this class.
         */
        return pageViewAPI;
    };
})(jQuery, jQuery.Percussion);
