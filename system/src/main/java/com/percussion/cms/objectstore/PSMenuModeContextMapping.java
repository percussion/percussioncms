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
package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This represents a relationship between an action and the mode and context
 * within which it should be displayed to the end user.
 * <p>This class is immutable, therefore it doesn't need to override the
 * clone methods.
 *
 * @author Paul Howard
 * @version 1.0
 *
 * @see PSMenuMode
 * @see PSMenuContext
 * @see PSAction
 */
public class PSMenuModeContextMapping extends PSDbComponent
{
   /**
    * no-args constructor
    */
   
   public PSMenuModeContextMapping()
   {
   }

   public PSMenuModeContextMapping(PSMenuMode mode, PSMenuContext ctx)
   {
      super(createKey(null, null, null));
      m_modeId = mode.getLocator().getPart(PSMenuMode.PRIMARY_KEY);
      m_contextId = ctx.getLocator().getPart(PSMenuContext.PRIMARY_KEY);
   }

   /**
    * Create an object from a previously serialized one.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException
    */
   public PSMenuModeContextMapping(Element src)
      throws PSUnknownNodeTypeException
   {
      super(createKey(null, null, null));
      fromXml(src);
   }
   
   /**
    * Creates an object from a mode and a context id.
    * 
    * @param modeId the mode id, never <code>null</code> or empty.
    * @param ctxId the context id, never <code>null</code> or empty.
    * @param actionId the action (or parent) id, never <code>null</code> or 
    *   empty.
    */
   public PSMenuModeContextMapping(String modeId, String ctxId, String actionId)
   {
      super(createKey(modeId, ctxId, actionId));
      m_modeId = modeId;
      m_contextId = ctxId;
   }

   /**
    * See {@link PSMenuMode} for details.
    *
    * @return The identifier associated with this mapping. Never <code>null
    *    </code> or empty.
    */
   public String getModeId()
   {
      return m_modeId;
   }

   /**
    * @return the name of the mode. It may be empty, but never <code>null</code>.
    */
   public String getModeName()
   {
      return m_modeName;
   }

   /**
    * Sets the mode name.
    * 
    * @param modeName the new mode name. It is treated as empty if the
    *   new context name is <code>null</code>.
    */
   public void setModeName(String modeName)
   {
      m_modeName = (modeName == null) ? "" : modeName;
   }
   
   /**
    * Sets the context name.
    * 
    * @param ctxName the new context name. It is treated as empty if the
    *   new context name is <code>null</code>.
    */
   public void setContextName(String ctxName)
   {
      m_contextName = (ctxName == null) ? "" : ctxName;
   }
   
   /**
    * See {@link PSMenuContext} for details.
    *
    * @return The identifier associated with this mapping. Never <code>null
    *    </code> or empty.
    */
   public String getContextId()
   {
      return m_contextId;
   }

   /**
    * @return the name of the context. It may be empty, but never 
    *    <code>null</code>.
    */
   public String getContextName()
   {
      return m_contextName;
   }


   /**
    * See interface/base class for description.
    * The dtd (based on the base class) is:
    * <pre><code>
    *    &lt;!ELEMENT getNodeName() (getLocator().getNodeName())&gt;
    *    &lt;!ATTLIST getNodeName()
    *       state (DBSTATE_xxx)
    *       modeId CDATA #REQUIRED
    *       uiContextId CDATA #REQUIRED
    *       modeName CDATA #IMPLIED
    *       uiContextName CDATA #IMPLIED
    *       &gt;
    * </code></pre>
    */
   public Element toXml(Document doc)
   {
      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_MODEID, m_modeId);
      root.setAttribute(XML_ATTR_CONTEXTID, m_contextId);
      root.setAttribute(XML_ATTR_MODENAME, m_modeName);
      root.setAttribute(XML_ATTR_CONTEXTNAME, m_contextName);
      return root;
   }


   //see interface/base class for description
   public void fromXml(Element source)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(source);
      m_modeId = PSXMLDomUtil.checkAttribute(source, XML_ATTR_MODEID, true);
      m_contextId = PSXMLDomUtil.checkAttribute(source, XML_ATTR_CONTEXTID,
            true);
      m_modeName = PSXMLDomUtil
            .checkAttribute(source, XML_ATTR_MODENAME, false);
      m_contextName = PSXMLDomUtil.checkAttribute(source, XML_ATTR_CONTEXTNAME,
            false);
      
      //do some validation
      PSKey key = getLocator();
      if (key.isAssigned())
      {
         if (!m_modeId.equals(key.getPart(MODEID_COLNAME))
               || !m_contextId.equals(key.getPart(CONTEXTID_COLNAME)))
         {
            String[] args =
            {
               getComponentType(),
               m_modeId + ":" + m_contextId,
               key.getPart(MODEID_COLNAME) + ":"
                     + key.getPart(CONTEXTID_COLNAME)
            };
            throw new PSUnknownNodeTypeException(
                  IPSCmsErrors.MISMATCH_BETWEEN_KEY_AND_DATA, args);
         }
      }
   }


   //see interface/base class for description
   public boolean equalsFull(Object obj)
   {
      if (!equals(obj))
         return false;
      else if (!super.equalsFull(obj))
         return false;
      return true;
   }


   //see interface/base class for description
   public boolean equals(Object obj)
   {
      if (!super.equals(obj))
         return false;

      PSMenuModeContextMapping other = (PSMenuModeContextMapping) obj;

      if (!m_modeId.equalsIgnoreCase(other.m_modeId))
         return false;
      else if (!m_contextId.equalsIgnoreCase(other.m_contextId))
         return false;

      return true;
   }


   /**
    * Generates code of the object. Overrides {@link Object#hashCode().
    */
   @Override
   public int hashCode()
   {
      return super.hashCode();
   }

   /**
    * Creates a key containing the proper definition for this object.
    *
    * @return Never <code>null</code>.
    */
   private static PSKey createKey(String modeId, String ctxId, String actId)
   {
      String[] keyDef = new String[] {MODEID_COLNAME, CONTEXTID_COLNAME,
         "ACTIONID"};
   
      if (modeId == null)
      {
         // as a reference object, don't need to assign any id
         return new PSKey(keyDef, false); 
      }
      else
      {
         String[] values = new String[] {modeId, ctxId, actId};
         return new PSKey(keyDef, values, false);
         
      }
   }

   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getModeId(), getContextId()};
   }

   /**
    * The name of the column that stores the m_modeId value.
    */
   private static final String MODEID_COLNAME = "MODEID";

   /**
    * The name of the column that stores the m_contextId value.
    */
   private static final String CONTEXTID_COLNAME = "UICONTEXTID";


   //constants for element/attribute names
   public static final String XML_ATTR_MODEID = "modeId";
   public static final String XML_ATTR_MODENAME = "modeName";
   public static final String XML_ATTR_CONTEXTID = "uiContextId";
   public static final String XML_ATTR_CONTEXTNAME = "uiContextName";

   /**
    * Set during construction, then never changed. The unique identifier for
    * the mode associated with this mapping.
    */
   private String m_modeId;

   /**
    * Set during construction, then never changed. The unique identifier for
    * the UI context associated with this mapping.
    */
   private String m_contextId;
   
   /**
    * The name of the mode. It is used for display only. It may be empty, but 
    * never <code>null</code>. It is only set by {@link #fromXml(Element)}
    * Default to empty.  
    */
   private String m_modeName = "";
   
   /**
    * The name of the context. It is used for display only. It may be empty, but 
    * never <code>null</code>. It is only set by {@link #fromXml(Element)}
    * Default to empty.  
    */
   private String m_contextName = "";
}
