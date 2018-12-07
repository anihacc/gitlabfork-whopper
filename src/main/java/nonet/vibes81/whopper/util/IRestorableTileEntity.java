package nonet.vibes81.whopper.util;

import net.minecraft.nbt.NBTTagCompound;

public interface IRestorableTileEntity {

    void readRestorableFromNBT(NBTTagCompound compound, boolean reset);

    void writeRestorableToNBT(NBTTagCompound compound);
}