package dev.megabench;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.event.events.ecs.CraftRecipeEvent;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.*;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

public final class MegaBenchPlugin extends JavaPlugin {
    static MegaBenchPlugin instance;
    static ResourceType<EntityStore,Progress> progressType;
    Catalogue catalogue;
    Compatibility compatibility;
    static final Map<UUID,NativeBridge> sessions=new java.util.concurrent.ConcurrentHashMap<>();
    public MegaBenchPlugin(JavaPluginInit init){super(init);}
    @Override protected void setup(){
        instance=this;
        progressType=getEntityStoreRegistry().registerResource(Progress.class,"MegaBenchWorldPlayersV1",Progress.CODEC);
        getCodecRegistry(Interaction.CODEC).register("MB_Open",OpenInteraction.class,OpenInteraction.CODEC);
        getEntityStoreRegistry().registerSystem(new ProgressTick());
        getEntityStoreRegistry().registerSystem(new CraftGate());
    }
    @Override protected void start(){
        compatibility=Compatibility.load(getDataDirectory().resolve("compatibility.json"));
        catalogue=new Catalogue(compatibility);
        getLogger().atInfo().log("MB_CATALOGUE %s",catalogue.sources.values().stream().map(s->s.group()+":"+s.id()+":"+s.supported()+":"+s.recipes().size()).toList());
        for(var source:catalogue.sources.values())if(source.supported()&&source.bench()!=null&&source.bench().getCategories()!=null)getLogger().atInfo().log("MB_CATEGORIES %s %s",source.id(),Arrays.stream(source.bench().getCategories()).map(c->c.getId()).toList());
        getLogger().atInfo().log("Mega Bench: discovers native crafting benches from loaded packs. Compatibility settings: %s",getDataDirectory().resolve("compatibility.json"));
    }
    static Progress progress(Store<EntityStore>s){return s.getResource(progressType);}
    static boolean present(World w,NativeBridge.Position p){
        var section=w.getChunkStore().getChunkSectionReferenceAtBlock(p.x(),p.y(),p.z());
        if(section==null||!section.isValid())return false;
        var blocks=w.getChunkStore().getStore().getComponent(section,BlockSection.getComponentType());if(blocks==null)return false;
        var b=BlockType.getAssetMap().getAsset(blocks.get(p.x()&31,p.y()&31,p.z()&31));return b!=null&&b.getId().equals("MB_MegaBench");
    }
    static void cue(Store<EntityStore>s,Ref<EntityStore>r,String family){var sounds=SoundEvent.getAssetMap();String id="MB_SFX_"+family;if(sounds.getAsset(id)!=null)SoundUtil.playSoundEvent2d(r,sounds.getIndex(id),SoundCategory.UI,s);}
    void open(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,NativeBridge.Position at){
        if(!present(s.getExternalData().getWorld(),at)||p.getTransform().getPosition().distanceSquared(at.x()+.5,at.y()+.5,at.z()+.5)>64)return;
        var player=s.getComponent(r,Player.getComponentType());if(player==null)return;
        // Scan when opening, after all plugins have had the chance to register their assets.
        try{catalogue=new Catalogue(compatibility);var page=new MegaBenchPage(p,at,catalogue);player.getPageManager().openCustomPage(r,s,page);cue(s,r,"Handling");}
        catch(RuntimeException ex){p.sendMessage(Message.raw("Mega Bench: "+ex.getMessage()));}
    }
    static final class ProgressTick extends TickingSystem<EntityStore>{
        @Override public void tick(float dt,int index,Store<EntityStore>s){progress(s).tick(dt);for(var session:sessions.values())if(session.store==s)session.pulse();}
    }
    static final class CraftGate extends EntityEventSystem<EntityStore,CraftRecipeEvent.Pre>{
        CraftGate(){super(CraftRecipeEvent.Pre.class);}
        @Override public Query<EntityStore> getQuery(){return Player.getComponentType();}
        @Override public void handle(int index,ArchetypeChunk<EntityStore>chunk,Store<EntityStore>s,CommandBuffer<EntityStore>buffer,CraftRecipeEvent.Pre event){
            var player=chunk.getComponent(index,PlayerRef.getComponentType());if(player==null)return;
            var session=sessions.get(player.getUuid());
            if(session!=null&&session.store==s&&!NativeBridge.admitted(session,event.getCraftedRecipe(),event.getQuantity()))event.setCancelled(true);
        }
    }
}
