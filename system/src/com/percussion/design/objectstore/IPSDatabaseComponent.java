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

import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This interface is used by Object store components whose state is saved in
 * a database rather than an XML file. The objects are still XML based. The
 * object uses the {@link PSDatabaseComponentLoader} class to perform the much
 * of the work of loading state. The conversion between the database state 
 * format and the object's XML format is done using an Rx application.
 * The database component loader will be used to generate xml elements that
 * conform to the model used in {@link IPSComponent#fromXml} for each 
 * component type. Saving will be done via generation of database modification
 * directives in XML format which can be sent to another Rx application for 
 * batch update processing.  This xml format will be accomplished by calling 
 * {@link #toDatabaseXml} in each component, which will decide what (if any) 
 * modifications to submit to the back end based on that component's state.
 */
public interface IPSDatabaseComponent
  extends IPSComponent
{
   /**
    * Is this database component to be removed from the back end?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isDelete();

   /**
    * Add the action(s) needed to serialize all changes made to this
    * database component to the back end.
    *
    * @param doc The database modification document being created. May
    * not be <code>null</code>.
    * 
    * @param actionRoot The root element under which all DBMS actions
    * (updates, inserts, and deletes) are to be added.  May not be
    * <code>null</code> or empty.
    *
    * @param relationContext  Any relationship context information, so 
    * that new relationships may be added to the back end as needed by
    * the components which require them.  May be <code>null</code> indicating
    * there is no relationship existing for this component.
    *
    * @throws IllegalArgumentException if any argument is invalid.
    *
    * @throws PSDatabaseComponentException if there are errors creating the 
    *    element.
    */      
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext)
      throws PSDatabaseComponentException;

   /**
    * Instantiate the values in this database component from the xml
    * element returned by the query resource associated with this database
    * component type.
    *
    * @param e The xml element defining this component.  
    *    May not be <code>null</code>.
    *
    * @param cl The database component loader.  May not be <code>null</code>,
    * this is may be used to load any subcomponents.
    *
    * @param relationContext The current relation context, may be 
    * <code>null</code> if no relationship exists.
    *
    * @throws IllegalArgumentException if any argument is invalid
    *
    * @throws PSUnknownNodeTypeException if this component or one of it's
    * sub-components fails to initialize with a supplied element.
    *
    * @throws PSDatabaseComponentException if any othererror occurs loading this
    * component from the database
    */
   public void fromDatabaseXml(Element e, PSDatabaseComponentLoader cl, 
      PSRelation relationContext)
      throws   PSUnknownNodeTypeException,
               PSDatabaseComponentException;

   /**
    * Get the name of the dataset in the system component application
    * which retrieves components of this type.
    *
    * @return The dataset name.  Never <code>null</code> or empty.
    */
   public String getDatabaseAppQueryDatasetName();

   /**
    * Get the name of the key to use to identify this component from the
    * data retrieved from the system components application
    *
    * @return The key name.  <code>null</code> or empty indicates all components
    * of this type are to be retrieved, or no key is necessary.
    */
   public String getDatabaseComponentIdKeyName();
   
   /**
    * The name of the database component's id attribute
    */
   public String DATABASE_COMPONENT_ID_XML_ATTR_NAME = "DbComponentId";

   /**
    * The component is unchanged since being retrieved by the back end.
    */
   static final int DATABASE_COMPONENT_UNCHANGED = 0;

   /**
    * The component has been updated since being retrieved by the back end.
    */
   static final int DATABASE_COMPONENT_UPDATED = 1;

   /**
    * The component has been deleted since being retrieved by the back end.
    */
   static final int DATABASE_COMPONENT_DELETED = 2;

   /**
    * The component is new, and must be added to the back end.
    */
   static final int DATABASE_COMPONENT_INSERTED = 3;

   /**
    * The component may be ignored (it was inserted, then deleted).
    */
   static final int DATABASE_COMPONENT_DISCARDED = 4;

   /**
    * String for insert action.
    */
   public static final String DATABASE_ACTION_INSERT = "INSERT";

   /**
    * String for update action.
    */
   public static final String DATABASE_ACTION_UPDATE = "UPDATE";

   /**
    * String for delete action.
    */
   public static final String DATABASE_ACTION_DELETE = "DELETE";
}

