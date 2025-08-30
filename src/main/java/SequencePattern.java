import java.util.List;

/**
 * Matches a sequence of patterns in order.
 */
public class SequencePattern implements PatternMatcher {
    private final List<PatternMatcher> patterns;
    
    public SequencePattern(List<PatternMatcher> patterns) {
        this.patterns = List.copyOf(patterns);
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
        return matchLength(input, position) >= 0;
    }
    
    @Override
    public int matchLength(String input, int position) {
        int currentPos = position;
        
        for (PatternMatcher pattern : patterns) {
            int length = pattern.matchLength(input, currentPos);
            if (length < 0) {
                return -1; // Pattern didn't match
            }
            currentPos += length;
        }
        
        return currentPos - position;
    }
}