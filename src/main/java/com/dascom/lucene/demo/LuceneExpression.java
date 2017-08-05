package com.dascom.lucene.demo;


import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class LuceneExpression {

    private static final Version version = Version.LUCENE_46;
    private static final Analyzer analyzer = new StandardAnalyzer(version);//和索引时用的分词器一致
    
    @Test
    public void testQueryPraser() throws Exception {
        QueryParser parser = new QueryParser(version, "content", analyzer);//默认在content域中查找
        // QueryParser parser = new MyQueryParser(version, "content", analyzer);//默认在content域中查找, 重写了getRangeQuery方法， 可以进行数字范围查询
        Query query = null; 
        
        //查找content（默认域）中存在软件的结果
        query = parser.parse("软件");
        
        //查找name域中有java的结果
        query = parser.parse("name:java");
        
        //使用通配符
        query = parser.parse("name: j*");//默认*号不能出现在最前端， 会影响效率, 使用parser.setAllowLeadingWildcard(true)打开
        query = parser.parse("lu*n?");//只能在单个分词中使用通配符， 即 使用 “lucene*action”就匹配不到
        query = parser.parse("lucene test");//默认lucene和action是或关系
        query = parser.parse("lucene OR test");//同上, OR必须大写
        query = parser.parse("lucene AND test");//与关系
        query = parser.parse("- lucene + test");//存在test但不存在lucene
        //query = parser.parse("size:[100 TO 200]");//这个query是TermRangeQuery所以不能用于数字（日期）范围查询
        query = parser.parse("name:[java TO java]");//查找有a-z字母的    {}不包含
        //数字范围查询需要重写QueryParser的getRangeQuery方法
        query = parser.parse("date:[1334550379955 TO 1334550379955]");
        query = parser.parse("\"lucene action\"~1");//1即phraseQuery中的slop=1
        query = parser.parse("name:xava~0.74");//加上~代表模糊查询， 要和上面的额短语查询相区别开来，另外~后面可以加一个浮点数去顶相似度
        //查询大小在145到150， 名称与javv相似， 创建时间在1334550379955之前的 不要， lucene与action相距1
        query = parser.parse("size:[145 TO 150] + name:javv~ - date:[1 TO 1334550379954] + \"lucene action\"~1");
        doSearch(query);
    }

    private void doSearch(Query query) throws Exception{
        Directory dir = FSDirectory.open(new File("E:/lucene/index"));
        IndexReader reader = IndexReader.open(dir); 
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs hits = searcher.search(query, 100);
        System.out.println("共找到" + hits.totalHits + "条记录");
        ScoreDoc[] docs = hits.scoreDocs;
        for(int i = 0; i < docs.length; i++){
            int docId = docs[i].doc;
            Document doc = searcher.doc(docId);
            System.out.println("name: " + doc.get("name"));
            System.out.println("date:" + doc.get("date"));
            System.out.println("size: " + doc.get("size"));
            System.out.println("content: " + doc.get("content"));
        }
    }
}

