package jp.ne.ruru.park.ando.naiview;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rfksystems.blake2b.Blake2b;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

/** Novel AI support interface
 * @author T.Ando
 */
public class MyNASI {
    /**
     * default prompt
     */
    public static final String DEFAULT_PROMPT = "1girl, best quality, amazing quality, very aesthetic, absurdres";

    /**
     * default UC prompt
     */
    public static final String DEFAULT_PROMPT_UC = "lowres, bad anatomy, bad hands, text, error, missing fingers, extra digit, fewer digits, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, blurry";

    /** mime type gif */
    public static final String IMAGE_PNG = "image/png";
    /** mime type png */
    public static final String IMAGE_GIF = "image/gif";

    /** data area */
    protected final byte[] bByte = new byte[1024*8];

    /**
     * state machine
     */
    public enum REST_TYPE {
        LOGIN,
        IMAGE,
        SUBSCRIPTION,
        UPSCALE,
        SUGGEST_TAGS
    }

    /**
     * all in 1 request data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1Request {
        /**
         * this is constructor
         *
         * @param type     execution type
         * @param email    mail address (for login)
         * @param password password (for login)
         */
        Allin1Request(
                REST_TYPE type,
                String email,
                String password) {
            this.type = type;
            this.email = email;
            this.password = password;
        }

