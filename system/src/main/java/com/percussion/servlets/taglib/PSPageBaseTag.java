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
package com.percussion.servlets.taglib;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.PSInternalRequest;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.servlets.utils.PSComponentUrls;
import com.percussion.util.PSHtmlBodyInputStream;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.request.PSRequestInfo;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This base tag implements behavior used by the header and sidenav tags
 * 
 * @author dougrand
 * 
 */
public abstract class PSPageBaseTag implements Tag
{
   protected PSComponentUrls m_urls = null;

   protected PageContext m_context = null;

   protected Tag m_parent = null;

   public void setPageContext(PageContext arg0)
   {
      m_context = arg0;
      m_urls = new PSComponentUrls((HttpServletRequest) m_context.getRequest());
   }

   public void setParent(Tag arg0)
   {
      m_parent = arg0;
   }

   public Tag getParent()
   {
      return m_parent;
   }

   /**
    * Get the contents for the given component name. This method makes an
    * internal request to get the document associated with the component name,
    * and then serializes that document to text.
    * 
    * @param componentname the name of the component, never <code>null</code>
    *           or empty
    * @param extra extra parameters to add to the request, may be
    *           <code>null</code>
    * @return the component as text, never <code>null</code> or empty
    * @throws PSInternalRequestCallException
    */
   @SuppressWarnings("unchecked")
   public String getUrlContent(String componentname, Map<String, String> extra)
         throws PSInternalRequestCallException
   {
      Map<String, String> requestparams = new HashMap<>();
      ServletRequest srvreq = m_context.getRequest();
      for (String name : (Set<String>) srvreq.getParameterMap().keySet())
      {
         String values[] = srvreq.getParameterValues(name);
         if (values != null && values.length > 0)
            requestparams.put(name, values[0]);
      }
      if (extra != null)
      {
         for (String name : extra.keySet())
         {
            requestparams.put(name, extra.get(name));
         }
      }
      String url = m_urls.getComponentUrl(componentname);
      // Remove parameters that are overridden
      Set<String> keys = requestparams.keySet();
      if (keys.size() > 0 && url.contains("?"))
      {
         String parts[] = url.split("\\u003f");
         StringBuilder b = new StringBuilder(parts[0]);
         String params[] = parts[1].split("&");
         boolean first = true;
         for (int i = 0; i < params.length; i++)
         {
            parts = params[i].split("=");
            if (!keys.contains(parts[0]))
            {
               if (first)
               {
                  b.append('?');
                  first = false;
               }
               else
               {
                  b.append('&');
               }
               b.append(params[i]);
            }
         }
         url = b.toString();
      }

      try
      {
         PSRequest req = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         PSInternalRequest ireq = PSServer.getInternalRequest(url, req,
               requestparams, false, null);
         ByteArrayOutputStream os = ireq.getMergedResult();

         PSHtmlBodyInputStream is = new PSHtmlBodyInputStream(
               new ByteArrayInputStream(os.toByteArray()));
         PSXmlDecoder enc = new PSXmlDecoder();
         InputStreamReader r = new InputStreamReader(is, "UTF8");
         StringBuilder b = new StringBuilder();
         char buf[] = new char[1024];
         int count;
         while ((count = r.read(buf)) > 0)
         {
            b.append(buf, 0, count);
         }
         return (String) enc.encode(b.toString());
      }
      catch (Exception e)
      {
         return e.getLocalizedMessage();
      }
   }

   public abstract int doStartTag() throws JspException;

   public abstract int doEndTag() throws JspException;

   public void release()
   {
      // TODO Auto-generated method stub

   }

}
