package co.yodo.mobile.ui.extension;

import android.app.Activity;
import android.view.View;

import co.yodo.mobile.R;
import co.yodo.mobile.helper.GUIUtils;
import co.yodo.mobile.ui.MainActivity;
import co.yodo.mobile.ui.extension.contract.IOption;
import co.yodo.mobile.ui.notification.AlertDialogHelper;
import co.yodo.mobile.ui.validator.PIPValidator;

/**
 * Created by hei on 14/06/16.
 * Implements the DeLink Account Option of the MainActivity
 */
public class DeLinkAccountOption extends IOption {
    /**
     * Sets up the main elements of the options
     * @param activity The Activity to handle
     */
    public DeLinkAccountOption( Activity activity ) {
        super( activity );
    }

    @Override
    public void execute() {
        final String title = this.mActivity.getString( R.string.input_pip );
        final PIPValidator validator = new PIPValidator( this.etInput );

        View.OnClickListener positiveClick = new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                GUIUtils.hideSoftKeyboard( mActivity );

                if( validator.validate() ) {
                    mAlertDialog.dismiss();
                    ( (MainActivity ) mActivity).deLinkAccount( etInput );
                }
            }
        };

        AlertDialogHelper.showAlertDialog(
                this.mActivity,
                title,
                this.etInput,
                buildOnClick( positiveClick )
        );
    }
}
