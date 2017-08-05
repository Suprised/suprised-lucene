package com.dascom.lucene.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 搜索内容
 * 
 * @author Administrator
 * 
 */
public class LuceneSearchIndexer {
    
    /**
     * 根据文件内容进行匹配
     * @param indexDir
     * @param queryFile
     * @throws IOException
     */
    public static void search(File indexDir, File queryFile) throws IOException {
        search(indexDir, null, queryFile, null);
    }

    /**
     * 根据查询字符串进行匹配
     * @param indexDir
     * @param queryString
     * @throws IOException
     */
    public static void search(File indexDir, String queryString) throws IOException {
        search(indexDir, queryString, null, null);
    }
    
    public static void searchByFields(File indexDir, String queryString , String field) throws IOException {
        search(indexDir, queryString, null, field);
    }
    
    /**
     * 搜索 指定的字符串或者文件内容
     * 
     * @param indexDir 索引位置
     * @param queryString 要搜索的字符字符串
     * @param queries 要搜索的文件中的内容
     * @throws IOException
     * 
     * 如果指定了queryString，则搜索queryString
     * 如果指定了queries ，则搜索queries
     * 如果同时指定：则按queryString
     */
    public static void search(File indexDir, String queryString, File queries, String field) throws IOException {
        if (field == null)
            field = Costs.CONTENTS; // 查找正文
        int repeat = 1;
        boolean raw = false;
        int hitsPerPage = 10;

        IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDir));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

        BufferedReader in = null;
        if (queries != null) {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(queries), "GBK"));
        } else {
            in = new BufferedReader(new InputStreamReader(System.in, "GBK"));
        }
        QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
        while (true) {
            if (queries == null && queryString == null) { // prompt the user
                System.out.println("请输入要查找的内容: ");
            }
            String line = queryString != null ? queryString : in.readLine();
            if (line == null || line.length() == -1) {
                break;
            }
            line = line.trim();
            if (line.length() == 0) {
                break;
            }
            Query query = null;
            try {
                parser.setAllowLeadingWildcard(true); // 允许*或者？为表达式的第一个
                query = parser.parse(line);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println("开始查找: " + query.toString(field));
            if (repeat > 0) { // 如果repeat大于0，则计算查找时间
                Date start = new Date();
                for (int i = 0; i < repeat; i++) {
                    // Sort sort = new Sort(SortField.FIELD_DOC);
                    SortField sortField = new SortField(Costs.MODIFYED, SortField.Type.LONG);
                    Sort sort = new Sort(sortField);
                    searcher.search(query, null, 100, sort);
                }
                Date end = new Date();
                System.out.println("Time: " + (end.getTime() - start.getTime()) + "ms");
            }

            doPagingSearch(in, searcher, query, hitsPerPage, raw, true);

            if (queryString != null) {
                break;
            }
        }
        reader.close();
    }

    /**
     * 进行分页查找
     * 
     * @param in
     * @param searcher
     * @param query
     * @param hitsPerPage
     * @param raw
     * @param interactive
     * @throws IOException
     */
    private static void doPagingSearch(BufferedReader in, IndexSearcher searcher, Query query, int hitsPerPage, boolean raw,
        boolean interactive) throws IOException {

        SortField sortField = new SortField(Costs.MODIFYED, SortField.Type.LONG);
        Sort sort = new Sort(sortField);
        // 进行搜索
        TopDocs results = searcher.search(query, 5 * hitsPerPage, sort);
        ScoreDoc[] hits = results.scoreDocs; // 查询的结果
        

        int numTotalHits = results.totalHits; // 总数
        System.out.println(numTotalHits + " 个文档中含有指定内容。");

        int start = 0;
        int end = Math.min(numTotalHits, hitsPerPage);

        while (true) {
            if (end > hits.length) {
                System.out.println("Only results 1 - " + hits.length + " of " + numTotalHits
                    + " total matching documents collected.");
                System.out.println("Collect more (y/n) ?");
                String line = in.readLine();
                if (line.length() == 0 || line.charAt(0) == 'n') {
                    break;
                }
                hits = searcher.search(query, numTotalHits, sort).scoreDocs;
            }

            end = Math.min(hits.length, start + hitsPerPage);

            for (int i = start; i < end; i++) {
                if (raw) { // output raw format
                    System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
                    continue;
                }

                Document doc = searcher.doc(hits[i].doc);
                String path = doc.get(Costs.PATH);
                System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score);
                if (path != null) {
                    System.out.println((i + 1) + ". " + path);
                    String title = doc.get(Costs.TITLE);
                    if (title != null) {
                        System.out.println("   Title: " + title + ":" + doc.get(Costs.MODIFYED));
                    }
                } else {
                    System.out.println(doc.get("key") + "|" + doc.get("creator") + "|" + doc.get("parentTitle") + "|" + doc.get("content") + "|" + doc.get("lastReplyDate"));
                }
            }
            if (!interactive || end == 0) {
                break;
            }
            if (numTotalHits >= end) {
                boolean quit = false;
                while (true) {
                    System.out.print("Press ");
                    if (start - hitsPerPage >= 0) {
                        System.out.print("(p)revious page, ");
                    }
                    if (start + hitsPerPage < numTotalHits) {
                        System.out.print("(n)ext page, ");
                    }
                    System.out.println("(q)uit or enter number to jump to a page.");

                    String line = in.readLine();
                    if (line.length() == 0 || line.charAt(0) == 'q') {
                        quit = true;
                        break;
                    }
                    if (line.charAt(0) == 'p') {
                        start = Math.max(0, start - hitsPerPage);
                        break;
                    } else if (line.charAt(0) == 'n') {
                        if (start + hitsPerPage < numTotalHits) {
                            start += hitsPerPage;
                        }
                        break;
                    } else {
                        int page = Integer.parseInt(line);
                        if ((page - 1) * hitsPerPage < numTotalHits) {
                            start = (page - 1) * hitsPerPage;
                            break;
                        } else {
                            System.out.println("No such page");
                        }
                    }
                }
                if (quit)
                    break;
                end = Math.min(numTotalHits, start + hitsPerPage);
            }
        }
    }
}
