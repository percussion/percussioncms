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
 * Copy site Dialog, ask for name of new site.
 */
(function($){
    //Public API
    $.PercCopySiteDialog = {
        open: open
    };

    //
    var selectedTreePath = null;
    var selectedCheckbox = "share-type-buttons-across-site";

    function open(siteName){
        //If it is a saas mode, check whether there are any site configs available
        if(!gIsSaaSEnvironment){
            openDialog(siteName);
        }
        else{
            $.PercSiteService.getSaaSSiteNames(true)
                .done(function(siteNames){
                    var saasSiteNames = $.PercServiceUtils.convertMapToArray(siteNames.psmap.entries);
                    if($.isEmptyObject(saasSiteNames)){
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.copy.site.dialog@Warning"), content: I18N.message("perc.ui.copy.site.dialog@Not Authorized to Create Site")});
                    }
                    else{
                        openDialog(siteName, saasSiteNames);
                    }
                })
                .fail(function(){
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.copy.site.dialog@Warning"), content: I18N.message("perc.ui.copy.site.dialog@Failed to Fetch Configurations")});
                });
        }
    }

    /**
     * Opens the copy site dialog and proposed name for the new site that the user can change.
     * @param siteName(String), assumed to be a valid name of a site.
     */
    function openDialog(siteName, saasSiteNames)
    {
        var dialog = null;
        var dialogHTML = createDialog();
        dialog = $(dialogHTML).perc_dialog( {
            resizable : false,
            title: I18N.message("perc.ui.copy.site.dialog@Copy Site"),
            modal: true,
            closeOnEscape : true,
            percButtons:{
                "Save":{
                    click: function(){
                        if ($("#perc_site_name").val() === ""){
                            $('#perc_copysite_error').text(I18N.message("perc.ui.copy.site.dialog@Site Name Required"));
                        }
                        else{
                            $.PercSiteService.getSites(validateAndConfirm);
                        }
                    },
                    id: "perc-copysite-dialog-save"
                },
                "Cancel":{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-copysite-dialog-cancel"
                }
            },
            id: "perc-copysite-dialog",
            open:function(){
                $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                generateNewSiteName(siteName);
                $.perc_filterField($("#perc_site_name"), $.perc_textFilters.HOSTNAME);
                $('#perc_site_name').on('keypress keydown', function(evt){
                    clearCopySiteErrorMessage();
                });
                selectedTreePath = null;
                selectedCheckbox = null;
            },
            width: 450
        });

        function clearCopySiteErrorMessage()
        {
            //clear error label on keypress
            $('#perc_copysite_error').text("");
        }

        //Check the unicity of the Site name and confirm the copy process
        function validateAndConfirm(status, result){
            if(status === $.PercServiceUtils.STATUS_SUCCESS){
                var newSiteName = $("#perc_site_name").val();
                if (!_validateUniqueSiteName(newSiteName, result)){
                    $('#perc_copysite_error').text(I18N.message("perc.ui.new.site.dialog@Unique Name Req"));
                }
                else if (!_validateFolders(newSiteName))
                {
                    $.noop();
                }
            }
            else{
                $.unblockUI();
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
            }
        }

        //Dialog Html
        function createDialog(){
            var label =  $('<div/>').css('margin-bottom', '30px').
            append(
                $('<label/>').text('Enter a new name for your site.')
            );
            var labelSiteName =   $('<div/>').
            append(
                $('<label/>').
                attr('for', 'perc_site_name').
                text(I18N.message("perc.ui.copy.site.dialog@Site Name"))
            );
            var siteInput = "";

            siteInput =  $('<div/>').css('position', 'relative').
            append(
                $('<input/>').
                attr('type', 'text').
                css('width', '240px').
                attr('id', 'perc_site_name').
                attr('maxlength', 86)
            );

            var radioButtons =   $('<div/>').
            append(
                '<div id="copy-site-asset-options"><input style="width:15px; margin-bottom:10px; margin-top:0px; " type="radio" name="share-type-buttons" class="shareTypeButtons" id="share-type-buttons-across-site" value="Share Assets across site" checked="checked" >' +
                '<label style="margin-left: 3px; margin-top:0px;" for="share-type-buttons-across-site">Share assets across sites.</label></div>' +
                '<div><input style="width:15px; margin-top:0px;" type="radio" name="share-type-buttons" class="shareTypeButtons" id="share-type-buttons-selected-folder" value="Copy Assets from the selected source">' +
                '<label style="margin-left: 3px; margin-top:0px;" for="share-type-buttons-selected-folder">Copy assets from the selected folder.</label></div>'
            ).
            css("margin-top", "8px");

            var labelTree =   $('<div/>').
            css("margin-top", "0px").
            append(
                $('<label/>').
                attr('for', 'perc-share-assets-location-tree').
                text('Specify the asset folder to copy:').
                css("font-weight", "bold"));


            var locationTree = $("<div id='perc-share-assets-location-tree' class='perc-share-assets-location-tree'>").
            css("margin-top", "7px")
                .PercFinderTree({
                    filter:$.PercFinderTreeConstants.FOLDERS_ONLY,
                    rootPath:$.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                    initialPath:$.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                    height:"135px",
                    width:"320px",
                    classNames:{container:"perc-section-selector-container", selected:"perc-section-selected-item"},
                    onClick:function(path){
                        selectedTreePath = path;
                        clearCopySiteErrorMessage();
                        $(".dynatree-active").removeClass("dynatree-active");
                    },
                    onRenderComplete: function(initialPath, initialNode)
                    {
                        $(initialNode.span).addClass("dynatree-active");
                        selectedTreePath = initialPath;
                    }
                });

            var treeContainer = $('<div id="perc-share-assets-tree-container" style="display: none;" />').
            append(labelTree).
            append(locationTree).
            css("margin-top", "22px").
            css("margin-left", "10px");

            $(document).on('change', 'input:radio[name=share-type-buttons]', function() {
                clearCopySiteErrorMessage();
                selectedCheckbox = $(this).attr("id");
                if ($(this).attr("id") === "share-type-buttons-across-site")
                    $("#perc-share-assets-tree-container").css("display", "none");
                else
                    $("#perc-share-assets-tree-container").css("display", "block");
            });

            var labelError = $('<div/>').
            append(
                $('<label/>').
                css('color', 'red').
                attr('id', 'perc_copysite_error').
                attr('for', 'perc_site_name')
            ).
            css("margin-top", "15px");


            var $dialogHtml = $('<div/>').
            append(label).
            append(labelSiteName).
            append(siteInput).
            append(radioButtons).
            append(treeContainer).
            append(labelError);

            return $dialogHtml;
        }

        function createConfirmDialog (newSiteName)
        {
            var settings = {
                id: "perc-copysite-dialog-confirm",
                title: I18N.message("perc.ui.copy.site.dialog@Copy Site Confirmation"),
                question: I18N.message("perc.ui.copy.site.dialog@A new site") + newSiteName + I18N.message("perc.ui.copy.site.dialog@Will Be Created") + siteName + I18N.message("perc.ui.copy.site.dialog@The Copy") +
                    I18N.message("perc.ui.copy.site.dialog@Running In Background") +
                    I18N.message("perc.ui.copy.site.dialog@Depending On Size") + siteName + "'. '" + newSiteName + I18N.message("perc.ui.copy.site.dialog@Will Be Added") +
                    I18N.message("perc.ui.copy.site.dialog@Finder When Copy Processing"),
                success: function(){
                    $('#perc-site-map-copy').addClass("perc-site-map-action-item-disabled");

                    var jsonRequest = createRequestObject(newSiteName, siteName);

                    $.PercSiteService.copySite(jsonRequest, function(status, result, defaultCode){

                        if(status === $.PercServiceUtils.STATUS_ERROR && result !== "")
                        {
                            $.perc_utils.alert_dialog({title: 'Error', content: result});
                        }else{
                            if ($('#perc-site-map-copy'))
                                $('#perc-site-map-copy').removeClass("perc-site-map-action-item-disabled");
                        }
                    });
                },
                cancel: function(){},
                yes: "Continue anyway",
                width: 370
            };

            dialog.remove();
            $.perc_utils.confirm_dialog(settings);
        }

        //calculate the new site name
        function generateNewSiteName(siteName){
            $.PercSiteService.getSites(function(status, result){
                var nro = "";
                siteName = siteName + '-copy';
                while(!_validateUniqueSiteName(siteName + nro, result)){
                    if (nro === "") {nro = 2;}
                    else{nro++;}
                }
                $('#perc_site_name').val(siteName + nro);
                $.unblockUI();
            });
        }

        // Calls the service in the server to validate the folder sent before removing the dialog
        function _validateFolders(newSiteName){
            if (selectedCheckbox === "share-type-buttons-selected-folder")
            {
                var jsonRequest = {"ValidateCopyFoldersRequest":{
                        "srcFolder" : selectedTreePath.path.replace($.perc_paths.ASSETS_ROOT + "/", ""),
                        "destFolder" : newSiteName}};

                $.PercSiteService.validateCopySiteFolders(jsonRequest, function(statusFolders, resultFolders, defaultCodeFolders){

                    if(statusFolders === $.PercServiceUtils.STATUS_ERROR)
                    {
                        $('#perc_copysite_error').text(resultFolders);
                        return false;
                    }
                    else
                    {
                        createConfirmDialog(newSiteName);
                        return true;
                    }
                });
            }
            else
            {
                createConfirmDialog(newSiteName);
                return true;
            }
        }

        function createRequestObject(newSiteName, siteName)
        {
            var jsonRequest = {"SiteCopyRequest":{"srcSite" : siteName, "copySite" : newSiteName, "assetFolder" : null}};
            if (selectedCheckbox === "share-type-buttons-selected-folder")
            {
                jsonRequest.SiteCopyRequest.assetFolder = selectedTreePath.path.replace($.perc_paths.ASSETS_ROOT + "/", "");
            }
            else
            {
                delete jsonRequest.SiteCopyRequest.assetFolder;
            }
            return jsonRequest;
        }

        /**
         * Retrieves the existing sites from the response object and compares the value against each one of those (case-
         * insensitive.)
         * @param value {String} - the name of the site to check, must be a non-empty string.
         * @param response {} - object containing an array of existing site summaries
         * @return true if the name does not conflict w/ any existing name, false otherwise
         */
        function _validateUniqueSiteName(value, response){
            //we need to add an assertion 'framework' and check them here
            for(i = 0; i < response.SiteSummary.length; i++)
            {
                if((response.SiteSummary[i].name + "").toLowerCase() === value.toLowerCase())
                {
                    return false;
                }
            }
            return true;
        }

    }// End open dialog
})(jQuery);
