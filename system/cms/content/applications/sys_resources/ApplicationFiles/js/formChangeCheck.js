 // Javascript to check for field changes
 // before closing a popup editor window
 
 
 
 //Globals   
 var ps_initialChecksum;
 var ps_theForm = null;
 var ps_hasEktron = false;
 var ps_hasWifx = false;
 var ps_formParams = "";
 var ps_submitElem;

 //Display dirty field warning
 function ps_warnIfDirty(){
    if(!ps_isPreviewMode() && ps_isFormDirty() == true){
     return window.confirm(LocalizedMessage('form_change_warning'));
      }

  }

  //Mark window for closing by activeedit
  function ps_MarkWindowForClose(){
  	
  	if(window.opener != null)
  	{
  	   window.opener.ps_CloseMe = true;
  	}
  	
  }
  
  //Set update indicator flag
  function ps_setUpdateFlag()
  {
  	if(window.opener != null)
  	   window.opener.ps_updateFlag = true;
  	
  }

 // Close the edit window but check for
 // dirty fields and post warning if changes
 // to the form were made
 function ps_closeWithDirtyCheck()
 {
    if(ps_theForm != null && ps_initialChecksum != null)
    {
        if(ps_warnIfDirty())
        {
            if(window.opener != null)
                window.opener.ps_noCloseFlag = false;
            ps_MarkWindowForClose();
            ps_theForm.onsubmit();
            if(!ps_hasWifx)
            {
                _formAlreadySubmitted = false; // Get around double click check
                ps_submitElem.click();
            }
        }
        else if(window.opener != null)
        {
            window.opener.ps_noCloseFlag = false;
            if(window.opener.ps_updateFlag == true)
            {
                if(!isOpenerActiveAssembly())
                {
                    // Refresh applet's selected view
                    if(ps_theForm.sys_contentid==null)
                    {		      
                        refreshCxApplet(window.opener, "Selected", null, null);
                    }
                    else
                    {                     
                        refreshCxApplet(window.opener, "Selected",ps_theForm.sys_contentid.value,"");
                    }
                    window.opener.ps_updateFlag = false;
                }
            }
            window.close();
        }
        else
        {
            window.close();
        }
    }
    else
    {
        window.close();
    }
 }
 
/**
 * Function to open the full editor. If the form is dirty, then alerts the
 * user for saving the item before opening the full editor.
 **/
