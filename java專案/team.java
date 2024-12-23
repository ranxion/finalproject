

enum Team{    
    RED,
    BLUE;
    private static final long serialVersionUID = 1L;
    public String toString() {
        switch (this) {
            case BLUE:
                return "Blue";
            case RED:
                return "Red";
            default:
                return "UnknownTeam";
        }
    }
}
