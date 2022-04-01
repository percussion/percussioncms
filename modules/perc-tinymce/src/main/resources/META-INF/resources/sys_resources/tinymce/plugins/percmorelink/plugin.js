tinymce.PluginManager.add('percmorelink', function(editor, url) {
	var cls = 'perc-blog-more-link', sep = '<!-- morelink -->', mlRE;
	var ml = '<img src="' + url + '/img/ReadMoreInsert-1.png" class="perc-blog-more-link mceItemNoResize" />';

    mlRE = new RegExp(sep.replace(/[\?\.\*\[\]\(\)\{\}\+\^\$\:]/g, function(a) {return '\\' + a;}), 'g');

	// Register commands
	editor.addCommand('mceInsertMoreLink', function() {

	});

	editor.ui.registry.addMenuItem('percmorelink', {
		text: 'More link',
		icon: 'morelink',
		onAction: function () {
            editor.execCommand('mceInsertContent', false, ml);
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
