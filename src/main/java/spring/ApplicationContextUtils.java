package spring;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import spring.impl.ResourceConfigFactoryImpl;

/**
 * Created by pabloperezgarcia on 04/03/2017.
 *
 */
public class ApplicationContextUtils {

    public static ResourceConfig getResourceConfig(String applicationContext, String packageRespurces) {
        return buildResourceConfig(packageRespurces, initializeApplicationContext(applicationContext));
    }

    private static ResourceConfig buildResourceConfig(String resourcePackage, ClassPathXmlApplicationContext context) {
        ResourceConfigFactory resourceConfigFactory = getRequestFilterFactory(context);
        return resourceConfigFactory.build(resourcePackage, context);
    }

    private static ResourceConfigFactory getRequestFilterFactory(ClassPathXmlApplicationContext context) {
        return context
                .getBean(ResourceConfigFactoryImpl.class);
    }

    private static ClassPathXmlApplicationContext initializeApplicationContext(String applicationContext) {
        return new ClassPathXmlApplicationContext(
                new String[]{applicationContext});
    }
}
