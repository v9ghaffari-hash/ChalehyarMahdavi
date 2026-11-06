package __PACKAGE__;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import androidx.activity.result.ActivityResult;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CapacitorPlugin(name = "NativeFileSaver")
public class NativeFileSaverPlugin extends Plugin {
    private final Map<String, OutputStream> openStreams = new ConcurrentHashMap<>();

    @PluginMethod
    public void chooseSaveLocation(PluginCall call) {
        String fileName = call.getString("fileName", "download.bin");
        String mimeType = call.getString("mimeType", "application/octet-stream");

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(call, intent, "handleSaveLocationResult");
    }

    @ActivityCallback
    private void handleSaveLocationResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result == null || result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("ذخیره‌سازی لغو شد.");
            return;
        }

        Uri targetUri = result.getData().getData();
        if (targetUri == null) {
            call.reject("سیستم فایل مقصد معتبری برنگرداند.");
            return;
        }

        try {
            OutputStream output = getContext().getContentResolver().openOutputStream(targetUri, "wt");
            if (output == null) {
                call.reject("بازکردن فایل مقصد برای نوشتن ممکن نشد.");
                return;
            }
            String sessionId = UUID.randomUUID().toString();
            openStreams.put(sessionId, output);
            JSObject resultData = new JSObject();
            resultData.put("sessionId", sessionId);
            resultData.put("fileName", call.getString("fileName", "download.bin"));
            call.resolve(resultData);
        } catch (Exception exception) {
            call.reject("ساخت فایل مقصد ناموفق بود: " + exception.getMessage(), exception);
        }
    }

    @PluginMethod
    public void writeChunk(PluginCall call) {
        String sessionId = call.getString("sessionId");
        String encoded = call.getString("data");
        if (sessionId == null || encoded == null) {
            call.reject("شناسه ذخیره‌سازی یا داده فایل ارسال نشده است.");
            return;
        }
        OutputStream output = openStreams.get(sessionId);
        if (output == null) {
            call.reject("نشست ذخیره‌سازی منقضی شده است.");
            return;
        }
        try {
            byte[] bytes = Base64.decode(encoded, Base64.DEFAULT);
            synchronized (output) {
                output.write(bytes);
            }
            JSObject response = new JSObject();
            response.put("bytesWritten", bytes.length);
            call.resolve(response);
        } catch (Exception exception) {
            call.reject("نوشتن بخشی از فایل ناموفق بود: " + exception.getMessage(), exception);
        }
    }

    @PluginMethod
    public void finishSave(PluginCall call) {
        String sessionId = call.getString("sessionId");
        OutputStream output = sessionId == null ? null : openStreams.remove(sessionId);
        if (output == null) {
            call.reject("نشست ذخیره‌سازی برای بستن پیدا نشد.");
            return;
        }
        try {
            output.flush();
            output.close();
            call.resolve();
        } catch (Exception exception) {
            call.reject("بستن فایل ذخیره‌شده ناموفق بود: " + exception.getMessage(), exception);
        }
    }

    @PluginMethod
    public void cancelSave(PluginCall call) {
        String sessionId = call.getString("sessionId");
        OutputStream output = sessionId == null ? null : openStreams.remove(sessionId);
        if (output != null) {
            try { output.close(); } catch (Exception ignored) { }
        }
        call.resolve();
    }
}
