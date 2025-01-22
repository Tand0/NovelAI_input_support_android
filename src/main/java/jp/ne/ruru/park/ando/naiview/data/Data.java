package jp.ne.ruru.park.ando.naiview.data;

import androidx.annotation.NonNull;

import org.bouncycastle.util.Iterable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Data implements Iterable<JSONObject> {
    /** dict name */
    public static final String VALUES = "values";

    /** dict name */
    public static final String CHILD = "child";

    /** dict name for tree activity */
    public static final String EXPAND = "expand";

    /** dict name for tree activity  */
    public static final String LEVEL = "level";

    /** dict name */
    public static final String I_TYPE = "ignore";

    /** character prompt type */
    public static final String P_TYPE = "prompt";

    /** dict name */
    public static final String T_TYPE = "text";

    /**
     * get object for JSONObject used name
     * @param object target JSONObject
     * @param name dict name
     * @return if in name then return result object, else return null (NOT JSONException)
     */
    public static JSONArray containJSONArray(Object object, String name) {
        if (object == null) {
            return null;
        }
        if (!(object instanceof JSONObject)) {
            return null;
        }
        JSONArray next;
        try {
            next = ((JSONObject)object).getJSONArray(name);
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
    public static JSONObject containJSONObject(Object object, String name) {
        if (object == null) {
            return null;
        }
        if (!(object instanceof JSONObject)) {
            return null;
        }
        JSONObject next;
        try {
            next = ((JSONObject)object).getJSONObject(name);
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
    public static String containString(Object object,String name) {
        if (object == null) {
            return null;
        }
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
    public static Integer containInt(Object object,String name) {
        if (object == null) {
            return null;
        }
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
    public static Boolean containBoolean(Object object,String name) {
        if (object == null) {
            return null;
        }
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
    public static Double containDouble(Object object,String name) {
        if (object == null) {
            return null;
        }
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
    private final Object object;
    private PromptType pType = null;
    private TextType tType = null;

    public Data(Object object) {
        if (object == null) {
            this.object = null;
        } else if (object instanceof Data) {
            this.object = ((Data)object).getObject();
        } else {
            this.object = object;
            Boolean iTypeBoolean = containBoolean(object, Data.I_TYPE);
            if (iTypeBoolean == null) {
                // for old version
                String textTypeString = containString(object, Data.T_TYPE);
                if (textTypeString != null) {
                    setIgnore(textTypeString.contains("Ignore-"));
                    if (textTypeString.contains("uc-")) {
                        setPromptType(PromptType.P_BASE_NG);
                    }
                    TextType textType = getTextType();
                    setTextType(textType);
                }
            }
        }
    }
    public boolean getIgnore() {
        Boolean iTypeBoolean = containBoolean(object,Data.I_TYPE);
        return iTypeBoolean != null && iTypeBoolean;
    }
    public void setIgnore(boolean type) {
        if ((object != null) && (object instanceof JSONObject)) {
            try {
                ((JSONObject)object).put(I_TYPE,type);
            } catch (JSONException e) {
                //
            }
        }
    }
    public Object getObject() {
        return this.object;
    }
    @NonNull
    public PromptType getPromptType() {
        if (pType == null) {
            if (object != null) {
                JSONObject target = null;
                if (object instanceof JSONObject) {
                    target = (JSONObject) object;
                }
                pType = PromptType.getType(target);
            } else {
                pType = PromptType.P_BASE_OK;
            }
        }
        return pType;
    }
    /**
     * change prompt type
     * @param pType prompt type
     */
    public void changePromptType(PromptType pType) {
        if (pType == null) {
            return;
        }
        this.setPromptType(pType);
        for (JSONObject child: this) {
            (new Data(child)).changePromptType(pType);
        }
    }
    public void setPromptType(PromptType type) {
        this.pType = type;
        //
        if ((object != null) && (object instanceof JSONObject)) {
            try {
                ((JSONObject)object).put(P_TYPE,type.toStringJson());
            } catch (JSONException e) {
                //
            }
        }
    }
    @NonNull
    public TextType getTextType() {
        if (tType == null) {
            if (object != null) {
                JSONObject target = null;
                if (object instanceof JSONObject) {
                    target = (JSONObject) object;
                }
                tType = TextType.getType(target);
            } else {
                tType = TextType.OTHER;
            }
        }
        return tType;
    }
    public void setTextType(TextType type) {
        this.tType = type;
        //
        if ((object != null) && (object instanceof JSONObject)) {
            try {
                ((JSONObject)object).put(T_TYPE,type.toStringJson());
            } catch (JSONException e) {
                //
            }
        }
    }

    @NonNull
    public String getValue() {
        if (object instanceof JSONObject) {
            String value = containString(object,VALUES);
            if (value == null) {
                return "";
            }
            return value;
        }
        return "";
    }
    public void setValue(String target) {
        if (object instanceof JSONObject) {
            try {
                if (target == null) {
                    target = "";
                }
                ((JSONObject)object).put(VALUES,target);
            } catch (JSONException e) {
                //
            }
        }
    }
    @Override
    @NonNull
    public Iterator<JSONObject> iterator() {
        JSONArray target;
        if (object == null) {
            target = null;
        } else if (object instanceof JSONArray) {
            target = (JSONArray) object;
        } else if (object instanceof JSONObject) {
            target = containJSONArray(object, CHILD);
        } else {
            target = null;
        }
        List<JSONObject> targetArray = new LinkedList<>();
        if (target != null) {
            for (int i = 0; i < target.length() ; i++) {
                try {
                    Object targetObject = target.get(i);
                    if (targetObject instanceof JSONObject) {
                        targetArray.add((JSONObject)targetObject);
                    }
                } catch (JSONException e) {
                    //
                }
            }
        }
        return targetArray.iterator();
    }
    @NonNull
    public JSONArray getChild() {
        JSONArray target;
        if (object == null) {
            target = new JSONArray();
        } else if (object instanceof JSONArray) {
            target = (JSONArray)object;
        } else if (object instanceof JSONObject) {
            try {
                target = ((JSONObject)object).getJSONArray(CHILD);
            } catch (JSONException e) {
                target = new JSONArray();
            }
        } else {
            target = new JSONArray();
        }
        return target;
    }
    public void setChild(JSONArray child) {
        if ((object != null) && (object instanceof JSONObject)) {
            try {
                ((JSONObject)object).put(CHILD,child);
            } catch (JSONException e) {
                //
            }
        }
    }
    public List<Data> getSelectableList() {
        List<Data> list = new ArrayList<>();
        for (JSONObject child : this) {
            Data data = new Data(child);
            if (!data.getIgnore()) {
                list.add(data);
            }
        }
        return list;
    }
    public boolean getExpand() {
        Boolean target = containBoolean(object,EXPAND);
        return target == null || target;
    }
    public void setExpand(boolean target) {
        if (object instanceof JSONObject) {
            try {
                ((JSONObject)object).put(EXPAND,target);
            } catch (JSONException e) {
                //
            }
        }
    }
    public int getLevel() {
        Integer target = containInt(object,LEVEL);
        return (target == null) ? 0 : target;
    }
    public void setLevel(int target) {
        if (object instanceof JSONObject) {
            try {
                ((JSONObject)object).put(LEVEL,target);
            } catch (JSONException e) {
                //
            }
        }
    }
}
