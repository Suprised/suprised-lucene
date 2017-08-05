package com.dascom.lucene.analyzer;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.dascom.lucene.index.Costs;

/**
 * 分词测试demo
 */
public class AnalyzerDemo {
    
    public static final String[] examplers = {
            "abcdefgtg don't hello world.",
            "分词测试 demo 你好我的名字是刘金喜或者陈欢。20J2EE是一J2EE是一个规范。个规范。 ",
            "1，导入包的格式必须为zip包，提供的为ISO包2，目前只支持在个人文件夹中导入积件包。因为知识点资源需要进行再压缩。3，servlet3不稳定，有时候取不到session。",
            "导入"};
    
    private static final Analyzer[] analyzers = {
        new WhitespaceAnalyzer(Costs.LUCENE_VERSION),
        new SimpleAnalyzer(Costs.LUCENE_VERSION),
        new StopAnalyzer(Costs.LUCENE_VERSION),
        new StandardAnalyzer(Costs.LUCENE_VERSION),
        new CJKAnalyzer(Costs.LUCENE_VERSION), // 支持中日韩的分词
    }; // 五种不同类型的分词

    /**
     * 进行分词，并将结果打印出来
     * 
     * @param text
     *          被分析的字符串
     */
    public static final void analyzer(String text) throws IOException {
        for (Analyzer analyzer : analyzers) {
            String className = analyzer.getClass().getSimpleName() ;
            System.out.print("\t" + className + ":\t");
            AnalyzerUtils.displayTokens(analyzer, text);
            System.out.println("\r\n");
        }
    }
    
    public static void main(String[] args) throws IOException {
        for (String text : examplers) {
            analyzer(text);
        }
    }
}
