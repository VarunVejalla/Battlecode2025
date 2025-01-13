package commsTesting;

import battlecode.common.*;

public class Soldier extends Bunny {

    boolean rightSide = false;
    MapLocation randomDestination = new MapLocation((int)(Math.random() * rc.getMapWidth()), 40);

    public Soldier(RobotController rc) throws GameActionException {
        super(rc);

        if(rc.getLocation().x > rc.getMapWidth() / 2){
            rightSide = true;
        }

    }

    public void run() throws GameActionException {
        super.run(); // Call the shared logic for all bunnies
        moveLogic();
    }

    /**
     * Attempt to paint or attack nearby tiles if possible.
     */
    public void paintOrAttack() throws GameActionException {

    }

    /**
     * Choose where to move:
     */
    public void moveLogic() throws GameActionException {

        // Testing code for A map: largeCommsTestSimple, single player
        if(rc.getRoundNum() < 40){
            nav.goTo(new MapLocation(8, 40), 0);
        }
        else if(rc.getRoundNum() < 80){
            nav.goTo(new MapLocation(8, 10), 0);
        }
        else{
            Util.log("END OF TEST!!!");
            comms.describeWorld();
            rc.resign();
        }
//
//
//
//        // TESTING CODE FOR B map: largeCommsTestSimple, double player
        // two players explore the same area of a map and then one
//        if(rc.getRoundNum() < 60){
//            nav.goTo(new MapLocation(8, 40), 0);
//        }
//        else if(rc.getRoundNum() < 100){
//            nav.goTo(new MapLocation(8, 10), 0);
//        }
//        else{
//            rc.resign();
//        }




        // TESTING CODE for C map: largeCommsTestSimple, single player, multi-visit
        // Description: The soldier explores, visits the paint tower, explores a different area, then revisits the paint tower
//        if(rc.getRoundNum() < 50){
//            if(rightSide){
//                nav.goTo(new MapLocation(40, 40), 0);
//            }
//            else{
//                nav.goTo(new MapLocation(8, 40), 0);
//            }
//        }
//
//        else if(rc.getRoundNum() < 100){
//            nav.goTo(new MapLocation(8,10), 0);
//        }
//
//        if (rc.getRoundNum() > 100) {
//            rc.resign();
//        }



        // TESTING CODE for D map: largeCommsTestSimple, single player, multi-visit
        // Description: The soldier explores, visits the paint tower, explores a different area, then revisits the paint tower

//        if(rc.getRoundNum() < 10){
//            return;
//        }
//        else if(rc.getRoundNum() < 80){
//            nav.goTo(randomDestination, 0);
//        }
//        else if(rc.getRoundNum() < 130){
//            nav.goTo(new MapLocation(20, 20), 0);
//        }
//        else{
//            nav.goTo(new MapLocation(8,10), 0);
//        }
//        if(rc.getRoundNum() > 160){
//            rc.resign();
//        }


    }

}