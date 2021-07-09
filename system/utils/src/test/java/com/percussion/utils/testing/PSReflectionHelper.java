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
package com.percussion.utils.testing;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author DougRand
 * 
 * Provides a series of methods that aid in the analysis of object equality,
 * cloning and test setup. These methods may or may not suit a specific class.
 * 
 * <p>
 * Classes that can be tested with this helper will fit the following pattern:
 * <ul>
 * <li>Implement a no-args constructor
 * <li>Don't care about specific values for members
 * </ul>
 * 
 * <p>
 * It would be possible to extend this framework with a context that could allow
 * arguments to be constrained.
 * 
 * <p>
 * The initial design point is to help test objects that have no argument
 * constructors and regular setter and getter methods. Minimal filtering is
 * supported, and the code assumes that only public accessors are interesting.
 * 
 * <p>
 * Note that this class is intentionally not thread safe at this time. This
 * class is intended for unit test support, and thread safety should not be an
 * issue.
 */
public class PSReflectionHelper
{
   /**
    * Logger for this class
    */
   private static final Logger ms_log = LogManager.getLogger(PSReflectionHelper.class);
   
   /**
    * This class groups the information about a specific field&apos;s getter and
    * setter method.
    */
   static public class Accessor
   {
      /**
       * Ctor to assemble an accessor from the associated get and set methods.
       * Get and set methods are paired such that they take the same type.
       * 
       * @param get The get method, must never be <code>null</code>
       * @param set The set method, must never be <code>null</code>
       * @param type The type, must never be <code>null</code>
       */
      public Accessor(Method get, Method set, Class type) {
         if (get == null)
         {
            throw new IllegalArgumentException("get must never be null");
         }
         if (set == null)
         {
            throw new IllegalArgumentException("set must never be null");
         }
         if (type == null)
         {
            throw new IllegalArgumentException("type must never be null");
         }
         m_getMethod = get;
         m_setMethod = set;
         m_valuetype = type;
      }

      /**
       * Returns the get method part of the get and set method pair.
       * 
       * @return a get method, will never be <code>null</code>.
       */
      public Method getGetMethod()
      {
         return m_getMethod;
      }

      /**
       * Returns the set method
       * 
       * @return a set method, will never be <code>null</code>.
       */
      public Method getSetMethod()
      {
         return m_setMethod;
      }

      /**
       * Returns the value class that the accessors use. The get and set method
       * match on this type, i.e. the get method returns it and the set method
       * takes it as the argument type.
       * 
       * @return the class, will never be <code>null</code>.
       */
      public Class getValuetype()
      {
         return m_valuetype;
      }
      
      /**
       * Determine if either accessor methods declare the static modifier
       * 
       * @return <code>true</code> if either are static, <code>false</code> 
       * otherwise.
       */
      public boolean isStatic()
      {
         return Modifier.isStatic(m_getMethod.getModifiers()) ||
         Modifier.isStatic(m_setMethod.getModifiers());
      }

      /**
       * The get method for a specific field. This is never <code>null</code>
       * after construction.
       */
      private Method m_getMethod;

      /**
       * The set method for a specific field. This is never <code>null</code>
       * after construction.
       */
      private Method m_setMethod;

      /**
       * The class for a specific field. This is never <code>null</code> after
       * construction.
       */
      private Class m_valuetype;
   }

   /**
    * This class gathers all the per field information into a single data
    * structure. Each field is mapped to an <code>Accessor</code>
    */
   static public class AccessorMap
   {
      /**
       * Create an empty accessor map.
       */
      public AccessorMap() {
         mi_fieldMap = new HashMap<String, Accessor>();
         mi_sortedFields = new TreeSet<String>();
      }

      /**
       * Add a mapping from a given fieldname to the type and method information
       * stored in the <code>Accessor</code> class.
       * 
       * @param fieldname The fieldname, must never be <code>null</code>
       * @param mapping The mapping information, which must never be
       *           <code>null</code>
       */
      public void addMapping(String fieldname, Accessor mapping)
      {
         if (fieldname == null)
         {
            throw new IllegalArgumentException("fieldname must never be null");
         }
         mi_fieldMap.put(fieldname, mapping);
         mi_sortedFields.add(fieldname);
      }

