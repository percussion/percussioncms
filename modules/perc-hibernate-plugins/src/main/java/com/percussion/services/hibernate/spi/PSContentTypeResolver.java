package com.percussion.services.hibernate.spi;

import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.service.spi.Startable;
import org.hibernate.service.spi.Stoppable;

public class PSContentTypeResolver implements Startable, Stoppable,IntegratorService{
    /**
     * Retrieve all integrators.
     *
     * @return All integrators.
     */
    @Override
    public Iterable<Integrator> getIntegrators() {
        return null;
    }

    /**
     * Start phase notification
     */
    @Override
    public void start() {

    }

    /**
     * Stop phase notification
     */
    @Override
    public void stop() {

    }
}
