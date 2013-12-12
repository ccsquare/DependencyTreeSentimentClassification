package cc.nlp.parse;

import org.junit.Before;
import org.junit.Test;

public class FilesParserTest {
	static FilesParser filesParser;
	@Before
	public void setUp() throws Exception {
		filesParser = FilesParser.createFilesParser("", "", false);
	}

	@Test
	public void testParseSingleLine() {
		System.out.println(
				filesParser.parseSingleLine(
				"rich in detail , gorgeously shot and beautifully acted , les destinees is , in its quiet , epic way , daring , inventive and refreshingly unusual .", 
				false, 
				1));
	}

}
