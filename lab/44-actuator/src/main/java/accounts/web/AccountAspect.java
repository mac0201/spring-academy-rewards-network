package accounts.web;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/*
 * -26 (Optional): Use AOP for counting logic
 * - Add `spring-boot-starter-aop` starter to the `pom.xml` or the
 *   `build.gradle`. (You might want to refresh your IDE so that
 *   it picks up the change in the `pom.xml` or the `build.gradle` file.)
 * - Make this class an Aspect, through which `account.fetch` counter,
 *   which has a tag of `type`/`fromAspect` key/value pair, gets incremented
 *   every time `accountSummary` method of the `AccountController` class
 *   is invoked
 * - Make this a component by using a proper annotation
 * - Access `/accounts` several times and verify the metrics of
 *   `/actuator/metrics/account.fetch?tag=type:fromAspect
 */
@Aspect
@Component
public class AccountAspect {

    private final Counter counter;

    public AccountAspect(MeterRegistry registry) {
        this.counter = registry.counter("account.fetch", "type", "fromAspect");
    }

    @Before("execution(* *..AccountController.accountSummary(..))")
    public void trackAccountSummaryInvoke() {
        counter.increment();
        System.err.println("accountSummary() invoked.");
    }

}
