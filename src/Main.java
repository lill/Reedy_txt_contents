import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main
{
	public static void main(String[] args) throws IOException
	{
		String address = "1. Волкодав.txt";
//		String address = "Жюль Верн - Таинственный остров.txt";
		ArrayList<ContentsItem> contents = getContents(address);
		for (ContentsItem content : contents) {
			System.out.println(content);
		}
		System.out.println("Количество строк в оглавлении: " + contents.size());
	}


	public static ArrayList<ContentsItem> getContents(String address) throws IOException
	{
//		//read ANSI file
//		InputStreamReader encoding = new InputStreamReader(new FileInputStream(address), "windows-1251");
//		BufferedReader br = new BufferedReader(encoding);

		//read UTF-8 file
		final BufferedReader br = new BufferedReader(new FileReader(address));
		final ArrayList<ContentsItem> contents = new ArrayList<ContentsItem>();

		boolean emptyAfter;
		int emptyBefore = 0;
		int lineNumber = 0;
		int nextLineLength;
		int currentLineLength = 0;
		int linesLengthNumber = 0;
		double linesLengthSum = 0;
		double spaceLengthSum = 0;
		double spaceDifferenceSum = 0;
		int nextLineSpaceCount;
		int currentLineSpaceCount = 0;
		String currentLine = "";

		String nextLine = br.readLine();
		if (nextLine.startsWith(UTF8_BOM))
			nextLine = nextLine.substring(1);

		while (true)
		{
			nextLineLength = nextLine.length();
			nextLineSpaceCount = spaceCount(nextLine);
			if (nextLineLength == nextLineSpaceCount) {
				emptyAfter = true;
			} else {
				linesLengthSum += nextLineLength;
				spaceLengthSum += nextLineSpaceCount;
				linesLengthNumber++;
				emptyAfter = false;
			}

			if (currentLineSpaceCount != currentLineLength) {
				double difference = Math.abs(currentLineSpaceCount - spaceLengthSum / linesLengthNumber);
				spaceDifferenceSum += difference;

				double isHeader = isHeader(emptyAfter, emptyBefore, linesLengthSum, linesLengthNumber, currentLineLength,
											currentLineSpaceCount, difference, spaceDifferenceSum, currentLine);
				if (isHeader >= 7.5) {
					ContentsItem item = new ContentsItem(currentLine, lineNumber);
					contents.add(item);
				}
				emptyBefore = 0;
			}
			else if (nextLineSpaceCount != nextLineLength) {
				emptyBefore++;
			}

			currentLine = nextLine;
			nextLine = br.readLine();
			if (nextLine == null) break;
			currentLineLength = nextLineLength;
			currentLineSpaceCount = nextLineSpaceCount;
			lineNumber++;
		}

		return contents;
	}


	public static double isHeader(boolean emptyAfter, int emptyBefore, double linesLengthSum,
								int linesLengthNumber, int currentLineLength, int currentLineSpaceCount,
								double difference, double spaceDifferenceSum, String currentLine)
	{
		double isHeader = 0;
		if (emptyBefore > 0) isHeader += 2.8;
		if (emptyAfter) isHeader += 3.6;

		double averageLines = linesLengthSum / linesLengthNumber;
		averageLines = calcAverage(averageLines);//
		double ratio = averageLines / currentLineLength;
		if (ratio >= 5) {
			isHeader += 3;
		}
		else {
			if (ratio < 1.7) isHeader -= 3.4;
		}


		double averageSpase = spaceDifferenceSum / linesLengthNumber;
		double differenceAverage = Math.abs(difference - averageSpase);
		if (differenceAverage >= 1.5) isHeader += 1.9;


		boolean isDot = false;
		boolean isComma = false;
		char[] chars = currentLine.toCharArray();
		int i = currentLine.length()-1;
		while (i >= 0) {
			if (!Character.isWhitespace(chars[i])) {
				if (dot.contains(chars[i])) {
					isDot = true;
					break;
				} else if (comma.contains(chars[i])) {
					isComma = true;
					break;
				} else break;
			}
			i--;
		}
		if (isComma) isHeader -= 2.9;
		else if (isDot) isHeader -= 2;
		else isHeader += 2;


		boolean isDash = false;
		boolean isNumber = false;
		boolean isStar = false;
		boolean hasLetter = false;
		boolean hasAnySymbols = false;
		short upperCaseCount = 0;
		for (int j = currentLineSpaceCount; j < currentLine.length(); j++) {
			char c = currentLine.charAt(j);
			if (!Character.isWhitespace(c)) {
				if (j == currentLineSpaceCount && dash.contains(c)) {
					isDash = true;
					break;
				}
				else if (Character.isLetter(c)) {
					hasLetter = true;
					if (Character.isUpperCase(c)) {
						upperCaseCount++;
					} else {
						upperCaseCount = 0;
						break;
					}
				} else if (star.contains(c)) {
					isStar = true;
				} else if (Character.isDigit(c)) {
					isNumber = true;
				} else
					hasAnySymbols = true;
			}
		}
		if (isDash) isHeader -= 5;
		else if (upperCaseCount > 2) isHeader += 5;
		else if (isNumber && !hasAnySymbols && !hasLetter && !isStar) isHeader += 5;
		else if (isStar && !hasAnySymbols && !hasLetter && !isNumber) isHeader += 5;

		if (isWord(currentLine)) isHeader += 4;

		return isHeader;
	}


	public static double calcAverage(double average)   // корректировка сред.значения длины строки
	{
		if (average >= 129) return average;
		else return 129;
	}

	public static int spaceCount(String s)   // кол-во пробелов в начале строки, отступ
	{
		int spaceCount = 0;
		for (char c : s.toCharArray()) {
			if (Character.isSpaceChar(c)) {
				spaceCount++;
			}
			else break;
		}
		return spaceCount;
	}

	static final Set<Character> dash  = new HashSet<Character>();
	static final Set<Character> comma = new HashSet<Character>();
	static final Set<Character> dot  = new HashSet<Character>();
	static final Set<Character> star  = new HashSet<Character>();
	static {
		dash.add('-');
		dash.add('‐');
		dash.add('‒');
		dash.add('–');
		dash.add('—');
		dash.add('―');
		dash.add('⁃');
		dash.add('−');
		dash.add('_');

		comma.add(',');
		comma.add(';');
		comma.add(')');//?
		comma.add(']');//?
		comma.add('…');//?
		comma.addAll(dash);

		dot.add('.');
		dot.add('!');
		dot.add('?');
		dot.add(':');
		dot.add('»');
		dot.add('"');

		star.add('*');
		star.add('=');
		star.add('+');
		star.addAll(dash);
	}


	static final String[] words = {
			"аннотаци", "предислови", "введени",
			"вступлени", "пролог", "глава",
			"часть", "послеслови", "эпилог",
			"примечани", "приложени", "благодарност",
			"annotation", "introduction", "part",
			"chapter", "epilogue", "note"
	};

	public static boolean isWord(String string)   // слова
	{
		string = (string.length() > 30
				? string.substring(0, 30)
				: string).toLowerCase();

		for (String word : words)
			if (string.contains(word)) return true;

		return false;
	}


	// FEFF because this is the Unicode char represented by the UTF-8 byte order mark (EF BB BF).
	public static final String UTF8_BOM = "\uFEFF";
}
