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
        return matchLengthWithBacktracking(input, position, 0);
    }
    
    private int matchLengthWithBacktracking(String input, int position, int patternIndex) {
        if (patternIndex >= patterns.size()) {
            return 0; // Successfully matched all patterns
        }
        
        PatternMatcher currentPattern = patterns.get(patternIndex);
        
        // For OneOrMorePattern, try all possible lengths
        if (currentPattern instanceof OneOrMorePattern) {
            OneOrMorePattern quantPattern = (OneOrMorePattern) currentPattern;
            int[] possibleLengths = quantPattern.getAllPossibleLengths(input, position);
            
            // Try from longest to shortest for greedy matching
            for (int i = possibleLengths.length - 1; i >= 0; i--) {
                int length = possibleLengths[i];
                int remainingLength = matchLengthWithBacktracking(input, position + length, patternIndex + 1);
                if (remainingLength >= 0) {
                    return length + remainingLength;
                }
            }
            return -1;
        } else if (currentPattern instanceof ZeroOrOnePattern) {
            // For optional patterns, try both matching and not matching
            ZeroOrOnePattern optPattern = (ZeroOrOnePattern) currentPattern;
            
            // First try matching the optional pattern
            int optLength = optPattern.getElement().matchLength(input, position);
            if (optLength > 0) {
                int remainingLength = matchLengthWithBacktracking(input, position + optLength, patternIndex + 1);
                if (remainingLength >= 0) {
                    return optLength + remainingLength;
                }
            }
            
            // Then try not matching (zero length)
            int remainingLength = matchLengthWithBacktracking(input, position, patternIndex + 1);
            if (remainingLength >= 0) {
                return remainingLength;
            }
            return -1;
        } else {
            // Regular pattern matching
            int length = currentPattern.matchLength(input, position);
            if (length < 0) {
                return -1;
            }
            
            int remainingLength = matchLengthWithBacktracking(input, position + length, patternIndex + 1);
            if (remainingLength >= 0) {
                return length + remainingLength;
            }
            return -1;
        }
    }
}