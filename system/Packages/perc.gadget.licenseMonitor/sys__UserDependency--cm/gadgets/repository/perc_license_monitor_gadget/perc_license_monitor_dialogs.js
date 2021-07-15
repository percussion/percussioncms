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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

(function($) {
    // Module public API
    $.perc_license_monitor_dialogs = {
        createActivateButton: createActivateButton,
        createEditButton: createEditButton,
        createResolveButton: createResolveButton,
        setServerUUID: setServerUUID,
        createModuleLicenseEditButton : createModuleLicenseEditButton
    };

    // TODO: Remove unused constants here: check changelist 61698
    var constants = {
            DIALOG_ACTIVATE_ID: 'perc-lmg-activation-dialog',
            DIALOG_EDIT_ID: 'perc-lmg-edit-dialog',
            DIALOG_MODULE_ID:'perc-module-lic-edit-dialog',
            REGISTER_METHOD_NAME: 'REGISTER_URL'
        },
        serverUUID,
        registrationAppUrl,
        onDialogCloseCallback,
        onSuccessCallback,
        onErrorCallback,
        cmsActivationDialog;

    function setServerUUID(_serverUUID)
    {
        serverUUID = _serverUUID;
    }
    /**
     * Returns the HTML button element that wil open the Activation dialog.
     * @param callback function to invoke when the Cancel button is clicked
     * @param callback function to invoke when the activation is successful
     * @param callback function to invoke when the activation is erroneous
     */
    function createActivateButton(_onDialogCloseCallback, _onSuccessCallback, _onErrorCallback)
    {
        onDialogCloseCallback = _onDialogCloseCallback;
        onSuccessCallback = _onSuccessCallback;
        onErrorCallback = _onErrorCallback;
        var activateButton = percJQuery(`<a href="#" class="perc-lmg-activate" alt="${I18N.message("perc.ui.gadgets.licenseMonitor@Activate")}">${I18N.message("perc.ui.gadgets.licenseMonitor@Activate")}</a>`).on("click",function(event)
        {
            event.preventDefault();
            _createActivationDialog(_onDialogCloseCallback, _onSuccessCallback, _onErrorCallback, false);
        });
        
        return activateButton;
    }

    /**
     * Creates the Edit button and bind the Edit dialog opening.}
     * @param callback function to invoke when the Cancel button is clicked
     * @param callback function to invoke when the edit is successful
     * @param callback function to invoke when the edit is erroneous
     */
    function createEditButton(_onDialogCloseCallback, _onSuccessCallback, _onErrorCallback)
    {
        var editButton = percJQuery(`<a class="perc-lmg-edit" alt="${I18N.message("perc.ui.gadgets.licenseMonitor@Edit")}" href="#">${I18N.message("perc.ui.gadgets.licenseMonitor@Edit")}</a>`).on("click",function(event)
        {
            event.preventDefault();
            _createActivationDialog(_onDialogCloseCallback, _onSuccessCallback, _onErrorCallback, true);
        });
        // Append the Edit button to the basic information DIV
        return editButton;
    }

    /**
     * Creates the Edit button and bind the Edit dialog opening.}
     * @param callback function to invoke when the Cancel button is clicked
     * @param callback function to invoke when the edit is successful
     * @param callback function to invoke when the edit is erroneous
     */
    function createModuleLicenseEditButton(onDialogCloseCallback, cloudInfo, combinedInfo)
    {
        var editButton = percJQuery(`<a class="perc-lmg-edit" alt="${I18N.message("perc.ui.gadgets.licenseMonitor@Activate")}" href="#">${I18N.message("perc.ui.gadgets.licenseMonitor@Activate")}</a>`).on("click",function(event)
        {
            event.preventDefault();
            _createModuleLicenseActivationDialog(onDialogCloseCallback, cloudInfo, combinedInfo);
        });
        // Append the Edit button to the basic information DIV
        return editButton;
    }

    /**
     * Creates activation dialog for both edit and new activation.
     */
    function _createActivationDialog(_onDialogCloseCallback, _onSuccessCallback, _onErrorCallback, isEdit){
        onDialogCloseCallback = _onDialogCloseCallback;
        onSuccessCallback = _onSuccessCallback;
        onErrorCallback = _onErrorCallback;
        var infoText = I18N.message("perc.ui.gadgets.licenseMonitor@Activation Dialog");
        // Set the basic markup for the dialog and assign it to the module's dialog variable
        var dialogContent;
        dialogContent =  '<div>';
        dialogContent +=     '<div class="perc-lmg-edit-panels-container">';
        dialogContent +=        '<div class="perc-lmg-edit-activation-panel">';
        dialogContent +=            '<div style="margin-bottom:20px;"><label>' + infoText + '</label></div>';
        dialogContent +=             '<form id="perc-lmg-activation-form" action="">';
        dialogContent +=                 '<div>';
        dialogContent +=                     '<label for="perc-lmg-activation-code">' + I18N.message("perc.ui.gadgets.licenseMonitor@Activation code") + ':' + '</label><br/>';
        dialogContent +=                     '<input type="text" name="perc-lmg-activation-code" id="perc-lmg-activation-code" maxlength="255" /><br/>';
        dialogContent +=                     '<label id="perc-lmg-activation-code-error"></label>';
        dialogContent +=                 '</div>';
        dialogContent +=             '</form>';
        dialogContent +=        '</div>';
        dialogContent +=     '</div>';
        dialogContent += '</div>';

        buttons = {};

        buttons[I18N.message("perc.ui.gadgets.licenseMonitor@Activate")] = {
            'id': 'perc-lmg-activation-dialog-activate',
            'click': function() {
                activationForm.trigger('submit');
                onSuccessCallback();
            }
        };

        buttons[I18N.message("perc.ui.gadgets.licenseMonitor@Cancel")] = {
            'id': 'perc-lmg-activation-dialog-cancel',
            'click': function() {
                cmsActivationDialog.remove();
            }
        };

        var dlgTitle = isEdit ? I18N.message("perc.ui.gadgets.licenseMonitor@Edit Activated License") : I18N.message("perc.ui.gadgets.licenseMonitor@Activate License");
        cmsActivationDialog = percJQuery(dialogContent).perc_dialog({
            'id' : constants.DIALOG_EDIT_ID,
            'width': 343,
            'resizable': false,
            'modal': true,
            'percButtons': buttons,
            'title' : dlgTitle
        });

        var activationForm = percJQuery('#perc-lmg-activation-form');
        // Filter the activation code to accept only URL friendly characters
        percJQuery.perc_filterField(
            activationForm.find('#perc-lmg-activation-code'),
            percJQuery.perc_textFilters.URL
        );

        // Bind the submit event of the form inside the dialog to the same callback used in the
        // Activate button of the dialog
        activationForm.submit(submitActivationCode);
        percJQuery('.perc-lmg-activate').on("click",function(event) {
            activationForm.trigger('submit');
        });
        return false;
    }

    function _createModuleLicenseActivationDialog(_onDialogCloseCallback, cloudInfo, combinedInfo){
        onDialogCloseCallback = _onDialogCloseCallback;
        var dialog;
        var infoText = I18N.message("perc.ui.gadgets.licenseMonitor@Module License Activation Dialog");
        // Set the basic markup for the dialog and assign it to the module's dialog variable
        var dialogContent;
        dialogContent =  '<div>';
        dialogContent +=     '<div class="perc-lmg-edit-panels-container">';
        dialogContent +=        '<div class="perc-lmg-edit-activation-panel">';
        dialogContent +=            '<div style="margin-bottom:20px;"><label>' + infoText + '</label></div>';
        dialogContent +=            '<div id="perc-module-license-display-container">';
        dialogContent +=            '</div>';
        dialogContent +=            '<div id="perc-module-license-activate-container">';
        dialogContent +=            '</div>';
        dialogContent +=        '</div>';
        dialogContent +=     '</div>';
        dialogContent += '</div>';
        dialogContent = $(dialogContent);
        dialogContent.find("#perc-module-license-display-container").append($.perc_module_license_manager.generateLicenseView(combinedInfo));
        dialogContent.find("#perc-module-license-activate-container").append($.perc_module_license_manager.generateLicenseActivatorView(cloudInfo.licenseTypes, activationSuccessCallback));

        buttons = {
            [I18N.message("perc.ui.gadgets.licenseMonitor@Close")] : {
                'id': 'perc-lmg-activation-dialog-cancel',
                'click': function() {
                    dialog.remove();
                    _onDialogCloseCallback();
                }
            }
        };
        var dlgTitle = I18N.message("perc.ui.gadgets.licenseMonitor@Edit Module Licenses");

        dialog = percJQuery(dialogContent).perc_dialog({
            'id' : constants.DIALOG_MODULE_ID,
            'width': 343,
            'resizable': false,
            'modal': true,
            'percButtons': buttons,
            'title' : dlgTitle
        });
        
        function activationSuccessCallback(newCombinedInfo){
            var index = -1;
            for(i=0;i<combinedInfo.length;i++){
                if(combinedInfo[i].name == newCombinedInfo.name){
                    index = i;
                    break;
                }
            }
            combinedInfo[i] = newCombinedInfo;
            dialogContent.find("#perc-module-license-display-container").empty().append($.perc_module_license_manager.generateLicenseView(combinedInfo));
        }
    }
    /**
     * Submits the activation code to the corresponding service.
     * @param event
     */
    function submitActivationCode(event)
    {
        var activationForm = $(event.target),
            errorLabel = activationForm.find('#perc-lmg-activation-code-error'),
            activationCode;

        /**
         * Before submitting the activation code, block the UI
         */
        function beforeActivate(data)
        {
            percJQuery.unblockUI();
        }

        /**
         * If success, perform an activation ignoring the cache (retrieve data directly from
         * Netsuite).
        */
        function onSuccessfulActivate
        (data)
        {
            // Assuming that the request was successful, refresh the status ignoring the cache.
            // If something went wrong show the error message from Netsuite
            if (data.netsuiteResponse.status.toUpperCase() === 'SUCCESS')
            {
                // On successful activation, unblock the UI and close the dialog (or show an error)
                cmsActivationDialog.remove();
                onSuccessCallback();
                onDialogCloseCallback();
            }
            else
            {
                onErroneousActivate(data.netsuiteResponse.message);
            }
        }

        /**
         * If anything bad happened during activation, show the error in a dialog.
         */
        function onErroneousActivate(data)
        {
            percJQuery.perc_utils.alert_dialog({
                title: I18N.message("perc.ui.gadgets.licenseMonitor@Error"),
                content: data
            });
        }
        
        /**
         * If the request has timed out during activation, unblock UI and show the error in a dialog.
         */
        function onAbortedActivate(status)
        {
            percJQuery.perc_utils.alert_dialog({title: I18N.message("perc.ui.gadgets.licenseMonitor@Error"), content: I18N.message("perc.ui.gadgets.licenseMonitor@License Could Not Be Activated")});
            beforeActivate();
        }

        ///////////////////////////////////////////////////
        // submitActivationCode function excecution starts here
        // Retrieve activation code and prevent submit event default behavior
        event.preventDefault();

        // Retrieve the activation code and encode it, because we are appending it to the URL
        activationCode = activationForm.find('#perc-lmg-activation-code').val().trim();
        activationCode = encodeURIComponent(activationCode);
        
        // Hide error message and check that the activationCode field is not empty
        // Error message will appear automattically if something goes wrong
        errorLabel.addClass('perc-hidden');
        if (activationCode === '')
        {
            errorLabel.removeClass('perc-hidden').html(I18N.message("perc.ui.gadgets.licenseMonitor@Activation code field is required"));
            return false;
        }

        // Block the UI until we got a response from the server
        percJQuery.PercBlockUI();
        invokeLicenseService(
            'activate',
            onSuccessfulActivate,
            onErroneousActivate,
            beforeActivate,
            activationCode,
            onAbortedActivate);
        return false;
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

    /**
     * Creates the Edit button and bind the Edit dialog opening.}
     */
    function createResolveButton()
    {
        var resolveDialogTitle = I18N.message("perc.ui.gadgets.licenseMonitor@Resolve Alerts"),
            resolveDialogMessage,
            resolveButton;

        resolveDialogMessage = I18N.message("perc.ui.gadgets.licenseMonitor@Resolve Dialog");

        resolveButton = percJQuery(`<a class="perc-lmg-button perc-lmg-resolve hidden" alt="${I18N.message("perc.ui.gadgets.licenseMonitor@Resolve")}" href="#">${I18N.message("perc.ui.gadgets.licenseMonitor@Resolve")}</a>`).on("click",function(event)
        {
            event.preventDefault();
            percJQuery.perc_utils.alert_dialog({
                id: constants.DIALOG_RESOLVE_ID,
                title: resolveDialogTitle,
                content: resolveDialogMessage
            });
            return false;
        });

        return resolveButton;
    }
    
})(jQuery);
