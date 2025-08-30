/**
 * Matches the end of input ($).
 */
public class EndAnchorPattern implements PatternMatcher {
    private final PatternMatcher innerPattern;
    
    public EndAnchorPattern(PatternMatcher innerPattern) {
        this.innerPattern = innerPattern;
    }
    
    @Override
    public boolean matches(String input) {
        for (int i = 0; i <= input.length(); i++) {
            if (matchesAt(input, i)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean matchesAt(String input, int position) {
        int length = innerPattern.matchLength(input, position);
        return length >= 0 && (position + length) == input.length();
    }
    
    @Override
    public int matchLength(String input, int position) {
        int length = innerPattern.matchLength(input, position);
        if (length >= 0 && (position + length) == input.length()) {
            return length;
        }
        return -1;
    }
}