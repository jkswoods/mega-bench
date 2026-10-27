package dev.megabench;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.builtin.crafting.component.*;
import com.hypixel.hytale.builtin.crafting.window.*;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.*;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Per-player entitlement and private source context. ECS components keep their EXACT native classes. */
final class NativeBridge {
    record Position(int x,int y,int z) {}
    private static final ScopedValue<NativeBridge> ADMISSION=ScopedValue.newInstance();
    static final int MAX_QUANTITY=16;
    final Store<EntityStore> store;final Ref<EntityStore> ref;final PlayerRef player;
    final Position at;final BenchBlock physical;final CraftingManager manager;
    final BooleanSupplier screenValid;
    String auditBefore="";CraftingRecipe activeRecipe;int activeQuantity=1;Runnable uiRefresh=()->{};
    String status="Select a workbench and recipe";
    NativeBridge(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,Position at,BooleanSupplier valid){
        store=s;ref=r;player=p;this.at=at;screenValid=valid;
        manager=s.getComponent(r,CraftingManager.getComponentType());
        if(manager==null||manager.getClass()!=CraftingManager.class||manager.hasBenchSet()||manager.getRemainingQueueSize()!=0)throw new IllegalStateException("Close the current crafting window and finish queued work");
        var w=s.getExternalData().getWorld();var blockRef=BlockModule.getBlockEntity(w,at.x(),at.y(),at.z());
        if(blockRef==null)throw new IllegalStateException("Mega Bench block entity is not ready");
        physical=w.getChunkStore().getStore().getComponent(blockRef,BenchBlock.getComponentType());
        if(physical==null||physical.getClass()!=BenchBlock.class||physical.getTierLevel()!=PrivateBenchContext.CAPACITY)throw new IllegalStateException("Replace this older Mega Bench in a backed-up test world before using this build");
        MegaBenchPlugin.sessions.put(p.getUuid(),this);MegaBenchPlugin.instance.getLogger().atInfo().log("MB_INVENTORY_TYPES %s",types(inventory(s,r)));
    }
    static ItemContainer inventory(ComponentAccessor<EntityStore>s,Ref<EntityStore>r){return InventoryComponent.getCombined(s,r,InventoryComponent.HOTBAR_STORAGE_BACKPACK);}
    boolean idle(){return manager.getRemainingQueueSize()==0;}
    boolean creative(){var p=store.getComponent(ref,Player.getComponentType());return p!=null&&CraftingAccess.creative(p.getGameMode());}
    void close(){
        if(ref.isValid()&&store.getComponent(ref,CraftingManager.getComponentType())==manager)manager.clearBench(ref,store);
        MegaBenchPlugin.sessions.remove(player.getUuid(),this);activeRecipe=null;
    }
    boolean valid(){return ref.isValid()&&screenValid.getAsBoolean()&&MegaBenchPlugin.present(store.getExternalData().getWorld(),at)&&player.getTransform().getPosition().distanceSquared(at.x()+.5,at.y()+.5,at.z()+.5)<=64;}
    void pulse(){if(!valid()){close();return;}if(activeRecipe!=null&&idle()){MegaBenchPlugin.instance.getLogger().atInfo().log("MB_COMPLETE_AUDIT recipe=%s quantity=%s expected=%s before=%s after=%s",activeRecipe.getId(),activeQuantity,CraftingManager.getOutputItemStacks(activeRecipe,activeQuantity),auditBefore,counts(inventory(store,ref)));status="Craft complete";activeRecipe=null;}uiRefresh.run();}
    boolean cancelCraft(){
        var recipe=activeRecipe;
        if(!manager.cancelAllCrafting(ref,store))return false;
        activeRecipe=null;status="Craft cancelled";
        MegaBenchPlugin.instance.getLogger().atInfo().log("MB_CANCEL_AUDIT recipe=%s quantity=%s before=%s after=%s",recipe==null?"":recipe.getId(),activeQuantity,auditBefore,counts(inventory(store,ref)));
        return true;
    }
    static boolean admitted(NativeBridge bridge,CraftingRecipe recipe,int count){return ADMISSION.isBound()&&ADMISSION.get()==bridge&&bridge.activeRecipe==recipe&&count==bridge.activeQuantity&&bridge.valid();}
    static void knowledge(Store<EntityStore>s,Ref<EntityStore>r,CraftingRecipe recipe){
        var p=s.getComponent(r,Player.getComponentType());var primary=recipe.getPrimaryOutput();
        if(recipe.isKnowledgeRequired()&&(primary==null||!p.getPlayerConfigData().getKnownRecipes().contains(primary.getItemId())))throw new IllegalArgumentException("Learn this source recipe first");
        int level=MemoriesPlugin.get().getMemoriesLevel(s.getExternalData().getWorld().getGameplayConfig());
        if(recipe.getRequiredMemoriesLevel()>1&&recipe.getRequiredMemoriesLevel()>level)throw new IllegalArgumentException("The original world memories requirement is not met");
    }
    void craft(Catalogue.Source source,CraftingRecipe recipe,int paidTier,int quantity){
        if(!valid())throw new IllegalArgumentException("Open a nearby Mega Bench");
        if(quantity<1||quantity>MAX_QUANTITY)throw new IllegalArgumentException("Choose between 1 and "+MAX_QUANTITY+" crafts");
        boolean creative=creative();
        if(!CraftingAccess.permits(creative,source.supported(),recipe,source.id(),paidTier))throw new IllegalArgumentException("Workbench or required tier is locked, or this recipe needs an unsupported augment");
        var pending=MegaBenchPlugin.progress(store).player(player.getUuid()).pending;
        if(!creative&&pending!=null&&pending.bench.equals(source.id()))throw new IllegalArgumentException("Wait for this workbench upgrade to finish");
        if(!idle())throw new IllegalArgumentException("Finish or cancel the current craft first");
        knowledge(store,ref,recipe);var inv=inventory(store,ref);
        if(!creative&&!inv.canRemoveMaterials(CraftingManager.getInputMaterials(recipe,quantity)))throw new IllegalArgumentException("Missing ingredients for the full batch");
        String beforeRoom=counts(inv);boolean roomAvailable=room(store,ref,recipe,quantity);String afterRoom=counts(inv);
        if(!beforeRoom.equals(afterRoom)){MegaBenchPlugin.instance.getLogger().atSevere().log("MB_CAPACITY_MUTATION before=%s after=%s",beforeRoom,afterRoom);throw new IllegalStateException("Output safety check unavailable; use the source workbench");}
        if(!roomAvailable)throw new IllegalArgumentException("Make room for every output before crafting");
        auditBefore=counts(inv);
        if(manager.hasBenchSet())manager.clearBench(ref,store);
        // The exact native manager reads the current game mode when taking inputs.
        // Creative needs no paid speed upgrade and retains the original recipe time.
        var context=PrivateBenchContext.create(source.block(),creative?1:paidTier);
        manager.setBench(at.x(),at.y(),at.z(),context);activeRecipe=recipe;activeQuantity=quantity;
        var window=new BridgeWindow(at,context,physical,this);
        boolean accepted=ScopedValue.where(ADMISSION,this).call(()->manager.queueCraft(ref,store,window,0,recipe,quantity,inv,CraftingManager.InputRemovalType.NORMAL));
        if(!accepted){activeRecipe=null;manager.clearBench(ref,store);throw new IllegalArgumentException("Native crafting authority refused this recipe");}
        status="Craft queued";
        MegaBenchPlugin.instance.getLogger().atInfo().log("MB_NATIVE_ADMIT player=%s source=%s paidTier=%s recipe=%s quantity=%s timePerCraft=%s capacity=%s creative=%s exactComponents=true",player.getUuid(),source.id(),paidTier,recipe.getId(),quantity,PrivateBenchContext.seconds(source.block(),recipe,creative?1:paidTier),physical.getTierLevel(),creative);
        MegaBenchPlugin.cue(store,ref,"Craft");
    }
    static String types(ItemContainer inv){if(inv instanceof com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer c){var names=new ArrayList<String>();for(int i=0;i<c.getContainersSize();i++)names.add(types(c.getContainer(i)));return inv.getClass().getName()+names;}return inv.getClass().getName();}
    static String counts(ItemContainer inv){var totals=new TreeMap<String,Integer>();for(short i=0;i<inv.getCapacity();i++){var item=inv.getItemStack(i);if(!ItemStack.isEmpty(item))totals.merge(item.getItemId(),item.getQuantity(),Integer::sum);}return totals.toString();}
    static boolean room(ComponentAccessor<EntityStore>s,Ref<EntityStore>r,CraftingRecipe recipe){
        return room(s,r,recipe,1);
    }
    static boolean room(ComponentAccessor<EntityStore>s,Ref<EntityStore>r,CraftingRecipe recipe,int quantity){
        var outputs=CraftingManager.getOutputItemStacks(recipe,quantity);var settings=s.getComponent(r,PlayerSettings.getComponentType());if(settings==null)settings=PlayerSettings.defaults();final var preferences=settings;
        return OutputSpace.fits(outputs,out->{
            var nativeRoute=InventoryUtils.getContainerForItemPickup(r,out.getItem(),preferences,s);
            for(var order:List.of(InventoryComponent.HOTBAR_STORAGE_BACKPACK,InventoryComponent.STORAGE_HOTBAR_BACKPACK,InventoryComponent.BACKPACK_STORAGE_HOTBAR)){
                if(nativeRoute!=InventoryComponent.getCombined(s,r,order))continue;
                var physical=new ArrayList<ItemContainer>();
                for(var type:order){var component=s.getComponent(r,type);if(component!=null)physical.add(component.getInventory());}
                return new com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer(physical.toArray(ItemContainer[]::new));
            }
            throw new UnsupportedOperationException("Unverified native pickup route");
        });
    }
    static final class BridgeWindow extends CraftingWindow {
        final NativeBridge bridge;
        BridgeWindow(Position at,com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType context,BenchBlock b,NativeBridge bridge){super(com.hypixel.hytale.protocol.packets.window.WindowType.BasicCrafting,at.x(),at.y(),at.z(),0,context,b);this.bridge=bridge;}
        @Override public void setBlockInteractionState(String state,World w){}
        @Override public void updateCraftingJob(float progress){bridge.status="Crafting: "+Math.round(progress*100)+"%";}
        @Override public void updateQueueSize(int remaining){if(remaining==0)bridge.status="Craft complete";}
    }
}
