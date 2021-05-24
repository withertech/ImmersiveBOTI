package com.withertech.tim_wim_holes_peripheral.alternate_dimension;

import net.minecraft.block.BlockState;
import net.minecraft.util.FastRandom;

import java.util.Arrays;
import java.util.Random;

class RegionErrorTerrainGenerator
{
	private final int regionX;
	private final int regionZ;
	private final double middle;
	private final double upMiddle;
	private final double downMiddle;
	Composition composition;
	private FormulaGenerator.TriNumFunction expression;

	public RegionErrorTerrainGenerator(
			int regionX_,
			int regionZ_,
			long seed
	)
	{
		regionX = regionX_;
		regionZ = regionZ_;

		initExpression(seed);

		middle = calcMiddle(0.4, 0.5);
		upMiddle = calcMiddle(1.0, 1.0);
		downMiddle = calcMiddle(0.0, 0);
	}

	private void initExpression(long seed)
	{
		long realSeed = FastRandom.mix(
				seed, FastRandom.mix(
						regionX, regionZ
				)
		);
		Random random = new Random(realSeed);
		expression = FormulaGenerator.getRandomTriCompositeExpression(random);

		composition = ErrorTerrainComposition.selector.select(random);
	}

	private double calcMiddle(double lowerHeight, double upperHeight)
	{
		double[] arr = new double[8];
		double zero = 0.2;
		double one = 0.8;
		arr[0] = expression.eval(zero, lowerHeight, zero);
		arr[1] = expression.eval(zero, lowerHeight, one);
		arr[2] = expression.eval(zero, upperHeight, zero);
		arr[3] = expression.eval(zero, upperHeight, one);
		arr[4] = expression.eval(one, lowerHeight, zero);
		arr[5] = expression.eval(one, lowerHeight, one);
		arr[6] = expression.eval(one, upperHeight, zero);
		arr[7] = expression.eval(one, upperHeight, one);
		Arrays.sort(arr);

		return arr[4];
	}

	private double calc(int worldX, int worldY, int worldZ)
	{
		int a = ErrorTerrainGenerator.regionChunkNum * 16;

		int regionStartX = regionX * a;
		int regionStartZ = regionZ * a;
		return expression.eval(
				(worldX - regionStartX) / ((double) a),
				worldY / ((double) ErrorTerrainGenerator.maxY),
				(worldZ - regionStartZ) / ((double) a)
		);
	}

	public BlockState getBlockComposition(
			int worldX,
			int worldY,
			int worldZ
	)
	{
		if (worldY >= ErrorTerrainGenerator.maxY)
		{
			return ErrorTerrainComposition.air;
		}

		double currValue = calc(worldX, worldY, worldZ);

		return composition.generate(
				worldY, currValue,
				middle, upMiddle, downMiddle,
				worldX, worldZ
		);
	}

	public interface Composition
	{
		BlockState generate(
				int worldY,
				double funcValue,
				double middle,
				double upMiddle,
				double downMiddle,
				int worldX,
				int worldZ
		);
	}

}
