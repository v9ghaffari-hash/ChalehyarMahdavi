package __PACKAGE__;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
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
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.json.JSONArray;
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

    @PluginMethod
    public void chooseUploadFolder(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(call, intent, "handleUploadFolderResult");
    }

    @ActivityCallback
    private void handleUploadFolderResult(PluginCall call, ActivityResult result) {
        if (call == null) return;
        if (result == null || result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("انتخاب پوشه لغو شد."); return;
        }
        Uri treeUri = result.getData().getData();
        if (treeUri == null) { call.reject("پوشه معتبری انتخاب نشد."); return; }
        try {
            getContext().getContentResolver().takePersistableUriPermission(treeUri,
                result.getData().getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
            JSONArray files = new JSONArray();
            Uri rootDoc = DocumentsContract.buildDocumentUriUsingTree(treeUri,
                DocumentsContract.getTreeDocumentId(treeUri));
            collectTreeFiles(treeUri, rootDoc, "", files, 0);
            JSObject response = new JSObject();
            response.put("files", files);
            response.put("folderName", queryDisplayName(rootDoc));
            call.resolve(response);
        } catch (Exception e) { call.reject("خواندن پوشه انتخاب‌شده ناموفق بود: " + e.getMessage(), e); }
    }

    private void collectTreeFiles(Uri treeUri, Uri documentUri, String parent, JSONArray output, int depth) throws Exception {
        if (depth > 30 || output.length() > 2000) return;
        String docId = DocumentsContract.getDocumentId(documentUri);
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId);
        String[] columns = { DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE };
        try (Cursor cursor = getContext().getContentResolver().query(children, columns, null, null, null)) {
            if (cursor == null) return;
            while (cursor.moveToNext()) {
                String id = cursor.getString(0), name = cursor.getString(1);
                String mime = cursor.getString(2);
                long size = cursor.isNull(3) ? 0 : cursor.getLong(3);
                Uri child = DocumentsContract.buildDocumentUriUsingTree(treeUri, id);
                String rel = parent.isEmpty() ? name : parent + "/" + name;
                if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                    collectTreeFiles(treeUri, child, rel, output, depth + 1);
                } else {
                    JSObject item = new JSObject();
                    item.put("name", name); item.put("relativePath", rel);
                    item.put("size", size); item.put("uri", child.toString());
                    output.put(item);
                }
                if (output.length() > 2000) break;
            }
        }
    }

    @PluginMethod
    public void readUploadFileChunk(PluginCall call) {
        String uriText = call.getString("uri");
        Integer offset = call.getInt("offset");
        Integer length = call.getInt("length");
        if (uriText == null || offset == null || length == null || offset < 0 || length < 1 || length > 262144) {
            call.reject("پارامترهای خواندن فایل نامعتبر است."); return;
        }
        try (InputStream input = getContext().getContentResolver().openInputStream(Uri.parse(uriText))) {
            if (input == null) throw new Exception("بازکردن فایل ممکن نشد.");
            long skipped = 0;
            while (skipped < offset) {
                long n = input.skip(offset - skipped);
                if (n <= 0) { if (input.read() == -1) break; n = 1; }
                skipped += n;
            }
            byte[] buffer = new byte[length]; int count = 0, n;
            while (count < length && (n = input.read(buffer, count, length - count)) != -1) count += n;
            byte[] exact = new byte[count]; System.arraycopy(buffer, 0, exact, 0, count);
            JSObject response = new JSObject();
            response.put("data", Base64.encodeToString(exact, Base64.NO_WRAP));
            call.resolve(response);
        } catch (Exception e) { call.reject("خواندن فایل پوشه ناموفق بود: " + e.getMessage(), e); }
    }

}
