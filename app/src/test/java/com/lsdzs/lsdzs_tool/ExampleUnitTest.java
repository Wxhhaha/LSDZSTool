package com.lsdzs.lsdzs_tool;

import org.junit.Test;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
        byte[] a = "0020".getBytes();
        int iResult = (a[0] * (255 - a[1]) * a[2] * a[3]) % 1000000;
        DecimalFormat df = new DecimalFormat();
        df.applyPattern("000000");
        System.out.println(df.format(iResult));
    }
}