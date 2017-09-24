package co.yodo.mobile.business.injection.module;

import android.content.Context;

import co.yodo.mobile.business.component.cipher.RSACrypt;
import co.yodo.mobile.business.injection.scope.ApplicationScope;
import co.yodo.mobile.business.network.encryption.HybridEncryption;
import co.yodo.mobile.business.network.encryption.IEncryption;
import dagger.Module;
import dagger.Provides;

@Module
public class CipherModule {
    @Provides
    @ApplicationScope
    RSACrypt providesRSACrypt(Context context) {
        return new RSACrypt(context);
    }

    @Provides
    @ApplicationScope
    IEncryption providesEncryptionMethod() {
        return new HybridEncryption();
    }
}
