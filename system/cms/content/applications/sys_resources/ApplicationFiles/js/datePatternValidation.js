// Pre-defined date constants that are regular expressions equivilent to the
// date parts as defined in Java's SimpleDateFormat class
var m_constants = new Array();
m_constants["MM"] = "(01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12)";
m_constants["dd"] = "(01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31)";
m_constants["hh"] = "(00|01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12)";
m_constants["HH"] = "(00|01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23)";
m_constants["mm"] = "(00|01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59)";
m_constants["ss"] = "(00|01|02|03|04|05|06|07|08|09|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|28|29|30|31|32|33|34|35|36|37|38|39|40|41|42|43|44|45|46|47|48|49|50|51|52|53|54|55|56|57|58|59)";
m_constants["yyyy"] = "\\d{4}";
m_constants["SSS"] = "\\d{1,3}";
m_constants["MMM"] = "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)";
m_constants["MMMM"] = "(january|february|march|april|may|june|july|august|september|october|november|december)";
m_constants["aaa"] = "(am|pm)";
m_constants["G"] = "(ad|bc)";

var m_patternStr = new Array();
var m_patternStrIdx = 1;

//add trim function to string object
String.prototype.trim = function() {
a = this.replace(/^\s+/, '');
return a.replace(/\s+$/, '');
};

/*
 Function to build a regular expressions, takes one or more arguments
 that get translated into a regular expresion. Each argument is a part of the
 date pattern that will be turned into a regular expression.
 
 Example:
 
 To create a regular expression with the following pattern:
 
    MM-dd-yyyy
    
 Use the following:
 
    getRegEx(true, "MM","-","dd","-","yyyy");
    
    The first arg is a flag that that determines if this pattern
    will put in the pattern string array which is used to show the
    list of allowed pattern in the error message.
 */
function getRegEx()
{
   var str = "";
   var pStr = "";
   var recordPattern = arguments[0];
   for(i = 1; i < arguments.length; i++)
   {
      var arg = arguments[i];
      if(arg == "\\.")
         pStr += ".";
      else
         pStr += arg;
      if(m_constants[arg] != null && m_constants[arg] != undefined && m_constants[arg] != "")
      {
         str += m_constants[arg];   
      }
      else
      {
         str += arg;
      }
   }
   
   if(recordPattern)
      m_patternStr[m_patternStrIdx++] = pStr;
   return new RegExp("^" + str + "$", "i");

}

/*
Tests the string against each pattern that exists in the patterns array
and returns true if a match is found
*/
function hasDatePatternMatch(str, patterns)
{
   str = str.trim();
   for(i = 0; i < m_patterns.length; i++)
   {
      if(m_patterns[i].test(str))
         return true;
   }
   
   return false;
}

/*
 Validates the contents of the passed in control to see if it is
 a valid date pattern. The function is expecting the control to be either
 a text type or textarea. If invalid the control will change its background color
 and text color to indicate the field error. A message will be displayed indicating
 the error
 */
function validateDatePattern(obj)
{
   
   var val = obj.value;
   if(val != null && val != "" && !hasDatePatternMatch(val, m_patterns))
   {
     canSubmit = false;
     obj.style.backgroundColor = "#fff8dc";
     obj.style.color = "red";
     obj.style.fontWeight = "bold";
     displayErrorMsg();
     obj.focus();
     
   }
   else
   {
     canSubmit = true;
     obj.style.backgroundColor = "#ffffff";
     obj.style.color = "#000000";
     obj.style.fontWeight = "normal";
   }
}

/*
 Displays an error message and lists all valid date patterns
 */
function displayErrorMsg()
{
   var msg = LocalizedMessage("invalidDatePattern");
   for(i = 1; i < m_patternStr.length; i++)
   {
      msg += m_patternStr[i] + "\n";         
   }
   alert(msg);

}

// Fixes Time part of date time field to use the format HH:mm:ss.S
function fixTimePart(field)
{
   var val = field.value;
   
   if(val.length > 0 && hasDatePatternMatch(val, m_timePatterns))
   {
      var temp = val.split(" ");
      if(temp.length > 1)
      {
         var datepart = temp[0];
         var timepart = temp[1];
         var fixedtimepart = "";
         temp = timepart.split(/[:.]/);
         if(temp.length == 0 || temp[0].trim().length == 0)
         {
            field.value = datepart.trim();
            return;
         }
         for(i = 0; i < 4; i++)
         {
            if(i == 3)
            {
               if(temp.length > i)
               {
                  fixedtimepart += temp[i];
               }
               else
               {
                  fixedtimepart += "0";
               }
            }
            else
            {
               if(temp.length > i)
               {

                  fixedtimepart += addLeadingZero(temp[i]);
               }
               else
               {
                  fixedtimepart += "00";
               }
            }
            if(i < 2)
               fixedtimepart += ":";
            if(i == 2)
               fixedtimepart += ".";

         }

         field.value = (datepart + " " + fixedtimepart).trim();

      }
   }
}

