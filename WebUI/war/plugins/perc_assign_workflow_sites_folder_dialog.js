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
 * perc_assign_workflow_sites_folder_dialog
 *
 * Show a dialog to assign sites and folder to a workflow.
 */
(function($){
    /**
     * Public API
     */
    $.perc_assign_workflow_sites_folder_dialog = {
        'createDialog' : createDialog,
        WORKFLOW_ASSIGNMENT_INPROCESS_MESSAGE : I18N.message("perc.ui.assign.workflow@Worklow Assignment In Process")
    };

    /*
     * Variables that will hold the original jsons retrieved from the server
     */
    var originalSitesJson;
    var originalAssetsJson;
    var siteJobId, assetJobId;
    var dialog;
    var selectedWorkflowName;
    var parentWasSelected;

    /*
     * Variable to keep track of the selected custom (righthand) checkboxes
     */
    var selectedCustomCheckboxes = [];
    /**
     * Creates the dialog
     * @param workflowname String the workflow that we are going to use to work.
     * @param onSave callback function invoked before removing the dialog after a successful save.
     * @return dialog element
     */
    function createDialog(selectedWorkflow, onSave)
    {
        originalSitesJson = originalAssetsJson = siteJobId = assetJobId = null;
        var buttons = {
            // FIXME: I18N
            'Save' : {
                'id' : "perc-assign-workflow-sites-folder-dialog-save",
                'click' : function() {
                    var selectedSites = jQuery("#datatree" + "-workflow-sites").dynatree('getTree').getSelectedNodes();
                    var selectedAssets = jQuery("#datatree" + "-workflow-assets").dynatree('getTree').getSelectedNodes();
                    var selected = selectedSites;
                    $.merge(selected, selectedAssets);
                    var selectedSitesPaths = [];
                    for (i = 0; i < selected.length; i++)
                    {
                        selectedSitesPaths.push(selected[i].data.key);
                    }

                    // Get unassigned folders
                    var unassignedFolders = getUnassignedFolders(selectedSitesPaths, selectedWorkflow);
                    var appliedFolders = selectedCustomCheckboxes;
                    var jsonAssignements = {"workflowAssignment" :
                            {
                                "workflowName": selectedWorkflow,
                                "assignedFolders": selectedSitesPaths,
                                "unassignedFolders" : unassignedFolders,
                                "appliedFolders" : appliedFolders
                            }
                    };

                    var theDialog = $(this);
                    $.PercBlockUI();
                    function assignFoldersWithWorkflow(){
                        $.PercFolderService.assignFoldersWithWorkflow(jsonAssignements, function(status, result) {
                            if (status === $.PercServiceUtils.STATUS_ERROR)
                            {
                                $.perc_utils.alert_dialog({title: I18N.message('perc.ui.page.general@Warning'), content: result});
                            }
                            else
                            {
                                if (typeof(onSave) != 'undefined' && typeof(onSave) == 'function')
                                {
                                    onSave();
                                }
                                theDialog.remove();
                            }
                            $.unblockUI();
                        }, function(){
                            theDialog.remove();
                            $.unblockUI();
                        });
                    }
                    $.PercFolderService.isWorkflowAssignmentInProgress(function(status){
                        if(status === "true"){
                            $.unblockUI();
                            $.perc_utils.alert_dialog({
                                "title" : I18N.message("perc.ui.page.general@Warning"),
                                "content" : $.perc_assign_workflow_sites_folder_dialog.WORKFLOW_ASSIGNMENT_INPROCESS_MESSAGE,
                                "okCallBack":function(){
                                    window.location.reload();
                                }
                            });
                        }
                        else{
                            assignFoldersWithWorkflow();
                        }

                    });

                }
            },
            //FIXME:I18N
            'Cancel' : {
                'id' : "perc-assign-workflow-sites-folder-dialog-cancel",
                'click' : function() { $(this).remove(); }
            }
        };

        // Initializes the custom checkboxes array
        selectedCustomCheckboxes = [];
        // Info title of the apply icon.
        var infoTitle = I18N.message("perc.ui.assign.workflow@Apply Settings to Folder");
        // Clear the dialog variable, just in case
        selectedWorkflowName = selectedWorkflow;
        $(dialog).remove();
        dialog = $('<div id="perc-assign-workflow-wrapper"></div>')
            .css("maxHeight", "350px")
            .css("background-color", "white")
            .css("padding-top", "0px");

        dialog.append('<div class="perc-loading-warning-message" style="margin-left:20px;margin-bottom:5px;"><img src="../css/dynatree/skin/loading.gif" style="vertical-align:bottom"/>' + I18N.message("perc.ui.assign.workflow@Workflow Loading") + ' </div>');

        // Create Sites collapsible panel
        dialog.append($('<div id="perc-assign-workflow-sites-title-wrapper"></div>')
            .append(
                $('<div id="perc-assign-workflow-sites-title"></div>')
                    .append($('<div id="perc-assign-workflow-sites-expander" style="display: inline;"></div>')
                        .append($('<div id="perc-assign-workflow-sites-title-span" class="perc-assign-workflow-expander-image collapsed">&nbsp;</div>'))
                        .append('<span>' + I18N.message("perc.ui.assign.workflow@Sites Collapsible Panel") + '</span>')
                        .click(function() {
                            $("#perc-assign-workflow-sites-tree-wrapper").toggle();
                            $("#perc-assign-workflow-sites-title-span").toggleClass('collapsed');
                            swapShowInfoSpans("sites");
                        })
                    )
                    .append($('<span class="perc-assign-workflow-sites-apply-top">Apply <span class="perc-assign-workflow-sites-apply-top-icon" title="' + infoTitle + '"/></span>')
                    )
            )
        );

        var tree_container = $('<div id="perc-assign-workflow-sites-tree"></div>');

        dialog.append($('<div id="perc-assign-workflow-sites-tree-wrapper"></div>')
            .append(tree_container));

        // Create Assets collapsible panel
        dialog.append($('<div id="perc-assign-workflow-assets-title-wrapper"></div>')
            .append(
                $('<div id="perc-assign-workflow-assets-title"></div>')
                    .append($('<div id="perc-assign-workflow-assets-expander" style="display: inline;"></div>')
                        .append($('<div id="perc-assign-workflow-assets-title-span" class="perc-assign-workflow-expander-image collapsed">&nbsp;</div>'))
                        .append('<span> ' + I18N.message("perc.ui.assign.workflow@Assets Collapsible Panel") + ' </span>')
                        .click(function() {
                            $("#perc-assign-workflow-assets-tree-wrapper").toggle();
                            $("#perc-assign-workflow-assets-title-span").toggleClass('collapsed');
                            swapShowInfoSpans("assets");
                        })
                    )
                    .append($('<span class="perc-assign-workflow-assets-apply-top">Apply <span class="perc-assign-workflow-assets-apply-top-icon" title="' + infoTitle + '"/></span>')
                    )
            )
        );


        var tree_container_assets = $('<div id="perc-assign-workflow-assets-tree"></div>');
        dialog.append($('<div id="perc-assign-workflow-assets-tree-wrapper"></div>')
            .append(tree_container_assets));

        dialog.perc_dialog({
            title: I18N.message('perc.ui.assign.workflow@Assigned Sites and Folders'),
            id: "perc-assign-workflow-sites-folder-dialog",
            width: 686,
            maxWidth: 1200,
            resizable : true,
            modal: true,
            percButtons: buttons,
            resizeStop: function(event) { $('#perc-assign-workflow-wrapper').css('maxHeight', '100%'); }
        });

        // We need to do this since the dynatree needs to be added to the DOM
        var dataTreeSitesConfig = {
            'instanceIdSuffix': '-workflow-sites',
            'addHeader'         : false,
            'collapsible'       : false,
            'showCheckboxes'    : true,
            'selectedWorkflow'  : selectedWorkflow,
            'showRoots'         : false,
            'customOnRender'    : customOnRender,
            'customOnExpand'    : customOnExpand,
            'onSelect'          : customOnSelect
        };
        var dataTreeAssetsConfig = {
            'instanceIdSuffix'  : '-workflow-assets',
            'addHeader'         : false,
            'collapsible'       : false,
            'showCheckboxes'    : true,
            'selectedWorkflow'  : selectedWorkflow,
            'showRoots'         : false,
            'customOnRender'    : customOnRender,
            'customOnExpand'    : customOnExpand,
            'onSelect'          : customOnSelect
        };
        var tree_container_sites = $("#perc-assign-workflow-sites-tree");
        var tree_container_assets = $("#perc-assign-workflow-assets-tree");
        // Initialize the PercDataTree plugin
        $.PercDataTree.init(tree_container_sites, dataTreeSitesConfig);
        $.PercDataTree.init(tree_container_assets, dataTreeAssetsConfig);
        startSiteAssetFoldersLoadingJobs(selectedWorkflow);
        tree_container.append($('<span>' + I18N.message("perc.ui.assign.workflow@Workflow Tree Apply") + '</span>')
            .addClass("perc-assign-workflow-sites-apply-bottom").hide());
        tree_container_assets.append($('<span>' + I18N.message("perc.ui.assign.workflow@Workflow Tree Apply") + '</span>')
            .addClass("perc-assign-workflow-assets-apply-bottom").hide());

        // Adds the warning message just after the trees container
        $("#perc-assign-workflow-wrapper").after('<div class="perc-assign-workflow-warning"> ' + I18N.message("perc.ui.assign.workflow@Workflow Processing Warning Background") + ' <br /> ' + I18N.message("perc.ui.assign.workflow@Workflow Processing Warning Exclusion") + '</div>');

        return dialog;
    }
    function startSiteAssetFoldersLoadingJobs(selectedWorkflow)
    {
        $.PercFolderService.startAssociatedFoldersJob('Sites', selectedWorkflow, function(status, result) {
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"), content: result});
                return false;
            }
            siteJobId = result;
            $.PercFolderService.startAssociatedFoldersJob('Assets', selectedWorkflow, function(status, result) {
                if(status === $.PercServiceUtils.STATUS_ERROR)
                {
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.labels@Error"), content: result});
                    return false;
                }
                assetJobId = result;
                loadSiteFolders();
                loadAssetFolders();
            }, true);

        }, true);

    }
    function loadSiteFolders()
    {
        if(siteJobId == null)
            return;
        $.PercFolderService.getAssociatedFoldersJobStatus(siteJobId, function(status, result) {
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.assign.workflow@Loading Site Folders Error"), content: result});
                return false;
            }
            var status = result.GetAssginedFoldersJobStatus.status;
            var message = result.GetAssginedFoldersJobStatus.message; // message will be blank if job is still running
            if( message === ""  && status !== "100" )
            {
                setTimeout(loadSiteFolders, 1000);
                return;
            }
            siteJobId = null;
            originalSitesJson = {};
            originalSitesJson.folderItem = result.GetAssginedFoldersJobStatus.folderItems;
            showFolderTrees();
        });
    }
    function loadAssetFolders()
    {
        if(assetJobId == null)
            return;
        $.PercFolderService.getAssociatedFoldersJobStatus(assetJobId, function(status, result) {
            if(status === $.PercServiceUtils.STATUS_ERROR)
            {
                $.perc_utils.alert_dialog({title: I18N.messsage("perc.ui.assign.workflow@Loading Asset Folders Error"), content: result});
                return false;
            }
            var status = result.GetAssginedFoldersJobStatus.status;
            var message = result.GetAssginedFoldersJobStatus.message; // message will be blank if job is still running
            if( message === ""  && status !== "100" )
            {
                setTimeout(loadAssetFolders, 1000);
                return;
            }
            assetJobId = null;
            originalAssetsJson = {};
            originalAssetsJson.folderItem = result.GetAssginedFoldersJobStatus.folderItems;
            showFolderTrees();
        });
    }
    function showFolderTrees()
    {
        if(originalSitesJson != null && originalAssetsJson != null)
        {
            var tree_container_sites = $("#perc-assign-workflow-sites-tree");
            var tree_container_assets = $("#perc-assign-workflow-assets-tree");
            if(originalSitesJson.folderItem){
                $.PercDataTree.updateTree(tree_container_sites, [originalSitesJson]);
                $.each($("#datatree-workflow-sites").dynatree("getRoot").childList, function(){
                    customOnExpand(this);
                });
                if (originalSitesJson.folderItem.length > 0)
                    $("span.perc-assign-workflow-sites-apply-bottom").show();
            }
            if(originalAssetsJson.folderItem){
                $.PercDataTree.updateTree(tree_container_assets, [originalAssetsJson]);
                $.each($("#datatree-workflow-assets").dynatree("getRoot").childList, function(){
                    customOnExpand(this);
                });
                if (originalAssetsJson.folderItem.length > 0)
                    $("span.perc-assign-workflow-assets-apply-bottom").show();
            }
            $(".perc-loading-warning-message").addClass("perc-loading-warning-message-hidden").removeClass("perc-loading-warning-message");
        }
    }
    /**
     * Toggles visibility of the apply labels
     *
     * @param treeType the type of is collapsing / expanding.
     * This param should take 1 of 2 values: "sites" or "assets"
     */
    function swapShowInfoSpans(treeType)
    {
        if ($("#perc-assign-workflow-" + treeType + "-title-span").hasClass("collapsed"))
        {
            $(".perc-assign-workflow-" + treeType + "-apply-top").show();
            if ((treeType === "sites" && originalSitesJson.folderItem && originalSitesJson.folderItem.length > 0) || (treeType === "assets" && originalAssetsJson.folderItem && originalAssetsJson.folderItem.length > 0))
                $(".perc-assign-workflow-" + treeType + "-apply-bottom").show();
        }
        else
        {
            $(".perc-assign-workflow-" + treeType + "-apply-top").hide();
            $(".perc-assign-workflow-" + treeType + "-apply-bottom").hide();
        }
    }

    /**
     * Function that updates neccesary extra rendering information when rendering the tree rows.
     * This function is mainly responsible for creating the new custom checkboxes for this particula implementation.
     * @param dtnode DynatreeNode
     */
    function customOnRender(dtnode)
    {
        span                = $(dtnode.span);
        if (span.find(".dynatree-custom-checkbox").length === 0)
        {
            var newSpan = $('<span />')
                .addClass("dynatree-custom-checkbox")
                .click(function(eventHandler){
                    if (!$(eventHandler.currentTarget).hasClass("dynatree-custom-checkbox-disabled"))
                    {
                        $(eventHandler.currentTarget).toggleClass("dynatree-custom-checkbox-selected");
                        if ($(eventHandler.currentTarget).hasClass("dynatree-custom-checkbox-selected"))
                        {
                            if ($.inArray(dtnode.data.key, selectedCustomCheckboxes) === -1)
                            {
                                selectedCustomCheckboxes.push(dtnode.data.key);
                            }
                            selectCustomCheckboxRecursive(true, dtnode);
                        }
                        else
                        {
                            selectedCustomCheckboxes.splice( $.inArray(dtnode.data.key, selectedCustomCheckboxes), 1 );
                            selectCustomCheckboxRecursive(false, dtnode);
                        }
                    }
                    checkWarningVisibility();
                });

            if (span.hasClass("perc-item-disabled"))
            {
                newSpan.addClass("dynatree-custom-checkbox-disabled");
            }
            else if ($.inArray(dtnode.data.key, selectedCustomCheckboxes) !== -1)
            {
                newSpan.addClass("dynatree-custom-checkbox-selected");
            }
            // This span is needed to apply the grey left border to the design
            var newSpanContainer = $('<span />')
                .addClass("dynatree-custom-checkbox-container")
                .append(newSpan);

            span.append(newSpanContainer);
        }

    }

    /**
     * Custom onExpand callback. Dynatree's default onExpand removes ous styling.
     * @param dtnode DynatreeNode
     */
    function customOnExpand(dtnode)
    {
        if (! dtnode.isSelected())
        {
            var other_selected = hasDescendantSelected(dtnode);
            if (other_selected)
            {
                $(dtnode.span).addClass("dynatree-partsel");
            }
            else
            {
                $(dtnode.span).removeClass("dynatree-partsel");
            }
        }
        customOnRender(dtnode);
        dtnode.visit(function(node) {
            if (! node.isSelected())
            {
                var other_selected = hasDescendantSelected(node);
                if (other_selected)
                {
                    $(node.span).addClass("dynatree-partsel");
                }
                else
                {
                    $(node.span).removeClass("dynatree-partsel");
                }
            }
            customOnRender(node);
        });
    }

    /**
     * Gets the custom checkbox selected. This function does 2 things:
     * - Adds the corresponding class to make the checkbox visually selected
     * - Adds the value of the selected custom checkboxes into internal array.
     * @param dtnode DynatreeNode to deselect the custom checkbox
     */
    function selectCustomCheckbox(dtnode)
    {
        $(dtnode.span).find(".dynatree-custom-checkbox").addClass("dynatree-custom-checkbox-selected");
        if ($.inArray(dtnode.data.key, selectedCustomCheckboxes) === -1)
        {
            selectedCustomCheckboxes.push(dtnode.data.key);
        }
    }

    /**
     * Gets the custom checkbox unselected. This function does 2 things:
     * - Removes the corresponding class to make the checkbox visually selected
     * - Removes the value of the selected custom checkboxes from internal array.
     * @param dtnode DynatreeNode to deselect the custom checkbox
     */
    function deselectCustomCheckbox(dtnode)
    {
        $(dtnode.span).find(".dynatree-custom-checkbox").removeClass("dynatree-custom-checkbox-selected");
        selectedCustomCheckboxes.splice( $.inArray(dtnode.data.key, selectedCustomCheckboxes), 1 );
    }

    /**
     * Recursive function to autoselect descendent custom checkboxes. If selected param is true
     * all the descendants will be selected as well, otherwise they will get unselected.
     * @param selected true(false) if the checkbox was checked(unchecked)
     * @param dtnode DynatreeNode to apply the changes and search for child nodes.
     */
    function selectCustomCheckboxRecursive(selected, dtnode)
    {
        var theChildren = dtnode.getChildren();
        var theChild;
        if (typeof(theChildren) == 'undefined' || theChildren == null)
            return;
        for (var k = 0; k < theChildren.length; k++)
        {
            theChild = theChildren[k];
            if (!$(theChild.span).hasClass("perc-item-disabled"))
            {
                if (selected)
                {
                    selectCustomCheckbox(theChild);
                }
                else
                {
                    deselectCustomCheckbox(theChild);
                }
            }
            selectCustomCheckboxRecursive(selected, theChild);
        }
    }

    /**
     * Cutom onSelect callback. Dynatree's select mode didn't fit the requirements.
     * @param selected true(false) if the checkbox was checked(unchecked)
     * @param dtnode DynatreeNode
     */
    function customOnSelect(selected, dtnode)
    {
        // Apply grey style to checkboxes and bold to labels
        checkParentNode(dtnode);
        selectCustomCheckbox(dtnode);

        // Check for the warning visibility, if at 1 least 1 right hand item is selected, warning should appear
        checkWarningVisibility();

        var theChildren = dtnode.getChildren();
        var theChild;
        if (typeof(theChildren) == 'undefined' || theChildren == null)
            return;
        for (var k = 0; k < theChildren.length; k++)
        {
            theChild = theChildren[k];
            theChild.select(selected);
            // Autoselect the right hand checkbox
            if (!$(theChild.span).hasClass("perc-item-disabled"))
            {
                selectCustomCheckbox(theChild);
            }
        }
    }

    /**
     * Check the visibility of the warning message.
     * If at least 1 custom (right hand) checkbox is selected the warning message should display.
     * Else the warning message should be hidden.
     */
    function checkWarningVisibility()
    {
        if (selectedCustomCheckboxes.length > 0)
        {
            $(".perc-assign-workflow-warning").show();
        }
        else
        {
            $(".perc-assign-workflow-warning").hide();
        }
    }

    /**
     * Apply bold style to the label of the node; and grey styles to the checkboxes.
     * @param dtnode DynatreeNode
     */
    function checkParentNode(dtnode)
    {
        span                = $(dtnode.span);
        parentSpan          = $(dtnode.getParent().span);
        allMyChildren       = span.next().find(".dynatree-checkbox");
        selectedMyChildren  = span.next().find(".dynatree-selected");
        selectedChildren    = parentSpan.next().find(".dynatree-selected");

        // If current node is checked and all of its children are checked too, turn it bold
        if (dtnode.isSelected() && allMyChildren.length === selectedMyChildren.length)
        {
            span.addClass("perc-item-all-same-workflow");
        }
        else
        {
            span.removeClass("perc-item-all-same-workflow");
        }


        // If current node is not selected but at least one child is, mark the checkbox grey
        if (! dtnode.getParent().isSelected())
        {
            if (selectedChildren.length > 0)
            {
                parentSpan.addClass("dynatree-partsel");
            }
            else
            {
                parentSpan.removeClass("dynatree-partsel");
            }
        }

        // Recursive call
        if (dtnode.getParent().getParent() != null)
        {
            checkParentNode(dtnode.getParent());
        }
    }

    /**
     * Apply bold style to the label of the node; and grey styles to the checkboxes.
     * @param dtnode DynatreeNode
     * @retun true if any descendant's checkbox is selected
     */
    function hasDescendantSelected(dtnode)
    {
        if (dtnode.isSelected())
        {
            return true;
        }
        if (dtnode.childList == null)
        {
            return false;
        }

        var result = false;
        for (var index = 0; index < dtnode.childList.length; index++)
        {
            result = result || hasDescendantSelected(dtnode.childList[index]);
        }
        return  result;
    }

    /**
     * Gets those paths (from sites and assets) that were selected on the
     * original json's but were unselected on the edit dialog.
     * @param selectedSitesPaths
     * @param selectedWorkflow
     * @returns Array
     */
    function getUnassignedFolders(selectedSitesPaths, selectedWorkflow)
    {
        // create the array of originally selected paths
        var originalAssignedPaths = getOriginalAssignedPaths(selectedWorkflow);

        // compare the original array with the new one
        for(i in selectedSitesPaths)
        {
            var indexInOriginal = $.inArray(selectedSitesPaths[i], originalAssignedPaths);
            if (indexInOriginal  > -1)
            {
                // Changes de originalAssinedPath array
                originalAssignedPaths.splice(indexInOriginal, 1);
            }
        }

        return originalAssignedPaths;
    }

    /**
     * Get the originally assigned paths, given a workflow name, for Sites and Assets nodes.
     * @param selectedWorkflow
     */
    function getOriginalAssignedPaths(selectedWorkflow)
    {
        var assignedPaths = [];

        // go for the sites first
        if(originalSitesJson.folderItem){
            $(originalSitesJson.folderItem).each(function(index, jsonObject){
                getAssignedPaths(jsonObject, selectedWorkflow, assignedPaths);
            });
        }
        // go for the assets
        if(originalAssetsJson.folderItem){
            $(originalAssetsJson.folderItem).each(function(index, jsonObject){
                getAssignedPaths(jsonObject, selectedWorkflow, assignedPaths);
            });
        }
        return assignedPaths;
    }

    /**
     * Recursive function that stores in an Array the paths of the nodes, given a workflow name.
     * @param jsonObject
     * @param workflowName
     * @param resultArray Where we are going to store the paths originally assigned
     */
    function getAssignedPaths(jsonObject, workflowName, resultArray)
    {
        // Check that the node is assigned to the workflow
        if (jsonObject.workflowName && jsonObject.workflowName === workflowName)
        {
            resultArray.push(jsonObject.id);
        }

        // Base case
        if (typeof(jsonObject.children) == 'undefined' || $.isEmptyObject(jsonObject.children))
        {
            return;
        }

        // make the call again - check if the children has more than one element
        // this is because of the CXF issue (a list with only one element is returned
        // as an object)
        if (recursive_case === typeof (jsonObject.children.child.length) === 'undefined')
        {
            getAssignedPaths(jsonObject.children.child, workflowName, resultArray);
        }
        else
        {
            $(jsonObject.children.child).each(function(index, child) {
                getAssignedPaths(child, workflowName, resultArray);
            });
        }
    }

})(jQuery);
