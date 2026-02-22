package dev.ilkersahin.java.spring.gym.security.selfservice;

import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationMethodMatcher;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component
public class SelfServiceAnnotationBeanPostProcessor implements BeanPostProcessor {
    private final Advisor advisor;
    private final List<Object> namesOfTargetBeans = new ArrayList<>();

    public SelfServiceAnnotationBeanPostProcessor() {
        var pointcut = new StaticMethodMatcherPointcut() {
            private final AnnotationMethodMatcher matcher = new AnnotationMethodMatcher(SelfService.class);
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return matcher.matches(method, targetClass);
            }
        };
        this.advisor = new DefaultPointcutAdvisor(pointcut, new SelfServiceAuthenticationInterceptor());
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        /*
            In this method, we check if the bean has any method annotated with @SelfService. If it does, we add the bean name to a list of target beans.
            We will use this list in the postProcessAfterInitialization method to create a proxy for the bean and apply the advisor to it.
            We do not create the proxy in this method because we need to wait until the bean is fully initialized before we can create a proxy for it.
         */
        for (Method declaredMethod : bean.getClass().getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(SelfService.class)) {
                namesOfTargetBeans.add(beanName);
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        /*
            In this method, we check if the bean name is in the list of target beans. If it is, we create a proxy for the bean and apply the advisor to it.
            We use CGLIB class-based proxying to create the proxy, which allows us to proxy classes that do not implement any interfaces.
            We do not check the methods annotations here because here bean is already proxied

            (P.s actually we could check it here via AopUtils.getTargetClass(bean) instead of checking it in the postProcessBeforeInitialization method,
            but I've decided to do it like this for demo purposes and to explain the diff between these two methods).
         */
        if (namesOfTargetBeans.contains(beanName)) {
            var pf = new ProxyFactory(bean);
            // true = CGLIB class-based proxy; false = JDK proxy (interfaces only)
            pf.setProxyTargetClass(true);
            pf.addAdvisor(advisor);
            return pf.getProxy();
        }
        return bean;
    }
}
