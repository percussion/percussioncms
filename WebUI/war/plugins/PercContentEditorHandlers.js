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
 * A common file for holding the content editor handlers.
 */
   
(function($){

   /**
    * Content editor pre-submit handler plugin. The content editor frame work calls all the handlers before submitting 
    * the form, if any of the handler returns false, then the form submission is not done.
    * It is handlers responsibility to provide the feed back to the user if it returns false.
    */
   $.PercContentPreSubmitHandlers = {
        //local variable to hold the array of handlers.
        _handlers:[],
        /**
         * Add the presubmit handlers, if a handler already added then it will be replaced with the new call.
         * @param handler(function) must be a function.
         */
        addHandler:function(handler){
            if(typeof(handler) != 'function')
            {
                alert(I18N.message("perc.ui.content.editor.handlers@Must Be Function"));
                return;
            }
            if($.inArray(handler, this._handlers) === -1)
            {
                this._handlers.push(handler);
            }
        },
        /**
         * Method to remove the handler that was added through the addHandler method.
         * @param handler(function) must be a function.
         */
        removeHandler:function(handler){
            if(typeof(handler) != 'function')
            {
                alert(I18N.message("perc.ui.content.editor.handlers@Must Be Function"));
                return;
            }
            if($.inArray(handler, this._handlers) > -1)
             {
                var len = this._handlers.length;
                for(var i = 0; i < len; i++)
                {
                   if(this._handlers[i] === handler)
                   {
                      this._handlers.splice(i, 1);
                      return;
                   }
                }
             }
        },
        /**
         * Returns the handlers, array of functions, may be empty but never null;
         */
        getHandlers: function(){
            return this._handlers;
        },
        /**
         * Clears the handlers.
         */
        clearHandlers: function(){
            this._handlers = [];
        }
        
    };
})(jQuery); //End closure



