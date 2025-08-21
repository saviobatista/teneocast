package com.teneocast.media.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class S3DisabledCondition implements Condition {
    
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String s3Enabled = context.getEnvironment().getProperty("aws.s3.enabled");
        return s3Enabled != null && !Boolean.parseBoolean(s3Enabled);
    }
}
