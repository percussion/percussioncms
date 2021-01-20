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

        // This event listener creates a tooltip when hovering over a broken image
        tinymceEditor.on("MouseOver", function(e) {
            $target = $(e.target);
            if($target.is('.perc-brokenlink, .perc-notpubliclink')) {
                $itemTarget = $target;
                itemJcrPath = $itemTarget.data('jcrpath');
                itemObject = $itemTarget.data('pathitem');
                var menu = new tinymce.ui.Menu({
                    classes: 'perc-broken-link-tooltip',
                    items: [{
                        text: $itemTarget.is('.perc-brokenlink') ? 'Deleted item referenced: ' + itemJcrPath : itemJcrPath,
                        onClick: function() {
                            if($itemTarget.is('.perc-notpubliclink')) {
                                openEditor(itemObject, itemJcrPath);
                            }
                        }
                    }],
                    context: "contextmenu",
                    onhide: function() { menu.remove(); }
                });

                menu.renderTo(document.body);

                var pos = tinymce.DOM.getPos(tinymceEditor.getContentAreaContainer());
                var targetPos = tinymceEditor.dom.getPos(e.target);
                var root = tinymceEditor.dom.getRoot();


                targetPos.x -= e.target.scrollLeft || root.scrollLeft;
                targetPos.y -= e.target.scrollTop || root.scrollTop;

                pos.x += targetPos.x;
                pos.y += targetPos.y;

                menu.moveTo(pos.x, pos.y + e.target.offsetHeight + 5);
            }
        });

    }
}

function openEditor(itemObject, itemJcrPath) {
    editorUrl = buildEditorUrl(itemObject, itemJcrPath);
    window.open(editorUrl);
}

function buildEditorUrl(itemObject, itemJcrPath) {
    itemId = itemObject.id;
    itemName = itemObject.name;
    itemPath = encodeURIComponent(itemJcrPath);
    itemView = (itemObject.type === 'percPage') ? 'editor' : 'editAsset';

    url = "/cm/app?view=${itemView}&mode=readonly&id=${itemId}&name=${itemName}&path=${itemPath}";

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
                if ($.isArray(data)) {
                    $.each(data, function(i, item) {
                        if (
                            (!item.hasOwnProperty('roles') || $(options.userRoles).not(item.roles).length < $(options.userRoles).length) &&
                            (!item.hasOwnProperty('communityNames') || $.inArray(options.community, item.communityNames) >= 0) &&
                            (!item.hasOwnProperty('fields') || $.inArray(options.typeName, item.fields) >= 0 || $.inArray(options.typeName + "." + options.fieldName, item.fields) >= 0)
                        ) {
                            config = $.extend({},config,item);
                        }
                    });
                } else if (data) {
                    config = data;

                }

                if (config)
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
                        options.language = langmap[perc_locale];
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
            "mode": "textareas",
            "autosave_interval": "10m",
            "autosave_retention": "1440m",
            "perc_config": "../sys_resources/tinymce/config/default_config.json",
            "content_css": "../sys_resources/css/tinymce/content.css",
            "theme": "modern",
            "editor_selector": "tinymce_callout",
            "valid_elements": "*[*]",
            "noneditable_leave_contenteditable": true,
            "autosave_ask_before_unload": false,
            "height": options.height,
            "width": "100%",
            "external_plugins": {
                'percadvimage': '/Rhythmyx/sys_resources/tinymce/plugins/percadvimage/plugin.min.js',
                'percadvlink': '/Rhythmyx/sys_resources/tinymce/plugins/percadvlink/plugin.min.js',
                'percglobalvariables': '/Rhythmyx/sys_resources/tinymce/plugins/percglobalvariables/plugin.min.js',
                'percmorelink': '/Rhythmyx/sys_resources/tinymce/plugins/percmorelink/plugin.min.js'
            },
            "style_formats_merge": true,
            "style_formats": styleFormats,
            "autosave_restore_when_empty": true,
            "init_instance_callback": "percTinyMceInitialized",
            "file_browser_callback": $.noop,
            "convert_urls" : false,
            "toolbar": "newdocument undo redo restoredraft | cut copy paste searchreplace | styleselect fontselect fontsizeselect forecolor backcolor removeformat | bold italic underline strikethrough superscript subscript | alignleft aligncenter alignright alignjustify alignnone |  bullist numlist outdent indent | link unlink openlink media | visualchars visualblocks fullscreen print preview | anchor charmap hr emoticons insertdatetime | table tabledelete tableinsertrowafter tabledeleterow tableinsertcolbefore tabledeletecol tablesplitcells tablemergecells | rxinlinelink rxinlinetemplate rxinlineimage rxinserthtml | ltr rtl codesample code"
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
        .then(load_type_config, fail_type_config)
        .then(load_user_config, fail_user_config)
        .then(init_tinymce);

}
