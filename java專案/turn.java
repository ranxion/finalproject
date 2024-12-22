enum Turn{
    FIRST,
    SECOND;
    @Override
    public String toString() {
        switch (this) {
            case FIRST:
                return "Player 1's Turn";
            case SECOND:
                return "Player 2's Turn";
            default:
                return "Unknown Turn";
        }
    }
}