function ps_openFullEditor()
{
     var saveItem = false;
     if(ps_isFormDirty() == true)
     {
        if(confirm("Changes have been made.\nDo you want to save before opening full editor?"))
        {
            saveItem = true;
        }
     }
     if(saveItem)
     {
        if(window.opener != null)
        {
           window.opener.ps_CloseMe = false;
           window.opener.ps_openFullEditorFlag = true;
        }
        _formAlreadySubmitted = false;
        ps_submitElem.click();
     }
     else
     {
         var ceurl = PSHref2Hash(window.location.href);
         ceurl["sys_view"]="sys_All";
         window.location.href=PSHash2Href(ceurl);
     }
}

 // Is the opener window Active Assembly?
 function isOpenerActiveAssembly()
 {
   if(window.opener != null && !window.opener.closed)
   {
     var openerUrl = window.opener.location.href;
     if(openerUrl.indexOf("sys_command=editrc") != -1)
        return true;
   }
   return false;
 }
 // Sets the flag that indicates that this window
 // should not be automatically closed
 function ps_noCloseOnSave(){
   if(window.opener != null)
   {
      window.opener.ps_noCloseFlag = true;
      window.opener.ps_returnUrl = window.location.href;
   }   
 } 
  
 // This function loops through the form passed in and
 // concatenates all the values together and returns
 // an MD5 hex checksum
 function ps_getAllFieldChecksums(theForm, isInitial){
   ps_theForm = theForm;
   out = "";
  if(ps_theForm != null)
  {
   elementCount = theForm.elements.length;
   for(i=0; i<elementCount; i++){
   	currentEl = theForm.elements[i];

	if(currentEl.type == "submit" && currentEl.name == "submitButton")
		ps_submitElem = currentEl;
   	
   	if(currentEl.name == "formParams")
   		ps_formParams = currentEl.value.substr(1);
   	if(currentEl.type != "hidden"){	
   	if(currentEl.type == "checkbox" || currentEl.type == "radio"){
   	if(currentEl.length == null){
   	//Handle single checkbox and radio buttons
   	if(currentEl.checked == true)
   	          out += "CK" + i; 
   	}else{
   	//Handle checkbox and radio button groups
   	for(y=0; y<currentEl.length; y++){
   	      if(currentEl[y].checked == true)
   	        out += "CK" + i; 
   	      
   	   }
   	  }
   	}else if(currentEl.type == "select-one" || currentEl.type == "select-multiple"){
        //Handle select boxes
   	for(y=0; y<currentEl.options.length; y++){
   	      if(currentEl.options[y].selected == true){
   	        out += currentEl.options[y].value; 
   	      
   	   }
   	  }   	
   	
   	}else if(!(currentEl.type == "" && currentEl.value == undefined)){
   	out += currentEl.value;
   	}
   	}
      }
      //Handle Ektron ewebeditpro
      if(ps_hasEktron){
      instanceCount = eWebEditPro.instances.length;
      for(i=0; i<instanceCount; i++){
        if(eWebEditPro.instances[i].isChanged()){
     
        out += "CH";
        }
      }
      }
      
      //Handle Ektron WebImageFx
     if(ps_hasWifx)
     {
               
       if(WebImageFX.instances[0].editor.IsDirty || wifxIsDirty)
       {
          out += "CW";
       }
      
     }
    
     //Handle Custom Controls
     if(!isInitial && typeof(psCustomControlIsDirty) == "function" && psCustomControlIsDirty())
     {
       out += "CC";
     }
    } 
   checksum = hex_md5(out);
   out = "";

   return checksum;
 
 }
 
 // We get the initial checksum when the form is first
 // loaded
 function ps_getInitialChecksum(theForm){
   ps_initialChecksum = ps_getAllFieldChecksums(theForm, true);
 }
 
 // Do a new checksum on the forms current state and
 // compare it tp the initial checksum.
 // Return true if the fields have data that has changed.
 function ps_isFormDirty(){
   newChecksum = ps_getAllFieldChecksums(ps_theForm, false);	 
   return !(newChecksum == ps_initialChecksum);
 
 }
 
 function ps_isPreviewMode()
 {
    var token = "sys_command=preview";
    var url = window.location.href;
    return (url.indexOf(token) != -1);
 }
 
 
/*
 * A JavaScript implementation of the RSA Data Security, Inc. MD5 Message
 * Digest Algorithm, as defined in RFC 1321.
 * Version 2.0 Copyright (C) Paul Johnston 1999 - 2002.
 * Other contributors: Greg Holt, Ydnar
 * Distributed under the Lesser GNU Public License (LGPL)
 * See http://pajhome.org.uk/crypt/md5 for more info.
 */

/*
 * Configurable variables. You may need to tweak these to be compatible with
 * the server-side, but the defaults work in most cases.
 */
var hexcase = 0   /* hex output format. 0 - lowercase; 1 - uppercase        */
var b64pad  = ""  /* base-64 pad character. "=" for strict RFC compliance   */
var chrsz   = 8   /* bits per input character. 8 - ASCII; 16 - Unicode      */

/*
 * These are the functions you'll usually want to call
 * They take string arguments and return either hex or base-64 encoded strings
 */
function hex_md5(s){ return binl2hex(core_md5(str2binl(s), s.length * chrsz)) }
function b64_md5(s){ return binl2b64(core_md5(str2binl(s), s.length * chrsz)) }
function hex_hmac_md5(key, data) { return binl2hex(str_hmac_md5(key, data)) }
function b64_hmac_md5(key, data) { return binl2b64(str_hmac_md5(key, data)) }

/* Backwards compatibility - same as hex_md5() */
function calcMD5(s){ return binl2hex(core_md5(str2binl(s), s.length * chrsz)) }

/* 
 * Perform a simple self-test to see if the VM is working 
 */
function md5_vm_test()
{
  return hex_md5("abc") == "900150983cd24fb0d6963f7d28e17f72"
}

/*
 * Calculate the MD5 of an array of little-endian words, and a bit length
 */
