package fts.database;

import java.util.Map;
import java.util.Set;

import fts.utils.Document;
import fts.utils.PostingList;
import fts.utils.Token;

/*
 * DBは実質3種類。本書では実質2種類だが、実装の簡単のため拡張したい。
 *
 * ①documents	( id int primary key, title text not null, body text not null)
 * ②tokens		( id int primary key, token text not null)
 *
 *
 */

//sqlite3_exec(env->db,"CREATE TABLE settings ("\"  key   TEXT PRIMARY KEY,"\"  value TEXT"\");",NULL,NULL,NULL);

//sqlite3_exec(env->db,"CREATE TABLE documents ("\"  id      INTEGER PRIMARY KEY," /* auto increment */ \"  title   TEXT NOT NULL,"\"  body    TEXT NOT NULL"\");",NULL,NULL,NULL);

//sqlite3_exec(env->db,"CREATE TABLE tokens ("\"  id         INTEGER PRIMARY KEY,"\"  token      TEXT NOT NULL,"\"  docs_count INT NOT NULL,"\"  postings   BLOB NOT NULL"\");",NULL,NULL,NULL);

public interface DBManager {

	public int dbGetDocumentIdAndAddDocumentIfNotExists(Document document, int bodySize);

	public void dbAddDocument(Document document, int bodySize);//いる？

	public int dbGetDocumentId(Document document);//いる？サーチの時に使う？

	public int dbGetTokenIdAndAddTokenIfNotExists(Token token);

	public void dbAddToken(Token token);

	public void dbAddTokenSet(Set<Token> tokenSet);

	public int dbGetTokenId(Token token);//いる？サーチの時に使う？

	public void dbUpdateBodyOfDocument(int documentId, String title, String partsOfBodyToAdd, int bodySize);

	public String dbGetBodyOfDocument(int documentId);

	public int dbGetBodySizeOfDocument(int documentId);

	public String dbGetTitleOfDocument(int documentId);

	public void dbUpdatePostingList(Token token, PostingList postingListToAdd);//いる？これよりもdbUpdatePostings的な関数の方がいる？

	public void dbUpdatePostingListMap(Map<Token, PostingList> postingListMap);//いる？これよりもdbUpdatePostings的な関数の方がいる？

	public String dbGetPostingList(Token token);//いる？サーチの時に使う？

	public int getTotalDocumentNum();

}
