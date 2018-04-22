package com.mike.MikePdfUtils;


import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.UUID;

public class MikePdfUtils {
    public static String getPdfInformation(String InputPdfFilePath, String PasswordString) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        File pdfFile = new File(InputPdfFilePath);
        InputStream input;
        PDDocument document;
        if (pdfFile.exists()){
            input = new FileInputStream( pdfFile );
            document = PDDocument.load( input, PasswordString);
            AccessPermission ap = document.getCurrentAccessPermission();
            if( ! ap.canExtractContent() )
            {
                throw new IOException( "You do not have permission to extract text" );
            }
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
        document.close();
        input.close();
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static int extractImages(String InputPdfFilePath, String PasswordString, String OutputPath, String ColorType) throws IOException {
        if (!OutputPath.endsWith("\\")){
            OutputPath = OutputPath + "\\";
        }
        File pdfFile = new File(InputPdfFilePath);
        if (pdfFile.exists()){
            InputStream input;
            PDDocument pdfDocument;
            input = new FileInputStream( pdfFile );
            pdfDocument = PDDocument.load( input, PasswordString);
            AccessPermission ap = pdfDocument.getCurrentAccessPermission();
            if( ! ap.canExtractContent() )
            {
                throw new IOException( "You do not have permission to extract text" );
            }
            PDDocumentCatalog cata = pdfDocument.getDocumentCatalog();
            PDPageTree pdfPageTree = cata.getPages();
            //int PageCount = pdfPageTree.getCount();
            for (PDPage currentPage : pdfPageTree) {
                PDResources bufResources = currentPage.getResources();
                Iterable<COSName> pageXObjectNames = bufResources.getXObjectNames();
                if (pageXObjectNames != null) {
                    for (COSName cosName : pageXObjectNames) {
                        if (bufResources.isImageXObject(cosName)) {
                            PDImageXObject image_xObject = (PDImageXObject) bufResources.getXObject(cosName);
                            BufferedImage image = image_xObject.getImage();
                            FileOutputStream out = new FileOutputStream(OutputPath + UUID.randomUUID() + ".jpg");
                            try {
                                switch (ColorType){
                                    case("RGB"):
                                        ImageIO.write(convertImageToRGB(image), "jpeg", out);
                                        break;
                                    case("Gray"):
                                        ImageIO.write(convertImageToGray(image), "jpeg", out);
                                        break;
                                    default:
                                        break;
                                }
                            } catch (IOException e) {
                                System.err.println(e.toString());
                            } finally {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    System.err.println(e.toString());
                                }
                            }
                        }
                    }
                }
            }
            IOUtils.closeQuietly(pdfDocument);
        } else {
            System.err.printf("The file %s does not exists!%n", InputPdfFilePath);
            throw new IOException(String.format("The file %s does not exists!", InputPdfFilePath));
        }
        return 1;
    }

    public static int extractGrayImages(String InputPdfFilePath, String PasswordString, String OutputPath) throws IOException {
        extractImages(InputPdfFilePath, PasswordString, OutputPath, "Gray");
        return 1;
    }

    public static int extractRGBImages(String InputPdfFilePath, String PasswordString, String OutputPath) throws IOException {
        extractImages(InputPdfFilePath, PasswordString, OutputPath, "RGB");
        return 1;
    }

    public static BufferedImage convertImageToRGB(BufferedImage input){
        BufferedImage resultBufferedImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
        resultBufferedImage.createGraphics().drawImage(input, 0, 0, Color.WHITE, null);
        return resultBufferedImage;
    }

    public static BufferedImage convertImageToGray(BufferedImage input){
        BufferedImage resultBufferedImage = convertImageToRGB(input);
        new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(resultBufferedImage, resultBufferedImage);
        return resultBufferedImage;
    }

