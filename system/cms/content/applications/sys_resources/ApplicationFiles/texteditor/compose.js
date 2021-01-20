//Constants.
SEP_PADDING = 5
HANDLE_PADDING = 7

var yToolbars = new Array();  // Array of all toolbars.

// Initialize everything when the document is ready
var YInitialized = false;
function document.onreadystatechange() {
  if (YInitialized) return;
  YInitialized = true;

  var i, s, curr;

  // Find all the toolbars and initialize them.
  for (i=0; i<document.body.all.length; i++) {
    curr=document.body.all[i];
    if (curr.className == "yToolbar") {
      if (! InitTB(curr)) {
        alert("Toolbar: " + curr.id + " failed to initialize. Status: false");
      }
      yToolbars[yToolbars.length] = curr;
    }
  }

  //Lay out the page, set handler.
  DoLayout();
  window.onresize = DoLayout;

  Composition.document.open()
  Composition.document.write("<body style=\"font:10pt arial,sans-serif\"></body>");
  Composition.document.close()
  Composition.document.designMode="On"
  //setTimeout("Composition.focus()",0)

}

// Initialize a toolbar button
function InitBtn(btn) {
  btn.onmouseover = BtnMouseOver;
  btn.onmouseout = BtnMouseOut;
  btn.onmousedown = BtnMouseDown;
  btn.onmouseup = BtnMouseUp;
  btn.ondragstart = YCancelEvent;
  btn.onselectstart = YCancelEvent;
  btn.onselect = YCancelEvent;
  btn.YUSERONCLICK = btn.onclick;
  btn.onclick = YCancelEvent;
  btn.YINITIALIZED = true;
  return true;
}

//Initialize a toolbar. 
function InitTB(y) {
  // Set initial size of toolbar to that of the handle
  y.TBWidth = 0;
    
  // Populate the toolbar with its contents
  if (! PopulateTB(y)) return false;
  
  // Set the toolbar width and put in the handle
  y.style.posWidth = y.TBWidth;
  
  return true;
}


// Hander that simply cancels an event
function YCancelEvent() {
  event.returnValue=false;
  event.cancelBubble=true;
  return false;
}

// Toolbar button onmouseover handler
function BtnMouseOver() {
  if (event.srcElement.tagName != "IMG") return false;
  var image = event.srcElement;
  var element = image.parentElement;
  
  // Change button look based on current state of image.
  if (image.className == "Ico") element.className = "BtnMouseOverUp";
  else if (image.className == "IcoDown") element.className = "BtnMouseOverDown";

  event.cancelBubble = true;
}

// Toolbar button onmouseout handler
function BtnMouseOut() {
  if (event.srcElement.tagName != "IMG") {
    event.cancelBubble = true;
    return false;
  }

  var image = event.srcElement;
  var element = image.parentElement;
  yRaisedElement = null;
  
  element.className = "Btn";
  image.className = "Ico";

  event.cancelBubble = true;
}

// Toolbar button onmousedown handler
function BtnMouseDown() {
  if (event.srcElement.tagName != "IMG") {
    event.cancelBubble = true;
    event.returnValue=false;
    return false;
  }

  var image = event.srcElement;
  var element = image.parentElement;

  element.className = "BtnMouseOverDown";
  image.className = "IcoDown";

  event.cancelBubble = true;
  event.returnValue=false;
  return false;
}

// Toolbar button onmouseup handler
function BtnMouseUp() {
  if (event.srcElement.tagName != "IMG") {
    event.cancelBubble = true;
    return false;
  }

  var image = event.srcElement;
  var element = image.parentElement;

  if (element.YUSERONCLICK) eval(element.YUSERONCLICK + "anonymous()");

  element.className = "BtnMouseOverUp";
  image.className = "Ico";

  event.cancelBubble = true;
  return false;
}

// Populate a toolbar with the elements within it
function PopulateTB(y) {
  var i, elements, element;

  // Iterate through all the top-level elements in the toolbar
  elements = y.children;
  for (i=0; i<elements.length; i++) {
    element = elements[i];
    if (element.tagName == "SCRIPT" || element.tagName == "!") continue;
    
    switch (element.className) {
    case "Btn":
      if (element.YINITIALIZED == null) {
	if (! InitBtn(element)) {
	  alert("Problem initializing:" + element.id);
	  return false;
	}
      }
      
      element.style.posLeft = y.TBWidth;
      y.TBWidth += element.offsetWidth + 1;
      break;
      
    case "TBGen":
      element.style.posLeft = y.TBWidth;
      y.TBWidth += element.offsetWidth + 1;
      break;
      
    case "TBSep":
      element.style.posLeft = y.TBWidth + 2;
      y.TBWidth += SEP_PADDING;
      break;
      
    case "TBHandle":
      element.style.posLeft = 2;
      y.TBWidth += element.offsetWidth + HANDLE_PADDING;
      break;
      
    default:
      alert("Invalid class: " + element.className + " on Element: " + element.id + " <" + element.tagName + ">");
      return false;
    }
  }

  y.TBWidth += 1;
  return true;
}

