package ValkyrienWarfareControl.Block;

import ValkyrienWarfareBase.API.IBlockForceProvider;
import ValkyrienWarfareBase.API.Vector;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAirShipEngine extends Block implements IBlockForceProvider {

	public static final PropertyDirection FACING = PropertyDirection.create("facing");

	public BlockAirShipEngine(Material materialIn) {
		super(materialIn);
		this.translucent = true;

	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		EnumFacing facing = BlockPistonBase.getFacingFromEntity(pos, placer);
		if (placer.isSneaking()) {
			facing = facing.getOpposite();
		}
		worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { FACING });
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		int i = ((EnumFacing) state.getValue(FACING)).getIndex();
		return i;
	}

	@Override
	public Vector getBlockForce(World world, BlockPos pos, IBlockState state, Entity shipEntity,
			double secondsToApply) {
		EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
		Vector acting = new Vector(0, 0, 0);
		if (!world.isBlockPowered(pos)) {
			return acting;
		}
		double power = 4000D * secondsToApply;
		switch (enumfacing) {
		case DOWN:
			acting = new Vector(0, power, 0);
			break;
		case UP:
			acting = new Vector(0, -power, 0);
			break;
		case EAST:
			acting = new Vector(-power, 0, 0);
			break;
		case NORTH:
			acting = new Vector(0, 0, power);
			break;
		case WEST:
			acting = new Vector(power, 0, 0);
			break;
		case SOUTH:
			acting = new Vector(0, 0, -power);
			break;
		}
		return acting;
	}

	@Override
	public boolean isForceLocalCoords(World world, BlockPos pos, IBlockState state, double secondsToApply) {
		return true;
	}

	@Override
	public Vector getBlockForcePosition(World world, BlockPos pos, IBlockState state, Entity shipEntity,
			double secondsToApply) {
		return null;
	}
    
	//thebest108:
	//please excuse the following mess, i needed to add ALL OF THESE in order
	//to make the animation render correctly.
	
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isFullyOpaque(IBlockState state) {
		return false;
	}

	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

}