
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

            var url = win.getData().src.value;
            var description = win.getData().alt;
            var title = win.getData().title;
            var datadescriptionoverride = win.getData()["data-description-override"];
            var datatitleoverride = win.getData()["data-title-override"];
            var datadecorativeoverride = win.getData()["data-decorative-override"];
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataconstrain = win.getData()["data-constrain"];
            var height = win.getData().height;
            var width = win.getData().width;

            // check for upgrade scenario.  here we need to see if customers have previously 
            // set overrides and enable the checkboxes for those if previously set.
            // data-description-override was not ever used prior to current patch 1/24/2018
            // so we can use these attributes to detect if they are upgrading
            if (imgElm && !cm1LinkData.title) {
                if(!imgElm.getAttribute('data-description-override') || !imgElm.getAttribute('data-title-override'))
                    isUpgradeScenario = true;
                getImageData('16777215-101-' + sys_dependentid, true);
            }

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith("http")) {
                win.enable("alt");
                win.enable("title");

            } 
            else {
                if(!datadescriptionoverride) {
                    //-- description.disabled(true);
                    //--description.addClass("disabled");
                    //--datadecorativeoverride.disabled(false);

                    win.disable("alt");
                    win.enable("data-decorative-override");
                } else {

                    //-- description.removeClass("disabled");
                    //--description.disabled(false);
                    win.enable("alt");

                    //-- datadecorativeoverride.disabled(true);
                    //--datadecorativeoverride.checked(false);
                    win.setData({"data-decorative-override":false});
                    win.disable("data-decorative-override");

                }

                if(!datatitleoverride) {
                    win.disable("title");
                } else {
                    //--title.removeClass("disabled");
                    //--title.disabled(false);
                    win.enable("title");
            }

                if(datadecorativeoverride) {
                    //--description.disabled(true);
                    //--description.addClass('disabled');
                    win.disable("alt");

                    //--datadescriptionoverride.checked(false);
                    //--datadescriptionoverride.disabled(true);
                    win.setData({"data-description-override":false});
                    win.disable("data-description-override");



            }
            else {
                    //--datadescriptionoverride.disabled(false);
                    win.enable("data-description-override");
                }

                if(!dataconstrain) {

                    //-- width.disabled(true);
                    //--height.disabled(true);
                    win.disable("width");
                    win.disable("height");
                }
            }
        }

        function setReadWriteModeAlt(e) {
            var url = win.getData().src.value;
            var description = win.getData().alt;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var datadescriptionoverride = win.getData()["data-description-override"];
            var datapreviousaltoverride = win.getData()["data-previous-alt-override"];

            //If it is an external url, we want to be able to write to it.
            if(url.startsWith("http")) {
                win.enable("title");
                win.enable("alt");
            } 
            else {
                if(!datadescriptionoverride) {
                    if(url !== "") {
                        getImageData("16777215-101-" + sys_dependentid, true);
                    }
                }
                else {
                    if(datapreviousaltoverride !== '')
                        win.setData({alt:datapreviousaltoverride});
                }
                setReadWriteMode(null);
            }
        }

        function setReadWriteModeTitle(e) {
            var data = win.getData();
            var title = win.getData().title;
            var url = win.getData().src.value;
            var description = win.getData().alt;
            var sys_dependentid = cm1LinkData.sys_dependentid;
            var dataprevioustitleoverride = win.getData()["data-previous-title-override"];
            var datatitleoverride = win.getData()["data-title-override"];
            //If it is an external url, we want to be able to write to it.
            if(url.startsWith("http")) {
                win.enable("alt");
                win.enable("title");
            } 
            else {
                if(!datatitleoverride) {
                    if(url !== "")
                        getImageData("16777215-101-" + sys_dependentid, true);
                } 
                else {
                    if(dataprevioustitleoverride !== '')
                        win.setData({title:dataprevioustitleoverride});
                }
                setReadWriteMode(null);
            }
        }

        function resetAriaBoxes() {

            var description = win.getData().alt;
            var title = win.getData().title;

            if(description && description.trim() != '') {
                $('.mce-perc-description').css({"border-color":"#c5c5c5", "transition":"border 2s ease"});
                $(".mce-perc-description").attr("role","");
                $(".mce-perc-description").attr("aria-invalid","false");
            }

            if(title && title.trim() != '') {
                $('.mce-perc-title').css({"border-color":"#c5c5c5", "transition":"border 2s ease"});
                $(".mce-perc-title").attr("role","");
                $(".mce-perc-title").attr("aria-invalid","false");
            }
            }

        /**
         * True if either title or alt is empty
         */
        function validateTitleAndAlt(alt, title) {
            if(alt && title) {
                return (alt.trim() == '' || title.trim() == '');
            } else {
                return true;
            }

        }

        function recalcSize(control) {
            var widthCtrl, heightCtrl, newWidth, newHeight;

            newWidth = win.getData().width;
            newHeight = win.getData().height;

            if (win.getData()["data-constrain"] && width && height && newWidth && newHeight) {
                win.enable("height");
                win.enable("width");

                if (control == "width") {
                        newHeight = Math.round(newWidth / proportion);
                    if (newHeight == 0) newHeight = 1;
                    win.setData({height:""+newHeight});
                    } else {
                        newWidth = Math.round(proportion * newHeight);
                    if (newWidth == 0) newWidth = 1;
                    win.setData({width:""+newWidth});
                }
            }else{
                win.disable("height");
                win.disable("width");
            }
            width = newWidth;
            height = newHeight;
        }

        function updateImage() {
            if(cm1LinkData.thumbUrl) {
                setSrcAndSize();
            }
            else if(cm1LinkData.sys_dependentid) {
                getImageData("-1-101-" + cm1LinkData.sys_dependentid, false);
            }
        }
        function setSrcAndSize() {
            //handle thumb url and switch from thumb to full
            if(win.getData()["data-imgtype"] == "_thumb") {
                var srcObj = {src:{"value":cm1LinkData.thumbUrl}};
                win.setData(srcObj);
                cm1LinkData.sys_dependentvariantid = cm1LinkData.thumbVarId;
            }
            else{
                var srcObj = {src:{"value":cm1LinkData.fullUrl}};
                win.setData(srcObj);
                cm1LinkData.sys_dependentvariantid = cm1LinkData.fullVarId;
            }
            var inlineImage = new Image();
            inlineImage.onload = function() {
                win.setData({width:""+this.width});
                win.setData({height:""+this.height});
                    width = this.width;
                    height = this.height;
                    proportion = width / height;
                };
            //setReadWriteMode(null);
            inlineImage.src = win.getData().src.value;
        }


        function windowItemPreProcessor() {
            var imageTitleDisable  = true;
            var hightNdWidthDisable  = true;
            var imageDiscDisable  =true;


            if(data.datatitleoverride){imageTitleDisable = false;}
            if(data.dataconstrain){hightNdWidthDisable=false;}
            if(data.datadescriptionoverride){imageDiscDisable=false;}
            windowArray = [];
            windowArray.push({name: 'src', tabindex:"1",  type: 'urlinput', filetype: 'image', label: 'Source' ,  onkeyup: setReadWriteMode});
            windowArray.push({name: 'data-previous-alt-override', type: 'input', hidden: true, value: data.datapreviousaltoverride});
            windowArray.push({name: 'data-previous-title-override', type: 'input', hidden: true, value: data.dataprevioustitleoverride}  );
            imageListCtrl,
                windowArray.push({
                    type: 'bar',
                    items: [
                        {name: 'alt', tabindex:"2",  type: 'input', label: 'Image description', size: 26, disabled:imageDiscDisable, classes: 'perc-description', onchange: resetAriaBoxes},
                        {type: 'checkbox', tabindex:"3",  name: 'data-description-override', checked: data.datadescriptionoverride, label: 'override', text: 'Override', onchange: setReadWriteModeAlt},
                        {type: 'checkbox',tabindex:"4",  name: 'data-decorative-override', checked: data.datadecorativeoverride, label: 'decorative', onchange: setReadWriteModeAlt}
                    ]
                }  );
            windowArray.push({
                type: 'bar',
                label: 'Image title*',
                layout: 'flex',
                direction: 'row',
                align: 'center',

                spacing: 5,
                items: [
                    {name: 'title', type: 'input', tabindex:"5",  label: 'Image title', size: 26,  disabled:imageTitleDisable ,classes: 'perc-title',onchange: resetAriaBoxes},
                    {type: 'checkbox', name: 'data-title-override',tabindex:"6",  checked: data.datatitleoverride, label: 'override',  onchange: setReadWriteModeTitle},
                ]
            }  );
            windowArray.push({
                type: 'bar',
                layout: 'flex',
                direction: 'row',
                align: 'center',
                spacing: 5,
                items: [
                    {name: 'width', label: 'Width', type: 'input',tabindex:"7",  maxLength: 3, size: 3, disabled:hightNdWidthDisable, onkeyup: recalcSize, onchange: recalcSize},
                    {name: 'height',label: 'Height', type: 'input',tabindex:"8",   maxLength: 3, size: 3, onkeyup: recalcSize,  disabled:hightNdWidthDisable,onchange: recalcSize},
                    {name: 'data-constrain', type: 'checkbox',tabindex:"9",  checked: data.dataconstrain, label: 'Constrain proportions', onchange: recalcSize}
                ]
            }  );

            windowArray.push({
                type: 'bar',

                items: [
                    {
                        name: 'data-imgtype',
                        type: 'selectbox',
                        label: 'Image type',
                        tabindex:"10",
                        minWidth: 90,
                        maxWidth:125,
                        onselect:updateImage,
                        items: [{text: 'Full', value: '_full'},{text: 'Thumbnail', value: '_thumb'}],
                    },
                    {
                        minWidth: 20,
                        maxWidth:20,
                        type: 'htmlpanel', // component type
                        html: '<span style="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>'
                    },
                    {
                        label: 'Alignment',
                        minWidth: 90,
                        tabindex:"11",
                        name: 'align',
                        type: 'selectbox',
                        text: 'None',

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
            }  );




            return windowArray;
        }
        function getImageData(itemId, updateTitle, callback) {
            topFrJQ.PercPathService.getInlineRenderLink(itemId, function(status, data) {
                if(!status) {
                    topFrJQ.perc_utils.info(data);
                    topFrJQ.perc_utils.alert_dialog({"title":"Error", "content":"Sorry for the inconvience, could not get selected item details to insert image."});
                    return;
                }
                
                var renderLink = data.InlineRenderLink;
                cm1LinkData.rxinlineslot = "104";
                cm1LinkData.sys_dependentid = renderLink.sys_dependentid;
                cm1LinkData.inlinetype = "rximage";
                cm1LinkData.alt = renderLink.altText;
                cm1LinkData.title = renderLink.title;

                var currentaltoverride = win.getData().alt;
                var currenttitleoverride = win.getData().title;
                var previousoverride = win.getData()["data-previous-alt-override"];
                var previoustitleoverride = win.getData()["data-previous-title-override"];
                var datadecorative = win.getData()["data-decorative-override"];
                var titleOverride = win.getData()["data-title-override"];
                var descriptionOverride = win.getData()["data-description-override"];

                if(isUpgradeScenario) {
                    //updateTitle = false;
                    if((currentaltoverride !== renderLink.altText) && currentaltoverride !== "") {
                        // here if the override does not match the value of the asset
                        // we assume an override has been set and set the overrides
                        // checkboxes to true in addition to preserving the current override
                        imgElm.setAttribute('data-description-override', true);
                        if(previousoverride !== 'undefined' && previousoverride === '' && currentaltoverride !== 'undefined' && currentaltoverride === '') {
                            if(renderLink.altText !== 'undefined')
                                win.setData({alt:renderLink.altText});
                            }

                        //--descriptionOverride.checked(true);
                        win.setData({"data-description-override":true});

                        //--datadecorative.checked(false);
                        win.setDat({"data-decorative-override":false});

                        //--datadecorative.disabled(true);
                        win.disable("data-decorative-override");
                    }
                    else if(renderLink.altText) {
                        imgElm.setAttribute('data-description-override', false);
                    }

                    if((currenttitleoverride !== renderLink.title) && currenttitleoverride !== "") {
                        //--titleOverride.checked(true);
                        win.setData({"data-title-override":true});
                        imgElm.setAttribute('data-title-override', true);
                        if(previoustitleoverride !== 'undefined' && previoustitleoverride === '' && currenttitleoverride !== 'undefined' && currenttitleoverride === '') {
                            if(renderLink.title !== 'undefined')
                                win.setData({title:renderLink.title});
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
                    if(!titleOverride){
                        win.setData({"title":renderLink.title});
                    // if the alt is not override AND there is alt text present and its not a decorative (empty) alt
                    // we use the renderLink value.
                    }if(!descriptionOverride && renderLink.altText && !datadecorative){
                    win.setData({"alt":renderLink.altText});
                    // this is not an alt override and there is alttext
                } else if(datadecorative && renderLink.altText){
                    win.setData({alt:''});
                }else if(!datadecorative && !descriptionOverride){
                    win.setData({alt:''});
                    }
                }

                // the asset has no alt text
                if(!renderLink.altText && currentaltoverride !== '') {
                    win.setData({'data-previous-alt-override':currentaltoverride});

                // the override alt text is different than the asset alt text and override has been deselected
                }else if(currentaltoverride !== renderLink.altText && !datadecorative && currentaltoverride !== ''){
                    win.setData({'data-previous-alt-override':currentaltoverride});
                // if the current override is empty and decorative is de-selected
                }else if(currentaltoverride === '' && !datadecorative){
                    win.setData({'data-previous-alt-override':previousoverride});

                }if(currenttitleoverride !== renderLink.title){
                    win.setData({'data-previous-title-override':currenttitleoverride});
                }

                cm1LinkData.thumbUrl = renderLink.thumbUrl;
                cm1LinkData.thumbVarId = renderLink.thumbsys_dependentvariantid;
                cm1LinkData.fullUrl = renderLink.url;
                cm1LinkData.fullVarId = renderLink.sys_dependentvariantid;

                if(cm1LinkData.thumbUrl === "")
                    win.disable('data-imgtype');

                setSrcAndSize();
                if(topFrJQ.isFunction(callback))
                    callback(renderLink.url, renderLink.thumbUrl, renderLink.title);
            });
        }
        var isImage = imgElm.nodeName == "IMG";
        width = data.width = isImage ? dom.getAttrib(imgElm, 'width'):'';
        height = data.height = isImage ? dom.getAttrib(imgElm, 'height'):'';
        proportion = width / height;
        data.dataimgtype = isImage ? dom.getAttrib(imgElm, 'data-imgtype') : '_full';
        data["data-imgtype"] = isImage ? dom.getAttrib(imgElm, 'data-imgtype') : '_full';

        data.dataconstrain = isImage ? (dom.getAttrib(imgElm, 'data-constrain') === 'true') : false;
        data["data-constrain"] = data.dataconstrain;
        var srcPath = isImage ? dom.getAttrib(imgElm, 'src') : '';
        var srcObj = {"value":srcPath};
        data.src = srcObj;
        data.alt = isImage ? dom.getAttrib(imgElm, 'alt') : '';
        data.datadescriptionoverride = isImage ? (dom.getAttrib(imgElm, 'data-description-override') === 'true') : false;
        data["data-description-override"] = data.datadescriptionoverride;
        data.datapreviousaltoverride = isImage ? dom.getAttrib(imgElm, 'data-previous-alt-override') : '';
        data.datapreviousaltoverride = isImage ? dom.getAttrib(imgElm, 'data-previous-alt-override') : '';
        data.dataprevioustitleoverride = isImage ? dom.getAttrib(imgElm, 'data-previous-title-override') : '';
        data.datadecorativeoverride = isImage ? (dom.getAttrib(imgElm, 'data-decorative-override') === 'true') : false;
        data.datatitleoverride = isImage ? (dom.getAttrib(imgElm, 'data-title-override') === 'true') : false;
        data["data-title-override"] = data.datatitleoverride ;

        data.title = isImage ? dom.getAttrib(imgElm, 'title') : '';
        data.align = isImage ? dom.getStyle(imgElm, 'float') !== '' ? dom.getStyle(imgElm, 'float') : dom.getStyle(imgElm, 'vertical-align') : '';
        cm1LinkData.sys_dependentvariantid = isImage ? dom.getAttrib(imgElm, 'sys_dependentvariantid') : '';
        cm1LinkData.rxinlineslot = isImage ? dom.getAttrib(imgElm, 'rxinlineslot') : '';
        cm1LinkData.sys_dependentid = isImage ? dom.getAttrib(imgElm, 'sys_dependentid') : '';
        cm1LinkData.inlinetype = isImage ? dom.getAttrib(imgElm, 'inlinetype') : '';

        if (imgElm.nodeName === "IMG" && !imgElm.getAttribute('data-mce-object')) {
        } else {
            imgElm = null;
        }

        if (editor.settings.image_list) {
            imageListCtrl = {
                name: 'target',
                type: 'selectbox',
                label: 'Image list',
                values: buildImageList(),
                onselect: function(e) {
                    var altCtrl = win.getData().alt;

                    if (!altCtrl.value() || (e.lastControl && altCtrl.value() == e.lastControl.text())) {
                        altCtrl.value(e.control.text());
                    }

                    win.setData({src:e.control.value()});
                }
            };
        }

        windowItems = windowItemPreProcessor();
        win = editor.windowManager.open({
            title: "Edit image",
            initialData: data,

            body: {
                type: 'panel',
                items: [...windowItems]
                },
                buttons : [
            {
                type: 'cancel', // button type
                name: 'cancel', // identifying name
                text: 'Cancel', // text for the button
                disabled: false, // button is active when the dialog opens
                tabindex:"12",
                align: 'end' // align the button to the left of the dialog footer
            },
            {

                type: 'submit', // button type
                name: 'save', // identifying name
                text: 'Save', // text for the button
                disabled: false, // button is active when the dialog opens
                primary: true,
                tabindex:"13",
                align: 'end' // align the button to the left of the dialog footer
            }
                ],

            onChange: (dialogApi, details) => {
            var data = dialogApi.getData();
            if (details.name === "src") {
                setReadWriteMode();
            } else if( details.name === "alt" || details.name === "title"){
                resetAriaBoxes();
            }else if(details.name === "data-description-override" || details.name === "data-decorative-override"){
                setReadWriteModeAlt();
            } else if(details.name ===  "data-title-override"){
                setReadWriteModeTitle();
            } else if(details.name === "width" || details.name === "height" ||  details.name === "data-constrain" ){
                recalcSize(details.name);
            } else if (details.name  === "data-imgtype"){
                updateImage();
            }


            },
            onOpen:function(e) {
                //initialize accessibility since tinymce currently doesn't allow it directly
            $(".mce-perc-description").attr("aria-invalid","false");
            $(".mce-perc-description").attr("aria-required","true");
            $(".mce-perc-title").attr("aria-required","true");
            $(".mce-perc-title").attr("aria-invalid","false");
                setReadWriteMode(e);
            },
            onSubmit: function(e) {
            var data = e.getData();
                data['data-previous-alt-override'] = data['data-description-override'] ? data.alt : data['data-previous-alt-override'];
            data['data-previous-title-override'] = data['data-title-override'] ? data.alt : data['data-previous-title-override'];
            data["src"] = data.src.value;
            if (validateTitleAndAlt(data.alt, data.title) && !data['data-decorative-override']) {
                if (!data.alt || data.alt == '') {

                        $('.mce-perc-description').css('border-color', 'red');

                    $(".mce-perc-description").attr("aria-invalid", "true");

                    $(".mce-perc-description").attr("role", "alert");

                    }

                if (!data.title || data.title == '') {

                        $('.mce-perc-title').css('border-color', 'red');

                    $(".mce-perc-title").attr("aria-invalid", "true");

                    $(".mce-perc-title").attr("role", "alert");

                    }


                //e.preventDefault();

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

                    data.alt = "";

                    }

                    //Handle allign
                    if(imgElm) {
                        editor.dom.setStyles(imgElm,{'float':'', 'vertical-align':''});

                    if (data.align == 'left' || data.align == 'right') {

                            editor.dom.setStyle(imgElm, 'float', data.align);

                    } else if (data.align != "") {

                            editor.dom.setStyle(imgElm, 'vertical-align', data.align);

                    }

                } else {

                    if (data.align == 'left' || data.align == 'right') {

                        data.style = "float:" + data.align + ";";

                    } else if (data.align && data.align != "") {

                        data.style = "vertical-align:" + data.align + ";";

                        }

                        }

                    delete data.align;
                    var cm1ImgAttrs = {
                            sys_dependentvariantid : cm1LinkData.sys_dependentvariantid,
                            rxinlineslot : cm1LinkData.rxinlineslot,
                            sys_dependentid : cm1LinkData.sys_dependentid,

                    sys_relationshipid: "",

                    inlinetype: cm1LinkData.inlinetype

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


                data["class"] ="perc-CustomPubliclink";
                    jQuery.extend(data,cm1ImgAttrs);
                    if (imgElm) {

                    if (imgElm.attributes.getNamedItem("style")) {

                            var style = {

                            style: imgElm.attributes.getNamedItem("style").value

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


            var imgPath = topFrJQ.trim(data.src), imgPathLower = imgPath.toLowerCase();

                //Resolve manually entered internal links

            if (imgPathLower.match("^//sites/") || imgPathLower.match("^//assets/") || imgPathLower.match("^/sites/") || imgPathLower.match("^/assets/") || imgPathLower.match("^sites/") || imgPathLower.match("^assets/")) {

                if (imgPathLower.match("^sites/") || imgPathLower.match("^assets/"))

                    imgPath = "/" + imgPath;

                else if (imgPathLower.match("^//sites/") || imgPathLower.match("^//assets/"))

                        imgPath = imgPath.substring(1);

                    topFrJQ.PercPathService.getPathItemForPath(imgPath, function(status, result) {

                    if (status == topFrJQ.PercServiceUtils.STATUS_ERROR || result.PathItem.type != "percImageAsset") {

                        topFrJQ.perc_utils.alert_dialog({
                            "title": "Error",
                            "content": "We were unable to create the image because this is an invalid URL. Please validate the URL and re-enter the image."
                        });

                    } else {

                            updateLinkData(result.PathItem, function(fullUrl, thumUrl, title) {

                            data.src = data.dataimgtype == "_thumb" ? thumUrl : fullUrl;

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
            jQuery(".tox-browse-url").unbind("click");

            e.close();


            }// end onSubmit()
            
        }); // end of win object initialization
        function updateLinkData(pathItem, callback) {
            //Save the path to cookie
            topFrJQ.cookie("perc-inlineimage-path", pathItem.path);
            getImageData(pathItem.id, true, callback);
        }

        var openCreateImageDialog = function(successCallback, cancelCallback) {
            $.topFrameJQuery.PercCreateNewAssetDialog("percImage", successCallback, cancelCallback);
        };

        $(".tox-form").find("input").eq(1).css({display:"none"});
        $(".tox-form").find("input").eq(2).css({display:"none"});
        $(".tox-form").find("input").eq(3).addClass("mce-perc-description");
        $(".tox-form").find("input").eq(6).addClass("mce-perc-title");

        var validator = function(pathItem) {
            return pathItem && pathItem.type == "percImageAsset"?null:"Please select an image.";
        };

        // to bind browse file click
        jQuery(".tox-browse-url").bind( "click", function() {
            var pathSelectionOptions = {
                okCallback: updateLinkData,
                dialogTitle: "Select an image",
                rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                initialPath: topFrJQ.cookie("perc-inlineimage-path"),
                selectedItemValidator:validator,
                acceptableTypes:"percImageAsset,site,Folder",
                createNew:{"label":"Upload", "iconclass":"icon-upload-alt", "onclick":openCreateImageDialog}
            };
            topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions);
        });




    }

    editor.ui.registry.addButton('image', {
        icon: 'image',
        tooltip: 'Insert/edit image',
        onAction: showDialog,
        stateSelector: 'img:not([data-mce-object])'
    });

    editor.ui.registry.addMenuItem('image', {
        icon: 'image',
        text: 'Insert image',
        onAction: showDialog,
        context: 'insert',
        prependToContext: true
    });

    editor.on('dblclick', function(e) {
        e = e.target;
        if (e.nodeName === 'IMG' && (editor.dom.hasClass(e,'perc-notpubliclink' ) || editor.dom.hasClass(e,'perc-CustomPubliclink' )  )) {
            editor.selection.select(e);
            showDialog();
        }
});
});