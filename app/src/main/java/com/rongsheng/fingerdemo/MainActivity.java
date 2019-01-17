package com.rongsheng.fingerdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * @author lei
 * @desc android指纹识别
 * @date 2019/1/17 0017 -- 上午 9:00.
 * 个人站:www.cllhui.cn
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String DEFAULT_KEY_NAME = "default_key";
    KeyStore keyStore;
    private Button mBtFinger;
    private TextView tips;
    private LoginPwdFragment fragment;
    private Cipher cipher;
    private FingerprintManager fingerprintManager;
    private CancellationSignal mCancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mBtFinger = (Button) findViewById(R.id.bt_finger);
        tips = (TextView) findViewById(R.id.tv_tips);

        mBtFinger.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_finger:
                startFingerprintRecognition();
                break;
        }
    }

    private void createFingerFragment() {
        if (fragment == null || fragment.getType() != 1 || !fragment.isResumed()) {
            createDialogFragment(1);
            openFinger();
        }
    }

    private void startFingerprintRecognition() {
        if (supportFingerprint()) {
            initKey();
            initCipher();
        } else {
            Toast.makeText(this, R.string.close_finger_input_pwd, Toast.LENGTH_SHORT).show();

        }
    }

    @TargetApi(23)
    private void initCipher() {
        try {
            SecretKey key = (SecretKey) keyStore.getKey(DEFAULT_KEY_NAME, null);
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                    + KeyProperties.BLOCK_MODE_CBC + "/"
                    + KeyProperties.ENCRYPTION_PADDING_PKCS7);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            createFingerFragment();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(23)
    private void initKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(DEFAULT_KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
            keyGenerator.init(builder.build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TargetApi(23)
    private void openFinger() {
        mCancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "没有指纹识别权限", Toast.LENGTH_SHORT).show();
            return;
        }
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                    Toast.makeText(MainActivity.this, errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                Toast.makeText(MainActivity.this, helpString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                // 验证成功，自动结束指纹识别
                if (fragment != null) {
                    fragment.dismiss();
                }
              tips.setText("指纹识别成功\n"+result);

            }

            @Override
            public void onAuthenticationFailed() {
                if (!fragment.setTextHint()) {
                    Toast.makeText(MainActivity.this, R.string.finger_not_match, Toast.LENGTH_SHORT).show();
                }
            }
        }, null);
    }

    public boolean supportFingerprint() {
        if (Build.VERSION.SDK_INT < 23) {
            Toast.makeText(this, "您的系统版本过低，不支持指纹功能", Toast.LENGTH_SHORT).show();
            return false;
        } else {

            KeyguardManager keyguardManager = getSystemService(KeyguardManager.class);
            fingerprintManager = getSystemService(FingerprintManager.class);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "没有指纹识别权限", Toast.LENGTH_SHORT).show();
            }
            if (!fingerprintManager.isHardwareDetected()) {
                Toast.makeText(this, "您的手机不支持指纹功能", Toast.LENGTH_SHORT).show();
                return false;
            } else if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "您还未设置锁屏，请先设置锁屏并添加一个指纹", Toast.LENGTH_SHORT).show();
                return false;
            } else if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(this, "您至少需要在系统设置中添加一个指纹", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * 弹出输入密码框按确定
     */
    public void clickOk2Intent(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 点击取消识别的操作逻辑
     */
    public void cancelAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    public void intoPwdFragment() {
        synchronized (MainActivity.class) {
            if (fragment == null || fragment.getType() != 0 || !fragment.isResumed()) {
                createDialogFragment(0);
            }
        }
    }

    private void createDialogFragment(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(LoginPwdFragment.TYPE, type);
        fragment = new LoginPwdFragment();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "Pwd");
    }

}
