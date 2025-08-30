/**
 * Matches zero or one occurrence of a pattern (?).
 */
public class ZeroOrOnePattern extends QuantifierPattern {
    
    public ZeroOrOnePattern(PatternMatcher element) {
        super(element);
    }
    
    @Override
    public int matchLength(String input, int position) {
        if (position >= input.length()) {
            return 0; // Zero matches at end of string
        }
        
        int elementLength = element.matchLength(input, position);
        if (elementLength > 0) {
            return elementLength; // One match
        }
        
        return 0; // Zero matches
    }
}