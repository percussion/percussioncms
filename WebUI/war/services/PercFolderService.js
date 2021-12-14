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
