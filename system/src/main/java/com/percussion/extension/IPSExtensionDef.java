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

package com.percussion.extension;

import java.util.Iterator;

/**
 * An IPSExtensionDef defines an extension and its deployment
 * settings, including the URLs of all its resources.
 * The actual contents of the resources are specified elsewhere.
 * <p>
 * This interface may be extended to support particular kinds of
 * extension definitions.
 */
public interface IPSExtensionDef
{
   /**
    * The full classname for the extension.
    */
   public static final String INIT_PARAM_CLASSNAME =
      "className";
   
   /**
    * The javascript version.
    */
   public static final String INIT_PARAM_JAVASCRIPT_VERSION =
      "version";
   
   /**
    * The extension version.
    */
   public static final String INIT_PARAM_VERSION =
      "com.percussion.extension.version";
   
   /**
    * The full classname of the factory that should be used to
    * serialize and deserialize this def. This is for future extensibility.
    */
   public static final String INIT_PARAM_FACTORY =
      "com.percussion.extension.factoryClassName";

   /**
    * "yes" or "no" depending on whether this extension is reentrant. This
    * is used by the server. The default value is "no". The initial version
    * only supports re-entrant extensions.
    */
   public static final String INIT_PARAM_REENTRANT =
      "com.percussion.extension.reentrant";

   /**
    * This is provided so all users will use this property consistently.
    * "yes" or "no" depending on whether this extension is scriptable.
    * The server does not use this property.
    */
   public static final String INIT_PARAM_SCRIPTABLE =
      "com.percussion.user.scriptable";

   /**
    * This is provided so all users will use this property consistently.
    * The human-readable, freeform description of this extension.
    * The server does not use this property.
    */
   public static final String INIT_PARAM_DESCRIPTION =
      "com.percussion.user.description";
   
   /**
    * Usually file name extension of template source for given assembler.
    * If specified then template editor adds this suffix when generating file
    * name it uses to discover editor for this source.
    * If not specified no editor will be displayed.
    * Sample values: ".txt", ".xml".
    */
   public static final String INIT_PARAM_ASSEMBLY_FILE_SUFFIX =
      "com.percussion.extension.assembly.fileSuffix";
   
   /**
    * Controls the loading of velocity macros. yes means that the
    * velocity engine is told to reload on change. sys_reinit=true may 
    * need to be used anyway.
    */
   public static final String INIT_PARAM_ASSEMBLY_AUTO_RELOAD =
      "com.percussion.extension.assembly.autoReload";
   
   /**
    * A comma delimited list of &quot;library&quot; velocity macro
    * files to load from the library locations.
    */
   public static final String INIT_PARAM_ASSEMBLY_LIBRARIES =
      "com.percussion.extension.assembly.libraries";
   
   /**
    * The the javascript script body.
    */
   public static final String INIT_PARAM_SCRIPT_BODY =
      "scriptBody";

   /**
    * Returns the extension reference containing the fully qualified
    * name of this extension.
    *
    * @return The extension reference. Never <CODE>null</CODE>.
    */
   public PSExtensionRef getRef();

   /**
    * Returns the fully qualified names of the known extension interfaces
    * implemented by this extension. These interfaces help to define
    * where instances of this extension may be used (for example, is
    * this a JDBC driver, a password filter, a result document
    * processor, or some combination of the three?).
    *
    * @return An Iterator over one or more non-<CODE>null</CODE> Strings.
    * Never <CODE>null</CODE>.
    */
   public Iterator getInterfaces();

   /**
    * Compares the supplied interface name against the interfaces supported
    * by this extension and returns the result.
    *
    * @param iface The fully qualified name of the interface to check.
    *
    * @return <code>true</code> if the supplied interface is supported by
    * this extension, <code>false</code> otherwise.
    */
   public boolean implementsInterface( String iface );

   /**
    * Returns the names of the initialization parameters.
    * Initialization parameters will be passed to the extension
    * (via an extension-specific mechanism) when the extension is first
    * initialized. All parameters beginning with "com.percussion.extension"
    * are used by the extension subsystem. No user defined params should
    * begin with this suffix.
    * The order of parameters is unspecified and should not be taken to
    * mean anything.
    *
    * @return An Iterator over zero or more non-<CODE>null</CODE> Strings.
    * Never <CODE>null</CODE>.
    */
   public Iterator getInitParameterNames();

   /**
    * Returns a String containing the value of the named initialization
    * parameter of the extension, or <CODE>null</CODE> if the parameter
    * does not exist. All parameters beginning with "com.percussion.extension"
    * are used by the extension subsystem. No user defined params should
    * begin with this suffix.
    *
    * @param name The parameter name. Must not be <CODE>null</CODE>.
    *
    * @return The value of the named parameter, or <CODE>null</CODE> if
    * it does not exist. This method will never return <CODE>null</CODE>
    * when called with a String value from the parameter name
    * iteration.
    *
    * @see #getInitParameterNames
    */
   public String getInitParameter(String name);

