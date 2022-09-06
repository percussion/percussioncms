/**
Javascript functions to manipulate the P13N edit
*/
/**
 * The main Javascript file for profile editor 
 */
/**
 * Global variables
 */
/**
 * Variable to hold on to the selected node. 
 * This is set when a profile weight node is selected and reset to null when the profile editor is loaded.
 */
//$(parent.frameSidebar.document).find('#ProfileEditPane').click(function() { alert("hello"); });
var p13nSelectedNode = null;
var p13nPreviewPageIsDirty = false;

$(document).ready(function(){
	$("#SegmentSplitter").splitter({
		type: 'v',
		initA: true,	// use width of A (#LeftPane) from styles
		accessKey: '|'
	});
	// Firefox doesn't fire resize on page elements
	$(window).bind("resize", function(){
		$("#SegmentSplitter").trigger("resize"); 
	}).trigger("resize");

	//Bind the menu actions
	$("#p13nMenuNew").click(p13nNewProfile);
	$("#p13nMenuSave").click(p13nSaveProfile);
	$("#p13nMenuSaveAs").click(p13nSaveAsProfile);
	$("#p13nMenuDelete").click(p13nDeleteProfile);
	$("#p13nMenuRefresh").click(p13nSaveProfileOnlyToSession);
	$("#p13nMenuHideorShow").toggle(p13nHideOutLine,p13nShowOutLine);
	$("#p13nMenuAA").click(p13nOpenAAPage);
	$('#ProfileEditPane').click( function () {
		if (p13nPreviewPageIsDirty) {
			p13nSaveProfileOnlyToSession();
		}
	});
	
	
	jQuery('#PagePreviewPane iframe').load(p13nResizePreviewFrame);
	
	//Load the profile editor with default profile
	p13nRefreshProfileEditor();

	//Load the preview page

});	

//Not used anymore.
p13nLoadProfileEditor = function() {
	$("#ProfileEditPane").load(profileEditorUrl,{},p13nProfileEditorLoadedRefreshContentPage);
};

p13nRefreshProfileEditor = function() {
	$("#ProfileEditPane").load(profileEditorUrl + " #ProfileEditPane",{},p13nProfileEditorLoaded);
};

/**
 * New profile menu action
 * Loads the profile editor with new Profile in it.
 */
p13nNewProfile = function()
{
	$("#ProfileEditPane").load(profileEditorUrl,{newProfile:"true"},p13nProfileEditorLoadedRefreshContentPage);
};

/**
 * Save profile menu action
 * Saves the profile, if the profile is newProfile then calls the p13nSaveAsProfile. 
 */
p13nSaveProfile = function()
{
	if($("#userid").attr("value")==undefined)
	{
		p13nSaveAsProfile();
		return;
	}
	var options = { 
		beforeSubmit : function (formData, jq, options) {
			formData[formData.length] = {name : 'saveProfile', value: 'true'} ;
		},
		success : p13nHandleProfileDataSubmit
	};
	$(".p13nProfileData").ajaxSubmit(options);
};

p13nSaveProfileOnlyToSession = function()
{
	var options = { 
		beforeSubmit : function (formData, jq, options) {
			formData[formData.length] = {name : 'saveOnlyToSession', value: 'true'} ;
		},
		success : function (txt) {
			handleTreeHighLighting(); p13nRefreshPage();
			p13nPreviewPageIsDirty = false;
		} 
	};
	$(".p13nProfileData").ajaxSubmit(options);
};

/**
 * SaveAs profile menu action
 * This method does not really save the profile rather makes the 
 * userid and label panel visible. The save button from that panel
 * calls p13nUserDataSave to save the profile.
 */
p13nSaveAsProfile = function()
{
	$(".p13nUserData").css("display","block");
};

/**
 * Saves the new profile. 
 */
p13nUserDataSave = function()
{
	$("#userid").val($("#p13nUserDataInputID").val());
	$("#label").val($("#p13nUserDataInputLabel").val());
   $(".p13nProfileData #id").val(0);
	$(".p13nUserData").css("display","none");
   //$(".p13nProfileData").ajaxSubmit(p13nHandleProfileDataSubmit);
   p13nSaveProfile();
};

p13nHandleProfileDataSubmit = function(responseText, statusText)
{
   if(statusText != "success")
   {
      alert("Failed to save profile");
      return;
   }
   var respObj = $(responseText);
   /*
   var newuid = respObj.find(".p13nProfileData #id").attr("value");
   var newoption = "<option value=" + newuid + ">"+ $("#label").val() + "</option>";
   $(".p13nProfileSelect").html($(".p13nProfileSelect").html()+newoption);
   var psBox = $(".p13nProfileSelect").get(0);
   psBox.options.selectedIndex = psBox.options.length -1;
   */
   
   $(".p13nProfileSelector").replaceWith(respObj.find(".p13nProfileSelector"));
   $(".p13nProfileSelect").change(p13nProfileChange);
   handleTreeHighLighting();
   p13nRefreshPage();
   p13nPreviewPageIsDirty = false;
};

