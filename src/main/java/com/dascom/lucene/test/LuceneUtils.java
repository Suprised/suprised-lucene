package com.dascom.lucene.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryTermScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/**
 * lucene工具类
 */
public final class LuceneUtils {
    
    private LuceneUtils() {
    }
    
    private static String getIndexDirectoryPath(String directoryPath) {
        String root = LuceneUtils.class.getClassLoader().getResource("").getPath();
        return root+directoryPath;
    }
    
    public static File getIndexDirectoryFile(String directoryPath) throws IOException {
        String root = LuceneUtils.class.getResource("/").getPath();
        return new File(root+directoryPath);
    }
    
    
    private static String getContent(File file) throws IOException {
        BufferedReader reader=null;
        try {
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
        String line = null;
        StringBuilder result= new StringBuilder();
        while((line=reader.readLine())!=null) {
            result.append(line);            
        }
        return result.toString();
        }finally {
            if(reader != null)
                reader.close();
        }
        
    }
    
    private static Document createDocument(File file) throws IOException {
        Document doc = new Document();
        doc.add(new LongField("size", file.length(),Store.YES));
        doc.add(new TextField("title", file.getName(), Store.YES));
        doc.add(new TextField("content",getContent(file) , Store.YES));
        doc.add(new TextField("path", file.getAbsolutePath(),Store.YES));
        return doc;
    }
    
    public static Document getDocument(String indexPath, Query query) throws IOException {
        Directory directory = new SimpleFSDirectory(new File(LuceneUtils.class.getResource(indexPath).getPath()));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, 100);
        Document document = searcher.doc(docs.scoreDocs[0].doc);
        return document;
    }   
    
    public static Directory getDirectory(String directoryPath) throws IOException {
        return new SimpleFSDirectory(new File(getIndexDirectoryPath(directoryPath)));
    }
    
    public static void createIndexFromFile(File file,String indexPath) throws IOException {
        createIndexFromFile(file,indexPath,true);
    }
    
    public static void createIndexFromFile(File file,String indexPath,boolean isMerge) throws IOException {
        createIndexFromFile(file,indexPath,new StandardAnalyzer(Version.LUCENE_46),isMerge);
    }
    public static void createIndexFromFile(File file,String indexPath,Analyzer analyzer,boolean isMerge) throws IOException {
        IndexWriter writer = null;
        try {
        Directory dir = new SimpleFSDirectory(getIndexDirectoryFile(indexPath));
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        writer = new IndexWriter(dir, conf);
        Document doc = createDocument(file);
        writer.addDocument(doc);
        if(isMerge)
            writer.forceMergeDeletes();
        writer.close();
        }finally {
            if(writer != null)
                writer.close();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void printTokenStreamDetails(Analyzer analyzer,String text) throws IOException {
        TokenStream stream=null;
        try {
        stream = analyzer.tokenStream("", text);
        stream.reset();     
        Class[] attrs = new Class[] {CharTermAttribute.class,OffsetAttribute.class,PositionLengthAttribute.class};
        for(Class attr : attrs) {
            if(!stream.hasAttribute(attr)) {
                stream.addAttribute(attr);
            }
        }
        CharTermAttribute cta = stream.getAttribute(CharTermAttribute.class);
        OffsetAttribute oa = stream.getAttribute(OffsetAttribute.class);
        PositionLengthAttribute ola=stream.getAttribute(PositionLengthAttribute.class);
        System.out.println("分词如下：");
        while(stream.incrementToken()) {    
            System.out.println(cta+"("+ola.getPositionLength()+","+oa.startOffset()+","+oa.endOffset()+")");
        }
        
        }catch(IOException e) {
            throw e;
        }finally{
            if(stream !=null)
                stream.close();
        }
    }
    
    public static Highlighter getHighlighter(Query query,String preTag,String postTag)
    {
        Formatter formatter= new SimpleHTMLFormatter(preTag, postTag);
        Scorer fragmentScorer = new QueryTermScorer(query);
        Highlighter hilighter = new Highlighter(formatter, fragmentScorer);
        return hilighter;
    }
    
    public static String getHilightString(Highlighter hilighter,Analyzer analyzer,String indexPath,Query query) throws IOException, InvalidTokenOffsetsException {
        hilighter.setTextFragmenter(new SimpleFragmenter(100));
        Document document = getDocument(indexPath, query);
        TokenStream stream = analyzer.tokenStream("content", document.get("content"));
        String result=hilighter.getBestFragment(stream, document.get("content"));
        return result;
    }
    
    public static String getHilightString(Analyzer analyzer,String indexPath,Query query) throws IOException, InvalidTokenOffsetsException {
        Highlighter hilighter = getHighlighter(query, "<B>", "<B>");
        hilighter.setTextFragmenter(new SimpleFragmenter(100));
        return LuceneUtils.getHilightString(hilighter, analyzer, indexPath, query);     
        
    }

}