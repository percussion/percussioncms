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
 * PercWorkflowStepView.js
 *
 *-Renders the steps of the workflow.
 *
 *
 */
(function($, P)
{
    var settings = {
        workflowObject : null,
        workflowName : null
    };
    $.PercWorkflowStepsView = 
    {
        init:init,
        refresh:refresh
    };
    function init(workflowName)
    {
        settings.workflowName = workflowName;
        $("#perc-workflow-steps-container table").addClass("perc-workflow-steps-rendering").removeClass("perc-workflow-steps-ready");
        __loadWorkflowObject(settings.workflowName, function(workflowObject){
            __renderWorkflowSteps(workflowObject, function(){
                __attachEvents();
                $("#perc-workflow-steps-container table").removeClass("perc-workflow-steps-rendering").addClass("perc-workflow-steps-ready");;
            });
        });
        
    }
    function refresh(workflowName)
    {
        $("#perc-workflow-steps-container table").addClass("perc-workflow-steps-rendering").removeClass("perc-workflow-steps-ready");
        __loadWorkflowObject(workflowName, function(workflowObject) {
            __renderWorkflowSteps(workflowObject, function(){
                __attachEvents();
                $("#perc-workflow-steps-container table").removeClass("perc-workflow-steps-rendering").addClass("perc-workflow-steps-ready");;
            });
        });
        
    }

    function __loadWorkflowObject(workflowName, callback)
    {
        $.PercWorkflowService().getWorkflowObject(workflowName, function(status, result)
        {
            workflowObject = result[0];
            if(status && (workflowObject != null) && (workflowObject != undefined))
            {
                callback(workflowObject);
            }
            else
            {
                //TODO show error to the user.
            }
        });
    }
    
   /** 
    *Renders the Workflowsteps.
    *@param workflowObject - An object containing the step name and associated roles.
    *For object structure details refer to the doc of getWorkflowObject() method.
    */
    function __renderWorkflowSteps(workflowObject, callback)
    {
        $("#perc-workflow-steps-container table").html("");
        var workflowObject = workflowObject;
        var workflowName = workflowObject.Workflow.workflowName;
        var workflowStepName;
        var percStepRow = $('<tr><td><div id = "perc-workflow-column-wrapper" style = "float:left"></div></td></tr>');
        var percArchiveStepRow = $('<tr id = "perc-workflow-archvie-row"><td></td></tr>');            
        $(percStepRow).attr('id','perc-wf-row-one');
        $("#perc-workflow-name").html(workflowName);
        for(var i=0; i<workflowObject.Workflow.workflowSteps.length; i++){       
            var workflowStep = workflowObject.Workflow.workflowSteps[i];
            var percStepColumn = $('<div class ="perc-workflow-step" ><div class ="perc-workflow-step-label perc-ellipsis"></div><div class = "perc-action-button"></div></div>');
            percStepColumn.data("workflowStep", workflowStep);
            var percStepColumnSpacer = $('<div class = "perc-workflow-step-spacer"></div>');            
            var percStepLabel = $(percStepColumn).find('.perc-workflow-step-label');
            var percWorkflowActionText = $('<div class ="perc-action-label"><span class = "perc-action-label-span-stepdown"></span><span class="perc-action-label-span-stepup"></span></div>');
            var percWorkflowEditButton = $('<div class = "perc-workflow-edit-button"></div>');
            var percStepRoles= $('<div class ="perc-workflow-step-roles"></div>');
            var percStepRolesContainer = $('<ul></ul>');
            var actionText = true;
            workflowStepName = workflowStep.stepName;           
            $(percStepColumn).attr('id',__generateElementId(workflowStepName));
            $(percStepColumn).attr('name', workflowStepName);             
            $(percStepLabel).html(workflowStepName).attr("title",workflowStepName);
            var stepRoles = workflowStep.stepRoles;
            if(!$.isArray(stepRoles))
            {
                var tempArray = [];
                tempArray.push(stepRoles);
                stepRoles = tempArray;
            }
            for(var j=0; j<stepRoles.length; j++){
                 var workflowStepRole;
                 var percStepRolesItems = $('<li class="perc-ellipsis"></li>');
                 //if there are more than three roles hide and collapse them
                 if(j>2){
                    percStepRolesItems.addClass("perc-more-list perc-hidden").hide();
                 }                 
                 workflowStepRole = stepRoles[j].roleName;    
                 $(percStepRolesItems).html(workflowStepRole).attr('title', workflowStepRole);
                 $(percStepRolesContainer).append(percStepRolesItems);
                 // Add more link after three items
                if(j==2 && stepRoles.length > 3 ) {
                    var moreLink = $('<li class="perc-moreLink perc-visible">more</li>');
                    $(percStepRolesContainer).append(moreLink);                    
                 }
                 
                 //Add less link after the last element   
                if(j == stepRoles.length - 1 && stepRoles.length > 3) {
                     var lessLink = $('<li style = "display:none" class="perc-lessLink perc-ellipsis perc-hidden">less</li>');
                    $(percStepRolesContainer).append(lessLink);                     
                 }
            }                
            $(percStepColumn).append($(percStepRoles).append(percStepRolesContainer), percWorkflowActionText);
            
            if(workflowStepName != "Archive"){
                $(percStepRow).find('#perc-workflow-column-wrapper').append(percStepColumn, percStepColumnSpacer);
                             
            }else{
                $(percArchiveStepRow).find('td').attr('style','float:right;').append(percStepColumn, percStepColumnSpacer);                
                               
            }
            __renderWorkflowActionText(workflowName, workflowStepName, percWorkflowActionText, percStepColumnSpacer, percStepColumn, workflowStep);             
        }                
            $("#perc-workflow-table").append(percStepRow, percArchiveStepRow);
            $("#perc-workflow-archive").parent().prepend("<div id = 'perc-archive-action-text'></div>");
        callback();
    }

    /**
     * Helper method to render the action text at the bottom of each step based on the step name.
     */
    function __renderWorkflowActionText(workflowName, stepName, percWorkflowActionText, percStepColumnSpacer, percStepColumn, workflowStep)
    {
        var stepUp = $(percWorkflowActionText).find('.perc-action-label-span-stepup');
        var stepDown = $(percWorkflowActionText).find('.perc-action-label-span-stepdown');
        $(percStepColumn).find('.perc-action-button').html('<span title="Configure Step" class="perc-reserved-step-config-bttn"></span>');
        $(percStepColumn).find('.perc-reserved-step-config-bttn').data({"isReservedStep":true});
        switch(stepName)
        {
        	case 'Draft':
                stepUp.text(I18N.message("perc.ui.workflow.steps.view@Submit"));
                percStepColumnSpacer.addClass('perc-twoheaded-arrow');
                percStepColumnSpacer.addClass('perc-twoheaded-arrow');
                percStepColumnSpacer.append('<div class = "perc-create-new-step" title = ' +I18N.message("perc.ui.workflow.steps.view@Add New Step") + '></div>');
                percStepColumnSpacer.attr('name', percStepColumnSpacer.prev().attr('name'));
                break;
            case 'Review':
                stepDown.html('<br />' +I18N.message("perc.ui.workflow.steps.view@Reject") + '');
                stepUp.html('' +I18N.message("perc.ui.edit.workflow.step.dialog@Approve") + '<br />' +I18N.message("perc.ui.navMenu.publish@Publish") + '');
                percStepColumnSpacer.addClass('perc-broken-arrow');
                break;
            case 'Approved':
                stepDown.html('' +I18N.message("perc.ui.page.general@Pending") + '<br/>' +I18N.message("perc.ui.workflow.steps.view@Live") + '');
                break;
            case 'Archive':
                stepDown.text('Restore');
                break;
            default:
                stepDown.text(I18N.message("perc.ui.workflow.steps.view@Reject"));
                stepUp.text(I18N.message("perc.ui.workflow.steps.view@Submit"));
                percStepColumnSpacer.addClass('perc-twoheaded-arrow');
                percStepColumnSpacer.addClass('perc-twoheaded-arrow');
                percStepColumnSpacer.append('<div class = "perc-create-new-step" title = ' +I18N.message("perc.ui.workflow.steps.view@Add New Step") + '></div>');
                percStepColumnSpacer.attr('name', percStepColumnSpacer.prev().attr('name'));
                $(percStepColumn).find('.perc-action-button').html('<span title=' +I18N.message("perc.ui.workflow.steps.view@Configure Step") + ' class="perc-step-config-button"></span><span title=' +I18N.message("perc.ui.workflow.steps.view@Delete Step") + ' class="perc-step-delete-button"></span>');
                $(percStepColumn).find('.perc-reserved-step-config-bttn').data({"isReservedStep":false});
        }
                // Attach the workflowStep object to each config button.
                $(percStepColumn).find('.perc-step-config-button, .perc-reserved-step-config-bttn').data(
                {
                    "workflowStep" : workflowStep
                });

                // Attach the workflowName and stepName to delete button of each step
                $(percStepColumn).find('.perc-step-delete-button, .perc-reserved-step-config-bttn').data(
                {
                    "workflowName" : workflowName,
                    "stepName" : stepName
                });
    }
    function __attachEvents()
    {
        //Bind add workflow step event
        $('.perc-create-new-step').click(function(){
            __createNewStep(this);
        });
        
        // Bind delete workflow step event
        $('.perc-step-delete-button').click(function() {
            __deleteWorkflowStep(this);
        });
        
        // Bind update workflow step event
        $('.perc-step-config-button, .perc-reserved-step-config-bttn').click(function() {
            __updateWorflowStep(this);
        });
        
        // Adjust the width of step container on window resize event
        $(window).resize(function()
        {
            __calculateRowWidth();
        });
        //Bind the click event to 'more link' and 'less link'
        $('#perc-workflow-steps-container .perc-moreLink, #perc-workflow-steps-container .perc-lessLink').click(function(){
            __activateMoreLessLink(this);
        });    
    }
    
    /**
     *  Create new workflow step
     */
     
    function __createNewStep(elem) {
        var isUpdate = false;
        var previousStepName = $(elem).parent().attr('name');
        var workflowName = $("#perc-workflow-name").text();
        var currentStep = "";
        var workflowStep = "";
        var isReservedStep = false;
        var successCallBack = function()
        {
            refresh(workflowName);                
        }
        var cancelCallBack = function()
        {
            //For now there is nothing to do.
        }            
        $.PercEditWorkflowStepDialog().open(isReservedStep,isUpdate, workflowStep, workflowName,previousStepName, currentStep, successCallBack, cancelCallBack);

     }    
    
    /**
     *  Delete workflow step
     */
    function __deleteWorkflowStep(elem) { 
        var workflowName = $(elem).data("workflowName");
        var stepName = $(elem).data("stepName");            
        var settings = {
            id: 'perc-wf-step-delete',
            type: 'YES_NO',
            title: I18N.message("perc.ui.workflow.steps.view@Confirm Step Deletion"),
            question: I18N.message("perc.ui.workflow.steps.view@About To Delete")+ stepName + "'<br /><br />" + I18N.message("perc.ui.workflow.steps.view@Are You Sure Delete Step"),
            success: function () {
                $.PercBlockUI();
                $.PercWorkflowService().deleteWorkflowStep(workflowName, stepName, function(status, newWorkflowObject) {
                                        if(!status) {
                                            var errorMessage = $.PercServiceUtils.extractDefaultErrorMessage(newWorkflowObject[0]);
                                            $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.workflow.steps.view@Error Deleteing Step"),"content":errorMessage});
                                            return;
                                        }
                                        refresh(workflowName);
                                    });
                                    $.unblockUI();
                                 }
            
        };                    
        $.perc_utils.confirm_dialog(settings);
    }
    
    /**
     *  Update workflow step
     */
     
    function __updateWorflowStep(elem) {
        var isUpdate = true;
        var previousStepName = $(elem).data("workflowStep").stepName;
        var workflowName = $("#perc-workflow-name").text();
        var currentStep = $(elem).data("workflowStep").stepName;
        var workflowStep = $(elem).data("workflowStep");
        var isReservedStep = $(elem).data("isReservedStep");
        var successCallBack = function()
        {
            refresh(workflowName);
        }
        var cancelCallBack = function()
        {
            //For now there is nothing to do.
        }            
        $.PercEditWorkflowStepDialog().open(isReservedStep, isUpdate, workflowStep,workflowName,previousStepName, currentStep, successCallBack, cancelCallBack);

    }          
         
    /** 
     *Removes the special characters and replaces the spaces with '-' in the WorkflowStepName.Returns an id for
     *the element by attaching perc-workflow- in front of the name.
     */
    function __generateElementId(name){    
        var id= name.toString().toLowerCase();        
        id = id.replace(/[^a-zA-Z 0-9]+/g,'');
        id= id.replace(/ /g,'-');
        return 'perc-workflow-'+ id;
    }

    /**
     * Calculates and fixes the row width so that the archive-step block in workflow is aligned with last element in
     * the rows above.The width of step blocks and spacer together is block width.
     */
    
    function __calculateRowWidth() {
        var stepsCotainerWidth = $("#perc-wf-row-one").find('td').width();       
        $("#perc-wf-row-one").find('td').width('100%');
        var initialWidth = $("#perc-workflow-steps-container").innerWidth()- 50; // 50 = left + right padding of container.
        var blockWidth = $(".perc-workflow-step").width() + $(".perc-workflow-step-spacer").width();
        var rowWidth = Math.floor((initialWidth)/blockWidth);
        rowWidth = rowWidth*blockWidth;
        if(stepsCotainerWidth > 0 ) {
            $("#perc-wf-row-one").find('td').width(rowWidth);
        }        
    }

    /**
     * Toggle the state of more/less link.
     */    
  
    function __activateMoreLessLink(elem) {
        $(elem).parent().find('.perc-more-list, .perc-moreLink, .perc-lessLink').toggle().toggleClass('perc-hidden perc-visible');               
    }   
    
})(jQuery, jQuery.Percussion);     