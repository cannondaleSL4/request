package com.execute.livequotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.exeption.ServerRequestExeption;
import com.interfaces.RequestData;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * Created by dima on 05.05.18.
 */
public class RequestLiveQuotesFinam extends RequestData<QuotesLive> {

    @Value("${currency.livequotes}")
    protected String MAIN;

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> quotesCriteriaBuilders) {
        mapResp.clear();
        quotesCriteriaBuilders.forEach(quotesCriteriaBuilder -> {
                    String currencyStr = quotesCriteriaBuilder.getCurrency().toString();
                    Integer number = quotesCriteriaBuilder.getCurrency().getByCurrensy(currencyStr);
                    try {
                        getLastForCurrentCurrency(currencyStr, number);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ServerRequestExeption serverRequestExeption) {
                        serverRequestExeption.printStackTrace();
                    } catch (Exception e) {
                        mapResp.put("error", messageError);
                    }
                }
        );
        if (mapResp.isEmpty()) {
            mapResp.put("successful", localResp);
        }
        return mapResp;
    }

    private void getLastForCurrentCurrency(String currency,Integer code) throws InterruptedException, IOException, ServerRequestExeption {
        String str = "";
        str = String.format(MAIN,code,currency,currency);
        httpGet = new HttpGet(String.format(MAIN,code,currency,currency));
        QuotesLive quotesLive = null;
        CloseableHttpClient client = HttpClientBuilder.create().build();
        TimeUnit.NANOSECONDS.sleep(700000000);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
        HttpResponse response = client.execute(httpGet);
        if(response.getStatusLine().getStatusCode()==200){
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            reader.readLine(); //add this for exept reading the first line
            String line =reader.readLine();//i need only second line, and needn't while or for
            if(line != null && !line.isEmpty()) {
                parseString(line);
            }
            reader.close();
        }else{
            System.out.println(str);
            System.out.println(response.getStatusLine().getStatusCode());
        }
    }

    private void parseString(String line){
        String [] strArray = line.split(",");
        if (strArray.length != 0){
            QuotesLive quotesLive = QuotesLive.builder()
                    .currency(strArray[0])
                    .price(new BigDecimal(strArray[7]))
                    .localDateTime(LocalDateTime.now())
                    .build();
            localResp.add(quotesLive);
        }
    }
}
