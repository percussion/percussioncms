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
 * This represents a relationship between a menu and one of its child items.
 * If a PSAction contains a submenu, each child PSAction in that submenu
 * is associated with the parent action with one of these objects (the
 * parent action in this case is not really an action).
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
public class PSMenuChild extends PSDbComponent
{
   /**
    * Create a new relation between a menu and its 'item'. It is only
    * useful once it has been added to an action.
    *
    * @param child Never <code>null</code>. The key for this action must be
    *    assigned.
    */
   public PSMenuChild(PSAction child)
   {
      super(getKey(null, null));
      if (null == child)
         throw new IllegalArgumentException("Action cannot be null.");

      PSKey key = child.getLocator();
      if (!key.isAssigned())
         throw new IllegalArgumentException("Key must be assigned.");
      m_childId = key.getPart(PSAction.PRIMARY_KEY);
   }

   /**
    * No args constructor for xstream serialization
    */
   public PSMenuChild(){
	   
   }

   /**
    *
    * @return The identifier associated with this mapping. Never <code>null
    *    </code> or empty.
    */
   public String getChildActionId()
   {
      return m_childId;
   }

   /**
    * Gets the (internal) name of the child action.
    * @return the name of the action, it may be <code>null</code> if not set
    *   by the {@link #fromXml(Element)} or {@link #PSMenuChild(Element)}.
    */
   public String getChildActioName()
   {
      return m_childName;
   }

   /**
    * Create an object from a previously serialized one.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See fromXml().
    */
   public PSMenuChild(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKey(null, null));
      super.fromXml(src);  //to restore key and state
      fromXml(src);
   }

   /**
    * Creates an object from a child and parent (action) ids.
    * 
    * @param childId the child id.
    * @param childName the child name, may be <code>null</code> or empty.
    * @param parentId the parent id.
    */
   public PSMenuChild(long childId, String childName, long parentId)
   {
      super(getKey(String.valueOf(childId), String.valueOf(parentId)));
      m_childId = String.valueOf(childId);
      m_childName = childName;
   }

   /**
    * See interface/base class for description.
    * The dtd (based on the base class) is:
    * <pre><code>
    *    &lt;!ELEMENT getNodeName() (getLocator().getNodeName())&gt;
    *    &lt;!ATTLIST getNodeName()
    *       state (DBSTATE_xxx)
    *       childId CDATA #REQUIRED
    *       &gt;
    * </code></pre>
    */
   public Element toXml(Document doc)
   {
      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_CHILDID, m_childId);
      if (m_childName != null)
         root.setAttribute(XML_ATTR_CHILDNAME, m_childName);
      return root;
   }


   //see interface/base class for description
   public void fromXml(Element source)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(source);
      m_childId = PSXMLDomUtil.checkAttribute(source, XML_ATTR_CHILDID, true);
      m_childName = PSXMLDomUtil.checkAttribute(source, XML_ATTR_CHILDNAME,
            false);

      //do some validation
      PSKey key = getLocator();
      if (key.isAssigned())
      {
         if (!m_childId.equals(key.getPart(CHILDACTIONID_COLNAME)))
         {
            String[] args =
            {
               getComponentType(),
               m_childId,
               key.getPart(CHILDACTIONID_COLNAME)
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

      PSMenuChild other = (PSMenuChild) obj;

      if (!m_childId.equalsIgnoreCase(other.m_childId))
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
    * Assumed no need to generate id and the id will be set afterwards.
    *
    * @return Never <code>null</code>.
    */
   private static PSKey getKey(String childId, String parentId)
   {
      String[] keyDef = new String [] {CHILDACTIONID_COLNAME, "ACTIONID"};
      
      if (childId == null || parentId == null)
      {
         // this is a reference object. don't need to assign ids
         // the ids have to be set later
         return new PSKey(keyDef, false);
      }
      else
      {
         String[] values = new String[] {childId, parentId};
         return new PSKey(keyDef, values, false);
      }
   }

   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getChildActionId()};
   }

   /**
    * The name of the column that stores the m_childActionId value.
    */
   private static final String CHILDACTIONID_COLNAME = "CHILDACTIONID";


   //constants for element/attribute names
   public static final String XML_ATTR_CHILDID = "childId";
   public static final String XML_ATTR_CHILDNAME = "childName";


   /**
    * Set during construction, then never changed. The unique identifier for
    * the child PSAction associated with this mapping.
    */
   private String m_childId;
   
   /**
    * The (internal) name of the object. It may be <code>null</code> if has
    * not been set.
    */
   private String m_childName = null;
}
