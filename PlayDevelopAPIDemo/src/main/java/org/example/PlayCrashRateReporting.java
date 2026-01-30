package org.example;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.playdeveloperreporting.v1beta1.Playdeveloperreporting;
import com.google.api.services.playdeveloperreporting.v1beta1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlayCrashRateReporting {
    private static final String APPLICATION_NAME = "Play-Developer-Reporting-Example";
    private static final String PACKAGE_NAME = "XXX";//TODO APP_PACKAGE_NAME
    private static final String RESOURCES_CLIENT_SECRETS_JSON = "src/main/resources/client_secrets.json";//TODO 在resources文件替换自己的json文件
    public static Playdeveloperreporting createPlayReportingService()
            throws GeneralSecurityException, IOException {

        final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                        new FileInputStream(RESOURCES_CLIENT_SECRETS_JSON))
                .createScoped(Collections.singletonList(
                        "https://www.googleapis.com/auth/playdeveloperreporting"));

        return new Playdeveloperreporting.Builder(
                httpTransport,
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) {
        try {
            Playdeveloperreporting service = createPlayReportingService();
            System.out.println("Playdeveloperreporting 服务创建成功！");
            String metricSetName = String.format("apps/%s/crashRateMetricSet", PACKAGE_NAME);

            // 使用服务进行API调用
            GoogleTypeDateTime startTime = new GoogleTypeDateTime();
            startTime.setYear(2025);
            startTime.setMonth(7);
            startTime.setDay(20);

            GoogleTypeDateTime endTime = new GoogleTypeDateTime();
            endTime.setYear(2025);
            endTime.setMonth(8);
            endTime.setDay(27);


            GooglePlayDeveloperReportingV1beta1TimelineSpec timelineSpec = new GooglePlayDeveloperReportingV1beta1TimelineSpec();
            timelineSpec.setAggregationPeriod("DAILY"); //
            timelineSpec.setStartTime(startTime);
            timelineSpec.setEndTime(endTime);

            GooglePlayDeveloperReportingV1beta1QueryCrashRateMetricSetRequest request =
                    new GooglePlayDeveloperReportingV1beta1QueryCrashRateMetricSetRequest();
            request.setMetrics(Arrays.asList("crashRate"));
            request.setDimensions(Arrays.asList("versionCode")); // 可以添加其他维度：osVersion, deviceModel, deviceType, country等
            request.setTimelineSpec(timelineSpec);
            // 4. 执行查询
           Playdeveloperreporting.Vitals.Crashrate.Query query =
                    service.vitals().crashrate().query(metricSetName,request);
            HttpResponse response = query.buildHttpRequest().execute();
            parseCrashRateResponse(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void parseCrashRateResponse(HttpResponse response) throws IOException {
        // 1. 将HTTP响应解析为响应对象
        GooglePlayDeveloperReportingV1beta1QueryCrashRateMetricSetResponse apiResponse =
                response.parseAs(GooglePlayDeveloperReportingV1beta1QueryCrashRateMetricSetResponse.class);

        // 2. 获取所有的数据行
        List<GooglePlayDeveloperReportingV1beta1MetricsRow> rows = apiResponse.getRows();

        if (rows == null || rows.isEmpty()) {
            System.out.println("没有找到数据");
            return;
        }

        // 3. 遍历每一行数据
        for (GooglePlayDeveloperReportingV1beta1MetricsRow row : rows) {
            parseMetricsRow(row);
        }

        // 4. 获取下一次查询的令牌（如果有分页）
        String nextPageToken = apiResponse.getNextPageToken();
        if (nextPageToken != null) {
            System.out.println("还有更多数据，下一页令牌: " + nextPageToken);
        }
    }

    private static void parseMetricsRow(GooglePlayDeveloperReportingV1beta1MetricsRow row) {
        System.out.println("=== 数据行 ===");

        // 解析维度（分组条件）
        if (row.getDimensions() != null) {
            System.out.println("维度信息:");
        }

        // 解析指标数据
        if (row.getMetrics() != null) {
            System.out.println("指标数据:");
        }

        // 解析时间聚合信息
        if (row.getAggregationPeriod() != null) {
            System.out.println("聚合周期: " + row.getAggregationPeriod());
        }

        System.out.println(); // 空行分隔
    }
}