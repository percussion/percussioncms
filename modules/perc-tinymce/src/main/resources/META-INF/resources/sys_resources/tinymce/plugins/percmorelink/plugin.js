tinymce.PluginManager.add('percmorelink', function(editor, url) {
	var cls = 'perc-blog-more-link', sep = '<span class="perc-blog-more-link" id="perc-blog-more-link"/>', mlRE;
	var ml = '<img src="' + url + '/img/ReadMoreInsert-1.png" class="perc-blog-more-link mceItemNoResize" />';

    mlRE = new RegExp(sep.replace(/[\?\.\*\[\]\(\)\{\}\+\^\$\:]/g, function(a) {return '\\' + a;}), 'g');

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

	// Register commands
	editor.addCommand('mceInsertMoreLink', function() {
			editor.execCommand('mceInsertContent', false,ml);
	});

	editor.ui.registry.addMenuItem('percmorelink', {
		text: 'More link',
		icon: 'more-drawer',
		onAction: function () {
            editor.execCommand('mceInsertContent',false, ml);
		},
		onSetup: function (buttonApi) {
			if (isRXEditor() === false) {
				buttonApi.setDisabled(false);
			} else {
				buttonApi.setDisabled(true);
			}
		},
     	separator: 'before',
    	context: 'insert'
	});


    editor.ui.registry.addButton('percmorelink', {
        icon: 'more-drawer',
        type: 'button',
        tooltip: 'More Link',
        onAction: function () {
            editor.execCommand('mceInsertContent', false,ml);
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
        separator: 'before',
    	context: 'insert'
	});

	editor.on('ResolveName', function(e) {
		if (e.target.nodeName == 'IMG' && editor.dom.hasClass(e.target, cls)) {
			e.name = 'percmorelink';
		}
	});

	editor.on('click', function(e) {
		e = e.target;

		if (e.nodeName === 'IMG' && editor.dom.hasClass(e, cls)) {
			editor.selection.select(e);
		}
	});

	editor.on('BeforeSetContent', function(e) {
        e.content = e.content.replace(mlRE, ml);
    });
	
	editor.on('PostProcess', function(e) {
        if (e.get)
            e.content = e.content.replace(/<img[^>]+>/g, function(im) {
                if (im.indexOf('class="perc-blog-more-link') !== -1)
                    im = sep;
                return im;
            });
    });
	editor.on('setup', function(e) {
		if (e.get)
			e.content = e.content.replace(/<img[^>]+>/g, function(im) {
				if (im.indexOf('class="perc-blog-more-link') !== -1)
					im = sep;
				return im;
			});
	});

    editor.on('PreInit', function() {
        if (editor.settings.content_css !== false)
            editor.dom.loadCSS(url + "/css/moreLink.css");

        if (editor.theme.onResolveName) {
            editor.theme.onResolveName.add(function(th, o) {
                if (o.node.nodeName == 'IMG' && ed.dom.hasClass(o.node, cls))
                    o.name = 'percmorelink';
            });
        }
	});
});
