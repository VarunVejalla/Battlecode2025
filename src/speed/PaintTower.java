package speed;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;

public class PaintTower extends Tower {
    int paintPerRound = 0;
    int estimatedRC = 0;

    public PaintTower(RobotController rc) throws GameActionException {
        super(rc);
    }

    public void run() throws GameActionException {
        super.run();
        boolean replenishingRobotsNearby = rc.senseNearbyRobots(GameConstants.PAINT_TRANSFER_RADIUS_SQUARED, myTeam).length > 0;
        if(currentPaint > previousPaint && !replenishingRobotsNearby) {
            paintPerRound = currentPaint - previousPaint;
        }
        estimatedRC = (paintPerRound - rc.getType().paintPerTurn) / 3;
        Util.addToIndicatorString("ERC: " + estimatedRC);

        int numMoneyTowers = estimatedChipsPerRound / (20 + 3 * estimatedRC);
        numMoneyTowers = Math.min(numMoneyTowers, rc.getNumberTowers());
        int numNonMoneyTowers = rc.getNumberTowers() - numMoneyTowers;
        Util.addToIndicatorString("EMT: " + numMoneyTowers);
        Util.addToIndicatorString("ENMT: " + numNonMoneyTowers);
    }
}
