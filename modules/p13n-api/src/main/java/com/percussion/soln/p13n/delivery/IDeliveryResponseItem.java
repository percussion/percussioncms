package com.percussion.soln.p13n.delivery;

import java.util.Iterator;

import javax.jcr.RepositoryException;

/**
 * 
 * A item in the delivery system that is ready to be processed and then returned to 
 * the visitor.
 * The item is a subset of the JCR 170 Node interface.
 * 
 * @author adamgent
 *
 */
public interface IDeliveryResponseItem {
    
    public String getId();
    
    /**
     * Gets property or fail.
     * @param name never <code>null</code>.
     * @return never <code>null</code>.
     * @throws RepositoryException
     */
    public IDeliveryProperty getProperty(String name) throws RepositoryException;
    
    /**
     * 
     * @return never <code>null</code>.
     * @throws RepositoryException
     */
    public Iterator<IDeliveryProperty> getProperties() throws RepositoryException;
    
    /**
     * 
     * @param name never <code>null</code>.
     * @return <code>true</code> if property exists.
     * @throws RepositoryException
     */
    public boolean hasProperty(String name) throws RepositoryException;
      
}
