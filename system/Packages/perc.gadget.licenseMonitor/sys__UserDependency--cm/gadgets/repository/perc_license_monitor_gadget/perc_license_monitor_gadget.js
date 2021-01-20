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
 * Implements License Monitor Gadget.
 */
(function($)
{
    var dialog,
        miniMsg,
        loadingMsg,
        isUserAdmin,
        wasRefreshClicked = false;

    var constants = {
        LOADING_CONTENT_CLASS: 'loading-content',
        FIELDNAMES: {
            COMPANY: 'company',
            LICENSE_STATUS: 'licenseStatus',
            LICENSE_TYPE: 'licenseType',
            LICENSE_ID: 'licenseId',
            STATUS: 'status',
            CURRENTLIVESITES: 'currentSites',
            CURRENTLIVEPAGES: 'currentPages',
            MAXLIVESITES: 'maxSites',
            MAXLIVEPAGES: 'maxPages',
            LASTREFRESHDATE: 'lastRefresh',
            ACTIVATION_STATUS: 'activationStatus',
            USAGE_EXCEEDED: 'usageExceeded'
        },
        STATUSCSSCLASSNAMES: {
            Inactive: '',
            Active: 'status-active',
            Warning: 'status-warning',
            Suspended: 'status-suspended'
        }
    };

    $(document).ready(function()
    {
         ///////////////////////////////////////////////////
        // ready.callback Function excecution starts here
        // Show Loading message until we update license status
        loadGadgetContent(false);
        
        isUserAdmin = percJQuery.PercNavigationManager.isAdmin();

        loadGadgetContent(true);
        createResolveButton();
        createRefreshButton();

        if (isUserAdmin)
        {
            showResolveButton(true);
            showRefreshButton(true);
        }
        updateGadgetInfo();

    });

    /**
     * Hide or show the gadget content while showing the "Loading..." message.
     * @param Boolean showContent
     */
    function loadGadgetContent(showContent)
    {
        gadgetContent = $('.gadget-content');
        if (showContent)
        {
            miniMsg.dismissMessage(loadingMsg);
            gadgetContent.removeClass('hidden');
        }
        else
        {
            gadgetContent.addClass('hidden');
            miniMsg = new gadgets.MiniMessage();
            loadingMsg = miniMsg.createStaticMessage("Loading...");
        }
    }

    /**
     * Shows an error message inside the gadget, in case any of the requests that retrieve its
     * status failed.
     * @param Stringg errorMessage
     */
    function gadgetErrorContent(errorMessage)
    {
        if (errorMessage !== undefined)
        {
            $('.error-content').removeClass('hidden').html(errorMessage);
            $('.perc-lmg-basic-info').addClass('hidden');
        }
    }
    
    /**
     * Reveals the Activate or Edit button
     * @param button String the name of the button that we must show
     */
    function showLicenseButton(action)
    {
        var button;
        action = 'createEditButton';
        button = $.perc_license_monitor_dialogs[action](
            refreshGadgetStatus,
            function(){},
            function(){}
        );
        $('.perc-lmg-category-cms').empty().append(button);
    }

    /**
     * Reveals the Activate or Edit button
     * @param button String the name of the button that we must show
     */
    function showModuleLicenseButton(closeCallback, cloudInfo, combinedInfo)
    {
        var button;
        action = 'createModuleLicenseEditButton';
        button = $.perc_license_monitor_dialogs[action](
            closeCallback,
            cloudInfo,
            combinedInfo
        );
        $('.perc-lmg-category-ml').empty().append(button);
    }

    /**
     * Creates the Resolve button and appends it to the Alerts section
     */
    function createResolveButton()
    {
        var resolveButton = $.perc_license_monitor_dialogs.createResolveButton();
        $('.perc-lmg-alert-info').append(resolveButton);
    }

    /**
     * Shows or hides the Resolve button accordingly.
     * @param Boolean flag show or hide the Resolve button
     */
    function showResolveButton(flag)
    {
        // TODO: If the button was an independent component, we should not know its class
        var button = $('.perc-lmg-alert-info').find('.perc-lmg-resolve');
        if (flag)
        {
            button.removeClass('hidden');
        }
        else
        {
            button.addClass('hidden');
        }
    }

    /**
     * The refresh gadget callback function.
     * @param refresh button element
     */
    function refreshGadgetStatus(event)
    {
        // Change the appearance, show the loading message
        var refreshButton = $('.perc-lmg-refresh').addClass('disabled'),
            loadingMsg = $('.perc-lmg-refresh-info').find('.loading').removeClass('nodisplay'),
            lastRefreshDate = $('.perc-lmg-refresh-info').find('.last-refresh-date');

        /**
         * Update the gadget information after a successful refresh.
         */
        function beforeRefresh()
        {
            // Return the refresh button to the original state
            refreshButton.removeClass('disabled');
            loadingMsg.addClass('nodisplay');
            wasRefreshClicked = false;
        }
        
        /**
         * If something went wrong, show the error message in an alert dialog
         */
        function onAbortGetStatus(status)
        {
            revealDateIfNotEmpty();
            percJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.gadgets.licenseMonitor@Error"), content: I18N.message("perc.ui.gadgets.licenseMonitor@License Could Not Be Refreshed")});
            beforeRefresh();
        }

        /**
         * Shows the last refresh date altought the current REFRESH was erroneous (if the hidden
         * date is not empty)
         */
        function revealDateIfNotEmpty()
        {
            if (lastRefreshDate.find('.last-refresh-date-info-span').text() !== '')
            {
                lastRefreshDate.removeClass('nodisplay');
            }
        }

        ///////////////////////////////////////////////////
        // refreshGadgetStatus function excecution starts here
        // Hide the last refresh date until we got the response
        lastRefreshDate.addClass('nodisplay');

        // Prevent multiple request after multiple clicks
        if (wasRefreshClicked)
        {
            return;
        }
        else
        {
            wasRefreshClicked = true;
        }

        return false;
    }

    /**
     * Appends the Refresh button and bind its corresponding behavior to the Refresh section.
     */
    function createRefreshButton()
    {
        // Append the Activate button to the refresh information DIV
        var refreshButton = $(`<a href="#" class="perc-lmg-button perc-lmg-refresh hidden" alt="${I18N.message("perc.ui.gadgets.licenseMonitor@Refresh")}">${I18N.message("perc.ui.gadgets.licenseMonitor@Refresh")}</a>`);
        refreshButton.click(refreshGadgetStatus);
        $('.perc-lmg-refresh-info').removeClass('nodisplay').append(refreshButton);
    }

    /**
     * Shows or hides the Refresh button accordingly.
     * @param Boolean flag show or hide the Refresh button
     */
    function showRefreshButton(flag)
    {
        // TODO: If the button was an independent component, we should not know its class
        var button = $('.perc-lmg-refresh-info').find('.perc-lmg-refresh');
        if (flag)
        {
            button.removeClass('hidden');
        }
        else
        {
            button.addClass('hidden');
        }
    }

    /**
     * Updates the gadget information.
     * @param Object licenseStatus
     */
    function updateGadgetInfo(licenseStatus)
    {
        var basicInfo = $('.perc-lmg-basic-info'),
            usageInfo = $('.perc-lmg-current-usage-info'),
            refreshInfo = $('.perc-lmg-refresh-info'),
            isAnyLimitExceeded;

        /**
         * Updates the activate / edit button (for now) depending on the activation status
         */
        function updateGadgetButtons()
        {
            if (isUserAdmin)
            {
                showLicenseButton('edit');
            }
        }

        /**
         * Updates the status icon in the Basic information section taking into account the
         * live sites and pages limits
         */
        function updateStatusIcon()
        {
            var statusIcon = basicInfo.find('.perc-lmg-icon-status');

            // Clear all the CSS classnames of the icon
            for (var i in constants.STATUSCSSCLASSNAMES)
            {
                if (constants.STATUSCSSCLASSNAMES.hasOwnProperty(i))
                {
                    statusIcon.removeClass(constants.STATUSCSSCLASSNAMES[i]);
                }
            }

            // Assign the corresponding className according to the status:
            statusIcon.addClass(calculateStatusIconCssClassName());
        }

        function updateModuleLicenseInformation()
        {
       		$.perc_module_license_manager.getModuleLicensesInfo(function(status, data){
                if(!status){
                    $("#perc-lmg-module-license-info").append($("<div class='perc-module-license-error'/>").text(data));
                    return;
                }
                $("#perc-lmg-module-license-info").empty().append($.perc_module_license_manager.generateLicenseView(data.combinedInfo));

                $("#perc-lmg-module-license-info").addClass("perc-module-licenses-refreshed");
                if (isUserAdmin)
                {
                    showModuleLicenseButton(updateModuleLicenseInformation,data.cloudInfo,data.combinedInfo);
                }
            });
            //render module licenses
            
        }


        /**
         * Updates the Refresh information section accordingly.
         */
        function updateRefreshInformation()
        {
            var refreshDateInfo = refreshInfo.find('.last-refresh-date');

            // Only show the last refresh date if the response came from the server
            if (licenseStatus[constants.FIELDNAMES.LASTREFRESHDATE] !== undefined &&
                licenseStatus[constants.FIELDNAMES.LASTREFRESHDATE] !== '')
            {
                // Parse and format the date
                var lastRefreshDate = $.perc_utils.splitDateTime(licenseStatus[constants.FIELDNAMES.LASTREFRESHDATE]);

                refreshDateInfo.removeClass('nodisplay')
                    .find('span.last-refresh-date-info-span').html(lastRefreshDate.date + ' ' + lastRefreshDate.time);
                    

                // Refresh button will be visible only using the Admin role
                showRefreshButton(isUserAdmin);
            }
            else
            {
                refreshDateInfo.addClass('nodisplay');
            }
        }

        //////////////////////////////////////////////
        // updateGadgetInfo function excecution starts from here
        // Hide error message if its in the gadget
        updateGadgetButtons();
        gadgetErrorContent();
        updateModuleLicenseInformation();
        //updateRefreshInformation();
    }

    /**
     * Ivokes the corresponding service with the corresponding callback.
     * @param String service name
     * @param okCallback
     * @param errorCallback
     * @param commonCallback
     * @param additionalServiceParam (only allows one because the services are simple)
     * @param abortCallback (function to call when request times out)
     */
    function invokeLicenseService(serviceName, okCallback, errorCallback, commonCallback, serviceParam, abortCallback)
    {
        $.PercLicenseService[serviceName](function(status, data)
        {
            if (status === $.PercServiceUtils.STATUS_ABORT)
            {
                if(abortCallback !== undefined)
                {
                    abortCallback(status);
                }
                return;
            }
            
            if (commonCallback !== undefined)
                commonCallback(data);

            if (status === $.PercServiceUtils.STATUS_ERROR)
            {
                errorCallback(data);
            }
            else
            {
                okCallback(data);
            }
        }, serviceParam);
    }

})(jQuery);