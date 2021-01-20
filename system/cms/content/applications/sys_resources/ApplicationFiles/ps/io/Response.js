/******************************************************************************
 *
 * [ ps.io.Response.js ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
dojo.provide("ps.io.Response");

/**
 * A small class to hold server action response information
 */
ps.io.Response = function()
{
	/**

	 * Get the response data string value.
	 * @return may be <code>null</code> or empty.
	 */
	this.getValue = function()
	{
	   return this._m_value;
	}
	
	/**
	 * Indicates that this response was successful.
	 * @return <code>true</code> if successful or <code>false</code> 
	 * if a failure.
	 */
	this.isSuccess = function()
	{
	   return this._m_success;
	}
	
	/**
	 * @return the error code if an error occured.
	 */
	this.getErrorCode = function()
	{
	   return this._m.errorcode;	   
	}
	
	// Private members
	
	/**
	 * Flag indicating a successful response.
	 */
	this._m_success= false;
	
	/**
	 * The response data value or error message if a failure.
	 */
	this._m_value = null;
	
	
	/**
	 * The error code.
	 */
	this._m_errorcode = null;
	
};
