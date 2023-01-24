/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
   private List<Item> items = new ArrayList<>();
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
	if(errors == null) errors = new ArrayList<>();
	errors.add(new Error(error,message));
	log.debug("Error is: {}", message);
}


public void addError(Error.ErrorCode error,Exception e) {
	if(errors == null) errors = new ArrayList<>();
	String message = e.getMessage() ;
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	errors.add(new Error(error,message + ":"+sw));
}

public void addError(Error.ErrorCode error,String message, Exception e) {
	if(errors == null) errors = new ArrayList<>();
	String messageex = e.getMessage() +"\n";
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	errors.add(new Error(error,messageex + "\n"+ message +"\n"+sw));
	log.debug("Error is: {}", message);
	
}


public void addError(Error.ErrorCode error) {
	if(errors == null) errors = new ArrayList<>();
	errors.add(new Error(error));
	log.debug("Error is: {}", error);
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
