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

(function($)
{
    /*
     * On document ready calls the render function, editor or read only.
     * FIXME
     */
    $(document).ready(function(){
       if($(".perc-google-calendar").length > 0)
        {
            $.googleCalendarSetup().renderEditor();
        }
        else if ($(".perc-google-calendar-readonly").length > 0)
        {
            $.googleCalendarSetup().renderReadOnly();
        }

		$('#perc-google-calendar-background-color, #perc-google-calendar-text-color').minicolors({
                    control: $(this).attr('data-control') || 'hue',
                    defaultValue: $(this).attr('data-defaultValue') || '',
                    format: $(this).attr('data-format') || 'hex',
                    keywords: $(this).attr('data-keywords') || '',
                    inline: $(this).attr('data-inline') === 'true',
                    letterCase: $(this).attr('data-letterCase') || 'lowercase',
                    opacity: $(this).attr('data-opacity'),
                    position: $(this).attr('data-position') || 'top left',
                    swatches: $(this).attr('data-swatches') ? $(this).attr('data-swatches').split('|') : [],
                    change: function(hex, opacity) {
                        var log;
                        try {
                            log = hex ? hex : 'transparent';
                            if( opacity ) log += ', ' + opacity;
                            console.log(log);
                        } catch(e) {}
                    },
                    theme: 'default'
                });


    });

      $.googleCalendarSetup = function()
      {
          var googleCalendarSetupApi = {
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

				//Hide Google Setup to start;
				 $('.perc-google-calendar').hide();

                //Load data from the data field - this is done in two steps
                //an input field gets the data and the name of the input field is passed to the id of a div that contains
                //our editor class - we get the id from the div and pull the data from the input field.
                var dataFieldName = $(".perc-google-calendar").attr("id");
                var dataStr = $("input[name='" + dataFieldName + "']").val();
                var data = null;
                dataStr = dataStr.trim();
                if(dataStr.length>0){
                    data = JSON.parse(dataStr);
                }

                 if(data!=null){
                 var $table = $('#perc-google-calendar-setup-editor');
                 	for(var i =0 ; i < data.config.length ; i++){
                    	var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');

                        $clone.find('.perc-google-calendar-name').val(data.config[i].calendarName);
                        $clone.find('.perc-google-calendar-id').val(data.config[i].calendarId);
                        $clone.find('.perc-google-calendar-api-key').val(data.config[i].apiKey);
						$clone.find('.perc-google-calendar-background-color').val(data.config[i].backgroundColor);
						$clone.find('.perc-google-calendar-text-color').val(data.config[i].textColor);


                        $table.find('table').append($clone);

				 	}
				 }
               _attachEvents();

           }

         /**
         * Helper method that is responsible for attaching the click events and auto fill functionality.
         * Also attaches the presubmit handler to _preSubmitHandler.
         */
        function _attachEvents()
        {
            var $table = $('#perc-google-calendar-setup-editor');

            //Add the pre-submit handler to prepare data for saving.
            window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(_preSubmitHandler);

           //Widget behavioral events
            $('.perc-table-remove').on("click",function () {
                $(this).parents('tr').detach();
            });

            $('.perc-table-add').on("click",function () {
                var $clone = $table.find('tr.hide').clone(true).removeClass('hide table-line');
                $table.find('table').append($clone);

				$('#perc-google-calendar-background-color, #perc-google-calendar-text-color').minicolors({
   					 hide: function() {
    				console.log('Hide event triggered!');
    				}
				});
            });

            $('.perc-toggle-help').on("click",function () {
                  if($('.perc-help').is(":visible")){
                         $('.perc-help').hide();
                  } else{
                      $('.perc-help').show();
                  }
           });

		   $('.perc-toggle-google-setup').on("click",function () {
                  if($('.perc-google-calendar').is(":visible")){
                         $('.perc-google-calendar').hide();
                  } else{
                      $('.perc-google-calendar').show();
                  }
           });

            $('.perc-table-up').on("click",function () {
                var $row = $(this).parents('tr');
                if ($row.index() === 1) {
                    return; // Don't go above the header
                }
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




        function convertToSlug(Text) {
    		return Text
        	.toLowerCase()
        	.replace(/[^\w ]+/g,'')
        	.replace(/ +/g,'-')
        	;
		}

        /***
         * Responsible for collecting Widget data into a Json object that can be saved to a field on the backing content type.
         */
        function getData(){
            var config = [];

            $(".perc-google-calendar-row").each(function() {
               if(!  $(this).hasClass("hide") ){
                  var alt = new Object();
                  alt["calendarName"] =  $(this).find(".perc-google-calendar-name").val();
                  alt["calendarId"] =  $(this).find(".perc-google-calendar-id").val();
                  alt["apiKey"] =  $(this).find(".perc-google-calendar-api-key").val();
                  alt["backgroundColor"] =  $(this).find(".perc-google-calendar-background-color").val();
                  alt["textColor"] =  $(this).find(".perc-google-calendar-text-color").val();
				  alt["className"] =  convertToSlug($(this).find(".perc-google-calendar-name").val());

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


           $("#perc-content-edit-sys_title").val($("#perc-content-edit-calendarName").val());

            //Get the Widget data and convert it to a JSON string that can be saved.
            var data = new Object();
            data.config=getData();

            var fieldName = $(".perc-google-calendar").attr("id");
            $("input[name='" + fieldName + "']").val(JSON.stringify(data));
            return true;

        }

        return googleCalendarSetupApi;
      }
})(jQuery);
