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

/**
 * More link used for expanding blog posts - when button is press span tag gets added as location of cursor.
 */
(function() {
	tinymce.create('tinymce.plugins.MoreLink', {
		init : function(ed, url) {
            var ml = '<img src="' + url + '/img/ReadMoreInsert-1.png" class="perc-blog-more-link mceItemNoResize" />';
            var cls = 'perc-blog-more-link';
            var sep = '<!-- morelink -->';
            var mlRE = new RegExp(sep.replace(/[\?\.\*\[\]\(\)\{\}\+\^\$\:]/g, function(a) {return '\\' + a;}), 'g');

            // Register commands
			ed.addCommand('mceInsertMoreLink', function() {
				ed.execCommand('mceInsertContent', 0, ml);
			});
            
            // Register button
			ed.addButton('morelink', {title : "More link", image: url + "/img/MoreLinkIcon.gif", cmd : 'mceInsertMoreLink'});
            
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
				infourl : 'https://help.percussion.com/cms',
				version : tinymce.majorVersion + "." + tinymce.minorVersion
			};
		},

	});

	// Register plugin
	tinymce.PluginManager.add('morelink', tinymce.plugins.MoreLink);
})();
