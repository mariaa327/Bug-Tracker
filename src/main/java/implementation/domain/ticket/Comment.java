package implementation.domain.ticket;

public class Comment {
    private String author;
    private String content;
    private String createdAt;

    /**
     * creates the comment
     * @param author the comment author
     * @param content the comment content
     * @param createdAt the creation timestamp
     */
    public Comment(final String author, final String content,
                   final String createdAt) {
        this.author = author;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * returns the comment author
     * @return the comment author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * returns the comment content
     * @return the comment content
     */
    public String getContent() {
        return content;
    }

    /**
     * returns the comment creation timestamp
     * @return the comment creation timestamp
     */
    public String getCreatedAt() {
        return createdAt;
    }
}
