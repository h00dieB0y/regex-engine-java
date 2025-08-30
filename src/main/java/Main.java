import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A simple regex pattern matcher that supports basic pattern matching features.
 * Supports: literal characters, digit class (\d), word class (\w), and
 * character groups ([abc], [^abc])
 */
public class Main {

  // ==================== ENTRY POINT ====================

  public static void main(String[] args) {
    if (args.length != 2 || !args[0].equals("-E")) {
      System.err.println("Usage: ./your_program.sh -E <pattern>");
      System.exit(1);
    }

    String pattern = args[1];

    try (Scanner scanner = new Scanner(System.in)) {
      String inputLine = scanner.nextLine();

      if (matchPattern(inputLine, pattern)) {
        System.exit(0);
      } else {
        System.exit(1);
      }
    }
  }

  // ==================== PATTERN ELEMENT TYPES ====================

  /**
   * Represents the different types of pattern elements that can be matched.
   */
  enum PatternElementType {
    DIGIT_CLASS, // \d - matches any digit
    WORD_CLASS, // \w - matches word characters (letters, digits, underscore)
    CHARACTER_GROUP, // [abc] or [^abc] - matches character sets
    LITERAL_CHARACTER, // a, b, c... - matches exact characters
    ONE_OR_MORE, // + quantifier
    ZERO_OR_ONE, // ? quantifier
    ANY_CHARACTER, // . quantifier
  }

  // ==================== MAIN MATCHING LOGIC ====================

