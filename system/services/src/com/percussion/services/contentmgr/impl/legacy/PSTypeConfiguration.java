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
package com.percussion.services.contentmgr.impl.legacy;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.design.objectstore.IPSBackEndMapping;
import com.percussion.design.objectstore.PSContentEditorSystemDef;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.server.PSServer;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSContentNode;
import com.percussion.services.contentmgr.impl.IPSContentRepository;
import com.percussion.services.contentmgr.impl.PSContentInternalLocator;
import com.percussion.services.contentmgr.impl.PSContentUtils;
import com.percussion.utils.jsr170.PSPropertyDefinition;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.utils.types.PSPair;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The type configuration is the heart of the legacy content repository. This is
 * what knows about table and field mappings, and what classes are involved.
 * Methods on this class enable the content repository to configure the
 * Hibernate session factory.
 * <p>
 * Each configuration describes a single content type or child.
 * <p>
 * This also implements the {@link NodeType} interface for JSR-170 as it has
 * all the appropriate data available.
 * 
 * @author dougrand
 * 
 */
public class PSTypeConfiguration implements NodeType, Serializable
{
   /**
    * Serial id to detect modified object classes on deserialization
    */
   private static final long serialVersionUID = 1L;

   /**
    * Content status table name
    */
   private static final String CONTENTSTATUS = "CONTENTSTATUS";

   /**
    * An enumeration that explains the kind of id that is used when looking up
    * instances in the database
    */
   public enum IDType {
      /**
       * Just the content id is used, as in the contentstatus table
       */
      CONTENTID,
      /**
       * The content id + revision id is used, as for all normal content tables
       */
      NORMALID,
      /**
       * The sysid is used, as for child tables
       */
      CHILDID;
   }

   /**
    * A static logger
    */
   private static final Logger ms_logger = LogManager.getLogger("PSTypeConfiguration");

   /**
    * Generate class names for classes generated that map tables using hibernate
    */
   public static class TypeNamingPolicy extends DefaultNamingPolicy
   {
      /**
       * Suffix iterator
       */
      static AtomicInteger ms_suffix = new AtomicInteger(0);

      /**
       * Base name, never <code>null</code> after ctor
       */
      String m_baseName;

      /**
       * Ctor
       * 
       * @param baseName the base name, assumed not <code>null</code>
       * @param lazyFields if <code>true</code>, then the generated class
       *           contains blob/clob fields
       */
      public TypeNamingPolicy(String baseName, boolean lazyFields) {
         baseName = PSStringUtils.replaceNonIdChars(baseName);
         m_baseName = "com.percussion.services.generated."
               + baseName;
         if (lazyFields)
         {
            m_baseName += "_lobs";
         }
      }

      /* (non-Javadoc) 
       * @see net.sf.cglib.core.DefaultNamingPolicy#getClassName(java.lang.String,
       *      java.lang.String, java.lang.Object, net.sf.cglib.core.Predicate)
       */
      @Override
      @SuppressWarnings("unused")
      public String getClassName(String prefix, String source, Object key,
            Predicate inuse)
      {
         StringBuilder b = new StringBuilder();
         b.append(m_baseName);
         if (inuse.evaluate(b.toString()))
         {
            b.append("_");
            b.append(ms_suffix.addAndGet(1));
         }
         return b.toString();
      }
      
      @Override
      public boolean equals(Object obj)
      {
         return EqualsBuilder.reflectionEquals(this, obj);
      }
      
      @Override
      public int hashCode()
      {
         return new HashCodeBuilder(5, 6).append(m_baseName).toHashCode()
               + super.hashCode();
      }
   }

   /**
    * Represents an implementation class for the content
    */
   public static class ImplementingClass implements Serializable
   {
      /**
       * Serial id to detect modified object classes on deserialization
       */
      private static final long serialVersionUID = 1L;

      /**
       * The class, never <code>null</code>
       */
      private Class m_clazz;

      /**
       * The hibernate configuration string, never <code>null</code>
       */
      private String m_configuration;

      /**
       * <code>true</code> if this class is lazy loaded, used for body and
       * image fields
       */
      private boolean m_isLazyLoaded;

      /**
       * Ctor
       * 
       * @param clazz the class, assumed not <code>null</code>
       * @param config the hibernate configuration string, never
       *           <code>null</code> or empty
       * @param lazy if <code>true</code>, the class is lazy loaded
       */
      public ImplementingClass(Class clazz, String config, boolean lazy) 
      {
         if (clazz == null)
            throw new IllegalArgumentException("clazz may be not null.");
         
         m_clazz = clazz;
         m_configuration = config;
         m_isLazyLoaded = lazy;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (!(obj instanceof ImplementingClass))
            return false;
         
         ImplementingClass other = (ImplementingClass) obj;
         
         // avoid to compare "m_configuration" since it will be different
         // even if isSameBaseName(m_clazz, other.m_clazz) == true.
         
         return m_isLazyLoaded == other.m_isLazyLoaded
               && isSameBaseName(m_clazz, other.m_clazz);
      }

      @Override
      public int hashCode()
      {
         return new HashCodeBuilder(12, 39).append(m_isLazyLoaded).append(
               stripAddedTailNumber(m_clazz.getName())).toHashCode();
      }
      /**
       * @return Returns the clazz.
       */
      public Class getImplementingClass()
      {
         return m_clazz;
      }

      /**
       * @return Returns the configuration.
       */
      public String getConfiguration()
      {
         return m_configuration;
      }

      /**
       * @return Returns the isLazyLoaded.
       */
      public boolean isLazyLoaded()
      {
         return m_isLazyLoaded;
      }

      /* (non-Javadoc) 
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         StringBuilder b = new StringBuilder();
         b.append("<impclass class=");
         b.append(m_clazz.getCanonicalName());
         b.append(" config=");
         b.append(StringUtils.abbreviate(m_configuration, 200));
         b.append(" isLazy=");
         b.append(m_isLazyLoaded);
         b.append(">");
         return b.toString();
      }
   }

   /**
    * Determines if the given classes have the same base name, but may have 
    * different tail number. For example, "Foo"/"Foo_1 and 
    * "FooChild1_2"/"FooChild1_4" are considered using the same base name.
    * 
    * @param c1 the 1st class in question, assumed not <code>null</code>.
    * @param c2 the 2nd class in question, assumed not <cod>null</code>.
    * 
    * @return <code>true</code> if the names of both classes have the same
    *    base name; otherwise return <code>false</code>.
    */
   private static boolean isSameBaseName(Class c1, Class c2)
   {
      String c1Name = stripAddedTailNumber(c1.getName());
      String c2Name = stripAddedTailNumber(c2.getName());
      return c1Name.equals(c2Name);
   }

