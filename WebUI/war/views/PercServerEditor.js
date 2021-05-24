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
 * Helper function for rendering editors based on mode and driver
 * @param mode: mode value can be 'new', readonly or 'editor'
 * @param driver: the value based on which type is selected
 * @param propertyObj: if mode is 'readonly' or 'editor' propertyObj will contain saved value for a given editor.
 */
(function($)
{
    var defaultFolderLocation = '';
    var defaultStagingLocation = '';
    var pubServerType = "PRODUCTION";
    $.PercEditorRenderer = function()
    {
        return {
            renderEditor: renderEditor,
            setReadOnlyProperties: setReadOnlyProperties,
            loadLocalFolderPath: loadLocalFolderPath
        }
    }
    
    $(document).ready(function()
    {
        //Bind onChange event on Type dropdown/combo-box
        $("#perc-servers-container").on('change', '#publishType', function()
        {
            var typeValue = $(this).val();
            $("#perc-editor-wrapper").html('');
            $("#perc-servers-container").find('span.perc_field_error').remove();
            updateDriverOptions(typeValue);
        });
        
        //Bind onChange event Driver dropdown/combo-box        
        $("#perc-servers-container").on('change', '#perc-driver', function()
        {
            $("#perc-servers-container").find('span.perc_field_error').remove();
            var driverValue = $(this).val();
            if (driverValue == 'Select') 
            {
                $("#perc-editor-wrapper").html('');
                return;
            }
            $("#perc-editor-wrapper").load("../app/includes/serverEditors/propEditor.jsp?editorName=" + driverValue, function()
            {
                if (driverValue == 'FTP') 
                {
                    loadPrivateFileKey();
                    secureFtp();
                    setPassPrivKey();
                }
                if (driverValue == 'Local') 
                {
                    updateFolderLocation();
                }
            });
        });
        //Bind click event secureFtp checkbox
        $("#perc-servers-container").on('click', '#perc-define-secure-ftp-input', secureFtp);
        
        //Bind click event on Password Radio buttons        
        $("#perc-servers-container").on('click', '#perc-ftp-password-rb, #perc-ftp-private-file-key-rb', setPassPrivKey);
        
        //Bind click event on Folder location Radio buttons
        $("#perc-servers-container").on('click', '#perc-defaultServer, #perc-ownServer', updateFolderLocation);
    });
    
    function loadLocalFolderPath(siteId){
        //Get the default folder location            
        service.getLocalFolderPath(siteId, "PRODUCTION", function(status, result)
        {
            if (status) 
            {
                defaultFolderLocation = result[0];
            }
            else 
            {
                //error
            }
        });
        service.getLocalFolderPath(siteId, "STAGING", function(status, result)
        {
            if (status) 
            {
                defaultStagingLocation = result[0];
            }
            else 
            {
                //error
            }
        });
    }
    
    /**
     * A helper function for rendering the editor either in Edit mode or as a New Server Editor
     * @param {Object} propertyObj: contains the properties of the server
     */
    function renderEditor(serverName, propertyObj, serverType)
    {
        pubServerType = serverType;

        $("#perc-editor-summary").html('');
        $("#perc-server-name-wrapper").hide();
        $("#perc-editor-form").load('../app/includes/serverEditors/serverEditor.jsp', function()
        {
            // if propertyObj is availabe open the editor in Edit mode and fill in the supplied properties
            if (!($.isEmptyObject(propertyObj))) 
            {
                $("#publishType").val(propertyObj.serverInfo.type);
                var propMap = _getPropertyMap(propertyObj.serverInfo.properties);
                var driverName = propMap["driver"];
                updateDriverOptions(propertyObj.serverInfo.type);
                $("#perc-driver").val(driverName);
                $("#perc-editor-wrapper").load("../app/includes/serverEditors/propEditor.jsp?editorName=" + driverName, function()
                {
                    setEditorProperties(propertyObj);
                    if (driverName == 'FTP') 
                    {
                        loadPrivateFileKey(propMap['privateKey']);
                        secureFtp();
                        setPassPrivKey();
                    }
                    if (driverName == 'Local') 
                    {
                        updateFolderLocation();
                    }
                });
            }
            if(serverType == "STAGING")
            {
                $("#perc-pub-now").hide();
                $("#perc-pub-now-wrapper").hide();
                $("#perc-ignore-unmodified-assets").css("margin-left","0px");
            }
        });
    }
    
    /**
     * A method to set the property for server in Edit Mode
     * @param {Object} propObj: contains the properties of the server
     */
    function setEditorProperties(propObj)
    {
        var propContainer = $("#perc-servers-wrapper");
        var isDefaultInput = $(propContainer.find('input[percName="isDefault"]'));
        propContainer.find('input[percName="serverName"]').val(propObj.serverInfo.serverName);
        isDefaultInput.prop('checked', propObj.serverInfo.isDefault);
        var ignoreAssets = $(propContainer.find('input[percName="ignoreUnModifiedAssets"]'))
        ignoreAssets.prop('checked', propObj.serverInfo.ignoreUnModifiedAssets);
        var publishRelatedItems = $(propContainer.find('input[percName="publishRelatedItems"]'))
        publishRelatedItems.prop('checked', propObj.serverInfo.publishRelatedItems);
        //If server is default - disable the checkbox
        if (propObj.serverInfo.isDefault) 
        {
            isDefaultInput.attr('disabled', true);
        }
        var serverProperties = [];
        if (!Array.isArray(propObj.serverInfo.properties))
        {
            serverProperties.push(propObj.serverInfo.properties);
        }
        else 
        {
            serverProperties = propObj.serverInfo.properties;
        }
        $.each(serverProperties, function(name, value)
        {
            var propertyName = '';
            var popertyValue = '';
            $.each(this, function(propName, value)
            {
                if (propName == 'key') 
                {
                    propertyName = value;
                }
                if (propName == 'value') 
                {
                    propertyValue = value;
                }
            });
            var propName = propertyName;
            var inputField = propContainer.find("input[percName=" + propName + "]");
            var inputType = inputField.attr('type');
            if (inputType == 'text' || inputType == 'password') 
            {
                inputField.val(propertyValue);
            }
            else if (inputType == 'checkbox' || inputType == 'radio') 
            {
                inputField.prop("checked", propertyValue);
            }
            
        });
    }
    
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
                if (propName == 'key') 
                {
                    propertyName = value;
                }
                if (propName == 'value') 
                {
                    propertyValue = value;
                }
                propertyMap[propertyName] = propertyValue;
            });
        });
        return propertyMap;
    }
    /**
     * A method to se the property for server in read only mode
     * @param {Object} propObj: contains the properties of the server
     */
    function setReadOnlyProperties(propObj)
    {
        var propContainer = $("#perc-editor-prop");
        var serverProperties = [];
        if (!Array.isArray(propObj.serverInfo.properties))
        {
            serverProperties.push(propObj.serverInfo.properties);
        }
        else 
        {
            serverProperties = propObj.serverInfo.properties;
        }
        $.each(serverProperties, function(name, value)
        {
            var propertyName = '';
            var propertyValue = '';
            $.each(this, function(propName, value)
            {
                if (propName == 'key') 
                {
                    propertyName = value;
                }
                if (propName == 'value') 
                {
                    propertyValue = value;
                }
                
            });
            if(propertyName == "ignoreUnModifiedAssets")
            {
                if(propertyValue == true)
                    $("#perc-editor-prop-container").find("span[percName=" + propertyName + "]").text("Only modified assets will be published.");
                else
                    $("#perc-editor-prop-container").find("span[percName=" + propertyName + "]").text("All assets will be published.");
            }
            else if(propertyName == "publishRelatedItems")
            {
                if(propertyValue == true)
                    $("#perc-editor-prop-container").find("span[percName=" + propertyName + "]").closest("li").show();
            }
            else if(propertyName != "driver")
                propContainer.find("span[percName=" + propertyName + "]").text(propertyValue);
        });
        if (propObj.serverInfo.isDefault) 
        {
            propContainer.find("#perc-default-server").show();
        }
    }
    
    /**
     * Fetch the privatekeys and set the selected one.
     * @param String selectedOption: currently selected key
     */
    function loadPrivateFileKey(selectedOption)
    {
        var utilService = $j.PercUtilService;
        var privateKeys = utilService.getPrivateKeys(function(status, result)
        {
            if (status == $.PercServiceUtils.STATUS_SUCCESS) 
            {
                var pkFileList = $("#perc-ftp-private-file-key");
                pkFileList.html("");
                if (result.data.PrivateKeys != "") 
                {
                    var keyNames = $.perc_utils.convertCXFArray(result.data.PrivateKeys.keyNames);
                    $.each(keyNames, function(val, text)
                    {
                        if (selectedOption != 'undefined' && selectedOption == text) 
                        {
                            pkFileList.append($('<option selected="selected"></option>').val(text).html(text));
                        }
                        else 
                        {
                            pkFileList.append($('<option></option>').val(text).html(text));
                        }
                    });
                    flagPrivateKeysLoaded = true;
                }
                else 
                {
                    disablePrivateKeysCombo();
                }
            }
            else 
            {
                disablePrivateKeysCombo();
            }
        });
    }
    
    //Helper function to disable the priavteKey combo box
    function disablePrivateKeysCombo()
    {
        if ($("#perc-define-secure-ftp-input").is(':checked')) 
        {
            $("#perc-ftp-private-file-key-rb").attr("disabled", "true");
        }
    }
    
    //Helper function to update the driver option based on 'Type' value
    function updateDriverOptions(typeValue)
    {
        $("#perc-driver").val('Select').removeAttr('disabled').css('background-color', '#FFFFFF');
        if (typeValue == "File") 
        {
            $('.file-driver, .database-driver').remove();    
            $('#perc-driver-filter').show();
            $("select#perc-driver").append($('<option  class="file-driver" name="FTP" ></option>').html('FTP'))
            .append($('<option  class="file-driver" name="LOCAL" ></option>').html('Local'))
            .append($('<option  class="file-driver" name="AMAZONS3" value="AMAZONS3"></option>').html('Amazon S3'));
			
			 //Get the available drivers and disable the option for the one not found
            $.PercPublisherService().getAvailableRegions(function(status, result)
            {
                if (status) 
                {
                    availableRegions = JSON.parse(result[0]);
                    $.each(availableRegions, function(region, value)
                    {
                        if (!(value)) 
                        {
                            $("#perc-region-filter").find('option[name=' + region + ']').attr("disabled", true);
                        }
                    });
                }
            });
                                 
        }
        else if (typeValue == "Database") 
        {
            $('.file-driver, .database-driver').remove();            
            $('#perc-driver-filter').show();
            $("select#perc-driver")
	            .append($('<option  class="database-driver" name="MSSQL" value="MSSQL"></option>').html('MS SQL'))
	            .append($('<option  class="database-driver" name="MYSQL" value="MySQL"></option>').html('MySQL'))
	            .append($('<option  class="database-driver" name="ORACLE" value="Oracle"></option>').html('Oracle'));
            
            //Get the available drivers and disable the option for the one not found
            $.PercPublisherService().getAvailableDrivers(function(status, result)
            {
                if (status) 
                {
                    availabelDrivers = JSON.parse(result[0]);
                    $.each(availabelDrivers, function(driver, value)
                    {
                        if (!(value)) 
                        {
                            $("#perc-driver-filter").find('option[name=' + driver + ']').attr("disabled", true);
                        }
                    });
                }
            });
        }
        else if (typeValue == "Select") 
        {
            //Disable the driver drop down menu if Type is not selected
            $('#perc-driver').attr('disabled', true).css('background-color', '#CCCCCC');
        }
    }
    //Helper function to update the UI behavior based on Secure FTP checkbox value
    function secureFtp()
    {
        var secureFTPchecked = $("#perc-define-secure-ftp-input").is(':checked');
        if (secureFTPchecked) 
        {
            $("#perc-ftp-private-file-key-rb").removeAttr("disabled");
            $("#perc-ftp-private-file-key-rb").attr("selected", "");
        }
        else 
        {
            $("#perc-ftp-private-file-key-rb, #perc-ftp-private-file-key").attr("disabled", "true");
            $("#perc-ftp-password-rb").prop('checked', true);
        }
        setPassPrivKey();
    }
    
    // Helper function to enable and disable password and private key
    function setPassPrivKey()
    {
        var passRadio = $("#perc-ftp-password-rb").is(':checked');
        if (passRadio) 
        {
            $("#perc-ftp-private-file-key").attr('disabled', true).val("").css('background-color', '#CCCCCC');
            $("#perc-ftp-password").removeAttr('disabled').css('background-color', '#FFFFFF');
        }
        else 
        {
            $("#perc-ftp-private-file-key").removeAttr('disabled', false).css('background-color', '#FFFFFF');
            $("#perc-ftp-password").attr('disabled', true).val("").css('background-color', '#CCCCCC');
        }
    }
    
    //Helper function to display the folder location field based on radio button fields
    function updateFolderLocation()
    {
    
        var defaultServer = $("#perc-defaultServer").is(':checked');
        if (defaultServer && pubServerType == "PRODUCTION") 
        {
            $("#perc-local-own-location").hide();
            $("#perc-local-default-location").show().val(defaultFolderLocation);
            
        }
		else if (defaultServer && pubServerType == "STAGING")
		{
			$("#perc-local-own-location").hide();
			$("#perc-local-default-location").show().val(defaultStagingLocation);
		}
        else 
        {
            if ($("#perc-site-id").data('secureSite')) 
            {
                $.perc_utils.alert_dialog({
                    content: I18N.message("perc.ui.perc.server.editor@Selecting Your Own Web Server"),
                    title: I18N.message("perc.ui.page.general@Warning")
                });
            }
            $("#perc-local-own-location").show();
            $("#perc-local-default-location").hide();
        }
        
    }
    
})(jQuery);

