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

      //listens for dropdown changes and in addition to disabling unrelated inputs, will also uncheck enable option for disabled rows
      $("#perc-social-button-type").change(function(){

        if( $(this).val() === 'page') {
          $(".perc-social-page-link").attr('disabled', false);
          $(".perc-base-url").css('color', '#333333');
          $(".perc-social-page-link").css('color', '#333333');
          $(".perc-email-row *").attr('disabled', true);
          $(".perc-email-row .perc-social-button-ui").addClass('perc-social-button-ui-disabled');
          $(".perc-email-row").find('td,input').css('color', '#7f7f7f');
          $(".perc-whatsapp-row *").attr('disabled', true);
          $(".perc-whatsapp-row .perc-social-button-ui").addClass('perc-social-button-ui-disabled');
          $(".perc-whatsapp-row").find('td,input').css('color', '#7f7f7f');
          $(".perc-youtube-row *").attr('disabled', false);
          $(".perc-youtube-row .perc-social-button-ui").removeClass('perc-social-button-ui-disabled');
          $(".perc-youtube-row").find('td,input').css('color', '#333333');
          $(".perc-social-enable-whatsapp-checkbox").attr("checked", false);
          $(".perc-social-enable-email-checkbox").attr("checked", false);

        }
        else {
          $(".perc-social-enable-youtube-checkbox").attr("checked", false);
          $(".perc-youtube-row *").attr('disabled', true);
          $(".perc-youtube-row .perc-social-button-ui").addClass('perc-social-button-ui-disabled');
          $(".perc-youtube-row").find('td,input').css('color', '#7f7f7f');
          $(".perc-email-row *").attr('disabled', false);
          $(".perc-email-row .perc-social-button-ui").removeClass('perc-social-button-ui-disabled');
          $(".perc-email-row").find('td,input').css('color', '#333333');
          $(".perc-whatsapp-row *").attr('disabled', false);
          $(".perc-whatsapp-row .perc-social-button-ui").removeClass('perc-social-button-ui-disabled');
          $(".perc-whatsapp-row").find('td,input').css('color', '#333333');
          $(".perc-social-page-link").attr('disabled', true);
          $(".perc-social-page-link").css('color', '#7f7f7f');
          $(".perc-base-url").css('color', '#7f7f7f');
        }
      });

     if($(".perc-social-buttons").length > 0)
      {
          $.socialButtonsSetup ().renderEditor();
      }
      else if ($(".perc-social-buttons-readonly").length > 0)
      {
          $.socialButtonsSetup ().renderReadOnly();
      }

    });

      $.socialButtonsSetup = function()
      {
          var socialButtonsSetupApi = {
            renderEditor : renderEditor,
            renderReadOnly : renderReadOnly
           };

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

                // We need to hide the youtube option and link fields on initial load
                if( $('#perc-social-button-type option:selected').val() === 'share'  ) {
                  $(".perc-youtube-row *").attr('disabled', true);
                  $(".perc-youtube-row .perc-social-button-ui").addClass('perc-social-button-ui-disabled');
                  $(".perc-youtube-row").find('td,input').css('color', '#7f7f7f');
                  $(".perc-social-page-link").attr('disabled', true);
                  $(".perc-base-url").css('color', '#7f7f7f');
                  $(".perc-social-page-link").css('color', '#7f7f7f');
                }

                //Load data from the data field - this is done in two steps
                //an input field gets the data and the name of the input field is passed to the id of a div that contains
                //our editor class - we get the id from the div and pull the data from the input field.
                var dataFieldName = $(".perc-social-buttons").attr("id");
                var dataStr = $("input[name='" + dataFieldName + "']").val();
                var data = null;
                if($.trim(dataStr).length>0){
                    data = JSON.parse(dataStr);
                }

                 if(data!==null) {
                     var i = 0;

                     //checks the dropdown value on initial load and triggers a change event if necessary
                     if (data.buttonType === 'page') {
                         $('#perc-social-button-type').val('page');
                         $('#perc-social-button-type').trigger('change');
                     } else {
                         // hides youtube row to start
                         $(".perc-youtube-row *").attr('disabled', true);
                         $(".perc-youtube-row .perc-social-button-ui").addClass('perc-social-button-ui-disabled');
                         $(".perc-youtube-row").find('td,input').css('color', '#7f7f7f');
                         $(".perc-social-page-link").attr('disabled', true);
                         $(".perc-base-url").css('color', '#7f7f7f');
                         $(".perc-social-page-link").css('color', '#7f7f7f');
                     }

                     //restores previously saved options
                     $(data.config).each(function () {
                         $('.perc-' + data.config[i].platform + '-row').find('.perc-social-platform').val(data.config[i].platform);

                         if (data.config[i].enableButton === true) {
                             $('.perc-' + data.config[i].platform + '-row').find('.perc-social-platform-enabled').prop('checked', true);
                         } else {
                             $('.perc-' + data.config[i].platform + '-row').find('.perc-social-platform-enabled').prop('checked', false);
                         }
                         if (data.config[i].enableDataPush === true) {
                             $('.perc-' + data.config[i].platform + '-row').find('.perc-social-data-push-enabled').prop('checked', true);
                         } else {
                             $('.perc-' + data.config[i].platform + '-row').find('.perc-social-data-push-enabled').prop('checked', false);
                         }
                         $('.perc-' + data.config[i].platform + '-row').find('.perc-social-page-link').val(data.config[i].socialLink);
                         $('.perc-' + data.config[i].platform + '-row').attr('data-order', data.config[i].buttonOrder);

                         i++;

                     });

                     // this will sort the rows into the correct order
                     $(".perc-social-button-row").sort(function () {
                         return ($(b).data('order')) < ($(a).data('order')) ? 1 : -1;
                     }).insertAfter('.perc-social-header'); // append again to the list

                 }

               _attachEvents();

           }

         /**
         * Helper method that is responsible for attaching the click events and auto fill functionality.
         * Also attaches the presubmit handler to _preSubmitHandler.
         */
        function _attachEvents()
        {
            // Enables enter keyboard selection for the checkboxes
            $('input:checkbox').keypress(function(e){
              if((e.keyCode ? e.keyCode : e.which) === 13){
                $(this).trigger('click');
              }
            });
            // This section listens for either a click or keypress on the move buttons
            $('.perc-move-button-up').on('keypress click', function(){
              moveRowUp( $(this) );
            });
            $('.perc-move-button-down').on('keypress click', function(){
              moveRowDown( $(this) );
            });

            // The moveRowUp and moveRowDown functions check to find out of the skipped element is hidden.
            // If the element is hidden, the function is called recursively. This was added to resolve an
            // issue that makes it look like the element has not moved up or down a row, when in fact,
            // it just skipped over a hidden element. The UI does not currently enable the hiding of elements,
            // but in the event it is implemented as part of a future update, no changes will be needed in this
            // section.
            function moveRowUp( thisObj ) {
              if( (thisObj.parents('.perc-social-button-row').prev().hasClass('perc-social-header')) === false ) {
                thisObj.parents('.perc-social-button-row').insertBefore(thisObj.parents('.perc-social-button-row').prev());
                if( thisObj.parents('.perc-social-button-row').next().css('display') === 'none' ){
                  moveRowUp(thisObj);
                }
              }
            }
            function moveRowDown(thisObj) {
              if( (thisObj.parents('.perc-social-button-row').is(':last-child')) === false ) {
                thisObj.parents('.perc-social-button-row').insertAfter(thisObj.parents('.perc-social-button-row').next());
                if( thisObj.parents('.perc-social-button-row').prev().css('display') === 'none' ){
                  moveRowDown(thisObj);
                }
              }
            }

            //Add the pre-submit handler to prepare data for saving.
            window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(_preSubmitHandler);

        }


        /***
         * Validates the Widget data.
         * @returns true if everything is valid, false if not
         */
        function validateForm(){
            var urlregex = new RegExp("^(http:\/\/|https:\/\/){1}((www\.)?([0-9A-Za-z]+\.)|(plus\.google\.){1})");

            $.each(config, function(key, value){

              // Checks to ensure that a valid URL has been entered
              var urlValid = urlregex.test(value.socialLink);

              if( data.buttonType === 'page' && value.enableButton === true && urlValid === false  ) {
                return false;
              }
            });
            return true;
        }

        /***
         * Responsible for collecting Widget data into a Json object that can be saved to a field on the backing content type.
         */
        function getData(){

            //initializes configuration array to store options for each platform
            config = [];

            //retrieves all selected options
            $(".perc-social-button-row").each(function(index) {

                  var alt = {};

                  alt.platform = $(this).find(".perc-social-platform").val();
                  alt.enableButton = false;
                  alt.enableDataPush = false;

                  if( $(this).find(".perc-social-platform-enabled").is(':checked') ) {
                    alt.enableButton =  true;
                  }

                  if( $(this).find(".perc-social-data-push-enabled").is(':checked') ){
                    alt.enableDataPush =  true;
                  }

                  alt.socialLink =  $(this).find(".perc-social-page-link").val();
                  alt.buttonOrder = index;

                  config.push(alt);

            });

            return config;
        }
        /**
         * From the editor, reads the values and creates a JSON Object and assigns the JSON object
         * as the value of the hidden input of the Content Types field.
         */
        function _preSubmitHandler()
        {

            //Get the Widget data and convert it to a JSON string that can be saved.
            var data = {};
            //retrieves dropdown value for buttons type
            data.buttonType = $('#perc-social-button-type option:selected').val();
            //runs function that retrieves individual options
            data.config=getData();

            //Validate the Widget.
            if( validateForm() === false ) {
              $('.perc-social-button-error').show();
              return false;
            }
            $('.perc-social-button-error').hide();


            //populates hidden input with JSON object
            var fieldName = $(".perc-social-buttons").attr("id");
            $("input[name='" + fieldName + "']").val(JSON.stringify(data));
            return true;

        }

        return socialButtonsSetupApi;
      };
})(jQuery);
