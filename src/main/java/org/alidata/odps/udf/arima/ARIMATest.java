package org.alidata.odps.udf.arima;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ARIMATest {
    private final static Log LOG = LogFactory.getLog(ARIMATest.class);

    public static void main(String args[]) {

        File file = new File(System.getProperty("user.dir") + "/data/train.data");
        BufferedReader reader = null;
        SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
        Date stime = null;
        Date etime = null;
        Date ctime = null;


        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            double diff = 0.0;
            while ((tempString = reader.readLine()) != null) {

                try {
                    stime = dt.parse("201407");
                    etime = dt.parse("201707");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String[] data = tempString.split(";");
                LOG.info("line " + line + ": " + tempString);
                double[] allData = new double[37];
                double[] trainData = new double[36];
                double target = 0;

                int idx = 0;
                for (int i = 0; i < data.length; i++) {
                    String[] tt = data[i].split("_");
                    if (tt.length != 2) {
                        continue;
                    }

                    try {
                        ctime = dt.parse(tt[0]);
//                        LOG.info(data[i]);
//                        LOG.info("ctime:" + ctime.toString() + " stime:" + stime.toString());
                        while (ctime.getTime() > stime.getTime()) {
                            allData[idx] = 0;
//                            LOG.info(idx + ":" + allData[idx]);
                            idx++;
                            Calendar rightNow = Calendar.getInstance();
                            rightNow.setTime(stime);
                            rightNow.add(Calendar.MONTH, 1);
                            stime = rightNow.getTime();
//                            LOG.info("ctime:" + ctime.toString() + " stime:" + stime.toString());
                        }
                        double gmv = Double.parseDouble(tt[1]);
                        allData[idx] = gmv;
//                        LOG.info(idx + ":" + allData[idx]);
                        idx++;
                        Calendar rightNow = Calendar.getInstance();
                        rightNow.setTime(ctime);
                        rightNow.add(Calendar.MONTH, 1);
                        stime = rightNow.getTime();
//                        LOG.info("ctime:" + ctime.toString() + " stime:" + stime.toString());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                while (ctime.getTime() < etime.getTime()) {
                    allData[idx] = 0;
                    idx++;
                    Calendar rightNow = Calendar.getInstance();
                    rightNow.setTime(ctime);
                    rightNow.add(Calendar.MONTH, 1);
                    ctime = rightNow.getTime();
                }

                for (int i = 0; i < 36; i++) {
                    trainData[i] = allData[i];
                    LOG.info(i + "=" + trainData[i]);
                }
                target = allData[36];
                LOG.info("target=" + target);

                ARIMAModel arima = new ARIMAModel(trainData);
                int period = 0;
                int[] bestModel = arima.getARIMAModel(period);
                double predictValue = 0.0;
                if (bestModel.length == 0) {
                    predictValue = trainData[trainData.length - period];
                } else {
                    int predictDiff = arima.predictValue(bestModel[0], bestModel[1], period);
                    predictValue = arima.aftDeal(predictDiff, period);
                    LOG.info("BestModel is " + bestModel[0] + " " + bestModel[1]);
                }


                if (target > 0) {
                    LOG.info("Predict value=" + predictValue + " Target value=" + target + " Predict error=" + (predictValue - target) / target);
                    diff += Math.abs((predictValue - target) / target);
                    line++;
                }
            }
            reader.close();
            LOG.info("total diff =" + diff / line + "  total line =" + line);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }


    }

}