      /**
       * Returns the given mapping belonging to the passed fieldname.
       * 
       * @param fieldname must never be <code>null</code> or empty.
       * @return The accessor that corresponds to the supplied fieldname.
       */
      public Accessor getMapping(String fieldname)
      {
         if (fieldname == null || fieldname.trim().length() == 0)
         {
            throw new IllegalArgumentException(
                  "fieldname must never be null or empty");
         }
         return (Accessor) mi_fieldMap.get(fieldname);
      }

      /**
       * Returns the <code>Set</code> of sorted fields. Since the internal set
       * is a <code>TreeSet</code>, it will be sorted.
       * 
       * @return may return an empty set, but never <code>null</code>. The
       *         return set should be treated as read-only as the ownership is
       *         retained by this class.
       */
      public Set<String> getFields()
      {
         return mi_sortedFields;
      }

      /**
       * This set is an instance of {@link java.util.TreeSet}, which keeps it's
       * members in a sorted order. Initialized in the constructor, never
       * <code>null</code> afterward.
       */
      private Set<String> mi_sortedFields;

      /**
       * This is initialized in the constructor and never <code>null</code>
       * afterward. This map contains mappings from a field name to an
       * {@link PSReflectionHelper.Accessor}.
       */
      private Map<String, Accessor> mi_fieldMap;

      /**
       * This maps class objects to a structure that describes the class's
       * accessors in a matched fashion.
       */
      static Map<Class, AccessorMap> ms_classAccessors = new HashMap<Class, AccessorMap>();
   }

   /**
    * Invocation handler used to "test" interfaces
    */
   static class TestInvocationHandler implements InvocationHandler
   {
      public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
      {
         if (method.getName().equals("hashCode"))
         {
            return 1;
         }
         else if (method.getName().equals("equals"))
         {
            return proxy == args[0];
         }
         else
         {
            return null;
         }
      }
   };

   /**
    * Are these objects basically different? To answer this question, check that
    * both the object references themselves are different as well as any objects
    * held in members. This test does not dive into objects, it only tests their
    * references. This means that all contained objects should be tested
    * separately.
    * 
    * The exception are <code>Collection</code> and <code>Map</code> objects
    * that are contained. Those are walked and compared to ensure they do not
    * contain the same references.
    * 
    * <p>
    * This method also assumes that the list <code>ms_immutables</code> is up
    * to date and contains all types that are basically immutable that are in
    * use. You, the caller, are responsible for extending that list as new
    * classes are added that are tested through getters in this method.
    * 
    * @param a An instance, never <code>null</code>
    * @param b A different instance, never <code>null</code>
    * @param filter A reflection filter, which may be <code>null</code>
    * @return <code>true</code> if the two differ, <code>false</code> if
    *         they are the same
    * @throws IllegalAccessException 
    * @throws InvocationTargetException 
    */
   public static boolean testClone(Object a, Object b,
         IPSReflectionFilter filter) throws IllegalAccessException,
         InvocationTargetException
   {
      if (a == null || b == null)
      {
         throw new IllegalArgumentException("Objects must not be null");
      }

      if (!a.getClass().equals(b.getClass()))
      {
         throw new IllegalArgumentException("Objects must be of the same class");
      }

      if (a == b)
         return false;

      List methods = findGetMethods(a.getClass(), filter);

      // For each get method, check the return type and invoke for object
      // types
      Iterator iter = methods.iterator();
      Object emptyArgs[] = new Object[0];
      Set copyingNotRequired = getImmutableSet();
      while (iter.hasNext())
      {
         Method m = (Method) iter.next();
         Object result1, result2;

         result1 = m.invoke(a, emptyArgs);
         result2 = m.invoke(b, emptyArgs);

         if (result1 == null && result2 == null)
         {
            // OK
         }
         else if (result1 == null)
         {
            return false; // One null and not the other
         }
         else if (result2 == null)
         {
            return false; // One null and not the other
         }
         else if (copyingNotRequired.contains(result1.getClass()) == false
               && result1 == result2)
         {
            return false;
         }

         if (result1 instanceof Set)
         {
            if (!testSet(result1, result2))
            {
               return false;
            }
         }
         else if (result1 instanceof List)
         {
            if (!testList(result1, result2))
            {
               return false;
            }
         }
         else if (result1 instanceof Map)
         {
            if (!testMap(result1, result2))
            {
               return false;
            }
         }
      }

      return true;
   }

