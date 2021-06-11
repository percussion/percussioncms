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

/**
 * Tag list control. Adds a pre submit handler.
 */
(function($)
{
    $(document).ready(function(){
    
        window.parent.jQuery.PercContentPreSubmitHandlers.addHandler(updateTagListField);
        
        //Allow selection of multiple values
        function split( val ) {
			return val.split( /,\s*/ );
		}
		function extractLast( term ) {
			return split( term ).pop();
		}
        
        $.perc_filterField($("#page_tags-display"), $.perc_textFilters.TAGS);

		if(window.percTagListSource !== undefined)
        {
            $("#page_tags-display").autocomplete(percTagListSource,{
				minChars: 1,
                selectFirst:false,
                multiple: true,
                scrollHeight: 135
			});
        }
	});
    
/**
 * Filter a text input field with a passed in filter.
 * @param tgt {String or jQuery} the input field to be filtered. Cannot be <code>null</code>.
 * @param filter {Function} the filter function, the fuction receives the currently
 * typed in char and should return the char back if ok or return empty string if
 * it should be filtered out.
 */     
(function($) {
  $.perc_filterField = function(tgt, filter)
  {
      $(tgt).bind('keypress.filterField', {'filter': filter}, function(evt){
         var filter = function(txt){return txt};
         
         var rawCode = evt.charCode ? evt.charCode : evt.which;
         if(rawCode == 0 || rawCode == 8)
            return;
         var theChar = String.fromCharCode(rawCode);
         if(typeof evt.data.filter == 'function')
            filter = evt.data.filter;
         var filtered = filter(theChar);
         if(filtered.length == 0)
         {
            evt.preventDefault();
         }
         else if(theChar != filtered)
         {
           // the filter changed the char to something else, update the field
           // and retain correct cursor caret positioning
           
           var field = evt.target;
           var start = $(field).caret().start;
           var end = $(field).caret().end;
           var t1 = field.value.slice(0, start);
           var t2 = field.value.slice(end);
           var newVal = t1 + filtered + t2;
           field.value = newVal;
           var cursorPos = t1.length + 1;
           $(field).caret({start: cursorPos, end: cursorPos});
           evt.preventDefault();
         }
      });
      
      $(tgt).bind('blur.filterField', {'filter': filter}, function(evt){
         // Run through the filter on blur
         tgt.val(evt.data.filter(tgt.val()));
      });
      
  }   
})(jQuery); 

/**
 * Define text filters for tags
 */
(function($) { 
  $.perc_textFilters = {
  
     // This filter allows only characters which are valid in name and/or id attributes.
     // Note: This filter does NOT force starting alpha, which IS a requirement of the W3C spec.
     // Allowed Characters: alpha-numeric, " ", '-', '_' and ',' 
     TAGS: function(txt) {return txt.replace(/[^a-zA-Z0-9\-\_\, ]/g, '');}

  };
})(jQuery);
    
    function updateTagListField()
    {
        //This needs to be changed to get dynamically from the field name.
        var $tagSelect = $("#perc-content-edit-page_tags");
        var newTags = $("#page_tags-display").val();
        var newTagsArray = newTags?newTags.split(","):[];
        var processedArray = [];

        $.each(newTagsArray, function()
        {
            var thisTag = this.trim().toLowerCase();
            if(thisTag.length > 0)
            {
                processedArray.push(thisTag);
            }
        });
        processedArray = $.unique(processedArray);
        $.each(processedArray, function()
        {
           $tagSelect.append("<option selected='selected'>" + this + "</option>");
        });
        return true;
   }        
})(jQuery);