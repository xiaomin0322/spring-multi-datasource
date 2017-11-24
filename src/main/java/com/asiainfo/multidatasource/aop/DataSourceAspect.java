package com.asiainfo.multidatasource.aop;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.asiainfo.multidatasource.datasource.DataSource;
import com.asiainfo.multidatasource.datasource.DataSourceHolder;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年5月7日  下午3:24:27
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
/*@Component
@Aspect
@Order(1)*/
public class DataSourceAspect {

	private static final Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);
	
	/**
	 * key是方法名称前缀，value是对应的数据源名称
	 */
	private Map<String,String> maps;
	
	public Map<String, String> getMaps() {
		return maps;
	}

	public void setMaps(Map<String, String> maps) {
		this.maps = maps;
	}

	// @Pointcut("execution(* com.asiainfo.multidatasource..service..*.*(..))")
    public void aspect() {}
    
	//@Before("aspect()")
	public Object execute(ProceedingJoinPoint point) {
		
		logger.info("before {}", point);
		
        MethodSignature proxySignature = (MethodSignature) point.getSignature();
        Method proxyMethod = proxySignature.getMethod();
        Class<?> target = point.getTarget().getClass();
        Method targetMethod = null;
        try {
            targetMethod = target.getMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        boolean isConfiger=false;
        String methodName = targetMethod.getName();
        if(MapUtils.isNotEmpty(maps)) {
        	  for(String s:maps.keySet()) {
        		 if(methodName.startsWith(s)) {
        			 isConfiger = true;
        			  String datasorrce = maps.get(s);
        			  logger.debug("methodName using specify configer datasource({}) ......",methodName,datasorrce);
        			  DataSourceHolder.setDataSource(datasorrce);
        			  break;
        		 }
              }
        }
      

        if (targetMethod != null && !isConfiger) {
            Transactional transactional = target.getAnnotation(Transactional.class);
            if (transactional != null) {
            	DataSource clazzDatasource = target.getAnnotation(DataSource.class);
            	DataSource methodDatasource = targetMethod.getAnnotation(DataSource.class);
                if (methodDatasource != null) {
                	logger.debug("methodName using specify method datasource({}) ......", methodName,methodDatasource.value());
                    DataSourceHolder.setDataSource(methodDatasource.value());
                } else if (clazzDatasource != null) {
                	logger.debug("methodName using specify clazz datasource({}) ......", methodName,clazzDatasource.value());
                    DataSourceHolder.setDataSource(clazzDatasource.value());
                } else {
                    logger.warn("does not specify any datasource, using default datasource ......");
                }
            } else {
            	logger.debug("non transactional method, using default datasource({}) ......");
            }
        }
        
        try {
			return point.proceed();
		} catch (Throwable e) {
			e.printStackTrace();
		}finally {
			DataSourceHolder.clear();
		}
        return null;
	}

	//@AfterReturning("aspect()")
	public void afterReturn(JoinPoint joinPoint) {
		logger.info("afterReturn {}", joinPoint);
	
	}
}
