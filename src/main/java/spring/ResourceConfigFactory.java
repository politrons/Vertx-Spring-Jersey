package spring;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *  Created by Pablo Perez Garcia on 28/02/2017.
 * Factory to build a {@link ResourceConfig} that component-scan base-packages indicated in resourcePackage attribute
 * 
 */
@Component
public interface ResourceConfigFactory {
	
	ResourceConfig build(String resourcePackage, ApplicationContext context);
	
}
