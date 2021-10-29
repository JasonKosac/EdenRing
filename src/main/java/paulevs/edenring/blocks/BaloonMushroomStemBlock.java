package paulevs.edenring.blocks;

import com.google.common.collect.Maps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import paulevs.edenring.EdenRing;
import paulevs.edenring.blocks.EdenBlockProperties.BaloonMushroomStemState;
import paulevs.edenring.registries.EdenBlocks;
import ru.bclib.blocks.BaseBlockNotFull;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.render.BCLRenderLayer;
import ru.bclib.interfaces.RenderLayerProvider;

import java.util.Map;

public class BaloonMushroomStemBlock extends BaseBlockNotFull implements RenderLayerProvider {
	public static final EnumProperty<BaloonMushroomStemState> BALOON_MUSHROOM_STEM = EdenBlockProperties.BALOON_MUSHROOM_STEM;
	private static final Map<BaloonMushroomStemState, ResourceLocation> MODELS = Maps.newEnumMap(BaloonMushroomStemState.class);
	private static final Map<BaloonMushroomStemState, VoxelShape> SHAPES = Maps.newEnumMap(BaloonMushroomStemState.class);
	
	public BaloonMushroomStemBlock() {
		super(FabricBlockSettings.copyOf(Blocks.MUSHROOM_STEM));
	}
	
	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> stateManager) {
		stateManager.add(BALOON_MUSHROOM_STEM);
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext ePos) {
		VoxelShape shape = SHAPES.get(state.getValue(BALOON_MUSHROOM_STEM));
		return shape == null ? Shapes.block() : shape;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		BaloonMushroomStemState shape = blockState.getValue(BALOON_MUSHROOM_STEM);
		if (shape == BaloonMushroomStemState.UP || shape == BaloonMushroomStemState.NORTH_SOUTH || shape == BaloonMushroomStemState.EAST_WEST) {
			return SHAPES.get(shape);
		}
		return Shapes.empty();
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		BaloonMushroomStemState state = blockState.getValue(BALOON_MUSHROOM_STEM);
		ResourceLocation modelId = MODELS.get(state);
		if (modelId != null) {
			if (state == BaloonMushroomStemState.NORTH_SOUTH) {
				return ModelsHelper.createRotatedModel(modelId, Axis.Z);
			}
			if (state == BaloonMushroomStemState.EAST_WEST) {
				return ModelsHelper.createRotatedModel(modelId, Axis.X);
			}
			return ModelsHelper.createBlockSimple(modelId);
		}
		return super.getModelVariant(stateId, blockState, modelCache);
	}
	
	@Override
	@Environment(EnvType.CLIENT)
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return ModelsHelper.createBlockItem(MODELS.get(BaloonMushroomStemState.UP));
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		BaloonMushroomStemState stem = state.getValue(BALOON_MUSHROOM_STEM);
		if (stem == BaloonMushroomStemState.THIN || stem == BaloonMushroomStemState.THIN_TOP) {
			BlockPos sidePos = pos.below();
			BlockState sideState = world.getBlockState(sidePos);
			if (sideState.is(this) || sideState.isFaceSturdy(world, sidePos, Direction.UP)) {
				sidePos = pos.above();
				sideState = world.getBlockState(sidePos);
				if (sideState.is(this) || sideState.isFaceSturdy(world, sidePos, Direction.DOWN)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public BlockState updateShape(BlockState state, Direction facing, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
		if (!canSurvive(state, world, pos)) {
			if (world.getBlockState(pos.above()).is(EdenBlocks.BALOON_MUSHROOM_BLOCK)) {
				world.removeBlock(pos.above(), true);
			}
			return Blocks.AIR.defaultBlockState();
		}
		return state;
	}
	
	static {
		SHAPES.put(BaloonMushroomStemState.UP, Block.box(4, 0, 4, 12, 16, 12));
		SHAPES.put(BaloonMushroomStemState.NORTH_SOUTH, Block.box(4, 4, 0, 12, 12, 16));
		SHAPES.put(BaloonMushroomStemState.EAST_WEST, Block.box(0, 4, 4, 16, 12, 12));
		SHAPES.put(BaloonMushroomStemState.THIN, Block.box(7, 0, 7, 9, 16, 9));
		SHAPES.put(BaloonMushroomStemState.THIN_TOP, Block.box(2, 0, 2, 14, 16, 14));
		
		MODELS.put(BaloonMushroomStemState.UP, EdenRing.makeID("block/baloon_mushroom_stem"));
		MODELS.put(BaloonMushroomStemState.NORTH_SOUTH, EdenRing.makeID("block/baloon_mushroom_stem"));
		MODELS.put(BaloonMushroomStemState.EAST_WEST, EdenRing.makeID("block/baloon_mushroom_stem"));
		MODELS.put(BaloonMushroomStemState.THIN, EdenRing.makeID("block/baloon_mushroom_stem_thin"));
		MODELS.put(BaloonMushroomStemState.THIN_TOP, EdenRing.makeID("block/baloon_mushroom_stem_top"));
	}
	
	@Override
	public BCLRenderLayer getRenderLayer() {
		return BCLRenderLayer.CUTOUT;
	}
}
