package jp.ne.ruru.park.ando.naiview;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

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
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyApplication  extends Application {

    public final ReentrantLock lock = new ReentrantLock();
    public final Handler mHandler = new Handler();
    public MyApplication() {
    }

    public static final String PRIVACY_POLICY_URL = "https://github.com/Tand0/NovelAI_input_support_android/blob/main/README.md";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private String log = "this is log.";
    public String getLog() {
        return this.log;
    }
    public void appendLog(Context context,String message) {
        final int len = 10000;
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
    private String prompt = "";
    public String getPrompt() {
        return this.prompt;
    }
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    private String uc = "";
    public String getUc() {
        return this.uc;
    }
    public void setUc(String uc) {
        this.uc = uc;
    }

    private JSONArray top = new JSONArray();
    public JSONArray getTop() {
        return this.top;
    }
    private byte[] imageBuffer;
    public byte[] getImageBuffer() {
        return this.imageBuffer;
    }
    public void setImageBuffer(byte[] imageBuffer) {
        this.imageBuffer = imageBuffer;
    }
    private String imageMimeType;
    public String getImageMimeType() {
        return this.imageMimeType;
    }
    public void setImageMimeType(String imageMimeType) {
        this.imageMimeType = imageMimeType;
    }
    public int anlas = -1;
    public int getAnlas() {
        return this.anlas;
    }
    public void setAnlas(int anlas) {
        this.anlas = anlas;
    }
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
    public void setTop(@NonNull JSONArray top) {
        this.top = top;
    }
    private JSONObject cut;
    public JSONObject getCut() {
        return this.cut;
    }
    public void setCut(JSONObject cut) {
        this.cut = cut;
    }
    private MyNASI myNASI = null;
    public MyNASI getMyNASI() {
        if (this.myNASI == null) {
            this.myNASI = new MyNASI();
        }
        return this.myNASI;
    }
    public boolean action(Context context, int id) {
        if (id == R.id.action_settings) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_prompt) {
            Intent intent = new Intent(context, PromptActivity.class);
            context.startActivity( intent );
            return true;
        } else if (id == R.id.action_uc) {
            Intent intent = new Intent(context, UcActivity.class);
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
            Intent intent = new Intent( Intent.ACTION_VIEW );
            intent.setData( Uri.parse(PRIVACY_POLICY_URL) );
            context.startActivity( intent );
            return true;
        } else if (id == R.id.generate_image) {
            execution( context,MyNASI.TYPE.IMAGE);
            return true;
        } else if (id == R.id.subscription) {
            subscription(context);
            return true;
        }
        return false;
    }
    public void fromPromptToTree(String target,boolean isPrompt) {
        this.ignoreData(isPrompt);
        this.createData(target,isPrompt);
    }
    public void fromTreeToPrompt(boolean isPrompt) {
        if (isPrompt) {
            this.setPrompt(fromTree(true));
        } else {
            this.setUc(fromTree(false));
        }
    }
    public void load(Context context,Uri imageUri,String mime) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String file;
        StringBuilder text = new StringBuilder();
        if ((mime== null) || (imageUri == null) || mime.contains("png")) {
            file = "x.png";
        } else {
            file = "x.jpg";
        }
        //
        updateImageBuffer(context,imageUri,mime);
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
                            this.setPrompt(string);
                        }
                        Integer integer = containInt(item,"steps");
                        if (integer != null) {
                            editor.putInt("prompt_steps",integer);
                        }
                        integer = containInt(item,"scale");
                        if (integer != null) {
                            editor.putInt("prompt_scale",integer);
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
    public void updateImageBuffer(Context context,Uri imageUri,String mime) {
        InputStream is = null;
        try {
            if (imageUri != null) {
                is = this.getContentResolver().openInputStream(imageUri);
            } else {
                is = context.getResources().openRawResource(R.raw.solo);
            }
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
        } catch (IOException e) {
            // NONE
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

    public static final String TEXT = "text";
    public static final String TEXT_IGNORE = "Ignore-";
    public static final String TEXT_UC = "uc-";
    public static final String TEXT_SEQUENCE = "Sequence";
    public static final String TEXT_SELECT = "Select";
    public static final String TEXT_WORD = "word";
    public static final String TEXT_WEIGHT = "Weight";
    public static final String VALUES = "values";
    public static final String CHILD = "child";
    public static final String EXPAND = "expand";
    public static final String LEVEL = "level";
    public void ignoreData(boolean isPrompt) {
        for (int i = 0 ; i < this.top.length() ; i++) {
            try {
                JSONObject item = (JSONObject)this.top.get(i);
                String text = containString(item,TEXT);
                if (text != null) {
                    boolean isPromptTarget = ! text.contains(TEXT_UC);
                    if (isPromptTarget != isPrompt) {
                        continue;
                    }
                    if (! text.contains(TEXT_IGNORE)) {
                        item.put(TEXT,TEXT_IGNORE + text);
                    }
                }
            } catch (JSONException e) {
                // NONE
            }
        }
    }
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
        top.put(parent);
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
    protected String createName(String name) {
        name = name.trim();
        name = name.replaceAll("\\s+", " ");
        int posX = getEnhancePos(name);
        name = getEnhanceText(name, posX);
        String name_raw = name
                .replace("{", "")
                .replace("}", "")
                .replace("[", "")
                .replace("]", "")
                .trim();
        name_raw = name_raw.replaceAll("\\s+", " ").trim();
        Pattern pattern = Pattern.compile("(.*)\\s*[\\s:](\\d+(\\.\\d+)?)\\s*$");
        Matcher result = pattern.matcher(name_raw);
        if (result.find()) {
            name = result.group(1);
            if (name == null) {
                name = name_raw;
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
                name = this.getEnhanceText(name,index);
            }
        } else {
            name = name.replace(":", " ").trim();
        }
        return name;
    }

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

    public String fromTree(boolean notIgnore) {
        String ans = "";
        JSONArray data = new JSONArray();
        this.deepCopyRemoveIgnore(data,top,notIgnore);
        LinkedList<String> result = new LinkedList<>();
        dictToList(result, data);
        if (0 < result.size()) {
            StringBuilder builder = new StringBuilder();
            for (String x : result) {
                builder.append(", ");
                builder.append(x);
            }
            ans = builder.toString();
            if (0 < result.size()) {
                ans = ans.substring(2);
            }
        }
        return ans;
    }
    public void deepCopyRemoveIgnore(JSONArray copy,JSONArray array, boolean notIgnore) {
        try {
            for (int i = 0; i < array.length(); i++) {
                Object object = array.get(i);
                if (!(object instanceof JSONObject)) {
                    continue;
                }
                JSONObject jsonObject = (JSONObject) object;
                String text = containString(jsonObject,TEXT);
                if (text != null) {
                    if (text.contains(TEXT_IGNORE)) {
                        continue;
                    }
                    boolean flag = text.contains(TEXT_UC);
                    if (flag == notIgnore) {
                        continue;
                    }
                    JSONObject child = new JSONObject();
                    copy.put(child);
                    text = containString(jsonObject,TEXT);
                    if (text != null) child.put(TEXT,text);
                    String values = containString(jsonObject,TEXT);
                    if (values != null) child.put(VALUES,values);
                    JSONArray copyChildArray = new JSONArray();
                    JSONArray childArray = containJSONArray(jsonObject,CHILD);
                    if (childArray != null) {
                        deepCopyRemoveIgnore(copyChildArray, childArray, notIgnore);
                    }
                    child.put(CHILD,childArray);
                }
            }
        } catch (JSONException e) {
            // NONE
        }
    }
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
                            list.add(values);
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
                            if (max != 0) {
                                boolean flag = true;
                                for (int i = 0; i < max; i++) {
                                    Random rand = new Random();
                                    int index = rand.nextInt(100);
                                    if (index < 60) {
                                        dictToList(list, childArray.get(index));
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
    public void subscription(Context context) {
        execution( context,MyNASI.TYPE.SUBSCRIPTION);
    }
    public void execution(Context context,MyNASI.TYPE type) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useTree = preferences.getBoolean("setting_use_tree",true);
        if (useTree) {
            this.setPrompt(fromTree(true));
            this.setUc(fromTree(false));
        }
        String prompt = this.getPrompt();
        String uc = this.getUc();
        String email = preferences.getString("setting_login_email","").trim();
        String password = preferences.getString("setting_login_password","").trim();
        int width;
        int height;
        try {
            String string =  preferences.getString("setting_width_x_height","512x768");
            int index = string.indexOf('x');
            if (0<index) {
                String widthString = string.substring(0,index);
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
        int scale;
        try {
            String string =  preferences.getString("prompt_scale","11");
            scale = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            scale = 11;
        }
        int steps;
        try {
            String string =  preferences.getString("prompt_steps","28");
            steps = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            steps = 28;
        }
        String sampler =  preferences.getString("prompt_sampler","k_dpmpp_2m");
        boolean sm;
        try {
            sm = preferences.getBoolean("prompt_sm",true);
        } catch (ClassCastException e) {
            sm = true;
        }
        boolean sm_dyn;
        try {
            sm_dyn = preferences.getBoolean("prompt_sm_dyn",true);
        } catch (ClassCastException e) {
            sm_dyn = true;
        }
        MyNASI.Allin1Request request = new MyNASI.Allin1Request(
                type,
                email,
                password,
                prompt,
                width,
                height,
                scale,
                steps,
                sampler,
                sm,
                sm_dyn,
                uc);
        Runnable runnable = () -> {
            if (lock.tryLock()) {
                try {
                    final MyNASI.Allin1Response res;
                    MyNASI nasi = this.getMyNASI();
                    if (request.type == MyNASI.TYPE.IMAGE) {
                        res = nasi.downloadImage(request);
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
    public void postSubscription(Context context,MyNASI.Allin1Response res) {
        StringBuilder buf = new StringBuilder();
        buf.append("code=").append(res.statusCode).append("\n")
                .append("description=").append(res.description).append("\n")
                .append("type=").append(res.type.name()).append("\n")
                .append("type=").append(res.mimeType).append("\n");
        if (res.type == MyNASI.TYPE.LOGIN) {
            if (res.statusCode != 200) {
                buf.append("result=").append(res.content).append("\n");
            }
        } else if (res.type == MyNASI.TYPE.IMAGE) {
            buf.append("result=").append(res.content).append("\n")
                    .append("request=").append(res.requestBody).append("\n");
            if (res.statusCode == 200) {
                setImageBuffer(res.imageBuffer);
                setImageMimeType(res.mimeType);
                setAnlas(res.anlas);
                if (context instanceof ImageActivity) {
                    ((ImageActivity)context).onMyResume();
                } else {
                    Intent intent = new Intent(context, ImageActivity.class);
                    context.startActivity( intent );
                }
            }
        } else { // MyNASI.TYPE.SUBSCRIPTION
            setAnlas(res.anlas);
            buf.append("anlas=").append(res.anlas).append("\n");
            buf.append("content=").append(res.content).append("\n");
        }
        this.appendLog(context,buf.toString());
    }

}
