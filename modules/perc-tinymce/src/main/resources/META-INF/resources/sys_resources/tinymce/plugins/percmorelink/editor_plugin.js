/**
 * More link used for expanding blog posts - when button is press span tag gets added as location of cursor.
 */
(function() {
	tinymce.create('tinymce.plugins.MoreLink', {
		init : function(ed,url) {
            var ml = '<img src="' + url + '/img/ReadMoreInsert-1.png" class="perc-blog-more-link mceItemNoResize" />';
            var cls = 'perc-blog-more-link';
            var sep = '<!-- morelink -->';
            var mlRE = new RegExp(sep.replace(/[\?\.\*\[\]\(\)\{\}\+\^\$\:]/g, function(a) {return '\\' + a;}), 'g');

            // Register commands
			ed.addCommand('mceInsertMoreLink', function() {
				ed.execCommand('mceInsertContent', 0, ml);
			});
            
            // Register button
			ed.addButton('morelink',
				{   title : "More link",
					icon: "MoreLinkIcon",
					onAction: function () {
						editor.execCommand('mceInsertMoreLink');
					}
				});
            
            ed.onInit.add(function() {
				if (ed.settings.content_css !== false)
					ed.dom.loadCSS(url + "/css/moreLink.css");

				if (ed.theme.onResolveName) {
					ed.theme.onResolveName.add(function(th, o) {
						if (o.node.nodeName == 'IMG' && ed.dom.hasClass(o.node, cls))
							o.name = 'morelink';
					});
				}
			});
            
            ed.onClick.add(function(ed, e) {
				e = e.target;
				if (e.nodeName === 'IMG' && ed.dom.hasClass(e, cls))
					ed.selection.select(e);
			});

			ed.onNodeChange.add(function(ed, cm, n) {
				cm.setActive('morelink', n.nodeName === 'IMG' && ed.dom.hasClass(n, cls));
			});

			ed.onBeforeSetContent.add(function(ed, o) {
				o.content = o.content.replace(mlRE, ml);
			});

			ed.onPostProcess.add(function(ed, o) {
				if (o.get)
					o.content = o.content.replace(/<img[^>]+>/g, function(im) {
						if (im.indexOf('class="perc-blog-more-link') !== -1)
							im = sep;
						return im;
					});
			});
            
		},
        //Setup required info
		getInfo : function() {
			return {
				longname : 'Insert more link',
				author : 'Percussion Software Inc.',
				authorurl : 'https://www.percussion.com',
				infourl : 'https://percussioncmshelp.intsof.com/cms',
				version : tinymce.majorVersion + "." + tinymce.minorVersion
			};
		},

	});

	// Register plugin
	tinymce.PluginManager.add('morelink', tinymce.plugins.MoreLink);
})();
