package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import jp.ne.ruru.park.ando.naiview.databinding.ActivityTreeBinding;
import jp.ne.ruru.park.ando.naiview.tree.JSONListAdapter;

public class TreeActivity extends AppCompatActivity {

    private JSONListAdapter<JSONObject> adapter;

    private final String TITLE = "data.json";
    ActivityResultLauncher<Intent> resultLauncherLoad = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        TreeActivity.this.loadForASFResult(resultData.getData());
                    }
                }
            });
    ActivityResultLauncher<Intent> resultLauncherSave = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent resultData  = result.getData();
                    if (resultData  != null) {
                        Uri uri = resultData.getData();
                        if (uri != null) {
                            TreeActivity.this.saveForASFResult(uri);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTreeBinding binding = ActivityTreeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //
        binding.actionSave.setOnClickListener(v->save());
        binding.actionLoad.setOnClickListener(v->load());
        //
        adapter = new JSONListAdapter<>(this, android.R.layout.simple_list_item_1);
        //
        ListView listView = binding.listView;
        listView.setAdapter(adapter);
    }
    @Override
    public void onResume() {
        super.onResume();
        onMyResume();
    }
    public void onMyResume() {
        adapter.updateJSONArray();
    }
    public void load() {
        Intent load = new Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("application/json")
        .putExtra(Intent.EXTRA_TITLE,TITLE);
        resultLauncherLoad.launch(load);
    }
    private void loadForASFResult(Uri imageUri) {
        MyApplication a = (MyApplication) this.getApplication();
        StringBuilder text = new StringBuilder();
        try (InputStream is = this.getContentResolver().openInputStream(imageUri);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bis = new BufferedReader(isr)) {
            while (true) {
                String line = bis.readLine();
                if (line == null) {
                    break;
                }
                text.append(line);
                text.append("\n");
            }
        } catch (IOException e) {
            text.append(e.getClass().getName());
            text.append("\n");
            text.append(e.getMessage());
            text.append("\n");
            a.appendLog(this,text.toString());
            return;
        }
        try {
            JSONArray array = new JSONArray(text.toString());
            a.setTop(array);
            onMyResume();
        } catch (JSONException e) {
            text.append(e.getClass().getName());
            text.append("\n");
            text.append(e.getMessage());
            text.append("\n");
        }
        a.appendLog(this,text.toString());
    }
    public void save() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE,TITLE);
        resultLauncherSave.launch(intent);
    }
    public void saveForASFResult(Uri uri) {
        MyApplication a = (MyApplication) this.getApplication();
        JSONArray array = a.getTop();
        if (array == null) {
            return;
        }
        String result;
        try {
            result = array.toString(2);
        } catch (JSONException e) {
            return;
        }
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            os.write(result.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            String text = e.getClass().getName() +
                    "\n" +
                    e.getMessage();
            a.appendLog(this, text);
        }
    }
}