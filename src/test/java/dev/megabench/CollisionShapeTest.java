package dev.megabench;

import com.google.gson.*;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.*;
import org.joml.*;
import java.nio.file.*;
import java.util.*;

/** Compare the shipped collider with transformed model corners using native block rotations. */
public final class CollisionShapeTest {
    static int checks;
    static void check(boolean ok,String reason){if(!ok)throw new AssertionError(reason);checks++;}
    static final class NativeBoxes extends BlockBoundingBoxes {
        NativeBoxes(Box[] boxes){baseDetailBoxes=boxes;processConfig();}
    }
    static double number(JsonObject object,String field,double fallback){return object!=null&&object.has(field)?object.get(field).getAsDouble():fallback;}
    static Vector3d vector(JsonObject object,double fallback){return new Vector3d(number(object,"x",fallback),number(object,"y",fallback),number(object,"z",fallback));}
    static void corners(JsonArray nodes,Matrix4d parent,List<Vector3d> result){
        for(var value:nodes){
            var node=value.getAsJsonObject();var position=vector(node.getAsJsonObject("position"),0);var q=node.getAsJsonObject("orientation");
            var transform=new Matrix4d(parent).translate(position).rotate(new Quaterniond(number(q,"x",0),number(q,"y",0),number(q,"z",0),number(q,"w",1)));
            var shape=node.getAsJsonObject("shape");
            if(shape!=null&&"box".equals(shape.get("type").getAsString())&&(!shape.has("visible")||shape.get("visible").getAsBoolean())){
                var size=vector(shape.getAsJsonObject("settings").getAsJsonObject("size"),0).mul(vector(shape.getAsJsonObject("stretch"),1)).mul(.5);
                var offset=vector(shape.getAsJsonObject("offset"),0);
                for(int x:new int[]{-1,1})for(int y:new int[]{-1,1})for(int z:new int[]{-1,1}){
                    var corner=new Vector3d(size).mul(x,y,z).add(offset);transform.transformPosition(corner);result.add(corner.div(32));
                }
            }
            if(node.has("children"))corners(node.getAsJsonArray("children"),transform,result);
        }
    }
    static Box box(JsonObject value){
        var min=value.getAsJsonObject("Min");var max=value.getAsJsonObject("Max");
        return new Box(min.get("X").getAsDouble(),min.get("Y").getAsDouble(),min.get("Z").getAsDouble(),max.get("X").getAsDouble(),max.get("Y").getAsDouble(),max.get("Z").getAsDouble());
    }
    public static void main(String[] args)throws Exception{
        var resources=Path.of(args[0]);var model=JsonParser.parseString(Files.readString(resources.resolve("Common/Blocks/MegaBench/MB_MegaBench.blockymodel"))).getAsJsonObject();
        var hitbox=JsonParser.parseString(Files.readString(resources.resolve("Server/Item/Block/Hitboxes/MB_MegaBench.json"))).getAsJsonObject();
        var points=new ArrayList<Vector3d>();corners(model.getAsJsonArray("nodes"),new Matrix4d(),points);check(!points.isEmpty(),"Model has visible geometry");
        var boxes=new ArrayList<Box>();for(var entry:hitbox.getAsJsonArray("Boxes"))boxes.add(box(entry.getAsJsonObject()));
        var nativeBoxes=new NativeBoxes(boxes.toArray(Box[]::new));
        for(var yaw:Rotation.values()){
            var rotation=RotationTuple.of(yaw,Rotation.None,Rotation.None);var bounds=nativeBoxes.get(yaw,Rotation.None,Rotation.None).getBoundingBox();
            var modelMin=new Vector3d(Double.POSITIVE_INFINITY);var modelMax=new Vector3d(Double.NEGATIVE_INFINITY);
            for(var point:points){var world=rotation.rotatedVector(new Vector3d(point)).add(.5,0,.5);modelMin.min(world);modelMax.max(world);}
            for(int axis=0;axis<3;axis++){
                double low=modelMin.get(axis)-bounds.min.get(axis),high=bounds.max.get(axis)-modelMax.get(axis);
                check(low>=-1e-6&&high>=-1e-6,"Collider clips visible geometry at "+yaw+" axis "+axis);
                check(low<=.02&&high<=.02,"Collider is displaced or excessively oversized at "+yaw+" axis "+axis);
            }
        }
        System.out.println("PASS "+checks+" model/collision checks across all four native yaw rotations; native walking/targeting checked separately");
    }
}
