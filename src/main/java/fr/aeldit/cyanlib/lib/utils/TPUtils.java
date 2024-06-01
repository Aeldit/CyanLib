package fr.aeldit.cyanlib.lib.utils;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TPUtils
{
    public static int getRequiredXpLevelsToTp(
            @NotNull ServerPlayerEntity player, @NotNull BlockPos tpPos, int opt
    )
    {
        double distanceX = player.getX() - tpPos.getX();
        double distanceZ = player.getZ() - tpPos.getZ();

        // Converts to a positive distance
        if (distanceX < 0)
        {
            distanceX *= -1;
        }
        if (distanceZ < 0)
        {
            distanceZ *= -1;
        }
        // Minecraft doesn't center the position to the middle of the block but in 1 corner,
        // so this allows for a better centering
        ++distanceX;
        ++distanceZ;

        int coordinatesDistance = (int) (distanceX + distanceZ) / 2;
        return coordinatesDistance < opt ? 1 : 1 + coordinatesDistance / opt;
    }
}
