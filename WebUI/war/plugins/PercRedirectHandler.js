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

(function($) {
    $.PercRedirectHandler =  {
        createRedirect: createRedirect
    };
    function createRedirect(fromPath, toPath, type)
    {
        var deferred = $.Deferred();
        if(fromPath === toPath){
            deferred.resolve(I18N.message("perc.ui.redirect.handler@To and From Same"));
        }
        else if(!gIsSaaSEnvironment ){
            deferred.resolve(I18N.message("perc.ui.redirect.handler@SaaS Environment"));
        }
        else if(type === $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK ||
                type === $.Perc_SectionServiceClient.PERC_SECTION_TYPE.EXTERNAL_LINK ) {
            deferred.resolve(I18N.message("perc.ui.redirect.handler@Skipping redirect creation"));
        }
        else{
        	// Check for and strip first of double slash if necessary
        	fromPath = (fromPath.match("^//Sites/")) ? fromPath.substring(1, fromPath.length) : fromPath;
        	toPath = (toPath.match("^//Sites/")) ? toPath.substring(1, toPath.length) : toPath;
            var data = {"data":{"fromPath":fromPath, "toPath":toPath, "type":type}};
            validate(data)
            .done(function(resData){
                handleRedirect(fromPath, toPath, resData)
                .done(function(msg){
                    deferred.resolve(msg);
                })
                .fail(function(errorMsg){
                    deferred.reject(errorMsg);
                });
            })
            .fail(function(errorMsg){
                deferred.reject(errorMsg);
            });

        }
        return deferred.promise();
    }
    function validate(data)
    {
        var deferred = $.Deferred();
        var url = $.perc_paths.REDIRECT_ROOT + "validate";
        $.PercServiceUtils.makeJsonRequest(
            url,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    deferred.resolve(result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    deferred.reject(defaultMsg);
                }
            },
            data,
            function(status)
            {
                // On abort(timeout) callback
                deferred.reject(I18N.message("perc.ui.redirect.handler@Redirect Service Timed Out"));
            }
        );
        return deferred.promise();
    }
    function handleRedirect(fromPath, toPath, resData){

        var deferred = $.Deferred();

        fromPathArray = fromPath.split('/');
        toPathArray = toPath.split('/');
        fromFileNamePosition = (fromPathArray.length - 1);
        toFileNamePosition = (toPathArray.length - 1);
        currentSite = findSiteFromPath(fromPath);

        sitePropertiesDeferred = $.Deferred();
        $.PercSiteService.getSiteProperties(currentSite, getSitePropertiesCallback);
        sitePropertiesDeferred.done(function(result) {

            siteProperties = result.SiteProperties;
            
            if(fromPathArray[fromFileNamePosition] === siteProperties.defaultDocument && siteProperties.canonicalDist === 'sections') {
                fromPath = makeSectionLink(fromPathArray, siteProperties.defaultDocument);
            }
            
            if(toPathArray[toFileNamePosition] === siteProperties.defaultDocument && siteProperties.canonicalDist === 'sections') {
                toPath = makeSectionLink(toPathArray, siteProperties.defaultDocument);
            }


            if(resData.response.status === "Error"){
                deferred.reject(resData.response.errorMessage);
            }
            if(resData.response.status === "NotApplicable"){
                deferred.resolve(I18N.message("perc.ui.redirect.handler@Redirects Not Applicable"));
            }
            else if(resData.response.status === "Published"){
                showRedirectDialog(fromPath, toPath, resData)
                .done(function(msg){
                    deferred.resolve(msg);
                })
                .fail(function(msg){
                    deferred.reject(msg);
                });
            }
            else{
                deferred.resolve(I18N.message("perc.ui.redirect.handler@Redirect Not Required"));
            }

        });

        return deferred.promise();

    }
    function makeRedirect(fromPath, toPath, resData){
        fromPath = getRelativePath(fromPath);
        toPath = getRelativePath(toPath);
        var deferred = $.Deferred();
        var redLic = resData.response.redirectLicense;
        var redData = {
           "category":"AUTOGEN",
           "condition":fromPath,
           "enabled":true,
           "key":redLic.key,
           "permanent":true,
           "redirectTo":toPath,
           "site":resData.response.bucketName,
           "type":"CM1"
        };
        var authHeader = {
            "id":redLic.key,
            "type":redLic.name,
            "token":redLic.handshake
        };
        $.ajax({
            type:"POST",
            beforeSend: function (request)
            {
                request.setRequestHeader("Authorization", JSON.stringify(authHeader));
            },
            url: redLic.apiProvider + "/rest/redirect/entries?replace=true",
            data: JSON.stringify(redData),
            contentType: 'application/json',
        })
        .done(function(data, status, request){
            deferred.resolve(I18N.message("perc.ui.redirect.handler@Created Redirect"));
        })
        .fail(function(request, status, errorMsg){
            var errMsg = I18N.message("perc.ui.redirect.handler@Rule Cannot Be Created");
            if(request && request.responseText)
                errMsg = JSON.parse(request.responseText).message;
            deferred.reject(errMsg);
        });

        return deferred.promise();
    }

    function makeSectionLink(originalPathArray, defaultDocument) {
        originalPathArray.splice(originalPathArray.indexOf(defaultDocument), 1);
        newPathArray = originalPathArray;
        newPath = newPathArray.join("/");

        return newPath;
    }

    function findSiteFromPath(fromPath) {
        pathArray = fromPath.split('/');
        return pathArray[2];
    }

    function getSiteProperties(currentSite) {
        $.PercSiteService.getSiteProperties(currentSite, getSitePropertiesCallback);
    }

    function getSitePropertiesCallback(status, result) {
        if(status === 'success') {
            sitePropertiesDeferred.resolve(result);
        }
        else {
            console.log(I18N.message( "perc.ui.redirect.handler@Could not retrieve site properties" ));
        }
    }

    function showRedirectDialog(fromPath, toPath, resData){
        var deferred = $.Deferred();
        var dialog;



        $.unblockUI();

        percDialogObject = {
             title: I18N.message("perc.ui.redirect.handler@Manage Redirects"),
             modal: true,
             percButtons:   {},
            id: "perc-redirect-dialog",
            width: 700
        };

        percDialogObject.percButtons["Yes"] = {
                    click: function()   {
                        if(!toPath){
                            toPath = dialog.find("#perc-redirect-to-path").val();
                        }
                        if(!toPath){
                            dialog.find("div.perc_field_error[for=perc-redirect-to-path]").show();
                            return;
                        }
                        else{
                            dialog.find("div.perc_field_error[for=perc-redirect-to-path]").hide();
                        }
                        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                        makeRedirect(fromPath, toPath, resData)
                        .fail(function(errorMessage){
                            deferred.reject(errorMessage);
                        })
                        .always(function(){
                            $.unblockUI();
                            dialog.remove();
                            deferred.resolve(I18N.message("perc.ui.redirect.handler@Created Redirect"));
                        });
                    },
                    id: "perc-redirect-yes"
                };

        percDialogObject.percButtons["No"] = {
                    click: function()   {
                        dialog.remove();
                        deferred.resolve(I18N.message("perc.ui.redirect.handler@User doesn't want to create redirect"));
                    },
                    id: "perc-redirect-no"
                };

        dialog = $(createDialogHtml(fromPath, toPath)).perc_dialog(percDialogObject);
        addBrowseHandler(dialog);
        return deferred.promise();

    }
    function createDialogHtml(fromPath, toPath){
        var dialogHtml = $("<div/>",{"id":"perc-redirect-container"});
        dialogHtml.append('<div><label>' +I18N.message("perc.ui.redirect.handler@Page Not Found") + '</label><br/><br/></div>');
        dialogHtml.append('<div><label>From path:</label><br/></div>');
        dialogHtml.append('<div class="readonlyinput">'+ getRelativePath(fromPath) +'</div><br/>');
        dialogHtml.append('<div><label>To path:</label><br/></div>');
        if(toPath){
            dialogHtml.append('<div class="readonlyinput">'+ getRelativePath(toPath) +'</div>');
        }
        else{
            dialogHtml.append("<div><input type='text' class='required' readonly='true' name='perc-redirect-to-path' id='perc-redirect-to-path'/><img id='perc-redirect-to-path-button' src='../images/images/buttonEllipse.png' class='perc-button-ellipse'/></div>");
            dialogHtml.append("<div for='perc-redirect-to-path' class='perc_field_error' style='display:none'>" + I18N.message("perc.ui.redirect.handler@To path is required") + "</div>");
        }
        return dialogHtml;
    }
    function addBrowseHandler(dialog){
        dialog.find("#perc-redirect-to-path-button").click(function(){
            var dlgTitle = I18N.message("perc.ui.redirect.handler@Select Path");
            var treeLabel = I18N.message("perc.ui.redirect.handler@To Path");
            var updateToPath = function(pathItem){
                var path = pathItem.path;
                //Add double slash if path doesn't start with //
                if(path.substring(0, 1) === "/" && (path.substring(0, 2) === "//"))
                    path = "/" + path;

                //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
                if(!path){
                    path = pathItem.folderPaths.split("$System$")[1];
                }
                path = getRelativePath(path);
                dialog.find("#perc-redirect-to-path").val(path).attr("title",path);
            };
            var validator = function(pathItem){
                var errMsg = null;
                if(!pathItem)
                    errMsg = I18N.message("perc.ui.redirect.handler@Select Folder or Page");
                else if(pathItem.path==="/Sites/")
                    errMsg = I18N.message("perc.ui.redirect.handler@Current Selection Sites Root");
                else if(pathItem.type==="site")
                    errMsg = I18N.message("perc.ui.redirect.handler@Current Selection Site");
                return errMsg;
            };
            var pathSelectionOptions = {
                okCallback: updateToPath,
                dialogTitle: dlgTitle,
                rootPath:"Sites",
                initialPath: dialog.find("#perc-convert-folder-path" ).val(),
                selectedItemValidator:validator,
                showFoldersOnly:false
            };
            $.PercPathSelectionDialog.open(pathSelectionOptions);
        });
    }
    function getRelativePath(path){
        path = path.replace("//Sites/","");
        path = path.replace("/Sites/","");
        path = path.substring(path.indexOf("/"));
        if(path.charAt(path.length-1) === "/")
        {
            path = path.substr(0, path.length - 1);
        }
        return path;
    }
})(jQuery);
