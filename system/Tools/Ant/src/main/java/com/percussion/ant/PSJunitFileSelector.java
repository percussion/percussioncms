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
package com.percussion.ant;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;
import org.apache.tools.ant.types.selectors.SelectorUtils;

/**
 * Custom Ant selector, used to select junit test cases based on a set of 
 * include/exclude filters.  See 
 * {@link com.percussion.testing.PSCustomFileFilter} for a description of the
 * custom selector definition supported.
 */
public class PSJunitFileSelector extends BaseExtendSelector
{   
   /**
    * Selects the file based on the filters specified.  See class header and
    * {@link org.apache.tools.ant.types.selectors.BaseSelector base class}
    * for more info. 
    */
   @Override
   public boolean isSelected(File baseDir, String filename, File file)
   {
     // check for init errors
      validate();
      
      // default to false if nothing specified, nothing selected
      boolean selected = false;
      
      // determine classname
      int extPos = filename.lastIndexOf(".");
      if (extPos != -1)
         filename = filename.substring(0, extPos);         
      filename = filename.replace('\\', '/');      
      String className = filename.replace('/', '.');      

      try
      {
         // try to load it
         Class theClass = loadClass(className);
         
         // load JUnit4 test annotation class
         loadTestAnnotation();
                
         int mods = theClass.getModifiers();
         //exclude non public classes, interfaces and abstracts
         if (!Modifier.isPublic(mods) ||
             Modifier.isInterface(mods) ||
             Modifier.isAbstract(mods))
            return false;
         
         final Class clazzTestCase = loadClass("junit.framework.TestCase");
         //automatically exclude all non JUnit/JUnit4 TestCase classes.
         if (!clazzTestCase.isAssignableFrom(theClass) && !isJUnit4(theClass))
            return false;
                  
         // get the package
         String pkgName = null;
         Package pkg = theClass.getPackage();
         if (pkg != null)
            pkgName = pkg.getName(); 

         // get class name without package
         if (pkg != null)
            className = className.substring(pkgName.length() + 1);

         selected = (isPkgIncluded(pkgName) && !isPkgExcluded(pkgName) && 
         isClassIncluded(className, theClass) && !isClassExcluded(className, 
            theClass));
      }
      catch (NoClassDefFoundError e)
      {
         e.printStackTrace();
         setError("Failed to load test class: " + className + ", filename: " + 
            filename + ", error: " + e.getLocalizedMessage());         
      }
      catch (ClassNotFoundException e)
      {
         e.printStackTrace();
         setError("Failed to load test class: " + className + ", filename: " + 
            filename + ", error: " + e.getLocalizedMessage());
      }
      
      return selected;      
   }
   
   /**
    * Determines if the supplied class is to be excluded.
    * 
    * @param className The name of the class, assumed not <code>null</code> or 
    * empty.
    * @param theClass The class object, assumed not <code>null</code> and to
    * match the supplied <code>className</code>.
    *  
    * @return <code>true</code> if it should be excluded, <code>false</code>
    * if not.
    */
   private boolean isClassExcluded(String className, Class theClass) 
   {
      boolean isExcluded = false;
      
      // see if name is excluded
      isExcluded = hasMatch(className, m_classNameExcludes.iterator(), false);
      
      // if name not excluded, check instanceof 
      if (!isExcluded)
         isExcluded = hasMatch(theClass, m_classImplExcludes.iterator(), false);

      return isExcluded;
   }

   /**
    * Determines if the supplied class is to be included.
    * 
    * @param className The name of the class, assumed not <code>null</code> or 
    * empty.
    * @param theClass The class object, assumed not <code>null</code> and to
    * match the supplied <code>className</code>.
    *  
    * @return <code>true</code> if it should be included, <code>false</code>
    * if not.
    */
   private boolean isClassIncluded(String className, Class theClass) 
   {
      // default to true is no filters provided
      boolean isIncluded = m_classNameIncludes.isEmpty() && 
         m_classImplIncludes.isEmpty();
      
      // see if name or class is included      
      isIncluded = isIncluded || 
         hasMatch(className, m_classNameIncludes.iterator(), false) || 
         hasMatch(theClass, m_classImplIncludes.iterator(), false) ||
         isJUnit4(theClass);      

      return isIncluded;
   }

