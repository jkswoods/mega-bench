package dev.megabench;

import com.google.gson.Gson;
import com.hypixel.hytale.codec.*;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** World-owned resource keyed by player UUID; independent of placement and source bench components. */
public final class Progress implements Resource<EntityStore> {
    private static final Gson JSON = new Gson();
    public static final BuilderCodec<Progress> CODEC = BuilderCodec.builder(Progress.class, Progress::new)
        .append(new KeyedCodec<>("PlayerBenchProgressV1", Codec.STRING),
            (p,v)->p.load(v), Progress::save).add().build();
    static final class PlayerData {
        Map<String,Integer> tiers = new TreeMap<>();
        Pending pending;
    }
    static final class Pending {
        String bench, recipe; int tier; float remaining;
        Pending(String b,String r,int t,float seconds) {bench=b;recipe=r;tier=t;remaining=seconds;}
    }
    final Map<String,PlayerData> players = new TreeMap<>();
    int tier(UUID player,String bench) {
        var p=players.get(player.toString());return p==null?0:p.tiers.getOrDefault(bench,0);
    }
    PlayerData player(UUID player) {return players.computeIfAbsent(player.toString(),k->new PlayerData());}
    void paid(UUID id,String bench,String recipe,int tier,float seconds) {
        var p=player(id);
        if(p.pending!=null || tier!=p.tiers.getOrDefault(bench,0)+1) throw new IllegalStateException("Stale progression request");
        p.pending=new Pending(bench,recipe,tier,seconds);advance(p,0);
    }
    void tick(float dt) {for(var p:players.values())advance(p,dt);}
    private static void advance(PlayerData p,float dt) {
        if(p.pending==null)return;
        p.pending.remaining=Math.max(0,p.pending.remaining-dt);
        if(p.pending.remaining==0){p.tiers.put(p.pending.bench,p.pending.tier);p.pending=null;}
    }
    String save() {return JSON.toJson(players);}
    void load(String value) {
        var type=new com.google.gson.reflect.TypeToken<TreeMap<String,PlayerData>>(){}.getType();
        Map<String,PlayerData> loaded=JSON.fromJson(value,type);
        if(loaded==null||loaded.size()>10000)throw new IllegalStateException("Invalid player progress");
        for(var entry:loaded.entrySet()){
            UUID.fromString(entry.getKey());var p=entry.getValue();
            if(p==null||p.tiers==null||p.tiers.size()>512)throw new IllegalStateException("Invalid bench progress");
            for(var tier:p.tiers.values())if(tier<1||tier>64)throw new IllegalStateException("Invalid bench tier");
            if(p.pending!=null && (!Float.isFinite(p.pending.remaining)||p.pending.remaining<0||p.pending.tier!=p.tiers.getOrDefault(p.pending.bench,0)+1))throw new IllegalStateException("Invalid paid progression");
        }
        players.clear();players.putAll(loaded);
    }
    @Override public Progress clone(){var p=new Progress();p.load(save());return p;}
}