function core_md5(x, len)
{
  /* append padding */
  x[len >> 5] |= 0x80 << ((len) % 32)
  x[(((len + 64) >>> 9) << 4) + 14] = len
  
  var a =  1732584193
  var b = -271733879
  var c = -1732584194
  var d =  271733878

  for(i = 0; i < x.length; i += 16)
  {
    var olda = a
    var oldb = b
    var oldc = c
    var oldd = d

    a = ff(a, b, c, d, x[i+ 0], 7 , -680876936)
    d = ff(d, a, b, c, x[i+ 1], 12, -389564586)
    c = ff(c, d, a, b, x[i+ 2], 17,  606105819)
    b = ff(b, c, d, a, x[i+ 3], 22, -1044525330)
    a = ff(a, b, c, d, x[i+ 4], 7 , -176418897)
    d = ff(d, a, b, c, x[i+ 5], 12,  1200080426)
    c = ff(c, d, a, b, x[i+ 6], 17, -1473231341)
    b = ff(b, c, d, a, x[i+ 7], 22, -45705983)
    a = ff(a, b, c, d, x[i+ 8], 7 ,  1770035416)
    d = ff(d, a, b, c, x[i+ 9], 12, -1958414417)
    c = ff(c, d, a, b, x[i+10], 17, -42063)
    b = ff(b, c, d, a, x[i+11], 22, -1990404162)
    a = ff(a, b, c, d, x[i+12], 7 ,  1804603682)
    d = ff(d, a, b, c, x[i+13], 12, -40341101)
    c = ff(c, d, a, b, x[i+14], 17, -1502002290)
    b = ff(b, c, d, a, x[i+15], 22,  1236535329)

    a = gg(a, b, c, d, x[i+ 1], 5 , -165796510)
    d = gg(d, a, b, c, x[i+ 6], 9 , -1069501632)
    c = gg(c, d, a, b, x[i+11], 14,  643717713)
    b = gg(b, c, d, a, x[i+ 0], 20, -373897302)
    a = gg(a, b, c, d, x[i+ 5], 5 , -701558691)
    d = gg(d, a, b, c, x[i+10], 9 ,  38016083)
    c = gg(c, d, a, b, x[i+15], 14, -660478335)
    b = gg(b, c, d, a, x[i+ 4], 20, -405537848)
    a = gg(a, b, c, d, x[i+ 9], 5 ,  568446438)
    d = gg(d, a, b, c, x[i+14], 9 , -1019803690)
    c = gg(c, d, a, b, x[i+ 3], 14, -187363961)
    b = gg(b, c, d, a, x[i+ 8], 20,  1163531501)
    a = gg(a, b, c, d, x[i+13], 5 , -1444681467)
    d = gg(d, a, b, c, x[i+ 2], 9 , -51403784)
    c = gg(c, d, a, b, x[i+ 7], 14,  1735328473)
    b = gg(b, c, d, a, x[i+12], 20, -1926607734)

    a = hh(a, b, c, d, x[i+ 5], 4 , -378558)
    d = hh(d, a, b, c, x[i+ 8], 11, -2022574463)
    c = hh(c, d, a, b, x[i+11], 16,  1839030562)
    b = hh(b, c, d, a, x[i+14], 23, -35309556)
    a = hh(a, b, c, d, x[i+ 1], 4 , -1530992060)
    d = hh(d, a, b, c, x[i+ 4], 11,  1272893353)
    c = hh(c, d, a, b, x[i+ 7], 16, -155497632)
    b = hh(b, c, d, a, x[i+10], 23, -1094730640)
    a = hh(a, b, c, d, x[i+13], 4 ,  681279174)
    d = hh(d, a, b, c, x[i+ 0], 11, -358537222)
    c = hh(c, d, a, b, x[i+ 3], 16, -722521979)
    b = hh(b, c, d, a, x[i+ 6], 23,  76029189)
    a = hh(a, b, c, d, x[i+ 9], 4 , -640364487)
    d = hh(d, a, b, c, x[i+12], 11, -421815835)
    c = hh(c, d, a, b, x[i+15], 16,  530742520)
    b = hh(b, c, d, a, x[i+ 2], 23, -995338651)

    a = ii(a, b, c, d, x[i+ 0], 6 , -198630844)
    d = ii(d, a, b, c, x[i+ 7], 10,  1126891415)
    c = ii(c, d, a, b, x[i+14], 15, -1416354905)
    b = ii(b, c, d, a, x[i+ 5], 21, -57434055)
    a = ii(a, b, c, d, x[i+12], 6 ,  1700485571)
    d = ii(d, a, b, c, x[i+ 3], 10, -1894986606)
    c = ii(c, d, a, b, x[i+10], 15, -1051523)
    b = ii(b, c, d, a, x[i+ 1], 21, -2054922799)
    a = ii(a, b, c, d, x[i+ 8], 6 ,  1873313359)
    d = ii(d, a, b, c, x[i+15], 10, -30611744)
    c = ii(c, d, a, b, x[i+ 6], 15, -1560198380)
    b = ii(b, c, d, a, x[i+13], 21,  1309151649)
    a = ii(a, b, c, d, x[i+ 4], 6 , -145523070)
    d = ii(d, a, b, c, x[i+11], 10, -1120210379)
    c = ii(c, d, a, b, x[i+ 2], 15,  718787259)
    b = ii(b, c, d, a, x[i+ 9], 21, -343485551)

    a = safe_add(a, olda)
    b = safe_add(b, oldb)
    c = safe_add(c, oldc)
    d = safe_add(d, oldd)
  }
  return Array(a, b, c, d)
  
  /*
   * These functions implement the four basic operations the algorithm uses.
   */
  function cmn(q, a, b, x, s, t)
  {
    return safe_add(rol(safe_add(safe_add(a, q), safe_add(x, t)), s), b)
  }
  function ff(a, b, c, d, x, s, t)
  {
    return cmn((b & c) | ((~b) & d), a, b, x, s, t)
  }
  function gg(a, b, c, d, x, s, t)
  {
    return cmn((b & d) | (c & (~d)), a, b, x, s, t)
  }
  function hh(a, b, c, d, x, s, t)
  {
    return cmn(b ^ c ^ d, a, b, x, s, t)
  }
  function ii(a, b, c, d, x, s, t)
  {
    return cmn(c ^ (b | (~d)), a, b, x, s, t)
  }
}

