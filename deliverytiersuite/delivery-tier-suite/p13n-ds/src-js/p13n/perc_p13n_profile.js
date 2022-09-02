
//$(parent.frameSidebar.document).find('#ProfileEditPane').click(function() { alert("hello"); });

var perc_p13n = getP13N();

function getP13N() {
	var perc_p13n = null;
	if (top.perc_p13n != null) {
		//alert("Using existing p13n " + location.href);
		perc_p13n = top.perc_p13n;
	}
	else {
		//alert("Create p13n " + location.href)
		perc_p13n = {};	
		perc_p13n._loggers = {};
		perc_p13n._onProfileEditor = [];
		top.perc_p13n = perc_p13n;
	}
	
	return perc_p13n;
}

perc_p13n.getLogger = function(loggerName) {
	if (perc_p13n._loggers[loggerName] != null ) return perc_p13n._loggers[loggerName];
	var logger = log4javascript.getLogger(loggerName);
	perc_p13n._loggers[loggerName] = logger;
	logger.removeAllAppenders();
	var appender = new log4javascript.BrowserConsoleAppender();
	var layout = new log4javascript.PatternLayout("%p [%c] %m");
	appender.setLayout(layout);
	logger.addAppender(appender);
	if ( ! perc_p13n._popupAppender && jQuery.browser.msie ) { 
		perc_p13n._popupAppender = new log4javascript.PopUpAppender();
		perc_p13n._popupAppender.setLayout(layout);
	}
	if (jQuery.browser.msie) {
		logger.addAppender(perc_p13n._popupAppender);
	}
	return logger;
}

perc_p13n.log = perc_p13n.getLogger("perc_p13n");

perc_p13n.triggerOnProfileEditor = function(profile) {
	perc_p13n.log.debug("Trigger on profile editor.");
	$.each(perc_p13n._onProfileEditor, function(i, func) {
		func(profile);
	});	
}

perc_p13n.bindOnProfileEditor = function(callback, win) {
	var editor = perc_p13n.getProfileEditor(win);
	if (editor == null) {
		perc_p13n._onProfileEditor.push(callback);
	}
	else {
		// The profile editor is ready now.
		callback(editor);
	}
}

perc_p13n.getProfileEditor = function(win) {
	
	perc_p13n.log.debug("Getting Profile Editor for window: " + win.location.href);
	if (typeof win.p13nProfileEditor != "undefined") {
		perc_p13n.log.debug("Profile Editor is attached to the window object: " 
			+ win.location.href);
		return win.p13nProfileEditor;
	}
	else if (typeof top.p13nProfileEditor != "undefined"){
		perc_p13n.log.debug("Profile Editor is attached to the parent window object for: " 
			+ win.location.href );
		return top.p13nProfileEditor;
	}
	else if (top == win){
		perc_p13n.log.debug("Creating Profile Editor for window: " + win.location.href);
		win.p13nProfileEditor = new P13NProfileEditor();
		perc_p13n.triggerOnProfileEditor(win.p13nProfileEditor);
		return win.p13nProfileEditor;
	}
	else {
		/*
		perc_p13n.log.warn("Profile Editor was not loaded in parent for window: " + location.href);
		perc_p13n.log.warn("Creating Profile Editor for parent: " + parent);
		parent.p13nProfileEditor = new P13NProfileEditor();
		*/
		perc_p13n.log.warn("Profile Editor was not loaded in parent for window: " + win.location.href);
		return null;
	}
}




function P13NProfileEditor() {
	this.selectedNode = null;
	this.previewPageIsDirty = false;
	this.profileEditorIsDirty = false;
	this.lastTrackingResponse = null;
	this._sidebarDocument = null;
	this._menubarDocument = null;
	this.profileEditorUrl = '/soln-p13n/profile/edit';
	this._mainDocument = top.document;
	$("#frameMain").load(willDo("onPreviewPageLoad", this));
}

