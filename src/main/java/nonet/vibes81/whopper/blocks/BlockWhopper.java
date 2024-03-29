package nonet.vibes81.whopper.blocks;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nonet.vibes81.whopper.Main;
import nonet.vibes81.whopper.util.IRestorableTileEntity;
import nonet.vibes81.whopper.util.Ref;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.inventory.InventoryHelper.spawnItemStack;

public class BlockWhopper extends Block implements ITileEntityProvider {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", facing -> facing != EnumFacing.UP);
    public static final ResourceLocation BLOCK_WHOPPER = new ResourceLocation(Ref.MOD_ID, "whopper");

    //Diet hopper code by RWTema
    private static final EnumMap<EnumFacing, List<AxisAlignedBB>> bounds;
    protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.625D, 1.0D);
    protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
    protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);

    static {
        List<AxisAlignedBB> commonBounds = ImmutableList.of(
                makeAABB(0, 10, 0, 16, 16, 16),
                makeAABB(4, 4, 4, 12, 10, 12)
        );
        bounds = Stream.of(EnumFacing.values())
                .filter(t -> t != EnumFacing.UP)
                .collect(Collectors.toMap(a -> a, a -> new ArrayList<>(commonBounds), (u, v) -> {
                    throw new IllegalStateException();
                }, () -> new EnumMap<>(EnumFacing.class)));

        bounds.get(EnumFacing.DOWN).add(makeAABB(6, 0, 6, 10, 4, 10));

        bounds.get(EnumFacing.NORTH).add(makeAABB(6, 4, 0, 10, 8, 4));
        bounds.get(EnumFacing.SOUTH).add(makeAABB(6, 4, 12, 10, 8, 16));

        bounds.get(EnumFacing.WEST).add(makeAABB(0, 4, 6, 4, 8, 10));
        bounds.get(EnumFacing.EAST).add(makeAABB(12, 4, 6, 16, 8, 10));
    }
    private static AxisAlignedBB makeAABB(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return new AxisAlignedBB(fromX / 16F, fromY / 16F, fromZ / 16F, toX / 16F, toY / 16F, toZ / 16F);
    }

    public BlockWhopper(){
        super(Material.WOOD);
        setHardness(1.0f);
        setResistance(1.0f);
        setHarvestLevel("axe", 0);
        setTranslationKey(Ref.MOD_ID + ".whopper");
        setRegistryName(BLOCK_WHOPPER);
        setCreativeTab(CreativeTabs.REDSTONE);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN));
    }

    // ----------------------------------------------------------------------------------------
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){

        EnumFacing opp = facing.getOpposite();
        return this.getDefaultState().withProperty(FACING, opp == EnumFacing.UP ? EnumFacing.DOWN : opp);
    }
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
    }
    @Override
    public int getMetaFromState(IBlockState state){
        return state.getValue(FACING).getIndex();
    }
    @Override
    protected BlockStateContainer createBlockState(){
        return new BlockStateContainer(this, FACING);
    }
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot){
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirror){
        return this.withRotation(state, mirror.toRotation(state.getValue(FACING)));
    }
    @Override
    public boolean isFullCube(IBlockState state){
        return false;
    }
    @Override
    public boolean isOpaqueCube(IBlockState state){
        return false;
    }
    @Override
    public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
        return bounds.get(blockState.getValue(FACING)).stream()
                .map(bb -> rayTrace(pos, start, end, bb))
                .anyMatch(Objects::nonNull)
                ? super.collisionRayTrace(blockState, worldIn, pos, start, end) : null;
    }
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FULL_BLOCK_AABB;
    }
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return true;
    }
    // ----------------------------------------------------------------------------------------
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileWhopper();
    }

    // ----------------------------------------------------------------------------------------
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        if (!worldIn.isRemote) {
            TileEntity tile = worldIn.getTileEntity(pos);
            if (tile instanceof TileWhopper) {
                return ((TileWhopper) tile).getComparatorLevel();
            }
        }
        return 0;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.neighborChange(worldIn, pos);

    }
    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor){
        super.onNeighborChange(world, pos, neighbor);

        if(world instanceof World){
            this.neighborChange((World)world, pos);
        }
    }
    private void neighborChange(World world, BlockPos pos) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof IRestorableTileEntity) {
                ((TileWhopper) tile).onChange();
            }
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

       if (stack.hasDisplayName()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileWhopper) {
                ((TileWhopper) te).setCustomName(stack.getDisplayName());
            }
       }

    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Only execute on the server
        if (world.isRemote) {
            return true;
        }
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof IRestorableTileEntity)) {
            return false;
        }
        player.openGui(Main.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof TileWhopper)
        {
            ItemStack itemstack = ((TileWhopper) tileentity).inventory.getStackInSlot(0);

            if (!itemstack.isEmpty())
            {
                int x = pos.getX();
                int y = pos.getY();
                int z = pos.getZ();
                spawnItemStack(worldIn, x, y, z, itemstack);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) {
            return true; // If it will harvest, delay deletion of the block until after getDrops
        }
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    @Override
    public void getDrops(NonNullList<ItemStack> result, IBlockAccess world, BlockPos pos, IBlockState metadata, int fortune) {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileWhopper && ((TileWhopper) tileEntity).hasCustomName()) {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
            String customName = ((TileWhopper) tileEntity).getName();
            stack.setRepairCost(0);
            stack.setStackDisplayName(customName);
            result.add(stack);
        }
        else {
            super.getDrops(result, world, pos, metadata, fortune);
        }
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        super.harvestBlock(world, player, pos, state, te, stack);
        world.setBlockToAir(pos);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
    }
}

