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

//TODO: I18N
/*global tinymce:true */

tinymce.PluginManager.add('percadvimage', function(editor) {
    function showDialog() {
        var win, data = {}, cm1LinkData = {}, dom = editor.dom, imgElm = editor.selection.getNode();
        var width, height, imageListCtrl;
        var mainEditor = editor.contentWindow.parent;
        var topFrJQ = mainEditor.jQuery.topFrameJQuery;
        var isUpgradeScenario = false;

        function buildImageList() {
            var linkImageItems = [{text: 'None', value: ''}];

            tinymce.each(editor.settings.image_list, function(link) {
                linkImageItems.push({
                    text: link.text || link.title,
                    value: link.value || link.url,
                    menu: link.menu
                });
            });

            return linkImageItems;
        }

        function setReadWriteMode(e) {

            var url = win.find('#src')[0].state.data.value;
            var description = win.find('#alt');
            var title = win.find('#title');
            var datadescriptionoverride = win.find('#data-description-override');
            var datatitleoverride = win.find('#data-title-override');
            var datadecorativeoverride = win.find('#data-decorative-override');
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataconstrain = win.find('#data-constrain');
            var height = win.find('#height');
            var width = win.find('#width');

            // check for upgrade scenario.  here we need to see if customers have previously 
            // set overrides and enable the checkboxes for those if previously set.
            // data-description-override was not ever used prior to current patch 1/24/2018
            // so we can use these attributes to detect if they are upgrading
            if (imgElm && !cm1LinkData.title) {
                if(!imgElm.getAttribute('data-description-override') || !imgElm.getAttribute('data-title-override')){
                    isUpgradeScenario = true;
                }
                getImageData('16777215-101-' + sys_dependentid, true);
            }

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                description.removeClass('disabled');
                title.removeClass('disabled');
                description.disabled(false);
                title.disabled(false);
            } 
            else {
                updateDecorativeImage(null);
                if(!dataconstrain.checked()) {
                    width.disabled(true);
                    height.disabled(true);
                }
            }
        }
        function updateDecorativeImage(e) {
            var url = win.find('#src')[0].state.data.value;
            var datadecorativeoverride = win.find('#data-decorative-override');
            var description = win.find('#alt');
            var title = win.find('#title');
            var titleOverride = win.find('#data-title-override');
            var descriptionOverride = win.find('#data-description-override');
            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                description.removeClass('disabled');
                title.removeClass('disabled');
                description.disabled(false);
                title.disabled(false);
            }
            else {
                if(datadecorativeoverride.checked()){
                    description.disabled(true);
                    description.addClass('disabled');
                    title.addClass('disabled');
                    title.disabled(true);
                    win.find('#alt').value('');
                    win.find('#title').value('');
                    titleOverride[0].checked(false);
                    titleOverride[0].disabled(true);
                    descriptionOverride[0].checked(false);
                    descriptionOverride[0].disabled(true);
                 }else {
                    titleOverride[0].disabled(false);
                    descriptionOverride[0].disabled(false);
                }
                if (e !== null) {
                    setReadWriteModeAlt(null);
                    setReadWriteModeTitle(null);
                }

            }
        }

        function setReadWriteModeAlt(e) {
            var url = win.find('#src')[0].state.data.value;
            var description = win.find('#alt');
            var title = win.find('#title');
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var datadescriptionoverride = win.find('#data-description-override');
            var datapreviousaltoverride = win.find('#data-previous-alt-override').value();

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                description.removeClass('disabled');
                title.removeClass('disabled');
                description.disabled(false);
                title.disabled(false);
            } 
            else {
                if(!datadescriptionoverride.checked()) {
                    description.addClass('disabled');
                    description.disabled(true);
                    if(url !== '') {
                        getImageData('16777215-101-' + sys_dependentid, true);
                    }
                }
                else {
                     description.removeClass('disabled');
                     description.disabled(false);
                    if(datapreviousaltoverride !== '') {
                        win.find('#alt').value(datapreviousaltoverride);
                    }
                }
                setReadWriteMode(null);
            }
        }

        function setReadWriteModeTitle(e) {
            var title = win.find('#title');
            var url = win.find('#src')[0].state.data.value;
            var description = win.find('#alt');
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataprevioustitleoverride = win.find('#data-previous-title-override').value();
            var datatitleoverride = win.find('#data-title-override');

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                description.removeClass('disabled');
                title.removeClass('disabled');
                description.disabled(false);
                title.disabled(false);
            } 
            else {
                if(!datatitleoverride.checked()) {
                    title.addClass('disabled');
                    title.disabled(true);
                    if(url !== '') {
                        getImageData('16777215-101-' + sys_dependentid, true);
                    }
                } 
                else {
                     title.removeClass('disabled');
                     title.disabled(false);
                    if(dataprevioustitleoverride !== '') {
                        win.find('#title').value(dataprevioustitleoverride);
                    }
                }
                setReadWriteMode(null);
            }
        }

        function resetAriaBoxes() {

            var description = win.find('#alt');
            var title = win.find('#title');
            var datadecorativeoverride = win.find('#data-decorative-override');
            var width = win.find('#width');
            var height = win.find('#height');

            if (description && description.value().trim() !== '') {
                $('.mce-perc-description').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-description').attr('role', '');
                $('.mce-perc-description').attr('aria-invalid', 'false');
            }

            if (title && title.value().trim() !== '') {
                $('.mce-perc-title').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-title').attr('role', '');
                $('.mce-perc-title').attr('aria-invalid', 'false');
            }


            if (width && width.value().trim() !== '') {
                $('.mce-perc-width').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.width').attr('role', '');
                $('.mce-perc-width').attr('aria-invalid', 'false');
            }

            if (height && height.value().trim() !== '') {
                $('.mce-perc-height').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-height').attr('role', '');
                $('.mce-perc-height').attr('aria-invalid', 'false');
            }

        }


        function recalcSize(e) {
            var widthCtrl, heightCtrl, newWidth, newHeight;

            widthCtrl = win.find('#width')[0];
            heightCtrl = win.find('#height')[0];

            newWidth = widthCtrl.value();
            newHeight = heightCtrl.value();

            if(typeof  proportion === 'undefined'){
                proportion = 1;
            }
            if(e.control === widthCtrl && isNaN(newWidth)){
                widthCtrl.value(width);
              }
            if(e.control === heightCtrl && isNaN(newHeight)){
                heightCtrl.value(height);
            }

            if (win.find('#data-constrain')[0].checked()){
                if (newWidth && newHeight) {

                    heightCtrl.disabled(false);
                    widthCtrl.disabled(false);

                    if (e.control === widthCtrl) {
                        newHeight = Math.round(newWidth / proportion);
                        if (newHeight === 0) {
                            newHeight = 1;
                        }

                        heightCtrl.value(newHeight);
                    } else {
                        newWidth = Math.round(proportion * newHeight);
                        if (newWidth === 0) {
                            newWidth = 1;
                        }
                        widthCtrl.value(newWidth);

                    }
                }
            }else{
                 heightCtrl.disabled(true);
                 widthCtrl.disabled(true);
            }
            width = newWidth;
            height = newHeight;
        }

        function updateImage() {
            if(cm1LinkData.thumbUrl) {
                setSrcAndSize();
            }
            else if(cm1LinkData.sys_dependentid) {
                getImageData('-1-101-' + cm1LinkData.sys_dependentid, false);
            }
        }
        function setSrcAndSize() {
            //handle thumb url and switch from thumb to full
            if(win.find('#data-imgtype').value() === '_thumb') {
                win.find('#src').value(cm1LinkData.thumbUrl);
                cm1LinkData.sys_dependentvariantid = cm1LinkData.thumbVarId;
            }
            else{
                win.find('#src').value(cm1LinkData.fullUrl);
                cm1LinkData.sys_dependentvariantid = cm1LinkData.fullVarId;
            }
            var inlineImage = new Image();
            inlineImage.onload = function() {
                if(!width && !height && !proportion) {
                    win.find('#width').value(this.width);
                    win.find('#height').value(this.height);
                    width = this.width;
                    height = this.height;
                    proportion = width / height;
                }
            };

            //setReadWriteMode(null);
            inlineImage.src = win.find('#src').value();
        }
        function getImageData(itemId, updateTitle, callback) {

            topFrJQ.PercPathService.getPathItemById(itemId, function(idstatus, pathItem){

                if(!idstatus) {
                    topFrJQ.perc_utils.info(pathItem);
                    topFrJQ.perc_utils.alert_dialog({'title':'Error', 'content':'Sorry for the inconvenience, could not get selected item details to insert image.'});
                    return;
                }

            topFrJQ.PercPathService.getInlineRenderLink(itemId, function(status, data) {
                if(!status) {
                    topFrJQ.perc_utils.info(data);
                    topFrJQ.perc_utils.alert_dialog({'title':'Error', 'content':'Sorry for the inconvenience, could not get selected item details to insert image.'});
                    return;
                }
                
                var renderLink = data.InlineRenderLink;
                cm1LinkData.rxinlineslot = '104';
                cm1LinkData.sys_dependentid = renderLink.sys_dependentid;
                cm1LinkData.inlinetype = 'rximage';
                cm1LinkData.alt = renderLink.altText;
                cm1LinkData.title = renderLink.title;
                cm1LinkData.jcrpath = pathItem.PathItem.folderPaths[0].replace('/Folders/$System$/','') + "/" + pathItem.PathItem.name;
                cm1LinkData.pathItem = pathItem.PathItem;

                var currentaltoverride = win.find('#alt').value();
                var currenttitleoverride = win.find('#title').value();
                var previousoverride = win.find('#data-previous-alt-override').value();
                var previoustitleoverride = win.find('#data-previous-title-override').value();
                var datadecorative = win.find('#data-decorative-override');
                var titleOverride = win.find('#data-title-override');
                var descriptionOverride = win.find('#data-description-override');
                var width = win.find('#width');
                var height = win.find('#height');

                if(isUpgradeScenario) {
                    //updateTitle = false;
                    if((currentaltoverride !== renderLink.altText) && currentaltoverride !== '') {
                        // here if the override does not match the value of the asset
                        // we assume an override has been set and set the overrides
                        // checkboxes to true in addition to preserving the current override
                        imgElm.setAttribute('data-description-override', true);
                        if(previousoverride !== 'undefined' && previousoverride === '' && currentaltoverride !== 'undefined' && currentaltoverride === '') {
                            if(renderLink.altText !== 'undefined') {
                                win.find('#alt').value(renderLink.altText);
                            }
                        }
                        descriptionOverride.checked(true);
                        datadecorative.checked(false);
                        datadecorative.disabled(true);
                    }
                    else if(renderLink.altText) {
                        imgElm.setAttribute('data-description-override', false);
                    }

                    if((currenttitleoverride !== renderLink.title) && currenttitleoverride !== '') {
                        titleOverride.checked(true);
                        imgElm.setAttribute('data-title-override', true);
                        if(previoustitleoverride !== 'undefined' && previoustitleoverride === '' && currenttitleoverride !== 'undefined' && currenttitleoverride === '') {
                            if(renderLink.title !== 'undefined') {
                                win.find('#title').value(renderLink.title);
                            }
                        }
                    }
                    else {
                        imgElm.setAttribute('data-title-override', false);
                    }
                    isUpgradeScenario = false;
                }

                if(updateTitle)
                {
                    // if the title is not overridden, we use the value from the renderLink obj.
                    if(!titleOverride.checked() && renderLink.title && !datadecorative.checked()) {
                        win.find('#title').value(renderLink.title);
                    }
                    // if the alt is not override AND there is alt text present and its not a decorative (empty) alt
                    // we use the renderLink value.
                    if(!descriptionOverride.checked() && renderLink.altText && !datadecorative.checked()) {
                        win.find('#alt').value(renderLink.altText);
                    }

                    // the asset has no title text
                    if(!renderLink.title && currenttitleoverride !== '') {
                        win.find('#data-previous-title-override').value(currenttitleoverride);
                    }
                    // the override title text is different than the asset title text and override has been deselected
                    else if(currenttitleoverride !== renderLink.title && !datadecorative.checked() && currenttitleoverride !== '') {
                        win.find('#data-previous-title-override').value(currenttitleoverride);
                    }
                    // if the current override is empty and decorative is de-selected
                    else if(currenttitleoverride === '' && !datadecorative.checked()) {
                        win.find('#data-previous-title-override').value(previoustitleoverride);
                    }
                }

                // the asset has no alt text
                if(!renderLink.altText && currentaltoverride !== '') {
                    win.find('#data-previous-alt-override').value(currentaltoverride);
                }
                // the override alt text is different than the asset alt text and override has been deselected
                else if(currentaltoverride !== renderLink.altText && !datadecorative.checked() && currentaltoverride !== '') {
                    win.find('#data-previous-alt-override').value(currentaltoverride);
                }
                // if the current override is empty and decorative is de-selected
                else if(currentaltoverride === '' && !datadecorative.checked()) {
                    win.find('#data-previous-alt-override').value(previousoverride);
                }

                if(currenttitleoverride !== renderLink.title) {
                    win.find('#data-previous-title-override').value(currenttitleoverride);
                }

                cm1LinkData.thumbUrl = renderLink.thumbUrl;
                cm1LinkData.thumbVarId = renderLink.thumbsys_dependentvariantid;
                cm1LinkData.fullUrl = renderLink.url;
                cm1LinkData.fullVarId = renderLink.sys_dependentvariantid;

                if(cm1LinkData.thumbUrl === '') {
                    win.find('#data-imgtype').disabled(true);
                }

                setSrcAndSize();
                if(typeof callback === "function") {
                    callback(renderLink.url, renderLink.thumbUrl, renderLink.title);
                }
            });
            });
        }
        var isImage = imgElm.nodeName === 'IMG';
        width = data.width = isImage ? dom.getAttrib(imgElm, 'width'):'';
        height = data.height = isImage ? dom.getAttrib(imgElm, 'height'):'';
        proportion = width / height;
        data.dataimgtype = isImage ? dom.getAttrib(imgElm, 'data-imgtype') : '_full';
        data.dataconstrain = isImage ? (dom.getAttrib(imgElm, 'data-constrain') === 'true') : false;
        data.src = isImage ? dom.getAttrib(imgElm, 'src') : '';
        data.alt = isImage ? dom.getAttrib(imgElm, 'alt') : '';
        data.datadescriptionoverride = isImage ? (dom.getAttrib(imgElm, 'data-description-override') === 'true') : false;
        data.datapreviousaltoverride = isImage ? dom.getAttrib(imgElm, 'data-previous-alt-override') : '';
        data.dataprevioustitleoverride = isImage ? dom.getAttrib(imgElm, 'data-previous-title-override') : '';
        data.datadecorativeoverride = isImage ? (dom.getAttrib(imgElm, 'data-decorative-override') === 'true') : false;
        data.datatitleoverride = isImage ? (dom.getAttrib(imgElm, 'data-title-override') === 'true') : false;
        data.title = isImage ? dom.getAttrib(imgElm, 'title') : '';
        data.align = isImage ? dom.getStyle(imgElm, 'float') !== '' ? dom.getStyle(imgElm, 'float') : dom.getStyle(imgElm, 'vertical-align') : '';
        cm1LinkData.sys_dependentvariantid = isImage ? dom.getAttrib(imgElm, 'sys_dependentvariantid') : '';
        cm1LinkData.rxinlineslot = isImage ? dom.getAttrib(imgElm, 'rxinlineslot') : '';
        cm1LinkData.sys_dependentid = isImage ? dom.getAttrib(imgElm, 'sys_dependentid') : '';
        cm1LinkData.inlinetype = isImage ? dom.getAttrib(imgElm, 'inlinetype') : '';
        cm1LinkData.jcrpath = isImage ? dom.getAttrib(imgElm, 'data-jcrpath') : '';

        if (imgElm.nodeName === 'IMG' && !imgElm.getAttribute('data-mce-object')) {
        } else {
            imgElm = null;
        }

        if (editor.settings.image_list) {
            imageListCtrl = {
                name: 'target',
                type: 'listbox',
                label: 'Image list',
                values: buildImageList(),
                onselect: function(e) {
                    var altCtrl = win.find('#alt');

                    if (!altCtrl.value() || (e.lastControl && altCtrl.value() === e.lastControl.text())) {
                        altCtrl.value(e.control.text());
                    }

                    win.find('#src').value(e.control.value());
                }
            };
        }

        win = editor.windowManager.open({
            title: 'Edit image',
            data: data,
            body: [
                {name: 'src', type: 'filepicker', filetype: 'image', label: 'Source', autofocus: true, onkeyup: setReadWriteMode},
                {name: 'data-previous-alt-override', type: 'textbox', hidden: true, value: data.datapreviousaltoverride},
                {name: 'data-previous-title-override', type: 'textbox', hidden: true, value: data.dataprevioustitleoverride},
                {name: 'data-decorative-override',type: 'checkbox', checked: data.datadecorativeoverride, label: 'Decorative (WAI)', text: 'Decorative Image', onchange: updateDecorativeImage},
                imageListCtrl,
                {
                    type: 'container',
                    label: 'Image description*',
                    layout: 'flex',
                    direction: 'row',
                    align: 'center',
                    spacing: 5,
                    items: [
                        {name: 'alt', type: 'textbox', label: 'Image description', size: 26, classes: 'perc-description', onchange: resetAriaBoxes},
                        {type: 'checkbox', name: 'data-description-override', checked: data.datadescriptionoverride, label: 'override', text: 'Override', onchange: setReadWriteModeAlt},
                    ]
                },
                {
                    type: 'container',
                    label: 'Image title*',
                    layout: 'flex',
                    direction: 'row',
                    align: 'center',
                    spacing: 5,
                    items: [
                        {name: 'title', type: 'textbox', label: 'Image title', size: 26, classes: 'perc-title',onchange: resetAriaBoxes},
                        {type: 'checkbox', name: 'data-title-override', checked: data.datatitleoverride, label: 'override', text: 'Override', onchange: setReadWriteModeTitle},
                    ]
                },
                {
                    type: 'container',
                    label: 'Dimensions',
                    layout: 'flex',
                    direction: 'row',
                    align: 'center',
                    spacing: 5,
                    items: [
                        {name: 'width', type: 'textbox', maxLength: 3, size: 3, onkeyup: recalcSize, onchange: recalcSize},
                        {type: 'label', text: 'x'},
                        {name: 'height', type: 'textbox', maxLength: 3, size: 3, onkeyup: recalcSize, onchange: recalcSize},
                        {name: 'data-constrain', type: 'checkbox', checked: data.dataconstrain, text: 'Constrain proportions', onchange: recalcSize}
                    ]
                },
                {
                    name: 'data-imgtype',
                    type: 'listbox',
                    label: 'Image type',
                    minWidth: 90,
                    maxWidth:135,
                    onselect:updateImage,
                    values: [{text: 'Full', value: '_full'},{text: 'Thumbnail', value: '_thumb'}],
                    onPostRender: function() {
                        if(data.dataimgtype === '') {
                            data.dataimgtype = this._values[0].value;
                        }
                        this.value(data.dataimgtype);
                    }
                },
                {
                    label: 'Alignment',
                    minWidth: 90,
                    name: 'align',
                    type: 'listbox',
                    text: 'None',
                    maxWidth: null,
                    values: [
                        {text: 'None', value: ''},
                        {text: 'Baseline', value: 'baseline'},
                        {text: 'Top', value: 'top'},
                        {text: 'Middle', value: 'middle'},
                        {text: 'Bottom', value: 'bottom'},
                        {text: 'Text top', value: 'text-top'},
                        {text: 'Text bottom', value: 'text-bottom'},
                        {text: 'Left', value: 'left'},
                        {text: 'Right', value: 'right'}
                    ]
                }

            ],
                buttons : [
                    {text: 'Ok', subtype: 'primary', minWidth: 50, onclick: function(e) {
                        win.find('form')[0].submit();
                    }},
                    {text: 'Cancel', onclick: function() {
                        win.close();
                    }}
                ],
            onPostRender:function() {
                var openCreateImageDialog = function(successCallback, cancelCallback) {
                    $.topFrameJQuery.PercCreateNewAssetDialog('percImage', successCallback, cancelCallback);
                };
                var validator;
                validator = function (pathItem) {
                    return pathItem && pathItem.type === 'percImageAsset' ? null : 'Please select an image.';
                };
                jQuery('[aria-label=\'Edit image\']').find('.mce-btn.mce-open').on("click",function() {
                    var pathSelectionOptions = {
                        okCallback: updateLinkData,
                        dialogTitle: 'Select an image',
                        rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                        initialPath: topFrJQ.cookie('perc-inlineimage-path'),
                        selectedItemValidator:validator,
                        acceptableTypes:'percImageAsset,site,Folder',
                        createNew:{'label':'Upload', 'iconclass':'icon-upload-alt', 'onclick':openCreateImageDialog}
                    };
                    topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions);
                });
            },
            onOpen:function(e) {
                //initialize accessibility since tinymce currently doesn't allow it directly
                $('.mce-perc-description').attr('aria-invalid','false');
                $('.mce-perc-description').attr('aria-required','true');
                $('.mce-perc-title').attr('aria-required','true');
                $('.mce-perc-title').attr('aria-invalid','false');
                $('.mce-perc-width').attr('aria-required','true');
                $('.mce-perc-width').attr('aria-invalid','false');
                $('.mce-perc-height').attr('aria-required','true');
                $('.mce-perc-height').attr('aria-invalid','false');
                setReadWriteMode(e);
                setReadWriteModeAlt(null);
                setReadWriteModeTitle(null);
            },
            onSubmit: function(e) {

                resetAriaBoxes();
                var data = e.data;

                data['data-previous-alt-override'] = data['data-description-override'] ? data.alt : data['data-previous-alt-override'];
                data['data-previous-title-override'] = data['data-title-override'] ? data.title : data['data-previous-title-override'];
                if(!data['data-decorative-override']) {
                    if (data['data-description-override'] && (!data.alt || data.alt.trim() === '') ){
                        $('.mce-perc-description').css('border-color', 'red');
                        $('.mce-perc-description').attr('aria-invalid', 'true');
                        $('.mce-perc-description').attr('role', 'alert');
                        e.preventDefault();
                        return;
                    }
                    if (data['data-title-override'] && (!data.title || data.title.trim() === '')) {
                        $('.mce-perc-title').css('border-color', 'red');
                        $('.mce-perc-title').attr('aria-invalid', 'true');
                        $('.mce-perc-title').attr('role', 'alert');
                        e.preventDefault();
                        return;
                    }

                }
                if(isNaN(data.width) ){
                    $('.mce-perc-width').css('border-color', 'red');
                    $('.mce-perc-width').attr('aria-invalid', 'true');
                    $('.mce-perc-width').attr('role', 'alert');
                    e.preventDefault();
                    return;
                }
                if (isNaN(data.height)){
                    $('.mce-perc-height').css('border-color', 'red');
                    $('.mce-perc-height').attr('aria-invalid', 'true');
                    $('.mce-perc-height').attr('role', 'alert');
                    e.preventDefault();
                    return;
                }

                function addImage() {
                    if (data.width === '') {
                        delete data.width;
                    }

                    if (data.height === '') {
                        delete data.height;
                    }

                    /* If this is a decorative image, we need to set the value to empty.
                     * We do, however, need to preserve the original overridden alt
                     * text if there was some. This sends the empty alt attribute
                     * to TinyMCE for updating the HTML, however we restore that
                     * value when loading the editor the next time right after
                     * the jQuery.extend method below.
                     **/
                    var tempAltValue = data.alt;
                    if(data['data-decorative-override']) {
                        data.alt = '';
                    }

                    //Handle allign
                    if(imgElm) {
                        editor.dom.setStyles(imgElm,{'float':'', 'vertical-align':''});
                        if(data.align === 'left' || data.align === 'right') {
                            editor.dom.setStyle(imgElm, 'float', data.align);
                        }
                        else if(data.align !== '') {
                            editor.dom.setStyle(imgElm, 'vertical-align', data.align);
                        }
                    }
                    else{
                        if(data.align === 'left' || data.align === 'right') {
                            data.style = 'float:' + data.align + ';';
                        }
                        else if(data.align && data.align !== '') {
                           data.style = 'vertical-align:' + data.align + ';';
                        }
                    }
                    delete data.align;
                    var cm1ImgAttrs = {
                            sys_dependentvariantid : cm1LinkData.sys_dependentvariantid,
                            rxinlineslot : cm1LinkData.rxinlineslot,
                            sys_dependentid : cm1LinkData.sys_dependentid,
                            sys_relationshipid : '',
                            inlinetype : cm1LinkData.inlinetype,
                            'data-jcrpath' : cm1LinkData.jcrpath,
                            'data-pathitem' : JSON.stringify(cm1LinkData.pathItem)
                    };

                    /*
                     * This section does not work.  What is its purpose?
                     * The properties data.dataimgtype, for example, isn't accessible
                     * because it's an object and needs to be accessed via
                     * data['data-img-type'].  However, doing this prevents the
                     * features from being loaded the next edit b/c they've been deleted.
                     * Makes no sense.
                     **/
                    tdi = data.dataimgtype;
                    delete data.dataimgtype;

                    tdc = data.dataconstrain;
                    delete data.dataconstrain;

                    delete data.datadescriptionoverride;
                    delete data.datatitleoverride;
                    delete data.datadecorativeoverride;

                    jQuery.extend(data,cm1ImgAttrs);
                    if (imgElm) {
                        if (!imgElm.attributes.getNamedItem('style')) {
                        } else {
                            var style = {
                                style: imgElm.attributes.getNamedItem('style').value
                            };
                            jQuery.extend(data, style);
                        }

                        dom.removeAllAttribs(imgElm);
                        dom.setAttribs(imgElm, data);
                        editor.nodeChanged();
                    } else {
                        editor.insertContent(dom.createHTML('img', data));
                    }

                    data.dataimgtype = tdi;
                    data.dataconstrain = tdc;
                    data.alt = tempAltValue;
                }

                var imgPath = topFrJQ.trim(data.src),imgPathLower = imgPath.toLowerCase();
                //Resolve manually entered internal links
                if(imgPathLower.match('^//sites/') || imgPathLower.match('^//assets/') || imgPathLower.match('^/sites/') || imgPathLower.match('^/assets/') || imgPathLower.match('^sites/') || imgPathLower.match('^assets/')) {
                    if(imgPathLower.match('^sites/') || imgPathLower.match('^assets/')) {
                        imgPath = '/' + imgPath;
                    }
                    else if(imgPathLower.match('^//sites/') || imgPathLower.match('^//assets/')) {
                        imgPath = imgPath.substring(1);
                    }
                    topFrJQ.PercPathService.getPathItemForPath(imgPath, function(status, result) {
                        if(status === topFrJQ.PercServiceUtils.STATUS_ERROR || result.PathItem.type !== 'percImageAsset') {
                            topFrJQ.perc_utils.alert_dialog({'title':'Error', 'content':'We were unable to create the image because this is an invalid URL. Please validate the URL and re-enter the image.'});
                        }
                        else{
                            updateLinkData(result.PathItem, function(fullUrl, thumUrl, title) {
                                data.src = data.dataimgtype === '_thumb' ? thumUrl : fullUrl;
                                data.width = width;
                                data.height = height;

                                addImage();
                            });
                        }
                    });
                }
                else{
                    addImage();
                }

            }// end onSubmit()
            
        }); // end of win object initialization
        function updateLinkData(pathItem, callback) {
            //Save the path to cookie
            topFrJQ.cookie('perc-inlineimage-path', pathItem.path);
            getImageData(pathItem.id, true, callback);
        }
    }

    editor.addButton('image', {
        icon: 'image',
        tooltip: 'Insert/edit image',
        onclick: showDialog,
        stateSelector: 'img:not([data-mce-object])'
    });

    editor.addMenuItem('image', {
        icon: 'image',
        text: 'Insert image',
        onclick: showDialog,
        context: 'insert',
        prependToContext: true
    });
});