   /**
    * Strips the tail number for the given class name. 
    * For example, "FooChild1_1" will return "FooChild1".
    * 
    * @param className the class name, assumed not <code>null</code>.
    * 
    * @return the name without underscore and its following digit characters if
    *    there is any, never <code>null</code>, but may empty.
    */
   private static String stripAddedTailNumber(String className)
   {
      int i = className.lastIndexOf('_');
      if (i == -1)
         return className;
      
      String tail = className.substring(i+1);
      if (StringUtils.isNumeric(tail))
         return className.substring(0, i);
      else
         return className;
   }

   /**
    * Fake base class for generator to use. Work around generator bug that
    * doesn't allow a "null" superclass
    */
   public static class GeneratedClassBase implements Serializable
   {
      /**
       * Serial id to detect modified object classes on deserialization
       */
      private static final long serialVersionUID = 1L;

      /**
       * Ctor
       */
      public GeneratedClassBase() {

      }
   }

   /**
    * The name of the child field if this configuration represents a child,
    * <code>null</code> otherwise
    */
   private String m_childField;

   /**
    * The content type id, only valid for children
    */
   private long m_contenttypeid;

   /**
    * The content type id, only valid for children
    */
   private int m_childid;

   /**
    * The list of configurations for children of a parent node
    */
   private List<PSTypeConfiguration> m_children = new ArrayList<>();

   /**
    * The map of simple children for this type. Each map key is the name of a
    * simple child. The value is a pair of the table and value column
    */
   private Map<String, PSPair<String, String>> m_simpleChildProperties = 
      new HashMap<>();

   /**
    * The simple children's types are stored here
    */
   private Map<String, String> m_simpleChildTypes = new HashMap<>();

   /**
    * A set of tables used for any simple children
    */
   private Set<String> m_simpleChildTables = new HashSet<>();

   /**
    * Each configuration is based on at least one implementing class. A content
    * item is loaded by doing a load on the implementing classes, and wrapping
    * the instances in property wrappers to implement the properties on the
    * loaded item.
    * <p>
    * Each pair in this list holds a hibernate configuration plus an
    * implementing class.
    */
   private List<ImplementingClass> m_implementingClasses = 
      new ArrayList<>();

   /**
    * Each class maps to a list of properties, the properties are used to create
    * properties elements in the content node when loading an item.
    */
   private Map<Class, List<String>> m_properties = 
      new HashMap<>();

   /**
    * Tracks the loading mechanism used for each class
    */
   private Map<Class, IDType> m_loadpolicy = 
      new HashMap<>();

   /**
    * Some properties represent item bodies, which means that they require a
    * more interesting property wrapper that allows interception to expand the
    * body on access. The expansion calls the assembly manager to expand inline
    * templates and links.
    */
   private Set<String> m_bodyProperties = new HashSet<>();

   /**
    * Some properties are not loaded until first accessed. These are recorded in
    * this set and used when creating the properties.
    */
   private Set<String> m_lazyLoadProperties = new HashSet<>();

   /**
    * The name of the type that this configuration represents, never
    * <code>null</code> after construction
    */
   private String m_type;

   /**
    * If <code>true</code>, this is a sorted child
    */
   private boolean m_sortedChild;

   public Map<String, Class> getM_fieldToType() {
      return m_fieldToType;
   }

   public void setM_fieldToType(Map<String, Class> m_fieldToType) {
      this.m_fieldToType = m_fieldToType;
   }

   /**
    * A map that associates a specific property with a type. The type is a basic
    * Java type such as date, string, float, etc.
    */
   private Map<String, Class> m_fieldToType;

   /**
    * A map that associates a property name to the original PSField. Used to
    * find information about the field when creating properties.
    */
   private Map<String, PSField> m_fieldToField;

   /**
    * A set of strings that correspond to this types tables. If this is a 
    * content type the tables are indexed on contentid + revisionid. If this
    * is a child type then the tables are indexed on contentid + revisionid + 
    * sysid. Simple children are stored using just contentid + revisionid.
    */
   private Set<String> m_tableNames;
   
   /**
    * Contains the local table for the content editor. Will never be
    * <code>null</code> after initialization for a valid ce.
    */
   private String m_firstTable = null;
   
   /**
    * Each entry is a single property definition. These property definitions
    * allow either single or wholesale querying of the property structure. They
    * are created on first access and must be guarded against multi thread
    * access.
    */
   private Map<String, PSPropertyDefinition> m_propertyDefinitions = null;
   
   /**
    * Type map used to translate simple child types back to a java class. If
    * not in this map, then the translation isn't meaningful. Used for search
    * mechanism.
    */
   private static Map<String,Class> ms_hibernateTypeMap = 
      new HashMap<>();
   /**
    * Initialize type map
    */
   static {
      ms_hibernateTypeMap.put("boolean", Boolean.class);
      ms_hibernateTypeMap.put("date", Date.class);
      ms_hibernateTypeMap.put("datetime", Date.class);
      ms_hibernateTypeMap.put("time", Date.class);
      ms_hibernateTypeMap.put("double", Double.class);
      ms_hibernateTypeMap.put("string", String.class);
      ms_hibernateTypeMap.put("long", Long.class);
   }

   /**
    * Create a new configuration, either for a parent type or a child. Children
    * in the legacy repository are never more than one level below their parent.
    * Children are accessed by child id, which may only be valid while the
    * server is running.
    * 
    * @param definition The parent item's definition, never <code>null</code>
    * @param child The child definition, may be <code>null</code> if this
    *           configuration is for a parent
    * @param isDerbyDatabase if <code>true</code> then the repository is a 
    *      derby database.
    */
   @SuppressWarnings("unchecked")
   public PSTypeConfiguration(PSItemDefinition definition, PSItemChild child, 
         boolean isDerbyDatabase) 
   {
      Iterator<PSField> fiter;
      Map<String, List<String>> fieldsByTable = new HashMap<>();
      Map<String, String> fieldToColumnName = new HashMap<>();
      m_fieldToType = new HashMap<>();
      m_fieldToField = new HashMap<>();
      m_tableNames = new HashSet<>();
      m_sortedChild = false;
      boolean isComplexChild = child != null;
      if (!isComplexChild)
      {
         m_childField = null;
         m_type = definition.getName();
         fiter = definition.getParentFields();
      }
      else
      {
         fiter = definition.getChildFields(child.getChildId());
         m_contenttypeid = definition.getTypeId();
         m_childid = child.getChildId();
         m_childField = child.getName();
         m_type = definition.getName() + "_" + child.getName();
         m_sortedChild = child.isSequenced();
      }
      
      processFields(definition, fiter, fieldsByTable, fieldToColumnName,
            m_fieldToType, m_tableNames, isComplexChild);
      calculateConfiguration(m_tableNames, fieldsByTable, fieldToColumnName,
            m_fieldToType, m_sortedChild, isDerbyDatabase);
   }

