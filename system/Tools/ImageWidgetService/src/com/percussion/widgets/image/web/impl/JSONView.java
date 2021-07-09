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

package com.percussion.widgets.image.web.impl;
      
      import java.io.PrintWriter;
      import java.util.Iterator;
      import java.util.Map;
      import java.util.Set;
      import javax.servlet.http.HttpServletRequest;
      import javax.servlet.http.HttpServletResponse;
      import net.sf.json.JSON;
      import org.springframework.web.servlet.View;
      import org.springframework.web.servlet.view.AbstractView;
      import org.springframework.web.util.WebUtils;
      
      public class JSONView extends AbstractView
        implements View
      {
      String contentType = "text/plain";
      String htmlContentType = "text/html";
      String modelObjectName = null;
      
        protected void renderMergedOutputModel(Map model, HttpServletRequest request, HttpServletResponse response)
          throws Exception
        {
        boolean debug = WebUtils.hasSubmitParameter(request, "debug");
        boolean isHtml = WebUtils.hasSubmitParameter(request, "forceHTML");
        String requestedWith = request.getHeader("HTTP_X_REQUESTED_WITH");
        boolean jQueryFormFile = WebUtils.hasSubmitParameter(request, "jQueryFormFile");
      
        PrintWriter pw = response.getWriter();
        response.setContentType(isHtml ? this.htmlContentType : this.contentType);
      
        if (isHtml)
          {
          pw.write("<html><head><title>ImageResult</title></head>");
          pw.write("<body><textarea rows=\"33\" cols=\" 100\">");
       } else if (jQueryFormFile)
          {
          pw.write("<textarea>");
          response.setContentType(this.htmlContentType);
          }
      
        if (this.modelObjectName != null)
          {
          JSON json = (JSON)model.get(this.modelObjectName);
          if (debug)
            pw.write(json.toString(3));
            else
            pw.write(json.toString());
          }
          else
          {
          Iterator itr = model.entrySet().iterator();
          while (itr.hasNext())
            {
            Object member = itr.next();
            if ((member instanceof JSON))
              {
              JSON json = (JSON)member;
              if (debug)
                pw.write(json.toString(3));
                else {
                pw.write(json.toString());
                }
              }
            }
          }
        if (isHtml)
          {
          pw.write("</textarea></body></html>");
        } else if (jQueryFormFile)
          {
          pw.write("</textarea>");
          }
        pw.flush();
       response.flushBuffer();
        }
      
        public String getContentType()
        {
        	return this.contentType;
        }
      
        public void setContentType(String contentType)
        {
        	this.contentType = contentType;
        }
      
        public String getModelObjectName()
        {
        	return this.modelObjectName;
        }
      
        public void setModelObjectName(String modelObjectName)
        {
        	this.modelObjectName = modelObjectName;
        }
      }
