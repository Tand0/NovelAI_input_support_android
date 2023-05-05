package jp.ne.ruru.park.ando.naiview;


import com.rfksystems.blake2b.Blake2b;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
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

    /** state machine */
    protected enum TYPE {
        LOGIN,
        IMAGE,
        SUBSCRIPTION
    }

    /**
     * all in 1 request data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1Request {
        /**
         * this is constructor
         * @param type execution type
         * @param email mail address (for login)
         * @param password password (for login)
         * @param model model
         * @param input prompt
         * @param width width
         * @param height height
         * @param scale scale
         * @param steps steps
         * @param sampler sampler
         * @param sm sm
         * @param sm_dyn sm_dyn
         * @param negative_prompt uc
         */
        Allin1Request(
                TYPE type,
                String email,
                String password,
                String model,
                String input,
                int width,
                int height,
                int scale,
                int steps,
                String sampler,
                boolean sm,
                boolean sm_dyn,
                String negative_prompt) {
            this.type = type;
            this.email = email;
            this.password = password;
            this.model = model;
            this.input = input;
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.steps = steps;
            this.sampler = sampler;
            this.sm = sm;
            this.sm_dyn = sm_dyn;
            this.negative_prompt = negative_prompt;
        }
        public final TYPE type;
        public final String email;
        public final String password;
        public final String model;
        public final String input;
        public final int width;
        public final int height;
        public final int scale;
        public final int steps;
        public final String sampler;
        public final boolean sm;
        public final boolean sm_dyn;
        public final String negative_prompt;
    }

    /**
     * all in 1 result data.
     * To make it thread-safe, I've wrapped up the data
     */
    public static class Allin1Response {
        /** this is constructor
         * @param type execution type
         * @param requestBody request body (for log)
         * @param statusCode status code (for callback)
         * @param mimeType mime type (for image activity)
         * @param content result body (for log)
         * @param imageData image data (for image activity)
         */
        Allin1Response(
                TYPE type,
                String requestBody,
                int statusCode,
                String mimeType,
                String content,
                byte[] imageData) {
            this.type = type;
            this.statusCode = statusCode;
            this.mimeType = mimeType;
            this.content = content;
            this.requestBody = requestBody;
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
        public final String requestBody;
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
        JSONObject object = new JSONObject();
        String requestBody;
        try {
            object.put("key", aKey);
            requestBody = object.toString();
        } catch (JSONException e) {
            return new Allin1Response(
                    TYPE.LOGIN,
                    "",
                    202,
                    "",
                    e.getMessage(),
                    null)
                    .setDescription("Login fail");
        }
        //
        Allin1Response res = getConnection(
                TYPE.LOGIN,LOGIN_URL,requestBody);
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
        res.setDescription(description);
        return res;
    }

    /**
     * download image
     * @param request all in 1 request
     * @return all in 1 result
     */
    public Allin1Response downloadImage(Allin1Request request) {
        //
        if (this.requireLogin()) {
            Allin1Response res = login(request);
            if (this.requireLogin()) {
                return res;
            }
        }
        Allin1Response res = this.subscription(request);
        if (res.statusCode != 200) {
            return res;
        }
        int anlas = res.anlas;
        //
        JSONObject m = new JSONObject();
        String requestBody;
        try {
            JSONObject p = new JSONObject();
            p.put("width",request.width);
            p.put("height",request.height);
            p.put("scale",request.scale);
            p.put("sampler",request.sampler);
            p.put("steps",request.steps);
            p.put("n_samples",1);
            p.put("ucPreset",2);
            p.put("qualityToggle",false);
            p.put("sm",request.sm);
            p.put("sm_dyn",request.sm_dyn);
            p.put("dynamic_thresholding",false);
            p.put("controlnet_strength",1);
            p.put("legacy",false);
            p.put("seed",(new java.util.Random().nextInt(Integer.MAX_VALUE - 1) + 1));
            p.put("negative_prompt",request.negative_prompt);
            //
            String input = "girl";
            if ((request.input != null) && (!request.input.equals(""))) {
                input = request.input;
            }
            m.put("input",input);
            m.put("model",request.model);
            m.put("action","generate");
            m.put("parameters",p);
            //
            requestBody = m.toString(2);
            //
        } catch (JSONException e) {
            return new Allin1Response(
                    TYPE.IMAGE,
                    "",
                    202,
                    "",
                    e.getMessage(),
                    null)
                    .setDescription("request fail");
        }
        res = this.getConnection(request.type,IMAGE_URL,requestBody)
                .setAnlas(anlas);
        //
        int status_code = res.statusCode;
        if (status_code == 200) {
            res.setDescription("The request has been accepted and the output is generating");
        } else if (status_code == 401) {
            res.setDescription("Access Key is incorrect.");
        } else if (status_code == 501) {
            res.setDescription("An unknown error occurred.");
        } else {
            res.setDescription("Unknown stats code.");
        }
        return res;
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
        Allin1Response res = this.getConnection(request.type,SUBSCRIPTION_URL,null);
        //
        int status_code = res.statusCode;
        if (status_code == 200) {
            res.setDescription("Current subscription, date of expiry and perks.");
            try {
                JSONObject item = new JSONObject(res.content);
                item = item.getJSONObject("trainingStepsLeft");
                int fixedTrainingStepsLeft = item.getInt("fixedTrainingStepsLeft");
                res.setAnlas(fixedTrainingStepsLeft);
            } catch (JSONException e) {
                // NONE
            }
        } else if (status_code == 401) {
            res.setDescription("Access Key is incorrect.");
        } else if (status_code == 501) {
            res.setDescription("An unknown error occurred.");
        } else {
            res.setDescription("Unknown stats code.");
        }
        return res;
    }

    /**
     * get image extension from mime type
     * @param mimeType mime type
     * @return extension
     */
    public String getImageExt(String mimeType) {
        String ext;
        if (mimeType.contains("image/png")) {
            ext = ".png";
        } else if (mimeType.contains("image/gif")) {
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
     * @param requestBody request body
     * @return all in 1 result
     */
    protected Allin1Response getConnection(TYPE type,String targetUrl,String requestBody) {
        final String authorization = "Authorization";
        String content = "";
        HttpsURLConnection urlCon = null;
        int statusCode = 0;
        String mime = "";
        byte[] result = null;
        Locale defaultLocale = null;
        try {
            //
            defaultLocale = Locale.getDefault();
            Locale.setDefault(Locale.US);
            //
            URL url = new URL(targetUrl);
            urlCon = (HttpsURLConnection) url.openConnection();
            urlCon.setReadTimeout(10000);
            urlCon.setConnectTimeout(20000);
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
            } else {
                mime = mime.toLowerCase();
                if (mime.contains("application/json")) {
                    //"application/json; charset=utf-8"
                    try (InputStream is = urlCon.getInputStream();
                         InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                         BufferedReader bufferedReader = new BufferedReader(isr)) {
                        StringBuilder buff = new StringBuilder();
                        String data;
                        while ((data = bufferedReader.readLine()) != null) {
                            buff.append(data);
                            buff.append("\n");
                        }
                        //
                        content = buff.toString();
                        //
                    }
                } else if (mime.contains("image/")) {
                    byte[] bByte = new byte[1024];
                    try (InputStream is = urlCon.getInputStream();
                         ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                        while(true) {
                            int len = is.read(bByte);
                            if (len <= 0) {
                                break;
                            }
                            stream.write(bByte,0,len);
                        }
                        result = stream.toByteArray();
                    }
                } else if (mime.contains("application/x-zip-compressed")) {
                    try (InputStream is = urlCon.getInputStream();
                         ZipInputStream zipInputStream = new ZipInputStream(is)) {
                        while (true) {
                            ZipEntry zipEntry = zipInputStream.getNextEntry();
                            if (zipEntry == null) {
                                break;
                            }
                            if (zipEntry.isDirectory()) {
                                continue;
                            }
                            byte[] bByte = new byte[1024];
                            try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                                while (true) {
                                    int len = zipInputStream.read(bByte);
                                    if (len <= 0) {
                                        break;
                                    }
                                    stream.write(bByte, 0, len);
                                }
                                mime = "image/png";
                                result = stream.toByteArray();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            content = content + "\n"
                    + e.getClass()+ "\n"
                    + e.getMessage() + "\n"
                    + e.getCause();
        } finally {
            if (urlCon != null) {
                urlCon.disconnect();
            }
            if (defaultLocale != null) {
                Locale.setDefault(defaultLocale);
            }
        }
        return new Allin1Response(type, requestBody, statusCode, mime, content,result);
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
