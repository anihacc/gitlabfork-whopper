package nonet.vibes81.whopper.util;


import net.minecraftforge.common.config.Config;

@Config(modid = Ref.MOD_ID, category = "whopper")
public class WhooperConfig {

    @Config.Comment(value = "Should the whopper disable on redstone signal")
    public static boolean REDSTONE_AWARE = true;

    @Config.Comment(value = "Should the whopper pickup items found on top of it")
    public static boolean WHOPPER_VACUUM = true;

    @Config.Comment(value = "Should the whopper output to comparators")
    public static boolean WHOPPER_COMPARATOR = true;

    @Config.Comment(value = "How fast the whopper works in ticks, vanilla hopper is set to 8 ticks")
    @Config.RangeInt(min = 5, max = 100)
    public static int WHOPPER_TICKS = 12;
}