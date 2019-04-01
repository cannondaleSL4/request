package com.execute.fileoperation;

import com.controller.exeption.NoServerInEurekaExeption;
import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.enums.Currency;
import com.dim.fxapp.entity.enums.Period;
import com.dim.fxapp.entity.impl.Quotes;
import com.google.common.collect.Lists;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.services.CriteriaService;
import com.util.RoundOfNumber;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.restart.RestartEndpoint;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteReaderFromFile {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private RestartEndpoint restartEndpoint;

    @Autowired
    private CriteriaService criteriaService;

    private Application application;

    @Value("${service.persist}")
    private String persistService;

    @Value("${currency.filepath}")
    protected String filepath;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    private final org.slf4j.Logger Log = LoggerFactory.getLogger(QuoteReaderFromFile.class);

    @PostConstruct
    public void init() {
        application = discoveryClient.getApplication(persistService);

        if (application == null){
            Log.error("Application persist is not available - module reques will be restarted");
            org.springframework.boot.devtools.restart.Restarter.getInstance().restart();
        }
    }

    public void reload(){
        final File folder = new File(filepath);
        Set<File> setOfFile = new HashSet<>(Arrays.asList(folder.listFiles()));
        reloadFromExistFiles(setOfFile);
    }

    public void reloadFromExistFiles(Set<File> files) {
        files.parallelStream()
                .filter(file -> FileUtils.sizeOf(file)!=0)
                .forEach(this::newPersist);
    }

    public Set<QuotesCriteriaBuilder> reloadEmpty(){
        final File folder = new File(filepath);
        return new HashSet<>(Arrays.asList(folder.listFiles()))
                .stream()
                .filter(file -> FileUtils.sizeOf(file)==0)
                .map(file -> parseExistFileName(file.getName()))
                .collect(Collectors.toSet());
        }

    private QuotesCriteriaBuilder parseExistFileName(String filename){
        String [] arrayFromFileName = filename.split("-");
        Currency currency = Currency.valueOf(arrayFromFileName[0]);
        Period period = null;
        if (arrayFromFileName[7].split("\\.")[0].equals("D")) period = Period.DAY;
        if (arrayFromFileName[7].split("\\.")[0].equals("5")) period = Period.FIVEMINUTES;
        if (arrayFromFileName[7].split("\\.")[0].equals("15")) period = Period.FIVETEENMINUTES;
        if (arrayFromFileName[7].split("\\.")[0].equals("W")) period = Period.WEEK;
        if (arrayFromFileName[7].split("\\.")[0].equals("M")) period = Period.MONTH;
        if (arrayFromFileName[7].split("\\.")[0].equals("60")) period = Period.ONEHOUR;
        LocalDate from  = LocalDate.parse(arrayFromFileName[3]+ "/" + arrayFromFileName[2] + "/" + arrayFromFileName[1], DateTimeFormatter.ofPattern("d/MM/yyyy"));
        LocalDate to = LocalDate.parse(arrayFromFileName[6]+ "/" + arrayFromFileName[5] + "/" + arrayFromFileName[4], DateTimeFormatter.ofPattern("d/MM/yyyy"));
        return criteriaService.getCriteria(currency,period,from, to);
    }

    public void newPersist(File file) {
        InstanceInfo instanceInfo = application.getInstances().get(0);
        final String URL = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/quotes/savelist";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            List<String> listofString = Files.lines(file.toPath())
                    .filter(str -> !str.contains("TICKER"))
                    .collect(Collectors.toList());

            List<List<String>> subListOfString = Lists.partition(listofString,3000);

            subListOfString.forEach(subList ->{
                List<Quotes> listOfQuotes = subList.stream()
                        .map(this::quotesFromString)
                        .collect(Collectors.toList());
                org.springframework.http.HttpEntity httpEntity = new org.springframework.http.HttpEntity(listOfQuotes, headers);
                restTemplate.exchange(URL,HttpMethod.POST, httpEntity, Quotes.class);
            });

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
