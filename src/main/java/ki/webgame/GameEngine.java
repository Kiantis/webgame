package ki.webgame;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import ki.webgame.C.Race;

/**
 * Game logic.
 *
 * Passive gains (for each hour):
 * - energy is incremented by   0.01 * land
 * - if task is S: strength is incremented by 0.001
 * - if task is L: land is incremented by 0.001
 */
public class GameEngine
{
    private static final String CHECKHOURLY_QUERY_MINUTES = " ( abs(timestampdiff(MINUTE, current_timestamp(), lastupdate)) ) ";
    
    private static final String CHECKHOURLY_QUERY = "update users set " +
        // if task is S, increment strength by 0.001 per time passed.
        "strength = strength + "+CHECKHOURLY_QUERY_MINUTES+" * "+C.BASE_GAIN_STRENGTH+" * (if (task = 'S', 1, 0)), " +
        // if task is L, increment landby 0.001 per time passed.
        "land = land + "+CHECKHOURLY_QUERY_MINUTES+" * "+C.BASE_GAIN_LAND+" * (if (task = 'L', 1, 0)), " +
        // increment energy by 0.01 * land per time passed. Also another bonus if user is 'A' (air)
        "energy = energy + "+CHECKHOURLY_QUERY_MINUTES+" * ("+C.BASE_GAIN_ENERGY+" + ("+C.BASE_GAIN_ENERGY_PER_LAND+" * land) + ("+C.AIR_ADDITIONAL_ENERGY_PER_MINUTE + " * if (race = 'A', 1, 0))), " +
        // Update the timestamp or it will add undefinetly
        "lastupdate = now() " +
        // only select users whose at least an hour has passed.
        "where minute(timediff(now(), lastupdate)) >= 1";
    