  /**
   * Checks if the input line matches the given pattern anywhere within it.
   * 
   * @param inputLine the string to search in
   * @param pattern   the regex pattern to match
   * @return true if the pattern matches anywhere in the input line
   */
  public static boolean matchPattern(String inputLine, String pattern) {
    if (pattern == null || inputLine == null) {
      return false;
    }

    if (pattern.startsWith("^")) {
      String remainingPattern = pattern.substring(1); // Remove ^

      // Check only the first position
      return matchesPatternAtPosition(inputLine, remainingPattern, 0);
    }

    for (int i = 0; i <= inputLine.length(); i++) {
      if (matchesPatternAtPosition(inputLine, pattern, i)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the pattern matches starting at a specific position in the input.
   * 
   * @param inputLine  the input string
   * @param pattern    the pattern to match
   * @param inputStart the starting position in the input
   * @return true if the pattern matches at this position
   */
  private static boolean matchesPatternAtPosition(String inputLine, String pattern, int inputStart) {
    if (pattern.isEmpty())
      return true;

    if (pattern.equals("$"))
      return inputStart == inputLine.length();

    if (inputStart >= inputLine.length())
      return false;

    if (pattern.length() > 1 && pattern.charAt(1) == '+') {
      var element = pattern.charAt(0);

      var remainingPattern = pattern.substring(2);

      return matchOneOrMore(element, remainingPattern, inputLine, inputStart);
    }

    if (pattern.length() > 1 && pattern.charAt(1) == '?') {
      var element = pattern.charAt(0);

      var remainingPattern = pattern.substring(2);

      return matchZeroOrOne(element, remainingPattern, inputLine, inputStart);
    }

    if (matchesPatternElement(inputLine.charAt(inputStart), pattern, 0)) {
      var remainingPattern = getRemainingPattern(pattern);

      return matchesPatternAtPosition(inputLine, remainingPattern, inputStart + 1);
    }

    return false;
  }

  private static String getRemainingPattern(String pattern) {
    if (pattern.isEmpty()) {
      return "";
    }

    if (pattern.length() > 1 && pattern.charAt(1) == '+') {
      return pattern.substring(2); // Skip "element+"
    }

    if (pattern.length() > 1 && pattern.charAt(1) == '?') {
      return pattern.substring(2); // Skip "element?"
    }

    int nextElementPosition = advanceToNextPatternElement(pattern, 0);
    return pattern.substring(nextElementPosition);
  }

  // ==================== PATTERN ELEMENT MATCHING ====================

  /**
   * Checks if a character matches a pattern element at a specific position.
   * 
   * @param character       the character to test
   * @param pattern         the full pattern string
   * @param patternPosition the position in the pattern to check
   * @return true if the character matches the pattern element
   */
  static boolean matchesPatternElement(char character, String pattern, int patternPosition) {
    Optional<PatternElementType> type = recognizePatternElement(pattern, patternPosition);

    if (type.isEmpty()) {
      return false;
    }

    return switch (type.get()) {
      case DIGIT_CLASS -> Character.isDigit(character);
      case WORD_CLASS -> Character.isLetterOrDigit(character) || character == '_';
      case CHARACTER_GROUP -> matchesCharacterGroup(character, extractCharacterGroup(pattern, patternPosition));
      case ONE_OR_MORE -> matchOneOrMore(character, pattern, pattern, patternPosition);
      case ZERO_OR_ONE -> false; // This case should not be handled here, it's handled in matchesPatternAtPosition
      case ANY_CHARACTER -> true;
      case LITERAL_CHARACTER -> character == pattern.charAt(patternPosition);
    };
  }

  /**
   * Determines the type of pattern element at a given position.
   * 
   * @param pattern  the pattern string
   * @param position the position to analyze
   * @return the type of pattern element, or empty if invalid position
   */
  static Optional<PatternElementType> recognizePatternElement(String pattern, int position) {
    int patternLength = pattern.length();

    if (position < 0 || position >= patternLength) {
      return Optional.empty();
    }

    if (pattern.charAt(position) == '+')
      return Optional.of(PatternElementType.ONE_OR_MORE);

    if (pattern.charAt(position) == '\\') {
      int nextPosition = position + 1;

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

    if (pattern.charAt(position) == '.') {
      return Optional.of(PatternElementType.ANY_CHARACTER);
    }

    return Optional.of(PatternElementType.LITERAL_CHARACTER);
  }

  // ==================== PATTERN NAVIGATION ====================

  /**
   * Advances the pattern position to the next pattern element.
   * 
   * @param pattern         the pattern string
   * @param currentPosition the current position in the pattern
   * @return the position of the next pattern element
   */
  private static int advanceToNextPatternElement(String pattern, int currentPosition) {
    Optional<PatternElementType> type = recognizePatternElement(pattern, currentPosition);

    if (type.isEmpty()) {
      return currentPosition + 1;
    }

    return switch (type.get()) {
      case DIGIT_CLASS, WORD_CLASS -> currentPosition + 2; // Skip '\' and 'd'/'w'
      case ONE_OR_MORE, ZERO_OR_ONE -> currentPosition + 2; // Skip element and quantifier
      case CHARACTER_GROUP -> pattern.indexOf(']', currentPosition + 1) + 1; // Skip to after ']'
      case ANY_CHARACTER, LITERAL_CHARACTER -> currentPosition + 1; // Single character
    };
  }

  // ==================== CHARACTER GROUP HANDLING ====================

  /**
   * Extracts the character group content from a pattern (content between [ and
   * ]).
   * 
   * @param pattern  the full pattern
   * @param position the position of the '[' character
   * @return the content of the character group (without brackets)
   */
  private static String extractCharacterGroup(String pattern, int position) {
    int endOfGroup = pattern.indexOf(']', position + 1);
    return pattern.substring(position + 1, endOfGroup); // Skip the '[' bracket
  }

  /**
   * Checks if a character matches a character group specification.
   * Supports both positive [abc] and negative [^abc] character groups.
   * 
   * @param c     the character to test
   * @param group the character group content (without brackets)
   * @return true if the character matches the group specification
   */
  private static boolean matchesCharacterGroup(char c, String group) {
    if (group.isEmpty()) {
      return false;
    }

    // Handle negated groups [^abc]
    if (group.charAt(0) == '^') {
      String negativeGroup = group.substring(1);

      if (negativeGroup.isEmpty()) {
        return false;
      }

      var excludedChars = negativeGroup.chars()
          .boxed()
          .collect(Collectors.toSet());

      return !excludedChars.contains((int) c);
    }

    // Handle positive groups [abc]
    var allowedChars = group.chars()
        .boxed()
        .collect(Collectors.toSet());

    return allowedChars.contains((int) c);
  }

  private static boolean matchOneOrMore(char c, String remainingPattern, String text, int textPos) {
    if (textPos >= text.length() || text.charAt(textPos) != c)
      return false;

    do {
      textPos++;

      if (matchesPatternAtPosition(text, remainingPattern, textPos))
        return true;

    } while (textPos < text.length() && text.charAt(textPos) == c);

    return false;
  }

  private static boolean matchZeroOrOne(char element, String remainingPattern, String inputLine, int inputStart) {
    // Try matching without consuming the element (zero occurrences)
    if (matchesPatternAtPosition(inputLine, remainingPattern, inputStart)) {
      return true;
    }

    // Try matching by consuming the element (one occurrence)
    if (inputStart < inputLine.length() && inputLine.charAt(inputStart) == element) {
      return matchesPatternAtPosition(inputLine, remainingPattern, inputStart + 1);
    }

    return false;
  }
}