   /**
    * Test two lists for proper cloning behavior.
    * 
    * @param result1 The first object, must be of type
    *           <code>java.util.List</code>.
    * @param result2 The second object, must be of type
    *           <code>java.util.List</code>.
    * @return <code>true</code> if the test succeeds, <code>false</code>
    *         otherwise.
    */
   private static boolean testList(Object result1, Object result2)
   {
      if (((List) result1).size() != ((List) result2).size())
      {
         return false;
      }
      // Check that elements are different, note that
      // instanceof handles null reasonably
      Iterator i1 = ((List) result1).iterator();
      Iterator i2 = ((List) result2).iterator();
      while (i1.hasNext())
      {
         Object o1 = i1.next();
         Object o2 = i2.next();
         if (isNotImmutable(o1) && o1 == o2)
            return false;
      }
      return true;
   }

   /**
    * Test two maps for proper cloning behavior.
    * 
    * @param result1 The first object, must be of type
    *           <code>java.util.Map</code>.
    * @param result2 The second object, must be of type
    *           <code>java.util.Map</code>.
    * @return <code>true</code> if the test succeeds, <code>false</code>
    *         otherwise.
    */
   private static boolean testMap(Object result1, Object result2)
   {
      if (((Map) result1).size() != ((Map) result2).size())
      {
         return false;
      }
      // Check that elements are different, note that
      // instanceof handles null reasonably
      Set keySet1 = ((Map) result1).keySet();
      Set keySet2 = ((Map) result2).keySet();
      // Test set members for proper cloning
      if (!testSet(keySet1, keySet2))
      {
         return false;
      }
      // Test sets for equality
      if (keySet1.equals(keySet2) == false)
      {
         return false;
      }
      Iterator i1 = keySet1.iterator();
      while (i1.hasNext())
      {
         Object key = i1.next();
         Object o1 = ((Map) result1).get(key);
         Object o2 = ((Map) result2).get(key);
         if (isNotImmutable(o1) && o1 == o2)
            return false;
      }
      return true;
   }

   /**
    * Test two sets for proper cloning behavior.
    * 
    * @param keySet1 The first object, must be of type
    *           <code>java.util.Set</code>.
    * @param keySet2 The second object, must be of type
    *           <code>java.util.Set</code>.
    * @return <code>true</code> if the test succeeds, <code>false</code>
    *         otherwise.
    */
   private static boolean testSet(Object keySet1, Object keySet2)
   {
      for (Iterator iter = ((Set) keySet1).iterator(); iter.hasNext();)
      {
         Object element = (Object) iter.next();
         // Now, if the element is mutable, walk the other set
         // looking for an == match
         if (isNotImmutable(element))
         {
            for (Iterator iter2 = ((Set) keySet2).iterator(); iter2.hasNext();)
            {
               Object comparison = (Object) iter2.next();
               if (element == comparison)
               {
                  return false;
               }
            }
         }
      }
      return true;
   }

   /**
    * Is the passed object&apos;s class an immutable one?
    * 
    * @param obj The object to test, must never be <code>null</code>.
    * @return <code>true</code> if the passed object is not an immutable
    *         object.
    */
   private static boolean isNotImmutable(Object obj)
   {
      if (obj == null)
      {
         throw new IllegalArgumentException("obj must never be null");
      }
      Set immutables = getImmutableSet();
      return !immutables.contains(obj.getClass());
   }

