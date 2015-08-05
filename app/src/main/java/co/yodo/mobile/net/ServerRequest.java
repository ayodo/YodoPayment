package co.yodo.mobile.net;

import co.yodo.mobile.helper.AppUtils;

public class ServerRequest {
	/** DEBUG */
	private final static String TAG = ServerRequest.class.getSimpleName();
	
	/** Protocol version used in the request */
	private static final String PROTOCOL_VERSION = "1.1.2";
	
	/** Parameters used for creating an authenticate request */
	private static final String AUTH_REQ                 = "0";
    public static final String AUTH_HW_SUBREQ            = "1";
    public static final String AUTH_HW_PIP_SUBREQ        = "2";
    public static final String AUTH_HW_PIP_EIFACE_SUBREQ = "3";

    /** Parameters used for creating a reset request */
    private static final String RESET_REQ           = "3";
    public static final String RESET_PIP_SUBREQ     = "1";
    public static final String RESET_BIO_PIP_SUBREQ = "2";

    /** Parameters used for creating a balance request */
    private static final String QUERY_REQ       = "4";
    public static final String QUERY_BAL_SUBREQ = "1";
    public static final String QUERY_ACC_SUBREQ = "3";

    /** Parameters used for creating a close request */
    private static final String CLOSE_REQ           = "8";
    private static final String CLOSE_CLIENT_SUBREQ = "1";

    /** Parameters used for creating a registration request */
    private static final String REG_REQ             = "9";
    public static final String REG_CLIENT_SUBREQ    = "0";
    public static final String REG_BIOMETRIC_SUBREQ = "3";

    /** Parameters used for the linking requests */
    private static final String LINK_REQ       = "10";
    public static final String LINK_ACC_SUBREQ = "0";

    /** Parameters used for the de-link requests */
    private static final String DELINK_REQ        = "11";
    public static final String DELINK_TO_SUBREQ   = "0";
    public static final String DELINK_FROM_SUBREQ = "1";

    /** Query Records */
    public static final int QUERY_BIOMETRIC_PIP   = 20;
    public static final int QUERY_RECEIPT         = 21;
    public static final int QUERY_ADVERTISING     = 22;
    public static final int QUERY_BIOMETRIC       = 24;
    public static final int QUERY_LINKING_CODE    = 25;
    public static final int QUERY_LINKED_ACCOUNTS = 26;
	
	/** Variable that holds request string separator */
	private static final String	REQ_SEP = ",";
	
	/**
	 * Creates an authentication switch request  
	 * @param sUsrData	Encrypted user's data
	 * @param iAuthReqType Sub-type of the request
	 * @return String Request for getting the authentication
	 */
	public static String createAuthenticationRequest(String sUsrData, int iAuthReqType) {
		StringBuilder sAuthenticationRequest = new StringBuilder();
		sAuthenticationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
		sAuthenticationRequest.append( AUTH_REQ ).append( REQ_SEP );
		
		switch( iAuthReqType ) {
            //RT = 0, ST = 1
            case 1: sAuthenticationRequest.append( AUTH_HW_SUBREQ ).append( REQ_SEP );
                break;

            //RT = 0, ST = 2
            case 2: sAuthenticationRequest.append( AUTH_HW_PIP_SUBREQ ).append( REQ_SEP );
                break;

            //RT = 0, ST = 3
            case 3: sAuthenticationRequest.append( AUTH_HW_PIP_EIFACE_SUBREQ ).append( REQ_SEP );
                break;
		}
		sAuthenticationRequest.append( sUsrData );
		
		AppUtils.Logger( TAG, "Authentication Request: " + sAuthenticationRequest.toString() );
		return sAuthenticationRequest.toString();
	}

    /**
     * Creates reset switch request to change a user profile
     * @param sUserData	Encrypted user's data
     * @param iResReqType Sub-type of the request
     * @return String 	Request to register a user
     */
    public static String createResetRequest(String sUserData, int iResReqType) {
        StringBuilder sResetRequest = new StringBuilder();
        sResetRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sResetRequest.append( RESET_REQ ).append( REQ_SEP );

        switch( iResReqType ) {
            //RT = 3, ST = 1
            case 1: sResetRequest.append( RESET_PIP_SUBREQ ).append( REQ_SEP );
                break;

            //RT = 3, ST = 2
            case 2: sResetRequest.append( RESET_BIO_PIP_SUBREQ ).append( REQ_SEP );
                break;
        }
        sResetRequest.append( sUserData );

        AppUtils.Logger( TAG, "Reset Request: " + sResetRequest.toString() );
        return sResetRequest.toString();
    }

