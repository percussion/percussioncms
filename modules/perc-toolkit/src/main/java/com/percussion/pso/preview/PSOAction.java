/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.preview;

import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;
import java.util.Properties;

/**
 * Like PSAction but without all the PSDbComponent baggage
 *
 * @author DavidBenua
 *
 */
public class PSOAction implements Comparable<PSOAction>
{
   
   private String handler;
   private String label;
   private String name;
   private String url;
   private String type; 
   private int sortrank;
   private String description;
   private Properties properties; 
   
   /**
    * Default constructor 
    */
   public PSOAction()
   {
      
   }

   /**
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(PSOAction other)
   {
      if(this == other) return 0; 
      return this.label.compareTo(other.label); 
   }

   /**
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return super.equals(obj);
   }

   @SuppressWarnings("unused")
public Element toXml(Document doc)
   {
	   Element root = doc.createElement("Action");
      if(doc == null)
         throw new IllegalArgumentException("doc may not be null.");

      root.setAttribute(NAME_ATTR, name);
      root.setAttribute(LABEL_ATTR, label);
      root.setAttribute(TYPE_ATTR, type);

      root.setAttribute(URL_ATTR, url);
      root.setAttribute(HANDLER_ATTR, handler);
      root.setAttribute(SORTRANK_ATTR, String.valueOf(sortrank));
      if (StringUtils.isNotBlank(description))
         {
         PSXmlDocumentBuilder.addElement(doc, root, "Description", description);
         }

     if ( properties != null && (!properties.isEmpty()))
     {
         Element props = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Props");
         Iterator<?> propitr = properties.keySet().iterator(); 
         while(propitr.hasNext())
         {
            String key = (String)propitr.next(); 
            String value = properties.getProperty(key);
            Element prop = PSXmlDocumentBuilder.addElement(doc, props, "Prop", value); 
            prop.setAttribute("propid", "0");
            prop.setAttribute("name", key);
         }
     }
  
      return root; 
   }
   /**
    * @return the handler
    */
   public String getHandler()
   {
      return handler;
   }

   /**
    * @param handler the handler to set
    */
   public void setHandler(String handler)
   {
      this.handler = handler;
   }

   /**
    * @return the label
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * @param label the label to set
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name the name to set
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      this.url = url;
   }

   /**
    * @return the type
    */
   public String getType()
   {
      return type;
   }

   /**
    * @param type the type to set
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * @return the sortrank
    */
   public int getSortrank()
   {
      return sortrank;
   }

   /**
    * @param sortrank the sortrank to set
    */
   public void setSortrank(int sortrank)
   {
      this.sortrank = sortrank;
   }

   /**
    * @return the description
    */
   public String getDescription()
   {
      return description;
   }

   /**
    * @param description the description to set
    */
   public void setDescription(String description)
   {
      this.description = description;
   }

   /**
    * @return the properties
    */
   public Properties getProperties()
   {
      return properties;
   }

   /**
    * @param properties the properties to set
    */
   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }
   
   private static final String LABEL_ATTR = "label";
   private static final String NAME_ATTR = "name";
   private static final String TYPE_ATTR = "type";
   private static final String URL_ATTR = "url";
   private static final String HANDLER_ATTR = "handler";
   private static final String SORTRANK_ATTR = "sortrank";
}
