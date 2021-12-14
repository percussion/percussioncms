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
 * Javascript to fetch the data for a requested content_type within a Parent Asset Directory.
 *
 * See percAssetFinderControl.xsl for the control file.
 *
 * Options:
 * @content_type: Enter the selected Content Type you wish to search for.
 * @paramName The paramter name used to update the hidden input field after values are selected.
 * @readonly Read only mode.
 *  Usage:
 readonly = readonly == 'true' ? true : false;
 var paramName = {contentType : contentType, readonly : readonly};
 $('#perc-asset-finder-' + paramName ).perc_AssetFinderReadOnly(paramName);
 *
 */

(function($) {
    $.fn.perc_AssetFinder = function(opts){

        return this.each(function(){

            var $thisElem = $(this);
            var contentType = opts.contentType;
            var fieldToDisplay = opts.fieldToDisplay;
            var readonly = opts.readonly;

            if( contentType != "" && fieldToDisplay != "") {

                var thisAssetId = getAssetId("sys_contentid");

                if (thisAssetId && thisAssetId != "#" && thisAssetId != undefined && thisAssetId != '') {
                    var thisAssetContentId = "16777215-101-" + thisAssetId;

                    $.PercPathService.getPathItemById( thisAssetContentId, function(status, result, errorCode){
                        if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                            var assetFolderPath = cleanFolderPath(result.PathItem.folderPaths);

                            searchForContentType(assetFolderPath, opts, $thisElem);
                        } else {
                            console.warn("Handle Error manually");
                            // handle error here
                        }
                    });
                } else {
                    console.log(contentType + " asset not saved yet. Using current folder.");

                    // If this is a new asset, use the current folder location to determine the department and organization
                    folderId = getAssetId('sys_folderid');
                    folderGuid = "16777215-101-" + folderId;

                    $.PercPathService.getPathItemById(folderGuid, function(status, result) {
                        cleanedAssetPath = cleanFolderPath(result.PathItem.folderPaths);
                        searchForContentType(cleanedAssetPath, opts, $thisElem);
                    });

                }

            } else {
                if (contentType == "") {
                    console.warn("No content_type value assigned for percAssetFinderControl.");
                    $thisElem.find(".perc-asset-finder-error").append("<div>No content_type value assigned for percAssetFinderControl.</div>");
                }
                if (fieldToDisplay == "") {
                    console.warn("No field_to_display value assigned for percAssetFinderControl.");
                    $thisElem.find(".perc-asset-finder-error").append("<div>No field_to_display value assigned for percAssetFinderControl.</div>");
                }
                console.warn("This widget is improperly configured, please see documentation on configuring AssetFinder control.");
            }
        }); // end Return statement
    };

    function searchForContentType(assetFolderPath, opts, $thisElem) {
        // if we are looking for parent organization, we want to trim the folder path start one level up by default.
        // opts.trimPathAgain hasn't been created yet so this will hold true the first time
        // if looking for the parentOrganization field.  Will set after first check so that
        // the path is not trimmed on the next visit.
        if (opts.paramName === 'parentOrganization' && opts.trimPathAgain === undefined) {
            assetFolderPath = splitFolderPath(assetFolderPath, opts, $thisElem);
            opts.trimPathAgain = false;
        }
		if (typeof assetFolderPath === "undefined")
            return;
      

        getFolderChildren(assetFolderPath)
            .then(function (response){
                console.log(response);
                if (response != null ) {
                    var foundContentType;
                    var childArray = response.PagedItemList.childrenInPage;

                    for (var j = childArray.length - 1; j >= 0; j--) {
                        if (childArray[j].type == opts.contentType) {
                            foundContentType = childArray[j];
                            break;
                        }
                    }
                    if (foundContentType != undefined) {

                        getAssetDetails(foundContentType)
                            .then(function(response) {
                                if (response != null ) {
                                    var displayValue ='';
                                    try{
                                        var assetFieldsArray = response.asset.fields;
                                        for(var i=0; i<assetFieldsArray.entry.length; i++){
                                            if(assetFieldsArray.entry[i].key==opts.fieldToDisplay){
                                                displayValue = assetFieldsArray.entry[i].value;
                                            }
                                        }
                                        //displayValue = response.asset.fields[opts.fieldToDisplay];
                                    }catch(e){
                                        // it means wrong attempt
                                        if(response.asset && response.asset.type =="percOrganization"){
                                            displayValue = response.asset.fields.orgName;
                                        }else if(response.asset && response.asset.type =="percDepartment"){
                                            displayValue = response.asset.fields.dptName;
                                        }else{
                                            displayValue = response.asset.name;
                                        }
                                    }
                                    $thisElem.find('.perc-asset-finder-data input#perc-content-display-' + opts.paramName).val(displayValue);

                                    var contentTypeId = getContentId(response);
                                    if (contentTypeId != false) {
                                        $thisElem.closest("div[type='sys_normal']").find('#perc-content-value-' + opts.paramName).val(contentTypeId);
                                    }
                                } else {
                                    console.warn("Error retreiving data for Asset: " + foundContentType);
                                    $thisElem.find(".perc-asset-finder-error").append('<label class="perc-asset-finder-warning" for="personEmail" generated="true" style="display: block; padding-left: 15px">No value found for this field.</label>');
                                }
                            });
                    } else {
                        console.info("Content Type " + opts.contentType + " not found in folder " + assetFolderPath);
                        console.info("Looking up one folder.");
                        var trimmedPath = splitFolderPath(assetFolderPath, opts, $thisElem);

                        if (trimmedPath !== null && trimmedPath !== '' && trimmedPath !== undefined)
                            searchForContentType(trimmedPath, opts, $thisElem);
                    }
                } else {
                    // handle error with console and ui warning
                    console.warn("No children fond in folder: " + assetFolderPath);
                    $thisElem.find(".perc-asset-finder-error").append("<div>No data found for this field. Please see configuration of percAssetFinderControl.</div>");
                    $thisElem.find(".perc-asset-finder-error").append('<label class="perc_field_error" for="personEmail" generated="true" style="display: block;">No data found for this field. Please see configuration of percAssetFinderControl.</label>');
                }
            });
    }

    function splitFolderPath(path, opts, $thisElem) {
        var pathArray = path.split("/");
        if (pathArray.length > 2){
            if (pathArray.length >= 3){
                pathArray.pop();
            }
            var trimmedPath = pathArray.join("/");
            return trimmedPath;
        } else {
            // If no Asset is found for the selected field, default to value to a hash symbol to store in the database
            $thisElem.closest("div[type='sys_normal']").find('#perc-content-value-' + opts.paramName).val("#");
            console.info("Content Type " + opts.contentType + " not found anywhere in Asset path.");
            $thisElem.find(".perc-asset-finder-error").append('<label class="perc-asset-finder-warning" for="personEmail" generated="true" style="display: block; padding-left: 15px">No value found for this field.</label>');
        }
    }

    function getFolderChildren(folderPath){
        return $.ajax({
            type: 'GET',
            error: handleError,
            url: $.perc_paths.PATH_PAGINATED_FOLDER + folderPath + "/?startIndex=1&maxResults=1000&child=Assets",
            dataType: 'json',
            cache: false
        });
    }

    function getAssetId(param) {
        var urlParams = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
        for (var i = 0; i < urlParams.length; i++) {
            var params = urlParams[i].split("=");
            if(params[0] == param)
                return params[1];
        }
        return false;
    }

    function cleanFolderPath(url){
        var pathObject = {};
        var urlStr = url.toString();
        pathObject.pathArray = urlStr.split("/");
        pathObject.folderCount = pathObject.pathArray.length;
        pathObject.pathArray.splice(0,4); // Clean beginning of the folder path
        return "/" + pathObject.pathArray.join("/");
    }

    function getAssetDetails(asset){
        var serviceUrl = "/Rhythmyx/rest/assets/by-path/" + asset.path;
        return $.ajax( {
            type: 'GET',
            url: serviceUrl,
            error: handleError,
            dataType: 'json',
            cache: false
        });
    }

    function folderPathCount(path){
        if (path != null && path != undefined && path != ""){
            var pathArray = path.split("/");
            return pathArray.length;
        } else {
            console.log("Incorrect folderpath used in folderPathCount function.")
        }
    }

    function getContentId(asset){
        if (asset != null && asset != undefined){
            var idArray = asset.asset.id.split("-");
            return idArray[idArray.length - 1];
        } else {
            console.warn("Cannot get contentId from given Asset.")
            return false;
        }
    }

    function handleError(result, textstatus, errorCode){
        var msg = "";
        console.log("result: " + result);
        console.log("textstatus: " + textstatus);
        console.log("errorCode: " + errorCode);
        if (errorCode == "cannot.find.item") {
            msg = I18N.message( 'perc.ui.common.error@Content Deleted' );
        }
        else {
            msg = result;
        }
        // defer.reject({title: 'Error on page lookup', content: msg});
    }

    $.fn.perc_AssetFinderReadOnly = function(opts){

        this.each(function(){

            var $thisElem = $(this);
            var contentType = opts.contentType;
            var fieldToDisplay = opts.fieldToDisplay;
            var readonly = opts.readonly;

            if( contentType != "" && fieldToDisplay != "") {

                //var thisAssetId = $thisElem.closest("div[type='sys_normal']").find('#perc-content-value-' + opts.paramName).val();
                var thisAssetId = getAssetId("sys_contentid");

                if (thisAssetId && thisAssetId != "#" && thisAssetId != undefined && thisAssetId != '') {
                    var thisAssetContentId = "16777215-101-" + thisAssetId;

                    $.PercPathService.getPathItemById( thisAssetContentId, function(status, result, errorCode){
                        if(status == $.PercServiceUtils.STATUS_SUCCESS) {

                            var assetFolderPath = cleanFolderPath(result.PathItem.folderPaths);
                            searchForContentType(assetFolderPath, opts, $thisElem);

                        } else {
                            console.warn("Handle Error manually");
                            // handle error here
                        }
                    });
                } else {
                    console.log(contentType + " asset not saved yet. Save asset to acquire " + fieldToDisplay + " for " + contentType + ".");
                }

            } else {
                if (contentType == "") {
                    console.warn("No content_type value assigned for percAssetFinderControl.");
                    $thisElem.find(".perc-asset-finder-error").append("<div>No content_type value assigned for percAssetFinderControl.</div>");
                }
                if (fieldToDisplay == "") {
                    console.warn("No field_to_display value assigned for percAssetFinderControl.");
                    $thisElem.find(".perc-asset-finder-error").append("<div>No field_to_display value assigned for percAssetFinderControl.</div>");
                }
                console.warn("This widget is improperly configured, please see documentation on configuring AssetFinder control.");
            }
        }); //end Return statement
    };

})(jQuery);

