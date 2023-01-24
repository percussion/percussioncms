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



