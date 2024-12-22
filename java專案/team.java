enum Team{
    RED,
    BLUE;
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
