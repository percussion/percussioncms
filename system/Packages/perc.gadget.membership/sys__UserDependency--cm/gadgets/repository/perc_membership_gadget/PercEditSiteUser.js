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
