package org.ethereum.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeConsumingTracer {

    public static Map<String, long[]> timerInterval = new HashMap<String, long[]>();
    public static Map<String, Map<String, Integer>> distributor = new HashMap<String, Map<String, Integer>>();
    public static Map<String, long[]> timerRecord = new HashMap<String, long[]>();
    public static Map<String, Integer> timerCounter = new HashMap<String, Integer>();
    public static List<String> resetTimer = new ArrayList<String>();

    public static int maxTimersPerTag = 1000;

    public static void record(String tag) {

        long currentTimeMillis = System.currentTimeMillis();

        StringBuffer identification = new StringBuffer();
        identification.append(new Exception().getStackTrace()[1].getClassName());//获取调用者的类名
        identification.append(".");
        identification.append(new Exception().getStackTrace()[1].getMethodName());//获取调用者的方法名
        identification.append(":");
        identification.append(new Exception().getStackTrace()[1].getLineNumber());

        int timerRecordPosition = 0;
        int tracePointsLength = 0;

        if (distributor.containsKey(tag)) {
            Map<String, Integer> tracePoints = distributor.get(tag);
            if (tracePoints.containsKey(identification.toString())) {
                timerRecordPosition = tracePoints.get(identification.toString());

            } else {
                if (tracePoints.size() == maxTimersPerTag) return;
                timerRecordPosition = tracePoints.size();
                tracePoints.put(identification.toString(), timerRecordPosition);
                distributor.put(tag, tracePoints);
            }

            tracePointsLength = tracePoints.size();
        } else {
            Map<String, Integer> tracePoints = new HashMap<>();
            tracePoints.put(identification.toString(), timerRecordPosition);
            distributor.put(tag, tracePoints);
            timerInterval.put(tag, new long[maxTimersPerTag]);
            timerRecord.put(tag, new long[maxTimersPerTag]);
            timerCounter.put(tag, 0);
        }

        if (timerRecordPosition == 0) {
            if (resetTimer.contains(tag)) {
                System.out.println("Reset timer counter " + tag);
                resetTimer.remove(tag);
                resetTimer(tag);
            } else {
                if (tracePointsLength > 1) {
                    long[] record = timerRecord.get(tag);
                    long[] interval = timerInterval.get(tag);
                    int counter = timerCounter.get(tag);

                    for (int i = 1; i < tracePointsLength; i++) {
                        long subInterval = record[i] - record[i - 1];
                        long totalInterval = interval[i - 1] + subInterval;
                        interval[i - 1] = totalInterval;
                        printCurrentTimer(tag, counter, i, subInterval, totalInterval);
                    }
                    timerInterval.put(tag, interval);
                }
            }
            timerCounter.put(tag, timerCounter.get(tag) + 1);
        }

        long[] saved = timerRecord.get(tag);
        saved[timerRecordPosition] = currentTimeMillis;
        timerRecord.put(tag, saved);
    }

    public static void printCurrentTimer(String tag, int loop, int endTimeIndex, long subInterval, long totalInterval) {

        float average = Math.round(totalInterval * 100) / loop / 100;

        StringBuffer sb = new StringBuffer();
        sb.append(loop)
                .append("@")
                .append(tag)
                .append(" T")
                .append(endTimeIndex - 1)
                .append("->T")
                .append(endTimeIndex)
                .append(" takes ")
                .append(subInterval)
                .append(" ms and total cost is ")
                .append(totalInterval)
                .append(" ms")
                .append(" and average is ")
                .append(average)
                .append(" ms@loop");

        System.out.println(sb);
    }

    public static void resetTimerCounter(String tag) {
        resetTimer.add(tag);
    }

    private static void resetTimer(String tag) {
        if (timerRecord.containsKey(tag)) {
            timerInterval.put(tag, new long[maxTimersPerTag]);
            timerCounter.put(tag, 0);
        }
    }

}
