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
package com.percussion.webui.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

@SuppressWarnings("serial")
public class TmxMessageTag extends TagSupport 
{
    public void setKey(String key)
	{
	   m_key = key;	
	}
	
	@Override
	public int doStartTag() throws JspException {
        
        
        String lang = (String)pageContext.getAttribute("sys_lang");
        String debug = (String)pageContext.getAttribute("debug");
        if(m_key == null || m_key.length() == 0)
            throw new IllegalArgumentException("The key must be specified."); 
        TmxCache cache = TmxCache.getInstance();
        String val = cache.getValue(lang, m_key);
        if(val == null || val.length() == 0)
        {
           val = (debug != null && debug.equalsIgnoreCase("true"))
              ? m_key 
              : getKeyDisplayValue(m_key); 
        }
        try 
        {
           pageContext.getOut().print(val);
        } catch (IOException e) {
        
           throw new JspException(e);
        }
        return SKIP_BODY;
    }

    private String getKeyDisplayValue(String key)
    {
        String[] temp = key.split("@");
        if(temp.length == 1 || temp.length > 2)
            return key;
        return temp[1];
    }

    private String m_key;
}
