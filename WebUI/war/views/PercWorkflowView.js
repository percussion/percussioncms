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

/**
 * PercWorkflowView.js
 *
 *-Renders the default workflow (for now)
 *
 *
 */
(function($, P)
{
    $.PercWorkflowView = function()
    {
        var dirtyController = $.PercDirtyController;
        var container = $("#perc-workflows-list");
        var selectedWorkflow = "";
        var originalSitesJson, originalAssetsJson, siteJobId, assetJobId;
        
        //map of workflow names to stagingRoleNames to identify permissions for staging Publish Now
        var stagingRoleNames = new Map();
        var api = {
            init : init,
            showNewWorkflowEditor : showNewWorkflowEditor,
            selectWorflow : selectWorflow,
            cancelJobs : cancelJobs,
            isWorkflowAvailable : true
        };

        // "Constant" of the DIV container for assigned sites/folders tree
        var ID_SITES_FOLDERS = '#perc-workflows-assigned-sites-folders';
        var assign_container = $(ID_SITES_FOLDERS);
        var WORKFLOW_ASSIGNMENT_INPROCESS_MESSAGE = "Your changes were unable to be saved. The system is currently assigning pages and assets to workflow. During this process access to workflow administration is prevented to avoid conflicting changes.  You can check the status of the assignment process using the Process Monitor on the Dashboard.";
        /**
         *  Show new workflow editor
         */
        function showNewWorkflowEditor()
        {
            $(".perc-wf-default").find('input').prop('checked', false).prop('disabled', false);
            hideWorkflowUpdateEditor();
            hideWorkflowEditButton();
            //create the workflow control for publish now to staging permissions
            generateRolesControl("perc-publish-now-roles-control-new",null);
            $("#perc-wf-new-editor").show();
            $("#perc-new-workflow-name").val("").on("focus change", function(evt)
            {
                dirtyController.setDirty(true, "workflow");
            });
        }
        function cancelJobs(sync){
            if (siteJobId) {
                $.perc_utils.info(I18N.message("perc.ui.workflow.view@Cancelling Assigned Site Folders Job"));
                cancelJob(siteJobId, sync);
            }
            if (assetJobId) {
                $.perc_utils.info(I18N.message("perc.ui.workflow.view@Cancelling Assigned Site Folders Job"));
                cancelJob(assetJobId, sync);
            }
        }
        function cancelJob(jobId, sync){
            if (!jobId) 
                return;
            $.perc_utils.info(I18N.message("perc.ui.workflow.view@Cancelling Assigned Folders Job") + jobId);
            var callback = function(status, result){
                if (status == $.PercServiceUtils.STATUS_ERROR) {
                    $.perc_utils.info(result);
                }
                else{
                    $.perc_utils.info(I18N.message("perc.ui.workflow.view@Cancelling Assigned Folders Job") + jobId);
                }
            };
            $.PercFolderService.cancelAssociatedFoldersJob(jobId, sync, callback);
        }
        
        /**
         *  Disable the Add new workflow, delete workflow and add new step buttons
         */
        function disableButtons()
        {
            $.PercDataList.disableButtons(container);
        }

        /**
         *  Enable the save and cancel buttons.
         */
        function enableButtons()
        {
        	//show buttons for updating and creating new workflow
            $("#perc-update-wf-save-cancel-block").show();
            $("#perc-new-wf-save-cancel-block").show();
        }

        /**
         *  Add new workflow
         */
        function addNewWorkflow()
        {

            //wipe out the existing workflow steps
            $("#perc-workflow-steps-container table").html("");
            disableButtons();
            enableButtons();

            //load the new workflow editor
            showNewWorkflowEditor();
            $.PercDataList.disableButtons(container);
            $.PercDataList.unhighlightAllItems(container);
        }

        /**
         *  Edit workflow
         */
        function editWorkflow(evt)
        {
            $(".perc-wf-default").find('input').removeAttr('checked').removeAttr('disabled');   
            $(".perc-step-config-button, .perc-reserved-step-config-bttn, .perc-step-delete-button, .perc-create-new-step").off("click").on("click");
            $(".perc-create-new-step").addClass("perc-step-disable");
            $(".perc-step-delete-button").addClass("perc-step-delete-disable");
            $(".perc-step-config-button").addClass("perc-step-config-disable");
            $(".perc-reserved-step-config-bttn").addClass("perc-reserved-step-config-disable");            
            hideWorkflowEditButton();
            $("#perc-wf-update-editor").show();
            var workflowName = $("#perc-workflow-steps-container").data("workflowName");
            
            //create the workflow control for publish now to staging permissions
            generateRolesControl("perc-publish-now-roles-control",workflowName);
            
            if(defaultWorkflow == workflowName) {
                $(".perc-wf-default").find('input').attr('checked', 'checked').attr('disabled', true);                
            }
            $("#perc-update-workflow-name").val(workflowName).on("focus change", function(evt)
            {
                dirtyController.setDirty(true, "workflow");
            });
            
            $(".perc-wf-default").find('input').on("change", function()
            {
                dirtyController.setDirty(true, "workflow");
            });
            
            $.PercDataList.disableButtons(container);
        }

        /**
         *  Hide the new workflow Editor
         */
        function hideWorkflowEditor()
        {
            $.PercDataList.enableButtons(container);
            $("#perc-workflow-name").val("");
            $("#perc-wf-new-editor").hide();
            dirtyController.setDirty(false);
        }

        /**
         *  Hide the new workflow Editor
         */
        function hideWorkflowUpdateEditor()
        {
            $.PercDataList.enableButtons(container);
            $("#perc-workflow-name").val("");
            $("#perc-wf-update-editor").hide();
            $("#perc-wf-name-wrapper").show();
            dirtyController.setDirty(false);
        }

        /**
         *  Hide worflow name and edit button
         */
        function hideWorkflowEditButton()
        {
            $.PercDataList.enableButtons(container);
            $("#perc-wf-update-editor").hide();
            $("#perc-wf-name-wrapper").hide();
            dirtyController.setDirty(false);
        }

        /**
         *  Delete workflow
         */
        function deleteWorkflow(workflowName)
        {
            var previousWorkflowName =  $(".perc-itemname[title='"+ workflowName+ "']").prev().attr('title');
            if (typeof(previousWorkflowName) == 'undefined')
                previousWorkflowName =  $(".perc-itemname[title='"+ workflowName+ "']").next().attr('title');
            if (previousWorkflowName == 'more')
                previousWorkflowName =  $(".perc-moreLink").prev().attr('title');  
            var settings = {
            id: 'perc-wf-delete',
            title: I18N.message("perc.ui.workflow.view@Confirm Workflow Deletion"),
            type: 'YES_NO',
            question: I18N.message("perc.ui.workflow.view@About To Delete Workflow")+ workflowName + "'<br /><br />" + I18N.message("perc.ui.workflow.view@Are You Sure Delete Workflow"),
            success: function () {
                     $.PercBlockUI();
                     $.PercWorkflowService().deleteWorkflow(workflowName, function(status, result){
                        if(status) {
                            refreshWorkflowContainer(previousWorkflowName);
                        }                
                        else {
                            var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                                                    $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.workflow.view@Error Deleting Workflow"),"content":errorMessage});
                                                    
                        }  
                        $.unblockUI();              
                    }); 
                                 }
        };                    
            $.perc_utils.confirm_dialog(settings);
          
        }
        
        /**
         *  Expand the workflow list if selected workflow is hidden after creating/updating operation.
         */        
        function expandWrokflowList(workflowName)
        {
            var isWfHidden =  container.find(".perc-itemname[title='"+ workflowName+ "']").is('.perc-hidden');
            if($("#perc-wf-min-max").is(".perc-items-maximizer")) {
                $("#perc-wf-min-max").trigger("click");
            }    
            if(isWfHidden) {    
                $("#perc-workflows-list").find('.perc-moreLink').trigger("click");
            }
        }

        /**
         *  Select workflow
         */
        function selectWorflow(workflowName)
        {
            container.find(".perc-itemname[title='"+ defaultWorkflow+ "']").addClass('perc-default-wf-list-marker');
            $(".perc-default-wf-marker").hide();
            var selectingSameWorkflow = workflowName == container.find(".perc-itemname.perc-item-selected").attr("title");
            $("#perc-workflow-steps-container").data("workflowName", workflowName);
            hideWorkflowEditor();
            hideWorkflowUpdateEditor();
            selectedWorkflow = workflowName;
            $.PercDataList.selectItem(container, workflowName);
            $.PercWorkflowStepsView.refresh(workflowName);
            $("#perc-wf-edit").removeClass('perc-wf-edit-disable')
            .off("click")
            .on("click", function(evt)
            {
                editWorkflow();
            });

            previousWorkflowName =  $(".perc-itemname[title='"+ workflowName+ "']").prev().attr('title');

            container.find('.perc-item-delete-button').off("click").on("click", function(evt)
            {
                deleteWorkflow(workflowName);
            });
            
            //Un-Bind Edit event if selected workflow is default
            if(workflowName === defaultWorkflow)
            {   
                $(".perc-default-wf-marker").show();
                container.find(".perc-item-delete-button").off("click");

                container.find('.perc-item-delete-button').addClass('perc-item-disabled');
            }
            
            // Update the Assigned tree/list
            if(!selectingSameWorkflow)
                updateSitesFolderAssignedSection(workflowName);
        }

        /**
         *  Save the workflow
         */
        function saveNewWorkflow()
        {
            var workflowName = $("#perc-new-workflow-name").val();
            if(workflowName.indexOf("??") !== -1)
            {
                var validationError = I18N.message("perc.ui.workflow.view@Workflow Invalid Characters");
                $.perc_utils.alert_dialog(
                {
                    "title" : I18N.message("perc.ui.workflow.view@Workflow Validation Error"),
                    "content" : validationError
                });
                return;
            }
            var isDefault = $(".perc-wf-default input").is(':checked');
            var workflowObj =
            {
                "Workflow" :
                {
                    "defaultWorkflow" : isDefault,
                    "workflowName" : workflowName,
                    "previousStepName" : "",
                    "workflowDescription" : "",
                    "stagingRoleNames" : getPublishNowStagingRoles(),
                    "previousWorkflowName" : ""
                }
            };

            function createWorkflowServiceCall(){
                $.PercWorkflowService().createWorkflow(workflowName, workflowObj, function(status, result)
                {
                    if(status)
                    {
                        
                        refreshWorkflowContainer(workflowName.trim());
                    }
                    else
                    {
                        var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                        $.perc_utils.alert_dialog(
                        {
                           //TODO: I18N TESTME
                        	"title" : I18N.message("perc.ui.workflow.view@Error Creating Workflow"),
                            "content" : errorMessage
                        });
                    }
                    $.unblockUI();
                });
            }            
            $.PercBlockUI();
            //User is trying to create workflow and set it as default, make sure workflow assignment is not in progress
            if(isDefault){
                $.PercFolderService.isWorkflowAssignmentInProgress(function(status){
                    if(status == "true"){
                        $.unblockUI();
                        $.perc_utils.alert_dialog({
                            "title" : I18N.message("perc.ui.page.general@Warning"),
                            "content" : WORKFLOW_ASSIGNMENT_INPROCESS_MESSAGE,
                            "okCallBack":function(){
                                dirtyController.setDirty(false);
                                window.location.reload();
                            }
                        });
                    }
                    else{
                        createWorkflowServiceCall();
                    }
                    
                });
            }
            else{
                createWorkflowServiceCall();
            }
        }

        /**
         *  Update the existing workflow
         */
        function updateWorkflow()
        {
            var previousWorkflowName = $("#perc-workflow-steps-container").data("workflowName");
            var newWorkflowName = $("#perc-update-workflow-name").val();
            var isDefault = $(".perc-wf-default input").is(':checked');
            if(newWorkflowName.indexOf("??") != -1)
            {
                var validationError = I18N.message("perc.ui.workflow.view@Workflow Invalid Characters");
                $.perc_utils.alert_dialog(
                {
                    "title" : I18N.message("perc.ui.workflow.view@Workflow Validation Error"),
                    "content" : validationError
                });
                return;
            }
            var workflowObj =
            {
                "Workflow" :
                {
                    "defaultWorkflow" : isDefault,
                    "workflowName" : newWorkflowName,
                    "previousStepName" : "",
                    "workflowDescription" : "",
                    "stagingRoleNames" : getPublishNowStagingRoles(),
                    "previousWorkflowName" : previousWorkflowName
                }
            };
            $.PercBlockUI();
            function updateWorkflowServiceCall(){
                $.PercWorkflowService().updateWorkflow(previousWorkflowName, workflowObj, function(status, result){
                    if (status) {
                        refreshWorkflowContainer(newWorkflowName.trim());
                    }
                    else {
                        var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(result[0]);
                        $.perc_utils.alert_dialog({
                            content: errorMessage,
                            title: I18N.message("perc.ui.publish.title@Error"),
                            okCallBack: function(){
                                refreshWorkflowContainer();
                            }
                        });
                    }
                    $.unblockUI();
                });
            }
            var currDefWf=$(".perc-default-wf-list-marker").attr("title");
            var isDefaultChanged = isDefault && currDefWf != previousWorkflowName;
            //User is trying to change default workflow, make sure workflow assignment is not in progress
            if(isDefaultChanged){
                $.PercFolderService.isWorkflowAssignmentInProgress(function(status){
                    if(status == "true"){
                        $.unblockUI();
                        $.perc_utils.alert_dialog({
                            "title" : I18N.message("perc.ui.page.general@Warning"),
                            "content" : WORKFLOW_ASSIGNMENT_INPROCESS_MESSAGE,
                            "okCallBack":function(){
                                dirtyController.setDirty(false);
                                window.location.reload();
                            }
                        });
                    }
                    else{
                        updateWorkflowServiceCall();
                    }
                });
            }
            else{
                updateWorkflowServiceCall();
            }
        }
        
        /** 
         * get roles from the list widget and build
         * a string array of roles seperated by ; to pass to server
         */
        function getPublishNowStagingRoles(){
            var roles = self.listEdit.getListItems();
            var serverStringRoles = "";
            for(var role in roles)
            {
                serverStringRoles = serverStringRoles + roles[role] + ";";
            }
            if(serverStringRoles.length > 0)
                serverStringRoles = serverStringRoles.substring(0, serverStringRoles.length - 1);
            return serverStringRoles;
        }
        
        /**
         * create the workflow publish now to staging permissions control
         * in the specified container with the selected workflow
         */
        function generateRolesControl(container, workflowName){
            //get the roles from the Roles Map for selected workflow
            var roles = [];
            if(workflowName)
            {
                if(stagingRoleNames.get(workflowName).trim() !== "")
                {
                    roles = stagingRoleNames.get(workflowName).split(";");
                }
            }
            
            $.PercUserService.getRoles(function(status, rolesJson) {
                if(status == $.PercServiceUtils.STATUS_ERROR) {
                    utils.alertDialog(I18N.message("perc.ui.workflow.view@Error Loading Roles"), rolesJson);
                    return;
                }

                //render the Roles Control
                self.listEdit = $.PercListEditorWidget({
                    // the DIV where this component will render
                    "container" : container,
                    // list of initial Roles to display
                    "items"     : roles,
                    "results"   : $.perc_utils.convertCXFArray(rolesJson.RoleList.roles),
                    // element that will toggle enable/disable of this component
                    //Needs toggler to initialize widget properly (no action is taken on toggle)
                    "toggler"   : $(".perc-wf-default").find('input'),
                    "title1" : I18N.message( "perc.ui.rolePropsDialog.title@Role Properties" ),
                    "title2" : I18N.message( "perc.ui.rolePropsDialog.title@Role Staging Actions" ),
                    "help" : I18N.message( "perc.ui.rolePropsDialog.title@Enter a role" )
                });
           });
        }

        /**
         *  Refresh the worflow section.
         */
        function refreshWorkflowContainer(workflowName)
        {
            getWorkflowList(function(status, result) {            
                if(status)
                {
                    $.PercDataList.updateList(container, result);         
                    hideWorkflowEditor();
                    hideWorkflowUpdateEditor();                    
                    selectWorflow(workflowName);
                    expandWrokflowList(workflowName);
                }
                else
                {
                    //TODO handle error
                }
            });
        }

        /**
         *  Cancel and close the new workflow editor
         */
        function cancel()
        {
            dirtyController.setDirty(false);
            hideWorkflowEditor();
            hideWorkflowUpdateEditor();
            selectWorflow(selectedWorkflow);
        }


        /**
         * Helper fucntion to attach workflow editor events.
         */
        function attachWorkflowEditorEvents()
        {
            //Bind Save event
            $("#perc-wf-save").off("click").on("click", function(evt)
            {
                saveNewWorkflow();
            });

            //Bind Cancel event
            $("#perc-wf-cancel, #perc-wf-update-cancel").off("click").on("click", function(evt)
            {
                cancel();
            });

            //Bind Edit event
            $("#perc-wf-edit").off("click").on("click", function(evt)
            {
                editWorkflow(evt);
            });

            //Bind Update event
            $("#perc-wf-update-save").off("click").on("click", function(evt)
            {
                updateWorkflow();
            });
        }

        /**
         * Gets the workflow list from server and calls the supplied callback.
         * The first parameter of the call back will be a boolean status and second parameter will be an array of workflow names
         * or error message.
         * @param callback assumed to be a function.
         */
        function getWorkflowList(callback)
        {
            //get list of workflows
            $.PercWorkflowService().getWorkflows(function(status, result)
            {
                if(status)
                {
                    var workflows = [];
                    if(!Array.isArray(result[0].Workflow)) {
                        workflows.push(result[0].Workflow);
                        }
                    else
                        workflows = result[0].Workflow;
                    var workflowArray = [];
                    $.each(workflows, function()
                    {
                        workflowArray.push(this.workflowName);
                        stagingRoleNames.set(this.workflowName,this.stagingRoleNames);
                        if(this.defaultWorkflow) {
                            defaultWorkflow = this.workflowName;
                        }
                    });
                    callback(true, workflowArray);
                }
                else
                {
                    callback(false, $.PercServiceUtils.extractDefaultErrorMessage(result[0]));
                }
            });
        }
        
        /**
         *   Initialization method
         */
        function init()
        {
            $("#perc-workflows-list").html("");
            // Pass the config data to dataItem plugin to render the list of workflows
            var container = $("#perc-workflows-list");
            var dataListConfig =
            {
                listItem : [],
                title : I18N.message("perc.ui.workflow.view@Workflows"),
                addTitle : I18N.message("perc.ui.workflow.view@Add New Workflow"),
                deleteTitle : I18N.message("perc.ui.workflow.view@Delete Workflow"),
                enableDelete : true,
                collapsible : true,
                createItem : addNewWorkflow,
                deleteItem : deleteWorkflow,
                selectedItem : selectWorflow,
                truncateEntries : true,
                truncateEntriesCount : 5
            };

            $.PercDataList.init(container, dataListConfig);

            //render workflow list and select the first item.
            getWorkflowList(function(status, result)
            {
                if(status)
                {
                    $.PercDataList.updateList(container, result);
                    selectWorflow(result[0]);
                }
                else
                {
                    //TODO handle error
                }
                    
            });
            
            //attach editor events
            attachWorkflowEditorEvents();              
            // Initialize the Assigned list/tree
            initPercDataTree();
        }
        /**
         * Initializes the list of assigned sites and folder.
         */
        function initPercDataTree()
        {
            // Initialize the PercDataTree plugin
            var dataTreeConfig = {
                'instanceIdSuffix' : '-instance1', // Supply a suffix to make an internal ID really unique
                'title' : I18N.message("perc.ui.workflow.view@Assigned"), // title of PercDataTree
                'addTitle' : I18N.message("perc.ui.workflow.view@Assign Sites and Folders to Workflow"), // title atribute of add button
                'collapsible' : true,
                'levelLimit' : 2,
                'createItem' : openAssignWorkflowSitesAndFoldersDialog, // function invoked when clickking add button
                'showCheckboxes' : false,
                'selectedWorkflow' : selectedWorkflow
            };
            $.PercDataTree.init(assign_container, dataTreeConfig);
            assign_container.append('<div class="perc-sa-loading-warning-message"><img src="../css/dynatree/skin/loading.gif" alt="perc.ui.assign.workflow@LoadingGifAlt" style="vertical-align:bottom"/>' +I18N.message("perc.ui.assign.workflow@Workflow Loading") + '</div>');

        }

        /**
         * Updates the sites/folder section according to the selected workflow.
         */
        function updateSitesFolderAssignedSection(workflowName)
        {
            $(".perc-sa-loading-warning-message-hidden").addClass("perc-sa-loading-warning-message").removeClass("perc-sa-loading-warning-message-hidden");            
            if (typeof(workflowName) === 'undefined')
            {
                workflowName = selectedWorkflow;
            }
            originalSitesJson = originalAssetsJson = null;
            startSiteAssetFoldersLoadingJobs();
        }
        //TODO The startSiteAssetFoldersLoadingJobs, loadSiteFolders, loadAssetFolders needs to be consolidated with the ones in perc_assign_workflow_sites_folder_dialog
        //these are duplicated.
        function startSiteAssetFoldersLoadingJobs()
        {
            $.PercFolderService.startAssociatedFoldersJob("Sites", selectedWorkflow, function(status, result) {
                if(status == $.PercServiceUtils.STATUS_ERROR)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                    return false;
                }
                if(siteJobId){
                    cancelJob(siteJobId, false);
                }
                siteJobId = result;
                $.perc_utils.info(I18N.message("perc.ui.workflow.view@Started Associated Folders Jobs For Sites") + siteJobId); 
                $.PercFolderService.startAssociatedFoldersJob("Assets", selectedWorkflow, function(status, result) {
                    if(status == $.PercServiceUtils.STATUS_ERROR)
                    {
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                        return false;
                    }
                    if(assetJobId){
                        cancelJob(assetJobId, false);
                    }
                    assetJobId = result;    
                    $.perc_utils.info(I18N.message("perc.ui.workflow.view@Started Associated Folders Jobs For Assets") + assetJobId); 
                    loadSiteFolders();
                    loadAssetFolders();
                }, false);
            
            }, false);
            
        }
        function loadSiteFolders()
        {
                if(siteJobId == null)
                    return;
                $.PercFolderService.getAssociatedFoldersJobStatus(siteJobId, function(status, result) {
                if(status == $.PercServiceUtils.STATUS_ERROR)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                    return false;
                }
                var status = result.GetAssginedFoldersJobStatus.status;
                var message = result.GetAssginedFoldersJobStatus.message; // message will be blank if job is still running
                if( message == ""  && status !== "100" )
                {
                    setTimeout(loadSiteFolders, 2000);
                    return;
                }
                $.perc_utils.info(I18N.message("perc.ui.workflow.view@Finished Associated Folders Jobs For Sites") + siteJobId); 
                siteJobId = null;
                originalSitesJson = {};
                originalSitesJson.folderItem = result.GetAssginedFoldersJobStatus.folderItems;
                showFolderTrees();
            });
        }
        function loadAssetFolders()
        {
            if(assetJobId == null)
                return;
            
            $.PercFolderService.getAssociatedFoldersJobStatus(assetJobId, function(status, result) {
                if(status == $.PercServiceUtils.STATUS_ERROR)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                    return false;
                }
                var status = result.GetAssginedFoldersJobStatus.status;
                var message = result.GetAssginedFoldersJobStatus.message; // message will be blank if job is still running
                if( message == ""  && status !== "100" )
                {
                    setTimeout(loadAssetFolders, 2000);
                    return;
                }
                $.perc_utils.info(I18N.message("perc.ui.workflow.view@Finished Associated Folders Jobs For Assets") + assetJobId); 
                assetJobId = null;
                originalAssetsJson = {};
                originalAssetsJson.folderItem = result.GetAssginedFoldersJobStatus.folderItems;
                showFolderTrees();
            });
        }
        
        function showFolderTrees()
        {
            if (originalSitesJson != null && originalAssetsJson != null) 
            {
                $(".perc-sa-loading-warning-message").addClass("perc-sa-loading-warning-message-hidden").removeClass("perc-sa-loading-warning-message");         
                $.PercDataTree.updateTree(assign_container, [originalSitesJson, originalAssetsJson], selectedWorkflow);
            }
        }
    
    
        /**
         * Clicking the add button opens a dialog to assign workflows to sites & folders.
         */
        function openAssignWorkflowSitesAndFoldersDialog()
        {
            // The variable selectedWorkflow is in the closure scope
            $.perc_assign_workflow_sites_folder_dialog.createDialog(selectedWorkflow, updateSitesFolderAssignedSection);
        }

        // Return the public api/interface
        return api;
    };
    
    /**
     *  On document ready
     */
    $(document).ready(function()
    {
        $.wfViewObject = $.PercWorkflowView();
        $.PercFolderService.isWorkflowAssignmentInProgress(function(status){
            if(status == "true"){
                $.wfViewObject.isWorkflowAvailable = false;
                $("#perc-workflow-wrapper").empty().addClass("perc-workflow-unavailale-message-table").append("<div class='perc-workflow-unavailale-message'>Workflow assignment in process</div>");
            }
            else{
                $.wfViewObject.init();
            }
            
        });
    });
})(jQuery, jQuery.Percussion);
