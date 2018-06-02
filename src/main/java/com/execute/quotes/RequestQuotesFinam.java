package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Period;
import com.dim.fxapp.entity.impl.Quotes;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.exeption.NoServerInEurekaExeption;
import com.interfaces.RequestData;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.util.RoundOfNumber;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by dima on 07.05.18.
 */
public class RequestQuotesFinam extends RequestData<QuotesLive> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    private final org.slf4j.Logger Log = LoggerFactory.getLogger(RequestQuotesFinam.class);

    @Value("${service.persist}")
    private String persistService;

    @Value("${currency.quotes}")
    protected String MAIN;

    @Value("${currency.filepath}")
    protected String filepath;

    Application application;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        this.application = discoveryClient.getApplication(persistService);
        localResp.clear();
        mapResp.clear();
        try {
            boolean isAnyEmpty = true;
            boolean isServerAvailable = isServerPersistanceAvailable();
            while (isAnyEmpty && isServerAvailable) {
                criteriaBuilders.forEach(this::getFileParseFile);
                isAnyEmpty = !checkIsEmptyFiles();
            }
        } catch (NoServerInEurekaExeption e) {
            Log.error(e.getMessage());
        }

        return null;
    }

    private void getFileParseFile(QuotesCriteriaBuilder criteriaBuilder) {
        String filename = criteriaBuilder.getCurrency().toString() + "-" +
                criteriaBuilder.getFrom() + "-" +
                criteriaBuilder.getTo() + "-" +
                criteriaBuilder.getPeriod();

        String localFilePath = filepath + "/" + filename + ".csv";
        File f = new File(localFilePath);
        try {
            if (!f.exists()) {
                FileUtils.touch(f);
                executeRequest(criteriaBuilder, f);
            } else {
                if (FileUtils.sizeOf(f) == 0) {
                    FileUtils.forceDelete(f);
                    FileUtils.touch(f);
                    executeRequest(criteriaBuilder, f);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeRequest(QuotesCriteriaBuilder criteriaBuilder, File f) {
        String stringForRequest = String.format(MAIN,
                f.getName(),
                criteriaBuilder.getCurrency().getByCurrensy(criteriaBuilder.getCurrency().toString()),
                criteriaBuilder.getCurrency().toString(),
                criteriaBuilder.getFrom().getDayOfMonth(),
                criteriaBuilder.getFrom().getMonthValue() - 1,
                criteriaBuilder.getFrom().getYear(),
                criteriaBuilder.getFrom().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                criteriaBuilder.getTo().getDayOfMonth(),
                criteriaBuilder.getTo().getMonthValue() - 1,
                criteriaBuilder.getTo().getYear(),
                criteriaBuilder.getTo().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                criteriaBuilder.getPeriod().getCodeByPeriod(criteriaBuilder.getPeriod().toString()),
                f.getName().split(("\\.(?=[^\\.]+$)"))[0],
                criteriaBuilder.getCurrency().toString()
        );

        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(stringForRequest);
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
            HttpResponse response = client.execute(request);

            HttpEntity entity = response.getEntity();

            if (response.getStatusLine().getStatusCode() == 200) {
                InputStream is = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                BufferedWriter writer = new BufferedWriter(new FileWriter(f));
                reader.readLine(); // this will read and skip the first line <TICKER>,<PER>,<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + '\n');
                    newPersist(line);
                }
                reader.close();
                writer.close();
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newPersist(String line) {
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/" + "quotes/";

        String[] array = line.split(",");
        Period period = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        if (array[1].equals("D")) period = Period.DAY;
        if (array[1].equals("5")) period = Period.FIVEMINUTES;
        if (array[1].equals("15")) period = Period.FIVETEENMINUTES;
        if (array[1].equals("W")) period = Period.WEEK;
        if (array[1].equals("M")) period = Period.MONTH;
        Quotes quotes = Quotes.builder()
                .currency(array[0])
                .period(period)
                .data(LocalDateTime.parse(array[2] + " " + array[3], formatter))
                .open(RoundOfNumber.round(array[4]))
                .high(RoundOfNumber.round(array[5]))
                .low(RoundOfNumber.round(array[6]))
                .close(RoundOfNumber.round(array[7]))
                .build();
        try {
//            restTemplate.postForEntity(url,quotes, Quotes.class);
        } catch (HttpClientErrorException ex) {
//            System.out.println("try to save again " + quotes.getCurrency());
        }
    }

    private boolean checkIsEmptyFiles() {
        final File folder = new File(filepath);
        List<File> fileArray = new ArrayList<>(Arrays.asList(folder.listFiles()));
        for (File file : fileArray) {
            if (FileUtils.sizeOf(file) == 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isServerPersistanceAvailable() throws NoServerInEurekaExeption {
        if (application == null) {
            throw new NoServerInEurekaExeption(persistService);
        }
        return true;
    }
}
