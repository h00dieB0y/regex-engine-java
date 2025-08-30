/**
 * Matches one or more occurrences of a pattern (+).
 */
public class OneOrMorePattern extends QuantifierPattern {
    
    public OneOrMorePattern(PatternMatcher element) {
        super(element);
    }
    
    @Override
    public int matchLength(String input, int position) {
        // Try to match at least once first
        int firstMatch = element.matchLength(input, position);
        if (firstMatch <= 0) {
            return -1; // Must match at least once
        }
        
        // Collect all possible match positions
        int currentPos = position;
        int matchCount = 0;
        
        while (currentPos < input.length()) {
            int elementLength = element.matchLength(input, currentPos);
            if (elementLength <= 0) {
                break;
            }
            currentPos += elementLength;
            matchCount++;
        }
        
        // Return the total length if we matched at least once
        return matchCount > 0 ? (currentPos - position) : -1;
    }
    
    /**
     * For backtracking support, returns all possible match lengths.
     */
    public int[] getAllPossibleLengths(String input, int position) {
        java.util.List<Integer> lengths = new java.util.ArrayList<>();
        
        int currentPos = position;
        int matchCount = 0;
        
        while (currentPos < input.length()) {
            int elementLength = element.matchLength(input, currentPos);
            if (elementLength <= 0) {
                break;
            }
            currentPos += elementLength;
            matchCount++;
            lengths.add(currentPos - position);
        }
        
        return matchCount > 0 ? lengths.stream().mapToInt(i -> i).toArray() : new int[0];
    }
}