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

package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base class for dependency objects.  Provides all common dependency
 * functionality.  A dependency represents any deployable Rhythmyx server
 * element, e.g. a content type, variant definiton, application, exit, etc.
 */
public abstract class PSDependency implements IPSDeployComponent, Comparable
{
   /**
    * Construct a dependency with all required parameters.
    *
    * @param dependencyType The type of dependency, must be one of the
    * <code>TYPE_xxx</code> types.
    * @param dependencyId Combined with <code>objectType</code> uniquely
    * identifies the object this dependency represents.  May not be
    * <code>null</code> or empty.
    * @param displayName Name to use when displaying this dependency.  May not
    * be <code>null</code> or empty.
    * @param objectType The type of object this dependency represents. May not
    * be <code>null</code> or empty.
    * @param objectTypeName Displayable form of the <code>objectType</code>,
    * may not be <code>null</code> or empty.
    * @param supportsIdTypes <code>true</code> if this object contains static
    * ID's whose type must be identified, <code>false</code> if not.
    * @param supportsIdMapping <code>true</code> if this object's ID can change
    * across server's and thus may be included in an ID Mapping.
    * @param supportsUserDependencies If <code>true</code>, this dependency
    * allows user defined dependencies to be added as children,
    * <code>false</code> otherwise.
    * @param supportsParentId If <code>true</code>, supports a parent id to be
    * specified, if <code>false</code>, does not.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSDependency(int dependencyType, String dependencyId,
      String objectType, String objectTypeName, String displayName,
      boolean supportsIdTypes, boolean supportsIdMapping,
         boolean supportsUserDependencies, boolean supportsParentId)
   {
      if (dependencyType >= TYPE_ENUM.length || dependencyType < 0)
         throw new IllegalArgumentException("Invalid dependency type");

      if (dependencyId == null || dependencyId.trim().length() == 0)
         throw new IllegalArgumentException(
            "dependencyId may not be null or empty");

      if (objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectType may not be null or empty");

      if (objectTypeName == null || objectTypeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "objectTypeName may not be null or empty");

      if (displayName == null || displayName.trim().length() == 0)
         throw new IllegalArgumentException(
            "displayName may not be null or empty");

      setDependencyType(dependencyType);
      m_dependencyId = dependencyId;
      m_displayName = displayName;
      m_objectType = objectType;
      m_objectTypeName = objectTypeName;
      m_supportsIdTypes = supportsIdTypes;
      m_supportsIdMapping = supportsIdMapping;
      m_supportsUserDependencies = supportsUserDependencies;
      m_supportsParentId = supportsParentId;
   }

   /**
    * Constructs a dependency from its XML representation.
    *
    * @param src The source element.  Format expected is defined by
    * {@link #toXml(Document)}.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    */
   public PSDependency(Element src) throws PSUnknownNodeTypeException
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");

