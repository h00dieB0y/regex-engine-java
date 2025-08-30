/**
 * Matches a literal character.
 */
public class LiteralCharacterPattern extends PatternElement {
    private final char character;
    
    public LiteralCharacterPattern(char character) {
        this.character = character;
    }
    
    @Override
    public boolean matchesCharacter(char c) {
        return c == character;
    }
}