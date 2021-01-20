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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.design.objectstore;

import com.percussion.cms.objectstore.PSKey;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A container that holds an object locator, e.g. for content items this
 * will be the contentid and revision.
 */
public class PSLocator extends PSKey implements Serializable
{
   /**
    * Creates an empty locator object. This is used for creating a new
    * component that has not been saved to the database.
    */
   public PSLocator()
   {
      super(KEY_PARTS);
   }
   
   /**
    * Convenience constructor {#link PSLocator(int, int)} with an undefined 
    * revision. The revision of the constructed object is default to 
    * <code>-1</code> and it is also default to persisted.
    *  
    * @param id the locator id, must be >= 0.
    */
   public PSLocator(int id)
   {
      super(KEY_PARTS, new int[]{id, -1}, true);
      
      if (id < 0)
         throw new IllegalArgumentException("id must be >= 0");
   }
   
   /**
    * This ctor can be used to create the definition and data from a
    * previously serialized key.
    *
    * @param src The xml previously created with <code>toXml</code>.
    *    Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException See {@link #fromXml(Element)}.
    */
   public PSLocator(Element src) throws PSUnknownNodeTypeException
   {
      super(src);
   }
   
   /**
    * Constructs a new locator for the supplied id and revision.
    * The constructed object is persisted.
    * 
    * @param id the locator id, must be >= 0.
    * @param revision the locator revision, -1 means undefined.
    * @throws IllegalArgumentException if the supplied id is invalid.
    */
   public PSLocator(int id, int revision)
   {
      super(KEY_PARTS, new int[]{id, revision}, true, false);

      if (id < 0)
         throw new IllegalArgumentException("id must be >= 0");      
   }
   
   /**
    * Just like {@link #PSLocator(int, int)}, except the persisted flag
    * is determined by the <code>persisted</code> parameter.
    */
   public PSLocator(int id, int revision, boolean persisted)
   {
      super(KEY_PARTS, new int[]{id, revision}, persisted, (!persisted));

      if (id < 0)
         throw new IllegalArgumentException("id must be >= 0");      
   }

   /**
    * Convenience constructor {#link PSLocator(String, String)} with an 
    * undefined revision. The constructed object is default to persisted.
    * 
    * @param id the locator id. It may not be <code>null</code> or empty. 
    *    It must be a parsable int and >= 0.
    */
   public PSLocator(String id)
   {
      super(KEY_PARTS, new String[]{id, ""}, false);

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (Integer.parseInt(id) < 0)
         throw new IllegalArgumentException("id must be >= 0");      
   }
   
   /**
    * Constructs a new locator for the supplied id and revision.
    * The constructed object is persisted.
    * 
    * @param id the locator id. It may not be <code>null</code> or empty. 
    *    It must be a parsable int and >= 0.
    * 
    * @param revision the locator revision, may be <code>null</code>.
    * 
    * @throws IllegalArgumentException if the supplied id is invalid.
    *    
    * @throws NumberFormatException if the supplied id is not a parsable int.
    */
   public PSLocator(String id, String revision)
   {
      super(KEY_PARTS, new String[]{id, revision}, true);

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (Integer.parseInt(id) < 0)
         throw new IllegalArgumentException("id must be >= 0");      
   }
   

   /**
    * Get id that has been assigned to this object.
    * 
    * @return <code>-1</code> if there is no assigned id yet.
    */   
   public int getId()
   {
      String sID = getPart(KEY_ID);
      
      return (sID.trim().length() == 0) ? -1 : Integer.parseInt(sID);
   }
   
   /**
    * Get the locator revision.
    * 
    * @return the locator revision, <= -1 means undefined.
    */
   public int getRevision()
   {
      String rev = getPart(KEY_REVISION);
      
      return (rev.trim().length() == 0) ? -1 : Integer.parseInt(rev);
   }
   
   /**
    * Set the new locator revision.
    * 
    * @param revision the new revision, <= -1 means undefined.
    */
   public void setRevision(int revision)
   {
      if (revision < -1)
         revision = -1;
         
      setPart(KEY_REVISION, Integer.toString(revision));
   }
   
   /**
    * Is the revision part of the locator?
    * 
    * @return <code>false</code> if the revision is undefined, <code>true</code>
    *    otherwise.
    */
   public boolean useRevision()
   {
      return (getRevision() >= 0);
   }
   
   /**
    * See {@link com.percussion.cms.objectstore.IPSCmsComponent#getNodeName()}
    */
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }   

   public Object clone()
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      PSKey copy = null;
      try
      {
         copy = new PSLocator(this.toXml(doc));
      }
      catch (Exception e)
      { /* not possible */ 
         System.out.println("PSLocator.clone() caught exception: \n" + e.toString());
      }

      return copy;
   }
   
   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXLocator";

   /*
    * The following strings define all elements/attributes used to parse/create 
    * the XML for this object. No Java documentation will be added to this.
    */   
   public final static String KEY_ID = "CONTENTID";
   public final static String KEY_REVISION = "REVISIONID";
   
   private final static String[] KEY_PARTS = new String[]{KEY_ID, KEY_REVISION};
   
}
