package sequenceMatcher;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;


public class ArgParse {
	
	public static String VERSION= "0.3.0";
	
	/* Parse command line args */
	public static Namespace argParse(String[] args){
		
		String mainHelp= "DESCRIPTION\n"
				+ "This program answers the question: Which sequences in file a "
				+ "are similar to the sequences in file b. In addition it optionally performs gloabl alignment.\n"
				+ "\n"
				+ "EXAMPLE\n"
				+ "java -jar SequenceMatcher.jar match -a seqA.fa -b seqB.fa\n"
				+ "\n"
				+ "java -jar SequenceMatcher.jar match -a seqA.fa -b seqB.fa -o sam \\\n"
				+ "| samtools view -Sb - > aln.bam"
				+ "\n"
				+ "SEE ALSO"
				+ "\n"
				+ "https://github.com/dariober/SequenceMatcher/wiki"
				+ "\n"
				+ "NOTES & TODO\n"
				+ "Names of fasta sequences must be unique within file.\n"
				+ "LD, HD, JWD, len_A, and len_B are computed on the raw input sequences NOT on the aligned sequences\n"
				+ "so they are independent on the alignment method chosen.\n"
				+ "When aligning in forward and revcomp, only the best match is returned. "
				+ "In case of ties one of the two matches is picked at random."
				+ "\n"
				+ "Todo: Better choice of which match to output in case of ties. Options to output both."
				+ "Todo: Read fastq files and output quality scores, although scores are not used.\n"
				+ "Todo: Support alternative scoring matrices.\n"
				+ "Todo: Option to customize RG tag."
;
		
		ArgumentParser parser= ArgumentParsers
				.newArgumentParser("SequenceMatcher")
				.defaultHelp(true)
				.version("${prog} " + VERSION)
				.description(mainHelp);

		parser.addArgument("--version", "-v").action(Arguments.version());

		Subparsers subparsers= parser.addSubparsers()
				.dest("subcmd")
				.description("Matching & parsing")
				.help("Run SequenceMatcher match -h to view options.");
		
		/* --------------------------------------------------------------------- */

		Subparser matchSubparser= subparsers.addParser("match");		
		matchSubparser.description("Main program to perform matching and alignment.");
		
		matchSubparser.addArgument("-a", "--a")
			.type(String.class)
			.required(true)
			.help("Fasta file 'A'; can be gzip'd. Use '-' to read from stdin. "
					+ "In SAM format this is 'Reference'");
		
		matchSubparser.addArgument("-b", "--b")
			.type(String.class)
			.required(false)
			.setDefault("")
			.help("Fasta file 'B'; can be gzip'd. Use '-' to read from stdin. "
					+ "In SAM format these are 'Reads'. If null the a file will be "
					+ "matched against itself.");
		
		matchSubparser.addArgument("-m", "--method")
			.type(String.class)
			.required(false)
			.setDefault("LD")
			.choices("LD", "HD")
			.help("Method to determine the threshold edit distance: LD (Levenshtein, default) or HD (Hamming)\n"
					+ "Hamming is much faster.");
		
		matchSubparser.addArgument("-nm", "--nm")
			.type(Integer.class)
			.setDefault(-1)
			.help("Maximum edit distance to output a match. If -1 (default) all sequence pairs are returned.");
		
		matchSubparser.addArgument("-norc", "--norc")
			.action(Arguments.storeTrue())
			.help("Do not reverse complement the sequences in file B. I.e. only match sequences as they are.");	

		matchSubparser.addArgument("-aln", "--aln")
			.choices("none", "global", "local")
			.setDefault("global")
			.help("Method to align sequences. Choose none to skip alignment. Default: global");	
		
		matchSubparser.addArgument("-noLD", "--noLD")
			.action(Arguments.storeTrue())
			.help("Do not compute Levenshtein distance (faster).");	

		matchSubparser.addArgument("-noJWD", "--noJWD")
			.action(Arguments.storeTrue())
			.help("Do not compute Jaro-Winkler distance (faster).");	

		matchSubparser.addArgument("-of", "--outfmt")
			.type(String.class)
			.required(false)
			.setDefault("tab")
			.choices("tab", "sam")
			.help("Output format. 'tab': Tab delim (see above); 'sam' SAM format");	

		/* -------------------------------------------------------------------- */
		
		Subparser convertSubparser= subparsers.addParser("convert");
		convertSubparser.description("Convert tab to sam and viceversa");
		
		convertSubparser.addArgument("-i", "--input")
			.type(String.class)
			.required(true)
			.help("Input file to convert. can be gzip'd. Use '-' to read from stdin.");
		
		convertSubparser.addArgument("-of", "--outfmt")
			.type(String.class)
			.choices("sam")
			.setDefault("sam")
			.help("Output format. 'sam' converts tabular to sam and it is currently the only"
					+ " output format supported.");

		convertSubparser.addArgument("-a", "--a")
		.type(String.class)
		.required(false)
		.help("Used for converting tab to sam: Reference fasta file aka as 'a' file; "
				+ "can be gzip'd. Use '-' to read from stdin. If not given the output "
				+ "sam will be header-less.");

		/* --------------------------------------------------------------------- */
		
		Namespace opts= null;
		try{
			opts= parser.parseArgs(args);
		}
		catch(ArgumentParserException e) {
			parser.handleError(e);
			System.exit(1);
		}		
		return(opts);
	}
	
	public static void validateArgs(Namespace opts){
		//		
	}
}
