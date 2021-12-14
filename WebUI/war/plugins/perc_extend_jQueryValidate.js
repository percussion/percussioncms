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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

// Extend jQuery Validator Plugin
//
// The jQuery Validator plugin must be included before this file.


(function($){
    var binaryExtArray = ["3gp","3g2","7z","ace","air","swf","fxp","pdf","ppd","aac","aif","avi","dxf","dwf",
    "bin","bmp","sh","btif","bz","bz2","csh","dae","cgm","wbs","dvi","dtd","dra","dts","dwg","xif","f4v","flv",
    "gtar","gif","h261","h263","h264","ico","ief","jad","jar","class","jnlp","jpm","jpg","jpeg","jpgv","m3u",
    "m4v","mdb","asf","exe","cab","clp","mdi","xls","xlsb","xltm","xlsm","chm","mny","xlsx","xltx","docx",
    "dotx","ppt","mpp","pub","vsd","wm","wma","doc","wps","xps","mid","mpga","mpeg","mp4a","mp4","oda","ogx",
    "oga","ogv","osf","odb","odi","odp","ots","odt","sxd","otf","psd","pic","pbm","png","ai","pbd","qt","rar",
    "rm","rtf","rtx","svg","sdc","sda","sdd","smf","sdw","sgl","au","tiff","tar","ttf","vcd","wav","hlp","wsdl","zip"];
    
   /*
    * Add our own remote validation method that allow a handler to be added
    * That will evaluate the response and determine if the value is valid
    *
    * Example call:
    *  $("#main").validate({
    *     rules: {
    *        bar: {
    *           perc_remote: {
    *              url: "testValid.jsp",
    *              type: "post",
    *              data: {
    *                 valA: "hello",
    *                 valB: "world"
    *              },
    *              handler: function(validator, element, value, response){
    *                 // DO SOMETHING
    *                 return result; // boolean
    *              }
    *           },
    *                            
    *        }
    *      },
    *     messages: {
    *        bar: {
    *           perc_remote: "Name must be unique!!"
    *        }
    *     }
    *  });
    *  
    */                        
    $.validator.addMethod("noBinary", function(value, element){ 
            
            // Gets the page name
            var match = value.trim().match(/[\w_.-]*?(?=[\?#])|[\w_.-]*$/i);
            if (typeof(match[0]) != "undefined") 
                var splitMatch = match[0].split("."); // matches 0 since it's the first group
            
            // Splits the page name to get the extension
            if (typeof(splitMatch[splitMatch.length - 1]) != "undefined")
            {
                var extension = splitMatch[splitMatch.length - 1];
            }
            
            return this.optional(element) || $.inArray(extension.toLowerCase(), binaryExtArray) === -1;
       });
   
   $.validator.addMethod("perc_remote",
         function(value, element, param) {
			if ( this.optional(element) )
				return "dependency-mismatch";
			
			var previous = this.previousValue(element);
			
			if (!this.settings.messages[element.name] )
				this.settings.messages[element.name] = {};
						
			param = typeof param == "string" && {url:param} || param;
			var handler = null;
         if('handler' in param)
			{
            handler = param.handler;
         }          
			
			if ( previous.old !== value ) {
				previous.old = value;
				var validator = this;
				this.startRequest(element);
				var data = {};
				data[element.name] = value;
				$.ajax($.extend(true, {
					url: param,
					mode: "abort",
					port: "validate" + element.name,
					dataType: "json",
					data: data,
					success: function(response) {
						var valid = handler == null ? 
                     response === true :
                     handler(validator, element.name, value, response);
						if ( valid ) {
							var submitted = validator.formSubmitted;
							validator.prepareElement(element);
							validator.formSubmitted = submitted;
							validator.successList.push(element);
							validator.showErrors();
						} else {
							var errors = {};
                            if(typeof(response) != "string")
                                response = false;
							errors[element.name] = previous.message = response || validator.defaultMessage( element, "perc_remote" ); 							
							validator.showErrors(errors);
						}
						previous.valid = valid;
						validator.stopRequest(element, valid);
					}
				}, param));
				return "pending";
			} else if( this.pending[element.name] ) {
				return "pending";
			}
			return previous.valid;
		}
      );
})(jQuery);
