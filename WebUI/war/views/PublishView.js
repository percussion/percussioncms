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

/***Helper function for view component of Publish page
 *** Since it doesn't have massive interaction, there's no need for controller,
 *** so view is directly being invoked
 */
(function($)
{
    var siteName;
    var siteId;
    var serverName;
    var serverId;
    var defaultServer;
    var PUB_SERVERS = 0;
    var PUB_REPORTS = 1;
    var service = $.PercPublisherService(false);
    this.utilService = $.PercUtilService;
    var dirtyController = $.PercDirtyController;
    var editorRenderer = $.PercEditorRenderer;
    var serverProperties = {};
    var isNewServer = false;
    var serverType = "PRODUCTION";
    var serversIds = [];
    
    siteName = $.PercNavigationManager.getSiteName();
    
    /****************** Binding onclick events with all the widgets and buttons toolbar ************************/
    $(document).ready(function()
    {
    
        $("#tabs").tabs({
            selected: 0,
            show: function()
            {
                fixBottomHeight();
            },
            select: function(event, ui)
            {
                if (dirtyController.isDirty()) 
                {
                    // if dirty, then show a confirmation dialog                    
                    dirtyController.confirmIfDirty(function()
                    {
                        // if they click ok, then reset dirty flag and proceed to select the tab
                        dirtyController.setDirty(false);
                        cancel();
                        $("#tabs").tabs('select', ui.index);
                    });
                    return false;
                }
                if (ui.index == PUB_SERVERS) 
                {
                    // Load the servers details for selected site                      
                    loadSiteServers();
                }
                if (ui.index == PUB_REPORTS) 
                {
                    $("#perc-servers").html('');
                    getServersList(function(servers)
                    {
                        var serversListContainer = $("#perc-servers");
                        $("#perc-servers").append($('<option></option>').val(' ').html('All'));
                        $.each(servers, function()
                        {
                            //TODO - Do appending outside the loop
                            $("#perc-servers").append($('<option></option>').val(this.serverId).html(this.serverName));
                        });
                        
                        //Refresh the Publishing log section(if its open) after Publish 
                        var pubLogContainer = $("#perc-publish-jobs-widget").find(".perc-foldable");
                        if (pubLogContainer.hasClass('perc-opened')) 
                        {
                            pubLogContainer.trigger("click").trigger("click");
                        }
                    });
                }
            }
        });
        
        // Bind the click even to Edit Server Button
        $("#perc-servers-wrapper").on('click', '#perc-server-edit',
            function(evt){
                loadServerEditor(evt);
            })
            .on('click', '#perc-define-save',
                function(evt){
                    save(evt);
                });

        $("#perc-servers-container").on('click', '#perc-define-cancel',
            function(evt){
                cancel(evt);
            })
            .on('click', '#perc_wizard_cancel', function(evt){
                cancel(evt);
            })
            .on('click', '#perc-publish-now', function(evt){
                    publishServer(evt);
            })
            .on('click', '#perc-full-publish-now',
                function(evt){
                    publishServer(evt);
                });

        if (siteName != null) 
        {
            //Load selected site servers details     
            //Then get List of servers List and highlight the default     
            loadSiteServers(function()
            {
                editorRenderer().loadLocalFolderPath(siteId);
                renderServerList();
            });
        }
    });
    
    //Clear any existent validation messages     
    function clearValidationMessages()
    {
        $("#perc-servers-container").find('span.perc_field_error').remove();
    }
    
    // Bind click event on Add Server button    
    function addPubServer()
    {
    	serverType = "PRODUCTION";
    	if($("#perc-servers-list").find(".perc-staging-server-marker").length < 1){
        	showServerTypePrompt().done(function(flag){
            	serverType = flag;
        		addServer();
            }).fail(function(){
            	//Do nothing user cancelled out of the create server.
            });
    	}
    	else{
    		addServer();
    	}
  
    }

    // Bind click event on Add Server button    
    function addServer()
    {
        var serverName = '';
        var serverProperties = {};
        var container = $("#perc-servers-list");
        isNewServer = true;
        dirtyController.setDirty(true);
        $.PercDataList.disableButtons(container);
        $.PercDataList.unhighlightAllItems(container);
        editorRenderer().renderEditor(serverName, serverProperties, serverType);
  
    }
    
    function showServerTypePrompt(){
        var deferred = $.Deferred();
        var dialog;
        var selection = "PRODUCTION";
        $.unblockUI();
        dialog = $(createDialogHtml(dialog)).perc_dialog( {
             title: I18N.message("perc.ui.publish.view@Create Publishing Server"),
             modal: true,
             percButtons:{
            	 "Ok":{
            		 click: function()   {
        			     dialog.remove();
            			 deferred.resolve(selection);
            		 },
            		 id:"perc-selection"
            	 },
            	 "Cancel":{
            		 click: function(){
            			 dialog.remove();
            			 deferred.reject();
            		 },
            		 id:"perc-cancel"
            	 }
             },
            id: "perc-server-type-dialog",
            width: 700
        });
        dialog.find("#perc-staging-server-button").on("click", function(evt){
            //dialog.remove();
        	$(".perc-text-button-selected").removeClass("perc-text-button-selected");
        	dialog.find("#perc-staging-server-button").addClass("perc-text-button-selected");
            selection = "STAGING";
        });
        dialog.find("#perc-production-server-button").on("click", function(evt){
            //dialog.remove();
        	$(".perc-text-button-selected").removeClass("perc-text-button-selected");
        	dialog.find("#perc-production-server-button").addClass("perc-text-button-selected");
        	selection = "PRODUCTION";
        });
        return deferred.promise();
    }


    //creates a staging dialog
    function createDialogHtml(dialog){
        var dialogHtml = $("<div/>",{"id":"perc-prompt-server-type-container"});
        dialogHtml.append('<div><label>' +I18N.message("perc.ui.publish.view@Select Publishing server") + '</label></div>');
        dialogHtml.append("<div><button id='perc-staging-server-button' name='perc-staging-server-button' class='btn btn-primary perc-text-button' type='button'>" +I18N.message("perc.ui.publish.view@Staging") + "</button><button id='perc-production-server-button'  name='perc-production-server-button' class='btn btn-primary perc-text-button perc-text-button-selected' type='button'>" +I18N.message("perc.ui.publish.view@Production") + "</button></div>");
        return dialogHtml;
    }

    //Bind click event on Delete Server Button
    function deleteServer()
    {
        var previousServerName = $(".perc-itemname[title='" + serverName + "']").prev().attr('title');
        if (typeof(previousServerName) == 'undefined') 
            previousServerName = $(".perc-itemname[title='" + serverName + "']").next().attr('title');
        var settings = {
            id: 'perc-server-delete',
            title: I18N.message("perc.ui.publish.view@Confirm Server Deletion"),
            type: 'YES_NO',
            question: I18N.message("perc.ui.publish.view@About To Delete Server") + serverName + "'<br /><br />" + I18N.message("perc.ui.publish.view@Are You Sure Delete Server"),
            success: function()
            {
                $.PercPublisherService().deleteSiteServer(siteId, serverId, function(status, result)
                {
                    if (status) 
                    {
                        var loadServer = JSON.parse(result[0]).serverInfo.serverName;
                        //Get List of servers List and highlight the default         
                        renderServerList(previousServerName);
                        
                        dirtyController.setDirty(false);
                    }
                    else 
                    {
                        var defaultMsg = result;
                        $.perc_utils.alert_dialog({
                            content: defaultMsg,
                            title: I18N.message("perc.ui.publish.title@Error")
                        });
                        return;
                    }
                });
            }
        };
        $.perc_utils.confirm_dialog(settings);
        
    }
    
    /**
     * Load Server Editor in Edit mode
     */
    function loadServerEditor(evt)
    {
        var container = $("#perc-servers-list");
        isNewServer = false;
        dirtyController.setDirty(true);
        $.PercDataList.disableButtons(container);
        
        // get the properties for selected server
        service.getServerProperties(siteId, serverId, function(status, result)
        {
            if (status) 
            {
                editorRenderer().renderEditor(serverName, result[0], result[0].serverInfo.serverType);
            }
            else 
            {
            
            }
        });
    }
    
    /**
     * Load the properties for selected server in read only mode
     */
    function loadReadOnlyEditor()
    {
    
        $("#perc-editor-form").html('');
        // get the properties for selected server
        service.getServerProperties(siteId, serverId, function(status, result)
        {
            if (status) 
            {
                serverProperties = result[0];
                //Capture the serverId as a global varialbe to this file
                serverId = serverProperties.serverInfo.serverId;
                var propMap = _getPropertyMap(serverProperties.serverInfo.properties);
                
                var driver = propMap["driver"];
                $("#perc-editor-summary").load('../app/includes/serverEditors/propReadOnlyEditor.jsp?editorName=' + driver, function()
                {
                    $("#perc-server-name").text(serverName);
                    $("#perc-server-name-wrapper").show();
                    editorRenderer().setReadOnlyProperties(serverProperties);
                    
                    //Set the Format value based on Radio flag
                    if (driver == 'FTP' || driver == 'Local') 
                    {
                        var isHTML = propMap["HTML"];
                        if (isHTML) 
                        {
                            $("#perc-editor-prop").find("span[percName ='format']").text('HTML');
                        }
                        else 
                        {
                            $("#perc-editor-prop").find("span[percName ='format']").text('XML');
                        }
                    }
                    //Convert the true/false to Yes/No for Secure FTP
                    if (driver == 'FTP') 
                    {
                        var isSecure = propMap["secure"];
                        if (isSecure) 
                        {
                            $("#perc-editor-prop").find("span[percName ='secure']").text('Yes');
                        }
                        else 
                        {
                            $("#perc-editor-prop").find("span[percName ='secure']").text('No');
                        }
                    }
                    //add last publish dates
                    _addLastPublishDates(serverProperties.serverInfo);
                    //Add publish buttons
                    _addPublishButtons(serverProperties);                    
                    var publishButton = $("#perc-servers-container").find('#perc-publish-now');
                    if (serverProperties.serverInfo.isModified) 
                    {
                        publishButton.addClass("perc-disabled");
                        $(".perc-warning-message").show();
                        $("#perc-default-server").addClass("perc-disabled");
                    }
                    else 
                    {
                        publishButton.removeClass("perc-disabled");
                        $(".perc-warning-message").hide();
                        $("#perc-default-server").removeClass("perc-disabled");
                    }

                });
            }
            else 
            {                
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.publish.view@Server No Longer Exists"),
                    title: I18N.message("perc.ui.publish.title@Error"),
                    okCallBack: function()
                    {
                        renderServerList();
                    }
                });
            }
            
        });
        
    }
    
    /**
     * Helper function to add publishing buttons.
     * @param {Object} serverProperties assumed to be a valid server properties object.
     */
    function _addPublishButtons(serverProperties){
        if(serverProperties.serverInfo.canIncrementalPublish && !serverProperties.serverInfo.isModified){
            if(serverProperties.serverInfo.isFullPublishRequired){
                $("#perc-publish-button").append("<button id='perc-full-publish-now' class='btn btn-primary' style=''>" +I18N.message("perc.ui.publish.view@Full Publish")+ "</button>");
            }
            else{
                _addSplitPublishButton(serverProperties);
            }
        }
        else{
            $("#perc-publish-button").append("<div id='perc-publish-now' class='perc-publish' style=''></div>");
        }
    }
    
    /**
     * Helper function adds the full publish date to the property list, if the property is not available then sets the value as -----
     * Also adds incremental publish date, if canIncrementalPublish is true and if incremental publish property is not available then sets the value as -----
     * @param {Object} serverInfo assumed not null.
     */
    function _addLastPublishDates(serverInfo){
        var lastFullPublish = "-----";
        if(serverInfo.lastFullPublishDate){
            var dateParts = $.perc_utils.splitDateTime(serverInfo.lastFullPublishDate);
            lastFullPublish = dateParts.date + ", " + dateParts.time;
        }
        var pubDates = "<li>" +
                            "<span class = 'perc-prop-name'>" +I18N.message("perc.ui.publish.view@Last Full Publish") + "</span>" +
                            "<span percName ='fullpublishdate' class = 'perc-prop-value'>" + lastFullPublish + "</span>" +
                        "</li>";
        if(serverInfo.canIncrementalPublish){
            var lastIncPublish = "-----";
            if(serverInfo.lastIncrementalPublishDate){
                var dateParts = $.perc_utils.splitDateTime(serverInfo.lastIncrementalPublishDate);
                lastIncPublish = dateParts.date + ", " + dateParts.time;
            }
            pubDates += "<li>" +
                                "<span class = 'perc-prop-name'>" +I18N.message("perc.ui.publish.view@Last Incremental Publish") + "</span>" +
                                "<span percName ='incpublishdate' class = 'perc-prop-value'>" + lastIncPublish + "</span>" +
                            "</li>";
        }
        $("#perc-editor-prop-container").append(pubDates);        
    }
    /**
     * Helper function to add publishing split button.
     */
    function _addSplitPublishButton(serverProperties){
        var dropdownLabels = [I18N.message("perc.ui.publish.view@Incremental Publish"), I18N.message("perc.ui.publish.view@Full Publish")];
        var dropdownParams = [];
        var dropdownActions = [incrementalPublish, publishServer];
        var dropdownButtonImage = '/cm/images/images/splitButtonArrow.gif';
        var dropdownButtonImageOver = '/cm/images/images/splitButtonArrowOn.gif';
        var publishDropdown = $("#perc-publish-button").addClass("perc-publish-split-button");
        publishDropdown.append($('<a />').attr('href', '#').html('&nbsp;').css('display', 'inline-block').addClass('perc-publish-split-button-incremental')).append($('<div />').addClass('perc-publish').css('display', 'inline-block'));
        publishDropdown.children('div').eq(0).PercDropdown({
            percDropdownRootClass: "perc-publish",
            percDropdownOptionLabels: dropdownLabels,
            percDropdownCallbacks: dropdownActions,
            percDropdownCallbackData: dropdownParams,
            percDropdownTitleImage: dropdownButtonImage,
            percDropdownTitleImageOver: dropdownButtonImageOver,
            percDropdownShowExpandIcon: false,
            percDropdownResizeToElement: "#perc-dropdown-publish"
        });
        publishDropdown.find('.perc-dropdown-title').off('click');
        var propMap = _getPropertyMap(serverProperties.serverInfo.properties);
        var showRelated = propMap.publishRelatedItems;
        publishDropdown.children('a').on("click", function()
        {
            showIncrementalPreview(showRelated);
        });
        var tootltiptext = I18N.message("perc.ui.publish.view@Publish Tip");
        var infoIcon = $("<a style=\"display:inline-block;float:right;\" title=\"" + tootltiptext + "\"><span id=\"perc-publish-button-help\" style=\"margin-top: 3px;background-color: #e6e6e9;color:#a1a1a1;\" class=\"perc-font-icon icon-info-sign\"></span></a>");
        infoIcon.tooltip({
            delay: 500,
            left:-25,
            top:25,
            bodyHandler: function(){
                 return "<p style='padding:5px 20px 5px 20px;'>" + tootltiptext + "</p>";   
            }
        });
        $("#tooltip").css("z-index",9999);
        $("#perc-publish-button").append(infoIcon);
    }
    
    //Build a sever property map.
    function _getPropertyMap(properties)
    {
        var propertyMap = [];
        var serverProperties = [];
        if (!Array.isArray(properties))
        {
            serverProperties.push(properties);
        }
        else 
        {
            serverProperties = properties;
        }
        $.each(serverProperties, function(name, value)
        {
            var propertyName = '';
            var popertyValue = '';
            $.each(this, function(propName, value)
            {
                if (propName === 'key')
                {
                    propertyName = value;
                }
                if (propName === 'value')
                {
                    propertyValue = value;
                }
            });
            propertyMap[propertyName] = propertyValue;
        });
        return propertyMap;
    }
    
    /**
     * Select Server and load properties in readonly mode
     */
    function selectServer(sName)
    {
        serverName = sName;
        var container = $("#perc-servers-list");
        $.PercDataList.selectItem(container, serverName);
        $.PercDataList.enableButtons(container);
        // Capture the serverId as a global varialbe.
        serverId = serversIds["perc-" + serverName];
        if (serverName == defaultServer.name) 
        {
            $(".perc-item-delete-button").off().addClass('perc-item-disabled');
        }
        else 
        {
            $(".perc-item-delete-button").removeClass('perc-item-disabled').off("click").on("click", function(evt)
            {
                deleteServer();
            });
        }
        loadReadOnlyEditor();
    }
    
    //Publish the site using selected server
    function publishServer(evt)
    {
        if (serverProperties.serverInfo.isModified) 
            return;
        $.PercBlockUI();
        $.PercPublisherService().publishSite(siteName, serverName, function(status, result)
        {
            $.unblockUI();
            if (result[1] == "success") 
            {
                var status = JSON.parse(result[0]).SitePublishResponse.status;
                var msg = "";
                if (status === $.PercPublisherService().PUBLISHER_JOB_STATUS_FORBIDDEN) 
                {
                    msg = I18N.message("perc.ui.publish.errordialog.message@Publish Not Allowed");
                }
                else if (status === $.PercPublisherService().PUBLISHER_JOB_STATUS_BADCONFIG) 
                {
                    msg = I18N.message("perc.ui.publish.errordialog.message@Bad configuration");
                }
                else 
                {
                    msg = I18N.message("perc.ui.publish.view@Server") + serverName + I18N.message("perc.ui.publish.view@Started Publishing");
                }
                
                $.perc_utils.alert_dialog({
                    content: msg,
                    title: I18N.message("perc.ui.publish.view@Server Publish"),
					id: "perc-publish-success"
                });
            }
            else 
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.publish.view@Unable Publish Server") + serverName,
                    title: I18N.message("perc.ui.publish.view@Server Publish"),
					id: "perc-publish-failure"
                });
            }
        });
    }
    
    //Show the incremental publishing preview list dialog.
    function showIncrementalPreview(showRelated){
        $.PercIncrementalPreviewDialog.open(siteName, serverName, showRelated, function(action){
            if(action == $.PercIncrementalPreviewDialog.CONTINUE){
                incrementalPublish();
            }
        });
    }
    
    function incrementalPublish(){
        if (serverProperties.serverInfo.isModified) 
            return;
        $.PercBlockUI();
        $.PercPublisherService().incrementalPublishSite(siteName, serverName, function(status, result)
        {
            $.unblockUI();
            if (status) 
            {
                var respStatus = JSON.parse(result[0]).SitePublishResponse.status;
                var msg = "";
                if (respStatus === $.PercPublisherService().PUBLISHER_JOB_STATUS_FORBIDDEN) 
                {
                    msg = I18N.message("perc.ui.publish.errordialog.message@Publish Not Allowed");
                    $.perc_utils.alert_dialog({
                        content: msg,
                        title: I18N.message("perc.ui.publish.view@Server Publish"),
                        id: "perc-publish-success"
                    });
                }	
                else if (respStatus === $.PercPublisherService().PUBLISHER_JOB_STATUS_BADCONFIG) 
                {
                    msg = I18N.message("perc.ui.publish.errordialog.message@Bad configuration");
                    $.perc_utils.alert_dialog({
                        content: msg,
                        title: I18N.message("perc.ui.publish.view@Server Publish"),
                        id: "perc-publish-success"
                    });
                }
                
              
            }
            else 
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.publish.view@Unable Publish Server") + serverName,
                    title: I18N.message("perc.ui.publish.view@Server Publish"),
                    id: "perc-publish-failure"
                });
            }
        });
    }
    
    /**
     * Get servers list for a given site
     * @param callback: callback function
     */
    function getServersList(callback)
    {
        service.getServersList(siteId, function(status, result)
        {
            if (status) 
            {
                var serverProperties = JSON.parse(result[0]);
                var servers = [];
                if (!Array.isArray(serverProperties.serverInfo))
                {
                    servers.push(serverProperties.serverInfo);
                    servers = serverProperties.serverInfo;
                }
                else 
                {
                    servers = serverProperties.serverInfo;
                }
                if (callback) 
                {
                    callback(servers);
                }
                
            }
            else 
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.publish.view@Unable To Get List Of Servers") + siteName,
                    title: I18N.message("perc.ui.publish.title@Error")
                });
            }
        });
        
    }
    
    /**
     * Render the list of the available servers for a given site
     */
    function renderServerList(loadServer)
    {
        getServersList(function(servers)
        {
            var serversArray = [];
            var stagingServer = null;
            $.each(servers, function()
            {
                serversArray.push(this.serverName);
                if (this.isDefault) 
                {
                    defaultServer = {
                        "name": this.serverName,
                        "isModified": this.isModified
                    };
                }
                
                if(this.serverType == "STAGING")
                {
                	stagingServer = this.serverName;
                }
                
                serversIds["perc-" + this.serverName] = this.serverId;
            });
            $("#perc-servers-list").html("");
            // Pass the config data to dataItem plugin to render the list of servers
            var container = $("#perc-servers-list");
            var dataListConfig = {
                listItem: [],
                title: I18N.message("perc.ui.publish.view@Servers"),
                addTitle: I18N.message("perc.ui.publish.view@Add New Server"),
                deleteTitle: I18N.message("perc.ui.publish.view@Delete Server"),
                enableDelete: true,
                collapsible: true,
                createItem: addPubServer,
                deleteItem: deleteServer,
                selectedItem: selectServer,
                truncateEntries: true,
                truncateEntriesCount: 10
            };
            $.PercDataList.init(container, dataListConfig);
            $.PercDataList.updateList(container, serversArray);
            //Select the first server by default
            if (loadServer) 
            {
                selectServer(loadServer);
            }
            else 
            {
                selectServer(serversArray[0]);
            }
            
        	if(stagingServer){
	            var stagingServerItem = container.find(".perc-itemname[title='" + stagingServer + "']");
	            stagingServerItem.addClass('perc-staging-server-marker');
        	}
            if (typeof(defaultServer) != "undefined") 
            {
                var defaultServerItem = container.find(".perc-itemname[title='" + defaultServer.name + "']");
                defaultServerItem.addClass('perc-default-server-marker');
                if (defaultServer.isModified) 
                {
                    defaultServerItem.addClass('perc-disabled');
                }
            }

        });
    }
    
    function loadSiteServers(callback)
    {
        service.getSitePublishProperties(siteName, function(status, defineDataJson)
        {
            if (status) 
            {
                var defineData = defineDataJson.SitePublishProperties;
                if (defineData != null) 
                {
                    siteId = defineData.id;
                    $("#perc-site-id").val(defineData.id).data('secureSite', defineData.secure);
                }
                if (typeof(callback) == "function") 
                    callback();
            }
            else 
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.publish.view@Unable Retrieve Site Properties"),
                    title: I18N.message("perc.ui.publish.view@Site Properties")
                });
            }
        });
        
    }
    
    /** Validate the fields
     *
     */
    validateFields = function(errorObj)
    {
        if (errorObj.error === "Bad Request")
        {
            var badFields = JSON.parse(errorObj.request.responseText).ValidationErrors.fieldErrors;
            var badFieldsArray = [];
            if (!Array.isArray(badFields))
            {
                badFieldsArray.push(badFields);
            }
            else 
            {
                badFieldsArray = badFields;
            }
            $.each(badFieldsArray, function()
            {
                var fieldName = this.field;
                var errorMessage = this.defaultMessage;
                $("#perc-servers-container").find("*[name=" + fieldName + "]").parent().append("<span class = 'perc_field_error'>" + errorMessage + "</span>");
            });
        }
        else 
        {
            var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(errorObj.request);
            if (defaultMsg == I18N.message("perc.ui.publish.view@Server No Longer Exists") || defaultMsg == I18N.message("perc.ui.publish.view@One Staging Server Per Site")) 
            {
                $.perc_utils.alert_dialog({
                    content: defaultMsg,
                    title: I18N.message("perc.ui.publish.title@Error"),
                    okCallBack: function()
                    {
                        renderServerList();
                    }
                });
            }
            else 
            {
                $.perc_utils.alert_dialog({
                    content: defaultMsg,
                    title: I18N.message("perc.ui.publish.title@Error")
                });
            }
        }
    };
    
    //Cancel the pending changes and switch to publish mode
    function cancel()
    {
        //Cancel the changes
        dirtyController.setDirty(false);
        //Reload the original values the Selected Site server
        renderServerList(serverName);
    }
    
    /* Save the FTP information
     Read the values from the FTP fields
     Call the service to update the properties of the site
     */
    save = function()
    {
        if (isNewServer) 
        {
            sId = '';
            server = '';
        }
        else 
        {
            sId = serverId;
            server = serverId;
        }
        clearValidationMessages();
        //Set the publish type
        var selectedSite = siteName;
        if (selectedSite != null) 
        {
            var container = $("#perc-servers-container");
            var newserverName = container.find('input[percName="serverName"]').val();
            var isDefault = container.find('input[percName="isDefault"]').is(':checked');
            var type = container.find('select#publishType').val();
            var ignoreUnModifiedAssets = container.find('input[percName="ignoreUnModifiedAssets"]').is(':checked');
            var publishRelatedItems = container.find('input[percName="publishRelatedItems"]').is(':checked');
            if (type == 'Select') 
            {
                type = '';
            }
            var driver = container.find('select#perc-driver').val();
            if (driver == 'Select') 
            {
                driver = '';
            }
            //crawl through all properties and create an array of propertis            
            var serverProp = function()
            {
            
                var propFields = $("#perc-editor-wrapper").find("*[percName]");
                var properties = [];
                $.each(propFields, function(index, value)
                {
                    var inputField = $(value);
                    var propName = inputField.attr('percName');
                    var propType = inputField.attr('type');
                    var propVal;
                    var ignoreProp = false;
                    
                    if (propType == 'radio') 
                    {
                        propVal = inputField.prop('checked');
                    }
                    else if (propType == 'checkbox') 
                    {
                        propVal = inputField.prop('checked');
                    }
                    else 
                    {
                        if(propName === 'password'){
                            propVal = btoa(inputField.val());
                        }else{
                            propVal = inputField.val();
                        }
                    }
                    
                    var propObjField = {
                        "key": propName,
                        "value": propVal
                    };

                    properties.push(propObjField);
                });
                // Add driver as property of server
                properties.push({
                    "key": "driver",
                    "value": driver
                });
                //push the ignore unmodified assets option
                properties.push({
                	"key":"ignoreUnModifiedAssets",
                	"value":ignoreUnModifiedAssets
                });
                //push the publish related items option
                properties.push({
                	"key":"publishRelatedItems",
                	"value":publishRelatedItems
                });
                return properties;
            };

            //Create the server Option Object
            var serverPropObj = {
                serverInfo: {
                    'isDefault': isDefault,
                    'serverId': sId,
                    'serverName': newserverName,
                    'type': type,
                    'isModified': '',
                    'properties': serverProp(),
                    'serverType':serverType
                }
            };
            if (!sId) 
                server = newserverName;
            //Call the service method to save the information. If 'serverId' is available service
            // will update the existing server and if serverId is not available it will create new one
            service.createUpdateSiteServer(siteId, server, serverPropObj, function(status, results)
            {
                if (status) 
                {
                    isNewServer = false;
                    var loadServer = results[0].serverInfo.serverName;
                    //Get List of servers List and highlight the default         
                    renderServerList(loadServer);
                    dirtyController.setDirty(false);
                }
                else 
                {
                    validateFields(results);
                    dirtyController.setDirty(true);
                }
            });
        }
    };
    //Fix the data overflow in the columns
    function handleOverflow(element)
    {
        var title = element.attr("title");
        if (title === '') 
            return true;
        var width = element.parents("td").width();
        element.css("width", width);
    }
    
})(jQuery);
