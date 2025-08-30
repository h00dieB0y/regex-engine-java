/**
 * Matches empty string.
 */
public class EmptyPattern implements PatternMatcher {
    
    @Override
    public boolean matches(String input) {
        return true; // Empty pattern always matches
    }
    
    @Override
    public boolean matchesAt(String input, int position) {
        return true; // Empty pattern matches at any position
    }
    
    @Override
    public int matchLength(String input, int position) {
        return 0; // Empty pattern consumes no characters
    }
}