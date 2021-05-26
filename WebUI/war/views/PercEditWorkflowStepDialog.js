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
 * PercEditWorkflowStepDialog.js
 * Generates the New/Edit step dialog. Takes three parameters WorkflowName,previousStepName, currentStepName.
 * 
 */
(function($)
{
    $.PercEditWorkflowStepDialog = function()
    {    
        //Document the parameters...
        var dialogApi = {
            "open":open,
            "tempPossiblePermissions": [I18N.message("perc.ui.edit.workflow.step.dialog@Submit"), I18N.message("perc.ui.edit.workflow.step.dialog@Reject"), I18N.message("perc.ui.edit.workflow.step.dialog@Approve"), I18N.message("perc.ui.edit.workflow.step.dialog@Publish"), I18N.message("perc.ui.edit.workflow.step.dialog@Notify")]
        };
        return dialogApi;
        
        var utils = $.perc_utils;
        function open(isReservedStep, isUpdate, workflowStep, workflowName, previousStepName, currentStep, successCallBack, cancelCallBack)
        {
            //Remove existent DIVs to fix issues with ESC key  
            $("#perc-edit-workflow-dialog").remove();
            $("#perc-workflow-addstep-dialog").remove();
            
            var dialog;
            var className = isReservedStep ? 'perc-step-readonly' : 'perc-step-editable';
            var isreadOnly = isReservedStep ? 'readonly = ""' : '';
            //TODO:I18N Dailog Title Below
            var dialogTitle = currentStep?I18N.message("perc.ui.workflow.steps.view@Configure Step"):I18N.message("perc.ui.workflow.steps.view@New Step");
            var buttons = {};
            dialog = $(" <div id='perc-workflow-addstep-dialog'>" +
                     "     <div id='perc-wfconfig-wrapper'>" +
                     "         <span style='position: relative; float: right; margin-top: -28px;'><label>" +I18N.message("perc.ui.general@Denotes Required Field") + "</label></span>" +
                     "         <div class = 'perc-required-field'>" +I18N.message("perc.ui.edit.workflow.step.dialog@Step Name") + "<br/><input maxlength = '50' name='perc-wfstep-name' id='perc-wfstep-name' class = '" +
                     className +
                     "' type='text'" +
                     isreadOnly +
                     "/></div>" +
                     "         <span style='color:red'></span>" +
                     "         <div><div style='font-weight:bold; margin-bottom:5px'>" +I18N.message("perc.ui.edit.workflow.step.dialog@Roles") + "</div>" +
                     "         <div >" +I18N.message("perc.ui.edit.workflow.step.dialog@Specify Permissions") + "</div><br/>" +
                     "         <div id='perc-wfconfig-roles-wrapper' style='background:#fff'>" +
                     "  </div></div></div>" +
                     "  <div class='ui-layout-south'>" +
                     "    <div id='perc_buttons' style='z-index: 100;'></div>" +
                     "  </div></div>"
                    )
                    .perc_dialog(
                    {
                        buttons: buttons,
                        percButtons:
                        {
                            "Save":
                            {
                                click: function()
                                {
                                    _save();
                                },
                                id:"perc-wfstep-save"
                            },
                            "Cancel":
                            {
                                click: function()
                                {
                                    _remove();
                                    cancelCallBack();
                                },
                                id:"perc-wfstep-cancel"
                            }
                        },
                        id: "perc-edit-workflow-dialog",
                        width: 620,
                        height:585,
                        title: dialogTitle,
                        modal: true
                    });
                    
            function _save(){
            $("#perc-wfstep-save").off();
                saveData(isUpdate, workflowName, workflowStep, previousStepName, function(){
                    _remove();  
                    successCallBack();
                });
            }

            function _remove()
            {
                dialog.remove();
            } 

            renderDialogContent(isUpdate, workflowStep);
        
        
        // Render the content of the dialog
        function renderDialogContent(isUpdate, workflowStep) {       
          $.PercUserService.getRoles(function(status, rolesJson) {
                if(status === $.PercServiceUtils.STATUS_ERROR) {
                    utils.alertDialog(I18N.message("perc.ui.edit.workflow.step.dialog@Error Loading Roles"), rolesJson);
                    return;
                }
                renderRoles(rolesJson, isUpdate, workflowStep);
            });
        }
        
        // Loop through the roles
        function renderRoles(rolesJson, isUpdate, workflowStep){
            
            var roleListObject = rolesJson;    
            var roleTable = $('<table id = "perc-wfconfig-roles-table"></table>');
            var roleTableBody = $('<tbody></tbody>');
            var roleHeaderRow = $('<tr class="perc-wfconfig-row-header"></tr>');
            $(roleHeaderRow).attr('class',generateRowId(0));            
            var roleHeader = $('<th align="left">Roles</th>');
            $(roleHeader).attr('class',generateColumnId(0));
            $(roleHeaderRow).append(roleHeader);
            var stepPermissions = workflowStep.permissionNames;
            var tempNotify = ["Notify"];
            if(!Array.isArray(stepPermissions))
            {
                var tempArray = [];
                tempArray.push(stepPermissions);
                stepPermissions = tempArray;
            }
            var possiblePermissions;
            if(isUpdate)
                possiblePermissions = stepPermissions.concat(tempNotify);
            else              
                possiblePermissions = $.PercEditWorkflowStepDialog().tempPossiblePermissions;
                
            $.each(possiblePermissions, function(index){
                var roleHeader = $('<th>' + this + '</th>');
                $(roleHeader).attr('class',generateColumnId(index+1));
                $(roleHeaderRow).append(roleHeader);                
            });

            $(roleTableBody).append(roleHeaderRow);       
            
            // Loop through the available Roles and generate that many table rows with first column dedicated to Role name in each row.
            for (var i=0; i < roleListObject.RoleList.roles.length; i++) {
                var roleRow = $('<tr class="perc-wfconfig-row"></tr>');
                roleRow.addClass(generateRowId(i+1));
                var roleNameElement=$('<td align:"left"><span class = "perc-roleName-wrapper perc-ellipsis"></span></td>');
                $(roleNameElement).addClass(generateColumnId(0));
                var roleName = roleListObject.RoleList.roles[i];
                roleRow.attr("data", roleName);
                var isReadOnly = isReadOnlyRole(roleName);
    
                $(roleNameElement).find('span').append(roleName).attr('title', roleName );            
                $(roleRow).append(roleNameElement);
                roleRow.data("roleName",roleName);
                
                for(var j=0; j<possiblePermissions.length; j++){
                    var roleRowElement = $('<td></td>');
                    var checkbox;
                    // Check and disabled the Submit, Reject and Approve transistions for Admin and Designer roles
                    if(isReadOnly && j!== possiblePermissions.length - 1 ){
                        checkbox = $('<input type="checkbox" checked disabled />');
                    }else{
                        checkbox = $('<input type="checkbox" />');
                    }
                    
                    // Keep the Notify box for Admin enabled and uncheck by default while disabled and uncheck for all other Roles.
                    if((possiblePermissions[j] === "Notify" || possiblePermissions[j] === "Publish") && !isReadOnly) {
                       $(checkbox).prop('disabled', true);
                    }
                    //Bind the click even to the checkboxes
                    $(checkbox).on("click", function(evt) {
                        updateNotification($(this), $(this).parent().parent());
                    });
                    $(roleRowElement).append(checkbox);
                    $(roleRowElement).prop('class', generateColumnId(j+1));
                    $(roleRowElement).prop('class', 'perc-'+ possiblePermissions[j].toLowerCase());
                    $(roleRow).append(roleRowElement);
                }
                $(roleTableBody).append(roleRow);
            }
            $(roleTable).append(roleTableBody);
            $("#perc-wfconfig-roles-wrapper").append(roleTable);
            
            //Update the permissions
            
            if(isUpdate)
            {
                updatePermissions(workflowStep);
                $("#perc-wfstep-name").trigger("blur");
            }
            
            // Bind the click event to check-all/un-check-all input.
            
            $('.perc-checkall').on("change",function() {
            var inputClass = ".perc-" + $(this).attr('data') + " input:not(:disabled)";
                if ($(this).attr("checked")) {
                    $("#perc-wfconfig-roles-table").find(inputClass).attr('checked', 'checked');
                }
                else {
                    $("#perc-wfconfig-roles-table").find(inputClass).attr('checked', false);
                }                
            });
        }
        
        //Loop through all the available roles and step roles and update each permission check box based on notify status value.    
        function updatePermissions(workflowStep) {
            $("#perc-wfstep-name").val(workflowStep.stepName);
            var stepRoles = workflowStep.stepRoles;
            if(!Array.isArray(stepRoles))
            {
                var tempArray = [];
                tempArray.push(stepRoles);
                stepRoles = tempArray;
            }
            $.each(stepRoles, function() {
                var roleName = $(this)[0].roleName;             
                var roleTransitions = $(this)[0].roleTransitions;
                var notifyStatus = $(this)[0].enableNotification;                
                matchRoleName(roleName, roleTransitions, notifyStatus);
            });
        }
        
        
        //Load all the available roles in step configure dialog
        function matchRoleName(roleName, roleTransitions, notifyStatus)
        {
            //Find the row directly rather than looping
            $(".perc-wfconfig-row").each(function() {
                var row = this;
                if($(row).find('td.perc-column-0').text() === roleName) {
                    matchPermissions(roleName, notifyStatus, roleTransitions, row);
                    return;
                }
            });
        }
        
        // Update the permissions (check/uncheck) for each role based on 'notifystatus' value
        function matchPermissions(roleName, notifyStatus, roleTransitions, row) {
            var isReadOnly = isReadOnlyRole(roleName);
            $(row).find('td:not(".perc-column-0")').each(function(){
                var column = this;
                var className = $(column).attr('class');
                className = className.replace('perc-','');
                if(!Array.isArray(roleTransitions))
                {
                    var tempArray = [];
                    tempArray.push(roleTransitions);
                    roleTransitions = tempArray;
                }
                for (let i=0; i<roleTransitions.length; i++) {
                    var tempName = roleTransitions[i];
                    tempName = tempName.transitionPermission.toLowerCase();
                    if(className === tempName){
                        $(column).find('input').attr('checked','checked');
                        if(tempName === "publish" && !isReadOnly) {
                            $(column).find('input').removeAttr('disabled', false);                        
                        }
                        if(tempName === "approve" && !isReadOnly) {
                            $(row).find('td.perc-publish input').removeAttr('disabled', false);                        
                        }
                    }

                }
                var notifyChkBox = $(row).find("td.perc-notify input").removeAttr('disabled', false);
                if(notifyStatus) {
                    notifyChkBox.attr('checked', true);
                }
            });
        }
        
        // Toggle the Notify checkbox based on other possible(Reject, Submit, Approve, Restore etc.) transistion value
        function updateNotification(clickedCheckbox, row) {

            //Enable Publish transistion if Approve transistion is checked. Disable and uncheck Publish transistion if Approve transistion is uncheck.
            if(clickedCheckbox.parent().hasClass('perc-approve')) {
                if(clickedCheckbox.is(':checked')) {
                    $(row).find(".perc-publish input").removeAttr('disabled');
                }
                else {                    
                    $(row).find(".perc-publish input").removeAttr('checked').attr('disabled', true);
                }
            }
            if($(row).find("td:not('.perc-notify') input:checked").length > 0){
                $(row).find(".perc-notify input").attr('disabled', false);
            }
            else 
            {
                $(row).find(".perc-notify input").removeAttr('checked').attr('disabled', true);
            }
            if(clickedCheckbox.parent().hasClass('perc-publish')&& clickedCheckbox.is(':checked')) {
               $(row).find(".perc-approve input").attr('checked', 'checked');
            }          
        }
        function generateRowId(number){    
            return 'perc-row-'+ number;
        }
        
        function generateColumnId(number){    
            return 'perc-column-'+ number;
        }
        
        function isReadOnlyRole(roleName){
            return roleName === "Admin" || roleName === "Designer";
        }
            
        // On click of 'Save' button, build the step object and send it to server. 'SuccessCallback' will refresh the step container.
        function saveData(isUpdate, workflowName, workflowStep, previousStepName, successCallback)
        {
            var stepName = $("#perc-wfstep-name").val().trim();
            
            if(stepName.indexOf("??") !== -1) {
                var validationError = I18N.message("perc.ui.workflow.steps.view@Invalid Character Sequence Quesitonmarks") + "'??'.";
                $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.workflow.view@Workflow Validation Error"),"content":validationError});
                return;
            }
            
            //Lopp through the rows and build the step roles object with permissions
            var stepRoles = [];
            if(isUpdate)
                var possiblePermissions = workflowStep.permissionNames;
            else
                var possiblePermissions = $.PercEditWorkflowStepDialog().tempPossiblePermissions;
            $(".perc-wfconfig-row").each(function(){
                var roleRow = $(this);
                var roleName = roleRow.data("roleName");
                var rolePermissions = [];
                var notifyStatus = false;
                $.each(possiblePermissions, function(){
                    var permName = this+"";
                    if(roleRow.find(".perc-" + permName.toLowerCase() + " :checked").length > 0){
                        var transition ={"transitionPermission" : permName};
                        rolePermissions.push(transition);
                    }
                });
                if(roleRow.find(".perc-notify input").is(':checked')) {
                    notifyStatus = true;
                }
                
                if(rolePermissions.length > 0)
                {
                    var stepRole = {"enableNotification":notifyStatus, "roleName":roleName,"roleTransitions":rolePermissions};
                    stepRoles.push(stepRole);
                }
            });            
            var stepObj = {
                            "Workflow": {
                                    "workflowName" : workflowName,
                                    "previousStepName" : previousStepName,
                                    "workflowSteps":[{
                                                "stepName":$("#perc-wfstep-name").val().trim(),
                                                "stepRoles":stepRoles
                                    }]
                            }
            };
            var stepName = $("#perc-wfstep-name").val().trim();
            $.PercBlockUI();
            //call the service to update the step data if 'isUpdate' is true
            if(isUpdate) {

                 $.PercWorkflowService().updateWorkflowStep(workflowName, previousStepName, stepObj, function(status, result) {
                    if(status)
                    {
                        $("#perc-wfstep-save").on("click", function(evt) {
                            _save();
                        });
                        successCallback();
                    }
                    else
                    {
                        $("#perc-wfstep-save").on("click", function(evt) {
                            _save();
                        });
                        var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                        $.perc_utils.alert_dialog({"title":"Error Updating Step","content":errorMessage});
                    }
                    $.unblockUI();
                });
            }
            
            else {
                //call the service to save and create the new step if 'isUpdate' is false.
                $.PercWorkflowService().createNewWorkflowStep(workflowName, stepName, stepObj, function(status, result) {
                    if(status)
                    {
                        $("#perc-wfstep-save").on("click",function(evt) {
                            _save();
                        });
                        successCallback();
                    }
                    else
                    {
                        $("#perc-wfstep-save").on("click", function(evt) {
                            _save();
                        });
                        var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                        $.perc_utils.alert_dialog({"title":"Error Creating Step","content":errorMessage});
                    }
                    $.unblockUI();
                });
                        
            }
        }
        }
    };
})(jQuery);