function DebugObject(obj) {
  var msg = "";
  for (var i in TB) {
    ans=prompt(i+"="+TB[i]+"\n");
    if (! ans) break;
  }
}

// Lay out the docked toolbars
function LayoutTBs() {
  NumTBs = yToolbars.length;

  // If no toolbars we're outta here
  if (NumTBs == 0) return;

  //Get the total size of a TBline.
  var i;
  var ScrWid = (document.body.offsetWidth) - 6;
  var TotalLen = ScrWid;
  for (i = 0 ; i < NumTBs ; i++) {
    TB = yToolbars[i];
    if (TB.TBWidth > TotalLen) TotalLen = TB.TBWidth;
  }

  var PrevTB;
  var LastStart = 0;
  var RelTop = 0;
  var LastWid, CurrWid;

  //Set up the first toolbar.
  var TB = yToolbars[0];
  TB.style.posTop = 0;
  TB.style.posLeft = 0;

  //Lay out the other toolbars.
  var Start = TB.TBWidth;
  for (i = 1 ; i < yToolbars.length ; i++) {
    PrevTB = TB;
    TB = yToolbars[i];
    CurrWid = TB.TBWidth;

    if ((Start + CurrWid) > ScrWid) { 
      //TB needs to go on next line.
      Start = 0;
      LastWid = TotalLen - LastStart;
    } 
    else { 
      //Ok on this line.
      LastWid = PrevTB.TBWidth;
      //RelTop -= TB.style.posHeight;
      RelTop -= TB.offsetHeight;
    }
      
    //Set TB position and LastTB width.
    TB.style.posTop = RelTop;
    TB.style.posLeft = Start;
    PrevTB.style.width = LastWid;

    //Increment counters.
    LastStart = Start;
    Start += CurrWid;
  } 

  //Set width of last toolbar.
  TB.style.width = TotalLen - LastStart;
  
  //Move everything after the toolbars up the appropriate amount.
  i--;
  TB = yToolbars[i];
  var TBInd = TB.sourceIndex;
  var A = TB.document.all;
  var item;
  for (i in A) {
    item = A.item(i);
    if (! item) continue;
    if (! item.style) continue;
    if (item.sourceIndex <= TBInd) continue;
    if (item.style.position == "absolute") continue;
    item.style.posTop = RelTop;
  }
}

//Lays out the page.
function DoLayout() {
  LayoutTBs();
}

// Check if toolbar is being used when in text mode
function validateMode() {
  if (! bTextMode) return true;
  alert("Please uncheck the \"View HTML source\" checkbox to use the toolbars");
  Composition.focus();
  return false;
}

//Formats text in composition.
function format(what,opt) {
  if (!validateMode()) return;
  
  if (opt=="removeFormat") {
    what=opt;
    opt=null;
  }

  if (opt==null) Composition.document.execCommand(what);
  else Composition.document.execCommand(what,"",opt);
  
  pureText = false;
  Composition.focus();
}

//Switches between text and html mode.
function setMode(newMode) {
  bTextMode = newMode;
  var cont;
  if (bTextMode) {
    cleanHtml();
    cleanHtml();

    cont=Composition.document.body.innerHTML;
    Composition.document.body.innerText=cont;
  } else {
    cont=Composition.document.body.innerText;
    Composition.document.body.innerHTML=cont;
  }
  
  Composition.focus();
}

//Finds and returns an element.
function getEl(sTag,start) {
  while ((start!=null) && (start.tagName!=sTag)) start = start.parentElement;
  return start;
}

function createLink() {
  if (!validateMode()) return;
  
  var isA = getEl("A",Composition.document.selection.createRange().parentElement());
  var str=prompt("Enter link location (e.g. http://www.yahoo.com):", isA ? isA.href : "http:\/\/");
  
  if ((str!=null) && (str!="http://")) {
    if (Composition.document.selection.type=="None") {
      var sel=Composition.document.selection.createRange();
      sel.pasteHTML("<A HREF=\""+str+"\">"+str+"</A> ");
      sel.select();
    }
    else format("CreateLink",str);
  }
  else Composition.focus();
}

