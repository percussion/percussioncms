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

package com.percussion.tools.simple;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * This class is used to create dummy classes with stubbed out methods.
 */
public class PSClassGenerator
{
   private static final Logger log = LogManager.getLogger(PSClassGenerator.class);

   /**
    * Generates stub classes for each class in the given directory.
    *
    * @param srcClassDirectory The source class directory, may not be <code>null</code>.
    * @param srcJavaDirectory The source java directory, may not be <code>null</code>.
    * @param tgtDirectory The target class directory, may not be <code>null</code>.
    * @param srcPackage The package of the classes in the directory
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IOException if any io error occurs
    * @throws FileNotFoundException if any file cannot be located.
    */
   public void createClasses(File srcClassDirectory, File srcJavaDirectory, File tgtDirectory, String srcPackage) throws IOException
   {
      // validate params
      if (srcClassDirectory == null || !srcClassDirectory.exists())
         throw new IllegalArgumentException("source class directory is invalid");

      if (srcJavaDirectory == null || !srcJavaDirectory.exists())
         throw new IllegalArgumentException("source java directory is invalid");
      
      if (tgtDirectory == null)
         throw new IllegalArgumentException("target directory is invalid");
      else
         tgtDirectory.mkdirs();
      
      if (srcPackage == null)
         throw new IllegalArgumentException("package is invalid");
      
      // build required lists for class and java files
      File[] srcClassFiles = srcClassDirectory.listFiles();
      File[] srcJavaFiles = srcJavaDirectory.listFiles();
      int i;
      String name = "";
      ArrayList<File> tempFiles = new ArrayList<>();
      
      // remove $ classes, directories, non-class files, test classes from class
      // list
      for (i=0; i<srcClassFiles.length; i++)
      {
         File f = srcClassFiles[i];
         name = f.getName();
         
         if ((name.indexOf("$") != -1) || f.isDirectory()
               || !name.endsWith(".class") || name.endsWith("Test.class"))  
            continue;
         else
            tempFiles.add(f);
      }
      
      srcClassFiles = new File[tempFiles.size()];
      
      for (i=0; i<tempFiles.size(); i++)
      {
         srcClassFiles[i] = tempFiles.get(i);
      }
          
      tempFiles = new ArrayList<>();
      
      // remove directories, non-java files, test files from java file list
      for (i=0; i<srcJavaFiles.length; i++)
      {
         File f = srcJavaFiles[i];
         
         if (f.isDirectory() || !f.getName().endsWith(".java")
               || f.getName().endsWith("Test.java"))  
            continue;
         else
            tempFiles.add(f);
      }
      
      srcJavaFiles = new File[tempFiles.size()];
      
      for (i=0; i<tempFiles.size(); i++)
      {
         srcJavaFiles[i] = tempFiles.get(i);
      }      
      
      // user reflection to generate classes
      Class[] srcClasses = new Class[srcJavaFiles.length];
      
      for (i=0; i<srcClassFiles.length; i++)
      {
         name = srcClassFiles[i].getName();
         
         name = name.substring(0, name.length() - 6); //remove .class
         
         try {
            Class c = Class.forName(srcPackage + "." + name);
            srcClasses[i] = c;
         }
         catch (Exception e) {
            log.error(e.getMessage());
            log.debug(e);
         }

      }
      
      // read in java class, write out after removing comments, bad imports
      String result = null;
      Class srcClass = null;
      Method methods[] = null;

         for (i=0; i<srcClasses.length; i++) {
            try (FileInputStream in = new FileInputStream(srcJavaFiles[i].getPath())) {
               try (BufferedReader b = new BufferedReader(new InputStreamReader(in))) {
                  try (FileWriter writer = new FileWriter(tgtDirectory.getPath() + File.separator + srcJavaFiles[i].getName())) {

                     String output = null;
                     String imports = "";

                     while ((output = b.readLine()) != null) {

                        if (output.indexOf("public") != -1) {
                           if (output.indexOf("class") != -1)
                              break;
                        }

                        // keep only installshield and java imports
                        if (output.indexOf("import") != -1) {
                           if ((output.indexOf("installshield") != -1) ||
                                   (output.indexOf("java") != -1)) {
                              imports += output + "\n";
                              continue;
                           }
                        }

                        if ((output.indexOf("/*") != -1) ||
                                (output.indexOf("*") != -1))
                           continue;
                     }

                     srcClass = srcClasses[i];
                     Package srcPack = srcClass.getPackage();
                     Class[] srcInterfaces = srcClass.getInterfaces();
                     Class srcSuper = srcClass.getSuperclass();
                     String header = "";
                     String srcClassName = srcClass.getName();
                     ArrayList beanProps = new ArrayList();

                     // build class declaration
                     header = "package " + srcPack.getName() + ";\n\n";
                     header += imports + "\n\n";

                     int dotIndex = srcClassName.lastIndexOf('.');
                     String className = srcClassName.substring(dotIndex + 1, srcClassName.length());
                     String realClassName = className;

                     String classLabel = "class";
                     int classMods = srcClass.getModifiers();

                     if (Modifier.isInterface(classMods)) {
                        header += "public interface " + className + " ";
                        header += "\n{}";
                        writer.write(header);
                        writer.close();
                        continue;
                     }

                     header += "public " + classLabel + " " + className + " ";

                     if (srcSuper != null && !srcSuper.getName().equals("Object")) {
                        String srcSuperName = srcSuper.getName();
                        dotIndex = srcSuperName.lastIndexOf('.');
                        className = srcSuperName.substring(dotIndex + 1, srcSuperName.length());
                        header += "extends " + className + " ";
                     }
            
            /*if (srcInterfaces.length > 0)
            {
               header += "implements ";
               for (int s = 0; s < srcInterfaces.length; s++)
               {
                  String srcInterName = srcInterfaces[s].getName();
                  dotIndex = srcInterName.lastIndexOf('.');
                  className = srcInterName.substring(dotIndex + 1, srcInterName.length());
                  header += className;
                  if (s < srcInterfaces.length - 1)
                     header += ",";
               }
            }*/

                     writer.write(header);
                     writer.write("\n{\n");

                     // build method declarations
                     methods = srcClass.getDeclaredMethods();
                     String methOut = "";
                     for (int j = 0; j < methods.length; j++) {
                        Method meth = methods[j];
                        int mods = meth.getModifiers();

                        // skip if private
                        if (Modifier.isPrivate(mods))
                           continue;

                        methOut = Modifier.toString(mods) + " ";

                        // get return type
                        Class returnType = meth.getReturnType();
                        String cleanType = returnType.getName();
                        int methMods = returnType.getModifiers();

                        if (returnType.isArray()) {
                           Class arrType = returnType.getComponentType();
                           String arrTypeName = arrType.getName();
                  /*cleanType = cleanType.substring(
                        cleanType.indexOf("[") + 2,
                        cleanType.length() - 1);*/
                           cleanType = arrTypeName;
                           methOut += cleanType + "[] ";
                        } else
                           methOut += cleanType + " ";

                        String methName = meth.getName();
                        methOut += methName;

                        boolean exists = false;

                        String propName = "";

                        // if getter method and bean property doesn't exist, add
                        if (methName.startsWith("get")) {
                           propName = methName.substring(3, methName.length());
                           exists = beanPropExists(propName, beanProps);

                           if (!exists) {
                              if (!returnType.getName().equals("void"))
                                 beanProps.add(new BeanProperty(propName, returnType, mods));
                           }
                        }

                        if (methName.startsWith("is")) {
                           propName = methName.substring(2, methName.length());
                           exists = beanPropExists(propName, beanProps);

                           if (!exists)
                              beanProps.add(new BeanProperty(propName, returnType, mods));
                           else {
                              getBeanProp(propName, beanProps).setbMods(mods);
                           }
                        }

                        // if setter method and bean property doesn't exist, add
                        if (methName.startsWith("set")) {
                           propName = methName.substring(3, methName.length());
                           exists = beanPropExists(propName, beanProps);

                           if (!exists) {
                              if (meth.getParameterTypes().length == 1)
                                 beanProps.add(new BeanProperty(propName, meth.getParameterTypes()[0], mods));
                           }
                        }

                        // get parameters
                        Class paramTypes[] = meth.getParameterTypes();
                        String paramName = "param";

                        methOut += "(";
                        int k;
                        for (k = 0; k < paramTypes.length; k++) {
                           String pType = paramTypes[k].getName();

                           if (paramTypes[k].isArray()) {
                              Class arrType = paramTypes[k].getComponentType();
                              String arrTypeName = arrType.getName();
                     /*pType = pType.substring(
                           pType.indexOf("[") + 2,
                           pType.length() - 1);*/
                              pType = arrTypeName;
                              pType += "[]";
                           }

                           methOut += pType + " " + paramName + (k + 1);
                           if (k < paramTypes.length - 1)
                              methOut += ",";
                        }
                        methOut += ") ";

                        // get exceptions thrown
                        Class execTypes[] = meth.getExceptionTypes();

                        if (execTypes.length > 0)
                           methOut += "throws ";

                        for (k = 0; k < execTypes.length; k++) {
                           methOut += execTypes[k].getName();
                           if (k < execTypes.length - 1)
                              methOut += ",";
                        }
                        methOut += "\n";

                        methOut += "{";

                        // construct body

                        String bProp = "";

                        // if getter method, return bean property
                        if (methName.startsWith("get") && !meth.getReturnType().getName().equals("void")) {
                           bProp = methName.substring(3, methName.length());
                           methOut += "return m_" + bProp + ";";
                        } else if (methName.startsWith("is") && !meth.getReturnType().getName().equals("void")) {
                           bProp = methName.substring(2, methName.length());
                           methOut += "return m_" + bProp + ";";
                        } else if (methName.startsWith("set") && meth.getParameterTypes().length == 1) {  // if setter method, set bean property
                           boolean match = true;
                           bProp = methName.substring(3, methName.length());
                           for (int l = 0; l < beanProps.size(); l++) {
                              BeanProperty prop = (BeanProperty) beanProps.get(l);
                              if (bProp.equalsIgnoreCase(prop.getbName())) {
                                 Class type = prop.getbType();
                                 if (!type.getName().equalsIgnoreCase(
                                         meth.getParameterTypes()[0].getName())) {
                                    match = false;
                                    methOut += "m_" + bProp + " = null;";
                                    break;
                                 }
                              }
                           }

                           if (match)
                              methOut += "m_" + bProp + " = " + "param1;";
                        } else if (methName.equals("defaultName")) {
                           // this is default name of condition bean
                           methOut += "return new " + cleanType + "(\"" + realClassName + "\");";
                        } else {
                           if (returnType.isArray())
                              methOut += "return new " + cleanType + "[0];";
                           else {
                              if (cleanType.equals("int") ||
                                      cleanType.equals("byte") ||
                                      cleanType.equals("double") ||
                                      cleanType.equals("float") ||
                                      cleanType.equals("long") ||
                                      cleanType.equals("short"))
                                 methOut += "return -1;";
                              else if (cleanType.equals("boolean"))
                                 methOut += "return false;";
                              else if (cleanType.equals("void"))
                                 methOut += "return;";
                              else if (cleanType.equals("char"))
                                 methOut += "return 'c';";
                              else {
                                 if (Modifier.isAbstract(methMods))
                                    methOut += "return null;";
                                 else if (cleanType.equals("com.percussion.install.Code") ||
                                         cleanType.equals("java.net.URL") ||
                                         cleanType.equals("java.awt.Color") ||
                                         cleanType.equals("java.lang.Class"))
                                    methOut += "return null;";
                                 else
                                    //methOut += "return null;";
                                    methOut += "return new " + cleanType + "();";
                              }
                           }
                        }
                        methOut += "}";

                        writer.write(methOut + "\n\n");
               
               
              
              /*  System.out.println("name 
                     = " + m.getName());
                        System.out.println("decl class = " +
                              m.getDeclaringClass());
                     Class pvec[] = m.getParameterTypes();
                     for (int j = 0; j < pvec.length; j++)
                        System.out.println("
                              param #" + j + " " + pvec[j]);
                              Class evec[] = m.getExceptionTypes();
                              for (int j = 0; j < evec.length; j++)
                                 System.out.println("exc #" + j 
                                       + " " + evec[j]);
                              System.out.println("return type = " +
                                    m.getReturnType());
                              System.out.println("-----"); */
                     }

                     writer.write("\n\n");

                     // write out bean properties
                     int k;
                     for (k = 0; k < beanProps.size(); k++) {
                        BeanProperty bp = (BeanProperty) beanProps.get(k);
                        String cleanBpType = "";

                        if (bp.getbType().isArray())
                           cleanBpType = bp.getbType().getComponentType().getName() + "[]";
                        else
                           cleanBpType = bp.getbType().getName();

                        if (cleanBpType.equals("void"))
                           continue;

                        String propOut = "";

                        if (Modifier.isStatic(bp.getMods()))
                           propOut = "private static " + cleanBpType;
                        else
                           propOut = "private " + cleanBpType;

                        propOut += " m_" + bp.getbName();

                        if (bp.getbType().isArray())
                           propOut += " = new " + bp.getbType().getComponentType().getName() + "[0]";

                        propOut += ";";

                        writer.write(propOut + "\n");
                     }

                     writer.write("}");
                     writer.close();
                  }
               }
            }
         }
   }
   
