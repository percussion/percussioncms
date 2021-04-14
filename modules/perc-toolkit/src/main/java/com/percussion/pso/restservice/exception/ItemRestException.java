/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.restservice.exception;

import com.percussion.pso.restservice.model.Error.ErrorCode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ItemRestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Log log = LogFactory.getLog(ItemRestException.class);
	private ErrorCode errorCode=ErrorCode.UNKNOWN_ERROR;

	public ItemRestException() {
		
	}
	public ItemRestException(ErrorCode errorCode,String msg) { 
		super(msg);
		this.errorCode=errorCode;
		log.debug("Rest exception "+msg);
	}
	public ItemRestException(String msg) { 
		super(msg);
		log.debug("Rest exception "+msg);
	}
	public ItemRestException(String msg,Exception e) { 
		super(msg,e);
		log.debug("Rest exception "+msg,e);
	}
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	public ErrorCode getErrorCode() {
		return errorCode;
	}
}
