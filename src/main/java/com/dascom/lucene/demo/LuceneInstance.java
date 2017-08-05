package com.dascom.lucene.demo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
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
import com.dascom.lucene.index.Costs;
import com.dascom.lucene.index.LuceneTest;

/**
 * 全文检索帮助类
 * 
 */
public enum LuceneInstance {
    
    Instance;
    
    public static final Version LUCENE_VERSION = Version.LUCENE_46;
    
    private static final String PATTERN = "yyyy-MM-dd hh:mm:ss";
    
    public AtomicInteger atomicInt = new AtomicInteger(0); // 实现原子更新,计数器
    private static File indexDirs ;
    private static IndexWriter writer;
    private static IndexReader reader;
    private static Directory directory;
    private static Analyzer analyzer;
    
    private static List<String> fileExts;
    
    static {
        indexDirs = LuceneTest.fileIndexDir; //默认索引的位置
        analyzer = new StandardAnalyzer(LUCENE_VERSION); // 标准分词
        IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_VERSION, analyzer);
        iwc.setOpenMode(OpenMode.CREATE);
        try {
            directory = FSDirectory.open(indexDirs);
            writer = new IndexWriter(directory, iwc);
            reader = DirectoryReader.open(directory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 可以进行索引的文件后缀
        fileExts = Arrays.asList("docx","doc","xls","xlsx","txt","jsp","java","xsd","js","xml","sql","html","htm","css","pdf");
    }
    
    public IndexSearcher getIndexSearcher() throws IOException{
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
    
    public void createIndexs(List<LuceneIndexBean> objs , boolean isCreate) throws IOException {
        try {
            for (LuceneIndexBean obj : objs) {
                index(obj, isCreate);
            }
        } finally {
            writer.commit();
        }
    }
    
    public void createIndex(LuceneIndexBean obj , boolean isCreate) throws IOException {
        try {
            index(obj, isCreate);
        } finally {
            writer.commit();
        }
    }
    
    /**
     * 创建或者更新索引
     * 
     * @param isCreate true:创建 false:更新
     */
    private void index(LuceneIndexBean obj , boolean isCreate) throws IOException {
        Date start = new Date();
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

        if (isCreate) { // 创建索引
            System.out.println("adding " + obj); // 添加索引
            writer.addDocument(document);
        } else {
            System.out.println("updating " + obj);
            writer.updateDocument(new Term("key", obj.getKey()), document);
        }
        System.out.println(new Date().getTime() - start.getTime() + " total milliseconds");
    }
    
    public List<LuceneIndexBean> query(String queryString, int currPage, int pageSize) throws IOException {
        IndexSearcher searcher = getIndexSearcher();
        List<LuceneIndexBean> results = doPageSearch(searcher, "content", queryString, currPage, pageSize);
        return results;
    }
    
    public Pagination queryPage(String queryString, int currPage, int pageSize) throws IOException {
        IndexSearcher searcher = getIndexSearcher();
        Pagination page = doPageSearch2(searcher, "contents", queryString, currPage, pageSize);
        return page;
    }
    
    
    private LuceneIndexBean wrapIndexBean(Document doc) {
        LuceneIndexBean bean = new LuceneIndexBean();
        //bean.setContent(doc.get("content"));
        //bean.setUrl(doc.get("url"));
        // String date = doc.get("date");
        //if (date != null) {
        //    try {
        //        bean.setDate(new SimpleDateFormat(PATTERN).parse(date));
        //    } catch (java.text.ParseException e) {
        //        e.printStackTrace();
        //    }
        //}
        String title = doc.get(Costs.PATH);
        //if (title != null && title.length() > 100) {
        //    title = title.substring(0, 99) + "......";
        //}
        bean.setTitle(title);
        bean.setSummary(doc.get(Costs.SUMMARY));
        //bean.setKey(doc.get("key"));
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
        // QueryParser parser = new QueryParser(LUCENE_VERSION, field, getAnalyzer());
        // 多字段查询
        MultiFieldQueryParser parser = new MultiFieldQueryParser(LUCENE_VERSION, new String[]{field, Costs.PATH}, getAnalyzer());
        Query query = null;
        try {
            parser.setDefaultOperator(Operator.AND);//设置默认的操作符，默认空格为OR
            parser.setAllowLeadingWildcard(true); // 允许*或者？为表达式的第一个
            query = parser.parse(queryString);
            System.out.println("分词后：" + query.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // 按修改时间排序
        SortField sortField = new SortField(Costs.MODIFYED, SortField.Type.LONG);
        Sort sort = new Sort(sortField);
        TopDocs docs = searcher.search(query, currPage * pageSize, sort);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<LuceneIndexBean> results = new ArrayList<LuceneIndexBean>();
        int start = (currPage - 1) * pageSize;
        Document doc ;
        LuceneIndexBean bean;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            // String contents = doc.get("contents");
            bean = wrapIndexBean(doc);
            // 自定义标注高亮文本的标签
            // SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"hightlight\">", "</span>");
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color='red'><b>", "</b></font>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(300)); // 减少高亮的默认大小，默认是100
            // TokenStream tokenStream = getAnalyzer().tokenStream("title", bean.getTitle());
            try {
                String title = highlighter.getBestFragment(getAnalyzer(),Costs.TITLE, bean.getTitle());
                String summary = highlighter.getBestFragment(getAnalyzer(),Costs.SUMMARY, bean.getSummary());
                //if (StringUtils.isNotBlank(fragment) && fragment.length() > 200) {
                //    fragment = fragment.substring(0, 199) + "......";
                //}
                //bean.setContent(fragment);
                if (StringUtils.isNotBlank(title)) {
                    bean.setTitle(title);
                }
                if (StringUtils.isNotBlank(summary)) {
                    bean.setSummary(summary);
                }
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
            System.out.println("分词后：" + query.toString());
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
            String fragment = "";
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
        return analyzer;
    }
    
    /**
     * 将一个目录创建索引
     * 
     * @param dataDir 数据的目录
     * @throws Exception
     */
    public long createIndex(File dataDir, boolean create) throws Exception {
        Date start = new Date();
        atomicInt = new AtomicInteger(0); //计数器
        // IndexWriter writer = getIndexWriter(indexDir, create);
        indexDocs(dataDir);
        writer.commit();
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
    private void indexDocs(File file) throws IOException {
        if (!file.canRead()) {
            return;
        }
        if (file.isDirectory()) { // 目录递归
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(new File(file, files[i]));
                }
            }
        } else { //创建文件索引
            String ext = FilenameUtils.getExtension(file.getName());
            if (!fileExts.contains(ext)) {
                return;
            }

            Document doc = new Document(); // 每个文件，作为一个Document
            // 每个文档有多个Field

            Field pathField = new StringField(Costs.PATH, file.getPath(), Field.Store.YES);
            doc.add(pathField); // 将路径作为一个Filed

            Field titleField = new TextField(Costs.TITLE, file.getName(), Field.Store.YES);
            doc.add(titleField);// 将标题作为一个Field
            doc.add(new LongField(Costs.MODIFYED, file.lastModified(), Field.Store.YES)); // 最后修改时间

            // 内容
            String contents = FileUtils.readFileToString(file, "UTF-8");
            long size = contents.length();
            if (size > 500) {
                doc.add(new StoredField(Costs.SUMMARY, contents.substring(0, 500) + "..."));
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

            while (true) { // 计数
                int i = atomicInt.get();
                boolean flag = atomicInt.compareAndSet(i, ++i);
                if (flag) {
                    break;
                }
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
//        LuceneIndexBean indexBean = new LuceneIndexBean();
//        indexBean.setUrl("http://www.baidu.com?key=123456789");
//        indexBean.setContent("这条微博是要被Lucene索引的数据。");
//        indexBean.setDate(new Date());
//        indexBean.setKey("123456789");
//        LuceneInstance helper = LuceneInstance.Instance;
//        try {
//            helper.createIndex(indexBean, false);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
        List<LuceneIndexBean> beans = LuceneInstance.Instance.query("Lucene", 1, 10);
        for (LuceneIndexBean bean : beans) 
            System.out.println(bean);
    }
}
