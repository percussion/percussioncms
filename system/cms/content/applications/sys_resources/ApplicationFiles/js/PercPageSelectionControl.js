/**
 * Page selection control, works with the sys_PagePath control.
 * Opens path selection dialog for image selection.
 */
(function($)
{
    $(document).ready(function(){
        $(".perc-page-field-select-button").on("click", function(){
            var $el = $(this);
            var pageInputName = $el.attr("for");
            var intialPath = $("#" + pageInputName ).val();
            if(intialPath.trim() === "")
                intialPath = $.topFrameJQuery.cookie("perc-pagefield-path");
            var updatePageData = function(pathItem){
                var path = pathItem.path;
                //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
                if(!path){
                    path ="/site";
                }

                //Save the path to cookie
                $.topFrameJQuery.cookie("perc-pagefield-path", path);
                $("#" + pageInputName ).val(path).attr("title",path);
                if(intialPath != path){
                    $("#" + pageInputName + "_linkId").val(path);
                }

                // Save the page content id to data attribute and trigger a change event for
                // Other scripts that might be listening for an event change
                $("#" + pageInputName ).attr("data-perc-page-content-id",pathItem.id.split(/[- ]+/).pop())
                    .trigger("change");

            };

            var validator = function(pathItem){
                return pathItem && pathItem.type === "percPage"?null:"Please select a page.";
            };
            var pathSelectionOptions = {
                okCallback: updatePageData,
                dialogTitle: "Select a page",
                rootPath:$.topFrameJQuery.PercFinderTreeConstants.ROOT_PATH_SITES,
                initialPath: intialPath,
                selectedItemValidator:validator
            };
            $.topFrameJQuery.PercPathSelectionDialog.open(pathSelectionOptions);
        });

        $(".perc-page-field-clear-button").on("click", function() {
            var $el = $(this);
            var pageInputName = $el.attr("for");
            var intialPath = $("#" + pageInputName ).val();
            if(intialPath.trim() === "") {
                return;
            }

            $('#' + pageInputName).val("");
        });

    });
})(jQuery);
