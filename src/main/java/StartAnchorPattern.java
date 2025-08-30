/**
 * Matches the start of input (^).
 */
public class StartAnchorPattern implements PatternMatcher {
    private final PatternMatcher innerPattern;
    
    public StartAnchorPattern(PatternMatcher innerPattern) {
        this.innerPattern = innerPattern;
    }
    
    @Override
    public boolean matches(String input) {
        return matchesAt(input, 0);
    }
    
    @Override
    public boolean matchesAt(String input, int position) {
        return position == 0 && innerPattern.matchesAt(input, position);
    }
    
    @Override
    public int matchLength(String input, int position) {
        if (position != 0) {
            return -1;
        }
        return innerPattern.matchLength(input, position);
    }
}