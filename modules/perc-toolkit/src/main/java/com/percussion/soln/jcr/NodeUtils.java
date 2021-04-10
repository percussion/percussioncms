package com.percussion.soln.jcr;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.contentmgr.IPSNodeDefinition;


public class NodeUtils {
    
    public abstract static class BeanPropertyToNodePropertyNameTranslator {
        public abstract String getPropertyName(PropertyDescriptor d);
    }
    
    public static class DefaultTranslator extends BeanPropertyToNodePropertyNameTranslator {

        private String fieldPrefix;
        private Collection<String> ignoreFields;
        private Map<String, String> nameMap;
        
        
        public DefaultTranslator(String fieldPrefix, Collection<String> ignoreFields, Map<String, String> nameMap) {
            super();
            this.fieldPrefix = fieldPrefix == null ? "" : fieldPrefix;
            this.ignoreFields = ignoreFields == null ? Collections.<String>emptyList() : ignoreFields;
            this.nameMap = nameMap;
        }

        @Override
        public String getPropertyName(PropertyDescriptor d) {
            String name = d.getName();
            if (ignoreFields.contains(name))
                return null;
            if (nameMap != null && nameMap.containsKey(name))
                return nameMap.get(name);
            return fieldPrefix + name;
        }
    }
    
    
    
    public static <T> void copyFromNode(Node node, T object, BeanPropertyToNodePropertyNameTranslator t) {
        notNull(node);
        notNull(object);
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(object);
        for (PropertyDescriptor d : descriptors) {
            String propName = t.getPropertyName(d);
            if (propName == null) continue;
            try {
                if (!node.hasProperty(propName)) {
                    continue;
                }
                Property p = node.getProperty(propName);
                Class<?> type = d.getPropertyType();
                if (type.isAssignableFrom(Long.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getLong());
                }
                else if (type.isAssignableFrom(Double.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getDouble());
                }
                else if (type.isAssignableFrom(String.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getString());
                }
                else if (type.isAssignableFrom(Boolean.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getBoolean());
                }
                else if (type.isAssignableFrom(Date.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getDate().getTime());
                }
                else if (type.isAssignableFrom(Calendar.class)) {
                    PropertyUtils.setProperty(object, d.getName(), p.getDate());
                }
                else if (type.isAssignableFrom(List.class)) {
                    List<String> vs = new ArrayList<String>();
                    if (p.getDefinition().isMultiple()) {
                        Value[] values = p.getValues();
                        for (Value v : values) {
                            vs.add(v.getString());
                        }
                    }
                    else {
                        vs.add(p.getString());
                    }
                    PropertyUtils.setProperty(object, d.getName(), vs);
                }
                else {
                    log.warn("Do not know how to convert type: " + type + " for property: " + propName);
                }
            } catch (Exception e) {
                log.error("Failure to copy property " + propName );
            }
            
        }
    }
    
    public static String getString(Node node, String name) {
        notNull(node);
        notEmpty(name);
        try {
            return node.getProperty(name).getString();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static Date getDate(Node node, String name) {
        notNull(node);
        notEmpty(name);
        try {
            return node.getProperty(name).getDate().getTime();
        } catch (Exception e) {
            return new Date();
        }
    }
    
    
    public static  String getContentType(Node node) {
        try {
            String contentType = ((IPSNodeDefinition) node.getDefinition()).getInternalName();
            return contentType;
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    public static <T> void copyFromNode(Node node, T object, String propertyPrefix, Collection<String> ignoreProperties) {
        copyFromNode(node, object, new DefaultTranslator(propertyPrefix, ignoreProperties, null));
    }
    
    public static <T> void copyFromNode(Node node, T object, 
            String propertyPrefix, 
            Collection<String> ignoreProperties,
            Map<String, String> nameMap) {
        copyFromNode(node, object, new DefaultTranslator(propertyPrefix, ignoreProperties, nameMap));
    }
    
    
    public static class NodeUtilsException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public NodeUtilsException(String message) {
            super(message);
        }

        public NodeUtilsException(String message, Throwable cause) {
            super(message, cause);
        }

        public NodeUtilsException(Throwable cause) {
            super(cause);
        }

    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(NodeUtils.class);


}
