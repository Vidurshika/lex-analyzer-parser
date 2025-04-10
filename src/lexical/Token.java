package lexical;

public class Token {
    private String content;
    private String type;
    private int line;
    private boolean isFirstToken;
    private boolean isLastToken;

    // Constructor
    public Token(String content, String type, int line) {
        this.content = content;
        this.type = type;
        this.line = line;
        this.isFirstToken = false;
        this.isLastToken = false;
    }

    // Getter for content
    public String getValue() {
        return content;
    }

    // Getter for type
    public String getType() {
        return type;
    }

    // Getter for line number
    public int getLine() {
        return line;
    }

    // Mark this token as the first token
    public void makeFirstToken() {
        this.isFirstToken = true;
    }

    // Mark this token as the last token
    public void makeLastToken() {
        this.isLastToken = true;
    }

    // Mark this token as a keyword
    public void makeKeyword() {
        this.type = "<KEYWORD>";
    }

    // Check if this token is the first token
    public boolean isFirstToken() {
        return isFirstToken;
    }

    // Check if this token is the last token
    public boolean isLastToken() {
        return isLastToken;
    }

    public void setType(String newType) {
        if (!this.type.equals(newType)) {
            this.type = newType;
        }
    }


    // Override toString for debugging
    @Override
    public String toString() {
        return String.format("Token(content='%s', type='%s', line=%d, isFirstToken=%b, isLastToken=%b)",
                content, type, line, isFirstToken, isLastToken);
    }
}