        public final REST_TYPE type;
        public final String email;
        public final String password;
    }
    /**
     * all in 1 request data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1RequestImage extends Allin1Request {
        /**
         * this is constructor
         *
         * @param isV4            if version_4 then true
         * @param type            execution type
         * @param email           mail address (for login)
         * @param password        password (for login)
         * @param model           model
         * @param input           prompt
         * @param width           width
         * @param height          height
         * @param scale           scale
         * @param steps           steps
         * @param cfgRescale      cfgRescale
         * @param sampler         sampler
         * @param sm              sm
         * @param sm_dyn          sm_dyn
         * @param variety          variety
         * @param dynamicThresholding dynamic_thresholding
         * @param negative_prompt uc
         * @param seed            seed
         * @param noise_schedule  noise schedule
         * @param imageBuffer     base image for image2image
         * @param strength        strength for image2image
         * @param noise           noise for image2image
         * @param characters      cha01_Ok, cha01_NG, ch02_Ok, ch02_NG
         * @param locations        character locations
         */
        Allin1RequestImage(
                boolean isV4,
                REST_TYPE type,
                String email,
                String password,
                String model,
                String input,
                int width,
                int height,
                int scale,
                int steps,
                int cfgRescale,
                String sampler,
                boolean sm,
                boolean sm_dyn,
                boolean variety,
                boolean dynamicThresholding,
                String negative_prompt,
                int seed,
                String noise_schedule,
                byte[] imageBuffer,
                int strength,
                int noise,
                String[] characters,
                int[] locations
        ) {
            super(type,email,password);
            this.isV4 = isV4;
            this.model = model;
            this.input = input;
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.steps = steps;
            this.cfgRescale = cfgRescale;
            this.sampler = sampler;
            this.sm = sm;
            this.sm_dyn = sm_dyn;
            this.variety = variety;
            this.dynamicThresholding = dynamicThresholding;
            this.negative_prompt = negative_prompt;
            this.seed = seed;
            this.noise_schedule = noise_schedule;
            this.imageBuffer = imageBuffer;
            this.strength = strength;
            this.noise = noise;
            this.characters = characters;
            this.locations = locations;
        }
        public final boolean isV4;
        public final String model;
        public final String input;

        public final int width;

        public final int height;
        public final int scale;
        public final int steps;
        public final int cfgRescale;
        public final String sampler;
        public final boolean sm;
        public final boolean sm_dyn;
        public final boolean variety;
        public final boolean dynamicThresholding;
        public final String negative_prompt;

        public final int seed;

        public final String noise_schedule;

        public final byte[] imageBuffer;

        public final int strength;

        public final int noise;

        public final String[] characters;

        public final int[] locations;
    }
    /**
     * all in 1 request data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1RequestUpscale extends Allin1Request {
        /**
         * this is constructor
         *
         * @param type            execution type
         * @param email           mail address (for login)
         * @param password        password (for login)
         * @param width           width
         * @param height          height
         * @param scale           scale
         * @param imageBuffer     image data
         */
        Allin1RequestUpscale(
                REST_TYPE type,
                String email,
                String password,
                int width,
                int height,
                int scale,
                byte[] imageBuffer) {
            super(type,email,password);
            this.width =width;
            this.height =height;
            this.scale =scale;
            this.imageBuffer = imageBuffer;
        }
        public final int width;
        public final int height;
        public final int scale;
        public final byte[] imageBuffer;
    }

    /**
     * all in 1 request data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1RequestSuggestTags extends Allin1Request {
        /**
         * this is constructor
         *
         * @param type            execution type
         * @param email           mail address (for login)
         * @param model           model
         * @param prompt          prompt
         */
        Allin1RequestSuggestTags(
                REST_TYPE type,
                String email,
                String password,
                String model,
                String prompt) {
            super(type,email,password);
            this.model =model;
            this.prompt =prompt;
        }
        public final String model;
        public final String prompt;
    }

    /**
     * all in 1 result data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1Response {
        /** this is constructor
         * @param type execution type
         * @param m request body (for log)
         * @param statusCode status code (for callback)
         * @param mimeType mime type (for image activity)
         * @param content result body (for log)
         * @param imageData image data (for image activity)
         */
        Allin1Response(
                REST_TYPE type,
                JSONObject m,
                int statusCode,
                String mimeType,
                String content,
                byte[] imageData) {
            this.type = type;
            this.statusCode = statusCode;
            this.mimeType = mimeType;
            this.content = content;
            this.m = m;
            this.imageBuffer = imageData;
        }
        /**
         * setter for description (status code result)
         * @see <a href="https://github.com/Aedial/novelai-api">Novel AI rest api</a>
         * @param description description
         * @return this
         */
        private Allin1Response setDescription(String description) {
            this.description = description;
            return this;
        }
        public final REST_TYPE type;
        public final JSONObject m;
        public final int statusCode;
        public final String mimeType;

        public final String content;
        public String description = "no description";
        public final byte[] imageBuffer;

        /** set anlas
         *
         * @param anlas anlas
         * @return this
         */
        public Allin1Response setAnlas(int anlas) {
            this.anlas = anlas;
            return this;
        }
        public int anlas = -1;
    }

    /** login url for rest api */
    public final String LOGIN_URL = "https://api.novelai.net/user/login";

    /** image url for rest api */
    public final String IMAGE_URL = "https://image.novelai.net/ai/generate-image";

    /** upscale url for rest api */
    public final String UPSCALE_URL = "https://api.novelai.net/ai/upscale";
    /** suggest tags url for rest api */
    public final String SUGGEST_TAGS_URL = "https://image.novelai.net/ai/generate-image/suggest-tags";

    /** subscription url for rest api */
    public final String SUBSCRIPTION_URL = "https://api.novelai.net/user/subscription";

    /** authorization key */
    private String authorizationKey = null;

    /**
     * check login
     * @return if already login then true
     */
    public boolean requireLogin() {
        return authorizationKey == null;
    }

    /**
     * login
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response login(Allin1Request request) {
        final String aKey = getAKey(request.email, request.password);
        Allin1Response res;
        try {
            JSONObject m = new JSONObject();
            m.put("key", aKey);
            res = getConnection(
                    REST_TYPE.LOGIN,LOGIN_URL,m);
        } catch (JSONException e) {
            return new Allin1Response(
                    REST_TYPE.LOGIN,
                    null,
                    202,
                    "",
                    e.getClass().getName(),
                    null)
                    .setDescription(e.getMessage());
        }
        //
        //
        int status_code = res.statusCode;
        String description;
        if (status_code == 201) {
            description = "Login successful.";
            authorizationKey = getAccessToken(res);
        } else if (status_code == 400) {
            description = "A validation error occurred.";
        } else if (status_code == 401) {
            description = "Access Key is incorrect.";
        } else if (status_code == 501) {
            description = "An unknown error occurred.";
        } else {
            description = "Unknown stats code.";
        }
        return res.setDescription(description);
    }

    @Nullable
    protected String getAccessToken(Allin1Response res) {
        String accessToken = null;
        if (res.content != null) {
            String authorizationHeader = "Bearer ";
            String accessToken1 = "accessToken";
            try {
                JSONObject jsonObject = new JSONObject(res.content);
                accessToken = jsonObject.getString(accessToken1);
                accessToken = authorizationHeader + accessToken;
            } catch (JSONException e) {
                // EMPTY
            }
        }
        return accessToken;
    }

    /**
     * download image
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response downloadImage(Allin1RequestImage request) {
        //
        if (this.requireLogin()) {
            Allin1Response res = login(request);
            if (this.requireLogin()) {
                return res;
            }
        }

        //
        Allin1Response res;
        try {
            String okInput = (request.input == null) ? "" : request.input;
            String ngInput = (request.negative_prompt == null) ? "" : request.negative_prompt;
            if (! request.isV4) {
                if (!request.characters[0].isEmpty()) {
                    okInput = request.characters[0] + ", " + okInput;
                }
                if (!request.characters[1].isEmpty()) {
                    ngInput = request.characters[1] + ", " + ngInput;
                }
                if (!request.characters[2].isEmpty()) {
                    okInput = request.characters[2] + ", " + okInput;
                }
                if (!request.characters[3].isEmpty()) {
                    ngInput = request.characters[3] + ", " + ngInput;
                }
            }
            JSONObject p = new JSONObject();
            p.put("params_version",3);
            p.put("width",request.width);
            p.put("height",request.height);
            p.put("scale",request.scale);
            p.put("sampler",request.sampler);
            p.put("steps",request.steps);
            p.put("seed",request.seed);
            p.put("n_samples",1);
            p.put("ucPreset", 0);
            p.put("qualityToggle",true);
            if (! request.isV4) {
                p.put("sm", request.sm);
                p.put("sm_dyn", request.sm_dyn);
            }
            p.put("dynamic_thresholding", request.variety);
            p.put("controlnet_strength",1);
            p.put("legacy",false);
            p.put("add_original_image",true);
            p.put("cfg_rescale", ((double)request.cfgRescale)/100.0);
            p.put("noise_schedule",request.noise_schedule);
            p.put("legacy_v3_extend", false);
            if (request.variety) {
                p.put("skip_cfg_above_sigma", 19);
            } else {
                p.put("skip_cfg_above_sigma", null);
            }
            if (request.isV4) {
                p.put("use_coords",true);
                JSONArray characterPrompts = new JSONArray();
                p.put("characterPrompts",characterPrompts);
                //character 01
                if (! request.characters[0].isEmpty()) {
                    String ok = request.characters[0];
                    String ng = request.characters[1];
                    int x = request.locations[0];
                    int y = request.locations[1];
                    characterPrompts.put(createCharacterPrompts(ok, ng, x, y));
                }
                if (! request.characters[2].isEmpty()) {
                    String ok = request.characters[2];
                    String ng = request.characters[3];
                    int x = request.locations[2];
                    int y = request.locations[3];
                    characterPrompts.put(createCharacterPrompts(ok, ng, x, y));
                }
                //
                JSONObject v4Prompt = new JSONObject();
                p.put("v4_prompt",v4Prompt);
                v4Prompt.put("caption",createCaption(request,true));
                v4Prompt.put("use_coords", true);
                v4Prompt.put("use_order", true);
                JSONObject v4NegativePrompt = new JSONObject();
                p.put("v4_negative_prompt",v4NegativePrompt);
                v4NegativePrompt.put("caption",createCaption(request,false));
            }
            p.put("negative_prompt",ngInput);
            JSONArray noArray = new JSONArray();
            p.put("reference_image_multiple",noArray);
            p.put("reference_information_extracted_multiple",noArray);
            p.put("reference_strength_multiple",noArray);
            p.put("deliberate_euler_ancestral_bug",false);
            p.put("prefer_brownian",true);
            if (request.imageBuffer != null) {
                // for image2image
                String image = Base64.getEncoder().encodeToString(request.imageBuffer);
                p.put("image", image);
                p.put("strength",((double) request.strength) / 100.0);
                p.put("noise", ((double) request.noise) / 100.0);
                p.put("extra_noise_seed",request.seed);
            }
            //
            JSONObject m = new JSONObject();
            m.put("input",okInput);
            m.put("model",request.model);
            if (request.imageBuffer == null) {
                // for generate image
                m.put("action", "generate");
            } else {
                // for img2img
                m.put("action", "img2img");
            }
            m.put("parameters",p);
            //
            res = this.getConnection(request.type,IMAGE_URL,m);
            //
        } catch (JSONException e) {
            return new Allin1Response(
                    request.type,
                    null,
                    202,
                    "",
                    e.getClass().getName(),
                    null)
                    .setDescription(e.getMessage());
        }
        //
        int status_code = res.statusCode;
        String description;
        int anlas = -1;
        if (status_code == 200) {
            description = "The request has been accepted and the output is generating";
            Allin1Response subscriptionResponse = this.subscription(request);
            if (subscriptionResponse.statusCode != 200) {
                return subscriptionResponse;
            }
            anlas = subscriptionResponse.anlas;
        } else if (status_code == 401) {
            description = "Access Key is incorrect.";
        } else if (status_code == 501) {
            description = "An unknown error occurred.";
        } else {
            description = "Unknown stats code.";
        }
        return res.setDescription(description).setAnlas(anlas);
    }

    public JSONObject createCharacterPrompts(String ok, String ng, int x, int y) throws JSONException {
        JSONObject target = new JSONObject();
        target.put("prompt",ok);
        target.put("uc",ng);
        target.put("center", createCenter(x, y));
        return target;
    }
    public JSONObject createCenter(int x, int y) throws JSONException {
        JSONObject center = new JSONObject();
        center.put("x", ((double)x)/10.0);
        center.put("y", ((double)y)/10.0);
        return center;
    }
    public JSONObject createCaption(Allin1RequestImage request, boolean isOk) throws JSONException {
        JSONObject target = new JSONObject();
        JSONArray charCaptions = new JSONArray();
        String base = isOk ? request.input : request.negative_prompt;
        boolean ch01Flag = ! request.characters[0].isEmpty();
        boolean ch02Flag = ! request.characters[2].isEmpty();
        String ch01 = isOk ? request.characters[0] : request.characters[1];
        String ch02 = isOk ? request.characters[2] : request.characters[3];
        target.put("base_caption", base);
        target.put("char_captions", charCaptions);
        if (ch01Flag) {
            int x = request.locations[0];
            int y = request.locations[1];
            JSONObject charCaption = new JSONObject();
            charCaption.put("char_caption",ch01);
            JSONArray centers = new JSONArray();
            centers.put(createCenter(x, y));
            charCaption.put("centers",centers);
            //
            charCaptions.put(charCaption);
        }
        if (ch02Flag) {
            int x = request.locations[2];
            int y = request.locations[3];
            JSONObject charCaption = new JSONObject();
            charCaption.put("char_caption",ch02);
            JSONArray centers = new JSONArray();
            centers.put(createCenter(x, y));
            charCaption.put("centers",centers);
            //
            charCaptions.put(charCaption);
        }
        return target;
    }

    /**
     * upscale image
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response upscale(Allin1RequestUpscale request) {
        //
        if (this.requireLogin()) {
            Allin1Response res = login(request);
            if (this.requireLogin()) {
                return res;
            }
        }

        //
        String image = Base64.getEncoder().encodeToString(request.imageBuffer);
        Allin1Response res;
        try {
            JSONObject m = new JSONObject();
            m.put("image", image);
            m.put("width",request.width);
            m.put("height",request.height);
            m.put("scale",request.scale);
            res = this.getConnection(request.type,UPSCALE_URL, m);
        } catch (JSONException e) {
            return new Allin1Response(
                    request.type,
                    null,
                    202,
                    "",
                    e.getClass().getName(),
                    null)
                    .setDescription(e.getMessage());
        }

        String description = getDescriptionString(res);
        return res.setDescription(description).setAnlas(-1);
    }

    @NonNull
    protected String getDescriptionString(Allin1Response res) {
        int status_code = res.statusCode;
        String description;
        if (status_code == 200) {
            description = "The request has been accepted and the output is generating";
        } else if (status_code == 400) {
            description = "A validation error occurred.";
        } else if (status_code == 401) {
            description = "Access Key is incorrect.";
        } else if (status_code == 402) {
            description =
                    "An active subscription is required to access this endpoint.";
        } else if (status_code == 409) {
            description = "A conflict error occurred.";
        } else if (status_code == 500) {
            description = "An unknown error occurred.";
        } else {
            description = "Unknown stats code.";
        }
        return description;
    }

    /**
     * generate-image/suggest-tags
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response suggestTags(Allin1RequestSuggestTags request) {
        //
        if (this.requireLogin()) {
            Allin1Response res = login(request);
            if (this.requireLogin()) {
                return res;
            }
        }
        //
        String model;
        try {
            model = URLEncoder.encode(request.model,"utf-8");
        } catch (UnsupportedEncodingException e) {
            model = request.model;
        }
        String prompt;
        try {
            boolean isJp = containsJapanese(request.prompt);
            prompt = URLEncoder.encode(request.prompt, "utf-8")
                    + (isJp? "&lang=jp": "");
        } catch (UnsupportedEncodingException e) {
            prompt = request.prompt;
        }
        String url = SUGGEST_TAGS_URL + "?model=" + model + "&prompt=" + prompt;
        Allin1Response res;
        try {
            res = this.getConnection(request.type,url, null);
        } catch (JSONException e) {
            return new Allin1Response(
                    request.type,
                    null,
                    202,
                    "",
                    e.getClass().getName(),
                    null)
                    .setDescription(e.getMessage());
        }
        //
        int status_code = res.statusCode;
        String description;
        if (status_code == 200) {
            description = "The request has been accepted and the output is generating";
        } else if (status_code == 401) {
            description = "Access Token is incorrect.";
        } else if (status_code == 500) {
            description = "An unknown error occurred.";
        } else {
            description = "Unknown stats code.";
        }
        return res.setDescription(description);
    }

    public static boolean containsJapanese(String str) {
        for(int i = 0 ; i < str.length() ; i++) {
            char ch = str.charAt(i);
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(ch);

            if (Character.UnicodeBlock.HIRAGANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.KATAKANA.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.equals(unicodeBlock))
                return true;

            if (Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.equals(unicodeBlock))
                return true;
        }
        return false;
    }
    /**
     * subscription
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response subscription(Allin1Request request) {
        //
        if (this.requireLogin()) {
            Allin1Response res = login(request);
            if (this.requireLogin()) {
                return res;
            }
        }
        //
        Allin1Response res;
        try {
            res = this.getConnection(request.type,SUBSCRIPTION_URL,null);
        } catch (JSONException e) {
            return new Allin1Response(
                    request.type,
                    null,
                    202,
                    "",
                    e.getClass().getName(),
                    null)
                    .setDescription(e.getMessage());
        }
        //
        int status_code = res.statusCode;
        String description;
        if (status_code == 200) {
            description = "Current subscription, date of expiry and perks.";
            try {
                JSONObject item = new JSONObject(res.content);
                item = item.getJSONObject("trainingStepsLeft");
                int fixedTrainingStepsLeft;
                try {
                    fixedTrainingStepsLeft = item.getInt("fixedTrainingStepsLeft");
                } catch (JSONException e) {
                    fixedTrainingStepsLeft = 0;
                }
                int purchasedTrainingSteps;
                try {
                    purchasedTrainingSteps = item.getInt("purchasedTrainingSteps");
                } catch (JSONException e) {
                    purchasedTrainingSteps = 0;
                }
                res.setAnlas(fixedTrainingStepsLeft + purchasedTrainingSteps);
            } catch (JSONException e) {
                // NONE
            }
        } else if (status_code == 401) {
            description = "Access Key is incorrect.";
        } else if (status_code == 501) {
            description = "An unknown error occurred.";
        } else {
            description = "Unknown stats code.";
        }
        return res.setDescription(description);
    }

    /**
     * get image extension from mime type
     * @param mimeType mime type
     * @return extension
     */
    public String getImageExt(String mimeType) {
        String ext;
        if (mimeType.contains(IMAGE_PNG)) {
            ext = ".png";
        } else if (mimeType.contains(IMAGE_GIF)) {
            ext = ".gif";
        } else {
            //"image/jpeg" or jpg
            ext = ".jpg";
        }
        return ext;
    }

    /**
     * Used rest api
     * @param type execution type
     * @param targetUrl uri for rest api
     * @param m request body
     * @return all in 1 result
     */
    protected Allin1Response getConnection(REST_TYPE type, String targetUrl, JSONObject m) throws JSONException{
        //
        String requestBody;
        if (m == null) {
            requestBody = null;
        } else {
            requestBody = m.toString(2);
        }
        //
        final String authorization = "Authorization";
        String content = "";
        HttpsURLConnection urlCon = null;
        int statusCode = 0;
        String mime = "";
        byte[] result = null;
        Locale defaultLocale = null;
        InputStream is = null;
        try {
            //
            defaultLocale = Locale.getDefault();
            Locale.setDefault(Locale.US);
            //
            URL url = new URL(targetUrl);
            urlCon = (HttpsURLConnection) url.openConnection();
            urlCon.setReadTimeout(60 * 1000);
            urlCon.setConnectTimeout(60 * 1000);
            if (requestBody != null) {
                urlCon.setRequestMethod("POST");
            } else {
                urlCon.setRequestMethod("GET");
            }
            urlCon.setUseCaches(false);
            urlCon.setInstanceFollowRedirects(true);
            //
            if (authorizationKey != null) {
                urlCon.setRequestProperty(authorization, authorizationKey);
            }
            if (requestBody != null) {
                urlCon.setDoOutput(true);
                urlCon.setRequestProperty("content-type", "application/json");
                byte[] b = requestBody.getBytes();
                urlCon.setFixedLengthStreamingMode(b.length);
                urlCon.getOutputStream().write(b);
                urlCon.getOutputStream().flush();
            } else {
                urlCon.setDoOutput(false);
                urlCon.setFixedLengthStreamingMode(0);
            }
            //
            urlCon.connect();
            //
            statusCode = urlCon.getResponseCode();
            mime = urlCon.getContentType();
            mime = (mime == null) ? "" : mime.toLowerCase();
            String ContentDisposition = urlCon.getHeaderField("Content-Disposition");
            ContentDisposition = (ContentDisposition == null) ? "" : ContentDisposition.toLowerCase();
            try {
                is = urlCon.getInputStream();
            } catch (IOException e) {
                is = urlCon.getErrorStream();
            }
            if (mime.contains("application/json")
                    || mime.contains("text/")
                    || mime.contains("image/")
                    || mime.contains("application/x-zip-compressed")
                    // || mime.contains("binary/octet-stream")
                    || ContentDisposition.contains(".zip")) {
                try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                    while (true) {
                        int len = is.read(bByte);
                        if (len <= 0) {
                            break;
                        }
                        stream.write(bByte, 0, len);
                    }
                    result = stream.toByteArray();
                } catch (IOException e) {
                    result = bByte;
                }
            } else if (mime.contains("binary/octet-stream")){
                result = bByte;
            }
        } catch (IOException e) {
            content = content + "\n"
                    + e.getClass()+ "\n"
                    + e.getMessage() + "\n"
                    + e.getCause();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // NONE
                }
            }
            if (urlCon != null) {
                urlCon.disconnect();
            }
            if (defaultLocale != null) {
                Locale.setDefault(defaultLocale);
            }
        }
        if (result != null) {
            if (mime.contains("application/json")
                    || mime.contains("text/")) {
                //"application/json; charset=utf-8"
                content = new String(result, StandardCharsets.UTF_8);
            } else if (mime.contains("application/x-zip-compressed")
                    || mime.contains("binary/octet-stream")) {
                try (InputStream ois = new ByteArrayInputStream(result);
                     ZipInputStream zipInputStream = new ZipInputStream(ois)) {
                    while (true) {
                        ZipEntry zipEntry = zipInputStream.getNextEntry();
                        if (zipEntry == null) {
                            break;
                        }
                        if (zipEntry.isDirectory()) {
                            continue;
                        }
                        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                            while (true) {
                                int len = zipInputStream.read(bByte);
                                if (len <= 0) {
                                    break;
                                }
                                stream.write(bByte, 0, len);
                            }
                            mime = IMAGE_PNG;
                            result = stream.toByteArray();
                            break;
                        }
                    }
                } catch (IOException e) {
                    result = null;
                }
            }
        }
        return new Allin1Response(type, m, statusCode, mime, content,result);
    }

    /**
     * get accesses key
     * @param email email
     * @param password password
     * @return accesses key
     */
    public String getAKey(String email,String password) {
        final int size = 64;
        final String domain = "novelai_data_access_key";
        String shortPassword;
        if (6 <= password.length()) {
            shortPassword = password.substring(0,6);
        } else {
            shortPassword = password;
        }
        String pre_salt = shortPassword + email + domain;
        byte[] b_pre_salt = pre_salt.getBytes(Charset.defaultCharset());
        final int digestSize = 16;
        byte[] salt = new byte[digestSize];
        Blake2b blake2b = new Blake2b(
                null,
                digestSize,
                null,
                null);
        blake2b.update(b_pre_salt,0,b_pre_salt.length);
        blake2b.digest(salt,0);
        final int iterations = 2;
        final int memory = 2000000 / 1024;
        final int parallelism = 1;

        byte[] hash = new byte[size];
        Argon2Parameters parameters = new Argon2Parameters
                .Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withParallelism(parallelism)
                .withMemoryAsKB(memory)
                .withIterations(iterations)
                .build();
        // @formatter:on
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(parameters);
        generator.generateBytes(password.toCharArray(), hash);
        final String hashed = Base64.getUrlEncoder().encodeToString(hash);

        // System.out.println(hashed);
        /*
        // System.out.println("email= " + email);
        // System.out.println("password= " + password);
        // System.out.println("site= " + size);
        // System.out.println("domain= " + domain);
        // System.out.println("pre_salt= " + pre_salt);
        // System.out.println("b_pre_salt= 0x" + this.getByteToString(b_pre_salt));
        // System.out.println("salt_hex= 0x" + this.getByteToString(salt));
        // System.out.println("raw= 0x" + this.getByteToString(raw));
         */
        return hashed.substring(0,64);
    }
}
