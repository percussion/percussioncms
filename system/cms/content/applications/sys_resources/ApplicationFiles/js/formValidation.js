/*
This is a general purpose javascript file consists of several functions for client side validations.
This file has been included in most of the application files.
You can place a function which is common to all applications here.
*/
/*
reField function takes two params one is the value of a form field and the second one is field display name.
Displays alert message and returns false if the field value is empty else returns true.
*/
function reqField(fieldValue,fieldName){
	if(fieldValue==""){
		alert(fieldName + ": field is a required field");
		return false;
	} 
	return true;
}
/*
numberField function takes two params one is the value of a form field and the second one is field display name.
Displays alert message and returns false if the field value is not a number else returns true.
*/
function numberField(fieldValue,fieldName){
	if(fieldValue == ""){
		return true;
	}
	if(parseInt(fieldValue,10) != fieldValue){
		alert("Only integer number is allowed in the field: " + fieldName);
		return false;
	}
	return true;
}
/*
portField function takes the value of a form field.
Checks for the value of port if not between 1-65535 return false if entered.
*/
function portField(fieldValue){
	if(fieldValue == ""){
		return true;
	}
	else if(parseInt(fieldValue,10) != fieldValue){
		alert("Only integer number between 1 and 65535 is allowed in the field: Port");
		return false;
	}
	else if(parseInt(fieldValue,10)<1 || parseInt(fieldValue,10)>65535){
		alert("Only integer number between 1 and 65535 is allowed in the field: Port");
		return false;
	}
	return true;
}
/*
numberField function takes two params one is the value of a form field and the second one is field display name.
Displays alert message and returns false if the field value is empty or not a number else returns true.
*/
function reqNumberField(fieldValue,fieldName){
	if(fieldValue==""){
		alert(fieldName + ": field is a required field");
		return false;
	} 
	else if(parseInt(fieldValue,10) != fieldValue){
		alert("Only integer numbers are allowed in the field: " + fieldName);
		return false;
	}
	return true;
}
/*
This function trims all leading and trailing white spaces. Normally required for URL fields.
*/
function trim(strText) { 
    // this will get rid of leading spaces 
    while (strText.substring(0,1) == ' ') 
        strText = strText.substring(1, strText.length);

    // this will get rid of trailing spaces 
    while (strText.substring(strText.length-1,strText.length) == ' ')
        strText = strText.substring(0, strText.length-1);

   return strText;
} 
/*
	Function for parsing the URl and filling the Search box parameters.
*/
function parseAndDisplayFormFiledsFromUrl(formName)
{
  parampairs = document.location.href.split("?")[1].split("&");
  theform = document.forms[formName];
  for(var i=0; i<theform.length; i++)
  {
	 if(theform[i].type == "text" || theform[i].type == "select-one")
	 {
		for(j=0;j<parampairs.length;j++){
			parampair = parampairs[j].split("=");
			if(theform[i].name==parampair[0]){
				if(theform[i].type == "select-one"){
					for(k=0;k<theform[i].options.length;k++){
						if(theform[i].options[k].value==replace(parampair[1],"+"," ")){
							theform[i].options.selectedIndex=k;
							break;
						}
					}
				}
				else{
					parval = unescape(replace(parampair[1],"+"," "));
					if(parval.charAt(0)=="%"){
						parval = parval.substring(1,parval.length);
					}
					if(parval.charAt(parval.length-1)=="%"){
						parval = parval.substring(0,parval.length-1);
					}
					theform[i].value = parval;
				}
				break;
			}
		}
	 }
  }
}
/*
numberField function takes three params one is the value of a form field and the second one is field display name
and third one is fileext.
Displays alert message and returns false if the field value does not consist of given extension else returns true.
*/
function reqFileExtinURL(fieldValue,fieldName,fileExt){
	if(fieldValue.toString().toUpperCase().indexOf("." + fileExt.toString().toUpperCase()) == -1){
		alert("Please enter an "+ fileExt +" file name in field:" + fieldName);
		return false;
	}
	return true;
}
/* The following two functions are general purpose formvalidation functions and 
can be used only if the field names are suffixed properly.
not yet implemented.
*/
/*
function validateOnSubmit(fname){
	fnameobj = document.forms[fname];
	nItems = fnameobj.length;
	for(i=0;i<nItems;i++){
	alert(fnameobj[i].type);
		if(fnameobj[i].type == "text"){
			filters = fnameobj[i].name.split("_");
			fieldValue = fnameobj[i].value;
			filtLen = filters.length-1;
			for(j=0;j<filtLen;j++){
				if(!valSwitch(filters[j],fieldValue,filters[filtLen])) return;
			}
		}
		else if(fnameobj[i].type == "select-one"){
			if(fnameobj[i][fnameobj[i].selectedIndex].value=="--Choose--") {			
				fnameobj[i][fnameobj[i].selectedIndex].value="";
			}
		}
	}
}
function valSwitch(strVal,fieldValue,fieldName){
	if(strVal == "RQ"){
		if(fieldValue==""){
			alert(fieldName + " field is a required field");
			return false;
		} 
	}
	else if(strVal == "DG" && fieldValue != ""){
		if(parseInt(fieldValue,10) != fieldValue){
			alert("Only integer numbers are allowed in the field: " + fieldName);
			return false;
		}
	}
	else if(strVal == "NS" && fieldValue != ""){
		if(parseInt(fieldValue,10) != fieldValue){
			alert("Only integer numbers are allowed in the field: " + fieldName);
			return false;
		}
	}
	return true;
}
*/

