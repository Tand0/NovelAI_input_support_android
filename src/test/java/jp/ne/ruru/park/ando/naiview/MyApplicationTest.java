package jp.ne.ruru.park.ando.naiview;


import org.json.JSONArray;
import org.junit.Test;

import org.junit.Assert;

import jp.ne.ruru.park.ando.naiview.data.PromptType;

/** test case for MyApplication
 * @author T.Ando
 */
public class MyApplicationTest {

    /**
     * load test
     */
    @Test
    public void loadImage() {
        MyApplication a = new MyApplication();
        a.load(a.getApplicationContext(),null,null);
        JSONArray array = a.getTop();
        String result = array.toString();
        System.out.println(result);
        Assert.assertNotNull(result);
        //
        //
        result = a.fromTree(PromptType.P_BASE_OK, a.getTop());
        Assert.assertEquals(result,"prompt test, aaa");
        result = a.fromTree(PromptType.P_BASE_NG, a.getTop());
        Assert.assertEquals(result,"uc test");
        //
        result = a.createName("[[aaa:1.6]]");
        Assert.assertEquals(result,"{{{{{aaa}}}}}");
        result = a.createName("{{{   aaa}}}");
        Assert.assertEquals(result,"{{{aaa}}}");
        //
        int pos;
        pos = a.getEnhancePos("[[aa");
        Assert.assertEquals(pos,-2);
        pos = a.getEnhancePos("[aa");
        Assert.assertEquals(pos,-1);
        pos = a.getEnhancePos("aa");
        Assert.assertEquals(pos,0);
        pos = a.getEnhancePos("{aa");
        Assert.assertEquals(pos,1);
        pos = a.getEnhancePos("{{aa");
        Assert.assertEquals(pos,2);
        //
        result = a.getEnhanceText("[[aa",-2);
        Assert.assertEquals(result,"[[aa]]");
        result = a.getEnhanceText("[[aa",-1);
        Assert.assertEquals(result,"[aa]");
        result = a.getEnhanceText("[[aa",0);
        Assert.assertEquals(result,"aa");
        result = a.getEnhanceText("[[aa",1);
        Assert.assertEquals(result,"{aa}");
        result = a.getEnhanceText("[[aa",2);
        Assert.assertEquals(result,"{{aa}}");
    }
}
