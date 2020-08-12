package fts;

import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.Sets;

import fts.index.tokenizer.NGramTokenizer2;
import fts.index.tokenizer.Tokenizer;
import fts.loader.Loader;
import fts.loader.WikiLoaderViaPython;
import fts.searcher.Displayer;
import fts.searcher.DisplayerImpl;
import fts.searcher.QueryAnalyzer;
import fts.searcher.QueryAnalyzerImpl;
import fts.searcher.RecordAcquirer;
import fts.searcher.RecordAcquirerImpl;
import fts.searcher.Searcher;
import fts.searcher.SearcherImpl;

public class Main {

	private static final Set<Character> IGNORED_CHARS = Sets.newHashSet('_', ' ', ':', ';', '[', ']', '{', '}', '<',
			'>', '（', '\'', '\"',
			'）', '(', ')', '。', '.', '「', '」', '\n', '\r', '#', '|', '*');

	static final String wikiXmlPath = "C:\\Users\\kotaro takeda\\Documents\\Programming\\03_searchengin\\workspace\\data\\jawiki-20200501-pages-articles.xml";
	static final String wikiXmlPath2 = "C:\\Users\\kotaro takeda\\Documents\\Programming\\03_searchengin\\workspace\\data\\text\\";
	static final String wikiFilePrefix = "wiki_";
	static final int maxWikiDocumentCounts = 1000;
	static final int ngramN = 2;
	static final int resultN = 10;

	public static void main(String[] args) throws Exception {

		Tokenizer tokenizer = new NGramTokenizer2(ngramN, IGNORED_CHARS);

		System.out.println("Main#main	args:" + Arrays.toString(args));

		String flag = args[0];
		switch (flag) {
		case "-i":
			String folderName = args[1];
			int fromIndex = Integer.parseInt(args[2]);
			int toIndex = Integer.parseInt(args[3]);
			buildIndexFromWiki(wikiXmlPath2, tokenizer, maxWikiDocumentCounts, folderName, fromIndex, toIndex);
			break;
		case "-s":
			String query = args[1];
			assert (query != null && query.length() > 0);
			search(query, tokenizer, resultN);
			break;
		default:
			System.out.println("do nothing.");
		}

	}

	static void buildIndexFromWiki(
			String wikiFilePath,
			Tokenizer tokenizer,
			int maxWikiDocumentCounts,
			String folderName,
			int fromIndex,
			int toIndex) throws Exception {
		Loader wikiLoader = new WikiLoaderViaPython(wikiFilePath, tokenizer, maxWikiDocumentCounts);
		for (int i = fromIndex; i < toIndex; i++) {
			wikiLoader.setFilePath(wikiFilePath + folderName + "\\" + wikiFilePrefix + String.format("%02d", i));
			wikiLoader.load();

		}
	}

	static void search(String query, Tokenizer tokenizer, int resultNum) {
		QueryAnalyzer queryAnalyzer = new QueryAnalyzerImpl(query, tokenizer);
		RecordAcquirer recordAcquirer = new RecordAcquirerImpl(queryAnalyzer.sortedToekns());
		Searcher searcher = new SearcherImpl(recordAcquirer.getSortedRecords(), tokenizer);
		Displayer displayer = new DisplayerImpl(searcher.search(resultNum));
		displayer.displayResult();
	}
}
