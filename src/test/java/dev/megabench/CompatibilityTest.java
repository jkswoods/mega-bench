package dev.megabench;

import java.nio.file.*;

public final class CompatibilityTest {
    static int checks;
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;}
    static void refuses(String json){try{Compatibility.parse(json);throw new AssertionError("Accepted invalid compatibility settings");}catch(RuntimeException expected){checks++;}}
    public static void main(String[] args)throws Exception{
        var defaults=Compatibility.defaults();
        check(defaults.allows("UnlistedWorkshop")&&defaults.allows("CompletelyDifferentPrefix"),"Unknown names are enabled without a dependency");
        check(defaults.allowBenchHandIn,"Physical bench payment available when no recipe exists");
        var selected=Compatibility.parse("""
            {"Schema":1,"AutoDiscover":false,"Benches":{"UnlistedWorkshop":{"Group":"Custom workshops"},"Blocked":{"Disabled":true}}}
            """);
        check(selected.allows("UnlistedWorkshop")&&!selected.allows("AnotherWorkshop")&&!selected.allows("Blocked"),"Explicit opt-in and exclusions are enforced");
        var disabled=Compatibility.parse("""
            {"Schema":1,"AllowBenchHandIn":false,"Benches":{"Blocked":{"Disabled":true},"Custom":{"ItemId":"Odd_Table","UnlockRecipeId":"Odd_Recipe"}}}
            """);
        check(!disabled.allows("Blocked")&&disabled.allows("AnotherWorkshop")&&!disabled.allowBenchHandIn,"Exclusion does not disable unrelated native benches");
        check(disabled.benches.get("Custom").itemId().equals("Odd_Table")&&disabled.benches.get("Custom").unlockRecipeId().equals("Odd_Recipe"),"Explicit original identity mapping retained");
        check(Compatibility.group("Hytale:Hytale").equals("Hytale")&&Compatibility.group("Example:OtherWorkshop").equals("Other Workshop"),"Pack grouping is independent of item prefixes");
        refuses("{\"Schema\":2}");refuses("{\"Schema\":1,\"AutoDiscover\":\"false\"}");
        refuses("{\"Schema\":1,\"AutoDisover\":false}");
        refuses("{\"Schema\":1,\"Benches\":{\"X\":{\"Disabled\":\"true\"}}}");
        refuses("{\"Schema\":1,\"Benches\":{\"X\":{\"Group\":\"bad\\nlabel\"}}}");
        refuses("{\"Schema\":1,\"Benches\":{\"X\":{\"UnlockRecipeId\":\"bad id\"}}}");
        refuses("{\"Schema\":1,\"Benches\":{\"X\":{\"ForceEnable\":true}}}");
        var temp=Files.createTempDirectory("mega-bench-config-test-");
        try{
            var file=temp.resolve("compatibility.json");Compatibility.load(file);
            check(Files.readString(file).equals(Compatibility.DEFAULT_JSON),"First load writes a reviewable config");
            String settings="{\"Schema\":1,\"AllowBenchHandIn\":false}";Files.writeString(file,settings);
            check(!Compatibility.load(file).allowBenchHandIn&&Files.readString(file).equals(settings),"Restart preserves existing settings");
        }finally{Files.deleteIfExists(temp.resolve("compatibility.json"));Files.delete(temp);}
        System.out.println("PASS "+checks+" compatibility policy checks; not native client evidence");
    }
}
