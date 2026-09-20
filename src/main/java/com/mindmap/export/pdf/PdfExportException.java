package com.mindmap.export.pdf;

/**
 * Exception thrown when an error occurs during PDF generation.
 */
public class PdfExportException extends RuntimeException {
    
    public PdfExportException(String message) {
        super(message);
    }
    
    public PdfExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
