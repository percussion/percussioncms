tinymce.PluginManager.add('percglobalvariables', function(editor, url) {
    var cls = 'perc-global-variable-marker';
    // Register commands
    editor.addCommand('mceInsertGlobalVariables', function() {
        //Insert code here

        //Records caret position for IE
        if (tinymce.Env.ie){
            actualCaretPositionBookmark = tinyMCE.activeEditor.selection.getBookmark();
        }

        var mainEditor = editor.contentWindow.parent;
        if(!mainEditor.jQuery || !mainEditor.jQuery.topFrameJQuery || typeof PercGlobalVariablesData == "undefined")
        {
            alert("Error occurred accessing global variables.");
            return;
        }
        var topFrJQ = mainEditor.jQuery.topFrameJQuery;
        var dialogHtml = '<div><div id="perc-global-variables-wrapper"><table id="perc-global-variables-table"><tbody><tr class="perc-row-0"><th scope="col" align="left" class="perc-column-0 perc-global-variables-column-header">Variable name</th><th scope="col" class="perc-column-1 perc-global-variables-column-header">Variable value</th></tr>';
		var counter2 = 0;
        topFrJQ.each(PercGlobalVariablesData, function(varName, varValue){

            dialogHtml += '<tr class="perc-wfconfig-row perc-gv-row"><td align:"left"="" class="perc-column-0 perc-global-variables-column"><input type="radio" class="perc-global-variables-radio" style="width:auto;"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-name">' + varName + '</span></td><td align:"left"="" class="perc-column-1 perc-global-variables-column"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-value">' + varValue + '</span></td></tr>';

			if(counter2 == 0){
				dialogHtml += '<tr class="perc-wfconfig-row perc-gv-row"><td scope = "row" align:"left"="" class="perc-column-0 perc-global-variables-column"><input type="radio" class="perc-global-variables-radio" style="width:auto;"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-name">' + varName + '</span></td><td align:"left"="" class="perc-column-1 perc-global-variables-column"><span class="perc-roleName-wrapper perc-ellipsis perc-gv-value">' + varValue + '</span></td></tr>';
			}

			counter2++;
        });
        dialogHtml += '</table></div></div>';
        // if we are in the new blog post dialog, the width is
        var dialogWidth = 700;
        var dialogHeight = 300;
        var dialog = topFrJQ(dialogHtml).perc_dialog({
            title: "Insert Global Variable",
            buttons: {},
            percButtons:{
                "Save":{
                    click: function(){
                        var gvRow = dialog.find(".perc-global-variable-selected");
                        if(gvRow.length < 1)
                        {
                            alert("Please select a global variable to insert.");
                            return;
                        }
                        var varName = gvRow.find(".perc-gv-name").html();
                        var varValue = PercGlobalVariablesData[varName];
                        var urlToServlet = window.location.protocol+"//"+window.location.host + "/textToImage?imageText=&lt; " + encodeURIComponent(varValue) + " &gt;";
                        var gvhtml = "<img class='perc-global-variable-marker' title='"+varName+"' src=\"" + urlToServlet + "\">";
                        //Fix for IE losing cursor position on insert
                        if (tinymce.Env.ie) {
                            tinyMCE.activeEditor.selection.moveToBookmark(actualCaretPositionBookmark);
                            editor.execCommand('mceInsertContent', 0, gvhtml);
                        }
                        else{
                            editor.execCommand('mceInsertContent', 0, gvhtml);
                        }
                        dialog.remove();
                    },
                    id: "perc-edit-global-variables-insert"
                },
                "Cancel":{
                    click: function(){
                        dialog.remove();
                    },
                    id: "perc-edit-global-variables-cancel"
                }
            },
            id: "perc-insert-global-variables-dialog",
            width: dialogWidth,
            modal: true
        });
        dialog.find(".perc-gv-row").on('click',(function(){
            dialog.find(".perc-global-variable-selected").removeClass("perc-global-variable-selected");
            dialog.find("input:radio").attr('checked',false);
            $(this).addClass("perc-global-variable-selected").find('td').addClass("perc-global-variable-selected");
            $(this).addClass("perc-global-variable-selected").find('td').find("input:radio").attr('checked',true);
        }));
    });

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

    editor.ui.registry.addButton('percglobalvariables', {
        tooltip: 'Global variables',
        icon: 'character-count',
        onAction:function () {
            editor.execCommand('mceInsertGlobalVariables');
        },
        onSetup: function (buttonApi) {
            var editorEventCallback = function (eventApi) {
                buttonApi.setDisabled(isRXEditor() === true );
            };
            editor.on('NodeChange', editorEventCallback);

            /* onSetup should always return the unbind handlers */
            return function (buttonApi) {
                editor.off('NodeChange', editorEventCallback);
            };
        },
        context: 'insert'
    });
    editor.ui.registry.addMenuItem('percglobalvariables', {
        text: 'Global variables',
        icon: 'character-count',
        onAction:function () {
            editor.execCommand('mceInsertGlobalVariables');
        },
        onSetup: function (buttonApi) {
            if (isRXEditor() === false) {
                buttonApi.setDisabled(false);
            } else {
                buttonApi.setDisabled(true);
            }
        },

        context: 'insert'
    });

    editor.on('click', function(e) {
        e = e.target;

        if (e.nodeName === 'IMG' && editor.dom.hasClass(e, cls)) {
            editor.selection.select(e);
        }
    });

    editor.on('BeforeSetContent', function(o) {
        o.content = o.content.replace(/<span[^>]*?(.*?)<\/span>/g, function(im) {
            if (im.indexOf('class="perc-global-variable-marker') !== -1 && typeof PercGlobalVariablesData != 'undefined'){
                var varName = $(im).attr("title");
                var varValue = PercGlobalVariablesData[varName];
                if(varValue)
                {
                    var urlToServlet = window.location.protocol+"//"+window.location.host + "/textToImage?imageText=&lt; " + encodeURIComponent(varValue) + " &gt;";
                    var gvhtml = "<img class='perc-global-variable-marker' title='"+varName+"' src=\"" + urlToServlet + "\">";

                    im = gvhtml;
                }
            }
            return im;
        });
    });

    editor.on('PostProcess', function(o) {
        if (o.get)
        {
            o.content = o.content.replace(/<img[^>]+>/g, function(im) {
                if (im.indexOf('class="perc-global-variable-marker') !== -1){
                    var varName = $(im).attr("title");
                    var varValue = PercGlobalVariablesData[varName];
                    var gvhtml = '<span class="perc-global-variable-marker" title="' + varName + '">' + varValue + '</span>';
                    im = gvhtml;
                }
                return im;
            });
        }
    });

    editor.on('PreInit', function() {
        if (editor.settings.content_css !== false)
            editor.dom.loadCSS(url + "/css/globalVariables.css");

        if (editor.theme.onResolveName) {
            editor.theme.onResolveName.add(function(th, o) {
                if (o.node.nodeName == 'IMG' && editor.dom.hasClass(o.node, cls))
                    o.name = 'percglobalvariables';
            });
        }
    });
});