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
package com.percussion.design.objectstore;

import com.percussion.cms.objectstore.PSKey;
import com.percussion.error.PSExceptionUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.Serializable;

/**
 * A container that holds an object locator, e.g. for content items this
 * will be the contentid and revision.
 */
public class PSLocator extends PSKey implements Serializable
{

   private static final Logger log = LogManager.getLogger(PSLocator.class);
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
         log.error("PSLocator.clone() caught exception: {}", PSExceptionUtils.getMessageForLog(e));
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