   /**
    * It is similar as the {@link Object#equals(Object)}, except it uses 
    * {@link PSField#equalMetaData(Object)} to compare field objects. It also
    * considers the {@link Class} objects are the same if the base name of
    * their class names are the same.
    * 
    * @param obj the object in question, may be <code>null</code>.
    * 
    * @return <code>true</code> if the meta data of the supplied object equals
    * this.
    */
   public boolean equalMetaData(Object obj)
   {
      if (! (obj instanceof PSTypeConfiguration))
         return false;
      
      PSTypeConfiguration other = (PSTypeConfiguration) obj;
      
      boolean isEqual = new EqualsBuilder()
         .append(m_bodyProperties, other.m_bodyProperties)
         .append(m_childField, other.m_childField)
         .append(m_childid, other.m_childid)
         .append(m_children, other.m_children)
         .append(m_contenttypeid, other.m_contenttypeid)
         .append(m_fieldToType, other.m_fieldToType)
         .append(m_firstTable, other.m_firstTable)
         .append(m_implementingClasses, other.m_implementingClasses)
         .append(m_lazyLoadProperties, other.m_lazyLoadProperties)
         .append(m_propertyDefinitions, other.m_propertyDefinitions)
         .append(m_simpleChildProperties, other.m_simpleChildProperties)
         .append(m_simpleChildTables, other.m_simpleChildTables)
         .append(m_simpleChildTypes, other.m_simpleChildTypes)
         .append(m_sortedChild, other.m_sortedChild)
         .append(m_tableNames, other.m_tableNames)
         .append(m_type, other.m_type)
         .isEquals();

      boolean isEqualFields = isEqualFieldMetaData(m_fieldToField,
            other.m_fieldToField);
      
      boolean isEqualPolicy = isEqualMap(m_loadpolicy, other.m_loadpolicy);
      boolean isEqualProps = isEqualMap(m_properties, other.m_properties);
      
      return isEqual && isEqualFields && isEqualPolicy && isEqualProps;
   }

   /**
    * Determines if the given pair of maps contain the same set of fields
    * where the meta data of the fields are the same.
    *  
    * @param fdMap1 a map that maps field name to its field object, assumed not
    *    <code>null</code>.
    * @param fdMap2 the 2nd map in question, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if both contain the same set of fields where
    * the meta data of the fields are equal.
    */
   private boolean isEqualFieldMetaData(Map<String, PSField> fdMap1,
         Map<String, PSField> fdMap2)
   {
      if (fdMap1.size() != fdMap2.size())
         return false;
      
      for (Map.Entry<String, PSField> e : fdMap1.entrySet())
      {
         PSField f2 = fdMap2.get(e.getKey());
         if (f2 == null || (!f2.equalMetaData(e.getValue())))
            return false;
      }
      
      return true;
   }
   
   /**
    * Determines if the given maps are equal. 
    * Using {@link #isSameBaseName(Class, Class)} to compare the keys.
    * 
    * @param m1 the 1st map in question, assumed not <code>null</code>.
    * @param m2 the 2nd map in question, assumed not <code>null</code>.
    *    
    * @return <code>true</code> the maps are equal. 
    */
   @SuppressWarnings("unchecked")
   private boolean isEqualMap(Map<Class, ? extends Object> m1,
         Map<Class, ? extends Object> m2)
   {
      if (m1.size() != m2.size())
         return false;
      
      for (Class k : m1.keySet())
      {
         Object v2 = getMapValue(k, m2);
         if (v2 == null || (!v2.equals(m1.get(k))) )
            return false;
         
      }
      return true;
   }
   
   /**
    * Gets the map value from the given map where the class name of the map key 
    * has the same base name as the supplied key. 
    * See {@link #isSameBaseName(Class, Class)} for detail.
    *  
    * @param srcKey the lookup key, assumed not <code>null</code>.
    * @param m the map that may contain the value, assumed not <code>null</code>.
    * 
    * @return the map value where the map key has the same base name as the 
    *    supplied key. It may be <code>null</code> if cannot find one in the map.
    */
   @SuppressWarnings("unchecked")
   private Object getMapValue(Class srcKey, Map<Class, ? extends Object> m)
   {
      for (Class k : m.keySet())
      {
         if (isSameBaseName(srcKey, k))
            return m.get(k);
      }
      return null;
   }
   
   /**
    * Get the system definition and add all the unmapped fields from the 
    * repository to the property definitions.
    */
   @SuppressWarnings("unchecked")
   private void addSystemFieldPropertyDefinitions()
   {
      PSContentEditorSystemDef systemDef = 
         PSServer.getContentEditorSystemDef();
      IPSContentRepository rep = 
         PSContentInternalLocator.getLegacyRepository();
      Set<String> systemFields = rep.getUnmappedSystemFields();
      PSFieldSet systemDefFields = systemDef.getFieldSet();
      Iterator fiter = systemDefFields.getEveryField();
      while(fiter.hasNext())
      {
         PSField field = (PSField) fiter.next();
         String name = field.getSubmitName();
         String prop = PSContentUtils.externalizeName(name);
         if (systemFields.contains(prop))
         {
            addPropertyDefinition(prop, field);
         }
      }
      
   }

