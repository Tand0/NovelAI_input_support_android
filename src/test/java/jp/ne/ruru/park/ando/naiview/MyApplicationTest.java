package jp.ne.ruru.park.ando.naiview;


import org.junit.Test;

import org.junit.Assert;


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
        //
        working(a,"1.00::aa::" , 0, "aa");
        working(a,"  aa " , 0, "aa");
        working(a,"{aa" , 1, "1.05::aa ::");
        working(a,"{{aa" , 2, "1.10::aa ::");
        working(a,"1.01::aa::" , 0, "aa");
        working(a,"1.05::aa::" , 1, "1.05::aa ::");
        working(a,"1.10::aa::" , 2, "1.10::aa ::");
        working(a,"1.11::aa::" , 2, "1.10::aa ::");
        working(a,"1.16::aa::" , 3, "1.16::aa ::");
        working(a,"1.22::aa::" , 4, "1.22::aa ::");
        working(a,"1.28::aa::" , 5, "1.28::aa ::");
        working(a,"1.34::aa::" , 6, "1.34::aa ::");
        working(a,"1.41::aa::" , 7, "1.41::aa ::");
        working(a,"1.48::aa::" , 8, "1.48::aa ::");
        working(a,"1.55::aa::" , 9, "1.55::aa ::");
        working(a,"1.63::aa::" , 10, "1.63::aa ::");
        working(a,"1.71::aa::" , 11, "1.71::aa ::");
        working(a,"1.80::aa::" , 12, "1.80::aa ::");
        working(a,"1.89::aa::" , 13, "1.89::aa ::");
        working(a,"1.98::aa::" , 14, "1.98::aa ::");
        working(a,"2.08::aa::" , 15, "2.08::aa ::");
        working(a,"2.18::aa::" , 16, "2.18::aa ::");
        working(a,"2.29::aa::" , 17, "2.29::aa ::");
        working(a,"2.41::aa::" , 18, "2.41::aa ::");
        working(a,"2.53::aa::" , 19, "2.53::aa ::");
        working(a,"2.65::aa::" , 20, "2.65::aa ::");
        working(a,"10::aa::" , 20, "2.65::aa ::");
        working(a,"[aa]" , -1, "0.95::aa ::");
        working(a,"[[aa" , -2, "0.91::aa ::");
        working(a,"0.95::aa::" , -1, "0.95::aa ::");
        working(a,"0.91::aa::" , -2, "0.91::aa ::");
        working(a,"0.38::aa::" , -20, "0.38::aa ::");
        working(a,"0.00::aa::" , -20, "0.38::aa ::");
    }

    protected void working(MyApplication a,String target,int posTarget, String resultTarget) {
        int pos = a.getEnhancePos(target);
        Assert.assertEquals("assert pos t=" + target, posTarget, pos);
        String result = a.getEnhanceText(target,pos);
        Assert.assertEquals("assert text t=" + target, resultTarget, result);
    }
}
