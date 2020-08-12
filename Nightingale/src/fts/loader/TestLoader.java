package fts.loader;

import java.nio.file.Paths;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class TestLoader extends DefaultHandler {
 String isbn;
 String title;
 String author;
 String text;

 public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 // 5. 何かの要素が始まった
 if ("Book".equals(qName)) {
 // 始まったのがBook要素なら、属性を読み取ってフィールドへ保存する
 isbn = attributes.getValue("isbn");
 author = attributes.getValue("author");
 title = attributes.getValue("title");
 }
 }

 public void characters(char[] ch, int start, int length) throws SAXException {
 // 6. テキストが出現したなら、char配列をStringにしてフィールドへ保存する
 text = new String(ch, start, length);
 }

 public void endElement(String uri, String localName, String qName) throws SAXException {
 // 7. 何かの要素が終わった
 if ("Book".equals(qName)) {
 // 終わったのがBook要素なら、フィールドに保存したBook要素の情報を出力する
 System.out.println("isbn = " + isbn);
 System.out.println("author = " + author);
 System.out.println("title = " + title);
 System.out.println("text = " + text);
 System.out.println();
 }
 }

 public static void main(String[] args) throws Exception {
 // 1. SAXParserFactoryを取得
 SAXParserFactory factory = SAXParserFactory.newInstance();
 // 2. SAXParserを取得
 SAXParser parser = factory.newSAXParser();
 // 3. SAXのイベントハンドラを生成(このクラスのインスタンス)
 TestLoader handler = new TestLoader();
 // 4. SAXParserにXMLを読み込ませて、SAXのイベントハンドラに処理を行わせる
 parser.parse(Paths.get("/bookList.xml").toFile(), handler);
 }
}