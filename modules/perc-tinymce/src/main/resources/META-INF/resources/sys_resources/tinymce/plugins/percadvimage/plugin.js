
/*global tinymce:true */

tinymce.PluginManager.add('percadvimage', function(editor, url) {
    var formData = {};

    function showDialog(buttonAPI) {
        var win, cm1LinkData = {}, dom = editor.dom, imgElm = editor.selection.getNode();
        var width, height, imageListCtrl;
        var mainEditor = editor.contentWindow.parent;
        var topFrJQ = mainEditor.jQuery.topFrameJQuery;
        var isUpgradeScenario = false;

        function buildImageList() {
            var linkImageItems = [{text: 'None', value: ''}];

            tinymce.each(editor.settings.image_list, function (link) {
                linkImageItems.push({
                    text: link.text || link.title,
                    value: link.value || link.url,
                    menu: link.menu
                });
            });

            return linkImageItems;
        }

        function setReadWriteMode() {
            var url = formData.url;
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
                if (!imgElm.getAttribute('data-description-override') || !imgElm.getAttribute('data-title-override')) {
                    isUpgradeScenario = true;
                }
                getImageData('16777215-101-' + sys_dependentid, true);
            }

            //If it is an external url, we want to be able to write to it.
            if (url.startsWith('http')) {
                win.enable('alt');
                win.enable('title');
            }
        }

        function addImage() {

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
            var cm1ImgAttrs = {
                sys_dependentvariantid : cm1LinkData.sys_dependentvariantid,
                rxinlineslot : cm1LinkData.rxinlineslot,
                sys_dependentid : cm1LinkData.sys_dependentid,
                sys_relationshipid : '',
                inlinetype : cm1LinkData.inlinetype,
                'data-jcrpath' : cm1LinkData.jcrpath
            };

            var url = formData.url;
            if(typeof url === 'undefined'){
                url = formData.src.value;
            }

            var formImgAttrs = {
                'data-description-override': formData.isDataDescriptionOverride,
                'data-previous-alt-override' : formData.dataPreviousAltOverride,
                'data-title-override' : formData.isDataTitleOverride,
                'data-previous-title-override' : formData.dataPreviousTitleOverride,
                'data-decorative-override': formData.isDataDecorativeOverride,
                'data-constrain' : formData.constrain,

                'src' :url,
                'alt' :formData.alt,
                'title' : formData.title,
                'align' : formData.align,
                'data-imgtype' : formData.dataImgtype,
                'height' :  formData.dimensions.height,
                'width' : formData.dimensions.width,
            };

            jQuery.extend(formImgAttrs,cm1ImgAttrs);
            if (imgElm) {
                if (!imgElm.attributes.getNamedItem('style')) {
                } else {
                    var style = {
                        style: imgElm.attributes.getNamedItem('style').value
                    };
                    jQuery.extend(formImgAttrs, style);
                }
				dom.setAttribs(imgElm, formImgAttrs);
                editor.nodeChanged();
            } else {
                editor.insertContent(dom.createHTML('IMG', formImgAttrs));
            }

        }

        function updateDecorativeImage() {
            var url = formData.url;
			if(typeof url === 'undefined'){
				url = formData.src.value;
			}
            var datadecorativeoverride = formData.isDataDecorativeOverride;
            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                win.enable('alt');
                win.enable('title');
            }
            else {
                if(datadecorativeoverride){
                    win.disable('alt');
                    win.disable('title');
                    formData.alt= '';
                    formData.title='';
                    formData.isDataTitleOverride =false;
                    win.disable('isDataTitleOverride');
                    formData.isDataDescriptionOverride = false;
                    win.disable('isDataDescriptionOverride');
                }else {
                    win.enable('isDataDescriptionOverride');
                    win.enable('isDataTitleOverride');
                    setReadWriteModeAlt();
                    setReadWriteModeTitle();
                }

                win.setData(formData);

            }
        }



        function setReadWriteModeAlt() {
            var url = formData.url;
			if(typeof url === 'undefined'){
				url = formData.src.value;
			}
            var description = formData.alt;
            var title = formData.title;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var datadescriptionoverride = formData.isDataDescriptionOverride;
            var datapreviousaltoverride = (formData.dataPreviousAltOverride == null || typeof formData.dataPreviousAltOverride === 'undefined' )? '': formData.dataPreviousAltOverride;
            var datadecorativeoverride = formData.isDataDecorativeOverride;

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                win.enable('alt');
                win.enable('title');
            }
            else {
                if(datadecorativeoverride){
                    win.disable('alt');
                    win.disable('isDataDescriptionOverride');
                }else{
                    if(datadescriptionoverride) {
                        win.enable('alt');
                        formData.alt= datapreviousaltoverride;

                    }
                    else {

                        win.disable('alt');
                        if(url !== '') {
                            getImageData('16777215-101-' + sys_dependentid, true);
                        }
                    }
                }
            }
        }

        function setReadWriteModeTitle() {
            var title = formData.title;
            var url = formData.url;
			if(typeof url === 'undefined'){
				url = formData.src.value;
			}
            var description = formData.alt;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataprevioustitleoverride = (formData.dataPreviousTitleOverride == null || typeof formData.dataPreviousTitleOverride === 'undefined' )? '': formData.dataPreviousTitleOverride;
            var datatitleoverride = formData.isDataTitleOverride;
            var datadecorativeoverride = formData.isDataDecorativeOverride;

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith('http')) {
                win.enable('alt');
                win.enable('title');
            }
            else {
                if(datadecorativeoverride){
                    win.disable('title');
                    win.disable('isDataTitleOverride');
                }else{
                    if(datatitleoverride) {
                        win.enable('title');
                        formData.title=dataprevioustitleoverride;
                    }
                    else {
                        win.disable('title');
                        if(url !== '') {
                            getImageData('16777215-101-' + sys_dependentid, true);
                        }

                    }
                }
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
				formData.url = cm1LinkData.thumbUrl;
                formData.src = {value: formData.url};

                cm1LinkData.sys_dependentvariantid = cm1LinkData.thumbVarId;
            }
            else{

				formData.url = cm1LinkData.fullUrl;
				formData.src = {value: formData.url};
                cm1LinkData.sys_dependentvariantid = cm1LinkData.fullVarId;
            }
            var inlineImage = new Image();
            inlineImage.onload = function() {
                formData.dimensions.width=this.width.toString();
                formData.dimensions.height=this.height.toString();
                win.setData(formData);

            };

			if(typeof formData.url === 'undefined'){
				inlineImage.src = formData.src.value;
			}else{
            inlineImage.src = formData.url;
        }
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

                    var currentaltoverride = formData.alt;
                    var currenttitleoverride = formData.title;
                    var previousoverride = imgElm !== null? imgElm.getAttribute('data-previous-alt-override'):'';
                    var previoustitleoverride = imgElm !== null? imgElm.getAttribute('data-previous-title-override'):'';
                    var datadecorative = formData.isDataDecorativeOverride;

                    var titleOverride = formData.isDataTitleOverride;
                    var descriptionOverride = formData.isDataDescriptionOverride;


                    if(isUpgradeScenario) {
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
                            formData.isDataDescriptionOverride = true;
                            formData.isDataDecorativeOverride = false;
                            win.disable('isDataDecorativeOverride');
                        }
                        else if(renderLink.altText) {
                            imgElm.setAttribute('data-description-override', false);
                        }

                        if((currenttitleoverride !== renderLink.title) && currenttitleoverride !== '') {
                            formData.isTitleOverride = true;
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
                        if(!formData.isDataTitleOverride && renderLink.title && !datadecorative) {
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
                        win.disable('dataImgtype');
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
        formData.dataImgtype = isImage ? dom.getAttrib(imgElm, 'data-imgtype') : '_full';
        formData.constrain = isImage ? (dom.getAttrib(imgElm, 'data-constrain') === 'true') : false;
        formData.url = isImage ? dom.getAttrib(imgElm, 'src') : '';
		formData.src = {value: formData.url};
        formData.alt = isImage ? dom.getAttrib(imgElm, 'alt') : '';
        formData.isDataDescriptionOverride = isImage ? (dom.getAttrib(imgElm, 'data-description-override') === 'true') : false;
        formData.dataPreviousAltOverride = isImage ? dom.getAttrib(imgElm, 'data-previous-alt-override') : '';
        formData.dataPreviousTitleOverride = isImage ? dom.getAttrib(imgElm, 'data-previous-title-override') : '';
        formData.isDataDecorativeOverride = isImage ? (dom.getAttrib(imgElm, 'data-decorative-override') === 'true') : false;
        formData.isDataTitleOverride = isImage ? (dom.getAttrib(imgElm, 'data-title-override') === 'true') : false;
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

        win = editor.windowManager.open({
            title: 'Edit image',
            data: formData,
            body: {
                type: 'panel', // root body panel
                items: [
                    {name: 'src', type: 'urlinput', filetype: 'image', label: 'Source'},
                    {name: 'isDataDecorativeOverride',type: 'checkbox', label: 'Decorative- Image (WAI)', text: 'Decorative Image'},
                    {type: 'bar',label: 'Image description*',name: 'imageDesc',layout: 'flex',direction: 'row', align: 'center',spacing: 5,
                        items: [
                            {name: 'alt', type: 'input', label: 'Image description', size: 26, classes: 'perc-description'},
                            {name: 'isDataDescriptionOverride', type: 'checkbox', label: 'override'},
                        ]
                    },
                    {type: 'bar',label: 'Image title*',name: 'imageTitle',layout: 'flex',direction: 'row', align: 'center', spacing: 5,
                        items: [
                            {name: 'title', type: 'input', label: 'Image title', size: 26, classes: 'perc-title'},
                            {type: 'checkbox', name: 'isDataTitleOverride',  label: 'override'},
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
                        items: [{text: 'Full', value: '_full'},{text: 'Thumbnail', value: '_thumb'}],
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

            initialData: formData,
            onChange: function(api, changeData){
                formData = win.getData();
                if(changeData.name === 'isDataDecorativeOverride') {
                    updateDecorativeImage();
                }
                if(changeData.name === 'isDataDescriptionOverride') {
                    setReadWriteModeAlt();
                }
                if(changeData.name === 'title') {
                    resetAriaBoxes();
                }

                if(changeData.name === 'isDataTitleOverride') {
                    setReadWriteModeTitle();

                }

                if(changeData.name === 'dataImgtype'){
                    updateImage();
                }
                if(changeData.name === 'align'){

                    formData.align = api.getData().align;
                }
            },

            onSubmit: function(e) {

                resetAriaBoxes();
                formData.dataPreviousAltOverride = formData.isDataDescriptionOverride ? formData.alt : formData.dataPreviousAltOverride;
                formData.dataPreviousTitleOverride = formData.isDataTitleOverride ? formData.title : formData.dataPreviousTitleOverride;
                if(!formData.isDataDecorativeOverride) {
                    if (formData.isDataDescriptionOverride && (!formData.alt || formData.alt.trim() === '') ){
                        $('.mce-perc-description').css('border-color', 'red');
                        $('.mce-perc-description').attr('aria-invalid', 'true');
                        $('.mce-perc-description').attr('role', 'alert');
                        return;
                    }
                    if (formData.isDataTitleOverride && (!formData.title || formData.title.trim() === '')) {
                        $('.mce-perc-title').css('border-color', 'red');
                        $('.mce-perc-title').attr('aria-invalid', 'true');
                        $('.mce-perc-title').attr('role', 'alert');
                        return;
                    }

                }
                if(isNaN(formData.dimensions.width) ){
                    $('.mce-perc-width').css('border-color', 'red');
                    $('.mce-perc-width').attr('aria-invalid', 'true');
                    $('.mce-perc-width').attr('role', 'alert');
                    return;
                }
                if (isNaN(formData.dimensions.height)){
                    $('.mce-perc-height').css('border-color', 'red');
                    $('.mce-perc-height').attr('aria-invalid', 'true');
                    $('.mce-perc-height').attr('role', 'alert');
                    return;
                }

                var dataSrc = formData.url;
				if(typeof dataSrc === 'undefined'){
					dataSrc = formData.src.value;
				}
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
                                formData.url = formData.dataimgtype === '_thumb' ? thumUrl : fullUrl;
								formData.src = {value: formData.url};
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

                win.close();

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

        //initialize accessibility since tinymce currently doesn't allow it directly
        $('.mce-perc-description').attr('aria-invalid','false');
        $('.mce-perc-description').attr('aria-required','true');
        $('.mce-perc-title').attr('aria-required','true');
        $('.mce-perc-title').attr('aria-invalid','false');
        $('.mce-perc-width').attr('aria-required','true');
        $('.mce-perc-width').attr('aria-invalid','false');
        $('.mce-perc-height').attr('aria-required','true');
        $('.mce-perc-height').attr('aria-invalid','false');
        setReadWriteMode();
        setReadWriteModeAlt();
        setReadWriteModeTitle();

        return win;
    }

    /**
     * This method checks if TinyMCE is part of ContentEditor or CMS UI
     * In Case it is CMS UI, it enables buttons and menu items applicable to CMS and
     * disables buttons and menuitems applicable to Rhythmyx Objects and vice a versa.
     * @returns {boolean}
     */
	function isRXEditor(){
		 var isRxEdr = false;
		if(typeof contentEditor !== 'undefined' && "yes" === contentEditor){
			isRxEdr = true;
		}
		return isRxEdr;
	}

    editor.ui.registry.addButton('image', {
        icon: 'image',
        tooltip: 'Insert/edit image',
        onAction:showDialog,
        stateSelector: 'img:not([data-mce-object])',
		onSetup: function (buttonApi) {
            var editorEventCallback = function (eventApi) {
              buttonApi.setDisabled(isRXEditor() === true );
            };
            editor.on('NodeChange', editorEventCallback);

            /* onSetup should always return the unbind handlers */
            return function (buttonApi) {
              editor.off('NodeChange', editorEventCallback);
            };
      }

    });

    editor.ui.registry.addMenuItem('image', {
        icon: 'image',
        text: 'Insert image',
        onAction: showDialog,
        context: 'insert',
        prependToContext: true,
        onSetup: function (buttonApi) {
            if (isRXEditor() === false) {
                buttonApi.setDisabled(false);
            } else {
                buttonApi.setDisabled(true);
            }
        }
    });
});
