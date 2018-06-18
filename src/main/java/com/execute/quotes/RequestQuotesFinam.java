package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.execute.fileoperation.QuoteReaderFromFile;
import com.execute.fileoperation.QuoteToFileWriter;
import com.exeption.NoServerInEurekaExeption;
import com.google.common.collect.ImmutableMap;
import com.interfaces.RequestData;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
public class RequestQuotesFinam extends RequestData<QuotesLive> {

    @Value("${currency.quotes}")
    protected String MAIN;

    @Value("${currency.filepath}")
    protected String filepath;

    @Autowired
    protected QuoteToFileWriter writer;

    @Autowired
    protected QuoteReaderFromFile reader;

    private final org.slf4j.Logger Log = LoggerFactory.getLogger(RequestQuotesFinam.class);

    private Map<File, QuotesCriteriaBuilder> mapOfCriteriaAndFile;
    private Set<File> setOfFile;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        mapOfCriteriaAndFile = new HashMap<>();
        setOfFile = new HashSet<>();
        long start = System.currentTimeMillis();
        mapOfCriteriaAndFile.putAll(writer.getHashMapOfCreteriaAndFiles(criteriaBuilders));
        setOfFile.addAll(writer.getHashMapOfCreteriaAndFiles(criteriaBuilders).keySet());
        while (isContinue(start)) {
            mapOfCriteriaAndFile.forEach(this::executeRequest);
            mapOfCriteriaAndFile = clearMap();
        }
        Log.info("for server request: " + (System.currentTimeMillis() - start) / 1000 + " sec.");
        reader.reloadFromExistFiles(setOfFile);
        mapResp = ImmutableMap.<String, Object>builder().put("successful", "data was updated").build();
        return mapResp;
    }

    public void reload() {
        final File folder = new File(filepath);
        Set<File> setOfFile = new HashSet<>(Arrays.asList(folder.listFiles()));
        reader.reloadFromExistFiles(setOfFile);
    }


    private void executeRequest(File file, QuotesCriteriaBuilder criteriaBuilder) {
        String stringForRequest = String.format(MAIN,
                file.getName(),
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
                file.getName().split(("\\.(?=[^\\.]+$)"))[0],
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
                writer.writeFile(is, file);
                client.close();
                is.close();
            }
        } catch (ClientProtocolException e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    private HashMap<File, QuotesCriteriaBuilder> clearMap() {
        return this.mapOfCriteriaAndFile.entrySet()
                .stream()
                .filter(x -> FileUtils.sizeOf(x.getKey()) == 0)
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue(),
                        (v1, v2) -> {
                            throw new IllegalStateException();
                        },
                        () -> new HashMap<>()));
    }

    private boolean isContinue(long start) {
        long threshold = start + 1800000;
        boolean isAnyEmpty = writer.isEmptyFiles(setOfFile);
        boolean isServerAvailable = false;
        try {
            isServerAvailable = reader.isServerPersistAvailable();
        } catch (NoServerInEurekaExeption ex) {
            Log.error(ex.getMessage());
        }
        return (isAnyEmpty && isServerAvailable && (System.currentTimeMillis() - start < threshold));
    }
}
