import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * A regex pattern matcher supporting character classes, quantifiers, anchors,
 * character groups, alternation, and grouped expressions.
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
   * Pattern element types supported by the matcher.
   */
  enum PatternElementType {
    DIGIT_CLASS,
    WORD_CLASS,
    CHARACTER_GROUP,
    OR_GROUP,
    LITERAL_CHARACTER,
    ONE_OR_MORE,
    ZERO_OR_ONE,
    ANY_CHARACTER,
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

    // Handle groups with quantifiers: (pattern)+ or (pattern)?
    if (pattern.charAt(0) == '(' && hasGroupQuantifier(pattern)) {
      return matchGroupWithQuantifier(inputLine, pattern, inputStart);
    }

    // Handle OR groups (cat|dog)
    if (pattern.charAt(0) == '(' && pattern.contains("|")) {
      return matchOrGroup(inputLine, pattern, inputStart);
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
        case ANY_CHARACTER -> true; // Any character matches '.'
        case LITERAL_CHARACTER -> character == pattern.charAt(patternPosition);
        
        // These cases should NOT be handled in this method:
        case OR_GROUP -> throw new IllegalArgumentException("OR_GROUP should be handled in matchesPatternAtPosition");
        case ONE_OR_MORE -> throw new IllegalArgumentException("ONE_OR_MORE should be handled in matchesPatternAtPosition"); 
        case ZERO_OR_ONE -> throw new IllegalArgumentException("ZERO_OR_ONE should be handled in matchesPatternAtPosition");
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

    if (pattern.charAt(position) == '(') {
      int indexSeparator = pattern.indexOf('|', position + 1);

      if (indexSeparator == -1) return Optional.of(PatternElementType.LITERAL_CHARACTER);

      if (pattern.indexOf(')', indexSeparator + 1) == -1) return Optional.of(PatternElementType.LITERAL_CHARACTER);

      return Optional.of(PatternElementType.OR_GROUP);
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
      case DIGIT_CLASS, WORD_CLASS -> currentPosition + 2;
      case ONE_OR_MORE, ZERO_OR_ONE -> currentPosition + 2;
      case CHARACTER_GROUP -> pattern.indexOf(']', currentPosition + 1) + 1;
      case OR_GROUP -> pattern.indexOf(')', currentPosition + 1) + 1;
      case ANY_CHARACTER, LITERAL_CHARACTER -> currentPosition + 1;
    };
  }

  // ==================== CHARACTER GROUP HANDLING ====================

  /**
   * Extracts character group content between brackets.
   * 
   * @param pattern  the full pattern
   * @param position position of the '[' character
   * @return character group content without brackets
   */
  private static String extractCharacterGroup(String pattern, int position) {
    int endOfGroup = pattern.indexOf(']', position + 1);
    return pattern.substring(position + 1, endOfGroup);
  }

  private static String[] extractOrGroup(String pattern, int position) {
    int indexSeparator = pattern.indexOf('|', position + 1);
    int endOfGroup = pattern.indexOf(')', indexSeparator +1);

    return new String[] {pattern.substring(position + 1, indexSeparator), pattern.substring(indexSeparator + 1, endOfGroup)};
  }

  /**
   * Checks if character matches character group specification.
   * 
   * @param c     character to test
   * @param group character group content
   * @return true if character matches group specification
   */
  private static boolean matchesCharacterGroup(char c, String group) {
    if (group.isEmpty()) {
      return false;
    }

    if (group.charAt(0) == '^') {
      String negativeGroup = group.substring(1);
      if (negativeGroup.isEmpty()) {
        return false;
      }
      var excludedChars = negativeGroup.chars().boxed().collect(Collectors.toSet());
      return !excludedChars.contains((int) c);
    }

    var allowedChars = group.chars().boxed().collect(Collectors.toSet());
    return allowedChars.contains((int) c);
  }

  private static boolean matchOneOrMore(char element, String remainingPattern, String text, int textPos) {
    if (textPos >= text.length())
      return false;

    if (!matchesElement(text.charAt(textPos), element)) {
      return false;
    }

    do {
      textPos++;
      if (matchesPatternAtPosition(text, remainingPattern, textPos))
        return true;
    } while (textPos < text.length() && matchesElement(text.charAt(textPos), element));

    return false;
  }

  private static boolean matchesElement(char character, char element) {
    if (element == '.') {
      return true;
    }
    return character == element;
  }

  private static boolean matchZeroOrOne(char element, String remainingPattern, String inputLine, int inputStart) {
    if (matchesPatternAtPosition(inputLine, remainingPattern, inputStart)) {
      return true;
    }

    if (inputStart < inputLine.length() && matchesElement(inputLine.charAt(inputStart), element)) {
      return matchesPatternAtPosition(inputLine, remainingPattern, inputStart + 1);
    }

    return false;
  }

  private static boolean matchOrGroup(String inputLine, String pattern, int inputStart) {
    String[] groups = extractOrGroup(pattern, 0);
    String remainingPattern = getRemainingPattern(pattern);

    // Try matching the first alternative
    if (matchesPatternSequence(inputLine, groups[0], inputStart, remainingPattern)) {
      return true;
    }

    // Try matching the second alternative
    return matchesPatternSequence(inputLine, groups[1], inputStart, remainingPattern);
  }

  private static boolean matchesPatternSequence(String inputLine, String subPattern, int inputStart, String remainingPattern) {
    int currentPos = inputStart;
    
    // Use the main matching logic for the subPattern
    if (matchesPatternAtPosition(inputLine, subPattern, inputStart)) {
      // Calculate how many characters were consumed
      currentPos = findMatchEndPosition(inputLine, subPattern, inputStart);
      return matchesPatternAtPosition(inputLine, remainingPattern, currentPos);
    }
    
    return false;
  }

  private static int findMatchEndPosition(String inputLine, String pattern, int startPos) {
    return findPatternMatchLength(inputLine, pattern, startPos);
  }

  private static boolean hasGroupQuantifier(String pattern) {
    int closeParenPos = findMatchingCloseParen(pattern, 0);
    if (closeParenPos == -1 || closeParenPos + 1 >= pattern.length()) {
      return false;
    }
    char nextChar = pattern.charAt(closeParenPos + 1);
    return nextChar == '+' || nextChar == '?';
  }

  private static int findMatchingCloseParen(String pattern, int openPos) {
    int depth = 0;
    for (int i = openPos; i < pattern.length(); i++) {
      if (pattern.charAt(i) == '(') {
        depth++;
      } else if (pattern.charAt(i) == ')') {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  private static boolean matchGroupWithQuantifier(String inputLine, String pattern, int inputStart) {
    int closeParenPos = findMatchingCloseParen(pattern, 0);
    char quantifier = pattern.charAt(closeParenPos + 1);
    String groupContent = pattern.substring(1, closeParenPos);
    String remainingPattern = pattern.substring(closeParenPos + 2);

    if (quantifier == '+') {
      return matchGroupOneOrMore(inputLine, groupContent, remainingPattern, inputStart);
    } else if (quantifier == '?') {
      return matchGroupZeroOrOne(inputLine, groupContent, remainingPattern, inputStart);
    }

    return false;
  }

  private static boolean matchGroupOneOrMore(String inputLine, String groupContent, String remainingPattern, int inputStart) {
    int currentPos = inputStart;
    int matchCount = 0;
    
    // Must match at least once
    while (currentPos < inputLine.length()) {
      if (!matchesPatternAtPosition(inputLine, groupContent, currentPos)) {
        break;
      }
      
      int newPos = findPatternMatchLength(inputLine, groupContent, currentPos);
      if (newPos <= currentPos) {
        break; // Prevent infinite loop
      }
      
      currentPos = newPos;
      matchCount++;
      
      // Try matching remaining pattern at each successful group match
      if (matchesPatternAtPosition(inputLine, remainingPattern, currentPos)) {
        return true;
      }
    }
    
    // Only return true if we had at least one match AND the remaining pattern was satisfied
    return false;
  }

  private static boolean matchGroupZeroOrOne(String inputLine, String groupContent, String remainingPattern, int inputStart) {
    // Try without matching the group (zero occurrences)
    if (matchesPatternAtPosition(inputLine, remainingPattern, inputStart)) {
      return true;
    }
    
    // Try with matching the group once
    if (matchesPatternAtPosition(inputLine, groupContent, inputStart)) {
      int newPos = findPatternMatchLength(inputLine, groupContent, inputStart);
      return matchesPatternAtPosition(inputLine, remainingPattern, newPos);
    }
    
    return false;
  }

  private static int findPatternMatchLength(String inputLine, String pattern, int startPos) {
    return findPatternMatchLengthRecursive(inputLine, pattern, startPos, 0);
  }

  private static int findPatternMatchLengthRecursive(String inputLine, String pattern, int inputPos, int patternPos) {
    if (patternPos >= pattern.length()) {
      return inputPos;
    }
    
    if (inputPos >= inputLine.length()) {
      return inputPos;
    }

    // Handle quantifiers
    if (patternPos + 1 < pattern.length() && pattern.charAt(patternPos + 1) == '+') {
      char element = pattern.charAt(patternPos);
      if (matchesElement(inputLine.charAt(inputPos), element)) {
        int newInputPos = inputPos + 1;
        while (newInputPos < inputLine.length() && matchesElement(inputLine.charAt(newInputPos), element)) {
          newInputPos++;
        }
        return findPatternMatchLengthRecursive(inputLine, pattern, newInputPos, patternPos + 2);
      }
      return inputPos;
    }

    if (patternPos + 1 < pattern.length() && pattern.charAt(patternPos + 1) == '?') {
      char element = pattern.charAt(patternPos);
      if (matchesElement(inputLine.charAt(inputPos), element)) {
        return findPatternMatchLengthRecursive(inputLine, pattern, inputPos + 1, patternPos + 2);
      } else {
        return findPatternMatchLengthRecursive(inputLine, pattern, inputPos, patternPos + 2);
      }
    }

    // Handle groups
    if (pattern.charAt(patternPos) == '(') {
      int closeParenPos = findMatchingCloseParen(pattern, patternPos);
      String groupContent = pattern.substring(patternPos + 1, closeParenPos);
      String remainingPattern = pattern.substring(closeParenPos + 1);
      
      if (groupContent.contains("|")) {
        String[] alternatives = extractOrGroup(pattern, patternPos);
        for (String alt : alternatives) {
          // Try to match this alternative completely within the group context
          if (canMatchAlternativeInContext(inputLine, alt, remainingPattern, inputPos)) {
            int altMatchLength = findPatternMatchLengthRecursive(inputLine, alt, inputPos, 0);
            return findPatternMatchLengthRecursive(inputLine, pattern, altMatchLength, closeParenPos + 1);
          }
        }
        return inputPos;
      } else {
        int matchLength = findPatternMatchLengthRecursive(inputLine, groupContent, inputPos, 0);
        return findPatternMatchLengthRecursive(inputLine, pattern, matchLength, closeParenPos + 1);
      }
    }

    // Handle single character match
    if (matchesPatternElement(inputLine.charAt(inputPos), pattern, patternPos)) {
      int nextPatternPos = advanceToNextPatternElement(pattern, patternPos);
      return findPatternMatchLengthRecursive(inputLine, pattern, inputPos + 1, nextPatternPos);
    }

    return inputPos;
  }

  private static boolean canMatchAlternativeInContext(String inputLine, String alternative, String remainingPattern, int inputPos) {
    // Try to match the alternative and see if what follows can match the remaining pattern
    int altMatchLength = findPatternMatchLengthRecursive(inputLine, alternative, inputPos, 0);
    
    if (altMatchLength <= inputPos) {
      return false; // Alternative didn't match anything
    }
    
    // Check if the remaining pattern can start matching at the position after the alternative
    if (remainingPattern.isEmpty()) {
      return true; // No more pattern to match
    }
    
    // For the specific case we're debugging: after matching "dog", we need to check if
    // the next character(s) can be handled by the remaining pattern
    return inputPos < inputLine.length() && canPatternMatchAtPosition(inputLine, remainingPattern, altMatchLength);
  }
  
  private static boolean canPatternMatchAtPosition(String inputLine, String pattern, int position) {
    if (pattern.isEmpty()) {
      return true;
    }
    
    if (position >= inputLine.length()) {
      return false;
    }
    
    // Simple check: does the pattern have a chance of matching at this position?
    return matchesPatternAtPosition(inputLine.substring(position), pattern, 0);
  }
}