   /**
    * This class may be used from the command line.
    * 
    * Arguments expected are:
    *
    * <ol>
    * <li>sourceClassDir: The source compiled class directory to build from. 
    * Must point to an existing directory with compiled class files.
    * </li>
    *
    * <li>sourceJavaDir: The source java class directory corresponding to the
    * source class directory.  Must point to an existing directory with java class
    * files.
    * </li>
    *
    * <li>outputJavaDir: The output directory for the generated java classes. 
    * Must point to an existing directory.
    * </li>
    *
    * <li>package: The package of the generated java classes.
    * </li>
    * 
    * </ol>
    *
    */
   public static void main(String[] args)
   {
      File srcClassDir = null;
      File srcJavaDir = null;
      File tgtDir = null;
      String srcPack = null;
      try
      {
         // get the args
         if (args.length < 4)
         {
            System.out.println("Incorrect number of arguments.");
            printUsage();
            return;
         }

         srcClassDir = new File(args[0]);
         srcJavaDir = new File(args[1]);
         tgtDir = new File(args[2]);
         srcPack = args[3];
         
         PSClassGenerator pscg = new PSClassGenerator();
         pscg.createClasses(srcClassDir, srcJavaDir, tgtDir, srcPack);
          
      }
      catch(Throwable t)
      {
         t.printStackTrace();
      }
   }

