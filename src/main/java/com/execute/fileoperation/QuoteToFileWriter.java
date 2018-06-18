package com.execute.fileoperation;

import com.dim.fxapp.entity.criteria.QuotesCriteriaBuilder;
import com.execute.quotes.RequestQuotesFinam;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuoteToFileWriter {

    @Value("${currency.filepath}")
    protected String filepath;

    private final org.slf4j.Logger Log = LoggerFactory.getLogger(RequestQuotesFinam.class);

    public void writeFile(InputStream is, File file) {
        if (FileUtils.sizeOf(file) == 0) {
            try(FileOutputStream fos= new FileOutputStream(file)) {
                int inByte;
                while ((inByte = is.read()) != -1) {
                    fos.write(inByte);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<File, QuotesCriteriaBuilder> getHashMapOfCreteriaAndFiles(final Set<QuotesCriteriaBuilder> criteriaBuilders) {
        return criteriaBuilders.stream()
                .collect(Collectors.toMap(
                        this::makeFile, k -> k));
    }

    private File makeFile(QuotesCriteriaBuilder criteriaBuilder) {
        String filename = criteriaBuilder.getCurrency().toString() + "-" + criteriaBuilder.getFrom() + "-"
                + criteriaBuilder.getTo() + "-" + criteriaBuilder.getPeriod().toString() + ".csv";
        String localFilePath = filepath + "/" + filename;
        File file = new File(localFilePath);

        try {
            if (!file.exists())FileUtils.touch(file);
        } catch (IOException e) {
            Log.error("could not create file: " + file);
        }
        return file;
    }

    public boolean isEmptyFiles(Set<File> setOfFile) {
        for (File file : setOfFile) {
            if (FileUtils.sizeOf(file) == 0) return true;
        }
        return false;
    }
}