function replace(string,text,by) {
// Replaces text with by in string
    var strLength = string.length, txtLength = text.length;
    if ((strLength == 0) || (txtLength == 0)) return string;

    var i = string.indexOf(text);
    if ((!i) && (text != string.substring(0,txtLength))) return string;
    if (i == -1) return string;

    var newstr = string.substring(0,i) + by;

    if (i+txtLength < strLength)
        newstr += replace(string.substring(i+txtLength,strLength),text,by);

    return newstr;
}


function createInlineLink(type) {
	if (!validateMode()) return;
	//Avoid adding links or variants if the selected html contains contenrEditable attribute
	var inlinetype = type;
	var selectedHtml = Composition.document.selection.createRange().htmlText;
	if(selectedHtml.toLowerCase().indexOf("contenteditable=") != -1)
	{
		alert("Your selection contains an inline variant or part of it.\nYou can not insert inline links or images or variants if your selection contains inline variant or part of it.");
		return;
	}

	if(inlinetype == 'rxhyperlink')
		inlineslotid = Composition.document.InlineLinkSlot;
	else if(inlinetype == 'rximage')
		inlineslotid = Composition.document.InlineImageSlot;
	else if(inlinetype == 'rxvariant')
		inlineslotid = Composition.document.InlineVariantSlot;
	else
	{
	   alert("The supplied inline type(" + inlinetype + ") is invalid.");
	   return;
	}
	var urlString = "searchagain";
	var params = "";
	while(urlString == "searchagain")
	{
		if (params!="")
		{
			params = String(params);
			params = replace(params,'%25','');
		}
		var inlinetext=Composition.document.selection.createRange().text;
		var params = window.showModalDialog("/Rhythmyx/sys_ceInlineSearch/contentsearch.html?inlineslotid=" + inlineslotid + "&inlinetext=" + inlinetext + "&inlinetype=" + inlinetype + "&" + params, "testVal", "dialogHeight: 300px; dialogWidth: 400px; dialogTop: 65px; dialogLeft: 453px; edge: Raised; center: Yes; help: No; resizable: Yes; status: No;");

		if(params == "cancel")
			return;

		var urlString = window.showModalDialog("/Rhythmyx/sys_ceInlineSearch/resultpage.html?inlineslotid=" + inlineslotid + "&inlinetext=" + inlinetext + "&inlinetype=" + inlinetype + "&" + params, "testVal", "dialogHeight: 300px; dialogWidth: 400px; dialogTop: 65px; dialogLeft: 453px; edge: Raised; center: Yes; help: No; resizable: Yes; status: No;");
		if(urlString == "cancel")
			return;
		urlString = window.showModalDialog("/Rhythmyx/sys_ceInlineSearch/returnvariant.html?inlineslotid=" + inlineslotid + "&inlinetext=" + inlinetext + "&inlinetype=" + inlinetype + "&" + urlString, "testVal", "dialogHeight: 300px; dialogWidth: 400px; dialogTop: 65px; dialogLeft: 453px; edge: Raised; center: Yes; help: No; resizable: Yes; status: No;");

	}

	if ((urlString==null) || (urlString=="http://")) 
	{
	  Composition.focus();
	}
	else
	{
	   var sel=Composition.document.selection.createRange();
	   sel.pasteHTML(urlString);
	   sel.select();
	}
	fixTargets();
}

//Fixes the targets for the links inside dhtml editors.
function fixTargets() {
	for (i=0;i<Composition.document.links.length;i++ )
	{
		Composition.document.links[i].target = "_new";
	}
}


//Sets the text color.
function foreColor() {
  if (! validateMode()) return;
  var arr = showModalDialog("colorselect.html", "", "dialogWidth:355px; dialogHeight:370px; center:yes");
  if (arr != null) format('forecolor', arr);
  else {
  Composition.focus()
  }
}

//Sets the background color.
function backColor() {
  if (! validateMode()) return;
  var arr = showModalDialog("colorselect.html", "", "dialogWidth:355px; dialogHeight:370px; center:yes");
  if (arr != null) format('backcolor', arr);
  else {
  Composition.focus()
  }
}

function cleanHtml() {
  var fonts = Composition.document.body.all.tags("FONT");
  var curr;
  for (var i = fonts.length - 1; i >= 0; i--) {
    curr = fonts[i];
    if (curr.style.backgroundColor == "#ffffff") curr.outerHTML = curr.innerHTML;
  }
}

function getPureHtml() {
  var str = "";
  var paras = Composition.document.body.all.tags("P");
  if (paras.length > 0) {
    for (var i=paras.length-1; i >= 0; i--) str = paras[i].innerHTML + "\n" + str;
  } else {
    str = Composition.document.body.innerHTML;
  }
  return str;
}