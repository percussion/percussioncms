/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.transform;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSFieldInputTransformer;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionException;
import com.percussion.pso.utils.PSOExtensionParamsHelper;
import com.percussion.server.IPSRequestContext;

/***
 * A field input transform for setting the value of a field
 * to the java @see String#hashCode() hashCode of the specified
 * source values.
 * 
 * An example use case for this may be where you want to index something
 * like a remote URL by it's hashCode.
 * 
 * @author natechadwick
 *
 */
public class HashCodeFieldTransform extends PSDefaultExtension
implements
IPSFieldInputTransformer
{
private static Log log = LogFactory.getLog(HashCodeFieldTransform.class);

private IPSExtensionDef extDef = null; 

	public Object processUdf(Object[] params, IPSRequestContext request)
			throws PSConversionException {
		
	
		PSOExtensionParamsHelper helper = new PSOExtensionParamsHelper(extDef,
		            params, request, log);
		
		 return helper.getParameter("source").hashCode();
	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot)
			throws PSExtensionException {
		  super.init(def, codeRoot);
	      extDef = def; 

	}

}
