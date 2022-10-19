package gq.bxteam.ndailyrewards.hooks.external;

import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.CitizensAPI;

public class CitizensHK
{
    public static void setup() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create((Class)DailyTrait.class));
    }
}
