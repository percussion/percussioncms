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
 * Service to handle the workflow transitions and check in and check out of items.
 */

(function($)
{
    $.PercWorkflowService = function()
    {
        return {
            checkIn : checkIn,
            checkOut : checkOut,
            forceCheckOut : forceCheckOut,
            transition : transition,
            getTransitions : getTransitions,
            isCheckedOutToCurrentUser: isCheckedOutToCurrentUser,
            isApproveAvailableToCurrentUser: isApproveAvailableToCurrentUser,
            getWorkflowObject: getWorkflowObject,
            createNewWorkflowStep : createNewWorkflowStep,
            deleteWorkflowStep : deleteWorkflowStep,
            updateWorkflowStep : updateWorkflowStep,
            getStatusByWorkflow : getStatusByWorkflow,
            getWorkflowList:getWorkflowList,
            getWorkflows: getWorkflows,
            createWorkflow : createWorkflow,
            updateWorkflow : updateWorkflow,
            deleteWorkflow : deleteWorkflow,
            bulkApproveItems: bulkApproveItems,
            getBulkApproveStatus: getBulkApproveStatus,
            getDefaultWorkflow: getDefaultWorkflow
        };
    };

    /**
     * Retrieve the status of a Workflow
     * @param workflow (string) name of the workflow. Note: the Url changes according to the workflow name.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function getStatusByWorkflow(workflow, callback){
        var url = "/Rhythmyx/services/workflowmanagement/workflows/" + $.perc_utils.encodeURL(workflow) + "/states/choices";
        callback = callback === null?function(){}:callback;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }
    /**
     * Makes an Ajax call to server to check out the item, Expects the following object from the server as result.
     * {"ItemUserInfo":
     *      {"checkOutUser":"user1",
     *       "currentUser":"editor1",
     *       "itemName":"Home Page",
     *       "assignmentType":true}}
     * See PSItemUserInfo for more details.
     * @param itemId (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function checkOut(itemId, callback)
    {
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID checkOut"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_CHECKOUT + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }

    /**
     * Makes an ajax call to the server to checkIn the supplied item.
     * @param itemId (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function checkIn(itemId, callback)
    {
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID checkIn"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_CHECKIN + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }

    /**
     * Makes an Ajax call to get the avialable transitions to the logged in user for the supplied item.
     * Expects the results from server in the following format.
     * {"ItemStateTransition":
     *  { "itemId":"2-101-781",
     *    "stateId":"4",
     *    "workflowId":"7",
     *    "transitionTriggers":["Publish","Reject"]
     *  }
     * }
     * @param itemId (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function getTransitions(itemId, callback)
    {
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID getTransitions"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_TRANSITIONS + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }

    /**
     * Makes an Ajax call to the server to transition the supplied item.
     * @param itemId (string), must not be blank.
     * @param transitionName, must not be blank
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function transition(itemId, transitionName, comment, callback)
    {
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID Transition"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_TRANSITION_COMMENT + "/" + itemId + "/" + transitionName + "?comment=" + comment;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }

    /**
     * Call this method to checkIn an item that has been checkedout to someone else. The logged in user must be an admin.
     * Use checkOut method first and then call this method based the results of that call.
     * Makes an Ajax call to server to check in the item first and then check it out, Expects the following object from
     * the server as result.
     * {"ItemUserInfo":
     *      {"checkOutUser":"user1",
     *       "currentUser":"editor1",
     *       "itemName":"Home Page",
     *       "assignmentType":true}}
     * @param itemId (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function forceCheckOut(itemId,callback){
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_FORCE_CHECKOUT + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);

    }

    /**
     * Makes an Ajax call to the server to find whether the supplied item is checked out to the current user or not.
     * @param itemId (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function isCheckedOutToCurrentUser(itemId,callback){
        if($.perc_utils.isBlankString(itemId)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank Item ID"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_CHECKED_OUT_TO_USER + "/" + itemId;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }

    /**
     * Makes an Ajax call to the server to find whether the currently logged in user has previleges to
     * perform an approve in the specified folder
     * @param folderPath The path to the folder
     * @param callback (function) callback function to be invoked when ajax call returns,
     * @return true if the current user has preveliges to perform approve, otherwise false
     */
    function isApproveAvailableToCurrentUser(folderPath, callback){
        if($.perc_utils.isBlankString(folderPath)){
            $.perc_utils.debug(I18N.message("perc.ui.workflow.service@Blank folderPath"));
            return false;
        }
        callback = callback === null?function(){}:callback;
        var url = $.perc_paths.WORKFLOW_IS_APPROVE_ALLOWED + folderPath;
        $.PercServiceUtils.makeJsonRequest(url,$.PercServiceUtils.TYPE_GET,false,callback);
    }


    /**
     * Makes an Ajax call to server to get the workflow object
     * {"Workflow":{
                    "workflowName":"Default Workflow",
                    "workflowSteps":[{
                                      "stepName":"Draft",
                                      "stepRoles":[
                                                    {"roleName":"Admin"},
                                                    {"roleName":"Contributor"},
                                                    {"roleName":"Editor"}
                                                   ]
                                     }]
                    }
        }
     * See PSItemUserInfo for more details.
     * @param workflowName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function getWorkflowObject(workflowName, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED + workflowName;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback($.PercServiceUtils.STATUS_ERROR, [results.request,results.textstatus,results.error]);
            }
            else
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, [results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Makes an Ajax call to server to update existing step
     *     "NewWorkflowStep": {
                                "workflowName" : workflowName,
                                "previousStepName" : previousStepName,
                                "workflowStep":[{
                                            "stepName":"Reveiw One",
                                            "stepRoles":stepRoles
                                }]
                            }
     * See PSItemUserInfo for more details.
     * @param workflowName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function updateWorkflowStep(workflowName,stepName, StepObj, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED +  workflowName + "/steps/" + stepName;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_PUT, false, serviceCallback, StepObj);
    }

    /**
     * Makes an Ajax call to server to update workflow object with new step
     *     "NewWorkflowStep": {
                                "workflowName" : workflowName,
                                "previousStepName" : previousStepName,
                                "workflowStep":[{
                                            "stepName":"Reveiw One",
                                            "stepRoles":stepRoles
                                }]
                            }
     * See PSItemUserInfo for more details.
     * @param workflowName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function createNewWorkflowStep(workflowName,stepName, newStepObj, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED +  workflowName + "/steps/" + encodeName(stepName);
        if((stepName.indexOf("/") !== -1) || 
            (stepName.indexOf("\\") !== -1) || 
            (stepName.indexOf(";") !== -1) || 
            (stepName.indexOf("\"") !== -1))
        {
            newStepObj.Workflow.workflowSteps[0].stepName = stepName.replace(/[\\\/;"]/g,'#');
        }

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_POST, false, serviceCallback, newStepObj);
    }
    /**
     * Makes an Ajax call to server to update workflow object with deleted step
     * @param workflowName (string), must not be blank.
     * @param stepName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */

    function deleteWorkflowStep(workflowName,stepName, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED +  workflowName + "/steps/" + stepName ;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_DELETE, false, serviceCallback);
    }

    /**
     * Makes an Ajax call to server to get list of all workflows
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */

    function getWorkflowList(callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                results.data.EnumVals.entries = $.perc_utils.convertCXFArray(results.data.EnumVals.entries);
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Makes an Ajax call to server to get the information of default workflow (name and id)
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function getDefaultWorkflow(callback)
    {
        var Url = $.perc_paths.DEFAULT_WORKFLOW_META;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                results.data.EnumVals.entries = $.perc_utils.convertCXFArray(results.data.EnumVals.entries);
                callback(true,results.data);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_GET, false, serviceCallback);
    }

    /**
     * Makes an Ajax call to server to get list of all availabel workflows plus info on which one is default workflow
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */

    function getWorkflows(callback)
    {
        var Url = $.perc_paths.WORKFLOW_META;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_GET, false, serviceCallback);

    }

    function createWorkflow(workflowName, workflowObj, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED + encodeName(workflowName);

        if ((workflowName.indexOf("/") !== -1) || 
            (workflowName.indexOf("\\") !== -1) || 
            (workflowName.indexOf(";") !== -1) || 
            (workflowName.indexOf("\"") !== -1))
        {
            workflowObj.Workflow.workflowName = workflowName.replace(/[\\\/;"]/g,'#');
        }

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_POST, false, serviceCallback, workflowObj);

    }

    /**
     * Makes an Ajax call to server to update existing workflow name
     *     {"Workflow":
                    {"default":false,
                     "workflowName":"New Workflow Name",
                     "previousStepName":"",
                     "workflowDescription":"",
                     "stagingRoleNames" : publishNowRoles,
                     "previousWorkflowName":"Old Workflow Name"}}"

     * See PSItemUserInfo for more details.
     * @param workflowName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */
    function updateWorkflow(prevWfName, workflowObj, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED + prevWfName;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_PUT, false, serviceCallback, workflowObj);
    }

    /**
     * Decodes a given name to be added to a URL
     * @param nameToEncode (function) nameToEncode the name to be encoded, not <code>null</code>
     */
    function encodeName(nameToEncode)
    {
        var encodedName = nameToEncode.toString();
        encodedName = encodedName.replace(/[\\\/;"]/g,'#');
        return encodeURIComponent(JSON.stringify(encodedName));
    }

    /**
     * Makes an Ajax call to server to update workflow object with deleted step
     * @param workflowName (string), must not be blank.
     * @param stepName (string), must not be blank.
     * @param callback (function) callback function to be invoked when ajax call returns, may be <code>null</code>
     * See $.PercServiceUtils.makeJsonRequest for parameter details, when this function is called.
     */

    function deleteWorkflow(workflowName, callback)
    {
        var Url = $.perc_paths.WORKFLOW_STEPPED +  workflowName;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback(false,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback(true,[results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_DELETE, false, serviceCallback);
    }

    function bulkApproveItems(approvalItems, callback)
    {
        var Url = $.perc_paths.WORKFLOW_BULK_APPROVE;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback($.PercServiceUtils.STATUS_ERROR,[results.request,results.textstatus,results.error]);
            }
            else
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, [results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_POST, false, serviceCallback, approvalItems);

    }

    function getBulkApproveStatus(jobId, isFull, callback)
    {
        var Url = $.perc_paths.WORKFLOW_BULK_APPROVE + "/status";
        if(isFull)
            Url += "/full/" + jobId;
        else
            Url += "/processed/" + jobId;

        var serviceCallback = function(status, results){
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                callback($.PercServiceUtils.STATUS_ERROR, [results.request,results.textstatus,results.error]);
            }
            else
            {
                callback($.PercServiceUtils.STATUS_SUCCESS, [results.data,results.textstatus]);
            }
        };
        $.PercServiceUtils.makeJsonRequest(Url, $.PercServiceUtils.TYPE_GET, false, serviceCallback);

    }

})(jQuery);
