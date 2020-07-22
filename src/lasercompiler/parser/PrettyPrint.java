package lasercompiler.parser;

public class PrettyPrint {

	public static String tabLines(String s) {
		StringBuilder format = new StringBuilder();
		for(String line : s.split("\n")) {
			format.append("\t");
			format.append(line);
			format.append("\n");
		}
		return format.toString();
	}
	
}
