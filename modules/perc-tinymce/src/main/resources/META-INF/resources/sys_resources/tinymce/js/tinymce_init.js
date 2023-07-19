
function percTinyMceInitialized(tinymceEditor) {

    var mainEditor = window.parent;

    if (!mainEditor.jQuery)
        return;
    var topFrJQ = mainEditor.jQuery;
    if (topFrJQ.PercDirtyController) {
        dirtyController = topFrJQ.PercDirtyController;
        // if user types into tiny mce, the asset is dirty until saved
        tinymceEditor.on('keypress', function() {
            dirtyController.setDirty(true, "asset");
            tinymceEditor.off('keypress');
        });
	}

        // This event listener creates a tooltip when hovering over a broken image
        tinymceEditor.on("MouseOver", function(e) {
            $target = $(e.target);
            if($target.is('.perc-brokenlink, .perc-notpubliclink')) {
                $itemTarget = $target;
                itemJcrPath = $itemTarget.data('jcrpath');
                itemObject = $itemTarget.data('pathitem');


            }
        });

        tinymceEditor.ui.registry.addMenuItem('useBrowserSpellcheck', {
            text: 'Use `Ctrl+Right click` to access spellchecker',
            icon:'spell-check',
            onAction: function () {
                tinymceEditor.notificationManager.open({
                    text: 'Spellchecker - hold the Control (Ctrl) key and right-click on the misspelt word.',
                    type: 'info',
                    timeout: 5000,
                    closeButton: true
                });
            }
        });

        tinymceEditor.ui.registry.addButton('useBrowserSpellcheck', {
            text: 'Use `Ctrl+Right click` to access spellchecker',
            icon:'spell-check',
            type: 'button',
            onAction: function () {
                tinymceEditor.notificationManager.open({
                    text: 'Spellchecker - hold the Control (Ctrl) key and right-click on the misspelt word.',
                    type: 'info',
                    timeout: 5000,
                    closeButton: true
                });
            }
        });

        tinymceEditor.ui.registry.addContextMenu('useBrowserSpellcheck', {
            update: function (node) {
                return tinymceEditor.selection.isCollapsed() ? ['useBrowserSpellcheck'] : [];
            }
        });


}

function openEditor(itemObject, itemJcrPath) {
    editorUrl = buildEditorUrl(itemObject, itemJcrPath);
    window.open(editorUrl);
}

function buildEditorUrl(itemObject, itemJcrPath) {
    itemId = itemObject.id;
    itemName = itemObject.name;
    itemPath = encodeURIComponent(itemJcrPath);
    itemView = (itemObject.type == 'percPage') ? 'editor' : 'editAsset';

    url = `/cm/app?view=${itemView}&mode=readonly&id=${itemId}&name=${itemName}&path=${itemPath}`;

    return url;

}

function mergeConfig(options, url) {
    return new Promise(function(resolve, reject) {
        $.ajax({
            url: url,
            dataType: 'json',
            success: function(data) {
                var config = {};
                var css_path = options.content_css;
                var external_plugins = options.external_plugins;
                if (Array.isArray(data)) {
                    $.each(data, function(i, item) {
                        if (
                            (!item.hasOwnProperty('roles') || $(options.userRoles).not(item.roles).length < $(options.userRoles).length) &&
                            (!item.hasOwnProperty('communityNames') || $.inArray(options.community, item.communityNames) >= 0) &&
                            (!item.hasOwnProperty('fields') || $.inArray(options.typeName, item.fields) >= 0 || $.inArray(options.typeName + "." + options.fieldName, item.fields) >= 0)
                        ) {
                            config = $.extend({},config,item);
                        }
                    });
                } else if (data != null) {
                    config = data;

                }

                if (config!=null)
                {
                    if (config.hasOwnProperty('content_css')) {
                        config.content_css = css_path + "," + config.content_css;
                    }
                    if (config.hasOwnProperty('external_plugins'))
                    {
                        config.external_plugins = $.extend({},config.external_plugins, external_plugins);
                    }

                }

                options = $.extend({}, options, config);

                if (options.hasOwnProperty('langmap')) {
                    var langmap = options.langmap;
                    var perc_locale = options.perc_locale;

                    if (langmap.hasOwnProperty(perc_locale)) {
                        var language = langmap[perc_locale];
                        options.language = language;
                    }
                }
                resolve(options);

            },
            error: function(xhr, status, error) {
                options.error = "Cannot load TinyMCE config " + url + " status=" + status +" error="+error;
                reject(options);
            }
        });
    });
}

