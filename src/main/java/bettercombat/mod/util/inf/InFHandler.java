package bettercombat.mod.util.inf;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.Loader;

public abstract class InFHandler {

    private static Boolean isFork = null;

    public static boolean isMultipart(Entity entity) {
        if(isFork == null) isFork = isLightningFork();
        return isFork ? InFLightningWrapper.isMultipart(entity) : InFMainWrapper.isMultipart(entity);
    }

    public static Entity getMultipartParent(Entity entity) {
        if(isFork == null) isFork = isLightningFork();
        return isFork ? InFLightningWrapper.getMultipartParent(entity) : InFMainWrapper.getMultipartParent(entity);
    }
    
    public static boolean isMultipartParent(Entity entity) {
        if(isFork == null) isFork = isLightningFork();
        return isFork? InFLightningWrapper.isMultipartParent(entity) : InFMainWrapper.isMultipartParent(entity);
    }

    private static boolean isLightningFork() {
        String[] arrOfStr = Loader.instance().getIndexedModList().get("iceandfire").getVersion().split("\\.");
        try {
            int i = Integer.parseInt(String.valueOf(arrOfStr[0]));
            if(i >= 2) return true;
        }
        catch(Exception ignored) { }
        return false;
    }
}