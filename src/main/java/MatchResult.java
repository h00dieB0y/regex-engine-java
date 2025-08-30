/**
 * Represents the result of a pattern matching operation.
 */
public class MatchResult {
    private final boolean matched;
    private final int startPosition;
    private final int length;
    
    private MatchResult(boolean matched, int startPosition, int length) {
        this.matched = matched;
        this.startPosition = startPosition;
        this.length = length;
    }
    
    public static MatchResult success(int startPosition, int length) {
        return new MatchResult(true, startPosition, length);
    }
    
    public static MatchResult failure() {
        return new MatchResult(false, -1, -1);
    }
    
    public boolean isMatched() {
        return matched;
    }
    
    public int getStartPosition() {
        return startPosition;
    }
    
    public int getLength() {
        return length;
    }
    
    public int getEndPosition() {
        return startPosition + length;
    }
}