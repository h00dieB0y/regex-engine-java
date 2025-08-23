import java.io.IOException;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
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
      var matchingGroup = pattern.substring(1, pattern.length() - 1);
      
      if (matchingGroup.isEmpty()) {
        return false;
      }

      if (matchingGroup.charAt(0) == '^') {
        var negativeGroup = matchingGroup.substring(1);
        
        if (negativeGroup.isEmpty()) {
          return true;
        }
        
        Set<Integer> excludedChars = negativeGroup.chars().boxed().collect(Collectors.toSet());

        return inputLine.chars().noneMatch(excludedChars::contains);
      }

      final Set<Integer> allowedChars = matchingGroup.chars().boxed().collect(Collectors.toSet());
      
      return inputLine.chars().anyMatch(allowedChars::contains);
    }

    throw new RuntimeException("Unhandled pattern: " + pattern);
  }
}
