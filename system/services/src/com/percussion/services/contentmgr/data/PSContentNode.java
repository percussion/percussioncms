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
package com.percussion.services.contentmgr.data;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.services.contentmgr.*;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.contentmgr.impl.legacy.PSContentPropertyLoader;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.*;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import java.io.InputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Implementation of the content node, which provides most of the functionality
 * of a node. Some methods are missing. See the JSR-170 docs for details.
 * 
 * @author dougrand
 */
@SuppressWarnings("unused")
public class PSContentNode implements IPSNode, IPSJcrCacheItem, Serializable
{
   /**
    * Serial number for serialization
    */
   private static final long serialVersionUID = -3275632991426047917L;

   /**
    * Interfaces to ignore when "figuring out" the body access class argument
    * classes
    */
   private static Set<Class> ms_ignoredClasses = new HashSet<Class>();

   static
   {
      ms_ignoredClasses.add(Serializable.class);
      ms_ignoredClasses.add(Comparable.class);
   }

   /**
    * Holds properties
    */
   private Map<String, Property> m_properties = new HashMap<String, Property>();

   /**
    * Holds children. Children are held in named collections, implemented by the
    * multimap
    */
   private MultiMap m_children = new MultiHashMap();

   /**
    * Remembers if children have been loaded
    */
   private boolean m_childrenLoaded = false;

   /**
    * Holds the depth, initialized in the ctor and never modified after
    */
   private int m_depth = 0;

   /**
    * Holds the index once calculated. Calculated in {@link #getIndex()} on
    * demand
    */
   private int m_index = -1;

   /**
    * The parent node, initialized in the ctor, read only afterward
    */
   private Node m_parent = null;

   /**
    * The name of this node, initialized in the ctor
    */
   private String m_name = null;

   /**
    * The GUID, initialized in the ctor, may be <code>null</code> for objects
    * that are not mapped to the database. This is mostly true of test objects.
    */
   private IPSGuid m_guid = null;

   /**
    * The configuration for this node, only <code>null</code> after
    * construction for test objects
    */
   private PSTypeConfiguration m_config;

   /**
    * The content manager configuration used to load this node, may be
    * <code>null</code>
    */
   private PSContentMgrConfig m_cmgrConfig;

   /**
    * The component summary for this node. More than one node may reference a
    * given component summary, but all such nodes would be part of the same
    * content item, just difference revisions. Child nodes will not have a
    * summary.
    */
   private PSComponentSummary m_summary = null;

   /**
    * The generated object is held by this reference. The actual class is
    * created in {@link PSTypeConfiguration}
    */
   private PSTypeConfiguration.GeneratedClassBase m_instance;

   /**
    * If this class contains lazy load data, this field will contain a reference
    * to the lazy loader. This is used to set the loaded data from the content
    * repository.
    */
   private PSContentPropertyLoader m_lazyLoader;
   
   
   /**
    * Id used internally
    */
   public static final String ID_PROPERTY_NAME = "sys_id";

