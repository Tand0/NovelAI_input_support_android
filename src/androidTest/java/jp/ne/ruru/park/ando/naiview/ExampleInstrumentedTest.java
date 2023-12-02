package jp.ne.ruru.park.ando.naiview;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.rfksystems.blake2b.Blake2b;

import java.nio.charset.Charset;
import java.util.Base64;


/**
 * Instrumented test, which will execute on an Android device.
 * @author foobar@em.boo.jp
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    /** activity */
    private static Context appContext;

    /**
     * set activity
     */
    @Before
    public void before() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        System.out.println(appContext.getClass().getName());
    }

    /**
     * test for useAppContext
     */
    @Test
    public void useAppContext() {
        // Context of the app under test.
        System.out.println(appContext.getPackageName());
        assertEquals("jp.ne.ruru.park.ando.naiview", appContext.getPackageName());
    }

    /**
     * test token.
     * Check if external library works in android
     */
    @Test
    public void tokenOk() {
        String email = "xxxx@yyyy.zzz";
        String password = "hogehero";
        MyNASI my = new MyNASI();
        String aKey = my.getAKey(email,password);
        assertEquals(aKey,"vtQmbCKY3JMbibT54ctxmAaAL-nZOK5BLVMSmF4M75yasKIKKPconLnx_L0ybh3P");
    }

    @Test
    public void tokenOk2() {
        String email = "xxxx@yyyy.zzz";
        String password = "hogehero";
        String aKey = this.getAKey(email,password);
        assertEquals(aKey,"vtQmbCKY3JMbibT54ctxmAaAL-nZOK5BLVMSmF4M75yasKIKKPconLnx_L0ybh3P");
    }
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

        System.out.println(hashed);
        System.out.println("email= " + email);
        System.out.println("password= " + password);
        System.out.println("site= " + size);
        System.out.println("domain= " + domain);
        System.out.println("pre_salt= " + pre_salt);
        System.out.println("b_pre_salt= 0x" + b_pre_salt.toString());
        System.out.println("salt_hex= 0x" + salt.toString());
        return hashed.substring(0,64);
    }
}