P13NProfileEditor.prototype.log = perc_p13n.getLogger("profileEditor");

function willDo(funcStr, obj) {
	return function(eventData) { 
		var args = $.makeArray(arguments);
		args.unshift(this);
		return obj[funcStr].apply(obj, args); 
	};
}

function queryFrom(container) {
	//return this.profileFrame.find(query);
	return function(arg) { return $(container).find(arg); };
}

P13NProfileEditor.prototype.__registerMenu = function() {
	this.log.debug("Registering Menu");
	var q = queryFrom(this.getMenubarDocument());
	var p = this;
	q("#p13nMenuNew").click(willDo("newProfile",this));
	q("#p13nMenuSave").click(willDo("saveProfile",this));
	q("#p13nMenuSaveAs").click(willDo("saveAsProfile",this));
	q("#p13nMenuDelete").click(willDo("deleteProfile",this));
	q("#p13nMenuRefresh").click(function() {
		if (this.profileEditorIsDirty) {
			p.saveProfileOnlyToSession();
		}
		else {
			//p.profileEditorIsDirty = true;
			p.refreshPreviewPage(true);
		}
	});
	
	q("#p13nMenuAA").click(willDo("openAAPage",this));
	q("#p13nMenuHideorShow").toggle(willDo("hideOutLine", this), willDo("showOutLine",this));
	this.log.debug("Finished Registering Menu");
}


P13NProfileEditor.prototype.getMenubarDocument = function() {
	return this._menubarDocument;
}

P13NProfileEditor.prototype.setMenubarDocument = function(d) {
	this.log.debug("Setting menubar dom" + d);
	this._menubarDocument = d;
	this.__registerMenu();
}


P13NProfileEditor.prototype.getMainDocument = function() {
	return this._mainDocument;
}

P13NProfileEditor.prototype.setMainDocument = function(d) {
	this.log.debug("Setting main dom" + d);
	this._mainDocument = d;
}

P13NProfileEditor.prototype.getSidebarDocument = function() {
	return this._sidebarDocument;
}

P13NProfileEditor.prototype.setSidebarDocument = function(d) {
	this.log.debug("Setting sidebar dom" + d);
	this._sidebarDocument = d;
	this.__registerProfileEditor();
}

P13NProfileEditor.prototype.newProfile = function() {
	this.loadProfileEditor({newProfile:"true"});
}

P13NProfileEditor.prototype.loadProfileEditor = function (options, func) {
	this.log.debug("loading profile editor");
	var callback = $.isFunction(func) ? func : function() {};
	if (! options ) {
		// If no parameters are supplied then we don't
		// need to refresh the preview page.
		this.previewPageIsDirty = false;
	}
	else {
		this.previewPageIsDirty = true;
	}
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	var purl = this.profileEditorUrl;
	if (jQuery.browser.msie) {
		
		this.log.debug("Browser is IE so we reload the whole sidebar frame: " + purl);
		var params = "";
		if (! options) {
			options = {};
		}
		var url = purl + '?' + $.param(options);
		this.log.debug("sidebar url should now be: " + url);
		this.setSidebarUrl(url);
		callback(p);
	}
	else {
		q("#ProfileSidebar").load(
			this.profileEditorUrl + " #ProfileEditPane",
			options,
			function() { 
				p.onProfileEditorLoad(); 
				callback(p); 
			}
		);
	}
}

P13NProfileEditor.prototype.refreshProfileEditor = function(force) {
	this.log.debug("Refreshing profile Editor.");
	if (this.profileEditorIsDirty || force) {
		this.loadProfileEditor();
	}
	else {
		this.log.debug("Not Refreshing profile Editor.")
	}
}

