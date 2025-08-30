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
        return tryMatch(input, position, 0);
    }
    
    private int tryMatch(String input, int position, int patternIndex) {
        if (patternIndex >= patterns.size()) {
            return 0; // Successfully matched all patterns
        }
        
        PatternMatcher pattern = patterns.get(patternIndex);
        
        // Handle OneOrMorePattern with backtracking
        if (pattern instanceof OneOrMorePattern) {
            OneOrMorePattern quantPattern = (OneOrMorePattern) pattern;
            
            // Try from maximum possible length down to minimum (1)
            int maxLength = quantPattern.matchLength(input, position);
            if (maxLength < 0) {
                return -1; // Can't match at all
            }
            
            // Try all possible lengths from max down to 1
            for (int len = maxLength; len >= 1; len--) {
                int actualLen = quantPattern.matchLengthUpTo(input, position, len);
                if (actualLen == len) {
                    int remainingMatch = tryMatch(input, position + len, patternIndex + 1);
                    if (remainingMatch >= 0) {
                        return len + remainingMatch;
                    }
                }
            }
            return -1;
        } else {
            // Regular pattern
            int length = pattern.matchLength(input, position);
            if (length < 0) {
                return -1;
            }
            
            int remainingMatch = tryMatch(input, position + length, patternIndex + 1);
            if (remainingMatch >= 0) {
                return length + remainingMatch;
            }
            return -1;
        }
    }
}