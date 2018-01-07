package co.yodo.mobile.rule;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import co.yodo.mobile.helper.PreferencesHelper;
import co.yodo.mobile.ui.registration.RegistrationActivity;

/**
 * Created by hei on 13/06/16.
 * Test rule for the registration activity
 */
public class RegistrationTestRule<A extends RegistrationActivity> extends ActivityTestRule<A> {
    /** Test context */
    private static Context mCtx;

    /** Mock values */
    private static final String fHardwareToken = "fake_hardware_token";

    public RegistrationTestRule( Class<A> activityClass ) {
        super( activityClass );
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        mCtx = InstrumentationRegistry.getTargetContext();
        PreferencesHelper.clearPrefConfig(mCtx);

        // Set preferences
        PreferencesHelper.saveEulaAccepted( true );
        PreferencesHelper.saveHardwareToken( fHardwareToken );
    }

    @Override
    protected void afterActivityFinished() {
        super.afterActivityFinished();
        PreferencesHelper.clearPrefConfig(mCtx);
    }
}
