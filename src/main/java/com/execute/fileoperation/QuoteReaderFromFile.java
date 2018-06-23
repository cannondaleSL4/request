package com.execute.fileoperation;

import com.controller.exeption.NoServerInEurekaExeption;
import com.dim.fxapp.entity.enums.Period;
import com.dim.fxapp.entity.impl.Quotes;
import com.execute.quotes.RequestQuotesFinam;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.util.RoundOfNumber;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuoteReaderFromFile {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    private Application application;

    @Value("${service.persist}")
    private String persistService;

    @Value("${currency.filepath}")
    protected String filepath;

    private final org.slf4j.Logger Log = LoggerFactory.getLogger(RequestQuotesFinam.class);

    @PostConstruct
    public void init() {
        application = discoveryClient.getApplication(persistService);
    }

    public void reloadFromExistFiles(Set<File> files) {
        long start = System.currentTimeMillis();
        files.parallelStream()
                .filter(file -> FileUtils.sizeOf(file)!=0)
                .forEach(this::newPersist);
       Log.info("for parse files was spent: " + (System.currentTimeMillis() - start)/1000 + " sec.");
    }

    public void newPersist(File file) {
        InstanceInfo instanceInfo = application.getInstances().get(0);
        final String URL = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/quotes/savelist";
        try {
            List<Quotes> listOfQuote = Files.lines(file.toPath())
                    .filter(str -> !str.contains("TICKER"))
                    .collect(Collectors.toList())
                    .stream()
                    .map(this::quotesFromString)
                    .collect(Collectors.toList());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            org.springframework.http.HttpEntity httpEntity = new org.springframework.http.HttpEntity(listOfQuote, headers);
            restTemplate.exchange(URL, HttpMethod.POST, httpEntity, String.class);
        } catch (HttpClientErrorException e) {
            Log.error(e.getMessage());
        }catch (HttpServerErrorException e){
            Log.error(e.getMessage());
        }catch (HttpMessageNotReadableException ex) {
            Log.error(ex.getMessage());
        } catch (IOException e) {
            Log.error(e.getMessage());
        }catch (Exception e){
            Log.error(e.getMessage());
        }
    }

    private Quotes quotesFromString(String line) {
        String[] array = line.split(",");
        Period period = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");
        if (array[1].equals("D")) period = Period.DAY;
        if (array[1].equals("5")) period = Period.FIVEMINUTES;
        if (array[1].equals("15")) period = Period.FIVETEENMINUTES;
        if (array[1].equals("W")) period = Period.WEEK;
        if (array[1].equals("M")) period = Period.MONTH;
        if (array[1].equals("60")) period = Period.ONEHOUR;
        return Quotes.builder()
                .currency(array[0])
                .period(period.toString())
                .data(LocalDateTime.parse(array[2] + " " + array[3], formatter))
                .open(RoundOfNumber.round(array[4]))
                .high(RoundOfNumber.round(array[5]))
                .low(RoundOfNumber.round(array[6]))
                .close(RoundOfNumber.round(array[7]))
                .build();
    }

    public boolean isServerPersistAvailable() throws NoServerInEurekaExeption {
        if (application == null) {
            throw new NoServerInEurekaExeption(persistService);
        }
        return true;
    }
}