   /**
    * Prints cmd line usage to the screen.
    */
   private static void printUsage()
   {
      System.out.println("Usage:");
      System.out.print("java com.percussion.tools.simple.PSClassGenerator ");
      System.out.println(
         "<source class dir> <source java dir> <output java dir> <package>");
   }
   
   private boolean beanPropExists(String name, ArrayList alist)
   {
      int l;
      boolean exists = false;
      
      for (l = 0; l < alist.size(); l++)
      {
         String beanPropName = ((BeanProperty) alist.get(l)).getbName();
         if (name.equals(beanPropName))
         {
            exists = true;
            break;
         }
      }
      
      return exists;   
   }
         
   private BeanProperty getBeanProp(String name, ArrayList alist)
   {
      int m;
      BeanProperty beanProp = null;
      BeanProperty tmpProp = null;
      
      for (m = 0; m < alist.size(); m++)
      {
         tmpProp = (BeanProperty) alist.get(m);
         String beanPropName = tmpProp.getbName();
         if (name.equals(beanPropName))
         {
            beanProp = tmpProp;
            break;
         }
      }
      
      return beanProp;
   }
   
   private class BeanProperty
   {
      public BeanProperty(String name, Class type, int mods)
      {
         bName = name;
         bType = type;
         bMods = mods;
      }
      
      public String getbName()
      {
         return bName;
      }
      
      public void setbName(String name)
      {
         bName = name;
      }
      
      public Class getbType()
      {
         return bType;
      }
               
      public void setbType(Class type)
      {
         bType = type;
      }
      
      public int getMods()
      {
         return bMods;
      }
      
      public void setbMods(int mods)
      {
         bMods = mods;
      }
      
      private String bName = "";
      private Class bType = null;
      private int bMods = 0;
   }
}
