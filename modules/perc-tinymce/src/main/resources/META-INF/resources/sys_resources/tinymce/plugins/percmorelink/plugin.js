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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

tinymce.PluginManager.add('percmorelink', function(editor, url) {
	var cls = 'perc-blog-more-link', sep = '<!-- morelink -->', mlRE;
	var ml = '<img src="' + url + '/img/ReadMoreInsert-1.png" class="perc-blog-more-link mceItemNoResize" />';

    mlRE = new RegExp(sep.replace(/[\?\.\*\[\]\(\)\{\}\+\^\$\:]/g, function(a) {return '\\' + a;}), 'g');

	// Register commands
	editor.addCommand('mceInsertMoreLink', function() {
		editor.execCommand('mceInsertContent', 0, ml);
	});

	editor.addMenuItem('percmorelink', {
		text: 'More link',
		icon: 'morelink',
		cmd: 'mceInsertMoreLink',
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