    public static int extractText(String InputPdfFilePath, String PasswordString, String OutputPath) throws IOException {
        int result=0;
        File outputFile=null;
        PDDocument document;
        if ( ! InputPdfFilePath.toLowerCase().endsWith(".pdf") ) {
            System.err.println("Not pdf file!");
            return 0;
        }
        File pdfFile = new File( InputPdfFilePath );
        if( outputFile == null && InputPdfFilePath.length() >4 )
        {
            outputFile = new File(OutputPath + File.separator + (pdfFile.getName().substring(0,pdfFile.getName().length()-4) + ".txt"));
        }else{
            outputFile = new File("content.txt");
        }
        document = PDDocument.load(new File( InputPdfFilePath ), PasswordString);

        AccessPermission ap = document.getCurrentAccessPermission();
        if( ! ap.canExtractContent() )
        {
            throw new IOException( "You do not have permission to extract text" );
        }
        int PageCount = document.getNumberOfPages();
        StringBuilder stringBuilder = new StringBuilder();
        PDFTextStripper stripper = new PDFTextStripper();
        for (int i = 1; i <= PageCount; i++){
            stripper.setSortByPosition(true);
            stripper.setStartPage(i);
            stripper.setEndPage(i);
            stringBuilder.append(stripper.getText(document));
        }

        // for any embedded PDFs:
        PDDocumentCatalog catalog = document.getDocumentCatalog();
        PDDocumentNameDictionary names = catalog.getNames();
        if (names != null)
        {
            PDEmbeddedFilesNameTreeNode embeddedFiles = names.getEmbeddedFiles();
            if (embeddedFiles != null)
            {
                Map<String, PDComplexFileSpecification> embeddedFileNames = embeddedFiles.getNames();
                if (embeddedFileNames != null)
                {
                    for (Map.Entry<String, PDComplexFileSpecification> ent : embeddedFileNames.entrySet())
                    {
                        PDComplexFileSpecification spec = ent.getValue();
                        PDEmbeddedFile file = spec.getEmbeddedFile();
                        if (file != null && "application/pdf".equals(file.getSubtype()))
                        {
                            InputStream fis = file.createInputStream();
                            PDDocument subDoc;
                            try
                            {
                                subDoc = PDDocument.load(fis);
                            }
                            finally
                            {
                                fis.close();
                            }
                            try
                            {
                                int subDocPageCount = subDoc.getNumberOfPages();
                                for (int i = 1; i <= subDocPageCount; i++){
                                    stripper.setSortByPosition(true);
                                    stripper.setStartPage(i);
                                    stripper.setEndPage(i);
                                    stringBuilder.append(stripper.getText(subDoc));
                                }
                            }
                            finally
                            {
                                IOUtils.closeQuietly(subDoc);
                            }
                        }
                    }
                }
            }
        }
        IOUtils.closeQuietly(document);
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        outputStream.write(stringBuilder.toString().getBytes());
        outputStream.close();
        return result;
    }


    public static void main(String[] args) {
        try {
            String infoJson="";
            infoJson = MikePdfUtils.getPdfInformation("C:\\Users\\mike\\Documents\\LIbs\\solr\\apache-solr-ref-guide-7.3.pdf", "");
            int ret = 0;
//            ret = MikePdfUtils.extractGrayImages("C:\\Users\\mike\\Documents\\LIbs\\solr\\apache-solr-ref-guide-7.3.pdf", "", "C:\\Users\\mike\\Documents\\LIbs\\solr\\temp");
            ret = MikePdfUtils.extractText("C:\\Users\\mike\\Documents\\LIbs\\solr\\apache-solr-ref-guide-7.3.pdf", "", "C:\\Users\\mike\\Documents\\LIbs\\solr\\temp");

            ret = MikePdfUtils.extractRGBImages("C:\\Users\\mike\\Documents\\LIbs\\solr\\apache-solr-ref-guide-7.3.pdf", "", "C:\\Users\\mike\\Documents\\LIbs\\solr\\temp");

            System.out.println(infoJson);
        }catch (Exception e){
            System.err.println(e.toString());
        }

    }


}