   /**
    * Determines if the specified package is to be included.
    * 
    * @param pkgName The name of the package, assumed not <code>null</code> or 
    * empty.
    *  
    * @return <code>true</code> if it should be included, <code>false</code>
    * if not.
    */
   private boolean isPkgIncluded(String pkgName)
   {
      return hasMatch(pkgName, m_packageIncludes.iterator(), true);
   }

   /**
    * Determines if the specified package is to be excluded.
    * 
    * @param pkgName The name of the package, assumed not <code>null</code> or 
    * empty.
    *  
    * @return <code>true</code> if it should be included, <code>false</code>
    * if not.
    */
   private boolean isPkgExcluded(String pkgName)
   {
      return hasMatch(pkgName, m_packageExcludes.iterator(), false);
   }
   
   /**
    * Determine if a name matches any of a list of patterns.
    *  
    * @param name The name, assumed not <code>null</code>.
    * @param patterns An iterator over patterns as <code>String</code> objects,
    * assumed not <code>null</code>. 
    * @param matchEmpty <code>true</code> to treat an empty
    * <code>patterns</code> as a match, <code>false</code> to treat it as a 
    * failure to match.
    * 
    * @return <code>true</code> if the name matches any patterns, or if 
    * <code>patterns</code> is empty and <code>matchEmpty</code> is 
    * <code>true</code>, <code>false</code> otherwise.
    */
   private boolean hasMatch(String name, Iterator patterns, boolean matchEmpty)
   {
      boolean hasMatch = (matchEmpty && !patterns.hasNext());
      
      while (patterns.hasNext() && !hasMatch)
      {
         hasMatch = SelectorUtils.match((String)patterns.next(), name);
      }
      
      return hasMatch;
   }

   /**
    * Determines if the specified class is an "instanceof" any of the supplied
    * class names.
    * 
    * @param theClass The class to check against the list of class names,
    * assumed not <code>null</code>.
    * @param classNames An iterator over zero or more class names as 
    * <code>String</code> objects, assumed not <code>null</code>, may be
    * empty.
    * @param matchEmpty <code>true</code> to consider an empty 
    * <code>classNames</code> as a match, <code>false</code> to consider it as
    * a failure to match.
    * 
    * @return <code>true</code> if the class is an instanceof any of the class
    * names, or if <code>classNames</code> is empty and <code>matchEmpty</code>
    * is <code>true</code>, <code>false</code> otherwise.
    */
   private boolean hasMatch(Class theClass, Iterator classNames, 
      boolean matchEmpty)
   {
      boolean hasMatch = (matchEmpty && !classNames.hasNext());
      
      while (classNames.hasNext() && !hasMatch)
      {
         Class testClass;
         String testName = (String)classNames.next();
         try
         {
            testClass = loadClass(testName);            
            hasMatch = testClass.isAssignableFrom(theClass);
         }
         catch (ClassNotFoundException e)
         {
            setError("Cannot load class for filter match: " + testName);
         }

      }
      
      return hasMatch;
   }
   
