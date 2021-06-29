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

tinymce.PluginManager.add('percadvlink', function(editor) {

    function createLinkList(callback) {
        return function() {
                var linkList = editor.settings.link_list;

                if (typeof(linkList) === 'string') {
                        tinymce.util.XHR.send({
                        url: linkList,
                        success: function(text) {
                            callback(tinymce.util.JSON.parse(text));
                        }
                    });
                } else {
                    callback(linkList);
                }
            };
        }

    function showDialog(linkList) {
        var data = {}, cm1LinkData = {}, selection = editor.selection, dom = editor.dom, selectedElm, anchorElm, initialText;
        var win, linkListCtrl, relListCtrl, targetListCtrl;
        var mainEditor = editor.contentWindow.parent;
        var topFrJQ = mainEditor.jQuery.topFrameJQuery;

        function linkListChangeHandler(e) {
            var textCtrl = win.find('#text');

            if (!textCtrl.value() || (e.lastControl && textCtrl.value() === e.lastControl.text())) {
                textCtrl.value(e.control.text());
            }

            win.find('#href').value(e.control.value());
        }

        function buildLinkList() {
            var linkListItems = [{text: I18N.message("perc.ui.widget.tincymce@None"), value: ''}];

            tinymce.each(linkList, function(link) {
                linkListItems.push({
                    text: link.text || link.title,
                    value: link.value || link.url,
                    menu: link.menu
                });
            });

            return linkListItems;
        }

        function buildRelList(relValue) {
            var relListItems = [{text: I18N.message("perc.ui.widget.tincymce@None"), value: ''}];

            tinymce.each(editor.settings.rel_list, function(rel) {
                relListItems.push({
                    text: rel.text || rel.title,
                    value: rel.value,
                    selected: relValue === rel.value
                });
            });

            return relListItems;
        }

        function buildTargetList(targetValue) {
            var targetListItems = [{text: I18N.message("perc.ui.widget.tincymce@None"), value: ''}];

            if (!editor.settings.target_list) {
                targetListItems.push({text: 'New window', value: '_blank'});
            }

            tinymce.each(editor.settings.target_list, function(target) {
                targetListItems.push({
                    text: target.text || target.title,
                    value: target.value,
                    selected: targetValue === target.value
                });
            });

            return targetListItems;
        }

        function buildAnchorListControl(url) {
            var anchorList = [];

            tinymce.each(editor.dom.select('a:not([href])'), function(anchor) {
                var id = anchor.name || anchor.id;

                if (id) {
                    anchorList.push({
                        text: id,
                        value: '#' + id,
                        selected: url.indexOf('#' + id) !== -1
                    });
                }
            });

            if (anchorList.length) {
                anchorList.unshift({text: I18N.message("perc.ui.widget.tincymce@None"), value: ''});

            return {
                    name: 'anchor',
                    type: 'listbox',
                    label: I18N.message("perc.ui.widget.tincymce@Anchors"),
                    values: anchorList,
                    onselect: linkListChangeHandler
                };
            }
        }



        selectedElm = selection.getNode();
        anchorElm = dom.getParent(selectedElm, 'a[href]');
        if (anchorElm) {
            selection.select(anchorElm);
        }

        data.text = initialText = selection.getContent({format: 'text'});
        data.href = anchorElm ? dom.getAttrib(anchorElm, 'href') : '';
        data.title = anchorElm ? dom.getAttrib(anchorElm, 'title') : '';
        data.target = anchorElm ? dom.getAttrib(anchorElm, 'target') : '';
        data.rel = anchorElm ? dom.getAttrib(anchorElm, 'rel') : '';
        cm1LinkData.sys_dependentvariantid = anchorElm ? dom.getAttrib(anchorElm, 'sys_dependentvariantid') : '';
        cm1LinkData.rxinlineslot = anchorElm ? dom.getAttrib(anchorElm, 'rxinlineslot') : '';
        cm1LinkData.sys_dependentid = anchorElm ? dom.getAttrib(anchorElm, 'sys_dependentid') : '';
        cm1LinkData.inlinetype = anchorElm ? dom.getAttrib(anchorElm, 'inlinetype') : '';


        if (linkList) {
            linkListCtrl = {
                type: 'listbox',
                label: I18N.message("perc.ui.widget.tinymce@Link list"),
                values: buildLinkList(),
                onselect: linkListChangeHandler
            };
        }

        if (editor.settings.target_list !== false) {
            targetListCtrl = {
                name: 'target',
                type: 'listbox',
                label: I18N.message("perc.ui.widget.tinymce@Target"),
                values: buildTargetList(data.target)
            };
        }

        if (editor.settings.rel_list) {
            relListCtrl = {
                name: 'rel',
                type: 'listbox',
                label: 'Rel',
                values: buildRelList(data.rel)
            };
        }

        win = editor.windowManager.open({
            title: I18N.message("perc.ui.widget.tinymce@Insert link"),
            data: data,
            body: [
                {
                    name: 'href',
                    type: 'filepicker',
                    filetype: 'file',
                    size: 40,
                    autofocus: true,
                    label: 'Url'
                },

                buildAnchorListControl(data.href),
                linkListCtrl,
                relListCtrl,
                targetListCtrl,

                {name: 'title', type: 'textbox', size: 40, label: I18N.message("perc.ui.widget.tinymce@Title")}
            ],
            onPostRender:function(){
                var validator = function(pathItem){
                    return pathItem && (pathItem.type === 'percPage' || pathItem.type === 'percImageAsset' || pathItem.type === 'percFileAsset')?null:'Please select a page, file, or an image';
                };

                jQuery('[aria-label=\'Insert link\']').find('.mce-btn.mce-open').on("click", function(){
                    var pathSelectionOptions = {
                        okCallback: updateLinkData,
                        dialogTitle: I18N.message("perc.ui.widget.tinymce@Please select"),
                        rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ALL,
                        initialPath: topFrJQ.cookie('perc-inlinelink-path'),
                        selectedItemValidator:validator,
                        acceptableTypes:'percPage,percImageAsset,percFileAsset,site,Folder'
                    };
                    topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions);
                });

            },
            onSubmit: function(e) {
                var data = e.data;
                data.text = initialText;
                var linkPath = data.href;
                if (!linkPath) {
                    editor.execCommand('unlink');
                    return;
                }

                //Inner function that adds the link.
                function addLink(extLink){
                    var anchorAttrs = {
                            href: data.href,
                            target: data.target ? data.target : null,
                            title: data.title ? data.title : null,
                            rel: data.rel ? data.rel : null,
                            sys_dependentvariantid : cm1LinkData.sys_dependentvariantid,
                            rxinlineslot : cm1LinkData.rxinlineslot,
                            sys_relationshipid : '',
                            sys_dependentid : cm1LinkData.sys_dependentid,
                            inlinetype : cm1LinkData.inlinetype,
                            'class': cm1LinkData.stateClass,
                            'data-jcrpath': cm1LinkData.jcrPath,
                            'data-pathitem': JSON.stringify(cm1LinkData.pathItem)
                       };
                    var extAnchorAttrs = {
							href: data.href,
                            target: data.target ? data.target : null,
                            title: data.title ? data.title : null
					};
                    if (anchorElm) {
                        editor.focus();
                        if(selectedElm.nodeName !== 'IMG'){
                        anchorElm.innerHTML = data.text;
                        }

                        if(extLink === 'yes'){
							var attrList = anchorElm.attributes;
							var i = attrList.length;
							while( i-- ){
							  anchorElm.removeAttributeNode(attrList[i]);
							}
							dom.setAttribs(anchorElm, extAnchorAttrs);
						} else {
							dom.setAttribs(anchorElm, anchorAttrs);
						}

                        if(anchorElm.target!=null && anchorElm.target!="" ){
                            anchorElm.rel="noopener noreferrer";
                        }
                        selection.select(anchorElm);
                    } else {

                        if(anchorAttrs.target!=null && anchorAttrs.target!="" ){
                            anchorAttrs.rel="noopener noreferrer";
                        }
                        editor.execCommand('mceInsertLink', !1, anchorAttrs);
                    }
                }

                //Resolve manually entered internal links
                if(linkPath.match('^//Sites/') || linkPath.match('^//Assets/') || linkPath.match('^/Sites/') || linkPath.match('^/Assets/') || linkPath.match('^Sites/') || linkPath.match('^Assets/')){
                    if(linkPath.match('^Sites/') || linkPath.match('^Assets/')) {
                        linkPath = '/' + linkPath;
                    }
                    else if(linkPath.match('^//Sites/') || linkPath.match('^//Assets/')) {
                        linkPath = linkPath.substring(1);
                    }
                    topFrJQ.PercPathService.getPathItemForPath(linkPath, function(status, result){
                        if(status === topFrJQ.PercServiceUtils.STATUS_ERROR){
                            topFrJQ.perc_utils.alert_dialog({"title":I18N.message("perc.ui.widget.tinymce@Error"), "content":I18N.message("perc.ui.widget.tinymce@Invalid Link Message")});
                        }
                        else{
                            updateLinkData(result.PathItem, function(url, title){
                                data.href = url;
                                data.title = title;
                                addLink('no');
                            });
                        }
                    });
                }
                else{
                	if(linkPath.match('^//Rhythmyx/') || linkPath.match('^/Rhythmyx/')){
						addLink('no');
					} else {
						addLink('yes');
					}
                }
            }
        });
        function updateLinkData(pathItem, callback)
        {
            //Save the path to cookie
            topFrJQ.cookie('perc-inlinelink-path', pathItem.path);
            topFrJQ.PercPathService.getInlineRenderLink(pathItem.id, function(status, data){
                if(!status)
                {
                    topFrJQ.perc_utils.info(data);
                    topFrJQ.perc_utils.alert_dialog({"title":I18N.message("perc.ui.widget.tincymce@Error"), "content":I18N.message("perc.ui.widget.tinymce@Could not get item details")});
                    return;
                }
                var renderLink = data.InlineRenderLink;
                cm1LinkData.sys_dependentvariantid = renderLink.sys_dependentvariantid;
                cm1LinkData.stateClass = renderLink.stateClass;
                cm1LinkData.rxinlineslot = '103';
                cm1LinkData.sys_dependentid = renderLink.sys_dependentid;
                cm1LinkData.inlinetype = 'rxhyperlink';
                cm1LinkData.jcrPath = pathItem.path;
                cm1LinkData.pathItem = pathItem;
                win.find('#href').value(renderLink.url);
                win.find('#title').value(renderLink.title);
                if(typeof callback === "function") {
                    callback(renderLink.url, renderLink.title);
                }
            });

        }

    }

    editor.addButton('link', {
        icon: 'link',
        tooltip: I18N.message("perc.ui.widget.tinymce@Insert Edit Link"),
        shortcut: 'Ctrl+K',
        onclick: createLinkList(showDialog),
        stateSelector: 'a[href]'
    });

    editor.addButton('unlink', {
        icon: 'unlink',
        tooltip: I18N.message("perc.ui.widget.tinymce@Remove links"),
        cmd: 'unlink',
        stateSelector: 'a[href]'
    });

    editor.addShortcut('Ctrl+K', '', createLinkList(showDialog));

    this.showDialog = showDialog;

    editor.addMenuItem('percadvlink', {
        icon: 'link',
        text: I18N.message("perc.ui.widget.tinymce@Insert link"),
        shortcut: 'Ctrl+K',
        onclick: createLinkList(showDialog),
        stateSelector: 'a[href]',
        context: 'insert',
        prependToContext: true
    });
});
