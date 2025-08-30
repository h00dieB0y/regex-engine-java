/**
 * Matches any character (.).
 */
public class AnyCharacterPattern extends PatternElement {
    
    @Override
    public boolean matchesCharacter(char c) {
        return true;
    }
}