   /**
    * Process fields and fill the various passed maps and table data. This
    * method initializes the state of the configuration object as a side effect.
    * 
    * @param definition the item definition
    * @param fiter an iterator over the fields being processed
    * @param fieldsByTable for each table, a list of fields is stored
    * @param fieldToColumnName a mapping from the field name to the column name
    *           is stored
    * @param fieldToType a mapping from the field name to the type is stored
    * @param tableNames a set of table names
    * @param isComplexChild a flag to indicate whether the fields belong to
    *           complex child or parent.
    */
   private void processFields(PSItemDefinition definition,
         Iterator<PSField> fiter, Map<String, List<String>> fieldsByTable,
         Map<String, String> fieldToColumnName, Map<String, Class> fieldToType,
         Set<String> tableNames, boolean isComplexChild)
   {
      while (fiter.hasNext())
      {
         PSField field = fiter.next();
         PSFieldSet simpleChildSet = definition.getSimpleChildSet(field
               .getSubmitName());
         if (simpleChildSet != null)
         {
            // Simple children
            // There will be one field, which will give us the table, column
            // and property name for the simple child
            Iterator childfields = simpleChildSet.getAll();
            PSField childField = (PSField) childfields.next();
            String parts[] = getFieldColumns(childField);
            String prop = childField.getSubmitName();
            m_simpleChildProperties.put(prop, new PSPair<>(
                  parts[0], parts[1]));
            m_simpleChildTypes.put(prop, getHibernateMappingType(childField));
            m_simpleChildTables.add(parts[0]);
            m_fieldToField.put(prop, childField);
         }

         String parts[] = getFieldColumns(field);
         if (parts == null || simpleChildSet != null)
         {
            // Skip fields that don't map to the database
            continue;
         }
         String table = parts[0];
         String column = parts[1];
         // Added isComplexChild condition in order to process the fields from
         // shared complex childs.
         if ((field.getType() == PSField.TYPE_LOCAL || isComplexChild)
               && m_firstTable == null)
            m_firstTable = table;
         tableNames.add(table);

         List<String> fields = fieldsByTable.get(table);
         if (fields == null)
         {
            fields = new ArrayList<>();
            fieldsByTable.put(table, fields);
         }
         fields.add(field.getSubmitName());
         if (field.cleanupBrokenInlineLinks() && field.mayHaveInlineLinks())
         {
            m_bodyProperties.add(PSContentUtils.externalizeName(field
                  .getSubmitName()));
         }
         fieldToColumnName.put(field.getSubmitName(), column);
         Class fieldType = calculateType(field);
         fieldToType.put(field.getSubmitName(), fieldType);
         m_fieldToField.put(field.getSubmitName(), field);
         if (fieldType.equals(Clob.class) || fieldType.equals(Blob.class))
         {
            m_lazyLoadProperties.add(field.getSubmitName());
         }
      }
   }

   /**
    * Extract column and table information from a field
    * 
    * @param field the field, assumed not <code>null</code>
    * @return an array of exactly two elements, the first is the table and the
    *         second is the column. If there is any error, this method returns
    *         <code>null</code>
    */
   private String[] getFieldColumns(PSField field)
   {
      IPSBackEndMapping mapping = field.getLocator();
      String columns[] = mapping.getColumnsForSelect();

      if (columns == null || columns.length != 1)
      {
         return null;
      }
      String parts[] = columns[0].split("\\x2e");
      if (parts.length == 2)
         return parts;
      else
         return null;
   }

   /**
    * Calculate the correct type for a property that can represent the given
    * field
    * 
    * @param field the field, assumed never <code>null</code>
    * @return the class that represents the given field's data type
    */
   private Class calculateType(PSField field)
   {
      String type = field.getDataType();
      if (type.equals(PSField.DT_BINARY) || type.equals(PSField.DT_IMAGE))
      {
         return Blob.class;
      }
      else if (type.equals(PSField.DT_BOOLEAN))
      {
         return Boolean.class;
      }
      else if (type.equals(PSField.DT_DATE) || type.equals(PSField.DT_DATETIME)
            || type.equals(PSField.DT_TIME))
      {
         return Date.class;
      }
      else if (type.equals(PSField.DT_FLOAT))
      {
         return Double.class;
      }
      else if (type.equals(PSField.DT_TEXT))
      {
         String format = field.getDataFormat();
         if (format != null && format.equals("max"))
            return Clob.class;
         else
            return String.class;
      }
      else if (type.equals(PSField.DT_INTEGER))
      {
         return Long.class;
      }
      else
      {
         throw new RuntimeException("Unknown type: " + type);
      }
   }

   /**
    * Translate a passed field to an appropriate hibernate mapping type
    * 
    * @param field the field, assumed never <code>null</code>
    * @return the type, never <code>null</code>
    */
   private String getHibernateMappingType(PSField field)
   {
      String type = field.getDataType();
      if (type.equals(PSField.DT_BINARY) || type.equals(PSField.DT_IMAGE))
      {
         return "blob";
      }
      else if (type.equals(PSField.DT_BOOLEAN))
      {
         return "boolean";
      }
      else if (type.equals(PSField.DT_DATE))
      {
         return "date";
      }
      else if (type.equals(PSField.DT_DATETIME))
      {
         return "datetime";
      }
      else if (type.equals(PSField.DT_TIME))
      {
         return "time";
      }
      else if (type.equals(PSField.DT_FLOAT))
      {
         return "double";
      }
      else if (type.equals(PSField.DT_TEXT))
      {
         String format = field.getDataFormat();
         if (format != null && format.equals("max"))
            return "clob";
         else
            return "string";
      }
      else if (type.equals(PSField.DT_INTEGER))
      {
         return "long";
      }
      else
      {
         throw new RuntimeException("Unknown type: " + type);
      }
   }

   /**
    * From the field, table and column information, create two things:
    * <ul>
    * <li>One or more property beans to hold the data
    * <li>Corresponding hibernate configurations for each bean
    * </ul>
    * The results get stored in the {@link #m_implementingClasses} list.
    * <p>
    * One table is specially handled, i.e. <code>CONTENTSTATUS</code>. This
    * is automatically mapped to the class {@link PSComponentSummary}, which is
    * already setup as part of the system.
    * 
    * @param tableNames the tables involved in this type, assumed never
    *           <code>null</code>
    * @param fieldsByTable a map that goes from a specific table to a list of
    *           field names, assumed never <code>null</code>
    * @param fieldToColumnName a map that correlates fields with columns,
    *           assumed never <code>null</code>
    * @param fieldToType A map that correlates fields to specific data types
    * @param sortedchild if <code>true</code>, this is configuring a sorted
    *           child field
    * @param isDerbyDatabase if <code>true</code> then the repository is a 
    *      derby database.
    */
   private void calculateConfiguration(Set<String> tableNames,
         Map<String, List<String>> fieldsByTable,
         Map<String, String> fieldToColumnName, Map<String, Class> fieldToType,
         boolean sortedchild, boolean isDerbyDatabase)
   {
      String firstTable = m_firstTable;

      if (firstTable == null)
      {
         ms_logger.warn("Content type had no tables: " + m_type);
         return;
      }

      handleImplementationClass(tableNames, fieldsByTable, fieldToColumnName,
            fieldToType, sortedchild, firstTable, false, isDerbyDatabase);

      if (m_lazyLoadProperties.size() > 0)
      {
         handleImplementationClass(tableNames, fieldsByTable,
               fieldToColumnName, fieldToType, sortedchild, firstTable, true, 
               isDerbyDatabase);
      }

      // Add PSComponentSummary
      if (isParent())
      {
         Class cs = PSComponentSummary.class;
         m_implementingClasses.add(new ImplementingClass(cs, null, false));
         m_properties.put(cs, fieldsByTable.get(CONTENTSTATUS));
         m_loadpolicy.put(cs, IDType.CONTENTID);
      }
   }

