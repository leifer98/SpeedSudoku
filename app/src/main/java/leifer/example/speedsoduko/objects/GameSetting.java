package leifer.example.speedsoduko.objects;

public class GameSetting {
    private int turnTimeLimit, comboTimeLimit;
    private int points, pointsMultiplier;

    public GameSetting() {    }

    public GameSetting(int turnTimeLimit,int comboTimeLimit,int points,int pointsMultiplier) {
        this.turnTimeLimit = turnTimeLimit;
        this.comboTimeLimit = comboTimeLimit;
        this.points = points;
        this.pointsMultiplier = pointsMultiplier;
    }

    public int getTurnTimeLimit() {
        return turnTimeLimit;
    }

    public void setTurnTimeLimit(int turnTimeLimit) {
        this.turnTimeLimit = turnTimeLimit;
    }

    public int getComboTimeLimit() {
        return comboTimeLimit;
    }

    public void setComboTimeLimit(int comboTimeLimit) {
        this.comboTimeLimit = comboTimeLimit;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getPointsMultiplier() {
        return pointsMultiplier;
    }

    public void setPointsMultiplier(int pointsMultiplier) {
        this.pointsMultiplier = pointsMultiplier;
    }
}
