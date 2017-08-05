package comdascom.lucene.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
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
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.dascom.lucene.index.Costs;

/**
 * Lucene 测试基类
 */
public class BaseIndexsTest extends TestCase {

    protected Directory directory;

    protected File indexDirs = new File("F:\\indexs\\testIndexs"); // 默认索引的位置

    protected static final Version LUCENE_VERSION = Costs.LUCENE_VERSION;

    protected String[] keywords = { "1", "2", "3", "4" };
    protected String[] unindexed = { "中国", "美国", "日本", "韩国" };
    protected String[] unstored = { "你好，我爱北京天安门你好。", "你好，姚明在休斯敦火箭队打球你好。", "日本东京你好东京", "韩国首尔你好首尔" };
    protected String[] text = { "中国北京", "美国休斯敦", "日本东京", "韩国首尔" };
    protected long[] longs = { 1001, 1002, 1003, 1004 };
    protected String[] times = { "201405", "201406", "201407", "201408" };

    private static IndexReader reader;
    
    /**
     * 所有测试开始都会执行该方法
     */
    public void setUp() throws IOException {
        directory = FSDirectory.open(indexDirs);
        IndexWriterConfig iwc = new IndexWriterConfig(LUCENE_VERSION, getAnalyzer());
        iwc.setOpenMode(OpenMode.CREATE); // 创建一个新的索引，覆盖原有的 清空之前的索引，然后重新创建新的索引
        // iwc.setOpenMode(OpenMode.CREATE_OR_APPEND); // 如果存在索引，则打开，不存在则创建一个新的 在之前的索引基础上增加索引， 数据会重复
        // iwc.setOpenMode(OpenMode.APPEND); //打开一个存在的索引 在之前的索引基础上增加索引， 数据会重复
        IndexWriter writer = new IndexWriter(directory, iwc);
        addDocuments(writer);
    }
    
    public IndexReader getReader() throws IOException{
        if (reader == null) {
            reader = DirectoryReader.open(directory);
        } else {
            IndexReader newReader = DirectoryReader.openIfChanged((DirectoryReader)reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
            }
        }
        return reader;
    }

    /**
     * 创建索引
     */
    protected void addDocuments(IndexWriter writer) throws IOException {
        Document doc;
        for (int i = 0; i < keywords.length; i++) {
            doc = new Document();
            // 索引，并储存，不分词
            Field fieldKey = new StringField("id", keywords[i], Store.YES);
            doc.add(fieldKey);
            Field fieldTime = new StringField("time", times[i], Store.YES);
            doc.add(fieldTime);
            // 储存 不分词 不索引
            Field fieldCountry = new StoredField("country", unindexed[i]);
            doc.add(fieldCountry);
            // 索引，不储存 ，分词
            Field fieldContent = new TextField("contents", unstored[i], Store.YES);
            // 不储存，进行分词
            // Field fieldContent = new TextField("contents", new BufferedReader(new StringReader(unstored[i])));
            doc.add(fieldContent);
            // 索引，储存， 分词
            Field fieldCity = new TextField("city", text[i], Store.YES);
            doc.add(fieldCity);
            // 不索引 不分词 存储
            Field fieldLong = new LongField("long", longs[i], Store.YES);
            doc.add(fieldLong);

            writer.addDocument(doc);
        }
        writer.close();
    }

    /**
     * 默认分词，子类可以重写
     */
    protected Analyzer getAnalyzer() {
        return new SimpleAnalyzer(LUCENE_VERSION);
    }
}
