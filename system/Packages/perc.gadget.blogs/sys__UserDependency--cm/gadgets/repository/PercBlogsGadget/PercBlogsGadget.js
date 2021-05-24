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
 * Implements Blogs Gadget for administering blogs, i.e., create, delete, etc.
 * 
 * @author Jose Annunziato
 */
(function ($) {

    var itemsPerPage;
    var tableDiv;
    var moduleID;
    var folderPath;
    var postsTemplateId;

    $.fn.PercBlogsGadget = function (siteName, rows, selectedBlogId, gadgetID) {

        itemsPerPage = rows;
        tableDiv = $(this);
        moduleID = gadgetID;
        // never show a scrollbar in the gadget
        $("body").css("overflow", "hidden");

        $("#perc-add-post-button")
            .on("click", function (event) {
                openNewPostDialog(event);
            });

        $("#perc-add-blog-button")
            .on("click", function (event) {
                $.PercBlogsGadget.showNewBlogDialog(event);
            });
        $('#perc-add-blog-button').attr('title', I18N.message("perc.ui.blogs.Gadget.title@text"));
        $.PercBlogsGadget.load(siteName, selectedBlogId);
    };

    $.PercBlogsGadget = {
        showNewBlogDialog: function (event) {
            percJQuery.PercNewBlogDialog.show();
            percJQuery.PercNewBlogDialog.moduleID = moduleID;
        },
        load: function (siteName, selectedBlogId) {

            if (selectedBlogId != "@null") {
                $.PercBlogService.getPostsForBlog(selectedBlogId, function (status, posts) {
                    if (status == $.PercServiceUtils.STATUS_SUCCESS)
                        $.PercBlogsGadget.updatePostsTable(posts.SiteBlogPosts);
                    else
                        PercMetadataService.save("perc.user." + percJQuery.PercNavigationManager.getUserName() + ".dash.page." + "0" + ".mid." + moduleID + "." + "selectedBlogID", "@null", function () { self.location.reload(); });
                });
            }
            else {
                if (!siteName)
                    siteName = "";
                $.PercBlogService.getBlogsForSite(siteName, function (status, blogs) {
                    if (status == $.PercServiceUtils.STATUS_SUCCESS)
                        $.PercBlogsGadget.updateBlogsTable(blogs.SiteBlogProperties);
                });
            }
        },
        updatePostsTable: function (posts) {
            folderPath = posts.blogSectionPath;
            postsTemplateId = posts.blogPostTemplateId;
            var gadgetTitle = "BLOGS: " + posts.blogTitle;
            gadgets.window.setTitle(gadgetTitle);

            var percData = [];
            var menus = [];
            var postsInfo = posts.posts;
            if (postsInfo != undefined) {
                postsInfo = $.makeArray(postsInfo);
                var postsText = postsInfo.length === 1 ? 'post' : 'posts';
                $('#perc-blogs-count span').text(postsInfo.length + ' ' + postsText);
                for (var b = 0; b < postsInfo.length; b++) {
                    var post = postsInfo[b];
                    var formattedDate = "";
                    var formattedTime = "";
                    if (post.postDate != undefined && post.postDate != "") {
                        var postDateParts = $.perc_utils.splitDateTime(post.postDate);
                        formattedDate = postDateParts.date;
                        formattedTime = postDateParts.time;
                    }

                    var pageId = post.id;
                    var pagePath = post.path;

                    var summary = $(post.summary).appendTo('<div />').parent().text();

                    if (summary == "" || summary == undefined)
                        summary = "&nbsp;";

                    var tags = "";
                    var tagsLine = "";
                    if (post.tags != undefined && post.tags.length > 0) {
                        tags = $.makeArray(post.tags).join(", ");
                        tagsLine = "<span class='tagsClass perc-ellipsis'>Tags: " + tags + "</span>";
                    }

                    var htmlTitleLine = '<span title="' + post.path + '" class="perc-callback">' + post.name + '</span>';
                    if (post.author != undefined && post.author != "") {
                        htmlTitleLine += '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <span style="color:grey; font-weight:bold;"> Author: ' + post.author + '</span>';
                    }

                    var commentLine = "<span class='perc-comments-commentsIcon'>&nbsp;</span> Comments (" + post.commentsCount + ") " + (post.newCommentsCount != undefined && post.newCommentsCount != 0 ? "<span class='perc-comments-newCount'>" + post.newCommentsCount + " New</span>" : "");
                    var row =
                    {
                        rowData: { pageId: pageId, pagePath: pagePath },
                        rowContent:
                            [
                                [
                                    { content: htmlTitleLine, title: post.path, callback: function (event) { previewPost(event); } },
                                    { content: summary, title: summary },
                                    { content: tagsLine, title: tags },
                                    { content: commentLine, title: "", callback: viewPostComments }
                                ],
                                [
                                    { content: formattedDate },
                                    { content: formattedTime }
                                ]
                            ]
                    };

                    percData.push(row);
                    menus.push($.PercPostMenuActions);
                }
            }
            var config = {
                percStayBelow: "#perc-add-post-button",
                additionalHeight: 20,
                additionalIframeHeight: 40, // being set to allow for extra space when adding the row-count to the blogs view window.
                percColumnWidths: ["*", "90"],
                aoColumns: [{ sType: "html" }, { sType: "date" }],
                iDisplayLength: itemsPerPage,
                percHeaders: ["Post Title", "Post Date"],
                percData: percData,
                percMenus: menus,
                oLanguage: { sZeroRecords: "No Posts Found. Click on the button above to create a new post" }
            };
            miniMsg.dismissMessage(loadingMsg);
            $("#perc-return-button").css("display", "block");
            var fancyTooltips = function () {
                $(tableDiv).find(".perc-index-0 > .perc-index-0 > span").each(function () {
                    $(this).PercTooltip();
                });
            };
            if (typeof (config.percTableRedrawCallback) !== "object") {
                if (typeof (config.percTableRedrawCallback) === "function") {
                    config.percTableRedrawCallback = [config.percTableRedrawCallback];
                }
                else {
                    config.percTableRedrawCallback = [];
                }
            }
            config.percTableRedrawCallback.push(fancyTooltips);
            tableDiv.PercActionDataTable(config);
            fancyTooltips();
        },

        updateBlogsTable: function (blogs) {
            var percData = [];
            var menus = [];
            for (var b = 0; b < blogs.length; b++) {
                var blog = blogs[b];
                var formattedDate = "";
                var formattedTime = "";
                if (blog.lastPublishDate != "") {
                    var lastPublishedDateParts = $.perc_utils.splitDateTime(blog.lastPublishDate);
                    formattedDate = lastPublishedDateParts.date;
                    formattedTime = lastPublishedDateParts.time;
                }
                var blogId = blog.id;
                var pageId = blog.pageId;
                var pagePath = blog.path;
                var summary = $(blog.description).appendTo('<div />').parent().text();
                if (summary == "")
                    summary = "&nbsp;";
                var postsLabel = (blog.blogPostcount == 1) ? "Post" : "Posts";

                var row = {
                    rowData: { blogId: blogId, pageId: pageId, pagePath: pagePath },
                    rowContent: [
                        [
                            {
                                content: blog.title,
                                title: blog.path,
                                callback: function (event) { previewBlog(event); }
                            },
                            {
                                content: summary,
                                title: blog.description
                            },
                            {
                                content: "<span class='perc-blog-post-link' style='font-weight: normal; cursor: pointer;' for='" + blog.title + "'>Open Blog</span>",
                                title: "",
                                callback: function (event) { openPosts(event); }
                            }
                        ],
                        [
                            {
                                content: formattedDate
                            },
                            {
                                content: formattedTime
                            }
                        ]
                    ]
                };
                percData.push(row);
                menus.push($.PercBlogMenuActions);
            }

            var config = {
                percStayBelow: "#perc-add-blog-button",
                percColumnWidths: ["*", "90"],
                aoColumns: [{ sType: "string" }, { sType: "date" }],
                iDisplayLength: itemsPerPage,
                percHeaders: ["Blog Title", "Published"],
                percData: percData,
                percMenus: menus,
                oLanguage: { sZeroRecords: "No Blogs Found. Click on the button above to create a new blog" }
            };
            miniMsg.dismissMessage(loadingMsg);

            var fancyTooltips = function () {
                $(tableDiv).find(".perc-index-0 > .perc-index-0 > span").each(function () {
                    $(this).PercTooltip();
                });
            };
            if (typeof (config.percTableRedrawCallback) !== "object") {
                if (typeof (config.percTableRedrawCallback) === "function") {
                    config.percTableRedrawCallback = [config.percTableRedrawCallback];
                }
                else {
                    config.percTableRedrawCallback = [];
                }
            }
            config.percTableRedrawCallback.push(fancyTooltips);

            tableDiv.PercActionDataTable(config);
            fancyTooltips();
        }
    };

    $.PercPostMenuActions = {
        title: "", menuItemsAlign: "left", stayInsideOf: ".dataTables_wrapper",
        items: [
            { label: "Open Post", callback: openPost },
            { label: "Preview Post", callback: previewPost },
            { label: "View Comments", callback: viewPostComments }
        ]
    };

    $.PercBlogMenuActions = {
        title: "", menuItemsAlign: "left", stayInsideOf: ".dataTables_wrapper",
        items: [
            { label: "Open Blog", callback: openBlog },
            { label: "Preview Blog", callback: previewBlog },
            { label: "View Posts", callback: openPosts }
        ]
    };

    function openBlog(event) {
        var pagePath = event.data.pagePath;
        percJQuery.PercNavigationManager.openPage(pagePath);
    }

    function previewBlog(event) {
        var pageId = event.data.pageId;
        percJQuery.perc_finder().launchPagePreview(pageId);
    }

    function openPosts(event) {
        var blogId = event.data.blogId;
        PercMetadataService.save(
            "perc.user." + percJQuery.PercNavigationManager.getUserName() + ".dash.page." + "0" + ".mid." + moduleID + "." + "selectedBlogID",
            blogId,
            function () { self.location.reload(); }
        );
    }

    function openPost(event) {
        var pagePath = event.data.pagePath;
        percJQuery.PercNavigationManager.openPage(pagePath);
    }

    function previewPost(event) {
        var pageId = event.data.pageId;
        percJQuery.perc_finder().launchPagePreview(pageId);
    }

    function viewPostComments(event) {
        var pagePath = "/" + event.data.pagePath.replace(/\/Sites\/.+?\//, "");
        var siteNameBlog = event.data.pagePath.replace(/\/Sites\//, "").replace(/\/.+/, "");
        jQuery.PercViewCommentsDialog.open(siteNameBlog, pagePath);
    }
    function openNewPostDialog() {
        var sendFolderPath = folderPath.substring(1, folderPath.length);
        percJQuery.PercFolderHelper().getAccessLevelByPath(sendFolderPath, false, function (status, result) {
            if (status == percJQuery.PercFolderHelper().PERMISSION_ERROR || result == percJQuery.PercFolderHelper().PERMISSION_READ) {
                percJQuery.perc_utils.alert_dialog({ title: 'Warning', content: "You do not have permission to create a post in this blog." });
                return;
            }
            else {
                // check for workflow perms as well
                percJQuery.PercUserService.getAccessLevel("percPage", -1, function (status, result) {
                    if (status == percJQuery.PercServiceUtils.STATUS_ERROR) {
                        percJQuery.perc_utils.alert_dialog({ title: 'Warning', content: result });
                        hasWritePermission = false;
                        return;
                    }
                    else if (status == percJQuery.PercServiceUtils.STATUS_SUCCESS && (result == percJQuery.PercUserService.ACCESS_READ || result == percJQuery.PercUserService.ACCESS_NONE)) {
                        percJQuery.perc_utils.alert_dialog({ title: 'Warning', content: "You are not authorized to create a blog post in this blog." });
                        hasWritePermission = false;
                        return;
                    }
                    else {
                        var templateId = postsTemplateId;
                        this.window.parent.jQuery.PercNewPageDialog().openDialog(sendFolderPath, templateId);
                        return;
                    }
                }, sendFolderPath);

            }
        });
    }

})(jQuery);
