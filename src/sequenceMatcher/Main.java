package sequenceMatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.biojava3.core.sequence.DNASequence;

import net.sourceforge.argparse4j.inf.Namespace;

public class Main {
	
	public final static String HDline= "@HD\tVN:1.0";
	public final static String RGline= "@RG\tID:NA\tLB:NA\tSM:NA\tPL:NA\tPU:NA\tPG:NA";
	
	public static void main(String[] args) throws IOException{

		/* Start parsing arguments */
		Namespace opts= ArgParse.argParse(args);
		ArgParse.validateArgs(opts);
		
		String subcmd= opts.getString("subcmd");
		
		if(subcmd.equals("match")){
			matcher(opts);
		} else if(subcmd.equals("convert")){
			converter(opts);
		} else {
			System.out.println("Unrecognized cmd.");
		}
		
	}

	/**
	 * Generate a list of element pairs. Each pair is stitched in a single string.
	 * Example: 
	 * ArrayList<String> lst= new ArrayList<String>();
	 * lst.add("a"); lst.add("b"); lst.add("c")
	 * combinePairs(lst) -> ['aa', 'ab', 'ac', 'bb', 'bc', 'cc'] 
	 * @param nameLst
	 * @return
	 */
	private static HashSet<String> combinePairs(ArrayList<String> nameLst){
		HashSet<String> pairs= new HashSet<String>();
		for(int i= 0; i < nameLst.size(); i++){
			for(int j= i; j < nameLst.size(); j++){
				pairs.add(nameLst.get(i) + nameLst.get(j));
			}
		}
		return pairs;
	}

	/**
	 * Returns pairs of names from fasta list.
	 * @param fasta
	 * @return
	 */
	private static HashSet<String> combineNamePairsFromFasta(ArrayList<String[]> fasta){
		ArrayList<String> nameLst= new ArrayList<String>();
		for(String[] x : fasta){
			nameLst.add(x[0]);
		}
		HashSet<String> pairs= combinePairs(nameLst);
		return pairs;
	}

	
	/**
	 * Routine to convert sam to tab and back. Called with SequenceMatcher convert ...
	 * @param opts Arguments taken from command line and parsed by ArgParse class
	 * @throws IOException 
	 */
	private static void converter(Namespace opts) throws IOException{

		String input= opts.getString("input");
		String a= opts.getString("a");
		String outfmt= opts.getString("outfmt");
		
		if(outfmt.equals("sam")){
			// * Convert tab to sam: 
			// ** Read reference file, output as sam header
			if(a != null){
				System.out.println(HDline);
				System.out.println(RGline);
				System.out.println(Sam.fastaFileToSQHeader(a));
			}
			// This is use to understand if first line is the header.
			String headerStart= Match.HEADER.get(0) + "\t" + 
							    Match.HEADER.get(1) + "\t" + 
					            Match.HEADER.get(2);
			BufferedReader br= Opener.openBr(input);
			String line;
			while((line= br.readLine()) != null){
				if(line.startsWith(headerStart)){
					continue;
				}
				Match m= new Match();
				m.stringToMatchObj(line);
				Sam s= Sam.matchToSam(m);
				System.out.println(s);
			}
		}
	}
	
	/**
	 * Routine to perform and output matching. Called with SequenceMatcher match ...
	 * @param opts Arguments taken from command line and parsed by ArgParse class
	 * @throws IOException
	 */
	private static void matcher(Namespace opts) throws IOException{
		
		/* Start parsing arguments */
		String a= opts.getString("a");
		String b= opts.getString("b");
		String method= opts.getString("method");
		int nm= opts.getInt("nm");
		boolean norc= opts.getBoolean("norc");
		String aln= opts.getString("aln");
		boolean noLD= opts.getBoolean("noLD");
		boolean noJWD= opts.getBoolean("noJWD");
		String outfmt= opts.getString("outfmt");
		
		/* Adjust args */
		
		// How many loops thorough each sequence in B? 
		// 1 if rev comp is not required. 2 otherwise (+ and -)
		int nLoops= (!norc) ? 2 : 1;
				
		// ---------------------------------------------------------------------
		
		ArrayList<String[]> fastaFileA= SequenceReader.readFastaToList(a);
		
		System.err.println("Sequences in A (" + a + "): " + fastaFileA.size());
		
		HashSet<String> pairs= new HashSet<String>();
		if(b.isEmpty()){
			//If b file is not given this is just the a file matched against itself.
			b= a;
			pairs= combineNamePairsFromFasta(fastaFileA);
		}
		// ---------------------------------------------------------------------
		// Prepare reading file b. 
		BufferedReader brB= Opener.openBr(b);
				
		long t0= System.currentTimeMillis();

		/* -------------------------------------------------------------------- */
		// Prepare header:
		if(outfmt.equals("sam")){
			System.out.println(HDline);
			System.out.println(RGline);
			System.out.println(Sam.fastaListToSQHeader(fastaFileA));
		} else if(outfmt.equals("tab")){
			System.out.println(new Match().getHeader());
		} else {
			System.err.println("Unsupproted output format");
			System.exit(1);
		}
		/* -------------------------------------------------------------------- */
		int nseq= 0;
		int nmatch= 0;
		String[] seqB;
		String seqBrc= null;
		while((seqB= SequenceReader.getNextSequence(brB)) != null){

			if(!norc){
				seqBrc= new DNASequence(seqB[1]).getReverseComplement().getSequenceAsString();
			}
			
			for(int i=0; i < fastaFileA.size(); i++){
				int nLoopCnt= 0;
								
				while(nLoopCnt < nLoops) {
					String[] seqA= fastaFileA.get(i);
					Match m= new Match(seqA, seqB);
					
					if(nLoopCnt == 0){
						m.setStrand("+");
					} else if (nLoopCnt == 1){
						m.setStrand("-");
						m.setSeqB(seqBrc);
					} else {
						System.exit(1);
					}
					nLoopCnt++;
					if(m.skipMatch(pairs)){
						continue;
					}
					
					double d= m.getFilterDistance(method, nm);
					
					if(nm < 0 || (d >= 0 && d <= nm)){
						// Edit distance is below threshold. 
						// Fill up remaining metrics:
						if(method != "LD" && !noLD){
							m.computeLD();
						} 
						if(method != "HD"){
							m.computeHD();
						}
						if(!noJWD){
							m.computeJWD();
						}
						if(!aln.equals("none")){
							m.setAlnMethod(aln);
							m.align();
						}
						nmatch++;
						if(outfmt.equals("sam")){
							System.out.println(Sam.matchToSam(m).toString());
						}else{
							System.out.println(m);
						}
					}
				}
			} // end for loop file A
			nseq++;
			if(nseq % 1000 == 0){
				System.err.println("Processed " + nseq + " sequences from B");
			}
		}
		
		long t1= System.currentTimeMillis();
		System.err.println("Completed in " + (t1-t0)/1000.0 + "s");
		System.err.println("N matches: " + nmatch);		
		System.exit(0);
		
	}
	
}
