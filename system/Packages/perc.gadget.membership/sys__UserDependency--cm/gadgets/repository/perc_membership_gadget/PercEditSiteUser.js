/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 *  PercActionDataTable.js
 *  @author Jose Annunziato
 */
(function($) { 

$.PercEditSiteUser = function(site, userData, successCalBack)
{
	var dialogHTML = createDialogHtml();
    var siteName  = site;

    var dialog = percJQuery(dialogHTML).perc_dialog( {
                resizable : false,
                title: I18N.message("perc.ui.gadgets.membership@Edit User"),
                modal: true,
                closeOnEscape : true,
                percButtons:{
                        [I18N.message("perc.ui.common.label@Save")] :{
                        	click: function(){
								saveUserData();
							},
	                        id: "perc-edituser-dialog-save"
                    },
	                    [I18N.message("perc.ui.common.label@Cancel")] :{
	                       click: function(){
	                            dialog.remove();
	                        },
	                        id: "perc-edituser-dialog-cancel"
	                }
                },
                id: "perc-edituser-dialog",
                width: 450,
                height: 320
     });
	
	function saveUserData()
	{
        var groups = window.parent.jQuery("#perc_user_groups").val();
		var userObj = {"UserGroup":{"email":userData.account, "groups":groups}};

        if(siteName===""){
            var sites = [];
            $(".perc-listing-type-site", window.parent.document).each(function() {
                sites.push($(this).find("div.perc-finder-item-name").html());
            });
            siteName = sites[0];
        }
        $.PercMembershipService.saveUser(siteName,userObj, function(status, data){
	        if(status === $.PercServiceUtils.STATUS_SUCCESS){
				dialog.remove();
                successCalBack(data);
	        }
			else
			{
				window.parent.jQuery.perc_utils.alert_dialog(data);
			}
		});		
	}
	function createDialogHtml()
	{
    	var dialogHtml =	'<div>' +  	
								'<div id="perc_user_label">' +
									`<label class = "perc_dialog_label" >${I18N.message("perc.ui.common.label@Cancel")}:</label>` +
							  	'</div>' +
								'<div id="perc_user">' +
									'<label style = "font-weight:bold" class = "perc_dialog_input perc_dialog_field">' + userData.account + '</label>' +
								'</div>' +
                                '<br />' +
								'<div id="perc_groups_label" class="">' +
									`<label class = "perc_dialog_label" >${I18N.message("perc.ui.common.label@Groups")}:</label>` +
								'</div>' +
								'<div id="perc_groups">' +
									'<input type="text" id="perc_user_groups" class = "perc_dialog_input perc_dialog_field" name="perc_user_groups" maxlength="4000" value="' + userData.groups +'"/><br />' +
                                    I18N.message("perc.ui.editSectionDialog.label@Please use a comma to separate each group name") +
								'</div>' +
							'</div>';
		return dialogHtml;
		
	}
	
};

})(jQuery); 