   /**
    * Ctor
    * 
    * @param guid the guid, may be <code>null</code>
    * @param name the name, never <code>null</code> or empty
    * @param parent the parent, may be <code>null</code>
    * @param config configuration that was used when creating this node, may be
    *           <code>null</code> for test objects
    * @param summary the component summary, may be <code>null</code>
    * @param instance the object instance that holds the item data, may be
    *           <code>null</code> to the ctor
    * @throws RepositoryException
    */
   public PSContentNode(IPSGuid guid, String name, Node parent,
         PSTypeConfiguration config, PSComponentSummary summary,
         PSTypeConfiguration.GeneratedClassBase instance)
         throws RepositoryException {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }
      m_guid = guid;
      m_name = name;
      m_config = config;
      m_summary = summary;
      m_instance = instance;
      setParent(parent);
   }

   /**
    * @return Returns the instance.
    */
   public PSTypeConfiguration.GeneratedClassBase getInstance()
   {
      return m_instance;
   }

   /**
    * @param instance The instance to set.
    */
   public void setInstance(PSTypeConfiguration.GeneratedClassBase instance)
   {
      m_instance = instance;
   }

   /**
    * @return Returns the summary.
    */
   public PSComponentSummary getSummary()
   {
      return m_summary;
   }

   /**
    * @param summary The summary to set.
    */
   public void setSummary(PSComponentSummary summary)
   {
      m_summary = summary;
   }

   /**
    * Set the parent, only used in node tree construction
    * 
    * @param parent the parent, may be <code>null</code>
    * @throws RepositoryException if there's a problem requesting the depth from
    *            the non-<code>null</code> parent.
    */
   public void setParent(Node parent) throws RepositoryException
   {
      m_parent = parent;
      if (parent != null)
      {
         m_depth = parent.getDepth() + 1;
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#addNode(java.lang.String)
    */
   public Node addNode(String name) throws ItemExistsException,
         PathNotFoundException, VersionException, ConstraintViolationException,
         LockException, RepositoryException
   {
      PSContentNode n = new PSContentNode(null, name, this, null, null, null);
      m_children.put(name, n);
      n.setParent(this);
      return n;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#addNode(java.lang.String, java.lang.String)
    */
   public Node addNode(String arg0, String arg1) throws ItemExistsException,
         PathNotFoundException, NoSuchNodeTypeException, LockException,
         VersionException, ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#orderBefore(java.lang.String, java.lang.String)
    */
   public void orderBefore(String arg0, String arg1)
         throws UnsupportedRepositoryOperationException, VersionException,
         ConstraintViolationException, ItemNotFoundException, LockException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();

   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value)
    */
   public Property setProperty(String arg0, Value arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value, int)
    */
   public Property setProperty(String arg0, Value arg1, int arg2)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[])
    */
   public Property setProperty(String arg0, Value[] arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Value[], int)
    */
   public Property setProperty(String arg0, Value[] arg1, int arg2)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[])
    */
   public Property setProperty(String arg0, String[] arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String[], int)
    */
   public Property setProperty(String arg0, String[] arg1, int arg2)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String)
    */
   public Property setProperty(String arg0, String arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.lang.String, int)
    */
   public Property setProperty(String arg0, String arg1, int arg2)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.io.InputStream)
    */
   public Property setProperty(String arg0, InputStream arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, boolean)
    */
   public Property setProperty(String arg0, boolean arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, double)
    */
   public Property setProperty(String arg0, double arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, long)
    */
   public Property setProperty(String arg0, long arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, java.util.Calendar)
    */
   public Property setProperty(String arg0, Calendar arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#setProperty(java.lang.String, javax.jcr.Node)
    */
   public Property setProperty(String arg0, Node arg1)
         throws ValueFormatException, VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new LockException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getNode(java.lang.String)
    */
   public Node getNode(String childPath) throws PathNotFoundException,
         RepositoryException
   {
      assureChildrenLoaded();

      PSPath path = new PSPath(childPath);
      if (path.isRelative() == false)
      {
         throw new PathNotFoundException("Child path must be relative");
      }
      int index = path.getIndex(0);
      String name = path.getName(0);
      List children = (List) m_children.get(name);
      if (children == null)
      {
         throw new PathNotFoundException("Child " + name + " was not found");
      }
      if (index < 0)
         index = 0;
      if (index >= children.size())
      {
         throw new PathNotFoundException("Index out of range: " + index);
      }
      Node specificChild = (Node) children.get(index);
      if (path.getCount() > 1)
      {
         PSPath rest = path.getRest();
         return specificChild.getNode(rest.toString());
      }
      else
      {
         return specificChild;
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getNodes()
    */
   public NodeIterator getNodes() throws RepositoryException
   {
      assureChildrenLoaded();

      return new PSNodeIterator(m_children, null);
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getNodes(java.lang.String)
    */
   public NodeIterator getNodes(String filter) throws RepositoryException
   {
      assureChildrenLoaded();

      return new PSNodeIterator(m_children, filter);
   }

   /**
    * Make sure the children are loaded. Track the load state so this method
    * is a noop if called multiple times for the same object.
    * 
    * @throws RepositoryException
    */
   private void assureChildrenLoaded() throws RepositoryException
   {
      // Flag is set in loadChildren
      if (!m_childrenLoaded)
      {
         IPSContentRepository rep = PSContentInternalLocator
               .getLegacyRepository();
         List<Node> nodes = new ArrayList<Node>();
         nodes.add(this);
         rep.loadChildren(nodes, m_cmgrConfig);
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getProperty(java.lang.String)
    */
   public Property getProperty(String name) throws PathNotFoundException,
         RepositoryException
   {
      if (StringUtils.isBlank(name))
      {
         throw new IllegalArgumentException("name may not be null or empty");
      }

      PSPath path = new PSPath(name);
      if (path.getCount() == 1)
      {
         if (name.indexOf(':') < 0)
            name = "rx:" + name;

         IPSProperty p = (IPSProperty) m_properties.get(name);
         if (p == null)
         {
            throw new PathNotFoundException("Property " + name + " not found");
         }         
         return p;
      }
      else
      {
         Node n = getNode(path.getAllButLast().toString());
         return n.getProperty(path.getLastName());
      }
   }
   
   public List<String> getPropertyStringValues(String name) throws PathNotFoundException, RepositoryException
   {
      Property p = getProperty(name);
      Set<String> result = new HashSet<String>();
      for (Value v : p.getValues())
         result.add(v.getString());
      
      List<String> list = new ArrayList<String>(result);
      Collections.sort(list);
      return list;
   }
   

   /** (non-Javadoc)
    * @see javax.jcr.Node#getProperties()
    */
   public PropertyIterator getProperties() throws RepositoryException
   {
      return new PSPropertyIterator(m_properties, null);
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getProperties(java.lang.String)
    */
   public PropertyIterator getProperties(String filterpattern)
         throws RepositoryException
   {
      return new PSPropertyIterator(m_properties, filterpattern);
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getPrimaryItem()
    */
   public Item getPrimaryItem() throws ItemNotFoundException,
         RepositoryException
   {
      // Return property whose name is body, or the first body field
      Item primary = m_properties.get("rx:body");
      if (primary == null)
      {
         primary = m_properties.get(m_config.getBodyProperties().iterator()
               .next());
      }
      if (primary instanceof IPSProperty && ((IPSProperty) primary).isNull())
      {
         throw new PathNotFoundException("Primary item not found");
      }
      
      return primary;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getUUID()
    */
   public String getUUID() throws UnsupportedRepositoryOperationException,
         RepositoryException
   {
      if (m_guid != null)
      {
         return Long.toString(((PSLegacyGuid) m_guid).getContentId());
      }
      else
      {
         return "";
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getIndex()
    */
   public int getIndex() throws RepositoryException
   {
      if (m_index > -1)
         return m_index;

      if (m_parent == null)
      {
         m_index = 0;
      }
      else if (m_parent instanceof PSContentNode)
      {
         PSContentNode parent = (PSContentNode) m_parent;
         List children = (List) parent.m_children.get(getName());
         if (children != null)
         {
            m_index = children.indexOf(this);
         }
         else
         {
            m_index = 0;
         }
      }
      return m_index;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getReferences()
    */
   public PropertyIterator getReferences() throws RepositoryException
   {
      return null;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#hasNode(java.lang.String)
    */
   public boolean hasNode(String path) throws RepositoryException
   {
      return getNode(path) != null;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#hasProperty(java.lang.String)
    */
   public boolean hasProperty(String arg0) throws RepositoryException
   {
      try
      {
         IPSProperty p = (IPSProperty) getProperty(arg0);
         return !( p == null || p.isNull());
      }
      catch (PathNotFoundException e)
      {
         return false;
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#hasNodes()
    */
   public boolean hasNodes() throws RepositoryException
   {
      return !m_children.isEmpty();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#hasProperties()
    */
   public boolean hasProperties() throws RepositoryException
   {
      return !m_properties.isEmpty();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getPrimaryNodeType()
    */
   public NodeType getPrimaryNodeType() throws RepositoryException
   {
      return m_config;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getMixinNodeTypes()
    */
   public NodeType[] getMixinNodeTypes() throws RepositoryException
   {
      return null;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#isNodeType(java.lang.String)
    */
   public boolean isNodeType(String arg0) throws RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#addMixin(java.lang.String)
    */
   public void addMixin(String arg0) throws NoSuchNodeTypeException,
         VersionException, ConstraintViolationException, LockException,
         RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#removeMixin(java.lang.String)
    */
   public void removeMixin(String arg0) throws NoSuchNodeTypeException,
         VersionException, ConstraintViolationException, LockException,
         RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#canAddMixin(java.lang.String)
    */
   public boolean canAddMixin(String arg0) throws NoSuchNodeTypeException,
         RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getDefinition()
    */
   public NodeDefinition getDefinition() throws RepositoryException
   {
      IPSContentMgr mgr = PSContentMgrLocator.getContentMgr();
      List<IPSNodeDefinition> defs = 
         mgr.findNodeDefinitionsByName(m_config.getType());
      if (defs.size() == 0)
      {
         throw new RepositoryException("Could not find def info");
      }
      return defs.get(0);
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#checkin()
    */
   public Version checkin() throws VersionException,
         UnsupportedRepositoryOperationException, InvalidItemStateException,
         LockException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#checkout()
    */
   public void checkout() throws UnsupportedRepositoryOperationException,
         LockException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#doneMerge(javax.jcr.version.Version)
    */
   public void doneMerge(Version arg0) throws VersionException,
         InvalidItemStateException, UnsupportedRepositoryOperationException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#cancelMerge(javax.jcr.version.Version)
    */
   public void cancelMerge(Version arg0) throws VersionException,
         InvalidItemStateException, UnsupportedRepositoryOperationException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#update(java.lang.String)
    */
   public void update(String arg0) throws NoSuchWorkspaceException,
         AccessDeniedException, LockException, InvalidItemStateException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#merge(java.lang.String, boolean)
    */
   public NodeIterator merge(String arg0, boolean arg1)
         throws NoSuchWorkspaceException, AccessDeniedException,
         MergeException, LockException, InvalidItemStateException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getCorrespondingNodePath(java.lang.String)
    */
   public String getCorrespondingNodePath(String arg0)
         throws ItemNotFoundException, NoSuchWorkspaceException,
         AccessDeniedException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#isCheckedOut()
    */
   public boolean isCheckedOut() throws RepositoryException
   {
      return m_summary.getCheckoutUserName() != null;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#restore(java.lang.String, boolean)
    */
   public void restore(String arg0, boolean arg1) throws VersionException,
         ItemExistsException, UnsupportedRepositoryOperationException,
         LockException, InvalidItemStateException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#restore(javax.jcr.version.Version, boolean)
    */
   public void restore(Version arg0, boolean arg1) throws VersionException,
         ItemExistsException, UnsupportedRepositoryOperationException,
         LockException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#restore(javax.jcr.version.Version, java.lang.String, boolean)
    */
   public void restore(Version arg0, String arg1, boolean arg2)
         throws PathNotFoundException, ItemExistsException, VersionException,
         ConstraintViolationException, UnsupportedRepositoryOperationException,
         LockException, InvalidItemStateException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#restoreByLabel(java.lang.String, boolean)
    */
   public void restoreByLabel(String arg0, boolean arg1)
         throws VersionException, ItemExistsException,
         UnsupportedRepositoryOperationException, LockException,
         InvalidItemStateException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getVersionHistory()
    */
   public VersionHistory getVersionHistory()
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getBaseVersion()
    */
   public Version getBaseVersion()
         throws UnsupportedRepositoryOperationException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#lock(boolean, boolean)
    */
   public Lock lock(boolean arg0, boolean arg1)
         throws UnsupportedRepositoryOperationException, LockException,
         AccessDeniedException, InvalidItemStateException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#getLock()
    */
   public Lock getLock() throws UnsupportedRepositoryOperationException,
         LockException, AccessDeniedException, RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#unlock()
    */
   public void unlock() throws UnsupportedRepositoryOperationException,
         LockException, AccessDeniedException, InvalidItemStateException,
         RepositoryException
   {
      throw new UnsupportedRepositoryOperationException();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#holdsLock()
    */
   public boolean holdsLock() throws RepositoryException
   {
      return false;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Node#isLocked()
    */
   public boolean isLocked() throws RepositoryException
   {
      return false;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getPath()
    */
   public String getPath() throws RepositoryException
   {
      StringBuilder b = new StringBuilder();
      if (m_parent != null)
      {
         b.append(m_parent.getPath());
      }
      b.append("/");

      b.append(getName());
      if (getIndex() > 0)
      {
         b.append('[');
         b.append(Integer.toString(getIndex()));
         b.append(']');
      }

      return b.toString();
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getName()
    */
   public String getName() throws RepositoryException
   {
      return m_name;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getAncestor(int)
    */
   public Item getAncestor(int index) throws ItemNotFoundException,
         AccessDeniedException, RepositoryException
   {
      if (m_depth < index)
      {
         throw new ItemNotFoundException(
               "Can't have an ancestor below this node's depth");
      }
      if (m_depth == index)
      {
         return this;
      }
      else
      {
         return m_parent.getAncestor(index);
      }
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getParent()
    */
   public Node getParent() throws ItemNotFoundException, AccessDeniedException,
         RepositoryException
   {
      return m_parent;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getDepth()
    */
   public int getDepth() throws RepositoryException
   {
      return m_depth;
   }

   /**
    * Set the depth on the node
    * 
    * @param i
    */
   public void setDepth(int i)
   {
      m_depth = i;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#getSession()
    */
   public Session getSession() throws RepositoryException
   {
      return null;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#isNode()
    */
   public boolean isNode()
   {
      return true;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#isNew()
    */
   public boolean isNew()
   {
      return false;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#isModified()
    */
   public boolean isModified()
   {
      return false;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#isSame(javax.jcr.Item)
    */
   public boolean isSame(Item arg0) throws RepositoryException
   {
      if (arg0 instanceof Node)
         return equals(arg0);
      else
         return false;
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#accept(javax.jcr.ItemVisitor)
    */
   public void accept(ItemVisitor visitor) throws RepositoryException
   {
      visitor.visit(this);
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#save()
    */
   public void save() throws AccessDeniedException, ItemExistsException,
         ConstraintViolationException, InvalidItemStateException,
         ReferentialIntegrityException, VersionException, LockException,
         NoSuchNodeTypeException, RepositoryException
   {
      throw new RepositoryException("Read-only instance");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#refresh(boolean)
    */
   public void refresh(boolean arg0) throws InvalidItemStateException,
         RepositoryException
   {
      throw new UnsupportedOperationException("Not supported");
   }

   /** (non-Javadoc)
    * @see javax.jcr.Item#remove()
    */
   public void remove() throws VersionException, LockException,
         ConstraintViolationException, RepositoryException
   {
      throw new RepositoryException("Read-only instance");
   }

   /**
    * Add a property to the content node, used internally only!
    * @param property the property never <code>null</code>
    * @throws RepositoryException
    */
   public void addProperty(Property property) throws RepositoryException
   {
      if (property == null)
      {
         throw new IllegalArgumentException("property may not be null");
      }
      m_properties.put(property.getName(), property);
   }

   /**
    * Method to extract the guid
    * 
    * @return the guid, never <code>null</code>
    */
   public IPSGuid getGuid()
   {
      return m_guid;
   }

   /**
    * @return Returns the childrenLoaded.
    */
   boolean isChildrenLoaded()
   {
      return m_childrenLoaded;
   }

   /**
    * @param childrenLoaded The childrenLoaded to set.
    */
   public void setChildrenLoaded(boolean childrenLoaded)
   {
      m_childrenLoaded = childrenLoaded;
   }

   /**
    * @return Returns the config.
    */
   public PSTypeConfiguration getConfiguration()
   {
      return m_config;
   }

   /**
    * @param config The config to set.
    */
   public void setConfiguration(PSTypeConfiguration config)
   {
      m_config = config;
   }

   /**
    * Add a new child node with a specific guid
    * 
    * @param childField the child name, never <code>null</code> or empty
    * @param guid the guid, never <code>null</code>
    * @return a new node, never <code>null</code>
    * @throws RepositoryException
    * @throws LockException
    * @throws ConstraintViolationException
    * @throws VersionException
    * @throws PathNotFoundException
    * @throws ItemExistsException
    */
   public PSContentNode addNode(String childField, IPSGuid guid)
         throws ItemExistsException, PathNotFoundException, VersionException,
         ConstraintViolationException, LockException, RepositoryException
   {
      if (StringUtils.isBlank(childField))
      {
         throw new IllegalArgumentException(
               "childField may not be null or empty");
      }
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      PSContentNode newnode = (PSContentNode) addNode(childField);
      newnode.m_guid = guid;
      return newnode;
   }

   /*
    * (non-Javadoc) 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      PSLegacyGuid g = (PSLegacyGuid) m_guid;
      return new ToStringBuilder(this).append(ID_PROPERTY_NAME,
            g.getContentId()).append("name", m_name).append("depth", m_depth)
            .append("type", m_config.getType()).toString();
   }

   /**
    * Nodes are equal if they reference the same object in the database with the
    * same name and guid
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if(!(obj instanceof IPSNode))
         return false;
      IPSNode b = (IPSNode) obj;
      try
      {
         return new EqualsBuilder().append(m_guid, b.getGuid()).append(m_name,
               b.getName()).isEquals();
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return new HashCodeBuilder().append(m_name).append(m_guid).toHashCode();
   }

   /**
    * If this instance contains properties to load on demand, this will be
    * called with a non-<code>null</code> loader.
    * 
    * @param loader the loader, may be <code>null</code>
    */
   public void setLazyLoader(PSContentPropertyLoader loader)
   {
      m_lazyLoader = loader;
   }

   /**
    * Get the loader
    * @return the loader, may be <code>null</code>
    */
   public PSContentPropertyLoader getLazyLoader()
   {
      return m_lazyLoader;
   }

   /**
    * Set the content manager configuration
    * 
    * @param cmgrConfig
    */
   public void setContentManagerConfiguration(PSContentMgrConfig cmgrConfig)
   {
      m_cmgrConfig = cmgrConfig;
   }

   public long getSizeInBytes()
   {
      // We'll basically ignore this object's size as it is fairly insignificant
      // Instead we'll add up the properties on this and any child nodes. We
      // need to be careful that nothing we do forces the load of binary and
      // body fields
      long size = 0;
      
      for(Property p : m_properties.values())
      {
         if (p instanceof IPSJcrCacheItem)
         {
            size += ((IPSJcrCacheItem) p).getSizeInBytes();
         }
      }
      
      for(Object n : m_children.values())
      {
         if (n instanceof IPSJcrCacheItem)
         {
            size += ((IPSJcrCacheItem) n).getSizeInBytes();
         }
      }
      
      return size;
   }
}
