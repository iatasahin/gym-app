package dev.ilkersahin.java.spring.gym.security;

import dev.ilkersahin.java.spring.gym.exception.UnauthorizedAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

@Component
public class SelfServiceAuthenticationInterceptor implements MethodInterceptor {

    @Override
    public @Nullable Object invoke(MethodInvocation invocation) throws Throwable {
        var annotation = invocation.getMethod().getAnnotation(SelfService.class);

        //Step 1: If the method is not annotated with @SelfService, proceed without authentication check
        if (annotation == null) {
            return invocation.proceed();
        }

        //Step 2: If the method is annotated with @SelfService, but the user is not authenticated, throw an exception
        var authenticatedUsername = AuthContextHolder.getAuthenticatedUsername();
        if(authenticatedUsername.isEmpty()) {
            throw new UnauthorizedAccessException("User is not authenticated");
        }

        /*
        Step 3: If the method is annotated with @SelfService, and the user is authenticated,
        but the username in the request does not match the authenticated user's username, throw an exception
        */
        Optional<String> targetUsername = extractUsernameOfTargetUser(invocation.getMethod(), invocation.getArguments(), annotation);
        if(targetUsername.isEmpty() || !targetUsername.get().equals(authenticatedUsername.get())) {
            throw new UnauthorizedAccessException("User is not authorized to access this resource");
        }

        return invocation.proceed();
    }

    private static Optional<String> extractUsernameOfTargetUser(Method method, Object[] args, SelfService annotation) {
        Optional<String> requestedUsername = Optional.empty();
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (annotation.usernameParam().equals(parameter.getName())) {
                requestedUsername = Optional.ofNullable(args[i].toString());
                break;
            }
        }

        return requestedUsername;
    }
}
