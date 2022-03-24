/**
 * Javascript to add event listener to all the elements in the DOM.
 * The purpose is to keep the server alive while user is still working.
 */

var keepAlive=null;

/**
 * Method which adds the event listeners to all the elements in the DOM.
 * There are two listeners getting added - 'keydown' and 'mouseover'.
 * It is intended to handle keyboard events to address the accessibility case(s). 
 */
function attachEventListener() {

	var x = document.getElementsByTagName("*");
	var i;

	for(i=0; i<x.length;i++) {
		
		x[i].addEventListener("keydown", function(){
			updateKeepAlive();
		});
		
		x[i].addEventListener("mouseover", function(){
			updateKeepAlive();
		});
		
		x[i].addEventListener("mousedown", function(){
			updateKeepAlive();
		});
		
		x[i].addEventListener("mouseup", function(){
			updateKeepAlive();
		});
	}
}

/**
 * Method to check the keep alive time status. 
 * It sends a request to the server to extend the session every minute if the user is still working.
 * In case of failures, a message could be checked in the browser console(developer tools) for more information.
 */
function updateKeepAlive() {
	var xhttp = new XMLHttpRequest();
	
	if(shouldKeepAlive()) {

		// Make a request to extend the session.
		xhttp.open("GET", "/Rhythmyx/sessioncheck?extendSession=true", true);
		xhttp.onreadystatechange = function (event) {  
		    if (xhttp.readyState === 4) {  
		        if (xhttp.status === 200) {  
		          console.log("Extend session request is successful ! " + xhttp.responseText)  
		        } else {  
		           console.log("Error", xhttp.statusText);  
		        }  
		    }  
		}; 
		xhttp.send();
	}
}

function shouldKeepAlive() {
	
	if(keepAlive === null || keepAlive === 'undefined') {
		keepAlive = new Date();
		
		return false;
	} else {
		var a = new Date();
		
		var t1 = a.getTime();
		var t2 = keepAlive.getTime();
		var diff = (t1 - t2)/(1000*60);
				
		if(diff >= 1) {
			keepAlive = new Date();
			
			return true;
		}
	}
}
 