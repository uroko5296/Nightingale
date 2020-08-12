package fts.indexer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Sets;

import fts.index.tokenizer.Tokenizer;
import fts.utils.Document;
import fts.utils.RichDocument;

public class DocLoaderFromWiki extends DefaultHandler implements DocLoader {

	Tokenizer tokenizer_;//コンストラクタでセットすることもできるし、後からsetTokenizerでセットすることもできる。
	String fpath_;

	/*
	 * 最初のコンストラクタ作成時には、パスとトークナイザの両方を設定する必要がある。
	 * トークナイザは後から変更できる。
	 */
	public DocLoaderFromWiki(
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

	private String preprocess(String oriPath) {
		String newPath = oriPath + ".tmp";

		Set<String> escapeChars = Sets.newHashSet("<", ">", "\'", "\"", "&");
		//Set<String> escapeChars = Sets.newHashSet("\'", "&");
		BufferedReader br = null;
		BufferedWriter bw = null;

		try {
			// ファイル入出力
			br = new BufferedReader(new InputStreamReader(new FileInputStream(oriPath)));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newPath)));

			String line;
			line = "<r>";//ルート要素用。
			bw.write(line);
			bw.newLine();
			//int count = 0;
			//int C = 20;
			while ((line = br.readLine()) != null) {

				// 置換処理
				for (String s : escapeChars) {
					//if (count < C)
					//System.out.println("preprocess1[" + s + "] line:" + line);
					if (line.startsWith("<doc") || line.startsWith("</doc>")) {
						//line = line;
					} else {
						line = line.replace(s, " ");
					}
					//count++;
					//if (count < C)
					//System.out.println("preprocess2[" + s + "] line:" + line);
				}

				// ファイルへ書き込み
				bw.write(line);
				bw.newLine();
			}
			line = "</r>";//ルート要素用。
			bw.write(line);
			bw.newLine();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException ie) {
				}
			}
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException ie) {
				}
			}
		}
		return newPath;
	}

	/*
	 * 最後は有効化する
	 */
	private void postprocess(String newFile) {
		File f = new File(newFile);
		f.delete();
	}

	/*
	 * load関数で更新する変数群
	 */
	private int maxDocumentCount_ = -1;
	private int documentCount_ = 0;
	IndexTableManager tableManager_ = null;
	private int id_ = -1;
	private String url_ = null;
	private String title_ = null;
	private String body_ = null;

	enum Status {
		ROOT, OUTSIDE_OF_DOC, INSIDE_OF_DOC
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

		String newPath = preprocess(fpath_);//一時ファイル（エスケープ文字を削除したもの）を作成

		parser.parse(Paths.get(newPath).toFile(), handler);//一時ファイルをパース

		//postprocess(newPath);//一時ファイルを削除

		System.out.println(fpath_);

		tableManager_.flushTable();//残っている部分をDBにマージする。
	}

	class WikiHandler extends DefaultHandler {

		private Status status_ = Status.ROOT;//最初はOUT_OF_DOC;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			switch (status_) {
			case ROOT:
				System.out.println("WikiLoaderViaPython1 id:" + id_ + " url:" + url_ + " title:" + title_);
				if (qName.equals("r")) {
					status_ = Status.OUTSIDE_OF_DOC;
					System.out.println("WikiLoaderViaPython2 id:" + id_ + " url:" + url_ + " title:" + title_);
				}
			case OUTSIDE_OF_DOC:
				if (qName.equals("doc")) {
					status_ = Status.INSIDE_OF_DOC;
					id_ = Integer.parseInt(attributes.getValue("id"));
					url_ = attributes.getValue("url");
					title_ = attributes.getValue("title");
					System.out.println("WikiLoaderViaPython3 id:" + id_ + " url:" + url_ + " title:" + title_);
				}

				break;
			case INSIDE_OF_DOC:
				break;
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			switch (status_) {
			case ROOT:
				break;
			case OUTSIDE_OF_DOC:
				break;
			case INSIDE_OF_DOC:
				body_ = body_ + new String(ch, start, length);
				/*
				if (documentCount_ < maxDocumentCount_) {
					Document document = new RichDocument(title_, body_, url_, id_);
					tableManager_.addDocumentIntoIndexTable(document, tokenizer_);
					//総文書数を出力する。
				}
				*/
				System.out.print(".");
				//tableManager_.addPartsOfDocumentIndexTable(id_, title_, body_);
				break;
			default:
				break;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			switch (status_) {
			case ROOT:
				break;
			case OUTSIDE_OF_DOC:
				if (qName.equals("r")) {
					status_ = Status.ROOT;
				}
				break;
			case INSIDE_OF_DOC:
				if (qName.equals("doc")) {
					status_ = Status.OUTSIDE_OF_DOC;
					if (documentCount_ < maxDocumentCount_) {
						Document document = new RichDocument(title_, body_, url_, id_);
						tableManager_.addDocumentIntoIndexTable(document, tokenizer_);
						//総文書数を出力する。
						System.out.println();
						System.out.println("count:" + documentCount_ + " title:" + title_);
					}
					title_ = null;
					body_ = null;
					url_ = null;
					id_ = -1;
					documentCount_++;
				}
				break;
			}
		}

	}
}