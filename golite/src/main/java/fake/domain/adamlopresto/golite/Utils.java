package fake.domain.adamlopresto.golite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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

        throw new RuntimeException(throwable);
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

        InputStream inputStream = null;
        String result = null;
        try {
            URL url = new URL("http://api.outpan.com/v2/products/"+barcode+"?apikey=7603ebff52fde57b0825a3226329b02e");

            URLConnection urlConnection = url.openConnection();
            inputStream = urlConnection.getInputStream();
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
            if (jsonObject.has("name"))
                return jsonObject.getString("name");
            else if (jsonObject.has("title"))
                return  jsonObject.getString("title");
            else return jsonObject.toString();
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