   /**
    * Take the passed objects and start modifying fields. The initial objects
    * should return <code>true</code> for equals.
    * 
    * <p>
    * Each field modification should yield a non-equal value, and copying the
    * new value to the second object should return the state to equals.
    * 
    * <p>
    * Note that for fields of type {@link java.util.Collection}, this code
    * generates empty collections. These will not work if the original is an
    * empty collection since the two empty collections would be equals after the
    * modification. This is a limitation of this testing technique since the
    * underlying code cannot know what types are valid for the given collection.
    * 
    * @param a First instance, must never be <code>null</code>
    * @param b Second instance, must never be <code>null</code>
    * @param filter A reflection filter to determine what methods should be
    *           called, may be <code>null</code>
    * @throws Exception 
    */
   public static void testEquals(Object a, Object b, IPSReflectionFilter filter)
         throws Exception
   {
      if (a == null)
      {
         throw new IllegalArgumentException("a must never be null");
      }
      if (b == null)
      {
         throw new IllegalArgumentException("b must never be null");
      }
      if (a.getClass().equals(b.getClass()) == false)
      {
         throw new Exception("The objects must be of the same class "
               + a.getClass());
      }

      if (a.equals(b) == false)
      {
         throw new Exception("Initial object state not equals " + a);
      }

      /*
       * Walk through each field and perform the test. The test sets each field
       * by calling the set method with the next value (not random to ensure
       * that the value is not the same). If this test passes then the first
       * object is updated with the same value in the same field and the test
       * ensures that the two objects are again equals.
       * 
       * For the equals case, the test checks that the hash codes are the same,
       * as the contract on <code>Object</code> states.
       */
      AccessorMap mappings = getAccessors(a.getClass(), filter);
      Iterator fields = mappings.getFields().iterator();
      while (fields.hasNext())
      {
         String field = (String) fields.next();
         Accessor accessor = mappings.getMapping(field);
         
         // don't test static accessors
         if (accessor.isStatic())
            continue;
         
         // Get new value to assign
         Object newval;
         try
         {
            newval = getNewValue(accessor.getValuetype());
         }
         catch (InstantiationException e)
         {
            System.err.println("Couldn't create new object for "
                  + accessor.getSetMethod());
            e.printStackTrace(System.err);
            throw e;
         }
         Object args[] = new Object[]
         {newval};
         Object empty[] = new Object[0];
         // Remember original values
         Object oa = accessor.getGetMethod().invoke(a, empty);
         Object ob = accessor.getGetMethod().invoke(b, empty);

         // Assign to the second object, test for not equals
         accessor.getSetMethod().invoke(b, args);
         if (a.equals(b))
         {
            throw new Exception(
                  "Values should not be equals after modifying field " + field);
         }
         // Assign to the first object, test for equals

         accessor.getSetMethod().invoke(a, args);
         if (a.equals(b) == false)
         {
            throw new Exception(
                  "Updating first object should restore equals for field "
                        + field);
         }

         // Check that hash matches
         if (a.hashCode() != b.hashCode())
         {
            throw new Exception(
                  "Objects that are equal must have the same hash code " + a);
         }

         // Reset original values
         accessor.getSetMethod().invoke(a, new Object[]
         {oa});
         accessor.getSetMethod().invoke(b, new Object[]
         {ob});
      }
   }

   /**
    * Coalesce the getters and setters into the internal datastructures and
    * register them with the class. This code assumes that field names are
    * unique ignoring letter case.
    * 
    * @param clazz The given class to analyze, never <code>null</code>
    * @param filter The filter for the setters and getters, may be
    *           <code>null</code>
    * @return the accessor map for the given class, never <code>null</code>
    * @throws Exception 
    */
   public static AccessorMap getAccessors(Class clazz,
         IPSReflectionFilter filter) throws Exception
   {
      if (clazz == null)
      {
         throw new IllegalArgumentException("Class may never be null");
      }
      if (AccessorMap.ms_classAccessors.get(clazz) != null)
         return (AccessorMap) AccessorMap.ms_classAccessors.get(clazz);

      List<Method> setters = findSetMethods(clazz, filter);
      List<Method> getters = findGetMethods(clazz, filter);

      // Build a map going from field names to setters
      Iterator siter = setters.iterator();
      Map<String, List<Method>> smap = new HashMap<String, List<Method>>();
      while (siter.hasNext())
      {
         Method setter = (Method) siter.next();
         String name = setter.getName();
         name = name.substring(3).toLowerCase(); // Strip "set", downcase
         List<Method> mapsetters = smap.get(name);
         if (mapsetters == null)
         {
            mapsetters = new ArrayList<Method>();
            smap.put(name, mapsetters);
         }
         mapsetters.add(setter);
      }

      // Walk through the getters and map a setter to each. Throw
      // an exception if no match found. A match is defined as a setter
      // that takes the same type as the getter returns
      Iterator giter = getters.iterator();
      AccessorMap mappings = new AccessorMap();
      while (giter.hasNext())
      {
         Method getter = (Method) giter.next();
         String name = getter.getName();
         name = name.substring(3).toLowerCase(); // Strip "set"
         Class rtype = getter.getReturnType();
         // Get possible setters
         List slist = (List) smap.get(name);

         boolean found = false;
         if (slist != null)
         {
            Iterator sliter = slist.iterator();
            while (sliter.hasNext())
            {
               Method setter = (Method) sliter.next();
               Class args[] = setter.getParameterTypes();
               if (args[0].equals(rtype))
               {
                  // Found
                  found = true;
                  Accessor a = new Accessor(getter, setter, rtype);
                  mappings.addMapping(name, a);
               }
            }
         }
         if (!found)
         {
            ms_log.warn("No matching setter for field " + name
                  + " in class " + clazz);
         }
      }
      AccessorMap.ms_classAccessors.put(clazz, mappings);
      return mappings;
   }

