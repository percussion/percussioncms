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

package com.percussion.share.data;

/**
 * Provides an abstract base class that encapsulates common CSV
 * formatting and encoding routines for CSV style data reports.
 * 
 * @author natechadwick
 *
 */
public abstract class PSAbstractBaseCSVReportRow {
	
	/**
	 * Get a header row that can be used for the first row of the CSV file. 
	 * @return A CSV formatted header row including the ending CRLF
	 */
	public abstract String getHeaderRow();
	
	/***
	 * Helper method to remove characters that would break the CSV format.
	 * @param value The string to escape
	 * @return The escaped string
	 */
	public String csvEscapeString(String value){
		if(value==null)
			value="";
		return value.replaceAll("\"", "'");
	}
	
	/***
	 * Helper method to build CSV friendly multi-line fields.
	 * @param current  The current column value. Must not be null;
	 * @param newline  The string to be added as a newline.  Must not be null.
	 * @return A new string with the newline parameter added.
	 */
	protected String addToMultiLineField(String current, String newline){
		if(current == null)
			current = "";
		
		if(newline == null)
			newline = "";
		
		return  current +"\r\n" + newline;	
	}
	
	/**
	 * Delimits the specified value with "
	 * @param value The value to be wrapped.
	 * @return The wrapped string.
	 */
	public String delimitValue(String value){
		if(value==null)
			value="";
		value = csvEscapeString(value);
		return "\"" + value + "\"";
	}
	
	protected String endRow(){
		return "\r\n";
	}
	
	public abstract String toCSVRow();
	
}
