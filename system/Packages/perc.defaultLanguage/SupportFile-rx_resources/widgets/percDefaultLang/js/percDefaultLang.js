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

(function($)
{
    /*
     * On document ready calls the render function, editor or read only.
     * FIXME
     */
    $(document).ready(function(){
       if($(".perc-default-lang").length > 0)
        {
            $.DefaultLangEditor().renderEditor();
        }
        else if ($(".perc-default-lang-readonly").length > 0)
        {
            $.DefaultLangEditor().renderReadOnly();
        }
        
    });     
    
      $.DefaultLangEditor = function()
      {
          var defaultLangEditorApi = {
            renderEditor : renderEditor,
            renderReadOnly : renderReadOnly
           };
           
           var self = this;
           
           /**
            * Respsonsible for rendering a read-only view of the control.
            */
           function renderReadOnly()
           {
           }
           
           /***
            * Responsible for rendering the Edit view of the control.
            */
           function renderEditor()
           {
               //Hide help to start
                $('.perc-help').hide();
           
           
                $.PercSiteService.getSites(function(status, data){
                if (status == $.PercServiceUtils.STATUS_SUCCESS) {
                    var sites = [];
                    $.each(data.SiteSummary, function(){
                        sites.push({
                            "name": this.name,
                            "id": this.siteId
                        });
                    });
                  
                   
                    var list = $('#perc-default-lang-editor').find('tr.hide').find('select.perc-site-list');
                    list.empty();
                    for(var i = 0; i < sites.length; i++){
                        list.append($("<option></option>")
                            .attr("value",sites[i].id)
                            .text(sites[i].name));
                    } 
                    
                    //Load data from the data field - this is done in two steps
                //an input field gets the data and the name of the input field is passed to the id of a div that contains 
                //our editor class - we get the id from the div and pull the data from the input field.
                var dataFieldName = $(".perc-default-lang").attr("id");
                var dataStr = $("input[name='" + dataFieldName + "']").val().trim();
                    var data = null;
                if(dataStr.length>0){
                    data = JSON.parse(dataStr);
                }    
                
                if(data!=null) {
                    var $table = $('#perc-default-lang-editor');
                    for(var i =0 ; i < data.config.length ; i++){
                        var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                        if (data.config[i].protocol) {
                            $clone.find('.perc-lang-protocol').val(data.config[i].protocol);
                            }
                        $clone.find('.perc-site-list').val(data.config[i].siteid);
                        $clone.find('.perc-lang-list').val(data.config[i].lang);
                        $clone.find('.perc-country-list').val(data.config[i].country);
                        $clone.find('input[name="default-lang"]').prop("checked", data.config[i].defLang);

                        $table.find('table').append($clone);
                    }
                    }

                
               _attachEvents();
                }
                else {
                   console.error("Failed to get the sites list.");
                }
            });
     
                
           }
           
         /**
         * Helper method that is responsible for attaching the click events and auto fill functionality.
         * Also attaches the presubmit handler to _preSubmitHandler.
         */
        function _attachEvents()
        {
            var $table = $('#perc-default-lang-editor');
            
            //Add the pre-submit handler to prepare data for saving.
            window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(_preSubmitHandler);
           
           //Widget behavioral events
            $('.perc-table-remove').on("click", function () {
                $(this).parents('tr').detach();
            });
            
            $('.perc-table-add').on("click", function () {
                var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                $table.find('table').append($clone);
            });

            $('.perc-toggle-help').on("click",function () {
                  if($('.perc-help').is(":visible")){
                         $('.perc-help').hide();
                  } else{
                      $('.perc-help').show();
                  }
           });
            
            $('.perc-table-up').on("click", function () {
                var $row = $(this).parents('tr');
                if ($row.index() === 1) return; // Don't go above the header
                    $row.prev().before($row.get(0));
            });

            $('.perc-table-down').on("click", function () {
                var $row = $(this).parents('tr');
                $row.next().after($row.get(0));
            });
                      
            
        }
        
        
        /***
         * Validates the Widget data.
         * @returns true if everything is valid, false if not
         */
        function validateForm(){
            return true;
        }
        
        
        /***
         * Responsible for collecting Widget data into a Json object that can be saved to a field on the backing content type.
         */
        function getData(){
            var config = [];

            $(".perc-defaultlang-row").each(function() {
               if(!  $(this).hasClass("hide")){
                  var alt = {};
                  alt["protocol"] =  $(this).find(".perc-lang-protocol").val();
                  alt["siteid"] =  $(this).find(".perc-site-list").val();
                  alt["sitename"] = $(this).find(".perc-site-list option:selected").text();
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
        function _preSubmitHandler()
        {
            //Validate the Widget.
            if(!validateForm()){return false;}
            
            
           $("#perc-content-edit-sys_title").val($("#perc-content-edit-configurationName").val());
            
            //Get the Widget data and convert it to a JSON string that can be saved.
            var data = {};
            data.config=getData();
            var fieldName = $(".perc-default-lang").attr("id");
            $("input[name='" + fieldName + "']").val(JSON.stringify(data));
            return true;
        }
        
        return defaultLangEditorApi;
      };
})(jQuery);
