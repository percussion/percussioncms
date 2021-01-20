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
(function($){

    var itemsPerPage;
    var tableDiv;
    var siteName;

    $.fn.PercMembershipGadget = function(site,rows) {
        siteName=site;
        itemsPerPage = rows;
        tableDiv = $(this);
        $(this).empty();
        // never show a scrollbar in the gadget
        $("body").css("overflow","hidden");

        $.PercMembershipGadget.load();
    }

    $.PercMembershipGadget = {
        load : function(){
            if(siteName==""){
                var sites = [];
                $(".perc-listing-type-site", window.parent.document).each(function() {
                    sites.push($(this).find("div.perc-finder-item-name").html());
                });
                siteName = sites[0];
            }
            $.PercMembershipService.getAccounts(siteName,function(status, data){
                if(status == $.PercServiceUtils.STATUS_SUCCESS){
                    var accounts = (typeof(data.getUsersResponse.userSummaries) != "undefined")?data.getUsersResponse.userSummaries:[];
                    $.PercMembershipGadget.updateMembershipTable($.perc_utils.convertCXFArray(accounts));
                }
                else {
                    $.PercMembershipGadget.updateMembershipTable([], I18N.message("perc.ui.gadgets.membership@No Accounts Found Long"));
                }
            });
        },
        updateMembershipTable : function(data, message){
            var percData = [];
            var percMenu =  [];
            var isLargeColumn = gadgets.window.getDashboardColumn() == 1; // if the gadget is in first column then we have to render it as large
            for(var b=0; b<data.length; b++){
                var account = data[b];
                var formattedDate = "";
                var formattedTime = "";
                if(account.createdDate != "") {
                    var lastPublishedDateParts = $.perc_utils.splitDateTime(account.createdDate);
                    formattedDate = lastPublishedDateParts.date;
                    formattedTime = lastPublishedDateParts.time;
                }

                var row = {
                    rowData : {account:account.email, created:account.createdDate, groups:account.groups, status:account.status},
                    rowContent : [
                        [
                            {    content : account.email,
                                 title : account.email,
                                 callback : function() {}
                            }
                        ],
                        [
                            {    content:formattedDate
                            },
                            {    content:formattedTime
                            }
                        ],
                        [
                            {content:account.groups,
                            title : account.groups}
                        ],
                        [
                            {content:account.status}
                        ]
                    ]};

                // Update menu entry for each account based on its status
                if(account.status == 'Blocked') {
                    var percItemMenu = { title : "", menuItemsAlign : "left", stayInsideOf : ".perc-datatable",
                                items : [
                                    {label : I18N.message("perc.ui.gadgets.membership@Edit User"), callback : editUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Activate"),   callback : activateUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Delete"), callback : deleteUser}
                                ]};
                }
                else if(account.status == 'Active') {
                    var percItemMenu = { title : "", menuItemsAlign : "left", stayInsideOf : ".perc-datatable",
                                items : [
                                    {label : I18N.message("perc.ui.gadgets.membership@Edit User"), callback : editUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Block"),    callback : blockUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Delete"), callback : deleteUser}
                                ]};
                }
                else if(account.status == 'Unconfirmed') {
                    var percItemMenu = { title : "", menuItemsAlign : "left", stayInsideOf : ".perc-datatable",
                                items : [
                                    {label : I18N.message("perc.ui.gadgets.membership@Edit User"), callback : editUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Activate"),    callback : activateUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Block"),    callback : blockUser},
                                    {label : I18N.message("perc.ui.gadgets.membership@Delete"), callback : deleteUser}
                                ]};

                }
                percData.push(row);
                percMenu.push(percItemMenu);
            }

            var percVisibleColumns = null;
            if(!isLargeColumn)
            percVisibleColumns = [0,3];

            var config = {
                percColumnWidths : ["*","120", "120", "90"],
                aoColumns : [{ sType: "string"},{ sType: "date"}, { sType: "string"}, { sType: "string"}],
                iDisplayLength : itemsPerPage,
                percHeaders : [I18N.message("perc.ui.gadgets.membership@Login Name"), I18N.message("perc.ui.gadgets.membership@Created"), I18N.message("perc.ui.gadgets.membership@Groups"), I18N.message("perc.ui.gadgets.membership@Status")],
                percData : percData,
                percVisibleColumns : percVisibleColumns,
                oLanguage : { sZeroRecords: (message!= null)? message : I18N.message("perc.ui.gadgets.membership@No Accounts Found Short")},
                percMenus :  percMenu
            };

           /**
            * Bind the Edit User click event
            */
            function editUser(event) {
               $.PercEditSiteUser(siteName,event.data, function(data){
                        tableDiv.empty();
                        var accounts = (typeof(data.getUsersResponse.userSummaries) != "undefined")?data.getUsersResponse.userSummaries:[];
                       $.PercMembershipGadget.updateMembershipTable($.perc_utils.convertCXFArray(accounts));
               });
            }

           /**
            * Bind the Activate User click event
            */
            function activateUser(event) {
                var data = event.data;
                var userObj = {"AccountSummary":{"email":data.account, "action":"Activate"}};
                $.PercMembershipService.activateUser(siteName,userObj, function(status, data){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS){
                        tableDiv.empty();
                        var accounts = (typeof(data.getUsersResponse.userSummaries) != "undefined")?data.getUsersResponse.userSummaries:[];
                       $.PercMembershipGadget.updateMembershipTable($.perc_utils.convertCXFArray(accounts));
                    }
                });
            }

           /**
            * Bind the Block user click event
            */
            function blockUser(event) {
                var data = event.data;
                var userObj = {"AccountSummary":{"email":data.account, "action":"Block"}};
                $.PercMembershipService.activateUser(siteName,userObj, function(status, data){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS){
                        tableDiv.empty();
                        var accounts = (typeof(data.getUsersResponse.userSummaries) != "undefined")?data.getUsersResponse.userSummaries:[];
                       $.PercMembershipGadget.updateMembershipTable($.perc_utils.convertCXFArray(accounts));
                    }
                });
            }

           /**
            * Bind the Delete user click event
            */
            function deleteUser(event) {
                var data = event.data;
                var userEmail = data.account;
                var settings = {
                id: 'perc-delete-user',
                type: 'YES_NO',
                title: I18N.message("perc.ui.gadgets.membership@Confirm User Deletion"),
                question: I18N.message("perc.ui.gadgets.membership@You are about to delete the user") + ": '"+ userEmail + "'<br /><br />" + I18N.message("perc.ui.gadgets.membership@Delete user confirmation") + "?",
                success: function () {$.PercMembershipService.deleteUser(siteName,userEmail, function(status, data){
                                            if(status == $.PercServiceUtils.STATUS_SUCCESS){
                                                tableDiv.empty();
                                                var accounts = (typeof(data.getUsersResponse.userSummaries) != "undefined")?data.getUsersResponse.userSummaries:[];
                                               $.PercMembershipGadget.updateMembershipTable($.perc_utils.convertCXFArray(accounts));
                                            }
                                        });
                                     }
                };
               window.parent.jQuery.perc_utils.confirm_dialog(settings);
            }
            miniMsg.dismissMessage(loadingMsg);
            tableDiv.PercActionDataTable(config);
            miniMsg.dismissMessage(loadingMsg);
        }
    };

})(jQuery);
