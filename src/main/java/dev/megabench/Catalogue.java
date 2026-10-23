package dev.megabench;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.BenchRequirement;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.CraftingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.*;
import java.util.*;

/** References original loaded assets. Recipe+source identity is never collapsed to output identity. */
final class Catalogue {
    record Source(String id,String item,String group,BlockType block,boolean supported,String reason) {
        CraftingBench bench(){return block.getBench() instanceof CraftingBench b?b:null;}
        List<CraftingRecipe> recipes(){return CraftingPlugin.getBenchRecipes(block.getBench());}
        List<CraftingRecipe> unlocks(){
            return CraftingRecipe.getAssetMap().getAssetMap().values().stream()
                .filter(r->{var out=CraftingManager.getOutputItemStacks(r);return out.size()==1 && out.getFirst().getItemId().equals(item) && out.getFirst().getQuantity()==1;})
                .sorted(Comparator.comparing(CraftingRecipe::getId)).toList();
        }
    }
    final SortedMap<String,Source> sources=new TreeMap<>();
    Catalogue(){
        for(var item:Item.getAssetMap().getAssetMap().values()){
            if(item.getBlockId()==null)continue;
            var block=BlockType.getAssetMap().getAsset(item.getBlockId());
            if(block==null||block.getBench()==null||block.getId().startsWith("*")||block.getBench().getId()==null)continue;
            String id=block.getBench().getId();if(id.equals("MegaBench"))continue;
            String group=item.getId().startsWith("EW_")?"Emberworks":item.getId().startsWith("FF_")?"Foundry Floor":item.getId().startsWith("Bench_")?"Hytale":"Other mods";
            boolean ordinary=block.getBench().getType()==BenchType.Crafting && block.getBench() instanceof CraftingBench;
            boolean supported=ordinary&&!group.equals("Other mods");
            var s=new Source(id,item.getId(),group,block,supported,ordinary?"Plugin adapter has not been verified":"Special or processing bench: adapter required");
            sources.putIfAbsent(id,s);
        }
    }
    Source source(String id){var s=sources.get(id);if(s==null)throw new IllegalArgumentException("Bench is no longer loaded");return s;}
    CraftingRecipe recipe(Source source,String id){return source.recipes().stream().filter(r->r.getId().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Recipe is no longer registered for this source"));}
    static BenchRequirement requirement(CraftingRecipe r,String source,int tier){
        if(r.getBenchRequirement()==null)return null;
        return Arrays.stream(r.getBenchRequirement()).filter(b->b.type==BenchType.Crafting&&source.equals(b.id)&&b.requiredTierLevel<=tier&&(b.requiredAugmentTags==null||b.requiredAugmentTags.length==0)).findFirst().orElse(null);
    }
    static String output(CraftingRecipe r){var p=r.getPrimaryOutput();return p==null?r.getId():Objects.toString(p.getItemId(),r.getId());}
}
