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
package com.percussion.design.objectstore;

import com.percussion.data.PSIdGenerator;
import com.percussion.data.PSSqlException;
import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.SQLException;

/**
 * This interface is used by Object store components whose state is saved in
 * a database rather than an XML file. The objects are still XML based. The
 * object uses this interface to serialize itself to and from a back end
 * database via a different XML format and a Rx application.
 */
public abstract class PSDatabaseComponent
 implements IPSDatabaseComponent
{
   // see interface for description
   public Object clone()
   {
      PSDatabaseComponent copy = null;
      try
      {
         copy = (PSDatabaseComponent) super.clone();
      } catch (CloneNotSupportedException e)
      {
      } // cannot happen
      return copy;
   }

   /**
    * Is this database component an insert?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isInsert()
   {
      return (m_componentState == DATABASE_COMPONENT_INSERTED);
   }

   // see interface for description
   public boolean isDelete()
   {
      return (m_componentState == DATABASE_COMPONENT_DELETED) ||
               (m_componentState == DATABASE_COMPONENT_DISCARDED);
   }


   /**
    * Is this database component an update?
    *
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isUpdate()
   {
      return (m_componentState == DATABASE_COMPONENT_UPDATED);
   }


   /**
    * Mark this database component to be an insert action against
    * the database when it is persisted.
    */
   protected void setInsert()
   {
      m_componentState = DATABASE_COMPONENT_INSERTED;
   }

   /**
    * Get the database component id.
    *
    * @return The database component id, -1 indicates that it has not
    * had an id allocated yet.
    */
   public int getDatabaseComponentId()
   {
      return m_databaseComponentId;
   }
   
   /**
    * Mark this database component to become a delete action against the 
    * database, unless the component was previously created before being
    * persisted to the database, in which case the component will be 
    * marked as discarded.
    */
   protected void setDelete()
   {
      if (isInsert())
         m_componentState = DATABASE_COMPONENT_DISCARDED;
      else
         m_componentState = DATABASE_COMPONENT_DELETED;
   }


   /**
    * Mark this database component to be an update action to
    * the database when it is persisted.
    */
   protected void setUpdate()
   {
      m_componentState = DATABASE_COMPONENT_UPDATED;
   }

   /**
    * Add the action(s) needed to serialize all changes made to this
    * database component to the back end.  Default case will use <code>toXml
    * </code>, first handling the creation of the action row, and db component
    * id (if necessary).  Any database elements which require relation
    * information and sub components must override this method and use the
    * relationContext to persist their children.
    *
    * @see IPSDatabaseComponent#toDatabaseXml(Document, Element, PSRelation) 
    * for the interface definition.
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null)
         throw new IllegalArgumentException("one or more params is null");

      // We'll ignore any relationContext passed. ph: if not null, shouldn't we add ourselves and save it?

      // if we are new, generate a new id
      if (isInsert())
         createDBComponentId();

      // Add action element to root
      Element actionElement = getActionElement(doc, actionRoot);
      actionRoot.appendChild(actionElement);

      // just toXml ourselves to this root - any extra data will be ignored
      actionElement.appendChild(toXml(doc));
   }


   // see IPSDatabaseComponent interface
   public String getDatabaseAppQueryDatasetName()
   {
      return buildAppQueryDatasetName( getComponentType());
   }


   /**
    * Given a component type, create the name of the dataset that is used
    * to retrieve components of this type.
    *
    * @param componentType See {@link #getComponentType()} for details. Never
    *    <code>null</code> or empty.
    *
    * @return A valid name of a dataset in the sys_components application that
    *    can be used to query objects of the specified type.
    *
    * @throws IllegalArgumentException If componentType is <code>null</code>
    *    or empty.
    */
   public static String buildAppQueryDatasetName( String componentType )
   {
      if ( null == componentType || componentType.trim().length() == 0 )
         throw new IllegalArgumentException( "type can't be null" );

      return "get" + componentType;
   }

   /**
    * Get the name of the key to use to identify this component from the
    * data retrieved from the system components application. By default, this
    * name is of the form '<componentType>_id'.
    *
    * @return A valid identifier.
    */
   public String getDatabaseComponentIdKeyName()
   {
      return getComponentType() + "_id";
   }

   /**
    * Get the action element associated with this database component.
    * Classes which extend this class should call this and then proceed
    * to add their own specific keys and values to the returned element.
    * 
    * @param doc The database modification document being created, assumed
    * not <code>null</code>.
    * 
    * @param actionRoot The root element under which all DBMS actions
    * (updates, inserts, and deletes) are to be added.  May not be
    * <code>null</code>.
    *
    * @return the action element for this database component, 
    * <code>null</code> indicates no action need be performed for this
    * component at this time.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   protected Element getActionElement(Document doc, Element actionRoot)
   {
      if (actionRoot == null)
         throw new IllegalArgumentException(
            "Action root element must be supplied.");      

      if (doc == null)
         throw new IllegalArgumentException(
            "Database modification document must be supplied.");

      Element actionElement = null;
      if (m_componentState != DATABASE_COMPONENT_UNCHANGED)
      {
         // create a new action element with the appropriate db action type
         actionElement = PSXmlDocumentBuilder.addEmptyElement(doc, actionRoot,
            XML_DBACTIONELEMENT_NAME);
         actionElement.setAttribute(XML_DBACTIONELEMENT_TYPE_ATTRIBUTE_NAME,
            getActionTypeString());
      }
      return actionElement;
   }

   /**
    * Get the text associated with the database action to be performed
    * by this component.
    *
    * @return The database action text, never <code>null</code>, if empty,
    * this indicates the component has not been altered, and no database
    * action needs to be performed for this component.
    */
   private String getActionTypeString()
   {
      switch (m_componentState)
      {
         case DATABASE_COMPONENT_INSERTED:
            return DATABASE_ACTION_INSERT;

         case DATABASE_COMPONENT_UPDATED:
            return DATABASE_ACTION_UPDATE;

         case DATABASE_COMPONENT_DELETED:
            return DATABASE_ACTION_DELETE;

         default:
            return "";
      }
   }
   
   /**
    * Set this db component to an unchanged state.  Call this method after
    * creating a component from the database if needed.  Override this method 
    * in components which are responsible for propagating this situation to 
    * sub-components.
    */
   void setUnchanged()
   {
      m_componentState = 
         IPSDatabaseComponent.DATABASE_COMPONENT_UNCHANGED;
   }
   
   /**
    * Instantiate the values in this database component from the xml
    * element returned by the query resource associated with this database
    * component type.  Will attempt to utilize fromXml() directly for the
    * default case.  Any database elements which require relation information
    * and sub components must override this method and use the database
    * component loader to retrieve it's relationships and actualize its
    * children.  Additionally they must set their {@link #m_componentState} to
    * {@link #DATABASE_COMPONENT_UNCHANGED}.  
    *
    * @see IPSDatabaseComponent#fromDatabaseXml(Element, 
    * PSDatabaseComponentLoader, PSRelation) for the interface definition.
    */
   public void fromDatabaseXml(Element e, PSDatabaseComponentLoader cl,
      PSRelation relationContext)
      throws   PSUnknownNodeTypeException,
               PSDatabaseComponentException
   {
      if (e == null)
         throw new IllegalArgumentException("Element must be supplied.");

      if (cl == null)
         throw new IllegalArgumentException(
            "Database component loader must be supplied.");

      // The default algorithm is to just use fromXml
      fromXml(e, null, null);

      // set state to unchanged
      m_componentState = DATABASE_COMPONENT_UNCHANGED;
   }

   /**
    * Adds this component's db state and database id to the supplied element.
    * Does so by adding an attribute for each to the supplied element.  See
    * {@link #XML_COMPONENT_STATE_ATTR} and 
    * {@link #DATABASE_COMPONENT_ID_XML_ATTR_NAME}
    * for the attribute name used.  Convenience method used by derived classes
    * in their <code>toXml</code> methods.
    *
    * @param e The element to which the state is added.  May not be <code>null
    * </code>.
    *
    * @throws IllegalArgumentException if e is <code>null</code>.
    */
   protected void addComponentState(Element e)
   {
      if (e == null)
         throw new IllegalArgumentException("Element may not be null.");
         
      e.setAttribute(XML_COMPONENT_STATE_ATTR, String.valueOf(
         m_componentState));
      e.setAttribute(DATABASE_COMPONENT_ID_XML_ATTR_NAME, String.valueOf(
         m_databaseComponentId));
   }

   /**
    * Constructs a component from a back end database.
    *
    * @param c The class of the IPSDatabaseComponent to create.
    * May not be <code>null</code>.  Must be assignable from 
    * IPSDatabaseComponent.
    *
    * @param e The back-end database xml element to instantiate with.
    * May not be <code>null</code>.
    *
    * @param rc The relation context information.
    * May not be <code>null</code>.
    *
    * @param cl The component loader to use for any needed related content.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If any argument is invalid.
    *
    * @throws PSDatabaseComponentException If there are any problems
    * loading the component
    *
    * @throws PSUnkownNodeTypeException if the component can not instantiate
    * itself from the supplied XML
    */
   public static IPSDatabaseComponent newComponentInstance(
      Class c, Element e, PSRelation rc, PSDatabaseComponentLoader cl)
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      if (c == null)
         throw new IllegalArgumentException(
            "Database component class must be supplied.");

      if (e == null)
         throw new IllegalArgumentException(
            "Database xml element must be supplied.");

      if (cl == null)
         throw new IllegalArgumentException(
            "Databse component loader must be supplied.");

      if (!IPSDatabaseComponent.class.isAssignableFrom(c))
         throw new IllegalArgumentException(
            "Class supplied must implement IPSDatabaseComponent.");

      IPSDatabaseComponent component = null;;
      
      try 
      {
         component = (IPSDatabaseComponent) c.newInstance();
      } catch (Exception ex)
      {
         Object[] args = {c.getName(), ex.toString()};
         throw new PSDatabaseComponentException(
            IPSObjectStoreErrors.DB_COMPONENT_LOAD_EXCEPTION, args);
      }

      component.fromDatabaseXml(e, cl, rc);

      return component;
   }
   
   /**
    * Gets this component's db state from the supplied element and stores is
    * Does so by checking the attribute in the supplied element.  If it is not
    * found, then the state is set to {@link #DATABASE_COMPONENT_UNCHANGED}.
    * See {@link #XML_COMPONENT_STATE_ATTR} for the attribute name used.  Also
    * retrieves and sets this component's database id.  See {@link
    * #DATABASE_COMPONENT_ID_XML_ATTR_NAME} for the attribute name used.  
    * Convenience
    * method used by derived classes in their <code>fromXml</code> methods, and
    * should always be used in conjuction with {@link #addComponentState} in
    * their <code>toXml</code> methods.
    *
    * @param e The element from which the state is retrieved.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if e is <code>null</code>.
    * @throws PSUnknownNodeTypeException if the provided element does not
    * contain the expected attributes.
    */
   protected void getComponentState(Element e)
      throws PSUnknownNodeTypeException
   {
      if (e == null)
         throw new IllegalArgumentException("Element may not be null");

      /* get the component state, if not found, assume this xml is from the
       * database and set our state to unchanged
       */
      String sTemp = e.getAttribute(XML_COMPONENT_STATE_ATTR);
      if (sTemp == null || sTemp.trim().length() == 0)
         m_componentState = DATABASE_COMPONENT_UNCHANGED;
      else
      {
         try
         {
            m_componentState = Integer.parseInt(sTemp);
         }
         catch (NumberFormatException ex)
         {
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
               XML_COMPONENT_STATE_ATTR);
         }
      }

      // get the db component id
      sTemp = e.getAttribute(DATABASE_COMPONENT_ID_XML_ATTR_NAME);
      if (sTemp == null || sTemp.trim().length() == 0)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
            DATABASE_COMPONENT_ID_XML_ATTR_NAME);
      }

      try
      {
         m_databaseComponentId = Integer.parseInt(sTemp);
      }
      catch (Exception ex)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
            DATABASE_COMPONENT_ID_XML_ATTR_NAME);
      }


   }

   /**
    * Returns the database component id from the specified xml element.
    *
    * @param dbComponentElement The xml element representing a database
    * component.  May not be <code>null</code>
    *
    * @return the database component id as a string, never <code>null</code>
    * or empty
    *
    * @throws PSUnknownNodeTypeException if the element does not contain
    *    a database component id attribute.
    *
    * @throws IllegalArgumentException if dbCopmponentElement is 
    * <code>null</code>
    */
   public static String getDatabaseComponentId(Element dbComponentElement)
      throws PSUnknownNodeTypeException
   {
      String dbId = dbComponentElement.getAttribute(
         DATABASE_COMPONENT_ID_XML_ATTR_NAME);

      if ((dbId == null) || (dbId.length() == 0))
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR,
            DATABASE_COMPONENT_ID_XML_ATTR_NAME);

      return dbId;
   }

   /**
    * Generates a new component id for this component and uses it to set the
    * value of {@link #m_databaseComponentId}.  This must only be called once
    * for each new component!
    */
   protected void createDBComponentId() throws PSDatabaseComponentException
   {
      try
      {
         m_databaseComponentId = PSIdGenerator.getNextId(
               getComponentType());
      }
      catch(SQLException e)
      {
         throw new PSDatabaseComponentException(
            IPSObjectStoreErrors.DB_COMPONENT_NEW_ID,
            PSSqlException.getFormattedExceptionText(e));
      }
   }


   /**
    * Returns the identifier for this object. By convention and default, the
    * base class name less the leading 'PS' is used for this identifier. For
    * example, if this class could be instantiated, it would return
    * 'DatabaseComponent'. It wasn't made static so it could be overridden,
    * but it is effectively static.
    *
    * @return A unique name for this object among all objects derived from
    *    this class, never <code>null</code> or empty.
    */
   public String getComponentType()
   {
      String name = getClass().getName();
      String base = name.substring(name.lastIndexOf('.')+1);
      if ( base.startsWith("PS"))
         base = base.substring(2);
      return base;
   }


   /**
    * This default ctor is used purely to validate that the derived class
    * has properly implemented the {@link #getComponentType} method.
    *
    * @throws IllegalStateException If the derived object has not properly
    *    implemented the {@link #getComponentType()} method.
    */
   protected PSDatabaseComponent()
   {
      String type = getComponentType();
      if (null == type || type.trim().length() == 0 )
         throw new IllegalStateException(
               "Derived object has not properly implemented getComponentType" );
   }

   // IPSComponent implementation

   // see IPSComponent interface
   public int getId()
   {
      return m_id;
   }

   // see IPSComponent interface
   public void setId(int id)
   {
      m_id = id;
   }

   // see IPSComponent interface
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      // no op
   }

   /**      
    * The component state, initially <code>DATABASE_COMPONENT_INSERTED</code>
    * which is the default for any administrative calls.  Components which are
    * loading themselves from the back end table should set this to 
    * <code>DATABASE_COMPONENT_UNCHANGED</code> at construction time.
    */
   protected int m_componentState = DATABASE_COMPONENT_INSERTED;

   /**
    * Each database component has an Id associated with it, 
    * initialized to -1, set when allocated for DB storage by a call to
    * {@link #createDBComponentId()} (for new components) or when loaded from 
    * the database (for existing components).  This value is used as the key
    * for this component in the back end database and any relationships it has
    * with super- or sub- components.
    */
   protected int m_databaseComponentId = -1;
   
   /**
    * The id assigned to this component, used by the IPSComponent
    * implementation.
    */
   protected int m_id = 0;

   /**
    * Attribute used to serialize the IPSComponent id
    */
   protected static final String ID_ATTR = "id";

   /**
    * Attribute used to serialize {@link #m_databaseComponentId}.
    */
   private static final String XML_COMPONENT_STATE_ATTR = "componentState";

   /**
    * Name for element for all database modification actions.
    */
   private static final String XML_DBACTIONELEMENT_NAME = "Action";

   /**
    * Attribute name for action element's modification type 
    * (insert, update, or delete).
    */
   private static final String XML_DBACTIONELEMENT_TYPE_ATTRIBUTE_NAME = "type";

}


