package com.mike.MikePdfUtils;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

public class MikePdfUtils {
    public static String GetPdfInfos(String InputPdfFilePath, String PasswordString) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        File pdfFile = new File(InputPdfFilePath);
        if (pdfFile.exists()){
            InputStream input;
            PDDocument document;
            input = new FileInputStream( pdfFile );
            document = PDDocument.load( input, PasswordString);
            PDDocumentInformation pdfInfo = document.getDocumentInformation();
            stringBuilder.append(String.format("\"Author\":\"%s\",",(pdfInfo.getAuthor()==null?"":pdfInfo.getAuthor())));
            SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            stringBuilder.append(String.format("\"CreationDate\":\"%s\",",timeformat.format(pdfInfo.getCreationDate().getTime())));
            stringBuilder.append(String.format("\"Creator\":\"%s\",",(pdfInfo.getCreator()==null?"":pdfInfo.getCreator())));
            stringBuilder.append(String.format("\"Keywords\":\"%s\",",(pdfInfo.getKeywords()==null?"":pdfInfo.getKeywords())));
            stringBuilder.append(String.format("\"ModificationDate\":\"%s\",",timeformat.format(pdfInfo.getModificationDate().getTime())));
            stringBuilder.append(String.format("\"Producer\":\"%s\",",(pdfInfo.getProducer()==null?"":pdfInfo.getProducer())));
            stringBuilder.append(String.format("\"Subject\":\"%s\",",(pdfInfo.getSubject()==null?"":pdfInfo.getSubject())));
            stringBuilder.append(String.format("\"Title\":\"%s\"",(pdfInfo.getTitle()==null?"":pdfInfo.getTitle())));
        } else {
            System.err.printf("The file %s does not exists!%n", InputPdfFilePath);
          throw new IOException(String.format("The file %s does not exists!", InputPdfFilePath));
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        try {
            String infoJson;
            infoJson = MikePdfUtils.GetPdfInfos("C:\\Users\\mike\\Documents\\LIbs\\solr\\apache-solr-ref-guide-7.3.pdf", "");
            System.out.println(infoJson);
        }catch (Exception e){
            System.err.println(e.toString());
        }

    }


}
