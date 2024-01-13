package jp.ne.ruru.park.ando.naiview;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;


import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ne.ruru.park.ando.naiview.adapter.UriEtc;


/** application
 * @author foobar@em.boo.jp
 */
public class MyApplication  extends Application {

    /**
     * Prevent multiple events from running at the same time
     */
    public final ReentrantLock lock = new ReentrantLock();

    /**
     * handle to return for android thread
     */
    public final Handler mHandler = new Handler();

    /**
     * this is constructor
     */
    public MyApplication() {
    }

    /**
     * privacy policy for google play console
     */
    public static final String PRIVACY_POLICY_URL = "https://github.com/Tand0/NovelAI_input_support_android/blob/main/README.md";

    /**
     * novelai toppage
     */
    public static final String CREATE_ACCOUNT_URL = "https://novelai.net/";

    /**
     * on create
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * log area
     */
    private String log = "this is log.";

    /**
     * getter for log
     *
     * @return log
     */
    public String getLog() {
        return this.log;
    }

    /**
     * append log
     *
     * @param context activity
     * @param message message
     */
    public void appendLog(Context context, String message) {
        final int len = 5000;
        this.log = this.log + "\n\n" + message;
        if (len < this.log.length()) {
            this.log = this.log.substring(this.log.length() - len);
        }
        if (context != null) {
            if (context instanceof MainActivity) {
                ((MainActivity) context).onMyResume();
            }
        }
    }

    /**
     * prompt area
     */
    private String prompt = MyNASI.DEFAULT_PROMPT;

    /**
     * getter for prompt
     *
     * @return prompt
     */
    public String getPrompt() {
        return this.prompt;
    }

    /**
     * setter for prompt
     *
     * @param prompt prompt
     */
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    /**
     * uc area
     */
    private String uc = "lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, blurry";

    /**
     * getter for prompt
     *
     * @return uc
     */
    public String getUc() {
        return this.uc;
    }

    /**
     * setter for uc
     *
     * @param uc uc
     */
    public void setUc(String uc) {
        this.uc = uc;
    }

    /**
     * top (root) of tree
     */
    private JSONArray top = new JSONArray();

    /**
     * getter for top
     *
     * @return top
     */
    public JSONArray getTop() {
        return this.top;
    }

    /**
     * setter for top
     *
     * @param top top
     */
    public void setTop(@NonNull JSONArray top) {
        this.top = top;
    }

    /**
     * image buffer area.
     * Used by image activity
     */
    private byte[] imageBuffer;

    /**
     * getter for image buffer
     *
     * @return image buffer
     */
    public byte[] getImageBuffer() {
        return this.imageBuffer;
    }

    /**
     * setter for image buffer
     *
     * @param imageBuffer image buffer
     */
    public void setImageBuffer(byte[] imageBuffer) {
        this.imageBuffer = imageBuffer;
    }

    /**
     * image mime type area.
     * Used by image activity
     */
    private String imageMimeType;

    /**
     * getter for image mime type
     *
     * @return image mime type
     */
    public String getImageMimeType() {
        return this.imageMimeType;
    }

