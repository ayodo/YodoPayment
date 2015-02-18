package co.yodo.mobile.main;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import co.yodo.mobile.R;
import co.yodo.mobile.broadcastreceiver.BroadcastMessage;
import co.yodo.mobile.component.ClearEditText;
import co.yodo.mobile.component.ImageLoader;
import co.yodo.mobile.component.ToastMaster;
import co.yodo.mobile.component.YodoHandler;
import co.yodo.mobile.data.ServerResponse;
import co.yodo.mobile.database.CouponsDataSource;
import co.yodo.mobile.database.ReceiptsDataSource;
import co.yodo.mobile.helper.AlertDialogHelper;
import co.yodo.mobile.helper.AppConfig;
import co.yodo.mobile.helper.AppEula;
import co.yodo.mobile.helper.AppUtils;
import co.yodo.mobile.net.YodoRequest;
import co.yodo.mobile.service.AdvertisingService;
import co.yodo.mobile.service.RESTService;
import co.yodo.mobile.sks.SKSCreater;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class MainActivity extends ActionBarActivity implements YodoRequest.RESTListener, View.OnClickListener {
    /** The context object */
    private Context ac;

    /** GUI Controllers */
    private TextView mAccountNumber;
    private TextView mAccountDate;
    private TextView mAccountBalance;
    private RelativeLayout mAdvertisingLayout;
    private ImageViewTouch mAdvertisingImage;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    /** Tutorial */
    private int counter = 0;
    private ShowcaseView showcaseView;

    /** Database and current merchant */
    private CouponsDataSource couponsdb;
    private ReceiptsDataSource receiptsdb;
    private String merchant;

    /** Hardware Identifier */
    private String hardwareToken;

    /** The Local Broadcast Manager */
    private LocalBroadcastManager lbm;

    /** SKS time to dismiss milliseconds */
    private static final int TIME_TO_DISMISS_SKS = 60000;

    /** SKS data separator */
    private static final String SKS_SEP   = "**";
    private static final String SKS_REGEX = "\\*\\*";

    /** SKS code */
    private String originalCode;

    /** Messages Handler */
    private static YodoHandler handlerMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppUtils.setLanguage( MainActivity.this );
        setContentView(R.layout.activity_main);

        setupGUI();
        updateData();
    }

    @Override
    public void onResume() {
        super.onResume();
        YodoRequest.getInstance().setListener( this );
        registerBroadcasts();
        // Open databases
        openDatabases();

        // Starts advertising service if enabled
        Intent iAdv = new Intent( ac, AdvertisingService.class );
        if( AppUtils.isMyServiceRunning( ac, AdvertisingService.class.getName() ) )
            stopService( iAdv );

        if( AppUtils.isAdvertising( ac ) )
            startService( iAdv );
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterBroadcasts();
        // Close databases
        closeDatabases();

        // Stops advertising service if running
        Intent iAdv = new Intent( ac, AdvertisingService.class );
        if( AppUtils.isMyServiceRunning( ac, AdvertisingService.class.getName() ) )
            stopService( iAdv );
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( mDrawerToggle.onOptionsItemSelected( item ) ) {
            return true;
        }

        int id = item.getItemId();
        switch( id ) {
            case R.id.action_settings:
                Intent intent = new Intent( MainActivity.this, SettingsActivity.class );
                startActivity( intent );
                return true;

            case R.id.action_about:
                final String title   = item.getTitle().toString();
                final String message = getString( R.string.version ) + " " +
                                       getString( R.string.actual_version ) + "/" +
                                       RESTService.getSwitch() + "\n\n" +
                                       getString( R.string.about_message );

                LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
                View layout = inflater.inflate( R.layout.dialog_about, new LinearLayout( this ), false );

                TextView emailView = (TextView) layout.findViewById( R.id.emailView );
                TextView messageView = (TextView) layout.findViewById( R.id.messageView );

                SpannableString email = new SpannableString( getString( R.string.about_email ) );
                email.setSpan( new UnderlineSpan(), 0, email.length(), 0 );

                emailView.setText( email );
                messageView.setText( message );

                emailView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent( Intent.ACTION_SEND );
                        String[] recipients = { getString( R.string.about_email ) };
                        intent.putExtra( Intent.EXTRA_EMAIL, recipients ) ;
                        intent.putExtra( Intent.EXTRA_SUBJECT, hardwareToken );
                        intent.setType( "text/html" );
                        startActivity( Intent.createChooser( intent, "Send mail" ) );
                    }
                });

                AlertDialogHelper.showAlertDialog(
                        ac,
                        title,
                        layout
                );
                return true;
        }
        return super.onOptionsItemSelected( item );
    }

    private void setupGUI() {
        // get the context
        ac = MainActivity.this;
        // Handler
        handlerMessages = new YodoHandler( MainActivity.this );
        // get local broadcast
        lbm = LocalBroadcastManager.getInstance( ac );
        // Globals GUI Components
        mAccountNumber     = (TextView) findViewById( R.id.accountNumberText );
        mAccountDate       = (TextView) findViewById( R.id.accountDateText );
        mAccountBalance    = (TextView) findViewById( R.id.accountBalanceText );
        mAdvertisingLayout = (RelativeLayout) findViewById( R.id.advertisingLayout );
        mAdvertisingImage  = (ImageViewTouch) findViewById( R.id.advertisingImage );
        mDrawerLayout      = (DrawerLayout) findViewById(R.id.drawerLayout);
        // Images fit parent
        mAdvertisingImage.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );
        // Only used at creation
        Toolbar actionBarToolbar = (Toolbar) findViewById( R.id.actionBar );

        setSupportActionBar(actionBarToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled( true );

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, actionBarToolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed( drawerView );
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened( drawerView );
            }
        };

        mDrawerLayout.setDrawerListener( mDrawerToggle );

        couponsdb  = new CouponsDataSource( ac );
        receiptsdb = new ReceiptsDataSource( ac );

        mAdvertisingImage.setOnLongClickListener( new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Drawable drawable = mAdvertisingImage.getDrawable();

                DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if( drawable != null ) {
                            Bitmap bitmap = AppUtils.drawableToBitmap( drawable );
                            File directory = new File( Environment.getExternalStorageDirectory(), AppConfig.COUPONS_FOLDER );
                            boolean success = true;

                            if( !directory.exists() )
                                success = directory.mkdir();

                            if( !success ) {
                                Toast.makeText( ac, R.string.image_saved_failed, Toast.LENGTH_SHORT ).show();
                                return;
                            }

                            int files = directory.listFiles().length;
                            File image = new File( directory, "ad" + (files + 1) + ".png" );

                            FileOutputStream outStream;
                            try {
                                outStream = new FileOutputStream( image );
                                bitmap.compress( Bitmap.CompressFormat.PNG, 90, outStream );

                                outStream.flush();
                                outStream.close();
                                couponsdb.createCoupon( image.getPath(), merchant );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };

                AlertDialogHelper.showAlertDialog(
                        ac,
                        R.string.save_image,
                        onClick
                );

                return true;
            }
        });

        if( AppUtils.isFirstLogin( ac ) ) {
            showcaseView = new ShowcaseView.Builder( this )
                    .setTarget( new ViewTarget( findViewById( R.id.actionBar ) ) )
                    .setContentTitle( R.string.tutorial_action_bar )
                    .setContentText( R.string.tutorial_action_bar_message )
                    .setStyle( R.style.CustomShowcaseTheme )
                    .setOnClickListener( this )
                    .build();
            showcaseView.setButtonText( getString( R.string.next ) );
        }

        AppEula.show( this );
    }

    private void updateData() {
        hardwareToken = AppUtils.getHardwareToken( ac );

        if( hardwareToken == null ) {
            ToastMaster.makeText( ac, R.string.no_hardware, Toast.LENGTH_LONG ).show();
            finish();
        }

        mAccountNumber.setText( hardwareToken );
        mAccountDate.setText( AppUtils.getCurrentDate() );
    }

    /**
     * Asks for the PIP to realize a payment
     * @param v, not used
     */
    public void paymentClick(View v) {
        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    mAdvertisingLayout.setVisibility( View.GONE );
                    originalCode = pip + SKS_SEP + hardwareToken + SKS_SEP;

                    YodoRequest.getInstance().createProgressDialog(
                            MainActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestPIPAuthentication(
                            MainActivity.this,
                            hardwareToken, pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                null,
                inputBox,
                onClick
        );
    }

    /**
     * List of saved coupons
     * @param v, not used
     */
    public void couponsClick(View v) {
        Intent intent = new Intent( MainActivity.this, CouponsActivity.class );
        startActivity(intent);
    }

    /**
     * Future feature
     * @param v, not used
     */
    public void networkClick(View v) {
        ToastMaster.makeText( MainActivity.this, R.string.no_available, Toast.LENGTH_SHORT ).show();
    }

    /**
     * Changes the current PIP
     * @param v, not used
     */
    public void resetPipClick(View v) {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent( MainActivity.this, PipResetActivity.class );
        startActivity( intent );
    }

    /**
     * Get the saved receipts
     * @param v, not used
     */
    public void savedReceiptsClick(View v) {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent( MainActivity.this, ReceiptsActivity.class );
        startActivity( intent );
    }

    /**
     * Link the account with another
     * @param v, not used
     */
    public void linkAccountsClick(View v) {
        mDrawerLayout.closeDrawers();

        Intent intent = new Intent( MainActivity.this, LinkingActivity.class );
        startActivity( intent );
    }

    /**
     * Get the user balance
     * @param v, not used
     */
    public void balanceClick(View v) {
        mDrawerLayout.closeDrawers();

        final String title      = getString( R.string.input_pip );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    YodoRequest.getInstance().createProgressDialog(
                            MainActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestBalance(
                            MainActivity.this,
                            hardwareToken, pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                null,
                inputBox,
                onClick
        );
    }

    /**
     * Closes the client account
     * @param v, not used
     */
    public void closeAccountClick(View v) {
        mDrawerLayout.closeDrawers();

        final String title      = getString( R.string.input_pip );
        final String message    = getString( R.string.close_message );
        final EditText inputBox = new ClearEditText( ac );

        DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String pip = inputBox.getText().toString();
                AppUtils.hideSoftKeyboard( MainActivity.this );

                if( pip.length() < AppConfig.MIN_PIP_LENGTH ) {
                    ToastMaster.makeText( MainActivity.this, R.string.pip_short, Toast.LENGTH_SHORT ).show();
                } else {
                    YodoRequest.getInstance().createProgressDialog(
                            MainActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestCloseAccount(
                            MainActivity.this,
                            hardwareToken, pip
                    );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                ac,
                title,
                message,
                inputBox,
                onClick
        );
    }

    /**
     * Register/Unregister the Broadcast Receivers.
     */
    private void registerBroadcasts() {
        IntentFilter filter = new IntentFilter();
        filter.addAction( BroadcastMessage.ACTION_NEW_MERCHANT );
        lbm.registerReceiver(mYodoBroadcastReceiver, filter);
    }

    private void unregisterBroadcasts() {
        lbm.unregisterReceiver(mYodoBroadcastReceiver);
    }

    /**
     * Open/Close the databases.
     */
    private void openDatabases() {
        if( couponsdb != null )
            couponsdb.open();

        if( receiptsdb != null )
            receiptsdb.open();
    }

    private void closeDatabases() {
        if( couponsdb != null )
            couponsdb.close();

        if( receiptsdb != null )
            receiptsdb.close();
    }

    /**
     * Method to show the dialog containing the SKS code
     */
    private void showSKSDialog(final String code, final Integer account_type) {
        try {
            final Bitmap qrCode = SKSCreater.createSKS( code, this, SKSCreater.SKS_CODE, account_type );
            final Dialog sksDialog = new Dialog( this );

            sksDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
            sksDialog.setContentView( R.layout.dialog_sks );
            sksDialog.setCancelable( false );

            // brightness
            final WindowManager.LayoutParams lp = getWindow().getAttributes();
            final float brightnessNow = lp.screenBrightness;

            sksDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    lp.screenBrightness = 100 / 100.0f;
                    getWindow().setAttributes( lp );
                }
            });

            sksDialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if( keyCode == KeyEvent.KEYCODE_BACK )
                        dialog.dismiss();
                    return true;
                }
            });

            sksDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    lp.screenBrightness = brightnessNow;
                    getWindow().setAttributes( lp );
                    mAdvertisingLayout.setVisibility( View.VISIBLE );

                    String[] parts = code.split( SKS_REGEX );

                    YodoRequest.getInstance().createProgressDialog(
                            MainActivity.this,
                            YodoRequest.ProgressDialogType.NORMAL
                    );

                    YodoRequest.getInstance().requestReceipt(
                            MainActivity.this,
                            hardwareToken, parts[0]
                    );
                }
            });

            ImageView image = (ImageView) sksDialog.findViewById( R.id.sks );
            image.setImageBitmap( qrCode );

            sksDialog.show();

            final Handler t = new Handler();
            t.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sksDialog.dismiss();
                }
            }, TIME_TO_DISMISS_SKS );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void receiptDialog(HashMap<String, String> params) {
        final Dialog receipt = new Dialog( MainActivity.this );
        receipt.requestWindowFeature( Window.FEATURE_NO_TITLE );

        LayoutInflater inflater = (LayoutInflater) getSystemService( LAYOUT_INFLATER_SERVICE );
        View layout = inflater.inflate( R.layout.dialog_receipt, new LinearLayout( this ), false );

        TextView descriptionText    = (TextView)  layout.findViewById( R.id.descriptionText );
        TextView authNumberText     = (TextView)  layout.findViewById( R.id.authNumberText );
        TextView createdText        = (TextView)  layout.findViewById( R.id.createdText );
        TextView totalAmountText    = (TextView)  layout.findViewById( R.id.paidText );
        TextView tenderAmountText   = (TextView)  layout.findViewById( R.id.cashTenderText );
        TextView cashBackAmountText = (TextView)  layout.findViewById( R.id.cashBackText );
        ImageView deleteButton      = (ImageView) layout.findViewById( R.id.deleteButton );
        ImageView saveButton        = (ImageView) layout.findViewById( R.id.saveButton );

        final String description    = params.get( ServerResponse.DESCRIPTION );
        final String authNumber     = params.get( ServerResponse.AUTHNUMBER );
        final String created        = params.get(ServerResponse.CREATED);
        final String totalAmount    = String.format( "%.2f", Double.parseDouble( params.get( ServerResponse.AMOUNT ) ) );
        final String tenderAmount   = String.format( "%.2f", Double.parseDouble( params.get( ServerResponse.TAMOUNT ) ) );
        final String cashBackAmount = String.format( "%.2f", Double.parseDouble( params.get( ServerResponse.CASHBACK ) ) );
        final String balance        = String.format( "%.2f", Double.parseDouble( params.get( ServerResponse.BALANCE ) ) );

        descriptionText.setText( description );
        authNumberText.setText( authNumber );
        createdText.setText( AppUtils.UTCtoCurrent( created ) );
        totalAmountText.setText( totalAmount );
        tenderAmountText.setText( tenderAmount );
        cashBackAmountText.setText( cashBackAmount );

        mAccountBalance.setText( balance );

        deleteButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receipt.dismiss();
            }
        });

        saveButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiptsdb.createReceipt(
                        description, authNumber, totalAmount,
                        tenderAmount, cashBackAmount, balance,
                        created
                );
                receipt.dismiss();
                ToastMaster.makeText( ac, R.string.saved_receipt, Toast.LENGTH_SHORT ).show();
            }
        });

        receipt.setCancelable( false );
        receipt.setContentView( layout );
        receipt.show();
    }

    @Override
    public void onResponse(YodoRequest.RequestType type, ServerResponse response) {
        if( type != YodoRequest.RequestType.QUERY_ADV_REQUEST )
            YodoRequest.getInstance().destroyProgressDialog();
        String code, message;

        switch( type ) {
            case ERROR_NO_INTERNET:
                mAdvertisingLayout.setVisibility( View.VISIBLE );
                handlerMessages.sendEmptyMessage( YodoHandler.NO_INTERNET );
                break;

            case ERROR_GENERAL:
                mAdvertisingLayout.setVisibility( View.VISIBLE );
                handlerMessages.sendEmptyMessage( YodoHandler.GENERAL_ERROR );
                break;

            case AUTH_PIP_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    originalCode += response.getRTime();
                    showSKSDialog( originalCode, null );
                    originalCode = null;
                } else {
                    mAdvertisingLayout.setVisibility( View.VISIBLE );
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }

                break;

            case QUERY_BAL_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED_BALANCE ) ) {
                    double balance = Double.parseDouble( response.getParam( ServerResponse.BALANCE ) );
                    mAccountBalance.setText( String.format( "%.2f", balance ) );
                } else {
                    mAccountBalance.setText( "" );
                    message = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;

            case QUERY_ADV_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    String url = response.getParam( ServerResponse.ADVERTISING ).replaceAll( " ", "%20" );
                    if( !url.isEmpty() )
                        ImageLoader.getInstance().DisplayImage( url, mAdvertisingImage );
                }
                break;

            case QUERY_RCV_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) )
                    receiptDialog( response.getParams() );

                break;

            case CLOSE_ACC_REQUEST:
                code = response.getCode();

                if( code.equals( ServerResponse.AUTHORIZED ) ) {
                    AppUtils.clearPrefConfig( ac );

                    couponsdb.delete();

                    DialogInterface.OnClickListener onClick = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    };

                    AlertDialogHelper.showAlertDialog(
                            ac,
                            getString( R.string.farewell_message_tittle ),
                            getString( R.string.farewell_message ),
                            onClick
                    );
                } else {
                    message  = response.getMessage();
                    AppUtils.sendMessage( handlerMessages, code, message );
                }
                break;
        }
    }

    /**
     * The broadcast receiver.
     */
    public BroadcastReceiver mYodoBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            String action = i.getAction();
			/* Broadcast: ACTION_NEW_MERCHANT */
			/* ****************************************** */
            if( action.equals( BroadcastMessage.ACTION_NEW_MERCHANT ) ) {
                merchant = i.getStringExtra( BroadcastMessage.EXTRA_NEW_MERCHANT );

                YodoRequest.getInstance().requestAdvertising(
                        MainActivity.this,
                        hardwareToken,
                        merchant
                );
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch( counter ) {
            case 0:
                showcaseView.setShowcase( new ViewTarget( findViewById( R.id.yodo_header ) ), true );
                showcaseView.setContentTitle( getString( R.string.tutorial_header ) );
                showcaseView.setContentText( getString( R.string.tutorial_header_message ) );
                showcaseView.setStyle( R.style.CustomShowcaseTheme );
                break;

            case 1:
                showcaseView.hide();
                AppUtils.saveFirstLogin( ac, false );
                break;
        }
        counter++;
    }
}