    /**
     * This method will check if the hour is passed and assign passive points.
     * @throws java.lang.Exception
     */
    public synchronized static void checkHourly() throws Exception
    {
        long t = System.currentTimeMillis();
        
        // Update values directly with one query.
        new DBQuery(CHECKHOURLY_QUERY).execute();
        
        t = System.currentTimeMillis() - t;
        // Log a warning when this whole procedure takes more than 40 milliseconds (1/25 of second)
        if (t > 40)
            Logger.getAnonymousLogger().log(Level.WARNING, "checkHourly() time: {0}ms", t);
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Spends some energy to gain either strength or land.
     * @param username username who is spending.
     * @param task for which task is spending energy.
     * @throws java.lang.Exception
     */
    public static void spendenergy(String username, String task) throws Exception
    {
        new DBQuery("update users set strength = "+C.MAX_STRENGTH+" where strength > "+C.MAX_STRENGTH).execute();
        new DBQuery("update users set land = "+C.MAX_LAND+" where land > "+C.MAX_LAND).execute();
        new DBQuery("update users set energy = "+C.MAX_ENERGY+" where energy > "+C.MAX_ENERGY).execute();
        switch (task)
        {
            case "S":
                // Spend 10% of energy for 5% of strength, energy has a discount if user is 'W' (water)
                new DBQuery("update users set strength = strength + "+C.ENERGY_BOOST_GAIN_STRENGTH+", energy = energy - ("+C.ENERGY_BOOST_COST+" * if (race = 'W', "+C.WATER_ENERGY_BOOST_MILTIPLIER+", 1)) where username = ? and energy >= "+C.ENERGY_BOOST_COST)
                    .addParameter(username)
                    .execute();
                // Check the limits...
                new DBQuery("update users set strength = 1 where username = ? and strength > 1")
                    .addParameter(username)
                    .execute();
                break;
            case "L":
                // Spend 10% of energy for 5% of land, energy has a discount if user is 'W' (water)
                new DBQuery("update users set land = land + "+C.ENERGY_BOOST_GAIN_LAND+", energy = energy - ("+C.ENERGY_BOOST_COST+" * if (race = 'W', "+C.WATER_ENERGY_BOOST_MILTIPLIER+", 1)) where username = ? and energy >= "+C.ENERGY_BOOST_COST)
                    .addParameter(username)
                    .execute();
                // Check the limits...
                new DBQuery("update users set land = 1 where username = ? and land > 1")
                    .addParameter(username)
                    .execute();
                break;
        }
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Spends a percentage of score points (10%, minimum points required: 100) to gain 10% rage.
     * @param username username who is spending.
     * @throws java.lang.Exception
     */
    public static void boostrage(String username) throws Exception
    {
        // Spend 10% of points for 1% of rage, only with minimum 100 points
        new DBQuery("update users set "
            + "rage = rage + "+C.BOOST_RAGE_GAIN_RAGE+", "
            + "score = score - score * "+C.BOOST_RAGE_COST_SCORE+" "
            + "where username = ? and score >= "+C.BOOST_RAGE_MIN_SCORE + " and ((rage + "+C.BOOST_RAGE_GAIN_RAGE+") < "+C.BOOST_RAGE_MAX_RAGE + ")")
            .addParameter(username)
            .execute();
        // Check the limits...
        new DBQuery("update users set rage = "+C.MAX_RAGE+" where username = ? and rage > "+C.MAX_RAGE)
            .addParameter(username)
            .execute();
    }
    
    /**
     * Reincarnation can change your race but you will lose half points and restart with the default values.
     * @param username username to change race
     * @param race the new race
     * @throws java.lang.Exception
     */
    public static void reincarnate(String username, Race race) throws Exception
    {
        if (race == null)
            return;
        
        // Pay the score price (with the minimum score value) to change race
        // Also reset all the values to start ones
        // Also ensure race to be different, so that no cost would be spent
        new DBQuery("update users set "
            + "score = score * "+C.REINCARNATE_COST_SCORE_MULTIPLIER+", "
            + "race = ?, "
            + "strength = "+C.START_STRENGTH+", "
            + "land = "+C.START_LAND+", "
            + "energy = "+C.START_ENERGY+", "
            + "rage = "+C.START_RAGE+", "
            + "where race <> ? username = ? and score > "+C.REINCARNATE_MIN_SCORE)
            .addParameter(race.name())
            .addParameter(race.name())
            .addParameter(username)
            .execute();
    }
    
    // -------------------------------------------------------------------------
    
    public static void applyUserCaps(Connection c) throws Exception
    {
        // Correct too many high values (1 = 100%) of all users, before doing anything else.
        // Since this brings up expensive query, do it each attack instead of the checkHourly.
        // The UI will always display overflow values as 100% anyway, but it's when an attack is done that things matter.
        new DBQuery(c, "update users set strength = "+C.MAX_STRENGTH+" where strength > "+C.MAX_STRENGTH).execute();
        new DBQuery(c, "update users set land = "+C.MAX_LAND+" where land > "+C.MAX_LAND).execute();
        new DBQuery(c, "update users set energy = "+C.MAX_ENERGY+" where energy > "+C.MAX_ENERGY).execute();
        new DBQuery(c, "update users set rage = "+C.MAX_RAGE+" where rage > "+C.MAX_RAGE).execute();
        // Correct any negative value
        new DBQuery(c, "update users set strength = 0 where strength < 0").execute();
        new DBQuery(c, "update users set land = 0 where land < 0").execute();
        new DBQuery(c, "update users set energy = 0 where energy < 0").execute();
        new DBQuery(c, "update users set rage = 0 where rage < 0").execute();
        // Also clean history older than 7 days
        new DBQuery("delete from attack_history where datediff(now(), attack_time) > 7").execute();
    }
    
    // -------------------------------------------------------------------------
    
    /**
     * Statistics of the attack
     */
    public static class AttackStats
    {
        // Gained score form this attack
        public long score;
        // Wheter the attack was void
        public boolean tie;
        // Wheter could not attack because there was not enough land
        public boolean defenderLandTooLow;
        // Wheter could not attack because there was not enough energy form the attacker
        public boolean attackerEnergyTooLow;
        // Wheter could not attack because there was not enough strength form the attacker
        public boolean attackerStrengthTooLow;
        // the attack difference from the defense, the total win/lose amount
        public double result;
        // The amount of gained (positive) or lost (negative) of strength
        public double strength;
        // The amount of gained (positive) or lost (negative) of land
        public double land;
        // The amount of lost energy (is negative)
        public double energy;
    }
    
    /**
     * Executes an attack to the specified user.
     * @param fromUser the user making the attack
     * @param toUser the user attacked
     * @return the statistics of the attack
     * @throws Exception 
     */
    public static AttackStats attack(String fromUser, String toUser) throws Exception
    {
        long t = System.currentTimeMillis();
        
        AttackStats result = null;
        
        // Use a separate connection so that web can handle a TRANSACTION
        try (Connection c = DBManager.getConnection())
        {
            applyUserCaps(c);
            
            // Start the attack
            c.setAutoCommit(false);
            try
            {
                final AttackStats ar = new AttackStats();
                new DBQuery(c,
                      "select strength, land, energy, rage, id, race from users where username = ? "
                    + "union "
                    + "select strength, land, energy, rage, id, race from users where username = ?")
                    .addParameter(fromUser)
                    .addParameter(toUser)
                    .execute((ResultSet rs) ->
                    {
                        if (!rs.next())
                            return;
                        
                        double astrength = rs.getDouble(1);
                        double aland = rs.getDouble(2);
                        double aenergy = rs.getDouble(3);
                        double arage = rs.getDouble(4);
                        long aid = rs.getLong(5);
                        String arace = rs.getString(6);
                        
                        if (!rs.next())
                            return;
                        
                        double dstrength = rs.getDouble(1);
                        double dland = rs.getDouble(2);
                        double denergy = rs.getDouble(3);
                        double drage = rs.getDouble(4);
                        long did = rs.getLong(5);
                        String drace = rs.getString(6);

                        // Here, calculate the attack results
                        // on attack, counts strength*energy
                        // on defense, counts land*energy
                        // this because if attacker loses, the defender gains some of the attacker strength
                        // if the defender loses, the attacker gains some of the defender land
                        // Also add the earth defense if the user has race 'E' (earth)
                        double vresult = astrength * aenergy + arage - (dland * C.DEFEND_LAND_FACTOR) * denergy - drage - (drace.equals("E") ? C.EARTH_BONUS_DEFENSE : 0);
                        
                        // Decrement a 1% of rage (used above) for the user attacking and normalize it
                        new DBQuery(c, "update users set rage = rage - "+C.RAGE_CONSUME_AS_ATTACKER+" where id = ?").addParameter(aid).execute();
                        
                        applyAttackResult(ar, c, aid, did, astrength, dstrength, aland, dland, aenergy, denergy, vresult);
                    });
                
                c.commit();
                // Only assing pointer if commit was OK
                result = ar;
            }
            finally
            {
                c.rollback();
            }
            c.setAutoCommit(true);

            // re-clean values
            applyUserCaps(c);
        }
        
        t = System.currentTimeMillis() - t;
        // Log a warning when this whole procedure takes more than 500 milliseconds (half a second)
        if (t > 500)
            Logger.getAnonymousLogger().log(Level.WARNING, "attack() time: {0}ms", t);
        
        return result;
    }
    
    private static void applyAttackResult(AttackStats ar, Connection c, long aid, long did, double astrength, double dstrength, double aland, double dland, double aenergy, double denergy, double vresult) throws Exception
    {
        // Can't attack when the defender has 5% or less land
        if (dland <= C.ATTACK_MINIMUM_LAND_VALUE_FOR_DEFENDER)
        {
            ar.defenderLandTooLow = true;
            return;
        }
        // Can't attack when the attacker has less than 5% of either energy or strength
        if (astrength < C.ATTACK_MINIMUM_STRENGTH_VALUE_FOR_ATTACKER ||
            aenergy < C.ATTACK_MINIMUM_ENERGY_VALUE_FOR_ATTACKER)
        {
            ar.attackerStrengthTooLow = astrength < C.ATTACK_MINIMUM_STRENGTH_VALUE_FOR_ATTACKER;
            ar.attackerEnergyTooLow = aenergy < C.ATTACK_MINIMUM_ENERGY_VALUE_FOR_ATTACKER;
            return;
        }
        
        // Assign value of winning result to the result
        // Even if it's a tie, it will tell the user at which value it was anyway.
        ar.result = vresult;
        
        if (vresult < C.ATTACK_MINIMUM_RESULT_FOR_WIN && vresult > -C.ATTACK_MINIMUM_RESULT_FOR_WIN)
        {
            // If result is within 0.05 and -0.05 then it is considered a tie
            ar.tie = true;
            return;
        }
        
        // vresult > 0.05: attacker won
        if (vresult >= C.ATTACK_MINIMUM_RESULT_FOR_WIN)
        {
            // each 5% of difference would equal to 1 point, multiplying by 20 (100/5)
            ar.score = (long)(vresult * C.ATTACK_SCORE_MULTIPLIER_FROM_RESULT);
            
            double land1 = aland * vresult;
            double land2 = dland * vresult;
            // MIN here, winning, positive
            double alandDelta = land1 > land2 ? land2 : land1;
            // OWN here (losing, negative)
            double dlandDelta = -land2;
            double aenergyDelta = -aenergy * vresult;
            double denergyDelta = denergy * vresult; // Defender gains energy instead
            // Gain the least of the two
            ar.land = alandDelta;
            // Attacking drains half of energy
            ar.energy = aenergyDelta;
            // Update DB, inserting the history of the attack
            new DBQuery(c,
                "insert into attack_history ("
                + "attacking_userid, "
                + "defending_userid, "
                + "result, "
                + "attacker_strength_delta, "
                + "attacker_land_delta, "
                + "attacker_energy_delta, "
                + "defender_strength_delta, "
                + "defender_land_delta, "
                + "defender_energy_delta) values (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .addParameter(aid)
                .addParameter(did)
                .addParameter(vresult)
                .addParameter(0)
                .addParameter(alandDelta)
                .addParameter(aenergyDelta)
                .addParameter(0)
                .addParameter(dlandDelta)
                .addParameter(denergyDelta)
                .execute();
            // Update DB, attacker, only winning attacker makes score points
            new DBQuery(c, "update users set land = land + ?, energy = energy + ?, score = score + ? where id = ?")
                .addParameter(alandDelta)
                .addParameter(aenergyDelta)
                .addParameter(ar.score)
                .addParameter(aid)
                .execute();
            // Update DB, defender
            new DBQuery(c, "update users set land = land + ?, energy = energy + ? where id = ?")
                .addParameter(dlandDelta)
                .addParameter(denergyDelta)
                .addParameter(did)
                .execute();
            
            // Increment defender's RAGE if the attacker WON, defender has a multiplier if is 'F' (fire)
            new DBQuery(c, "update users set rage = rage + ("+C.RAGE_GAIN_AS_DEFENDER+" * if (race = 'F', "+C.FIRE_RAGE_GAIN_MULTIPLER+", 1)) where id = ?")
                .addParameter(did)
                .execute();
        }
        // vresult < -0.05: defender won
        else if (vresult <= -C.ATTACK_MINIMUM_RESULT_FOR_WIN)
        {
            double strength1 = astrength * -vresult;
            double strength2 = dstrength * -vresult;
            // OWN here, losing (negative)
            double astrengthDelta = -strength1;
            // MIN here, winning, turn on positive
            double dstrengthDelta = strength1 > strength2 ? strength2 : strength1;
            double aenergyDelta = -aenergy * -vresult;
            double denergyDelta = denergy * -vresult; // Defender gains energy instead
            // Lose the most of the two
            ar.strength = astrengthDelta;
            // Attacking drains half of energy
            ar.energy = aenergyDelta;
            // Update DB, inserting the history of the attack
            new DBQuery(c,
                "insert into attack_history ("
                + "attacking_userid, "
                + "defending_userid, "
                + "result, "
                + "attacker_strength_delta, "
                + "attacker_land_delta, "
                + "attacker_energy_delta, "
                + "defender_strength_delta, "
                + "defender_land_delta, "
                + "defender_energy_delta) values (?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .addParameter(aid)
                .addParameter(did)
                .addParameter(vresult)
                .addParameter(astrengthDelta)
                .addParameter(0)
                .addParameter(aenergyDelta)
                .addParameter(dstrengthDelta)
                .addParameter(0)
                .addParameter(denergyDelta)
                .execute();
            // Update DB, attacker
            new DBQuery(c, "update users set strength = strength + ?, energy = energy + ? where id = ?")
                .addParameter(astrengthDelta)
                .addParameter(aenergyDelta)
                .addParameter(aid)
                .execute();
            // Update DB, defender
            new DBQuery(c, "update users set strength = strength + ?, energy = energy + ? where id = ?")
                .addParameter(dstrengthDelta)
                .addParameter(denergyDelta)
                .addParameter(did)
                .execute();
        }
    }
}
