package jp.ne.ruru.park.ando.naiview;


import com.rfksystems.blake2b.Blake2b;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
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
 * @author foobar@em.boo.jp
 */
public class MyNASI {
    /**
     * default prompt
     */
    public static final String DEFAULT_PROMPT= "1girl, best quality, amazing quality, very aesthetic, absurdres";

    /** mime type gif */
    public static final String IMAGE_PNG = "image/png";
    /** mime type png */
    public static final String IMAGE_GIF = "image/gif";

    /** data area */
    protected final byte[] bByte = new byte[1024*8];

    /**
     * state machine
     */
    protected enum TYPE {
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
                TYPE type,
                String email,
                String password) {
            this.type = type;
            this.email = email;
            this.password = password;
        }

        public final TYPE type;
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
         * @param type            execution type
         * @param email           mail address (for login)
         * @param password        password (for login)
         * @param model           model
         * @param input           prompt
         * @param width           width
         * @param height          height
         * @param scale           scale
         * @param steps           steps
         * @param uncodeScale     uncodeScale
         * @param sampler         sampler
         * @param sm              sm
         * @param sm_dyn          sm_dyn
         * @param negative_prompt uc
         * @param seed            seed
         * @param noise_schedule  noise schedule
         * @param imageBuffer     base image for image2image
         * @param strength        strength for image2image
         * @param noise           noise for image2image
         */
        Allin1RequestImage(
                TYPE type,
                String email,
                String password,
                String model,
                String input,
                int width,
                int height,
                int scale,
                int steps,
                int uncodeScale,
                String sampler,
                boolean sm,
                boolean sm_dyn,
                String negative_prompt,
                int seed,
                String noise_schedule,
                byte[] imageBuffer,
                int strength,
                int noise
        ) {
            super(type,email,password);
            this.model = model;
            this.input = input;
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.steps = steps;
            this.uncodeScale = uncodeScale;
            this.sampler = sampler;
            this.sm = sm;
            this.sm_dyn = sm_dyn;
            this.negative_prompt = negative_prompt;
            this.seed = seed;
            this.noise_schedule = noise_schedule;
            this.imageBuffer = imageBuffer;
            this.strength = strength;
            this.noise = noise;
        }
        public final String model;
        public final String input;

        public final int width;

        public final int height;
        public final int scale;
        public final int steps;
        public final int uncodeScale;
        public final String sampler;
        public final boolean sm;
        public final boolean sm_dyn;
        public final String negative_prompt;

        public final int seed;

        public final String noise_schedule;

        public final byte[] imageBuffer;

        public final int strength;

        public final int noise;
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
                TYPE type,
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
                TYPE type,
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
                TYPE type,
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
        public final TYPE type;
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
    public final String IMAGE_URL = "https://api.novelai.net/ai/generate-image";

    /** upscale url for rest api */
    public final String UPSCALE_URL = "https://api.novelai.net/ai/upscale";
    /** suggest tags url for rest api */
    public final String SUGGEST_TAGS_URL = "https://api.novelai.net/ai/generate-image/suggest-tags";

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
                    TYPE.LOGIN,LOGIN_URL,m);
        } catch (JSONException e) {
            return new Allin1Response(
                    TYPE.LOGIN,
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
            String accessToken;
            if (res.content == null) {
                accessToken = null;
            } else {
                String authorizationHeader = "Bearer ";
                String accessToken1 = "accessToken";
                try {
                    JSONObject jsonObject = new JSONObject(res.content);
                    accessToken = jsonObject.getString(accessToken1);
                    accessToken = authorizationHeader + accessToken;
                } catch (JSONException e) {
                    accessToken = null;
                }
            }
            authorizationKey = accessToken;
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
            JSONObject p = new JSONObject();
            p.put("width",request.width);
            p.put("height",request.height);
            p.put("scale",request.scale);
            p.put("sampler",request.sampler);
            p.put("steps",request.steps);
            p.put("n_samples",1);
            p.put("qualityToggle",false);
            p.put("sm",request.sm);
            p.put("sm_dyn",request.sm_dyn);
            p.put("dynamic_thresholding",false);
            p.put("controlnet_strength",1);
            p.put("legacy",false);
            p.put("uncode_scale",((double)request.uncodeScale)/100.0);
            p.put("seed",request.seed);
            p.put("negative_prompt",request.negative_prompt);
            p.put("noise_schedule",request.noise_schedule);
            if (request.imageBuffer != null) {
                // for image2image
                String image = Base64.getEncoder().encodeToString(request.imageBuffer);
                p.put("image", image);
                p.put("strength",((double) request.strength) / 100.0);
                p.put("noise", ((double) request.noise) / 100.0);
                p.put("ucPreset", 0);
                p.put("add_original_image", false);
                p.put("cfg_rescale", 0.0);
                p.put("legacy_v3_extend", false);
                p.put("params_version", 1);
                p.put("extra_noise_seed",request.seed);
            } else {
                p.put("ucPreset", 2);
            }
            //
            String input = DEFAULT_PROMPT;
            if ((request.input != null) && (!request.input.equals(""))) {
                input = request.input;
            }
            JSONObject m = new JSONObject();
            m.put("input",input);
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
        //
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
        return res.setDescription(description).setAnlas(-1);
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
        String url = SUGGEST_TAGS_URL;
        try {
            url = url + "?model=" + URLEncoder.encode(request.model,"utf-8");
        } catch (UnsupportedEncodingException e) {
            url = url + "?model=" + request.model;
        }
        try {
            url = url + "&prompt=" + URLEncoder.encode(request.prompt,"utf-8");
        } catch (UnsupportedEncodingException e) {
            url = url + "&prompt=" + request.prompt;
        }
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
    protected Allin1Response getConnection(TYPE type, String targetUrl, JSONObject m) throws JSONException{
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
            if (mime == null) {
                mime = "";
            }
            mime = mime.toLowerCase();
                try {
                    is = urlCon.getInputStream();
                } catch (IOException e) {
                    is = urlCon.getErrorStream();
                }
                if (mime.contains("application/json")
                        || mime.contains("text/")
                        || mime.contains("image/")
                        || mime.contains("application/x-zip-compressed")) {
                    try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                        while(true) {
                            int len = is.read(bByte);
                            if (len <= 0) {
                                break;
                            }
                            stream.write(bByte,0,len);
                        }
                        result = stream.toByteArray();
                    }
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
            } else if (mime.contains("application/x-zip-compressed")) {
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
