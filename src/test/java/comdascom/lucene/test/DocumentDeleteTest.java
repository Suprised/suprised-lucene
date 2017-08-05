package comdascom.lucene.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

/**
 * 
 * (一个索引的增删查改的例子)
 * 
 * 删除索引中的文档
 * 
 * 在测试运行之前，基类会重新创建含有两个文档的索引
 * 
 * IndexReder 为单利，每个应用只能有一个Reader实例
 */
public class DocumentDeleteTest extends BaseIndexsTest {

    public void testAddIndex() throws IOException {
        // 增加索引
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, getAnalyzer());
        // config.setOpenMode(OpenMode.CREATE);
        IndexWriter writer = new IndexWriter(directory, config);
        Document document = new Document();
        document.add(new StringField("id", "100", Store.YES));
        document.add(new LongField("age", 2000, Store.YES));
        writer.updateDocument(new Term("id", "1100"), document);
        writer.close();

        printResults("id", "1");
        printResults("id", "2");
        printResults("id", "3");
        printResults("id", "4");
        printResults("id", "100");
    }

    /**
     * 删除索引
     */
    public void testDeleteBeforeIndexMerge() throws IOException {
        // IndexReader reader = DirectoryReader.open(directory);
        IndexReader reader = getReader();
        // 每个索引文档都有一个唯一标号，对象变化从0开始(编号不是唯一不变，可以重新编号)
        System.out.println(reader.numDocs());
        System.out.println(reader.maxDoc());
        System.out.println(reader.numDeletedDocs());
        // assertEquals(2, reader.maxDoc());//下一个Document对象的编号是2
        // assertEquals(2, reader.numDocs());//索引中有两个文档

        //reader.close();

        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(LUCENE_VERSION, getAnalyzer()));
        Query query = new TermQuery(new Term("id", "1"));
        writer.deleteDocuments(query); // 标记为删除，但是没有真正删掉
        // writer.deleteDocuments(new Term("id","1"), new Term("id","2")); // 同时删除两个

        reader = getReader();
        System.out.println(reader.numDocs());
        System.out.println(reader.maxDoc());
        System.out.println(reader.numDeletedDocs());
        //reader.close();

        // writer.rollback(); // 恢复删除
        writer.close(); // 真正删掉(只是移动到回收站，可以恢复)

        reader = getReader();
        System.out.println(reader.numDocs());
        System.out.println(reader.maxDoc());
        System.out.println(reader.numDeletedDocs());
        //reader.close();

        IndexWriter writer2 = new IndexWriter(directory, new IndexWriterConfig(LUCENE_VERSION, getAnalyzer()));
        writer2.forceMergeDeletes(); // 清空回收站的删除索引
        // writer2.deleteAll(); // 删除全部掉索引
        reader = getReader();
        System.out.println(reader.numDocs());
        System.out.println(reader.maxDoc());
        System.out.println(reader.numDeletedDocs());
        //reader.close();
        writer2.close();

        reader = getReader();
        System.out.println(reader.numDocs());
        System.out.println(reader.maxDoc());
        System.out.println(reader.numDeletedDocs());
        //reader.close();
    }

    /**
     * 更新索引
     */
    public void testUpdateIndex() throws IOException {
        printResults("id", "1"); // 修改前
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(LUCENE_VERSION, getAnalyzer()));
        Document doc = new Document();
        doc.add(new StringField("id", "3", Store.YES));
        doc.add(new TextField("city", "中国上海", Store.YES));
        doc.add(new StoredField("country", "中国"));
        doc.add(new TextField("contents", "上海是东方明珠", Store.YES));

        writer.updateDocument(new Term("id", "1"), doc); // 将id为1的文档改为3.
        // writer.updateDocument(new Term("contents","在"), doc, getAnalyzer()); // 如果没有找到要修改的，则增加一个文档
        printResults("id", "1"); // 修改未提交

        // writer.rollback(); // 回滚
        writer.close(); // 正式修改

        printResults("id", "3"); // 修改后
        printResults("id", "2");
        printResults("id", "1"); // 修改后
    }

    /**
     * 查询索引
     */
    public void testQueryIndex() throws IOException {
        int pageSize = 4;
        int currPage = 1;
        String field = "contents";
        // String queryString = "北京 +city:北 +id:1"; // 搜索多个Field
        String queryString = "time:[201406 TO 201408]";// RangeQuery 根据范围来搜索 time 在201406-201408之间
        IndexSearcher searcher = new IndexSearcher(getReader());
        List<Document> results = doPageSearch(searcher, field, queryString, currPage, pageSize);
        for (Document doc : results) {
            System.out.println("----------------------------------");
            System.out.println(doc.get("id"));
            System.out.println(doc.get("city"));
            System.out.println(doc.get("country"));
            System.out.println(doc.get("contents"));
            System.out.println(doc.get("long"));
            System.out.println(doc.get("time"));
            System.out.println("----------------------------------");
        }
    }

    /**
     * 分页查找
     * 
     * @param searcher
     * @param field
     * @param queryString
     * @return
     */
    private List<Document> doPageSearch(IndexSearcher searcher, String field, String queryString, int currPage, int pageSize)
        throws IOException {
        // Term t = new Term(field, queryString);

        QueryParser parser = new QueryParser(LUCENE_VERSION, field, getAnalyzer());
        Query query = null;
        try {
            query = parser.parse(queryString);
            System.out.println(query.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 自定义标注高亮文本的标签
        // SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<span class=\"hightlight\">", "</span>");
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b>", "</b>");
        Highlighter highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(new SimpleFragmenter(50)); // 减少高亮的默认大小，默认是100

        TopDocs docs = searcher.search(query, currPage * pageSize);
        System.out.println("总条数：" + docs.totalHits);
        ScoreDoc[] hits = docs.scoreDocs;
        List<Document> results = new ArrayList<Document>();
        int start = (currPage - 1) * pageSize;
        Document doc = null;
        for (int i = start; i < hits.length; i++) {
            doc = searcher.doc(hits[i].doc);
            Explanation explanation = searcher.explain(query, i);
            System.out.println(explanation.toString());
            String contents = doc.get("contents");
            TokenStream tokenStream = getAnalyzer().tokenStream("contents", contents);
            /*
             * String fragment; try { fragment = highlighter.getBestFragment(tokenStream, contents);
             * System.out.println(fragment); } catch (InvalidTokenOffsetsException e) { e.printStackTrace(); }
             */
            results.add(doc);
        }
        return results;
    }

    private void printResults(String field, String queryString) throws IOException {
        IndexSearcher searcher = new IndexSearcher(getReader());
        Term t = new Term(field, queryString);
        TopDocs docs = searcher.search(new TermQuery(t), 10);
        ScoreDoc[] hits = docs.scoreDocs;
        for (ScoreDoc doc : hits) {
            Document document = searcher.doc(doc.doc);
            System.out.println("----------------------------------");
            System.out.println("id:" + document.get("id"));
            System.out.println("city:" + document.get("city"));
            System.out.println("country:" + document.get("country"));
            System.out.println("contents:" + document.get("contents"));
            System.out.println("age:" + document.get("age"));
            System.out.println("----------------------------------");
        }
    }

    @Override
    protected Analyzer getAnalyzer() {
        return new StandardAnalyzer(LUCENE_VERSION);
    }
}
