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

// Define globals
var inprogress = false;
var currentcount = 0;
var targetpath = "";
var cancelCalled = false;
var sizeLimit = 157286400;
var bigFileIds = [];
var bigFiles = [];

$(function () {
    // Initialize the finder tree for asset selection
    $('#perc-bulk-target-tree').PercFinderTree({
        showFoldersOnly: true,
        rootPath: $.PercFinderTreeConstants.ROOT_PATH_ASSETS,
        classNames: {'container': "perc-folder-selector-container", 'selected':"perc-folder-selected-item"},
        initialPath: '/Assets/uploads',
        onClick: function(pathItem){
            if(pathItem.path === "/Assets/")
            {
                _setApprovalCheckboxState(false);
                return;
            }
            if(pathItem.accessLevel === "READ")
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
            if(accessLevel === "READ")
            {
                return false;
            }
            if(path === "/Assets/")
            {
                return false;
            }
            return true;
        },
        onRenderComplete: function(initialPath, initialNode){
            $("perc-bulk-target-tree").show();
        }
    });

    $('#perc-files-upload').on("change",function() {
        console.log('Change detected');
        _handleButtonEnableState();
    });

    $('#perc-html-selector').on("keyup",function() {
        console.log('Change deteceted');
        _handleButtonEnableState();
    });

    $('#perc-bulk-asset-type').on("change", function() {
        _handleButtonEnableState();
    });
});

function handleChecks(pathItem) {
    $('#perc-bulk-target-folder').text(pathItem.path)
        .data("pathItem", pathItem);
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
    if( percAssetType === "html" ||  percAssetType === "richtext" ||  percAssetType === "simpletext")
    {
        var percCssSelectorValue = $('#perc-html-selector').val();
        if (percCssSelectorValue == null || percCssSelectorValue === "" )
        {
            enableUpload = false;
        }
    }
    var filesList = $('input[type="file"]').prop('files');
    var totalfilecount = filesList.length;
    if (totalfilecount === 0) {
        totalfilecount = TOTAL_IN_QUEUE;
    }
    if(enableUpload && !inprogress && targetpath.length > 0 && totalfilecount > 0)
    {
        $("#perc-upload-start").prop('disabled', false);
    }
    else
    {
        $("#perc-upload-start").prop('disabled', true);
    }

    // Clear all button
    if(totalfilecount > 0 && !inprogress)
    {
        $("#perc-upload-clear").prop('disabled', false);
    }
    else
    {
        $("#perc-upload-clear").prop('disabled', true);
    }

    // Asset type
    if(inprogress)
    {
        $('#perc-bulk-asset-type').prop("disabled", true);
    }
    else
    {
        $('#perc-bulk-asset-type').prop("disabled", false);
    }

}
