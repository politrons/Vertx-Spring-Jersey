package spring.impl;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import spring.ResourceConfigFactory;

/**
 * Created by Pablo Perez Garcia on 28/02/2017.
 *
 * Implementation of {@link ResourceConfigFactory} <br>
 * Builds a {@link ResourceConfig} that component-scan base-packages indicated in resourcePackage attribute. <br>
 * 
 */
@Component
public class ResourceConfigFactoryImpl implements ResourceConfigFactory {

	private static final String CONTEXT_CONFIG = "contextConfig";

	@Override
	public ResourceConfig build(String resourcePackage, ApplicationContext context) {
		ResourceConfig res = new ResourceConfig();
		res.property( CONTEXT_CONFIG, context );
		res.property( ServerProperties.BV_FEATURE_DISABLE, true );
		res.property( ServerProperties.RESOURCE_VALIDATION_IGNORE_ERRORS, true );
		res.packages( resourcePackage );
		return res;
	}
	
}
