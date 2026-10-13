package io.github.hossensyedriadh.cbs.configuration.quartz;

import org.jspecify.annotations.NonNull;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.quartz.autoconfigure.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class QuartzConfiguration {
    @Bean
    SchedulerFactoryBeanCustomizer quartzJobFactoryCustomizer(AutowireCapableBeanFactory beanFactory) {
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(new SpringBeanJobFactory() {
            @Override
            @NonNull
            protected Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
                Object job = super.createJobInstance(bundle);
                beanFactory.autowireBean(job);
                return job;
            }
        });
    }
}