   /**
    * Adds the specified filter to the appropriate list of package filters.      * 
    * 
    * @param filter The filter to add, may not be <code>null</code> or empty.
    * @param isInclude <code>true</code> to add the filter to the list of 
    * package includes, <code>false</code> to add it to the package excludes.
    */
   public void setPackageFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_packageIncludes.add(filter);
      else
         m_packageExcludes.add(filter);      
   }

   /**
    * Adds the specified filter to the appropriate list of class name filters.
    * 
    * @param filter The filter to add, may not be <code>null</code> or empty.
    * @param isInclude <code>true</code> to add the filter to the list of 
    * class name includes, <code>false</code> to add it to the class name
    * excludes.
    */
   public void setClassNameFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_classNameIncludes.add(filter);
      else
         m_classNameExcludes.add(filter);      
   }

   /**
    * Adds the specified filter to the appropriate list of class implementation
    * filters.
    * 
    * @param filter The filter to add, may not be <code>null</code> or empty.
    * @param isInclude <code>true</code> to add the filter to the list of 
    * class impl includes, <code>false</code> to add it to the class impl
    * excludes.
    */   
   public void setClassImplFilter(String filter, boolean isInclude)
   {
      if (filter == null || filter.trim().length() == 0)
         throw new IllegalArgumentException("filter may not be null or empty");

      if (isInclude)
         m_classImplIncludes.add(filter);
      else
         m_classImplExcludes.add(filter);      
   }
   

   
   /**
    * Sets the parameters from the XML build file.  Expects parameters with a
    * name matching one of the <code>XXX_ATTR</code> values, a type of either
    * 'include' or 'exclude', defaults to 'include' if no value is supplied, and
    * the value is the filter pattern, which may not be empty.
    * 
    * @see org.apache.tools.ant.types.Parameterizable#setParameters(Parameter[] 
    * Paraeterizable.setParameters()) for more info.
    */
   public void setParameters(Parameter parameters[])
   {
      super.setParameters(parameters);
      
      // clear lists
      m_packageIncludes.clear();
      m_packageExcludes.clear();
      m_classNameIncludes.clear();
      m_classNameExcludes.clear();
      m_classImplIncludes.clear();
      m_classImplExcludes.clear();
            
      if(parameters != null)
      {
         String classPath = null;
      
         for(int i = 0; i < parameters.length; i++)
         {
            String type = parameters[i].getType();
            boolean isInclude = true;
            if (TYPE_PATH.equalsIgnoreCase(type))
            {
               if (parameters[i].getName().equalsIgnoreCase(
                  FILTER_CLASS_PATH_ATTR))
               {
                  classPath = parameters[i].getValue();
                  continue;                  
               }
               else
               {
                  setError("Unsupported param name (" +  parameters[i].getName() 
                     + ") for type: " + type);
               }
            }
            else if (TYPE_EXCLUDE.equalsIgnoreCase(type))
            {
               isInclude = false;
            }            
            else if (!TYPE_INCLUDE.equalsIgnoreCase(type))
            {
               setError("Invalid parameter type: " + type);
               return;
            }
            
            // default type to include
            String paramname = parameters[i].getName();
            if(PACKAGE_FILTER_ATTR.equalsIgnoreCase(paramname))
            {
               setPackageFilter(parameters[i].getValue(), isInclude);               
            }
            else if (CLASS_NAME_FILTER_ATTR.equalsIgnoreCase(paramname))
            {
               setClassNameFilter(parameters[i].getValue(), isInclude);
            }
            else if (CLASS_IMPL_FILTER_ATTR.equalsIgnoreCase(paramname))
            {
               setClassImplFilter(parameters[i].getValue(), isInclude);
            }
            else
            {
               setError("Invalid parameter name: " + paramname);
               return;
            }
         }
         
         // if classpath supplied, create and set a classloader
         if (classPath != null && classPath.trim().length() > 0)
         {
            if (ms_classPath == null || !ms_classPath.equals(classPath))
            {
               ms_classPath = classPath;
               URL[] urls = pathToURLs(classPath);
               // dont' give parent loader as that causes conflicts if a class
               // is found by the parent loader, but a class it depends on is
               // not.
               ms_classLoader = new URLClassLoader(urls, 
                  null);                
            }
         }
         else
         {
            ms_classPath = null;
            ms_classLoader = null;
         }
      }      
   }
   

   
   /**
    * Parses the supplied classpath into an array of URL objects.
    * 
    * @param classPath The classpath to parse, assumed not <code>null</code> or 
    * empty.
    * 
    * @return An array of url objects for each classpath element that is found
    * to exist.
    */
   private URL[] pathToURLs(String classPath)
   {
      List<URL> urlList = new ArrayList<URL>();
      StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
      while (st.hasMoreTokens())
      {
         File file = new File(st.nextToken());
         if (file.exists())
         {
            try
            {
               urlList.add(file.toURL());
            }
            catch (MalformedURLException e)
            {
               // ignore bad entries (that's what Java does)
               e.printStackTrace();
            }
         }
      }
         
      return urlList.toArray(new URL[urlList.size()]);
   }
   
   /**
    * Loads the specified class using the appropriate class loader.  If a 
    * custom classpath parameter has been specified, that classloader is used,
    * otherwise the default classloader is used.
    * 
    * @param className The fully qualified name of the class to load, assumed 
    * not <code>null</code> or empty.
    * 
    * @return The class, never <code>null</code>.
    * 
    * @throws ClassNotFoundException If the class cannot be loaded.
    */
   private Class loadClass(String className) throws ClassNotFoundException
   {
      Class theClass = null;
      if (ms_classLoader == null)
         theClass = Class.forName(className);
      else
         theClass = ms_classLoader.loadClass(className);
      
      return theClass;
   }

   /**
    * Determines if a class represents a JUnit4-style test class.  It does this
    * by checking for methods annotated with the {@link org.junit.Test}
    * annotation.
    * 
    * @param theClass The test class to inspect, assumed not <code>null</code>.
    *  
    * @return <code>true</code> if the test class is JUnit4, <code>false</code>
    * otherwise.
    */
   private boolean isJUnit4(Class theClass)
   {
      Method[] methods = theClass.getMethods();
      for (Method m : methods)
      {
         Annotation[] annotations = m.getDeclaredAnnotations();
         for (Annotation a : annotations)
         {
            if (ms_testAnnotation.isAssignableFrom(a.getClass()))
               return true;
         }
      }
      
      return false;
   }
   
   /**
    * Loads the class which represents the JUnit4 test annotation.  The
    * class is loaded once and stored in {@link #ms_testAnnotation}.
    * 
    * @throws ClassNotFoundException If the class cannot be loaded.
    */
   private void loadTestAnnotation() throws ClassNotFoundException
   {
      if (ms_testAnnotation == null)
         ms_testAnnotation = loadClass("org.junit.Test");
   }
   
   /**
    * The parameter name to indicate the supplied filter is a package filter.
    */
   public static final String PACKAGE_FILTER_ATTR = "packageFilter";
   
   /**
    * The parameter name to indicate the supplied filter is a class name filter.
    */
   public static final String CLASS_NAME_FILTER_ATTR = "classNameFilter";

   /**
    * The parameter name to indicate the supplied filter is a class 
    * implementation filter.
    */   
   public static final String CLASS_IMPL_FILTER_ATTR = "classImplFilter";
   
   /**
    * The parameter name to indicate a classpath is specified, used to define
    * the classpath the selector's class loader should use. 
    */
   public static final String FILTER_CLASS_PATH_ATTR = "filterClasspath"; 
   
   /**
    * The value to indicate the supplied parameter should be treated as an 
    * exclude filter.
    */
   public static final String TYPE_EXCLUDE = "exclude";

   /**
    * The value to indicate the supplied parameter should be treated as an 
    * include filter.
    */
   public static final String TYPE_INCLUDE = "include";
   
   /**
    * The value to indicate the supplied parameter will supply a path
    */
   public static final String TYPE_PATH = "path";
   
   /**
    * List of package include filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */
   private List<String> m_packageIncludes = new ArrayList<String>();

   /**
    * List of package exlude filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */
   private List<String> m_packageExcludes = new ArrayList<String>();

   /**
    * List of classname include filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */   
   private List<String> m_classNameIncludes = new ArrayList<String>();

   /**
    * List of classname exclude filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */   
   private List<String> m_classNameExcludes = new ArrayList<String>();

   /**
    * List of class impl include filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */   
   private List<String> m_classImplIncludes = new ArrayList<String>();
   
   /**
    * List of class impl exclude filters, never <code>null</code>, filters as
    * <code>String</code>objects are added during the call to 
    * {@link #setParameters(Parameter[])}.
    */   
   private List<String> m_classImplExcludes = new ArrayList<String>();  

   /**
    * Class path to use to load test classes, initialized by 
    * {@link #setParameters(Parameter[])}, <code>null</code> if a classpath
    * is not specified in the parameters.   Never modified otherwise.
    */
   private static String ms_classPath = null;
   
   /**
    * Class loader to use to load test classes, initialized by 
    * {@link #setParameters(Parameter[])}, <code>null</code> if a classpath
    * is not specified in the parameters.   Never modified otherwise.
    */
   private static ClassLoader ms_classLoader = null;
   
   /**
    * Stores the test annotation class used by JUnit4-style test classes.
    * Initialized in {@link #loadTestAnnotation()}.
    */
   private static Class ms_testAnnotation = null;
}
