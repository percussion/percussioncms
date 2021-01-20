// CALENDAR POPUP FUNCTIONS - added by Percussion Software

function SetVals(fieldname) {

fieldname.value = document.all.dynamsg.html;
}

function LoadVals(fieldname) {

document.all.dynamsg.html = fieldname.value ;
}