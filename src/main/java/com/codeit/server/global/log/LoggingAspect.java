package com.codeit.server.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // 서비스 계층의 모든 메서드를 포함하는 포인트 컷
    @Pointcut("execution(* com.codeit.server..service..*.*(..))")
    public void serviceLayerPointcut() {}

    // 컨트롤러 계층의 모든 메서드를 포함하는 포인트 컷
    @Pointcut("execution(* com.codeit.server..controller..*.*(..))")
    public void controllerLayerPointcut() {}

    // 메서드 시작 이전의 로그를 남기는 어드바이스
    @Before("serviceLayerPointcut() || controllerLayerPointcut()")
    public void logBefore(JoinPoint joinPoint){
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("==> {}.{}({})", className, methodName, Arrays.toString(args));
    }

    // 메서드가 정상적으로 호출된 이후에 남기는 어드바이스
    @AfterReturning(pointcut = "serviceLayerPointcut() || controllerLayerPointcut()",
            returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result){
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.debug("<== {}.{}({}), return {}", className, methodName, Arrays.toString(args), result);
    }

    // 예외 발생했을 때 처리하는 어드바이스(권장★★★)
    @AfterThrowing(pointcut = "serviceLayerPointcut() || controllerLayerPointcut()",
            throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Exception e){
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String exceptionName = e.getClass().getSimpleName();

        log.error("<== {}.{}({}), exceptionName={}, message={}",
                className, methodName, Arrays.toString(args), exceptionName, e.getMessage(), e);
        // error에 Throwable객체를 넣어주면 trace 시켜줌! ★★★★★
    }

    // 실행시간 측정하여 시간 오버된 경우 경고 남기기!!
    @Around("serviceLayerPointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        StopWatch watch = new StopWatch();
        watch.start();
        try {
            Object result = joinPoint.proceed(); // 대상 메소드 실행코드!
            watch.stop();
            long executionTime = watch.getTotalTimeMillis();

            log.info("{}.{} 실행시간 : {}ms", className, methodName, executionTime);

            // 성능 경고(1초)
            if (executionTime > 1000){
                log.warn("[warn!] {}.{} 실행시간이 {}ms로 느립니다. 성능 최적화가 필요합니다!"
                            ,className, methodName, executionTime);
            }
            return result;
        } catch (Throwable throwable) {
            watch.stop();
            long executionTime = watch.getTotalTimeMillis();
            log.error("{}.{} 실행 실패!! - 실행시간 : {}ms", className, methodName, executionTime);
            throw throwable;
        }
    }

    // 중요 비지니스 로직 이벤트 로깅
    @Pointcut("execution(* com.codeit.server.user.service.UserService.login(..))"
            + " || execution(* com.codeit.server.user.service.UserService.register(..))")
    public void businessEventsPointcut() {}

    // 중요한 비지니스 로직만 로그로 남기는 로직
    @Before("businessEventsPointcut()")
    public void logBusinessEventsBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("==> {}.{}({})", className, methodName, Arrays.toString(args));
    }
}