/**
 * User data cancel handling function. Resets the user id and label fields.
 */
p13nUserDataCancel = function()
{
	$(".p13nUserData").css("display","none");
	$("#p13nUserDataInputID").val("");
	$("#p13nUserDataInputLabel").val("");
};

/**
 * Delete profile menu action
 */
p13nDeleteProfile = function()
{
	alert("This feature has not yet been implemented.");
};

/**
 * Refreshes the page with the updated profile values
 */
p13nRefreshPage = function()
{
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
	
};

p13nResizePreviewFrame = function ()
{
	var iframe = jQuery('#PagePreviewPane iframe').get(0);
	var height = iframe.contentWindow.document.body.scrollHeight;
	var width = iframe.contentWindow.document.body.scrollWidth;
	//alert("scrolling h:" + height + " dom h:" + iframe.height);
  	//change the height of the iframe
  	if ( p13nInsideRhythmyx ) {
  		iframe.height = height + 40;
  	}
  	else {
  		iframe.height = height + 800;
  	}
  	iframe.width = width;
  	p13nRefreshProfileEditor();
};

/**
 * Function to handle hiding the outline
 */
p13nHideOutLine = function()
{
	$("#p13nMenuHideorShow").text("Show Outline");
	$("#ProfileEditPane").css("display","none");
};

/**
 * Function to handle show the outline
 */
p13nShowOutLine = function()
{
	$("#p13nMenuHideorShow").text("Hide Outline");
	$("#ProfileEditPane").css("display","block");
};

/**
 * Function to open the active assemble page.
 */
p13nOpenAAPage = function()
 {
	var link = jQuery('#PagePreviewPane iframe').get(0).src;
	//Bug with the template parameter in AA.
	link = link.replace("sys_template", "sys_variantid");
	link = link + "&sys_command=editrc";
	window.open(link);
 };

/**
 * Function executed when the profile editor loaded.
 */
p13nProfileEditorLoaded = function()
{
	$(".SegmentTree").treeview( { collapsed: true, persist: 'cookie'} );
	$(".p13nSegmentWeightInput").click(handleSegmentWeightClick);
	$(".p13nSegmentWeightInput").keypress(function(e) {
		if (e.which == 13) {
			p13nSaveProfileOnlyToSession();
		}
		else {
			p13nPreviewPageIsDirty = true;
		}
	});
	$("#p13nLockProfile").change(function() {
		p13nSaveProfileOnlyToSession();
	});
	$(".p13nProfileSelect").change(p13nProfileChange);
	$(".p13nSelectableSegment").css("cursor","default");
   var options = {success:p13nHandleProfileDataSubmit};
	$(".p13nProfileData").ajaxForm(options);
	$("#p13nUserDataCancelButton").click(p13nUserDataCancel);
	$("#p13nUserDataSaveButton").click(p13nUserDataSave);
   handleTreeHighLighting();
	p13nSelectedNode = null;
	
	jQuery(".segmentWeightCloud a").each(function(i,e) { 
		jQuery(e).css({'font-size': ((55 + 10 * parseInt(e.title)) + '%')}); 
	});
	
	$(".segmentWeightCloud a").click(function() { 
		p13nFocusSegmentInput(this.id.replace("cloud_segment_","")); 
	});
	
};

p13nProfileEditorLoadedRefreshContentPage = function() {
	p13nProfileEditorLoaded();
	p13nRefreshPage();
};

/**
 * Handles the profile changes. 
 * Reloads the profile editor with the selected profile id.
 */
p13nProfileChange = function()
{
	var psBox = $(".p13nProfileSelect").get(0);
	var profileid = psBox.options[psBox.options.selectedIndex].value;
	var params = profileid == 0 ? {} : { id : profileid, switchProfile : "true"};
	$("#ProfileEditPane").load(profileEditorUrl, params,p13nProfileEditorLoadedRefreshContentPage);
};


p13nFocusSegmentInput = function(id)
{
	$("#segmentWeights_" + id).parents().filter("li.expandable").children(".hitarea").click();
	$("#segmentWeights_" + id).focus();
};

/**
 * Handles the segment weight click changes.
 */
handleSegmentWeightClick = function()
{
   if(p13nSelectedNode != null)
	{
      handleTreeHighLighting();
   }

  	p13nSelectedNode = $(this);
	
};

handleTreeHighLighting = function()
{
   $(".p13nSegmentWeightSpan").css("font-weight","normal");
   var wNodes = $(".p13nSegmentWeightInput[value != '']");
   wNodes.each(function(i, w) {
   		var nameNode = $(w).siblings(".p13nSegmentWeightSpan");
   		handleNodeHighLighting(nameNode, "bold");
   });
};

handleNodeHighLighting = function(jqNode,fwt)
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
      handleNodeHighLighting(chSpan,fwt);
   }
};
