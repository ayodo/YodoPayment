package co.yodo.mobile.component.cipher;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Base64;

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import co.yodo.mobile.helper.CryptUtils;

/**
 * Created by hei on 15/07/16.
 * RSA implementation
 */
public class RSACrypt {
	/** DEBUG */
	@SuppressWarnings( "unused" )
	private final static String TAG = RSACrypt.class.getSimpleName();

	/**
	 * Public key generated with: openssl rsa -in 11.private.pem -pubout -outform DER -out 11.public.der
	 * This key is created using the private key generated using openssl in unix environments
	*/
    private static final String PUBLIC_KEY = CryptUtils.getPublicKey();
	private final PublicKey mPubKey;
	
	/** Public key instance */
	private static final String KEY_INSTANCE = "RSA";

	/** Cipher instance used for encryption */
	private static final String CIPHER_INSTANCE = "RSA/None/PKCS1Padding";

	/**
	 * Private constructor for the singleton
	 * @param context The Android context for the application
     */
	public RSACrypt( Context context ) {
		mPubKey = readPublicKey( context );
	}

	/**
	 * Function that opens the public key and returns the java object that contains it
	 * @param context    Context of the application
	 * @return PublicKey The public key specified in PUBLIC_KEY
	 */
	private static PublicKey readPublicKey( Context context ) {
		PublicKey pkPublic = null;

		try {
			AssetManager as = context.getAssets();
			InputStream inFile = as.open( PUBLIC_KEY );
			DataInputStream dis = new DataInputStream( inFile );

			byte[] encodedKey = new byte[inFile.available()];
			dis.readFully( encodedKey );
			dis.close();

			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec( encodedKey );
			KeyFactory kf = KeyFactory.getInstance( KEY_INSTANCE );
			pkPublic = kf.generatePublic( publicKeySpec );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return pkPublic;
	}

	/**
	 * Encrypts a string and transforms the byte array containing
	 * the encrypted string to Hex format
	 * @param plainText The unencrypted string
	 * @return String   The encrypted string in Hex
	 */
	public String encrypt( String plainText ) {
		String encryptedData = null;

		try {
			final Cipher cipher = Cipher.getInstance( CIPHER_INSTANCE );
			cipher.init( Cipher.ENCRYPT_MODE, mPubKey );
			final byte[] cipherData = cipher.doFinal( plainText.getBytes( "UTF-8" ) );
			encryptedData = CryptUtils.bytesToHex( cipherData );
		} catch( Exception e ) {
			e.printStackTrace();
		}

		return encryptedData;
	}
}