package org.example.cp.oms.domain.service;

import org.example.cp.oms.domain.ability.SerializableIsolationAbility;
import org.example.cp.oms.domain.step.SubmitOrderStepsExec;
import org.ddd.cp.ddd.annotation.DomainService;
import org.ddd.cp.ddd.model.IDomainService;
import org.ddd.cp.ddd.runtime.DDD;
import lombok.extern.slf4j.Slf4j;
import org.example.cp.oms.domain.CoreDomain;
import org.example.cp.oms.domain.ability.DecideStepsAbility;
import org.example.cp.oms.domain.exception.OrderException;
import org.example.cp.oms.domain.model.OrderModel;
import org.example.cp.oms.spec.Steps;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.locks.Lock;

@DomainService(domain = CoreDomain.CODE)
@Slf4j
public class SubmitOrder implements IDomainService {

    @Resource
    private SubmitOrderStepsExec submitOrderStepsExec;

    public void submit(@NotNull OrderModel orderModel) throws OrderException {
        Lock lock = DDD.findAbility(SerializableIsolationAbility.class).acquireLock(orderModel);
        if (SerializableIsolationAbility.useLock(lock) && !lock.tryLock()) {
            // 存在并发
            throw new OrderException();
        }

        List<String> steps = DDD.findAbility(DecideStepsAbility.class).decideSteps(orderModel, Steps.SubmitOrder.Activity);
        submitOrderStepsExec.execute(Steps.SubmitOrder.Activity, steps, orderModel);
    }
}
