package greg.prototyping;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import java.util.NoSuchElementException;

public class Prototyper {
	public static final int CPP = 1;
	public static final int C = 2;
	public static final int JAVA = 4;
	public static final int PYTHON = 8;
	public static final int SWIFT = 16;
	private int lang = CPP;
	Scanner sc;
	private boolean insideClass = false; // this is also true when inside C-structs
	private boolean insideFunction = false;
	Pattern classOpeningPattern;
	Pattern classClosingPattern;
	Pattern functionOpeningPattern;
	Matcher classOpeningMatcher;
	Matcher classClosingMatcher;
	Matcher functionClosingPattern;
	private void setMatchingPatterns() {
		classOpeningPattern = switch (lang) {
			case C -> Pattern.compile("\\s*(typedef\\s+)?\\s*struct\\b.*\\{\\s*");
			case CPP -> Pattern.compile("\\s*(template\\s*<.*>)?\\s*class\\b.*\\{\\s*");
			default -> null;
		};
		functionOpeningPattern = switch (lang) {
			case C -> Pattern.compile("^\\s*([a-zA-z_]\\w*\\s+){1,3}[a-zA-z_]+\\w*\\s*\\((((\\s*[a-zA-z_]+\\w*(((\\s)" +
									  "|(\\*))+([a-zA-z_]+\\w*)?)*\\b[a-zA-z_]+\\w*\\s*,)*\\s*(([a-zA-z_]+\\w*(((\\s)" +
									  "|(\\*))+([a-zA-z_]+\\w*)?)*\\b[a-zA-z_]+\\w*)|(\\.\\.\\.)))|(void))\\s*\\).*" +
									  "\\{\\s*$");
			case CPP -> Pattern.compile("");
			default -> null;
		};
		classClosingPattern = switch (lang) {
			case C, CPP -> Pattern.compile("\\s*}.*;\\s*");
			default -> null;
		};
	}
	private void createScanner(File f) throws IOException {
		if (!f.canRead())
			throw new IOException("The file passed cannot be read from.");
		sc = new Scanner(f);
		insideClass = false;
	}
	private void setLang(int language) throws InvalidLanguageError {
		if (language > SWIFT || (language & (language - 1)) != 0)
			throw new InvalidLanguageError("The language specified is not supported or is invalid.");
		lang = language;
		this.setMatchingPatterns();
	}
	private void init(File f, int language) throws IOException, InvalidLanguageError {
		this.createScanner(f);
		this.setLang(language);
	}
	public Prototyper(File f) throws IOException, InvalidLanguageError {
		this.createScanner(f);
	}
	public Prototyper(File f, int language) throws IOException, InvalidLanguageError {
		this.init(f, language);
	}
	public Prototyper(String path, int language) throws IOException, InvalidLanguageError {
		this.init(new File(path), language);
	}
	public void setFile(File f) throws IOException, FileNotFoundException {
		this.createScanner(f);
	}
	public void setPath(String path) throws IOException, FileNotFoundException {
		this.createScanner(new File(path));
	}
	public void setLanguage(int language) throws InvalidLanguageError {
		this.setLang(language);
	}
	public String getNextDeclaration() {
		StringBuilder declaration = new StringBuilder();
		String word;
		try {
			if (lang == C) {
				while (true) {
					word = sc.next();
					// deal with "" and /**/ and //
					if (word.contains(";")) {
						declaration.delete(0, declaration.length());
						continue;
					}
					if (word.contains("#") || word.contains("\\") || word.contains("<") || word.contains(">")) {
						sc.nextLine();
						declaration.delete(0, declaration.length());
						continue;
					}
					declaration.append(word).append(' ');
					if (word.contains("}")) {
						declaration.delete(0, declaration.length());
						// reduce closing brace count - will need this for structs
					}
					else if (word.contains("{")) {
						// System.out.println(declaration);
						if (functionOpeningPattern.matcher(declaration).matches()) {
							declaration.delete(declaration.indexOf("{"), declaration.length());
							return declaration.toString().strip() + ';';
						}
						else {
							declaration.delete(0, declaration.length());
						}
					}
				}
			}
		} catch (NoSuchElementException e) {
			return null;
		}
		return declaration.toString();
	}
	public static void main(String [] args) throws IOException, InvalidLanguageError {
		Pattern p = Pattern.compile("\\s*#h?");
		// Pattern p = Pattern.compile("^[a-zA-z_]+\\w*((\\s)|(\\*))+[a-zA-z_]+\\w*$");
		Matcher m = p.matcher(" #h");
		if (m.matches()) {
			System.out.println("Yes!");
		}
		else {
			System.out.println("No");
		}
		Scanner sc = new Scanner(new File("/Users/gregorhartlwatters/JavaScanner.txt"));
		String next;
		// while (sc.hasNext()) {
		// 	next = sc.next();
		// 	System.out.println(next);
		// 	if (next.contains("//") && sc.hasNextLine()) {
		// 		System.out.println(sc.nextLine());
		// 	}
		// }
		Prototyper prot = new Prototyper(new File("/Users/gregorhartlwatters/C/Commands/arithmetic.h"), Prototyper.C);
		while ((next = prot.getNextDeclaration()) != null)
			System.out.println(next);
	}
}