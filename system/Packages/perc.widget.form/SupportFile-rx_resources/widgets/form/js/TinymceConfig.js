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

tinymce.EditorManager.settings =
	{
			selector: "textarea#elm1",
			// General options
            branding: false,
			theme : "modern",
			plugins : '-example', // - tells TinyMCE to skip the loading of the plugin 
		    skin : "lightgray",
		    skin_variant : "black",
			// Theme options
			menubar: "null",
			toolbar: "bold,italic,underline,undo,redo,formatselect",
			theme_advanced_buttons1 : "bold,italic,underline,undo,redo,formatselect",
			theme_advanced_buttons2 : "", 
			theme_advanced_buttons3 : "",
			theme_advanced_buttons4 : "",
			theme_advanced_toolbar_location : "top",
			theme_advanced_toolbar_align : "left",
            autosave_ask_before_unload: false,
			theme_advanced_statusbar_location : "bottom",
			theme_advanced_resizing : true,
            theme_advanced_blockformats : "p,h1,h2,h3,h4,h5,h6",
			// Example content CSS (should be your site CSS)
            content_css : "../sys_resources/css/tinymce/content.css",
			auto_focus: 'elm1',
			setup: function (editor) {
				editor.on('click', function () {
					console.log('Editor was clicked');
				});
			}
		};
