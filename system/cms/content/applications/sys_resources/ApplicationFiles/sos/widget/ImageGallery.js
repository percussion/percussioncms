/**
* This file contains an Image Gallery widget built upon the 
* Dojo Ajax toolkit.  For a sample usage, see http://www.skynet.ie/~sos/photos.php
*
* @author  Copyright 2007 Shane O Sullivan (shaneosullivan1@gmail.com)
* @license Licensed under the Academic Free License 3.0 http://www.opensource.org/licenses/afl-3.0.php
* 
* Version: 0.2
*/

dojo.provide("sos.widget.ImageGallery");
dojo.require("dojo.widget.*");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.lfx.html");

dojo.declare(
"sos.widget.ImageGallery", 
dojo.widget.HtmlWidget,
{
  ns: "sos",
  widgetType: "ImageGallery",
  imageStore: [],
  
  imageHeight: 375,
  imageWidth: 500,
  
  defaultTitle: "No Title",
  
  images: [],
  thumbs: [],
  
  thumbCounter: 0,
  imageCounter: 0,
  
  numberThumbs: 5,
  thumbIndex: 0,
  
  dataFn: "",
  
  slideshowInterval: 3,
  
  twoConnectionsUsedForMain: false,
  
  templateString: "<div dojoAttachPoint='outerNode' class='image-gallery-wrapper'>" + 
          "<div dojoAttachPoint='thumbsNode' class='image-gallery-thumbs-wrapper'>" +
          "<table><tr dojoAttachPoint='thumbsTableRow'></tr></table></div>" + 
          "<div dojoAttachPoint='largeNode' class='image-gallery-image-wrapper'></div>" + 
          "<div dojoAttachPoint='slideshowNode' class='image-gallery-slideshow'><a href='#'>Start Slideshow</a></div>" + 
          "<div dojoAttachPoint='hiddenNode' class='image-gallery-hidden'></div>" + 
          "</div>",
  templateCssPath: dojo.uri.moduleUri("sos", "widget/templates/ImageGallery.css"),
  
  tempImgPath: dojo.uri.moduleUri("sos", "widget/templates/images/1pixel.gif"),
  
  tmpImage: null,
  
  postCreate: function()
  {
    var img = document.createElement("img");
    img.setAttribute("width", this.imageWidth);
    img.setAttribute("height", this.imageHeight);
    img.setAttribute("src", this.tempImgPath);
  
    var thisWidget = this;
  
    dojo.event.browser.addListener(this.slideshowNode.firstChild,"onclick",function(evt){
      thisWidget.toggleSlideshow();
      dojo.event.browser.stopEvent(evt);
      return false;
    });

    this.largeNode.appendChild(img);
    this.tmpImage = img;
  
    if(this.dataFn != "" && typeof(dj_global[this.dataFn]) == "function") {
      var data = dj_global[this.dataFn]();
      if(data) {
        this.setData(data);
      }
    } else 
    {
      this.init();
    }
  },
  
  init: function()
  {
    if(this.isInitialized || this.imageStore.length < 1){
      return;
    }
    
    if(!this.thumbCells) 
    {
      dojo.html.setStyle(this.hiddenNode, "position:absolute: left:-10000px;");
      this.thumbCells = [];
      this.thumbIndex = 0;
      this.numberThumbs = Number(this.numberThumbs);    
    
      for(var i = 0; i < this.numberThumbs + 2; i++){
        var cell = document.createElement("td");
        cell.setAttribute("id","img_cell_"+i);
        cell.setAttribute("align","center");    
      
        this.thumbCells[this.thumbCells.length] = cell;
      
        if(i > 0 && i < this.numberThumbs + 1) 
        {
          dojo.html.setClass(cell, "image-thumb-placeholder");        
        } else 
        {
          dojo.html.addClass(cell, "image-thumb-nav");
          if(i == 0) 
          {
            dojo.html.addClass(cell, "image-thumb-prev");
          } else 
          {
            dojo.html.addClass(cell, "image-thumb-next");      
          }
        }
        cell.innerHTML = "<img src='"+this.tempImgPath+"' class='image-gallery-thumb'/>";
        this.thumbsTableRow.appendChild(cell);
      }	  
	  
      var thisWidget = this;
      
      dojo.event.browser.addListener(this.thumbCells[0], "onclick", function(evt){
        thisWidget.showThumbs(thisWidget.thumbIndex - thisWidget.numberThumbs);
      });
      dojo.event.browser.addListener(this.thumbCells[this.numberThumbs + 1], "onclick", function(evt){
        thisWidget.showThumbs(thisWidget.thumbIndex + thisWidget.numberThumbs);
      });
    
    //calculate the correct width for the widget
      var width = 100 * this.numberThumbs + 90;
      dojo.html.insertCssText(".image-gallery-wrapper{text-align:center;width:"+width+"px;}");        
    }
    this.isInitialized = true;
     
    this.updateNavControls();
   
    this.loadNextThumbnail();
    this.showImage(0);
  },
  
  setData: function(data) {
    this.imageStore = data;
    this.init()
  },
  
  reset: function() {
    this.imageStore = [];
  
    while(this.largeNode.firstChild){
        this.largeNode.removeChild(this.largeNode.firstChild);
    }
    this.largeNode.appendChild(this.tmpImage);

    while(this.hiddenNode.firstChild) {
      this.hiddenNode.removeChild(this.hiddenNode.firstChild);
    }
    var img;
    for(var pos = 0; pos < this.images.length; pos++) {
      img = this.images[pos];
      if(img) {
        dojo.event.browser.clean(img);
        if(img.parentNode){
          img.parentNode.removeChild(img);    
        }
      }
    }
    for(var pos = 0; pos < this.thumbs.length; pos++) {
      img = this.thumbs[pos];
      if(img) {
        dojo.event.browser.clean(img);
        if(img.parentNode){
          img.parentNode.removeChild(img);    
        }  
      }
    }

    this.images = [];
    this.thumbs = [];
    this.isInitialized = false;
    this.thumbCounter = 0;
    this.twoConnectionsUsedForMain = false;
    this.imageCounter = 0;
    this.thumbIndex = 0;
  },
  
  showThumbs: function(idx){
    var thisWidget = this;
    var idx = arguments.length == 0 ? this.thumbIndex : arguments[0];
    
    idx = Math.max(idx, 0);
    idx = Math.min(idx, this.imageStore.length);
  
    if(idx == this.imageStore.length) {
      return;
    }
    
    var imgId;
    var existingNodes = [];
    var imagesToPlace = [];
    
    for(var i = 0; i < this.numberThumbs; i++) {
      imgId = "img_"+(i + idx);
      
      if(this.thumbCells[i + 1].firstChild) {
        if(this.thumbCells[i + 1].firstChild.getAttribute("id") == imgId) {
          continue;
        }
        existingNodes[existingNodes.length] = this.thumbCells[i + 1].firstChild;
      }
      imagesToPlace[i] = imgId;
    }
    var page = this.page;
    var showNodes = function() {
      //if the user has moved onto another page, do nothing
      if(page != thisWidget.page)
      {
      return;
      }
    
      for(var i = 0; i < thisWidget.numberThumbs; i++) 
	  {
        if(imagesToPlace[i]) 
		{
          while(thisWidget.thumbCells[i + 1].firstChild) 
          {
            thisWidget.hiddenNode.appendChild(thisWidget.thumbCells[i + 1].firstChild);
          }
      
          var node = dojo.byId(imagesToPlace[i]);
          if(node)
		  {
            if(node.parentNode != thisWidget.thumbCells[i + 1])
            {
              thisWidget.thumbCells[i + 1].appendChild(node);        
            }
            if(dojo.html.getOpacity(node) != 100)
            {
              dojo.html.setOpacity(node, 100);
            }
          } else 
		  {
            var loadPos = imagesToPlace[i].split("_")[1];
            dojo.debug("No thumb '"+imagesToPlace[i]+"' loading from "+(loadPos));
            thisWidget.loadNextThumbnail(imagesToPlace[i].split("_")[1]);
          }
        }
      }
      thisWidget.thumbIndex = idx;
      thisWidget.updateNavControls();
    };
  
    if(existingNodes.length > 0) {
      dojo.lfx.html.fadeOut(existingNodes, 300, null, showNodes).play();
    } else {
      showNodes();
    }
  },
  
  updateNavControls: function() {
    var firstCell = this.thumbsTableRow.cells[0];
    if(this.thumbIndex < 1) {
      dojo.html.removeClass(firstCell,"image-thumb-prev");
    } else if(!dojo.html.hasClass(firstCell,"image-thumb-prev")) {
      dojo.html.addClass(firstCell,"image-thumb-prev");
    }
  
    var lastCell = this.thumbsTableRow.cells[this.thumbsTableRow.cells.length -1];
  
    if(this.thumbIndex + this.numberThumbs >= this.imageStore.length) {
      dojo.html.removeClass(lastCell,"image-thumb-next");
    } else if(!dojo.html.hasClass(lastCell,"image-thumb-next")) {
      dojo.html.addClass(lastCell,"image-thumb-next");
    }
  },
  
  loadNextThumbnail: function()
  { 
    var initPos =  arguments.length == 0 ? -1 : arguments[0];
    var pos = arguments.length == 0 ? this.thumbCounter++ : arguments[0];
    while(pos < this.thumbs.length && this.thumbs[pos]) {
      pos ++;
    }
    
    if(this.thumbCounter >= this.imageStore.length){
      if(this.imageStore.length > 0) {
        if(!this.twoConnectionsUsedForMain) {
          //if all the thumbnails have been loaded, then use the second connection to 
          //the image server to load primary images
          this['twoConnectionsUsedForMain'] = true;
          this.loadNextImage();
        } 
      }
      //return;
    }
    if(pos >= this.imageStore.length) {
      return;
    }
  
    var url = this.imageStore[pos]["thumb"];
    var img = document.createElement("img");
    var imgContainer = document.createElement("div");
    imgContainer.setAttribute("id","img_"+pos);
    imgContainer.appendChild(img);
  
    this.thumbs[pos] = imgContainer;
    
    var loadingDiv = document.createElement("div");
    loadingDiv.innerHTML = "<!-- -->";
    
    loadingDiv.setAttribute("id","loadingDiv_"+pos);
    dojo.html.setClass(loadingDiv,"image-gallery-notifier");
    
    if(this.images.length > pos && this.images[pos]) {
      dojo.html.addClass(loadingDiv, "image-gallery-loaded");
    }
    
    imgContainer.appendChild(loadingDiv);
    this.hiddenNode.appendChild(imgContainer);
        
    var thisWidget = this;
    var page = this.page;
    dojo.event.browser.addListener(img, "onload", function(){
      if(page != thisWidget.page)
      {
        return;
      }
      if(pos >= thisWidget.thumbIndex && pos <thisWidget.thumbIndex + thisWidget.numberThumbs) {
        thisWidget.showThumbs();
      }
      thisWidget.loadNextThumbnail();
      return false;
    });
    dojo.event.browser.addListener(img, "onclick", function(){
      if(page != thisWidget.page)
      {
        return;
      }
      thisWidget.showImage(pos);
      return false;
    });
    
    dojo.html.setClass(img, "image-gallery-thumb");
    img.setAttribute("src", url);
  
    if(this.imageStore[pos]["title"])
    {
      img.setAttribute("title",this.imageStore[pos]["title"]);
    }
  
    this.showThumbs();
  },
  
  loadNextImage: function()
  {
    while(this.images.length >= this.imageCounter && this.images[this.imageCounter]){
      this.imageCounter++;
    }
    
    this.loadImage(this.imageCounter);
  },
  
  loadImage: function(pos, callbackFn)
  {
    if(this.images[pos] || pos >= this.imageStore.length )
    {
      return;
    }
    
    var thumbNotifier = dojo.byId("loadingDiv_"+pos);
    if(thumbNotifier && !dojo.html.hasClass(thumbNotifier, "image-gallery-loading")){
      dojo.html.addClass(thumbNotifier, "image-gallery-loading");
    }
        
    var url = this.imageStore[pos]["large"];
    var img = document.createElement("img");
    var div = document.createElement("div");
    if(!this.imageStore[pos]["link"])
    {
      div.appendChild(img);
    } else
	{
      var a = document.createElement("a");
      a.setAttribute("href",this.imageStore[pos]["link"]);
      a.setAttribute("target","_blank");
      a.setAttribute("rel", "noopener noreferrer");
      div.appendChild(a);
      a.appendChild(img);
    }
    div.setAttribute("id",this.widgetId + "_imageDiv"+pos);
    div.setAttribute("width",this.imageWidth);
    div.setAttribute("height",this.imageHeight);
    img.setAttribute("width",this.imageWidth);
    img.setAttribute("height",this.imageHeight);
    
    var thisWidget = this;
    var page = this.page;
    dojo.event.browser.addListener(img, "onload", function(){
      //If the user has changed the page, do nothing when an image loads
      if(page != thisWidget.page)
      {
        return;
      }
      var divId = "loadingDiv_"+pos;
      var thumbNotifier = dojo.byId(divId);
      if(thumbNotifier) {
        dojo.html.addClass(thumbNotifier, "image-gallery-loaded");
      } 
      thisWidget.loadNextImage();
    });
    if(callbackFn){
      dojo.event.browser.addListener(img, "onload", callbackFn);
    }
    this.hiddenNode.appendChild(div);
  
    var titleDiv = document.createElement("div");
    dojo.html.addClass(titleDiv, "image-gallery-title");
    div.appendChild(titleDiv);
    
    this.images[pos] = div;
    
    img.setAttribute("src", url);
  
    var title = this.imageStore[pos]["title"];
    if(title)
    {
      img.setAttribute("title",title);
      titleDiv.innerHTML = title;
    } else{
      titleDiv.innerHTML = this.defaultTitle;
    }
  },
  
  destroy: function(){
    if(this._slideId) {
    this.cancelSlideshow();
  }
  },
  
  showNextImage: function(inTimer) {
    if(Number(this.imageIndex) + 1 >= this.imageStore.length) {
      if(this._slideId)
	  {
        this.cancelSlideshow();
      }
      return false;
    }
    var thisWidget = this;
    this.showImage(Number(this.imageIndex + 1), function(){
      if(inTimer)
      {
        thisWidget.startTimer();
      }
    });
    return true;
  },
  
  toggleSlideshow: function() {
    if(this._slideId) {
      this.cancelSlideshow();
    } else {
      this.slideshowNode.firstChild.innerHTML= "Stop Slideshow";
      var success = this.showNextImage(true);
      if(!success) {
        this.cancelSlideshow();
      }
    }
  },
  
  cancelSlideshow: function(){
    if(this._slideId) {
      clearTimeout(this._slideId);
    }     
    this._slideId = null;
    this.slideshowNode.firstChild.innerHTML= "Start Slideshow";
  },
  
  startTimer: function() {
    this._slideId = setTimeout("dojo.widget.byId('"+
      this.widgetId + 
      "').showNextImage(true);", this.slideshowInterval * 1000);
  },
  
  showImage: function(index, /*optional*/callback)
  {  
    if(!callback && this._slideId)
	{
      this.toggleSlideshow();
    }
    var thisWidget = this;
    var current = this.largeNode.getElementsByTagName("div");
    this.imageIndex = Number(index);
        
    var showOrLoadIt = function() {
      if(thisWidget.images[index])
	  {
        while(thisWidget.largeNode.firstChild)
	    {
          thisWidget.largeNode.removeChild(thisWidget.largeNode.firstChild);
        }
        thisWidget.images[index].style.opacity = 0;
        thisWidget.largeNode.appendChild(thisWidget.images[index]);      
        dojo.lfx.html.fadeIn(thisWidget.images[index], 300, null, callback).play();  
      }
      else 
	  {
        thisWidget.loadImage(index, function(){
          thisWidget.showImage(index);
        });
      }
    };
  
  var thumbNotifier = dojo.byId("loadingDiv_" + index);
    if(thumbNotifier && !dojo.html.hasClass(thumbNotifier, "image-gallery-loading")){
      dojo.html.addClass(thumbNotifier, "image-gallery-loading");
    }
    
    if(current && current.length > 0){
      dojo.lfx.html.fadeOut(current[0], 300, null, function(){
        thisWidget.hiddenNode.appendChild(current[0]);
        showOrLoadIt();
      }).play();

    }else {
      showOrLoadIt();
    }
  } 
}
);

