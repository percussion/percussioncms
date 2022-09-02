package integrationtest.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringSetup {
    static ApplicationContext context;
    
    public synchronized static void loadXmlBeanFiles(String... files) {
        context = new FileSystemXmlApplicationContext(files);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name, Class<T> beanType) {
        if (context == null) throw new IllegalStateException("Context is not loaded. Use loadXmlBeanFiles first.");
        return (T) context.getBean(name);
    }
    
    public static void destroyContext() {
        context = null;
    }

}
