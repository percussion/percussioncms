(function ($) {
    var _ = $.layout;

// make sure the callbacks branch exists
    if (!_.callbacks)
        _.callbacks = {};

// this callback is bound to resize all container type widgets with the layout
    _.callbacks.resizeJQuery = function (x, ui) {
        var $P = ui.jquery ? ui : $(ui.newPanel || ui.panel);
        // find all VISIBLE layouts inside this pane/panel and resize them
        $P.filter(":visible").find(".ui-layout-container:visible").andSelf().each(function () {
            // alert('resizing self and innards');
            var layout = $(this).data("layout");
            if (layout) {
                layout.options.resizeWithWindow = false; // set option just in case not already set
                layout.resizeAll();
            }
        });
        /**
         * Resize UI Accordion
         */
        // find all VISIBLE accordions inside this pane and resize them
        $P.find(".ui-accordion").each(function () {
            //  alert('found an accordion');
            var $E = $(this);
            if ($E.data("accordion"))  // jQuery < 1.9
                $E.accordion("resize");
            if ($E.data("ui-accordion")) // jQuery >= 1.9
            {
                $E.accordion("refresh");
                //   alert('resize accordion');
            }
        });
        
        
        
        // may be called EITHER from layout-pane.onresize OR tabs.show
        var oPane = ui.jquery ? ui[0] : ui.panel;
        // cannot resize if the pane is currently closed or hidden
        if (!$(oPane).is(":visible"))
            return;
        // find all data tables inside this pane and resize them
        $($.fn.dataTable.fnTables(true)).each(function (i, table) {
            if ($.contains(oPane, table)) {
                $(table).dataTable().fnAdjustColumnSizing();
            }
        });
        
        
    };
})(jQuery);