package jp.ne.ruru.park.ando.naiview;


import org.json.JSONArray;
import org.junit.Test;

import org.junit.Assert;

/** test case for MyApplication
 * @author foobar@em.boo.jp
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
        a.ignoreData(true);
        a.ignoreData(false);
        System.out.println(a.getTop().toString());
        a.createData(a.getPrompt(),true);
        a.createData(a.getUc(),false);
        //
        array = new JSONArray(); // clear
        a.setTop(array);
        a.setPrompt("prompt test,  aaa");
        a.setUc("uc test");
        a.ignoreData(true);
        a.ignoreData(false);
        a.createData(a.getPrompt(),true);
        a.createData(a.getUc(),false);
        //
        Assert.assertEquals(2,a.getTop().length());
        //
        result = a.fromTree(true);
        Assert.assertEquals(result,"prompt test, aaa");
        result = a.fromTree(false);
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
