//TODO: I18N Needs applied.
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

        function windowItemPreProcessor() {
            windowArray = [];
            windowArray.push({name: 'text', type: 'input', label: 'Text to display'});
            windowArray.push({name: 'href', type: 'urlinput', filetype: 'file', size: 40, autofocus: true, label: 'URL'});
            anchorList = buildAnchorListControl(data.href);
            if(anchorList && anchorList.items.length > 0) {
                anchorList.items.unshift({text: 'None', value: ''});
                windowArray.push(anchorList);
            }
            if(linkListCtrl && linkListCtrl.items.length > 0) {
                windowArray.push(linkListCtrl);
            }
            if(relListCtrl && relListCtrl.items.length > 0) {
                windowArray.push(relListCtrl);
            }
            if(targetListCtrl && targetListCtrl.items.length > 0) {
                windowArray.push(targetListCtrl);
            }
            windowArray.push({name: 'title', type: 'input', label: 'Title'});
            windowArray.push({name: 'aria', type: 'input', label: 'Aria Label'});
            return windowArray;
        }

        function linkListChangeHandler(e) {
            var textCtrl = win.find('#text');

            if (!textCtrl.value() || (e.lastControl && textCtrl.value() === e.lastControl.text())) {
                textCtrl.value(e.control.text());
            }

            win.find('#href').value(e.control.value());
        }

        function buildLinkList() {
            var linkListItems = [{text: 'None', value: ''}];

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
            var relListItems = [{text: 'None', value: ''}];

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
            var targetListItems = [{text: 'None', value: ''}];

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
                        selected: url.value.indexOf('#' + id) !== -1
                    });
                }
            });

            return {
                    name: 'anchor',
                type: 'selectbox',
                label: 'Anchors',
                items: anchorList,
                size: 1,
                onChange: linkListChangeHandler
                };
            }



        selectedElm = selection.getNode();
        anchorElm = dom.getParent(selectedElm, 'a[href]');
        if (anchorElm) {
            selection.select(anchorElm);
        }

        data = {
            href: {},
        };

        data.text = initialText = selection.getContent({format: 'text'});
        data.href.value = anchorElm ? dom.getAttrib(anchorElm, 'href') : '';
        data.title = anchorElm ? dom.getAttrib(anchorElm, 'title') : '';
        data.target = anchorElm ? dom.getAttrib(anchorElm, 'target') : '';
        data.rel = anchorElm ? dom.getAttrib(anchorElm, 'rel') : '';
        data.aria = anchorElm ? dom.getAttrib(anchorElm, 'aria-label') : '';
        cm1LinkData.sys_dependentvariantid = anchorElm ? dom.getAttrib(anchorElm, 'sys_dependentvariantid') : '';
        cm1LinkData.rxinlineslot = anchorElm ? dom.getAttrib(anchorElm, 'rxinlineslot') : '';
        cm1LinkData.sys_dependentid = anchorElm ? dom.getAttrib(anchorElm, 'sys_dependentid') : '';
        cm1LinkData.inlinetype = anchorElm ? dom.getAttrib(anchorElm, 'inlinetype') : '';


        if (linkList) {
            linkListCtrl = {
                type: 'selectbox',
                label: 'Link list',
                items: buildLinkList(),
                onChange: linkListChangeHandler
            };
        }

        if (editor.settings.target_list !== false) {
            targetListCtrl = {
                name: 'target',
                type: 'selectbox',
                label: 'Target',
                items: buildTargetList(data.target)
            };
        }

        if (editor.settings.rel_list) {
            relListCtrl = {
                name: 'rel',
                type: 'selectbox',
                label: 'Rel',
                items: buildRelList(data.rel)
            };
        }

        windowItems = windowItemPreProcessor();

        win = editor.windowManager.open({
            title: 'Insert link',
            initialData: data,
            buttons: [
                {
                    type: 'cancel', // button type
                    name: 'cancel', // identifying name
                    text: 'Cancel', // text for the button
                    disabled: false, // button is active when the dialog opens
                    align: 'end' // align the button to the left of the dialog footer
                },
                {
                    type: 'submit', // button type
                    name: 'save', // identifying name
                    text: 'Save', // text for the button
                    disabled: false, // button is active when the dialog opens
                    align: 'end' // align the button to the left of the dialog footer
                }
            ],
            body: {
                type: 'panel',
                items: [...windowItems]
            },
            onSetup: function (api) {
                var validator = function(pathItem){
                    return pathItem && (pathItem.type === 'percPage' || pathItem.type === 'percImageAsset' || pathItem.type === 'percFileAsset')?null:'Please select a page, file, or an image';
                };

                jQuery('.tox-browse-url').click(function(){
                    var pathSelectionOptions = {
                        okCallback: updateLinkData,
                        dialogTitle: 'Select a page, file, or an image',
                        rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ALL,
                        initialPath: topFrJQ.cookie('perc-inlinelink-path'),
                        selectedItemValidator:validator,
                        acceptableTypes:'percPage,percImageAsset,percFileAsset,site,Folder'
                    };
                    topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions);
                });

            },
            onSubmit: function(e) {
                var data = e.getData();
                data.text = {};
                data.text.value = initialText;
                var linkPath = data.href.value;
                if (!linkPath) {
                    editor.execCommand('unlink');
                    e.close();
                    return;
                }

                //Inner function that adds the link.
                function addLink(extLink){
                    var anchorAttrs = {
                        href: data.href.value,
                            target: data.target ? data.target : null,
                            title: data.title ? data.title : null,
                            rel: data.rel ? data.rel : null,
                        'aria-label': data.aria ? data.aria : null,
                            sys_dependentvariantid : cm1LinkData.sys_dependentvariantid,
                            rxinlineslot : cm1LinkData.rxinlineslot,
                            sys_relationshipid : '',
                            sys_dependentid : cm1LinkData.sys_dependentid,
                            inlinetype : cm1LinkData.inlinetype,
                            'class': cm1LinkData.stateClass,
                        'data-jcrpath': cm1LinkData.jcrPath
                       };
                    var extAnchorAttrs = {
                        href: data.href.value,
                            target: data.target ? data.target : null,
                        title: data.title ? data.title : null,
                        'aria-label': data.aria ? data.aria : null
					};
                    if (anchorElm) {
                        editor.focus();
                        if(selectedElm.nodeName !== 'IMG'){
                            anchorElm.innerHTML = data.text.value;
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

                        selection.select(anchorElm);
                    } else {
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
                            topFrJQ.perc_utils.alert_dialog({
                                'title': 'Error',
                                'content': 'We were unable to create the link because this is an invalid URL. Please validate the URL and re-enter the link.'
                            });
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

                e.close();
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
                    topFrJQ.perc_utils.alert_dialog({'title':'Error', 'content':'Sorry for the inconvenience, could not get selected item details to insert link.'});
                    return;
                }
                var renderLink = data.InlineRenderLink;
                cm1LinkData.sys_dependentvariantid = renderLink.sys_dependentvariantid;
                cm1LinkData.stateClass = renderLink.stateClass;
                cm1LinkData.rxinlineslot = '103';
                cm1LinkData.sys_dependentid = renderLink.sys_dependentid;
                cm1LinkData.inlinetype = 'rxhyperlink';
                cm1LinkData.jcrPath = pathItem.path;
                win.find('#href').value(renderLink.url);
                win.find('#title').value(renderLink.title);
                if(topFrJQ.isFunction(callback)) {
                    callback(renderLink.url, renderLink.title);
                }
            });

        }

    }

    editor.ui.registry.addButton('link', {
        icon: 'link',
        tooltip: 'Insert/edit link',
        shortcut: 'Ctrl+K',
        onAction: createLinkList(showDialog),
        stateSelector: 'a[href]'
    });

    editor.addShortcut('Ctrl+K', '', createLinkList(showDialog));

    this.showDialog = showDialog;

    editor.ui.registry.addMenuItem('percadvlink', {
        icon: 'link',
        text: 'Insert link',
        shortcut: 'Ctrl+K',
        onAction: createLinkList(showDialog),
        stateSelector: 'a[href]',
        context: 'insert',
        prependToContext: true
    });
});
