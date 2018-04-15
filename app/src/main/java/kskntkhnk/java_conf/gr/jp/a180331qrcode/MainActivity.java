package kskntkhnk.java_conf.gr.jp.a180331qrcode;

import android.app.LoaderManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.json.JSONObject;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {
    private  static final String TAG =
            MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                // EditTextにQRコードの内容をセット
                EditText et = (EditText)findViewById(R.id.et);
                et.setText(result.getContents());
                et.setSelection(et.getText().length());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_capture: // キャプチャ画面起動
                IntentIntegrator integrator = new IntentIntegrator(this);

                // Fragmentで呼び出す場合
                // IntentIntegrator integrator = IntentIntegrator.forFragment(this);

                // 独自でキャプチャ画面のActivity画面を作成
                // integrator.setCaptureActivity(ToolbarCaptureActivity.class);
                // →QrToolbarCaptureActivityのSample:
                // https:~~

                // スキャンするバーコード形式を指定
                // integrator.setDesireBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);

                // キャプチャ画面の下方にメッセージを表示
                integrator.setPrompt("Scan a barcode");

                // カメラの特定（この場合はフロントカメラを使用）
                // integrator.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);

                // 読み取り時の音声をオフに
                integrator.setBeepEnabled(false);

                // バーコードを画像保存できるっぽい（保存先はonActivityResultでIntentResult#getBarcoeImagePath()で取得）
                integrator.setBarcodeImageEnabled(true);

                // スキャン画面の回転の制御
                integrator.setOrientationLocked(true);

                // キャプチャ画面起動
                integrator.initiateScan();

                break;

            case R.id.create_qr: // QRコード生成
                Bitmap bitmap;
                try {
                    EditText et = (EditText) findViewById(R.id.et);
                    String text = et.getText().toString();
                    if (text.equals("")) {
                        return;
                    }
                    bitmap = createQRCodeByZxing(et.getText().toString(), 480);
                    ImageView iv = (ImageView) findViewById(R.id.iv);
                    iv.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    Log.e(TAG, "WriterException", e);
                    return;
                }
                break;
        }
    }

    public Bitmap createQRCodeByZxing(String contents, int size) throws WriterException {
        // QRコードをエンコードするクラス
        QRCodeWriter writer = new QRCodeWriter();

        // 異なる方の値を入れるためgenericは使えない
        Hashtable encodeHint = new Hashtable();

        // 日本語を扱うためにシフトJISを指定
        encodeHint.put(EncodeHintType.CHARACTER_SET, "shiftjis");

        // エラー修復レベルを指定
        // L  7%が修復可能
        // M 15%が修復可能
        // Q 25%が修復可能
        // H 30%が修復可能
        encodeHint.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix qrCodeData = writer.encode(contents, BarcodeFormat.QR_CODE, size, size, encodeHint);
        // QRコードのbitmap画像作成
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.argb(255, 255, 255, 255)); //いらないかも
        for (int x = 0; x < qrCodeData.getWidth(); x++) {
            for (int y = 0; y < qrCodeData.getHeight(); y++) {
                if (qrCodeData.get(x, y) == true) {
                    // 0はblack
                    bitmap.setPixel(x, y, Color.argb(255, 0,0, 0));
                } else {
                    // -1はwhite
                    bitmap.setPixel(x, y, Color.argb(255, 255, 255, 255));
                }
            }
        }

        return bitmap;
    }

}
