import java.io.IOException;
import java.util.Scanner;

public class Main {
  public static void main(String[] args){
    if (args.length != 2 || !args[0].equals("-E")) {
      System.out.println("Usage: ./your_program.sh -E <pattern>");
      System.exit(1);
    }

    String pattern = args[1];  
    Scanner scanner = new Scanner(System.in);
    String inputLine = scanner.nextLine();

    if (matchPattern(inputLine, pattern)) {
        System.exit(0);
    } else {
        System.exit(1);
    }
  }

  public static boolean matchPattern(String inputLine, String pattern) {
    if (pattern == null || inputLine == null) {
      return false;
    }
    
    // Single character literal match
    if (pattern.length() == 1) {
      return inputLine.contains(pattern);
    }
    
    // Digit character class
    if ("\\d".equals(pattern)) {
      return inputLine.chars().anyMatch(Character::isDigit);
    }

    // Word character class (alphanumeric + underscore)
    if ("\\w".equals(pattern)) {
      return inputLine.chars().anyMatch(c -> Character.isLetterOrDigit(c) || c == '_');
    }

    // Character group [...]
    if (pattern.startsWith("[") && pattern.endsWith("]")) {
      String matchingGroup = pattern.substring(1, pattern.length() - 1);

      if (matchingGroup.isEmpty()) return false;

      // Check If negatif group
      if (matchingGroup.charAt(0) == '^'){
        var negGroup = matchingGroup.substring(1);

        return inputLine.chars().anyMatch(c-> negGroup.indexOf(c) == -1);
      }

      return inputLine.chars().anyMatch(c -> matchingGroup.indexOf(c) != -1);
    }
    
    throw new RuntimeException("Unhandled pattern: " + pattern);
  }
}
