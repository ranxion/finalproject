

enum Team{    
    RED,
    BLUE;
    private static final long serialVersionUID = 1L;
    public String toString() {
        switch (this) {
            case BLUE:
                return "BlueTeam";
            case RED:
                return "RedTeam";
            default:
                return "UnknownTeam";
        }
    }
}
