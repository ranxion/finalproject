enum Turn{
    FIRST,
    SECOND;
    private static final long serialVersionUID = 1L;
    @Override
    public String toString() {
        switch (this) {
            case FIRST:
                return "BlueTeam's Turn";
            case SECOND:
                return "RedTeam's Turn";
            default:
                return "Unknown's Turn";
        }
    }
}
