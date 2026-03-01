package com.emissor.service;

import com.emissor.dto.ReportItemDTO;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ReportImportService {
    
    private static final String NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    
    /**
     * Checks if the XLSX file has a "Grade" column
     */
    public boolean hasGradeColumn(File file) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            List<String> sharedStrings = readSharedStrings(zipFile);
            Document sheetDoc = readXml(zipFile, "xl/worksheets/sheet1.xml");
            NodeList rowNodes = sheetDoc.getElementsByTagNameNS(NS, "row");
            
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element row = (Element) rowNodes.item(i);
                Map<String, String> rowValues = readRowCells(row, sharedStrings);
                Map<String, String> headerMap = detectHeaderRow(rowValues);
                if (!headerMap.isEmpty()) {
                    // Found header, now check if it has a Grade column
                    return rowValues.values().stream()
                            .map(this::normalize)
                            .anyMatch(v -> "grade".equals(v));
                }
            }
        }
        return false;
    }
    
    public List<ReportItemDTO> parseReport(File file) throws IOException {
        return parseReport(file, false);
    }
    
    /**
     * Parse report with optional grade mode
     * @param file The XLSX file to parse
     * @param useGradeMode If true, looks for "Grade" section and extracts codes before "-"
     */
    public List<ReportItemDTO> parseReport(File file, boolean useGradeMode) throws IOException {
        try (ZipFile zipFile = new ZipFile(file)) {
            List<String> sharedStrings = readSharedStrings(zipFile);
            Document sheetDoc = readXml(zipFile, "xl/worksheets/sheet1.xml");
            NodeList rowNodes = sheetDoc.getElementsByTagNameNS(NS, "row");
            
            int headerRowNum = -1;
            Map<String, String> headerColumns = Collections.emptyMap();
            
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element row = (Element) rowNodes.item(i);
                Map<String, String> rowValues = readRowCells(row, sharedStrings);
                Map<String, String> headerMap = detectHeaderRow(rowValues, useGradeMode);
                if (!headerMap.isEmpty()) {
                    headerRowNum = parseRowNumber(row);
                    headerColumns = headerMap;
                    break;
                }
            }
            
            if (headerRowNum == -1) {
                throw new IOException("Cabecalho nao encontrado no relatorio.");
            }
            
            List<ReportItemDTO> items = new ArrayList<>();
            
            if (useGradeMode) {
                // Grade mode: look for lines containing "Grade" and extract variant codes
                items = parseGradeMode(rowNodes, sharedStrings, headerRowNum, headerColumns);
            } else {
                // Normal mode: extract using reference column
                items = parseNormalMode(rowNodes, sharedStrings, headerRowNum, headerColumns);
            }
            
            return items;
        }
    }
    
    private List<ReportItemDTO> parseNormalMode(NodeList rowNodes, List<String> sharedStrings, 
                                                   int headerRowNum, Map<String, String> headerColumns) {
        List<ReportItemDTO> items = new ArrayList<>();
        
        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element row = (Element) rowNodes.item(i);
            int rowNum = parseRowNumber(row);
            if (rowNum <= headerRowNum) {
                continue;
            }
            
            Map<String, String> rowValues = readRowCells(row, sharedStrings);
            if (rowValues.isEmpty()) {
                continue;
            }
            
            String referencia = rowValues.getOrDefault(headerColumns.get("referencia"), "").trim();
            String descricao = rowValues.getOrDefault(headerColumns.get("descricao"), "").trim();
            String quantidadeRaw = rowValues.getOrDefault(headerColumns.get("quantidade"), "");
            
            // Skip summary rows
            if (isSummaryRow(referencia, descricao)) {
                break;
            }
            
            // Skip rows without reference
            if (referencia.isEmpty()) {
                continue;
            }
            
            int quantidade = parseQuantidade(quantidadeRaw);
            
            // Skip products with zero quantity
            if (quantidade <= 0) {
                continue;
            }
            
            items.add(new ReportItemDTO(referencia, descricao, quantidade));
        }
        
        return items;
    }
    
    private List<ReportItemDTO> parseGradeMode(NodeList rowNodes, List<String> sharedStrings,
                                             int headerRowNum, Map<String, String> headerColumns) {
        List<ReportItemDTO> items = new ArrayList<>();
        boolean waitingForGradeCode = false;
        String productDescription = "";  // Store the main product description
        String lastRowDescription = "";  // Store the description from the last processed row BEFORE "Grade"

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element row = (Element) rowNodes.item(i);
            int rowNum = parseRowNumber(row);
            if (rowNum <= headerRowNum) {
                continue;
            }

            Map<String, String> rowValues = readRowCells(row, sharedStrings);
            if (rowValues.isEmpty()) {
                continue;
            }

            // Get all cell values - check all columns for content
            String allContent = String.join(" ", rowValues.values()).trim();
                                                    String descricao = rowValues.getOrDefault(headerColumns.get("descricao"), "").trim(); // Get description from the description column (mesmo que modo normal)

            // Check if this row contains "Grade"
            if (normalize(allContent).contains("grade")) {
                // Use the last row's description as the product description
                productDescription = lastRowDescription;
                waitingForGradeCode = true;
                System.out.println("[GRADE] Encontrado marcador Grade. Descrição principal: " + productDescription);
                continue;
            }
            
            // If summary row, stop processing
            if (isSummaryRow("", descricao)) {
                break;
            }
            
            if (waitingForGradeCode) {
                // Look for a code in any column - should be in format "XXXXX - description"
                for (String cellValue : rowValues.values()) {
                    String gradeCode = extractGradeCode(cellValue);
                    if (!gradeCode.isEmpty()) {
                        // Found a valid grade code
                        // Extract only the description part (after the first "-")
                        String descricaoGrade = extractGradeDescription(cellValue);
                        // Combine product description with grade description
                        String fullDescription = productDescription.isEmpty() 
                            ? descricaoGrade 
                            : productDescription + " - " + descricaoGrade;
                        // Get the quantity from this row
                        String quantidadeRaw = rowValues.getOrDefault(headerColumns.get("quantidade"), "");
                        int quantidade = parseQuantidade(quantidadeRaw);
                        if (quantidade <= 0) {
                            quantidade = 1; // Default to 1 if no valid quantity found
                        }
                        System.out.println("[GRADE] Código: " + gradeCode + " | Descrição: " + fullDescription + " | Quantidade: " + quantidade);
                        items.add(new ReportItemDTO(gradeCode, fullDescription, quantidade));
                        break;  // Only take first valid code from this row
                    }
                }
                
                // Check if we should continue looking for more grades
                String referencia = rowValues.getOrDefault(headerColumns.get("referencia"), "").trim();
                
                // If we find a referencia that doesn't contain a dash, it's likely a new product
                if (!referencia.isEmpty() && !referencia.contains("-")) {
                    // New product section started, reset flag
                    waitingForGradeCode = false;
                    productDescription = "";  // Reset product description
                    lastRowDescription = "";  // Reset last row description
                }
            } else {
                // Not in grade mode yet - capture the description from the header column
                // This will be the LAST valid description before "Grade" marker
                if (!descricao.isEmpty()) {
                    System.out.println("[GRADE-CAPTURE-DEBUG] Descrição bruta: '" + descricao + "' | Validação: " + isValidProductDescription(descricao));
                }
                
                if (!descricao.isEmpty() && isValidProductDescription(descricao)) {
                    lastRowDescription = descricao;
                    System.out.println("[GRADE-CAPTURE] Descrição capturada: " + lastRowDescription);
                }
            }
        }
        
        return items;
    }
    
    private List<String> readSharedStrings(ZipFile zipFile) throws IOException {
        ZipEntry entry = zipFile.getEntry("xl/sharedStrings.xml");
        if (entry == null) {
            return Collections.emptyList();
        }
        
        Document doc = readXml(zipFile, "xl/sharedStrings.xml");
        NodeList stringNodes = doc.getElementsByTagNameNS(NS, "t");
        List<String> sharedStrings = new ArrayList<>(stringNodes.getLength());
        
        for (int i = 0; i < stringNodes.getLength(); i++) {
            Node node = stringNodes.item(i);
            sharedStrings.add(node.getTextContent());
        }
        
        return sharedStrings;
    }
    
    private Document readXml(ZipFile zipFile, String entryName) throws IOException {
        ZipEntry entry = zipFile.getEntry(entryName);
        if (entry == null) {
            throw new IOException("Arquivo nao encontrado no XLSX: " + entryName);
        }
        
        try (InputStream inputStream = zipFile.getInputStream(entry)) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            return factory.newDocumentBuilder().parse(inputStream);
        } catch (Exception e) {
            throw new IOException("Erro ao ler XML do XLSX: " + entryName, e);
        }
    }
    
    private Map<String, String> readRowCells(Element row, List<String> sharedStrings) {
        Map<String, String> values = new HashMap<>();
        NodeList cellNodes = row.getElementsByTagNameNS(NS, "c");
        
        for (int i = 0; i < cellNodes.getLength(); i++) {
            Element cell = (Element) cellNodes.item(i);
            String cellRef = cell.getAttribute("r");
            String column = extractColumn(cellRef);
            if (column.isEmpty()) {
                continue;
            }
            
            String value = readCellValue(cell, sharedStrings);
            if (value != null && !value.isEmpty()) {
                values.put(column, value);
            }
        }
        
        return values;
    }
    
    private String readCellValue(Element cell, List<String> sharedStrings) {
        NodeList valueNodes = cell.getElementsByTagNameNS(NS, "v");
        if (valueNodes.getLength() == 0) {
            return "";
        }
        
        String value = valueNodes.item(0).getTextContent();
        String type = cell.getAttribute("t");
        if ("s".equals(type)) {
            try {
                int idx = Integer.parseInt(value);
                if (idx >= 0 && idx < sharedStrings.size()) {
                    return sharedStrings.get(idx);
                }
            } catch (NumberFormatException ignored) {
                return "";
            }
        }
        
        return value;
    }
    
    private int parseRowNumber(Element row) {
        String rowRef = row.getAttribute("r");
        if (rowRef == null || rowRef.trim().isEmpty()) {
            return -1;
        }
        try {
            return Integer.parseInt(rowRef);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    private String extractColumn(String cellRef) {
        if (cellRef == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cellRef.length(); i++) {
            char ch = cellRef.charAt(i);
            if (Character.isLetter(ch)) {
                sb.append(ch);
            } else {
                break;
            }
        }
        return sb.toString();
    }
    
    private Map<String, String> detectHeaderRow(Map<String, String> rowValues) {
        return detectHeaderRow(rowValues, false);
    }
    
    private Map<String, String> detectHeaderRow(Map<String, String> rowValues, boolean useGradeMode) {
        Map<String, String> result = new HashMap<>();
        
        for (Map.Entry<String, String> entry : rowValues.entrySet()) {
            String normalized = normalize(entry.getValue());
            if ("grade".equals(normalized)) {
                result.put("grade", entry.getKey());
            } else if ("referencia".equals(normalized) || "reference".equals(normalized)) {
                result.put("referencia", entry.getKey());
            } else if ("descricao".equals(normalized) || "descricao produto".equals(normalized) || 
                       "description".equals(normalized) || "descricao do produto".equals(normalized) ||
                       "codigo".equals(normalized) || "code".equals(normalized)) {
                result.put("descricao", entry.getKey());
            } else if ("qtde.".equals(normalized) || "qtde".equals(normalized) || 
                       "quantidade".equals(normalized) || "qty".equals(normalized) ||
                       "estoque".equals(normalized) || "stock".equals(normalized)) {
                result.put("quantidade", entry.getKey());
            }
        }
        
        if (useGradeMode) {
            // In grade mode, we just need Description column (can be any of the data columns)
            if (result.containsKey("descricao")) {
                return result;
            }
            // If no descricao found, accept any non-empty header as a valid header for grade processing
            if (!rowValues.isEmpty()) {
                result.put("descricao", rowValues.keySet().iterator().next());
                return result;
            }
        } else {
            // In normal mode, we need Reference, Description, and Quantity columns
            if (result.containsKey("referencia") && result.containsKey("descricao") && result.containsKey("quantidade")) {
                return result;
            }
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * Extract grade code from grade string
     * e.g., "13058080002 - ROSA - 06" -> "13058080002"
     * or "13055120004 - VERD - 14" -> "13055120004"
     */
    private String extractGradeCode(String gradeValue) {
        if (gradeValue == null || gradeValue.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = gradeValue.trim();
        
        // Look for the first dash/hyphen (with or without spaces)
        int dashIndex = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (ch == '-' || ch == '–' || ch == '−') {
                dashIndex = i;
                break;
            }
        }
        
        if (dashIndex > 0) {
            String code = trimmed.substring(0, dashIndex).trim();
            // Verify it looks like a numeric code (all digits)
            if (!code.isEmpty() && code.matches("^[0-9]+$")) {
                return code;
            }
        }
        
        return "";
    }
    
    /**
     * Extract grade description from grade string
     * e.g., "13058080002 - ROSA - 06" -> "ROSA - 06"
     * or "13055120004 - VERD - 14" -> "VERD - 14"
     */
    private String extractGradeDescription(String gradeValue) {
        if (gradeValue == null || gradeValue.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = gradeValue.trim();
        
        // Look for the first dash/hyphen (with or without spaces)
        int dashIndex = -1;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (ch == '-' || ch == '–' || ch == '−') {
                dashIndex = i;
                break;
            }
        }
        
        if (dashIndex > 0) {
            String beforeDash = trimmed.substring(0, dashIndex).trim();
            // Only return description if the part before dash is numeric (a valid code)
            if (!beforeDash.isEmpty() && beforeDash.matches("^[0-9]+$")) {
                return trimmed.substring(dashIndex + 1).trim();
            }
        }
        
        // If no valid code found, return the whole string
        return trimmed;
    }
    
    private int parseQuantidade(String value) {
        if (value == null) {
            return 0;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return 0;
        }
        
        if (normalized.contains(",") && normalized.contains(".")) {
            normalized = normalized.replace(".", "").replace(",", ".");
        } else if (normalized.contains(",")) {
            normalized = normalized.replace(",", ".");
        }
        
        try {
            return new BigDecimal(normalized).intValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private boolean isSummaryRow(String referencia, String descricao) {
        String refNorm = normalize(referencia);
        String descNorm = normalize(descricao);
        return "resumo total".equals(refNorm) || "resumo total".equals(descNorm);
    }
    
    /**
     * Validate if a string looks like a valid product description
     */
    private boolean isValidProductDescription(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = text.trim();
        String normalized = normalize(trimmed);
        
        // Must have meaningful length (reduced from 10 to 8 for more flexibility)
        if (trimmed.length() < 8) {
            return false;
        }
        
        // Should not be just numbers and punctuation
        if (trimmed.matches("^[0-9.,\\-\\s]+$")) {
            return false;
        }
        
        // Filter out common UI/filter texts, headers and company names
        if (normalized.contains("filtro") || 
            normalized.contains("aplicado") ||
            normalized.contains("selecione") ||
            normalized.contains("buscar") ||
            normalized.contains("pesquisar") ||
            normalized.contains("empresa") ||
            normalized.contains("ltda") ||
            normalized.contains("cnpj") ||
            normalized.equals("grade") ||
            normalized.equals("codigo") ||
            normalized.equals("referencia") ||
            normalized.equals("descricao") ||
            normalized.equals("localizacao") ||
            normalized.equals("quantidade") ||
            normalized.equals("qtde") ||
            normalized.contains("estoque") ||
            normalized.contains("total")) {
            return false;
        }
        
        // Should contain letters (product descriptions have text)
        if (!trimmed.matches(".*[a-zA-ZÀ-ÿ].*")) {
            return false;
        }
        
        return true;
    }
    
    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim().toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
