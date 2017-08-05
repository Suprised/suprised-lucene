package com.dascom.lucene.example.index;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;

/**
 * 全文检索帮助类
 * 
 */
public enum LuceneInstance {
    
    Instance;
    
    public static final Version LUCENE_VERSION = LuceneCosts.LUCENE_VERSION;
    
    private static File indexDirs;
    private static IndexWriter writer;
    private static IndexReader reader;
    private static Directory directory;
    private static Analyzer analyzer;
    
    static {
        //indexDirs = new File("D:\\lucene_indexs\\fileIndex"); // 文件索引的位置
        indexDirs = new File("D:\\lucene_indexs\\fileIndex_mms"); // 文件索引的位置
        //indexDirs = new File("D:\\lucene_indexs\\fileIndex_ik"); // 文件索引的位置
        // analyzer = new StandardAnalyzer(LUCENE_VERSION); // 标准分词
        analyzer = new MMSegAnalyzer(); // 采用MMseg4j分词
        // analyzer = new IKAnalyzer();// 采用IK分词
        IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_VERSION, analyzer);
        iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
        try {
            directory = FSDirectory.open(indexDirs);
            writer = new IndexWriter(directory, iwc);
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获得Writer
     */
    public IndexWriter getWriter() {
        return writer;
    }
    
    /**
     * 获得Searcher
     */
    public IndexSearcher getSearcher() throws IOException{
        if (reader == null) {
            reader = DirectoryReader.open(directory);
        } else {
            IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader)reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
            }
        }
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
    
    /**
     * 获得分词器
     */
    public Analyzer getAnalyzer() {
        return analyzer;
    }
    
    /**
     * 获得Lucene的版本
     */
    public Version getVersion() {
        return LUCENE_VERSION;
    }
    
    /**
     * 提交索引
     */
    public void commit() {
        try {
            writer.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
