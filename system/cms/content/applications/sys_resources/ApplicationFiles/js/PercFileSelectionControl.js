/**
 * Image selection control, works with the sys_ImagePath control.
 * Adds a click event to image browse button with class name perc-image-field-select-button
 * and gets the input field from for attribute.
 * Opens path selection dialog for image selection.
 */
(function($)
{
    $(document).ready(function(){
        $(".perc-file-field-select-button").on("click",function(){
            var $el = $(this);
            var fileInputName = $el.attr("for");
            var intialPath = $("#" + fileInputName ).val();

            if(intialPath.trim() === "")
                 intialPath = $.topFrameJQuery.cookie("perc-filefield-path");
            var updateFileData = function(pathItem){
                var path = pathItem.path;
                //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
                if(!path){
                    path = pathItem.folderPaths.split("$System$")[1] + "/" + pathItem.name;
                } 
                
                //Save the path to cookie
                $.topFrameJQuery.cookie("perc-filefield-path", path);
                $("#" + fileInputName ).val(path).attr("title",path);
                if(intialPath != path){
                    $("#" + fileInputName + "_linkId").val(path);
                }
                
            }
            //Create new button click function. The success callback is called with PathItem, if the new image creation is successful
            //Otherwise cancelcall back is called. 
            var openCreateFileDialog = function(successCallback, cancelCallback){
                $.topFrameJQuery.PercCreateNewAssetDialog("percFile", successCallback, cancelCallback);    
            };

            var validator = function(pathItem){
                return pathItem && pathItem.type == "percFileAsset"?null:"Please select a file.";
            }            
            var pathSelectionOptions = {
                okCallback: updateFileData,
                dialogTitle: "Select a file",
                rootPath:$.topFrameJQuery.PercFinderTreeConstants.ROOT_PATH_ASSETS,
                initialPath: intialPath,
                createNew:{"label":"Upload", "iconclass":"icon-upload-alt", "onclick":openCreateFileDialog},
                selectedItemValidator:validator,
                acceptableTypes:"percFileAsset,site,Folder"
            };
            $.topFrameJQuery.PercPathSelectionDialog.open(pathSelectionOptions);
        });

        $(".perc-file-field-clear-button").on("click", function() {
            var $el = $(this);
            var fileInputName = $el.attr("for");
            var intialPath = $("#" + fileInputName ).val();
            if(intialPath.trim() === "") {
                return;
            }

            $('#' + fileInputName).val("");
        });

    });
})(jQuery);