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

// Define globals
var inprogress = false;
var currentcount = 0;
var targetpath = "";
var cancelCalled = false;
var sizeLimit = 157286400;
var bigFileIds = [];
var bigFiles = [];

$(document).ready(function () {
    // Initialize the finder tree for asset selection
    $('#perc-bulk-target-tree').PercFinderTree({
        showFoldersOnly: true,
        rootPath: $.PercFinderTreeConstants.ROOT_PATH_ASSETS,
        classNames: {'container': "perc-folder-selector-container", 'selected':"perc-folder-selected-item"},
        initialPath: '/Assets/uploads',
        onClick: function(pathItem){
            if(pathItem.path == "/Assets/")
            {
                _setApprovalCheckboxState(false);
                return;
            }
            if(pathItem.accessLevel == "READ")
            {
                _setApprovalCheckboxState(false);
                var msg = "You don't have permission to upload to " + pathItem.path;
                PERC_UTILS.alert_dialog({title: "Warning", content: msg});
                return;
            }
            handleChecks(pathItem);
        },
        onQueryActivate: function(flag, dtnode){
            var pathItem = dtnode.data.pathItem;
            var accessLevel = pathItem.accessLevel;
            var path = pathItem.path;
            if(accessLevel == "READ")
            {
                return false;
            }
            if(path == "/Assets/")
            {
                return false;
            }
            return true;
        }
    });

    $('#perc-files-upload').change(function() {
        console.log('Change deteceted');
        _handleButtonEnableState();
    });

    $('#perc-html-selector').keyup(function() {
        console.log('Change deteceted');
        _handleButtonEnableState();
    });

    $('#perc-bulk-asset-type').change(function() {
        _handleButtonEnableState();
    });
});

function handleChecks(pathItem) {
    $('#perc-bulk-target-folder').text(pathItem.path);
    $('#perc-bulk-target-folder').data("pathItem", pathItem);
    targetpath = pathItem.path;
    _handleApprovalCheckboxState(targetpath);
    _handleButtonEnableState();
}

function _handleApprovalCheckboxState(folderPath)
{
    $.PercWorkflowService().isApproveAvailableToCurrentUser(folderPath, function(status,result)
    {
        _setApprovalCheckboxState(result.data);
    });
}

function _setApprovalCheckboxState(enabled)
{
    var approvalCheckbox = $("#perc-bulk-approve-onupload");
    var approvalLabel = $("#perc-bulk-approve-onupload-label");
    if(enabled)
    {
        approvalCheckbox.prop('disabled', false);
        approvalLabel.removeClass('perc-approval-label-disabled');
    }
    else
    {
        //if user doesn't have permission to approve, uncheck and disable
        approvalCheckbox.prop('checked', false);
        approvalCheckbox.prop('disabled', true);
        approvalLabel.addClass('perc-approval-label-disabled');
    }
}

function _handleButtonEnableState(){
    // Cancel button
    if(inprogress && !cancelCalled)
    {
        $("#perc-bulk-cancel-upload").removeClass("perc-inactive");
    }
    else
    {
        $("#perc-bulk-cancel-upload").addClass("perc-inactive");
    }

    // Upload button

    // If asset type is one of HTML, Richtext or Simpletext - check for the css
    // selector input field value. If there is no value deactivate the button.
    var enableUpload = true;
    var percAssetType = $('#perc-bulk-asset-type option:selected').val();
    if( percAssetType == "html" ||  percAssetType == "richtext" ||  percAssetType == "simpletext")
    {
        var percCssSelectorValue = $('#perc-html-selector').val();
        if (percCssSelectorValue == null || percCssSelectorValue == "" )
        {
            var enableUpload = false;
        }
    }
    var filesList = $('input[type="file"]').prop('files');
    var totalfilecount = filesList.length;
    if (totalfilecount === 0) {
        totalfilecount = TOTAL_IN_QUEUE;
    }
    if(enableUpload && !inprogress && targetpath.length > 0 && totalfilecount > 0)
    {
        $("#perc-upload-start").attr('disabled', false);
    }
    else
    {
        $("#perc-upload-start").attr('disabled', true);
    }

    // Clear all button
    if(totalfilecount > 0 && !inprogress)
    {
        $("#perc-upload-clear").attr('disabled', false);
    }
    else
    {
        $("#perc-upload-clear").attr('disabled', true);
    }

    // Asset type
    if(inprogress)
    {
        $('#perc-bulk-asset-type').attr("disabled", "true");
    }
    else
    {
        $('#perc-bulk-asset-type').removeAttr("disabled");
    }

}
