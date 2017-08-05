package com.dascom.lucene.demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.cloverworxs.common.Pagination;

/**
 * 全文检索帮助类
 * 
 */
public class LuceneHelper {

    public static final Version LUCENE_VERSION = Version.LUCENE_46;
    
    private static final String PATTERN = "yyyy-MM-dd hh:mm:ss";
    
    protected File indexDirs = new File("F:\\indexs\\webTestIndexs"); //默认索引的位置
    
    private IndexWriter getIndexWriter(File indexDir, boolean create) throws IOException {
        Directory dir = FSDirectory.open(indexDir);
        Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION); // 标准分词
        IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_VERSION, analyzer);
        // 创建所以模式或者OpenMode.CREATE_OR_APPEND
        iwc.setOpenMode(create ? OpenMode.CREATE : OpenMode.CREATE_OR_APPEND);
        IndexWriter writer = new IndexWriter(dir, iwc);
        return writer;
    }
    
    /**
     * 创建或者更新索引
     * 
     * @param isCreate true:创建 false:更新
     */
    public void createIndex(LuceneIndexBean obj , boolean isCreate) throws IOException {
        Date start = new Date();
        IndexWriter writer = getIndexWriter(indexDirs, isCreate);
        Document document = new Document();
        // 保存并索引
        Field fieldKey = new StringField("key", obj.getKey(), Store.YES);
        document.add(fieldKey);
        // 索引并保存(支持分词)
        Field fieldContent = new TextField("content", obj.getContent(), Store.YES);
        document.add(fieldContent);
        // 保存，索引
        Field fieldIndexDate = new StoredField("indexDate", new SimpleDateFormat(PATTERN).format(obj.getDate()));
        document.add(fieldIndexDate);
        
        if (obj.getDate() != null) {
            Field fieldDate = new StoredField("date", new SimpleDateFormat(PATTERN).format(obj.getDate()));
            document.add(fieldDate);
        }
        
        if (obj.getTitle() != null){
            Field fieldTitle = new StoredField("title", obj.getTitle());
            document.add(fieldTitle);
        }
        
        Field fieldUrl = new StoredField("url", obj.getUrl());
        document.add(fieldUrl);

        if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
            System.out.println("adding " + obj); // 添加索引
            writer.addDocument(document);
        } else {
            System.out.println("updating " + obj);
            writer.updateDocument(new Term("key", obj.getKey()), document);
        }
        writer.close();
        Date end = new Date();
        System.out.println(end.getTime() - start.getTime() + " total milliseconds");
    }
    
    public List<LuceneIndexBean> query(String queryString, int currPage, int pageSize) throws IOException {
        Directory directory = FSDirectory.open(indexDirs);
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        List<LuceneIndexBean> results = doPageSearch(searcher, "content", queryString, currPage, pageSize);
//        return wrapIndexBeans(results);
        return results;
    }
    
    public Pagination queryPage(String queryString, int currPage, int pageSize) throws IOException {
        Directory directory = FSDirectory.open(indexDirs);
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        Pagination page = doPageSearch2(searcher, "content", queryString, currPage, pageSize);
        return page;
    }
    
    private LuceneIndexBean wrapIndexBean(Document doc) {
        LuceneIndexBean bean = new LuceneIndexBean();
        bean.setContent(doc.get("content"));
        bean.setUrl(doc.get("url"));
        String date = doc.get("date");
        if (date != null) {
            try {
                bean.setDate(new SimpleDateFormat(PATTERN).parse(date));
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }
        }
        String title = doc.get("title");
        if (title != null && title.length() > 100) {
            title = title.substring(0, 99) + "......";
        }
        bean.setTitle(title);
        bean.setKey(doc.get("key"));
        return bean;
    }
    