/*
 * Calculate the HMAC-MD5, of a key and some data
 */
function str_hmac_md5(key, data)
{
  var bkey = str2binl(key) 
  if(bkey.length > 16) bkey = core_md5(bkey, key.length * chrsz)

  var ipad = Array(16), opad = Array(16)
  for(var i = 0; i < 16; i++) 
  {
    ipad[i] = bkey[i] ^ 0x36363636
    opad[i] = bkey[i] ^ 0x5C5C5C5C
  }

  var hash = core_md5(ipad.concat(str2binl(data)), 512 + data.length * chrsz)
  return core_md5(opad.concat(hash), 512 + 128)
}

/*
 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
 * to work around bugs in some JS interpreters.
 */
function safe_add(x, y)
{
  var lsw = (x & 0xFFFF) + (y & 0xFFFF)
  var msw = (x >> 16) + (y >> 16) + (lsw >> 16)
  return (msw << 16) | (lsw & 0xFFFF)
}

/*
 * Bitwise rotate a 32-bit number to the left.
 */
function rol(num, cnt)
{
  return (num << cnt) | (num >>> (32 - cnt))
}

/*
 * Convert a string to an array of little-endian words
 * If chrsz is ASCII, characters >255 have their hi-byte silently ignored.
 */
function str2binl(str)
{
  var bin = Array()
  var mask = (1 << chrsz) - 1
  for(var i = 0; i < str.length * chrsz; i += chrsz)
    bin[i>>5] |= (str.charCodeAt(i / chrsz) & mask) << (i%32)
  return bin
}

/*
 * Convert an array of little-endian words to a hex string.
 */
function binl2hex(binarray)
{
  var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef"
  var str = ""
  for(var i = 0; i < binarray.length * 4; i++)
  {
    str += hex_tab.charAt((binarray[i>>2] >> ((i%4)*8+4)) & 0xF) +
           hex_tab.charAt((binarray[i>>2] >> ((i%4)*8  )) & 0xF)
  }
  return str
}

/*
 * Convert an array of little-endian words to a base-64 string
 */
function binl2b64(binarray)
{
  var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
  var str = ""
  for(var i = 0; i < binarray.length * 4; i += 3)
  {
    var triplet = (((binarray[i   >> 2] >> 8 * ( i   %4)) & 0xFF) << 16)
                | (((binarray[i+1 >> 2] >> 8 * ((i+1)%4)) & 0xFF) << 8 )
                |  ((binarray[i+2 >> 2] >> 8 * ((i+2)%4)) & 0xFF)
    for(var j = 0; j < 4; j++)
    {
      if(i * 8 + j * 6 > binarray.length * 32) str += b64pad
      else str += tab.charAt((triplet >> 6*(3-j)) & 0x3F)
    }
  }
  return str;
}

 


