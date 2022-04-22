
/*global tinymce:true */

tinymce.PluginManager.add('percadvimage', function(editor, url) {


    function showDialog(buttonAPI) {
        var win, formData = {}, cm1LinkData = {}, dom = editor.dom, imgElm = editor.selection.getNode();
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
            var url = formData.src[0].state.data.value;
            var description = formData.alt;
            var title = formData.title;
            var datadescriptionoverride = formData.isDataDescriptionOverride;
            var datatitleoverride = formData.isDataTitleOverride;
            var datadecorativeoverride = formData.isDataDecorativeOverride;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataconstrain = formData.constrain;
            var height = formData.dimensions.height;
            var width = formData.dimensions.width;

            // check for upgrade scenario.  here we need to see if customers have previously
            // set overrides and enable the checkboxes for those if previously set.
            // isDataDescriptionOverride was not ever used prior to current patch 1/24/2018
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
                if(!dataconstrain) {
                    width.disabled(true);
                    height.disabled(true);
                }
            }
        }
        function updateDecorativeImage(e) {
            var url = formData.src[0].state.data.value;
            var datadecorativeoverride = formData.isDataDecorativeOverride;
            var description = formData.alt;
            var title = formData.title;
            var isTitleOverride = formData.isDataTitleOverride;
            var descriptionOverride = formData.isDataDescriptionOverride;
            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                description.removeClass('disabled');
                title.removeClass('disabled');
                description.disabled(false);
                title.disabled(false);
            }
            else {
                if(datadecorativeoverride){
                    description.disabled(true);
                    description.addClass('disabled');
                    title.addClass('disabled');
                    title.disabled(true);
                    formData.alt= '';
                    formData.title='';
                    formData.isDataTitleOverride =false;
                    //@todo :titleOverride[0].disabled(true);
                    formData.isDescriptionOverride = false;
                    //@todo descriptionOverride[0].disabled(true);
                }else {
                    //@todo titleOverride[0].disabled(false);
                    //@todo descriptionOverride[0].disabled(false);
                }
                if (e !== null) {
                    setReadWriteModeAlt(null);
                    setReadWriteModeTitle(null);
                }

            }
        }

        function setReadWriteModeAlt(e) {
            var url = formData.src[0].state.data.value;
            var description = formData.alt;
            var title = formData.title;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var datadescriptionoverride = formData.isDataDescriptionOverride;
            var datapreviousaltoverride = formData.data-previous-alt-override;

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
                        formData.alt= datapreviousaltoverride;
                    }
                }
                setReadWriteMode(null);
            }
        }

        function setReadWriteModeTitle(e) {
            var title = formData.title;
            var url = formData.src[0].state.data.value;
            var description = formData.alt;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataprevioustitleoverride = formData.dataPreviousAltOverride;
            var datatitleoverride = formData.isDataTitleOverride;

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
                        formData.title=dataprevioustitleoverride;
                    }
                }
                setReadWriteMode(null);
            }
        }

        function resetAriaBoxes() {


            var description = formData.alt;
            var title = formData.title;
            var datadecorativeoverride = formData.isDataDecorativeOverride;
            var width = formData.dimensions.width;
            var height = formData.dimensions.height;

            if (description && description.trim() !== '') {
                $('.mce-perc-description').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-description').attr('role', '');
                $('.mce-perc-description').attr('aria-invalid', 'false');
            }

            if (title && title.trim() !== '') {
                $('.mce-perc-title').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-title').attr('role', '');
                $('.mce-perc-title').attr('aria-invalid', 'false');
            }


            if (width && width !== '') {
                $('.mce-perc-width').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.width').attr('role', '');
                $('.mce-perc-width').attr('aria-invalid', 'false');
            }

            if (height && height !== '') {
                $('.mce-perc-height').css({'border-color': '#c5c5c5', 'transition': 'border 2s ease'});
                $('.mce-perc-height').attr('role', '');
                $('.mce-perc-height').attr('aria-invalid', 'false');
            }

        }


        function recalcSize(e) {
            var widthCtrl, heightCtrl, newWidth, newHeight;

            widthCtrl = formData.dimensions.width;
            heightCtrl = formData.dimensions.height;

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

            if (formData.constrain){
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
            if(formData.dataImgtype === '_thumb') {
                formData.src = cm1LinkData.thumbUrl;
                cm1LinkData.sys_dependentvariantid = cm1LinkData.thumbVarId;
            }
            else{
                formData.src = cm1LinkData.fullUrl;
                cm1LinkData.sys_dependentvariantid = cm1LinkData.fullVarId;
            }
            var inlineImage = new Image();
            inlineImage.onload = function() {
                if(!width && !height && !proportion) {
                    formData.dimensions.width=this.width;
                    formData.dimensions.height=this.height;
                    width = this.width;
                    height = this.height;
                    proportion = width / height;
                }
            };

            //setReadWriteMode(null);
            inlineImage.src = formData.src;
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


                    var currentaltoverride = data.alt;
                    var currenttitleoverride = data.title;
                    var previousoverride = data.dataPreviousAltOverride;
                    var previoustitleoverride = data.dataPreviousTitleOverride;
                    var datadecorative = data.isDataDecorativeOverride;
                    var titleOverride = data.isDataTitleOverride;
                    var descriptionOverride = data.isDataDescriptionOverride;
                    var width = 0;
                    var height = 0;
                    if(typeof data.dimensions !== 'undefined'){
                        width = data.dimensions.width;
                        height = data.dimensions.height;
                    }

                    if(isUpgradeScenario) {
                        //updateTitle = false;
                        if((currentaltoverride !== renderLink.altText) && currentaltoverride !== '') {
                            // here if the override does not match the value of the asset
                            // we assume an override has been set and set the overrides
                            // checkboxes to true in addition to preserving the current override
                            imgElm.setAttribute('data-description-override', true);
                            if(previousoverride !== 'undefined' && previousoverride === '' && currentaltoverride !== 'undefined' && currentaltoverride === '') {
                                if(renderLink.altText !== 'undefined') {
                                    formData.alt=renderLink.altText;
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
                                    formData.title=renderLink.title;
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
                        if(!titleOverride && renderLink.title && !datadecorative) {
                            formData.title=renderLink.title;
                        }
                        // if the alt is not override AND there is alt text present and its not a decorative (empty) alt
                        // we use the renderLink value.
                        if(!descriptionOverride && renderLink.altText && !datadecorative) {
                            formData.alt=renderLink.altText;
                        }

                        // the asset has no title text
                        if(!renderLink.title && currenttitleoverride !== '') {
                            formData.dataPreviousTitleOverride=currenttitleoverride;
                        }
                        // the override title text is different than the asset title text and override has been deselected
                        else if(currenttitleoverride !== renderLink.title && !datadecorative && currenttitleoverride !== '') {
                            formData.dataPreviousTitleOverride=currenttitleoverride;
                        }
                        // if the current override is empty and decorative is de-selected
                        else if(currenttitleoverride === '' && !datadecorative) {
                            formData.dataPreviousTitleOverride=previoustitleoverride;
                        }
                    }

                    // the asset has no alt text
                    if(!renderLink.altText && currentaltoverride !== '') {
                        formData.dataPreviousAltOverride=currentaltoverride;
                    }
                    // the override alt text is different than the asset alt text and override has been deselected
                    else if(currentaltoverride !== renderLink.altText && !datadecorative && currentaltoverride !== '') {
                        formData.dataPreviousAltOverride=currentaltoverride;
                    }
                    // if the current override is empty and decorative is de-selected
                    else if(currentaltoverride === '' && !datadecorative) {
                        formData.dataPreviousAltOverride=previousoverride;
                    }

                    if(currenttitleoverride !== renderLink.title) {
                        formData.dataPreviousAltOverride=currenttitleoverride;
                    }

                    cm1LinkData.thumbUrl = renderLink.thumbUrl;
                    cm1LinkData.thumbVarId = renderLink.thumbsys_dependentvariantid;
                    cm1LinkData.fullUrl = renderLink.url;
                    cm1LinkData.fullVarId = renderLink.sys_dependentvariantid;

                    if(cm1LinkData.thumbUrl === '') {
                        //@todo: formData.dataImgtype').disabled(true);
                    }

                    setSrcAndSize();
                    win.setData(formData);
                    if(typeof callback === "function") {
                        callback(renderLink.url, renderLink.thumbUrl, renderLink.title);
                    }
                });
            });
        }
        var isImage = imgElm.nodeName === 'IMG';
        width =  isImage ? dom.getAttrib(imgElm, 'width'):'';
        height =  isImage ? dom.getAttrib(imgElm, 'height'):'';
        formData.dimensions = {width:width,height:height};
        proportion = width / height;
        formData.dataImgtype = isImage ? dom.getAttrib(imgElm, 'dataImgtype') : '_full';
        formData.constrain = isImage ? (dom.getAttrib(imgElm, 'dataConstrain') === 'true') : false;
        formData.src = isImage ? dom.getAttrib(imgElm, 'src') : '';
        formData.alt = isImage ? dom.getAttrib(imgElm, 'alt') : '';
        formData.isDataDescriptionOverride = isImage ? (dom.getAttrib(imgElm, 'isDataDescriptionOverride') === 'true') : false;
        formData.dataPreviousAltOverride = isImage ? dom.getAttrib(imgElm, 'dataPreviousAltOverride') : '';
        formData.dataPreviousTitleOverride = isImage ? dom.getAttrib(imgElm, 'dataPreviousTitleOverride') : '';
        formData.isDataDecorativeOverride = isImage ? (dom.getAttrib(imgElm, 'isDataDecorativeOverride') === 'true') : false;
        formData.dataTitleOverride = isImage ? (dom.getAttrib(imgElm, 'dataTitleOverride') === 'true') : false;
        formData.title = isImage ? dom.getAttrib(imgElm, 'title') : '';
        formData.align = isImage ? dom.getStyle(imgElm, 'float') !== '' ? dom.getStyle(imgElm, 'float') : dom.getStyle(imgElm, 'vertical-align') : '';
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
            imageListCtrl = [{
                name: 'target',
                type: 'listbox',
                label: 'Image list',
                items: buildImageList(),
                onAction: function(e) {
                    var altCtrl = formData.alt;

                    if (!altCtrl.value() || (e.lastControl && altCtrl.value() === e.lastControl.text())) {
                        altCtrl.value(e.control.text());
                    }

                    formData.src=e.control.value();
                }
            }];
        }

        win = editor.windowManager.open({
            title: 'Edit image',
            data: formData,
            body: {
                type: 'panel', // root body panel
                items: [
                    {name: 'srcPath', type: 'urlinput', filetype: 'image', label: 'Source'},
                    {name: 'src', type: 'input', label: 'Source'},
                    {name: 'isDataDecorativeOverride',type: 'checkbox', label: 'Decorative (WAI)', text: 'Decorative Image', onChange: updateDecorativeImage},
                    {name: 'target', type: 'listbox', label: 'Image list',items: buildImageList(),
                        onChange: function(e) {
                            var altCtrl = formData.alt;

                            if (!altCtrl.value() || (e.lastControl && altCtrl.value() === e.lastControl.text())) {
                                altCtrl.value(e.control.text());
                            }

                            formData.src=e.control.value();
                        }
                    },
                    {type: 'bar',label: 'Image description*',name: 'imageDesc',layout: 'flex',direction: 'row', align: 'center',spacing: 5,
                        items: [
                            {name: 'alt', type: 'input', label: 'Image description', size: 26, classes: 'perc-description', onChange: resetAriaBoxes},
                            {name: 'isDataDescriptionOverride', type: 'checkbox', label: 'override',  onChange: setReadWriteModeAlt},
                        ]
                    },
                    {type: 'bar',label: 'Image title*',name: 'imageTitle',layout: 'flex',direction: 'row', align: 'center', spacing: 5,
                        items: [
                            {name: 'title', type: 'input', label: 'Image title', size: 26, classes: 'perc-title',onChange: resetAriaBoxes},
                            {type: 'checkbox', name: 'isDataTitleOverride',  label: 'override',  onChange: setReadWriteModeTitle},
                        ]
                    },
                    {
                        type: 'sizeinput',
                        label: 'Dimensions',
                        name: 'dimensions',
                        constrain: true

                    },
                    {
                        name: 'dataImgtype',
                        type: 'listbox',
                        label: 'Image type',
                        minWidth: 90,
                        maxWidth:135,
                        onSubmit:updateImage,
                        items: [{text: 'Full', value: '_full'},{text: 'Thumbnail', value: '_thumb'}],
                        onSetup: function() {
                            if(formData.dataImgtype === '') {
                                formData.dataImgtype = this._values[0].value;
                            }
                            this.value(formData.dataImgtype);
                        }
                    },
                    {
                        label: 'Alignment',
                        minWidth: 90,
                        name: 'align',
                        type: 'listbox',
                        text: 'None',
                        maxWidth: null,
                        items: [
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
                ]

            },

            buttons: [
                {
                    type: 'cancel',
                    text: 'Cancel',
                    onAction: function() {
                        win.close();
                    }
                },
                {
                    type: 'submit',
                    text: 'Ok',
                    primary: true,
                    onAction :function() {
                        win.find('form')[0].submit();
                    }
                }],

            initialData: {
                'isDataDecorativeOverride': formData.isDataDecorativeOverride,
                'isDataDescriptionOverride': formData.isDataDescriptionOverride,
                'isDataTitleOverride': false,
                'constrain':formData.dataconstrain
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

                formData.dataPreviousAltOverride = formData.isDataDescriptionOverride ? formData.alt : formData.dataPreviousAltOverride;
                formData.dataPreviousTitleOverride = formData.isDataTitleOverride ? formData.title : formData.dataPreviousTitleOverride;
                if(!formData.isDataDecorativeOverride) {
                    if (formData.isDataDescriptionOverride && (!formData.alt || formData.alt.trim() === '') ){
                        $('.mce-perc-description').css('border-color', 'red');
                        $('.mce-perc-description').attr('aria-invalid', 'true');
                        $('.mce-perc-description').attr('role', 'alert');
                        e.preventDefault();
                        return;
                    }
                    if (formData.isDataTitleOverride && (!formData.title || formData.title.trim() === '')) {
                        $('.mce-perc-title').css('border-color', 'red');
                        $('.mce-perc-title').attr('aria-invalid', 'true');
                        $('.mce-perc-title').attr('role', 'alert');
                        e.preventDefault();
                        return;
                    }

                }
                if(isNaN(formData.dimensions.width) ){
                    $('.mce-perc-width').css('border-color', 'red');
                    $('.mce-perc-width').attr('aria-invalid', 'true');
                    $('.mce-perc-width').attr('role', 'alert');
                    e.preventDefault();
                    return;
                }
                if (isNaN(formData.dimensions.height)){
                    $('.mce-perc-height').css('border-color', 'red');
                    $('.mce-perc-height').attr('aria-invalid', 'true');
                    $('.mce-perc-height').attr('role', 'alert');
                    e.preventDefault();
                    return;
                }

                function addImage() {
                    if (formData.dimensions.width === '') {
                        delete formData.dimensions.width;
                    }

                    if (formData.dimensions.height === '') {
                        delete formData.dimensions.height;
                    }

                    /* If this is a decorative image, we need to set the value to empty.
                     * We do, however, need to preserve the original overridden alt
                     * text if there was some. This sends the empty alt attribute
                     * to TinyMCE for updating the HTML, however we restore that
                     * value when loading the editor the next time right after
                     * the jQuery.extend method below.
                     **/
                    var tempAltValue = formData.alt;
                    if(formData.isDataDecorativeOverride) {
                        formData.alt = '';
                    }

                    //Handle allign
                    if(imgElm) {
                        editor.dom.setStyles(imgElm,{'float':'', 'vertical-align':''});
                        if(formData.align === 'left' || formData.align === 'right') {
                            editor.dom.setStyle(imgElm, 'float', formData.align);
                        }
                        else if(formData.align !== '') {
                            editor.dom.setStyle(imgElm, 'vertical-align', formData.align);
                        }
                    }
                    else{
                        if(formData.align === 'left' || formData.align === 'right') {
                            formData.style = 'float:' + formData.align + ';';
                        }
                        else if(formData.align && formData.align !== '') {
                            formData.style = 'vertical-align:' + formData.align + ';';
                        }
                    }
                    delete formData.align;
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
                    tdi = formData.dataImgtype;
                    delete formData.dataImgtype;

                    tdc = formData.constrain;
                    delete formData.constrain;

                    delete formData.datadescriptionoverride;
                    delete formData.datatitleoverride;
                    delete formData.datadecorativeoverride;

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
                        dom.setAttribs(imgElm, formData);
                        editor.nodeChanged();
                    } else {
                        editor.insertContent(dom.createHTML('img', formData));
                    }

                    formData.dataImgtype = tdi;
                    formData.constrain = tdc;
                    formData.alt = tempAltValue;
                }

                var dataSrc = formData.src;
                var imgPath = dataSrc.trim(),imgPathLower = imgPath.toLowerCase();
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
                                formData.src = formData.dataimgtype === '_thumb' ? thumUrl : fullUrl;
                                formData.dimensions.width = width;
                                formData.dimensiond.height = height;

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

        editor.addCommand('updateFileSelection', function (ui, selectedItem) {
            console.log("Hello");
            updateLinkData(selectedItem,null);
        });
        return win;
    }




    editor.ui.registry.addButton('image', {
        icon: 'image',
        tooltip: 'Insert/edit image',
        onAction:showDialog,
        stateSelector: 'img:not([data-mce-object])',

    });




    editor.ui.registry.addMenuItem('image', {
        icon: 'image',
        text: 'Insert image',
        onAction: showDialog,
        context: 'insert',
        prependToContext: true
    });
});
