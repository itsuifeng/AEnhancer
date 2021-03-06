package com.baidu.aenhancer.core.processor.ext.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.baidu.aenhancer.core.context.ProcessContext;
import com.baidu.aenhancer.core.processor.Processor;
import com.baidu.aenhancer.core.processor.ext.FallbackProxy;
import com.baidu.aenhancer.exception.CodingError;

@Component("ReturnNull")
public class ReturnNull implements FallbackProxy{

    @Override
    public void init(ProceedingJoinPoint jp, ApplicationContext context) throws CodingError {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void beforeProcess(ProcessContext ctx, Processor currentProcess) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object fallback(Object[] param) {
        return null;
    }

}
