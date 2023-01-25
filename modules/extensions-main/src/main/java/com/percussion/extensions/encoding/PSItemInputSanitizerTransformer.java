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

package com.percussion.extensions.encoding;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSItemInputTransformer;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionParams;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * Sanitizes the specified fields on the content type. 
 * <table>
 * <tr>
 * <th>Parameter</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>
 * Field List
 * </td>
 * <td>
 * A comma separated list of field names or HTML parameters. Example:  name,description,label
 * </td>
 * </tr>
 * <td>
 * White List (optional)
 * </td> 
 * <td>
 * Specifies the whitelist to use when sanitizing input.
 * <ul>
 * <li>none - (default) All tags are removed only text nodes are preserved</li>
 * <li>simpleText -  Allow b, em, i, strong, u.</li>
 * <li>basic -  a, b, blockquote, br, cite, code, dd, dl, dt, em, i, li, ol, p, pre, q, small, span, strike, strong, sub, sup, u, ul, and appropriate attributes.  Links (a elements) can point to http, https, ftp, mailto, and have an enforced rel=nofollow attribute.</li>
 * <li>basicWithImages - basic+ img tags, with appropriate attributes, with src pointing to http or https.</li>
 * <li>relaxed - a, b, blockquote, br, caption, cite, code, col, colgroup, dd, div, dl, dt, em, h1, h2, h3, h4, h5, h6, i, img, li, ol, p, pre, q, small, span, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul
Links do not have an enforced rel=nofollow attribute</li>
 * 
 * </ul>
 * </td>
 * </tr>
 * </table>
 * 
 * @author natechadwick
 *
 */
public class PSItemInputSanitizerTransformer implements IPSItemInputTransformer{

	/***
	 * Default public constructor
	 */
	public PSItemInputSanitizerTransformer(){}
	
	@Override
	public void preProcessRequest(Object[] params, IPSRequestContext request) throws PSAuthorizationException,
			PSRequestValidationException, PSParameterMismatchException, PSExtensionProcessingException {
		 
		// expects one comma separated string parameter   
		PSExtensionParams ep; 
		String htmlParamCSV;
		Safelist safelist;
		String wp;
		
		try{
			ep = new PSExtensionParams(params); 
			
			htmlParamCSV = ep.getStringParam(0, null, true);
			wp = ep.getStringParam(1, "none", false);
		
			if(wp.equalsIgnoreCase("simpletext"))
			   safelist = Safelist.simpleText();
			else if (wp.equalsIgnoreCase("basic"))
			   safelist = Safelist.basic();
			else if(wp.equalsIgnoreCase("basicwithimages"))
			   safelist = Safelist.basicWithImages();
			else if(wp.equalsIgnoreCase("relaxed"))
			   safelist = Safelist.relaxed();
			else
			   safelist = Safelist.none();
		
			
		}catch(PSConversionException e){
			throw new PSParameterMismatchException("Expected a string for parameter 0, got an unknown data type instead.");
		}
		
		if(htmlParamCSV==null)
			throw new PSParameterMismatchException("Missing value for parameter 0, expected a comma seperated list, got an empty list");
		
		 	
	     
		String[] htmlParams = htmlParamCSV.split(",");
		for(String s: htmlParams){
			s = s.trim();
			if(!StringUtils.isEmpty(s)){
				
				String v = request.getParameter(s);
				if(v!=null){
					request.setParameter(s, Jsoup.clean(v, safelist));
				}
			}
		}

	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
		// TODO Auto-generated method stub

	}


}