P13NProfileEditor.prototype.refreshProfileEditorWithTracking = function(data, params) {
	this.log.debug("Refreshing profile Editor.");
	var p = this;
	p.lastTrackingResponse = data;
	this.loadProfileEditor(null, function() { 
		var q = queryFrom(p.getSidebarDocument());
		var message = (data && data.visitorProfile && data.visitorProfile.lockProfile) ?
			"Visit tracking prevented..." :
			"Visit Tracked...";
		q(".trackingMessage").fadeIn("fast")
			.html('<div class="message">' + message + "</div>")
			.append('<div class="actionName">Action: ' + params.actionName + "</div>");
			//.toggle(function(){$(this).hide();}, function(){$(this).show();});
	});
}
/**
 * Save profile menu action
 * Saves the profile, if the profile is newProfile then calls the p13nSaveAsProfile. 
 */
P13NProfileEditor.prototype.saveProfile = function()
{
	this.log.debug("saveProfile");
	var q = queryFrom(this.getSidebarDocument());
	if(q("#userId").attr("value")==undefined) {
		this.saveAsProfile();
		return;
	}
	else {
		this.submitProfileData("saveProfile");
	}
}

P13NProfileEditor.prototype.submitProfileData = function (flag) {

	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	var options = { 
		beforeSubmit : function (formData, jq, options) {
			formData[formData.length] = {name : flag , value: 'true'} ;
		},
		success : willDo("onProfileDataSubmit",p)
	};
	q(".p13nProfileData").ajaxSubmit(options);
}


P13NProfileEditor.prototype.saveProfileOnlyToSession = function()
{
	this.log.debug("saveProfileOnlyToSession");
	this.submitProfileData("saveOnlyToSession");
}

/**
 * SaveAs profile menu action
 * This method does not really save the profile rather makes the 
 * userid and label panel visible. The save button from that panel
 * calls p13nUserDataSave to save the profile.
 */
P13NProfileEditor.prototype.saveAsProfile = function()
{
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	q(".p13nUserData").css("display","block");
}




/**
 * Saves the new profile. 
 */
P13NProfileEditor.prototype.onUserDataSave = function() {
	this.log.debug("User saving data.");
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	q("#userId").val(q("#p13nUserDataInputID").val());
	q("#label").val(q("#p13nUserDataInputLabel").val());
   	q(".p13nProfileData #id").val(0);
	q(".p13nUserData").css("display","none");
   //$(".p13nProfileData").ajaxSubmit(p13nHandleProfileDataSubmit);
   this.saveProfile();
}

P13NProfileEditor.prototype.onProfileDataSubmit = function(domObj, responseText, statusText) {
	this.log.debug("onProfileDataSubmitted");
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
   if(statusText != "success") {
      this.log.error("Failed to save profile: " + statusText);
      return;
   }

   if ( ! jQuery.browser.msie ) {
	   var respObj = jQuery("<div/>").append(responseText.replace(/<script(.|\s)*?\/script>/g, ""));
	   q("#ProfileEditPane").replaceWith(respObj.find("#ProfileEditPane").hide());
	   this.__registerProfileEditor();
	   q("#ProfileEditPane").show();
   }
   else {
	   this.loadProfileEditor();
   }
   this.profileEditorIsDirty = false;
   this.previewPageIsDirty = true;
   this.refreshPreviewPage(true);
}

/**
 * User data cancel handling function. Resets the user id and label fields.
 */
P13NProfileEditor.prototype.onUserDataCancel = function() {
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	q(".p13nUserData").css("display","none");
	q("#p13nUserDataInputID").val("");
	q("#p13nUserDataInputLabel").val("");
}

/**
 * Delete profile menu action
 */
P13NProfileEditor.prototype.deleteProfile = function() {
	this.log.debug("!! deleteProfile !!")
	var profileid = this.getSelectedProfileId()
	var params = profileid == 0 ? {} : { id : profileid, deleteProfile : "true"};
	this.loadProfileEditor(params);
}

P13NProfileEditor.prototype.getPreviewPageUrl = function() {
	return parent.frameMain.location.href.toString();
}

P13NProfileEditor.prototype.setPreviewPageUrl = function(url) {
	parent.frameMain.location.href = url;
}

