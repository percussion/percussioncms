/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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


package com.percussion.design.objectstore.server;

import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.design.objectstore.PSRevisionEntry;
import com.percussion.design.objectstore.PSRevisionHistory;
import com.percussion.security.PSAclHandler;
import com.percussion.server.PSServer;
import com.percussion.util.PSCollection;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.FieldPosition;
import java.util.Comparator;
import java.util.Date;

/**
 *
 *   This object will contain the summary information for an application.
 *   <p>It will also include an AclHandler to check against when performing
 *   application updates, and a the file's last modified time to ensure the
 * AclHandler is valid (current).<p>
 *
 *   @see   PSXmlObjectStoreHandler
 */

public class PSApplicationSummary
{

   /**
    *
    *   Create an application summary based on the application itself<p>
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    * @param   app   The application
    */
   public PSApplicationSummary(PSApplication app)
   {
      m_name         = app.getName();
      m_id            = app.getId();
      m_description   = app.getDescription();
      m_isEnabled      = app.isEnabled();
      m_appRoot      = app.getRequestRoot();
      m_version      = app.getVersion();
      m_acl            = app.getAcl();
      m_roles        = app.getRoles();
      m_hidden       = app.isHidden();
      m_isEmpty       = app.isEmpty();
      m_appType       = app.getApplicationType();

      PSRevisionHistory hist = app.getRevisionHistory();
      if (hist != null)
      {
         m_majorVersion = hist.getLatestMajorVersion();
         m_minorVersion = hist.getLatestMinorVersion();
         PSRevisionEntry initialRevsionEntry = hist.getInitialRevision();
         if (initialRevsionEntry != null)
         {
            m_createdBy = initialRevsionEntry.getAgent();
            m_createdOn = initialRevsionEntry.getTime();
         }
      }
   }

   /**
    * Returns the ACL for the app. This will be <CODE>null</CODE>
    * after the first call to <CODE>setAclHandler()</CODE>. It could
    * also be <CODE>null</CODE> if the app did not define an ACL
    * handler.
    * <p>
    * This is a package-access method because only PSXmlObjectStoreHandler
    * should use it.
    *
    * @author   chadloder
    *
    * @version 1.6 1999/07/21
    *
    * @return   PSAcl
    */
   PSAcl getAcl()
   {
      return m_acl;
   }

   /**
    * Returns the roles for the app. This will be <CODE>null</CODE>
    * after the first call to <CODE>setAclHandler()</CODE>. It could
    * also be <CODE>null</CODE> if the app did not define an ACL
    * handler.
    * <p>
    * This is a package-access method because only PSXmlObjectStoreHandler
    * should use it.
    *
    * @author   chadloder
    *
    * @version 1.6 1999/07/21
    *
    * @return   PSAcl
    */
   PSCollection getRoles()
   {
      return m_roles;
   }

   /**
    *   Returns the Id of this application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   The app id
    */
   public int getId()
   {
      return m_id;
   }

   /**
    *
    *   Returns the name of this application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   The name
    */
   public String getName()
   {
      return m_name;
   }

   /**
    *
    *   Returns the description of this application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   The description
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    *
    *   Returns the acl handler for this application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   The acl handler for this application
    */
   public PSAclHandler getAclHandler()
   {
      return m_handler;
   }

   /**
    *
    *   Returns the application's root directory.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/25
    *
    *   @return   The application root directory
    */
   public String getAppRoot()
   {
      return m_appRoot;
   }

   /**
    *
    *   Returns the last modified time for the file
    *      which contains this application's definition.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   The last modified time.
    */
   public long getFileLastModified()
   {
      return m_fileLastModified;
   }

   /**
    *
    *   Returns whether or not this application is enabled.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   <code>true<code>   The application is enabled
    *            <code>false<code>   The application is not enabled
    */
   public boolean isEnabled()
   {
      return m_isEnabled;
   }

   /**
    * Is this a hidden application?
    *
    * @return  <code>true</code> if it is, <code>false</code> indicates
    *          a normal application.
    */
   public boolean isHidden()
   {
      return m_hidden;
   }

   /**
    * Is this a hidden application?
    *
    * @return  <code>true</code> if it is, <code>false</code> indicates
    *          a normal application.
    */
   public boolean isEmpty()
   {
      return m_isEmpty;
   }


   /**
    * Get the application type.
    *
    * @return  one of the {@link PSApplicationType} enumertated values.
    */
   public PSApplicationType getAppType()
   {
      return m_appType;
   }
   
   /**
    *
    *   Returns whether or not this application is running on the server.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @return   <code>true<code>   The application is running
    *            <code>false<code>   The application is not running
    */
   public boolean isActive()
   {
      return PSServer.isApplicationActive(m_name);
   }

   /**
    * Returns the version of this application.
    *
    * @author   chadloder
    *
    * @version 1.2 1999/07/12
    *
    * @return   String
    */
   public String getVersion()
   {
      return m_version;
   }

   /**
    *
    *   Sets the application id.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   id   The application id
    */
   void setId(int id)
   {
      m_id = id;
   }

