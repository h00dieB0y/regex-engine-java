import java.util.Scanner;

/**
 * Main entry point for the regex matcher application.
 */
public class Main {
    private final PatternFactory patternFactory;
    
    public Main() {
        this.patternFactory = new PatternFactory();
    }

    public static void main(String[] args) {
        new Main().run(args);
    }
    
    private void run(String[] args) {
        if (args.length != 2 || !"-E".equals(args[0])) {
            System.err.println("Usage: ./your_program.sh -E <pattern>");
            System.exit(1);
        }

        String patternString = args[1];
        
        try (Scanner scanner = new Scanner(System.in)) {
            String inputLine = scanner.nextLine();
            
            PatternMatcher pattern = patternFactory.createPattern(patternString);
            boolean matches = pattern.matches(inputLine);
            
            System.exit(matches ? 0 : 1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}