   /**
    * Gets the names of all runtime parameters required by this extension.
    * These runtime parameters must be bound (in the returned order) to
    * the extension instance at runtime (usually the caller of the
    * extension is responsible for doing the binding).
    *
    * @return An Iterator over zero or more non-<CODE>null</CODE>
    * String param names. The order of the parameter names
    * is important and should be preserved.
    */
   public Iterator getRuntimeParameterNames();

   /**
    * Gets the parameter definition for the named runtime parameter,
    * or <CODE>null</CODE> if no parameter by that name is used.
    *
    * @param name The param name. Must not be <CODE>null</CODE>.
    *
    * @return The parameter definition, or <CODE>null</CODE>.
    */
   public IPSExtensionParamDef getRuntimeParameter(String name);

   /**
    * Returns the locations of all resources required by the defined
    * extension. This includes any files which make up the extension
    * itself (such as Java .class or .jar files, native executables
    * and libraries, and images and .properties files).
    * <p>
    * <STRONG>Note</STRONG>: Depending on the type of extension defined, not
    * all resources need to be specified explicitly. For instance,
    * some unspecified resources may be contained within resources
    * which are explicitly specified (such as files and subdirectories
    * which are contained within an explicitly specified directory,
    * or Java .class files which are contained within an explicitly
    * specified .jar file, and so on).
    * <p>
    * In the current version of the product, each resource location must be
    * a relative <CODE>file:</CODE> URL whose parent directory will be
    * determined at runtime by the extension handler.
    * <p>
    * In future versions, some extensions may be allowed to use absolute
    * <CODE>file:</CODE> URLs and/or other protocols (such as http). Handler
    * implementations should verify that they understand the locations
    * of each specified resource before installing the extension.
    *
    * @return An Iterator over zero or more non-<CODE>null</CODE>
    * URLs. Never <CODE>null</CODE>.
    */
   public Iterator getResourceLocations();


   /**
    * Returns the list of URL objects that point to the files supplied with
    * this def when it was installed or last updated.
    *
    * @return An Iterator over zero or more URL objects.  May be
    * <code>null</code> if this def does not contain such a list (backward
    * compatibility.
    */
   public Iterator getSuppliedResources();

   /**
    * Sets the list of supplied resource files installed with this extension so
    * their names and locations will be saved with this definition.
    * @param resources A non-<code>null</code> but possibly empty Iterator of
    * URL objects.
    */
   public void setSuppliedResources(Iterator resources);

   /**
    * Sets whether or not this extension has been deprecated.
    *
    * @param isDeprecated If <code>true</code>, then this extension is flagged
    * as having been deprecated.  If <code>false</code>, then this extension
    * is not deprecated.
    */
   public void setDeprecated(boolean isDeprecated);

   /**
    * Determines if this extension has been deprecated.
    *
    * @return <code>true</code> if this extension has been deprecated, <code>
    * false</code> if not.
    */
   public boolean isDeprecated();

   /**
    * @return <code>true</code> indicates that if this extension call is
    * followed by an error condition then the request parameters modified by this
    * extension should be explicitly restored by calling PSRequest.restoreParams(),
    * <code>false</code> otherwise.
   */
   public boolean isRestoreRequestParamsOnError();
   
   /**
    * Sets the list of the applications required by the implementation of this
    * extension.
    * 
    * @param apps An iterator over zero or more application names as 
    * <code>String</code> objects, may not be <code>null</code>.
    */
   public void setRequiredApplications(Iterator apps);
   
   /**
    * Get the list of required applications.  See 
    * {@link #setRequiredApplications(Iterator)} of more info.
    * 
    * @return An iterator over zero or more application names as 
    * <code>String</code> objects, never <code>null</code>.
    */
   public Iterator getRequiredApplications();
   
   /**
    * Is this extension based on the java expression language (JEXL)?
    * 
    * @return <code>true</code> if it is, <code>false</code> otherwise.
    */
   public boolean isJexlExtension();
   
   /**
    * Add the supplied extion method.
    * 
    * @param method the new method to add, not <code>null</code>. If a method
    *    with the same name already exists, it will be overridden. Otherwise
    *    the new method is added.
    */
   public void addExtensionMethod(PSExtensionMethod method);
   
   /**
    * Get all extension methods.
    * 
    * @return all extension methods, never <code>null</code>, may be empty.
    */
   public Iterator<PSExtensionMethod> getMethods();
   
   /**
    * Removes the identified extension method.
    * 
    * @param name the name or the extension method to be removed, not
    *    <code>null</code> or empty. Does nothing if no method exists for the
    *    supplied name.
    */
   public void removeExtensionMethod(String name);
   
   /**
    * Clone this instance.
    * 
    * @return a clone of this instance, never <code>null</code>.
    */
   public IPSExtensionDef clone();
   
   /**
    * Returns an integer form of the "rhythmyx.extension.version"
    * init property.  If the property is not specified or invalid, it will
    * return 1 (one).
    *
    * @return A positive int representing the extension version.
    */
   public int getVersion();
}
