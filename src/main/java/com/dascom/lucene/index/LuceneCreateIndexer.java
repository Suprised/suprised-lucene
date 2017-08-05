package com.dascom.lucene.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.dascom.lucene.bean.TopicBaseBean;

public class LuceneCreateIndexer {
    
    public static AtomicInteger atomicInt = new AtomicInteger(0); // 实现原子更新,计数器
    
    private static List<String> fileExts;
    
    private static IndexWriter writer ;
    private static IndexReader reader ;
    
    static {
        fileExts = new ArrayList<String>(); // 可以进行索引的文件后缀
        fileExts.add("docx");
        fileExts.add("doc");
        fileExts.add("xls");
        fileExts.add("xlsx");
        fileExts.add("txt");
        fileExts.add("pdf");
        fileExts.add("jsp");
        fileExts.add("java");
        fileExts.add("xsd");
        fileExts.add("js");
        fileExts.add("xml");
        fileExts.add("sql");
        fileExts.add("html");
        fileExts.add("htm");
        fileExts.add("css");
    }
    
    public static IndexWriter getIndexWriter(File indexDir, boolean create)  throws Exception {
        if (writer != null)
            return writer;
        Directory dir = FSDirectory.open(indexDir);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46); // 标准分词
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
        // 创建所以模式或者OpenMode.CREATE_OR_APPEND
        iwc.setOpenMode(create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
//        iwc.setOpenMode(OpenMode.CREATE); 
        writer = new IndexWriter(dir, iwc);
        return writer;
    }
    
    public static void createIndex(List<TopicBaseBean> objs, String[] indexFields, File indexDir, boolean create) throws Exception {
        Date start = new Date();
        IndexWriter writer = getIndexWriter(indexDir, create);
        System.out.println(writer);
        int index = 0;
        for (TopicBaseBean obj : objs) {
            Document document = new Document();
            // 保存并索引
            Field field = new StringField("key", obj.getKey(), Store.YES);
            document.add(field);
            // 索引不保存(支持分词)
            Field fieldContent = new TextField("content", obj.getContent(), Store.NO);
            document.add(fieldContent);
            // 保存，索引
            Field fieldDate = new StoredField("lastReplyDate", obj.getLastReplyDate().toString());
            document.add(fieldDate);
            Field fieldTitle = new StringField("parentTitle", obj.getParentTitle(), Store.YES);
            document.add(fieldTitle);
            Field fieldCreator = new StoredField("creator", obj.getCreator());
            document.add(fieldCreator);
            
            if (index % 100 == 0) {
                fieldContent.setBoost(1.5f); // 设置优先级别
            }
            index++;
            
            if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                System.out.println("adding " + obj); // 添加索引
                writer.addDocument(document);
            } else {
                System.out.println("updating " + obj);
                writer.updateDocument(new Term("key"), document);
//                writer.updateDocument(new Term("content"), document); // 更新文件路径索引
//                writer.updateDocument(new Term("creator"), document); // 更新文件路径索引
            }
        }
        writer.commit();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }
    
    /**
     * 将一个目录创建索引
     * @param indexDir 索引的目录
     * @param dataDir 数据的目录
     * @throws Exception
     */
    public static long createIndex(File indexDir, File dataDir, boolean create) throws Exception {
        Date start = new Date();
        atomicInt = new AtomicInteger(0); //计数器
        IndexWriter writer = getIndexWriter(indexDir, create);
        indexDocs(writer, dataDir);
        writer.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
        return end.getTime() - start.getTime();
    }

    /**
     * 创建索引
     * 
     * @param writer
     * @param file
     * @throws IOException
     */
    private static void indexDocs(IndexWriter writer, File file) throws IOException {
        if (!file.canRead()) {
            return;
        }
        if (file.isDirectory()) { // 目录递归
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(writer, new File(file, files[i]));
                }
            }
        } else { //创建文件索引
            String ext = FilenameUtils.getExtension(file.getName());
            if (!fileExts.contains(ext)) {
                return;
            }
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
            } catch (FileNotFoundException fnfe) {
                return;
            }

            try {
                Document doc = new Document(); // 每个文件，作为一个Document
                //每个文档有多个Field
                
                Field pathField = new StringField(Costs.PATH, file.getPath(), Field.Store.YES);
                doc.add(pathField); // 将路径作为一个Filed 
                
                Field titleField = new TextField(Costs.TITLE,file.getName(), Field.Store.YES);
                doc.add(titleField);// 将标题作为一个Field
                doc.add(new LongField(Costs.MODIFYED, file.lastModified(), Field.Store.YES)); //最后修改时间

                // 内容
                String contents = FileUtils.readFileToString(file, "UTF-8");
                long size = contents.length();
                if (size > 500) {
                    doc.add(new StoredField(Costs.SUMMARY, contents.substring(0,500) + "..."));
                } else {
                    doc.add(new StoredField(Costs.SUMMARY, contents));
                }
                // doc.add(new TextField(Costs.CONTENTS, new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
                doc.add(new TextField(Costs.CONTENTS, contents, Store.NO));

                if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
                    System.out.println("adding " + file); // 添加索引
                    writer.addDocument(doc);
                } else {
                    System.out.println("updating " + file);
                    writer.updateDocument(new Term(Costs.PATH, file.getPath()), doc); // 更新文件路径索引
                }
                
                while(true) { // 计数
                    int i = atomicInt.get();
                    boolean flag = atomicInt.compareAndSet(i, ++i);
                    if (flag) {
                        break;
                    }
                }
            } finally {
                fis.close();
            }
        }
    }
    
    
    public static void main(String[] args) {
        String ext = FilenameUtils.getExtension("java.txt");
        System.out.println(ext);
    }
}
