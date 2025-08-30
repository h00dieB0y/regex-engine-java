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
        
        // For backtracking support, return just the first match length
        // The SequencePattern will handle trying longer matches via getAllPossibleLengths
        return firstMatch;
    }
    
    /**
     * For backtracking support, returns all possible match lengths.
     */
    public int[] getAllPossibleLengths(String input, int position) {
        java.util.List<Integer> lengths = new java.util.ArrayList<>();
        
        int currentPos = position;
        
        // Try to match the element repeatedly and collect all possible stopping points
        while (currentPos < input.length()) {
            int elementLength = element.matchLength(input, currentPos);
            if (elementLength <= 0) {
                break;
            }
            currentPos += elementLength;
            lengths.add(currentPos - position);
        }
        
        // Convert to array, ensuring we have at least one match (since this is OneOrMore)
        return lengths.isEmpty() ? new int[0] : lengths.stream().mapToInt(i -> i).toArray();
    }
}