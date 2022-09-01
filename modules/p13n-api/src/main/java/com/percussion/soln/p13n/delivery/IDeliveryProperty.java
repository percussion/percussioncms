package com.percussion.soln.p13n.delivery;

import java.util.Calendar;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

/**
 * An abridged version of the JSR170 Node Property for Delivery Items.
 * @author adamgent
 *
 */
public interface IDeliveryProperty {

    public boolean getBoolean() throws ValueFormatException, RepositoryException;

    public Calendar getDate() throws ValueFormatException, RepositoryException;
    
    public boolean isMultiple() throws RepositoryException;

    public double getDouble() throws ValueFormatException, RepositoryException;

    public long getLong() throws ValueFormatException, RepositoryException;

    public String getName() throws RepositoryException;

    public String getString() throws ValueFormatException, RepositoryException;

    public int getType() throws RepositoryException;

    public Value getValue() throws ValueFormatException, RepositoryException;

    public Value[] getValues() throws ValueFormatException, RepositoryException;
}
