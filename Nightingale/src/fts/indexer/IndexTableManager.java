package fts.indexer;

import fts.tokenizer.Tokenizer;
import fts.utils.Document;

public interface IndexTableManager {

	void addDocumentIntoIndexTable(Document document, Tokenizer tokenizer);

	void addPartsOfDocumentIndexTable(int documentId, String title, String partsOfBodyToAdd);

	void flushTable();
}
