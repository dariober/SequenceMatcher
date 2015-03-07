package sequenceMatcher;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

/* The SAM output should be validated with 
 * java -jar SequenceMatcher.jar match -a seqA.fa -b seqB.fa -aln local -o sam > match.sam
 * java -jar picard.jar ValidateSamFile I=match.sam
 * 
 */

public class SamTest {

	@Test
	public void canConvertfastaListToSQHeader() {
		
		ArrayList<String[]> fastaList= new ArrayList<String[]>();
		fastaList.add(new String[] {"seq1", "ACTG"});
		fastaList.add(new String[] {"seq2", "ACTGACTG"});
		fastaList.add(new String[] {"seq3", "ACTGACTGACTG"});

		String expect= "@SQ	SN:seq1	LN:4\n"
   					 + "@SQ	SN:seq2	LN:8\n"
					 + "@SQ	SN:seq3	LN:12";

		String sqHeader= Sam.fastaListToSQHeader(fastaList);		
		assertEquals(expect, sqHeader);
	}

	@Test
	public void canConvertfastaFileToSQHeader() throws IOException {
		
		String fastafile= "test/seqs.fa";
		
		String sqHeader= Sam.fastaFileToSQHeader(fastafile);
		
		String expect= "@SQ	SN:seq1	LN:39\n"
   					 + "@SQ	SN:seq2	LN:28\n"
					 + "@SQ	SN:seq3	LN:4";

		assertEquals(expect, sqHeader);
	}

	
	@Test
	public void canGetAlnStartPosFromRefAndReadStrings(){
		int s;
		String aln= "ACTG";
		String ref= "ACTG";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(0, s);
		
		aln= "NACTG";
		ref= "-ACTG";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(0, s);
		
		aln= "--TG";
		ref= "ACTG";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(2, s);
		
		aln= "---G";
		ref= "ACTG";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(3, s);

		aln= "ACTG";
		ref= "----";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(-1, s);

		aln= "----";
		ref= "----";
		s= Sam.getAlnStartPos(aln, ref);
		assertEquals(-1, s);
		
	}
	
	@Test
	public void canGetCigarFromAln(){
		String aln;
		String ref;
		String cigar;

		aln= "A";
		ref= "A";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("1M", cigar);
		
		aln= "ACTG";
		ref= "ACTG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("4M", cigar);
		
		aln= "NACTG";
		ref= "-ACTG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("1I4M", cigar);

		aln= "NNNACTG";
		ref= "---ACTG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("3I4M", cigar);

		aln= "ACT-G";
		ref= "ACTNG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("3M1D1M", cigar);

		aln= "ACT---G";
		ref= "ACTNNNG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("3M3D1M", cigar);

		aln= "ACNTG";
		ref= "AC-TG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("2M1I2M", cigar);

		aln= "ACNNNTG";
		ref= "AC---TG";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("2M3I2M", cigar);

		aln= "ACTGNN";
		ref= "ACTG--";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("4M2I", cigar);

		aln= "-C-G-N";
		ref= "A-T-N-";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("1I1D1I1D1I", cigar);

		aln= "--ACTGN--";
		ref= "NNNNACTGN";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("5M", cigar);

		aln= "N-CTTAGTTCAGGAGAT-GT--GAC----N";
		ref= "-NATAA---AGCTGATCGTTGAACCGTGGN";
		cigar= Sam.getCigarFromAln(aln, ref);
		assertEquals("1I1D4M3I8M1D2M2D3M4D1M", cigar);	
	}
	
	@Test
	public void canGetOriginalReadSeqFromAln(){
		String aln= "-ACTG--ACT";
		String read= Sam.getSeqFromAln(aln);
		assertEquals("ACTGACT", read);
	}
	
	@Test
	public void canConvertMatchToTags(){
		Match m= new Match();
		m.setSeqA("ACTGN");
		m.setSeqB("ACTGA");
		m.setAlnMethod("global");
		m.setStrand("+");
		m.setNameA("read1");
		m.setNameB("ref1");
		m.align();
		m.computeHD();
		m.computeLD();
		m.computeJWD();

		ArrayList<String> tags= Sam.matchToTagList(m);
		
		assertTrue(tags.contains("NM:i:1"));
		assertTrue(tags.contains("AS:i:18"));
		assertTrue(tags.contains("XL:i:1"));
		assertTrue(tags.contains("XH:i:1"));		
		assertTrue(tags.contains("XJ:f:0.9200"));
		assertTrue(tags.contains("XP:f:0.8000"));
		assertTrue(tags.contains("XR:Z:ACTGN"));
		assertTrue(tags.contains("XS:Z:ACTGA"));
		
		Sam sam= Sam.matchToSam(m);
			
	}
}
