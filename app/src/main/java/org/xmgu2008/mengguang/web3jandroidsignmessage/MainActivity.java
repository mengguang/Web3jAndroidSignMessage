package org.xmgu2008.mengguang.web3jandroidsignmessage;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.spongycastle.asn1.x9.X9ECParameters;
import org.spongycastle.crypto.ec.CustomNamedCurves;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPublicKeyParameters;
import org.spongycastle.crypto.signers.ECDSASigner;
import org.spongycastle.math.ec.ECPoint;
import org.spongycastle.math.ec.custom.sec.SecP256R1Curve;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigInteger;


@EActivity(R.layout.activity_main)
public class MainActivity extends Activity {

    public static final String Password = "123qwe";
    @ViewById
    TextView logMessage;

    @Click
    void startButton() {
        startWeb3jTest();
    }

    @Background
    void startWeb3jTest() {

        String fileName;
        File cwd = getApplicationContext().getDataDir();
        writeLogMessage(cwd.getAbsolutePath());

        FilenameFilter filter = (dir, name) -> {
            String lowercaseName = name.toLowerCase();
            if (lowercaseName.endsWith(".json")) {
                return true;
            } else {
                return false;
            }
        };
        if (cwd.listFiles(filter).length == 0) {
            try {
                fileName = WalletUtils.generateNewWalletFile(
                        Password,
                        cwd, false);
                System.out.println(fileName);
                writeLogMessage(fileName);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        File keystore = cwd.listFiles(filter)[0];
        writeLogMessage(keystore.getAbsolutePath());

        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(
                    Password,
                    keystore);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String fromAddress = credentials.getAddress();
        writeLogMessage(fromAddress);

        BigInteger publicKey = credentials.getEcKeyPair().getPublicKey();
        String hexPublicKey = "04" + Numeric.toHexStringNoPrefix(publicKey);
        System.out.println(hexPublicKey);

        String data = "Hello World";
        byte[] hash = Hash.sha256(data.getBytes());
        String hexHash = Numeric.toHexStringNoPrefix(hash);
        System.out.println(hexHash);

        ECDSASignature signature = credentials.getEcKeyPair().sign(hash);
        String r = Numeric.toHexStringNoPrefixZeroPadded(signature.r, 64);
        String s = Numeric.toHexStringNoPrefixZeroPadded(signature.s, 64);
        String hexSignature = r + s;
        System.out.println(hexSignature);

        boolean result = verifyData(hexPublicKey, hash, signature);
        System.out.println("verify result: " + result);
    }

    private boolean verifyData(String hexPublicKey, byte[] message, ECDSASignature signature) {
        ECDSASigner signer = new ECDSASigner();

        X9ECParameters CURVE_PARAMS = CustomNamedCurves.getByName("secp256r1");
        ECDomainParameters CURVE = new ECDomainParameters(
                CURVE_PARAMS.getCurve(), CURVE_PARAMS.getG(), CURVE_PARAMS.getN(), CURVE_PARAMS.getH());

        SecP256R1Curve s = new SecP256R1Curve();
        ECPoint p = s.decodePoint(Numeric.hexStringToByteArray(hexPublicKey));

        ECPublicKeyParameters publicKeyParm = new ECPublicKeyParameters(p, CURVE);

        signer.init(false, publicKeyParm);

        return signer.verifySignature(message, signature.r, signature.s);
    }

    @UiThread
    void writeLogMessage(String message) {
        logMessage.append(message + "\n");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}

