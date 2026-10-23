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
    record Source(String id,String item,String group,BlockType block,boolean supported,String reason,List<String> items,String unlockRecipeId,boolean allowHandIn) {
        CraftingBench bench(){return block.getBench() instanceof CraftingBench b?b:null;}
        List<CraftingRecipe> recipes(){return CraftingPlugin.getBenchRecipes(block.getBench());}
        List<CraftingRecipe> unlocks(){
            return CraftingRecipe.getAssetMap().getAssetMap().values().stream()
                .filter(r->unlockRecipeId==null||r.getId().equals(unlockRecipeId))
                .filter(r->{var out=CraftingManager.getOutputItemStacks(r);return out.size()==1 && items.contains(out.getFirst().getItemId()) && out.getFirst().getQuantity()==1;})
                .sorted(Comparator.comparing(CraftingRecipe::getId)).toList();
        }
        boolean handIn(){return supported&&allowHandIn&&unlockRecipeId==null&&unlocks().isEmpty();}
    }
    final SortedMap<String,Source> sources=new TreeMap<>();
    Catalogue(){this(Compatibility.defaults());}
    Catalogue(Compatibility policy){
        var candidates=new TreeMap<String,List<Item>>();
        for(var item:Item.getAssetMap().getAssetMap().values()){
            if(item.getBlockId()==null)continue;
            var block=BlockType.getAssetMap().getAsset(item.getBlockId());
            if(block==null||block.getBench()==null||block.getId().startsWith("*")||block.getBench().getId()==null)continue;
            String id=block.getBench().getId();if(id.equals("MegaBench"))continue;
            candidates.computeIfAbsent(id,unused->new ArrayList<>()).add(item);
        }
        for(var entry:candidates.entrySet()){
            String id=entry.getKey();var items=entry.getValue();items.sort(Comparator.comparing(Item::getId));
            var rule=policy.benches.get(id);var item=items.getFirst();String reason="";
            if(rule!=null&&rule.itemId()!=null){
                item=items.stream().filter(i->i.getId().equals(rule.itemId())).findFirst().orElse(item);
                if(!item.getId().equals(rule.itemId()))reason="Configured item does not belong to this bench";
            }
            var block=BlockType.getAssetMap().getAsset(item.getBlockId());
            // Custom bench subclasses may enforce additional plugin logic which this adapter cannot preserve.
            boolean ordinary=ordinary(block);
            if(reason.isEmpty()&&!ordinary)reason="Special station: use the original bench";
            if(reason.isEmpty()&&!policy.allows(id))reason="Disabled by server compatibility settings";
            String group=rule!=null&&rule.group()!=null?rule.group():Compatibility.group(Item.getAssetMap().getAssetPack(item.getId()));
            String unlock=rule==null?null:rule.unlockRecipeId();
            var source=new Source(id,item.getId(),group,block,reason.isEmpty(),reason,items.stream().map(Item::getId).toList(),unlock,policy.allowBenchHandIn);
            if(reason.isEmpty()&&unlock!=null&&source.unlocks().isEmpty()){
                source=new Source(id,item.getId(),group,block,false,"Configured unlock recipe is missing or does not make one bench",source.items(),unlock,policy.allowBenchHandIn);
            }
            sources.put(id,source);
        }
    }
    static boolean ordinary(BlockType block){return block.getClass()==BlockType.class&&block.getBench().getType()==BenchType.Crafting&&block.getBench().getClass()==CraftingBench.class;}
    Source source(String id){var s=sources.get(id);if(s==null)throw new IllegalArgumentException("Bench is no longer loaded");return s;}
    CraftingRecipe recipe(Source source,String id){return source.recipes().stream().filter(r->r.getId().equals(id)).findFirst().orElseThrow(()->new IllegalArgumentException("Recipe is no longer registered for this source"));}
    static BenchRequirement requirement(CraftingRecipe r,String source,int tier){
        if(r.getBenchRequirement()==null)return null;
        return Arrays.stream(r.getBenchRequirement()).filter(b->b.type==BenchType.Crafting&&source.equals(b.id)&&b.requiredTierLevel<=tier&&(b.requiredAugmentTags==null||b.requiredAugmentTags.length==0)).findFirst().orElse(null);
    }
    static String output(CraftingRecipe r){
        var primary=r.getPrimaryOutput();if(primary!=null&&primary.getItemId()!=null)return primary.getItemId();
        return CraftingManager.getOutputItemStacks(r).stream().map(com.hypixel.hytale.server.core.inventory.ItemStack::getItemId).filter(Objects::nonNull).findFirst().orElse(r.getId());
    }
}
