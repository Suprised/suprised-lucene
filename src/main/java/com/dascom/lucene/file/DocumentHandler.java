package com.dascom.lucene.file;

import java.io.InputStream;

import org.apache.lucene.document.Document;

/**
 * 不同的文件转为Lucene的Document
 */
public interface DocumentHandler {

    /**
     * 根据文件流创建一个Lucene的Document 
     * 
     * @param inputStream
     *          转为文档的文件流
     * @return
     *          一个Lucene的Document
     * @throws DocumentHandlerException
     *          处理异常
     */
    public Document getDocument(InputStream inputStream) throws DocumentHandlerException ;
    
}
