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
package br.unb.cic.bionimbus.controller.elasticitycontroller;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 *
 * @author guilherme
 */
public class AmazonMonitoring {

    final String awsAccessKey = "AKIAJKOWVDC56R7RR4UQ";
    final String awsSecretKey = "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";
    

    public ArrayList<Datapoint> monitoring(String instanceId) {
        final AmazonCloudWatchClient client = client(awsAccessKey, awsSecretKey);
        final GetMetricStatisticsRequest request = request(instanceId);
        final GetMetricStatisticsResult result = result(client, request);

        ArrayList<Datapoint> infos = new ArrayList();

        for (final Datapoint dataPoint : result.getDatapoints()) {
            infos.add(dataPoint);
//            System.out.println("teste");
        }

        Collections.sort(infos,
                (o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));
        System.out.println(infos);

        return infos;
    }

    public AmazonCloudWatchClient client(final String awsAccessKey, final String awsSecretKey) {
        final AmazonCloudWatchClient client = new AmazonCloudWatchClient(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
        client.setEndpoint("monitoring.sa-east-1.amazonaws.com");
        return client;
    }

    public GetMetricStatisticsRequest request(final String instanceId) {
        final long start = 1000 * 60 * 60 * 1;
        final int period = 60 * 15;
        return new GetMetricStatisticsRequest()
                .withStartTime(new Date(new Date().getTime() - start))
                .withNamespace("AWS/EC2")
                .withPeriod(period)
                .withDimensions(new Dimension().withName("InstanceId").withValue(instanceId))
                .withMetricName("CPUUtilization")
                .withStatistics("Average", "Maximum")
                .withEndTime(new Date());
    }

    public GetMetricStatisticsResult result(
            final AmazonCloudWatchClient client, final GetMetricStatisticsRequest request) {
        return client.getMetricStatistics(request);
    }

//    public PutMetricAlarmRequest setAlarm() {
//        Iterable<AutoScalingGroup> autoScalingGroups = null;
//        
//        for (AutoScalingGroup asg : autoScalingGroups) {
//            PutScalingPolicyRequest spr = new PutScalingPolicyRequest();
//            spr.setAutoScalingGroupName(asg.getAutoScalingGroupName());
//            spr.setPolicyName("testautoscalePolicy");
//            spr.setAdjustmentType("ChangeInCapacity");
//            spr.setScalingAdjustment(2);
//            PutScalingPolicyResult result1 = autoClient.putScalingPolicy(spr);
//        
//        PutMetricAlarmRequest putMetricAlarmRequest = new PutMetricAlarmRequest();
//        putMetricAlarmRequest.setAlarmName("Scaleup-alarm");
//        putMetricAlarmRequest.setMetricName("CPUUtilization");
//        putMetricAlarmRequest.setComparisonOperator(ComparisonOperator.GreaterThanOrEqualToThreshold);
//        putMetricAlarmRequest.setStatistic(Statistic.Sum);
//        putMetricAlarmRequest.setUnit(StandardUnit.Percent);
//        putMetricAlarmRequest.setThreshold(40.0);
//        putMetricAlarmRequest.setPeriod(60);
//        putMetricAlarmRequest.setEvaluationPeriods(1);
//        List actions = new ArrayList();
//        actions.add(result1.getPolicyARN());
//        putMetricAlarmRequest.setAlarmActions(actions);
//
//        AmazonCloudWatchClient amazonCloudWatchClient = new AmazonCloudWatchClient(awscredentials);
//        PutMetricAlarmResult alarmResult = amazonCloudWatchClient.putMetricAlarm(putMetricAlarmRequest);
//    }

//    public void toStdOut(final GetMetricStatisticsResult result, final String instanceId) {
//        System.out.println(result); // outputs empty result: {Label: CPUUtilization,Datapoints: []}
//        for (final Datapoint dataPoint : result.getDatapoints()) {
//            System.out.printf("%s instance's average CPU utilization : %s%n", instanceId, dataPoint.getAverage());
//            System.out.printf("%s instance's max CPU utilization : %s%n", instanceId, dataPoint.getMaximum());
//        }
//    }
}
