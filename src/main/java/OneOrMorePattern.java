/**
 * Matches one or more occurrences of a pattern (+).
 */
public class OneOrMorePattern extends QuantifierPattern {
    
    public OneOrMorePattern(PatternMatcher element) {
        super(element);
    }
    
    @Override
    public int matchLength(String input, int position) {
        // For patterns that need backtracking (like in sequences), 
        // we should return the maximum possible match length
        int currentPos = position;
        int matchCount = 0;
        
        // Must match at least once
        while (currentPos < input.length()) {
            int elementLength = element.matchLength(input, currentPos);
            if (elementLength <= 0) {
                break;
            }
            
            currentPos += elementLength;
            matchCount++;
        }
        
        return matchCount > 0 ? (currentPos - position) : -1;
    }
    
    /**
     * Try to match with a specific maximum length for backtracking support.
     */
    public int matchLengthUpTo(String input, int position, int maxLength) {
        int currentPos = position;
        int matchCount = 0;
        int totalLength = 0;
        
        // Must match at least once
        while (currentPos < input.length() && totalLength < maxLength) {
            int elementLength = element.matchLength(input, currentPos);
            if (elementLength <= 0) {
                break;
            }
            
            if (totalLength + elementLength > maxLength) {
                break;
            }
            
            currentPos += elementLength;
            matchCount++;
            totalLength += elementLength;
        }
        
        return matchCount > 0 ? totalLength : -1;
    }
}