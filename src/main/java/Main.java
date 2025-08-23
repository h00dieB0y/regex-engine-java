import java.util.Optional;
import java.util.Scanner;
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

    for (int i = 0; i <= inputLine.length(); i++) {
      if (matchesPatternAtPosition(inputLine, pattern, i)) {
        return true;
      }
    }
    return false;

  }

  private static boolean matchesPatternAtPosition(String inputLine, String pattern, int inputStart) {
    int currentInputIndex = inputStart;
    int currentPatternIndex = 0;

    while (currentPatternIndex < pattern.length() && currentInputIndex < inputLine.length()) {
      if (!matchesPatternElement(inputLine.charAt(currentInputIndex), pattern, currentPatternIndex)) {
        return false;
      }

      currentInputIndex++;
      currentPatternIndex = advanceToNextPatternElement(pattern, currentPatternIndex);
    }

    // Read all the pattern
    return currentPatternIndex >= pattern.length();
  }

  private static int advanceToNextPatternElement(String pattern, int currentPosition) {
    var type = recognizePatternElement(pattern, currentPosition);

    if (type.isEmpty()) {
      return currentPosition + 1;
    }

    return switch (type.get()) {
      case DIGIT_CLASS, WORD_CLASS -> currentPosition + 2;
      case CHARACTER_GROUP -> pattern.indexOf(']', currentPosition + 1) + 1;
      case LITERAL_CHARACTER -> currentPosition + 1;
    };
  }

  enum PatternElementType {
    DIGIT_CLASS, // \d
    WORD_CLASS, // \w
    CHARACTER_GROUP, // [abc]
    LITERAL_CHARACTER // a, b, c...
  }

  static boolean matchesPatternElement(char character, String pattern, int patternPosition) {
    var type = recognizePatternElement(pattern, patternPosition);

    if (type.isEmpty()) {
      return false;
    }
    return switch (type.get()) {
      case DIGIT_CLASS -> Character.isDigit(character);
      case WORD_CLASS -> Character.isLetterOrDigit(character) || character == '_';
      case CHARACTER_GROUP -> matchesCharacterGroup(character, extractCharacterGroup(pattern, patternPosition));
      case LITERAL_CHARACTER -> character == pattern.charAt(patternPosition);
    };

  }

  static Optional<PatternElementType> recognizePatternElement(String pattern, int position) {
    var patternLength = pattern.length();

    if (position < 0 || position >= patternLength) {
      return Optional.empty();
    }

    if (pattern.charAt(position) == '\\') {
      var nextPosition = position + 1;

      if (nextPosition >= patternLength) {
        return Optional.of(PatternElementType.LITERAL_CHARACTER);
      }

      return switch (pattern.charAt(nextPosition)) {
        case 'd' -> Optional.of(PatternElementType.DIGIT_CLASS);
        case 'w' -> Optional.of(PatternElementType.WORD_CLASS);
        default -> Optional.of(PatternElementType.LITERAL_CHARACTER);
      };
    }

    if (pattern.charAt(position) == '[') {

      if (pattern.indexOf(']', position + 1) == -1) {
        return Optional.of(PatternElementType.LITERAL_CHARACTER);
      }

      return Optional.of(PatternElementType.CHARACTER_GROUP);
    }

    return Optional.of(PatternElementType.LITERAL_CHARACTER);
  }

  private static String extractCharacterGroup(String pattern, int position) {
    // We assume that we use this function only for group pattern

    int endOfGroup = pattern.indexOf(']', position + 1);
    return pattern.substring(position + 1, endOfGroup); // Skip the '[' bracket
  }

  private static boolean matchesCharacterGroup(char c, String group) {
    if (group.isEmpty()) {
      return false;
    }

    // Negated group
    if (group.charAt(0) == '^') {
      var negativeGroup = group.substring(1);

      if (negativeGroup.isEmpty()) {
        return false;
      }

      var excludedChars = negativeGroup.chars()
          .boxed()
          .collect(Collectors.toSet());

      return !excludedChars.contains((int) c);
    }

    var allowedChars = group.chars()
        .boxed()
        .collect(Collectors.toSet());

    return allowedChars.contains((int) c);
  }

}
