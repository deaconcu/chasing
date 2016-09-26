import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableScheduling
@PropertySource("classpath:chasing-tool.properties")
@ComponentScan(basePackages = {
        "com.prosper.chasing.common.bean.client",
        "com.prosper.chasing.tool.controll",
        "com.prosper.chasing.tool.service",
        "com.prosper.chasing.tool.util",
        "com.prosper.chasing.tool.validation",
        "com.prosper.chasing.tool.aspect"})
public class TestBean {
    
    @Bean(name="propertySources")
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
}
