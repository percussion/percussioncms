/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

tinymce.EditorManager.settings =
	{
		"selector": "textarea#elm1",
		// General options
		"theme" : "silver",
		"plugins" : '-example', // - tells TinyMCE to skip the loading of the plugin
		"branding": false,
		// when tinymce is initialized, setup event handlers to detect changes and set dirty flag
		// Theme options
		"menubar": "null",
		"toolbar": ["bold italic underline undo redo styleselect "],
		"autosave_ask_before_unload": false,
		// Example content CSS (should be your site CSS)
		"content_css" : "../sys_resources/css/tinymce/content.css",
		"auto_focus": 'elm1',
		setup: function (editor) {
			editor.on('click', function () {
				console.log('Editor was clicked');
			});
		}
	};
