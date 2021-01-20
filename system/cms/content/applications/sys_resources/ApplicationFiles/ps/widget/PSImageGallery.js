dojo.provide("ps.widget.PSImageGallery");
dojo.require("sos.widget.ImageGallery");
dojo.require("dojo.style");
dojo.widget.defineWidget(    
   // widget name and class    
   "ps.widget.PSImageGallery",         
   // superclass    
   sos.widget.ImageGallery,        
   // properties and methods    
   {        
      templateString: "<div dojoAttachPoint='outerNode' class='image-gallery-wrapper'>" + 
         "<div dojoAttachPoint='largeNode' class='image-gallery-image-wrapper'></div>" + 
         "<div dojoAttachPoint='thumbsNode' class='image-gallery-thumbs-wrapper'>" +
         "<table><tr dojoAttachPoint='thumbsTableRow'></tr></table></div>" + 
         "<div dojoAttachPoint='slideshowNode' class='image-gallery-slideshow'><a href='#'>Start Slideshow</a></div>" + 
         "<div dojoAttachPoint='hiddenNode' class='image-gallery-hidden'></div>" + 
         "</div>",
      numberThumbs: 4,
      imageHeight: 300,
      imageWidth: 400,
      

      /**
       * Hides the slide show link that is part of sos.widget.ImageGallery
       */
      postCreate: function()
      {
         sos.widget.ImageGallery.prototype.postCreate.call(this);
         dojo.style.hide(this.slideshowNode);
      },

      /**
       * Creates input data for Image gallery widget. Creates a data objects by 
       * looping through thumbUrls array,  gets the corresponding full image 
       * url and title from supplied fullUrls and titles at the same index.
       * @param thumbUrls array of thumbnail image urls, must be an Array object
       * @param fullUrls array of full image urls, must be an Array object
       * @param titles optional titles if provided must be an Array object.
       */
      setImages: function(thumbUrls,fullUrls, titles)
      {
         dojo.lang.assertType(thumbUrls,Array);
         dojo.lang.assertType(fullUrls,Array);
         if(titles)
            dojo.lang.assertType(titles,Array);
         var imageData = [];
         for(var i=0;i<thumbUrls.length;i++)
         {
            imageData[imageData.length] = {"thumb": thumbUrls[i], "large": fullUrls[i], "title":titles[i]};
         }
         this.setData(imageData);
      }
   }
);

