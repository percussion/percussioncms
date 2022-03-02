package com.percussion.services.hibernate.spi;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.spi.ServiceContributor;

public class PSContentTypeResolverContributor implements ServiceContributor {

    /**
     * Contribute services to the indicated registry builder.
     *
     * @param serviceRegistryBuilder The builder to which services (or initiators) should be contributed.
     */
    @Override
    public void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        serviceRegistryBuilder.addService(
                ConnectionProvider.class,
                new LatestAndGreatestConnectionProviderImpl()
        );
    }
}