P13NProfileEditor.prototype.getSidebarUrl = function() {
	return parent.frameSidebar.location.href.toString();
}

P13NProfileEditor.prototype.setSidebarUrl = function(url) {
	parent.frameSidebar.location.href = url;
}

/**
 * Refreshes the page with the updated profile values
 */
P13NProfileEditor.prototype.refreshPreviewPage = function(force) {
	this.log.debug("refreshPreviewPage url: " + this.getPreviewPageUrl());
	if (this.previewPageIsDirty || force) {
		this.previewPageIsDirty = false;
		this.setPreviewPageUrl(this.getPreviewPageUrl());
	}
	else {
		this.log.debug("not refreshing page because previewPageIsDirty=" + this.previewPageIsDirty);
	}
	/*
	var iframe = jQuery('#PagePreviewPane iframe').get(0);
	var src = iframe.src;
	var realSrc = iframe.contentWindow.location.href;
	realSrc = realSrc.replace("about:blank","").replace(/^\s+|\s+$/g,"");
	if (realSrc != "" && (realSrc.indexOf("p13n_enable=true") != -1 || 
			! p13nInsideRhythmyx )) {
		iframe.contentWindow.location.reload(true);
	}
	else if (realSrc != "" && realSrc.indexOf("p13n_enable=true") == -1 && p13nInsideRhythmyx) {
		realSrc = realSrc + "&p13n_enable=true";
		iframe.contentWindow.location.href = realSrc;
	}
	else {	
		iframe.src = src;
	}
	*/
}

/**
 * Function to handle hiding the outline
 */
P13NProfileEditor.prototype.hideOutLine = function()
{
	this.log.debug("Hide sidebar.");
	var qSide = queryFrom(this.getSidebarDocument());
	var qMenu = queryFrom(this.getMenubarDocument());
	qMenu("#p13nMenuHideorShow").text("Show Outline");
	//TODO fix me.
	//$("#ProfileEditPane").css("display","none")
	//$('#frameSidebar').parent().get(0).cols = "0,*";
	$(this._mainDocument).find('#framesetMain').get(0).cols="0,*";
}

/**
 * Function to handle show the outline
 */
P13NProfileEditor.prototype.showOutLine = function()
{
	this.log.debug("show sidebar.");
	var qSide = queryFrom(this.getSidebarDocument());
	var qMenu = queryFrom(this.getMenubarDocument());
	qMenu("#p13nMenuHideorShow").text("Hide Outline");
	//TODO fix me
	//$("#ProfileEditPane").css("display","block");
	$(this._mainDocument).find('#framesetMain').get(0).cols = "250,*";
}

/**
 * Function to open the active assemble page.
 */
P13NProfileEditor.prototype.openAAPage = function()
 {
	var link = this.getPreviewPageUrl();
	//Bug with the template parameter in AA.
	link = link.replace("sys_template", "sys_variantid");
	link = link + "&sys_command=editrc";
	window.open(link);
 }



/**
 * Function executed when the profile editor loaded.
 */
