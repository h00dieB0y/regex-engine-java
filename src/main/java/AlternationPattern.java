import java.util.List;

/**
 * Matches any one of several alternative patterns (|).
 */
public class AlternationPattern implements PatternMatcher {
    private final List<PatternMatcher> alternatives;
    
    public AlternationPattern(List<PatternMatcher> alternatives) {
        this.alternatives = List.copyOf(alternatives);
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
        for (PatternMatcher alternative : alternatives) {
            int length = alternative.matchLength(input, position);
            if (length >= 0) {
                return length;
            }
        }
        return -1; // No alternative matched
    }
}