    /**
     * setter for image mime type
     *
     * @param imageMimeType image mime type
     */
    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }

    /**
     * anlas area.
     * Used by image activity
     */
    public int anlas = -1;

    /**
     * getter for anlas
     *
     * @return anlas
     */
    public int getAnlas() {
        return this.anlas;
    }

    /**
     * setter for anlas
     *
     * @param anlas anlas
     */
    public void setAnlas(int anlas) {
        this.anlas = anlas;
    }

    /**
     * cut data area.
     * Used by tree activity
     */
    private JSONObject cut;

    /**
     * getter for cut data
     *
     * @return cut data
     */
    public JSONObject getCut() {
        return this.cut;
    }

    /**
     * setter for cut data
     *
     * @param cut cut data
     */
    public void setCut(JSONObject cut) {
        this.cut = cut;
    }

    /**
     * seed data area.
     * Used by tree activity
     */
    private int seed;

    /**
     * getter for seed data
     *
     * @return seed data
     */
    public int getSeed() {
        return this.seed;
    }

    /**
     * setter for seed data
     *
     * @param seed seed data
     */
    public void setSeed(int seed) {
        this.seed = seed;
    }

    /**
     * download Flag data area.
     * Used by tree activity
     */
    private boolean downloadFlag = false;

    /**
     * getter for downloadFlag data
     *
     * @return downloadFlag data
     */
    public boolean getDownloadFlag() {
        return this.downloadFlag;
    }

    /**
     * setter for downloadFlag data
     *
     * @param downloadFlag downloadFlag data
     */
    public void setDownloadFlag(boolean downloadFlag) {
        this.downloadFlag = downloadFlag;
    }

    /**
     * imagePosition data area.
     * Used by tree activity
     */
    private int imagePosition = -1;

    /**
     * getter for imagePosition data
     *
     * @return imagePosition data
     */
    public int getImagePosition() {
        return this.imagePosition;
    }

    private final LinkedList<UriEtc> uriEtcList = new LinkedList<>();
    public LinkedList<UriEtc> getUriEtcList() {
        return uriEtcList;
    }
    /**
     * setter for imagePosition data
     *
     * @param imagePosition imagePosition data
     */
    public void setImagePosition(int imagePosition) {
        this.imagePosition = imagePosition;
    }

    public boolean isUseTree(SharedPreferences preferences) {
        return preferences.getBoolean("setting_use_tree", true);
    }
    public void setUseTree(SharedPreferences preferences,boolean flag) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("setting_use_tree", flag);
        editor.apply();
    }
    public boolean isPromptFixedSeed(SharedPreferences preferences) {
        return preferences.getBoolean("prompt_fixed_seed", false);
    }
    public void setPromptFixedSeed(SharedPreferences preferences,boolean flag) {
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("prompt_fixed_seed", flag);
        editor.apply();
    }
    public int getSettingScale(SharedPreferences preferences) {
        return preferences.getBoolean("setting_scale", true) ? 4 : 2;
    }

    public String getSettingWidthXHeight(SharedPreferences preferences) {
        return preferences.getString("setting_width_x_height", "512x768");
    }

    /**
     * Novel AI Support Interface area
     */
    private MyNASI myNASI = null;

    /**
     * getter for Novel AI Support Interface
     * @return Novel AI Support Interface
     */
    public MyNASI getMyNASI() {
        if (this.myNASI == null) {
            this.myNASI = new MyNASI();
        }
        return this.myNASI;
    }

    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public JSONArray containJSONArray(Object object, String name) {
        if (!(object instanceof JSONObject)) {
            return null;
        }
        Object next;
        try {
            next = ((JSONObject)object).get(name);
        } catch (JSONException e) {
            return null;
        }
        if (!(next instanceof JSONArray)) {
            return null;
        }
        return (JSONArray)next;
    }
    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public String containString(Object object,String name) {
        if (!(object instanceof JSONObject)) {
            return null;
        }
        Object next;
        try {
            next = ((JSONObject)object).get(name);
        } catch (JSONException e) {
            return null;
        }
        if (!(next instanceof String)) {
            return null;
        }
        return (String)next;
    }
    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public Integer containInt(Object object,String name) {
        if (!(object instanceof JSONObject)) {
            return null;
        }
        int next;
        try {
            next = ((JSONObject)object).getInt(name);
        } catch (JSONException e) {
            return null;
        }
        return next;
    }
    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public Boolean containBoolean(Object object,String name) {
        if (!(object instanceof JSONObject)) {
            return null;
        }
        boolean next;
        try {
            next = ((JSONObject)object).getBoolean(name);
        } catch (JSONException e) {
            return null;
        }
        return next;
    }
    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public Double containDouble(Object object,String name) {
        if (!(object instanceof JSONObject)) {
            return null;
        }
        double next;
        try {
            next = ((JSONObject)object).getDouble(name);
        } catch (JSONException e) {
            return null;
        }
        return next;
    }
    /**
     * click action for button
     * @param context activity
     * @param id button id
     * @return if used then true
     */
    public boolean action(Context context, int id) {
        if (id == R.id.action_settings) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_prompt) {
            Intent intent = new Intent(context, PromptActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_tree) {
            Intent intent = new Intent(context, TreeActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_image) {
            Intent intent = new Intent(context, ImageActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_policy) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(PRIVACY_POLICY_URL));
            context.startActivity(intent);
            return true;
        } else if (id == R.id.action_create_account) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(CREATE_ACCOUNT_URL));
            context.startActivity(intent);
            return true;
        } else if (id == R.id.subscription) {
            subscription(context);
            return true;
        }
        return false;
    }

    /**
     * send data from prompt/uc to tree
     * @param target prompt
     * @param isPrompt if prompt then true, uc then false
     */
    public void fromPromptToTree(String target,boolean isPrompt) {
        this.ignoreData(isPrompt);
        this.createData(target,isPrompt);
    }

    /**
     * send data from tree to prompt/uc
     * @param isPrompt if prompt then true, uc then false
     */
    public void fromTreeToPrompt(boolean isPrompt) {
        if (isPrompt) {
            this.setPrompt(fromTree(getTop(),true));
        } else {
            this.setUc(fromTree(getTop(),false));
        }
    }

    /**
     * load data to prompt/uc and image view
     * @param context activity
     * @param imageUri image uri, if null then used resource
     * @param mime mime type
     */
    public void load(Context context,Uri imageUri,String mime) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String file;
        StringBuilder text = new StringBuilder();
        if (mime == null) {
            mime = MyNASI.IMAGE_PNG;
        }
        if ((imageUri == null) || mime.contains("png")) {
            file = "x.png";
        } else {
            file = "x.jpg";
        }
        //
        updateImageBuffer(context,imageUri,mime);
        if ((imageBuffer == null) || (imageBuffer.length == 0)) {
            return;
        }
        //
        try (InputStream is = new ByteArrayInputStream(this.getImageBuffer())) {
            ImageMetadata data = Imaging.getMetadata(is,file);
            if (data == null) {
                throw new ImageReadException("ImageMetadata null");
            }
            for(ImageMetadata.ImageMetadataItem x :data.getItems()) {
                text.append(x.toString());
                text.append("\n\n");
                final String COMMENT = "comment:";
                final String PARAM = "parameters:";
                if (x.toString().toLowerCase().startsWith(COMMENT)) {
                    String comment = x.toString().substring(COMMENT.length());
                    try {
                        JSONObject item = new JSONObject(comment);
                        String string = containString(item,"prompt");
                        if (string != null) {
                            this.setPrompt(string);
                        }
                        string = containString(item,"uc");
                        if (string != null) {
                            this.setUc(string);
                        }
                        Integer integer = containInt(item,"steps");
                        if (integer != null) {
                            editor.putInt("prompt_int_number_steps",integer);
                        }
                        integer = containInt(item,"scale");
                        if (integer != null) {
                            editor.putInt("prompt_int_number_scale",integer);
                        }
                        Double doubleX = containDouble(item,"uncode_scale");
                        if (doubleX != null) {
                            editor.putInt("prompt_int_uncode_scale",(int)(doubleX*100));
                        }
                        string = containString(item,"sampler");
                        if (string != null) {
                            editor.putString("prompt_sampler",string);
                        }
                        Boolean targetBoolean = containBoolean(item,"sm");
                        if (targetBoolean != null) {
                            editor.putBoolean("prompt_sm",targetBoolean);
                        }
                        targetBoolean = containBoolean(item,"sm_dyn");
                        if (targetBoolean != null) {
                            editor.putBoolean("prompt_sm_dyn",targetBoolean);
                        }
                        integer = containInt(item,"seed");
                        if (integer != null) {
                            this.setSeed(integer);
                        }
                        string = containString(item,"noise_schedule");
                        if (string != null) {
                            editor.putString("noise_schedule",string);
                        }
                        editor.apply();
                    } catch (JSONException e) {
                        text.append(e.getMessage());
                    }
                } else if (x.toString().toLowerCase().startsWith(PARAM)) {
                    String prompt = x.toString();
                    String key = "Negative prompt: ";
                    int index = prompt.indexOf(key);
                    if (0 <= index) {
                        prompt = prompt.substring(0,index);
                    }
                    this.setPrompt(prompt);
                    this.ignoreData(true);
                    this.fromPromptToTree(prompt,true);
                }
            }
        } catch (ImageReadException | IOException e) {
            text.append(e.getClass().getName());
            text.append("\n");
            text.append(e.getMessage());
        }
        this.appendLog(context,text.toString());
        //
        if (!(context instanceof ImageActivity)) {
            Intent intent = new Intent(context, ImageActivity.class);
            context.startActivity(intent);
        }
    }

    /**
     * update image buffer from uri
     * @param context activity
     * @param imageUri image uri, if null then use resources
     * @param mime mime type
     */
    protected void updateImageBuffer(Context context,Uri imageUri,String mime) {
        if (mime == null) {
            mime = MyNASI.IMAGE_PNG;
        }
        //
        InputStream is = null;
        try {
            if (imageUri != null) {
                is = this.getContentResolver().openInputStream(imageUri);
            } else {
                is = context.getResources().openRawResource(R.raw.solo);
            }
            if (is != null) {
                byte[] bByte = new byte[1024];
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    while(true) {
                        int len = is.read(bByte);
                        if (len <= 0) {
                            break;
                        }
                        stream.write(bByte,0,len);
                    }
                    this.setImageBuffer(stream.toByteArray());
                    this.setImageMimeType(mime);
                }
            }
        } catch (IOException e) {
            String text = e.getClass().getName() +
                    "\n" +
                    e.getMessage();
            appendLog(context, text);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // NONE
                }
            }
        }
    }

    public void savingImageBuffer(Context context,Uri uri) {
        String text = "Save\n";
        try (OutputStream os = getContentResolver().openOutputStream(uri)) {
            if (os != null) {
                os.write(this.getImageBuffer());
            }
        } catch (IOException e) {
            text = e.getClass().getName() +
                    "\n" +
                    e.getMessage();
        }
        appendLog(context, text);
    }

    /** key for preferences */
    private final String KEY_PROMPT = "prompt";
    /**
     * save from the top
     * @param context activity
     */
    public void saveInternal(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String topString = this.getTop().toString();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_PROMPT,topString);
        editor.apply();
    }

    /**
     * load to the top
     * @param context activity
     */
    public void loadInternal(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String topString = preferences.getString(KEY_PROMPT,"");
            JSONArray top = new JSONArray(topString);
            this.setTop(top);
            preferences.edit().apply();
        } catch (JSONException e) {
            appendLog(context,"Internal save data not fond");
        }
    }
    /** dict name */
    public static final String TEXT = "text";

    /** dict value */
    public static final String TEXT_IGNORE = "Ignore-";

    /** dict value */
    public static final String TEXT_UC = "uc-";

    /** dict value */
    public static final String TEXT_SEQUENCE = "Sequence";

    /** dict value */
    public static final String TEXT_SELECT = "Select";

    /** dict value */
    public static final String TEXT_WORD = "word";

    /** dict value */
    public static final String TEXT_WEIGHT = "Weight";

    /** dict name */
    public static final String VALUES = "values";

    /** dict name */
    public static final String CHILD = "child";

    /** dict name for tree activity */
    public static final String EXPAND = "expand";

    /** dict name for tree activity  */
    public static final String LEVEL = "level";

    /**
     * Disable objects directly under root
     * @param isPrompt if true then prompt else uc.
     */
    public void ignoreData(boolean isPrompt) {
        for (int i = 0 ; i < this.getTop().length() ; i++) {
            try {
                JSONObject item = (JSONObject)this.getTop().get(i);
                String text = containString(item,TEXT);
                if (text != null) {
                    boolean isPromptTarget = ! text.contains(TEXT_UC);
                    if (isPromptTarget != isPrompt) {
                        continue;
                    }
                    if (! text.contains(TEXT_IGNORE)) {
                        item.put(TEXT,TEXT_IGNORE + text);
                        item.put(EXPAND,false);
                    }
                }
            } catch (JSONException e) {
                // NONE
            }
        }
    }

    /**
     * create dict from prompt/uc
     * @param string prompt/uc data
     * @param isPrompt if true then prompt else uc.
     */
    public void createData(String string,boolean isPrompt) {
        string = string.replace("\"", ",")
                .replace("_", " ")
                .replace("(", "{")
                .replace(")", "}")
                .replace("+", ",")
                .replace("|", ",")
                .replace("\r", ",")
                .replace("\n", ",");
        String[] middle = string.split(",");
        String topText;
        String wordText;
        String values;
        if (isPrompt) {
            topText = TEXT_SEQUENCE;
            wordText = TEXT_WORD;
            values = "prompt";
        } else {
            topText = TEXT_UC + TEXT_SEQUENCE;
            wordText = TEXT_UC + TEXT_WORD;
            values = "uc";
        }
        JSONObject parent = new JSONObject();
        this.getTop().put(parent);
        try {
            parent.put(TEXT,topText);
            parent.put(VALUES,values);
            JSONArray childArray = new JSONArray();
            parent.put(CHILD,childArray);
            for (String name : middle) {
                name = this.createName(name);
                if ((name == null) || (name.equals(""))) {
                    continue;
                }
                JSONObject child = new JSONObject();
                child.put(TEXT,wordText);
                child.put(VALUES,name);
                child.put(CHILD,new JSONArray());
                childArray.put(child);
            }
        } catch (JSONException e) {
            // NONE
        }
    }

    /**
     * create parse the characters
     * @param word original word
     * @return shaped word
     */
    protected String createName(String word) {
        word = word.trim();
        word = word.replaceAll("\\s+", " ");
        int posX = getEnhancePos(word);
        word = getEnhanceText(word, posX);
        String wordRaw = word
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "")
                .trim();
        wordRaw = wordRaw.replaceAll("\\s+", " ").trim();
        Pattern pattern = Pattern.compile("(.*)\\s*[\\s:](\\d+(\\.\\d+)?)\\s*$");
        Matcher result = pattern.matcher(wordRaw);
        if (result.find()) {
            word = result.group(1);
            if (word == null) {
                word = wordRaw;
            }
            String group2 = result.group(2);
            if (group2 != null) {
                double pos = Double.parseDouble(group2);
                double now = 1.0;
                int index = 0;
                if (pos < 1.0) {
                    for (int i = 0; i < 10; i++) {
                        now = now / 1.1;
                        index--;
                        if (now < pos) {
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        index++;
                        now = now * 1.1;
                        if (now > pos) {
                            break;
                        }
                    }
                }
                word = this.getEnhanceText(word,index);
            }
        } else {
            word = word.replace(":", " ").trim();
        }
        return word;
    }

    /**
     * get enhance index from word
     * @param string word
     * @return enhance index
     */
    public int getEnhancePos(String string) {
        int count = 0;
        for (int i = 0 ; i < string.length() ; i++) {
            char ch = string.charAt(i);
            if (ch == '{') {
                count++;
            } else if (ch == '[') {
                count--;
            }
        }
        return count;
    }

    /**
     * enhance word
     * @param string word
     * @param index enhance index
     * @return shaped word
     */
    public String getEnhanceText(String string,int index) {
        string = string
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "")
                .trim();
        StringBuilder body = new StringBuilder();
        if (index == 0) {
            body.append(string);
        } else if (index < 0) {
            for (int i = 0; i < (-index) ; i++) {
                body.append("[");
            }
            body.append(string);
            for (int i = 0 ; i < (-index) ; i++) {
                body.append("]");
            }
        } else {
            for (int i = 0 ; i < index ; i++) {
                body.append("{");
            }
            body.append(string);
            for (int i = 0 ; i < index ; i++) {
                body.append("}");
            }
        }
        return body.toString();
    }

    /**
     * get prompt/uc from tree
     * @param array original data
     * @param isPrompt if true then prompt else uc.
     * @return prompt/uc
     */
    public String fromTree(JSONArray array,boolean isPrompt) {
        List<String> result = fromTreeList(array,isPrompt);
        String ans = "";
        if (0 < result.size()) {
            StringBuilder builder = new StringBuilder();
            final String comma = ", ";
            for (String x : result) {
                builder.append(comma);
                builder.append(x);
            }
            ans = builder.substring(comma.length());
        }
        return ans;
    }

    /**
     * get prompt/uc from tree
     * @param array original data
     * @param isPrompt if true then prompt else uc.
     * @return prompt/uc
     */
    public List<String> fromTreeList(JSONArray array,boolean isPrompt) {
        JSONArray data = new JSONArray();
        this.deepCopyRemoveIgnore(data,array,isPrompt);
        LinkedList<String> result = new LinkedList<>();
        dictToList(result, data);
        return result;
    }
    /**
     * get a deep copy of the data ignoring TEXT_IGNORE
     * @param copy copy data
     * @param array original data
     * @param isPrompt if true then prompt else uc.
     */
    public void deepCopyRemoveIgnore(JSONArray copy,JSONArray array, boolean isPrompt) {
        try {
            for (int i = 0; i < array.length(); i++) {
                Object object = array.get(i);
                if (!(object instanceof JSONObject)) {
                    continue;
                }
                JSONObject jsonObject = (JSONObject) object;
                String text = containString(jsonObject,TEXT);
                if (text == null) {
                    continue;
                }
                if (text.contains(TEXT_IGNORE)) {
                    continue;
                }
                boolean flag = text.contains(TEXT_UC);
                if (flag == isPrompt) {
                    continue;
                }
                JSONObject child = new JSONObject();
                copy.put(child);
                text = containString(jsonObject,TEXT);
                if (text != null) child.put(TEXT,text);
                String values = containString(jsonObject,VALUES);
                if (values != null) child.put(VALUES,values);
                JSONArray copyChildArray = new JSONArray();
                JSONArray childArray = containJSONArray(jsonObject,CHILD);
                if ((childArray != null) && (0 < childArray.length())) {
                    deepCopyRemoveIgnore(copyChildArray, childArray, isPrompt);
                }
                child.put(CHILD,copyChildArray);
            }
        } catch (JSONException e) {
            // NONE
        }
    }

    /**
     * change tree to list
     * @param list list
     * @param object tree
     */
    public void dictToList(LinkedList<String> list,Object object) {
        if (object == null) {
            return;
        }
        try {
            if (object instanceof JSONArray) {
                JSONArray array = (JSONArray) object;
                for (int i = 0; i < array.length(); i++) {
                    dictToList(list, array.get(i));
                }
            } else if (object instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) object;
                String mode = containString(jsonObject, TEXT);
                if (mode != null) {
                    if (mode.contains(TEXT_WORD)) {
                        String values = containString(jsonObject, VALUES);
                        if (values != null) {
                            values = values.replace(","," ").trim();
                            if (! values.equals("")) {
                                list.add(values);
                            }
                        }
                    } else if (mode.contains(TEXT_SEQUENCE)) {
                        dictToList(list, containJSONArray(jsonObject, CHILD));
                    } else if (mode.contains(TEXT_SELECT)) {
                        JSONArray childArray = containJSONArray(jsonObject, CHILD);
                        if (childArray != null) {
                            int max = childArray.length();
                            if (max != 0) {
                                Random rand = new Random();
                                int index = rand.nextInt(max);
                                dictToList(list, childArray.get(index));
                            }
                        }
                    } else if (mode.contains(TEXT_WEIGHT)) {
                        JSONArray childArray = containJSONArray(jsonObject, CHILD);
                        if (childArray != null) {
                            int max = childArray.length();
                            if (0 < max) {
                                Random rand = new Random();
                                boolean flag = true;
                                for (int i = 0; i < max; i++) {
                                    if (rand.nextInt(100) < 60) {
                                        dictToList(list, childArray.get(i));
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    dictToList(list, childArray.get(max - 1));
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            // NONE
        }
    }
    public void changePart(Context context,JSONObject item) {
        String key = containString(item,TEXT);
        boolean isPrompt;
        if (key == null) {
            isPrompt = Boolean.FALSE;
        } else {
            isPrompt = ! key.contains(TEXT_UC);
        }
        //
        JSONArray dummyTop = new JSONArray();
        dummyTop.put(item);
        String answer = fromTree(dummyTop,isPrompt);
        String target;
        if (isPrompt) {
            target = this.getPrompt();
        } else {
            target = this.getUc();
        }
        target = deleteTextFromItem(target,item);
        if (!target.equals("") && !answer.equals("")) {
            target = target + ", ";
        }
        target = target + answer;
        if (isPrompt) {
            this.setPrompt(target);
        } else {
            this.setUc(target);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setUseTree(preferences,false);
        //
        action(context,R.id.action_prompt);
    }

    /**
     * delete text from item
     * @param targets original text
     * @param object item
     * @return change text
     */
    protected String deleteTextFromItem(String targets,Object object) {
        List<String> keys = getDeleteTextList(object);
        keys.sort((a,b)-> a.length() == b.length() ? b.compareTo(a) : b.length() - a.length());
        String[] targetsArray = targets.split(",");
        StringBuilder result = new StringBuilder();
        boolean commaFlag = false;
        for (String target : targetsArray) {
            boolean notHit = true;
            String targetBreak = changeBaseKey(target);
            for (String key : keys) {
                if (targetBreak.equals(key)) {
                    notHit = false;
                     break;
                }
            }
            if (notHit) {
                if (commaFlag) {
                    result.append(", ");
                }
                commaFlag = true;
                result.append(target.trim());
            }
        }
        return result.toString();
    }
    protected List<String> getDeleteTextList(Object object) {
        List<String> list = new java.util.ArrayList<>();
        if (object == null) {
            return list;
        } else if (object instanceof JSONArray) {
            JSONArray array = (JSONArray)object;
            for (int i = 0 ; i < array.length() ; i++) {
                try {
                    list.addAll(getDeleteTextList(array.get(i)));
                } catch (JSONException e) {
                    // NONE
                }
            }
        } else if (object instanceof JSONObject) {
            String type = containString(object,TEXT);
            if ((type != null) && type.contains(TEXT_WORD)) {
                String key = containString(object,VALUES);
                if (key != null) {
                    list.add(changeBaseKey(key));
                }
            }
            list.addAll(getDeleteTextList(containJSONArray(object,CHILD)));
        }
        return list;
    }
    private String changeBaseKey(String key) {
        return key.replaceAll("[{}\\[\\]]","").replaceAll("\\s+"," ").trim();
    }


    /** get subscription
     * @param context activity
     */
    public void subscription(Context context) {
        execution(context,MyNASI.TYPE.SUBSCRIPTION,-1,-1,null);
    }

    /** send data fo Novel AI Support Interface
     *
     * @param context activity
     * @param type execution type
     * @param bitmapX display image width
     * @param bitmapY display image height
     * @param option option
     */
    public void execution(Context context,MyNASI.TYPE type,int bitmapX,int bitmapY,Object option) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String email = preferences.getString("setting_login_email","").trim();
        String password = preferences.getString("setting_login_password","").trim();
        MyNASI.Allin1Request request;
        if (type == MyNASI.TYPE.UPSCALE) {
            String message = context.getResources().getString(R.string.upscale);
            Toast.makeText(this , message, Toast.LENGTH_LONG).show();
            request = new MyNASI.Allin1RequestUpscale(
                    type,
                    email,
                    password,
                    bitmapX,
                    bitmapY,
                    getSettingScale(preferences),
                    this.imageBuffer);
        } else if (type == MyNASI.TYPE.IMAGE) {
            String message = context.getResources().getString(R.string.generate_image);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            boolean useTree = isUseTree(preferences);
            if (useTree) {
                if (0 < this.getTop().length()) {
                    this.setPrompt(fromTree(getTop(),true));
                    this.setUc(fromTree(getTop(),false));
                } else {
                    fromPromptToTree(getPrompt(), true);
                    fromPromptToTree(getUc(), false);
                }
            }
            String prompt = this.getPrompt();
            String uc = this.getUc();
            String model = preferences.getString("prompt_model", "nai-diffusion-3").trim();
            int width;
            int height;
            try {
                String string = getSettingWidthXHeight(preferences);
                int index = string.indexOf('x');
                if (0 < index) {
                    String widthString = string.substring(0, index);
                    String heightString = string.substring(index + 1);
                    width = Integer.parseInt(widthString);
                    height = Integer.parseInt(heightString);
                } else {
                    throw new NumberFormatException("not number x number");
                }
            } catch (NumberFormatException e) {
                width = 512;
                height = 768;
            }
            int scale = preferences.getInt("prompt_int_number_scale", 11);
            int steps = preferences.getInt("prompt_int_number_steps", 28);
            int uncodeScale = preferences.getInt("prompt_int_uncode_scale", 100);
            String sampler = preferences.getString("prompt_sampler", "k_dpmpp_2m");
            boolean sm;
            try {
                sm = preferences.getBoolean("prompt_sm", true);
            } catch (ClassCastException e) {
                sm = true;
            }
            boolean sm_dyn;
            try {
                sm_dyn = preferences.getBoolean("prompt_sm_dyn", true);
            } catch (ClassCastException e) {
                sm_dyn = true;
            }
            boolean fixed_seed;
            try {
                fixed_seed = isPromptFixedSeed(preferences);
            } catch (ClassCastException e) {
                fixed_seed = false;
            }
            int seed = this.getSeed();
            if ((!fixed_seed) || (seed == 0)) {
                seed = new java.util.Random().nextInt(Integer.MAX_VALUE - 1) + 1;
                this.setSeed(seed);
            }
            String noise_schedule = preferences.getString("prompt_noise_schedule", "native").trim();
            request = new MyNASI.Allin1RequestImage(
                    type,
                    email,
                    password,
                    model,
                    prompt,
                    width,
                    height,
                    scale,
                    steps,
                    uncodeScale,
                    sampler,
                    sm,
                    sm_dyn,
                    uc,
                    seed,
                    noise_schedule);
        } else if (type == MyNASI.TYPE.SUGGEST_TAGS) {
            String model = preferences.getString("prompt_model", "nai-diffusion-3").trim();
            String target = (String) option;
            //
            request = new MyNASI.Allin1RequestSuggestTags(
                    type,
                    email,
                    password,
                    model,
                    target);
         } else {
            request = new MyNASI.Allin1Request(
                    type,
                    email,
                    password);
        }
        Runnable runnable = () -> {
            if (lock.tryLock()) {
                try {
                    final MyNASI.Allin1Response res;
                    MyNASI nasi = this.getMyNASI();
                    if (request.type == MyNASI.TYPE.UPSCALE) {
                        res = nasi.upscale((MyNASI.Allin1RequestUpscale)request);
                    } else if (request.type == MyNASI.TYPE.IMAGE) {
                        res = nasi.downloadImage((MyNASI.Allin1RequestImage)request);
                    } else if (request.type == MyNASI.TYPE.SUGGEST_TAGS) {
                        res = nasi.suggestTags((MyNASI.Allin1RequestSuggestTags)request);
                    } else {
                        res = nasi.subscription(request);
                    }
                    //
                    mHandler.post(()->postSubscription(context,res));
                } finally {
                    lock.unlock();
                }
            }
        };
        Executors.newSingleThreadExecutor().execute(runnable);
    }

    /** call back from Novel AI Support Interface
     * @param context activity
     * @param res result data
     */
    public void postSubscription(Context context,MyNASI.Allin1Response res) {
        StringBuilder buf = new StringBuilder();
        buf.append("statusCode=").append(res.statusCode).append("\n")
                .append("description=").append(res.description).append("\n")
                .append("type=").append(res.type.name()).append("\n")
                .append("mimeType=").append(res.mimeType).append("\n");
        if ((res.content != null) && (!res.content.equals(""))) {
            buf.append("content=").append(res.content).append("\n");
        }
        if (res.type == MyNASI.TYPE.LOGIN) {
            buf.append("login\n");
        } else if ((res.type == MyNASI.TYPE.IMAGE)
            || (res.type == MyNASI.TYPE.UPSCALE)) {
            if (res.type == MyNASI.TYPE.IMAGE) {
                buf.append("requestBody=").append(res.requestBody).append("\n");
            }
            if (res.statusCode == 200) {
                setImageBuffer(res.imageBuffer);
                setImageMimeType(res.mimeType);
                setDownloadFlag(true);
                setAnlas(res.anlas);
                if (context instanceof ImageActivity) {
                    ((ImageActivity) context).onMyResume();
                } else {
                    Intent intent = new Intent(context, ImageActivity.class);
                    context.startActivity(intent);
                }
                String message = "OK";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            } else {
                String message = "NG";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        } else if (res.type == MyNASI.TYPE.SUGGEST_TAGS) {
            if (res.statusCode == 200) {
                if (context instanceof SuggestActivity) {
                    ((SuggestActivity) context).suggestTagsResponse(res.content);
                }
            }
        } else { // MyNASI.TYPE.SUBSCRIPTION
            setAnlas(res.anlas);
            buf.append("anlas=").append(res.anlas).append("\n");
        }
        this.appendLog(context,buf.toString());
    }

}