dojo.provide("sos.widget.FlickrImageGallery");
dojo.require("dojo.html.display");

dojo.declare("sos.widget.FlickrImageGallery", sos.widget.ImageGallery, {
  widgetType: "FlickrImageGallery",
  flickrUserId: "",
  flickrApiKey: "",
  flickrSetId: "",
  page: 1,
  perPage: 50,
  numPageLinks: 8,
  useSetMenus: false,
  
  postCreate: function(){
    sos.widget.ImageGallery.prototype.postCreate.call(this);
    if(this.flickrUserId != "" && this.flickrApiKey !="") {
      this.loadFlickrPublicPhotos(this.flickrUserId, this.flickrApiKey)
    }
  
    this.pagingDiv = document.createElement('div');
    this.outerNode.insertBefore(this.pagingDiv, this.slideshowNode);
  
    dojo.html.setClass(this.pagingDiv, "image-gallery-pager");
      
    if(!this.maxPages){
      dojo.html.hide(this.pagingDiv);
    }
	if(this.useSetMenus != false) {
	  this.initSetMenus();
	}
  },
  
  generatePagingLinks: function() {
    if(!this.maxPages) {
      return;
    }
  
    while(this.pagingDiv.firstChild){
      this.pagingDiv.firstChild.parentNode.removeChild(this.pagingDiv.firstChild);
    }
  
    var maxLinks = this.numPageLinks -1;
  
    var first = Math.max(1,Math.min(this.page, this.maxPages));
	var diff = first % this.numPageLinks;
	
    first = first - (diff == 0 ? this.numPageLinks : diff) + 1;
	
    var html = ["<ul id='" + this.widgetId + "_pageLinks'>","<li>Pages: </li>"];
  
    var prefix = "<li><a href='#' onclick='dojo.widget.byId(\"" + this.widgetId 
                         + "\").setPage(";
    var postfix = "</a></li>";
  
    if(first > 1)
    {
      var pageNum = first - 1;
      html[html.length] = prefix + pageNum+"); return false;' " 
                         + "title='Page" + pageNum + "'"
               + ">&lt;&lt;" + postfix;
    }

    var button;
    for(var i = first; i<= this.maxPages && i <= first + maxLinks; i++) {
      html[html.length] = prefix + i + "); return false;' " 
              + "title='Page " + i +"'"
              + ((i == this.page) ? " class='current-page'" : "")
                          + ">" + i + postfix;
    }
  
    if(this.page + maxLinks <= this.maxPages)
	{
      var pageNum = Math.min(first + maxLinks + 1, this.maxPages);
      html[html.length] = prefix + pageNum
               + ");  return false;' " 
               + " title='Page " + pageNum +"'"
               + ">&gt;&gt;" + postfix;
    }
	
    html[html.length] = "</ul>"	
	
    this.pagingDiv.innerHTML = html.join('\n');
	
    if(this._setMenu) {
      this.createSetButton();
	}
  },
  
  createSetButton: function() {
  	var listNode = dojo.byId(this.widgetId + "_pageLinks");
	if(!listNode) {
		return;
	}
	var li = document.createElement("li");
	li.innerHTML = "<a href='#' id='" + this.widgetId + "_menuLink'>Sets</a>";
	
	listNode.appendChild(li);
	
    var thisWidget = this;
	dojo.event.browser.addListener(li.firstChild, "onclick", function(e){
	  thisWidget._setMenu.onOpen(e);return false;
    });
  },
  
  addFlickrData: function(flickrData)
  {
    if(flickrData['stat'] != "ok")
    {
      alert("Flickr data is not valid, stat = " + flickrData['stat']);
      return;
    }
	var baseKey = 'photos';
	if(flickrData['photos'])
	{
      baseKey = 'photos'
	} else if(flickrData['photoset'])
	{
	  baseKey = "photoset";
	} else if(flickrData['photosets'])
	{
	  this.initSetMenus(flickrData);
	  return;
	}
  
    var oldPages = this.maxPages;
    if(flickrData[baseKey] && flickrData[baseKey]['pages'])
    {
      this.maxPages = Number(flickrData[baseKey]['pages']);
    }
  
    this.generatePagingLinks();      
    this.reset();	
	
	var defaultOwner = flickrData[baseKey]['owner'];
    
    if(flickrData[baseKey] && flickrData[baseKey]['photo']){
      flickrData = flickrData[baseKey]['photo'];
    }
    var newData = [];
    for(var i = 0; i< flickrData.length; i++)
    {
      var owner = flickrData[i]['owner'] || defaultOwner;
      var baseUrl = "http://farm"+flickrData[i]['farm']+".static.flickr.com/"+flickrData[i]['server'] +
        "/"+flickrData[i]['id']+"_"+flickrData[i]['secret'];
      var title = flickrData[i]['title'];
      var link = "http://www.flickr.com/photos/" + owner + "/"+flickrData[i]["id"];
      newData[newData.length] = {"thumb": baseUrl+"_t.jpg", "large": baseUrl+".jpg", "title":title,"link":link};
    }
    this.setData(newData);
  
    if(!this.maxPages){
      dojo.html.hide(this.pagingDiv);
    } else {
      dojo.html.show(this.pagingDiv);
    }
    
    this.init();
  },
  
  loadFlickrPublicPhotos: function(userId, apiKey, setId, page, perPage) {
  	var method = "flickr.people.getPublicPhotos";
    if(arguments.length == 0) 
    {
      if(this.flickrApiKey == "" || this.flickrUserId == "") {
        dojo.debug("FlickrImageGallery: no userId or apiKey specified. Cannot load Flickr images.");
        return;
      }
    }
	if(!userId) {
	  userId = this.flickrUserId;
	}
	if(!apiKey) {
      apiKey = this.flickrApiKey;		
	}
    if(!page) {
      page = this.page;
    }
    if(!perPage){
      perPage = this.perPage;
    }
    if(!setId) {
      var setId = this.flickrSetId;
	 }
    var extraArgs = "";
    if(setId && setId != "")
    {
      method = "flickr.photosets.getPhotos";
      extraArgs += "&photoset_id=" + setId;
    }
  
    var thisWidget = this;
    dj_global['jsonFlickrApi'] = function(data){
      thisWidget.addFlickrData(data);
    };
  
    dojo.require("dojo.io.*");
    dojo.require("dojo.io.ScriptSrcIO");
	
    var url = "http://www.flickr.com/services/rest/?method="+ method + "&format=json&api_key="
      + apiKey +"&user_id=" + userId+"&page=" + page + "&per_page=" + perPage + extraArgs;	
  
    dojo.io.bind({
      url: url,
      transport: "ScriptSrcTransport",
      mimetype: "application/json"
    });
  },
  
  initSetMenus: function(data) {
  	if(arguments.length == 0) {
	  url = "http://www.flickr.com/services/rest/?method=flickr.photosets.getList&format=json&api_key="
        + this.flickrApiKey +"&user_id=" + this.flickrUserId;
	  var thisWidget = this;
	  dj_global['jsonFlickrApi'] = function(data){
        thisWidget.addFlickrData(data);
      };
		
	  dojo.io.bind({
        url: url,
        transport: "ScriptSrcTransport",
        mimetype: "application/json"
      });
	  return;
	}
	
  	if(data['stat'] != 'ok' || !data['photosets'])
	{
		alert("Error: Invalid Flickr Set data.");
		return;
	}
	data = data['photosets']['photoset'];
	dojo.require("dojo.widget.Menu2");
	dojo.require("dojo.lang.array");
	
	var menu = dojo.widget.byId(this.widgetId + "_menu");
	if(menu) {
		menu.destroy();
	}
	
	var thisWidget = this;
    function makeMenu(items, isTop, id){
      var menu2 = dojo.widget.createWidget("PopupMenu2", 
	    {
		  contextMenuForWindow: isTop, 
		  widgetId:id
		});
	  menu2.addChild(dojo.widget.createWidget("MenuItem2", {
	  	caption: "All Photos",
		onClick: function() {
          thisWidget.flickrSetId = null;
          thisWidget.loadFlickrPublicPhotos();
		}
	  }));
      menu2.addChild(dojo.widget.createWidget("MenuSeparator2"));
      dojo.lang.forEach(items, function(itemJson){	   
	    var item = dojo.widget.createWidget("MenuItem2",  itemJson);
	    menu2.addChild(item);
      });
      return menu2;
    }
	
	var menuItems = [];
	
	function genFn(setIdToLoad) {
		return function() {
		  thisWidget.loadFlickrPublicPhotos(null,null, setIdToLoad);
		  return true;
		};
	}
	
	for(var count = 0; count < data.length; count ++) {
      var setId = data[count].id;
      menuItems[menuItems.length] = {
          caption: data[count]["title"]["_content"],
          onClick: genFn(setId)
	  };
	}
	//this.largeNode.setAttribute("oncontextmenu","return false;");
	this._setMenu = makeMenu(menuItems, false, this.widgetId + "_menu");
	this.createSetButton(); 
  },
  nextPage: function() {
    this.setPage(this.page +1 );
  },
  prevPage: function() {
    this.setPage(this.page -1 );
  },
  setPage: function(num) {
    if(num < 0 || (this.maxPages && num > this.maxPages) || num == this.page){
    return;
  }
  this.page = num;
  this.loadFlickrPublicPhotos();
  }
});

if(typeof(imageGalleryLoaded) == "function"){
  imageGalleryLoaded();
}
