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

/**
 * Section tree Dialog, displays the sections as a tree.
 */
(function($){
    //Public API for the section tree dialog.
    $.PercSectionTreeDialog = {
        open: openDialog
    };
    /**
     * Opens the section tree dialog and shows all the sections in expanded state.
     * @param siteName(String), assumed to be a valid name of a site.
     * @param excludeId(String) the string format of the guid of the section that needs to be excluded from the tree,
     * this section and all sub-sections below it are not rendered.
     * @param treeLabel (String), the label for the tree section.
     * @param dlgTitle (String), the dialog title.
     * @param okButton, A string representing what button needs to be rendered for positive action. Currently supported
     * Strings are Move and Select.
     * @param okCallback (function), function to call when user clicks on OK button. Called like okCallback(sectionId,sectionPath),
     * where sectionId(String) is the string format of the guid of the selected section and sectionPath(String) is
     * the path of the section from the root.
     *
     */
    function openDialog(siteName, excludeId, treeLabel, dlgTitle, okButton, okCallback)
    {

        var self = this;
        var $dialog = null;
        // Get section tree
        $.Perc_SectionServiceClient.getTree(siteName, function(status, result){
            if(status === $.PercServiceUtils.STATUS_SUCCESS)
            {
                $dialog =   $("<div/>")
                    .append(
                        $("<div style='float:left;'/>").html(treeLabel)
                            .append(
                                $("<div id='perc-movesection-tree' />")
                                    .append(
                                        $("<ul/>")
                                            .append(buildSectionTreeList(result.SectionNode))
                                    )
                            )
                    )
                    .append(
                        $("<div class='ui-layout-south'/>")
                            .append(
                                $("<div id='perc_buttons' style='z-index: 100;'>")
                            )
                    )

                    .perc_dialog({
                        title: dlgTitle,
                        resizable: false,
                        modal: true,
                        percButtons:    {
                            "Move": {
                                click: function(){
                                    onOk();
                                },
                                id: "perc-movesection-move"
                            },
                            "Select": {
                                click: function(){
                                    onOk();
                                },
                                id: "perc-select-section-button"
                            },
                            "Cancel":{
                                click: function(){
                                    $dialog.remove();
                                },
                                id: "perc-movesection-cancel"
                            }
                        },
                        open: function(){
                            if(okButton === "Select")
                            {
                                $("#perc-movesection-move").hide();
                                $("#perc-select-section-button").show();
                            }
                            else
                            {
                                $("#perc-movesection-move").show();
                                $("#perc-select-section-button").hide();
                            }
                        },
                        id: "perc-move-section-dialog",
                        width: 600
                    });
                $("#perc-movesection-tree").dynatree({
                    imagePath: "/cm/images/images/"
                });
                // Expand all nodes
                $("#perc-movesection-tree").dynatree("getRoot").visit(function(dtnode){
                    dtnode.expand(true);
                });

            }
            else
            {
                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result.data});
            }
        });

        /**
         * Helper function to handle OK button(Move/Select) click.
         * Calls the supplied OK callback with selected item id and the path from root.
         */
        function onOk()
        {
            var sel =
                $("#perc-movesection-tree").dynatree("getActiveNode");
            var nodePath = "";
            nodePath = getSelectedNodePath(sel, nodePath);
            if(typeof(sel) != "undefined" && sel != null)
            {
                $dialog.remove();
                var temp = sel.data.key.split(/\|/);
                okCallback(temp[1],nodePath);
            }

        }

        /**
         * Helper function to create the section tree HTML list for the section target
         * selection dialog.
         * @param sectionNode {Object} the SectionNode object as returned from the
         * server. Cannot be <code>null</code>.
         * @return the HTML list that will represent the tree.
         * @type String
         */
        function buildSectionTreeList(sectionNode){
            var results;
            if(sectionNode.sectionType && sectionNode.sectionType !== $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION)
                return "";
            if(sectionNode.id === excludeId)
                return "";
            menuTitle = sectionNode.title + "";
            results = $("<li/>")
                .attr("id", "perc_section_tree|" + sectionNode.id)
                .attr("data", "icon:'section.png',sectionName:'" + menuTitle.replace(/'/g, "\\'").replace(/"/g, "\\\"") + "'")
                .append(
                    $("<a/>").attr("href", "#").text(menuTitle)
                );

            if(sectionNode.childNodes !== "")
            {
                var children = sectionNode.childNodes.SectionNode;
                if (children === undefined) {
                    children = sectionNode.childNodes;
                }
                var ulItem = $("<ul/>");
                if(Array.isArray(children))
                {
                    var len = children.length;
                    for(var i = 0; i < len; i++)
                    {
                        results.append(ulItem.append(buildSectionTreeList(children[i])));
                    }
                }
                else
                {
                    results.append(ulItem.append(buildSectionTreeList(children)));
                }
            }
            return results;
        }

        /**
         * Returns the display path of the selected node from the root.
         * @param selectedNode, dynatree node object assumed not null.
         * @param nodePath(String) this can be an empty string to start with, then the function recursively builds
         * the path by prepending /name to the path.
         * @return the path from the root to the selected node in the form of /Home/Section1/Section2...
         * @type String.
         */
        function getSelectedNodePath(selectedNode, nodePath)
        {
            if(selectedNode.data.sectionName == null)
                return nodePath;
            nodePath = "/" + selectedNode.data.sectionName + nodePath;
            nodePath = getSelectedNodePath(selectedNode.parent, nodePath);
            return nodePath;
        }
    }// End open dialog

})(jQuery);
