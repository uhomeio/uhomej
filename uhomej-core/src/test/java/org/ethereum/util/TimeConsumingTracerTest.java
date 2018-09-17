package org.ethereum.util;

import org.junit.Test;

public class TimeConsumingTracerTest {

    @Test
    public void recordTest() throws InterruptedException {

        String tag = "current";

        for (int i = 0; i < 20; i++) {
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            addTimer(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);


        }

    }

    @Test
    public void recordTest2() throws InterruptedException {

        String tag = "current";

        for (int i = 0; i < 20; i++) {
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            if (i > 0 && i % 15 == 0) {
                TimeConsumingTracer.resetTimerCounter(tag);
            }
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
        }
    }


    @Test
    public void recordTest3() throws InterruptedException {

        String tagHeader = "current";

        for (int i = 0; i < 20; i++) {
            String tag = tagHeader + "go";
            if (i % 2 == 0) tag = tagHeader + "xs";

            TimeConsumingTracer.record(tag);
            Thread.sleep(20);

            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            addTimer(tag);
        }
    }

    @Test
    public void recordTest4() throws InterruptedException {

        String tagHeader = "current";

        for (int i = 0; i < 60; i++) {
            String tag = tagHeader + "go";
            if (i % 2 == 0) tag = tagHeader + "xs";

            TimeConsumingTracer.record(tag);
            Thread.sleep(20);

            if (i > 0 && i % 15 == 0) {
                TimeConsumingTracer.resetTimerCounter(tag);
            }
            TimeConsumingTracer.record(tag);
            Thread.sleep(20);
            addTimer(tag);
        }
    }


    public void addTimer(String tag) throws InterruptedException {
        TimeConsumingTracer.record(tag);
        Thread.sleep(20);
        TimeConsumingTracer.record(tag);
        Thread.sleep(20);
        TimeConsumingTracer.record(tag);
        Thread.sleep(20);
        TimeConsumingTracer.record(tag);
        Thread.sleep(20);
    }


}
