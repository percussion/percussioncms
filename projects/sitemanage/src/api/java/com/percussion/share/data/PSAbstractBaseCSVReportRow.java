/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
