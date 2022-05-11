

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
                src: '/Rhythmyx/sys_resources/js/browser.js'
            });

            editor.getDoc().getElementsByTagName('head')[0].appendChild(scriptElm);
        }

    }

    // Adds a button to the toolbar
    editor.ui.registry.addButton('rxinlinelink', {
        icon: '/rx_resources/images/slink.gif',
        type: 'button',
        tooltip: 'Insert Inline Link',
        onAction: function() {
            createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
        }
    });

    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlinelink', {
        text: 'Inline Link',
        shortcut: 'Meta+Shift+L',
        icon: '/rx_resources/images/slink.gif',
        onAction: function() {
            createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
        },
        context: 'insert',
        prependToContext: true
    });

    // Adds rxhyperlink keyboard shortcut
    editor.shortcuts.add('ctrl+shift+l','rxhyperlink', function() {
        createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
    });


    // Adds a button to the toolbar
    editor.ui.registry.addButton('rxinlinetemplate', {
        icon: '/rx_resources/images/variant.gif',
        tooltip: 'Insert Inline Template',
        onAction: function() {
            createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
        }
    });

    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlinetemplate', {
        text: 'Inline Template',
        shortcut: 'Meta+Shift+T',
        icon: '/rx_resources/images/variant.gif',
        onAction: function() {
            createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
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
        icon: '/rx_resources/images/inlineimage.gif',
        tooltip: 'Insert Inline Image',
        onAction: function() {
            createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
        }
    });


    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinlineimage', {
        text: 'Inline Image',
        shortcut: 'Meta+Shift+I',
        icon: '/rx_resources/images/inlineimage.gif',
        onAction: function() {
            createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
        },
        context: 'insert',
        prependToContext: true
    });

    // Adds rximage keyboard shortcut
    editor.shortcuts.add('ctrl+shift+i','rximage', function() {
        createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
    });



    function insertInlineText(returnedHTML)
    {
        tinyMCE.activeEditor.selection.setContent(returnedHTML);
    }

});


