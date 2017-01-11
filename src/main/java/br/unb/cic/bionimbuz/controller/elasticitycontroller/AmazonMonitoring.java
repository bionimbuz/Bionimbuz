/*
 * Copyright (C) 2016 guilherme
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.unb.cic.bionimbuz.controller.elasticitycontroller;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 *
 * @author guilherme
 */
public class AmazonMonitoring{

    final String awsAccessKey = "";
    final String awsSecretKey = "";
    //final String instanceId = "i-0364f4b725eeaf44e";

    //final AmazonCloudWatchClient client = client(awsAccessKey, awsSecretKey);
    //final GetMetricStatisticsRequest request = request(instanceId); 
    //final GetMetricStatisticsResult result = result(client, request);
    //toStdOut(result, instanceId);   
    public ArrayList<Datapoint> monitoring(String instanceId) {
        final AmazonCloudWatchClient client = client(awsAccessKey, awsSecretKey);
        final GetMetricStatisticsRequest request = request(instanceId);
        final GetMetricStatisticsResult result = result(client, request);

        //toStdOut(result, instanceId);   
        //Object[] teste = null;
        //teste = result.getDatapoints().toArray(teste);
        ArrayList infos = new ArrayList();
        ArrayList dates = new ArrayList();
        ArrayList<Datapoint> dat = new ArrayList();
        //ArrayList<ArrayList> all = new ArrayList<ArrayList>();

        for (final Datapoint dataPoint : result.getDatapoints()) {
            
            dat.add(dataPoint);
            //System.out.printf("%s instance's average CPU utilization : %s%n", instanceId, dataPoint.getAverage());
            //System.out.printf("%s instance's max CPU utilization : %s%n", instanceId, dataPoint.getMaximum());

            dates.add(dataPoint.getTimestamp());
//          infos.add(dataPoint.getAverage());
//          infos.add(dataPoint.getMaximum());
//dates.add(infos);

        }

        Collections.sort(dat, 
                        (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        System.out.println(dat.toString());
        return dat;
    }


    public AmazonCloudWatchClient client(final String awsAccessKey, final String awsSecretKey) {
        final AmazonCloudWatchClient client = new AmazonCloudWatchClient(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        client.setEndpoint("monitoring.sa-east-1.amazonaws.com");
        return client;
    }

    public GetMetricStatisticsRequest request(final String instanceId) {
        final long twentyFourHrs = 1000 * 60 * 60 * 24;
        final int oneHour = 60 * 60;
        return new GetMetricStatisticsRequest()
                .withStartTime(new Date(new Date().getTime() - twentyFourHrs))
                .withNamespace("AWS/EC2")
                .withPeriod(oneHour)
                .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
                .withMetricName("CPUUtilization")
                .withStatistics("Average", "Maximum")
                .withEndTime(new Date());
    }

    public GetMetricStatisticsResult result(
            final AmazonCloudWatchClient client, final GetMetricStatisticsRequest request) {
        return client.getMetricStatistics(request);
    }

//    public void toStdOut(final GetMetricStatisticsResult result, final String instanceId) {
//        System.out.println(result); // outputs empty result: {Label: CPUUtilization,Datapoints: []}
//        for (final Datapoint dataPoint : result.getDatapoints()) {
//            System.out.printf("%s instance's average CPU utilization : %s%n", instanceId, dataPoint.getAverage());
//            System.out.printf("%s instance's max CPU utilization : %s%n", instanceId, dataPoint.getMaximum());
//        }
//    }

}
