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

(function($){
   $.widget("ui.perc_imageselect", {
       // Globals
        _idPostfix: "_perc_is",
        _class: "perc_imageselect",
        _pseudoCtl: null,

      /**
       * Initialize the widget
       */             
      _init: function() {
         var self = this;
         options = this.options;            
         if("select" !== this.element[0].nodeName.toLowerCase())
            window.alert(I18N.message("perc.ui.image.select@Widget Use Error") + 
               I18N.message("perc.ui.image.select@Select HTML Element"));
         self._addPseudoControl();
         self._bindEvents(false);
      
      },
       /**
        * Create the pseudo image select control from the real select
        */               
      _addPseudoControl: function() {
         var self = this;
         var buff = "";
         var id = this.element[0].id;
         if('undefined' === id || null === id || '' === id) {
             window.alert(
                 I18N.message("perc.ui.image.select@Attribute Required Error"));
         }
         buff += "<div id='";
         buff += (id + this._idPostfix);
         buff += "' class='";
         buff += this._class;
         buff += "'>";      
         var children = this.element.children('option');
         
         children.each(function(){
            buff += self._getItemHtml(this.value, this.text);     
         });
         buff += "</div>";
         this.element.hide();
         this.element.after(buff);
         this._pseudoCtl = $("#" + this.element[0].id + this._idPostfix);
         this._addChildIndexes(this._pseudoCtl.children(".perc_imageselect_item"));

      }, _getItemHtml: function(val, imageurl){
         var buff = "";
         buff += "<div class='perc_imageselect_item'>";
         buff += "<span class='perc_imageselect_value'>" + val + "</span>";
         buff += "<img src='";
         buff += imageurl;
         buff += "'/><span>";
	 buff += $.PercTruncateText(val.split(".")[2], 22);
	 buff += "</span></div>";
         return buff;      
      },
      
      _addChildIndexes: function(elem){
         
         var count = 0;
         //Remove any existing index nodes
         elem.each(function(){
            $(this).remove(".perc_imageselect_index");
         });
         //Add index nodes        
         elem.each(function(){
            $(this).append(
               "<span class='perc_imageselect_index'>" + (count++) + "</span>");
         });
         
      },
      
      destroy: function() {
         $.widget.prototype.apply(this, arguments); // default destroy
         this._unbindEvents(false);
       },

      
      /**
       * Bind all necessary events
       */             
      _bindEvents: function(childrenOnly) {
         var self = this;
         // Add events to the pseudo control          
         if(!childrenOnly)
         {
           this._pseudoCtl.on('keydown', function(evt){
              self._handleKeyDown(evt);
           });
           
           this._pseudoCtl.on('click', function(evt){
              $(this).focus();
           });
                              
         }
                  
         this._pseudoCtl.children(".perc_imageselect_item")
         .on('click',
            function(evt){
              var idx = $(this).children(".perc_imageselect_index").text();
              self.selectIndex(idx);   
         });
         
         this._pseudoCtl.children(".perc_imageselect_item").on('dblclick',
            function(evt){
              if(self.options.hardSelect)
                 self._onSelect();    
         });
         
         
         
      },
      /**
       * Unbind events from the controls
       * @param childrenOnly (boolean) if <code>true</code> then only
       * unbind from the child nodes. 
       */                           
      _unbindEvents: function(childrenOnly){
         if(!childrenOnly)
         {
         this._pseudoCtl
            .off('keydown')
            .off('click');
         }
         
         this._pseudoCtl.children(".perc_imageselect_item")
            .off('click')
            .off('dblclick');
                       
      },      
      
      /**
       * Handle the keydown events
       */             
      _handleKeyDown: function(evt){
         if (!this.options.vertical && (37 === evt.keyCode || 39 === evt.keyCode)) {
					this._moveSelection(39 === evt.keyCode);
					return evt.preventDefault();
			}	
					
			if (this.options.vertical && (38 === evt.keyCode || 40 === evt.keyCode)) {
					this._moveSelection(40 === evt.keyCode);
					return evt.preventDefault();
			}
			if (this.options.hardSelect && 13 === evt.keyCode)
			{
			   this._onSelect();
            return evt.preventDefault();
         }
			return true;            
      },
      
      /**
       * Increment or decrement selection if possible.
       * @param increment (boolean).
       */                    
      _moveSelection: function(increment){
         var current = this.getSelectedIndex();
         var childCount = this._pseudoCtl.children(".perc_imageselect_item").length;
         if(current !== -1)
         {
            if(increment && (childCount - 1) > current)
            {
               this.selectIndex(current + 1);   
            }
            else if(!increment && 0 < current)
            {
               this.selectIndex(current - 1);
            }   
         
         }
      },
      /**
       * Fire off the on select callback if one was specified.
       */             
      _onSelect: function(){
         if(null !== this.options.onSelect)
         {
             var selection = 
                this._pseudoCtl.children(".perc_imageselect_selected");                
             var value = selection.children(".perc_imageselect_value").text();
             var imageurl = selection.children("img").attr("src");               
             this.options.onSelect(value, imageurl);
         }
      },
      
      /**
       * Sets items for this image select.
       * @param items (Array) an array of the items the following
       * format. [["value1", "imageurl1"], ["value2", "imageurl2"]]              
       */             
      setItems: function(items){
         if(null === items || 'undefined' === items)
         {
            alert(I18N.message("perc.ui.image.select@Null Or Undefined Items"));
         }
         else
         {
            this.clearSelection();
            this._unbindEvents(true);
            this._pseudoCtl.children(".perc_imageselect_item").remove();
            for(var key in items)
            {
               var current = items[key];
               this._pseudoCtl.append(this._getItemHtml(current[0], current[1]));
            }
            this._addChildIndexes(
               this._pseudoCtl.children(".perc_imageselect_item"));
            this._bindEvents(true);   
         }      
      },
      /**
       * Load from remote server based on passed in url.
       * Expects to receive a JSON object with an items property that contains an 
       * array in the format specified by
       * the {@link #setItems(items)} function.
       * @param url (string) the url of the remote to get the items
       * from.
       */                                        
      loadFromUrl: function(url)
      {
         var self = this;
         if(null !== url && 'undefined' !== url)
         {
            $.getJSON(url, function(data){
               self.setItems(data.items);
            });   
         
         }
      },      
      
      /**
       * Clear all selected items.
       */             
      clearSelection: function(){
         var imageselectId = this.element[0].id + this._idPostfix; 
         $("#" + imageselectId + " .perc_imageselect_selected")
            .removeClass("perc_imageselect_selected");
      },
      
      /**
       * Select the item based on the passed in index.
       */             
      selectIndex: function(idx){
         this.clearSelection();
         var imageselectId = this.element[0].id + this._idPostfix;
         $("#" + imageselectId + 
            " .perc_imageselect_item:eq(" + idx + ")").addClass(
               "perc_imageselect_selected");
         var targetOffset =  
            this._pseudoCtl.children(".perc_imageselect_selected").offset().top - 50; 
         var sourceOffset = this._pseudoCtl.offset().top;
         var oldScrollTop = this._pseudoCtl.scrollTop();
         if(!this.options.hardSelect)
         {
            
            this._onSelect();      
         }
         this._pseudoCtl.animate({scrollTop: oldScrollTop + targetOffset - sourceOffset}, 500);             
         this._pseudoCtl.focus();          
      },
      
      /**
       * Get the index of the selected item.
       * @return index of the selection or -1 if no selection.
       */                    
      getSelectedIndex: function(){
         var selection = this._pseudoCtl.children(".perc_imageselect_selected");
         if(0 < selection.length)
         {
           return parseInt(selection.children(".perc_imageselect_index").text());
         }
         return -1;     
      }      
     
      
      

   });

   $.extend($.ui.perc_imageselect, {
      version: "1.0.0",
      defaults: {
         vertical: true,
         onSelect: null,
         hardSelect: true     
      }
   });
 
})(jQuery);