   /**
    *
    *   Sets the application name.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   name   The application name
    */
   void setName(String name)
   {
      m_name = name;
   }

   /**
    *
    *   Sets the application id.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   description   The application description
    */
   void setDescription(String description)
   {
      m_description = description;
   }

   /**
    *
    *   Sets the enabled status of the application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   isEnabled  is this application enabled?
    */
   void setEnabled(boolean isEnabled)
   {
      m_isEnabled = isEnabled;
   }

   /**
    *
    *   Stores the acl handler associated with this application.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   handler   The application's acl handler.
    */
   void setAclHandler(PSAclHandler handler)
   {
      m_handler = handler;
      m_acl = null;
   }

   /**
    * Set whether this application is hidden or not.
    *
    * @param hidden  <code>true</code> if it is to be hidden,
    *                <code>false</code> indicates a normal application.
    */
   public void setHidden(boolean hidden)
   {
      m_hidden = hidden;
   }

   /**
    *
    *   Stores the last modified time associated with this application,
    *      to be used in conjunction with the acl handler to check
    *      security...
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/24
    *
    *   @param   lastModified   The application's on-file last modified time
    */
   void setFileLastModified(long lastModified)
   {
      m_fileLastModified = lastModified;
   }

   /**
    *
    *   Stores the appRoot for this application,
    *      to be used in application file save logic, to determine root.
    *
    * @author   David Gennaco
    *
    * @version 1.0 1999/6/25
    *
    *   @param   appRoot   The application's on-file last modified time
    */
   void setAppRoot(String appRoot)
   {
      m_appRoot = appRoot;
   }

   /**
    * Sets the version of the product that created this application.
    *
    * @author   chadloder
    *
    * @version 1.2 1999/07/12
    *
    * @return   String
    */
   void setVersion(String version)
   {
      m_version = version;
   }

   int getMajorVersion()
   {
      return m_majorVersion;
   }

   int getMinorVersion()
   {
      return m_minorVersion;
   }

   void setMajorMinorVersion(int major, int minor)
   {
      m_majorVersion = major;
      m_minorVersion = minor;
   }

   String getCreatedBy()
   {
      return m_createdBy;
   }

   Date getCreatedOn()
   {
      return m_createdOn;
   }

   public String toString()
   {
      StringBuilder buf = new StringBuilder(80);
      buf.append(m_name).append("(").append(m_id).append(") v").append( m_majorVersion).append(".").append(m_minorVersion)
         .append(" modified ");

      // append the modification date to the buffer
      buf.insert(new FieldPosition(0).getBeginIndex(),FastDateFormat.getInstance().format(new Date(m_fileLastModified)));

      return buf.toString();
   }

   /**
    * Returns an object that implements Comparator that is capable of comparing
    * two application summary objects.
    *
    * @return The Comparator.  Never <code>null</code>, compares the two objects
    * lexicographically, ignoring case.
    */
   public static Comparator getComparator()
   {
      return new PSApplicationSummaryComparator();
   }

   String         m_appRoot;
   String         m_description;
   String         m_name;
   int            m_id;
   boolean         m_isEnabled;
   PSAcl            m_acl;
   PSCollection   m_roles;
   PSAclHandler   m_handler;
   long            m_fileLastModified;
   String         m_version; // product version
   int            m_majorVersion;
   int            m_minorVersion;
   Date            m_createdOn;
   String         m_createdBy;

   /*
    * Is this a hidden application?  <code>true</code> if so, <code>false</code>
    * indicates a normal application.
    */
   boolean        m_hidden;

   /*
    * Is this an empty application?  <code>true</code> if so, <code>false</code>
    * indicates that the application has at least one resource.
    */
   boolean        m_isEmpty;
   
   /*
    * Is this a hidden application?  <code>true</code> if so, <code>false</code>
    * indicates a normal application.
    */
   PSApplicationType m_appType;

   /**
    * A class that can compare two PSApplicationSummary objects
    * lexicographically by application name.
    */
   private static class PSApplicationSummaryComparator implements Comparator
   {
      /**
       * Compares the m_name elements of two PSApplicationSummary objects
       * lexicographically, case insensitive.
       *
       * @param o1 The first PSApplicationSummary object.  May not be <code>null
       * </code>.
       * @param o2 The second PSApplicationSummary object. May not be <code>null
       * </code>.
       *
       * @return a negative integer, zero, or a positive integer as the
       * first object's name is less than, equal to, or greater than the second
       * lexicographically.
       *
       * @throws IllegalArgumentException if either argument is <code>null
       * </code>.
       * @throws ClassCastException if either parameter is not an instance of
       * a PSApplicationSummary object.
       */
      public int compare(Object o1, Object o2)
      {
         if (o1 == null || o2 == null)
            throw new IllegalArgumentException("one or more params is null");
            
         return ((PSApplicationSummary)o1).getName().compareToIgnoreCase(
            ((PSApplicationSummary)o2).getName());
      }
   }
}
