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

(function($) {
    // Public API
    $.perc_module_license_manager = {
        //Gets the module license info to render
        //the returned object will be a json object
        getModuleLicensesInfo: getModuleLicensesInfo,
        //Activates the module license by calling external license service
        activateModuleLicense: activateModuleLicense,
        //Saves the module license to the CM1 server
        saveModuleLicense: saveModuleLicense,
        generateLicenseView:generateLicenseView,
        generateLicenseActivatorView:generateLicenseActivatorView,
        LICENSING_BASE_URL:"",
        TYPES_URL:"/licensing/types",
        VALIDITY_URL:"/licensing/validity",
        HANDSHAKE_URL:"/licensing/handshake",
        PING_URL:"/platform/status/ping",
        SERVICE_NOT_AVAILABLE_MSG: I18N.message("perc.ui.gadgets.licenseMonitor@Licensing Service Not Available"),
        AJAX_TIMEOUT:10000
    };

    function getModuleLicensesInfo(callback)
    {
        var getModuleLicensesInfoCB = callback;
        function _getModuleLicensesCB(status, result)
        {
            if(!status){
                getModuleLicensesInfoCB(false, result);
                return;
            }
            var modLic = result;
            //If we have module license validate them else get the types
            if(modLic && Array.isArray(modLic)){
                var payLoad = _createPayload(modLic);
                _validateModuleLicenses(payLoad, _validateModuleLicensesCB);
            }
            else{
                _getModuleLicenseTypes(_getModuleLicenseTypesCB);
            }
        }
        function _validateModuleLicensesCB(status, result){
            if(!status){
                getModuleLicensesInfoCB(false, result);
                return;
            }
            var combinedInfo = _createCombinedInfo(result);
            var data = {};
            data.combinedInfo = combinedInfo;
            data.cloudInfo = result;
            getModuleLicensesInfoCB(true, data);
        }
        function _getModuleLicenseTypesCB(status, result){
            if(!status){
                getModuleLicensesInfoCB(false, result);
                return;
            }
            var combinedInfo = _createCombinedInfo(result);
            var data = {};
            data.combinedInfo = combinedInfo;
            data.cloudInfo = result;
            getModuleLicensesInfoCB(true, data);
        }

        _getModuleLicenses(_getModuleLicensesCB);

    }

    function activateModuleLicense(type, key, callback)
    {
        var handshakeUrl = $.perc_module_license_manager.LICENSING_BASE_URL + $.perc_module_license_manager.HANDSHAKE_URL;
        var payLoad = $.extend({},payLoadTemplate);
        payLoad.key = key;
        payLoad.type = type;
        var dataObject = [];
        dataObject.push(payLoad);
        $.ajax({
            type: 'POST',
            url: handshakeUrl,
            contentType: 'application/json',
            data : JSON.stringify(dataObject),
            dataType : 'json'
        })
            .done(function(data){
                callback(true, data);
            })
            .fail(function(jqXhr, textstatus, errorMessage){
                errorMessage = errorMessage?errorMessage:I18N.message("perc.ui.gadgets.licenseMonitor@Failed to activate the license");
                callback(false,errorMessage);
            });


    }

    function saveModuleLicense(data, callback)
    {
        var handshake = data.handshakes[0];
        var licType;
        $.each(data.licenseTypes,function(){
            if(this.id == handshake.type){
                licType = this;
                return;
            }
        });
        var moduleLicenseData = {};
        moduleLicenseData.name = handshake.type;
        moduleLicenseData.key = handshake.key;
        moduleLicenseData.handshake = handshake.token;
        moduleLicenseData.apiProvider = licType.api_provider;
        moduleLicenseData.uiProvider = licType.ui_provider;
        var payLoad = {"moduleLicense":moduleLicenseData};
        $.PercLicenseService.saveModuleLicense(payLoad, function(status, data)
        {
            if (!status){
                callback(false,data);
            }
            else{
                callback(true);
            }
        });
    }

    function _getModuleLicenses(callback)
    {
        //call the service and get the results
        $.PercLicenseService.getAllModuleLicenses(function(status, data)
        {
            if (!status){
                callback(false,data);
            }
            else{
                $.perc_module_license_manager.LICENSING_BASE_URL = data.moduleLicenses.licenseServiceUrl;
                var result = data.moduleLicenses.moduleLicenses;
                if(result && !Array.isArray(result))
                    result = [result];
                callback(true,result);
            }
        });
    }

    function _getModuleLicenseTypes(callback)
    {
        var typesUrl = $.perc_module_license_manager.LICENSING_BASE_URL + $.perc_module_license_manager.TYPES_URL;
        $.ajax({
            type: 'GET',
            url: typesUrl,
            timeout:$.perc_module_license_manager.AJAX_TIMEOUT
        })
            .done(function(data){
                //Remove SOCIAL_PROMOTION from module license list till cloud build is deployed.
                for(var i=0; i<=data.licenseTypes.length; i++){
                    if(data.licenseTypes[i].id==="SOCIAL_PROMOTION"){
                        data.licenseTypes.splice(i,1);
                    }
                }
                callback(true, data);
            })
            .fail(function(jqXhr, textstatus, errorMessage){
                errorMessage = errorMessage?errorMessage:I18N.message("perc.ui.gadgets.licenseMonitor@Failed to get available modules");
                callback(false,errorMessage);
            });
    }

    function _createPayload(moduleLicenses)
    {
        var payLoad = [];
        $.each(moduleLicenses,function(){
            var obj = $.extend({}, payLoadTemplate);
            obj.key = this.key;
            obj.token = this.token;
            obj.type = this.name;
            payLoad.push(obj);
        });
        return payLoad;
    }

    function _createCombinedInfo(result){
        var infoObjs = [];
        var handShakesMap = {};
        if(result.handshakes){
            $.each(result.handshakes, function(){
                handShakesMap[this.type] = this;
            });
        }
        $.each(result.licenseTypes, function(){
            var obj = {};
            obj.label = this.label;
            obj.name = this.id;
            var hs = handShakesMap[this.id];
            if(hs){
                obj.isValid = hs.valid;
                obj.message = hs.message;
            }
            else{
                obj.isValid = false;
                obj.message = I18N.message("perc.ui.gadgets.licenseMonitor@Inactive");
            }
            infoObjs.push(obj);
        });
        return infoObjs;
    }

    function _validateModuleLicenses(payLoad, callback)
    {
        var validityUrl = $.perc_module_license_manager.LICENSING_BASE_URL + $.perc_module_license_manager.VALIDITY_URL;
        $.ajax({
            type: 'POST',
            url: validityUrl,
            contentType: 'application/json',
            data : JSON.stringify(payLoad),
            dataType : 'json'
        })
            .done(function(data){
                for(var i=0; i<=data.licenseTypes.length; i++){
                    if(data.licenseTypes[i].id=="SOCIAL_PROMOTION"){
                        data.licenseTypes.splice(i,1);
                    }
                }
                callback(true, data);
            })
            .fail(function(jqXhr, textstatus, errorMessage){
                errorMessage = errorMessage?errorMessage:I18N.message("perc.ui.gadgets.licenseMonitor@Failed to get validated modules");
                callback(false,errorMessage);
            });
    }

    function generateLicenseView(licenseInfo,  newLicenseInfo){

        var licInfoElemCont = $("<div/>");
        $.each(licenseInfo, function(){
            var curLI = this;
            if(newLicenseInfo && curLI.name == newLicenseInfo.name){
                curLI = newLicenseInfo;
            }
            var licInfoElem = $("<div/>");
            $("<span/>").addClass("perc-lmg-modulelicense-label").text(curLI.label).appendTo(licInfoElem);
            var msg = curLI.isValid?" : " + I18N.message("perc.ui.gadgets.licenseMonitor@Active") : " : " + curLI.message + " ";
            $("<span/>").addClass("perc-lmg-modulelicense-message").html(msg).appendTo(licInfoElem);
            var iconClass = curLI.isValid? "status-active" : "status-warning";
            $("<span/>",{"class":"perc-lmg-icon-status"}).addClass(iconClass).attr("for", curLI.name).appendTo(licInfoElem);
            licInfoElemCont.append(licInfoElem);
        });
        return licInfoElemCont;
    }

    function generateLicenseActivatorView(licenseTypes, activationSuccessCallback){
        var licActElemCont = $("<div/>").addClass("perc-lmg-modulelicense-act-top");
        var typeComboBox = $("<select/>",{"id":"perc-lmg-modulelicense-type-list"});
        $.each(licenseTypes, function(){
            $("<option/>").attr("value",this.id).text(this.label).appendTo(typeComboBox);
        });

        licActElemCont.append(typeComboBox).append(`<input name='perc-lmg-modulelicense-key'/>`).append(`<button aria-label="Activate License" id="activate-license-button" class="btn btn-primary advanced">${I18N.message("perc.ui.gadgets.licenseMonitor@Activate")}</button>`);
        licActElemCont.append("<div id='perc-lmg-modulelicense-key-error' style='display:none; color:red'/>");
        _addActivateClickEvent(licActElemCont, activationSuccessCallback);
        return licActElemCont;
    }

    function _addActivateClickEvent(licActElemCont, activationSuccessCallback){
        licActElemCont.find("#activate-license-button").on("click", function(){
            var parent = $(this).closest(".perc-lmg-modulelicense-act-top");
            var errElem = parent.find("#perc-lmg-modulelicense-key-error").hide();
            var key = parent.find("input[name=perc-lmg-modulelicense-key]").val();
            var type = parent.find("#perc-lmg-modulelicense-type-list").val();
            key = key.trim();
            if(key.length<1)
            {
                errElem.text(I18N.message("perc.ui.gadgets.licenseMonitor@Please enter a valid key")).show();
                return;
            }
            percJQuery.PercBlockUI(percJQuery.PercBlockUIMode.CURSORONLY);

            activateModuleLicense(type,key,function(status, result){
                if(!status){
                    errElem.text(result).show();
                    percJQuery.unblockUI();
                    return;
                }
                var handshakeResult = result;
                var handshake = handshakeResult.handshakes[0];
                if(!handshake.valid){
                    var message = handshake.message?handshake.message:I18N.message("perc.ui.gadgets.licenseMonitor@License activation failed");
                    errElem.text(message).show();
                    percJQuery.unblockUI();
                    return;
                }
                saveModuleLicense(result, function(status, result){
                    percJQuery.unblockUI();
                    if(!status){
                        var message = handshake.message?handshake.message:I18N.message("perc.ui.gadgets.licenseMonitor@Failed to save the activated license");
                        errElem.text(message).show();
                        return;
                    }
                    var cmbInfo = _createCombinedInfo(handshakeResult);
                    var hresult = null;
                    $.each(cmbInfo, function(){
                        if(this.name == type){
                            hresult = this;
                            return;
                        }
                    });
                    activationSuccessCallback(hresult);
                });
            });
        });

    }

    var payLoadTemplate =   {
        "key":"YOUR-KEY",
        "token":"THIS_IS_WHERE_THE_TOKEN_IS_RETURNED",
        "type":"THE_LICENSE_TYPE",
        "valid":false
    };
})(jQuery);
