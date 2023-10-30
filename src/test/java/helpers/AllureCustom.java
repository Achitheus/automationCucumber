package helpers;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;

public class AllureCustom {
    public static void markOuterStepAsFailedAndStop() {
        AllureLifecycle lifecycle = Allure.getLifecycle();
        lifecycle.updateStep(step -> step.setStatus(Status.FAILED));
        lifecycle.stopStep();
    }
}