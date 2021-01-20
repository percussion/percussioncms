
var ___serverProperties = {allowTrueInlineTemplate: "true"};

var ___blockTags = new Array(
    "address", "blockquote", "dd", "dl", "dt", "div", "h1", "h2", "h2", "h3",
    "h4", "h5", "h6", "hr", "ins", "li", "noscript'", "ol", "p", "pre",
    "ul"); 

function encodeAmpersand(urlstring)
{
   var re = new RegExp("&amp;","g");
   urlstring = urlstring.replace(re,"&");
   var re1 = new RegExp("&","g");
   urlstring = urlstring.replace(re1,"&amp;");
   return urlstring;
}

function inlineCallback(objectId, callback, type)
{
   var oId = new ps.aa.ObjectId(objectId.toString());

   var buff = "";
   var attribs = "";
var allowTrueInlineTemplates = (___serverProperties.allowTrueInlineTemplates != undefined
               ? ___serverProperties.allowTrueInlineTemplates : "true").toLowerCase() == "true";
   attribs += (" sys_dependentvariantid=\"" + oId.getTemplateId() + "\"");
   attribs += (" sys_dependentid=\"" + oId.getContentId() + "\"");
   attribs += (" inlinetype=\"" + type + "\"");
   attribs += (" rxinlineslot=\"" + oId.getSlotId() + "\"");
   var sId = oId.getSiteId() != null ? oId.getSiteId() : "";
   var fId = oId.getFolderId() != null ? oId.getFolderId() : "";
   attribs += (" sys_siteid=\"" + sId + "\"");
   attribs += (" sys_folderid=\"" + fId + "\"");
    if(type == "rxhyperlink" || type == "rximage")
    {
       var response = ps.io.Actions.getUrl(oId, "CE_LINK");
       if(response.isSuccess())
      {
         var urlstring = encodeAmpersand(response.getValue().url);
         if(type == "rxhyperlink")
         {
            buff += "<a href=\"" + urlstring + "\"" + attribs + ">";
            buff += ___selectedContent;
            buff += "</a>";
         }
         else
         {
            buff += "<img src=\"" + urlstring + "\"" + attribs + ">";
         }

       }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

    }
    else if(type == "rxvariant" && allowTrueInlineTemplates)
    {
        var response =
           ps.io.Actions.getSnippetContent(oId, false, ___selectedContent);
       if(response.isSuccess())
      {
        var theContent = unicode2entity(response.getValue());
        var tagstart = theContent.indexOf("<") + 1;
        var tagend = minIgnoreNegative(
           theContent.indexOf(">", tagstart),
           theContent.indexOf(" ", tagstart));
        var mattribs = "";
        var tagname = theContent.substring(tagstart, tagend);
        tagname = stringtrim(tagname);
        var prefix = theContent.substring(0,theContent.indexOf(">"));
        var postfix = theContent.substring(theContent.indexOf(">"));
        var isBlock = isBlockTag(tagname);
        if(isBlock)
        {
           attribs += (" class=\"rx_ephox_inlinevariant mceNonEditable\"");
           postfix += "\n<p></p>";
        }
        else
      {
          postfix +="&nbsp;";
      }
        
          attribs += (" contenteditable=\"false\" class=\"rx_inlinevariant mceNonEditable\"");


        attribs += (" rxselectedtext=\"" + encodeURIComponent(___selectedContent)+ "\"");
        buff += prefix + attribs + postfix;
       
       }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

    }
   else if(type == "rxvariant" && !allowTrueInlineTemplates)
   {
        var response =
           ps.io.Actions.getSnippetContent(oId, false, ___selectedContent);
       if(response.isSuccess())
      {
        attribs += (" style=\"display: inline;\"");
        attribs += (" class=\"rx_ephox_inlinevariant mceNonEditable\"");
        attribs += (" contenteditable=\"false\"");
          attribs += (" rxselectedtext=\"" + encodeURIComponent(___selectedContent)+ "\"");
        buff += ("<div" + attribs + ">\n");
        buff += unicode2entity(response.getValue());
        buff += "\n</div>\n<p></p>";
       }
      else
      {
         ps.io.Actions.maybeReportActionError(response);
         return;
      }

   }
   callback(buff);
   ___bws.close();
}

// Converts unicode chars into the hexidecimal reference
// entity
function unicode2entity(str)
{
   var sPos = 0;
   var ePos = -1;
   var result = "";

   while((sPos = str.indexOf("%u", sPos)) != -1)
   {
      if(ePos == -1)
      {
          ePos = 0;
          result += str.substring(ePos, sPos);
      }
      else if(sPos - ePos > 0)
      {
          result += str.substring(ePos + 1, sPos);
      }
      result += "&#x" + str.substr(sPos + 2, 4) + ";";
      sPos += 5;
      ePos = sPos;

   } 
   result += str.substring(ePos + 1);

   return result;
}
function minIgnoreNegative(a, b)
{
   if(a == -1)
      return b;
   if(b == -1)
      return a;
   return Math.min(a,b);
}
function stringtrim(str)
{
   return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}
 function isBlockTag(tagname)
 {
    if(tagname == null || tagname.length == 0)
       alert("tagname cannot be null or empty.");
    var len = ___blockTags.length;
    for(i = 0; i < len; i++)
    {
       if(___blockTags[i].toLowerCase() == tagname.toLowerCase())
          return true;
    }
      return false;
 }

function createInlineSearchBox(type,selectedText,inlineslotid,ctypeid,callback)
{

     var inlinetype = type;
  __rxroot="/Rhythmyx";
   //var type = dataObject.searchType;
    //set serverproperties
  

  ___selectedContent=selectedText

  ___slotId ='["1",null,null,null,null,null,null,"'
          + ctypeid + '",null,"'
          + inlineslotid + '",null,null,null,null,null]';

  

  ___cBackFunction = function(objectId) {
    inlineCallback(objectId,callback, type) ;
  }
  if(type == "rxhyperlink")
  {   
     ___bwsMode = ps.util.BROWSE_MODE_RTE_INLINE_LINK;
  }
 else if(type == "rximage")
  {
     ___bwsMode =  ps.util.BROWSE_MODE_RTE_INLINE_IMAGE;
  }
  else if(type == "rxvariant")
  {
      ___bwsMode =  ps.util.BROWSE_MODE_RTE_INLINE;
  }
  
  ___bws = window.open("/Rhythmyx/ui/content/ContentBrowserDialog.jsp", "contentBrowerDialog",
             "resizable=1;status=0,toolbar=0,scrollbars=0,menubar=0,location=0,directories=0,width=750,height=500"); 
  
  

   setTimeout(function(){___bws.focus();},1000);
}
