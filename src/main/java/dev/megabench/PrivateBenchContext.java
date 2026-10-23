package dev.megabench;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.*;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;

/** Transient source configuration for one native manager; never registered as an asset. */
final class PrivateBenchContext {
    static final int CAPACITY=64;
    static BlockType create(BlockType original,int paidTier){
        if(!(original.getBench() instanceof CraftingBench)||paidTier<1||paidTier>CAPACITY)throw new IllegalArgumentException("Unsupported paid crafting tier");
        return new ContextBlock(original,paidTier);
    }
    static float seconds(BlockType source,CraftingRecipe recipe,int paidTier){
        float seconds=recipe.getTimeSeconds();
        if(seconds>0&&paidTier>1){var data=source.getBench().getTierLevel(paidTier-1);if(data!=null)seconds-=seconds*data.getCraftingTimeReductionModifier();}
        return seconds;
    }
    private static final class ContextBlock extends BlockType {
        ContextBlock(BlockType original,int paidTier){super(original);bench=new ContextBench((CraftingBench)original.getBench(),paidTier);}
        // The native helper must retain this private copy rather than re-fetching a shared asset.
        @Override public String getDefaultStateKey(){return null;}
    }
    private static final class ContextBench extends CraftingBench {
        final CraftingBench original;final int paidTier;
        ContextBench(CraftingBench source,int paidTier){
            original=source;this.paidTier=paidTier;type=source.getType();id=source.getId();categories=source.getCategories();
            descriptiveLabel=source.getDescriptiveLabel();
            localOpenSoundEventId=source.getLocalOpenSoundEventId();localOpenSoundEventIndex=source.getLocalOpenSoundEventIndex();
            localCloseSoundEventId=source.getLocalCloseSoundEventId();localCloseSoundEventIndex=source.getLocalCloseSoundEventIndex();
            completedSoundEventId=source.getCompletedSoundEventId();completedSoundEventIndex=source.getCompletedSoundEventIndex();
            failedSoundEventId=source.getFailedSoundEventId();failedSoundEventIndex=source.getFailedSoundEventIndex();
            benchUpgradeSoundEventId=source.getBenchUpgradeSoundEventId();benchUpgradeSoundEventIndex=source.getBenchUpgradeSoundEventIndex();
            benchUpgradeCompletedSoundEventId=source.getBenchUpgradeCompletedSoundEventId();benchUpgradeCompletedSoundEventIndex=source.getBenchUpgradeCompletedSoundEventIndex();
        }
        @Override public BenchTierLevel getTierLevel(int capacityIndex){return original.getTierLevel(paidTier-1);}
        // Paid upgrades belong to Progress. A native window may not upgrade the shared access block.
        @Override public BenchUpgradeRequirement getUpgradeRequirement(int tier){return null;}
    }
}
