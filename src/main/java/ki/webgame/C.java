package ki.webgame;

public class C
{
    // Starting values
    // WARNING: these values if changed won't change the default values of the database DDL!
    // They will only affect reincarnation in this way
    public static final double START_STRENGTH = 0.25d;
    public static final double START_LAND = 0.25d;
    public static final double START_ENERGY = 0.5d;
    public static final double START_RAGE = 0d;
    // Maximum values, 1 means 100%
    public static final double MAX_STRENGTH = 1d;
    public static final double MAX_LAND = 1d;
    public static final double MAX_ENERGY = 1d;
    public static final double MAX_RAGE = 1d;
    // Energy boost, spend 10% of energy to gain either 5% of strength or 5% of land
    public static final double ENERGY_BOOST_COST = 0.1d;
    public static final double ENERGY_BOOST_GAIN_STRENGTH = 0.05d;
    public static final double ENERGY_BOOST_GAIN_LAND = 0.05d;
    // Rage boosting: 10% of points (minimum 100 total present) for 1% rage, max boostable up to 10% rage total
    public static final int BOOST_RAGE_MIN_SCORE = 100;
    public static final double BOOST_RAGE_COST_SCORE = 0.1d;
    public static final double BOOST_RAGE_GAIN_RAGE = 0.05d;
    public static final double BOOST_RAGE_MAX_RAGE = 0.5d;
    // Base gains: STRENGTH and LAND are multiplied by the time slot (0.1%)
    public static final double BASE_GAIN_STRENGTH = 0.001d;
    public static final double BASE_GAIN_LAND = 0.001d;
    // This is the basic energy gain per time slot (0.1%)
    public static final double BASE_GAIN_ENERGY = 0.001d;
    // This is the additional energy gained which will be multiplied by LAND per time slot (1% of LAND)
    public static final double BASE_GAIN_ENERGY_PER_LAND = 0.01d;
    // Minimum values valid for an attack to occur
    public static final double ATTACK_MINIMUM_LAND_VALUE_FOR_DEFENDER = 0.1d;
    public static final double ATTACK_MINIMUM_STRENGTH_VALUE_FOR_ATTACKER = 0.1d;
    public static final double ATTACK_MINIMUM_ENERGY_VALUE_FOR_ATTACKER = 0.1d;
    // This one is considered also in negative
    public static final double ATTACK_MINIMUM_RESULT_FOR_WIN = 0.01d;
    // each 1% of difference in a WINNING attack (0.01) would equal to 1 point, multiplying by 100
    public static final double ATTACK_SCORE_MULTIPLIER_FROM_RESULT = 100d;
    // Only 90% of land counts as a defending bonus, so that no defenders would become invincible with 100% land and 100% energy
    public static final double DEFEND_LAND_FACTOR = 0.9d;
    // Rage usage
    public static final double RAGE_GAIN_AS_DEFENDER = 0.01d;
    public static final double RAGE_CONSUME_AS_ATTACKER = 0.01d;
    // Special race powers
    public static final double FIRE_RAGE_GAIN_MULTIPLER = 3d;
    public static final double AIR_ADDITIONAL_ENERGY_PER_MINUTE = 0.005d;
    public static final double WATER_ENERGY_BOOST_MILTIPLIER = 0.5d;
    public static final double EARTH_BONUS_DEFENSE = 0.2d;
    // Reincarnation minimal score value to trigger
    public static final long REINCARNATE_MIN_SCORE = 100;
    // Spend half score to reincarnate...
    public static final double REINCARNATE_COST_SCORE_MULTIPLIER = 0.5d;
    // Possible races - ensured to pass a valid value to function, internal use
    public enum Race {F, A, W, E};
}
