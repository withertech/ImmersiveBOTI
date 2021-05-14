package com.withertech.imm_ptl_peripheral.alternate_dimension;

import com.withertech.tim_wim_holes.Helper;
import net.minecraft.util.Tuple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomSelector<T>
{
	private final Object[] entries;
	private final int[] subWeightSum;
	private final int weightSum;

	public RandomSelector(List<Tuple<T, Integer>> data)
	{
		entries = data.stream().map(Tuple::getA).toArray();

		subWeightSum = Helper.mapReduce(
				data.stream(),
				(preSum, curr) -> preSum + curr.getB(),
				new Helper.SimpleBox<>(0)
		).mapToInt(i -> i).toArray();

		weightSum = subWeightSum[subWeightSum.length - 1];
	}

	public T select(Random random)
	{
		int randomValue = random.nextInt(weightSum);

		return selectByRandomValue(randomValue);
	}

	private T selectByRandomValue(int randomValue)
	{
		int result = Arrays.binarySearch(
				subWeightSum,
				0, subWeightSum.length,
				randomValue
		);

		if (result >= 0)
		{
			return (T) entries[result + 1];
		} else
		{
			//result = -firstEleGreaterThanValue - 1
			int firstEleGreaterThanValue = -(result + 1);
			return (T) entries[firstEleGreaterThanValue];
		}
	}

	public static class Builder<A>
	{
		private final ArrayList<Tuple<A, Integer>> data = new ArrayList<>();

		public Builder()
		{
		}

		public Builder<A> add(int weight, A element)
		{
			data.add(new Tuple<>(element, weight));
			return this;
		}

		public RandomSelector<A> build()
		{
			return new RandomSelector<>(data);
		}
	}

}
