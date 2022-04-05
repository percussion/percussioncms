tinymce.PluginManager.add('rxinserthtml', function(editor, url) {

    editor.addButton('rxinserthtml', {
        tooltip: 'Insert HTML Fragment...',
		image: '../rx_resources/images/inserthtml.gif',
        onclick: function() {
               editor.windowManager.open({
        title: 'Insert HTML Fragment...',
		width: 600,
        height: 400,
        body: [
          {type: 'textbox', name: 'content', multiline: true, style:'height:345px'}
        ],
        onsubmit: function(e) {
          // Insert content when the window form is submitted
          editor.insertContent(e.data.content);
        }
      });
        }
    });

    // Adds a menu item to the tools menu
    editor.addMenuItem('rxinserthtml', {
        text: 'Insert HTML Fragment...',
		image: '../rx_resources/images/inserthtml.gif',
		shortcut: 'Meta+Shift+H',
		context:'insert',
        onclick: function() {
               editor.windowManager.open({
        title: 'Insert HTML Fragment...',
		width: 600,
        height: 400,
        body: [
          {type: 'textbox', name: 'content', multiline: true, style:'height:345px'}
        ],
        onsubmit: function(e) {
          // Insert content when the window form is submitted
          editor.insertContent(e.data.content);
        }
      });
        },
    });
	
	// Adds rxhyperlink keyboard shortcut
    editor.shortcuts.add('ctrl+shift+h','rxinserthtml', function() {		
               editor.windowManager.open({
        title: 'Insert HTML Fragment...',
		width: 600,
        height: 400,
        body: [
          {type: 'textbox', name: 'content', multiline: true, style:'height:345px'}
        ],
        onsubmit: function(e) {
          // Insert content when the window form is submitted
          editor.insertContent(e.data.content);
        }
      });	
	});
	
});