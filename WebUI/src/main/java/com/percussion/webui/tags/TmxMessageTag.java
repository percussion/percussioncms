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
package com.percussion.webui.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

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
