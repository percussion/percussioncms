/**
 * Image selection control, works with the sys_ImagePath control.
 * Adds a click event to image browse button with class name perc-image-field-select-button
 * and gets the input field from for attribute.
 * Opens path selection dialog for image selection.
 */
(function($)
{
    $(document).ready(function(){
        $(".perc-image-field-select-button").on("click", function(){
            var $el = $(this);
            var imgInputName = $el.attr("for");
            var eventSource = $("#" + imgInputName ).attr("data-perc-widget-event-source");
            var intialPath = $("#" + imgInputName ).val();
            if(intialPath.trim() === "")
                intialPath = $.topFrameJQuery.cookie("perc-imagefield-path");
            var updateImageData = function(pathItem){
                var path = pathItem.path;
                //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
                if(!path){
                    path = pathItem.folderPaths.split("$System$")[1] + "/" + pathItem.name;
                }

                //Save the path to cookie
                $.topFrameJQuery.cookie("perc-imagefield-path", path);
                $("#" + imgInputName ).val(path).attr("title",path);
                if(intialPath != path){
                    $("#" + imgInputName + "_linkId").val(path);
                }

                // Save the image content id to data attribute and trigger an event change for other
                // scripts that might be listening for an event change
                $("#" + imgInputName ).attr("data-perc-image-content-id",pathItem.id.split(/[- ]+/).pop())
                    .trigger("change");

            }
            //Create new button click function. The success callback is called with PathItem, if the new image creation is successful
            //Otherwise cancelcall back is called.
            var openCreateImageDialog = function(successCallback, cancelCallback){
                $.topFrameJQuery.PercCreateNewAssetDialog("percImage", successCallback, cancelCallback);
            }
            var validator = function(pathItem){
                return pathItem && pathItem.type == "percImageAsset"?null:"Please select an image.";
            }

            var pathSelectionOptions = {
                okCallback: updateImageData,
                dialogTitle: "Select an image",
                rootPath:$.topFrameJQuery.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                initialPath: intialPath,
                createNew:( eventSource == 'imageSlider' ? false : {"label":"Upload", "iconclass":"icon-upload-alt", "onclick":openCreateImageDialog} ),
                selectedItemValidator:validator,
                acceptableTypes:"percImageAsset,site,Folder"
            };
            $.topFrameJQuery.PercPathSelectionDialog.open(pathSelectionOptions);
        });

        $(".perc-image-field-clear-button").on("click", function() {
            var $el = $(this);
            var imgInputName = $el.attr("for");
            var intialPath = $("#" + imgInputName ).val();
            if(intialPath.trim() === "") {
                return;
            }

            $('#' + imgInputName).val("");
        });

    });
})(jQuery);
