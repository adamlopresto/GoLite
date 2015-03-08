package fake.domain.adamlopresto.golite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Utils {

    static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.###");

    public static void error(final Context context, @NotNull final Throwable throwable) {
        if (context == null)
            throw new AssertionError("Null context when handling error "+throwable);
        new AlertDialog.Builder(context)
            .setMessage(throwable.getMessage())
            .setPositiveButton("Send bug report", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent send = new Intent(Intent.ACTION_SENDTO);
                    String uriText = "mailto:" + Uri.encode("adamlopresto@gmail.com") +
                            "?subject=" + Uri.encode("GoLite bug report") +
                            "&body=" + Uri.encode(Log.getStackTraceString(throwable));
                    Uri uri = Uri.parse(uriText);

                    send.setData(uri);
                    context.startActivity(Intent.createChooser(send, "Send mail..."));
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();

        Log.e("GoLite", throwable.getMessage(), throwable);
    }

    public static void error(Context context, @NotNull String msg){
        error(context, new Throwable(msg));
    }

    public static String getText(TextView v){
        if (v == null)
            return null;
        CharSequence seq = v.getText();
        if (seq == null)
            return null;
        return seq.toString();
    }

    public static String getNameFromBarcode(String barcode){
        HttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
        HttpUriRequest httppost = new HttpPost("http://www.outpan.com/api/get-product.php?apikey=7603ebff52fde57b0825a3226329b02e&barcode="+barcode);
        httppost.setHeader("Content-type", "application/json");

        InputStream inputStream = null;
        String result = null;
        try {
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            inputStream = entity.getContent();
            // json is UTF-8 by default
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line).append('\n');
            }
            result = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
            //return e.toString();
            // Oops
        }
        finally {
            try{if(inputStream != null)inputStream.close();}catch(Exception ignored){}
        }

        try {
            JSONObject jsonObject = new JSONObject(result);
            return jsonObject.getString("name");
        } catch (JSONException e) {
            return e.toString();
        }
    }

    public static double getDoubleFromTextView(@NotNull TextView textView){
        String string = textView.getText().toString();
        if (TextUtils.isEmpty(string))
            return 0.0;
        else
            return Double.parseDouble(string);
    }
}
