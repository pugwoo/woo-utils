package com.pugwoo.wooutils.redis;
import java.util.ArrayList;
import java.util.List;

/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.PatternMatchUtils;

import com.pugwoo.wooutils.redis.impl.SyncMethodInterceptor;

/**
 */
public class RedisSyncProxyCreator extends AbstractAutoProxyCreator implements InitializingBean, ApplicationContextAware {

    private static final long  serialVersionUID = -9094985530794052264L;

    private List<Class<?>>     beanTypes;

    private ApplicationContext context;

    private List<String>       beanNames        = new ArrayList<String>();
    
    private RedisHelper redisHelper;

    public void setRedisHelper(RedisHelper redisHelper) {
		this.redisHelper = redisHelper;
	}

	public void setBeanTypes(List<Class<?>> beanTypes) {
        this.beanTypes = beanTypes;
    }

    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Identify as bean to proxy if the bean name is in the configured list of names.
     */
    @SuppressWarnings("rawtypes")
    protected Object[] getAdvicesAndAdvisorsForBean(Class beanClass, String beanName, TargetSource targetSource) {
        for (String mappedName : this.beanNames) {
            if (FactoryBean.class.isAssignableFrom(beanClass)) {
                if (!mappedName.startsWith(BeanFactory.FACTORY_BEAN_PREFIX)) {
                    continue;
                }
                mappedName = mappedName.substring(BeanFactory.FACTORY_BEAN_PREFIX.length());
            }
            if (isMatch(beanName, mappedName)) {
                return PROXY_WITHOUT_ADDITIONAL_INTERCEPTORS;
            }
        }
        return DO_NOT_PROXY;
    }

    /**
     * Return if the given bean name matches the mapped name.
     * <p>
     * The default implementation checks for "xxx*", "*xxx" and "*xxx*" matches, as well as direct equality. Can be
     * overridden in subclasses.
     * 
     * @param beanName the bean name to check
     * @param mappedName the name in the configured list of names
     * @return if the names match
     * @see org.springframework.util.PatternMatchUtils#simpleMatch(String, String)
     */
    protected boolean isMatch(String beanName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, beanName);
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(beanTypes, "beanTypes cannot be null");
        for (Class<?> targetBeanType : beanTypes) {
            String[] beanNames = context.getBeanNamesForType(targetBeanType);
            for (String name : beanNames) {
                this.beanNames.add(name);
            }
        }
        
        SyncMethodInterceptor interceptor = new SyncMethodInterceptor(redisHelper);
        ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
        beanFactory.registerSingleton("syncMethodInterceptor", interceptor);
        
        super.setInterceptorNames("syncMethodInterceptor");
    }

}