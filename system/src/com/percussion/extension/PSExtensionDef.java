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

package com.percussion.extension;

import com.percussion.design.objectstore.PSExtensionParamDef;
import com.percussion.services.data.IPSCloneTuner;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Element;

/**
 * An extension definition defines the name, initialization parameters,
 * and resource locations for an extension. The actual files (if any)
 * which make up the extension are supplied elsewhere.
 */
public class PSExtensionDef implements IPSExtensionDef, Serializable,
   IPSCloneTuner
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = -4369644099981854127L;

   /**
    * Convenients constructor that calls {@link #PSExtensionDef(PSExtensionRef, 
    * Iterator, Iterator, Properties, Iterator, Iterator, boolean, boolean) 
    * PSExtensionDef(ref, interfaces, resourceURLs, initParams, runtimeParams, 
    * null, false, false)}.
    */
   public PSExtensionDef(PSExtensionRef ref, Iterator interfaces,
      Iterator resourceURLs, Properties initParams, Iterator runtimeParams)
   {
      this(ref, interfaces, resourceURLs, initParams, runtimeParams, null,
         false, false);
   }

   /**
    * Constructs a new extension def.
    *
    * @param ref The extension reference. Must not be <CODE>null</CODE>.
    *
    * @param interfaces An Iterator over 1 or more non-<CODE>null</CODE>
    * names of interfaces that this extension implements.
    *
    * @param resourceURLs An Iterator over 0 or more non-<CODE>null</CODE>
    * URL objects referring to resources used by the extension. May be
    * <CODE>null</CODE>, in which case the extension must be self-contained.
    *
    * @param initParams A Properties object containing 0 or more custom
    * initialization properties required by the defined extension. May be
    * <CODE>null</CODE>, in which case no properties will be set.
    *
    * @param runtimeParams An iterator over zero or non-<CODE>null</CODE>
    * PSExtensionParamDef objects. The order of parameters
    * is important. May be <CODE>null</CODE>, in which case no runtimeParam
    * defs will be set.
    *
    * @param suppliedResources An Iterator over 0 or more non-<CODE>null</CODE>
    * URL objects referring to files used by the extension. May be
    * <CODE>null</CODE>, in which case they may be added later using
    * {@link #setSuppliedResources(Iterator) setSuppliedResources()}.
    *
    * @param isDeprecated If <code>true</code>, then this extension will be
    * flagged as deprecated, if <code>false</code>, it will not.
    *
    * @param isRestoreRequestParamsOnError <code>true</code> indicates that
    * based on the Extensions.xml restoreRequestParamsOnError="yes" attribute
    * this extension modifies request parameters, <code>false</code> otherwise.
    */
   public PSExtensionDef(PSExtensionRef ref, Iterator interfaces,
      Iterator resourceURLs, Properties initParams, Iterator runtimeParams,
      Iterator suppliedResources, boolean isDeprecated,
      boolean isRestoreRequestParamsOnError)
   {
      if (ref == null)
         throw new IllegalArgumentException("Extension ref cannot be null");

      if (interfaces == null)
         throw new IllegalArgumentException("interfaces cannot be null");

      if (!interfaces.hasNext())
         throw new IllegalArgumentException(
            "At least one interface must be defined");

      m_ref = ref;
      m_resURLs = new ArrayList<URL>();
      if (resourceURLs != null)
      {
         while (resourceURLs.hasNext())
         {
            // do an unnecessary cast so that any objects of the wrong
            // type will cause an error
            URL u = (URL) (resourceURLs.next());
            if (u == null)
            {
               throw new IllegalArgumentException(
                  "null resource URLs are not allowed");
            }
            m_resURLs.add(u);
         }
      }

      if (initParams != null)
      {
         m_initParams = (Properties)initParams.clone();
      }
      else
      {
         m_initParams = new Properties();
      }

      m_interfaces = new ArrayList<String>();
      while (interfaces.hasNext())
      {
         m_interfaces.add((String) interfaces.next());
      }

      m_runtimeParams = new ArrayList<PSExtensionParamDef>();
      m_runtimeParamsMap = new HashMap<String, PSExtensionParamDef>();
      if (runtimeParams != null)
      {
         while (runtimeParams.hasNext())
         {
            PSExtensionParamDef p = (PSExtensionParamDef)runtimeParams.next();
            m_runtimeParams.add(p);
            m_runtimeParamsMap.put(p.getName(), p);
         }
      }

      // add the supplied resources
      if (suppliedResources != null)
         setSuppliedResources(suppliedResources);

      // set deprecation flag
      m_isDepecated = isDeprecated;

      //set modifes request params flag
      m_isRestoreRequestParamsOnError = isRestoreRequestParamsOnError;
      
      m_requiredApplications = new ArrayList<String>();
   }

   /**
    * Default ctor. Added mainly to facilitate serialization. Could be in an
    * invalid state if required fields are not added using set/add methods.
    */
   public PSExtensionDef()
   {
      m_initParams = new Properties();
      m_resURLs = new ArrayList<URL>();
      m_interfaces = new ArrayList<String>();
      m_runtimeParams = new ArrayList<PSExtensionParamDef>();
      m_runtimeParamsMap = new HashMap<String, PSExtensionParamDef>();
      m_requiredApplications = new ArrayList<String>();
   }

   /**
    * @see IPSExtensionDef#getRef
    */
   public PSExtensionRef getRef()
   {
      return m_ref;
   }

   /**
    * Set/Replace the extension ref.
    * @param extRef must not be <code>null</code>
    */
   public void setExtensionRef(PSExtensionRef extRef)
   {
      if (extRef == null)
      {
         throw new IllegalArgumentException(
            "extRef must not be null"); //$NON-NLS-1$
      }
      m_ref = extRef;
   }

   /**
    * @see IPSExtensionDef#getInterfaces
    */
   public Iterator<String> getInterfaces()
   {
      return m_interfaces.iterator();
   }

   /**
    * Set the interface for the extension definition.
    * 
    * @param interfaces interface collection to set, must not be
    * <code>null</code> or empty.
    */
   @SuppressWarnings("unchecked")
   public void setInterfaces(Collection interfaces)
   {
      if (interfaces == null || interfaces.size() == 0)
      {
         throw new IllegalArgumentException(
            "interfaces must not be null or empty");
      }
      m_interfaces = interfaces;
   }

   /**
    * @see IPSExtensionDef#implementsInterface
    */
   public boolean implementsInterface( String iface )
   {
      return m_interfaces.contains( iface );
   }

   /**
    * @see IPSExtensionDef#getInitParameterNames
    */
   public Iterator getInitParameterNames()
   {
      return m_initParams.keySet().iterator();
   }

   /**
    * @see IPSExtensionDef#getInitParameter
    */
   public String getInitParameter(String name)
   {
      return m_initParams.getProperty(name);
   }

   /**
    * Sets the value of the named parameter, overwriting
    * any existing value.
    *
    * @param name The param name. Must not be <CODE>null</CODE>.
    *
    * @param value The param value. If <CODE>null</CODE>, the
    * param will be erased.
    *
    * @throw IllegalArgumentException If any param is invalid.
    */
   public void setInitParameter(String name, String value)
   {
      if (name == null)
         throw new IllegalArgumentException("name cannot be null");

      if(value != null)
         m_initParams.setProperty(name, value);
      else
         m_initParams.remove(name);
   }

   /**
    * @see IPSExtensionDef#getResourceLocations
    */
   public Iterator getResourceLocations()
   {
      return m_resURLs.iterator();
   }

   /** @see IPSExtensionDef#getRuntimeParameterNames */
   public Iterator getRuntimeParameterNames()
   {
      return new RuntimeParamNameIterator(m_runtimeParams.iterator());
   }

   /** @see IPSExtensionDef#getRuntimeParameter */
   public IPSExtensionParamDef getRuntimeParameter(String name)
   {
      return m_runtimeParamsMap.get(name);
   }
   
   /**
    * Set the runtime parameters for this definition
    * @param params may be <code>null</code>, if so then all
    * params will be cleared.
    */
   public void setRuntimeParameters(Iterator<PSExtensionParamDef> params)
   {
      m_runtimeParams = new ArrayList<PSExtensionParamDef>();
      m_runtimeParamsMap = new HashMap<String, PSExtensionParamDef>();
      if(params == null)
         return;
      while(params.hasNext())
      {
         PSExtensionParamDef param = params.next();
         m_runtimeParams.add(param);
         m_runtimeParamsMap.put(param.getName(), param);
      }
   }

   /**
    * Set the resource locations, see 
    * {@link IPSExtensionDef#getResourceLocations()} for details.
    * 
    * @param locations The locations, may not be <code>null</code>, may be
    * empty.
    */
   public void setResourceLocations(Collection<URL> locations)
   {
      if (locations == null)
         throw new IllegalArgumentException("locations may not be null");
      
      m_resURLs = locations;
   }

   /** @see IPSExtensionDef#getSuppliedResources */
   public Iterator getSuppliedResources()
   {
      Iterator resources = null;

      if (m_suppliedResources != null)
         resources = m_suppliedResources.iterator();

      return resources;
   }


   /** @see IPSExtensionDef#setSuppliedResources(Iterator) */
   public void setSuppliedResources(Iterator resources)
   {
      // validate input
      if (resources == null)
         throw new IllegalArgumentException("resources may not be null");

      m_suppliedResources = new ArrayList<URL>();

      // walk the list and build the internal collection
      while (resources.hasNext())
      {
         URL resource = (URL)resources.next();
         m_suppliedResources.add(resource);
      }
   }


   /** @see IPSExtensionDef#setDeprecated(boolean) */
   public void setDeprecated(boolean isDeprecated)
   {
      m_isDepecated = isDeprecated;
   }


   /** @see IPSExtensionDef#isDeprecated() */
   public boolean isDeprecated()
   {
      return m_isDepecated;
   }

   /** @see IPSExtensionDef#isRestoreRequestParamsOnError() */
   public boolean isRestoreRequestParamsOnError()
   {
      return m_isRestoreRequestParamsOnError;
   }

   // see IPSExtensionDef
   public void setRequiredApplications(Iterator apps)
   {
      if (apps == null)
         throw new IllegalArgumentException("apps may not be null");
      
      m_requiredApplications.clear();
      while (apps.hasNext())
      {
         m_requiredApplications.add(apps.next().toString());
      }
   }
   
   // see IPSExtensionDef
   public Iterator getRequiredApplications()
   {
      return m_requiredApplications.iterator();
   }
   
   /* (non-Javadoc)
    * @see IPSExtensionDef#isJexlExtension()
    */
   public boolean isJexlExtension()
   {
      Iterator interfaces = getInterfaces();
      while (interfaces.hasNext())
      {
         String iface = (String) interfaces.next();
         if (iface.equals(IPSJexlExpression.class.getName()))
            return true;
      }
      
      return false;
   }

   /* (non-Javadoc)
    * @see IPSExtensionDef#addExtensionMethod(PSExtensionMethod)
    */
   public void addExtensionMethod(PSExtensionMethod method)
   {
      if (method == null)
         throw new IllegalArgumentException("method cannot be null");
      
      m_methods.put(method.getName(), method);
   }

   /* (non-Javadoc)
    * @see IPSExtensionDef#getMethods()
    */
   public Iterator<PSExtensionMethod> getMethods()
   {
      return m_methods.values().iterator();
   }

   /* (non-Javadoc)
    * @see IPSExtensionDef#removeExtensionMethod(String)
    */
   public void removeExtensionMethod(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name cannot be null or empty");
      
      m_methods.remove(name);
   }

   /* (non-Javadoc)
    * @see IPSExtensionDef#getVersion()
    */
   public int getVersion()
   {
      int ver = 1;
      String verStr = getInitParameter(INIT_PARAM_VERSION);
      if (verStr != null)
      {
         try
         {
            ver = Integer.parseInt(verStr);
         }
         catch (NumberFormatException e)
         {
            /* ignore */
         }
      }
      
      if (ver < 1)
         ver = 1;

      return ver;
   }
   
   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }
   
   @Override
   public IPSExtensionDef clone()
   {
      IPSExtensionDef clone = null;
      try
      {
         PSExtensionDefFactory factory = new PSExtensionDefFactory();
         
         Element root = 
            PSXmlDocumentBuilder.createXmlDocument().createElement("root");
         clone = factory.fromXml(factory.toXml(root, this));
      }
      catch (PSExtensionException e)
      {
         // this should never happen
         throw new RuntimeException(e);
      }
      
      return clone;
   }

   /**
    * Gets string representation of this definition (extension reference).
    * 
    * @return the extension reference name, never <code>null</code> or empty.
    */
   @Override
   public String toString()
   {
      return m_ref.getExtensionName();
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.data.IPSCloneTuner#tuneClone(long)
    */
   public Object tuneClone(@SuppressWarnings("unused") long newId)
   {
      // nothing to do
      return this;
   }

   private class RuntimeParamNameIterator implements Iterator
   {
      public RuntimeParamNameIterator(Iterator params)
      {
         m_params = params;
      }

      public boolean hasNext()
      {
         return m_params.hasNext();
      }

      public Object next()
      {
         PSExtensionParamDef param = (PSExtensionParamDef)m_params.next();
         return param.getName();
      }

      public void remove()
      {
         throw new UnsupportedOperationException();
      }

      private Iterator m_params;
   }

   /** The extension name + handler name. */
   private PSExtensionRef m_ref;

   /** The URLs of resources used by the extension. */
   private Collection<URL> m_resURLs;

   /** Initialization properties for the extension. */
   private Properties m_initParams;

   /** The names of all relevant interfaces implemented by this extension */
   private Collection<String> m_interfaces;

   /** The runtime params, in the order passed into the constructor. */
   private Collection<PSExtensionParamDef> m_runtimeParams;

   /**
    * A map from runtime param names to runtime parameters, in no particular
    * order.
    */
   private Map<String, PSExtensionParamDef> m_runtimeParamsMap;

   /**
    * The files required by this extension as URL objects.  Should be a catalog
    * of files located in all locations supplied by {@link #m_resURLs}, unless
    * it is a jar file, in which case the jar is included in this list as a
    * file.
    * May be <code>null</code>, unless {@link #setSuppliedResources(Iterator)}
    * has been called, or the extension has been previously saved after such a
    * call.
    */
   private Collection<URL> m_suppliedResources;

   /**
    * The names of applications referenced by the implementation of this 
    * extension as <code>String</code> objects.  Never <code>null</code> after
    * construction, may be empty.  Modified by calls to 
    * {@link #setRequiredApplications(Iterator)}.
    */
   private Collection<String> m_requiredApplications;
   
   /**
    * Indicates if this Extension has been deprecated.  <code>True</code> if it
    * has been deprecated, <code>false</code> if not.  Modified by calls to
    * {@link #setDeprecated(boolean)}.
    */
   private boolean m_isDepecated = false;

   /**
    * Indicates if this Extension modifies request params.  Set during
    * construction, never modified after that.
    */
   private boolean m_isRestoreRequestParamsOnError = false;
   
   /**
    * A map with all supported extension methods, never <code>null</code>, 
    * may be empty.
    */
   private Map<String, PSExtensionMethod> m_methods = 
      new HashMap<String, PSExtensionMethod>();
}
