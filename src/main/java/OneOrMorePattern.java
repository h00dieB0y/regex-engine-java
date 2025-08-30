/**
 * Matches one or more occurrences of a pattern (+).
 */
public class OneOrMorePattern extends QuantifierPattern {
    
    public OneOrMorePattern(PatternMatcher element) {
        super(element);
    }
    
    @Override
    public int matchLength(String input, int position) {
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
}