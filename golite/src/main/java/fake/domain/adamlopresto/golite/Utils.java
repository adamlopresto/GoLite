package fake.domain.adamlopresto.golite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

public class Utils {
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
}
