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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.extensions.encoding;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

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
		Whitelist whitelist;
		String wp;
		
		try{
			ep = new PSExtensionParams(params); 
			
			htmlParamCSV = ep.getStringParam(0, null, true);
			wp = ep.getStringParam(1, "none", false);
		
			if(wp.equalsIgnoreCase("simpletext"))
			   whitelist = Whitelist.simpleText();
			else if (wp.equalsIgnoreCase("basic"))
			   whitelist = Whitelist.basic();
			else if(wp.equalsIgnoreCase("basicwithimages"))
			   whitelist = Whitelist.basicWithImages();
			else if(wp.equalsIgnoreCase("relaxed"))
			   whitelist = Whitelist.relaxed();
			else
			   whitelist = Whitelist.none();
		
			
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
					request.setParameter(s, Jsoup.clean(v, whitelist));
				}
			}
		}

	}

	@Override
	public void init(IPSExtensionDef def, File codeRoot) throws PSExtensionException {
		// TODO Auto-generated method stub

	}


}