function getBaseConfig(parameters) {
    var options = parameters[0];
    var styleFormats = parameters[1];
    return new Promise(function(resolve, reject) {

        var mergedBaseOptions = $.extend({}, {
            "branding": false,
            "selector": "textarea.tinymce",
            "autosave_interval": "10m",
            "autosave_retention": "1440m",
            "link_context_toolbar": true,
            "perc_config": "../sys_resources/tinymce/config/default_config.json",
            "content_css": "../sys_resources/css/tinymce/content.css",
            "theme": "silver",
            "valid_elements": "*[*]",
            "noneditable_leave_contenteditable": true,
            "height": options.height,
            "width": "100%",
            "table_sizing_mode": "relative",
            "external_plugins": {
                'codemirror': '/sys_resources/tinymce/plugins/codemirror/plugin.js',
                'percadvimage': '/sys_resources/tinymce/plugins/percadvimage/plugin.js',
                'percadvlink': '/sys_resources/tinymce/plugins/percadvlink/plugin.js',
                'percglobalvariables': '/sys_resources/tinymce/plugins/percglobalvariables/plugin.js',
                'percmorelink': '/sys_resources/tinymce/plugins/percmorelink/plugin.js',
                'rxinline':'/sys_resources/tinymce/plugins/rxinline/plugin.js',
                'rxinserthtml':'/sys_resources/tinymce/plugins/rxinserthtml/plugin.js',

            },
            "codemirror": {
                "indentOnInit": true, // Whether or not to indent code on init.
                "fullscreen": false,   // Default setting is false
                "saveCursorPosition": true,
                "config": {
                    "lineNumbers": true,
                    "autofocus": true,
                    "screenReaderLabel":'HTML Source Code Editor',
                }

            },
            "style_formats_merge": true,
            "style_formats": styleFormats,
            "autosave_restore_when_empty": false,
            "init_instance_callback": "percTinyMceInitialized",
            "file_picker_callback": function(callback, value, meta) {
                var mainEditor = window.parent;
                var topFrJQ = mainEditor.jQuery;
                var filePickerDialog;
                var imagePickerDialog;

                var parentDialog = jQuery('[type=\'url\']').find(meta.fieldName);
                // Provide file and text for the link dialog
                if (meta.filetype == 'file') {
                    var pathSelectionOptions = {
                        okCallback: function(selectedItem){
                            tinymce.execCommand('updateFileSelection',false,selectedItem);
                        },
                        dialogTitle: I18N.message("perc.ui.widget.tinymce@Please select"),
                        rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ALL,
                        initialPath: topFrJQ.cookie('perc-inlinelink-path'),
                        selectedItemValidator: function(pathItem){
                            return pathItem && (pathItem.type === 'percPage' || pathItem.type === 'percImageAsset' || pathItem.type === 'percFileAsset')?null:'Please select a page, file, or an image';
                        },
                        acceptableTypes:'percPage,percImageAsset,percFileAsset,site,Folder'
                    };

                    filePickerDialog = topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions,callback);


                }

                // Provide image and alt text for the image dialog
                if (meta.filetype == 'image') {

                    var openCreateImageDialog = function(successCallback, cancelCallback) {
                        $.topFrameJQuery.PercCreateNewAssetDialog('percImage', successCallback, cancelCallback);
                    };
                    var validator = function (pathItem) {
                        return pathItem && pathItem.type === 'percImageAsset' ? null : 'Please select an image.';
                    };
                    var pathSelectionOptions2 = {
                        okCallback: function(selectedItem){
                            tinymce.execCommand('updateFileSelection',false,selectedItem);
                        },
                        dialogTitle: 'Select an image',
                        rootPath:topFrJQ.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                        initialPath: topFrJQ.cookie('perc-inlineimage-path'),
                        selectedItemValidator:validator,
                        acceptableTypes:'percImageAsset,site,Folder',
                        createNew:{'label':'Upload', 'iconclass':'icon-upload-alt', 'onclick':openCreateImageDialog}
                    };
                    imagePickerDialog = topFrJQ.PercPathSelectionDialog.open(pathSelectionOptions2);
                }

            },
            "convert_urls" : false,
        }, options);
        console.log("From base config="+JSON.stringify(mergedBaseOptions, null, 2));

        resolve(mergedBaseOptions);
    });
}

function getStyles(options)
{
    return new Promise(function(resolve, reject) {
        if (options.readonly) {
            resolve([options,[]]);
        } else {

            $.PercCustomStyleService.getCustomStyles(function(status, result) {
                styleFormats = status ? result : [];
                resolve([options,styleFormats]);
            });
        }
    });
}

function perc_tinymce_init(options) {


    load_type_config = function(options) {
        console.log("load type config="+JSON.stringify(options, null, 2));
        return mergeConfig(options, options.perc_config);
    };

    load_user_config = function(options) {
        console.log("load user config="+JSON.stringify(options, null, 2));
        return mergeConfig(options, "../rx_resources/tinymce/config/customer_config_override.json");
    };

    fail_type_config = function(options) {
        return new Promise(function(resolve, reject) {
            alert(options.error);
            reject(options);
        });
    };

    fail_user_config = function(options) {
        return new Promise(function(resolve, reject) {
            alert(options.error);
            resolve(options);
        });
    };

    init_tinymce = function(options) {
        return new Promise(function(resolve, reject) {
            console.log("Effective TinyMCE config1="+JSON.stringify(options, null, 2));
            tinyMCE.init(options);
            resolve();
        });
    };

    console.log("From control config1="+JSON.stringify(options, null, 2));

    getStyles(options)
        .then(getBaseConfig)
        .then(load_type_config)
        .then(load_user_config, fail_type_config)
        .catch(fail_user_config)
        .then(init_tinymce);

}