   /**
    * Handles the details of creating the bean and configuration. This is called
    * twice. Once for the normal fields and once if there are any lazy loaded
    * fields.
    * 
    * @param tableNames
    * @param fieldsByTable
    * @param fieldToColumnName
    * @param fieldToType
    * @param sortedchild
    * @param firstTable
    * @param lazyFields
    * @param isDerbyDatabase if <code>true</code> then the repository is a 
    * derby database.
    */
   private void handleImplementationClass(Set<String> tableNames,
         Map<String, List<String>> fieldsByTable,
         Map<String, String> fieldToColumnName, Map<String, Class> fieldToType,
         boolean sortedchild, String firstTable, boolean lazyFields, 
         boolean isDerbyDatabase)
   {
      IDType load;
      List<String> props = new ArrayList<>();
      StringBuilder hibProps = new StringBuilder(512);
      StringBuilder hibId = new StringBuilder(128);
      StringBuilder hibJoin = new StringBuilder(128);
      BeanGenerator gen = createBeanGenerator(lazyFields);

      for (String table : tableNames)
      {
         if (table.equals(CONTENTSTATUS) || m_simpleChildTables.contains(table))
            continue;
         StringBuilder pertableprops = new StringBuilder(128);

         // Add all the properties
         handleProperties(fieldsByTable, fieldToColumnName, fieldToType,
               firstTable, props, hibProps, gen, table, pertableprops,
               lazyFields, isDerbyDatabase);

         // Handle join
         if (!table.equals(firstTable))
         {
            addHibernateJoin(hibJoin, table, pertableprops.toString());
         }
      }

      // Simple children are joined back using CONTENTID + REVISIONID
      if (m_simpleChildProperties.size() > 0 && !lazyFields)
      {
         handleSimpleChildren(props, hibProps, gen);
      }

      if (isParent())
      {
         load = IDType.NORMALID;
         handleParentId(hibProps, hibId, gen);
      }
      else
      {
         load = IDType.CHILDID;
         handleChildId(sortedchild, hibProps, hibId, gen);
      }

      // Finish the configuration for hibernate
      gen.setUseCache(false); // Do not reuse classes!

      Class beanClass = (Class) gen.createClass();
      String hibConfig = buildHibernateConfiguration(firstTable, hibProps,
            hibId, hibJoin, beanClass);

      // Add to the implementing classes
      m_implementingClasses.add(new ImplementingClass(beanClass, hibConfig
            .toString(), lazyFields));
      m_properties.put(beanClass, props);
      m_loadpolicy.put(beanClass, load);
   }

   /**
    * Handle all the property mappings for the implementation class.
    * 
    * @param fieldsByTable
    * @param fieldToColumnName
    * @param fieldToType
    * @param firstTable
    * @param props
    * @param hibProps
    * @param gen
    * @param table
    * @param pertableprops
    * @param lazyFields if <code>true</code> then only map the lazy fields for
    *           properties, if <code>false</code> only map the non-lazy fields
    * @param isDerbyDatabase if <code>true</code> then the repository is a 
    * derby database.
    */
   private void handleProperties(Map<String, List<String>> fieldsByTable,
         Map<String, String> fieldToColumnName, Map<String, Class> fieldToType,
         String firstTable, List<String> props, StringBuilder hibProps,
         BeanGenerator gen, String table, StringBuilder pertableprops,
         boolean lazyFields, boolean isDerbyDatabase)
   {
      for (String field : fieldsByTable.get(table))
      {
         // Don't add the Content Id and Revision fields:
         // They are join columns handled by the "addHibernateXXXX" methods.
         if (field.equals("sys_contentid") || field.equals("sys_revision"))
         {
            continue;
         }

         // Decide if field should be added to main or auxiliary class: 
         // -Lazy loaded (LOB) fields go in auxiliary class.
         boolean isLazy = m_lazyLoadProperties.contains(field);
         if ((!lazyFields && isLazy) || (lazyFields && !isLazy))
            continue;

         // Add the field to the generated class bean.
         Class type = fieldToType.get(field);
         
         if (isDerbyDatabase)
         {
            // Note special processing (for Derby, specifically).
            //  CLOBs will be materialized as a String field.
            //  BLOBs will be materialized as an Array of Bytes.
            if (type.getName().equals("java.sql.Clob"))
            {
               type = String.class;
            }
            else if (type.getName().equals("java.sql.Blob"))
            {
               type = byte[].class;            
            }
         }
         gen.addProperty(field, type);


         // Decide to which stream to append the property info.
         // "firstTable" represents the content types distinct fields.
         StringBuilder appendTo = null;
         if (table.equals(firstTable))
         {
            appendTo = hibProps;
         }
         else
         {
            appendTo = pertableprops;
            appendTo.append("  ");
         }

         // Get the name of the DB column to which this property is mapped.
         // Add any special properties needed in the Hibernate properties file.
         // Add field to the list of properties.
         String column = fieldToColumnName.get(field);
         addHibernateProperty(field, column, appendTo, isLazy, type);
         props.add(field);
      }
   }

   /**
    * Create a configured bean generator
    * 
    * @param lazyFields if <code>true</code> then this bean is for the lazy
    *           fields
    * 
    * @return the bean generator
    */
   private BeanGenerator createBeanGenerator(boolean lazyFields)
   {
      BeanGenerator gen = new BeanGenerator();
      gen.setSuperclass(GeneratedClassBase.class);
      gen.setClassLoader(getClass().getClassLoader());
      String pcasetable = StringUtils.capitalize(m_type);
      NamingPolicy policy = new TypeNamingPolicy(pcasetable, lazyFields);
      gen.setNamingPolicy(policy);
      return gen;
   }