//    private List<LuceneIndexBean> wrapIndexBeans(List<Document> results) {
//        List<LuceneIndexBean> indexsBeans = new ArrayList<LuceneIndexBean>();
//        LuceneIndexBean bean = null;
//        for (Document doc : results) {
//            bean = new LuceneIndexBean();
//            bean.setContent(doc.get("content"));
//            bean.setUrl(doc.get("url"));
//            String date = doc.get("date");
//            if (date != null) {
//                try {
//                    bean.setDate(new SimpleDateFormat(PATTERN).parse(date));
//                } catch (java.text.ParseException e) {
//                    e.printStackTrace();
//                }
//            }
//            bean.setTitle(doc.get("title"));
//            bean.setKey(doc.get("key"));
//            indexsBeans.add(bean);
//        }
//        return indexsBeans;
//    }

    private Pagination doPageSearch2(IndexSearcher searcher, String field, String queryString, int currPage, int pageSize) throws IOException {
        QueryParser parser = new QueryParser(LUCENE_VERSION, field, getAnalyzer());
        Query query = null;
        try {
            query = parser.parse(queryString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        TopDocs docs = searcher.search(query, currPage * pageSize);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<LuceneIndexBean> results = new ArrayList<LuceneIndexBean>();
        int start = (currPage - 1) * pageSize;
        Document doc ;
        LuceneIndexBean bean;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            String contents = doc.get("content");
            bean = wrapIndexBean(doc);
            // 自定义标注高亮文本的标签
            // SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"hightlight\">", "</span>");
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(300)); // 减少高亮的默认大小，默认是100
            TokenStream tokenStream = getAnalyzer().tokenStream("content", contents);
            String fragment;
            try {
                fragment = highlighter.getBestFragment(tokenStream, contents);
                if (fragment.length() > 200) {
                    fragment = fragment.substring(0, 199) + "......";
                }
                bean.setContent(fragment);
//                System.out.println(fragment);
            } catch (InvalidTokenOffsetsException e) {
                e.printStackTrace();
            }
            results.add(bean);
        }
        Pagination page = new Pagination(docs.totalHits, currPage, pageSize);
        page.setRow(results);
        return page;
    }
    
    /**
     * 分页查找
     * 
     * @param searcher
     * @param field
     * @param queryString
     * @return
     */
    private List<LuceneIndexBean> doPageSearch(IndexSearcher searcher, String field, String queryString, int currPage, int pageSize) throws IOException {
        QueryParser parser = new QueryParser(LUCENE_VERSION, field, getAnalyzer());
        Query query = null;
        try {
            query = parser.parse(queryString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        TopDocs docs = searcher.search(query, currPage * pageSize);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<LuceneIndexBean> results = new ArrayList<LuceneIndexBean>();
        int start = (currPage - 1) * pageSize;
        Document doc ;
        LuceneIndexBean bean;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            String contents = doc.get("content");
            bean = wrapIndexBean(doc);
            // 自定义标注高亮文本的标签
            // SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"hightlight\">", "</span>");
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(300)); // 减少高亮的默认大小，默认是100
            TokenStream tokenStream = getAnalyzer().tokenStream("content", contents);
            String fragment;
            try {
                fragment = highlighter.getBestFragment(tokenStream, contents);
                if (fragment.length() > 300) {
                    fragment = fragment.substring(0, 299) + "......";
                }
                bean.setContent(fragment);
                System.out.println(fragment);
            } catch (InvalidTokenOffsetsException e) {
                e.printStackTrace();
            }
            results.add(bean);
        }
        return results;
    }
    
    private Analyzer getAnalyzer() {
        return new StandardAnalyzer(LUCENE_VERSION);
    }
    
    public static void main(String[] args) throws IOException {
//        LuceneIndexBean indexBean = new LuceneIndexBean();
//        indexBean.setUrl("http://www.baidu.com?key=123456789");
//        indexBean.setContent("这条微博是要被Lucene索引的数据。");
//        indexBean.setDate(new Date());
//        indexBean.setKey("123456789");
        LuceneHelper helper = new LuceneHelper();
//        try {
//            helper.createIndex(indexBean, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        List<LuceneIndexBean> beans = helper.query("Lucene", 1, 10);
        for (LuceneIndexBean bean : beans) 
            System.out.println(bean);
    }
}
