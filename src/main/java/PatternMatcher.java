/**
 * Interface for pattern matching operations.
 */
public interface PatternMatcher {
    /**
     * Checks if the pattern matches anywhere in the input string.
     */
    boolean matches(String input);
    
    /**
     * Checks if the pattern matches at a specific position.
     */
    boolean matchesAt(String input, int position);
    
    /**
     * Returns the length of the match if successful, or -1 if no match.
     */
    int matchLength(String input, int position);
}