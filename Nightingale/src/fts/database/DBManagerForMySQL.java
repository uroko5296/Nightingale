package fts.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fts.utils.Document;
import fts.utils.PostingList;
import fts.utils.Token;

public class DBManagerForMySQL implements DBManager {

	public static void main(String[] args) {
		DBManager manager = new DBManagerForMySQL();
		int r = manager.getTotalDocumentNum();

		System.out.println("MySQLManager#main	r:" + r);

	}

	//private static final String URL = "jdbc:mysql://localhost:3306/test_db?serverTimezone=JST";
	private static final String HOST = "jdbc:mysql://localhost:3306/";
	private static final String DB = "test_db";
	private static final String TIMEZONE = "serverTimezone=JST";
	private static final String USER = "test";
	private static final String PASS = "test";

	/*
	 * テーブル名：
	 * インデックス構築のテスト用テーブルは、documents3、tokens3
	 * サーチのテスト用テーブルは、s_documents, s_tokens
	 *
	 * RichDocument用は、
	 * document2, tokens2
	 */
	private static final String DOCUMENTS = "documents4";
	private static final String TOKENS = "tokens4";

	/*
	 *
	 */

	private static String connectionURL() {
		return HOST + DB + "?" + TIMEZONE;
	}

	@Override
	public int dbGetDocumentIdAndAddDocumentIfNotExists(Document document) {
		int documentId = dbGetDocumentId(document);
		if (documentId < 0) {
			dbAddDocument(document);
			return dbGetDocumentId(document);
		} else {
			return documentId;
		}
	}

	private final String sqlAddDocument = "INSERT INTO " + DOCUMENTS + " (title,body) VALUES(?,?);";