   /**
    * Constructs the list of set methods apropos for the given class. The set
    * methods return take a single argument. This is done by simply walking all
    * the class&apos;s methods.
    * <p>
    * Two filters are applied in this process. The first filter accepts methods
    * that start with the string "set". The next filter accepts methods that
    * have a single argument. The last filter is an optional user supplied
    * filter to the process. That filter may apply any criteria. Each accepted
    * method is added to the return list.
    * 
    * @param clazz The given class, assumed not <code>null</code>
    * @param filter The given filter, which may be <code>null</code>. The
    *           {@link IPSReflectionFilter filter} exposes an accept method that
    *           is called with the name of the method. It returns
    *           <code>true</code> if the method is accepted.
    * @return a list of methods, must never be <code>null</code>, but
    *         conceivably could be empty.
    */
   private static List<Method> findSetMethods(Class clazz,
         IPSReflectionFilter filter)
   {
      List<Method> rval = new ArrayList<Method>();
      Method methods[] = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         String name = method.getName();
         if (name.startsWith("set"))
         {
            Class types[] = method.getParameterTypes();
            if (types.length == 1)
            {
               if (filter != null)
               {
                  if (filter.acceptMethod(name))
                  {
                     rval.add(method);
                  }
               }
               else
               {
                  rval.add(method);
               }
            }
         }
      }

