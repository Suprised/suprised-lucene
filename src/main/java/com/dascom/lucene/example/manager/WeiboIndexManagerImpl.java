package com.dascom.lucene.example.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
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
import com.dascom.lucene.example.bean.WeiboIndexBean;
import com.dascom.lucene.example.index.LuceneInstance;

public class WeiboIndexManagerImpl implements WeiboIndexManager {

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
        // MultiFieldQueryParser parser = new MultiFieldQueryParser(LuceneInstance.Instance.getVersion(), new String[]{WeiboIndexBean.WB_CONTENT}, LuceneInstance.Instance.getAnalyzer());
        QueryParser parser = new QueryParser(LuceneInstance.Instance.getVersion(), WeiboIndexBean.WB_CONTENT, LuceneInstance.Instance.getAnalyzer());
        Query query = null;
        try {
            parser.setDefaultOperator(Operator.AND);//设置默认的操作符，默认空格为OR
            // parser.setAllowLeadingWildcard(true); // 允许*或者？为表达式的第一个
            query = parser.parse(queryString);
            System.out.println("搜索字符串：" + query.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        IndexSearcher searcher = LuceneInstance.Instance.getSearcher();
        // 按修改时间排序
        SortField sortField = new SortField(WeiboIndexBean.WB_PUBLISH_DATE, SortField.Type.LONG, true); // true-降序  false-升序 默认false
        Sort sort = new Sort(sortField);
        TopDocs docs = searcher.search(query, currPage * pageSize, sort);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<WeiboIndexBean> results = new ArrayList<WeiboIndexBean>();
        int start = (currPage - 1) * pageSize;
        Document doc ;
        WeiboIndexBean bean;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            bean = wrapWeiboIndexBean(query, doc);
            results.add(bean);
        }
        Pagination page = new Pagination(docs.totalHits, currPage, pageSize);
        page.setRow(results);
        return page;
    }
    
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
    
    private WeiboIndexBean wrapWeiboIndexBean(Query query,Document doc) {
        WeiboIndexBean bean = new WeiboIndexBean();
        bean.setId(doc.get(WeiboIndexBean.WB_ID));
        String summary = doc.get(WeiboIndexBean.WB_SUMMARY);
        bean.setSummary(highligher(summary, query, WeiboIndexBean.WB_SUMMARY));
        bean.setPublishDate(new Date(Long.valueOf(doc.get(WeiboIndexBean.WB_PUBLISH_DATE))));
        return bean;
    }

    @Override
    public void addIndex(WeiboIndexBean indexBean) {
        try {
            LuceneInstance.Instance.getWriter().addDocument(filed2Doc(indexBean));
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Document filed2Doc(WeiboIndexBean indexBean) {
        Document doc = new Document();
        String content = indexBean.getContent();
        doc.add(new StringField(WeiboIndexBean.WB_ID, indexBean.getId(), Store.YES));
        doc.add(new TextField(WeiboIndexBean.WB_CONTENT, content, Store.YES));
        String summary;
        if (content.length() > 200) {
            summary = content.substring(0, 200) + "...";
        } else {
            summary = content;
        }
        doc.add(new StringField(WeiboIndexBean.WB_SUMMARY, summary, Store.YES));
        doc.add(new LongField(WeiboIndexBean.WB_PUBLISH_DATE, new Date().getTime(), Store.YES));
        return doc;
    }

    @Override
    public void updateIndex(WeiboIndexBean indexBean) {
        try {
            LuceneInstance.Instance.getWriter().updateDocument(new Term(WeiboIndexBean.WB_ID, indexBean.getId()), filed2Doc(indexBean));
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void deleteIndex(String id) {
        try {
            LuceneInstance.Instance.getWriter().deleteDocuments(new Term(WeiboIndexBean.WB_ID, id));
            LuceneInstance.Instance.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
