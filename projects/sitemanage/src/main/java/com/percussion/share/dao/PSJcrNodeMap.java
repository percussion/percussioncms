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
package com.percussion.share.dao;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.jsr170.PSMultiProperty;
import com.percussion.utils.tools.PSCopyStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * Lazy loaded Map of properties from a JCR Node.
 * Updates are allowed to the map but do not actually update
 * the JCR Node. <strong>The JCR Node is never altered.</strong>
 * 
 * @author adamgent
 *
 */
public class PSJcrNodeMap extends AbstractMap<String, Object>
{
    private static final String JCR_PREFIX = "rx:";

    private Node node;

    private Map<String, Object> override = new HashMap<>();
    
    private boolean allowBinary = false;

    /**
     * Allow binary is set to false. See other constructor.
     * @param node never <code>null</code>.
     * @see #PSJcrNodeMap(Node, boolean)
     */
    public PSJcrNodeMap(Node node)
    {
        super();
        this.node = node;
    }
    
    /**
     * Constructs a map that will delegate to the given node if the property has
     * not been overridden.
     * @param node never <code>null</code>.
     * @param allowBinary <code>true</code> will allow you to retrieve the binary data of a binary property as a
     *  {@link PSPurgableTempFile}.  Default is <code>false</code> as <code>true</code> is usually dangerous because of
     *  character encoding and size of binaries.
     */
    public PSJcrNodeMap(Node node, boolean allowBinary)
    {
        super();
        this.node = node;
        this.allowBinary = allowBinary;
    }
    
    @Override
    public String toString()
    {
        try
        {
            Iterator<Entry<String, Object>> i = entrySet().iterator();
            if (!i.hasNext())
                return "{}";

            StringBuilder sb = new StringBuilder();
            sb.append('{');
            for (;;)
            {
                Entry<String, Object> e = i.next();
                String key = e.getKey();
                Property p = getNodeProperty(key);
                String value =
                    p != null && p.getType() == PropertyType.BINARY ? "<BINARY>" : (e.getValue()==null?"":e.getValue().toString());
                    
                sb.append(key);
                sb.append('=');
                sb.append(value);
                if (!i.hasNext())
                    return sb.append('}').toString();
                sb.append(", ");
            }
        }
        catch (RepositoryException e)
        {
            return "{ <ERROR> }";
        }
    }    

    @Override
    public Object get(Object key)
    {
        notNull(key, "key");
        String k  = StringUtils.removeStart((String)key, JCR_PREFIX);
        if (override.containsKey(k)) return override.get(k);
        return getNodePropertyValue(k);
    }
    
    private Object getNodePropertyValue(String k) {
        Property p = getNodeProperty(k);
        FileOutputStream fos = null;
        try
        {
            if (p == null) return null;
            /*
             * TODO probably should base64 encode binaries
             */
            if (p.getType() == PropertyType.BINARY)
            {
                if (!allowBinary)
                {
                    return "";
                }
                else
                {
                    PSPurgableTempFile ptf = new PSPurgableTempFile("tmp", null, null);
                    fos = new FileOutputStream(ptf);
                    PSCopyStream.copyStream(p.getStream(), fos);

                    return ptf;
                }
            }
            if(p instanceof PSMultiProperty)
            {
               List<String> multiValues = new ArrayList<>();
               Value[] values = p.getValues();
               for(Value value : values)
               {
                  multiValues.add(value.getString());
               }
               return multiValues;
            }
            
            return p.getString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    
                }
            }
        }
    }

    private Property getNodeProperty(String k) {
        try
        {
            if (node.hasProperty(k))
                return node.getProperty(k);
        }
        catch (RepositoryException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Object put(String key, Object value)
    {
        notNull(key, "key");
        String k = StringUtils.removeStart(key, JCR_PREFIX);
        return override.put(k, value);
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet()
    {
        return new PSPropertySet(node);
    }

    public class PSEntry implements Entry<String, Object>
    {

        private String key;


        public PSEntry(String key)
        {
            super();
            notNull(key);
            this.key = key;
        }

        public String getKey()
        {
            return key;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public Object getValue()
        {
            return get(key);
        }

        public Object setValue(Object value)
        {
            return put(key, value);
        }

        @Override
        public int hashCode()
        {
            return key.hashCode();
        }
        
        

    }

    public class PSPropertyIterator implements Iterator<Entry<String, Object>>
    {

        PropertyIterator pi;

        public PSPropertyIterator(PropertyIterator pi)
        {
            super();
            this.pi = pi;
        }

        public boolean hasNext()
        {
            return pi.hasNext();
        }

        public Entry<String, Object> next()
        {
            Property p = pi.nextProperty();
            if (p == null)
                return null;
            try
            {
                return new PSEntry(StringUtils.removeStart(p.getName(), JCR_PREFIX));
            }
            catch (RepositoryException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException("remove is not yet supported");
        }

    }

    public class PSPropertySet extends AbstractSet<Map.Entry<String, Object>>
    {

        private Node node;

        public PSPropertySet(Node node)
        {
            super();
            this.node = node;
        }

        @Override
        public Iterator<Entry<String, Object>> iterator()
        {
            try
            {
                PropertyIterator pi = node.getProperties();
                return new PSPropertyIterator(pi);

            }
            catch (RepositoryException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public int size()
        {
            try
            {
                return (int) node.getProperties().getSize();
            }
            catch (RepositoryException e)
            {
                throw new RuntimeException(e);
            }
        }

    }

}
