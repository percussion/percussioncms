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

(function ($) {
    $.perc_build_delete_page_button = function (mcol, content) {
        var ut = $.perc_utils;
        var finder = $.perc_finder();
        var mcol_path = ['', 'site'];
        var warnOpenSpan = "<span id='perc-delete-warn-msg'>";
        var warnCloseSpan = "</span>";
        var spec;

        var btn = $('<a id="perc-finder-delete" class="perc-font-icon icon-remove fas fa-trash" title="' + I18N.message("perc.ui.delete.page.button@Click Delete Page") + '"href="#" ></a>')
            .off("click")
            .perc_button()
            .on("click",function(evt){
                deleteFn(evt);
            });

        function deleteFn(evt) {
            var encodedPath = mcol_path;
            if ($.perc_utils.isPathUnderDesign(encodedPath)) {
                encodedPath = $.perc_utils.encodePathArray(mcol_path);
            }
            $.PercFolderHelper().getAccessLevelByPath(encodedPath.join('/'), false, function (status, result) {
                if (status === $.PercFolderHelper().PERMISSION_ERROR) {
                    $.perc_utils.alert_dialog({ title: I18N.message("perc.ui.publish.title@Error"), content: result });
                    mcol.refresh();
                    return;
                }
                else if (result == $.PercFolderHelper().PERMISSION_READ) {
                    $.perc_utils.alert_dialog({ title: I18N.message("perc.ui.page.general@Warning"), content: I18N.message("perc.ui.delete.page.button@Delete Permissions") });
                    return;
                }
                else {
                    handleDelete();
                }
            });
        }

        /**
         ** Displays appropriate dialog to user when an asset cannot be deleted
         ** @param data (String) - response string from service
         ** @param textStatus (String) - status string
         ** @param errorThrown (String) - error string
         **/
        function asset_delete_handle_error(data, textStatus, errorThrown) {
            var title = I18N.message("perc.ui.deleteassetdialog.title@Delete Asset");
            delete_handle_error(data, "asset", title, function () {
                var assetId = spec['PathItem']['id'];
				 if (mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
					purge_item(assetId, $.perc_paths.ASSET_PURGE, 'asset');
					return;
				}
                $.PercAssetService.forceDeleteAsset(assetId,
                    delete_success(assetId, 'asset'),
                    asset_delete_handle_error);
            });
        }


        /**
         ** Displays appropriate dialog to user when a page cannot be deleted
         ** @param data (String) - response string from service
         ** @param textStatus (String) - status string
         ** @param errorThrown (String) - error string
         **/
        function page_delete_handle_error(data, textStatus, errorThrown) {
            var title = I18N.message("perc.ui.deletepagedialog.title@Delete Page");
            delete_handle_error(data, "page", title, function () {
                var pageId = spec['PathItem']['id'];
                $.PercPageService.forceDeletePage(pageId,
                    delete_success(pageId, 'page'),
                    page_delete_handle_error);
            });
        }


        /**
         ** @param data (String) - response string from service
         ** @param type (String) - object type
         ** @param title (String) - dialog title
         **/
        function delete_handle_error(data, type, title, forceDeleteCallback) {
            var result = $.PercDeleteItemHelper.extractDeleteErrorMessage(data, spec['PathItem']['name'], type);
            if (result.canForceDelete) {
                if($("#perc-finder-delete-approved-ok").is(':visible')){
                    return;
                }
                showForceDeleteDialog(result.dialogid, title, result.content, result.chkBoxId, forceDeleteCallback);
            }
            else {
                ut.alert_dialog({
                    id: result.dialogid,
                    title: title,
                    content: warnOpenSpan + result.content + warnCloseSpan
                });
            }
        }

        function showForceDeleteDialog(id, title, content, chkBoxId, forceDeleteCallback) {
            var dia = $("<div/>").append(warnOpenSpan + content + warnCloseSpan).perc_dialog({
                id: id,
                title: title,
                success: function () {
                    if ($("#" + chkBoxId).length > 0) {
                        if ($("#" + chkBoxId).get(0).checked) {
                            forceDeleteCallback();
                        }
                    }
                },
                width: 500,
                resizable: false,
                percButtons: {
                    "Ok": {
                        id: 'perc-finder-delete-approved-ok',
                        cls: 'ui-state-disabled',
                        click: function () { }
                    },
                    "Cancel": {
                        id: 'perc-finder-delete-approved-cancel',
                        cls: 'perc-cancel',
                        click: function () { dia.remove(); }
                    }
                },
                open: function () {
                    $("#" + chkBoxId).on("click",function () {
                        if ($(this).get(0).checked) {
                            $("#perc-finder-delete-approved-ok").on("click",function () {
                                if ($("#" + chkBoxId).length > 0) {
                                    if ($("#" + chkBoxId).get(0).checked) {
                                        forceDeleteCallback();
                                        dia.remove();
                                    }
                                }
                            });
                            $("#perc-finder-delete-approved-ok").removeClass('ui-state-disabled');
                        }
                        else {
                            $("#perc-finder-delete-approved-ok").addClass('ui-state-disabled');
                        }
                    });
                }
            });

        }
        function delete_site() {
            $.PercBlockUI();
            $.perc_pagemanager.delete_site(spec['PathItem']['name'],
                function () {
                    dialog.remove();
                    var eventData = { type: 'site', name: spec['PathItem']['name'] };
                    finder.fireActionEvent(finder.ACTIONS.DELETE, eventData);
                    setTimeout(function () {
                            $.PercDirtyController.setDirty(false);
                            $.PercNavigationManager.goToLocation(
                                $.PercNavigationManager.getView(), null, null, null, null, null, null);
                        },
                        200);
                },
                function (result) {
                    $.unblockUI();
                    site_delete_handle_error(result);
                });
        }

        function delete_asset() {
            var assetId = spec['PathItem']['id'];
            if (mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                purge_item(assetId, $.perc_paths.ASSET_PURGE, 'asset');
                return;
            }
            $.PercAssetService.deleteAsset(
                assetId,
                function () {
                    delete_success(assetId, 'asset');
                },
                asset_delete_handle_error
            );
        }
		
        function handleDelete() {

            $.perc_pathmanager.open_path(ut.acop(mcol_path), false, function (specResponse) {

                spec = specResponse;

                // Here is where everything starts

                // if we are deleting a page
                if (spec['PathItem']['type'] == 'percPage') {

                    // we cant delete the landing page
                    if (spec['PathItem']['category'] == 'LANDING_PAGE') {
                        return;
                    }

                    // if we are not deleting the landing page
                    else {

                        // check with server if we can delete this page
                        $.PercPageService.validateDeletePage(spec['PathItem']['id'],

                            // if we can delete this page
                            function () {

                                // before we delete the page we need to:
                                // 1. Save the current template, page, or asset they might be working on if they are dirty
                                // 2. Delete the page
                                // 3. Reload the template, page, or asset they were working on so they can see the effects
                                //    of having deleted the page on the template, page or asset they were working on

                                // prepare confirm dialog asking if they really want to remove the page
                                checkIfLinkedPage(spec);
                            }, page_delete_handle_error);

                    }
                }

                // if we are deleting a folder
                else if (spec['PathItem']['type'] == 'Folder') {
                    if ((spec['PathItem']['category'] == 'FOLDER') ||
                        spec['PathItem']['category'] === 'SECTION_FOLDER' && mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                        function deleteFolder() {
                            // do not validate if deleting folders and user is Admin
                            if ($.PercNavigationManager.isAdmin()) {
                                $.PercPathService.deleteFolderSkipValidation(mcol_path.join('/'), mcol_path[mcol_path.length - 1], mcol_path[1], function (data) {
                                    cbDfSuccess(data);
                                });
                            }
                            else {
                                // call validation as usual
                                $.PercPathService.deleteFolder(mcol_path.join('/'), mcol_path[mcol_path.length - 1], mcol_path[1], cbDfSuccess);
                            }
                        }
                        if (spec['PathItem'].path.match("^/Sites/") || spec['PathItem'].path.match("^//Sites/")) {
                            $.PercRedirectHandler.createRedirect(spec['PathItem'].path, "", "folder").fail(function (errMsg) {
                                $.perc_utils.alert_dialog({
                                    title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"),
                                    content: errMsg,
                                    okCallBack: function () {
                                        deleteFolder();
                                    }
                                });
                            }).done(function () {
                                deleteFolder();
                            });
                        }
                        else {
                            deleteFolder();
                        }

                    }
                    else {
                        //not allowed
                        return;
                    }
                }

                // if we are deleting a theme folder or a theme file
                else if (spec['PathItem']['type'] == 'FSFolder') {
                    // manually encode the url for non-Ascii characters
                    var url = $.perc_utils.encodePathArray(mcol_path);

                    $.PercPathService.deleteFSFolder(url.join('/'), mcol_path[mcol_path.length - 1], cbDfSuccess);
                }

                else if (spec['PathItem']['type'] == 'FSFile') {
                    var url = "";
                    var paths = spec['PathItem']['path'].split("/");
                    paths = paths.slice(3, paths.length - 1);

                    $.each(paths, function (index, element) {
                        url = url + "/" + element;
                    });

                    // manually encode the url for non-Ascii characters
                    url = $.perc_utils.encodeURL(url);

                    $.PercWebResourcesService.deleteFile(url, mcol_path[mcol_path.length - 1], cbDfSuccess);
                }

                // if we are deleting an asset
                else {
                    // check with server if we can delete this asset
                    $.PercAssetService.validateDeleteAsset(
                        spec['PathItem']['id'],

                        // if we can delete this asset
                        function () {
							checkIfLinkedPageForAsset(spec);
                        }, asset_delete_handle_error
                    );

                }
            }, ut.show_error);
        }
		
		function getAssetDeleteQuestionString(spec,data){

            var message = mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH ? I18N.message("perc.ui.deleteassetdialog.purge@Confirm") : I18N.message("perc.ui.deleteassetdialog.warning@Confirm");
            var dialog = I18N.message("perc.ui.deleteassetdialog.tag@Asset") + ': ' + spec['PathItem']['name'] + "<br/><br/>"	+message;
            if (data != null && data.ArrayList != null && data.ArrayList.length > 0) {

                dialog = I18N.message("perc.ui.publish.question@Remove From Site") + "<br/><br/>";
                $.each(data.ArrayList, function (index, value) {
                    if (index > 9) {
                        return false;
                    }
                    dialog += value.pagePath + '<br />';
                });
            }
            return dialog;
        }
		
		function validateIfAssetCanBeDeleted(spec,data,takeDownUrl) {
		   // prepare confirm dialog asking if they really want to remove the asset
			var dirtyType = $.PercDirtyController.dirtyObjectType;
			var title = mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH ? I18N.message('perc.ui.deleteassetdialog.title@Recycle Asset') : I18N.message('perc.ui.deleteassetdialog.title@Delete Asset');
			var message = mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH ? I18N.message("perc.ui.deleteassetdialog.purge@Confirm") : I18N.message("perc.ui.deleteassetdialog.warning@Confirm");
			var options = {
				id: 'perc-finder-delete-confirm',
				title: title,
				question: warnOpenSpan + getAssetDeleteQuestionString(spec,data) + warnCloseSpan,

				success: function () {
					$.PercDirtyController.setDirty(false);
					takeDownPageAndDeleteAsset(takeDownUrl,data);
				},
				yes: I18N.message("perc.ui.deletepagedialog.title@Delete Page")
			};
			ut.confirm_dialog(options);
  
        }
		
		function takeDownPageAndDeleteAsset(takeDownUrl,data){
			 var serviceCallback = function(status, results){
			if(status == $.PercServiceUtils.STATUS_ERROR)
			{
				page_delete_handle_error(results.request,results.textstatus,results.error);
			}
			else
			{
				delete_asset();
			}
		 };

		$.PercServiceUtils.makeJsonRequest(takeDownUrl, $.PercServiceUtils.TYPE_PUT, true, serviceCallback, data);
	}

        /**
         * Delete folder success callback, simply refresh the finder.
         */
        function cbDfSuccess(data) {
            setTimeout(function () { mcol.refresh(); }, 200);
            var eventData = { type: 'folder' };
            finder.fireActionEvent(finder.ACTIONS.DELETE, eventData);
        }

        function getDeleteQuestionString(spec,data){

            var message = mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH ? I18N.message("perc.ui.deletepagedialog.purge@Confirm") : I18N.message("perc.ui.deletepagedialog.warning@Confirm");
            var dialog = I18N.message("perc.ui.deletepagedialog.tag@Delete Page") + ': ' + spec['PathItem']['name'] + "<br/><br/>"	+message;
            if (data != null && data.ArrayList != null && data.ArrayList.length > 0) {

                dialog = I18N.message("perc.ui.publish.question@Remove From Site") + "<br/><br/>";
                $.each(data.ArrayList, function (index, value) {
                    if (index > 9) {
                        return false;
                    }
                    dialog += value.pagePath + '<br />';
                });
            }
            return dialog;
        }

        function validateIfPageCanBeDeleted(spec,data,takeDownUrl) {
            var dirtyType = $.PercDirtyController.dirtyObjectType;
            var title = mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH ? I18N.message("perc.ui.deletepagedialog.title@Recycle Page") : I18N.message("perc.ui.deletepagedialog.title@Delete Page");
            var options = {
                id: 'perc-finder-delete-confirm',
                title: title,
                question: warnOpenSpan + getDeleteQuestionString(spec,data) + warnCloseSpan,
                success: function () {
					$.PercDirtyController.setDirty(false);
                    takeDownPageAndDeletePage(takeDownUrl,data,spec);
                },
                yes: I18N.message("perc.ui.deletepagedialog.title@Delete Page")
            };
            ut.confirm_dialog(options);
        }
		
		function takeDownPageAndDeletePage(takeDownUrl,data,spec){
			 var serviceCallback = function(status, results){
			if(status == $.PercServiceUtils.STATUS_ERROR)
			{
				page_delete_handle_error(results.request,results.textstatus,results.error);
			}
			else
			{
				delete_page(spec);
			}
		 };

		$.PercServiceUtils.makeJsonRequest(takeDownUrl, $.PercServiceUtils.TYPE_PUT, true, serviceCallback, data);
	}
        // recycles an item.  now calls purge_page() if path starts with /Recycling.
        function delete_page(spec) {
            if (mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH) {
                purge_item(spec.PathItem.id, $.perc_paths.PAGE_PURGE, 'page');
                return;
            }
            $.PercRedirectHandler.createRedirect(spec.PathItem.path, "", "page")
                .fail(function (errMsg) {
                    $.perc_utils.alert_dialog({
                        title: I18N.message("perc.ui.contributor.ui.adaptor@Redirect creation error"), content: errMsg, okCallBack: function () {
                            $.perc_pagemanager.delete_page(spec['PathItem']['id'],
                                function () {
                                    delete_success(spec['PathItem']['id'], 'page')
                                },
                                page_delete_handle_error
                            );
                        }
                    });
                })
                .done(function () {
                    $.perc_pagemanager.delete_page(spec['PathItem']['id'],
                        function () {
                            delete_success(spec['PathItem']['id'], 'page')
                        },
                        page_delete_handle_error);
                });
        }
        function purge_item(id, path, type) {
            $.PercRecycleService.purgeItem(
                id,
                path,
                function(status, data) {
                    if (status === $.PercServiceUtils.STATUS_ERROR) {
                        console.error('Error!');
                    } else {
                        delete_success(id, type);
                    }
                }
            );
        }

        function checkIfLinkedPage(spec) {
            var findLinkedItemsUrl = $.perc_paths.ITEM_LINKED_TO_ITEM + "/" + spec['PathItem']['id'];
            var takeDownUrl =  $.perc_paths.PAGE_TAKEDOWN ;
            takeDownUrl+="/" + spec['PathItem']['id'];

            $.PercServiceUtils.makeJsonRequest(findLinkedItemsUrl, $.PercServiceUtils.TYPE_GET, true, function(status, result) {
                if (status == $.PercServiceUtils.STATUS_ERROR) {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result);
                    console.error(defaultMsg);
                    // if there is an error, we proceed with previous behavior (no confirm display)
                    validateIfPageCanBeDeleted(spec,null,null);

                }
                else {
                    validateIfPageCanBeDeleted(spec,result.data,takeDownUrl);

                }
            }, null);

        }
		
		function checkIfLinkedPageForAsset(spec) {
            var findLinkedItemsUrl = $.perc_paths.ITEM_LINKED_TO_ITEM + "/" + spec['PathItem']['id'];
            var takeDownUrl =  $.perc_paths.PAGE_TAKEDOWN ;
            takeDownUrl+="/" + spec['PathItem']['id'];

            $.PercServiceUtils.makeJsonRequest(findLinkedItemsUrl, $.PercServiceUtils.TYPE_GET, true, function(status, result) {
                if (status == $.PercServiceUtils.STATUS_ERROR) {
                    var defaultMsg = $.PercServiceUtils.extractDefaultErrorMessage(result);
                    console.error(defaultMsg);
                    // if there is an error, we proceed with previous behavior (no confirm display)
                    validateIfAssetCanBeDeleted(spec,null,null);

                }
                else {
                    validateIfAssetCanBeDeleted(spec,result.data,takeDownUrl);

                }
            }, null);

        }

        /**
         * Handles cleanup after an item is successfully deleted.
         *
         * @param id the id of the item.
         */
        function delete_success(id, type) {
            setTimeout(function () { mcol.refresh() }, 200);
            var isOpen = false;
            if (id == $.PercNavigationManager.getId()) {
                isOpen = true;
                content.clear();
            }

            var eventData = { type: type, id: id, isOpen: isOpen };
            finder.fireActionEvent(finder.ACTIONS.DELETE, eventData);

            // Refreshes the pages in the design manager carousel
            $("form.perc-template-pages-controls").trigger("submit");
            var clickEvent = jQuery.Event("click");
            clickEvent.deletedPageId = id;
            $(".resetPaging").trigger(clickEvent);

            // Reload the current view if it is not VIEW_EDITOR, without pageId (deleted) in memento.
            var view = $.PercNavigationManager.getView();
            if (type == 'page' && view != $.PercNavigationManager.VIEW_EDITOR) {
                page_deleted(view);
            }
        }
        /**
         * Handles redirect if after deleting a page, the current view is not VIEW_EDITOR
         *
         * @param view the view to redirect to.
         */
        function page_deleted(view) {
            if (typeof memento != 'undefined') {
                var mem = { 'templateId': memento.templateId, 'pageId': null };
                // Use the PercNavigationManager to reload to the template editor without pageId
                var querystring = $.deparam.querystring();
                $.PercNavigationManager.goToLocation(
                    view,
                    querystring.site,
                    null,
                    null,
                    null,
                    querystring.path,
                    null,
                    mem);
            }
        }

        /**
         * Update the button state based on the current selection.
         * @param path {string} the path of the selected item, cannot be <code>null</code>.
         */
        function update_btn(path) {
            mcol_path = path;
            // Unfortunately need to call path manager to get info about
            // the node.
            $.perc_pathmanager.open_path(
                ut.acop(path),
                false,
                function (spec) {
                    var type = spec['PathItem'].type;
                    var cat = spec['PathItem'].category;
                    var disable = (typeof (cat) != 'undefined')
                        && (cat == 'LANDING_PAGE'
                            || (cat == 'SECTION_FOLDER' && mcol_path[1] !== $.perc_paths.RECYCLING_ROOT_NO_SLASH)
                            || cat == 'SYSTEM');
                    if (type == 'Folder' && spec['PathItem'].accessLevel != $.PercFolderHelper().PERMISSION_ADMIN)
                        disable = true;
                    else if ((cat == 'ASSET' || cat == 'PAGE') && spec['PathItem'].accessLevel == $.PercFolderHelper().PERMISSION_READ)
                        disable = true;

                    // Story CM-79: if the type is FSFile or FSFolder, the cat will still be SYSTEM, so we need to recheck
                    if ((type == "FSFolder" || type == "FSFile") && mcol_path.length >= 5
                        && spec['PathItem'].accessLevel != $.PercFolderHelper().PERMISSION_ADMIN) {
                        disable = false;
                    }

                    if (mcol_path[1] === $.perc_paths.RECYCLING_ROOT_NO_SLASH && mcol_path.length <= 3) {
                        disable = true;
                    }

                    if (!disable && mcol_path.length > 2) {
                        if (type === 'site') {
                            $(".perc-finder-menu #perc-finder-delete").removeClass('ui-enabled').addClass('ui-disabled').off('click');
                        }
                        else {
                            $(".perc-finder-menu #perc-finder-delete").removeClass('ui-disabled').addClass('ui-enabled').off('click').on('click',
                                function(evt){
                                deleteFn(evt);
                                });
                        }
                    }
                    else {
                        $(".perc-finder-menu #perc-finder-delete").removeClass('ui-enabled').addClass('ui-disabled').off('click');
                    }
                },
                function (request) {
                    var msg = $.PercServiceUtils.extractDefaultErrorMessage(request);
                    $.perc_utils.alert_dialog({ title: I18N.message("perc.ui.publish.title@Error"), content: msg });
                }
            );

        }

        mcol.addPathChangedListener(update_btn);

        return btn;
    };
})(jQuery);
