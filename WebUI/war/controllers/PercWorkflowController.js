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
 * Workflow controller class to perform the workflow actions.
 * Makes appropriate calls to the workflow service to check out, check in and transition items.
 * based on the type of the object shows appropriate error messages.
 */
(function($)
{
    $.PercWorkflowController = function()
    {
        return {
            checkIn : checkIn,
            checkOut : checkOut,
            transition : transition,
            getTransitions : getTransitions,
            isCheckedOutToCurrentUser : isCheckedOutToCurrentUser,
            doesItemExist : doesItemExist
        };
    };

    /**
     * Workflow service instance to perform the service calls for different methods.
     */
    var wfService = $.PercWorkflowService();

    /**
     * Checkin is an implicit action, calls workflow service checkin method to check in the supplied item.
     * If there is any error performing this action shows the error to the user and calls the call back with false value.
     * If not calls the callback with true.
     * @param itemId(String), the id of the item(String format of guid) that needs to be checked in. Must not be blank.
     * @param callback(function (boolean)), if the callback function is not <code>null</code> then it is called by this
     * method with boolean true if checked in successfully otherwise false.
     */
    function checkIn(itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        //Local callback function that is passed to the service checkin method, this callback function calls the passed in
        //callback with appropriate value.
        var chkInCb = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR){
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.debug("Error : " + defMsg);
                callback(false);
            }
            else{
                callback(true);
            }
        };
        wfService.checkIn(itemId, chkInCb);
    }

    /**
     * Calls workflow service checkout method. If the checkout user is same as logged in user then calls the callback
     * function with true value. If not depending on the assignment type of the user shows appropriate message.
     * Assignment type is Admin, then provides a confirmation dialog with an option to override. If user clicks on override
     * then calls _forceCheckOut to check it out to the admin.
     * Assignment type Assignee, shows an alert message to the user that the item is being edited by some other user.
     * Assignment type Reader, shows an alert message to the user that he is not authorized to open the item.
     *
     * @param itemType, the type of the item "percPage" or "percAsset" used in the warning messages.
     * @param itemId(String), the id of the item (String format of guid) that needs to be checked in. Must not be blank.
     * @param callback(function (boolean)), if the callback function is not <code>null</code> then it is called by this
     * method with boolean true if checked out successfully otherwise false.
     */
    function checkOut(itemType, itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        wfService.checkOut(itemId, function(status, results)
        {
            if(status == $.PercServiceUtils.STATUS_SUCCESS){
                var itemStatus = results.data.ItemUserInfo;
                if(itemStatus.checkOutUser != itemStatus.currentUser)
                {
                    if(itemStatus.assignmentType == "Admin")
                    {
                        var options = {title:warningDlgTitle[itemType],
                            question:_getChkOutDlgContent(0, itemStatus, itemType),
                            cancel:function(){callback(false);},
                            success:function(){_forceCheckOut(itemId, callback);},
                            type:"OVERRIDE_OK"
                        };
                        $.perc_utils.confirm_dialog(options);
                    }
                    else if(itemStatus.assignmentType == "Assignee")
                    {
                        var options = {title:warningDlgTitle[itemType],
                            content:_getChkOutDlgContent(1, itemStatus, itemType)};
                        $.perc_utils.alert_dialog(options);
                        callback(false);
                        return;
                    }
                    else
                    {
                        var options = {title:warningDlgTitle[itemType],
                            content:_getChkOutDlgContent(2, itemStatus, itemType)};
                        $.perc_utils.alert_dialog(options);
                        callback(false);
                        return;
                    }
                }
                else
                {
                    callback(true);
                }
            }
            else{
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                if (defMsg.indexOf("Not a valid content id") != -1)
                {
                    var options = {title:warningDlgTitle[itemType],
                        content:_getChkOutDlgContent(3, itemStatus, itemType)};
                    $.perc_utils.alert_dialog(options);
                    callback(false);
                    return;
                }

                $.perc_utils.alert_dialog({title: I18N.message('perc.ui.labels@Error'), content: defMsg});
                callback(false);
            }
        });
    }

    /**
     * Helper method that calls the service to force check out the item to the admin.
     * This should be called if the logged in user has admin access.
     * @param itemId(String format of guid) assumed not null.
     * @param callback(function (boolean)), if the callback function is not <code>null</code> then it is called by this
     * method with boolean true if checked out successfully otherwise false.
     */
    function _forceCheckOut(itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        wfService.forceCheckOut(itemId, function(status, results){
            if(status == $.PercServiceUtils.STATUS_SUCCESS){
                callback(true);
            }
            else{
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.alert_dialog({title: I18N.message('perc.ui.labels@Error'), content: defMsg});
                callback(false);
            }

        });
    }


    /**
     * Calls workflow service transtion method to transition the supplied item.
     * If there is any error performing this action shows the error to the user and calls the call back with false value.
     * If not calls the call back with true.
     * @param itemId(String), the id of the item(String format of guid) that needs to be checked in. Must not be blank.
     * @param callback(function (boolean)), , if the callback function is not <code>null</code> then it is called by this
     * method with boolean true if the item is transitioned successfully otherwise false.
     */
    function transition(itemId, itemType, transitionName, comment, callback)
    {
        callback = callback == null?function(){}:callback;
        var trCb = function(status, results){
            if(status == $.PercServiceUtils.STATUS_ERROR){
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.alert_dialog({title: I18N.message('perc.ui.labels@Error'), content: defMsg});
                callback(false);
            }
            else{
                var trResults = results.data;
                var fassets = trResults.ItemTransitionResults.failedAssets;
                if(typeof fassets !=='undefined' && (!Array.isArray(fassets) || !fassets.length) && fassets.length > 0)
                {
                    var type = (itemType === "percAsset")?"asset":"page";
                    var msg = I18N.message("perc.ui.workflow.steps.view@Cannot Publish", [type]) + "<br/><br/>";

                    if(Array.isArray(fassets))
                    {
                        $.each(fassets,function(){
                            var fp = this.folderPaths;
                            fp = fp.substring(fp.indexOf("/Assets")) + "/" + this.name;
                            msg += "<b>" + fp + "</b><br/>";
                        });
                    }
                    else
                    {
                        var fp = fassets.folderPaths;
                        fp = fp.substring(fp.indexOf("/Assets")) + "/" + fassets.name;
                        msg += "<b>" + fp + "</b><br/>";
                    }
                    msg += "<br/>" + I18N.message("perc.ui.workflow.steps.view@Remove Assets");

                    $.perc_utils.alert_dialog({title: I18N.message('perc.ui.labels@Error'), content: msg});
                    callback(false);
                }
                else
                {
                    callback(true);
                }
            }
        };
        wfService.transition(itemId, transitionName, comment, trCb);
    }

    /**
     * Returns the available transitions to the user  as a second param of the callback function.
     * An array of TransitionAction objects.
     * {"name":"Reject", "class":"perc-wf-reject","alt":"Reject"}
     * The first parameter is a boolean status, true in case of success and false in case of failure.
     * Shows the error message in case of error.
     * @param itemId(String), the id of the item for which the transitions are required. Must not be blank.
     * @param callback(boolean status, array of transitionActions)), if the callback function is not <code>null</code>
     * then it is called by this method.
     */
    function getTransitions(itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        wfService.getTransitions(itemId, function(status, results){
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                var trAs = [];
                var triggers = results.data.ItemStateTransition.transitionTriggers;
                if(Array.isArray(triggers)){
                    $.each(triggers, function(index){
                        //As we already added Publish skip it if exists.
                        if(transitionActions[triggers[index]])
                            trAs.push(transitionActions[triggers[index]]);
                    });
                }
                else
                {
                    if(transitionActions[triggers])
                        trAs.push(transitionActions[triggers]);
                }
                callback(true, trAs);
            }
            else{
                var defMsg = $.PercServiceUtils.extractDefaultErrorMessage(results.request);
                $.perc_utils.alert_dialog({title: 'Error', content: defMsg});
                callback(false, []);
            }
        });
    }

    /**
     * Checks whether the item with the supplied id is checked out to the current user or not.
     * @param itemId(String), the id of the item. Must not be blank.
     * @param callback(function (boolean, string)), the callback function, calls it with a boolean and string value.
     * The boolean value will be true if it is checked out to the current user, otherwise false.  The string value will
     * contain true, false, or an error message.
     */
    function isCheckedOutToCurrentUser(itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        var isChkCb = function(status, results){
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                callback(results.data === true, results.data);
            }
            else{
                callback(false, $.PercServiceUtils.extractDefaultErrorMessage(results.request));
            }
        };
        wfService.isCheckedOutToCurrentUser(itemId, isChkCb);
    }

    /**
     * Checks whether the item with the supplied id exists.
     * @param itemId(String), the id of the item. Must not be blank.
     * @param callback(function (boolean)), the callback function, calls it with a boolean value. The boolean value will be true
     * it exists, otherwise false.
     */
    function doesItemExist(itemId, callback)
    {
        callback = callback == null?function(){}:callback;
        /* isCheckedOutToCurrentUser(itemId, function(status, msg){
            if(typeof msg == "string")
                callback(status || msg.indexOf("does not exist") == -1);
            else
                callback(status);
        });
*/
        $.PercPathService.getPathItemById(itemId, function(status, data){
           callback(status);
        });
    }

    /**
     * Helper method that creates appropriate message for check out.
     * @param msgType(int) type of the message required.
     * @param itemStatus assumed not null, see PercWorkflowService#checkOut method for details.
     * @param itemType assumed not <code>null</code> (String "page" or "asset")
     */
    function _getChkOutDlgContent(msgType, itemStatus, itemType)
    {
        var msg = "";
        var type = "page";
        if(itemType == "percAsset")
            type = "asset";
        if(msgType == 0)
        {
            msg = I18N.message("perc.ui.workflow.steps.view@Override", [type, itemStatus.itemName, itemStatus.checkOutUser]);
        }
        else if(msgType==1)
        {
            msg = I18N.message("perc.ui.workflow.steps.view@Edited By", [type, itemStatus.itemName, itemStatus.checkOutUser]);
        }
        else if(msgType==2)
        {
            msg = I18N.message("perc.ui.workflow.steps.view@Not Authorized", [type, itemStatus.itemName]);
        }
        else if(msgType==3)
        {
            msg = I18N.message("perc.ui.workflow.steps.view@Deleted In Another Session", [type]);
        }
        return msg;
    }

    var warningDlgTitle = [];
    warningDlgTitle.percPage = "Open Page";
    warningDlgTitle.percAsset = "Open Asset";

    /**
     * Object map for transition trigger names and its classes.
     * The class consists of the appropriate buttons.
     */
    var transitionActions = [];
    transitionActions.Reject = {"name":"Reject", "cssClass":"perc-wf-reject","alt":"Reject"};
    transitionActions.Submit = {"name":"Submit", "cssClass":"perc-wf-submit","alt":"Submit"};
    transitionActions.Approve = {"name":"Approve", "cssClass":"perc-wf-approve","alt":"Approve"};
    transitionActions.Resubmit = {"name":"Resubmit", "cssClass":"perc-wf-resubmit","alt":"Resubmit"};
    transitionActions["Quick Edit"] = {"name":"Quick Edit", "cssClass":"perc-wf-edit","alt":"Edit"};
    transitionActions.Archive = {"name":"Archive", "cssClass":"perc-wf-archive","alt":"Archive"};

})(jQuery);