      return rval;
   }

   /**
    * Constructs the list of get methods apropos for the given class. Will not
    * include any methods implemented by <code>Object</code>. This is done by
    * simply walking all the class&apos;s methods.
    * <p>
    * Several filters are applied in this process. The first filter accepts
    * methods that start with the string "get". The next only accepts methods
    * with no arguments. The last filter is an optional user supplied filter to
    * the process. That filter may apply any criteria. Each accepted method is
    * added to the return list.
    * 
    * @param clazz The given class, assumed not <code>null</code>
    * @param filter The given filter, which may be <code>null</code>. The
    *           {@link IPSReflectionFilter filter} exposes an accept method that
    *           is called with the name of the method. It returns
    *           <code>true</code> if the method is accepted.
    * @return a list of methods, must never be <code>null</code>, but
    *         conceivably could be empty.
    */
   private static List<Method> findGetMethods(Class clazz,
         IPSReflectionFilter filter)
   {
      List<Method> rval = new ArrayList<Method>();
      Method methods[] = clazz.getMethods();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         String name = method.getName();
         if (name.startsWith("get")
               && method.getDeclaringClass().equals(Object.class) == false)
         {
            Class types[] = method.getParameterTypes();
            if (types.length == 0)
            {
               if (filter != null)
               {
                  if (filter.acceptMethod(name))
                  {
                     rval.add(method);
                  }
               }
               else
               {
                  rval.add(method);
               }
            }
         }
      }

      return rval;
   }

   /**
    * The immutable classes are stored in this set.
    */
   private static Set<Class> ms_immutableSet = new HashSet<Class>();

   /**
    * Gets a list of classes that are known to be immutable. Immutable objects
    * are those that cannot be modified after construction. Immutable objects do
    * not need to be copied during cloning and may be shared among objects.
    * 
    * @return a set of immutable classes, will never be <code>null</code>.
    */
   private static Set<Class> getImmutableSet()
   {
      if (ms_immutableSet.size() == 0)
      {
         for (int i = 0; i < ms_immutables.length; i++)
         {
            Class clazz = ms_immutables[i];
            ms_immutableSet.add(clazz);
         }
      }
      return ms_immutableSet;
   }

   /**
    * Returns an appropriate new value according to type. For unknown classes it
    * simply calls {@link Class#newInstance()}. Note that this requires that
    * the class in question have a default constructor.
    * 
    * @param clazz the class to instantiate, assumed not <code>null</code>.
    * 
    * @return a new value of the same class, which is guaranteed to be at least
    *         !=, and which will be ! equals for immutable objects.
    * @throws InstantiationException 
    * @throws IllegalAccessException 
    */
   private static Object getNewValue(Class clazz)
         throws InstantiationException, IllegalAccessException
   {
      if (clazz.equals(String.class))
      {
         return getNextStringValue();
      }
      else if (clazz.equals(Long.class) || clazz.equals(long.class))
      {
         return getNextLongValue();
      }
      else if (clazz.equals(Integer.class) || clazz.equals(int.class))
      {
         return getNextIntegerValue();
      }
      else if (clazz.equals(Short.class) || clazz.equals(short.class))
      {
         return getNextShortValue();
      }
      else if (clazz.equals(Byte.class) || clazz.equals(byte.class))
      {
         return getNextByteValue();
      }
      else if (clazz.equals(List.class) || clazz.equals(Collection.class))
      {
         return getNextListValue();
      }
      else if (clazz.equals(Map.class))
      {
         return getNextMapValue();
      }
      else if (clazz.isInterface())
      {
         return getProxyInstance(clazz);
      }
      else
      {
         return clazz.newInstance();
      }
   }

   /**
    * Create a proxy of the passed class. This creates a disfunctional object,
    * but one that is adequate for the purposes of testing.
    * 
    * @param clazz a class that is an interface, assumed not <code>null</code>
    * @return a proxy that obeys the given interface
    */
   private static Object getProxyInstance(Class clazz)
   {
      Class xface[] = new Class[]
      {clazz};

      return Proxy.newProxyInstance(clazz.getClassLoader(), xface,
            new TestInvocationHandler());
   }

   /**
    * Return a new instance of a <code>String</code> with a guaranteed
    * non-equal value. This assumes that all values have been obtained through
    * these methods as a given object might, in fact, match the value.
    * 
    * @return A new value obtained by using a static counter.
    */
   private static String getNextStringValue()
   {
      ms_nextValue++;

      return Long.toString(ms_nextValue);
   }

   /**
    * Return a new instance of a <code>Long</code> with a guaranteed non-equal
    * value. This assumes that all values have been obtained through these
    * methods as a given object might, in fact, match the value.
    * 
    * @return A new value obtained by using a static counter.
    */
   private static Long getNextLongValue()
   {
      ms_nextValue++;

      return new Long(ms_nextValue);
   }

   /**
    * Return a new instance of an <code>Integer</code> with a guaranteed
    * non-equal value. This assumes that all values have been obtained through
    * these methods as a given object might, in fact, match the value.
    * 
    * @return A new value obtained by using a static counter.
    */
   private static Integer getNextIntegerValue()
   {
      ms_nextValue++;

      return new Integer((int) ms_nextValue);
   }

   /**
    * Return a new instance of a <code>Short</code> with a guaranteed
    * non-equal value. This assumes that all values have been obtained through
    * these methods as a given object might, in fact, match the value.
    * 
    * @return A new value obtained by using a static counter.
    */
   private static Short getNextShortValue()
   {
      ms_nextValue++;

      return new Short((short) ms_nextValue);
   }

   /**
    * Return a new instance of a <code>Byte</code> with a guaranteed non-equal
    * value. This assumes that all values have been obtained through these
    * methods as a given object might, in fact, match the value.
    * 
    * @return A new value obtained by using a static counter.
    */
   private static Byte getNextByteValue()
   {
      ms_nextValue++;

      return new Byte((byte) ms_nextValue);
   }

   /**
    * Return a new instance of a <code>List</code>. This is simply a new
    * list, not necessarily a list that is non-equals to another list. This must
    * be used with care.
    * 
    * @return A new value of an empty list.
    */
   private static List getNextListValue()
   {
      return new ArrayList(); // Just needs to be !=, not different
   }

   /**
    * Return a new instance of a <code>Map</code>. This is simply a new map,
    * not necessarily a map that is non-equals to another map. This must be used
    * with care.
    * 
    * @return A new value of an empty map.
    */
   private static Map getNextMapValue()
   {
      return new HashMap(); // Just needs to be !=, not different
   }

   /**
    * This is used by the code that creates new values of various types
    */
   private static long ms_nextValue = 0;

   /**
    * A list of classes that cannot be modified after construction. This is not
    * a complete list, and should be extended as required. Classes on this list
    * are not required to be copied in clones.
    */
   private static Class ms_immutables[] =
   {Long.class, String.class, Integer.class, Short.class, Byte.class,
         Character.class};

}
