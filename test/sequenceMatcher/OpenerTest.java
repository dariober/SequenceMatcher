package sequenceMatcher;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class OpenerTest {

	@Test
	public void canReturnBufferedReaderFromFilename() throws IOException {
		BufferedReader br= Opener.openBr("test/openerTestFile.txt");
		String line= br.readLine();
		br.close();
		assertEquals("first line", line);
	}
	@Test
	public void canReturnBufferedReaderFromFilenameGZ() throws IOException {
		BufferedReader br= Opener.openBr("test/openerTestFile.txt.gz");
		String line= br.readLine();
		br.close();
		assertEquals("first line", line);
	}

	/**
	 * Should test for reading from stdin. In reality it just tests that
	 * passing "-" returns a BufferedReader. 
	 * @throws IOException
	 */
	@Test
	public void canReturnBufferedReaderStdin() throws IOException {
		BufferedReader br= Opener.openBr("-");
		br.close();
	}
}
