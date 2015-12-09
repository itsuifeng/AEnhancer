package com.baidu.acache.entry;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.baidu.acache.driver.CacheDriver;
import com.baidu.acache.driver.CacheDriverFactory;
import com.baidu.acache.exception.CacheAopException;
import com.baidu.acache.model.MethodInfo;
import com.baidu.acache.processor.CacheDataProcessor;
import com.baidu.acache.processor.CacheFrontProcessor;

/**
 * 定义Cached修饰的切点（point cut）和它的连接点（join point）处理
 * 
 * @author xushuda
 *
 */
@Aspect
public class CacheAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheAspect.class);

    // 没有用spring托管
    private CacheFrontProcessor preProcessor = new CacheFrontProcessor();

    private CacheDataProcessor postProcessor = new CacheDataProcessor();

    @Autowired
    private CacheDriverFactory cacheDriverFactory;

    /**
     * around 入口,这个方法会捕获所有runtimeException（可能由cacheDriver或者框架抛出，比如网络异常） <br>
     * 还有post processor抛出的所有CacheAopException的子类都会被捕获。但是pre processor抛出的异常都是明显编码错误，直接抛出
     * 
     * @param jp
     * @param cached
     * @return 方法调用的返回值
     * @throws Throwable
     */
    @Around("@annotation(cached)")
    public Object around(ProceedingJoinPoint jp, Cached cached) throws Throwable {
        // 获取driver
        CacheDriver cacheDriver = cacheDriverFactory.getCacheDriver(cached.driver());
        // 预处理，这里抛出的异常是受检异常，应该直接抛出，所以不在try块中
        MethodInfo methodInfo = preProcessor.preProcess(jp, cached);
        // 处理数据
        try {
            logger.info("start to retrive data from: {} ", jp.getSignature().toLongString());
            Object ret = null;
            // 根据不同的请求类型
            if (!methodInfo.aggrInvok()) {
                ret = postProcessor.processNormal(methodInfo, cacheDriver);
            } else {
                ret = postProcessor.processAggregated(methodInfo, cacheDriver);
            }
            logger.info("data retrieved is: {}", ret);
            return ret;
        } catch (CacheAopException exp) {
            // be careful, this kind of exception may caused by your incorrect code
            logger.error("revive error occors in cache aop , caused by :", exp);
            if (methodInfo.getBatchSize() > 0) {
                return postProcessor.processAggregatedWithoutCache(methodInfo);
            }
            // return the original call
            return jp.proceed(jp.getArgs());
        } catch (RuntimeException rtExp) {
            // swallow the runtime exception
            logger.error("revive runtime exception occurs in cache aop , caused by :", rtExp);
            // be careful about this kind of exception, the cache server may just crash
            if (methodInfo.getBatchSize() > 0) {
                return postProcessor.processAggregatedWithoutCache(methodInfo);
            }
            return jp.proceed(jp.getArgs());
        }
    }
}
