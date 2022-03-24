 /******************************************************************************
 *
 * [ managenavigation.js ]
 * 
 * Javascript functions for use by managenavedit.jsp  
 * 
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
 
 
 /**
  * Creates the workflow drop-down menu for the content item, based
  * on user permissions, current workflow state and community.
  */     
 function createWorkflowDropDown()
 {
    var contentId = $('body').data("contentid");
    var url = '/Rhythmyx/contentui/aa?action=GetWorkFlowActions' +
          '&objectId=["2","' + contentId + '",null,null,null,null,null,null,null,null,null,null,null,null,null]';
        
        
         $.ajax({
             url: url,
             type: 'GET',
             success: function(data, status, xhr){
                var dropdown = $("<ul/>").addClass("perc-dropdown-options");
                var shadow = $("<div/>").addClass("perc-dropdown-shadow");
                for(i = 0; i < data.length; i++)
                {
                    
                     var entry = $("<li>").text(data[i].label);
                     entry.data("wfAction", data[i]);
                     entry.on('click',function(){
                        doWorkflowAction($(this).data("wfAction"));
                        dropdown.hide();
                        shadow.hide();
                        });
                     entry.on('mouseover',function(){$(this).addClass("perc-entry-over");});
                     entry.on('mouseout',function(){$(this).removeClass("perc-entry-over");});
                     dropdown.append(entry);
                }
                dropdown.hide();
                $("body").append(dropdown);
                
                shadow.hide();
                $("body").append(shadow);
             }
         });
 }
 
 /**
  * Invokes the workflow action specified, if necessary displays a
  * dialog to capture a comment and/or ad-hoc users.
  * @param action an action object returned from the server, assumed not
  * <code>null</code>.
  */         
 function doWorkflowAction(action)
 {
    var hasComment = parseInt(action.comment) > 0;
    var commentRequired = parseInt(action.comment) == 2;
    var hasAdhoc = parseInt(action.adhoc) == 1;
    if(hasComment || hasAdhoc)
    {
       hasComment ? $(".comment-group").show() : $(".comment-group").hide();
       hasAdhoc ? $(".adhoc-group").show() : $(".adhoc-group").hide();
       var dialog = $( ".perc-workflow-dialog" );
       dialog.data("wfAction", action);
       dialog.dialog("option", "title", action.label);
       dialog.dialog("open");
    }
    else
    {
       doWorkflowActionCall(action, null, null, function(){self.close();});
    }
 }
 
 /**
  * Makes the actual ajax call to the server for the workflow action passed
  * in. 
  * @param action
  * @param comment may be <code>null</code>.
  * @param adhocusers may be <code>null</code>.
  * @param callback function that is called upon success.
  */             
 function doWorkflowActionCall(action, comment, adhocusers, callback)
 {
    var url = "/Rhythmyx/contentui/aa?action=Workflow";
    var data = {"contentId": $('body').data("contentid")};
    if(comment != null)
       data.comment = comment;
    if(adhocusers != null)
       data.adHocUsers = adhocusers;
    switch(parseInt(action.actiontype))
    {
       case 0: // Checkin
          data.operation = "checkIn";
          break;
       case 1: // Force-checkin
          data.operation = "checkIn";
          break;
       case 2: // Checkout
          data.operation = "checkOut";
          break;
       case 3: //Transition and Checkout
          data.operation = "transition_checkout";
          data.triggerName = action.actionname;
          break;
       case 4: //Transition
          data.operation = "transition";
          data.triggerName = action.actionname;
          break;
    }
    if(typeof(data.operation) != 'undefined' && data.operation.length > 0)
    {
          $.ajax({
             url: url,
             type: 'POST',
             data: data,
             success: function(data, status, jqXHR){callback(data, status, jqXHR)},
             error: function(jqXHR, status, error){alert("An error occurred while attempting workflow action.");}
         });
    }
 } 
   
 function showWorkflowDropDown()
 {
    var wfButton = $(".workflow-button");
    var dropdown = $(".perc-dropdown-options");
    var shadow = $(".perc-dropdown-shadow");
    var offset = wfButton.offset();
    var top = offset.top + wfButton.height() - 2;
    var left = offset.left + 3;
    dropdown.css({top: top, left: left});
    dropdown.show();
    shadow.css({top: top + 2, left: left + 2, height: dropdown.height(), width: dropdown.width()});
    shadow.show();
    
 }
 
 function hideWorkflowDropDown()
 {
     $(".perc-dropdown-options").hide();
     $(".perc-dropdown-shadow").hide();
 }
          
 