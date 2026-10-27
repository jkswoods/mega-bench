package dev.megabench;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;

/** Creative access is temporary; it never changes paid player/world progress. */
final class CraftingAccess {
    private CraftingAccess(){}
    static boolean creative(GameMode mode){return mode==GameMode.Creative;}
    static int tier(boolean creative,int paidTier){return creative?PrivateBenchContext.CAPACITY:paidTier;}
    static boolean permits(boolean creative,boolean supported,CraftingRecipe recipe,String source,int paidTier){
        int tier=tier(creative,paidTier);
        return supported&&tier>=1&&tier<=PrivateBenchContext.CAPACITY&&Catalogue.requirement(recipe,source,tier)!=null;
    }
}
