package jp.ne.ruru.park.ando.naiview;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Example local unit test, which will execute on the development machine (host).
 * test case for Novel AI support interface
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class MyNASITest {

    /**
     * token test
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