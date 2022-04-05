

tinymce.PluginManager.add('rxinline', function(editor, url) {



	var tinyMCEinlineLinkSlot = tinymce.trim(editor.getParam("inlineLinkSlot", "103"));
	var tinyMCEinlineVariantSlot = tinymce.trim(editor.getParam("inlineVariantSlot", "105"));
    var tinyMCEinlineImageSlot = tinymce.trim(editor.getParam("inlineImageSlot", "104"));
	
    var ctypeid = document.forms.EditForm.sys_contenttypeid.value;
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
    editor.addButton('rxinlinelink', {
	image: '../rx_resources/images/slink.gif',
        tooltip: 'Insert Inline Link',
        onclick: function() {
		createInlineSearchBox("rxhyperlink",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineLinkSlot,ctypeid,insertInlineText);
	}
    });

    // Adds a menu item to the tools menu
    editor.addMenuItem('rxinlinelink', {
        text: 'Inline Link',
		shortcut: 'Meta+Shift+L',
		image: '../rx_resources/images/slink.gif',
        onclick: function() {
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
    editor.addButton('rxinlinetemplate', {
		image: '../rx_resources/images/variant.gif',
		tooltip: 'Insert Inline Template',
		onclick: function() {
			createInlineSearchBox("rxvariant",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineVariantSlot,ctypeid,insertInlineText);
			}
    });	

     // Adds a menu item to the tools menu
    editor.addMenuItem('rxinlinetemplate', {
        text: 'Inline Template',
		shortcut: 'Meta+Shift+T',
		image: '../rx_resources/images/variant.gif',
        onclick: function() {
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
    editor.addButton('rxinlineimage', {
		image: '../rx_resources/images/inlineimage.gif',
		tooltip: 'Insert Inline Image',
		onclick: function() {
			createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
			}
    });


     // Adds a menu item to the tools menu
    editor.addMenuItem('rxinlineimage', {
        text: 'Inline Image',
		shortcut: 'Meta+Shift+I',
		image: '../rx_resources/images/inlineimage.gif',
        onclick: function() {
                createInlineSearchBox("rximage",tinyMCE.activeEditor.selection.getContent(),tinyMCEinlineImageSlot,ctypeid,insertInlineText);
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


