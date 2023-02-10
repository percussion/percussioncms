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

(function($) {
    // Exposed API/interface
    $.PercFolderService = {
        getFoldersWithWorkflow   : getFoldersWithWorkflow,
        assignFoldersWithWorkflow : assignFoldersWithWorkflow,
        startAssociatedFoldersJob : startAssociatedFoldersJob,
        getAssociatedFoldersJobStatus : getAssociatedFoldersJobStatus,
        getFolderPagesById : getFolderPagesById ,
        cancelAssociatedFoldersJob:cancelAssociatedFoldersJob,
        isWorkflowAssignmentInProgress:isWorkflowAssignmentInProgress
    };
    
    /**
     * Retrieves a JSON object with a data structure that contains the folders and
     * subfolders asigned to a workflow.
     * 
     * @param path
     * @param workflowName
     * @param callback
     * @param includeFoldersWithDifferentWorkflow
     */
    function getFoldersWithWorkflow(path, workflowName, callback, includeFoldersWithDifferentWorkflow)
    {
        var requestURL = $.perc_paths.FOLDERMGT_FOLDERS_WITH_WORKFLOW + '/' + encodeURI(workflowName) + '/' + path;
        if(includeFoldersWithDifferentWorkflow)
        {
            requestURL = requestURL + '?includeFoldersWithDifferentWorkflow=true';
        }
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
        
    }
    
    function startAssociatedFoldersJob(path, workflowName, callback, includeFoldersWithDifferentWorkflow)
    {
        var requestURL = $.perc_paths.FOLDERMGT_START_ASSOCIATED_FOLDERS_JOB + encodeURI(workflowName) + '/' + path;
        if(includeFoldersWithDifferentWorkflow)
        {
            requestURL = requestURL + '?includeFoldersWithDifferentWorkflow=true';
        }
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
        
        
    }
    function getAssociatedFoldersJobStatus(jobId, callback)
    {
        var requestURL = $.perc_paths.FOLDERMGT_GET_ASSOCIATED_FOLDERS_JOB_STATUS + jobId;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    /**
     * Makes the request needed to assign and unassign sites/folders to a workflow.
     *  
     * @param workflowAssignment
     * @param callback
     */
    function assignFoldersWithWorkflow(workflowAssignment, callback, abortCallback)
    {
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.FOLDERMGT_FOLDERS_WITH_WORKFLOW_ASSIGN,
            $.PercServiceUtils.TYPE_POST,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            },
            workflowAssignment,abortCallback,5000
        );
    }
    
    function getFolderPagesById(folderId, callback){
        $.PercServiceUtils.makeJsonRequest(
            $.perc_paths.FOLDERMGT_FOLDER_PAGES + folderId,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result)
            {
                if(status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(true, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback(false, defaultMsg);
                }
            }
        );
        
    }
    
    function cancelAssociatedFoldersJob(jobId, sync, callback){
        var requestURL = $.perc_paths.FOLDERMGT_CANCEL_ASSOCIATED_FOLDERS_JOB + jobId;
        $.PercServiceUtils.makeJsonRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            sync,
            function(status, result) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback($.PercServiceUtils.STATUS_SUCCESS, result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    callback($.PercServiceUtils.STATUS_ERROR, defaultMsg);
                }
            }
        );
    }
    function isWorkflowAssignmentInProgress(callback){
        var requestURL = $.perc_paths.FOLDERMGT_IS_WORKFLOW_ASSIGN_PROGRESS;
        $.PercServiceUtils.makeRequest(
            requestURL,
            $.PercServiceUtils.TYPE_GET,
            false,
            function(status, result) {
                if (status === $.PercServiceUtils.STATUS_SUCCESS)
                {
                    callback(result.data);
                }
                else
                {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result.request);
                    $.perc_utils.info(defaultMsg);
                    callback(false);
                }
            }
        );
    }
})(jQuery);
