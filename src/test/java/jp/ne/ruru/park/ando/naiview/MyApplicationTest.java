package jp.ne.ruru.park.ando.naiview;


import org.json.JSONArray;
import org.junit.Test;

import org.junit.Assert;

import java.io.File;
public class MyApplicationTest {
    @Test
    public void loadImage() {
        MyApplication application = new MyApplication();
        application.load(application.getApplicationContext(),null,null);
        JSONArray array = application.getTop();
        String result = array.toString();
        System.out.println(result);
        Assert.assertNotNull(result);
        //
        //
        application.ignoreData(true);
        application.ignoreData(false);
        System.out.println(application.getTop().toString());
        application.createData(application.getPrompt(),true);
        application.createData(application.getUc(),false);
        //
        array = new JSONArray(); // clear
        application.setTop(array);
        application.setPrompt("prompt test,  aaa");
        application.setUc("uc test");
        application.ignoreData(true);
        application.ignoreData(false);
        application.createData(application.getPrompt(),true);
        application.createData(application.getUc(),false);
        //
        Assert.assertEquals(2,application.getTop().length());
        //
        result = application.fromTree(true);
        Assert.assertEquals(result,"prompt test, aaa");
        result = application.fromTree(false);
        Assert.assertEquals(result,"uc test");
        //
        result = application.createName("[[aaa:1.6]]");
        Assert.assertEquals(result,"{{{{{aaa}}}}}");
        result = application.createName("{{{   aaa}}}");
        Assert.assertEquals(result,"{{{aaa}}}");
        //
        int pos;
        pos = application.getEnhancePos("[[aa");
        Assert.assertEquals(pos,-2);
        pos = application.getEnhancePos("[aa");
        Assert.assertEquals(pos,-1);
        pos = application.getEnhancePos("aa");
        Assert.assertEquals(pos,0);
        pos = application.getEnhancePos("{aa");
        Assert.assertEquals(pos,1);
        pos = application.getEnhancePos("{{aa");
        Assert.assertEquals(pos,2);
        //
        result = application.getEnhanceText("[[aa",-2);
        Assert.assertEquals(result,"[[aa]]");
        result = application.getEnhanceText("[[aa",-1);
        Assert.assertEquals(result,"[aa]");
        result = application.getEnhanceText("[[aa",0);
        Assert.assertEquals(result,"aa");
        result = application.getEnhanceText("[[aa",1);
        Assert.assertEquals(result,"{aa}");
        result = application.getEnhanceText("[[aa",2);
        Assert.assertEquals(result,"{{aa}}");



    }



}