      fromXml(src);
   }

   /**
    * Parameterless ctor for use by derived classes only.
    */
   protected PSDependency()
   {
   }

   /**
    * Determines if is this dependency is included in the package.
    *
    * @return <code>true</code> if the dependency is included,
    * <code>false</code> otherwise.  Dependencies of type
    * {@link #TYPE_LOCAL} are always included, dependencies of type
    * {@link #TYPE_SERVER} or {@link #TYPE_SYSTEM} may never be included,
    * dependencies of type {@link #TYPE_SHARED} may optionally be included.
    */
   public boolean isIncluded()
   {
      return m_isIncluded;
   }

   /**
    * Sets a new dependency id on this dependency.  
    * 
    * @param id Combined with <code>objectType</code> uniquely
    * identifies the object this dependency represents.  May not be
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>id</code> is invalid.
    */
   public void setDependencyId(String id)
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      m_dependencyId = id;
   }
   
   /**
    * Sets whether this dependency is included in the package. In general this 
    * should only be called with force set to <code>false</code>.
    * Only a special case should set force to <code>true</code>.
    *
    * @param isIncluded If <code>true</code>, the dependency is included, if
    * <code>false</code> it is not.
    * @param force allows forcing of package inclusion regardless
    * of what {@link #canBeIncludedExcluded()} returns.
    */
   public void setIsIncluded(boolean isIncluded, boolean force)
   {
      m_isIncluded = isIncluded;
   }
   
   /**
    * Convenience method to call {@link #setIsIncluded(boolean, boolean)} as
    * setIsIncluded(boolean, false).
    */
   public void setIsIncluded(boolean isIncluded)
   {
      setIsIncluded(isIncluded, false);
   }
   
   

   /**
    * Determines if the <code>isIncluded</code> setting of this dependency
    * is modifiable.  See {@link #setIsIncluded(boolean)} for more info.
    *
    * @return <code>true</code> if this dependency's included setting can be
    * changed, <code>false</code> if not.
    */
   public boolean canBeIncludedExcluded()
   {
      boolean result = true;
      switch (m_dependencyType)
      {
         case TYPE_LOCAL:
         case TYPE_SERVER:
         case TYPE_SYSTEM:
            result = false;
            break;
      }

      return result;
   }
   
   /**
    * Determines if this dependency is deployable or not. Dependencies of types
    * <code>TYPE_SERVER</code> and <code>TYPE_SYSTEM</code> are not deployable, 
    * all others are deployable.
    * 
    * @return <code>true</code> if this dependency is deployable, otherwise
    * <code>false</code>
    */
   public boolean isDeployable()
   {
      boolean result = true;
      switch (m_dependencyType)
      {
         case TYPE_SERVER:
         case TYPE_SYSTEM:
            result = false;
            break;
      }

      return result;
   }


   /**
    * Gets all dependencies of this dependency that have been set.
    *
    * @return Iterator over zero or more <code>PSDependency</code> objects, or
    * <code>null</code> if the dependencies have not been set on this object.
    */
   public Iterator<PSDependency> getDependencies()
   {
      Iterator<PSDependency> deps = null;
      if (m_dependencies != null)
         deps = m_dependencies.iterator();

      return deps;
   }

   /**
    * Gets all dependencies for the specified type that have been set.
    *
    * @param type One of the <code>TYPE_XXX</code> types.
    *
    * @return Iterator over zero or more <code>PSDependency</code> objects, or
    * <code>null</code> if the dependencies have not been set on this object.
    */
   public Iterator<PSDependency> getDependencies(int type)
   {
      Iterator<PSDependency> deps = null;

      if (m_dependencies != null)
      {
         List<PSDependency> results = new ArrayList<PSDependency>();

         Iterator<PSDependency> i = m_dependencies.iterator();
         while (i.hasNext())
         {
            PSDependency dep = i.next();
            if (dep.getDependencyType() == type)
               results.add(dep);
         }
         deps = results.iterator();
      }

      return deps;
   }

   /**
    * Get child dependencies matching the specified object type.
    * 
    * @param objectType The type to find, may not be <code>null</code> or 
    * empty.
    * 
    * @return an iterator over zero or more <code>PSDependency</code> objects,
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public Iterator<PSDependency> getDependencies(String objectType)
   {
      if (objectType == null || objectType.trim().length() == 0)
         throw new IllegalArgumentException(" may not be null or empty");
      
      List<PSDependency> result = new ArrayList<PSDependency>();
      if (m_dependencies != null)
      {
         Iterator<PSDependency> deps = m_dependencies.iterator();
         while (deps.hasNext())
         {
            PSDependency dep = deps.next();
            if (dep.getObjectType().equals(objectType))
               result.add(dep);
         }         
      }
      
      return result.iterator();
   }
   
   
   /**
    * Get a child dependency for the given id and object type.
    *
    * @param id The id of the child dependency, may not be <code>null</code> or
    * empty.
    * @param objType The object type of the child dependency, may not be
    * <code>null</code> or empty.
    *
    * @return The specified child dependency in the dependency list;
    * <code>null</code> if not found in the dependency list, which may be one of
    * two things, the child dependency has not been set or the child
    * dependency does not exist in the current object.
    */
   public PSDependency getChildDependency(String id, String objType)
   {
      PSDependency childDep = null;

      if (m_dependencies != null)
      {
         Iterator<PSDependency> i = m_dependencies.iterator();
         while (i.hasNext() && childDep == null)
         {
            PSDependency dep = i.next();
            if (dep.getDependencyId().equals(id) &&
                dep.getObjectType().equals(objType) )
            {
               childDep = dep;
            }
         }
      }

      return childDep;
   }

   /**
    * Gets the ancestors of this dependency if they have been set.
    *
    * @return Iterator over zero or more <code>PSDependency</code> objects, or
    * <code>null</code> if the ancestors have not been set on this object.
    */
   public Iterator<PSDependency> getAncestors()
   {
      Iterator<PSDependency> ancestors = null;

      if (m_ancestors != null)
         ancestors = m_ancestors.iterator();

      return ancestors;
   }

   /**
    * Sets the child dependencies of this object.
    *
    * @param dependencies The dependencies to set, will replace any existing 
    * dependencies of this object.  May be <code>null</code> to clear the
    * dependencies.
    *
    * @throws IllegalArgumentException if <code>dependencies</code> contains 
    * a <code>null</code> element.
    */
   public void setDependencies(Iterator dependencies)
   {
      if (m_dependencies != null)
      {
         // clear the parent dependency on any current children
         for (Iterator children = m_dependencies.iterator(); children.hasNext();)
         {
            PSDependency child = (PSDependency) children.next();
            child.setParentDependency(null);
         }
      }
      if (dependencies == null)
         m_dependencies = null;
      else
      {
         m_dependencies = new TreeSet();
         while (dependencies.hasNext())
         {
            PSDependency dep = (PSDependency) dependencies.next();
            if ( dep == null )
               throw new IllegalArgumentException(
                  "dependencies may not contain null element");
   
            m_dependencies.add(dep);
            dep.setParentDependency(this); // support a doubly-linked list
         }
      }
   }

   /**
    * Sets the parent dependencies of this object.
    *
    * @param ancestors The ancestors to set, will replace any existing 
    * ancestors of this object.  May be <code>null</code> to clear the
    * ancestors.
    *
    * @throws IllegalArgumentException if <code>ancestors</code> is
    * <code>null</code>
    */
   public void setAncestors(Iterator<PSDependency> ancestors)
   {
      if (ancestors == null)
         m_ancestors = null;
      else
      {
         m_ancestors = new TreeSet<PSDependency>();
         while (ancestors.hasNext())
            m_ancestors.add(ancestors.next());
      }
   }

   /**
    * Gets the text to use when displaying this dependency.
    *
    * @return The text, never <code>null</code> or empty.
    */
   public String getDisplayName()
   {
      return m_displayName;
   }
   
   /**
    * Gets the string identifier of this element in the form "display 
    * name(object type name)". For example "Article(Content Editor)".
    * 
    * @return the identifier never <code>null</code> or empty.
    */
   public String getDisplayIdentifier()
   {  
      return getDisplayName() + "(" + getObjectTypeName() + ")";
   }

   /**
    * Gets String representation of this object. Uses the display name to
    * represent this object.
    *
    * @return the string, never <code>null</code> or empty.
    */
   public String toString()
   {
      return getDisplayIdentifier();
   }
   
   /**
    * Get a string representation of this dependency and all it's children and
    * ancestors.  Used for debugging purposes.
    * 
    * @return The tree representation, never <code>null</code>.
    */
   public String printDependencyTree()
   {
      StringBuffer buf = new StringBuffer();
      
      printDependencyTree(buf, new ArrayList(), "");
      
      return buf.toString();
   }
   
   /**
    * Recursive worker method for {@link #printDependencyTree()}, recurses into
    * all child dependencies and ancestors.
    * 
    * @param buf The buffer to which the tree data is appended, assumed not 
    * <code>null</code>.
    * @param processed List of already processed dependencies to avoid infinite
    * loops.
    * @param prefix Used to indent for each level of the tree, assumed not 
    * <code>null</code>, may be empty.
    */
   private void printDependencyTree(StringBuffer buf, List processed, 
      String prefix)
   {
      Iterator checkedDeps = processed.iterator();
      while (checkedDeps.hasNext())
      {
         PSDependency processedDep = (PSDependency)checkedDeps.next();
         if (processedDep == this)
            return;
      }   
      processed.add(this);
      
      buf.append(prefix);
      buf.append(toString());
      buf.append("\n");
      prefix += "-";
      if (m_dependencies != null)
      {
         buf.append(prefix);
         buf.append("deps:");
         buf.append("\n");

         Iterator deps = m_dependencies.iterator();
         while (deps.hasNext())
            ((PSDependency)deps.next()).printDependencyTree(buf, processed, 
               prefix);
      }
      
      if (m_ancestors != null)
      {
         buf.append(prefix);
         buf.append("ancs:");
         buf.append("\n");

         Iterator ancs = m_ancestors.iterator();
         while (ancs.hasNext())
            ((PSDependency)ancs.next()).printDependencyTree(buf, processed, 
               prefix);
      }      
   }

   /**
    * Determines if this dependency can be passed to retrieve an ID type map.
    *
    * @return <code>true</code> if this dependency supports ID type mappings,
    * <code>false</code> otherwise.
    */
   public boolean supportsIdTypes()
   {
      return m_supportsIdTypes;
   }

   /**
    * Get the dependency type of this object.
    *
    * @return One of the <code>TYPE_XXX</code> types.
    */
   public int getDependencyType()
   {
      return m_dependencyType;
   }

   /**
    * Sets the type of this dependency. <br />
    * If supplied type specifies
    * {@link #TYPE_LOCAL}, then this dependency will be marked as 
    * <ol>
    *   <li>included: ({@link #isIncluded()} will return <code>true</code>),</li>
    *   <li>NOT an association: ({@link #isAssociation()} will return 
    *   <code>false</code>) </li>
    * </ol>
    * If the supplied type specifies {@link #TYPE_SERVER} or {@link #TYPE_SYSTEM},
    * then it will be marked as not included. <br />
    * 
    * @param type One of the <code>TYPE_XXX</code> types.
    */
   public void setDependencyType(int type)
   {
      if (!validateType(type))
         throw new IllegalArgumentException("invalid type");
      

      m_dependencyType = type;

      if (type == TYPE_LOCAL)
      {
         m_isIncluded = true;
         m_isAssociation = false;
      }
      else if (type == TYPE_SERVER || type == TYPE_SYSTEM)
      {
         m_isIncluded = false;
      }
   }

   /**
    * Sets the display name for this dependency.  
    * 
    * @param name The new display name, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>name</code> is invalid.
    */
   public void setDisplayName(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
         
      m_displayName = name;
   }
   
   /**
    * Validates the supplied type as a valid dependency type.
    *
    * @param type the type to check
    *
    * @return <code>true</code> if it is one of the <code>TYPE_XXX</code> types,
    * otherwise <code>false</code>
    */
   public static boolean validateType(int type)
   {
      if (type < 0 || type >= TYPE_ENUM.length)
         return false;

      return true;
   }


   /**
    * Gets an identifier that along with the object type uniquely
    * identifies this dependency.  May not be unique across all element or
    * object types.
    *
    * @return The id, never <code>null</code> or empty.
    */
   public String getDependencyId()
   {
      return m_dependencyId;
   }

   /**
    * Returns the type of the object this dependency instance represents.
    *
    * @return The object type, never <code>null</code> or empty.
    */
   public String getObjectType()
   {
      return m_objectType;
   }

   /**
    * Returns the name of the type of the object this dependency instance
    * represents.
    *
    * @return The object type name, never <code>null</code> or empty.
    */
   public String getObjectTypeName()
   {
      return m_objectTypeName;
   }
   /**
    * Creates a user dependency, adds it to this object, and returns it so that
    * it can be saved to the server.  If the user dependency is not also
    * separately saved to the server, it will not appear as a dependency of this
    * object the next time its dependencies are loaded.
    *
    * @param path The path of the dependency file relative to the Rhythmyx
    * server root directory.
    *
    * @return The user dependency, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>path</code> is
    * <code>null</code>.
    * @throws IllegalStateException if {@link #supportsUserDependencies()}
    * would return <code>false</code> or if {@link #setDependencies(Iterator)} 
    * has not been called.
    */
   public PSUserDependency addUserDependency(File path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");

      if (!supportsUserDependencies())
         throw new IllegalStateException("user dependencies not supported");

      if (m_dependencies == null)   
         throw new IllegalStateException(
            "child dependencies have not yet been set");
         
      PSUserDependency dep = new PSUserDependency(path, this);
      dep.setIsAssociation(false);
      dep.setIsIncluded(true);
      m_dependencies.add(dep);

      return dep;
   }
   
   /**
    * Removes the user dependency with the supplied path from this dependency.
    * 
    * @param path The path of the user dependency to be removed, may not be
    * <code>null</code>.
    * 
    * @return <code>true</code> if a matching child user dependency was found
    * and removed, <code>false</code> if not.
    */
   public boolean removeUserDependency(File path)
   {
      if (path == null)
         throw new IllegalArgumentException("path may not be null");
      
      boolean removed = false;

      Iterator deps = m_dependencies.iterator();
      while (deps.hasNext() && !removed)
      {
         PSDependency dep = (PSDependency) deps.next();
         if (dep.getDependencyType() == PSDependency.TYPE_USER)
         {
            PSUserDependency userDep = (PSUserDependency) dep;
            if (userDep.getPath().getPath().equals(path.getPath()))
            {
               deps.remove();
               removed = true;
            }  
         }
      }
      
      return removed;
   }
   

   /**
    * Determines if this dependency's ID can change across servers and thus may
    * be included in an ID Mapping.
    *
    * @return <code>true</code> if this dependency can be referenced by an ID
    * mapping, <code>false</code> otherwise.
    */
   public boolean supportsIDMapping()
   {
      return m_supportsIdMapping;
   }

   /**
    * Determines if this dependency supports adding user depedencies as
    * child dependencies.
    *
    * @return <code>true</code> if it is suppported, <code>false</code> if
    * it does not.
    */
   public boolean supportsUserDependencies()
   {
      return m_supportsUserDependencies;
   }

   /**
    * Determines if this dependency can have a parent id and type specified.
    * 
    * @return <code>true</code> if it supports specifying a parent id, 
    * <code>false</code> otherwise.
    */
   public boolean supportsParentId()
   {
      return m_supportsParentId;
   }
   
   /**
    * Sets the parent id and type for this dependency.
    * 
    * @param id The parent id, may not be <code>null</code> or empty.
    * @param type The parent type, may not be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if {@link #supportsParentId()} returns
    * <code>false</code>
    */
   public void setParent(String id, String type)
   {
      if (!supportsParentId())
         throw new IllegalArgumentException(
            "dependency does not support parent id");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");      
      
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
         
      m_parentId = id;
      m_parentType = type;
   }
   
   /**
    * Get the parent id of this dependency if one has been set
    * 
    * @return The parent id, may be <code>null</code>, never empty.
    */
   public String getParentId()
   {
      return m_parentId;
   }
   
   /**
    * Get the parent type of this dependency if one has been set.
    * 
    * @return The parent type, may be <code>null</code>, never empty.
    */
   public String getParentType()
   {
      return m_parentType;
   }

   /**
    * Assigns the specified dependency as this dependency's parent, allowing the
    * dependency tree to be transversed upwards.
    * 
    * @param parent the dependency to assign as this dependency's parent, may be 
    * <code>null</code>
    */
   private void setParentDependency(PSDependency parent)
   {
      m_parentDependency = parent;
   }

   /**
    * Gets the parent dependency of this dependency, if one has been set.
    * 
    * @return The parent dependency, may be <code>null</code>.
    */
   public PSDependency getParentDependency()
   {
      return m_parentDependency;
   }

   /**
    * Determines if this dependency is an auto dependency, meaning it was added
    * as a child dependency automatically during archive creation, and is not
    * part of the original descriptor.
    * 
    * @return <code>true</code> if this dependency is an auto dependency,
    * <code>false</code> otherwise.
    */
   public boolean isAutoDependency()
   {
      return m_isAutoDependency;
   }
   
   /**
    * Sets this dependency to be an auto dependency.  See 
    * {@link #isAutoDependency()} for more info.
    * 
    * @param isAuto <code>true</code> if this dependency is an auto dependency,
    * <code>false</code> otherwise.
    */
   public void setIsAutoDependency(boolean isAuto)
   {
      m_isAutoDependency = isAuto;
   }
   
   /**
    * Determines if the supplied dependency is included in this dependency's
    * child dependencies, recursively.
    *
    * @param dep The dependency to check for, may not be <code>null</code>.
    *
    * @return <code>true</code> if it is included, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>dep</code> is <code>null</code>.
    */
   public boolean containsDependency(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      boolean found = false;

      if (dep.getKey().equals(getKey()))
         found = true;
      else
      {
         List checked = new ArrayList();
         found = containsDependency(dep, checked);
      }
      
      return found;
   }

   /**
    * Determines if the supplied dependency is included in this dependency's
    * child dependencies and if so, if {@link #isIncluded()} 
    * returns <code>true</code>, recursively.  Since the same dependency can
    * appear multiple times in the tree, the entire tree is searched, even if
    * non-included matches are found.  If the supplied dependency is of type
    * {@link #TYPE_LOCAL}, then it is only considered to be included if the 
    * first non-local ancestor found walking up its ancestors is included.
    *
    * @param dep The dependency to check for, may not be <code>null</code>.
    * @param sameInstance Determine if match should be made using 
    * {@link #getKey()}, or if the same instance of the dependency must be 
    * located.  If <code>true</code>, the same instance is located, if 
    * <code>false</code>, the key is used.  <code>true</code> is used to 
    * determine if a local dependency should be considered to be included.
    *
    * @return <code>true</code> if it is included, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>dep</code> is <code>null</code>.
    */
   public boolean includesDependency(PSDependency dep, boolean sameInstance)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      boolean included = false;
      if (sameInstance)
      {
         // verify that this object is same as the root ancestor of the 
         // supplied dependency
         boolean isParent = false;
         PSDependency parent = dep;
         while (parent.getParentDependency() != null)
         {
            parent = parent.getParentDependency();
            if (this == parent)
            {
               isParent = true;
               break;
            }
         }
         
         if (!isParent)
            return false;

         // if local, walk back up the parent deps to first non-local
         if (dep.getDependencyType() == PSDependency.TYPE_LOCAL)
         {
            PSDependency ancestor = dep.getParentDependency();
            while (ancestor != null)
            {
               if (ancestor.getDependencyType() != PSDependency.TYPE_LOCAL)
               {
                  included = ancestor.isIncluded();
                  break;
               }
               ancestor = ancestor.getParentDependency();
            }
         }
         else
         {
            // just check if dep is included
            included = dep.isIncluded();
         }
      }
      else
      {
         // search all dependencies
         List checked = new ArrayList();
         List parentStack = new ArrayList();
         included = includesDependency(dep, sameInstance, parentStack, checked);
      }
      return included;
   }


   /**
    * Returns a unique key for this dependency.
    *
    * @return The key, never <code>null</code> or empty.
    */
   public String getKey()
   {
      String key = getObjectType() + KEY_SEP + getDependencyId();
      if (m_parentId != null)
         key += KEY_SEP + getParentType() + KEY_SEP + getParentId();
         
      return key;
   }
   
   /**
    * Implements interface method. Compares the display identifier of this
    * dependency with the supplied dependency's display identifier 
    * lexicographically ignoring the case. Please see {@link 
    * java.lang.String#compareToIgnoreCase(String) compareToIgnoreCase} for more
    * information about return value.
    * 
    * @param obj the object to compare, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if obj is <code>null</code>
    * @throws ClassCastException if obj is not an instance of PSDependency.
    */
   public int compareTo(Object obj)
   {
      if(obj == null)
         throw new IllegalArgumentException("obj may not be null");
         
      PSDependency dep = (PSDependency)obj;
      int result = getDisplayIdentifier().compareToIgnoreCase(
         dep.getDisplayIdentifier());
      if (result == 0)
         result = getDependencyId().compareToIgnoreCase(dep.getDependencyId());
         
      return result;
   }

   /**
    * Parses the supplied key and returns the dependency's object type and id.
    * 
    * @param key A valid dependency key, may not be <code>null</code> or empty.
    * 
    * @return An array with two elements, the first being the type and the 
    * second being the id, never <code>null</code>, and neither element is ever
    * <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if <code>key</code> is not a valid 
    * dependency key.
    */
   public static String[] parseKey(String key)
   {
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException("key may not be null or empty");
      
      int sepPos = key.indexOf(KEY_SEP);
      if (sepPos == -1 || sepPos == 0 || sepPos == key.length() - 1)
         throw new IllegalArgumentException("invalid key: " + key);
      
      String[] result = new String[2];
      result[0] = key.substring(0, sepPos);
      result[1] = key.substring(sepPos + 1, key.length());
      
      return result;
   }

   /**
    * Worker method for {@link #containsDependency(PSDependency)} that avoids
    * infinite loops and performs the actual check.
    *
    * @param dep The dependency to check for, assumed not <code>null</code>.
    * @param checked A list of dependencies that have already been checked to 
    * see if <code>dep</code> is a child dependency.  If the instance this 
    * method is called on is already in this list, it will immediately return
    * <code>false</code> to avoid infinite looping.  Assumed not
    * <code>null</code>.
    *
    * @return <code>true</code> if <code>dep</code> is one of this dependency's
    * children, recursively, <code>false</code> otherwise.
    */
   private boolean containsDependency(PSDependency dep, List checked)
   {
      Iterator checkedDeps = checked.iterator();
      while (checkedDeps.hasNext())
      {
         PSDependency checkedDep = (PSDependency)checkedDeps.next();
         if (checkedDep == this)
            return false;
      }   
      checked.add(this);

      boolean found = false;

      if (m_dependencies != null)
      {
         // first check each child dep
         String depKey = dep.getKey();         
         Iterator deps = m_dependencies.iterator();
         while (deps.hasNext() && !found)
         {
            PSDependency childDep = (PSDependency)deps.next();
            if (depKey.equals(childDep.getKey()))
               found = true;
         }
         
         // if not found, recurse the dependencies of each child
         if (!found)
         {
            deps = m_dependencies.iterator();
            while (deps.hasNext() && !found)
            {
               PSDependency child = (PSDependency)deps.next();
               found = child.containsDependency(dep, checked);
            }
         }
      }
      return found;
   }
   
   /**
    * Worker method for {@link #includesDependency(PSDependency, boolean)} that 
    * avoids infinite loops and performs the actual check.  See that method's
    * documentation for information and any parameters not described here.
    *
    * @param parentStack The parents of this dependency that have been checked
    * so far.  Used to walk up the ancestors to determine if local dependencies
    * are really included.  In each call to this method, this object is added
    * to the list before recursing, and removed after returning from recursive
    * calls.  Assumed not <code>null</code>.
    * @param checked A list of dependencies that have already been checked to 
    * see if <code>dep</code> is included.  If the instance this 
    * method is called on is already in this list, it will immediately return
    * <code>false</code> to avoid infinite looping.  Assumed not
    * <code>null</code>.
    * 
    * @return <code>true</code> if the supplied dep is included, 
    * <code>false</code> otherwise.
    */
   private boolean includesDependency(PSDependency dep, boolean sameInstance, 
      List parentStack, List checked)
   {
      Iterator checkedDeps = checked.iterator();
      while (checkedDeps.hasNext())
      {
         PSDependency checkedDep = (PSDependency)checkedDeps.next();
         if (checkedDep == this)
            return false;
      }   
      checked.add(this);

      // make sure it's even possible to be included
      if (!dep.isIncluded() && (sameInstance || !dep.canBeIncludedExcluded()))
         return false;
      
      boolean included = false;
      
      // see if this is a match
      if ((sameInstance && this == dep) || 
         (!sameInstance && getKey().equals(dep.getKey())))
      {
         // if local, walk back up the parent stack to first non-local
         if (getDependencyType() == PSDependency.TYPE_LOCAL)
         {
            Iterator parents = parentStack.iterator();
            while (parents.hasNext())
            {
               PSDependency parent = (PSDependency)parents.next();
               if (parent.getDependencyType() != PSDependency.TYPE_LOCAL)
               {
                  included = parent.isIncluded();
                  break;
               }
            }
         }
         else
            included = isIncluded();         
      }

      // recurse children if not included yet, stop at elements that arent't the
      // root
      boolean isSubElement = this instanceof PSDeployableElement && 
         !parentStack.isEmpty();
      if (!included && m_dependencies != null && !isSubElement)
      {
         // push self onto the parent stack
         parentStack.add(0, this);
         
         // check each child dep
         Iterator deps = m_dependencies.iterator();
         while (deps.hasNext() && !included)
         {
            PSDependency child = (PSDependency)deps.next();
            included = child.includesDependency(dep, sameInstance, 
               parentStack, checked);
         }
         
         // pop the parent stack
         parentStack.remove(0);
      }
      
      return included;
   }

   /**
    * Gets the name of this dependency's type.
    *
    * @return The name, never <code>null</code> or empty.
    */
   public String getDependencyTypeName()
   {
      return TYPE_ENUM[m_dependencyType];
   }
   
   /**
    * Recursively gets the number of child dependencies that are set to be 
    * included.
    * 
    * @return The count, never less than 0.
    */
   public int getIncludedChildCount()
   {
      return getChildCount(true);
   }

   /**
    * Recursively gets the number of child dependencies.  Does not include
    * deployable elements
    * 
    * @param includedOnly If <code>true</code>, only included child dependencies
    * will be added to the count.  If <code>false</code>, all child dependencies
    * will be added to the count.
    * 
    * @return The count, never less than 0.
    */
   public int getChildCount(boolean includedOnly)
   {
      int count = 0;
      if (m_dependencies != null)
      {
         Iterator deps = m_dependencies.iterator();
         while (deps.hasNext())
         {
            PSDependency dep = (PSDependency)deps.next();
            if (!(dep instanceof PSDeployableElement))
            {
               if ((includedOnly && dep.isIncluded()) || !includedOnly)
                  count++;
            }
            count += dep.getIncludedChildCount();
         }
      }
      
      return count;
   }

   /**
    * Determines if this dependency should auto-expand in to display all local 
    * dependencies in the dependency tree in the UI.  Use 
    * {@link #setShouldAutoExpand(boolean)} to modify this property.
    * 
    * @return <code>true</code> if it should auto-expand, <code>false</code>
    * otherwise.  Defaults to <code>true</code>.
    */
   public boolean shouldAutoExpand()
   {
      return m_autoExpand;
   }
   
   /**
    * See {@link #shouldAutoExpand()} for an explanation of this property.
    * 
    * @param autoExpand <code>true</code> to auto-expand, 
    * <code>false</code> to supress auto-expand.
    */
   public void setShouldAutoExpand(boolean autoExpand)
   {
      m_autoExpand = autoExpand;
   }
   
   /**
    * Determines if this link is an association. That is,
    * it is optional to include in a package.
    * @return <code>true</code> if the link is an association.
    */
   public boolean isAssociation()
   {
      return m_isAssociation;
   }
   
   /**
    * See {@link #isAssociation()} for information on this property.
    * @param isAssociation <code>true</code> if link is an association.
    */
   public void setIsAssociation(boolean isAssociation)
   {
      m_isAssociation = isAssociation;
   }

   /**
    * Determines if this dependency contains a child dependency that is
    * included.  Does not consider <code>TYPE_LOCAL</code> dependencies or 
    * deployable elements, and does not recurse into deployable elements.  Does 
    * not check if this dependency is included.
    * 
    * @return <code>true</code> if this dependency contains an included 
    * dependency, <code>false</code> otherwise.
    */   
   public boolean containsIncludedDependency()
   {
      boolean hasIncluded = false;
      Iterator deps = getDependencies();
      if (deps != null)
      {
         while (deps.hasNext() && !hasIncluded)
         {
            PSDependency dep = (PSDependency)deps.next();
            if (dep instanceof PSDeployableElement)
               continue;
            hasIncluded = (dep.getDependencyType() != PSDependency.TYPE_LOCAL) && 
               dep.isIncluded();
            if (!hasIncluded)
               hasIncluded = dep.containsIncludedDependency();
         }
      }
      
      return hasIncluded;
   }
   
   /**
    * Update the typically immutable properties of this dependency from the 
    * supplied source dependency that may have changed since it was created.  
    * This should only be used when restoring a persisted dependency that may 
    * have been created with a previous version or build and whose defintion may 
    * have changed since it was last persisted.
    * 
    * @param src The source dependency whose defintion will be used, may not
    * be <code>null</code> and must have the same object type and id.
    */
   public void updateDependencyDefinition(PSDependency src)
   {
      if (src == null)
         throw new IllegalArgumentException("src may not be null");
      
      if (!src.m_objectType.equals(m_objectType))
         throw new IllegalArgumentException("src object type must match");
      
      if (!src.m_dependencyId.equals(m_dependencyId))
         throw new IllegalArgumentException("src dependency id must match");
      
      m_autoExpand = src.m_autoExpand;
      m_objectTypeName = src.m_objectTypeName;
      m_supportsIdMapping = src.m_supportsIdMapping;
      m_supportsIdTypes = src.m_supportsIdTypes;
      m_supportsUserDependencies = src.m_supportsUserDependencies;      
   }
   
   //Methods generated from implementation of interface IPSDeployComponent
   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. Format is:
    * <pre><code>
    * &lt;!ELEMENT PSXDepenedency (Dependencies?, Ancestors?)>
    * &lt;!ATTLIST PSXDepenedency
    *    dependencyType CDATA #REQUIRED
    *    dependencyId CDATA #REQUIRED
    *    displayName  CDATA #REQUIRED
    *    objectType CDATA #REQUIRED
    *    objectTypeName CDATA #REQUIRED
    *    supportsIdTypes (yes | no) "no"
    *    supportsIdMapping (yes | no) "no"
    *    supportsUserDependencies (yes | no) "no"
    *    supportsParentId (yes | no) "no"
    *    parentId CDATA #IMPLIED
    *    parentType CDATA #IMPLIED
    *    isIncluded (yes | no) "no"
    *    autoExpand (yes | no) "yes"
    *    autoDep (yes | no) "no"
    *    isGeneric (yes | no) "no"
    * >
    * &lt;!ELEMENT Dependencies (PSXDeployableElement | PSXDeployableObject |
    *    PSXUserDependency)*>
    * &lt;!ELEMENT Ancestors (PSPSXDeployableElement | PSXDeployableObject)*>
    * </pre></code>
    *
    * @param doc The document to use to create the element, may not be
    * <code>null</code>.
    *
    * @return the newly created XML element node, never <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATT_DEPENDENCY_TYPE, TYPE_ENUM[m_dependencyType]);
      root.setAttribute(XML_ATT_DEPENDENCY_ID, m_dependencyId);
      root.setAttribute(XML_ATT_DISPLAY_NAME, m_displayName);
      root.setAttribute(XML_ATT_OBJECT_TYPE, m_objectType);
      root.setAttribute(XML_ATT_OBJECT_TYPE_NAME, m_objectTypeName);
      root.setAttribute(XML_ATT_SUPPORTS_ID_TYPES, m_supportsIdTypes ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATT_SUPPORTS_ID_MAPPING, m_supportsIdMapping ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATT_SUPPORTS_USER_DEPENDENCIES,
         m_supportsUserDependencies ? XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATT_IS_INCLUDED, m_isIncluded ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATT_SUPPORTS_PARENT_ID, m_supportsParentId ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      if (m_parentId != null)
         root.setAttribute(XML_ATT_PARENT_ID, m_parentId);
      if (m_parentType != null)
         root.setAttribute(XML_ATT_PARENT_TYPE, m_parentType);
      root.setAttribute(XML_ATTR_AUTO_EXPAND, m_autoExpand ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATTR_AUTO_DEP, m_isAutoDependency ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);
      root.setAttribute(XML_ATTR_GENERIC, m_isAssociation ?
         XML_ATT_VAL_TRUE : XML_ATT_VAL_FALSE);

      if (m_dependencies != null)
      {
         Element deps = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            XML_EL_DEPENDENCIES);
         Iterator i = m_dependencies.iterator();
         while (i.hasNext())
         {
            PSDependency dep = (PSDependency)i.next();
            deps.appendChild(dep.toXml(doc));
         }
      }

      if (m_ancestors != null)
      {
         Element ancs = PSXmlDocumentBuilder.addEmptyElement(doc, root,
            XML_EL_ANCESTORS);
         Iterator i = m_ancestors.iterator();
         while (i.hasNext())
         {
            PSDependency dep = (PSDependency)i.next();
            ancs.appendChild(dep.toXml(doc));
         }
      }

      return root;
   }

   /**
    * This method is called to populate this object from its XML representation.
    *
    * @param sourceNode the XML element node to populate from, not
    * <code>null</code>.  See {@link #toXml(Document)} for the format expected.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    * represent a type supported by the class.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }


      String sTemp = getRequiredAttribute(sourceNode, XML_ATT_DEPENDENCY_TYPE);
      m_dependencyType = -1;
      for (int i = 0; i < TYPE_ENUM.length && m_dependencyType == -1; i++)
      {
         if (TYPE_ENUM[i].equals(sTemp))
            m_dependencyType = i;
      }
      if (m_dependencyType == -1)
      {
         Object[] args = {sourceNode.getTagName(), XML_ATT_DEPENDENCY_TYPE,
               sTemp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_dependencyId = getRequiredAttribute(sourceNode,
         XML_ATT_DEPENDENCY_ID);
      m_displayName = getRequiredAttribute(sourceNode, XML_ATT_DISPLAY_NAME);
      m_objectType = getRequiredAttribute(sourceNode, XML_ATT_OBJECT_TYPE);
      m_objectTypeName = getRequiredAttribute(sourceNode,
         XML_ATT_OBJECT_TYPE_NAME);
      m_supportsIdTypes = XML_ATT_VAL_TRUE.equals(sourceNode.getAttribute(
         XML_ATT_SUPPORTS_ID_TYPES));
      m_supportsIdMapping = XML_ATT_VAL_TRUE.equals(sourceNode.getAttribute(
         XML_ATT_SUPPORTS_ID_MAPPING));
      m_supportsUserDependencies = XML_ATT_VAL_TRUE.equals(
         sourceNode.getAttribute(XML_ATT_SUPPORTS_USER_DEPENDENCIES));
      m_isIncluded = XML_ATT_VAL_TRUE.equals(sourceNode.getAttribute(
         XML_ATT_IS_INCLUDED));
      m_supportsParentId = XML_ATT_VAL_TRUE.equals(
         sourceNode.getAttribute(XML_ATT_SUPPORTS_PARENT_ID));
      // this should default to true if not specified
      m_autoExpand = !XML_ATT_VAL_FALSE.equals(sourceNode.getAttribute(
         XML_ATTR_AUTO_EXPAND));
      m_isAutoDependency = XML_ATT_VAL_TRUE.equals(sourceNode.getAttribute(
         XML_ATTR_AUTO_DEP));
      m_isAssociation = XML_ATT_VAL_TRUE.equals(
         sourceNode.getAttribute(XML_ATTR_GENERIC));
         
      sTemp = sourceNode.getAttribute(XML_ATT_PARENT_ID);
      if (sTemp != null && sTemp.trim().length() > 0)
         m_parentId = sTemp;
      else
         m_parentId = null;
         
      sTemp = sourceNode.getAttribute(XML_ATT_PARENT_TYPE);
      if (sTemp != null && sTemp.trim().length() > 0)
         m_parentType = sTemp;
      else
         m_parentType = null;

      int firstFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);
      int nextFlags = (PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      m_dependencies = null;
      Element deps = tree.getNextElement(XML_EL_DEPENDENCIES, firstFlags);
      if (deps != null)
      {
         m_dependencies = new TreeSet();
         Element dep = tree.getNextElement(firstFlags);
         while (dep != null)
         {
            PSDependency depObj;
            if (dep.getNodeName().equals(PSDeployableElement.XML_NODE_NAME))
               depObj = new PSDeployableElement(dep);
            else if (dep.getNodeName().equals(PSDeployableObject.XML_NODE_NAME))
               depObj = new PSDeployableObject(dep);
            else if (dep.getNodeName().equals(PSUserDependency.XML_NODE_NAME))
               depObj = new PSUserDependency(dep);
            else
            {
               Object[] args = {PSDeployableElement.XML_NODE_NAME + ", " +
                     PSDeployableObject.XML_NODE_NAME, dep.getNodeName()};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
            }
            m_dependencies.add(depObj);
            depObj.setParentDependency(this); // support a doubly-linked list
            dep = tree.getNextElement(nextFlags);
         }
      }

      tree.setCurrent(sourceNode);
      m_ancestors = null;
      Element ancs = tree.getNextElement(XML_EL_ANCESTORS, firstFlags);
      if (ancs != null)
      {
         m_ancestors = new TreeSet();
         Element dep = tree.getNextElement(firstFlags);
         while (dep != null)
         {
            PSDependency depObj;
            if (dep.getNodeName().equals(PSDeployableElement.XML_NODE_NAME))
               depObj = new PSDeployableElement(dep);
            else if (dep.getNodeName().equals(PSDeployableObject.XML_NODE_NAME))
               depObj = new PSDeployableObject(dep);
            else
            {
               Object[] args = {PSDeployableElement.XML_NODE_NAME + ", " +
                     PSDeployableObject.XML_NODE_NAME, dep.getNodeName()};
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
            }
            m_ancestors.add(depObj);
            dep = tree.getNextElement(nextFlags);
         }
      }
   }

   // see IPSDeployComponent
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSDependency))
         throw new IllegalArgumentException("obj wrong type");

      PSDependency dep = (PSDependency)obj;

      m_dependencyId = dep.m_dependencyId;
      m_dependencyType = dep.m_dependencyType;
      m_displayName = dep.m_displayName;
      m_isIncluded = dep.m_isIncluded;
      m_objectType = dep.m_objectType;
      m_objectTypeName = dep.m_objectTypeName;
      m_supportsIdMapping = dep.m_supportsIdMapping;
      m_supportsIdTypes = dep.m_supportsIdTypes;
      m_supportsUserDependencies = dep.m_supportsUserDependencies;
      m_supportsParentId = dep.m_supportsParentId;
      m_parentId = dep.m_parentId;
      m_parentType = dep.m_parentType;
      m_autoExpand = dep.m_autoExpand;
      m_isAutoDependency = dep.m_isAutoDependency;
      m_isAssociation = dep.m_isAssociation;
      
      if (dep.m_ancestors == null)
         m_ancestors = null;
      else
      {
         m_ancestors = new TreeSet();
         m_ancestors.addAll(dep.m_ancestors);
      }

      if (dep.m_dependencies == null)
         m_dependencies = null;
      else
      {
         m_dependencies = new TreeSet();
         m_dependencies.addAll(dep.m_dependencies);
      }
   }
   
   /**
    * Creates a new instance of this object, deep copying all member variables.
    * If derived classes has mutable member variables, it must override this
    * method and clone() each of those variables. This method will create a
    * shallow copy of them if it is not overridden.
    * 
    * @return a deep-copy clone of this instance, never <code>null</code>.
    */
   public Object clone()
   {
      PSDependency copy = null;
      // won't happen because it implements Cloneable interface
      try
      {
         copy = (PSDependency) super.clone();
      }
      catch (CloneNotSupportedException e)
      {
      }

      if (m_ancestors == null)
         copy.m_ancestors = null;
      else
      {
         copy.m_ancestors = new TreeSet<PSDependency>();
         Iterator<PSDependency> ancestors = m_ancestors.iterator();
         while (ancestors.hasNext())
            copy.m_ancestors.add((PSDependency)ancestors.next().clone());
      }
      if (m_dependencies == null)
         copy.m_dependencies = null;
      else
      {
         copy.m_dependencies = new TreeSet<PSDependency>();
         Iterator<PSDependency> dependencies = m_dependencies.iterator();
         while (dependencies.hasNext())
         {
            PSDependency sourceDep = dependencies.next();
            PSDependency clonedDep = (PSDependency) sourceDep.clone();
            copy.m_dependencies.add(clonedDep);
            clonedDep.setParentDependency(copy); // repoint parent
         }
      }

      return copy;
   }

   // see IPSDeployComponent
   public int hashCode()
   {
      int code = 0;

      code =
         m_dependencyId.hashCode() +
         m_dependencyType +
         m_displayName.hashCode() +
         m_objectType.hashCode() +
         m_objectTypeName.hashCode() +
         (m_isIncluded ? 1 : 0) +
         (m_supportsIdMapping ? 1 : 0) +
         (m_supportsIdTypes ? 1 : 0) +
         (m_supportsParentId ? 1 : 0) +
         (m_supportsUserDependencies ? 1 : 0) +
         (m_parentId == null ? 0 : m_parentId.hashCode()) +
         (m_parentType == null ? 0 : m_parentType.hashCode()) +
         (m_dependencies == null ? 0 : m_dependencies.hashCode()) +
         (m_ancestors == null ? 0 : m_ancestors.hashCode()) +
         (m_autoExpand ? 1 : 0) +
         (m_isAutoDependency ? 1 : 0) +
         (m_isAssociation ? 1 : 0);

      return code;
   }

   // see IPSDeployComponent
   public boolean equals(Object obj)
   {
      boolean isEqual = true;
      if (!(obj instanceof PSDependency))
         isEqual = false;
      else
      {
         PSDependency other = (PSDependency)obj;
         if (!m_dependencyId.equals(other.m_dependencyId))
            isEqual = false;
         else if (m_dependencyType != other.m_dependencyType)
            isEqual = false;
         else if (!m_displayName.equals(other.m_displayName))
            isEqual = false;
         else if (m_isIncluded != other.m_isIncluded)
            isEqual = false;
         else if (!m_objectType.equals(other.m_objectType))
            isEqual = false;
         else if (!m_objectTypeName.equals(other.m_objectTypeName))
            isEqual = false;
         else if (m_supportsIdMapping != other.m_supportsIdMapping)
            isEqual = false;
         else if (m_supportsIdTypes != other.m_supportsIdTypes)
            isEqual = false;
         else if (m_supportsParentId != other.m_supportsParentId)
            isEqual = false;
         else if (m_supportsUserDependencies !=
            other.m_supportsUserDependencies)
         {
            isEqual = false;
         }
         else if ((m_ancestors == null) ^ (other.m_ancestors == null))
            isEqual = false;
         else if ( m_ancestors != null && !m_ancestors.equals(
            other.m_ancestors))
         {
            isEqual = false;
         }
         else if ((m_dependencies == null) ^ (other.m_dependencies == null))
         {
            isEqual = false;
         }
         else if (m_dependencies != null &&  !m_dependencies.equals(
            other.m_dependencies))
         {
            isEqual = false;
         }
         else if ((m_parentId == null) ^ (other.m_parentId == null))
            isEqual = false;
         else if ( m_parentId != null && !m_parentId.equals(
            other.m_parentId))
         {
            isEqual = false;
         }
         else if ((m_parentType == null) ^ (other.m_parentType == null))
            isEqual = false;
         else if ( m_parentType != null && !m_parentType.equals(
            other.m_parentType))
         {
            isEqual = false;
         }
         else if (m_autoExpand != other.m_autoExpand)
            isEqual = false;
         else if (m_isAutoDependency != other.m_isAutoDependency)
            isEqual = false;
         else if (m_isAssociation != other.m_isAssociation)
            isEqual = false;
      }

      return isEqual;
   }

   /**
    * Utility method to get a required attibute value.
    *
    * @param source Element to get the attribute from, assumed not
    * <code>null</code>.
    * @param attName The name of the attribute to get, assumed not
    * <code>null</code> or empty
    *
    * @return The attribute value, never <code>null</code> or empty.
    *
    * @throws PSUnknownNodeTypeException If the specified attribute cannot be
    * found with a non-empty value.
    */
   protected String getRequiredAttribute(Element source, String attName)
      throws PSUnknownNodeTypeException
   {
      return PSDeployComponentUtils.getRequiredAttribute(source, attName);
   }



   /**
    * Constant for the local type.  Local dependencies must always be included
    * in a package if their parent is included.
    */
   public static final int TYPE_LOCAL = 0;

   /**
    * Constant for the shared type.  Shared dependencies may optionally be
    * included in a package if their parent is included.
    */
   public static final int TYPE_SHARED = 1;

   /**
    * Constant for dependencies that are defined by the end user, but cannot
    * be included in a package as they cannot be deployed (for example, a
    * loadable handler).
    */
   public static final int TYPE_SERVER = 2;

   /**
    * Constant for dependencies that are part of the system (e.g. a system app
    * or system exit) and can never be included in a package.
    */
   public static final int TYPE_SYSTEM = 3;

   /**
    * Constant for the type user defined dependency.  This type can always be
    * included in a package, and are always files.
    */
   public static final int TYPE_USER = 4;

   /**
    * Enumeration of the names for the <code>TYPE_xxx</code> constants.  Index
    * of the type constant is used as an index into this array to get its
    * name and visa versa.
    */
   public static final String[] TYPE_ENUM =
   {
      "Local", "Shared", "Server", "System", "User"
   };

   /**
    * Constant for this object's root XML node.
    */
   public static final String XML_NODE_NAME = "PSXDependency";

   /**
    * The separator used in dependency keys.
    */
   private static final String KEY_SEP = "-";
   
   /**
    * Name of the object this dependency represents.  Never <code>null</code> or
    * empty after ctor, may be modified by call to <code>setDisplayName()</code> 
    * or <code>copyFrom()</code>.
    */
   private String m_displayName;

   /**
    * Type of this dependency, one of the <code>TYPE_xxx</code> values, set
    * during ctor, may be modified by call to <code>copyFrom()</code>.
    */
   private int m_dependencyType = -1;

   /**
    * Indicates if the object type of this instance may contain static ID's that
    * must be identified as other types of dependencies.
    * <code>true</code> if this dependency supports id types, <code>false</code>
    * otherwise. Set during ctor, may be modified by call to
    * <code>copyFrom()</code>.
    */
   private boolean m_supportsIdTypes = false;

   /**
    * The unique identifier for this dependency within its dependency type. Set
    * during ctor, may be modified by call to <code>copyFrom()</code>.
    */
   private String m_dependencyId;

   /**
    * The type of object this class represents.  Set during construction, never
    * <code>null</code> after that, may be modified by call to
    * <code>copyFrom()</code>.
    */
   private String m_objectType;

   /**
    * The display name of the type of object this class represents.  Set during
    * construction, never <code>null</code> after that, may be modified by call
    * to <code>copyFrom()</code>.
    */
   private String m_objectTypeName;

   /**
    * Indicates if the object type of this instance has a numeric ID that needs
    * to be mapped to different ID's when installing then on a different server.
    * <code>true</code> if this dependency supports ID mapping,
    * <code>false</code> otherwise.  Set during ctor, may be modified by call to
    * <code>copyFrom()</code>.
    */
   private boolean m_supportsIdMapping = false;

   /**
    * Marks this dependency for inclusion in a package archive.  If
    * <code>true</code> it will be included, <code>false</code> otherwise.
    * Initially <code>false</code>, set to <code>true</code> in the ctor if the
    * type is {@link #TYPE_SHARED}, may be modified by a call to
    * {@link #setIsIncluded(boolean)}.
    */
   private boolean m_isIncluded = false;

   /**
    * <code>true</code> if this dependency supports adding user
    * dependencies, <code>false</code> otherwise.  Initialized during the ctor,
    * never <code>null</code> or empty or modified after that.
    */
   private boolean m_supportsUserDependencies = false;

   /**
    * The <code>PSDependency</code> objects that have been set as child
    * dependencies of this object.  <code>null</code> until the first call to
    * {@link #setDependencies(Iterator)}.
    */
   private Set<PSDependency> m_dependencies = null;

   /**
    * The parent dependency of this dependency, if any.  This field allows a 
    * tree of dependencies to be traversed from descendents to ancestors.  
    * Assigned in <code>fromXml</code> and <code>setDependencies</code>.
    */
   private transient PSDependency m_parentDependency;
   /**
    * The <code>PSDependency</code> objects that have been set as parent
    * dependencies of this object.  <code>null</code> until the first call to
    * {@link #setAncestors(Iterator)}.
    */
   private Set<PSDependency> m_ancestors = null;
   
   /**
    * <code>true</code> if this dependency can specify a parent id, 
    * <code>false</code> otherwise.  Initialized during the ctor, never 
    * <code>null</code> or empty or modified after that.
    */
   private boolean m_supportsParentId = false;
   
   /**
    * The unique identifier for this dependency's parent within its parent's 
    * dependency type. Set by a call to {@link #setParent(String, String)}, may
    * be <code>null</code>, never empty.
    */
   private String m_parentId = null;

   /**
    * The type of object this dependency's parent represents. Set by a call to 
    * {@link #setParent(String, String)}, may be <code>null</code>, never empty.
    */
   private String m_parentType = null;

   /**
    * <code>true</code> if the dependency tree in the UI should auto-expand
    * to display all local dependencies, <code>false</code> if it should not
    * auto-expand.  Defaults to <code>true</code>, may be modified by a call to
    * {@link #setShouldAutoExpand(boolean)}.
    */   
   private boolean m_autoExpand = true;
   
   /**
    * Flag to indicate if this dependency was automatically added as a child
    * by the system upon archive creation, or if it is part of the original
    * descriptor.  Initially <code>false</code>, modified by calls to 
    * {@link #setIsAutoDependency(boolean)}. 
    */
   private boolean m_isAutoDependency = false;
   
   /**
    * Flag to indicate that the dependency is considered generic.
    * Initially <code>true</code>, modified by calls to
    * {@link #setIsAssociation(boolean)}.
    */
   private boolean m_isAssociation = true;
   
   // private constants for XML representation
   private static final String XML_ATT_VAL_TRUE = "yes";
   private static final String XML_ATT_VAL_FALSE = "no";
   private static final String XML_EL_DEPENDENCIES = "Dependencies";
   private static final String XML_EL_ANCESTORS = "Ancestors";
   private static final String XML_ATT_DEPENDENCY_TYPE = "dependencyType";
   private static final String XML_ATT_DEPENDENCY_ID = "dependencyId";
   private static final String XML_ATT_DISPLAY_NAME  = "displayName";
   private static final String XML_ATT_OBJECT_TYPE = "objectType";
   private static final String XML_ATT_OBJECT_TYPE_NAME = "objectTypeName";
   private static final String XML_ATT_SUPPORTS_ID_TYPES = "supportsIdTypes";
   private static final String XML_ATT_SUPPORTS_ID_MAPPING =
      "supportsIdMapping";
   private static final String XML_ATT_IS_INCLUDED = "isIncluded";
   private static final String XML_ATT_SUPPORTS_USER_DEPENDENCIES =
      "supportsUserDependencies";
   private static final String XML_ATT_SUPPORTS_PARENT_ID = 
      "supportsParentId";
   private static final String XML_ATT_PARENT_ID = "parentId";
   private static final String XML_ATT_PARENT_TYPE = "parentType";
   private static final String XML_ATTR_AUTO_EXPAND = "autoExpand";
   private static final String XML_ATTR_AUTO_DEP = "autoDep";
   private static final String XML_ATTR_GENERIC = "isAssociation";

}