	@Override
	public void dbAddDocument(Document document) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlAddDocument);
			pstmt.setString(1, document.getTitle());
			pstmt.setString(2, document.getBody());

			pstmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final String sqlSelectDocumentsIdByTitle = "SELECT id FROM " + DOCUMENTS + " WHERE title=?;";

	@Override
	public int dbGetDocumentId(Document document) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int r = -1;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlSelectDocumentsIdByTitle);
			pstmt.setString(1, document.getTitle());

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = rs.getInt("id");
			} else {
				r = -1;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
	}

	@Override
	public int dbGetTokenIdAndAddTokenIfNotExists(Token token) {
		int tokenId = dbGetTokenId(token);
		if (tokenId < 0) {
			dbAddToken(token);
			return dbGetTokenId(token);
		} else {
			return tokenId;
		}
	}

	private final String sqlSelectTokenIdByToken = "SELECT id FROM " + TOKENS + " WHERE token=?;";

	@Override
	public int dbGetTokenId(Token token) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int r = -1;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlSelectTokenIdByToken);
			pstmt.setString(1, token.getToken());

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = rs.getInt("id");
			} else {
				r = -1;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
	}

	/*
	 * tokenがなければINSERT、あればUPDATEしたい。
	 * 過去に読んでDBに記録されている文書と、今回追加する文書は別であることを前提とする。
	 * つまり、UPDATE時には単にdocs_countとpostingsの追加をするだけでよい。
	 */

	private final String sqlInsertPListOnDuplicateKeyUpdate = "INSERT INTO " + TOKENS
			+ " (id,token,docs_count,postings) values (?,?,?,?) ON DUPLICATE KEY UPDATE postings = CONCAT(postings, ?);";

	@Override
	public void dbUpdatePostingList(Token token, PostingList postingListToAdd) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlInsertPListOnDuplicateKeyUpdate);
			pstmt.setInt(1, this.dbGetTokenId(token));
			pstmt.setString(2, token.getToken());
			pstmt.setInt(3, postingListToAdd.getDocsCount());
			pstmt.setString(4, postingListToAdd.encode());
			pstmt.setString(5, postingListToAdd.encode());

			pstmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final String sqlInsertPListMapOnDuplicateKeyUpdate1 = "INSERT INTO " + TOKENS
			+ " (token,docs_count,postings) values";
	private final String sqlInsertPListMapOnDuplicateKeyUpdate2 = "(?,?,?),";
	private final String sqlInsertPListMapOnDuplicateKeyUpdate3 = "ON DUPLICATE KEY UPDATE postings = CONCAT(postings, VALUES(`postings`));";

	@Override
	public void dbUpdatePostingListMap(Map<Token, PostingList> postingListMap) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		if (postingListMap.keySet().size() < 1) {
			return;
		}

		String sql = sqlInsertPListMapOnDuplicateKeyUpdate1;
		for (Token token : postingListMap.keySet()) {
			sql = sql + " " + sqlInsertPListMapOnDuplicateKeyUpdate2;
		}
		sql = sql.substring(0, sql.length() - 1);
		sql = sql + " " + sqlInsertPListMapOnDuplicateKeyUpdate3;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sql);
			int i = 0;
			for (Token token : postingListMap.keySet()) {
				PostingList pList = postingListMap.get(token);
				pstmt.setString(i + 1, token.getToken());
				pstmt.setInt(i + 2, pList.getDocsCount());
				pstmt.setString(i + 3, pList.encode());
				i += 3;
			}

			pstmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final String sqlSelectToken = "SELECT docs_count,postings FROM " + TOKENS + " where token=?;";

	@Override
	public String dbGetPostingList(Token token) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlSelectToken);
			pstmt.setString(1, token.getToken());
			rs = pstmt.executeQuery();

			String postingListAsString = "";
			if (rs.next()) {
				postingListAsString = rs.getString("postings");
				pstmt.close();
			} else {
				postingListAsString = "";
				pstmt.close();
			}
			return postingListAsString;

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private final String sqlSelectBodyOfDocumentsIdById = "SELECT body FROM " + DOCUMENTS + " WHERE id=?;";

	@Override
	public String dbGetBodyOfDocument(int documentId) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String r = "";

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlSelectBodyOfDocumentsIdById);
			pstmt.setInt(1, documentId);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = rs.getString("body");
			} else {
				r = "";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
	}

	private final String sqlCountAllDocuments = "SELECT COUNT(id) FROM " + DOCUMENTS + ";";

	@Override
	public int getTotalDocumentNum() {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		int r = -1;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlCountAllDocuments);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = rs.getInt("COUNT(id)");
			} else {
				r = -2;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
	}

	private final String sqlInsertBodyOnDuplicateKeyUpdate = "INSERT INTO " + DOCUMENTS
			+ " (id,title,body) values (?,\"\",?) ON DUPLICATE KEY UPDATE body = CONCAT(body, ?);";

	@Override
	public void dbUpdateBodyOfDocument(int documentId, String title, String partsOfBodyToAdd) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		System.out.println("dbUpdateBodyOfDocument id:" + documentId + " title:" + title);

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlInsertBodyOnDuplicateKeyUpdate);
			pstmt.setInt(1, documentId);
			pstmt.setString(2, partsOfBodyToAdd);
			pstmt.setString(3, partsOfBodyToAdd);
			pstmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private final String sqlSelectTitleOfDocumentsIdById = "SELECT title FROM " + DOCUMENTS + " WHERE id=?;";

	@Override
	public String dbGetTitleOfDocument(int documentId) {

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String r = "";

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlSelectTitleOfDocumentsIdById);
			pstmt.setInt(1, documentId);

			rs = pstmt.executeQuery();

			if (rs.next()) {
				r = rs.getString("title");
			} else {
				r = "";
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return r;
	}

	//docs_countはDB上では更新しない想定。検索時にはlog([postingsの長さ])時間で取得可能。
	private final String sqlInsertIgnoreToken = "INSERT IGNORE INTO " + TOKENS
			+ " (token,docs_count,postings) values (?,0,\"\");";

	@Override
	public void dbAddToken(Token token) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlInsertIgnoreToken);
			pstmt.setString(1, token.getToken());
			pstmt.execute();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	//docs_countはDB上では更新しない想定。検索時にはlog([postingsの長さ])時間で取得可能。
	private final String sqlInsertIgnoreTokenSet1 = "INSERT IGNORE INTO " + TOKENS
			+ " (token,docs_count,postings) values";
	private final String sqlInsertIgnoreTokenSet2 = "(?,0,\"\"),";

	@Override
	public void dbAddTokenSet(Set<Token> tokenSet) {

		String sqlInsertIgnoreTokenSet = sqlInsertIgnoreTokenSet1;
		List<Token> tokenList = tokenSet.stream().collect(Collectors.toList());
		for (int i = 0; i < tokenList.size(); i++) {
			sqlInsertIgnoreTokenSet = sqlInsertIgnoreTokenSet + " " + sqlInsertIgnoreTokenSet2;
		}
		sqlInsertIgnoreTokenSet = sqlInsertIgnoreTokenSet.substring(0, sqlInsertIgnoreTokenSet.length() - 1);
		sqlInsertIgnoreTokenSet = sqlInsertIgnoreTokenSet + ";";

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String url = connectionURL();
			con = DriverManager.getConnection(
					url, //タイムゾーンを指定しないとなぜかエラーが出る。
					USER, //"root",
					PASS//"password"
			);// "password"の部分は，各自の環境に合わせて変更してください。

			pstmt = con.prepareStatement(sqlInsertIgnoreTokenSet);

			for (int i = 0; i < tokenList.size(); i++) {
				pstmt.setString(i + 1, tokenList.get(i).getToken());
			}

			pstmt.execute();

		} catch (

		SQLException e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

	}

}