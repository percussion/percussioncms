tinymce.PluginManager.add('rxinserthtml', function(editor) {


    editor.ui.registry.addButton('rxinserthtml', {
        tooltip: 'Insert HTML Fragment...',
		icon: '../rx_resources/images/inserthtml.gif',
        onAction: function() {
            win = editor.windowManager.open({
                    title: 'Insert HTML Fragment...',
                    size: 'normal',
                    data: {},
                    body: {
                        type: 'panel', // root body panel
                        items: [
                            {type: 'textarea', name: 'content'}
                        ]},
                    onSubmit: function(e) {
                        // Insert content when the window form is submitted
                        editor.insertContent(e.getData().content);
                        win.close();
                    },
                    buttons: [
                        { type: 'cancel', text: 'Close' },
                        { type: 'submit', text: 'Save', primary: true}
                    ]
                }

            );
        }
    });

    // Adds a menu item to the tools menu
    editor.ui.registry.addMenuItem('rxinserthtml', {
        text: 'Insert HTML Fragment...',
		icon: '../rx_resources/images/inserthtml.gif',
        shortcut: 'Meta+Shift+H',
        context:'insert',
        onAction: function() {
            win = editor.windowManager.open({
                title: 'Insert HTML Fragment...',
                data: {},
                size: 'normal',
                body: {
                    type: 'panel', // root body panel
                    items: [
                        {type: 'textarea', name: 'content'}
                    ]},
                onSubmit: function(e) {
                    // Insert content when the window form is submitted
                    editor.insertContent(e.getData().content);
                    win.close();
                },
                buttons: [
                    { type: 'cancel', text: 'Close' },
                    { type: 'submit', text: 'Save', primary: true}
                ]
            });
        },
    });

    // Adds rxhyperlink keyboard shortcut
    editor.shortcuts.add('ctrl+shift+h','rxinserthtml', function() {
        win =  editor.windowManager.open({
            title: 'Insert HTML Fragment...',
            size: 'normal',
            data: {},
            body: {
                type: 'panel', // root body panel
                items: [
                    {type: 'textarea', name: 'content'}
                ]},
            onSubmit: function(e) {
                // Insert content when the window form is submitted
                editor.insertContent(e.getData().content);
                win.close();
            },
            buttons: [
                { type: 'cancel', text: 'Close' },
                { type: 'submit', text: 'Save', primary: true}
            ]
        });
    });

});