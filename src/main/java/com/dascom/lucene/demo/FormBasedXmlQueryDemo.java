package com.dascom.lucene.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.xml.CorePlusExtensionsParser;
import org.apache.lucene.queryparser.xml.QueryTemplateManager;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.IOUtils;
import org.apache.lucene.util.Version;

public class FormBasedXmlQueryDemo extends HttpServlet {
    
    private static final long serialVersionUID = 8131980144304004833L;
    
    private QueryTemplateManager queryTemplateManager;
    private CorePlusExtensionsParser xmlParser;
    private IndexSearcher searcher;
    private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        try {
            openExampleIndex();

            String xslFile = config.getInitParameter("xslFile");
            String defaultStandardQueryParserField = config.getInitParameter("defaultStandardQueryParserField");

            this.queryTemplateManager = new QueryTemplateManager(getServletContext().getResourceAsStream("/WEB-INF/" + xslFile));

            this.xmlParser = new CorePlusExtensionsParser(defaultStandardQueryParserField, this.analyzer);
        } catch (Exception e) {
            throw new ServletException("Error loading query template", e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Properties completedFormFields = new Properties();
        Enumeration<String> pNames = request.getParameterNames();
        while (pNames.hasMoreElements()) {
            String propName = pNames.nextElement();
            String value = request.getParameter(propName);
            if ((value != null) && (value.trim().length() > 0)) {
                completedFormFields.setProperty(propName, value);
            }
        }

        try {
            org.w3c.dom.Document xmlQuery = this.queryTemplateManager.getQueryAsDOM(completedFormFields);

            Query query = this.xmlParser.getQuery(xmlQuery.getDocumentElement());

            TopDocs topDocs = this.searcher.search(query, 10);

            if (topDocs != null) {
                ScoreDoc[] sd = topDocs.scoreDocs;
                org.apache.lucene.document.Document[] results = new org.apache.lucene.document.Document[sd.length];
                for (int i = 0; i < results.length; ++i) {
                    results[i] = this.searcher.doc(sd[i].doc);
                    request.setAttribute("results", results);
                }
            }
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Error processing query", e);
        }
    }

    private void openExampleIndex() throws IOException {
        RAMDirectory rd = new RAMDirectory();
        IndexWriterConfig iwConfig = new IndexWriterConfig(Version.LUCENE_46, this.analyzer);
        IndexWriter writer = new IndexWriter(rd, iwConfig);
        InputStream dataIn = getServletContext().getResourceAsStream("/WEB-INF/data.tsv");
        BufferedReader br = new BufferedReader(new InputStreamReader(dataIn, IOUtils.CHARSET_UTF_8));
        String line = br.readLine();
        FieldType textNoNorms = new FieldType(TextField.TYPE_STORED);
        textNoNorms.setOmitNorms(true);
        while (line != null) {
            line = line.trim();
            if (line.length() > 0) {
                StringTokenizer st = new StringTokenizer(line, "\t");
                org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                doc.add(new Field("location", st.nextToken(), textNoNorms));
                doc.add(new Field("salary", st.nextToken(), textNoNorms));
                doc.add(new Field("type", st.nextToken(), textNoNorms));
                doc.add(new Field("description", st.nextToken(), textNoNorms));
                writer.addDocument(doc);
            }
            line = br.readLine();
        }
        writer.close();

        IndexReader reader = DirectoryReader.open(rd);
        this.searcher = new IndexSearcher(reader);
    }
}