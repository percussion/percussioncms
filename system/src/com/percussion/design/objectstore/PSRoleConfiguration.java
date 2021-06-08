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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains the various DB components required for role
 * administration and management.  It implements IPSDocument so that it
 * can be sent to and retrieved from the administrator (and administration
 * applications i.e. PSRoleSynchronizer) by the server.
 */
public class PSRoleConfiguration implements IPSDocument
{
   private static final Logger log = LogManager.getLogger(PSRoleConfiguration.class);
   /**
    * Empty ctor (for fromXml(), fromDb())
    */
   public PSRoleConfiguration()
   {
      PSGlobalSubject subj = new PSGlobalSubject();
      m_subjects = new PSDatabaseComponentCollection(
         subj.getClass(), subj.getDatabaseAppQueryDatasetName());

      PSRole role = new PSRole();
      m_roles = new PSDatabaseComponentCollection(
         role.getClass(), role.getDatabaseAppQueryDatasetName());
   }

   /**
    * Instantiates the role configuration from the specified xml
    * document.
    *
    * The format of the role configuration object is as follows:<p>
    * <pre><code>
    * &lt;!ELEMENT PSXRoleConfiguration(PSXDatabaseComponentCollection*,
    *  PSXDatabaseComponentCollection*)&gt;
    * </pre></code>
    *
    * @param xmlDef The document containing the RoleConfiguration
    * definition.  May not be <code>null</code>.
    *
    * @throws PSUnknownDocTypeException if the document type is invalid.
    *
    * @throws PSUnknownNodeTypeException if any component in the document
    * is invalid.
    *
    * @throws IllegalArgumentException if xmlDef is <code>null</code>.
    */
   public PSRoleConfiguration( Document xmlDef )
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      this();
      if (xmlDef == null)
         throw new IllegalArgumentException("xmlDef must be supplied.");
      fromXml(xmlDef);
   }

   /**
    * Instantiates the role configuration from the specified database
    * component loader.
    *
    * @param loader The loader to retrieve the RoleConfiguration
    * components from.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if loader is <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if any component retrieved from
    * the loader is invalid.
    *
    * @throws PSDatabaseComponentException if any database component errors
    * occur.
    */
   public PSRoleConfiguration( PSDatabaseComponentLoader loader )
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      this();
      if (loader == null)
         throw new IllegalArgumentException("Loader must be supplied.");
      fromDb(loader);
   }

   /**
    * Initialize this role configuration using the component loader.
    *
    * @param loader The component loader.  May not be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if any component retrieved from
    * the loader is invalid.
    *
    * @throws PSDatabaseComponentException if any database component errors
    * occur.
    *
    * @throws IllegalArgumentException If loader is <code>null</code>.
    */
   public void fromDb(PSDatabaseComponentLoader loader)
      throws PSDatabaseComponentException, PSUnknownNodeTypeException
   {
      if (loader == null)
         throw new IllegalArgumentException("Loader must be supplied.");

      loader.actualizeCollectionComponent(m_subjects);
      loader.actualizeCollectionComponent(m_roles);
   }

   // see interface
   public void fromXml(Document sourceDoc)
      throws PSUnknownDocTypeException ,PSUnknownNodeTypeException
   {
      if (null == sourceDoc)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      Element root = sourceDoc.getDocumentElement();
      if (root == null)
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      //make sure we got the correct root node tag
      if (false == ms_NodeType.equals (root.getNodeName()))
      {
         Object[] args = { ms_NodeType, root.getNodeName() };
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      //Read PSXRoleConfiguration object attributes
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceDoc);
      String sTemp = null;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element e = null;
      e = tree.getNextElement(m_subjects.ms_NodeType, firstFlags);
      if ( null == e )
      {
         Object[] args = { m_subjects.ms_NodeType, "null", "''" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_subjects.fromXml(e, null, null);

      e = tree.getNextElement(m_roles.ms_NodeType, nextFlags);
      if ( null == e )
      {
         Object[] args = { m_roles.ms_NodeType, "null", "''" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_roles.fromXml(e, null, null);
   }

   // see base class
   public Document toXml()
   {
      Document doc   = PSXmlDocumentBuilder.createXmlDocument();

      Element  root  = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);

      if (m_roles != null)
      {
         // update the global subjects
         updateGlobalSubjects();

         root.appendChild(m_subjects.toXml(doc));
         root.appendChild(m_roles.toXml(doc));
      }

      return doc;
   }

   /**
    * Creates an XML document which contains database XML insert, update,
    * and delete requests for any modified components.  This method should only
    * be called from the server (not a client) as it may need access to the
    * server database to generate id numbers.
    *
    * @return The document, never <code>null</code>.
    *
    * @throws PSDatabaseComponentException if any database component errors
    * occur.
    */
   public Document toDbXml()
      throws PSDatabaseComponentException
   {
      Document doc   = PSXmlDocumentBuilder.createXmlDocument();

      Element  root  = PSXmlDocumentBuilder.createRoot(doc, ms_NodeType);

      if (m_roles != null)
      {
         // update the global subjects
         updateGlobalSubjects();

         m_subjects.toDatabaseXml(doc, root, new PSRelation());

         // Update relative subjects with the ids of the global subjects that
         // they correspond to
         updateRelativeSubjects();

         m_roles.toDatabaseXml(doc, root, new PSRelation());
      }

      return doc;
   }

   /**
    * Clean up any global subjects that don't have corresponding role subjects
    * and add global subjects for related subjects which have no corresponding
    * global subjects.
    */
   private void updateGlobalSubjects()
   {
      // Delete any unneeded global subjects
      // Run backwards, since removing elements bumps the indexes!
      for (int i = m_subjects.size() - 1; i>=0; i--)
      {
         PSSubject sub = (PSSubject) m_subjects.get(i);
         boolean found = false;

         // check the roles for this subject
         for (int j = 0; j < m_roles.size(); j++)
         {
            PSRole role = (PSRole) m_roles.get(j);
            if (role.containsCorrespondingSubject(sub))
            {
               found = true;
               break;
            }
         }

         // Subject is in no roles - remove it
         if (!found)
            m_subjects.removeElementAt(i);
      }

      // add any needed global subjects
      for (int i = 0; i < m_roles.size(); i++)
      {
         PSDatabaseComponentCollection roleSubjects =
            ((PSRole) m_roles.get(i)).getSubjects();
         for (int j = 0; j < roleSubjects.size(); j++)
         {
            PSRelativeSubject rel = (PSRelativeSubject) roleSubjects.get(j);
            getGlobalSubject(rel, true);
         }
      }
   }

   /**
    * Get the global subject which matches the supplied relative subject.
    *
    * @param the relative subject, assumed not <code>null</code>
    *
    * @return the global subject, never <code>null</code>
    *
    * @see PSSubject#isMatch(PSSubject)
    */
   private PSGlobalSubject getCorrespondingGlobalSubject(PSRelativeSubject sub)
   {
      for (Iterator i = m_subjects.iterator(); i.hasNext();)
      {
         PSGlobalSubject gSub = (PSGlobalSubject) i.next();
         if (gSub.isMatch(sub))
            return gSub;
      }
      for (Iterator i = m_subjects.deletes(); i.hasNext();)
      {
         PSGlobalSubject gSub = (PSGlobalSubject) i.next();
         if (gSub.isMatch(sub))
            return gSub;
      }

      // This would be a most heinous situation, so this is truly a runtime
      // exception  (could never happen during normal operations) - there is
      // no recovery possible!  ph: throw a RuntimeException
      throw new NullPointerException("No matching global subject found for " +
         sub.getName());
   }

   /**
    * Add the database component Ids to the subjects in the specified role,
    * based on their corresponding global subject's id.
    *
    * @param the role to traverse, assumed not <code>null</code>
    *
    * @see #getCorrespondingGlobalSubject(PSRelativeSubject)
    */
   private void addRoleSubjectDbIds(PSRole role)
   {
      for (Iterator i = role.getSubjects().iterator(); i.hasNext();)
      {
         PSRelativeSubject sub = (PSRelativeSubject) i.next();
         sub.setDbId(getCorrespondingGlobalSubject(sub).getDatabaseComponentId());
      }
      for (Iterator i = role.getSubjects().deletes(); i.hasNext();)
      {
         PSRelativeSubject sub = (PSRelativeSubject) i.next();
         sub.setDbId(getCorrespondingGlobalSubject(sub).getDatabaseComponentId());
      }
   }

   /**
    * Add database component Id's to the relative subjects, they need this
    * information to correctly to update their subcomponents.
    *
    * @see #addRoleSubjectDbIds(PSRole)
    */
   private void updateRelativeSubjects()
   {
      /* Traverse the roles and deleted roles and add database component
         ids to the role subjects from the corresponding global subjects */
      for (Iterator i = m_roles.iterator(); i.hasNext();)
      {
         addRoleSubjectDbIds((PSRole) i.next());
      }
      for (Iterator i = m_roles.deletes(); i.hasNext();)
      {
         addRoleSubjectDbIds((PSRole) i.next());
      }
   }

   /**
    * Gets the global subjects. It is not possible to add or remove global
    * subjects through the returned iterator, as clients should not manipulate
    * global subjects directly.
    * 
    * If you are looking for a subject you've just added to a role so you
    * can add global attributes to it, use {@link 
    * #getGlobalSubject(PSRelativeSubject,boolean) 
    * getGlobalSubject(newSubject, true)} instead.
    *
    * @return a protected iterator (no remove) containing the global subjects
    * (<code>PSGlobalSubject</code> objects), never <code>null</code>.
    */
   public Iterator getSubjects()
   {
      return m_subjects.iterator();
   }

   /**
    * Convenience method for accessing a single subject of the server.
    * Any modifications made to the returned subject will affect the server's
    * subject.
    *
    * @param roleSubj The role member subject to lookup the server
    *                   entry with.  Can't be <code>null</code>.
    *
    * @param createsubject If <code>true</code> a new subject will be
    *    created with no attributes and returned if no corresponding
    *    subject is found in the server collection.
    *
    * @return The server subject meeting the specified criteria, or
    *    <code>null</code> if a corresponding subject does not exist
    *    on the server and <code>createsubject</code> is <code>false</code>.
    *
    * @throws IllegalArgumentException if any argument is invalid.
    */
   public synchronized PSGlobalSubject getGlobalSubject(
                                                PSRelativeSubject roleSubj,
                                                boolean createsubject)
   {
      if (roleSubj == null)
      {
         throw new IllegalArgumentException(
            "Subject must be specified for lookup.");
      }

      PSGlobalSubject subject = null;

      for (int i = 0; i < m_subjects.size(); i++)
      {
         PSGlobalSubject sub = (PSGlobalSubject)m_subjects.get(i);
         if (sub.isMatch(roleSubj))
         {
            subject = sub;
            break;
         }
      }

      if ((subject == null) && (createsubject))
      {
         subject = roleSubj.makeGlobalSubject();
         m_subjects.add(subject);
      }

      return subject;
   }

   /**
    * Get the  roles defined for use by this role configuration.
    *
    * @return a database component collection containing the roles defined for
    *    this role configuration (PSRole objects), never <code>null</code>
    */
   public PSDatabaseComponentCollection getRoles()
   {
      return m_roles;
   }

   /**
    * The roles of this role configuration.  Never <code>null</code>
    * after construction.
    */
   private   PSDatabaseComponentCollection m_roles = null;

   /**
    * The global subjects of this role configuration.  Never <code>null</code>
    * after construction.
    */
   private   PSDatabaseComponentCollection m_subjects = null;

   /* package access on this so they may reference each other in fromXml */
   static final String      ms_NodeType = "PSXRoleConfiguration";

   /** -- Test Scenario -- **/
   public static void main(String[] args)
   {
      java.io.Writer w = null;
      java.io.Reader r = null;

      try
      {
         r = new java.io.FileReader("RoleConfigXmlIn.xml");
         Document d = PSXmlDocumentBuilder.createXmlDocument(r, false);
         r.close(); r = null;

         PSRoleConfiguration rcfg = new PSRoleConfiguration(d);
         w = new java.io.FileWriter("RoleConfigXmlOut.xml");
         PSXmlDocumentBuilder.write(rcfg.toXml(), w);
         w.close(); w = null;


//         PSRoleConfiguration rcfg = new PSRoleConfiguration();
//         if (args.length > 0)
//         {
//         //         File f = new File(args[0]);
//         //         Get from file not currently supported
//         }
//         PSDatabaseComponentCollection roles = rcfg.getRoles();
//         PSDatabaseComponentCollection subjects = rcfg.getSubjects();
//
//         PSRole role = new PSRole("rolesToBeKept");
//         PSRelativeSubject relSub = new PSRelativeSubject("fred",
//            PSSubject.SUBJECT_TYPE_USER,
//            com.percussion.security.PSSecurityProvider.SP_TYPE_ANY, "", null);
//         // Create a role named roleToBeKept
//         roles.add(role);
//         role.add(relSub);
//         PSAttributeList l = role.getAttributes();
//         PSAttribute att = new PSAttribute("Att1");
//         ArrayList values = new ArrayList();
//         values.add("value1");
//         values.add("value2");
//         values.add("value3");
//         att.setValues(values);
//         l.add(att);
//
//         w = new java.io.FileWriter("RoleConfigXmlOut.xml");
//
//         PSXmlDocumentBuilder.write(rcfg.toXml(), w);
//         w.close();
//         w = null;
//         w = new java.io.FileWriter("RoleConfigDbOut.xml");
//         PSXmlDocumentBuilder.write(rcfg.toDbXml(), w);
      } catch (Throwable t)
      {
         log.error("Error : {}", t.getMessage());
         log.error(t.getMessage());
         log.debug(t.getMessage(), t);
      } finally
      {
         try {
            if (w != null)
               w.close();
         } catch (java.io.IOException e) { };
         try {
            if (r != null)
               r.close();
         } catch (java.io.IOException e) { };
      }

   }
}
