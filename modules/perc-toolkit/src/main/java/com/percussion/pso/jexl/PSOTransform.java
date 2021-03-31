/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * com.percussion.pso.transform PSOTransform.java
 *  
 * @author DavidBenua
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 */
package com.percussion.pso.jexl;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.data.PSCachedStylesheet;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.xmldom.PSStylesheetCacheManager;

/**
 * Transforms an XML string with XSLT. 
 * 
 * @author DavidBenua
 *
 */
public class PSOTransform extends PSJexlUtilBase implements IPSJexlExpression
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOTransform.class);
   
   /**
    * 
    */
   public PSOTransform()
   {
      super();
   }

   /**
    * Transforms an XML string with XSLT. Convenience method without parameters 
    * @param source the source string.  Must be well formed. 
    * @param stylesheetName the name of the stylesheet, relative to the Percussion CMS root. 
    * @return the result of the transform. 
    */
   @IPSJexlMethod(description="Transform a value with an XSLT transform", 
         params={
        @IPSJexlParam(name="source", type="String", description="the source to transform"),
        @IPSJexlParam(name="stylesheetName", type="String", description="the URI of the stylesheet to apply")})
   public String transform(String source, String stylesheetName)
   {
      Map<String,Object> params = new HashMap<String,Object>();
      return transform(source, stylesheetName, params);
   }
   
   /**
    * Transforms an XML string with XSLT.  The source will be wrapped in a 
    * <code>&lt;div class="rxbodyfield"&gt;</code>. The source may contain 
    * multiple elements, but otherwise must be well formed.
    * <p>
    * The parameters may be referenced with <code>&lt;xsl:param...</code> 
    * as a direct child of the <code>&lt;xsl:stylesheet></code> node.   
    * @param source the source string.  
    * @param stylesheetName the name of the stylesheet, relative to the Percussion CMS root. This should be
    * a file url. For example: <code>file:rx_resource/stylesheets/myStylesheet.xsl</code>
    * @param params a map of parameter names and values. 
    * @return the transformed source
    */
   @IPSJexlMethod(description="Transform a value with an XSLT transform", 
         params={
        @IPSJexlParam(name="source", type="String", description="the source to transform"),
        @IPSJexlParam(name="stylesheetName", type="String", description="the file: URL of the stylesheet to apply"),
        @IPSJexlParam(name="params", description="XSLT parameters for stylesheet")})
   public String transform(String source, String stylesheetName, Map<String,Object> params)
   {
      URL styleFile;
      PSCachedStylesheet styleCached = null; 
      
      try
      {
         styleFile = new URL(stylesheetName);
         styleCached = PSStylesheetCacheManager.getStyleSheetFromCache(styleFile);
         
         Transformer nt = styleCached.getStylesheetTemplate().newTransformer();
         
         for(String pkey : params.keySet())
         { 
            Object pval = params.get(pkey);
            log.debug("Adding parameter " + pkey + " value " + pval);
            nt.setParameter(pkey, pval); 
         }
         Source src = new StreamSource(new StringReader(wrapField(source)));
         StringWriter outString = new StringWriter();
         StreamResult res = new StreamResult(outString);

         nt.transform(src, res);
         
         return outString.toString();

      } catch (Throwable ex)
      {         
         log.error("XSLT Error: " + ex.getMessage(), ex);
      } 
      return "";
   }
   
   /**
    * Wraps a field in <code>&lt;div class="rxbodyfield"&gt;</code>
    * @param field the field to wrap
    * @return the wrapped string. Never null or empty. 
    */
   private static String wrapField(String field)
   {
      StringBuilder sb = new StringBuilder(); 
      sb.append("<div class=\"rxbodyfield\">");
      sb.append(field);
      sb.append("</div>");
      return sb.toString(); 
   }
}
