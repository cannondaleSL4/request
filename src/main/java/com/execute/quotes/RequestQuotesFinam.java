package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Period;
import com.dim.fxapp.entity.impl.Quotes;
import com.dim.fxapp.entity.impl.QuotesLive;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by dima on 07.05.18.
 */
public class RequestQuotesFinam extends RequestData<QuotesLive> {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    @Value("${service.persist}")
    private String persistService;

    @Value("${currency.quotes}")
    protected String MAIN;

    @Value("${currency.filepath}")
    protected String filepath;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        localResp.clear();
        mapResp.clear();

        //this block only for fill on base for begin
        boolean isAnyEmty=true;
        while(isAnyEmty){
            criteriaBuilders.forEach(creteria -> getFileParseFile(creteria));
            final File folder = new File(filepath);
            List<File> fileArray = new ArrayList<>(Arrays.asList(folder.listFiles()));
            isAnyEmty=false;
            for(File file : fileArray){
                if(FileUtils.sizeOf(file)==0){
                    isAnyEmty=true;
                }
            }
        }

        persist();


//        criteriaBuilders.forEach(creteria -> getFileParseFile(creteria));
        return null;
    }

    private void getFileParseFile(QuotesCriteriaBuilder criteriaBuilder){
        String filename = criteriaBuilder.getCurrency().toString()+"-"+
                criteriaBuilder.getFrom()+"-"+
                criteriaBuilder.getTo()+"-"+
                criteriaBuilder.getPeriod();

        String localFilePath = filepath +"/" + filename + ".csv";
        File f = new File(localFilePath);
        try{
            if(!f.exists()){
                FileUtils.touch(f);
                executeRequest(criteriaBuilder,f);
            } else {
                if(FileUtils.sizeOf(f)==0){
                    FileUtils.forceDelete(f);
                    FileUtils.touch(f);
                    executeRequest(criteriaBuilder,f);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeRequest(QuotesCriteriaBuilder criteriaBuilder, File f){
        String stringForRequest = String.format(MAIN,
                f.getName(),
                criteriaBuilder.getCurrency().getByCurrensy(criteriaBuilder.getCurrency().toString()),
                criteriaBuilder.getCurrency().toString(),
                criteriaBuilder.getFrom().getDayOfMonth(),
                criteriaBuilder.getFrom().getMonthValue()-1,
                criteriaBuilder.getFrom().getYear(),
                criteriaBuilder.getFrom().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                criteriaBuilder.getTo().getDayOfMonth(),
                criteriaBuilder.getTo().getMonthValue()-1,
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

            if(response.getStatusLine().getStatusCode() == 200){
                InputStream is = entity.getContent();
                FileOutputStream fos = new FileOutputStream(f);
                int inByte;
                while ((inByte = is.read()) != -1) {
                    fos.write(inByte);
                }
                is.close();
                fos.close();
                client.close();
            }
            TimeUnit.NANOSECONDS.sleep(10000000);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void persist(){
        Application application = discoveryClient.getApplication(persistService);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/" + "quotes/";

        final File folder = new File(filepath);
        List<File> fileArray = new ArrayList<>(Arrays.asList(folder.listFiles()));

        for(File file: fileArray){
            if(FileUtils.sizeOf(file)!=0){
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    Set<Quotes> setOfQuotes = new LinkedHashSet<>();
                    while ((line = br.readLine()) != null) {
                        if(!line.contains("TICKER")){
                            String [] array = line.split(",");
                            Period period = null;
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
                            if (array[1].equals("D"))period = Period.DAY;
                            if (array[1].equals("5"))period = Period.FIVEMINUTES;
                            if (array[1].equals("15"))period = Period.FIVETEENMINUTES;
                            if (array[1].equals("W"))period = Period.WEEK;
                            if (array[1].equals("M"))period = Period.MONTH;
                            Quotes quotes = Quotes.builder()
                                    .currency(array[0])
                                    .period(period)
                                    .data(LocalDateTime.parse(array[2]+" " + array[3], formatter))
                                    .open(RoundOfNumber.round(array[4]))
                                    .high(RoundOfNumber.round(array[5]))
                                    .low(RoundOfNumber.round(array[6]))
                                    .close(RoundOfNumber.round(array[7]))
                                    .build();
//                            setOfQuotes.add(quotes);
                            restTemplate.put(url,quotes);

                        }
                    }
//                    restTemplate.put(url,setOfQuotes);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
