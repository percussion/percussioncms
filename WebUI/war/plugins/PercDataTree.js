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
 * PercDataTree.js
 *
 * Generate the HTML and handle the events for a tree of sites's (and assets) folders
 * that have a workflow assigned.
 * 
 * TODO:
 * - Expose ASSETS_TITLE_KEY in some way.
 * - Make the principal roots Sites/Assets optional.
 * - Adapt with the new JSON object that the server returns.
 */
(function($) {
    // Exposed API/interface
    $.PercDataTree = {
        init                : init,
        updateTree          : updateTree,
        enableAddButton     : enableAddButton,
        disableAddButton    : disableAddButton,
        disableButtons      : disableButtons,
        enableButtons       : enableButtons
    };

    var defaultConfig = {
        instanceIdSuffix    : undefined,        // We have to be sure that the instance has a unique ID
        listItem            : [],
        addTitle            : "Add",
        enableAdd           : true,
        collapsible         : false,            // Make the PercDataTree collapsible?
        levelLimit          : undefined,        // Number of visible levels under the main roots Sites and Assets
        createItem          : function(){},     // Function invoked when clicking the add button.
        showCheckboxes      : false,
        addHeader           : true,
        showRoots           : true,
        cookieId            : "dynatree-checkbox",
        idPrefix            : "dynatree-checkbox-",
        customOnRender      : function(){},
        customOnExpand      : function(){},
		keyboard            : true
     };

    // Some CLASSES to reuse in selectors
    var LABEL_HEAD_CLASS                = 'perc-datatree-label';
    var TREELIST_CONTAINER_CLASS        = 'perc-itemname-treelist';
    var COLLAPSE_BTN_CLASS              = 'perc-wf-minmax';
    var ADD_BTN_CLASS                   = 'perc-item-add-button';
    var MINIMIZER_CLASS                 = 'perc-items-minimizer';
    var MAXIMIZER_CLASS                 = 'perc-items-maximizer';
    var DISABLED_ITEM_CLASS             = 'perc-item-disabled';
    var CHILDREN_OTHER_WORKFLOW_CLASS   = 'perc-children-other-workflow';
    var ALL_SAME_WORKFLOW_CLASS         = 'perc-item-all-same-workflow';
    var DISABLED_ITEM_CLASS             = 'perc-item-disabled';
    // Some ID important for the plugin
    var DATATREE_ELEM_ID                = 'datatree';
    // Other reusable constants
    var SITES_TITLE_KEY                 = 'Sites';
    var ASSETS_TITLE_KEY                = 'Assets';

    // Values needed when identing labels inside the tree
    var DYNATREE_UL_LI_PADDING          = 13;
    var DYNATREE_UL_LI_PADDING_OFFSET   = 5;
    
    var selectedWorkflow = "";

    /**
     * Initialize the plugin
     * @param container
     * @param config
     */
    function init(container, config)
    {
        // Extend options and assign as data in the container the options extended
        var options = $.extend(true, {}, defaultConfig, config);
        // Check that the instanceID property was defined
        if (typeof options.instanceIdSuffix === 'undefined')
        {
            throw "instanceIdSuffix must be defined.";
        }
        options.instanceId = DATATREE_ELEM_ID + options.instanceIdSuffix;
        container.data('options', options);
        
        selectedWorkflow = options.selectedWorkflow;
        
        if (options.addHeader)
        {
             // Create the head element that holds the collapse button, title and buttons
            var head = $('<div class="' + LABEL_HEAD_CLASS + '" />').html(options.title);//.css('color', '#0099CC');
            container.append(head);
            // Enable/disable collapse and add buttons (the sentence after '&&' is evaluated only if the option is truthy)
            options.collapsible  && head.append($('<span role="button" tabindex="0" style="float: left;" class ="' + COLLAPSE_BTN_CLASS + ' ' + MINIMIZER_CLASS + '" />').attr('title', I18N.message("perc.ui.workflow.view@Minimize")));
            options.enableAdd    && head.append($('<div role="button" tabindex="0" class="' + ADD_BTN_CLASS + '"/>').attr('title', options.addTitle));

            // Bind collapse and add button evetns
            container.find('.' + COLLAPSE_BTN_CLASS).off().on("click",function() {
                $(this).toggleClass(MINIMIZER_CLASS).toggleClass(MAXIMIZER_CLASS);

				var combinedClass = $(this).attr('class');
				var minmaxClass = combinedClass.split(" ");
				if(minmaxClass[1] == MINIMIZER_CLASS){
					$(this).attr('title',I18N.message("perc.ui.workflow.view@Minimize"));
				}else if(minmaxClass[1] == MAXIMIZER_CLASS){
					$(this).attr('title',I18N.message("perc.ui.workflow.view@Maximize"));
				}

                container.find('.' + TREELIST_CONTAINER_CLASS).slideToggle("fast");
            });
			$('.' + COLLAPSE_BTN_CLASS).off("keydown").on("keydown",function(event) {
				if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
				}
            });

            container.find('.' + ADD_BTN_CLASS).off().on("click",function() {
                var options = container.data('options');
                if (typeof(options.createItem) == 'function')
                {                
                    options.createItem();
                }    
            });


			///
			$("."+ADD_BTN_CLASS).off("keydown").on("keydown",function(event) {
				if(event.code == "Enter" || event.code == "Space"){
						document.activeElement.click();
				}
            });
			///
        }

        // Create the tree/list container after the head
        container.append($('<div id="' + options.instanceId + '" class="' + TREELIST_CONTAINER_CLASS + '" />'));

        if (options.showRoots)
        {
            /// Main roots of the tree
            var sitesRoot = {
                title        : SITES_TITLE_KEY,
                key          : SITES_TITLE_KEY,
                isFolder     : true,
                isLazy       : false,
                expand       : true,
                children     : [],
                icon         : false
            };
            var assetsRoot = {
                title        : ASSETS_TITLE_KEY,
                key          : ASSETS_TITLE_KEY,
                isFolder     : true,
                isLazy       : false,
                expand       : true,
                children     : [],
                icon         : false
            };
            
            var mainRoots = [sitesRoot, assetsRoot];
        }
        
        var tree_width = $('#' + options.instanceId).width();
        // Apply the dynatree plugin
        $('#' + options.instanceId).dynatree({
            children        : (options.showRoots ? mainRoots : []),
            noLink          : true,
            clickFolderMode : 3,
            checkbox        : options.showCheckboxes,
            cookieId        : "dynatree-" + options.instanceId,
            idPrefix        : "dynatree-" + options.instanceId + "-",
            onRender        : function(dtnode, nodeSpan) {
                // Correct identation of elements that does not have expander
                span = $(nodeSpan);
                var uls  = span.parents("ul").length - 1;
                span.attr("for",dtnode.data.title).css("padding-left", uls * DYNATREE_UL_LI_PADDING + DYNATREE_UL_LI_PADDING_OFFSET).find('.dynatree-title').addClass("perc-ellipsis");
                var titleSpan = span.find('.dynatree-title');
				var checkSpan = span.find('.dynatree-checkbox');
				if(typeof titleSpan !== 'undefined'){

				}
				if(typeof checkSpan !== 'undefined'){
					checkSpan.attr("title",dtnode.data.title);
					checkSpan.attr("role","button");
					checkSpan.attr("tabindex","0");
					checkSpan.on("keydown",function(eventHandler){
					if(eventHandler.code == "Space"){
						document.activeElement.click();
						}
					});
				}

				// If the element has no children, correct the align
                if (! $(span[0]).hasClass('dynatree-has-children')) {
                    span.find('.dynatree-title').css('padding-left', '13px');
                }
            },
            onExpand        : function(flag, dtnode) {
                options.customOnExpand(dtnode);
            },
            onSelect        : options.onSelect,
            onQuerySelect   : options.onQuerySelect
        });

        // Once the tree is ready, update its contents with an empty collection,
        if (typeof options.listItem !== 'undefined' && typeof(options.listItem) === 'object')
        {
            updateTree(container, options.listItem);
        }
    }
    
    /**
     * Generate and update the HTML list.
     * @param container HTML element created using this plugin
     * @param itemList [sitesNodes, assetsNodes]
     */
    function updateTree(container, itemList, workflowName)
    {
        // Get the current options (we set them in the init() method ) from the container
        options = container.data('options');
        if (typeof workflowName !== 'undefined')
        {
            selectedWorkflow = workflowName;
        }   

        // CLEAR THE CHILDREN of Sites and Assets subtrees
        var tree = $(container).find('#' + options.instanceId).dynatree('getTree');
        var level = 1;
        if (options.showRoots)
        {
            // CLEAR THE CHILDREN of Sites and Assets subtrees
            var sitesRoot = tree.getNodeByKey(SITES_TITLE_KEY);
            var assetsRoot = tree.getNodeByKey(ASSETS_TITLE_KEY);
            assetsRoot.removeChildren();
            sitesRoot.removeChildren();
            
            // Populate sitesRoot if the corresponding 'folders' property is defined (same for 
            // assetsRoot) and redraw the tree
            
            itemList[0] && itemList[0].folderItem && addSubtreeContents(sitesRoot, itemList[0].folderItem, options.levelLimit,level);
            itemList[1] && itemList[1].folderItem && addSubtreeContents(assetsRoot, itemList[1].folderItem, options.levelLimit, level);
        }
        else
        {
            var rootNode = tree.getRoot();
            rootNode.removeChildren();
            
            // Populate sitesRoot if the corresponding 'folders' property is defined (same for 
            // assetsRoot) and redraw the tree
            itemList[0] && itemList[0].folderItem && addSubtreeContents(rootNode, itemList[0].folderItem, options.levelLimit,level);
        }
        tree.visit(function(dtnode){
            options.customOnRender(dtnode);
        }, false);
    }

        /**
         * Function that recursively constructs the tree data structure needed by dynatree plugin.
         * @param dynetree_node DynaTreeNode (check: http://wwwendt.de/tech/dynatree/doc/dynatree-doc.html)
         * @param dataSubtree [] object that holds the workflow assignment for a specific node.
         */
        function addSubtreeContents(dynatree_node, dataSubtrees, levelLimit, level)
        {
            if (typeof levelLimit !== 'undefined' && level > levelLimit)
            {
                return;
            }

            // We must convert dataSubtrees to an array using a utility method (CXF problem)
            dataSubtrees = $.perc_utils.convertCXFArray(dataSubtrees);
            // dataSubtree's length must be greater than 0
            if (dataSubtrees && dataSubtrees instanceof Array && dataSubtrees.length > 0)
            {
                $.each(dataSubtrees, function(i) {
                    // For each element in dataSubtrees, add a child in dyn_children_array and...
                    var child_template = {
                        title     : this.name,
                        key       : this.id,
                        tooltip   : this.path,
                        isFolder  : true,
                        isLazy    : false,
                        children  : [],
                        addClass  : '',
                        icon      : false
                    };

                    // If the dataSubtree's workflow is not equal to the selected one, disable the checkbox,
                    // append the workflow between parenthesis and add a special class to it
                    var has_workflow = typeof(this.workflowName) != 'undefined';
                    if (options.showCheckboxes && has_workflow && this.workflowName !== selectedWorkflow)
                    {
                        child_template.unselectable = true;
                        child_template.title = child_template.title + ' (' + this.workflowName + ')';
                        child_template.addClass = child_template.addClass + ' ' + DISABLED_ITEM_CLASS;
                    }

                    // If mark the checkbox only if the node is assigned to the current workflow
                    if (options.showCheckboxes && has_workflow && this.workflowName === selectedWorkflow)
                    {
                        child_template.select = true;
                    }

                    // If the node has at least one descendant that belongs to other class, assing a special class
                    if (! this.allChildrenAssociatedWithWorkflow || this.workflowName !== selectedWorkflow)
                    {
                        child_template.addClass = child_template.addClass + ' ' + CHILDREN_OTHER_WORKFLOW_CLASS;
                    }
                    else
                    {
                        // If the node has all of its descendant assigned to the selected workflow and the
                        // current node is checked, add a special class (bold)
                        if (options.showCheckboxes && child_template.select === true)
                        {
                            child_template.addClass = child_template.addClass + ' ' + ALL_SAME_WORKFLOW_CLASS;
                        }
                    }

                    // Add some useful data to the node
                    child_template.workflowName = this.workflowName;
                    child_template.allChildrenAssociatedWithWorkflow = this.allChildrenAssociatedWithWorkflow;
                    // Create a node using the template
                    dynatree_node.addChild(child_template);

                    // Call recursively addSubtreeContents with dynatree_node.chldren
                    // we must also convert the 'children' property to an array (CXF problem)

                    if(typeof this.children !== 'undefined') {
                        this.children !== "" && addSubtreeContents(dynatree_node.getChildren()[i], this.children.child, levelLimit, level + 1);
                    }
                });

            }
        }
    
    /**
     * Enables the add button in the head.
     * @param container HTML element created using this plugin
     */
    function enableAddButton(container)
    {
        var options = container.data('options');
        if (options.enableAdd)
        {
            container.find('.' + ADD_BTN_CLASS)
                .removeClass('.' + DISABLED_ITEM_CLASS)
                .off()
                .on("click",function() {
                    if (typeof(options.createItem) == 'function')
                    {
                        options.createItem();
                    }
                })
				.on("keydown",function(eventHandler) {
                    if(eventHandler.code == "Enter" || eventHandler.code == "Space"){
						document.activeElement.click();
					}
                });
        }
    }
    
    /**
     * Disables the add button in the head.
     * @param container HTML element created using this plugin
     */
    function disableAddButton(container)
    {
        var options = container.data('options');
        if (options.enableAdd)
        {
            container.find('.' + ADD_BTN_CLASS)
                .addClass('.' + DISABLED_ITEM_CLASS)
                .off();
        }
    }

    /**
     * Enables all buttons in the head (except collapse).
     * @param container HTML element created using this plugin
     */
    function enableButtons(container)
    {
        enableAddButton(container);
    }

    /**
     * Disables all buttons in the head (except collapse).
     * @param container HTML element created using this plugin
     */
    function disableButtons(container)
    {
        disableAddButton(container);
    }
    
})(jQuery);
