package nonet.vibes81.whopper.blocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import nonet.vibes81.whopper.util.IGuiTile;
import nonet.vibes81.whopper.util.IRestorableTileEntity;
import nonet.vibes81.whopper.util.WhooperConfig;

import javax.annotation.Nonnull;
import java.util.List;

public class TileWhopper extends TileEntity implements IRestorableTileEntity, ITickable, IGuiTile {

    static final int SIZE = 1;
    private boolean didFirstTick;
    private boolean isRedstonePowered;


    // ----------------------------------------------------------------------------------------
    //Logic
    @Override
    public void update() {
        if (!world.isRemote) {

            if (!this.isRedstonePowered && this.world.getTotalWorldTime() % WhooperConfig.WHOPPER_TICKS == 0) {
                //Pull
                if(this.handlerPull != null) {
                    for(int i = 0; i < this.handlerPull.getSlots(); i++){
                        ItemStack stack = this.handlerPull.extractItem(i, 1, true);
                        ItemStack left = this.inventory.insertItem(0, stack, false);
                        if(!ItemStack.areItemStacksEqual(stack, left)){
                            int toExtract = stack.getCount() - left.getCount();
                            this.handlerPull.extractItem(i, toExtract, false);
                            break;
                        }
                    }
                } else if (WhooperConfig.WHOPPER_VACUUM){
                    List<EntityItem> items = this.world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(this.pos.getX(), this.pos.getY()+0.5, this.pos.getZ(), this.pos.getX()+1, this.pos.getY()+2, this.pos.getZ()+1));
                    if(!items.isEmpty()){
                        for(EntityItem item : items)
                            if (item != null && !item.isDead) {
                                ItemStack left = this.inventory.insertItem(0, item.getItem(), false);
                                item.setItem(left);
                            }
                    }
                }
                //push
                if(this.handlerPush != null && this.inventory != null) {
                    for(int i = 0; i < this.handlerPush.getSlots(); i++){

                        ItemStack stack1 = this.inventory.extractItem(0, 1, true);
                        ItemStack left1 = this.handlerPush.insertItem(i, stack1, false);

                        if(!ItemStack.areItemStacksEqual(stack1, left1)){
                            int toExtract = stack1.getCount() - left1.getCount();
                            this.inventory.extractItem(0, toExtract, false);
                            break;
                        }
                    }
                }
                markDirty();
            }
        }

        if(!this.didFirstTick){
            this.onChange();
            this.didFirstTick = true;
        }
    }

    // ----------------------------------------------------------------------------------------
    //Slot Handlers
    ItemStackHandler inventory = new ItemStackHandler(SIZE) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            if (!world.isRemote) {
                markDirty();
            }
        }
    };
    private IItemHandler handlerPull;
    private IItemHandler handlerPush;

    // ----------------------------------------------------------------------------------------
    //NBT Data
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readRestorableFromNBT(compound, false);
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeRestorableToNBT(compound);
        return compound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound compound) {
        compound.setTag("inventory", inventory.serializeNBT());
    }
    @Override
    public void readRestorableFromNBT(NBTTagCompound compound, boolean reset) {
        inventory.deserializeNBT(compound.getCompoundTag("inventory"));
    }

    int getComparatorLevel()
    {
        if(!WhooperConfig.WHOPPER_COMPARATOR)
            return 0;
        int i = 0;
        float f = 0.0F;
        ItemStack itemStack = inventory.getStackInSlot(0);
        if (!itemStack.isEmpty()) {
            f += (float)itemStack.getCount() / (float) itemStack.getMaxStackSize();
            ++i;
        }
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }

    // ----------------------------------------------------------------------------------------
    //Interactions
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
        return !oldState.getBlock().isAssociatedBlock(newState.getBlock());
    }
    void onChange(){
        this.handlerPull = null;
        this.handlerPush = null;

        this.isRedstonePowered = WhooperConfig.REDSTONE_AWARE && this.world.isBlockPowered(pos);

        //Top of whopper
        TileEntity from = this.world.getTileEntity(this.pos.up());
        if(from != null && from.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN)){
            this.handlerPull = from.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
        }

        //Base of whopper (output)
        IBlockState state = this.world.getBlockState(this.pos);
        EnumFacing facing = state.getValue(BlockWhopper.FACING);
        BlockPos toPos = this.pos.offset(facing);
        if(this.world.isBlockLoaded(toPos)){
            TileEntity to = this.world.getTileEntity(toPos);
            if(to != null){
                EnumFacing opp = facing.getOpposite();
                if(to.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opp)){
                    this.handlerPush = to.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, opp);
                }
            }
        }
    }

    boolean canInteractWith(EntityPlayer playerIn) {
        // If we are too far away from this tile entity you cannot use it
        return !isInvalid() && playerIn.getDistanceSq(pos.add(0.5D, 0.5D, 0.5D)) <= 64D;
    }
    @Override
    public Container createContainer(EntityPlayer player) {
        return new ContainerWhopper(player.inventory, this);
    }
    @Override
    public GuiContainer createGui(EntityPlayer player) {
        return new GuiWhopper(this, new ContainerWhopper(player.inventory, this));
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }
    @SuppressWarnings("NullableProblems")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == null) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            }
            if (facing != EnumFacing.DOWN) {
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
            }
        }
        return super.getCapability(capability, facing);
    }
}
