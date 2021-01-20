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

(function($) {
    /*
     * On document ready calls the render function, editor or read only.
     * FIXME
     */
    $(document).ready(function() {
       if($(".perc-local-lang").length > 0)
        {
            $.LocalLangEditor().renderEditor();
        }
        else if ($(".perc-local-lang-readonly").length > 0)
        {
            $.LocalLangEditor().renderReadOnly();
        }
        
    });     
    
      $.LocalLangEditor = function() {
          var localLangEditorApi = {
            renderEditor : renderEditor,
            renderReadOnly : renderReadOnly
           };
           
           var self = this;
           /**
            * Respsonsible for rendering a read-only view of the control.
            */
           function renderReadOnly() {
           }
           /***
            * Responsible for rendering the Edit view of the control.
            */
           function renderEditor() {
               //Hide help to start
                $('.perc-help').hide();
           
                //Load data from the data field - this is done in two steps
                //an input field gets the data and the name of the input field is passed to the id of a div that contains 
                //our editor class - we get the id from the div and pull the data from the input field.
                var dataFieldName = $(".perc-local-lang").attr("id");
                var dataStr = $("input[name='" + dataFieldName + "']").val();
                var data = null;
                if($.trim(dataStr).length>0){
                    data = JSON.parse(dataStr);
                }    
                
                if(data!=null){
                    var $table = $('#perc-local-lang-editor');    
                    for(var i =0; i < data.config.length ; i++){
                        
                        var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                        if (data.config[i].protocol) {
                            $clone.find('.perc-lang-protocol').val(data.config[i].protocol);
                        }
                        $clone.find('.perc-page-select input[type="text"]').attr('data-page-id', data.config[i].pageId);
                        $clone.find('.perc-lang-list').val(data.config[i].lang);
                        $clone.find('.perc-country-list').val(data.config[i].country);
                        $clone.find('input[name="default-lang"]').prop("checked", data.config[i].defLang);
    
                        var cloneNum = (i + 1);
                        var newCloneId = "perc-content-page-selections-" + cloneNum;
                        $clone.find('.perc-page-select input[type="text"]').attr('id', newCloneId);
                        $clone.find('.perc-page-select input[type="button"]').attr('for', newCloneId);
                        $table.find('table').append($clone);      
                        var $element = $table.find('#' + newCloneId);
                        
                        $element.attr({
                            title: data.config[i].pagepath,
                            value: data.config[i].pagepath
                        });
                        // Update pagepath if page has been moved.  
                        if (data.config[i].pageId != "" && $element != null )
                            updatePagePath(data.config[i], $element);
                    }
                }
            
                _attachEvents();
                
                // Page selection handler
                $(document).ready(function(){
                    $(".perc-page-field-select-button").click(function(){
                        var $el = $(this);
                        var pageInputName = $el.attr("for");
                        var intialPath = $("#" + pageInputName ).val();
                        var pageLabel = $("#" + pageInputName ).attr('id');
                        if($.trim(intialPath) == "")
                            intialPath = $.topFrameJQuery.cookie("perc-pagefield-path");
                        var updatePageData = function(pathItem){
                            var path = "/" + pathItem.path;
                            var pageId = pathItem.id;
                            //Some of the services from server are not setting the path on PathItem, if not defined get it from folderPaths
                            if(!path){
                                path ="/site";
                                pageId = "";
                            } 
                            
                            //Save the path to cookie
                            $.topFrameJQuery.cookie(pageLabel, path);
                            $("#" + pageInputName ).val(path).attr({
                                "title": path,
                                "data-page-id": pageId
                            });
                            $('#page-selection-error').remove();
                            if(intialPath != path){
                                $("#" + pageInputName + "_linkId").val(path);
                            }
                        }
                    
                        var validator = function(pathItem){
                            return pathItem && pathItem.type == "percPage"?null:"Please select a page.";
                        }            
                        var pathSelectionOptions = {
                            okCallback: updatePageData,
                            dialogTitle: "Select a page",
                            rootPath:$.topFrameJQuery.PercFinderTreeConstants.ROOT_PATH_SITES,
                            initialPath: intialPath,
                            selectedItemValidator:validator
                        };
                        $.topFrameJQuery.PercPathSelectionDialog.open(pathSelectionOptions);
                    });
                });
            }
         
         /**
          * Helper function for updating selected pagepaths if they've moved
          */
          // Check system to see if page was moved 
          function updatePagePath(pageData, $element){
            var defer = $.Deferred();
  
            $.PercPathService.getPathItemById(pageData.pageId, function(status, result, errorCode){
                    if(status == $.PercServiceUtils.STATUS_SUCCESS) {
                        var newPagePath = result.PathItem.folderPaths + "/" + result.PathItem.name;
                        console.log("for pageID: " + pageData.pageId + " newPagePath: " + newPagePath);
                        if (pageData.pagepath != newPagePath){
                            pageData.pagepath = newPagePath;
                        }
                        $element.attr({
                            title: pageData.pagepath,
                            value: pageData.pagepath
                        });
                        defer.resolve();
                    } else {
                        var msg = "";
                        if (errorCode == "cannot.find.item") {
                            msg = I18N.message( 'perc.ui.common.error@Content Deleted' );
                        }
                        else {
                            msg = result;
                        }
                        defer.reject({title: 'Error on page lookup', content: msg});
                    }
            });  
            return defer.promise();
          }
         /**
         * Helper method that is responsible for attaching the click events and auto fill functionality.
         * Also attaches the presubmit handler to _preSubmitHandler.
         */
        function _attachEvents() {
            var $table = $('#perc-local-lang-editor');
            
            //Add the pre-submit handler to prepare data for saving.
            window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(_preSubmitHandler);
           
           //Widget behavioral events
            $('.perc-table-remove').click(function () {
                $(this).parents('tr').detach();
            });
            
            $('.perc-table-add').click(function () {
                var rowCount = $('.perc-local-lang-row');
                if (rowCount == 1){
                    var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                    $table.find('table').append($clone);
                } else {
                    var cloneNum = $('.perc-local-lang-row').length;
                    var newCloneId = "perc-content-page-selections-" + cloneNum;
                    var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                    $clone.find('.perc-page-select input[type="text"]').attr('id', newCloneId);
                    $clone.find('.perc-page-select input[type="text"]').attr('value', '');
                    $clone.find('.perc-page-select input[type="button"]').attr('for', newCloneId);
                    $table.find('table').append($clone);
                }
            });

            $('.perc-toggle-help').click(function () {
                  if($('.perc-help').is(":visible")){
                         $('.perc-help').hide();
                  } else{
                      $('.perc-help').show();
                  }
           });
            
            $('.perc-table-up').click(function () {
                var $row = $(this).parents('tr');
                if ($row.index() === 1) return; // Don't go above the header
                    $row.prev().before($row.get(0));
            });

            $('.perc-table-down').click(function () {
                var $row = $(this).parents('tr');
                $row.next().after($row.get(0));
            });
        }
        
        /***
         * Validates the Widget data.
         * @returns true if everything is valid, false if not
         */
        function validateForm() {
            var validation = false;
            // Check all page selector fields for a valid page-id.
            $('.perc-page-select input[name="pageSelections"]:not("#perc-content-page-selections-0")').each(function(){
                var selectedPageId = $(this).attr("data-page-id");
                if (selectedPageId == "false" ){
                    console.log("No page selected for page field.");
                    $('#perc-local-lang-editor').append('<div id="page-selection-error">Error: Page selection missing. Please select an alternate language page for each row.</div>')
                    validation = false;
                } else {
                    validation = true;
                }
            });
            return validation;
        }
        
        /***
         * Responsible for collecting Widget data into a Json object that can be saved to a field on the backing content type.
         */
        function getData() {
            var config = [];

            $(".perc-local-lang-row").each(function() {
               if(!  $(this).hasClass("hide")){
                  var alt = new Object();
                  alt["protocol"] =  $(this).find(".perc-lang-protocol").val();
                  alt["pagepath"] = $(this).find('.perc-page-select input').attr('title');
                  var pathArray = alt.pagepath.split('/');
                  alt["pagename"] = pathArray[pathArray.length-1];
                  alt["pageId"] = $(this).find('.perc-page-select input').data('page-id');
                  alt["lang"] =  $(this).find(".perc-lang-list").val();
                  alt["country"] =  $(this).find(".perc-country-list").val();
                  alt["defLang"] =  $(this).find("input[name='default-lang']").is(':checked');
                  config.push(alt);
                }
            });
            
            return config;
        }
        /**
         * From the editor, reads the values and creates a JSON Object and assigns the JSON object
         * as the value of the hidden input of the Content Types field.
         */
        function _preSubmitHandler() {
            //Validate the Widget.
            if(!validateForm()){return false;}
            
           $("#perc-content-edit-sys_title").val($("#perc-content-edit-configurationName").val());
            
            //Get the Widget data and convert it to a JSON string that can be saved.
            var data = new Object();
            data.config = getData();
            var fieldName = $(".perc-local-lang").attr("id");
            $("input[name='" + fieldName + "']").val(JSON.stringify(data));
            return true;
        }
        
        return localLangEditorApi;
      }
})(jQuery);