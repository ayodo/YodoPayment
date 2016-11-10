package co.yodo.mobile.helper;

import co.yodo.mobile.BuildConfig;
import co.yodo.mobile.network.ApiClient;

/**
 * Created by luis on 15/12/14.
 * Keys and defaults
 */
public class AppConfig {
    /** DEBUG flag */
    public static final boolean DEBUG = true;

    /** ID of the shared preferences file */
    public static final String SHARED_PREF_FILE = "YodoPaymentSharedPref";

    /**
     * Keys used with the Shared Preferences (SP) and default values.
     * {{ ======================================================================
     */

    /* Hardware token for the account
     * type -- String
     */
    static final String SPREF_HARDWARE_TOKEN = "SPHardwareToken";

    /* EULA Accepted.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- The user accepted the EULA
	 * false -- The user didn't accept the EULA
	 */
    static final String SPREF_EULA_ACCEPTED = "SPEulaAccepted" + BuildConfig.VERSION_NAME;

    /* First Login status.
	 * type -- Boolean
	 *
	 * __Values__
	 * true  -- First time that the user is logged in
	 * false -- It was already logged in several times
	 */
    static final String SPREF_FIRST_LOGIN = "SPFirstLogin";

    /* The current language.
	 * type -- String
	 */
    public static final String SPREF_CURRENT_LANGUAGE = "SPCurrentLanguage";

    /* The current state of the PIP visibility.
     * type -- Boolean
     */
    static final String SPREF_PIP_VISIBILITY = "SPPIPVisibility";

    /* The current user balance.
     * type -- String
     */
    static final String SPREF_CURRENT_BALANCE = "SPCurrentBalance";

    /* Registration authnumber
	 * type -- String
	 */
    static final String SPREF_AUTH_NUMBER = "SPAuthNumber";

    /* If the token was successfully sent to the server
	 * type -- boolean
	 */
    static final String SPREF_TOKEN_TO_SERVER  = "SPTokenToServer" + ApiClient.getSwitch();

    /* Action to be taken
     * type -- String
     */
    public static final String SPREF_SUBSCRIPTION_TASK = "subscription_task";

    /* If the main activity is in foreground
	 * type -- boolean
	 */
    static final String SPREF_FOREGROUND = "SPForeground";

    /**
     * Default values
     * {{ ======================================================================
     */

    /* Biometric Default */
    public static final String YODO_BIOMETRIC = "BiometricTest";

    /* Coupons folder */
    public static final String COUPONS_FOLDER = "Yodo";

    /* Minimum length for the PIP */
    public static final int MIN_PIP_LENGTH = 4;

    /* Progress Dialog */
    public static final String IS_SHOWING = "is_showing";

    /* Default values for user balance */
    static final String NO_BALANCE = "*.**";
    public static final String DEFAULT_BALANCE = "0.00";
}