    /**
     * Creates a query request
     * @param sUsrData	Encrypted user's data
     * @param iQueryReqType Sub-type of the request
     * @return String Request for getting the balance
     */
    public static String createQueryRequest(String sUsrData, int iQueryReqType) {
        StringBuilder sQueryRequest = new StringBuilder();
        sQueryRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sQueryRequest.append( QUERY_REQ ).append( REQ_SEP );

        switch( iQueryReqType ) {
            //RT = 4, ST = 1
            case 1: sQueryRequest.append( QUERY_BAL_SUBREQ ).append( REQ_SEP );
                break;

            //RT = 4, ST = 3
            case 3: sQueryRequest.append( QUERY_ACC_SUBREQ ).append( REQ_SEP );
                break;
        }
        sQueryRequest.append( sUsrData );

        AppUtils.Logger( TAG, "Third Party Balance Request: " + sQueryRequest.toString() );
        return sQueryRequest.toString();
    }

    /**
     * Creates a close switch request to close the account
     * @param sUserData	Encrypted user's data
     * @return String Request to close a user account
     */
    public static String createCloseRequest(String sUserData) {
        StringBuilder sCloseRequest = new StringBuilder();
        sCloseRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sCloseRequest.append( CLOSE_REQ ).append( REQ_SEP );

        /// RT = 8, ST = 1
        sCloseRequest.append( CLOSE_CLIENT_SUBREQ ).append( REQ_SEP );
        sCloseRequest.append( sUserData );

        AppUtils.Logger( TAG, "Close Request: " + sCloseRequest.toString() );
        return sCloseRequest.toString();
    }

    /**
     * Creates an registration switch request
     * @param sUsrData	Encrypted user's data
     * @param iRegReqType Sub-type of the request
     * @return String Request for getting the registration code
     */
    public static String createRegistrationRequest(String sUsrData, int iRegReqType) {
        StringBuilder sRegistrationRequest = new StringBuilder();
        sRegistrationRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sRegistrationRequest.append( REG_REQ ).append( REQ_SEP );

        switch( iRegReqType ) {
            // RT = 9, ST = 0
            case 0: sRegistrationRequest.append( REG_CLIENT_SUBREQ ).append( REQ_SEP );
                break;

            // RT = 9, ST = 3
            case 3: sRegistrationRequest.append( REG_BIOMETRIC_SUBREQ ).append( REQ_SEP );
                break;
        }
        sRegistrationRequest.append( sUsrData );

        AppUtils.Logger( TAG, "Registration Request: " + sRegistrationRequest.toString() );
        return sRegistrationRequest.toString();
    }

    /**
     * Creates a link switch request to link accounts
     * @param sUserData	Encrypted user's data
     * @param iLinkReqType Sub-type of the request
     * @return String Request to link a user account
     */
    public static String createLinkingRequest(String sUserData, int iLinkReqType) {
        StringBuilder sLinkingRequest = new StringBuilder();
        sLinkingRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sLinkingRequest.append( LINK_REQ ).append( REQ_SEP );

        switch( iLinkReqType ){
            // RT = 10, ST = 0
            case 0: sLinkingRequest.append( LINK_ACC_SUBREQ ).append(REQ_SEP);
                break;
        }
        sLinkingRequest.append( sUserData );

        AppUtils.Logger( TAG, "Linking Request: " + sLinkingRequest.toString() );
        return sLinkingRequest.toString();
    }

    /**
     * Creates a de-link switch request to de-link accounts
     * @param sUserData	Encrypted user's data
     * @param iDeLinkReqType Sub-type of the request
     * @return String Request to de-link a user account
     */
    public static String createDeLinkRequest(String sUserData, int iDeLinkReqType) {
        StringBuilder sDeLinkRequest = new StringBuilder();
        sDeLinkRequest.append( PROTOCOL_VERSION ).append( REQ_SEP );
        sDeLinkRequest.append( DELINK_REQ ).append( REQ_SEP );

        switch( iDeLinkReqType ) {
            // RT = 11, ST = 0
            case 0: sDeLinkRequest.append( DELINK_TO_SUBREQ ).append( REQ_SEP );
                break;

            // RT = 11, ST = 1
            case 1: sDeLinkRequest.append( DELINK_FROM_SUBREQ ).append( REQ_SEP );
                break;
        }
        sDeLinkRequest.append( sUserData );

        AppUtils.Logger( TAG, "DeLink Request: " + sDeLinkRequest.toString() );
        return sDeLinkRequest.toString();
    }
}
