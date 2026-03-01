package com.emissor;
import java.io.*;
import java.util.zip.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.util.*;

public class ReadXlsxGrade {
    public static void main(String[] args) throws Exception {
        String xlsxPath = "C:\\Users\\wilqu\\Downloads\\Relatório de Graus.xlsx";
        
        ZipFile zip = new ZipFile(xlsxPath);
        
        // Read sharedStrings.xml
        List<String> strings = readSharedStrings(zip);
        
        System.out.println("\n=== FIRST 80 SHARED STRINGS ===");
        for (int i = 0; i < Math.min(80, strings.size()); i++) {
            System.out.println(i + ": " + strings.get(i));
        }
        
        // Read sheet1.xml
        System.out.println("\n=== FIRST 25 DATA ROWS ===");
        readSheetRows(zip, strings, 25);
        
        zip	.close();
    }
    
    static List<String> readSharedStrings(ZipFile zip) throws Exception {
        List<String> strings = new ArrayList<>();
        
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            System.err.println("sharedStrings.xml not found");
            return strings;
        }
        
        InputStream is = zip.getInputStream(entry);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().parse(is);
        
        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList tNodes = si.getElementsByTagName("t");
            
            StringBuilder text = new StringBuilder();
            for (int j = 0; j < tNodes.getLength(); j++) {
                Element t = (Element) tNodes.item(j);
                text.append(t.getTextContent());
            }
            strings.add(text.toString());
        }
        
        is.close();
        return strings;
    }
    
    static void readSheetRows(ZipFile zip, List<String> strings, int maxRows) throws Exception {
        ZipEntry entry = zip.getEntry("xl/worksheets/sheet1.xml");
        if (entry == null) {
            System.err.println("sheet1.xml not found");
            return;
        }
        
        InputStream is = zip.getInputStream(entry);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc = dbf.newDocumentBuilder().parse(is);
        
        NodeList rows = doc.getElementsByTagName("row");
        int rowIndex = 0;
        
        for (int r = 0; r < Math.min(maxRows, rows.getLength()); r++) {
            Element row = (Element) rows.item(r);
            String rowNum = row.getAttribute("r");
            
            System.out.println("\nRow " + rowNum + ":");
            
            NodeList cells = row.getElementsByTagName("c");
            for (int c = 0; c < cells.getLength(); c++) {
                Element cell = (Element) cells.item(c);
                String cellRef = cell.getAttribute("r");
                String cellType = cell.getAttribute("t");
                
                String cellValue = "";
                
                // Get value from <v> element
                NodeList vNodes = cell.getElementsByTagName("v");
                if (vNodes.getLength() > 0) {
                    String vValue = vNodes.item(0).getTextContent();
                    
                    // If it's a string reference, resolve it
                    if ("s".equals(cellType) || "str".equals(cellType)) {
                        try {
                            int idx = Integer.parseInt(vValue);
                            if (idx >= 0 && idx < strings.size()) {
                                cellValue = strings.get(idx);
                            }
                        } catch (NumberFormatException e) {
                            cellValue = vValue;
                        }
                    } else {
                        cellValue = vValue;
                    }
                }
                
                // Or from <t> element
                if (cellValue.isEmpty()) {
                    NodeList tNodes = cell.getElementsByTagName("t");
                    if (tNodes.getLength() > 0) {
                        cellValue = tNodes.item(0).getTextContent();
                    }
                }
                
                if (!cellValue.isEmpty()) {
                    System.out.println("  " + cellRef + " = " + cellValue);
                }
            }
        }
        
        is.close();
    }
}
