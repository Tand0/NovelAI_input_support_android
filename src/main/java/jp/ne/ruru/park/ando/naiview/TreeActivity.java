package jp.ne.ruru.park.ando.naiview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

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
import jp.ne.ruru.park.ando.naiview.adapter.JSONListAdapter;

/** tree activity
 * @author T.Ando
 */
public class TreeActivity extends AppCompatActivity {

    /** tree adapter */
    private JSONListAdapter<JSONObject> adapter;

    /** save or load filename */
    private final String TITLE = "data.json";

    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
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

    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
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

    /** on create
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityTreeBinding binding = ActivityTreeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        //
        binding.actionSaveInternal.setOnClickListener(v->saveInternal());
        binding.actionSaveExternal.setOnClickListener(v->save());
        binding.actionLoad.setOnClickListener(v->load());
        //
        adapter = new JSONListAdapter<>(this, android.R.layout.simple_list_item_1);
        //
        ListView listView = binding.listView;
        listView.setAdapter(adapter);
    }

    /**
     * on resume
     */
    @Override
    public void onResume() {
        super.onResume();
        onMyResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu_tree, menu);
        return true;
    }
    /**
     * repaint data
     */
    public void onMyResume() {
        adapter.updateJSONArray();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuButton){
        int buttonId = menuButton.getItemId();
        if (buttonId == android.R.id.home) {
            finish();
            return true;
        } else if (buttonId == R.id.action_prompt) {
            Intent intent = new Intent(this, PromptActivity.class);
            this.startActivity( intent );
            return true;
        }
        return super.onOptionsItemSelected(menuButton);
    }

    /** data load */
    public void load() {
        Intent load = new Intent(Intent.ACTION_OPEN_DOCUMENT)
        .addCategory(Intent.CATEGORY_OPENABLE)
        .setType("application/json")
        .putExtra(Intent.EXTRA_TITLE,TITLE);
        resultLauncherLoad.launch(load);
    }

    /**
     * intent call back method.
     * Used by Storage Access Framework
     * @param imageUri uri for image
     */
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

    /** save for callback */
    public void saveInternal() {
        MyApplication a = (MyApplication) this.getApplication();
        a.saveInternal(this);
        Toast.makeText(this , R.string.action_save_internal, Toast.LENGTH_SHORT).show();
    }

    /** save for callback */
    public void save() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_TITLE,TITLE);
        resultLauncherSave.launch(intent);
    }

    /**
     * intent call back method.
     * Used by Storage Access Framework
     */
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
            if (os != null) {
                os.write(result.getBytes(Charset.defaultCharset()));
            }
        } catch (IOException e) {
            String text = e.getClass().getName() +
                    "\n" +
                    e.getMessage();
            a.appendLog(this, text);
        }
    }
}