function addLeadingZero(val)
{
   if(val.length < 2)
   {
     val = "0" + val;
   }
   return val;
}



// Build pattern array
//
// *Note: Many of the patterns have been commented out as they are not commonly used.
//        However, we will leave them here so they can be easily added be uncommenting
//        them.
var m_patterns = new Array();
var m_idx = 0;
//m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MMMM","-","dd"," at ","hh",":", "mm",":","ss"," ","aaa");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MMMM","-","dd"," ","HH",":", "mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MMMM","\\.","dd"," at ","hh",":", "mm",":","ss"," ","aaa");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MMMM","\\.","dd"," ","HH",":", "mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MMMM","\\.","dd"," at ","hh",":", "mm"," ","aaa");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MM","-","dd"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MM","-","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MM","-","dd"," ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MM","-","dd"," ","HH",":", "mm");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MM","\\.","dd"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MM","\\.","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MM","\\.","dd"," ","HH",":", "mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","/","MM","/","dd"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "yyyy","/","MM","/","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "yyyy","/","MM","/","dd"," ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "yyyy","/","MM","/","dd"," ","HH",":", "mm");
m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MM","-","dd");
m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MM","\\.","dd");
m_patterns[m_idx++] = getRegEx(true, "yyyy","/","MM","/","dd");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","-","MMMM","-","dd");
//m_patterns[m_idx++] = getRegEx(true, "yyyy","\\.","MMMM","\\.","dd");
m_patterns[m_idx++] = getRegEx(true, "yyyy");
//m_patterns[m_idx++] = getRegEx(true, "MM","-","dd","-","yyyy"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "MM","-","dd","-","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "MM","-","dd","-","yyyy"," ","HH",":", "mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "MM","\\.","dd","\\.","yyyy"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "MM","\\.","dd","\\.","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "MM","\\.","dd","\\.","yyyy"," ","HH",":", "mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "MM","/","dd","/","yyyy"," ","G"," at ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "MM","/","dd","/","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_patterns[m_idx++] = getRegEx(true, "MM","/","dd","/","yyyy"," ","HH",":", "mm",":","ss");
m_patterns[m_idx++] = getRegEx(true, "MM","/","dd","/","yyyy"," ","HH",":", "mm");
m_patterns[m_idx++] = getRegEx(true, "MM","-","dd","-","yyyy");
m_patterns[m_idx++] = getRegEx(true, "MM","\\.","dd","\\.","yyyy");
m_patterns[m_idx++] = getRegEx(true, "MM","/","dd","/","yyyy");
m_patterns[m_idx++] = getRegEx(true, "yyyy","MM","dd"," ","HH",":","mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "MMM"," ","dd",", ","yyyy");
//m_patterns[m_idx++] = getRegEx(true, "MMM"," ","yyyy");
//m_patterns[m_idx++] = getRegEx(true, "HH",":","mm",":","ss");
//m_patterns[m_idx++] = getRegEx(true, "HH",":","mm");


// Patterns that all contain a time element
var m_timePatterns = new Array();
var m_tIdx = 0;
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","-","MM","-","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","-","MM","-","dd"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","-","MM","-","dd"," ","HH",":", "mm");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","\\.","MM","\\.","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","\\.","MM","\\.","dd"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","/","MM","/","dd"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","/","MM","/","dd"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","/","MM","/","dd"," ","HH",":", "mm");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","-","dd","-","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","-","dd","-","yyyy"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","\\.","dd","\\.","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","\\.","dd","\\.","yyyy"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","/","dd","/","yyyy"," ","HH",":", "mm",":","ss","\\.","SSS");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","/","dd","/","yyyy"," ","HH",":", "mm",":","ss");
m_timePatterns[m_tIdx++] = getRegEx(false, "MM","/","dd","/","yyyy"," ","HH",":", "mm");
m_timePatterns[m_tIdx++] = getRegEx(false, "yyyy","MM","dd"," ","HH",":","mm",":","ss");