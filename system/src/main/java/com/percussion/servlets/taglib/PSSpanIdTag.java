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

import javax.faces.component.UIComponent;

/**
 * The span id tag allows the creation of elements in a JSF tree that have
 * calculate ids, which is useful for updating said elements using Ajax
 * 
 * @author dougrand
 *
 */
public class PSSpanIdTag extends PSJSFBaseTag
{
   /**
    * The id
    */
   String m_definedid;
   
   /**
    * A CSS style to be applied
    */
   String m_inlineStyle;
   
   @Override
   public String getComponentType()
   {
      return "com.percussion.jsf.SpanId";
   }

   /* (non-Javadoc)
    * @see com.percussion.servlets.taglib.PSJSFBaseTag#setProperties(javax.faces.component.UIComponent)
    */
   @Override
   protected void setProperties(UIComponent comp)
   {
      super.setProperties(comp);
      setValueBinding(comp, "definedid", m_definedid);
      setValueBinding(comp, "inlinestyle", m_inlineStyle);
   }
   
   /**
    * @return the id
    */
   public String getDefinedid()
   {
      return m_definedid;
   }

   /**
    * @param definedid the id to set
    */
   public void setDefinedid(String definedid)
   {
      m_definedid = definedid;
   }
   
   /**
    * @return the inlineStyle
    */
   public String getInlineStyle()
   {
      return m_inlineStyle;
   }

   /**
    * @param inlineStyle the inlineStyle to set
    */
   public void setInlineStyle(String inlineStyle)
   {
      m_inlineStyle = inlineStyle;
   }
}
