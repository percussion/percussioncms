

tinymce.PluginManager.add('rxinline', function(editor) {



    var tinyMCEinlineLinkSlot = tinymce.trim(editor.getParam("inlineLinkSlot", "103"));
    var tinyMCEinlineVariantSlot = tinymce.trim(editor.getParam("inlineVariantSlot", "105"));
    var tinyMCEinlineImageSlot = tinymce.trim(editor.getParam("inlineImageSlot", "104"));

    var ctypeid;
    if( typeof document.forms.EditForm !== 'undefined' ){
        ctypeid= document.forms.EditForm.sys_contenttypeid.value;
    }
    var scriptId;

    function addBrowserjs() {
        // Include
        if (!scriptId)
        {
            scriptId = editor.dom.uniqueId();
            scriptElm = editor.dom.create('script', {
                id: scriptId,
                type: 'text/javascript',
                src: '/sys_resources/js/browser.js'
            });

            editor.getDoc().getElementsByTagName('head')[0].appendChild(scriptElm);
        }
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



    // Adds rxhyperlink keyboard shortcut
    editor.shortcuts.add('ctrl+shift+l','rxhyperlink', function() {
        createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
    });

    editor.ui.registry.addMenuItem('unlink', {
        icon: 'unlink',
        text: I18N.message("perc.ui.widget.tinymce@Remove links"),
        onAction: function () {
            editor.execCommand('unlink');
        },
        onSetup: function (buttonApi) {
			  if(isRXEditor() === false ){
			  buttonApi.setDisabled(true);
		  }else{
			   buttonApi.setDisabled(false);
		  }


        },
        stateSelector: 'a[href]',
        context: 'insert',
        prependToContext: true
    });

    editor.ui.registry.addButton('unlink', {
        icon: 'unlink',
        type: 'button',
        tooltip: I18N.message("perc.ui.widget.tinymce@Remove links"),
        onAction: function () {
            editor.execCommand('unlink');
        },
        onSetup: function (buttonApi) {
            var editorEventCallback = function (eventApi) {
                buttonApi.setDisabled(isRXEditor() === false );
            };
            editor.on('NodeChange', editorEventCallback);

            /* onSetup should always return the unbind handlers */
            return function (buttonApi) {
                editor.off('NodeChange', editorEventCallback);
            };
        },
        stateSelector: 'a[href]'
    });

    // Adds a button to the toolbar
    editor.ui.registry.addButton('rxinlinelink', {
        icon: 'slink',
        type: 'button',
        tooltip: 'Insert Inline Link',
        onAction: function() {
            createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
        },
        onSetup: function (buttonApi) {
            var editorEventCallback = function (eventApi) {
                buttonApi.setDisabled(isRXEditor() === false );
            };
            editor.on('NodeChange', editorEventCallback);

            /* onSetup should always return the unbind handlers */
            return function (buttonApi) {
                editor.off('NodeChange', editorEventCallback);
            };
        }
    });

    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlinelink', {
        text: 'Inline Link',
        shortcut: 'Meta+Shift+L',
        icon: 'slink',
        onAction: function() {
            createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
        },
        onSetup: function (buttonApi) {
            if(isRXEditor() === false ){
                buttonApi.hide();
            }else{
                buttonApi.show();
            }
        },
        context: 'insert',
        prependToContext: true
    });


    // Adds a button to the toolbar
    editor.ui.registry.addButton('rxinlinetemplate', {
        icon: 'variant',
        tooltip: 'Insert Inline Template',
        onAction: function() {
            createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
        },
	   onSetup: function (buttonApi) {
			var editorEventCallback = function (eventApi) {
			  buttonApi.setDisabled(isRXEditor() === false );
			};
			editor.on('NodeChange', editorEventCallback);

			/* onSetup should always return the unbind handlers */
			return function (buttonApi) {
			  editor.off('NodeChange', editorEventCallback);
			};
        }
    });

    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlinetemplate', {
        text: 'Inline Template',
        shortcut: 'Meta+Shift+T',
        icon: 'variant',
        onAction: function() {
            createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
        },
		onSetup: function (buttonApi) {
			  if(isRXEditor() === false ){
			  buttonApi.setDisabled(true);
		  }else{
			   buttonApi.setDisabled(false);
		  }
      },
        context: 'insert',
        prependToContext: true
    });

    // Adds rxvariant keyboard shortcut
    editor.shortcuts.add('ctrl+shift+t','rxvariant', function() {
        createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
    });

    // Adds a button to the toolbar
    editor.ui.registry.addButton('rxinlineimage', {
        icon: 'inlineimage',
        tooltip: 'Insert Inline Image',
        onAction: function() {
            createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
        },
		onSetup: function (buttonApi) {
			var editorEventCallback = function (eventApi) {
			  buttonApi.setDisabled(isRXEditor() === false );
			};
			editor.on('NodeChange', editorEventCallback);

			/* onSetup should always return the unbind handlers */
			return function (buttonApi) {
			  editor.off('NodeChange', editorEventCallback);
			};
        }
    });


    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlineimage', {
        text: 'Inline Image',
        shortcut: 'Meta+Shift+I',
        icon: 'inlineimage',
        onAction: function() {
            createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
        },
		onSetup: function (buttonApi) {
			  if(isRXEditor() === false ){
			  buttonApi.setDisabled(true);
              }else{
                   buttonApi.setDisabled(false);
              }
		  },
        context: 'insert',
        prependToContext: true
    });

    // Adds rximage keyboard shortcut
    editor.shortcuts.add('ctrl+shift+i','rximage', function() {
        createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
    });

	editor.on('init', addBrowserjs);

	// Workaround for tinymce bug links open in tinymce frame on JavaFX
    editor.on('click', function(e) {
        var elm = e.target;

        do {
            if (elm.tagName === 'A') {
                e.preventDefault();
                return;
            }
        } while ((elm = elm.parentNode));
    });


    function insertInlineText(returnedHTML)
    {
        tinyMCE.activeEditor.selection.setContent(returnedHTML);
    }

});


