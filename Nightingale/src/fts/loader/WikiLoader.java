package fts.loader;

import java.nio.file.Paths;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fts.index.tokenizer.Tokenizer;
import fts.indexer.IndexTableManager;
import fts.indexer.IndexTableManagerImpl;
import fts.utils.BasicDocument;
import fts.utils.Document;

public class WikiLoader extends DefaultHandler implements Loader {

	Tokenizer tokenizer_;//コンストラクタでセットすることもできるし、後からsetTokenizerでセットすることもできる。
	String fpath_;

	/*
	 * 最初のコンストラクタ作成時には、パスとトークナイザの両方を設定する必要がある。
	 * トークナイザは後から変更できる。
	 */
	public WikiLoader(
			String fpath,
			Tokenizer tokenizer,
			int maxWikiDocumentCounts) throws Exception {
		fpath_ = fpath;
		setTokenizer(tokenizer);
		setMaxDocumentCount(maxWikiDocumentCounts);
	}

	@Override
	public void setTokenizer(Tokenizer tokenizer) {
		tokenizer_ = tokenizer;
	}

	@Override
	public void setMaxDocumentCount(int n) {
		assert (n > 0);
		maxDocumentCount_ = n;
	}

	@Override
	public void setFilePath(String path) {
		fpath_ = path;
	}

	/*
	 * load関数で更新する変数群
	 */
	private int maxDocumentCount_ = -1;
	private int documentCount_ = 0;
	IndexTableManager tableManager_ = null;
	private String title_ = null;
	private String body_ = null;

	enum Status {
		IN_DOCUMENT, IN_PAGE, IN_PAGE_TITLE, IN_PAGE_ID, IN_PAGE_REVISION, IN_PAGE_REVISION_TEXT
	};

	@Override
	public void load() throws Exception {
		System.out.println("load start:	" + maxDocumentCount_);
		assert (tokenizer_ != null && maxDocumentCount_ > 0);//先にset関数でセットする想定。
		documentCount_ = 0;

		tableManager_ = new IndexTableManagerImpl();

		// 1. SAXParserFactoryを取得
		SAXParserFactory factory = SAXParserFactory.newInstance();
		// 2. SAXParserを取得
		SAXParser parser = factory.newSAXParser();
		// 3. SAXのイベントハンドラを生成(このクラスのインスタンス)
		WikiHandler handler = new WikiHandler();
		// 4. SAXParserにXMLを読み込ませて、SAXのイベントハンドラに処理を行わせる
		System.out.println(fpath_);
		parser.parse(Paths.get(fpath_).toFile(), handler);
		System.out.println(fpath_);

		tableManager_.flushTable();//残っている部分をDBにマージする。
	}

	class WikiHandler extends DefaultHandler {

		private Status status_ = Status.IN_DOCUMENT;//最初はIN_DOCUMENT;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (status_) {
			case IN_DOCUMENT:
				if (qName.equals("page"))
					status_ = Status.IN_PAGE;
				break;
			case IN_PAGE:
				if (qName.equals("title"))
					status_ = Status.IN_PAGE_TITLE;
				else if (qName.equals("id"))
					status_ = Status.IN_PAGE_ID;
				else if (qName.equals("revision"))
					status_ = Status.IN_PAGE_REVISION;
				break;
			case IN_PAGE_TITLE:
				break;
			case IN_PAGE_ID:
				break;
			case IN_PAGE_REVISION:
				if (qName.equals("text"))
					status_ = Status.IN_PAGE_REVISION_TEXT;
				break;
			case IN_PAGE_REVISION_TEXT:
				break;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			switch (status_) {
			case IN_PAGE_TITLE:
				title_ = new String(ch, start, length);
				break;
			case IN_PAGE_REVISION_TEXT:
				body_ = new String(ch, start, length);
				break;
			default:
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (status_) {
			case IN_DOCUMENT:
				break;
			case IN_PAGE:
				if (qName.equals("page"))
					status_ = Status.IN_DOCUMENT;
				break;
			case IN_PAGE_TITLE:
				if (qName.equals("title"))
					status_ = Status.IN_PAGE;
				break;
			case IN_PAGE_ID:
				if (qName.equals("id"))
					status_ = Status.IN_PAGE;
				break;
			case IN_PAGE_REVISION:
				if (qName.equals("revision"))
					status_ = Status.IN_PAGE;
				break;
			case IN_PAGE_REVISION_TEXT:
				if (qName.equals("text")) {
					status_ = Status.IN_PAGE_REVISION;
					if (documentCount_ < maxDocumentCount_) {
						System.out.println("count:" + documentCount_ + " title:" + title_);
						Document document = new BasicDocument(title_, body_);
						tableManager_.addDocumentIntoIndexTable(document, tokenizer_);
						//総文書数を出力する。
						System.out.println("count:" + documentCount_ + " title:" + title_);
					} else {
						//System.out.println("loading ended...");
					}
					title_ = null;
					body_ = null;
					documentCount_++;
				}
				break;
			}
		}

	}
}