/*
	Displays a confirm message before deleting.
*/
function delConfirm(link) 
{
 if(confirm( "Delete actions cannot be undone. Are you sure you want to continue?")) 
 {
  location=link;
 }
}
/*
	Displays a confirm message before deleting.
*/
function delComfirmonSubmit() 
{
   if(confirm( "Delete actions cannot be undone. Are you sure you want to continue?")) 
   {
      return true;
   }
	return false;

 }
/*
	* The following function validates the date entered in the search box.
	* At present the backend server supports
	* dates enetered in the form of YYYY-MM-DD only
	* the minimum date as 1900-01-01 and Maximum date as 2079-06-06.
	* Above criteria has been followed to validate the dates entered in the search box date fields. 
*/
function dateValidate(dateValue){
	var ermsg = LocalizedMessage("date_error"); 
	var mindate = new Date(1900,0,1);
	var maxdate = new Date(2079,6,6);
	if(!dateValue==""){
		datearr = dateValue.split("-");
		if(datearr.length!=3 || datearr[0]=="" || datearr[1]=="" || datearr[2]=="" || parseInt(datearr[0],10)!=datearr[0] || parseInt(datearr[1],10)!=datearr[1] || parseInt(datearr[2],10)!=datearr[2]){
			alert(ermsg);
			return false;
		}
		var entdate = new Date(parseInt(datearr[0],10),parseInt(datearr[1],10)-1,parseInt(datearr[2],10));
		if(parseInt(datearr[0],10)>2079 || parseInt(datearr[0],10)<1900 || entdate<mindate || entdate>maxdate){
			alert(ermsg);
			return false;
		}
	}
	return true;
}
/*
	The following two functions are used to open a window for searching of users and their roles.
*/
function showUserSearch(datafield, fromRoles, contentid){
	if(fromRoles == null)
		fromRoles = "";
	userDataField = datafield;
	window.open("../sys_ServerUserRoleSearch/rolelist.html?sys_command=GetRoles&fromRoles=" + fromRoles + "&sys_contentid=" + contentid,"rolesearch","toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=400,height=300,z-lock=1");
}
function showUserSearch2(datafield, windowName){

        // This function is referenced by "wfTransition.xsl"
        // It opens an empty window, which is the target for the "UserSearchForm" to post to
	userDataField = datafield;

        window.open("", windowName,"toolbar=0,location=0,directories=0,status=0,menubar=0,scrollbars=1,resizable=1,width=400,height=300,z-lock=1");
        document.UserSearchForm.submit();

}

// This function merges the new values with the existing values. Note that
// empty values are skipped by the logic
function setUserDataField(val) 
{
	newvalues = val.split(";");
	originalArray = userDataField.value.split(";");
	// First copy the original values
	var newval = "";
	var i;
	for(i = 0; i < originalArray.length; i++)
	{
		var val = originalArray[i];
		if (val != "")
		{
			newval = appendWithDel(newval,";",val);
		}
	}
	// Now copy new values that don't exist in the existing values
	for(i = 0; i < newvalues.length; i++)
	{
		var val = newvalues[i];
		if (val.length > 0 && !contains(originalArray, val))
		{
			newval = appendWithDel(newval,";",val);
		}
	}
	// Reset to new value
	userDataField.value = newval;
}
function replace(string,text,by) {
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
// Returns true if the given value is in the passed vector
// vector a set of values
// value a value to search the vector for
function contains(vector, value)
{
   var len = vector.length;
   var i = 0;
   while(i < len)
   {
      if (vector[i] == value) return true;
      i++;
   }
   return false;
}

// Append the new element using the delimiter. Omit the delimeter
// if the string is empty
function appendWithDel(string, del, newelement)
{
	if (string.length == 0)
	{
		return newelement;
	}
	else
	{
		return string + del + newelement;
	}
}

// Check a content list url for validity. One that references the contentlist
// servlet is valid, otherwise the old rules apply
function contentListUrl(url,fieldName,fileExt)
{
	if (url.indexOf("/contentlist") > 0)
	{
		return true;
	}
	return reqFileExtinURL(url,fieldName,fileExt);
}