package com.execute.quotes;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.dim.fxapp.entity.impl.QuotesLive;
import com.interfaces.RequestData;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;

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

    @Override
    public Map<String, Object> getRequest(Set<QuotesCriteriaBuilder> criteriaBuilders) {
        localResp.clear();
        mapResp.clear();
        criteriaBuilders.forEach(creteria -> getFileParseFile(creteria));
        return null;
    }

    private void getFileParseFile(QuotesCriteriaBuilder criteriaBuilder){
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(String.format(MAIN));
            request.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36");
            HttpResponse response = client.execute(request);

            HttpEntity entity = response.getEntity();

            int responseCode = response.getStatusLine().getStatusCode();

//            System.out.println("Request Url: " + request.getURI());
//            System.out.println("Response Code: " + responseCode);

            InputStream is = entity.getContent();

            //String filePath = "./temp/AUDCAD.csv";


            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // write the output to stdout
            String line;
            while ((line = reader.readLine()) != null){
                System.out.println(line);
            }
            reader.close();
//            FileUtils.touch(new File(filePath));

//            FileOutputStream fos = new FileOutputStream(new File(filePath));
//
//            int inByte;
//            while ((inByte = is.read()) != -1) {
//                fos.write(inByte);
//            }
//            is.close();
//            fos.close();
//            client.close();
//            System.out.println("File Download Completed!!!");
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persist(Map<String, Object> request){
        Application application = discoveryClient.getApplication(persistService);
        InstanceInfo instanceInfo = application.getInstances().get(0);
        String url = "http://" + instanceInfo.getIPAddr() + ":" + instanceInfo.getPort() + "/" + "quotes/";
        restTemplate.put(url,request.get("successful"));
    }
}