P13NProfileEditor.prototype.__registerProfileEditor = function()
{
	this.log.debug("Register Profile Editor");
	var q = queryFrom(this.getSidebarDocument());
	var p = this;
	
	q(".segmentWeightCloud a").click(function() { 
		p.focusSegmentInput(this.id.replace("cloud_segment","")); 
	});
	
	//setTimeout(function() {
		q(".SegmentTree").treeview( { collapsed: true, persist: 'cookie'} );
	//}, 100);
	
	q(".p13nSegmentWeightInput").click(willDo("onSegmentWeightClick", p));
	q(".p13nSegmentWeightInput").keypress(function(e) {
		if (e.which == 13) {
			p.saveProfileOnlyToSession();
		}
		else {
			p.profileEditorIsDirty = true;
		}
	});
	
	q(".segmentWeightTree").click(function() {
		if (p.profileEditorIsDirty) {
			p.saveProfileOnlyToSession();
		}
	});
	
	q("#p13nLockProfile").change(function() {
		p.saveProfileOnlyToSession();
	});
	q(".p13nProfileSelect").change(willDo("onProfileChange",this));
	q(".p13nSelectableSegment").css("cursor","default");
	
	var options = { success : willDo("onProfileDataSubmit", p) };
	
	q(".p13nProfileData").ajaxForm(options);
	q("#p13nUserDataCancelButton").click(willDo("onUserDataCancel",p));
	q("#p13nUserDataSaveButton").click(willDo("onUserDataSave",p));
	
	this.handleTreeHighLighting();
	this.selectedNode = null;
	/*
	 * Show the tracking message after 1.5 seconds no
	 * matter what (it may show up) earlier.
	 */
	setTimeout(function() { q(".trackingMessage").fadeIn("fast"); }, 1500);
	this.log.debug("Finished Register Profile Editor");
}

P13NProfileEditor.prototype.onProfileEditorLoad = function() {
	this.log.debug("Profile Editor Loaded");
	this.__registerProfileEditor();
	this.profileEditorIsDirty = false;
	this.refreshPreviewPage(false);
}

P13NProfileEditor.prototype.onPreviewPageLoad = function () {
	this.log.debug("Preview Page Loaded");
	this.previewPageIsDirty = false;
	//The tracking system will refresh the profile editor
	//see perc_p13n_track.js
	//this.refreshProfileEditor(true);
}

P13NProfileEditor.prototype.getSelectedProfileId = function() {
	var q = queryFrom(this.getSidebarDocument());
	var psBox = q(".p13nProfileSelect").get(0);
	return psBox.options[psBox.options.selectedIndex].value;
}
/**
 * Handles the profile changes. 
 * Reloads the profile editor with the selected profile id.
 */
P13NProfileEditor.prototype.onProfileChange = function() {
	var profileid = this.getSelectedProfileId()
	var params = profileid == 0 ? {} : { id : profileid, switchProfile : "true"};
	this.loadProfileEditor(params);
}


P13NProfileEditor.prototype.focusSegmentInput = function(id) {
	this.log.debug("focusSegmentInput on id: " + id);
	var q = queryFrom(this.getSidebarDocument());
	q("#segmentWeights_" + id).parents().filter("li.expandable").children(".hitarea").click();
	q("#segmentWeights_" + id).focus();
}

/**
 * Handles the segment weight click changes.
 */
P13NProfileEditor.prototype.onSegmentWeightClick = function(html) {
	var p = this;
	var q = queryFrom(this.getSidebarDocument());
	if(this.selectedNode != null) {
      this.handleTreeHighLighting();
    }
  	this.selectedNode = $(html);
	
}

P13NProfileEditor.prototype.handleTreeHighLighting = function() {
	var p = this;
	var q = queryFrom(this.getSidebarDocument());
   	q(".p13nSegmentWeightSpan").css("font-weight","normal");
   	var wNodes = q(".p13nSegmentWeightInput[value != '']");
   	wNodes.each(function(i, w) {
		if(w.value !== 0){
			var nameNode = $(w).siblings(".p13nSegmentWeightSpan");
			p.handleNodeHighLighting(nameNode, "bold");
		}
   	});
}

P13NProfileEditor.prototype.handleNodeHighLighting = function(jqNode,fwt)
{
   if(jqNode == null)
      return;
   else if(jqNode.css("font-weight")==fwt)
      return;
   else if(jqNode.is(".SegmentTree"))
      return;
   else
   {
      jqNode.css("font-weight",fwt);
      var par = jqNode.parent().parent();
      var chSpan = null;
      var loopvar = 0;
      while(loopvar++ < 100)
      {
          if(par.is("li"))
          {
             chSpan = par.children("span");
             break;
          }
          par = par.parent();
      }
      this.handleNodeHighLighting(chSpan,fwt);
   }
}
