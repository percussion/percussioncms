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

// JavaScript Document
(function($)
{
    var options;
    var gadgetArray;
    var gadgetList;
    var dashboardObj;
    var westSize = 500;
    var layout;
    var currentPage = 0;
    var username = $.PercNavigationManager.getUserName();
    var dashboardConfigMetaKey = "perc.user." + username + ".dash.page." + currentPage;
    var userPrefsMetaKey = dashboardConfigMetaKey + ".prefs";
    var menuMinimize;
    var menuExpand;
    var menuConfig;
    var menuRemove;
    var menu;
    var titleBarShadow = "<div class='perc-gadget-titlebar-shadow'/>";
    var dragDelay = ($.PercNavigationManager.isAutoTest() ? 0 : 250);

    // fix text overflow when window resizes
    $(window).on('resize', function()
    {
        getWidthOfCol();
        $(".perc-ellipsis").each(function()
        {
            handleOverflowHeader($(this));
        });
    });

    //Define the default dashboard config
    var defaultDashboardConfig = {
        "DashboardConfig": {
            "gadgets": [
                {
                    "instanceId": 0,
                    "url": "/cm/gadgets/repository/cm1_welcome_gadget/perc_welcome_gadget.xml",
                    "col": 0,
                    "row": 0,
                    "expanded": true
                }, {
                    "instanceId": 1,
                    "url": "/cm/gadgets/repository/perc_license_monitor_gadget/perc_license_monitor_gadget.xml",
                    "col": 1,
                    "row": 0,
                    "expanded": true
                }, {
                    "instanceId": 2,
                    "url": "/cm/gadgets/repository/perc_workflow_status_gadget/perc_workflow_status_gadget.xml",
                    "col": 1,
                    "row": 1,
                    "expanded": true
                }]
        }
    };

    $.PercDashboard = function(opts)
    {
        options = opts;
        return {
            load: _load,
            refresh: refreshAllGadgets,
            toggle: _toggle,
            resize: _resize,
            west_size: _west_size,
            set_layout: _set_layout,
            addGadget: addGadget,
            moveGadget: moveGadget,
            removeGadget: removeGadget,
            restore: restore,
            saveDashboard: saveDashboard,
            setupTray: _setupTray,
            showSplashDialog: showSplashDialog
        };
    };

    /**
     * Remove a gadget.
     * @param instanceId {int} the instance id for the gadget.
     */
    function removeGadget(instanceId)
    {
        var $theGadget = $("#gid_" + instanceId);
        $.perc_utils.confirm_dialog(
            {
                type: "YES_NO_PREFERRED",
                title: I18N.message("perc.ui.dashboard@Confirm Deletion"),
                question: I18N.message("perc.ui.dashboard@Confirm Deletion Question") + $theGadget.attr("name") + I18N.message("perc.ui.dashboard@Gadget"),
                success: function()
                {
                    $theGadget.remove();
                    gadgets.container.removeGadget(instanceId);
                    //Remove gadget related metadata
                    var key = dashboardConfigMetaKey + ".mid." + instanceId + ".";
                    $.PercMetadataService.deleteEntryByPrefix(key, function(status, msg)
                    {
                        if (status === jQuery.PercServiceUtils.STATUS_ERROR)
                        {
                            //Maybe log here when we have a good way to log
                        }
                    });
                    saveDashboard();
                }
            });

    }

    /**
     * Add a new gadget.
     * @param url {string} the url to the gadget definition xml of the gadget to be added.
     *  this can be an external url starting with http:/https: or a local url in which case
     *  it must start with /cm.
     * @param col {int} target column.
     * @param row {int} target row.
     */
    function addGadget(url, col, row)
    {
        var jGadget = {
            "expanded": true,
            "url": url,
            "col": col,
            "row": row
        };
        var newGadget = createGadget(jGadget);
        saveDashboard();
        return newGadget;
    }

    /**
     * Move a gadget.
     * @param instanceId {int} the instance id for the gadget.
     * @param col {int} target column.
     * @param row {int} target row.
     */
    function moveGadget(instanceId, col, row)
    {
        var $gadget = $("#gid_" + instanceId);
        insertGadgetHTML($gadget, col, row);
        setTimeout(function()
        {
            refreshGadget(instanceId);
        }, 100);
        saveDashboard();
    }

    /**
     * Refresh the contents of a specific gadget.
     * @param instanceId {int} the instance id for the gadget.
     */
    function refreshGadget(instanceId)
    {
        var gInstance = gadgets.container.getGadget(instanceId);
        gInstance.refresh();
    }

    /**
     * Refresh all gadgets on the dashboard.
     */
    function refreshAllGadgets()
    {
        $(".perc-gadget").each(function()
        {
            refreshGadget($(this).attr("instanceId"));
        });
    }

    /**
     * Saves the state of the dashboard to the server.
     * @param callback {function} optional callback function to be executed
     * when server save is complete.
     */
    function saveDashboard(callback)
    {
        //Scan dashboard DOM and build the config object
        updateGadgetPositionAttributes();
        disableTextSelectionInIE();

        var config = {
            "DashboardConfig": {
                "gadgets": []
            }
        };
        $(".perc-gadget-column").each(function(columnIdx)
        {
            $(this).children(".perc-gadget").each(function(rowIdx)
            {
                var entry = {
                    "instanceId": parseInt($(this).attr("instanceId")),
                    "url": $(this).data("gadgetUrl"),
                    "col": columnIdx,
                    "row": rowIdx,
                    "expanded": $(this).hasClass("perc-gadget-content-expanded")
                };
                config.DashboardConfig.gadgets.push(entry);
            });
        });
        $.PercMetadataService.save(dashboardConfigMetaKey, config, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {

                if (typeof(callback) == 'function') callback($.PercServiceUtils.STATUS_SUCCESS);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                if (typeof(callback) == 'function') callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });

    }

    /**
     * Restore the current dashboard page to the default dashboard settings.
     */
    function restore()
    {
        $.perc_utils.confirm_dialog(
            {
                type: "YES_NO",
                title: I18N.message("perc.ui.page.general@Warning"),
                question: I18N.message("perc.ui.dashboard@Reset Dashboard") + "<br /><br />" + I18N.message("perc.ui.dashboard@Proceed question"),
                width: 500,
                success: function()
                {
                    $.PercMetadataService.deleteEntry(dashboardConfigMetaKey, function(status, result)
                    {
                        if (status === $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            $.PercMetadataService.deleteEntryByPrefix(dashboardConfigMetaKey + ".", function(status, result)
                            {
                                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                                {
                                    window.location.reload();
                                }
                                else
                                {
                                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                                    $.perc_utils.alert_dialog(
                                        {
                                            title: I18N.message("perc.ui.publish.title@Error"),
                                            content: defaultMsg
                                        });
                                }
                            });
                        }
                        else
                        {
                            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                            $.perc_utils.alert_dialog(
                                {
                                    title: I18N.message("perc.ui.publish.title@Error"),
                                    content: defaultMsg
                                });
                        }
                    });
                }
            });

    }

    /**
     * Helper function to build a local url for a gadget on this server if the
     * path starts with "/cm
     * @param path {string} the url path to the gadget including leading forward
     * slash.
     */
    function makeLocalUrl(path)
    {

        if(nonSslPort === -1){
            return hostScheme + "://" + hostAddress + path;
        }else{
            return hostScheme + "://" + hostAddress + ":" + nonSslPort + path;
        }
        return path;
    }

    /**
     * Retrieve dashboard configuration from the server.
     * @param callback {function} the callback function that will
     * return the dashboard config.
     * <pre>
     * Args:
     *    status
     *    DashboardConfig object
     * </pre>
     */
    function getUserDashboardConfig(callback)
    {
        $.PercMetadataService.find(dashboardConfigMetaKey, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                if (result == null) result = {};
                var obj = result.metadata;
                var config = defaultDashboardConfig;
                if (!isEmpty(obj))
                {
                    var exec = "config = " + obj.data + ";";
                    eval(exec);
                }
                if (!$.PercNavigationManager.isAdmin())
                {
                    //Remove any admin only gadgets as the user is no an admin
                    var adminOnlyGadgets = [];
                    for (i = 0; i < gadgetList.length; i++)
                    {
                        if (gadgetList[i].adminOnly)
                        {
                            adminOnlyGadgets.push(gadgetList[i].url);
                        }
                    }
                    for (i = (config.DashboardConfig.gadgets.length - 1); i >= 0; i--)
                    {
                        var current = config.DashboardConfig.gadgets[i];
                        if ($.inArray(current.url, adminOnlyGadgets) !== -1)
                        {
                            config.DashboardConfig.gadgets.splice(i, 1);
                        }
                    }
                }
                callback($.PercServiceUtils.STATUS_SUCCESS, config);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
            }
        });

    }

    /**
     * Initializes the preference store.
     */
    function initPreferenceStore(callback)
    {
        gadgets.container.userPrefStore.init(userPrefsMetaKey, function()
        {
            callback();
        });
    }

    /**
     * Load the gadget listing from the server and cache it locally.
     */
    function loadGadgetListing(callback)
    {
        $.PercDashboardService.getTrayGadgets($('.perc-gadget-type').val(), function(status, data)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                gadgetList = data.Gadget;
            }
            else
            {
                $.perc_utils.alert_dialog(
                    {
                        title: I18N.message("perc.ui.publish.title@Error"),
                        content: I18N.meesage("perc.ui.dashboard@Cannot Load Gadget List")
                    });
            }
            if (typeof(callback) == 'function') callback();
        });
    }

    function _set_layout(ly)
    {
        layout = ly;
    }

    function _resize(newSize)
    {
        if (newSize !== westSize)
        {
            westSize = newSize;
        }
    }

    function _west_size()
    {
        return westSize;
    }

    /**
     * Load all gadgets and their preferences and render them in the dashboard.
     */
    function _load(serviceURL, callback)
    {
        loadGadgetListing(function()
        {
            initPreferenceStore(function()
            {
                getUserDashboardConfig(function(status, results)
                {
                    if (status === $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        westSize = 500;
                        if (layout)
                        {
                            layout.sizePane('west', westSize);
                        }
                        gadgetArray = new Array(results.DashboardConfig.gadgets.length);
                        gadgets.container.addSetTitleListener(_afterSetTitle);
                        renderGadgets(results.DashboardConfig.gadgets);

                        $(document).on("click","#perc-dashboard-restore-menu",function()
                        {
                            restore();
                        });

                        disableTextSelectionInIE();
                        if (typeof(callback) == 'function') callback();
                    }
                    else
                    {
                        $.perc_utils.alert_dialog(
                            {
                                title: I18N.message("perc.ui.publish.title@Error"),
                                content: results
                            });
                    }
                });
            });
        });

    }

    function showSplashDialog(){
        if ($.PercNavigationManager.isAdmin())
        {
            $.perc_pathmanager.open_path( "/Sites/?startIndex=1&maxResults=1", true,
                function(folder_spec){
                    var calculatedHeight = 639;
                    if (navigator.userAgent.indexOf('MSIE') !== -1) {
                        calculatedHeight = 656;
                    }
                    if (folder_spec.PagedItemList.childrenCount === 0){
                        $.perc_utils.confirm_dialog({
                            id: "perc-splashscreen",
                            type: "CANCEL_START",
                            title: "",
                            showAgainCheck: true,
                            question: "<img style='height: 449px' src='../images/images/splash-v2.png'>",
                            width: 686,
                            height: calculatedHeight,
                            dontShowAgainAction: function(){},
                            success: function()
                            {
                                $('#perc-finder-new-site').trigger("click");
                            }
                        });
                        //Chrome have some issue recalculating the heigth
                        $("#perc-splashscreen").find(".ui-dialog-content").css("overflow", "hidden");
                    }
                }, function(){}, true );
        }
    }
    /**
     * Create and render all gadgets.
     */
    function renderGadgets(jsonGadgets)
    {
        var i = 0;
        var chromeIds = Array();
        gadgets.container.clearGadgetInstanceIds();
        for (var i = 0; i < jsonGadgets.length; i++)
        {
            if (jsonGadgets[i].url) createGadget(jsonGadgets[i]);
        }
        updateGadgetPositionAttributes();
    }

    /**
     * Adds column and row attributes to all the dashboard gadgets.
     * This helps QA keep track where the gadgets are.
     */
    function updateGadgetPositionAttributes()
    {
        $(".perc-gadget").each(function()
        {
            var column = parseInt($(this).parent(".perc-gadget-column").attr("id").replace("col-", ""));
            var row = $(this).prevAll().length;
            $(this).attr("column", column).attr("row", row);
        });
    }

    /**
     * Create and add a gadget.
     * @param jsonGadget {object} object with gadget metadata. Cannot be <code>null</code>.
     */
    function createGadget(jsonGadget)
    {
        var args = {
            specUrl: makeLocalUrl(jsonGadget.url),
            instanceId: jsonGadget.instanceId
        };
        var gadgetObj = gadgets.container.createGadget(args);
        gadgets.container.addGadget(gadgetObj);
        jsonGadget.instanceId = gadgetObj.id;
        createGadgetHTML(jsonGadget);
        getGadgetMeta([{
                "url": makeLocalUrl(jsonGadget.url),
                "moduleId": gadgetObj.id
            }], [gadgets.container.userPrefStore.getPrefs(gadgetObj)], function(data)
            {
                var meta = data.gadgets[0];
                var prefs = meta["userPrefs"];
                var prefCount = 0;
                for (c in prefs)
                {
                    prefCount++;
                } // Check if user prefs has values
                gadgetObj.title = meta.title;
                $("#gid_" + gadgetObj.id).prop("name", meta.title);
                gadgetObj.height = meta.height;
                gadgetObj.width = meta.width;
                gadgetObj.hasPrefs = prefCount > 0;
                gadgetObj.metaKey = dashboardConfigMetaKey + ".mid." + gadgetObj.id + ".";

                gadgets.container.layoutManager.addGadgetChromeId(gadgetObj.id, "gid_" + gadgetObj.id);
                gadgets.container.renderGadget(gadgetObj);
                addMenu($("#gid_" + gadgetObj.id));
                if (!jsonGadget.expanded)
                {
                    minimizeGadget($("#gid_" + gadgetObj.id));
                }
                makeDashboardGadgetsDraggable($("#gid_" + gadgetObj.id));
            }

        );
        return gadgetObj.id;
    }

    /**
     * Set the title of Gadgets header
     * @param titleid {string} the element id for the title bar span.
     */
    function _afterSetTitle(titleid)
    {
        var gadgetId = '#' + titleid;
        $(gadgetId).attr('title', $(gadgetId).text());
        $(gadgetId).removeClass('gadgets-gadget-title').addClass('gadgets-gadget-title-change');
    }

    /**
     * Add the gadget menu
     */
    function addMenu($gadget)
    {
        var self = $gadget;
        var name = $gadget.attr("name");
        var instanceId = $gadget.attr("instanceId");
        var titleBar = $gadget.find(".gadgets-gadget-title-bar");
        var html = titleBar.html();

        html = html.replace(/\|/g, "");

        titleBar.html(html);

        titleBar.after(titleBarShadow);

        var title = $gadget.find(".gadgets-gadget-title");
        var titleButtons = $gadget.find(".gadgets-gadget-title-button-bar");
        titleButtons.css("float", "right");
        var gadgetMenuButton = $("<img src='../images/images/gadgetMenuButton.png' class='perc-gadget-menu-button' style='cursor:pointer' title='Click to show the Gadget Menu' alt='Gadget menu icon'/>").on("click",function(event)
        {
            event.stopPropagation();
            showMenu(self, titleBar, event);
        });
        titleButtons.append(gadgetMenuButton);

        // fixf the width of columns
        getWidthOfCol();

        // fix text overflow at first
        $(".perc-ellipsis").each(function()
        {
            handleOverflowHeader($(this));
        });
    }

    function showMenu(gadget, titleBar)
    {
        // grab all the elements we need
        menu = $("#perc-gadget-menu");
        menuMinimize = $("#perc-gadget-menu-minimize");
        menuExpand = $("#perc-gadget-menu-expand");
        menuConfig = $("#perc-gadget-menu-config");
        menuRemove = $("#perc-gadget-menu-remove");
        var instanceId = gadget.attr("instanceId");
        var gInstance = gadgets.container.getGadget(instanceId);
        var hasPrefs = gInstance.hasPrefs;
        // move the menu to the current gadget so that it shows right under the gadget's titlebar
        var top = titleBar.position().top + $(".perc-dashboard-container").scrollTop();
        var left = titleBar.position().left;
        var menuX = left + titleBar.outerWidth() - menu.width();
        var menuY = top + titleBar.outerHeight();
        menu.css("top", menuY).css("left", menuX).css("display", "block");

        // update the menu items based on the current state of the gadget
        updateMinimizeExpandMenuItem(gadget);

        // handle maximize menu item
        $(document).on("click","#perc-gadget-menu-minimize",function(eventObject )
        {
            minimizeGadget(gadget);
            menu.hide();
        });

        // handle remove menu item
        $(document).on("click","#perc-gadget-menu-remove", function(eventObject )
        {
            var instanceId = gadget.attr("instanceId");
            removeGadget(instanceId);
        });

        // handle expand menu item
        $(document).on("click", "#perc-gadget-menu-expand",function(eventObject )
        {
            expandGadget(gadget);
            menu.hide();
        });

        // handle edit settings
        $(document).on("click","#perc-gadget-menu-config", function(eventObject )
        {
            handlePrefs(instanceId);
        });

        if (hasPrefs)
        {
            menuConfig.show();
        }
        else
        {
            menuConfig.hide();
        }

        // hide the menu if you are about to resize the finder
        $(document).on("hover",".ui-resizable-handle",function(eventObject )
        {
            menu.hide();
        });

        // hide the menu if you hover away from it
        //TODO:
      /* $(document).on("mouseout","#perc-gadget-menu",function(eventObject )
        {
            menu.hide();
        });
        */

    }

    // check display attribute of gadget content
    // if it's display none,  then show expand  menu  item
    // if it's display block, then show minimize menu item
    function updateMinimizeExpandMenuItem(gadget)
    {
        if (_isGadgetExpanded(gadget))
        {
            menuExpand.hide();
            menuMinimize.show();
        }
        else
        {
            menuExpand.show();
            menuMinimize.hide();
        }
    }

    // minimize the gadget
    function minimizeGadget(gadget)
    {
        // if it's already minimized then return
        if (!_isGadgetExpanded(gadget)) return;

        var instanceId = gadget.attr("instanceId");
        var flagConfirm = true;

        try
        {
            //When the gadget is moved the self['remote_iframe_' + instanceId] is windows undefined.
            var notifyFunc = $('#remote_iframe_' + instanceId)[0].contentWindow._minimizeConfirm;
            if (typeof(notifyFunc) == 'function') flagConfirm = notifyFunc();
        }
        catch (ignore)
        {}

        if (!flagConfirm) return;
        gadgets.container.getGadget(instanceId).handleToggle();

        // this is a marker for QA testing
        gadget.css("min-height", "36px").addClass("perc-gadget-content-minimized").removeClass("perc-gadget-content-expanded");

        gadget.find(".perc-gadget-titlebar-shadow").hide();
        gadget.find(".gadgets-gadget-title-bar").css("-moz-border-radius-bottomleft", "7px");
        gadget.find(".gadgets-gadget-title-bar").css("-moz-border-radius-bottomright", "7px");

        gadget.find(".gadgets-gadget-title-bar").css("-webkit-border-bottom-left-radius", "7px");
        gadget.find(".gadgets-gadget-title-bar").css("-webkit-border-bottom-right-radius", "7px");
        saveDashboard();
    }

    // expand the gadget
    function expandGadget(gadget)
    {
        // if it's already expanded then return
        if (_isGadgetExpanded(gadget)) return;

        var instanceId = gadget.attr("instanceId");
        gadgets.container.getGadget(instanceId).handleToggle();

        // this is a marker for QA testing
        gadget.removeAttr("style").removeClass("perc-gadget-content-minimized").addClass("perc-gadget-content-expanded");

        gadget.find(".perc-gadget-titlebar-shadow").show();
        gadget.find(".gadgets-gadget-title-bar").css("-moz-border-radius-bottomleft", "");
        gadget.find(".gadgets-gadget-title-bar").css("-moz-border-radius-bottomright", "");

        gadget.find(".gadgets-gadget-title-bar").css("-webkit-border-bottom-left-radius", "");
        gadget.find(".gadgets-gadget-title-bar").css("-webkit-border-bottom-right-radius", "");
        saveDashboard();
        //Notify gadgets that provide the _expandNotify function that an expand action
        //occurred.
        try
        {
            //When the gadget is moved the self['remote_iframe_' + instanceId] is windows undefined.
            //var notifyFunc = self['remote_iframe_' + instanceId]._expandNotify;
            var notifyFunc = $('#remote_iframe_' + instanceId)[0].contentWindow._expandNotify;

            if (typeof(notifyFunc) == 'function') notifyFunc();
        }
        catch (ignore)
        {}
    }

    // checks display attribute of gadget div
    // return true if display block
    // return false otherwise
    function _isGadgetExpanded(gadget)
    {
        var content = gadget.find(".gadgets-gadget-content");
        var display = content.css("display");
        if (display === "block") return true;
        return false;
    }

    function handlePrefs(idx)
    {
        gadgets.container.getGadget(idx).handleOpenUserPrefsDialog(function(gadgetId)
        {
            userPrefsDialogDone(gadgetId);
        });
        return false;
    }

    function userPrefsDialogDone(gadgetId)
    {
        addDatePickersToTrafficGadgets(gadgetId);
        makeFieldsIntoTitles(gadgetId);
        handleWorkFlowChange(gadgetId);
    }

    function handleWorkFlowChange(gadgetId)
    {
        var workflowSelect = $('[name="m_' + gadgetId + '_up_ssworkflow"]');
        if (workflowSelect.length > 0)
        {
            workflowSelect.on("change",function()
            {
                updateStatusOptions(gadgetId);
            });
        }
    }

    /**
     * Update the status options depending on which workflow are selected.
     */
    function updateStatusOptions(gadgetId)
    {
        var workflowSelect = $('[name="m_' + gadgetId + '_up_ssworkflow"]');
        $.PercWorkflowService().getStatusByWorkflow(workflowSelect.val(), function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                var statusSelect = $('[name="m_' + gadgetId + '_up_status"]');
                statusSelect.find('option').remove();
                var statusList = $.perc_utils.convertCXFArray(result.data.EnumVals.entries);
                for (s in statusList)
                {
                    var value = statusList[s].value;
                    statusSelect.append($('<option/>').val(value).html(value));
                }
            }
        });
    }

    /**
     * Set 'Date From' to 3 months back Date if was empty
     * @param dateText {string}
     * @param inst {object} datepicker instance
     */
    function checkDateFromEmpty(dateText, inst)
    {
        if (dateText === "")
        {
            var months3Back = new Date();
            months3Back.setMonth(months3Back.getMonth() - 3);
            $(this).datepicker('setDate', months3Back);
        }
    }

    /**
     * Set 'Date To' to Today if was empty
     * @param dateText {string}
     * @param inst {object} datepicker instance
     */
    function checkDateToEmpty(dateText, inst)
    {
        if (dateText === "")
        {
            var today = new Date();
            $(this).datepicker('setDate', today);
        }
    }

    function addDatePickersToTrafficGadgets(gadgetId)
    {
        var dateFromName = "m_" + gadgetId + "_up_b-dateFrom";
        var dateToName = "m_" + gadgetId + "_up_c-dateTo";
        var dateFromInput = $("input[name=" + dateFromName + "]");
        var dateToInput = $("input[name=" + dateToName + "]");

        //add the check function on onClose event
        dateFromInput.datepicker(
            {
                showAnim: '',
                onClose: checkDateFromEmpty
            });
        dateToInput.datepicker(
            {
                showAnim: '',
                onClose: checkDateToEmpty
            });

        if (dateFromInput.val() === "")
        {
            var today = new Date();
            var months3Back = new Date();
            months3Back.setMonth(months3Back.getMonth() - 3);
            dateFromInput.datepicker('setDate', months3Back);
            dateToInput.datepicker('setDate', today);
        }

        $("#ui-datepicker-div").css("z-index", "100000");
    }

    function makeFieldsIntoTitles(gadgetId)
    {
        var activityTitleName = "m_" + gadgetId + "_up_e0-activity-title";
        var livePagesTitleName = "m_" + gadgetId + "_up_f1-livePages-title";
        $("input[name=" + activityTitleName + "]").hide();
        $("input[name=" + livePagesTitleName + "]").hide();
    }

    /**
     * Create and insert the HTML that will contain the gadget content
     * @param gadget {object} the gadget info object from the dashboard config.
     */
    function createGadgetHTML(gadget)
    {
        var ex = (gadget.expanded);
        var expanded = ex ? 'style="display:block"' : 'style="display:none"';
        var exicon = ex ? 'ui-icon-circle-triangle-s' : 'ui-icon-circle-triangle-n';
        var gid = "gid_" + gadget.instanceId;

        gadgetHTML = '<div id="' + gid + '" class="perc-gadget perc-gadget-content-expanded" name="' + gadget.name + '" instanceid="' + gadget.instanceId + '"></div>\n';
        insertGadgetHTML(gadgetHTML, gadget.col, gadget.row);
        $("#" + gid).data("gadgetUrl", gadget.url);
        if (!ex)
        {
            $("#" + gid).addClass("perc-gadget-content-minimized").removeClass("perc-gadget-content-expanded");
        }
    }

    /**
     * Helper to insert gadget HTML into the proper column and row in the dashboard.
     * @param gadgetHTML {string} the html string to insert, cannot be <code>null</code>.
     * @param col {int} the column index.
     * @param row {int} the row index.
     */
    function insertGadgetHTML(gadgetHTML, col, row)
    {
        var $column = $("#col-" + col);
        var $existing = $column.children(".perc-gadget:eq(" + row + ")");
        if ($existing.length === 0)
        {
            $column.append(gadgetHTML);
        }
        else
        {
            $existing.before(gadgetHTML);
        }

        getWidthOfCol();

        $(".perc-ellipsis").each(function()
        {
            handleOverflowHeader($(this));
        });
    }

    /**
     * Helper method to determine if an object is empty.
     */
    function isEmpty(ob)
    {
        for (var i in ob)
        {
            return false;
        }
        return true;
    }

    function _toggle(i)
    {
        var gContentDiv = "gadget-content-" + i;
        var div = document.getElementById(gContentDiv);
        var tog = $("#tog-" + i);
        if (div.style.display === 'none')
        {
            tog.removeClass("ui-icon-circle-triangle-n").addClass("ui-icon-circle-triangle-s");
            div.style.display = 'block';
        }
        else
        {
            tog.addClass("ui-icon-circle-triangle-n").removeClass("ui-icon-circle-triangle-s");
            div.style.display = 'none';
        }
        dirty = true;
    }

    /**
     * Retrieve gadget meta from the shindig meta service.
     * @param gadgets {array}
     * @param uPrefs {array}
     * @param callback {function}
     * @return
     * @type object
     */
    function getGadgetMeta(gadgets, uPrefs, callback)
    {
        var requestObj = {
            "context": {
                "view": "default",
                "container": "default",
                "country": "US",
                "language": "en"
            },
            "gadgets": []
        };
        for (var i = 0; i < gadgets.length; i++)
        {
            var current = gadgets[i];
            var gadget = {
                "url": current.url,
                "moduleId": current.moduleId,
                "prefs": uPrefs[i]
            };
            requestObj.gadgets.push(gadget);
        }

        $.PercServiceUtils.makeJsonRequest("/cm/gadgets/metadata", $.PercServiceUtils.TYPE_POST, false, function(status, result)
        {
            if (status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                callback(result.data);
            }
            else
            {
                var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                $.perc_utils.alert_dialog(
                    {
                        title: I18N.message("perc.ui.publish.title@Error"),
                        content: defaultMsg
                    });
            }
        }, requestObj);
    }

    /**
     *  Gadget Tray
     */
    var gadgetTrayExpander = null;
    var gadgetTray = null;
    var gadgetTrayList = null;
    var gadgetTrayExpanderIcon = null;
    var gadgetTrayTemplate = null;
    var gadgetDrop = null;
    var gadgetDropFeedback = null;
    var dashboardContainer = null;
    var DASHBOARD_GADGET_MARGIN = 10;
    var DASHBOARD_GADGET_DROP_MIN_HEIGHT = 100;

    /**
     * Setsup the Gadget Tray below the Finder right above the Dashboard
     * Populates Gadget Tray with gadgets that can then be dragged onto
     * the Dashboard.
     */
    function _setupTray()
    {
        // grab UI components
        gadgetTrayExpander = $("#perc-dashboard-gadget-tray-expander");
        gadgetTray = $(".perc-dashboard-gadget-tray");
        gadgetTrayList = $("#perc-dashboard-gadget-list");
        gadgetTrayExpanderIcon = $(".perc-tray-expander-icon");
        gadgetTrayTemplate = $("#perc-tray-gadget-template");
        gadgetDrop = $("#perc-dashboard-gadget-drop");
        gadgetDropFeedback = gadgetDrop.find(".perc-dashboard-gadget-highlight");
        dashboardColumns = $(".perc-gadget-column");
        dashboardContainer = $(".perc-dashboard-container");

        expandGadgetTray();
        $(document).off("change",".perc-gadget-type", function()
        {
            loadGadgetListing(function()
            {
                populateTrayGadgets(function()
                {
                    fixBottomHeight();
                });
            });
        });

        $(document).on("change",".perc-gadget-category",filterGadgetLibrary);

        populateTrayGadgets(function()
        {
            collapseGadgetTray();
            fixBottomHeight();
        });

        gadgetTrayExpander.on("click",function()
        {
            toggleGadgetTray();
            fixBottomHeight();
        });
    }

    /**
     * Gets the list of gadgets from the server and adds them to the tray.
     * Calls on makeTrayGadgetDraggable to make each tray gadget draggable
     * Retrieves list of gadgets from the server as a JSON object with the following structure:
     * {"Gadget":[  {"description":"description 1","iconUrl":"/url/to/image1.png","name":"Gadget 1","url":"/url/to/gadget1.xml"},
     *              {"description":"description 2","iconUrl":"/url/to/image2.png","name":"Gadget 2","url":"/url/to/gadget2.xml"}]}
     * @param callback (function) to pass list of gadgets to
     */
    function populateTrayGadgets(callback)
    {
        gadgetTrayList.empty();
        var trayGadgetsJson = gadgetList;
        for (g = 0; g < trayGadgetsJson.length; g++)
        {
            var trayGadgetJson = trayGadgetsJson[g];
            if (!trayGadgetJson.adminOnly || (trayGadgetJson.adminOnly && $.PercNavigationManager.isAdmin()))
            {
                var trayGadget = addGadgetToTray(trayGadgetJson, g);
                makeTrayGadgetDraggable(trayGadget);
            }
        }
        manageCategories();
        if (typeof(callback) == 'function') callback();
    }

    /**
     * Calculate the corresponding categories of the selected type
     */
    function manageCategories()
    {
        var typeSelected = $('.perc-gadget-type').val();
        //Get the predefined categories
        var predefinedCategories = [];
        $('.perc-gadget-category-predefined').text(function(i, text)
        {
            predefinedCategories[i] = text.toLowerCase();
        });

        //Get the custom categories
        var customCategories = [];
        $.each($('.perc-tray-item'), function(i, gadget)
        {
            if (typeof($(gadget).data('gadget').type) != "undefined")
            {
                var type = $(gadget).data('gadget').type;
                if (type.toLowerCase() === "custom")
                {
                    var category = $(gadget).data('gadget').category;
                    if (typeof(category) != "undefined")
                    {
                        $.each(category.split(","), function(index, value)
                        {
                            if (value !== "" && $.inArray(value, customCategories) === -1)
                            {
                                customCategories.push(value);
                            }
                        });
                    }
                }
            }
        });

        //When change the type selected set all category as default
        var categoryFilter = $('.perc-gadget-category');
        categoryFilter.val('all');

        //Determine the options for the category filter
        $('.perc-gadget-category-custom').remove();
        if (typeSelected === "all")
        {
            $('.perc-gadget-category-predefined').show();
            if (customCategories.length > 0)
            {
                categoryFilter.append($('<option />').addClass('perc-gadget-category-custom').val('other').text('Other'));
            }
        }
        if (typeSelected === "percussion" || typeSelected === "community")
        {
            $('.perc-gadget-category-predefined').show();
        }
        if (typeSelected === "custom")
        {
            $('.perc-gadget-category-predefined').hide();
            $.each(customCategories, function(index, value)
            {
                categoryFilter.append($('<option />').addClass('perc-gadget-category-custom').val(value).text(value));
            });
        }
    }

    /**
     * Filter the gadget library by category
     */
    function filterGadgetLibrary()
    {
        $.each($('.perc-tray-item'), function()
        {
            if (containsCategory(this)) $(this).show();
            else $(this).hide();
        });
    }

    /**
     * Check if the gadget belong to the current category selected.
     */
    function containsCategory(gadget)
    {
        var selectedCategory = $('.perc-gadget-category').val();
        var predefinedCategories = [];
        if (selectedCategory === "all")
        {
            return true;
        }
        if (selectedCategory === "other")
        {
            var type = $(gadget).data('gadget').type;
            return (type.toLowerCase() === "custom");
        }
        if (typeof($(gadget).data('gadget').category) == 'undefined')
        {
            return false;
        }
        var category = $(gadget).data('gadget').category.split(",");
        var predefinedCategories = [];
        $('.perc-gadget-category-predefined').text(function(i, text)
        {
            predefinedCategories[i] = text.toLowerCase();
        });
        return ($.inArray(selectedCategory, category) !== -1);
    }

    /**
     * Adds JSON representation of a tray gadget to the tray as a new DOM element.
     * Clones a hidden tray gadget DOM element to create each new instance.
     * Copies the gadget URL and name from the JSON representation as attributes of the DOM element.
     * Dynamically sets the icon image and text label from the JSON object.
     *
     * @param trayGadgetJson (JSON Object) JSON from server representing a single gadget in the tray
     * {description : "description", iconUrl : "/url/to/image.png", name : "Name of gadget", url : "/url/to/gadgets.xml"}
     */
    function addGadgetToTray(trayGadgetJson)
    {
        var iconUrl = trayGadgetJson.iconUrl;
        var name = trayGadgetJson.name;
        var title = trayGadgetJson.name.toUpperCase() + " - " + trayGadgetJson.description;
        var url = trayGadgetJson.url;

        var gadgetTrayItem = gadgetTrayTemplate.clone();
        var id = "perc-tray-gadget-" + g;

        //Include the current protocol into the background URL as a workaround for IE 8, see detail at http://support.microsoft.com/kb/925014/en-us?fr=1
        var location = window.location;
        iconUrl = location.protocol + "//" + location.host + iconUrl;
        gadgetTrayItem.attr("id", id).attr("name", name).attr("title", title).attr("url", url).data('gadget', trayGadgetJson).css("display", "inline-block").find(".perc-tray-item-icon").css("background", "url(" + iconUrl + ")");

        gadgetTrayItem.find(".perc-tray-item-label div").attr("name", trayGadgetJson.name).html(name);

        gadgetTrayList.append(gadgetTrayItem);

        //var element = $(".perc-tray-item .perc-tray-item-label div");
        //$.PercTextOverflow(element, 95);

        return gadgetTrayItem;
    }

    /**
     * Bind draggable gesture to a trayGadget DOM element.
     * Add gadget drop areas when dragging begins.
     * Remove drop areas when dragging stops.
     * @param trayGadget (jQuery DOM element) tray gadget that has been added to the tray from JSON object retrieved
     * from server
     */
    function makeTrayGadgetDraggable(trayGadget)
    {
        trayGadget.draggable(
            {
                refreshPositions: true,
                helper: 'clone',
                start: function(event, ui)
                {
                    addGadgetDropAreas();
                    dashboardContainer.percAutoScroll(
                        {
                            width: 10,
                            speed: 5,
                            directions: 'n, s'
                        });
                },
                stop: function(event, ui)
                {
                    removeGadgetDropAreas();
                    dashboardContainer.percAutoScroll.remove();
                }
            });
    }

    /**
     * Returns the gadget position based on the dom elements.
     * @param instanceId, the gadget instanceid assumed not null
     * @return An object consisting of row and col. providing the row and column of the gadget.
     * Usage: var pos = getGadgetPosition(2);
     * pos.row -- Gives the row
     * pos.col -- gives the col.
     */
    function getGadgetPosition(instanceId)
    {
        var colId = $("#gid_" + instanceId).parent().attr("id");
        var col = colId.substring(4);
        var gadgetArray = $("#" + colId).find(".perc-gadget");
        var row = $.inArray($("#gid_" + instanceId)[0], gadgetArray);
        return {
            "row": row,
            "col": col
        };
    }

    function getDashboardMaxHeight()
    {
        var maxHeight = -1;
        dashboardColumns.each(function()
        {
            var dashboardColumn = $(this);
            if (dashboardColumn.height() > maxHeight) maxHeight = dashboardColumn.height();
        });
        return maxHeight;
    }

    /**
     * Removes all drop areas that had been added when starting to drag tray gadgets
     */
    function removeGadgetDropAreas()
    {
        $(".perc-dashboard-gadget-drop").remove();
    }

    /**
     * For each gadget on the dashboard, clones a hidden gadget drop element and adds it on top of the gadget to
     * receive drop gestures to add new gadgets from the tray.
     * Dropping tray gadgets on a dashboard gadget adds a new dashboard gadget to the top of the existing dashboard gadget.
     * Calculates position, height, width, column and row index based on each of the existing dashboard gadgets.
     * Adds an additional drop area under every last (bottom) dashboard gadget to append gadgets at the bottom.
     * Relies on addGadgetDropArea() to actually add the drop area to the DOM.
     */
    function addGadgetDropAreas()
    {
        var dashboardGadgets = $(".perc-gadget[perc-helper!=true]");

        // if the dashboard columns are empty, add an additional drop area at the top of the column
        var columnIndex = 0;
        dashboardColumns.each(function()
        {
            var dashboardColumn = $(this);
            var columnGadgets = dashboardColumn.find(".perc-gadget");
            if (columnGadgets.length === 0)
            {
                var dashboardColumnPosition = dashboardColumn.position();
                var dashboardContainPosition = dashboardContainer.position();
                addGadgetDropArea(null, columnIndex, 0, dashboardColumnPosition.left, dashboardContainPosition.top, dashboardColumn.width() - DASHBOARD_GADGET_MARGIN, dashboardContainer.height());
            }
            columnIndex++;
        });

        dashboardGadgets.each(function()
        {
            var dashboardGadget = $(this);
            var gadgetDropArea = addGadgetDropArea(dashboardGadget);

            // if this is the last gadget, add an additional drop area below it
            if (isLastGadget(dashboardGadget))
            {
                var rowIndex = gadgetDropArea.data("rowIndex") + 1;
                // the top of the drop area will be under the last gadget
                var top = gadgetDropArea.data("height") + gadgetDropArea.data("top") + DASHBOARD_GADGET_MARGIN;
                addGadgetDropArea(dashboardGadget, null, rowIndex, null, top, null, dashboardContainer.height());
            }
        });
    }

    /**
     * Adds the gadgetDropArea created in addGadgetDropAreas to the dashboard sizing it.
     * If no sizes and positions are provided they are inferred from the dashboardGadget.
     * If the dashboardGadget is null, then you must provide the positions and sizes.
     * @param dashboardGadget (jQuery DOM element) gadget in dashboard where we are adding a drop area on.
     * If null, you should provide the positions and sizes
     * @param columnIndex (integer)
     * @param rowIndex (integer)
     * @param left (integer) pixel position location of drop zone from left
     * @param top (integer) pixel position location of drop zone from top
     * @param width (integer) pixel width size of drop zone
     * @param height (integer) pixel width size of drop zone
     */
    function addGadgetDropArea(dashboardGadget, columnIndex, rowIndex, left, top, width, height)
    {
        // you have to at least give me the dashboardGadget or the sizes
        if (dashboardGadget === undefined && (width === undefined || height === undefined)) throw "Exception in PercDashboard.addGadgetDropArea(): Unable to calculate Gadget Drop Area size. " + "At least a Dashboard Gadget or sizes are needed to add a gadget drop area. " + "Neither have been provided";

        // if you dont give me a dashboardGadget, then you have to give me position
        if (dashboardGadget === undefined && (left === undefined || top === undefined || columnIndex === undefined || rowIndex === undefined)) throw "Exception in PercDashboard.addGadgetDropArea(): Unable to calculate Gadget Drop Area location. " + "At least a Dashboard Gadget or a location is needed to add a gadget drop area. " + "Neither have been provided";

        // if I do have a dashboardGadget, try to infer position and size from it,
        // but honor position and size parameters passed in explicitly
        if (dashboardGadget !== undefined)
        {
            var dashboardGadgetPosition = dashboardGadget.position();

            var gadgetParentColumnId = dashboardGadget.parent(".perc-gadget-column").attr("id");
            var columnIndexString = gadgetParentColumnId.replace("col-", "");

            // if passed in honor the parameters otherwise get from dashboard gadget
            top = top === undefined ? dashboardGadgetPosition.top : top;
            left = left === undefined ? dashboardGadgetPosition.left : left;
            columnIndex = columnIndex === undefined ? parseInt(columnIndexString) : columnIndex;
            rowIndex = rowIndex === undefined ? dashboardGadget.prevAll().length : rowIndex;
            width = width === undefined ? dashboardGadget.width() : width;
            height = height === undefined ? dashboardGadget.height() : height;
        }

        var gadgetDropArea = gadgetDrop.clone();
        gadgetDropArea.data("top", top);
        gadgetDropArea.data("left", left);
        gadgetDropArea.data("width", width);
        gadgetDropArea.data("height", height);
        gadgetDropArea.data("columnIndex", columnIndex);
        gadgetDropArea.data("rowIndex", rowIndex);

        if (rowIndex === 0 && dashboardContainer.scrollTop() === 0)
        {
            $("body").append(gadgetDropArea);
            top = dashboardContainer.position().top;
        }
        else
        {
            top += dashboardContainer.scrollTop();
            dashboardContainer.append(gadgetDropArea);
        }

        var feedback = gadgetDropArea.children(".perc-dashboard-gadget-highlight");
        feedback.width(0);

        gadgetDropArea.attr("column", columnIndex).attr("row", rowIndex).width(width).height(height).css("top", top - DASHBOARD_GADGET_MARGIN + 1).css("left", left).addClass("perc-dashboard-gadget-drop").show().droppable(
            {
                tolerance: "pointer",
                over: function(event, ui)
                {
                    feedback.show();
                    if (isFeedbackVisible(feedback, rowIndex)) feedback.width(width);
                },
                out: function()
                {
                    feedback.width(0).hide();
                },
                drop: function(event, ui)
                {
                    var columnIndex = $(this).attr("column");
                    var rowIndex = $(this).attr("row");
                    if (ui.helper.hasClass("perc-gadget"))
                    {
                        var instanceid = ui.draggable.attr("instanceid");
                        var origPos = ui.helper.data("origPos");
                        if (origPos && origPos.col === columnIndex && origPos.row === rowIndex) return false;
                        moveGadget(instanceid, columnIndex, rowIndex);
                    }
                    else if (ui.helper.hasClass("perc-tray-item"))
                    {
                        var gadgetName = ui.draggable.attr("name");
                        var gadgetUrl = ui.draggable.attr("url");
                        addGadgetToDashboard(gadgetName, gadgetUrl, columnIndex, rowIndex, feedback);
                    }
                    feedback.hide();
                }
            });

        return gadgetDropArea;
    }

    /**
     * Makes the dashboard gadgets draggable.
     */
    function makeDashboardGadgetsDraggable(gadget)
    {
        gadget.draggable(
            {
                refreshPositions: true,
                delay: dragDelay,
                helper: function()
                {
                    var helper = $(this).clone();
                    helper.css("width", $(this).width());
                    helper.find(".gadgets-gadget-content").empty();
                    helper.data("origPos", getGadgetPosition(gadget.attr("instanceid")));
                    helper.attr("perc-helper", "true");
                    return helper;
                },
                handle: '.gadgets-gadget-title-bar',
                start: function(event, ui)
                {
                    addGadgetDropAreas();
                },
                stop: function(event, ui)
                {
                    removeGadgetDropAreas();
                    getWidthOfCol();
                    handleOverflowHeader($(this));
                }
            });
    }

    function isFirstGadget(dashboardGadget)
    {
        return dashboardGadget.prevAll(".perc-gadget[perc-helper!=true]").length === 0;
    }

    /**
     * Return true if dashboardGadget does not have any siblings after it
     * @param dashboardGadget (jQuery DOM element) representing dashboard gadget
     */
    function isLastGadget(dashboardGadget)
    {
        return dashboardGadget.nextAll(".perc-gadget[perc-helper!=true]").length === 0;
    }

    /**
     * If the feedback was visible, then add the gadget to the column and row where the drop occurred
     * @param gadgetName (string)
     * @param gadgetUrl (string)
     * @param columnIndex (integer) 0 based column where the drop occurred
     * @param rowIndex (integer) 0 based row where drop occurred
     * @param feedback (jQuery DOM element) represents the drop area where the drop occurred
     */
    function addGadgetToDashboard(gadgetName, gadgetUrl, columnIndex, rowIndex, feedback)
    {
        if (isFeedbackVisible(feedback, rowIndex)) addGadget(gadgetUrl, columnIndex, rowIndex);
    }

    /**
     * Returns true if the top of the drop area is not below tray.
     * This is to avoid drops when the dashboard has been scrolled and part of it is under the tray and finder.
     * @param feedback (jQuery DOM element) represents the drop area where the drop occurred
     */
    function isFeedbackVisible(feedback, rowIndex)
    {
        if (rowIndex === 0) if (dashboardContainer.scrollTop() === 0) return true;
        else return false;
        var dashboardContainerPosition = dashboardContainer.position();
        var feedbackPosition = feedback.offset();
        if (feedbackPosition.top > dashboardContainerPosition.top - DASHBOARD_GADGET_MARGIN) return true;
        return false;
    }

    /**
     * Shows or hides the gadget tray if it is hidden or visible respectively
     */
    function toggleGadgetTray()
    {
        if (gadgetTrayList.is(":visible"))
        {
            collapseGadgetTray();
        }
        else
        {
            expandGadgetTray();
        }
    }

    function notify_resize () {
        fixBottomHeight();
        fixIframeHeight();
    }

    /**
     * Changes the gadget tray CSS to show the tray expanded and shows the tray
     */
    function expandGadgetTray()
    {
        gadgetTrayExpander.removeClass("perc-tray-expander-collapsed").addClass("perc-tray-expander-expanded");
        gadgetTray.addClass('ui-active');
        $('.perc-dashboard-gadget-list-container').slideDown(notify_resize);
    }

    /**
     * Changes the gadget tray CSS to show the tray collapsed and hides the tray
     */
    function collapseGadgetTray()
    {
        gadgetTrayExpander.removeClass("perc-tray-expander-expanded").addClass("perc-tray-expander-collapsed");
        gadgetTray.removeClass('ui-active');
        $('.perc-dashboard-gadget-list-container').slideUp(notify_resize);
    }

    function disableTextSelectionInIE()
    {
        $(".perc-gadget").each(function()
        {
            var gadget = $(this);
            gadget[0].onselectstart = function()
            {
                return false;
            };
        });
    }

    /**
     * Set the title for Gaget header
     */
    function setHeaderTitle()
    {
        $('.perc-ellipsis').each(function()
        {
            $(this).attr('title', $(this).text());
        });
    }

    /**
     * Set the width for dashboadr table in pixels value
     */
    function getWidthOfCol()
    {
        var tableWidth = $(window).width() - 27;
        $("#col-0").css("width", tableWidth * .33);
        $("#col-1").css("width", tableWidth * .67);
    }

    /**
     * Apply ellipsis if Header title is too long
     */
    function handleOverflowHeader(element)
    {
        return element.attr("title") === '';
    }

})(jQuery);