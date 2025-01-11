package blitz;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the
     * Battlecode world.
     * It is like the main function for your robot. If this method returns, the
     * robot dies!
     *
     * @param rc The RobotController object. You use it to perform actions from this
     *           robot, and to get
     *           information on its current status. Essentially your portal to
     *           interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception {
        int currentTurn = rc.getRoundNum();

        // End early for debugging.
//        Util.log("Current turn: " + currentTurn);
//        if (currentTurn > 200) {
//            Util.log("TRYNA RESIGN");
//            rc.resign();
//        }

        if (rc.getRoundNum() != currentTurn) {
            Util.log("BYTECODE EXCEEDED");
            // rc.resign();
        }

        Robot robot = switch (rc.getType()) {
            case SOLDIER -> new Soldier(rc);
            case MOPPER -> new Mopper(rc);
            case SPLASHER -> new Splasher(rc);

            case LEVEL_ONE_DEFENSE_TOWER, LEVEL_TWO_DEFENSE_TOWER, LEVEL_THREE_DEFENSE_TOWER -> new DefenseTower(rc);
            case LEVEL_ONE_MONEY_TOWER, LEVEL_TWO_MONEY_TOWER, LEVEL_THREE_MONEY_TOWER -> new MoneyTower(rc);
            case LEVEL_ONE_PAINT_TOWER, LEVEL_TWO_PAINT_TOWER, LEVEL_THREE_PAINT_TOWER -> new PaintTower(rc);
            default -> new Tower(rc);
        };

        while (true) {
            currentTurn = rc.getRoundNum();

            try {
                robot.run();
                if (rc.getRoundNum() != currentTurn) {
                    Util.log("BYTECODE EXCEEDED");
                    // rc.resign();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }
}