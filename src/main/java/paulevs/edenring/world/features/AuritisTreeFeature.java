package paulevs.edenring.world.features;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Material;
import paulevs.edenring.registries.EdenBlocks;
import ru.bclib.blocks.BlockProperties;
import ru.bclib.blocks.BlockProperties.TripleShape;
import ru.bclib.complexmaterials.WoodenComplexMaterial;
import ru.bclib.util.BlocksHelper;
import ru.bclib.util.MHelper;
import ru.bclib.world.features.DefaultFeature;

import java.util.Random;

public class AuritisTreeFeature extends DefaultFeature {
	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
		WorldGenLevel level = featurePlaceContext.level();
		BlockPos center = featurePlaceContext.origin();
		Random random = featurePlaceContext.random();
		
		Block below = level.getBlockState(center.below()).getBlock();
		if (!(below instanceof GrassBlock) && below != Blocks.DIRT) {
			return false;
		}
		
		BlockState log = EdenBlocks.AURITIS_MATERIAL.getBlock(WoodenComplexMaterial.BLOCK_LOG).defaultBlockState();
		BlockState bark = EdenBlocks.AURITIS_MATERIAL.getBlock(WoodenComplexMaterial.BLOCK_BARK).defaultBlockState();
		BlockState leaves = EdenBlocks.AURITIS_LEAVES.defaultBlockState();
		BlockState moss = EdenBlocks.EDEN_MOSS.defaultBlockState();
		boolean natural = featurePlaceContext.config() != null;
		
		boolean hasMax = false;
		MutableBlockPos pos = center.mutable();
		for (Direction dir: BlocksHelper.HORIZONTAL) {
			pos.set(center).move(dir);
			int h = hasMax ? random.nextInt(2) : random.nextInt(3);
			if (!hasMax) {
				hasMax = h == 2;
			}
			for (int i = -1; i <= h; i++) {
				pos.setY(center.getY() + i);
				if (canReplace(level.getBlockState(pos))) {
					BlocksHelper.setWithoutUpdate(level, pos, i == h ? bark : log);
					if (i == h && random.nextInt(4) > 0) {
						pos.setY(pos.getY() + 1);
						if (canReplace(level.getBlockState(pos))) {
							BlocksHelper.setWithoutUpdate(level, pos, moss);
						}
					}
				}
			}
		}
		
		pos.set(center);
		BlocksHelper.setWithoutUpdate(level, pos, log);
		int h = MHelper.randRange(7, 10, random);
		for (int i = 1; i < h; i++) {
			pos.setY(center.getY() + i);
			if (canReplace(level.getBlockState(pos))) {
				BlocksHelper.setWithoutUpdate(level, pos, log);
			}
			else {
				return true;
			}
		}
		
		int count = MHelper.randRange(3, 5, random);
		float angle = (float) Math.PI * 2 * random.nextFloat();
		float deltaAngle = (float) Math.PI * 2 / count;
		
		makeCanopy(level, pos, leaves, natural, random);
		
		for (int i = 0; i < count; i++) {
			float delta = MHelper.randRange(0.3F, 0.6F, random);
			float dx = (float) Math.sin(angle) * delta;
			float dz = (float) Math.cos(angle) * delta;
			angle += deltaAngle;
			int length = (int) MHelper.randRange(h * 0.3F, h * 0.7F, random);
			if (length == 0) {
				break;
			}
			pos.setY(center.getY() + h - length - random.nextInt(3));
			makeBranch(level, pos, dx, dz, length, bark, leaves, moss, natural, random);
		}
		
		return true;
	}
	
	private void makeBranch(WorldGenLevel level, BlockPos pos, float dx, float dz, int length, BlockState bark, BlockState leaves, BlockState moss, boolean natural, Random random) {
		MutableBlockPos mut = pos.mutable();
		float px = 0.5F;
		float pz = 0.5F;
		int maxMoss = length >> 1;
		for (int i = 0; i < length; i++) {
			px += dx;
			pz += dz;
			mut.setX(Mth.floor((double) pos.getX() + px));
			mut.setZ(Mth.floor((double) pos.getZ() + pz));
			mut.setY(pos.getY() + i);
			if (canReplace(level.getBlockState(mut))) {
				BlocksHelper.setWithoutUpdate(level, mut, bark);
				if (natural && i < maxMoss && random.nextBoolean()) {
					mut.setY(mut.getY() + 1);
					if (level.getBlockState(mut).isAir()) {
						BlocksHelper.setWithoutUpdate(level, mut, moss);
					}
				}
			}
		}
		makeCanopy(level, mut, leaves, natural, random);
	}
	
	private void makeCanopy(WorldGenLevel level, BlockPos pos, BlockState leaves, boolean generateVines, Random random) {
		makeCircle(level, pos, leaves, 2, 0, generateVines, random);
		makeCircle(level, pos.above(), leaves, 1, 1, false, random);
	}
	
	private void makeCircle(WorldGenLevel level, BlockPos pos, BlockState leaves, int radius, int offset, boolean generateVines, Random random) {
		BlockState bottom = EdenBlocks.EDEN_VINE.defaultBlockState().setValue(BlockProperties.TRIPLE_SHAPE, TripleShape.BOTTOM);
		BlockState middle = EdenBlocks.EDEN_VINE.defaultBlockState().setValue(BlockProperties.TRIPLE_SHAPE, TripleShape.MIDDLE);
		BlockState top = EdenBlocks.EDEN_VINE.defaultBlockState().setValue(BlockProperties.TRIPLE_SHAPE, TripleShape.TOP);
		
		MutableBlockPos mut = pos.mutable();
		for (int x = -radius; x <= radius; x++) {
			int ax = Mth.abs(x);
			mut.setX(pos.getX() + x);
			for (int z = -radius; z <= radius; z++) {
				int az = Mth.abs(z);
				mut.setZ(pos.getZ() + z);
				if (ax != radius || az != radius || ax != az) {
					BlockState state = level.getBlockState(mut);
					if (state.isAir() || state.is(EdenBlocks.EDEN_MOSS)) {
						int distance = ax + az + offset;
						if (distance == 0) {
							continue;
						}
						BlocksHelper.setWithoutUpdate(level, mut, leaves.setValue(BlockStateProperties.DISTANCE, distance));
						if (generateVines && random.nextInt(16) == 0) {
							int h = MHelper.randRange(3, 6, random);
							for (int i = 1; i <= h; i++) {
								mut.setY(pos.getY() - i);
								if (!level.getBlockState(mut).isAir()) {
									if (i == 1) {
										break;
									}
									mut.setY(mut.getY() + 1);
									BlocksHelper.setWithoutUpdate(level, mut, bottom);
									break;
								}
								BlocksHelper.setWithoutUpdate(level, mut, i == 1 ? top : i == h ? bottom : middle);
							}
							mut.setY(pos.getY());
						}
					}
				}
			}
		}
	}
	
	private boolean canReplace(BlockState state) {
		return state.isAir() || state.is(BlockTags.LEAVES) || state.getMaterial().isReplaceable() || state.getMaterial().equals(Material.PLANT);
	}
}
