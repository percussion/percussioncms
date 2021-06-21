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
 * perc_site_map Widget
 *
 * This widget will create/edit a navigation site map that represents a particular
 * site.
 */
(function($){

    $.widget("ui.perc_site_map",
        {

            /* Constants */
            NODEWIDTH: 92,
            NODESPACING: 40,
            MINLEFT: 116,
            TOPSPACING: 14,
            MAX_SECTION_TITLE_LENGTH: 18,
            IMAGE_BASE: "../images/images/",
            LINE_IMAGE: "singlePixelGreen.gif",
            ADD_BUTTON_IMAGE: "buttonAddPage2.gif",
            /* These 2 buttons are designed to go together, config on the left and
               delete on the right. */
            CONFIG_BUTTON_IMAGE: "buttonConfigurePage.gif",
            DELETE_BUTTON_IMAGE: "buttonDeletePage.gif",
            /* This image is identical to CONFIG_BUTTON_IMAGE, except it is designed to
               be by itself. */
            CONFIG_BUTTON_IMAGE_STANDALONE: "buttonConfigStandalone.png",
            ARROW_ON_IMAGE: "arrowSubOn.gif",
            ARROW_OFF_IMAGE: "arrowSubOff.gif",

            /* Globals */
            data:  [],
            rootSection: null,
            newSectionDialog: null,
            editSectionDialog: null,
            editSiteSectionDialog: null,


            /**
             * Initializes the widget by adding and expanding the root section
             */
            _init: function(){
                var self = this;
                this.element.addClass("perc-site-map");
                this.element.append($("<div></div>").css("height", "8px"));
                this.element.append($("<div id='perc_store_draggable'></div>").hide());
                this.load(this.options.site);
                this.newSectionDialog = $.perc_newSectionDialog();
                this.editSectionDialog = $.perc_editSectionDialog();
                this.editSiteSectionDialog = $.perc_editSiteSectionDialog();
                this.addActionsMenuItems();
                // Handle window resize by doing a full layout of all levels
                $(window).on("resize",function(){
                    self.layoutAll();
                });
            },
            /**
             * Load site map for specified site.
             * @param site the sitename, may be <code>null</code> or empty
             * in which case nothing will show.
             * @param noexpand {boolean} flag that if <code>true</code> indicates that we
             * should not auto expand the root section upon load.
             * @param postCallback {function} callback function called after
             * load completes. No arguments are passed to the callback.
             */
            load: function(site, noexpand, postCallback)
            {
                var self = this;
                this.removeLevel(1);
                if(site)
                {
                    $.Perc_SectionServiceClient.getRootSection(site, function(status, data){
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            var section = data.SiteSection;
                            var $level1 = self.addLevel("");
                            var $rootSection = self.createSection(section, 1, "", 0);
                            self.addSectionToLevel($rootSection, 1);
                            self.layoutLevel(1);
                            if(!noexpand)
                                self.expandSection(section.id);
                            if(typeof(postCallback) === 'function')
                                postCallback();
                        }
                        else
                        {
                            var msg = data;
                            if(data.indexOf("Cannot find object name") != -1)
                            {
                                msg = I18N.message("perc.ui.site.map@Requested Site") + site
                                    + I18N.message("perc.ui.site.map@Site Does Not Exist");
                            }
                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: msg,
                                okCallBack: function(){
                                    $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_SITE_ARCH);
                                }});
                        }
                    });
                    // Handle window resize by doing a full layout of all levels
                    $(window).on("resize",function(){
                        self.layoutAll();
                    });
                }
            },

            /**
             * Handle things when the widget is destroyed.
             */
            destroy: function(){
                $.widget.prototype.destroy.apply(this, arguments);
                $(window).off("resize");
            },

            /**
             * Handle the Move Section Action item enabled/disabled state..
             */
            handleMoveActionItemState: function(){
                var selected = $(".perc-site-map-box-selected").length > 0;
                if(selected)
                {
                    $(".perc-site-map-action-item-disabled")
                        .not('#perc-site-map-copy,#perc-site-map-delete')
                        .removeClass("perc-site-map-action-item-disabled")
                        .addClass("perc-site-map-action-item-enabled");
                }
                else
                {
                    $(".perc-site-map-action-item")
                        .not('#perc-site-map-copy,#perc-site-map-delete')
                        .removeClass("perc-site-map-action-item-enabled")
                        .addClass("perc-site-map-action-item-disabled");
                }

            },

            /**
             * Fire the onChange callback function
             */
            fireOnChange: function(){
                if(typeof(this.options.onChange) == 'function')
                {
                    this.options.onChange();
                }
            },

            /**
             * Layout a existing levels.
             */
            layoutAll: function(){
                var $level = $(".perc-site-map-level");
                var len = $level.length;
                for(var i = 1; i <= len; i++)
                    this.layoutLevel(i);
            },

            /**
             * Layout all the section boxes and lines for a specified level.
             * @param idx (Integer) the level index which is a 1 based integer.
             */
            layoutLevel: function(idx){
                var self = this;
                var topspacing = this.TOPSPACING * (idx == 1 ? 2 : 1);

                var $level = $(".perc-site-map-level").slice(idx - 1, idx);
                var levelParentId = $level.metadata({type: 'attr', name: 'data'}).parentId;
                var $nodes = $level.children(".perc-site-map-nodes").children(".perc-site-map-node");
                var len = $nodes.length;
                var requiredWidth = (len * this.NODEWIDTH) + ((len - 1) * this.NODESPACING);
                var levelTop = $level.offset().top;
                var leftOffset = this.MINLEFT;
                var centerOffset = ($(window).width() - requiredWidth) / 2;
                var offscreenParentOffset = -1;

                if(levelParentId.length > 0)
                {
                    // center under parent node
                    var parentOffset = $("#" + levelParentId).offset().left;
                    centerOffset = (parentOffset + (this.NODEWIDTH / 2)) - (requiredWidth / 2);

                    // Attempt to keep on screen calculation
                    if(centerOffset + requiredWidth > ($(window).width() + $(window).scrollLeft()))
                    {
                        var temp = (centerOffset + requiredWidth) - ($(window).width() + $(window).scrollLeft());
                        offscreenParentOffset = parentOffset;
                        centerOffset -= (temp + 20);
                    }

                }
                else
                {
                    // center in the browser window
                    centerOffset = ($(window).width() - requiredWidth) / 2;
                    // Position the site title
                    var $siteTitle = $(".perc-site-map-sitetitle");
                    var sTitleWidth = $siteTitle.width();
                    $siteTitle
                        .css("top", levelTop + 10)
                        .css("left", ($(window).width() - sTitleWidth) / 2);
                    // Position the action menu
                    var $actionMenu = $(".perc-site-map-actions");
                    var aMenuWidth = $actionMenu.width();
                    $actionMenu
                        .css("top", levelTop + 10)
                        .css("left", ($(window).width() - aMenuWidth) - 40 );
                }

                if(centerOffset > this.MINLEFT)
                {
                    leftOffset = centerOffset;
                }

                var startOffset = leftOffset;
                var endOffset = startOffset;
                //Remove existing drop areas in the level
                $level.children(".perc-site-map-nodes").children(".perc-site-map-droparea").remove();
                // Layout each node
                $nodes.each(function(i){
                    $(this).css("top", levelTop + topspacing);
                    $(this).css("left", leftOffset);
                    if(idx > 1)
                        self.addDropArea($(this), levelTop, leftOffset, i);
                    endOffset = leftOffset;
                    // Layout bottom line if expanded
                    if($(this).hasClass("perc-site-map-expanded"))
                    {
                        $bottomLine = $(this).children(".perc-site-map-node-bottomline");
                        $bottomLine.css("left", self.NODEWIDTH / 2);
                        $bottomLine.show();
                    }
                    leftOffset += (self.NODESPACING + self.NODEWIDTH);

                });

                // Layout level line
                var $levelLine = $level.children(".perc-site-map-level-line");
                if(len > 1 || (len == 1 && offscreenParentOffset > -1))
                {
                    var eOffset = Math.max(endOffset, offscreenParentOffset);
                    //if(offscreenParentOffset > -1 && len > 1)
                    // eOffset += 64;
                    $levelLine.css("top", levelTop + topspacing - 2);
                    $levelLine.css("left", startOffset + 46);
                    $levelLine.children("img").css("width", (eOffset - startOffset) + 2);
                    $levelLine.show();
                }
                else
                {
                    $levelLine.hide();
                }
                self.adjustLevelWidths();
                $nodes.each(function(){
                    $(this).show();
                });

            },
            /**
             * Adjusts level widths so that the section nodes "overflow" them.
             */
            adjustLevelWidths: function(){
                var rightBuffer = 40;
                var maxPos = 0;
                // Find largest left positional value for each level
                $(".perc-site-map-level").each(function(idx){
                    if(idx < 1)
                        return;
                    var $lastDropArea =
                        $(this).find(".perc-site-map-nodes .perc-site-map-droparea:last");
                    if($lastDropArea.length == 0)
                        return;
                    var left = $lastDropArea.offset().left;
                    maxPos = Math.max(maxPos, left);
                });
                // Reset all levels widths
                $(".perc-site-map-level").each(function(idx){
                    if(idx < 1)
                        return;
                    if((maxPos + rightBuffer) > $(window).width())
                    {
                        $(this).css("width", (maxPos + rightBuffer) + "px");
                    }
                    else
                    {
                        $(this).css("width", null);
                    }
                });

            },
            /**
             * Adds drop area(s) to the passed in section node.
             * @param $node {jQuery} the section node as a jQuery object. Cannot be <code>null</code>
             * or empty.
             * @param top {int} the top offset of the section node.
             * @param left {int} the left offset of the section node.
             * @param i {int} the position index of the section within the level.
             */
            addDropArea: function($node, top, left, i)
            {
                var data = $node.metadata({type: 'attr', name: 'data'});
                var info = {targetId: data.parentId, index: 0};
                //Before Drop area
                if(i == 0)
                {
                    var $bDroparea = $("<div class='perc-site-map-droparea'></div>");
                    $bDroparea.attr("data", JSON.stringify(info));
                    $node.before($bDroparea);
                    $bDroparea.css("top", top + 51);
                    $bDroparea.css("left", left - 38);
                    this.initDropArea($bDroparea);
                }
                info.index = i + 1;
                var $aDroparea = $("<div class='perc-site-map-droparea'</div>");
                $aDroparea.attr("data", JSON.stringify(info));
                $node.after($aDroparea);
                $aDroparea.css("top", top + 51);
                $aDroparea.css("left", left + 94);
                this.initDropArea($aDroparea);

            },
            /**
             *  Initialize a drop area making the div droppable and adding all needed
             *  events.
             *  @param $dropArea {jQuery} the drop area element as a jQuery object, cannot be
             *  <code>null</code>.
             *  @param targetId of the section that will be the target for the section drop.
             */
            initDropArea: function($dropArea, targetId){
                var self = this;
                $dropArea.droppable({
                    accept: function(droppable){return self.onDropAccept(this, droppable)},
                    tolerance: "pointer",
                    over: function(event, ui){
                        var $target = $(this);
                        $target.addClass("perc-site-map-droparea-over");

                    },
                    out: function(event, ui){
                        var $target = $(this);
                        $target.removeClass("perc-site-map-droparea-over");
                    },
                    drop: function(event, ui){
                        var $target = $(this);
                        $target.removeClass("perc-site-map-droparea-over");
                        var targetData = JSON.parse($dropArea.attr('data'));
                        var droppedData =  JSON.parse(ui.draggable.attr('data'));
                        self.onMove(
                            droppedData.id,
                            droppedData.parentId,
                            droppedData.index,
                            targetData.targetId,
                            targetData.index)
                    }
                });
            },
            /**
             * Helper function to build the image path.
             * @param imageName (String) the image name without the ext.
             * @param isOver (Boolean) flag indicating that this is a mouse over image.
             */
            getImageSrc: function(imageName, isOver)
            {
                var postfix = isOver ? "Over" : "";
                // assumes all images have extensions
                var extIndex = imageName.lastIndexOf('.');
                var base = imageName.substring(0, extIndex);
                var ext = imageName.substring(extIndex, imageName.length);
                return this.IMAGE_BASE + base + postfix + ext;
            },

            /**
             * Adds a section to the specified level.
             * @param $section (jQuery) the section to be added, assumed not <code>null</code>.
             * @param levelIdx (int) the level index.
             */
            addSectionToLevel: function($section, levelIdx){
                var $level =  $("#perc_level_" + levelIdx);
                var $nodes = $level.children(".perc-site-map-nodes");
                $nodes.append($section);
                // if level 1 then hide top line
                if(levelIdx == 1)
                    $section.children(".perc-site-map-node-topline").hide();
            },

            /**
             * Adds (appends) a new level to the view.
             * @param parentId (String) the parent id of the expanded level that owns
             * this level.
             */
            addLevel: function(parentId){
                var $levels = $(".perc-site-map-level");
                var levelCount = $levels.length;
                var isTopLevel = levelCount == 0;
                var title = isTopLevel
                    ? I18N.message('perc.ui.sitemap.label@Level 1 (Top)')
                    : I18N.message('perc.ui.sitemap.label@Level', [(levelCount + 1) + ""]);
                var $sitemap = $(".perc-site-map");
                var $level = $("<div></div>").addClass("perc-site-map-level")
                    .addClass(isTopLevel ? "perc-site-map-root" : "perc-site-map-sub")
                    .attr("id", "perc_level_" + (levelCount + 1))
                    .attr("data", "{level: '" + (levelCount + 1) + "', parentId: '" + parentId + "'}");
                $sitemap.append($level);

                $level.append($("<div></div>").addClass("perc-site-map-level-title").
                text(title));
                if(!isTopLevel)
                {
                    $level.append($("<div></div>").addClass("perc-site-map-level-line")
                        .append($("<img/>").attr("src", this.getImageSrc(this.LINE_IMAGE))
                            .attr("width", "2")
                            .attr("height", "2")
                            .attr("alt", "")));
                    var $imgButton =
                        this.createImageButton(this.ADD_BUTTON_IMAGE,
                            I18N.message('perc.ui.sitemap.tooltip@Add Section'),
                            this.onAddSection,
                            {"level": levelCount + 1, "parentSectionId": parentId, "context": this},
                            "level_" + levelCount + 1);
                    $imgButton.attr("id", "perc_level_" + (levelCount + 1) + "_add");
                    var $addSectionButton = $level.append($("<div></div>")
                        .addClass("perc-site-map-addpage-button").append($imgButton));
                }
                else
                {
                    $level.append($("<div></div>").addClass("perc-site-map-sitetitle").
                    text(this.options.site));
                    $level.append($("<div></div>").addClass("perc-site-map-actions"));
                    this.checkCopySite();
                }

                $level.append($("<div></div>").addClass("perc-site-map-nodes"));
                return $level;
            },

            /**
             * Removes the specified level and all of its sub levels from the view.
             * @param idx (int) the level index of the level to be removed.
             */
            removeLevel: function(idx){
                var self = this;
                var $levels = $(".perc-site-map-level");
                var levelCount = $levels.length;
                if(idx < 1 || idx > levelCount)
                    return;
                var $level = $($levels[idx - 1]);
                // remove any levels under this level first
                if(idx < levelCount)
                    this.removeLevel(idx + 1);
                // unbind all events from this level first
                $level.off(".level_" + idx);

                // now remove it from the dom
                $level.remove();

            },
            /**
             * Add the action menu items to the site map.
             */
            addActionsMenuItems: function(){
                var self = this;
                var $menu = $("#perc-navigation-menu");

                if ($.PercNavigationManager.isAdmin())
                {
                    // Add Site Delete action menu item (disable at first)
                    var $delete = $("<div/>").addClass("perc-site-map-action-item")
                        .addClass("perc-site-map-action-item-disabled")
                        .css("display", "table-cell")
                        .css("padding-right", "30px")
                        .attr("id", "perc-site-map-delete")
                        .attr("title", "Delete Site")
                        .text(I18N.message("perc.ui.site.map@Delete Site"));

                    isSiteBeingImported(function(result){
                        // enable and bind the event
                        //CMS-8082 : result was returning false as string. Need to convert to boolean to enable the delete site button.
                        if (!JSON.parse(result))
                        {
                            $delete.removeClass("perc-site-map-action-item-disabled")
                                .addClass("perc-site-map-action-item-enabled")
                                .attr("for", self.options.site)
                                .on("click", function(evt) {
                                    self.onDeleteSiteDialog();
                                });
                        }

                    })

                    $menu.append($delete);

                    // Add Copy action menu item
                    var $copy = $("<div/>").addClass("perc-site-map-action-item")
                        .addClass("perc-site-map-action-item-disabled")
                        .css("display", "table-cell")
                        .css("padding-right", "30px")
                        .attr("id", "perc-site-map-copy")
                        .text(I18N.message("perc.ui.site.map@Copy Site"))
                        .on("click", function(evt){
                            self.onCopySiteDialog();
                        });
                    $menu.append($copy);
                }


                // Add Move action menu item
                var $move = $("<div></div>").addClass("perc-site-map-action-item")
                    .addClass("perc-site-map-action-item-disabled")
                    .css("display", "table-cell")
                    .attr("id", "perc-site-map-move")
                    .text(I18N.message('perc.ui.sitemap.menuitem@Move Section'))
                    .on("click", function(evt){
                        self.onMoveWithDialog();
                    });
                $menu.append($move);

                /**
                 * Checks if the selected site is being imported.
                 */
                function isSiteBeingImported(callback)
                {
                    var sitename = $.PercNavigationManager.getSiteName();
                    if(sitename != "undefined")
                    {
                        $.PercSiteService.isSiteBeingImported(sitename, function (status, result)
                        {
                            callback(result);
                        });
                    }
                    else
                    {
                        callback(false);
                    }
                }
            },

            /**
             * Prompts the user with confirmation dialog .
             */

            onDeleteSiteDialog: function() {
                var ut = $.perc_utils;
                var mcol_path = ['','site'];
                var ut = $.perc_utils;
                var sitename = [$(".perc-site-map-sitetitle").text()];
                var confirmMessage =  I18N.message("perc.ui.deletesitedialog.warning@Confirm", sitename);
                dialog = $("<div/>")
                    .append(confirmMessage)
                    .perc_dialog({
                        id: "perc-finder-delete-confirm",
                        title: I18N.message( "perc.ui.deletesitedialog.warning@Title" ),
                        percButtons: {
                            "Ok": {
                                click: function() {
                                    dialog.remove();
                                    deleteSiteWithDirtyCheck();
                                },
                                id: "perc-confirm-generic-ok"
                            },
                            "Cancel": {
                                click: function() {
                                    dialog.remove();
                                },
                                id: "perc-confirm-generic-cancel"
                            }
                        },
                        width: "500px",
                        modal: true
                    });
                //});

            },

            /**
             * Prompts the user with a dialog the name of the new site.
             */
            onCopySiteDialog: function(){
                var sitename = $(".perc-site-map-sitetitle").text();

                if(sitename != "" && $('#perc-site-map-copy').not('.perc-site-map-action-item-disabled').length>0)
                {
                    $.PercSiteService.copySiteInfo(function(status, result){
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            if (!jQuery.isEmptyObject(result.psmap.entries)){

                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: I18N.message("perc.ui.site.map@Cannot Start A Copy Site")});
                            }
                            else{
                                $.PercCopySiteDialog.open(sitename);
                            }
                        }
                        else{
                            $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                        }
                    });
                }
            },

            /**
             * Check if exists a copy site process currently.
             */
            checkCopySite: function(){
                var self = this;
                $.PercSiteService.copySiteInfo(function(status, result){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        if (!jQuery.isEmptyObject(result.psmap.entries)){
                            $('#perc-site-map-copy').addClass("perc-site-map-action-item-disabled").removeClass("perc-site-map-action-item-enabled");
                        }
                        else{
                            $('#perc-site-map-copy').removeClass("perc-site-map-action-item-disabled").addClass("perc-site-map-action-item-enabled").attr("for", self.options.site);
                        }
                    }
                    else{
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: result});
                    }
                });
            },

            /**
             * Creates a new section in the dom for the passed in section object.
             * @param sectionObj {Object} the section object, assumed not <code>null</code>.
             * @param levelIdx {int} the 1 based level index.
             * @param parentId {String} the parent section id for this section, never <code>null</code>,
             * may be empty.
             * @param idx {int} the child sort order for this section within its parent.
             */
            createSection: function(sectionObj, levelIdx, parentId, idx){
                var self = this;
                var $section = $("<div></div>").addClass("perc-site-map-node")
                    .attr("id", sectionObj.id)
                    .attr("perc-section-title", sectionObj.title).hide();
                $section.append($("<div></div>").addClass("perc-site-map-node-topline")
                    .append($("<img/>").attr("src", this.getImageSrc(this.LINE_IMAGE))
                        .attr("width", "2")
                        .attr("height", "14")
                        .attr("alt", "")
                    ));
                $section.append($("<div></div>").addClass("perc-site-map-sectiontitle")
                    .text(this.truncateSectionTitle(sectionObj.title + ""))
                    .attr("title", sectionObj.title)
                    .attr("alt", sectionObj.title));
                var data = {
                    level: levelIdx,
                    parentId: parentId
                };
                $.extend(data, sectionObj);
                $section.attr("data", JSON.stringify(data));
                var $box = $("<div></div>").addClass("perc-site-map-box");
                $box.attr("data", JSON.stringify({
                    id: data.id,
                    parentId: parentId,
                    index: idx,
                    name: sectionObj.title,
                    sectionType:sectionObj.sectionType,
                    folderPath: sectionObj.folderPath}));
                $section.append($box);
                this.addNodeButtons($box, sectionObj, levelIdx);
                this.addNodeCountArea($box, sectionObj, levelIdx);
                if(levelIdx > 1)
                {
                    $box.on("click", function(evt){
                        $(".perc-site-map-box-selected").removeClass("perc-site-map-box-selected");
                        $(this).addClass("perc-site-map-box-selected");
                        self.handleMoveActionItemState();
                    });
                    // Add Drag & Drop
                    $box.draggable({opacity: 0.7,
                        helper: 'clone',
                        delay: 150
                    });
                }
                $box.droppable({
                    accept: function(droppable){return self.onDropAccept(this, droppable)},
                    tolerance: "pointer",
                    zIndex: 9600,
                    over: function(event, ui){
                        var $target = $(this);
                        $target.addClass("perc-site-map-box-over");
                    },
                    out: function(event, ui){
                        var $target = $(this);
                        $target.removeClass("perc-site-map-box-over");
                    },
                    drop: function(event, ui){
                        var isLandingPageAssign = ui.draggable.hasClass("perc-listing-category-PAGE");
                        var $target = $(this);
                        $target.removeClass("perc-site-map-box-over");
                        var targetData = JSON.parse($target.attr('data'));

                        if(isLandingPageAssign)
                        {
                            self.onAssignLandingPage(ui.draggable, targetData);
                        }
                        else
                        {

                            var droppedData = JSON.parse(ui.draggable.attr('data'));
                            self.onMove(
                                droppedData.id,
                                droppedData.parentId,
                                droppedData.index,
                                targetData.id,
                                -1);
                        }

                    }
                });


                return $section;

            },
            /**
             *  Truncate the section title to not be longer then the
             *  MAX_SECTION_TITLE_LENGTH.
             */
            truncateSectionTitle: function(title){
                if(title.length <= this.MAX_SECTION_TITLE_LENGTH)
                    return title;
                var len = this.MAX_SECTION_TITLE_LENGTH - 3;
                return   title.substr(0, len) + "...";
            },

            /**
             * Add the node buttons to the specified section parent.
             * @param $parent (jQuery) the parent section where the node buttons
             * will be added to. Assumed not <code>null</code>.
             * @param sectionObj the section object, assumed not <code>null</code>.
             */
            addNodeButtons: function($parent, sectionObj, levelIdx){
                var self = this;
                var $nodeButtons = $("<div></div>").addClass("perc-site-map-node-buttons");

                $parent.append($nodeButtons);
                var site = $('.perc-site-map-sitetitle').text();
                var configImageName;
                if (levelIdx > 1)
                    configImageName = this.CONFIG_BUTTON_IMAGE;
                else
                    configImageName = this.CONFIG_BUTTON_IMAGE_STANDALONE;
                var configAction = levelIdx > 1 ? this.onConfig : this.onSiteEdit;
                var $configButton =
                    this.createImageButton(configImageName,
                        I18N.message('perc.ui.sitemap.tooltip@Configure Section'),
                        configAction, {"sectionId": sectionObj.id,
                            "context": this, site: site},
                        "level_" + levelIdx);
                $configButton.attr("id", sectionObj.id + "_config");
                $configButton.addClass("perc-site-map-config-button");
                $nodeButtons.append($configButton);
                if(levelIdx > 1)
                {
                    var $deleteButton =
                        this.createImageButton(this.DELETE_BUTTON_IMAGE,
                            I18N.message('perc.ui.sitemap.tooltip@Remove section from Navigation'),
                            this.onDelete, {"sectionId": sectionObj.id, "context": self},
                            "level_" + levelIdx);
                    $deleteButton.attr("id", sectionObj.id + "_delete");
                    $deleteButton.addClass("perc-site-map-delete-button");
                    $nodeButtons.append($deleteButton);
                }

            },

            /**
             * Add the node count area to the specified section parent.
             * @param $parent (jQuery) the parent section where the node count area
             * will be added to. Assumed not <code>null</code>.
             * @param sectionObj the section object, assumed not <code>null</code>.
             */
            addNodeCountArea: function($parent, sectionObj, levelIdx){
                var self = this;
                if(sectionObj.sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.EXTERNAL_LINK)
                {
                    var $extLink = $("<div></div>").addClass("perc-site-map-externallink").append("External Link");
                    $parent.append($extLink);
                }
                else if(sectionObj.sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
                {
                    var $secLink = $("<div></div>").addClass("perc-site-map-sectionlink").append("Section Link");
                    $parent.append($secLink);
                }
                else
                {
                    var $nodeCount = $("<div></div>").addClass("perc-site-map-nodecount");
                    $parent.append($nodeCount);
                    var $pageCount = $("<div></div>").addClass("perc-site-map-pagecount");
                    $nodeCount.append($pageCount);
                    var $arrowImg = $("<img/>").attr("src", this.getImageSrc(this.ARROW_OFF_IMAGE))
                        .attr("alt", "perc.ui.images@ArrowIconAlt");
                    $pageCount.append($arrowImg);
                    var count = this.convertCXFArray(sectionObj.childIds).length;
                    $pageCount.append($("<span value=\"" + count + "\"></span>").text(count));

                    $nodeCount.on("click." + "level_" + levelIdx,
                        {"sectionId" : sectionObj.id}, function(evt){
                            self.onNodeCountClick(evt);
                        });
                }
            },

            /**
             * Helper function to create an image 'Button'.
             * @param image (String) the name of the image without ext of the image that
             * will serve as the button.
             * @param tooltip (String) the tooltip text for the button.
             * @param onclick (Function) the callback function to call when
             * a click event occurs.
             * @param data (Object) extra data that can be passed to the event handler
             * callback when the event fires.
             * @param namespace (String) event namspace that will be added to the click event.
             */
            createImageButton: function(image, tooltip, onclick, data, namespace)
            {
                var self = this;
                var $button = $("<img/>")
                    .attr("src", this.getImageSrc(image))
                    .attr("alt", tooltip)
                    .attr("title", tooltip)
                    .on("click." + namespace, data, onclick)
                    .on("mouseenter." + namespace, function(evt){
                        $(this).attr("src", self.getImageSrc(image, true));
                    })
                    .on("mouseleave." + namespace, function(evt){
                        $(this).attr("src", self.getImageSrc(image));
                    });

                return $button;
            },
            /**
             * Given an array of top to bottom section ids, expand each of
             * these sections. Note, this will currently only work correctly after a load
             * is complete and when no section have yet been expanded.
             * @param sectionIds {array} an array of section ids, may be <code>null</code> or
             * empty.
             */
            expandSections: function(sectionIds)
            {
                if(sectionIds == null || sectionIds.length == 0)
                    return;
                var self = this;
                var temp = [];
                $.extend(temp, sectionIds);
                temp.shift();
                this.expandSection(sectionIds[0], function(){
                    self.expandSections(temp);
                });

            },
            /**
             * Expands the specified section.
             * @param sectionId (String) the section id for the section to be expanded.
             */
            expandSection: function(sectionId, postCallback){
                var self = this;
                var $section = $("#" + sectionId);
                //incase parent node is deleted then chilc node refresh fails.
                if ($section[0] === undefined)
                    return;
                var data = $section.metadata({type: 'attr', name: 'data'});
                var levelIdx =  parseInt(data.level);
                var isExpanded = $section.hasClass("perc-site-map-expanded");
                if(!isExpanded)
                {
                    $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                    var $expanded =
                        $("#perc_level_" + levelIdx + " .perc-site-map-nodes .perc-site-map-expanded");

                    if($expanded.length === 1)
                        this.collapseSection($expanded.attr("id"));

                    $section.addClass("perc-site-map-expanded");
                    var $img = $section.find(".perc-site-map-pagecount img");
                    $img.attr("src", this.getImageSrc(this.ARROW_ON_IMAGE));


                    // Remove all sub levels if they exist
                    self.removeLevel(levelIdx + 1);
                    // Add new sub level
                    var $level = self.addLevel(data.id);
                    var $nodes = $level.children(".perc-site-map-nodes");
                    // Add the child sections to the sublevel
                    var children = self.convertCXFArray(data.childIds);
                    var childCount = children.length;
                    if(childCount > 0)
                    {
                        // Add bottom line
                        var $bottomLine = $("<div></div>")
                            .addClass("perc-site-map-node-bottomline").append(
                                $("<img/>").attr("src", self.getImageSrc(self.LINE_IMAGE))
                                    .attr("width", "2")
                                    .attr("alt", "")
                            ).hide();
                        $section.append($bottomLine);

                        $.Perc_SectionServiceClient.getChildren(self.getSectionObject($section),
                            function(status, parentSection, data){
                                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                                {
                                    var children = self.convertCXFArray(data.SiteSection);
                                    var count = children.length;
                                    for(var i = 0; i < count; i ++)
                                    {
                                        var child = children[i];
                                        self.addSectionToLevel(self.createSection(child, levelIdx + 1, sectionId, i), levelIdx + 1);
                                    }
                                    self.layoutLevel(levelIdx);
                                    self.layoutLevel(levelIdx + 1);
                                    if(typeof(postCallback) == 'function')
                                        postCallback();
                                    $.unblockUI();
                                }
                                else
                                {
                                    $.unblockUI();
                                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                                }
                            });

                    }
                    else
                    {
                        self.adjustLevelWidths();
                        $.unblockUI();
                    }

                }
            },

            /**
             * Creates a section json object from the section jQuery object and
             * its metadata.
             * @param $section (jQuery) the section, assumed not <code>null</code>.
             */
            getSectionObject: function($section){
                var data = $section.metadata({type: 'attr', name: 'data'});
                return {SiteSection: {
                        id: data.id,
                        title: data.title,
                        folderPath: data.folderPath,
                        childIds: data.childIds
                    }};
            },

            /**
             * Helper function to convert CXF style array into an actual array.
             */
            convertCXFArray: function(cxfarray){

                if(typeof(cxfarray) == 'undefined')
                    return [];
                if(typeof(cxfarray) != 'object')
                    return [cxfarray];
                return cxfarray;
            },

            /**
             * Collapse the specified section.
             * @param sectionId (String) the section id for the section to be expanded.
             */
            collapseSection: function(sectionId){
                var self = this;
                var $section = $("#" + sectionId);
                var isExpanded = $section.hasClass("perc-site-map-expanded");
                if(isExpanded)
                {
                    $section.removeClass("perc-site-map-expanded");
                    $section.children(".perc-site-map-node-bottomline").remove();
                    var $img = $section.find(".perc-site-map-pagecount img");
                    $img.attr("src", this.getImageSrc(this.ARROW_OFF_IMAGE));
                    var levelIdx =  parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                    self.removeLevel(levelIdx + 1);
                    self.handleMoveActionItemState();

                }
            },
            /**
             * Decides if a drop is allowed for the specified droppable/target pair.
             */
            onDropAccept: function(target, droppable)
            {
                var $target = $(target);
                var $source = $(droppable[0]);
                var spec = $source.data("spec");
                if($source.hasClass("perc-listing-category-PAGE") && $target.hasClass("perc-site-map-box"))
                {
                    var targetData = JSON.parse($target.attr('data'));
                    if(targetData.sectionType && targetData.sectionType != $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION)
                        return false;
                    var pageSite = (spec['path'].split('/'))[2];
                    if($.PercNavigationManager.getSiteName() != pageSite)
                        return false;
                    return true;
                }
                if(!$source.hasClass("perc-site-map-box"))
                    return false;
                try
                {
                    var targetData = JSON.parse($target.attr('data'));
                    var sourceData =  JSON.parse($source.attr('data'));
                    if(targetData.sectionType && targetData.sectionType != $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION)
                        return false;
                    if(typeof(targetData.targetId) != "undefined")
                        targetData.parentId = targetData.targetId; // This is a drop areas parent
                    if(typeof(targetData.parentId) == "undefined")
                        return false; // target is root
                    if(targetData.parentId == sourceData.id)
                        return false;
                    if(targetData.index==0)
                        return true;
                    var parents = this.getSectionParentChain(targetData.parentId);
                    return ($.inArray(sourceData.id, parents) == -1);
                }
                catch(err)
                {
                    return false;
                }
            },

            /**
             * Callback function for when the node count area is clicked. This
             * will invoke either expand or collapse of the section depending on
             * the current expanded state. The section id will
             * be availble as (evt.data.sectionId).
             * @param evt (Object) the jQuery event object.
             */
            onNodeCountClick: function(evt){
                var self = this;
                // Is section expanded already?
                var $section = $("#" + evt.data.sectionId);
                var isExpanded = $section.hasClass("perc-site-map-expanded");
                if(isExpanded)
                {
                    self.collapseSection(evt.data.sectionId);
                }
                else
                {
                    self.expandSection(evt.data.sectionId);
                }
            },
            /**
             *  Callback function for when the config button of the root (site) node is clicked.
             *  @param evt {Object} the jQuery event object.
             */
            onSiteEdit: function(evt){
                var self = evt.data.context;
                var sectionID = evt.data.sectionId;
                self.editSiteSectionDialog.open(evt.data.site, function(dStatus, fieldData){
                    if(dStatus == "ok")
                    {
                        // create a map of the request parameters
                        var fields = {};
                        $.each( fieldData, function() { fields[this.name] = this.value; } );

                        // if parameters include a list of users, they are comma separated, parse them into an array
                        var writePrincipalsParam = fields['writePrincipals'];
                        var usernames = [];
                        if(writePrincipalsParam)
                            usernames = writePrincipalsParam.split(",");

                        // if there were users passed in, create object to POST to the server
                        var writePrincipals = [];
                        for(u=0; u<usernames.length; u++)
                            writePrincipals[u] = {name : usernames[u], type : "USER"};

                        var secureSite = (typeof(fields['perc-enable-site-security']) != "undefined");
                        var canonicalURLs = (typeof(fields['perc-enable-canonical-url']) != "undefined");
                        var mobilePreviewEnabled = (typeof(fields['perc-enable-mobile-preview']) != "undefined");
                        var canonicalURLsReplace = (typeof(fields['perc-replace-canonical-tags']) != "undefined");

                        var sitePropsObj = {'SiteProperties': {
                                'id': fields['site_id'],
                                'homePageLinkText': fields['page_title_link'],
                                'name': fields['site_hostname'],
                                'description': fields['site_desc'],
                                'folderPermission':{'accessLevel':fields['perc-site-folder-permission'],
                                    'writePrincipals':writePrincipals},
                                'loginPage' : fields['perc-site-login-page'],
                                //'registrationPage' : fields['perc-site-registration-page'],
                                'registrationConfirmationPage' : fields['perc-site-registration-confirmation-page'],
                                'resetRequestPasswordPage' : fields['perc-site-reset-pw-request-page'],
                                'resetPage' : fields['perc-site-reset-password-page'],
                                'isSecure' : secureSite,
                                'cssClassNames' : fields['perc-site-navigation-cssclassnames'],
                                'defaultFileExtention' : fields['perc-site-pagefile-extention-default'],
                                'isCanonical' : canonicalURLs,
                                'siteProtocol' : fields['perc-site-protocol'],
                                'defaultDocument' : fields['perc-site-default-document'],
                                'canonicalDist' : fields['perc-canonical-url-dist'],
                                'isCanonicalReplace' : canonicalURLsReplace,
                                'mobilePreviewEnabled': mobilePreviewEnabled
                            }};
                        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                        $.PercSiteService.updateSiteProperties(sitePropsObj, function(status, results){
                            if (status == $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                if (results.SiteProperties.pubServersChanged)
                                {
                                    $.unblockUI();
                                    $.perc_utils.alert_dialog({
                                        title: I18N.message("perc.ui.page.general@Warning"),
                                        content: I18N.message("perc.ui.site.map@Publishing Location Changed"),
                                        okCallBack: function(){
                                            $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_SITE_ARCH, fields['site_hostname']);
                                        }
                                    });
                                }
                                else
                                {
                                    $.PercNavigationManager.goToLocation($.PercNavigationManager.VIEW_SITE_ARCH, fields['site_hostname']);
                                }
                            }
                            else
                            {
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: results});
                            }
                        });
                    }
                });
            },

            /**
             * Callback function when the section config button is clicked.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"sectionId": section id, "context": this class object, "site":  name of the site}
             */
            onConfig: function(evt){
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var sectionType = $section.metadata({type: 'attr', name: 'data'}).sectionType;
                if(sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
                {
                    self.editSectionLink(evt);
                }
                else if(sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.EXTERNAL_LINK)
                {
                    self.editExternalLink(evt);
                }
                else
                {
                    self.editSection(evt);
                }
            },

            /**
             * Helper function to handle the section editing. Opens the edit section dialog and on OK of that dialog, collects
             * the section data and calls the section service method ($.Perc_SectionServiceClient.edit) with
             * SiteSectionProperties object. Calls afterEditSectionCallback method once the service returns for further processing.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"sectionId": section id, "context": this class object, "site":  name of the site}
             */
            editSection: function(evt)
            {
                var self = evt.data.context;
                var sectionID = evt.data.sectionId;
                self.editSectionDialog.open(evt.data.sectionId, function(dStatus, fieldData) {
                    if (dStatus == "ok")
                    {
                        // Process the OK

                        // create a map of the request parameters
                        var fields = { };
                        $.each( fieldData, function() { fields[this.name] = this.value; } );

                        // if parameters include a list of users, they are comma separated, parse them into an array
                        var writePrincipalsParam = fields['writePrincipals'];
                        var usernames = [];
                        if(writePrincipalsParam)
                            usernames = writePrincipalsParam.split(",");

                        // if there were users passed in, create object to POST to the server
                        var writePrincipals = [];
                        for(u=0; u<usernames.length; u++)
                            writePrincipals[u] = {name : usernames[u], type : "USER"};

                        var editSectionObj = {'SiteSectionProperties' : {
                                'id' : sectionID,
                                'title' : fields['section_name'],
                                'folderName' : fields['page_url'],
                                'target' : fields['perc-section-target'],
                                'folderPermission':{'accessLevel':fields['perc-section-folder-permission'],
                                    'writePrincipals':writePrincipals},
                                'requiresLogin': fields['requiresLogin'],
                                'allowAccessTo':fields['perc-group-name-allowed'],
                                'cssClassNames':fields['perc-section-navigation-cssclassnames']
                            }};
                        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                        $.Perc_SectionServiceClient.edit(editSectionObj, function(status, data) {
                            self.afterEditSectionCallback(status,data,evt);
                        });

                    }
                });
            },

            /**
             * Helper function to handle the section link editing. Opens the edit section links dialog and on OK of that dialog,
             * creates UpdateSectionLink object using the selected section and old section. Calls afterEditSectionCallback
             * method once the service returns for further processing.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"sectionId": section id, "context": this class object, "site":  name of the site}
             */
            editSectionLink: function(evt)
            {
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var sectionObj = $section.metadata({type: 'attr', name: 'data'});
                var levelIdx =   parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                var parentSectionId = $("#perc_level_" + levelIdx).metadata({type: 'attr', name: 'data'}).parentId;
                var siteName = $(".perc-site-map-sitetitle").text();
                //Get original sections path.
                var origSecId = evt.data.sectionId.split("_")[0];

                //FIX ISSUE CM-1364, retrieve the displayPath from the server instead of calculate it.
                var sectionPath = $section.metadata({type: 'attr', name: 'data'}).displayTitlePath;
                var dlgTitle = I18N.message('perc.ui.sitemap.editsectionlink.dlgtitle@Edit Section Link');
                $.PercEditSectionLinksDialog().open(sectionObj, sectionPath, siteName, dlgTitle, function(dStatus, fieldData) {
                    if (dStatus == "ok")
                    {
                        // Process the OK
                        // create a map of the request parameters
                        var fields = { };
                        $.each( fieldData, function() { fields[this.name] = this.value; } );
                        //If user picks the same section simply return.
                        if(evt.data.sectionId == fields['perc-section-link-targetid'] || evt.data.sectionId.split("_")[0] ==
                            fields['perc-section-link-targetid'])
                        {
                            $.unblockUI();
                            return;
                        }
                        var tid = fields['perc-section-link-targetid'];
                        if(self.isChild(parentSectionId,tid))
                        {
                            self.displayDuplicateSectionMessage();
                            return;
                        }
                        else
                        {
                            var secLinkObj = {'UpdateSectionLink' : {
                                    'oldSectionId' : evt.data.sectionId,
                                    'newSectionId' : fields['perc-section-link-targetid'],
                                    'parentSectionId': parentSectionId
                                } };
                            $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                            $.Perc_SectionServiceClient.updateSectionLink(secLinkObj, function(status, data) {
                                self.afterEditSectionCallback(status,data,evt);
                            });

                        }
                    }
                });
            },

            /**
             * Helper function to handle the external link editing. Opens the edit section links dialog and on OK of that dialog,
             * creates CreateExternalLinkSection object using the form data from the dialog. Calls afterEditSectionCallback
             * method once the service returns for further processing.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"sectionId": section id, "context": this class object, "site":  name of the site}
             */
            editExternalLink: function(evt)
            {
                var self = evt.data.context;
                var sectionID = evt.data.sectionId;
                var $section = $("#" + evt.data.sectionId);
                var sectionObj = $section.metadata({type: 'attr', name: 'data'});
                var siteName = $(".perc-site-map-sitetitle").text();
                var parentPath = self.getParentPath(evt.data.sectionId);
                var dlgTitle = I18N.message('perc.ui.sitemap.editexternallink.dlgtitle@Edit External Link');
                $.PercEditSectionLinksDialog().open(sectionObj, parentPath, siteName, dlgTitle, function(dStatus, fieldData) {
                    if (dStatus == "ok")
                    {
                        // Process the OK
                        // create a map of the request parameters
                        var fields = { };
                        $.each( fieldData, function() { fields[this.name] = this.value; } );

                        var extLinkObj = {'CreateExternalLinkSection' : {
                                'externalUrl' : fields['perc-external-link-url'],
                                'linkTitle' : fields['perc-external-link-text'],
                                'folderPath': sectionObj.folderPath,
                                'sectionType':sectionObj.sectionType,
                                'target':fields['perc-external-link-target'],
                                'cssClassNames':fields['perc-external-link-navigation-cssclassnames']
                            } };
                        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                        $.Perc_SectionServiceClient.updateExternalLink(sectionID, extLinkObj, function(status, data) {
                            self.afterEditSectionCallback(status,data,evt);
                        });

                    }
                });
            },


            /**
             * Helper function to update the sections after editing is done. If the type is section link, calls the load
             * method to refresh the all the sections. Otherwise updates the titles and other data. If the tyoe is
             * Section updates the section link titles if exist.
             * @param status expects a String object with $.PercServiceUtils.STATUS_XXX value.
             * @param data with SiteSection object.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"sectionId": section id, "context": this class object, "site":  name of the site}
             */
            afterEditSectionCallback: function(status, data, evt)
            {
                var self = evt.data.context;
                var sectionID = evt.data.sectionId;
                var $section = $("#" + sectionID);
                var oldSecData = JSON.parse($("#" + sectionID).attr("data"));
                if (status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    if(data.SiteSection.sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
                    {
                        // need to do a reload as we moved to a different section parent
                        var expandState = this.getExpansionState();
                        var scrollTop = $(window).scrollTop();
                        $.Perc_SectionServiceClient.clearCache();
                        var sitename = $(".perc-site-map-sitetitle").text();
                        self.load(sitename, true, function(){
                            self.expandSections(expandState);
                            self.handleMoveActionItemState();
                            window.setTimeout(function(){$(window).scrollTop(scrollTop);}, 200);
                            self.fireOnChange();  // refresh finder
                            $.unblockUI();
                        });
                        return;

                    }
                    var oldSectionName = $("#" + sectionID + " div.perc-site-map-sectiontitle").text();
                    var parentNodePath = self.getParentPath(sectionID);

                    // update the title/label of the section
                    $("#" + sectionID + " div.perc-site-map-sectiontitle")
                        .text(self.truncateSectionTitle(data.SiteSection.title + ""))
                        .attr("title", data.SiteSection.title)
                        .attr("alt", data.SiteSection.title);


                    // recursively update the "data" attribute for this section and any child sections
                    self.updateSectionData($section, data);

                    //Update section links pointing to the section, if we are editing the section
                    if(data.SiteSection.sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION)
                    {
                        $("[id*='" + sectionID + "_']").each(function(){
                            $(this).find("div.perc-site-map-sectiontitle")
                                .text(self.truncateSectionTitle(data.SiteSection.title + ""))
                                .attr("title", data.SiteSection.title)
                                .attr("alt", data.SiteSection.title);
                        });
                    }

                    //Related to the fix for issue CM-1364, since we don't calculate the displaytitlepath in the client side,
                    //we should keep the displayTitlePath property of the section links updated.
                    //Update the displayTitlePath of the sections links that are affected by node title change.
                    if(oldSectionName != data.SiteSection.title){ // just if the display title change
                        $(".perc-site-map-sectionlink").parents(".perc-site-map-node").each(function(){
                            //Get all sections nodes
                            var displayTitlePath = $(this).metadata({type: 'attr', name: 'data'}).displayTitlePath;
                            if(typeof(displayTitlePath) != "undefined"){
                                //Replace the entire path to the node.
                                var oldPath = parentNodePath + "/" + oldSectionName;
                                var newPath = parentNodePath + "/" + data.SiteSection.title;
                                $(this).metadata({type: 'attr', name: 'data'}).displayTitlePath = displayTitlePath.replace(oldPath, newPath);
                            }
                        })
                    }

                    $.Perc_SectionServiceClient.clearCache("getChildren");

                    self.handleMoveActionItemState();
                    self.layoutAll();
                    self.fireOnChange(); // refresh finder in case folder name changed
                    $.unblockUI();

                    if (data.SiteSection.sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION) {
                        $.PercRedirectHandler.createRedirect(oldSecData.folderPath, data.SiteSection.folderPath, "section").fail(function(errMsg){
                            $.unblockUI();
                            $.perc_utils.alert_dialog({
                                title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"),
                                content: errMsg,
                                okCallBack: $.noop
                            });
                        }).done($.noop);
                    }
                }
                else
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                }
            },

            /**
             * Update the supplied section with the supplied data, walk expanded child sections and load their data
             * and recurse.
             *
             * @param $section JQuery object wrapping the section node to update
             * @param data The corresponding section data returned from the server
             *
             */
            updateSectionData: function($section, data)
            {
                var self = this;
                var oldData = JSON.parse($section.attr("data"));
                $.extend(oldData, data.SiteSection);
                $section.attr("data", JSON.stringify(oldData));

                var $sectionBox = $section.find(".perc-site-map-box");
                var oldBoxData = JSON.parse($sectionBox.attr("data"));
                $.extend(oldBoxData, data.SiteSection);
                $sectionBox.attr("data", JSON.stringify(oldBoxData));

                // update the metadata plugin cache
                var oldMetaData = $section.metadata({type: 'attr', name: 'data'});
                $.extend(oldMetaData, data.SiteSection);

                // fixme update child data recursively
                var isExpanded = $section.hasClass("perc-site-map-expanded");
                if(isExpanded)
                {
                    var levelIdx =  parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                    var $childLevel =  $("#perc_level_" + (levelIdx + 1));
                    var $nodes = $childLevel.children(".perc-site-map-nodes").children(".perc-site-map-node")
                    $nodes.each(function(){

                        var $childSection = $(this);
                        var mData = JSON.parse($(this).attr("data"));

                        $.Perc_SectionServiceClient.getSection(mData.id, function(status, result){

                            if (status == $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                self.updateSectionData($childSection, result);
                            }
                            else
                            {
                                $.perc_utils.debug(result);
                            }

                        });
                    });
                }
            },

            /**
             * Prompts the user with a section tree dialog to select the target selection.
             * Initiates move if target is selected.
             */
            onMoveWithDialog: function(){
                var self = this;
                var $selected = $(".perc-site-map-box-selected");

                if($selected.length > 0)
                {
                    var selectedData = JSON.parse($selected.attr("data"));
                    var parentData = JSON.parse($selected.parent().attr("data"));
                    var sitename = $(".perc-site-map-sitetitle").text();
                    var treeLabel = I18N.message('perc.ui.sitemap.movesection.label@message', [parentData.title])
                    $.PercSectionTreeDialog.open(sitename, selectedData.id, treeLabel, I18N.message('perc.ui.sitemap.movesection.title@Move Section'), "Move", function(targetId){
                        self.onMove(
                            selectedData.id,
                            selectedData.parentId,
                            selectedData.index,
                            targetId,
                            -1);
                    });
                }
            },

            /**
             * Checks whether the supplied child section id is present in child ids of the supplied parent or not.
             * @param parentSectionId, the guid of the parent id.
             * @param childSectionId, the guid of the child id
             * @return boolean <code>true</code> if it is a child otherwise <code>false</code>.
             */
            isChild: function(parentSectionId, childSectionId)
            {
                var childIds = [];
                //strip the complex ids from children and create another array
                var $parentSection = $("#" + parentSectionId);
                var pdata = $parentSection.metadata({type: 'attr', name: 'data'});
                if(pdata.childIds)
                {
                    if(Array.isArray(pdata.childIds)){
                        $.each(pdata.childIds, function(){
                            childIds.push(this.split("_")[0]);
                        });
                    }else{
                        childIds.push(pdata.childIds.split("_")[0]);
                    }
                }
                childSectionId = childSectionId.split("_")[0];
                return $.inArray(childSectionId, childIds) > -1;
            },

            /**
             * Helper function to display the duplicate section not allowed message.
             * Unblocks the UI before displaying the message.
             */
            displayDuplicateSectionMessage: function()
            {
                $.unblockUI();
                var msg = I18N.message('perc.ui.sitemap.duplicatesection@Duplicate section not allowed');
                var errorLabel = I18N.message('perc.ui.labels@Error');
                $.perc_utils.alert_dialog({title: errorLabel, content: msg});
            },

            /**
             * Function to handle assigning a new landing page to a section.
             * @param source {object} object with meta info about the source page, cannot
             * be <code>null</code>.
             * @param target {object} object with meta info about the target section, cannot
             * be <code>null</code> or empty.
             */
            onAssignLandingPage: function(source, target){
                var self = this;
                var srcSpec = source.data("spec");
                var pageId = srcSpec['id'];
                var pageName = srcSpec['name'];
                var sectionName = target.name;
                var sectionId = target.id;
                var path = target.folderPath;
                $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                $.Perc_SectionServiceClient.replaceLandingPage(
                    pageId,
                    sectionId,
                    function(status, result){
                        if(status == $.PercServiceUtils.STATUS_SUCCESS)
                        {
                            self.fireOnChange();
                            $.unblockUI();
                            var data = result.ReplaceLandingPage;
                            var args = [
                                pageName,
                                sectionName,
                                data.newLandingPageName,
                                data.oldLandingPageName,
                                path];
                            var $dialog = $("<div>"
                                + "<div style='float:left;'>"
                                + I18N.message('perc.ui.sitemap.assign.landing.page.label@message', args)
                                + "</div>"
                                + "<div class='ui-layout-south'>"
                                + "<div id='perc_buttons' style='z-index: 100;'></div>"
                                +  "</div>"
                                + "</div>")
                                .perc_dialog({
                                    title: I18N.message('perc.ui.sitemap.assign.landing.page.title@Landing Page Assigned'),
                                    resizable: false,
                                    modal: true,
                                    percButtons:  {
                                        "Ok": {
                                            click: function(){$dialog.remove()},
                                            id: "perc-landing-page-assign-info-ok"
                                        }
                                    },
                                    id: "perc-landing-page-assign-info",
                                    width: 400
                                });

                        }
                        else
                        {
                            $.unblockUI();
                            $.perc_utils.alert_dialog({title: 'Error', content: result});
                        }
                    }
                );

            },
            /**
             * Moves the source section to the target section in the specified index.
             * @param sourceId {String} id of the source section, cannot be <code>null</code> or
             * empty.
             * @param sourceParentId {String} id of the sources parent section, cannot be
             * <code>null</code>, may be empty.
             * @param sourceIdx {int} positional index of the source section within its parent.
             * @param targetId {String} id of the target section of which the source section will
             * become a child.
             * @param targetIdx {int} intended positional index of the moved section within the
             * target section. If -1 then the section will just be appended.
             */
            onMove: function(sourceId, sourceParentId, sourceIdx, targetId, targetIdx){
                var self = this;
                var isSortOnly = (sourceParentId == targetId);
                var newIndex = (isSortOnly && (sourceIdx < targetIdx))
                    ? targetIdx - 1
                    : targetIdx;
                //Create request object
                var req = {MoveSiteSection: {
                        sourceParentId: sourceParentId,
                        sourceId: sourceId,
                        targetId: targetId,
                        targetIndex: newIndex
                    }};
                var expandState = this.getExpansionState();
                var scrollTop = $(window).scrollTop();
                $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                $.Perc_SectionServiceClient.move(req, function(status, data, result){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS)
                    {
                        var $source = $("#" + sourceId);
                        var $sourceParent = $("#" + sourceParentId);
                        var $target = $("#" + targetId);
                        function handleMoveRefresh(){
                            var sourceParentParentId = JSON.parse($sourceParent.attr("data")).parentId;
                            // Handle cache clearing
                            $.Perc_SectionServiceClient.clearCache("getChildren");
                            if (isSortOnly) {
                                // Just modify the dom locally as only a sort occurred and not a move
                                var targetData = $target.metadata({
                                    type: 'attr',
                                    name: 'data'
                                });
                                var lev = $source.metadata({
                                    type: 'attr',
                                    name: 'data'
                                }).level;
                                var $level = $("#perc_level_" + lev);
                                var $nodes = $level.children(".perc-site-map-nodes");
                                if (sourceIdx > targetIdx) {
                                    var $node = $nodes.children(".perc-site-map-node:eq(" + targetIdx + ")");
                                    $node.before($source);
                                    $.extend(targetData, result.SiteSelection);
                                    $target.attr("data", JSON.stringify(targetData));
                                    $nodes.find(".perc-site-map-node .perc-site-map-box").each(function(i){
                                        var mData = JSON.parse($(this).attr("data"));
                                        $.extend(mData, {
                                            index: i
                                        });
                                        $(this).attr("data", JSON.stringify(mData));
                                    });
                                    self.handleMoveActionItemState();
                                    self.layoutAll();
                                }
                                else
                                if ((sourceIdx < targetIdx) && (sourceIdx != (targetIdx - 1))) {

                                    var $node = $nodes.children(".perc-site-map-node:eq(" + (targetIdx - 1) + ")");
                                    $node.after($source);
                                    $.extend(targetData, result.SiteSelection);
                                    $target.attr("data", JSON.stringify(targetData));
                                    $nodes.find(".perc-site-map-node .perc-site-map-box").each(function(i){
                                        var mData = JSON.parse($(this).attr("data"));
                                        $.extend(mData, {
                                            index: i
                                        });
                                        $(this).attr("data", JSON.stringify(mData));
                                    });
                                    self.handleMoveActionItemState();
                                    self.layoutAll();
                                }
                                $.unblockUI();
                            }
                            else {
                                // need to do a reload as we moved to a different section parent
                                var sitename = $(".perc-site-map-sitetitle").text();
                                self.load(sitename, true, function(){
                                    self.expandSections(expandState);
                                    self.handleMoveActionItemState();
                                    window.setTimeout(function(){
                                        $(window).scrollTop(scrollTop);
                                    }, 200);
                                    self.fireOnChange(); // refresh finder
                                    $.unblockUI();
                                });
                            }
                        }
                        var sourceData = JSON.parse($source.attr("data"));
                        var sectionType = sourceData.sectionType;
                        var oldPath = sourceData.folderPath;
                        var targetData = JSON.parse($target.attr("data"));
                        var newPath = targetData.folderPath + oldPath.substring(oldPath.lastIndexOf("/"));

                        $.PercRedirectHandler.createRedirect(oldPath, newPath, sectionType)
                            .fail(function(errMsg){
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"), content: errMsg, okCallBack: function(){
                                        handleMoveRefresh();
                                    }});
                            })
                            .done(function(){
                                handleMoveRefresh();
                            });

                    }
                    else
                    {
                        $.unblockUI();
                        $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                    }
                });

            },

            /**
             * Callback function when a section delete button is clicked. Delegates to appropriate method based on the type
             * of the section.
             * @param evt (Object) the jQuery event object. The section id will
             * be availble as (evt.data.sectionId).
             */
            onDelete: function(evt){
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var levelIdx =   parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                var parentSectionId = $("#perc_level_" + levelIdx).metadata({type: 'attr', name: 'data'}).parentId;
                var $parentSection = $("#" + parentSectionId);
                var folderPath = JSON.parse($section.attr("data")).folderPath;
                var path = folderPath.replace('//Sites', $.perc_paths.SITES_ROOT);
                var sectionName = JSON.parse($section.attr("data")).title;
                var sectionType = $section.metadata({type: 'attr', name: 'data'}).sectionType;
                if(sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
                {
                    self.deleteSectionLink(evt);
                }
                else if(sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.EXTERNAL_LINK)
                {
                    self.deleteExternalLink(evt);
                }
                else
                {
                    var delConfirmHtml = $("<div class=\"perc-remove-section-confirm-container\">" +
                        "<div>Do you want to remove the section from the Navigation or delete the section from the site entirely?</div><br/>" +
                        "<div class=\"perc-remove-section-option-container\">" +
                        "<div class=\"perc-remove-section-option-row\">" +
                        "<input type=\"radio\" name=\"perc-remove-section-option\" value=\"remove-section\" checked=\"checked\"/>" +
                        "<label for=\"remove-section\">Convert section to folder and remove section and all sub-sections from the Navigation.</label>" +
                        "</div>" +
                        "<div style=\"clear:both\"/>" +
                        "<div class=\"perc-remove-section-option-row\">" +
                        "<input type=\"radio\" name=\"perc-remove-section-option\" value=\"delete-section\"/>" +
                        "<label for=\"delete-section\">Delete the section and all sub-sections from the site. All pages and content will be moved to Recycle Bin.</label>" +
                        "</div>" +
                        "<div style=\"clear:both\"/>" +
                        "</div>" +
                        "</div>");
                    delConfirmHtml.find(".perc-delete-section-text").on("click",function(){
                        var forattr = $(this).attr("for");
                        delConfirmHtml.find("input[value=" + forattr + "]").prop("checked", true);
                    });
                    $.perc_utils.confirm_dialog({
                        id: 'perc-delete-section-link',
                        title: 'Delete Section Link',
                        question: delConfirmHtml,
                        success: function(){
                            var deleteOption = $("input:radio[name ='perc-remove-section-option']:checked").val();
                            if(deleteOption == "delete-section"){
                                $.PercPathService.deleteSection(path, sectionName,function(){
                                    self.afterDeleteCallback(evt, true);
                                });
                            }
                            else{
                                $.Perc_SectionServiceClient.convertSectionToFolder(evt.data.sectionId, function(status, message){
                                    if(!status){
                                        $.perc_utils.alert_dialog({"title":I18N.message("perc.ui.site.map@Error Deleting Section"), "content":message});
                                    }
                                    self.afterDeleteCallback(evt, false);
                                });
                            }

                        },
                        width:500});
                }
            },

            /**
             * Helper method to handle the refresh of the section map after deleting a section. If the deleted object type is
             * Section and if it has associated section links, then forces a reload of the whole map.
             * @param evt (Object) the jQuery event object. The section id will
             * be availble as (evt.data.sectionId).
             */
            afterDeleteCallback: function(evt, createRedirect)
            {
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var metadata = $section.metadata({type: 'attr', name: 'data'});
                var sectionType = metadata.sectionType;
                //If the section has any section links then hard refresh as we have to remove the section links
                if((sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION && $("[id*='" + evt.data.sectionId + "_']").length>0)||
                    sectionType == $.Perc_SectionServiceClient.PERC_SECTION_TYPE.SECTION_LINK)
                {
                    var expandState = this.getExpansionState();
                    var scrollTop = $(window).scrollTop();
                    $.Perc_SectionServiceClient.clearCache();
                    var sitename = $(".perc-site-map-sitetitle").text();
                    self.load(sitename, true, function(){
                        self.expandSections(expandState);
                        self.handleMoveActionItemState();
                        window.setTimeout(function(){$(window).scrollTop(scrollTop);}, 200);
                        self.fireOnChange();  // refresh finder
                        $.unblockUI();
                    });
                    if(createRedirect){
                        $.PercRedirectHandler.createRedirect(metadata.folderPath, "", sectionType)
                            .fail(function(errMsg){
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"), content: errMsg, okCallBack: function(){
                                    }});
                            })
                            .done(function(){
                            });
                    }
                    return;
                }
                var levelIdx =   parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                var parentSectionId = $("#perc_level_" + levelIdx).metadata({type: 'attr', name: 'data'}).parentId;
                var $parentSection = $("#" + parentSectionId);
                self.collapseSection(evt.data.sectionId);
                $section.remove();

                var $pagecount = $parentSection.find(".perc-site-map-pagecount span");
                var count = parseInt($pagecount.text()) - 1;
                $pagecount.attr("value",count).text(count);
                if(count == 0)
                    $parentSection.children(".perc-site-map-node-bottomline").remove();

                // Update child list in parent
                var temp = $parentSection.metadata({type: 'attr', name: 'data'});
                temp.childIds = self.convertCXFArray(temp.childIds);
                var idx = $.inArray(evt.data.sectionId, temp.childIds);
                if(idx > -1)
                {
                    temp.childIds.splice(idx, 1);
                    $parentSection.attr("data", JSON.stringify(temp));
                }
                $.Perc_SectionServiceClient.clearCache("getChildren", parentSectionId);
                $.Perc_SectionServiceClient.clearCache("getChildren", evt.data.sectionId);
                self.handleMoveActionItemState();
                self.layoutAll();
                self.fireOnChange();  // refresh finder
            },

            /**
             * Helper method to handle the section link deletions, shows an alert message before deleting. Calls the
             * $.Perc_SectionServiceClient.deleteSectionLink with section id and parent id. Calls afterDeleteCallback for
             * refreshing the map after deleting the section.
             * @param evt (Object) the jQuery event object. The section id will
             * be availble as (evt.data.sectionId).
             */
            deleteSectionLink: function(evt)
            {
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var levelIdx =   parseInt($section.metadata({type: 'attr', name: 'data'}).level);
                var parentSectionId = $("#perc_level_" + levelIdx).metadata({type: 'attr', name: 'data'}).parentId;
                var sectionName = $section.metadata({type: 'attr', name: 'data'}).title;
                $.perc_utils.confirm_dialog({
                    id: 'perc-delete-section-link',
                    title: 'Delete Section Link',
                    question: 'Delete section link ' + sectionName +'?',
                    success: function(){
                        $.Perc_SectionServiceClient.deleteSectionLink(evt.data.sectionId,parentSectionId,function(status, result){
                            if(status == $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                self.afterDeleteCallback(evt, true);
                            }
                            else
                            {
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                            }
                        });
                    },
                    width:500});
            },

            /**
             * Helper method to handle the external link deletions, shows an alert message before deleting. Calls the
             * $.Perc_SectionServiceClient.deleteSection with section id. Calls afterDeleteCallback for
             * refreshing the map after deleting the section.
             * @param evt (Object) the jQuery event object. The section id will
             * be availble as (evt.data.sectionId).
             */
            deleteExternalLink: function(evt)
            {
                var self = evt.data.context;
                var $section = $("#" + evt.data.sectionId);
                var sectionName = $section.metadata({type: 'attr', name: 'data'}).title;
                $.perc_utils.confirm_dialog({
                    id: 'perc-delete-external-link',
                    title: 'Delete External Link',
                    question: 'Delete external link ' + sectionName +'?',
                    success: function(){
                        $.Perc_SectionServiceClient.deleteSection(evt.data.sectionId,function(status, result){
                            if(status == $.PercServiceUtils.STATUS_SUCCESS)
                            {
                                self.afterDeleteCallback(evt, false);
                            }
                            else
                            {
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                            }
                        });
                    },
                    width:500});
            },

            /**
             * Callback function for when a section save button is clicked.
             * @param evt (Object) the jQuery event object. The parent section id will
             * be availble as (evt.data.parentSectionId). The level index will be
             * available as (evt.data.level).        *
             */
            onAddSection: function(evt){
                var self = evt.data.context;
                var $parentSection = $("#" + evt.data.parentSectionId);
                var folderPath = $parentSection.metadata({type: 'attr', name: 'data'}).folderPath;
                var allTitle = [];
                $(evt.currentTarget).parent().next().children().each( function( ){
                    var data  = $(this).attr("data")
                    data = JSON.parse(data)
                    var title  = data.title
                    if(!(title == undefined || title == "")){
                        allTitle.push(title);
                    }
                });
                self.newSectionDialog.open(self.options.site, evt.data.parentSectionId, function(dStatus, fieldData){
                    if(dStatus == "ok")
                    {
                        var fields = {};
                        $.each( fieldData, function() { fields[this.name] = this.value; } );
                        $.PercBlockUI($.PercBlockUIMode.CURSORONLY);
                        if(fields['perc-section-type'] == "section")
                        {
                            var fileName = 'index';
                            var duplicateErrorMessage = I18N.message("perc.ui.site.map@Section Duplicate",[fields['section_name']]);
                            if(allTitle.indexOf(fields['section_name']) > -1) {
                                $.unblockUI();
                                $.perc_utils.alert_dialog({title: 'Error', content: duplicateErrorMessage});
                                return;
                            }
                            $.PercSiteService.getSiteProperties(self.options.site, function(status, result) {
                                if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                                    var fileExt = result.SiteProperties.defaultFileExtention;
                                    if (fileExt) {
                                        fileName += "." + fileExt;
                                    }
                                } else {
                                    $.perc_utils.alert_dialog({title: 'Error', content: result});
                                }
                                var sectionObj = {'CreateSiteSection' : {
                                        'pageName' : fileName,
                                        'pageTitle' : fields['section_name'],
                                        'templateId' : fields['template'],
                                        'pageUrlIdentifier' : fields['page_url'],
                                        'pageLinkTitle' : fields['section_name'],
                                        'folderPath': folderPath,
                                        'copyTemplates':false,
                                        'sectionType':fields['perc-section-type']} };
                                $.Perc_SectionServiceClient.create(sectionObj, function(status, data){
                                    self.addNewSectionCallback(status, data, evt);});
                            });

                        }
                        else if(fields['perc-section-type'] == "externallink")
                        {
                            var sectionObj = {'CreateExternalLinkSection' : {
                                    'externalUrl' : fields['perc-external-link-url'],
                                    'linkTitle' : fields['perc-external-link-text'],
                                    'folderPath': folderPath,
                                    'sectionType':fields['perc-section-type']} };
                            $.Perc_SectionServiceClient.create(sectionObj,  function(status, data){
                                self.addNewSectionCallback(status, data, evt);});
                        }
                        else if(fields['perc-section-type'] == "sectionlink")
                        {
                            var sid = fields['perc-section-link-targetid'];
                            if(self.isChild(evt.data.parentSectionId,sid))
                            {
                                self.displayDuplicateSectionMessage();

                                return;
                            }
                            else
                            {
                                $.Perc_SectionServiceClient.createSectionLink(fields['perc-section-link-targetid'], evt.data.parentSectionId,  function(status, data){
                                    self.addNewSectionCallback(status, data, evt);});
                            }
                        }
                        else if(fields['perc-section-type'] == "convertfolder")
                        {
                            var sectionObj = {'CreateSectionFromFolderRequest' : {
                                    'sourceFolderPath' : fields['perc-convert-folder-path'],
                                    'pageName' : fields['perc-landing-page'],
                                    'parentFolderPath': folderPath,
                                    'sectionType':fields['perc-section-type']} };
                            $.Perc_SectionServiceClient.convertFolder(sectionObj,  function(status, data){
                                self.addNewSectionCallback(status, data, evt);});
                        }
                    }

                });
            },

            /**
             * Helper function to handle the refreshing of the map after the section has been created.
             * @param status expects a String object with $.PercServiceUtils.STATUS_XXX value.
             * @param data with SiteSection object.
             * @param evt (Object) the jQuery event object. The following object is available as data.
             * {"level": level, "parentSectionId": parent Section Id, "context": this class object}
             */
            addNewSectionCallback : function(status, data, evt)
            {
                var self = evt.data.context;
                var $parentSection = $("#" + evt.data.parentSectionId);
                var folderPath = $parentSection.metadata({type: 'attr', name: 'data'}).folderPath;
                if(status == $.PercServiceUtils.STATUS_SUCCESS)
                {
                    var temp = $parentSection.metadata({type: 'attr', name: 'data'});
                    temp.childIds = self.convertCXFArray(temp.childIds);
                    self.addSectionToLevel(self.createSection(data.SiteSection,
                        evt.data.level, evt.data.parentSectionId, temp.childIds.length), evt.data.level);
                    // Update child list in parent
                    temp.childIds.push(data.SiteSection.id);
                    $parentSection.attr("data", JSON.stringify(temp));
                    var $pagecount = $parentSection.find(".perc-site-map-pagecount span");
                    var count = parseInt($pagecount.text()) + 1;
                    $pagecount.attr("value",count).text(count);
                    if(count == 1)
                    {
                        // Add bottom line
                        var $bottomLine = $("<div></div>")
                            .addClass("perc-site-map-node-bottomline").append(
                                $("<img/>").attr("src", self.getImageSrc(self.LINE_IMAGE))
                                    .attr("width", "2")
                            ).hide();
                        $parentSection.append($bottomLine);
                    }

                    $.Perc_SectionServiceClient.clearCache("getChildren");
                    self.handleMoveActionItemState();
                    self.layoutAll();
                    self.fireOnChange();   // refresh finder
                    $.unblockUI();

                }
                else
                {
                    $.unblockUI();
                    $.perc_utils.alert_dialog({title: I18N.message("perc.ui.publish.title@Error"), content: data});
                }
            },

            /**
             * Helper function to retrieve all parents of the passed in section as
             * an array. The order will be from the root section down.
             * @param sectionid {String} the id of the section where we will start to get
             * the parent chain from. Cannot be <code>null</code> or empty.
             * @param results {Array} this is an aray object where the results are stored,
             * generally the caller will not pass this argument in, it is really for use by the
             * function when it calls itsel recursively.
             * @return array with all parents up to the root section. Never <code>null</code>, may
             * be empty.
             * @type Array
             */
            getSectionParentChain: function(sectionid, results)
            {
                if(typeof(results) == 'undefined' || results == null)
                    results = new Array();
                var $current  = $("#" + sectionid);
                var temp = $current.metadata({type: 'attr', name: 'data'});
                if(typeof(temp.parentId) != 'undefined'
                    && temp.parentId != null && temp.parentId.length > 0)
                {
                    this.getSectionParentChain(temp.parentId, results);
                    results.push(temp.parentId);
                }
                return results;
            },

            /**
             * Returns the path of the section from the root. Concats the section titles with forward slash.
             * @param sectionId, for which the parent path is needed, if not supplied returns an empty string.
             * @return path(String) from the root. Not null.
             *
             */
            getParentPath: function(sectionId)
            {
                if(!sectionId)
                    return "";
                var parentArray = this.getSectionParentChain(sectionId);
                var ppath = "";
                $.each(parentArray,function(){
                    ppath += "/" + $("#"+this).find(".perc-site-map-sectiontitle").attr("title");
                });
                return ppath;
            },

            /**
             * Helper function to return an array of all expanded section ids.
             * @return array of expanded section ids, never <code>null</code>, may be
             * empty.
             * @type Array
             */
            getExpansionState: function()
            {
                var expanded = new Array();
                $(".perc-site-map-expanded").each(function(){
                    expanded.push($(this).attr("id"));
                });
                return expanded;
            }


        }); // End widget


    function deleteSiteWithDirtyCheck() {
        var sitename = $(".perc-site-map-sitetitle").text();
        $.PercBlockUI();
        $.perc_pagemanager.delete_site( sitename,function(){
                dialog.remove();
                var sitename = $(".perc-site-map-sitetitle").text();
                var eventData = {type: 'site', name: sitename};
                var finder = $.perc_finder();
                finder.fireActionEvent(finder.ACTIONS.DELETE, eventData);
                setTimeout(function(){
                        $.PercDirtyController.setDirty(false);
                        $.PercNavigationManager.goToLocation(
                            $.PercNavigationManager.getView(), null, null, null, null, null,null);
                    },
                    200);
            },
            function(result) {
                $.unblockUI();
                site_delete_handle_error(result);
            });
    };

    function site_delete_handle_error( data, textStatus, errorThrown) {
        var ut = $.perc_utils;
        var sitename = $(".perc-site-map-sitetitle").text();
        var warnOpenSpan = "<span id='perc-delete-warn-msg'>";
        var warnCloseSpan = "</span>";
        var id = "perc-finder-delete-error-open";
        var title = I18N.message("perc.ui.site.map@Delete Site");
        var defMessage = $.PercServiceUtils.extractDefaultErrorMessage(data)
        if( data.responseText.indexOf("site.isPublishing") > 0 ) {
            ut.alert_dialog( {
                id: id,
                title: title,
                content: warnOpenSpan + I18N.message( "perc.ui.deletesiteedialog.warning@SiteTag") + " " + sitename + " " + I18N.message( "perc.ui.deletesiteedialog.warning@SitePublishing" ) + warnCloseSpan
            });
        }
        else if (defMessage != "") {
            ut.alert_dialog( {
                id: id,
                title: title,
                content: warnOpenSpan + defMessage + warnCloseSpan
            })
        }
        else {
            ut.alert_dialog( {
                id: id,
                title: title,
                content: warnOpenSpan + I18N.message( "perc.ui.deletesiteedialog.warning@GenericText") + warnCloseSpan
            });
        }
        dialog.remove();
    }

})(jQuery); //End closure
