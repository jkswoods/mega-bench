package dev.megabench;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.ui.builder.*;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Version 1 optional companion seam. Source recipe/progression and native outputs/refunds stay in Mega Bench.
 * See docs/COMPANION-API.md for the world-thread, source validation and UI contracts.
 */
public final class CompanionExtension {
    private CompanionExtension() {}
    public static final int API_VERSION=1;
    public record Context(Store<EntityStore> store,Ref<EntityStore> ref,PlayerRef player,int x,int y,int z) {}
    public interface Provider { /** Return a new player/world-scoped session, or null to decline. */ Session open(Context context); }
    public interface Session {
        /** Read-only discovery: return a non-null validated extra source, never the player's own inventory. */
        ItemContainer inputs(CraftingRecipe recipe,int quantity);
        /** Acquire input leases before native admission. May refuse with IllegalArgumentException/IllegalStateException. */
        default void started() {}
        /** Idempotent lease release after native completion/refund, also after refused admission or during close. */
        default void finished() {}
        /** Idempotently close UI/handlers and release resources. Cleanup callbacks must not throw. */
        default void close() {}
        default String signature() {return "";}
        default String inputLabel() {return "Player inventory + permitted disks";}
        void render(String view,long version,UICommandBuilder commands,UIEventBuilder events);
        void action(String action,String search);
    }
    private static volatile Provider provider;
    public static synchronized void register(int api,Provider value){
        if(api!=API_VERSION||value==null||provider!=null)throw new IllegalStateException("Mega Bench companion API mismatch or provider already registered");
        provider=value;
    }
    public static synchronized void unregister(Provider value){if(provider==value)provider=null;}
    public static String itemName(String id,String language){return NativePresentation.text(id,language);}
    public static void openBench(Store<EntityStore>s,Ref<EntityStore>r,PlayerRef p,int x,int y,int z){MegaBenchPlugin.instance.open(s,r,p,new NativeBridge.Position(x,y,z));}
    static Session open(Context c){var p=provider;return p==null?null:p.open(c);}
}