   /**
    * Build the complete hibernate configuration from the calculated pieces.
    * Keeps the cache region for the parent and child items at a different
    * region as the cache keys are different for them.
    * 
    * @param firstTable
    * @param hibProps
    * @param hibId
    * @param hibJoin
    * @param beanClass
    * @return the hibernate configuration string, never <code>null</code> or
    *         empty
    */
   private String buildHibernateConfiguration(String firstTable,
         StringBuilder hibProps, StringBuilder hibId, StringBuilder hibJoin,
         Class beanClass)
   {
      StringBuilder hibConfig = new StringBuilder(512);
      hibConfig.append("<?xml version=\"1.0\"?>\n");
      hibConfig
            .append("<!DOCTYPE hibernate-mapping PUBLIC\r\n"
                  + "      \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\r\n"
                  + "          \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
      hibConfig.append("<hibernate-mapping>\n");
      hibConfig.append("<class name=\"");
      hibConfig.append(beanClass.getName());
      hibConfig.append("\" table=\"");
      hibConfig.append(firstTable);
      hibConfig.append("\">\n");
      if (!beanClass.getName().contains("_lobs"))
      {
         if (isParent())
         {
            hibConfig
                  .append("   <cache usage=\"read-write\" region=\"item\" />\n");
         }
         else
         {
            hibConfig
                  .append("   <cache usage=\"read-write\" region=\"childitem\" />\n");
         }
      }

      hibConfig.append(hibId);
      hibConfig.append(hibProps);
      hibConfig.append(hibJoin);
      hibConfig.append("</class>\n");
      hibConfig.append("</hibernate-mapping>\n");
      if (ms_logger.isDebugEnabled())
      {
         ms_logger.debug("Hibernate config for class-name = \""
               + beanClass.getName() + "\"\n" + hibConfig.toString());
      }
      return hibConfig.toString();
   }

   /**
    * Create the hibernate configuration and bean properties for simple
    * children. Simple children are those that will appear in the node as multi
    * valued properties.
    * 
    * @param props the current properties, assumed never <code>null</code>,
    *           will be modified by this method
    * @param hibProps the hibernate configuration being build, assumed never
    *           <code>null</code>, will be modified by this method
    * @param gen the current bean generator, assumed never <code>null</code>,
    *           will be modified by this method
    */
   private void handleSimpleChildren(List<String> props,
         StringBuilder hibProps, BeanGenerator gen)
   {
      for (String property : getSimpleChildProperties())
      {
         PSPair<String, String> mapping = m_simpleChildProperties.get(property);
         String type = m_simpleChildTypes.get(property);
         gen.addProperty(property, List.class);
         addHibernateBag(hibProps, property, mapping, type);
         props.add(property);
      }
   }

   /**
    * Handle the child id field details
    * 
    * @param sortedchild <code>true</code> if this child must maintain a sort
    *           order
    * @param hibProps the properties stringbuilder, assumed not
    *           <code>null</code>
    * @param hibId the id stringbuilder, assumed not <code>null</code>
    * @param gen the properties generator, assumed not <code>null</code>
    */
   private void handleChildId(boolean sortedchild, StringBuilder hibProps,
         StringBuilder hibId, BeanGenerator gen)
   {
      // All children have the contentid and revision as foreign
      // keys.
      addHibernateProperty("sys_contentid", "CONTENTID", hibProps, false,
            Integer.class);
      addHibernateProperty("sys_revision", "REVISIONID", hibProps, false,
            Integer.class);
      hibId.append("<id \n");
      hibId.append("  name=\"sys_sysid\" column=\"SYSID\">\n");
      hibId.append("  <generator class=\"assigned\"/>\n");
      hibId.append("</id>\n");
      gen.addProperty("sys_sysid", Integer.class);
      gen.addProperty("sys_contentid", Integer.class);
      gen.addProperty("sys_revision", Integer.class);

      if (sortedchild)
      {
         gen.addProperty("sys_sortrank", Integer.class);
         addHibernateProperty("sys_sortrank", "SORTRANK", hibProps, false,
               Integer.class);
      }
   }

   /**
    * Handle the parent id information
    * 
    * @param hibProps the properties stringbuilder, assumed not
    *           <code>null</code>
    * @param hibId the id stringbuilder, assumed not <code>null</code>
    * @param gen the properties generator, assumed not <code>null</code>
    */
   private void handleParentId(StringBuilder hibProps, StringBuilder hibId,
         BeanGenerator gen)
   {
      addHibernateCompositeKey(hibId);
      gen.addProperty(PSContentNode.ID_PROPERTY_NAME, PSLegacyCompositeId.class);

      // Link to PSComponentSummary
      gen.addProperty("sys_componentsummary", PSComponentSummary.class);
      addHibernateManyToOne(hibProps, "sys_componentsummary",
            PSComponentSummary.class, "CONTENTID",
            " insert=\"false\" update=\"false\"");
   }

   /**
    * Add a many to one mapping to the properties
    * 
    * @param builder
    * @param property
    * @param targetentity
    * @param column
    * @param extras
    */
   private void addHibernateManyToOne(StringBuilder builder, String property,
         Class targetentity, String column, String extras)
   {
      builder.append("<many-to-one name=\"");
      builder.append(property);
      builder.append("\" class=\"");
      builder.append(targetentity.getName());
      builder.append("\" column=\"");
      builder.append(column);
      builder.append("\" cascade=\"all\"");
      builder.append(extras);
      builder.append("/>\n");
   }

   /**
    * Add a bag mapping to the properties
    * 
    * @param builder
    * @param property
    * @param mapping
    * @param type
    */
   private void addHibernateBag(StringBuilder builder, String property,
         PSPair<String, String> mapping, String type)
   {
      builder.append("<bag name='");
      builder.append(property);
      builder.append("' table='");
      builder.append(mapping.getFirst());
      builder.append("' inverse='true' lazy='extra'>\n");
      builder.append("   <cache usage=\"read-write\" region=\"item\" />\n");
      builder.append("   <key>\n");
      builder.append("      <column name='CONTENTID'/>\n");
      builder.append("      <column name='REVISIONID'/>\n");
      builder.append("   </key>\n");
      builder.append("   <element type='");
      builder.append(type);
      builder.append("' column='");
      builder.append(mapping.getSecond());
      builder.append("' not-null='true' unique='false'/>\n");
      builder.append("</bag>\n");
   }

   /**
    * Add a composite key to the hibernate configuration
    * 
    * @param builder
    */
   private void addHibernateCompositeKey(StringBuilder builder)
   {
      builder.append("<composite-id "
            + "class='com.percussion.services.contentmgr.impl"
            + ".legacy.PSLegacyCompositeId' name='"
            + PSContentNode.ID_PROPERTY_NAME 
            + "'>\n");
      builder.append("   <key-property name=\"sys_contentid\""
            + " column=\"CONTENTID\" />\n");
      builder.append("   <key-property name=\"sys_revision\""
            + " column=\"REVISIONID\" />\n");
      builder.append("</composite-id>\n");
   }

   /**
    * Add hibernate join configuration
    * 
    * @param builder the builder to append to
    * @param table the table name
    * @param properties the properties for this table
    */
   private void addHibernateJoin(StringBuilder builder, String table,
         String properties)
   {
      builder.append("<join table=\"");
      builder.append(table);
      builder.append("\" optional=\"true\" >\n");
      builder.append("  <key>\n");
      builder.append("     <column name=\"CONTENTID\"/>\n");
      builder.append("     <column name=\"REVISIONID\"/>\n");
      builder.append("  </key>\n");
      builder.append(properties);
      builder.append("</join>\n");
   }

   /**
    * Add a hibernate property to the passed string builder
    * 
    * @param field the field, assumed not <code>null</code>
    * @param column the column, assumed not <code>null</code>
    * @param builder the builder, assumed not <code>null</code>
    * @param isLazy if <code>true</code> then this is a lob property
    * @param type the type of the property, assumed not <code>null</code> 
    */
   @SuppressWarnings("unchecked")
   private void addHibernateProperty(String field, String column,
         StringBuilder builder, boolean isLazy, Class type)
   {
      builder.append("<property name=\"");
      builder.append(field);
      builder.append("\" column=\"");
      builder.append(column);
      builder.append("\" ");
      if (isLazy)
      {
         String t;
         if (type.isAssignableFrom(String.class))
         {
            t = "text";
         }
         else if (type.isAssignableFrom(Clob.class))
         {
            t = "clob";
         }
         else if (type.isAssignableFrom(Blob.class))
         {
            t = "blob";
         }
         else
         {
            t = "binary";
         }
         builder.append("type=\"");
         builder.append(t);
         builder.append("\" ");
      }
      builder.append("/>\n");
   }

   /**
    * Each content type maps to a list of implementing classes. This method
    * returns a list where the first element is the hibernate configuration and
    * the second element is the name of the implementing class.
    * <p>
    * Implementing classes, with the exception of {@link PSComponentSummary} are
    * all generated classes. The generated classes are recreated on restart of
    * the system or an individual content editor.
    * <p>
    * For all main content types this list includes {@link PSComponentSummary},
    * plus at least one other implementing class.
    * 
    * @return Returns the implementingClasses, never <code>null</code> and
    *         never empty
    */
   public List<ImplementingClass> getImplementingClasses()
   {
      return m_implementingClasses;
   }

   /**
    * Does this represent a content type?
    * 
    * @return returns <code>true</code> if this type configuration represents
    *         a content type and not a child
    */
   public boolean isParent()
   {
      return m_childField == null;
   }

   /**
    * Some properties represent item bodies, which means that they require a
    * more interesting property wrapper that allows interception to expand the
    * body on access. The expansion calls the assembly manager to expand inline
    * templates and links.
    * 
    * @return Returns the bodyProperties, may be empty but not <code>null</code>
    */
   public Set<String> getBodyProperties()
   {
      return m_bodyProperties;
   }

   /**
    * @return Returns the simpleChildProperties.
    */
   public Set<String> getSimpleChildProperties()
   {
      return m_simpleChildProperties.keySet();
   }

   /**
    * @return Returns the sortedChild.
    */
   public boolean isSortedChild()
   {
      return m_sortedChild;
   }

   /**
    * @return Returns the loadpolicy.
    */
   public Map<Class, IDType> getLoadpolicy()
   {
      return m_loadpolicy;
   }

   /**
    * @return Returns the properties.
    */
   public Map<Class, List<String>> getProperties()
   {
      return m_properties;
   }

   /**
    * Get the type of a given property
    * 
    * @param property the name of the property, never <code>null</code> or
    *           empty
    * @return the type, never <code>null</code>
    */
   public Class getPropertyType(String property)
   {
      if (StringUtils.isBlank(property))
      {
         throw new IllegalArgumentException("property may not be null or empty");
      }
      // Strip "rx:"
      if (property.startsWith("rx:"))
      {
         property = property.substring(3);
      }

      // Special handling for jcr properties
      if (property.startsWith("jcr:"))
      {
         if (property.equals("jcr:path") || property.equals("jcr:primaryType")
               || property.equals("jcr:mixinType"))
            return String.class;
         else
         {
            throw new IllegalArgumentException("Unknown jcr property "
                  + property);
         }
      }

      // Special handling for relationship properties
      if (property.startsWith("f."))
      {
         return Long.class;
      }

      Class type = m_fieldToType.get(property);
      // Try simple children
      if (m_simpleChildTypes.containsKey(property))
      {
         String typename = m_simpleChildTypes.get(property);
         type = ms_hibernateTypeMap.get(typename);
      }
      if (type == null)
      {
         throw new IllegalArgumentException("property " + property
               + " unknown, or the type of the property cannot be returned");
      }
      return type;
   }

   /**
    * Get all properties supported by this type, externalized for comparison
    * with JSR-170 properties. Includes simple children but does not include
    * complex children
    * 
    * @return a list, never <code>null</code> or empty
    */
   public Set<String> getAllJSR170Properties()
   {
      Set<String> props = new HashSet<>();

      for (ImplementingClass cr : m_implementingClasses)
      {
         List<String> plist = m_properties.get(cr.getImplementingClass());
         if (plist == null)
            continue;

         for (String prop : plist)
         {
            if (cr.getImplementingClass() == PSComponentSummary.class)
            {
               // Limit to real fields for component summary
               if (PSContentRepository.mapCSFieldToProperty(prop) == null)
                  continue;
            }
            props.add(PSContentUtils.externalizeName(prop));
         }
      }

      for (String prop : m_simpleChildProperties.keySet())
      {
         props.add(PSContentUtils.externalizeName(prop));
      }
      
      // Add additional properties we add for all 
      for (int i = 0; i < PSContentRepository.ms_fieldToAdd.length; i += 2)
      {
         String field = PSContentRepository.ms_fieldToAdd[i];
         props.add(field);
      }

      // Add contentid, revision
      props.add(IPSContentPropertyConstants.RX_SYS_CONTENTID);
      props.add(IPSContentPropertyConstants.RX_SYS_REVISION);
      
      return props;
   }

   /**
    * Add a child's configuration.
    * 
    * @param childconfig the child configuration, assumed never
    *           <code>null</code>
    */
   void addChildConfiguration(PSTypeConfiguration childconfig)
   {
      m_children.add(childconfig);
   }

   /**
    * Clear current children, only used when reloading the configurations.
    */
   void clearChildren()
   {
      m_children.clear();
   }

   /**
    * @return Returns the childField.
    */
   public String getChildField()
   {
      return m_childField;
   }

   /**
    * @return Returns the children.
    */
   public List<PSTypeConfiguration> getChildren()
   {
      return m_children;
   }

   /**
    * @return Returns the childid.
    */
   public int getChildid()
   {
      return m_childid;
   }

   /**
    * @return Returns the contenttypeid.
    */
   public long getContenttypeid()
   {
      return m_contenttypeid;
   }

   /**
    * @return Returns the type.
    */
   public String getType()
   {
      return m_type;
   }
   
   /**
    * A set of strings that correspond to this type's tables. If this is a 
    * content type the tables are indexed on contentid + revisionid. If this
    * is a child type then the tables are indexed on contentid + revisionid + 
    * sysid. Simple children are stored using just contentid + revisionid.
    * 
    * @return the tableNames, never <code>null</code> after construction
    */
   public Set<String> getTableNames()
   {
      return m_tableNames;
   }

   /**
    * Get the original PSField for the given property
    * 
    * @param property the property name without any prefix, never
    *           <code>null</code> or empty
    * @return the field, <code>null</code> if the property is unknown
    */
   public PSField getField(String property)
   {
      if (StringUtils.isBlank(property))
      {
         throw new IllegalArgumentException("property may not be null or empty");
      }
      return m_fieldToField.get(property);
   }

   /**
    * Find and return the first implementation class that isn't the component
    * summary
    * 
    * @return the implementation class, never <code>null</code> for a valid
    *         configuration
    */
   public Class getMainClass()
   {
      for (ImplementingClass c : m_implementingClasses)
      {
         if (c.getConfiguration() != null && !c.isLazyLoaded())
         {
            return c.getImplementingClass();
         }
      }
      return null;
   }

   /**
    * Return those properties that are not loaded initially
    * 
    * @return the properties, never <code>null</code> but may be empty
    */
   public Set<String> getLazyProperties()
   {
      return m_lazyLoadProperties;
   }

   /**
    * Find and return the class that is lazy loaded for the configuration
    * 
    * @return the class or <code>null</code> if this configuration did not
    *         require a lazy loader
    */
   public Class getLazyLoadClass()
   {
      for (ImplementingClass c : m_implementingClasses)
      {
         if (c.getConfiguration() != null && c.isLazyLoaded())
         {
            return c.getImplementingClass();
         }
      }
      return null;
   }

   /**
    * Return the simple child tables used
    * 
    * @return a set of child property tables, might be empty but not <code>null</code>
    */
   public Set<String> getSimpleChildPropertiesTables()
   {
      return m_simpleChildTables;
   }

   /**
    * If the property definitions haven't been created yet, calculated them.
    * This basically iterates through all the known JCR properties and creates
    * a property definition object based on the associated field information.
    */
   public synchronized void loadPropertyDefinitions()
   {
      if (m_propertyDefinitions != null)
         return;
      m_propertyDefinitions = new HashMap<>();
      Collection<String> props = m_fieldToField.keySet();
      for(String prop : props)
      {
         PSField field = m_fieldToField.get(prop);
         addPropertyDefinition(prop, field);
      }
      addSystemFieldPropertyDefinitions();
   }

   /**
    * Calculate the property definition for the given field and add it to the
    * set of stored definitions.
    * 
    * @param prop the property name, assumed never <code>null</code> or empty.
    * @param field the field, assumed never <code>null</code>.
    */
   private void addPropertyDefinition(String prop, PSField field)
   {
      String type = getHibernateMappingType(field);
      boolean multiple = m_simpleChildProperties.containsKey(prop);
      int itype;
      if (type.equals("blob"))
      {
         itype = PropertyType.BINARY;
      }
      else if (type.equals("boolean"))
      {
         itype = PropertyType.BOOLEAN;
      }
      else if (type.equals("date") || type.equals("datetime") ||
            type.equals("time"))
      {
         itype = PropertyType.DATE;
      }
      else if (type.equals("double"))
      {
         itype = PropertyType.DOUBLE;
      }
      else if (type.equals("string") || type.equals("clob"))
      {
         itype = PropertyType.STRING;
      }
      else if (type.equals("long"))
      {
         itype = PropertyType.LONG;
      }
      else
      {
         itype = PropertyType.UNDEFINED;
      }
      m_propertyDefinitions.put(prop, new PSPropertyDefinition(
            PSContentUtils.externalizeName(prop), multiple, itype, this));
   }
   
   /*
    * Node type methods, bare implementation
    */
   public boolean canAddChildNode(String arg0)
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean canAddChildNode(String arg0, String arg1)
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean canRemoveItem(String arg0)
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public boolean canSetProperty(String arg0, Value arg1)
   {
      return false;
   }

   public boolean canSetProperty(String arg0, Value[] arg1)
   {
      return false;
   }

   public NodeDefinition[] getChildNodeDefinitions()
   {
      return getDeclaredChildNodeDefinitions();
   }

   public NodeDefinition[] getDeclaredChildNodeDefinitions()
   {
      throw new UnsupportedOperationException("Not implemented");
   }

   public synchronized PropertyDefinition[] getDeclaredPropertyDefinitions()
   {
      loadPropertyDefinitions();
      Collection<PSPropertyDefinition> defs = m_propertyDefinitions.values();
      PropertyDefinition rval[] = new PropertyDefinition[defs.size()];
      defs.toArray(rval);
      return rval;
   }

   public NodeType[] getDeclaredSupertypes()
   {
      return null;
   }

   public String getName()
   {
      return PSContentUtils.externalizeName(m_type).replace(' ', '_');
   }

   public String getPrimaryItemName()
   {
      if (m_bodyProperties.size() > 0)
         return m_bodyProperties.iterator().next();
      else
         return null;
   }

   public PropertyDefinition[] getPropertyDefinitions()
   {
      return getDeclaredPropertyDefinitions();
   }

   public NodeType[] getSupertypes()
   {
      return null;
   }

   public boolean hasOrderableChildNodes()
   {
      return false;
   }

   public boolean isMixin()
   {
      return false;
   }

   public boolean isNodeType(String arg0)
   {
      return arg0.equals(getName());
   }
}
