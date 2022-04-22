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
            var textCtrl = data.text;

            if (!textCtrl.value() || (e.lastControl && textCtrl.value() === e.lastControl.text())) {
                textCtrl.value(e.control.text());
            }
            data.href =e.control.value();
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
                onChange: linkListChangeHandler
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
            body: {
                type: 'panel', // root body panel
                items: [
                    { name: 'hrefPath',type: 'urlinput',filetype: 'file',size: 40,label: 'Url' },
                    { name: 'href', type: 'input', label: 'Url'},
                    { name: 'title', type: 'input',label: I18N.message("perc.ui.widget.tinymce@Title"),inputMode: 'text'},
                    { name: 'target', type: 'listbox',label: I18N.message("perc.ui.widget.tinymce@Target"), onChange: function(e){
                        data.target = e.control.value();
                        },
                        items: [
                            { text: 'Same Window', value: '_self' },
                            { text: 'New Window', value: '_blank' },
                            { text: 'Parent Window', value: '_parent' },
                            { text: 'Top Window', value: '_top' }
                        ]
                    }

                ]
            },
            buttons: [
                { type: 'cancel', text: 'Close' },
                { type: 'submit', text: 'Save', primary: true}
            ],
			initialData: data,

            onSubmit: function(e) {
                var linkPath = data.href;
                if (!linkPath) {
                    editor.execCommand('unlink');
                    return;
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
                            updateLinkData(result.PathItem);
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
				win.close();
            }
        });

        function updateLinkData(pathItem)
        {
            //Save the path to cookie
            topFrJQ.cookie('perc-inlinelink-path', pathItem.path);
            topFrJQ.PercPathService.getInlineRenderLink(pathItem.id, function(status, retData){

                if(!status)
                {
                    topFrJQ.perc_utils.info(retData);
                    topFrJQ.perc_utils.alert_dialog({"title":I18N.message("perc.ui.widget.tincymce@Error"), "content":I18N.message("perc.ui.widget.tinymce@Could not get item details")});
                    return;
                }
                var renderLink = retData.InlineRenderLink;
                cm1LinkData.sys_dependentvariantid = renderLink.sys_dependentvariantid;
                cm1LinkData.stateClass = renderLink.stateClass;
                cm1LinkData.rxinlineslot = '103';
                cm1LinkData.sys_dependentid = renderLink.sys_dependentid;
                cm1LinkData.inlinetype = 'rxhyperlink';
                cm1LinkData.jcrPath = pathItem.path;
                cm1LinkData.pathItem = pathItem;
                data.href = renderLink.url;
                data.title= renderLink.title;
                addLink('no');
                win.setData(data);
            });
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

        editor.addCommand('updateFileSelection', function (ui, selectedItem) {
            updateLinkData(selectedItem,null);
        });
    }

    editor.ui.registry.addButton('link', {
        icon: 'link',
        type: 'button',
        tooltip: I18N.message("perc.ui.widget.tinymce@Insert Edit Link"),
        shortcut: 'Ctrl+K',
        onAction: createLinkList(showDialog),
        stateSelector: 'a[href]'
    });

    editor.ui.registry.addButton('unlink', {
        icon: 'unlink',
        type: 'button',
        tooltip: I18N.message("perc.ui.widget.tinymce@Remove links"),
        onAction: function () {
            editor.execCommand('unlink');
        },
        stateSelector: 'a[href]'
    });

    editor.shortcuts.add('ctrl+k', '', createLinkList(showDialog));

    this.showDialog = showDialog;

    editor.ui.registry.addMenuItem('percadvlink', {
        icon: 'link',
        text: I18N.message("perc.ui.widget.tinymce@Insert link"),
        shortcut: 'Ctrl+K',
        onAction: createLinkList(showDialog),
        stateSelector: 'a[href]',
        context: 'insert',
        prependToContext: true
    });
});
