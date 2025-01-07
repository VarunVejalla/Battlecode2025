package basebot;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws Exception {
        int currentTurn = rc.getRoundNum();
        if(rc.getRoundNum() != currentTurn){
            Util.log("BYTECODE EXCEEDED");
            rc.resign();
        }

        Robot robot;
        switch (rc.getType()){

            case SOLDIER: robot = new Soldier(rc); break;
            case MOPPER: robot = new Mopper(rc); break;
            case SPLASHER: robot = new Splasher(rc); break;


            // Defense Towers
            case LEVEL_ONE_DEFENSE_TOWER: robot = new DefenseTower(rc); break;
            case LEVEL_TWO_DEFENSE_TOWER: robot = new DefenseTower(rc); break;
            case LEVEL_THREE_DEFENSE_TOWER: robot = new DefenseTower(rc); break;


            case LEVEL_ONE_MONEY_TOWER: robot = new MoneyTower(rc); break;
            case LEVEL_TWO_MONEY_TOWER: robot = new MoneyTower(rc); break;
            case LEVEL_THREE_MONEY_TOWER: robot = new MoneyTower(rc); break;


            case LEVEL_ONE_PAINT_TOWER: robot = new PaintTower(rc); break;
            case LEVEL_TWO_PAINT_TOWER: robot = new PaintTower(rc); break;
            case LEVEL_THREE_PAINT_TOWER: robot = new PaintTower(rc); break;




            default: robot = new Tower(rc); break;
        }

//        switch(rc.getType()) {
//
//        }

        while (true) {
            currentTurn = rc.getRoundNum();

//            if (rc.getRoundNum() > 300){
//                rc.resign();
//            }
            try{
                robot.run();
                if(rc.getRoundNum() != currentTurn){
                    Util.log("BYTECODE EXCEEDED");
                    rc.resign();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                Clock.yield();
            }
        }
    }
}