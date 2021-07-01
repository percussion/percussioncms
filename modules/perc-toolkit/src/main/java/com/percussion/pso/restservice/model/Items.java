/*******************************************************************************


 * 
 * 
 * 
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
@XmlRootElement(name = "Items")
public class Items {
   private List<Item> items = new ArrayList<Item>();
   private List<Error> errors = null;


@XmlElement(name = "Item")
public List<Item> getItems() {
	return items;
}


public void setItems(List<Item> items) {
	this.items = items;
}

private static final Logger log = LogManager.getLogger(Items.class);
@XmlElement(name = "Error")
@XmlElementWrapper(name="Errors")
public List<Error> getErrors() {
	return errors;
}

public void setErrors(List<Error> errors) {
	this.errors = errors;
}

public void addError(Error.ErrorCode error, String message) {
	if(errors == null) errors = new ArrayList<Error>(); 
	errors.add(new Error(error,message));
	log.debug("Error is: " + message);
}


public void addError(Error.ErrorCode error,Exception e) {
	if(errors == null) errors = new ArrayList<Error>();
	String message = e.getMessage() ;
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	errors.add(new Error(error,message + ":"+sw.toString()));
}

public void addError(Error.ErrorCode error,String message, Exception e) {
	if(errors == null) errors = new ArrayList<Error>();
	String messageex = e.getMessage() +"\n";
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	errors.add(new Error(error,messageex + "\n"+ message +"\n"+sw.toString()));
	log.debug("Error is: " + message);
	
}


public void addError(Error.ErrorCode error) {
	if(errors == null) errors = new ArrayList<Error>();
	errors.add(new Error(error));
	log.debug("Error is: " + error.toString());
}

public boolean hasError(Error.ErrorCode error) {
	if (errors != null) {
		for (Error errorTest : errors) {
			if (errorTest.getErrorCode() == error) {
				return true;
			}
		}
	}
	return false;
}

public boolean hasItems() {
	if (items != null && !items.isEmpty()) {
		return true;
	}
	return false;
}


public boolean hasErrors() {
	if (errors != null && !errors.isEmpty()) {
		return true;
	}
	return false;
}

}
