package com.dascom.lucene.example.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

import com.cloverworxs.common.Pagination;
import com.dascom.lucene.example.bean.FileIndexBean;
import com.dascom.lucene.example.index.LuceneInstance;

public class FileIndexManagerImpl implements FileIndexManager {

    private static List<String> fileExts;
    
    static {
        // 可以进行索引的文件后缀
        fileExts = Arrays.asList("docx","doc","xls","xlsx","txt","jsp","java","xsd","js","xml","sql","html","htm","css","pdf");
    }
    
    @Override
    public void addIndex(File dir) {
        try {
            indexDocs(dir, true);
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteIndex(String id) {
        try {
            LuceneInstance.Instance.getWriter().deleteDocuments(new Term(FileIndexBean.PATH, id));
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateIndex(File dir) {
        try {
            indexDocs(dir, false);
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Pagination findByIndex(String condition, int currPage, int pageSize) {
        try {
            return doPageSearch(condition, currPage, pageSize);
        } catch (IOException e) {
            e.printStackTrace();
            return new Pagination(null);
        }
    }
    
    private Pagination doPageSearch(String queryString, int currPage, int pageSize) throws IOException {
        // 多字段查询
        MultiFieldQueryParser parser = new MultiFieldQueryParser(LuceneInstance.Instance.getVersion(), new String[]{FileIndexBean.PATH, FileIndexBean.CONTENTS}, LuceneInstance.Instance.getAnalyzer());
        Query query = null;
        try {
            parser.setDefaultOperator(Operator.AND);//设置默认的操作符，默认空格为OR
            parser.setAllowLeadingWildcard(true); // 允许*或者？为表达式的第一个
            query = parser.parse(queryString);
            System.out.println("搜索字符串：" + query.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        IndexSearcher searcher = LuceneInstance.Instance.getSearcher();
        // 按修改时间排序
        SortField sortField = new SortField(FileIndexBean.MODIFYED, SortField.Type.LONG);
        Sort sort = new Sort(sortField);
        TopDocs docs = searcher.search(query, currPage * pageSize, sort);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<FileIndexBean> results = new ArrayList<FileIndexBean>();
        int start = (currPage - 1) * pageSize;
        Document doc ;
        FileIndexBean bean;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            bean = wrapIndexBean(query, doc);
            results.add(bean);
        }
        Pagination page = new Pagination(docs.totalHits, currPage, pageSize);
        page.setRow(results);
        return page;
    }
    
    /**
     * 高亮显示搜索的字
     */
    /*private String highligher(String text,Query query,String field) {
        try {
            // 自定义标注高亮文本的标签
            SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color='red'><b>", "</b></font>");
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
            highlighter.setTextFragmenter(new SimpleFragmenter(300)); // 减少高亮的默认大小，默认是100
            String path = highlighter.getBestFragment(LuceneInstance.Instance.getAnalyzer(),field, text);
            if(path == null) {
                if(text.length()>=200) {
                    text = text.substring(0, 200);
                    text=text + "....";
                }
                return text;
            } else {
                return path.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }
        return text;
    }*/
    
    /**
     * 高亮显示搜索的字
     */
    private String highligher(String text, Query query, String field) {
        try {
            QueryScorer scorer = new QueryScorer(query);
            Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
            Formatter formatter = new SimpleHTMLFormatter("<font color='red'><b>", "</b></font>");
            Highlighter lighter = new Highlighter(formatter, scorer);
            lighter.setTextFragmenter(fragmenter);
            String ht = lighter.getBestFragment(LuceneInstance.Instance.getAnalyzer(), field, text);
            if (ht == null) {
                if (text.length() >= 200) {
                    text = text.substring(0, 200);
                    text = text + "....";
                }
                return text;
            } else {
                return ht.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidTokenOffsetsException e) {
            e.printStackTrace();
        }
        return text;
    }
    
    private FileIndexBean wrapIndexBean(Query query,Document doc) {
        FileIndexBean bean = new FileIndexBean();
        String path = doc.get(FileIndexBean.PATH);
        bean.setPath(highligher(path, query, FileIndexBean.PATH));
        bean.setLastModifiedDate(new Date(Long.valueOf(doc.get(FileIndexBean.MODIFYED))));
        return bean;
    }

    public static AtomicInteger atomicInt = new AtomicInteger(0); // 实现原子更新,计数器
    
    /**
     * 创建索引
     * 
     * @param writer
     * @param file
     * @throws IOException
     */
    private void indexDocs(File file, boolean create) throws IOException {
        if (!file.canRead()) {
            return;
        }
        if (file.isDirectory()) { // 目录递归
            String[] files = file.list();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    indexDocs(new File(file, files[i]), create);
                }
            }
        } else { //创建文件索引
            String ext = FilenameUtils.getExtension(file.getName());
            if (!fileExts.contains(ext)) {
                return;
            }
            // 每个文件，作为一个Document
            Document doc = new Document(); 
            // 每个文档有多个Field
            Field pathField = new TextField(FileIndexBean.PATH, file.getPath(), Field.Store.YES);
            doc.add(pathField); // 将路径作为一个Filed
            Field titleField = new StringField(FileIndexBean.TITLE, file.getName(), Field.Store.YES);
            doc.add(titleField);// 将标题作为一个Field
            doc.add(new LongField(FileIndexBean.MODIFYED, file.lastModified(), Field.Store.YES)); // 最后修改时间
            String contents = FileUtils.readFileToString(file, "UTF-8");
            // 摘要查询出来之后再处理
            /*long size = contents.length();
            if (size > 500) {// 摘要
                doc.add(new StoredField(FileIndexBean.SUMMARY, contents.substring(0, 500) + "..."));
            } else {
                doc.add(new StoredField(FileIndexBean.SUMMARY, contents));
            }*/
            doc.add(new TextField(FileIndexBean.CONTENTS, contents, Store.NO)); //内容

            if (create) {
                System.out.println("adding " + file); // 添加索引
                LuceneInstance.Instance.getWriter().addDocument(doc);
            } else {
                System.out.println("updating " + file);
                LuceneInstance.Instance.getWriter().updateDocument(new Term(FileIndexBean.PATH, file.getPath()), doc); // 更新文件路径索引
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
}
