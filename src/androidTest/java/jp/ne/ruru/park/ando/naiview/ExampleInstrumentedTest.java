package jp.ne.ruru.park.ando.naiview;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


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
     * test load
     */
    @Test
    public void load() {
        Context context = appContext.getApplicationContext();
        MyApplication application = (MyApplication) context;
        application.load(context, null,null);
        System.out.println("== log ====");
        System.out.println(application.getLog());
        System.out.println("===========");
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
}