package dev.megabench;

import com.google.gson.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Server-owned discovery policy. No links to another plugin's classes or manifest. */
final class Compatibility {
    record BenchRule(boolean disabled,String group,String itemId,String unlockRecipeId) {}
    static final String DEFAULT_JSON="""
        {
          "Schema": 1,
          "AutoDiscover": true,
          "AllowBenchHandIn": true,
          "Benches": {}
        }
        """;
    final boolean autoDiscover,allowBenchHandIn;
    final Map<String,BenchRule> benches;
    Compatibility(boolean discover,boolean handIn,Map<String,BenchRule> rules){autoDiscover=discover;allowBenchHandIn=handIn;benches=Map.copyOf(rules);}
    static Compatibility defaults(){return parse(DEFAULT_JSON);}
    static Compatibility load(Path file){
        try{
            Files.createDirectories(file.getParent());
            if(!Files.exists(file))try{Files.writeString(file,DEFAULT_JSON,StandardOpenOption.CREATE_NEW);}catch(FileAlreadyExistsException ignored){}
            if(Files.size(file)>1_048_576)throw new IllegalArgumentException("Compatibility file exceeds 1 MiB");
            return parse(Files.readString(file));
        }catch(IOException|RuntimeException ex){throw new IllegalStateException("Cannot load Mega Bench compatibility.json: "+ex.getMessage(),ex);}
    }
    static Compatibility parse(String json){
        var root=JsonParser.parseString(json).getAsJsonObject();
        only(root,Set.of("Schema","AutoDiscover","AllowBenchHandIn","Benches"));
        if(!root.has("Schema")||!root.get("Schema").toString().equals("1"))throw new IllegalArgumentException("Compatibility Schema must be 1");
        var rules=new TreeMap<String,BenchRule>();
        if(root.has("Benches")){
            var entries=root.getAsJsonObject("Benches");
            if(entries.size()>512)throw new IllegalArgumentException("Too many bench rules");
            for(var entry:entries.entrySet()){
                id(entry.getKey());var rule=entry.getValue().getAsJsonObject();
                only(rule,Set.of("Disabled","Group","ItemId","UnlockRecipeId"));
                String group=text(rule,"Group",80),item=text(rule,"ItemId",256),recipe=text(rule,"UnlockRecipeId",256);
                if(item!=null)id(item);if(recipe!=null)id(recipe);
                rules.put(entry.getKey(),new BenchRule(bool(rule,"Disabled",false),group,item,recipe));
            }
        }
        return new Compatibility(bool(root,"AutoDiscover",true),bool(root,"AllowBenchHandIn",true),rules);
    }
    private static void only(JsonObject value,Set<String> fields){for(String key:value.keySet())if(!fields.contains(key))throw new IllegalArgumentException("Unknown compatibility field: "+key);}
    private static boolean bool(JsonObject value,String key,boolean fallback){
        if(!value.has(key))return fallback;
        var field=value.get(key);if(!field.isJsonPrimitive()||!field.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException(key+" must be true or false");
        return field.getAsBoolean();
    }
    private static String text(JsonObject value,String key,int limit){
        if(!value.has(key))return null;
        var field=value.get(key);if(!field.isJsonPrimitive()||!field.getAsJsonPrimitive().isString())throw new IllegalArgumentException(key+" must be text");
        String text=field.getAsString();if(text.isBlank()||text.length()>limit||text.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Invalid "+key);
        return text;
    }
    private static void id(String value){if(value.isBlank()||value.length()>256||value.chars().anyMatch(c->Character.isWhitespace(c)||Character.isISOControl(c)))throw new IllegalArgumentException("Invalid bench, item or recipe ID");}
    boolean allows(String id){var rule=benches.get(id);return (autoDiscover||rule!=null)&&(rule==null||!rule.disabled());}
    static String group(String pack){
        if(pack==null||pack.isBlank())return "Other mods";
        if(pack.equals("Hytale:Hytale"))return "Hytale";
        return NativePresentation.label(pack.substring(pack.lastIndexOf(':')+1));
    }
}
