package com.github.tand0.park.ando.naiview;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;



/**
 * Instrumented test, which will execute on an Android device.
 * @author T.Ando
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
        assertEquals("com.github.tand0.naiview", appContext.getPackageName());
    }
}