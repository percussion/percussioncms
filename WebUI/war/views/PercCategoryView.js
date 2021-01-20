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
 * PercCategoryView.js
 * 
 */
(function($) {

    var dirtyController = $.PercDirtyController;
    var sitesList = [];
        
    $.PercCategoryView = function() {
        
        var viewApi = {
            init                            : init,
            getCategories                   : getCategories,
            showSelectedCategoryEditor      : showSelectedCategoryEditor,
            alertDialog                     : alertDialog,
            errorDialog                     : errorDialog,
            confirmDialog                   : confirmDialog,
            editCategories                  : editCategories,
            displayCategoryDetails          : displayCategoryDetails,
            deleteCategory                  : deleteCategory,
            handleDelete                    : handleDelete,
            getCurrentDate                  : getCurrentDate,
            visitTreeForBaseProperties      : visitTreeForBaseProperties,
            manageDynaProps                 : manageDynaProps,
            getUpdatedCategoryArray         : getUpdatedCategoryArray,
            updateCategoryXML               : updateCategoryXML,
            save                            : save,
            findUpTargetNode                : findUpTargetNode,
            moveNodeUp                      : moveNodeUp,
            findDownTargetNode              : findDownTargetNode,
            moveNodeDown                    : moveNodeDown,
            publishToDTS                    : publishToDTS
            
        }

        // A snippet to adjust the frame size on resizing the window.
        $(window).resize(function() {
            fixIframeHeight();
            fixTemplateHeight();
        });

        var container = $("#perc-category-tree");
        var controller = $.PercCategoryController;
        var sitename="";
        var categories;
        var editing = false;
        var isMoved = false;
        var isDelete = false;
        var isPublished = false;
        var siteSelection;
        var originalTitle = null;
        var generateUid = function () {
            var delim = "-";

            function S4() {
                return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
            }

            return (S4() + S4() + delim + S4() + delim + S4() + delim + S4() + delim + S4() + S4() + S4());
        };
        
        controller.init(viewApi);
        
        function addSitesToDropdown(selectionid, siteArray, selectedArray, allSitesOption) {
             var optionsAsString = "";  
             if (allSitesOption)
             {
                optionsAsString += '<option value="">All Sites</option>';
             }
             for(i = 0; i < siteArray.length; i++)
             {
                 selectedString="";
                 if (selectedArray != null && $.inArray(siteArray[i], selectedArray) > -1)
                 {
                     selectedString=" selected='selected'";
                 }
                 optionsAsString += '<option value="' + siteArray[i] + '"' +selectedString+ '>' + siteArray[i] + '</option>';
             }
             $( selectionid ).html( optionsAsString );
        };
        
        function stringToList(string)
        {
            if (string != null )
                return string.split(',');
            else
                return [];
        }
        

        function getSelectedSites(node) {
            if (node == null || typeof node == "undefined")
            {
                return sitesList;
            }
            if ( node.data.allowedSites == null || typeof node.data.allowedSites == "undefined")
            {
                return getAllowedSites(node);

            }
                return node.data.allowedSites.split(",");
        }
        
        function getAllowedSites(node) {
            if (node == null || typeof node == "undefined")
            {
                return sitesList;
            }
            return getSelectedSites(node.getParent());
        }
        
        function init() {
            
             $.PercSiteService.getSites(function(status, result){
                 var optionsAsString = "";      
                 for(i = 0; i < result.SiteSummary.length; i++)
                 {
                     sitesList.push(result.SiteSummary[i].name);
                 }
                 addSitesToDropdown("#perc-category-site-dropdown",sitesList,null, true);
                 sitename = $('#perc-category-site-dropdown').find(":selected").val();
                 controller.getCategories(sitename);
             })
             
            
            $( "#perc-category-site-dropdown" ).change(function() {

                if (editing)
                {
                        currentlyEditing();
                        siteSelection.prop("selected",true);
                        return;
                }
                sitename = $('#perc-category-site-dropdown').find(":selected").val();

                container.dynatree("destroy");
                controller.getCategories(this.value);
            });
            
            $("#perc-categories-add-category-button").unbind().click(function(){
                
                if (!$.PercNavigationManager.isAdmin()) {
                    alertDialog(I18N.message("perc.ui.category.view@User Admin"), I18N.message("perc.ui.category.view@User Admin Delete"));
                    return;
                }
                
                if (editing)
                {
                        currentlyEditing();
                        return;
                }


                var node = newNode(false);
                displayCategoryDetails(node);
                showSelectedCategoryEditor(node);

            });

            $("#perc-categories-add-child-category-button").unbind().click(function(){
                
                if (!$.PercNavigationManager.isAdmin()) {
                    alertDialog(I18N.message("perc.ui.category.view@User Admin"), I18N.message("perc.ui.category.view@User Admin Delete"));
                    return;
                }
                
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                
                var node = newNode(true);
                displayCategoryDetails(node);
                showSelectedCategoryEditor(node);

            });
            
            $("#perc-categories-delete-category-button").unbind().click(function(){
                
                if (!$.PercNavigationManager.isAdmin()) {
                    alertDialog(I18N.message("perc.ui.category.view@User Admin"), I18N.message("perc.ui.category.view@User Admin Delete"));
                    return;
                }
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                var tree = container.dynatree("getTree");
                if(tree.count() == 1)
                    alertDialog(I18N.message("perc.ui.category.view@Delete Category"), I18N.message("perc.ui.category.view@Cannot Delete Node"));
                else {
                    isDelete = true;
                    
                    deleteCategory();
                    editing = false;
                }

            });
            
            $("#perc-categories-edit-category-button").unbind().click(function(){
                
                if (!$.PercNavigationManager.isAdmin()) {
                    alertDialog(I18N.message("perc.ui.category.view@User Admin"), I18N.message("perc.ui.category.view@User Admin Edit"));
                    return;
                }
                
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                var node = container.dynatree("getActiveNode");
                displayCategoryDetails(node);
                showSelectedCategoryEditor(node);
                    
            });
            
            $("#perc-categories-moveup-button").unbind().click(function(){
                
                if (editing)
                {
                        currentlyEditing();
                        return;
                }


                var node = container.dynatree("getActiveNode");

                var targetNode = findUpTargetNode(node);
                if(targetNode != null)
                    moveNodeUp(node, targetNode);
                
                displayCategoryDetails(container.dynatree("getActiveNode"));
            });
            
            $("#perc-categories-movedown-button").unbind().click(function(){
                
                if (editing)
                {
                        currentlyEditing();
                        return;
                }


                var node = container.dynatree("getActiveNode");
                var targetNode = findDownTargetNode(node);
                
                if(targetNode != null)
                    moveNodeDown(node, targetNode);
                
                displayCategoryDetails(container.dynatree("getActiveNode"));
            });
            
            //Bind Save event
            $("#perc-category-save").unbind().click(function(){
                var node = container.dynatree("getActiveNode");
                if (node.data.title === "New Category")
                {
                    alertDialog("Error", "You must change the category name.");
                    return;
                }
                save();
            });
            //Bind Cancel event
            $("#perc-category-cancel").unbind().click(function(){
                //controller.cancel();
                var node = container.dynatree("getActiveNode");
                editing = false;
                if (!node.data.saved)
                {
                    parent = node.parent;
                    parent.activate();
                    if (!node.parent.childList.length == 0)
                        node.remove();
                    
                    node = parent;
                }
                else
                {
                    node.data.title = originalTitle;
                    node.render();
                
                }
                displayCategoryDetails(node);
                try {
                    node.childList[0].activate()
                }catch(err) {}
            });
            
            $("#perc-categories-publish-staging").unbind().click(function(){
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                var node = container.dynatree("getActiveNode");
                publishToDTS(node, "Staging");
            });
            
            $("#perc-categories-publish-production").unbind().click(function(){
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                var node = container.dynatree("getActiveNode");
                publishToDTS(node, "Production");
            });
            
            $("#perc-categories-publish-both").unbind().click(function(){
                if (editing)
                {
                        currentlyEditing();
                        return;
                }

                var node = container.dynatree("getActiveNode");
                publishToDTS(node, "Both");
            });
            
        }
        
        function getCategories(categoryJson) {

            var treedata = categoryJson;
            
            var categorytree = treedata.topLevelNodes;
            
            if (categorytree == null || typeof categorytree == "undefined" || categorytree.length == 0)
            {
                categorytree = [ // Pass an array of nodes.
                {
                                    id : generateUid(),
                                    title : "New Category",
                                    selectable : true,
                                    showInPgMetaData : true,
                                    createdBy : "system",
                                    creationDate : getCurrentDate(),
                                    deleted : false,
                                    activate: true,
                                    saved: false,
                                    initialViewCollapsed : true
                                }
                ];
            }
            container.dynatree({
                selectMode: 3,
                autoCollapse: true,
                children: categorytree,
                onPostInit: function(isReloading, isError) {
                    visitTreeForBaseProperties();
                    this.activateKey("_2");
                },
                onQueryActivate: function(flag,node) {
    
                    if (editing)
                    {
                            currentlyEditing();
                            return false;
                    }
                
                },
                onActivate: function(node) {
                    displayCategoryDetails(node);
                },
                dnd: {
                    preventVoidMoves: true, // Prevent dropping nodes 'before self', etc.
                    onDragStart: function(node) {
                      return true;
                    },
                    onDragEnter: function(node, sourceNode) {
                      // Prevent dropping a parent below another parent (only sort
                      // nodes under the same parent)
                      if(node.parent !== sourceNode.parent){
                        return false;
                      }
                      // Don't allow dropping *over* a node (would create a child)
                      return ["before", "after"];
                    },
                    onDrop: function(node, sourceNode, hitMode, ui, draggable) {
                      /** This function MUST be defined to enable dropping of items on
                       *  the tree.
                       */
                      sourceNode.move(node, hitMode);
                      isMoved = true;
                      save();
                    }
                  }
            });
            
        }
        
        function visitTreeForBaseProperties() {

            var treeRoot = container.dynatree("getRoot");
            
            treeRoot.visit(function(node){
                node.data.saved=true;
                if(node.data.initialViewCollapsed === "false") {
                    node.expand(true);
                }
            });
        }
        
        function displayCategoryDetails(node) {
            if (node == null)
                return;
            originalTitle = node.data.title;
            $("#perc-category-save-cancel-block").hide();
            
            $("#perc-category-name-field").prop("disabled", true);
            $("#perc-category-name-field").addClass("perc-category-field-readonly");
       
            $("#perc-category-name-field").val(node.data.title);
            
            $("#perc-category-selectable-field").prop("disabled", true);
            $("#perc-category-selectable-field").addClass("perc-category-field-readonly");
            var selectable = node.data.selectable;
            if(selectable == true || selectable === "true") {
                $("#perc-category-selectable-field").prop("checked", true);
            }
            else {
                $("#perc-category-selectable-field").prop("checked", false);
            }
            
            $("#perc-category-show-in-page-field").prop("disabled", true);
            $("#perc-category-show-in-page-field").addClass("perc-category-field-readonly");
            var sinpmd = node.data.showInPgMetaData;
            if(sinpmd === "true" || sinpmd == true) {
                $("#perc-category-show-in-page-field").prop("checked", true);
            }
            else {
                $("#perc-category-show-in-page-field").prop("checked", false);
            }
     
            $("#perc-allowedsites-field").addClass("perc-category-field-readonly");
            $("#perc-allowedsites-field").prop("disabled", true);
      
            addSitesToDropdown("#perc-allowedsites-field",getAllowedSites(node),getSelectedSites(node));
            

            $("#perc-category-createdby-field").val(node.data.createdBy);
            $("#perc-category-creationdt-field").val(node.data.creationDate);
            $("#perc-category-lstmodifiedby-field").val(node.data.lastModifiedBy);
            $("#perc-category-lstmodifieddt-field").val(node.data.lastModifiedDate);
        }
        
        function showSelectedCategoryEditor(node) {
            editing = true;
            originalTitle = node.data.title;

            $("#perc-category-name-field").prop("disabled", false);

            $("#perc-category-name-field").on('keyup', function() {
                 var node = container.dynatree("getActiveNode");
                 var text =  $( this ).val();
                 if (text=="") text="[empty]";
                 node.data.title = $( this ).val();
                 node.render();
            });

            $("#perc-category-name-field").removeClass("perc-category-field-readonly");
            
            $("#perc-allowedsites-field").removeClass("perc-category-field-readonly");
            $("#perc-allowedsites-field").prop("disabled", false);

            $("#perc-category-selectable-field").prop("disabled", false);
            $("#perc-category-selectable-field").removeClass("perc-category-field-readonly");
            
            $('#perc-category-selectable-field option[value="'+sitename+'"]').prop('disabled', true);
    
            $("#perc-category-selectable-field option").on('click',function() {
                $('#perc-category-selectable-field option[value="'+sitename+'"]').prop('selected',true);
            });

            $("#perc-category-show-in-page-field").prop("disabled", false);
            $("#perc-category-show-in-page-field").removeClass("perc-category-field-readonly");
        
            $("#perc-category-save-cancel-block").show();
            $("#perc-category-name-field").focus();
        }
        
       
        
        function getCurrentDate() {
            
            var d = new Date();

            var month = d.getMonth()+1;
            var day = d.getDate();

            var output = d.getFullYear() + '-' +
                (month<10 ? '0' : '') + month + '-' +
                (day<10 ? '0' : '') + day + 'T' + 
                d.getHours() + ':' + 
                d.getMinutes() + ':' + 
                d.getSeconds() + '.' + d.getMilliseconds();
            
            return output;
        }
        
        function currentlyEditing() {
            var parentNode;
            
                w = 400;

                $.perc_utils.alert_dialog({
                    title: I18N.message("perc.ui.category.view@Editing Category"),
                    content: I18N.message("perc.ui.category.view@Editing Category Dialog"),
                    width: w,
                    okCallBack: function()
                    {
                        
                    }
                });
        }
        
         function confirmDialog(title, message, w) {
           
            $.perc_utils.confirm_dialog({
                title: title,
                question: message,
                success: function()
                {
                    if(isDelete) {
                        
                        handleDelete();
                    }
                    
                    controller.getCategories();
                },
                cancel: function () 
                {
                    
                }
            });
        }

        function alertDialog(title, message, w) {
            var parentNode;
            
            if(w == null || w == undefined || w == "" || w < 1)
                w = 400;
            $.perc_utils.alert_dialog({
                title: title,
                content: message,
                width: w,
                okCallBack: function()
                {
                    if(isDelete) {
                        
                        handleDelete();
                    }
                    
                    controller.getCategories();
                }
            });
        }

        function errorDialog(title, message, w, useCallback) {
            var parentNode;
            
            if(w == null || w == undefined || w == "" || w < 1)
                w = 400;
            $.perc_utils.alert_dialog({
                title: title,
                content: message,
                width: w,
                okCallBack: function()
                {
                }
            });
        }

        function handleDelete() {

            isDelete = false;
            var node = container.dynatree("getActiveNode");
            parentNode = node.getParent();
            var upTarget = findUpTargetNode(node);
                

            if(node.hasChildren()) {
                node.visit(function(node){
                    node.data.deleted = true;
                });
            } 
            node.data.deleted = true;
            node.data.lastModifiedBy = $.PercNavigationManager.getUserName();
            node.data.lastModifiedDate = getCurrentDate();

            updateCategoryXML();
            
            node.remove();
            
            var switchtoNode = null;
            if(upTarget != null)
                switchtoNode = upTarget
            else if (parentNode!=null)
            {
                switchtoNode = parentNode;
            }
            container.dynatree("getTree").activateKey(switchtoNode.data.key);
            displayCategoryDetails(switchtoNode);
            controller.getCategories();
        }
        
        function newNode(child)
        {
            
            var root = container.dynatree("getRoot");
            
            var destinationNode = null;
            var children =  root.childList;
            if ( children == null || typeof children == "undefined" || children.length==0)
            {
                destinationNode = root;
                child=true;
            } else if (children.length==1 && children[0].data.title == "New Category")
            {
                return children[0];
            } else {
                var destinationNode = container.dynatree("getActiveNode");
                if (destinationNode == null || typeof destinationNode == "undefined" || !destinationNode.hasOwnProperty('parent'))
                {
                    destinationNode = root;
                    child=true;
                } 
            }

            if (child==true)
            {
                addTo = destinationNode;
            } 
            else
                addTo = destinationNode.getParent();
                
            var child = addTo.addChild({
                                    id : generateUid(),
                                    title : "New Category",
                                    selectable : true,
                                    showInPgMetaData : true,
                                    createdBy : $.PercNavigationManager.getUserName(),
                                    creationDate : getCurrentDate(),
                                    deleted : false,
                                    activate: true,
                                    saved: false,
                                    initialViewCollapsed : true
                                });

                child.visitParents(function (childnode) {
                    childnode.expand(true);
                }, true); 

                return child;
    
        }

        function editCategories(node) {
            
            var nodeKey = node.data.key;
            var childNode;
            
            if ($('#perc-allowedsites-field option:not(:checked)').length == 0)
            {
                allowedSites=null;
            } else {
                allowedSites = $("#perc-allowedsites-field option:selected").map(function () {
                    return $(this).text();
                }).get().join(',');
            }
            
        
            var categoryname  = $.trim($("#perc-category-name-field").val());
           
            if (originalTitle !== categoryname)
            	node.data.previousCategoryName = originalTitle;
            
            node.data.lastModifiedBy = $.PercNavigationManager.getUserName();
            node.data.lastModifiedDate = getCurrentDate();

            if(isPublished) {
                node.data.publishDate = node.data.lastModifiedDate;
                isPublished = false;
            }
            

            node.data.title = categoryname;

            var selectable = $("#perc-category-selectable-field").prop("checked");

            if(selectable === true) {
                node.data.selectable = "true";
            } else {
                node.data.selectable = "false";
            }
            var showInPage = $("#perc-category-show-in-page-field").prop("checked");

            if(showInPage === true) {
                node.data.showInPgMetaData = "true";
            } else {
                node.data.showInPgMetaData = "false";
            }

            if(node.data.createdBy == null) {
                node.data.createdBy = $.PercNavigationManager.getUserName();
            }

            if(node.data.creationDate == null) {
                node.data.creationDate = getCurrentDate();
            }

            node.data.allowedSites = allowedSites;
            //  Add site save   
      
            return node;
        }
        
        
        function deleteCategory() {
            
            var node = container.dynatree("getActiveNode");
            
            if(node.hasChildren() === false) {
                confirmDialog(I18N.message("perc.ui.category.view@Delete Category"), I18N.message("perc.ui.category.view@Are You Sure"));
            } else {
                confirmDialog(I18N.message("perc.ui.category.view@Delete Category"), I18N.message("perc.ui.category.view@Category And Children Deleted"));
            }
        }
        
        function manageDynaProps() {
            
            var treeRoot = container.dynatree("getRoot");
            var children = [];
            treeRoot.visit(function(node){
                var parent = node.getParent();

                if(parent.data.title == null) {
                    children.push(node.toDict(true, function(dict) {
                        delete dict.activate;
                        delete dict.addClass;
                        delete dict.expand;
                        delete dict.focus;
                        delete dict.hideCheckbox;
                        delete dict.icon;
                        delete dict.isFolder;
                        delete dict.isLazy;
                        delete dict.key;
                        delete dict.noLink;
                        delete dict.select;
                        delete dict.tooltip;
                        delete dict.saved;
                        delete dict.unselectable;
                    }));
                }
            });
            
            return children;
        }
        
        function getUpdatedCategoryArray(tempChildList) {
            var children = [];
            
            for(i = 0; i < tempChildList.length; i++) {
                children.push(tempChildList[i].data);
            }
            
            return children;
        }
        
        function updateCategoryXML() {
            var catArray = manageDynaProps();
            controller.editCategories(catArray, sitename,
            function(){
                var node = container.dynatree("getActiveNode");
                displayCategoryDetails(node);
                node.data.saved = true;
                editing = false;
            },
            function(){
           
            }
            );
        }
        
        function save() {
        
            var node = container.dynatree("getActiveNode");
            
            
            if(!isMoved) {
                var thisnode = editCategories(node);
                node = thisnode;
            }
            
            
            isMoved = false;
            updateCategoryXML();
        }
        
        function findUpTargetNode(sourceNode) {
            
            var parentNode = sourceNode.getParent();
            var tempNode = null;
            var targetNode = null;
            var i = 0;
            // if the souceNode is a top level parent node, 
            // traverse the tree for only top level parent nodes.
            if(parentNode.data.title == null) {
                var treeRoot = container.dynatree("getRoot");
                
                treeRoot.visit(function(node){
                    var parent = node.getParent();
                    
                    if(parent.data.title == null) {
                        i++;
                        if(sourceNode.data.id != node.data.id)
                            tempNode = node;
                        else {
                            if(i > 1) {
                                targetNode = tempNode;
                                return false;
                            }
                            return false;
                        }
                    }
                });
                
                return targetNode;
            } else {
                parentNode.visit(function(node) {
                    i++;
                    var p = node.getParent();
                    if(p.data.id == parentNode.data.id) {
                        if(sourceNode.data.id != node.data.id)
                            tempNode = node;
                        else {
                            if(i > 1) {
                                targetNode = tempNode;
                                return false;
                            }
                            return false;
                        }
                        
                    }
                }); 
                
                return targetNode;
            }
        }
        
        function moveNodeUp(node, targetNode) {
            node.move(targetNode, "before");
            
            isMoved = true;
            save();
        }
        
        function findDownTargetNode(sourceNode) {
            
            var parentNode = sourceNode.getParent();
            var tempNode = null;
            var targetNode = null;
            var i = 0;
            // if the souceNode is a top level parent node, 
            // traverse the tree for only top level parent nodes.
            if(parentNode.data.title == null) {
                var treeRoot = container.dynatree("getRoot");
                
                treeRoot.visit(function(node){
                    var parent = node.getParent();
                    
                    if(parent.data.title == null) {
                        if(sourceNode.data.id != node.data.id) {
                            if(i > 0) {
                                targetNode = node;
                                return false;
                            }
                        }
                        else {
                            i++;

                        }
                    }
                });
                
                return targetNode;
            } else {
                parentNode.visit(function(node) {
                    var p = node.getParent();
                    if(p.data.id == parentNode.data.id) {
                        if(sourceNode.data.id != node.data.id) {
                            if(i > 0) {
                                targetNode = node;
                                return false;
                            }
                        }
                        else {
                            i++;

                        }
                    }
                }); 
                
                return targetNode;
            }
        }
        
        function moveNodeDown(node, targetNode) {
            node.move(targetNode, "after");
            
            isMoved = true;
            save();
        }
        
        function publishToDTS(node, deliveryServer) {
            var catArray = manageDynaProps();
            if (sitename == null || typeof sitename == "undefined" || sitename=="")
            {
                alertDialog(I18N.message("perc.ui.category.view@Select A Site"), I18N.message("perc.ui.category.view@Select A Site Content"));
                return;
            }
            controller.publishToDTS(catArray, deliveryServer, sitename);
            
            isPublished = true;
            save();
        }
    };
})(jQuery);
